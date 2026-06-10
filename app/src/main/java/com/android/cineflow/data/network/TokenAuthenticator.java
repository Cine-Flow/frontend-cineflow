package com.android.cineflow.data.network;

import android.util.Log;

import com.android.cineflow.data.auth.AuthManager;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.LoginResponseDto;
import com.android.cineflow.data.network.dto.TokenRefreshRequestDto;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {
    private static final String TAG = "TokenAuthenticator";

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        // Stop retrying if we've already tried and failed to authenticate this request.
        if (responseCount(response) >= 2) {
            Log.w(TAG, "Response count >= 2, stopping re-authentication attempt.");
            handleLogout();
            return null;
        }

        AuthManager authManager = AuthManager.getInstance();
        if (authManager == null || !authManager.isLoggedIn()) {
            return null;
        }

        String refreshToken = authManager.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            Log.w(TAG, "No refresh token found. Logging out.");
            handleLogout();
            return null;
        }

        Log.d(TAG, "Access token expired. Attempting to refresh using refresh token: " + refreshToken);

        // Make a synchronous call to refresh the token.
        // We use a new ApiClient or create the service to avoid cyclic dependency during OkHttpClient creation.
        // But since we can use ApiClient.getFilmApiService(), let's make sure it doesn't cause loop.
        // Wait, ApiClient has a static FilmApiService. It uses the client which has this Authenticator.
        // If we call ApiClient.getFilmApiService().refreshToken(...) it will use the client. But refreshToken endpoint is public (permitAll)
        // so it won't trigger 401 even if token is expired. But it's safer to make a separate Retrofit instance or just make the call.
        // Let's create a temporary basic client specifically for token refresh to avoid any potential deadlock/interceptor issues.
        retrofit2.Retrofit tempRetrofit = new retrofit2.Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/api/v1/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build();
        FilmApiService tempService = tempRetrofit.create(FilmApiService.class);

        try {
            retrofit2.Response<ApiResponseDto<LoginResponseDto>> tokenResponse = tempService
                    .refreshToken(new TokenRefreshRequestDto(refreshToken))
                    .execute();

            if (tokenResponse.isSuccessful() && tokenResponse.body() != null && tokenResponse.body().getData() != null) {
                LoginResponseDto data = tokenResponse.body().getData();
                Log.d(TAG, "Token refresh successful. Saving new tokens.");

                // Save new tokens
                authManager.updateTokens(data.getAccessToken(), data.getRefreshToken());

                // Retry original request with new access token
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + data.getAccessToken())
                        .build();
            } else {
                Log.e(TAG, "Token refresh failed with code: " + tokenResponse.code() + ". Logging out.");
                handleLogout();
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during token refresh: " + e.getMessage());
            handleLogout();
            return null;
        }
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    private void handleLogout() {
        AuthManager authManager = AuthManager.getInstance();
        if (authManager != null) {
            authManager.clearSession();
        }
    }
}

package com.android.cineflow.data.network;

import com.android.cineflow.data.auth.AuthManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        AuthManager auth = AuthManager.getInstance();
        if (auth != null) {
            String token = auth.getToken();
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
        }
        Response response = chain.proceed(builder.build());
        if (response.code() == 401) {
            AuthManager authManager = AuthManager.getInstance();
            if (authManager != null && authManager.isLoggedIn()) {
                authManager.clearSession();
            }
        }
        return response;
    }
}

package com.android.cineflow.data.network;

import android.content.Context;

import com.android.cineflow.data.auth.AuthManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/api/v1/";

    private static FilmApiService filmApiService;

    public static FilmApiService getFilmApiService() {
        if (filmApiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            AuthInterceptor authInterceptor = new AuthInterceptor();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .authenticator(new TokenAuthenticator())
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            filmApiService = retrofit.create(FilmApiService.class);
        }
        return filmApiService;
    }

    public static void reset() {
        filmApiService = null;
    }
}

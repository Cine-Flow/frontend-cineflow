package com.android.cineflow.data.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // 10.0.2.2 is the emulator alias for the host machine's localhost.
    // For a real device on the same network, replace with your machine's LAN IP.
    private static final String BASE_URL = "http://10.0.2.2:8080/api/v1/";

    private static FilmApiService filmApiService;

    public static FilmApiService getFilmApiService() {
        if (filmApiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
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
}

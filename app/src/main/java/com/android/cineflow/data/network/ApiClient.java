package com.android.cineflow.data.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApiClient {

    public static final String BASE_URL = "http://10.0.2.2:8080/api/v1/";

    private static FilmApiService filmApiService;
    private static FilmApiService publicFilmApiService;
    private static RequestQueue requestQueue;
    private static RequestQueue publicRequestQueue;
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static FilmApiService getFilmApiService() {
        if (filmApiService == null) {
            if (appContext == null) {
                throw new IllegalStateException("ApiClient has not been initialized with Context. Call ApiClient.init(Context) first.");
            }

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            AuthInterceptor authInterceptor = new AuthInterceptor();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .authenticator(new TokenAuthenticator())
                    .build();

            requestQueue = Volley.newRequestQueue(appContext, new OkHttp3Stack(client));
            filmApiService = new VolleyFilmApiServiceImpl(requestQueue, BASE_URL, client);
        }
        return filmApiService;
    }

    public static FilmApiService getPublicFilmApiService() {
        if (publicFilmApiService == null) {
            if (appContext == null) {
                throw new IllegalStateException("ApiClient has not been initialized with Context. Call ApiClient.init(Context) first.");
            }

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            publicRequestQueue = Volley.newRequestQueue(appContext, new OkHttp3Stack(client));
            publicFilmApiService = new VolleyFilmApiServiceImpl(publicRequestQueue, BASE_URL, client);
        }
        return publicFilmApiService;
    }

    public static void reset() {
        filmApiService = null;
        publicFilmApiService = null;
        if (requestQueue != null) {
            requestQueue.stop();
            requestQueue = null;
        }
        if (publicRequestQueue != null) {
            publicRequestQueue.stop();
            publicRequestQueue = null;
        }
    }
}

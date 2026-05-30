package com.android.cineflow.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.cineflow.data.model.Movie;
import com.android.cineflow.data.model.SportEvent;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.FilmDto;
import com.android.cineflow.data.network.dto.HomeFilmsDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import com.android.cineflow.data.local.AppDatabase;
import com.android.cineflow.data.local.dao.HomeDao;
import com.android.cineflow.data.local.entity.HomeCacheEntity;
import com.google.gson.Gson;
import java.util.concurrent.Executors;

public class HomeRepository {

    private static final String TAG = "HomeRepository";

    // Singleton — one instance shared across the app lifecycle
    private static HomeRepository instance;

    private final MutableLiveData<List<Movie>>      bannerMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>>      newReleases  = new MutableLiveData<>();
    private final MutableLiveData<List<SportEvent>> sportEvents  = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>>      hotSeries    = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>>      dailyMovies  = new MutableLiveData<>();

    // UI state: loading & error
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String>  error   = new MutableLiveData<>(null);

    private HomeDao homeDao;
    private final Gson gson = new Gson();
    private boolean isInitialized = false;

    public static HomeRepository getInstance() {
        if (instance == null) instance = new HomeRepository();
        return instance;
    }

    private HomeRepository() {
    }

    public void init(Context context) {
        if (!isInitialized) {
            homeDao = AppDatabase.getInstance(context).homeDao();
            isInitialized = true;
            fetchHomeData();
        }
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public LiveData<List<Movie>>      getBannerMovies() { return bannerMovies; }
    public LiveData<List<Movie>>      getNewReleases()  { return newReleases; }
    public LiveData<List<SportEvent>> getSportEvents()  { return sportEvents; }
    public LiveData<List<Movie>>      getHotSeries()    { return hotSeries; }
    public LiveData<List<Movie>>      getDailyMovies()  { return dailyMovies; }
    public LiveData<Boolean>          isLoading()       { return loading; }
    public LiveData<String>           getError()        { return error; }

    /** Re-fetch home data from the network (used by pull-to-refresh). */
    public void refresh() {
        fetchHomeData();
    }

    // ── Network ──────────────────────────────────────────────────────────────

    private void fetchHomeData() {
        loading.postValue(true);
        error.postValue(null);

        // First, load from cache
        Executors.newSingleThreadExecutor().execute(() -> {
            if (homeDao != null) {
                HomeCacheEntity cache = homeDao.getHomeCache();
                if (cache != null && cache.jsonData != null) {
                    try {
                        HomeFilmsDto cachedData = gson.fromJson(cache.jsonData, HomeFilmsDto.class);
                        if (cachedData != null) {
                            postData(cachedData);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing cache", e);
                    }
                }
            }
            
            // Then fetch from network
            ApiClient.getFilmApiService().getHomeFilms()
                    .enqueue(new Callback<ApiResponseDto<HomeFilmsDto>>() {
                        @Override
                        public void onResponse(Call<ApiResponseDto<HomeFilmsDto>> call,
                                               Response<ApiResponseDto<HomeFilmsDto>> response) {
                            loading.postValue(false);
                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().getData() != null) {
                                HomeFilmsDto data = response.body().getData();
                                postData(data);
                                
                                // Save to cache
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    if (homeDao != null) {
                                        String json = gson.toJson(data);
                                        homeDao.insertHomeCache(new HomeCacheEntity(json));
                                    }
                                });
                            } else {
                                Log.w(TAG, "Home API failed: HTTP " + response.code());
                                error.postValue("Lỗi tải dữ liệu (HTTP " + response.code() + ")");
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponseDto<HomeFilmsDto>> call, Throwable t) {
                            loading.postValue(false);
                            Log.e(TAG, "Home API call failed", t);
                            error.postValue("Không thể kết nối đến máy chủ");
                        }
                    });
        });
    }

    private void postData(HomeFilmsDto data) {
        bannerMovies.postValue(toMovies(data.getBanners()));
        newReleases.postValue(toMovies(data.getNewReleases()));
        sportEvents.postValue(toSportEvents(data.getSportEvents()));
        hotSeries.postValue(toMovies(data.getHotSeries()));
        dailyMovies.postValue(toMovies(data.getDailyMovies()));
    }

    // ── Mapping helpers ──────────────────────────────────────────────────────

    private List<Movie> toMovies(List<FilmDto> films) {
        List<Movie> result = new ArrayList<>();
        for (FilmDto f : films) {
            boolean isNew = f.getReleaseYear() >= 2025;
            boolean is4K  = f.getIsPremium();
            result.add(new Movie(
                    String.valueOf(f.getId()),
                    f.getTitle(),
                    f.getThumbnailUrl(),
                    f.getType(),
                    f.getReleaseYear(),
                    isNew,
                    is4K));
        }
        return result;
    }

    private List<SportEvent> toSportEvents(List<FilmDto> films) {
        List<SportEvent> result = new ArrayList<>();
        for (FilmDto f : films) {
            result.add(new SportEvent(
                    String.valueOf(f.getId()),
                    f.getTitle(),
                    f.getThumbnailUrl(),
                    "",    // league — not in current schema
                    true,  // all LIVE-type films are live
                    "",    // broadcastTime — not in current schema
                    f.getTrailerUrl()));
        }
        return result;
    }
}

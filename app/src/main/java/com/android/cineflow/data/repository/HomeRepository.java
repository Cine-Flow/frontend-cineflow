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

public class HomeRepository {

    private static final String TAG = "HomeRepository";

    // Singleton — one instance shared across the app lifecycle
    private static HomeRepository instance;

    private final MutableLiveData<List<Movie>>      bannerMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>>      newReleases  = new MutableLiveData<>();
    private final MutableLiveData<List<SportEvent>> sportEvents  = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>>      hotSeries    = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>>      dailyMovies  = new MutableLiveData<>();

    public static HomeRepository getInstance() {
        if (instance == null) instance = new HomeRepository();
        return instance;
    }

    private HomeRepository() {
        fetchHomeData();
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public LiveData<List<Movie>>      getBannerMovies() { return bannerMovies; }
    public LiveData<List<Movie>>      getNewReleases()  { return newReleases; }
    public LiveData<List<SportEvent>> getSportEvents()  { return sportEvents; }
    public LiveData<List<Movie>>      getHotSeries()    { return hotSeries; }
    public LiveData<List<Movie>>      getDailyMovies()  { return dailyMovies; }

    // ── Network ──────────────────────────────────────────────────────────────

    private void fetchHomeData() {
        ApiClient.getFilmApiService().getHomeFilms()
                .enqueue(new Callback<ApiResponseDto<HomeFilmsDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<HomeFilmsDto>> call,
                                           Response<ApiResponseDto<HomeFilmsDto>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            HomeFilmsDto data = response.body().getData();
                            bannerMovies.postValue(toMovies(data.getBanners()));
                            newReleases.postValue(toMovies(data.getNewReleases()));
                            sportEvents.postValue(toSportEvents(data.getSportEvents()));
                            hotSeries.postValue(toMovies(data.getHotSeries()));
                            dailyMovies.postValue(toMovies(data.getDailyMovies()));
                        } else {
                            Log.w(TAG, "Home API failed: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<HomeFilmsDto>> call, Throwable t) {
                        Log.e(TAG, "Home API call failed", t);
                    }
                });
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
                    ""));  // broadcastTime — not in current schema
        }
        return result;
    }
}

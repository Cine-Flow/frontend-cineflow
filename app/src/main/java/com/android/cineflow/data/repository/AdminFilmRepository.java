package com.android.cineflow.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.FilmApiService;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.CreateFilmRequestDto;
import com.android.cineflow.data.network.dto.FilmDetailDto;
import com.android.cineflow.data.network.dto.PagedResponseDto;
import com.android.cineflow.data.network.dto.UpdateFilmRequestDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFilmRepository {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static AdminFilmRepository instance;
    private final FilmApiService api;

    private final MutableLiveData<List<FilmDetailDto>> films = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<FilmDetailDto> selectedFilm = new MutableLiveData<>();

    private int currentPage = 0;
    private int totalPages = 0;
    private long totalElements = 0;
    private String currentSearch = null;
    private boolean hasMore = true;

    public static synchronized AdminFilmRepository getInstance() {
        if (instance == null) instance = new AdminFilmRepository();
        return instance;
    }

    private AdminFilmRepository() {
        api = ApiClient.getFilmApiService();
    }

    public LiveData<List<FilmDetailDto>> getFilms() { return films; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<FilmDetailDto> getSelectedFilm() { return selectedFilm; }

    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return totalPages; }
    public long getTotalElements() { return totalElements; }
    public boolean hasMore() { return hasMore; }

    public void fetchFilms(int page, int size, String search) {
        loading.postValue(true);
        api.getAllFilms(page, size, search).enqueue(new Callback<ApiResponseDto<PagedResponseDto<FilmDetailDto>>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<PagedResponseDto<FilmDetailDto>>> call,
                                   Response<ApiResponseDto<PagedResponseDto<FilmDetailDto>>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    PagedResponseDto<FilmDetailDto> paged = response.body().getData();
                    currentPage = paged.getPageNumber();
                    totalPages = paged.getTotalPages();
                    totalElements = paged.getTotalElements();
                    hasMore = !paged.isLast();
                    currentSearch = search;
                    films.postValue(paged.getContent());
                } else {
                    error.postValue("Failed to load films: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<PagedResponseDto<FilmDetailDto>>> call, Throwable t) {
                loading.postValue(false);
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void fetchFirstPage(String search) {
        fetchFilms(0, DEFAULT_PAGE_SIZE, search);
    }

    public void fetchNextPage() {
        if (hasMore) {
            fetchFilms(currentPage + 1, DEFAULT_PAGE_SIZE, currentSearch);
        }
    }

    public void fetchPrevPage() {
        if (currentPage > 0) {
            fetchFilms(currentPage - 1, DEFAULT_PAGE_SIZE, currentSearch);
        }
    }

    public void fetchFilmById(int id) {
        loading.postValue(true);
        api.getAdminFilmById(id).enqueue(new Callback<ApiResponseDto<FilmDetailDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<FilmDetailDto>> call,
                                   Response<ApiResponseDto<FilmDetailDto>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    selectedFilm.postValue(response.body().getData());
                } else {
                    error.postValue("Failed to load film: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<FilmDetailDto>> call, Throwable t) {
                loading.postValue(false);
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void createFilm(CreateFilmRequestDto request, OnResultListener listener) {
        loading.postValue(true);
        api.createFilm(request).enqueue(new Callback<ApiResponseDto<FilmDetailDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<FilmDetailDto>> call,
                                   Response<ApiResponseDto<FilmDetailDto>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listener.onSuccess(response.body().getData());
                    fetchFirstPage(currentSearch);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "HTTP " + response.code();
                    listener.onError("Create failed: " + msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<FilmDetailDto>> call, Throwable t) {
                loading.postValue(false);
                listener.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateFilm(int id, UpdateFilmRequestDto request, OnResultListener listener) {
        loading.postValue(true);
        api.updateFilm(id, request).enqueue(new Callback<ApiResponseDto<FilmDetailDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<FilmDetailDto>> call,
                                   Response<ApiResponseDto<FilmDetailDto>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listener.onSuccess(response.body().getData());
                    fetchFirstPage(currentSearch);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "HTTP " + response.code();
                    listener.onError("Update failed: " + msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<FilmDetailDto>> call, Throwable t) {
                loading.postValue(false);
                listener.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteFilm(int id, OnResultListener listener) {
        loading.postValue(true);
        api.deleteFilm(id).enqueue(new Callback<ApiResponseDto<Void>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<Void>> call,
                                   Response<ApiResponseDto<Void>> response) {
                loading.postValue(false);
                if (response.isSuccessful()) {
                    listener.onSuccess(null);
                    fetchFirstPage(currentSearch);
                } else {
                    listener.onError("Delete failed: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                loading.postValue(false);
                listener.onError("Network error: " + t.getMessage());
            }
        });
    }

    public interface OnResultListener {
        void onSuccess(FilmDetailDto film);
        void onError(String message);
    }
}

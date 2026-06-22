package com.android.cineflow.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.FilmApiService;
import com.android.cineflow.data.network.Response;
import com.android.cineflow.data.network.dto.AdminCategoryDto;
import com.android.cineflow.data.network.dto.AdminCategoryRequestDto;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.PagedResponseDto;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryRepository {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static AdminCategoryRepository instance;
    private final FilmApiService api;

    private final MutableLiveData<List<AdminCategoryDto>> categories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private int currentPage = 0;
    private int totalPages = 0;
    private long totalElements = 0;
    private String currentSearch = null;
    private boolean hasMore = true;

    public static synchronized AdminCategoryRepository getInstance() {
        if (instance == null) instance = new AdminCategoryRepository();
        return instance;
    }

    private AdminCategoryRepository() {
        api = ApiClient.getFilmApiService();
    }

    public LiveData<List<AdminCategoryDto>> getCategories() { return categories; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return totalPages; }
    public long getTotalElements() { return totalElements; }
    public boolean hasMore() { return hasMore; }

    public void fetchCategories(int page, int size, String search) {
        loading.postValue(true);
        api.getAdminCategories(page, size, search).enqueue(new Callback<ApiResponseDto<PagedResponseDto<AdminCategoryDto>>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<PagedResponseDto<AdminCategoryDto>>> call,
                                   Response<ApiResponseDto<PagedResponseDto<AdminCategoryDto>>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    PagedResponseDto<AdminCategoryDto> paged = response.body().getData();
                    currentPage = paged.getPageNumber();
                    totalPages = paged.getTotalPages();
                    totalElements = paged.getTotalElements();
                    hasMore = !paged.isLast();
                    currentSearch = search;
                    categories.postValue(paged.getContent());
                } else {
                    error.postValue("Failed to load categories: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<PagedResponseDto<AdminCategoryDto>>> call, Throwable t) {
                loading.postValue(false);
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void fetchFirstPage(String search) {
        fetchCategories(0, DEFAULT_PAGE_SIZE, search);
    }

    public void fetchNextPage() {
        if (hasMore) {
            fetchCategories(currentPage + 1, DEFAULT_PAGE_SIZE, currentSearch);
        }
    }

    public void fetchPrevPage() {
        if (currentPage > 0) {
            fetchCategories(currentPage - 1, DEFAULT_PAGE_SIZE, currentSearch);
        }
    }

    public void createCategory(AdminCategoryRequestDto request, OnResultListener listener) {
        loading.postValue(true);
        api.createCategory(request).enqueue(new Callback<ApiResponseDto<AdminCategoryDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<AdminCategoryDto>> call,
                                   Response<ApiResponseDto<AdminCategoryDto>> response) {
                loading.postValue(false);
                if (response.isSuccessful()) {
                    listener.onSuccess();
                    fetchFirstPage(currentSearch);
                } else {
                    listener.onError("Create failed: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<AdminCategoryDto>> call, Throwable t) {
                loading.postValue(false);
                listener.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateCategory(Integer id, AdminCategoryRequestDto request, OnResultListener listener) {
        loading.postValue(true);
        api.updateCategory(id, request).enqueue(new Callback<ApiResponseDto<AdminCategoryDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<AdminCategoryDto>> call,
                                   Response<ApiResponseDto<AdminCategoryDto>> response) {
                loading.postValue(false);
                if (response.isSuccessful()) {
                    listener.onSuccess();
                    fetchFirstPage(currentSearch);
                } else {
                    listener.onError("Update failed: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<AdminCategoryDto>> call, Throwable t) {
                loading.postValue(false);
                listener.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteCategory(Integer id, OnResultListener listener) {
        loading.postValue(true);
        api.deleteCategory(id).enqueue(new Callback<ApiResponseDto<Void>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<Void>> call, Response<ApiResponseDto<Void>> response) {
                loading.postValue(false);
                if (response.isSuccessful()) {
                    listener.onSuccess();
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
        void onSuccess();
        void onError(String message);
    }
}

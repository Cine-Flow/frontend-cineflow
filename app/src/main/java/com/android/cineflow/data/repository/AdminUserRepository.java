package com.android.cineflow.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.FilmApiService;
import com.android.cineflow.data.network.Response;
import com.android.cineflow.data.network.dto.AdminUserDto;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.PagedResponseDto;

import java.util.ArrayList;
import java.util.List;

public class AdminUserRepository {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static AdminUserRepository instance;
    private final FilmApiService api;

    private final MutableLiveData<List<AdminUserDto>> users = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private int currentPage = 0;
    private int totalPages = 0;
    private long totalElements = 0;
    private String currentSearch = null;
    private boolean hasMore = true;

    public static synchronized AdminUserRepository getInstance() {
        if (instance == null) instance = new AdminUserRepository();
        return instance;
    }

    private AdminUserRepository() {
        api = ApiClient.getFilmApiService();
    }

    public LiveData<List<AdminUserDto>> getUsers() { return users; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return totalPages; }
    public long getTotalElements() { return totalElements; }
    public boolean hasMore() { return hasMore; }

    public void fetchUsers(int page, int size, String search) {
        loading.postValue(true);
        api.getAdminUsers(page, size, search).enqueue(new Callback<ApiResponseDto<PagedResponseDto<AdminUserDto>>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<PagedResponseDto<AdminUserDto>>> call,
                                   Response<ApiResponseDto<PagedResponseDto<AdminUserDto>>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    PagedResponseDto<AdminUserDto> paged = response.body().getData();
                    currentPage = paged.getPageNumber();
                    totalPages = paged.getTotalPages();
                    totalElements = paged.getTotalElements();
                    hasMore = !paged.isLast();
                    currentSearch = search;
                    users.postValue(paged.getContent());
                } else {
                    error.postValue("Failed to load users: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<PagedResponseDto<AdminUserDto>>> call, Throwable t) {
                loading.postValue(false);
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void fetchFirstPage(String search) {
        fetchUsers(0, DEFAULT_PAGE_SIZE, search);
    }

    public void fetchNextPage() {
        if (hasMore) {
            fetchUsers(currentPage + 1, DEFAULT_PAGE_SIZE, currentSearch);
        }
    }

    public void fetchPrevPage() {
        if (currentPage > 0) {
            fetchUsers(currentPage - 1, DEFAULT_PAGE_SIZE, currentSearch);
        }
    }

    public void deleteUser(String id, OnResultListener listener) {
        loading.postValue(true);
        api.deleteUser(id).enqueue(new Callback<ApiResponseDto<Void>>() {
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

    public void updateUser(String id, com.android.cineflow.data.network.dto.AdminUserRequestDto request, OnResultListener listener) {
        loading.postValue(true);
        api.updateUser(id, request).enqueue(new Callback<ApiResponseDto<AdminUserDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<AdminUserDto>> call, Response<ApiResponseDto<AdminUserDto>> response) {
                loading.postValue(false);
                if (response.isSuccessful()) {
                    listener.onSuccess();
                    fetchFirstPage(currentSearch);
                } else {
                    listener.onError("Update failed: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<AdminUserDto>> call, Throwable t) {
                loading.postValue(false);
                listener.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void resetUserPassword(String id, OnResultListener listener) {
        api.resetUserPassword(id).enqueue(new Callback<ApiResponseDto<Void>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<Void>> call, Response<ApiResponseDto<Void>> response) {
                listener.onSuccess();
            }

            @Override
            public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                listener.onError("Network error: " + t.getMessage());
            }
        });
    }

    public interface OnResultListener {
        void onSuccess();
        void onError(String message);
    }
}

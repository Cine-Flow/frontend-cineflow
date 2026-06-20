package com.android.cineflow.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.cineflow.data.model.ShortVideo;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.ShortsDto;
import com.android.cineflow.data.network.dto.ShortsResponseDto;

import java.util.ArrayList;
import java.util.List;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

import com.android.cineflow.data.network.dto.CommentDto;

public class ShortsRepository {

    private static final String TAG = "ShortsRepository";

    private static ShortsRepository instance;

    private final MutableLiveData<List<ShortVideo>> shortVideos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public static ShortsRepository getInstance() {
        if (instance == null) instance = new ShortsRepository();
        return instance;
    }

    private ShortsRepository() {}

    // ── Public API ───────────────────────────────────────────────────────────

    public LiveData<List<ShortVideo>> getShortVideos() { return shortVideos; }
    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    /** Fetch shorts from the backend API. */
    public void fetchShorts() {
        loading.postValue(true);
        error.postValue(null);

        ApiClient.getFilmApiService().getShorts()
                .enqueue(new Callback<ApiResponseDto<ShortsResponseDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<ShortsResponseDto>> call,
                                           Response<ApiResponseDto<ShortsResponseDto>> response) {
                        loading.postValue(false);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            ShortsResponseDto data = response.body().getData();
                            List<ShortVideo> list = toShortVideos(data.getShorts());
                            shortVideos.postValue(list);
                        } else {
                            Log.w(TAG, "Shorts API failed: HTTP " + response.code());
                            error.postValue("Lỗi tải Shorts (HTTP " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<ShortsResponseDto>> call, Throwable t) {
                        loading.postValue(false);
                        Log.e(TAG, "Shorts API call failed", t);
                        error.postValue("Không thể kết nối đến máy chủ");
                    }
                });
    }

    /**
     * Append more shorts to the existing list (for pagination).
     * Since the backend currently returns all shorts in one call,
     * this method re-fetches and appends any new items.
     */
    public void loadMore() {
        if (Boolean.TRUE.equals(loading.getValue())) return;

        loading.postValue(true);
        error.postValue(null);

        ApiClient.getFilmApiService().getShorts()
                .enqueue(new Callback<ApiResponseDto<ShortsResponseDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<ShortsResponseDto>> call,
                                           Response<ApiResponseDto<ShortsResponseDto>> response) {
                        loading.postValue(false);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            ShortsResponseDto data = response.body().getData();
                            List<ShortVideo> newItems = toShortVideos(data.getShorts());

                            // Append to existing list
                            List<ShortVideo> current = shortVideos.getValue();
                            if (current == null) current = new ArrayList<>();
                            List<ShortVideo> combined = new ArrayList<>(current);
                            combined.addAll(newItems);
                            shortVideos.postValue(combined);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<ShortsResponseDto>> call, Throwable t) {
                        loading.postValue(false);
                        Log.e(TAG, "Load more failed", t);
                    }
                });
    }

    public void likeShort(String id, Callback<ApiResponseDto<Void>> callback) {
        ApiClient.getFilmApiService().likeShort(id).enqueue(callback);
    }

    public void unlikeShort(String id, Callback<ApiResponseDto<Void>> callback) {
        ApiClient.getFilmApiService().unlikeShort(id).enqueue(callback);
    }

    public void getShortComments(String id, Callback<ApiResponseDto<List<CommentDto>>> callback) {
        ApiClient.getFilmApiService().getShortComments(id).enqueue(callback);
    }

    public void postShortComment(String id, String content, Callback<ApiResponseDto<CommentDto>> callback) {
        ApiClient.getFilmApiService().postShortComment(id, new com.android.cineflow.data.network.dto.CreateCommentRequestDto(content))
                .enqueue(callback);
    }

    // ── Mapping helpers ──────────────────────────────────────────────────────

    private List<ShortVideo> toShortVideos(List<ShortsDto> dtoList) {
        List<ShortVideo> result = new ArrayList<>();
        if (dtoList == null) return result;
        for (ShortsDto dto : dtoList) {
            ShortVideo sv = new ShortVideo(
                    String.valueOf(dto.getId()),
                    dto.getVideoUrl(),
                    dto.getTitle(),
                    dto.getUploader(),
                    dto.getThumbnailUrl(),
                    dto.getDescription(),
                    dto.getViewCount());
            sv.setLikeCount(dto.getLikeCount());
            sv.setLiked(dto.getLiked());
            result.add(sv);
        }
        return result;
    }
}


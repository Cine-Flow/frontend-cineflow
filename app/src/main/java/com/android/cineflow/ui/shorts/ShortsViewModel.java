package com.android.cineflow.ui.shorts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.cineflow.data.model.ShortVideo;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.CommentDto;
import com.android.cineflow.data.repository.ShortsRepository;

import java.util.List;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

public class ShortsViewModel extends ViewModel {

    private final ShortsRepository repository;

    public LiveData<List<ShortVideo>> shortVideos;
    public LiveData<Boolean> isLoading;
    public LiveData<String> error;

    public ShortsViewModel() {
        repository = ShortsRepository.getInstance();
        shortVideos = repository.getShortVideos();
        isLoading = repository.isLoading();
        error = repository.getError();

        // Fetch shorts from backend on init
        repository.fetchShorts();
    }

    /** Reload shorts from backend. */
    public void refresh() {
        repository.fetchShorts();
    }

    /** Load more shorts (pagination). */
    public void loadMore() {
        repository.loadMore();
    }

    public void toggleLike(ShortVideo video, boolean isLiked) {
        if (isLiked) {
            repository.likeShort(video.getId(), new Callback<ApiResponseDto<Void>>() {
                @Override public void onResponse(Call<ApiResponseDto<Void>> call, Response<ApiResponseDto<Void>> response) {}
                @Override public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {}
            });
        } else {
            repository.unlikeShort(video.getId(), new Callback<ApiResponseDto<Void>>() {
                @Override public void onResponse(Call<ApiResponseDto<Void>> call, Response<ApiResponseDto<Void>> response) {}
                @Override public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {}
            });
        }
    }

    public LiveData<List<CommentDto>> getComments(String videoId) {
        MutableLiveData<List<CommentDto>> comments = new MutableLiveData<>();
        repository.getShortComments(videoId, new Callback<ApiResponseDto<List<CommentDto>>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<List<CommentDto>>> call, Response<ApiResponseDto<List<CommentDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    comments.postValue(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<List<CommentDto>>> call, Throwable t) {
                // Return empty list or handle error
                comments.postValue(null);
            }
        });
        return comments;
    }

    public LiveData<CommentDto> postComment(String videoId, String content) {
        MutableLiveData<CommentDto> newComment = new MutableLiveData<>();
        repository.postShortComment(videoId, content, new Callback<ApiResponseDto<CommentDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<CommentDto>> call, Response<ApiResponseDto<CommentDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    newComment.postValue(response.body().getData());
                } else {
                    newComment.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<CommentDto>> call, Throwable t) {
                newComment.postValue(null);
            }
        });
        return newComment;
    }
}


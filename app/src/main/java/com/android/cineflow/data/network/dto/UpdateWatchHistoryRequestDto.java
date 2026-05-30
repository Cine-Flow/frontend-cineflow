package com.android.cineflow.data.network.dto;

public class UpdateWatchHistoryRequestDto {
    private final int resumePositionSeconds;

    public UpdateWatchHistoryRequestDto(int resumePositionSeconds) {
        this.resumePositionSeconds = resumePositionSeconds;
    }
}

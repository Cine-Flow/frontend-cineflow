package com.android.cineflow.data.network.dto;

import com.google.gson.annotations.SerializedName;

public class UserAnalyticsDto {
    @SerializedName("totalWatchTimeMinutes")
    private Integer totalWatchTimeMinutes;

    @SerializedName("totalEpisodesWatched")
    private Integer totalEpisodesWatched;

    @SerializedName("averageWatchTimePerDay")
    private Integer averageWatchTimePerDay;

    @SerializedName("actionPercent")
    private Integer actionPercent;

    @SerializedName("animationPercent")
    private Integer animationPercent;

    @SerializedName("romancePercent")
    private Integer romancePercent;

    public Integer getTotalWatchTimeMinutes() {
        return totalWatchTimeMinutes != null ? totalWatchTimeMinutes : 0;
    }

    public Integer getTotalEpisodesWatched() {
        return totalEpisodesWatched != null ? totalEpisodesWatched : 0;
    }

    public Integer getAverageWatchTimePerDay() {
        return averageWatchTimePerDay != null ? averageWatchTimePerDay : 0;
    }

    public Integer getActionPercent() {
        return actionPercent != null ? actionPercent : 0;
    }

    public Integer getAnimationPercent() {
        return animationPercent != null ? animationPercent : 0;
    }

    public Integer getRomancePercent() {
        return romancePercent != null ? romancePercent : 0;
    }
}

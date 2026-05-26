package com.android.cineflow.data.network.dto;

import com.google.gson.annotations.SerializedName;

public class EpisodeDto {
    private Integer id;
    @SerializedName("episodeNumber")
    private Integer episodeNumber;
    private String title;
    @SerializedName("videoUrl")
    private String videoUrl;
    private Integer duration;
    @SerializedName("viewCount")
    private Integer viewCount;

    public Integer getId() { return id; }
    public Integer getEpisodeNumber() { return episodeNumber; }
    public String getTitle() { return title; }
    public String getVideoUrl() { return videoUrl; }
    public Integer getDuration() { return duration; }
    public Integer getViewCount() { return viewCount; }
}

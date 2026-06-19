package com.android.cineflow.data.network.dto;

public class CreateEpisodeRequestDto {
    private Integer episodeNumber;
    private String title;
    private String videoUrl;
    private Integer duration;

    public CreateEpisodeRequestDto() {}

    public CreateEpisodeRequestDto(Integer episodeNumber, String title, String videoUrl, Integer duration) {
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.videoUrl = videoUrl;
        this.duration = duration;
    }

    public Integer getEpisodeNumber() { return episodeNumber; }
    public void setEpisodeNumber(Integer episodeNumber) { this.episodeNumber = episodeNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}

package com.android.cineflow.data.network.dto;

public class FootballContentDto {
    private Integer id;
    private String title;
    private String thumbnailUrl;
    private String videoUrl;
    private String contentType;
    private String badge;
    private String publishedAt;

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getVideoUrl() { return videoUrl; }
    public String getContentType() { return contentType; }
    public String getBadge() { return badge; }
    public String getPublishedAt() { return publishedAt; }
}

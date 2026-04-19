package com.android.cineflow.data.model;

public class ShortVideo {
    private String id;
    private String videoUrl;
    private String title;
    private String uploader;
    private String avatarUrl;

    public ShortVideo(String id, String videoUrl, String title, String uploader, String avatarUrl) {
        this.id = id;
        this.videoUrl = videoUrl;
        this.title = title;
        this.uploader = uploader;
        this.avatarUrl = avatarUrl;
    }

    public String getId() { return id; }
    public String getVideoUrl() { return videoUrl; }
    public String getTitle() { return title; }
    public String getUploader() { return uploader; }
    public String getAvatarUrl() { return avatarUrl; }
}

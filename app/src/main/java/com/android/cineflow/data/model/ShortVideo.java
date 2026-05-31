package com.android.cineflow.data.model;

public class ShortVideo {
    private String id;
    private String videoUrl;
    private String title;
    private String uploader;
    private String avatarUrl;
    private String description;
    private int viewCount;
    private int likeCount;
    private boolean isLiked;

    public ShortVideo(String id, String videoUrl, String title, String uploader, String avatarUrl) {
        this.id = id;
        this.videoUrl = videoUrl;
        this.title = title;
        this.uploader = uploader;
        this.avatarUrl = avatarUrl;
        this.description = "";
        this.viewCount = 0;
        this.likeCount = 0;
        this.isLiked = false;
    }

    public ShortVideo(String id, String videoUrl, String title, String uploader,
                      String avatarUrl, String description, int viewCount) {
        this.id = id;
        this.videoUrl = videoUrl;
        this.title = title;
        this.uploader = uploader;
        this.avatarUrl = avatarUrl;
        this.description = description;
        this.viewCount = viewCount;
        this.likeCount = viewCount; // Use viewCount as initial like count
        this.isLiked = false;
    }

    public String getId() { return id; }
    public String getVideoUrl() { return videoUrl; }
    public String getTitle() { return title; }
    public String getUploader() { return uploader; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getDescription() { return description; }
    public int getViewCount() { return viewCount; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }
}

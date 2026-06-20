package com.android.cineflow.data.network.dto;

import com.google.gson.annotations.SerializedName;

public class ShortsDto {
    private Integer id;
    private String title;
    private String thumbnailUrl;
    private String videoUrl;
    private String description;
    private String uploader;
    private Integer duration;
    private Integer viewCount;
    private Integer likeCount;
    private Boolean liked;

    public Integer getId() { return id; }
    public String getTitle() { return title != null ? title : ""; }
    public String getThumbnailUrl() { return thumbnailUrl != null ? thumbnailUrl : ""; }
    public String getVideoUrl() { return videoUrl != null ? videoUrl : ""; }
    public String getDescription() { return description != null ? description : ""; }
    public String getUploader() { return uploader != null ? uploader : ""; }
    public Integer getDuration() { return duration != null ? duration : 0; }
    public Integer getViewCount() { return viewCount != null ? viewCount : 0; }
    public Integer getLikeCount() { return likeCount != null ? likeCount : 0; }
    public Boolean getLiked() { return liked != null ? liked : false; }
}


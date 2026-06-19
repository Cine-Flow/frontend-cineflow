package com.android.cineflow.data.network.dto;

import com.google.gson.annotations.SerializedName;

public class FilmCommentDto {
    private Integer id;

    @SerializedName("filmId")
    private Integer filmId;

    @SerializedName("userId")
    private String userId;

    private String username;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    private String content;

    @SerializedName("createdAt")
    private String createdAt;

    public Integer getId() { return id; }
    public Integer getFilmId() { return filmId; }
    public String getUserId() { return userId != null ? userId : ""; }
    public String getUsername() { return username != null ? username : ""; }
    public String getAvatarUrl() { return avatarUrl != null ? avatarUrl : ""; }
    public String getContent() { return content != null ? content : ""; }
    public String getCreatedAt() { return createdAt != null ? createdAt : ""; }
}

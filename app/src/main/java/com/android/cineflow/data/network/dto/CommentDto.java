package com.android.cineflow.data.network.dto;

import com.google.gson.annotations.SerializedName;

public class CommentDto {
    private String id;
    private String content;

    @SerializedName("username")
    private String authorName;

    @SerializedName("avatarUrl")
    private String authorAvatar;


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }
}

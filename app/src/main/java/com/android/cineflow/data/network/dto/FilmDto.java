package com.android.cineflow.data.network.dto;

import com.google.gson.annotations.SerializedName;

public class FilmDto {
    private Integer id;
    private String title;

    @SerializedName("thumbnailUrl")
    private String thumbnailUrl;

    @SerializedName("releaseYear")
    private Integer releaseYear;

    @SerializedName("isPremium")
    private Boolean isPremium;

    private String type; // "SINGLE", "SERIES", "LIVE"

    public Integer getId() { return id; }
    public String getTitle() { return title != null ? title : ""; }
    public String getThumbnailUrl() { return thumbnailUrl != null ? thumbnailUrl : ""; }
    public int getReleaseYear() { return releaseYear != null ? releaseYear : 0; }
    public boolean getIsPremium() { return isPremium != null && isPremium; }
    public String getType() { return type != null ? type : ""; }
}

package com.android.cineflow.data.network.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateFilmRequestDto {
    private String title;
    private String description;
    private String thumbnailUrl;
    private String trailerUrl;
    private Integer releaseYear;
    private Boolean isPremium;
    private String type;

    public UpdateFilmRequestDto() {}

    public UpdateFilmRequestDto(String title, String description, String thumbnailUrl,
                                String trailerUrl, Integer releaseYear, Boolean isPremium, String type) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.trailerUrl = trailerUrl;
        this.releaseYear = releaseYear;
        this.isPremium = isPremium;
        this.type = type;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public Boolean getIsPremium() { return isPremium; }
    public void setIsPremium(Boolean isPremium) { this.isPremium = isPremium; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

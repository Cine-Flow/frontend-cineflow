package com.android.cineflow.data.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FilmDetailDto {
    private Integer id;
    private String title;
    private String description;

    @SerializedName("thumbnailUrl")
    private String thumbnailUrl;
    
    @SerializedName("trailerUrl")
    private String trailerUrl;

    @SerializedName("releaseYear")
    private Integer releaseYear;

    @SerializedName("isPremium")
    private Boolean isPremium;

    private String type; // "SINGLE", "SERIES", "LIVE"
    
    private List<EpisodeDto> episodes;

    public Integer getId() { return id; }
    public String getTitle() { return title != null ? title : ""; }
    public String getDescription() { return description != null ? description : ""; }
    public String getThumbnailUrl() { return thumbnailUrl != null ? thumbnailUrl : ""; }
    public String getTrailerUrl() { return trailerUrl != null ? trailerUrl : ""; }
    public int getReleaseYear() { return releaseYear != null ? releaseYear : 0; }
    public boolean getIsPremium() { return isPremium != null && isPremium; }
    public String getType() { return type != null ? type : ""; }
    public List<EpisodeDto> getEpisodes() { return episodes; }
}

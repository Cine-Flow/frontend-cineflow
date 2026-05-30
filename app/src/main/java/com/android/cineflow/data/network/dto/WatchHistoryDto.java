package com.android.cineflow.data.network.dto;

public class WatchHistoryDto {
    private Integer id;
    private FilmDto film;
    private EpisodeDto episode;
    private Integer resumePositionSeconds;

    public Integer getId() { return id; }
    public FilmDto getFilm() { return film; }
    public EpisodeDto getEpisode() { return episode; }
    public int getResumePositionSeconds() { return resumePositionSeconds != null ? resumePositionSeconds : 0; }
}

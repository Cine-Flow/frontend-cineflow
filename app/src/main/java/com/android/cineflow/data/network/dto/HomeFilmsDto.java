package com.android.cineflow.data.network.dto;

import java.util.Collections;
import java.util.List;

public class HomeFilmsDto {
    private List<FilmDto> banners;
    private List<FilmDto> newReleases;
    private List<FilmDto> sportEvents;
    private List<FilmDto> hotSeries;
    private List<FilmDto> dailyMovies;

    public List<FilmDto> getBanners()      { return orEmpty(banners); }
    public List<FilmDto> getNewReleases()  { return orEmpty(newReleases); }
    public List<FilmDto> getSportEvents()  { return orEmpty(sportEvents); }
    public List<FilmDto> getHotSeries()    { return orEmpty(hotSeries); }
    public List<FilmDto> getDailyMovies()  { return orEmpty(dailyMovies); }

    private List<FilmDto> orEmpty(List<FilmDto> list) {
        return list != null ? list : Collections.emptyList();
    }
}

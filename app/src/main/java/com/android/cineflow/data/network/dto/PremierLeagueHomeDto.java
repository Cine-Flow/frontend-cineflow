package com.android.cineflow.data.network.dto;

import java.util.Collections;
import java.util.List;

public class PremierLeagueHomeDto {
    private List<FootballContentDto> banners;
    private List<FootballContentDto> highlights;
    private List<FootballMatchDto> schedule;
    private List<FootballMatchDto> results;
    private List<FootballStandingDto> standings;
    private List<FootballContentDto> news;

    public List<FootballContentDto> getBanners() { return orEmpty(banners); }
    public List<FootballContentDto> getHighlights() { return orEmpty(highlights); }
    public List<FootballMatchDto> getSchedule() { return orEmpty(schedule); }
    public List<FootballMatchDto> getResults() { return orEmpty(results); }
    public List<FootballStandingDto> getStandings() { return orEmpty(standings); }
    public List<FootballContentDto> getNews() { return orEmpty(news); }

    private <T> List<T> orEmpty(List<T> items) {
        return items != null ? items : Collections.emptyList();
    }
}

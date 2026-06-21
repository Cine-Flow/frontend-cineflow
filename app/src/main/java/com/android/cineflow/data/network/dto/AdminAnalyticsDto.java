package com.android.cineflow.data.network.dto;

import java.util.Collections;
import java.util.List;

public class AdminAnalyticsDto {
    private Integer period;

    private Long totalUsers;
    private Long newSignups;
    private Long activeUsers;
    private Long episodeViews;
    private Long watchSessions;
    private Long totalFavorites;
    private Long totalComments;

    private List<TimePoint> dailySignups;
    private List<TimePoint> dailyWatchSessions;

    private List<MetricSlice> filmTypes;
    private List<MetricSlice> premiumFreeMix;
    private List<MetricBar> topCategories;
    private List<MetricBar> topFilms;
    private List<MetricBar> topFavoritedFilms;
    private List<MetricBar> topCommentedFilms;
    private List<MetricBar> topEpisodes;
    private Long filmsWithZeroViews;

    public int getPeriod() { return period != null ? period : 30; }
    public long getTotalUsers() { return totalUsers != null ? totalUsers : 0L; }
    public long getNewSignups() { return newSignups != null ? newSignups : 0L; }
    public long getActiveUsers() { return activeUsers != null ? activeUsers : 0L; }
    public long getEpisodeViews() { return episodeViews != null ? episodeViews : 0L; }
    public long getWatchSessions() { return watchSessions != null ? watchSessions : 0L; }
    public long getTotalFavorites() { return totalFavorites != null ? totalFavorites : 0L; }
    public long getTotalComments() { return totalComments != null ? totalComments : 0L; }
    public long getFilmsWithZeroViews() { return filmsWithZeroViews != null ? filmsWithZeroViews : 0L; }

    public List<TimePoint> getDailySignups() { return dailySignups != null ? dailySignups : Collections.emptyList(); }
    public List<TimePoint> getDailyWatchSessions() { return dailyWatchSessions != null ? dailyWatchSessions : Collections.emptyList(); }
    public List<MetricSlice> getFilmTypes() { return filmTypes != null ? filmTypes : Collections.emptyList(); }
    public List<MetricSlice> getPremiumFreeMix() { return premiumFreeMix != null ? premiumFreeMix : Collections.emptyList(); }
    public List<MetricBar> getTopCategories() { return topCategories != null ? topCategories : Collections.emptyList(); }
    public List<MetricBar> getTopFilms() { return topFilms != null ? topFilms : Collections.emptyList(); }
    public List<MetricBar> getTopFavoritedFilms() { return topFavoritedFilms != null ? topFavoritedFilms : Collections.emptyList(); }
    public List<MetricBar> getTopCommentedFilms() { return topCommentedFilms != null ? topCommentedFilms : Collections.emptyList(); }
    public List<MetricBar> getTopEpisodes() { return topEpisodes != null ? topEpisodes : Collections.emptyList(); }

    public static class MetricSlice {
        private String label;
        private Long value;
        public String getLabel() { return label != null ? label : ""; }
        public long getValue() { return value != null ? value : 0L; }
    }

    public static class MetricBar {
        private String label;
        private Long value;
        public String getLabel() { return label != null ? label : ""; }
        public long getValue() { return value != null ? value : 0L; }
    }

    public static class TimePoint {
        private String date;
        private Long value;
        public String getDate() { return date != null ? date : ""; }
        public long getValue() { return value != null ? value : 0L; }
    }
}

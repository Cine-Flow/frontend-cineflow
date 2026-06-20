package com.android.cineflow.data.network.dto;

import java.math.BigDecimal;
import java.util.List;

public class AdminAnalyticsDto {
    private Integer period;
    private Long totalUsers;
    private Long newSignups;
    private Long episodeViews;
    private Long watchSessions;
    private Long premiumUsers;
    private BigDecimal revenue;
    private List<MetricSlice> filmTypes;
    private List<MetricSlice> subscriptions;
    private List<MetricBar> topCategories;
    private List<MetricBar> topFilms;

    public int getPeriod() { return period != null ? period : 30; }
    public long getTotalUsers() { return totalUsers != null ? totalUsers : 0L; }
    public long getNewSignups() { return newSignups != null ? newSignups : 0L; }
    public long getEpisodeViews() { return episodeViews != null ? episodeViews : 0L; }
    public long getWatchSessions() { return watchSessions != null ? watchSessions : 0L; }
    public long getPremiumUsers() { return premiumUsers != null ? premiumUsers : 0L; }
    public BigDecimal getRevenue() { return revenue != null ? revenue : BigDecimal.ZERO; }
    public List<MetricSlice> getFilmTypes() { return filmTypes; }
    public List<MetricSlice> getSubscriptions() { return subscriptions; }
    public List<MetricBar> getTopCategories() { return topCategories; }
    public List<MetricBar> getTopFilms() { return topFilms; }

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
}

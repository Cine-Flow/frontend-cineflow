package com.android.cineflow.data.network.dto;

public class FootballMatchDto {
    private Integer id;
    private FootballTeamDto homeTeam;
    private FootballTeamDto awayTeam;
    private String kickoffAt;
    private String round;
    private String status;
    private Integer homeScore;
    private Integer awayScore;
    private String bannerUrl;
    private String highlightUrl;

    public Integer getId() { return id; }
    public FootballTeamDto getHomeTeam() { return homeTeam; }
    public FootballTeamDto getAwayTeam() { return awayTeam; }
    public String getKickoffAt() { return kickoffAt; }
    public String getRound() { return round; }
    public String getStatus() { return status; }
    public Integer getHomeScore() { return homeScore; }
    public Integer getAwayScore() { return awayScore; }
    public String getBannerUrl() { return bannerUrl; }
    public String getHighlightUrl() { return highlightUrl; }
}

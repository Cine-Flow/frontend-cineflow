package com.android.cineflow.data.network.dto;

public class FootballStandingDto {
    private FootballTeamDto team;
    private String season;
    private Integer rank;
    private Integer played;
    private Integer won;
    private Integer drawn;
    private Integer lost;
    private Integer goalDifference;
    private Integer points;

    public FootballTeamDto getTeam() { return team; }
    public String getSeason() { return season; }
    public Integer getRank() { return rank; }
    public Integer getPlayed() { return played; }
    public Integer getWon() { return won; }
    public Integer getDrawn() { return drawn; }
    public Integer getLost() { return lost; }
    public Integer getGoalDifference() { return goalDifference; }
    public Integer getPoints() { return points; }
}

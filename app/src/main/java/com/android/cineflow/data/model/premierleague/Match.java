package com.android.cineflow.data.model.premierleague;

public class Match {
    private final String id;
    private final String homeTeamCode; // e.g., "WHU"
    private final String homeTeamLogo;
    private final String awayTeamCode; // e.g., "WOL"
    private final String awayTeamLogo;
    private final String time;         // e.g., "02:00"
    private final String date;         // e.g., "11/04/2026"
    private final String round;        // e.g., "Vòng 32"
    private final boolean isLive;
    private final String homeScore;    // For results
    private final String awayScore;    // For results

    public Match(String id, String homeTeamCode, String homeTeamLogo, String awayTeamCode, String awayTeamLogo, String time, String date, String round, boolean isLive, String homeScore, String awayScore) {
        this.id = id;
        this.homeTeamCode = homeTeamCode;
        this.homeTeamLogo = homeTeamLogo;
        this.awayTeamCode = awayTeamCode;
        this.awayTeamLogo = awayTeamLogo;
        this.time = time;
        this.date = date;
        this.round = round;
        this.isLive = isLive;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public String getId() { return id; }
    public String getHomeTeamCode() { return homeTeamCode; }
    public String getHomeTeamLogo() { return homeTeamLogo; }
    public String getAwayTeamCode() { return awayTeamCode; }
    public String getAwayTeamLogo() { return awayTeamLogo; }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public String getRound() { return round; }
    public boolean isLive() { return isLive; }
    public String getHomeScore() { return homeScore; }
    public String getAwayScore() { return awayScore; }
}

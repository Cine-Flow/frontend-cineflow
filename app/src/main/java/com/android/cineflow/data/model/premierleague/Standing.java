package com.android.cineflow.data.model.premierleague;

public class Standing {
    private final int rank;
    private final String teamCode; // e.g., "ARS"
    private final String teamLogo;
    private final int played;
    private final int won;
    private final int drawn;
    private final int lost;
    private final int goalDifference;
    private final int points;

    public Standing(int rank, String teamCode, String teamLogo, int played, int won, int drawn, int lost, int goalDifference, int points) {
        this.rank = rank;
        this.teamCode = teamCode;
        this.teamLogo = teamLogo;
        this.played = played;
        this.won = won;
        this.drawn = drawn;
        this.lost = lost;
        this.goalDifference = goalDifference;
        this.points = points;
    }

    public int getRank() { return rank; }
    public String getTeamCode() { return teamCode; }
    public String getTeamLogo() { return teamLogo; }
    public int getPlayed() { return played; }
    public int getWon() { return won; }
    public int getDrawn() { return drawn; }
    public int getLost() { return lost; }
    public int getGoalDifference() { return goalDifference; }
    public int getPoints() { return points; }
}

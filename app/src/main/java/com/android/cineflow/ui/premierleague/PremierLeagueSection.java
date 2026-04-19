package com.android.cineflow.ui.premierleague;

import com.android.cineflow.data.common.uimodel.ContentCard;
import com.android.cineflow.data.model.premierleague.Match;
import com.android.cineflow.data.model.premierleague.Standing;
import java.util.List;

public class PremierLeagueSection {
    public static final int TYPE_BANNER = 0;
    public static final int TYPE_HORIZONTAL_LIST = 1;
    public static final int TYPE_MATCH_SCHEDULE = 2;
    public static final int TYPE_STANDINGS = 3;

    private final int type;
    private final String title;
    private final List<ContentCard> cards;
    private final List<Match> matches;
    private final List<Standing> standings;

    // For Banners and Horizontal Lists
    public PremierLeagueSection(int type, String title, List<ContentCard> cards) {
        this.type = type;
        this.title = title;
        this.cards = cards;
        this.matches = null;
        this.standings = null;
    }

    // For Match Schedule / Results
    public PremierLeagueSection(int type, String title, List<Match> matches, boolean isMatchList) {
        this.type = type;
        this.title = title;
        this.cards = null;
        this.matches = matches;
        this.standings = null;
    }

    // For Standings
    public PremierLeagueSection(int type, String title, List<Standing> standings, boolean isStandingList, boolean unused) {
        this.type = type;
        this.title = title;
        this.cards = null;
        this.matches = null;
        this.standings = standings;
    }

    public int getType() { return type; }
    public String getTitle() { return title; }
    public List<ContentCard> getCards() { return cards; }
    public List<Match> getMatches() { return matches; }
    public List<Standing> getStandings() { return standings; }
}

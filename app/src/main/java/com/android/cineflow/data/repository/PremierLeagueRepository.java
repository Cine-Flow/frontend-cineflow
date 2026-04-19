package com.android.cineflow.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.cineflow.data.common.uimodel.ContentCard;
import com.android.cineflow.data.model.premierleague.Match;
import com.android.cineflow.data.model.premierleague.Standing;
import com.android.cineflow.ui.premierleague.PremierLeagueSection;

import java.util.ArrayList;
import java.util.List;

public class PremierLeagueRepository {

    private static PremierLeagueRepository instance;
    private final MutableLiveData<List<PremierLeagueSection>> sections = new MutableLiveData<>();

    public static PremierLeagueRepository getInstance() {
        if (instance == null) instance = new PremierLeagueRepository();
        return instance;
    }

    private PremierLeagueRepository() {
        fetchData();
    }

    public LiveData<List<PremierLeagueSection>> getSections() {
        return sections;
    }

    private void fetchData() {
        List<PremierLeagueSection> data = new ArrayList<>();

        // 1. Banner Section
        List<ContentCard> banners = new ArrayList<>();
        banners.add(new ContentCard("1", "Leeds vs Brentford", "https://example.com/banner1.jpg", "LIVE", ContentCard.STYLE_BANNER));
        data.add(new PremierLeagueSection(PremierLeagueSection.TYPE_BANNER, "", banners));

        // 2. Highlights
        List<ContentCard> highlights = new ArrayList<>();
        highlights.add(new ContentCard("h1", "Aston Villa vs West Ham", "https://example.com/h1.jpg", "MỚI", ContentCard.STYLE_LANDSCAPE));
        highlights.add(new ContentCard("h2", "Arsenal vs Liverpool", "https://example.com/h2.jpg", "HOT", ContentCard.STYLE_LANDSCAPE));
        data.add(new PremierLeagueSection(PremierLeagueSection.TYPE_HORIZONTAL_LIST, "Highlights", highlights));

        // 3. Match Schedule (Lịch đấu)
        List<Match> schedule = new ArrayList<>();
        schedule.add(new Match("m1", "WHU", "", "WOL", "", "02:00", "11/04/2026", "Vòng 32", false, "", ""));
        schedule.add(new Match("m2", "ARS", "", "BOU", "", "18:30", "11/04/2026", "Vòng 32", false, "", ""));
        schedule.add(new Match("m3", "BRE", "", "EVE", "", "21:00", "11/04/2026", "Vòng 32", false, "", ""));
        schedule.add(new Match("m4", "BUR", "", "BHA", "", "21:00", "11/04/2026", "Vòng 32", false, "", ""));
        schedule.add(new Match("m5", "LIV", "", "FUL", "", "23:30", "11/04/2026", "Vòng 32", false, "", ""));
        data.add(new PremierLeagueSection(PremierLeagueSection.TYPE_MATCH_SCHEDULE, "Thứ 7 - 11/04/2026", schedule, true));

        // 4. Results (Kết quả) - using Match model but with scores
        List<Match> results = new ArrayList<>();
        results.add(new Match("r1", "AVL", "", "WHU", "", "", "22/03/2026", "Vòng 31", false, "2", "0"));
        results.add(new Match("r2", "TOT", "", "NFO", "", "", "22/03/2026", "Vòng 31", false, "0", "3"));
        results.add(new Match("r3", "NEW", "", "SUN", "", "", "22/03/2026", "Vòng 31", false, "1", "2"));
        results.add(new Match("r4", "LEE", "", "BRE", "", "", "22/03/2026", "Vòng 31", false, "0", "0"));
        results.add(new Match("r5", "EVE", "", "CHE", "", "", "22/03/2026", "Vòng 31", false, "3", "0"));
        // We reuse TYPE_MATCH_SCHEDULE for list layout, logic in adapter will handle score display
        data.add(new PremierLeagueSection(PremierLeagueSection.TYPE_MATCH_SCHEDULE, "Vòng 31 - Hôm qua 22/03/2026", results, true));


        // 5. Standings (Bảng xếp hạng)
        List<Standing> standings = new ArrayList<>();
        standings.add(new Standing(1, "ARS", "", 31, 21, 7, 3, 39, 70));
        standings.add(new Standing(2, "MCI", "", 30, 18, 7, 5, 32, 61));
        standings.add(new Standing(3, "MUN", "", 31, 15, 10, 6, 13, 55));
        standings.add(new Standing(4, "AVL", "", 31, 16, 6, 9, 5, 54));
        standings.add(new Standing(5, "LIV", "", 31, 14, 7, 10, 8, 49));
        standings.add(new Standing(6, "CHE", "", 31, 13, 9, 9, 15, 48));

        data.add(new PremierLeagueSection(PremierLeagueSection.TYPE_STANDINGS, "Premier League 2025/26", standings, true, true));

        // 6. English Football (Bóng đá Anh)
        List<ContentCard> news = new ArrayList<>();
        news.add(new ContentCard("n1", "Tin nóng Ngoại hạng", "https://example.com/n1.jpg", "MỚI", ContentCard.STYLE_LANDSCAPE));
        news.add(new ContentCard("n2", "Review Vòng 31", "https://example.com/n2.jpg", "", ContentCard.STYLE_LANDSCAPE));
        data.add(new PremierLeagueSection(PremierLeagueSection.TYPE_HORIZONTAL_LIST, "Bóng đá Anh", news));

        sections.postValue(data);
    }
}

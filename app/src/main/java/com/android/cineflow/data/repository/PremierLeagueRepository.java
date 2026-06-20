package com.android.cineflow.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.cineflow.data.common.uimodel.ContentCard;
import com.android.cineflow.data.model.premierleague.Match;
import com.android.cineflow.data.model.premierleague.Standing;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.FootballContentDto;
import com.android.cineflow.data.network.dto.FootballMatchDto;
import com.android.cineflow.data.network.dto.FootballStandingDto;
import com.android.cineflow.data.network.dto.FootballTeamDto;
import com.android.cineflow.data.network.dto.PremierLeagueHomeDto;
import com.android.cineflow.ui.premierleague.PremierLeagueSection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

public class PremierLeagueRepository {

    private static final String TAG = "PremierLeagueRepo";
    private static final DateTimeFormatter INPUT_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static PremierLeagueRepository instance;

    private final MutableLiveData<List<PremierLeagueSection>> sections = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private String selectedScheduleDate = null;
    private String selectedResultsDate = null;
    private long lastFetchTime = 0;
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes cache

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

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchData() {
        selectedScheduleDate = null;
        selectedResultsDate = null;
        loading.setValue(true);
        errorMessage.setValue(null);

        ApiClient.getFilmApiService().getPremierLeagueHome()
                .enqueue(new Callback<ApiResponseDto<PremierLeagueHomeDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<PremierLeagueHomeDto>> call,
                                           Response<ApiResponseDto<PremierLeagueHomeDto>> response) {
                        loading.setValue(false);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            lastFetchTime = System.currentTimeMillis();
                            sections.setValue(toSections(response.body().getData()));
                            return;
                        }

                        String message = response.body() != null
                                ? response.body().getMessage()
                                : "Không thể tải dữ liệu Ngoại hạng Anh";
                        errorMessage.setValue(message);
                        Log.w(TAG, "Premier League API failed: HTTP " + response.code());
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<PremierLeagueHomeDto>> call, Throwable t) {
                        loading.setValue(false);
                        errorMessage.setValue("Không thể kết nối đến máy chủ");
                        Log.e(TAG, "Premier League API call failed", t);
                    }
                });
    }

    public void fetchDataIfNeeded() {
        if (loading.getValue() != null && loading.getValue()) {
            return;
        }
        long now = System.currentTimeMillis();
        List<PremierLeagueSection> current = sections.getValue();
        if (current == null || current.isEmpty() || (now - lastFetchTime > CACHE_DURATION_MS)) {
            fetchData();
        }
    }

    public void expandSection(String mode) {
        loading.setValue(true);
        errorMessage.setValue(null);

        if (PremierLeagueSection.MODE_STANDINGS.equals(mode)) {
            ApiClient.getFilmApiService().getPremierLeagueStandings()
                    .enqueue(new Callback<ApiResponseDto<List<FootballStandingDto>>>() {
                        @Override
                        public void onResponse(Call<ApiResponseDto<List<FootballStandingDto>>> call,
                                               Response<ApiResponseDto<List<FootballStandingDto>>> response) {
                            loading.setValue(false);
                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().getData() != null) {
                                replaceStandingsSection(toStandings(response.body().getData()));
                                return;
                            }
                            errorMessage.setValue("Unable to load Premier League standings");
                        }

                        @Override
                        public void onFailure(Call<ApiResponseDto<List<FootballStandingDto>>> call, Throwable t) {
                            loading.setValue(false);
                            errorMessage.setValue("Unable to connect to the server");
                            Log.e(TAG, "Premier League standings API call failed", t);
                        }
                    });
            return;
        }

        String apiDate = PremierLeagueSection.MODE_FINISHED.equals(mode) ? selectedResultsDate : selectedScheduleDate;
        String displayDate = null;
        if (apiDate != null) {
            try {
                String[] parts = apiDate.split("-");
                if (parts.length == 3) {
                    displayDate = parts[2] + "/" + parts[1] + "/" + parts[0];
                }
            } catch (Exception ignored) {}
        }
        loadMatches(mode, apiDate, displayDate);
    }

    public void loadFixturesForDate(String apiDate, String displayDate) {
        loadMatchesForDate(PremierLeagueSection.MODE_UPCOMING, apiDate, displayDate);
    }

    public void loadMatchesForDate(String mode, String apiDate, String displayDate) {
        if (PremierLeagueSection.MODE_FINISHED.equals(mode)) {
            selectedResultsDate = apiDate;
        } else {
            selectedScheduleDate = apiDate;
        }
        loading.setValue(true);
        errorMessage.setValue(null);
        loadMatches(mode, apiDate, displayDate);
    }

    private void loadMatches(String mode, String apiDate, String displayDate) {
        ApiClient.getFilmApiService().getPremierLeagueMatches(mode, apiDate)
                .enqueue(new Callback<ApiResponseDto<List<FootballMatchDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<List<FootballMatchDto>>> call,
                                           Response<ApiResponseDto<List<FootballMatchDto>>> response) {
                        loading.setValue(false);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            replaceMatchSection(mode, toMatches(response.body().getData()), displayDate);
                            return;
                        }
                        errorMessage.setValue("Unable to load Premier League matches");
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<List<FootballMatchDto>>> call, Throwable t) {
                        loading.setValue(false);
                        errorMessage.setValue("Unable to connect to the server");
                        Log.e(TAG, "Premier League matches API call failed", t);
                    }
                });
    }

    private List<PremierLeagueSection> toSections(PremierLeagueHomeDto data) {
        List<PremierLeagueSection> result = new ArrayList<>();
        addCardsSection(result, PremierLeagueSection.TYPE_BANNER, "", data.getBanners(), ContentCard.STYLE_BANNER);
        addCardsSection(result, PremierLeagueSection.TYPE_HORIZONTAL_LIST, "Highlights", data.getHighlights(), ContentCard.STYLE_LANDSCAPE);

        List<Match> schedule = toMatches(data.getSchedule());
        result.add(new PremierLeagueSection(
                PremierLeagueSection.TYPE_MATCH_SCHEDULE,
                scheduleHeader(schedule),
                schedule,
                PremierLeagueSection.MODE_UPCOMING));

        List<Match> results = toMatches(data.getResults());
        result.add(new PremierLeagueSection(
                PremierLeagueSection.TYPE_MATCH_SCHEDULE,
                "Kết quả gần nhất",
                results,
                PremierLeagueSection.MODE_FINISHED));

        List<Standing> standings = toStandings(data.getStandings());
        if (!standings.isEmpty()) {
            result.add(new PremierLeagueSection(
                    PremierLeagueSection.TYPE_STANDINGS,
                    standingsHeader(data.getStandings()),
                    standings,
                    false));
        }

        addCardsSection(result, PremierLeagueSection.TYPE_HORIZONTAL_LIST, "Bóng đá Anh", data.getNews(), ContentCard.STYLE_LANDSCAPE);
        return result;
    }

    private void addCardsSection(List<PremierLeagueSection> sections,
                                 int sectionType,
                                 String title,
                                 List<FootballContentDto> items,
                                 int cardStyle) {
        List<ContentCard> cards = new ArrayList<>();
        for (FootballContentDto item : items) {
            cards.add(new ContentCard(
                    String.valueOf(item.getId()),
                    item.getTitle(),
                    item.getThumbnailUrl(),
                    item.getBadge(),
                    cardStyle,
                    item.getVideoUrl(),
                    item.getContentType()));
        }
        if (!cards.isEmpty()) {
            sections.add(new PremierLeagueSection(sectionType, title, cards));
        }
    }

    private List<Match> toMatches(List<FootballMatchDto> items) {
        List<Match> matches = new ArrayList<>();
        for (FootballMatchDto item : items) {
            FootballTeamDto home = item.getHomeTeam();
            FootballTeamDto away = item.getAwayTeam();
            LocalDateTime kickoff = parseDateTime(item.getKickoffAt());
            matches.add(new Match(
                    String.valueOf(item.getId()),
                    home != null ? valueOrEmpty(home.getCode()) : "",
                    home != null ? valueOrEmpty(home.getLogoUrl()) : "",
                    away != null ? valueOrEmpty(away.getCode()) : "",
                    away != null ? valueOrEmpty(away.getLogoUrl()) : "",
                    kickoff != null ? kickoff.format(DISPLAY_TIME) : "",
                    kickoff != null ? kickoff.format(DISPLAY_DATE) : "",
                    valueOrEmpty(item.getRound()),
                    "LIVE".equals(item.getStatus()),
                    item.getHomeScore() != null ? String.valueOf(item.getHomeScore()) : "",
                    item.getAwayScore() != null ? String.valueOf(item.getAwayScore()) : "",
                    item.getHighlightUrl() != null ? item.getHighlightUrl() : ""));
        }
        return matches;
    }

    private List<Standing> toStandings(List<FootballStandingDto> items) {
        List<Standing> standings = new ArrayList<>();
        for (FootballStandingDto item : items) {
            FootballTeamDto team = item.getTeam();
            standings.add(new Standing(
                    valueOrZero(item.getRank()),
                    team != null ? valueOrEmpty(team.getCode()) : "",
                    team != null ? valueOrEmpty(team.getLogoUrl()) : "",
                    valueOrZero(item.getPlayed()),
                    valueOrZero(item.getWon()),
                    valueOrZero(item.getDrawn()),
                    valueOrZero(item.getLost()),
                    valueOrZero(item.getGoalDifference()),
                    valueOrZero(item.getPoints())));
        }
        return standings;
    }

    private String scheduleHeader(List<Match> schedule) {
        if (schedule == null || schedule.isEmpty()) {
            return "Lịch đấu";
        }
        String date = schedule.get(0).getDate();
        return date.isEmpty() ? "Lịch đấu" : "Lịch đấu - " + date;
    }

    private String standingsHeader(List<FootballStandingDto> standings) {
        if (standings.isEmpty() || standings.get(0).getSeason() == null) {
            return "Premier League";
        }
        return "Premier League " + standings.get(0).getSeason();
    }

    private void replaceMatchSection(String mode, List<Match> matches, String displayDate) {
        List<PremierLeagueSection> current = sections.getValue();
        if (current == null) return;

        List<PremierLeagueSection> updated = new ArrayList<>(current);
        for (int i = 0; i < updated.size(); i++) {
            PremierLeagueSection section = updated.get(i);
            if (mode.equals(section.getListMode())) {
                updated.set(i, new PremierLeagueSection(
                        PremierLeagueSection.TYPE_MATCH_SCHEDULE,
                        displayDate != null ? (PremierLeagueSection.MODE_FINISHED.equals(mode) ? "Kết quả - " + displayDate : "Lịch đấu - " + displayDate) : section.getTitle(),
                        matches,
                        mode,
                        section.isExpanded()));
                sections.setValue(updated);
                return;
            }
        }
    }

    private void replaceStandingsSection(List<Standing> standings) {
        List<PremierLeagueSection> current = sections.getValue();
        if (current == null) return;

        List<PremierLeagueSection> updated = new ArrayList<>(current);
        for (int i = 0; i < updated.size(); i++) {
            PremierLeagueSection section = updated.get(i);
            if (PremierLeagueSection.MODE_STANDINGS.equals(section.getListMode())) {
                updated.set(i, new PremierLeagueSection(
                        PremierLeagueSection.TYPE_STANDINGS,
                        section.getTitle(),
                        standings,
                        true));
                sections.setValue(updated);
                return;
            }
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return LocalDateTime.parse(value, INPUT_DATE_TIME);
        } catch (Exception e) {
            Log.w(TAG, "Invalid kickoff time: " + value, e);
            return null;
        }
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }
}

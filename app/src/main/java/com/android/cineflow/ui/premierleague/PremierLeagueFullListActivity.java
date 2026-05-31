package com.android.cineflow.ui.premierleague;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.FootballMatchDto;
import com.android.cineflow.data.network.dto.FootballStandingDto;
import com.android.cineflow.data.network.dto.FootballTeamDto;
import com.bumptech.glide.Glide;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PremierLeagueFullListActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_SCHEDULED = "SCHEDULED";
    public static final String MODE_FINISHED = "FINISHED";
    public static final String MODE_STANDINGS = "STANDINGS";

    private static final DateTimeFormatter INPUT_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private LinearLayout container;
    private View progress;
    private TextView emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premier_league_full_list);

        container = findViewById(R.id.full_list_container);
        progress = findViewById(R.id.progress_full_list);
        emptyView = findViewById(R.id.tv_full_list_empty);

        String mode = getIntent().getStringExtra(EXTRA_MODE);
        TextView title = findViewById(R.id.tv_full_list_title);
        if (MODE_STANDINGS.equals(mode)) {
            title.setText(R.string.premier_league_standings);
            loadStandings();
        } else {
            boolean finished = MODE_FINISHED.equals(mode);
            title.setText(finished ? R.string.premier_league_results : R.string.premier_league_fixtures);
            loadMatches(finished ? MODE_FINISHED : MODE_SCHEDULED);
        }
    }

    private void loadMatches(String status) {
        ApiClient.getFilmApiService().getPremierLeagueMatches(status)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<List<FootballMatchDto>>> call,
                                           Response<ApiResponseDto<List<FootballMatchDto>>> response) {
                        List<FootballMatchDto> items = response.body() != null ? response.body().getData() : null;
                        if (!response.isSuccessful() || items == null) {
                            showError();
                            return;
                        }
                        showMatches(items);
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<List<FootballMatchDto>>> call, Throwable t) {
                        showError();
                    }
                });
    }

    private void loadStandings() {
        ApiClient.getFilmApiService().getPremierLeagueStandings()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<List<FootballStandingDto>>> call,
                                           Response<ApiResponseDto<List<FootballStandingDto>>> response) {
                        List<FootballStandingDto> items = response.body() != null ? response.body().getData() : null;
                        if (!response.isSuccessful() || items == null) {
                            showError();
                            return;
                        }
                        showStandings(items);
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<List<FootballStandingDto>>> call, Throwable t) {
                        showError();
                    }
                });
    }

    private void showMatches(List<FootballMatchDto> items) {
        showContent(items.isEmpty());
        LayoutInflater inflater = LayoutInflater.from(this);
        for (FootballMatchDto match : items) {
            View row = inflater.inflate(R.layout.item_match_row, container, false);
            FootballTeamDto home = match.getHomeTeam();
            FootballTeamDto away = match.getAwayTeam();
            ((TextView) row.findViewById(R.id.tv_home_team)).setText(codeOf(home));
            ((TextView) row.findViewById(R.id.tv_away_team)).setText(codeOf(away));
            loadLogo(row.findViewById(R.id.iv_home_logo), logoOf(home));
            loadLogo(row.findViewById(R.id.iv_away_logo), logoOf(away));
            TextView center = row.findViewById(R.id.tv_match_center);
            if ("LIVE".equals(match.getStatus())) {
                center.setText("LIVE");
                center.setBackgroundColor(Color.parseColor("#D32F2F"));
            } else if (match.getHomeScore() != null && match.getAwayScore() != null) {
                center.setText(match.getHomeScore() + " - " + match.getAwayScore());
            } else {
                center.setText(formatKickoff(match.getKickoffAt()));
            }
            container.addView(row);
        }
    }

    private void showStandings(List<FootballStandingDto> items) {
        showContent(items.isEmpty());
        LayoutInflater inflater = LayoutInflater.from(this);
        for (FootballStandingDto standing : items) {
            View row = inflater.inflate(R.layout.item_standing_row, container, false);
            ((TextView) row.findViewById(R.id.tv_rank)).setText(String.valueOf(valueOrZero(standing.getRank())));
            ((TextView) row.findViewById(R.id.tv_team_code)).setText(codeOf(standing.getTeam()));
            loadLogo(row.findViewById(R.id.iv_team_logo), logoOf(standing.getTeam()));
            ((TextView) row.findViewById(R.id.tv_played)).setText(String.valueOf(valueOrZero(standing.getPlayed())));
            ((TextView) row.findViewById(R.id.tv_won)).setText(String.valueOf(valueOrZero(standing.getWon())));
            ((TextView) row.findViewById(R.id.tv_drawn)).setText(String.valueOf(valueOrZero(standing.getDrawn())));
            ((TextView) row.findViewById(R.id.tv_lost)).setText(String.valueOf(valueOrZero(standing.getLost())));
            ((TextView) row.findViewById(R.id.tv_gd)).setText(String.valueOf(valueOrZero(standing.getGoalDifference())));
            ((TextView) row.findViewById(R.id.tv_points)).setText(String.valueOf(valueOrZero(standing.getPoints())));
            container.addView(row);
        }
    }

    private void showContent(boolean empty) {
        progress.setVisibility(View.GONE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showError() {
        showContent(true);
        Toast.makeText(this, R.string.premier_league_load_error, Toast.LENGTH_SHORT).show();
    }

    private void loadLogo(ImageView imageView, String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(imageView);
    }

    private String formatKickoff(String value) {
        try {
            return LocalDateTime.parse(value, INPUT_DATE_TIME).format(DISPLAY_DATE_TIME);
        } catch (Exception ignored) {
            return value != null ? value : "";
        }
    }

    private String codeOf(FootballTeamDto team) {
        return team != null && team.getCode() != null ? team.getCode() : "";
    }

    private String logoOf(FootballTeamDto team) {
        return team != null ? team.getLogoUrl() : null;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }
}

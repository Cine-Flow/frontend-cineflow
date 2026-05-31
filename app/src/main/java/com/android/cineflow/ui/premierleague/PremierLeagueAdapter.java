package com.android.cineflow.ui.premierleague;

import android.content.Context;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.cineflow.R;
import com.android.cineflow.data.common.uimodel.ContentCard;
import com.android.cineflow.data.model.premierleague.Match;
import com.android.cineflow.data.model.premierleague.Standing;
import com.android.cineflow.ui.home.ContentRowAdapter;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class PremierLeagueAdapter extends BaseAdapter {

    public interface OnViewAllClickListener {
        void onViewAll(String mode);
    }

    public interface OnFixtureDateSelectedListener {
        void onDateSelected(String apiDate, String displayDate);
    }

    public interface OnCardClickListener {
        void onCardClick(ContentCard card);
    }

    private final Context context;
    private final OnViewAllClickListener viewAllClickListener;
    private final OnFixtureDateSelectedListener fixtureDateSelectedListener;
    private final OnCardClickListener cardClickListener;
    private List<PremierLeagueSection> sections;

    public PremierLeagueAdapter(Context context, List<PremierLeagueSection> sections,
                                OnViewAllClickListener viewAllClickListener,
                                OnFixtureDateSelectedListener fixtureDateSelectedListener,
                                OnCardClickListener cardClickListener) {
        this.context = context;
        this.sections = sections;
        this.viewAllClickListener = viewAllClickListener;
        this.fixtureDateSelectedListener = fixtureDateSelectedListener;
        this.cardClickListener = cardClickListener;
    }

    public void setSections(List<PremierLeagueSection> newSections) {
        this.sections = newSections;
        notifyDataSetChanged();
    }

    @Override public int getCount() { return sections != null ? sections.size() : 0; }
    @Override public Object getItem(int position) { return sections.get(position); }
    @Override public long getItemId(int position) { return position; }
    @Override public int getViewTypeCount() { return 5; }

    @Override
    public int getItemViewType(int position) {
        return sections.get(position).getType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        PremierLeagueSection section = sections.get(position);
        LayoutInflater inflater = LayoutInflater.from(context);

        if (convertView == null) {
            switch (type) {
                case PremierLeagueSection.TYPE_BANNER:
                    convertView = inflater.inflate(R.layout.item_home_banner, parent, false);
                    break;
                case PremierLeagueSection.TYPE_HORIZONTAL_LIST:
                    convertView = inflater.inflate(R.layout.item_home_section_row, parent, false);
                    break;
                case PremierLeagueSection.TYPE_MATCH_SCHEDULE:
                    convertView = inflater.inflate(R.layout.item_section_match_schedule, parent, false);
                    break;
                case PremierLeagueSection.TYPE_STANDINGS:
                    convertView = inflater.inflate(R.layout.item_section_standings, parent, false);
                    break;
                default:
                    convertView = new View(context);
            }
        }

        switch (type) {
            case PremierLeagueSection.TYPE_BANNER:
                bindBanner(convertView, section);
                break;
            case PremierLeagueSection.TYPE_HORIZONTAL_LIST:
                bindHorizontalList(convertView, section);
                break;
            case PremierLeagueSection.TYPE_MATCH_SCHEDULE:
                bindMatchSchedule(convertView, section);
                break;
            case PremierLeagueSection.TYPE_STANDINGS:
                bindStandings(convertView, section);
                break;
        }

        return convertView;
    }

    private void bindBanner(View v, PremierLeagueSection section) {
        LinearLayout container = v.findViewById(R.id.rv_banner_container);
        if (container != null) {
            container.removeAllViews();
            ContentRowAdapter adapter = new ContentRowAdapter(context, section.getCards(), ContentCard.STYLE_BANNER,
                    card -> { if (cardClickListener != null) cardClickListener.onCardClick(card); });
            for (int i = 0; i < adapter.getCount(); i++) {
                container.addView(adapter.getView(i, null, container));
            }
        }
    }

    private void bindHorizontalList(View v, PremierLeagueSection section) {
        TextView tvTitle = v.findViewById(R.id.tv_section_title);
        LinearLayout container = v.findViewById(R.id.rv_section_row_container);
        tvTitle.setText(section.getTitle());
        if (container != null) {
            container.removeAllViews();
            ContentRowAdapter adapter = new ContentRowAdapter(context, section.getCards(), ContentCard.STYLE_LANDSCAPE,
                    card -> { if (cardClickListener != null) cardClickListener.onCardClick(card); });
            for (int i = 0; i < adapter.getCount(); i++) {
                container.addView(adapter.getView(i, null, container));
            }
        }
    }

    private void bindMatchSchedule(View v, PremierLeagueSection section) {
        TextView tvHeader = v.findViewById(R.id.tv_schedule_header);
        LinearLayout container = v.findViewById(R.id.match_list_container);
        TextView tvEmptyMatches = v.findViewById(R.id.tv_empty_matches);
        Button btnViewAll = v.findViewById(R.id.btn_view_all_matches);
        Button btnSelectDate = v.findViewById(R.id.btn_select_fixture_date);
        tvHeader.setText(section.getTitle());
        btnViewAll.setVisibility(section.isExpanded() ? View.GONE : View.VISIBLE);
        btnViewAll.setOnClickListener(view -> expandSection(section.getListMode()));
        boolean isFixtures = PremierLeagueSection.MODE_UPCOMING.equals(section.getListMode());
        btnSelectDate.setVisibility(isFixtures ? View.VISIBLE : View.GONE);
        btnSelectDate.setOnClickListener(view -> showFixtureDatePicker(section));
        boolean hasMatches = section.getMatches() != null && !section.getMatches().isEmpty();
        tvEmptyMatches.setVisibility(hasMatches ? View.GONE : View.VISIBLE);
        if (container != null) {
            container.removeAllViews();
            MatchListAdapter adapter = new MatchListAdapter(context, section.getMatches());
            for (int i = 0; i < adapter.getCount(); i++) {
                container.addView(adapter.getView(i, null, container));
            }
        }
    }

    private void bindStandings(View v, PremierLeagueSection section) {
        TextView tvTitle = v.findViewById(R.id.tv_standing_title);
        LinearLayout container = v.findViewById(R.id.standings_table_container);
        Button btnViewAll = v.findViewById(R.id.btn_view_all_standings);
        tvTitle.setText(section.getTitle());
        btnViewAll.setVisibility(section.isExpanded() ? View.GONE : View.VISIBLE);
        btnViewAll.setOnClickListener(view -> expandSection(section.getListMode()));
        if (container != null) {
            container.removeAllViews();
            StandingTableAdapter adapter = new StandingTableAdapter(context, section.getStandings());
            for (int i = 0; i < adapter.getCount(); i++) {
                container.addView(adapter.getView(i, null, container));
            }
        }
    }

    // --- Inner Adapters ---
    class MatchListAdapter extends BaseAdapter {
        Context ctx; List<Match> matches;
        MatchListAdapter(Context ctx, List<Match> matches) { this.ctx = ctx; this.matches = matches; }
        @Override public int getCount() { return matches != null ? matches.size() : 0; }
        @Override public Object getItem(int position) { return matches.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = LayoutInflater.from(ctx).inflate(R.layout.item_match_row, parent, false);
            Match m = matches.get(position);
            TextView tvHome = convertView.findViewById(R.id.tv_home_team);
            TextView tvAway = convertView.findViewById(R.id.tv_away_team);
            TextView tvCenter = convertView.findViewById(R.id.tv_match_center);
            ImageView ivHome = convertView.findViewById(R.id.iv_home_logo);
            ImageView ivAway = convertView.findViewById(R.id.iv_away_logo);
            tvHome.setText(m.getHomeTeamCode());
            tvAway.setText(m.getAwayTeamCode());
            loadTeamLogo(ivHome, m.getHomeTeamLogo());
            loadTeamLogo(ivAway, m.getAwayTeamLogo());
            
            tvCenter.setTextColor(Color.parseColor("#FFFFFF"));
            if (m.isLive()) {
                tvCenter.setText("LIVE");
                tvCenter.setBackgroundColor(Color.parseColor("#FF1744")); // Neon Red Live
            } else if (m.getHomeScore() != null && !m.getHomeScore().isEmpty()) {
                tvCenter.setText(m.getHomeScore() + " - " + m.getAwayScore());
                tvCenter.setBackgroundColor(Color.parseColor("#2A2A2A")); // Premium Grey Box
            } else {
                tvCenter.setText(m.getTime());
                tvCenter.setBackgroundColor(Color.parseColor("#1F1F1F")); // Solid Time Box
            }
            return convertView;
        }
    }

    class StandingTableAdapter extends BaseAdapter {
        Context ctx; List<Standing> standings;
        StandingTableAdapter(Context ctx, List<Standing> standings) { this.ctx = ctx; this.standings = standings; }
        @Override public int getCount() { return standings != null ? standings.size() : 0; }
        @Override public Object getItem(int position) { return standings.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = LayoutInflater.from(ctx).inflate(R.layout.item_standing_row, parent, false);
            Standing s = standings.get(position);
            
            TextView tvRank = convertView.findViewById(R.id.tv_rank);
            TextView tvTeamCode = convertView.findViewById(R.id.tv_team_code);
            TextView tvPoints = convertView.findViewById(R.id.tv_points);
            
            tvRank.setText(String.valueOf(s.getRank()));
            tvTeamCode.setText(s.getTeamCode());
            loadTeamLogo(convertView.findViewById(R.id.iv_team_logo), s.getTeamLogo());
            ((TextView)convertView.findViewById(R.id.tv_played)).setText(String.valueOf(s.getPlayed()));
            ((TextView)convertView.findViewById(R.id.tv_won)).setText(String.valueOf(s.getWon()));
            ((TextView)convertView.findViewById(R.id.tv_drawn)).setText(String.valueOf(s.getDrawn()));
            ((TextView)convertView.findViewById(R.id.tv_lost)).setText(String.valueOf(s.getLost()));
            ((TextView)convertView.findViewById(R.id.tv_gd)).setText(String.valueOf(s.getGoalDifference()));
            tvPoints.setText(String.valueOf(s.getPoints()));
            
            // Dynamic UEFA Champions League, Europa League and Relegation Coloring
            int rank = s.getRank();
            if (rank >= 1 && rank <= 4) {
                tvRank.setTextColor(Color.parseColor("#00E676")); // Neon Green for Champions League
                tvRank.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (rank == 5) {
                tvRank.setTextColor(Color.parseColor("#FF9100")); // Orange for Europa League
                tvRank.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (rank >= 18) {
                tvRank.setTextColor(Color.parseColor("#FF1744")); // Red for Relegation
                tvRank.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvRank.setTextColor(Color.parseColor("#FFFFFF")); // Standard text color
                tvRank.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
            
            // Highlight team names and points with high contrast
            tvTeamCode.setTextColor(Color.parseColor("#FFFFFF"));
            tvTeamCode.setTypeface(null, android.graphics.Typeface.BOLD);
            tvPoints.setTextColor(Color.parseColor("#FFFFFF"));
            tvPoints.setTypeface(null, android.graphics.Typeface.BOLD);
            
            return convertView;
        }
    }

    private void expandSection(String mode) {
        if (viewAllClickListener != null) {
            viewAllClickListener.onViewAll(mode);
        }
    }

    private void showFixtureDatePicker(PremierLeagueSection section) {
        Calendar calendar = Calendar.getInstance();
        if (section.getMatches() != null && !section.getMatches().isEmpty()) {
            try {
                calendar.setTime(new SimpleDateFormat("dd/MM/yyyy", Locale.US)
                        .parse(section.getMatches().get(0).getDate()));
            } catch (Exception ignored) {
                // Keep today's date if the API value cannot be parsed.
            }
        }
        new DatePickerDialog(
                context,
                (view, year, month, day) -> {
                    String apiDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                    String displayDate = String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year);
                    if (fixtureDateSelectedListener != null) {
                        fixtureDateSelectedListener.onDateSelected(apiDate, displayDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void loadTeamLogo(ImageView imageView, String logoUrl) {
        Glide.with(imageView.getContext())
                .load(logoUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(imageView);
    }
}

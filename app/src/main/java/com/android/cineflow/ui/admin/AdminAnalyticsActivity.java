package com.android.cineflow.ui.admin;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;
import com.android.cineflow.data.network.dto.AdminAnalyticsDto;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.ui.admin.view.BarChartView;
import com.android.cineflow.ui.admin.view.DonutChartView;
import com.android.cineflow.ui.admin.view.LineChartView;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminAnalyticsActivity extends com.android.cineflow.ui.base.BaseActivity {

    private enum Section { OVERVIEW, CATALOG }

    private TextView chip7, chip30, chip90;
    private int period = 30;
    private TextView tvSectionTitle, tvSectionSubtitle;
    private NestedScrollView sectionOverview, sectionCatalog;
    private LinearLayout tabOverview, tabCatalog;
    private ImageView iconOverview, iconCatalog;
    private TextView labelOverview, labelCatalog;
    private LineChartView chartSignups, chartWatch;
    private TextView tvSignupsTotal, tvWatchTotal;
    private DonutChartView donutFilmType, donutPremiumFree;
    private LinearLayout legendFilmType, legendPremiumFree;
    private BarChartView barsCategories, barsTopFilms, barsTopFavorited, barsTopCommented, barsTopEpisodes;
    private TextView tvZeroViewFilms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.admin_title_analytics);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageView btnExport = findViewById(R.id.btn_export);
        btnExport.setVisibility(View.GONE);

        tvSectionTitle = findViewById(R.id.tv_section_title);
        tvSectionSubtitle = findViewById(R.id.tv_section_subtitle);
        sectionOverview = findViewById(R.id.section_overview);
        sectionCatalog = findViewById(R.id.section_catalog);
        tabOverview = findViewById(R.id.tab_overview);
        tabCatalog = findViewById(R.id.tab_catalog);
        iconOverview = findViewById(R.id.icon_overview);
        iconCatalog = findViewById(R.id.icon_catalog);
        labelOverview = findViewById(R.id.label_overview);
        labelCatalog = findViewById(R.id.label_catalog);
        chip7 = findViewById(R.id.chip_7d);
        chip30 = findViewById(R.id.chip_30d);
        chip90 = findViewById(R.id.chip_90d);
        chartSignups = findViewById(R.id.chart_signups);
        chartWatch = findViewById(R.id.chart_watch);
        tvSignupsTotal = findViewById(R.id.tv_signups_total);
        tvWatchTotal = findViewById(R.id.tv_watch_total);
        donutFilmType = findViewById(R.id.donut_film_type);
        donutPremiumFree = findViewById(R.id.donut_premium_free);
        legendFilmType = findViewById(R.id.legend_film_type);
        legendPremiumFree = findViewById(R.id.legend_premium_free);
        barsCategories = findViewById(R.id.bars_categories);
        barsTopFilms = findViewById(R.id.bars_top_films);
        barsTopFavorited = findViewById(R.id.bars_top_favorited);
        barsTopCommented = findViewById(R.id.bars_top_commented);
        barsTopEpisodes = findViewById(R.id.bars_top_episodes);
        tvZeroViewFilms = findViewById(R.id.tv_zero_view_films);

        tabOverview.setOnClickListener(v -> switchTo(Section.OVERVIEW));
        tabCatalog.setOnClickListener(v -> switchTo(Section.CATALOG));
        chip7.setOnClickListener(v -> { period = 7; refreshChips(); loadAnalytics(); });
        chip30.setOnClickListener(v -> { period = 30; refreshChips(); loadAnalytics(); });
        chip90.setOnClickListener(v -> { period = 90; refreshChips(); loadAnalytics(); });

        refreshChips();
        switchTo(Section.OVERVIEW);
        loadAnalytics();
    }

    private void loadAnalytics() {
        ApiClient.getFilmApiService().getAdminAnalytics(period)
                .enqueue(new Callback<ApiResponseDto<AdminAnalyticsDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<AdminAnalyticsDto>> call,
                                           Response<ApiResponseDto<AdminAnalyticsDto>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            bindAnalytics(response.body().getData());
                        } else {
                            Toast.makeText(AdminAnalyticsActivity.this,
                                    R.string.admin_toast_cannot_load_analytics, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<AdminAnalyticsDto>> call, Throwable t) {
                        Toast.makeText(AdminAnalyticsActivity.this,
                                getString(R.string.admin_toast_server_error_format, t.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindAnalytics(AdminAnalyticsDto data) {
        String periodLabel = getString(R.string.admin_period_last_days, data.getPeriod());
        String allTime = getString(R.string.admin_period_all_time);

        bindKpi(findViewById(R.id.kpi_users), getString(R.string.admin_kpi_total_users),
                formatNumber(data.getTotalUsers()), "", true, allTime,
                R.color.brand_primary);
        bindKpi(findViewById(R.id.kpi_signups), getString(R.string.admin_kpi_new_signups),
                formatNumber(data.getNewSignups()), "", true, periodLabel,
                R.color.status_info);
        bindKpi(findViewById(R.id.kpi_active_users), getString(R.string.admin_kpi_active_users),
                formatNumber(data.getActiveUsers()), "", true, periodLabel,
                R.color.status_success);
        bindKpi(findViewById(R.id.kpi_views), getString(R.string.admin_kpi_episode_views),
                formatNumber(data.getEpisodeViews()), "", true, periodLabel,
                R.color.badge_movie);
        bindKpi(findViewById(R.id.kpi_watch_sessions), getString(R.string.admin_kpi_watch_sessions),
                formatNumber(data.getWatchSessions()), "", true, periodLabel,
                R.color.badge_series);
        bindKpi(findViewById(R.id.kpi_favorites), getString(R.string.admin_kpi_favorites),
                formatNumber(data.getTotalFavorites()), "", true, allTime,
                R.color.brand_accent);

        tvSignupsTotal.setText(formatNumber(data.getNewSignups()));
        tvWatchTotal.setText(formatNumber(data.getWatchSessions()));
        chartSignups.setData(toPoints(data.getDailySignups()), getColor(R.color.brand_primary));
        chartWatch.setData(toPoints(data.getDailyWatchSessions()), getColor(R.color.badge_movie));

        List<DonutChartView.Slice> filmSlices = toSlices(data.getFilmTypes(), new int[] {
                R.color.badge_movie, R.color.badge_series, R.color.badge_live, R.color.brand_primary
        });
        donutFilmType.setSlices(filmSlices);
        donutFilmType.setCenterText(formatNumber(sumSlices(data.getFilmTypes())), getString(R.string.admin_donut_films_unit));
        renderLegend(legendFilmType, filmSlices);

        List<DonutChartView.Slice> premiumSlices = toSlices(data.getPremiumFreeMix(), new int[] {
                R.color.badge_premium, R.color.status_info
        });
        donutPremiumFree.setSlices(premiumSlices);
        donutPremiumFree.setCenterText(formatNumber(sumSlices(data.getPremiumFreeMix())), getString(R.string.admin_donut_films_unit));
        renderLegend(legendPremiumFree, premiumSlices);

        barsCategories.setBars(toBars(data.getTopCategories(), R.color.brand_primary));
        barsTopFilms.setBars(toBars(data.getTopFilms(), R.color.badge_movie));
        barsTopFavorited.setBars(toBars(data.getTopFavoritedFilms(), R.color.brand_accent));
        barsTopCommented.setBars(toBars(data.getTopCommentedFilms(), R.color.status_info));
        barsTopEpisodes.setBars(toBars(data.getTopEpisodes(), R.color.badge_series));

        tvZeroViewFilms.setText(formatNumber(data.getFilmsWithZeroViews()));
    }

    private void switchTo(Section section) {
        sectionOverview.setVisibility(section == Section.OVERVIEW ? View.VISIBLE : View.GONE);
        sectionCatalog.setVisibility(section == Section.CATALOG ? View.VISIBLE : View.GONE);
        styleTab(iconOverview, labelOverview, section == Section.OVERVIEW);
        styleTab(iconCatalog, labelCatalog, section == Section.CATALOG);
        if (section == Section.OVERVIEW) {
            tvSectionTitle.setText(R.string.admin_section_overview);
            tvSectionSubtitle.setText(R.string.admin_section_overview_subtitle);
        } else {
            tvSectionTitle.setText(R.string.admin_section_catalog);
            tvSectionSubtitle.setText(R.string.admin_section_catalog_subtitle);
        }
    }

    private void styleTab(ImageView icon, TextView label, boolean selected) {
        int color = getColor(selected ? R.color.brand_primary : R.color.text_tertiary);
        icon.setColorFilter(color);
        label.setTextColor(color);
    }

    private void refreshChips() {
        setChipState(chip7, period == 7);
        setChipState(chip30, period == 30);
        setChipState(chip90, period == 90);
    }

    private void setChipState(TextView chip, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(40f);
        if (selected) {
            bg.setColor(getColor(R.color.brand_primary));
            chip.setTextColor(getColor(R.color.text_primary));
        } else {
            bg.setColor(getColor(R.color.surface_secondary));
            bg.setStroke(1, getColor(R.color.surface_tertiary));
            chip.setTextColor(getColor(R.color.text_secondary));
        }
        chip.setBackground(bg);
    }

    private void bindKpi(View root, String label, String value, String delta, boolean positive,
                         String subtitle, int accentColorRes) {
        ((TextView) root.findViewById(R.id.tv_kpi_label)).setText(label);
        ((TextView) root.findViewById(R.id.tv_kpi_value)).setText(value);
        TextView tvDelta = root.findViewById(R.id.tv_kpi_delta);
        tvDelta.setText(delta);
        tvDelta.setVisibility(delta == null || delta.isEmpty() ? View.GONE : View.VISIBLE);
        tvDelta.setTextColor(getColor(positive ? R.color.status_success : R.color.status_error));
        TextView tvSub = root.findViewById(R.id.tv_kpi_sub);
        tvSub.setText(subtitle);
        tvSub.setVisibility(subtitle == null || subtitle.isEmpty() ? View.GONE : View.VISIBLE);
        View accent = root.findViewById(R.id.v_kpi_accent);
        if (accent != null) accent.setBackgroundColor(getColor(accentColorRes));
    }

    private List<LineChartView.Point> toPoints(List<AdminAnalyticsDto.TimePoint> series) {
        List<LineChartView.Point> points = new ArrayList<>();
        if (series == null || series.isEmpty()) return points;
        int n = series.size();
        for (int i = 0; i < n; i++) {
            AdminAnalyticsDto.TimePoint p = series.get(i);
            // Show only first / mid / last label to avoid crowding
            String label = (i == 0 || i == n - 1 || i == n / 2) ? shortDate(p.getDate()) : "";
            points.add(new LineChartView.Point(label, p.getValue()));
        }
        return points;
    }

    private String shortDate(String iso) {
        if (iso == null || iso.length() < 10) return iso == null ? "" : iso;
        // yyyy-MM-dd → dd/MM
        return iso.substring(8, 10) + "/" + iso.substring(5, 7);
    }

    private List<DonutChartView.Slice> toSlices(List<AdminAnalyticsDto.MetricSlice> items, int[] colors) {
        List<DonutChartView.Slice> slices = new ArrayList<>();
        if (items == null) return slices;
        for (int i = 0; i < items.size(); i++) {
            AdminAnalyticsDto.MetricSlice item = items.get(i);
            slices.add(new DonutChartView.Slice(item.getLabel(), item.getValue(),
                    getColor(colors[i % colors.length])));
        }
        return slices;
    }

    private List<BarChartView.Bar> toBars(List<AdminAnalyticsDto.MetricBar> items, int colorRes) {
        List<BarChartView.Bar> bars = new ArrayList<>();
        if (items == null) return bars;
        for (AdminAnalyticsDto.MetricBar item : items) {
            bars.add(new BarChartView.Bar(item.getLabel(), item.getValue(),
                    formatNumber(item.getValue()), getColor(colorRes)));
        }
        return bars;
    }

    private void renderLegend(LinearLayout container, List<DonutChartView.Slice> slices) {
        container.removeAllViews();
        for (DonutChartView.Slice slice : slices) {
            TextView row = new TextView(this);
            row.setText(slice.label + " - " + formatNumber((long) slice.value));
            row.setTextColor(getColor(R.color.text_secondary));
            row.setTextSize(12f);
            row.setPadding(0, 4, 0, 4);
            container.addView(row);
        }
    }

    private long sumSlices(List<AdminAnalyticsDto.MetricSlice> items) {
        long total = 0;
        if (items != null) {
            for (AdminAnalyticsDto.MetricSlice item : items) total += item.getValue();
        }
        return total;
    }

    private String formatNumber(long value) {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(value);
    }
}

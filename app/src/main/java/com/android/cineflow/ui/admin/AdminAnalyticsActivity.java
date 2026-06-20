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

    private enum Section { OVERVIEW, CATALOG, SUBSCRIPTIONS }

    private TextView chip7, chip30, chip90;
    private int period = 30;
    private TextView tvSectionTitle, tvSectionSubtitle;
    private NestedScrollView sectionOverview, sectionCatalog, sectionSubs;
    private LinearLayout tabOverview, tabCatalog, tabSubs;
    private ImageView iconOverview, iconCatalog, iconSubs;
    private TextView labelOverview, labelCatalog, labelSubs;
    private LineChartView chartSignups, chartWatch;
    private TextView tvSignupsTotal, tvWatchTotal;
    private DonutChartView donutFilmType, donutSubs;
    private LinearLayout legendFilmType, legendSubs;
    private BarChartView barsCategories, barsTopFilms;
    private TextView tvSubsActive, tvSubsExpiring, tvSubsExpired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Analytics Dashboard");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageView btnExport = findViewById(R.id.btn_export);
        btnExport.setVisibility(View.GONE);

        tvSectionTitle = findViewById(R.id.tv_section_title);
        tvSectionSubtitle = findViewById(R.id.tv_section_subtitle);
        sectionOverview = findViewById(R.id.section_overview);
        sectionCatalog = findViewById(R.id.section_catalog);
        sectionSubs = findViewById(R.id.section_subscriptions);
        tabOverview = findViewById(R.id.tab_overview);
        tabCatalog = findViewById(R.id.tab_catalog);
        tabSubs = findViewById(R.id.tab_subscriptions);
        iconOverview = findViewById(R.id.icon_overview);
        iconCatalog = findViewById(R.id.icon_catalog);
        iconSubs = findViewById(R.id.icon_subscriptions);
        labelOverview = findViewById(R.id.label_overview);
        labelCatalog = findViewById(R.id.label_catalog);
        labelSubs = findViewById(R.id.label_subscriptions);
        chip7 = findViewById(R.id.chip_7d);
        chip30 = findViewById(R.id.chip_30d);
        chip90 = findViewById(R.id.chip_90d);
        chartSignups = findViewById(R.id.chart_signups);
        chartWatch = findViewById(R.id.chart_watch);
        tvSignupsTotal = findViewById(R.id.tv_signups_total);
        tvWatchTotal = findViewById(R.id.tv_watch_total);
        donutFilmType = findViewById(R.id.donut_film_type);
        donutSubs = findViewById(R.id.donut_subs);
        legendFilmType = findViewById(R.id.legend_film_type);
        legendSubs = findViewById(R.id.legend_subs);
        barsCategories = findViewById(R.id.bars_categories);
        barsTopFilms = findViewById(R.id.bars_top_films);
        tvSubsActive = findViewById(R.id.tv_subs_active);
        tvSubsExpiring = findViewById(R.id.tv_subs_expiring);
        tvSubsExpired = findViewById(R.id.tv_subs_expired);

        tabOverview.setOnClickListener(v -> switchTo(Section.OVERVIEW));
        tabCatalog.setOnClickListener(v -> switchTo(Section.CATALOG));
        tabSubs.setOnClickListener(v -> switchTo(Section.SUBSCRIPTIONS));
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
                                    "Cannot load analytics", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<AdminAnalyticsDto>> call, Throwable t) {
                        Toast.makeText(AdminAnalyticsActivity.this,
                                "Server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindAnalytics(AdminAnalyticsDto data) {
        bindKpi(findViewById(R.id.kpi_users), "TOTAL USERS", formatNumber(data.getTotalUsers()),
                "", true, "all-time users", R.color.brand_primary);
        bindKpi(findViewById(R.id.kpi_signups), "NEW SIGN-UPS", formatNumber(data.getNewSignups()),
                "", true, "last " + data.getPeriod() + " days", R.color.status_info);
        bindKpi(findViewById(R.id.kpi_views), "EPISODE VIEWS", formatNumber(data.getEpisodeViews()),
                "", true, "SUM(episodes.view_count)", R.color.badge_movie);
        bindKpi(findViewById(R.id.kpi_watch_sessions), "WATCH SESSIONS", formatNumber(data.getWatchSessions()),
                "", true, "watch_history count", R.color.badge_series);
        bindKpi(findViewById(R.id.kpi_premium), "PREMIUM USERS", formatNumber(data.getPremiumUsers()),
                "", true, "active subscriptions", R.color.badge_premium);
        bindKpi(findViewById(R.id.kpi_revenue), "REVENUE", formatCurrency(data),
                "", true, "active packages", R.color.status_success);

        tvSignupsTotal.setText(formatNumber(data.getNewSignups()));
        tvWatchTotal.setText(formatNumber(data.getWatchSessions()));
        chartSignups.setData(simpleSeries(data.getPeriod(), data.getNewSignups()), getColor(R.color.brand_primary));
        chartWatch.setData(simpleSeries(data.getPeriod(), data.getWatchSessions()), getColor(R.color.badge_movie));

        List<DonutChartView.Slice> filmSlices = toSlices(data.getFilmTypes(), new int[] {
                R.color.badge_movie, R.color.badge_series, R.color.badge_live, R.color.brand_primary
        });
        donutFilmType.setSlices(filmSlices);
        donutFilmType.setCenterText(formatNumber(sumSlices(data.getFilmTypes())), "films");
        renderLegend(legendFilmType, filmSlices);

        List<DonutChartView.Slice> subSlices = toSlices(data.getSubscriptions(), new int[] {
                R.color.badge_premium, R.color.brand_primary, R.color.status_info, R.color.status_warning
        });
        donutSubs.setSlices(subSlices);
        donutSubs.setCenterText(formatNumber(sumSlices(data.getSubscriptions())), "subs");
        renderLegend(legendSubs, subSlices);

        barsCategories.setBars(toBars(data.getTopCategories(), R.color.brand_primary));
        barsTopFilms.setBars(toBars(data.getTopFilms(), R.color.badge_movie));

        tvSubsActive.setText(formatNumber(data.getPremiumUsers()));
        tvSubsExpiring.setText("-");
        tvSubsExpired.setText("-");
    }

    private void switchTo(Section section) {
        sectionOverview.setVisibility(section == Section.OVERVIEW ? View.VISIBLE : View.GONE);
        sectionCatalog.setVisibility(section == Section.CATALOG ? View.VISIBLE : View.GONE);
        sectionSubs.setVisibility(section == Section.SUBSCRIPTIONS ? View.VISIBLE : View.GONE);
        styleTab(iconOverview, labelOverview, section == Section.OVERVIEW);
        styleTab(iconCatalog, labelCatalog, section == Section.CATALOG);
        styleTab(iconSubs, labelSubs, section == Section.SUBSCRIPTIONS);
        if (section == Section.OVERVIEW) {
            tvSectionTitle.setText("Overview");
            tvSectionSubtitle.setText("Engagement KPIs from backend analytics");
        } else if (section == Section.CATALOG) {
            tvSectionTitle.setText("Catalog");
            tvSectionSubtitle.setText("Film type, category and top title data");
        } else {
            tvSectionTitle.setText("Subscriptions");
            tvSectionSubtitle.setText("Active packages and revenue summary");
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
        ((TextView) root.findViewById(R.id.tv_kpi_sub)).setText(subtitle);
        View accent = root.findViewById(R.id.v_kpi_accent);
        if (accent != null) accent.setBackgroundColor(getColor(accentColorRes));
    }

    private List<LineChartView.Point> simpleSeries(int days, long total) {
        List<LineChartView.Point> points = new ArrayList<>();
        int slots = Math.max(2, Math.min(days, 10));
        float avg = slots == 0 ? 0 : (float) total / slots;
        for (int i = 0; i < slots; i++) {
            points.add(new LineChartView.Point(String.valueOf(i + 1), avg));
        }
        return points;
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

    private String formatCurrency(AdminAnalyticsDto data) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(data.getRevenue());
    }
}

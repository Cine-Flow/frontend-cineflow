package com.android.cineflow.ui.admin;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.android.cineflow.R;
import com.android.cineflow.ui.admin.view.BarChartView;
import com.android.cineflow.ui.admin.view.DonutChartView;
import com.android.cineflow.ui.admin.view.LineChartView;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AdminAnalyticsActivity extends com.android.cineflow.ui.base.BaseActivity {

    private enum Section { OVERVIEW, CATALOG, SUBSCRIPTIONS }

    private TextView chip7, chip30, chip90;
    private int period = 30;
    private Section currentSection = Section.OVERVIEW;
    private final Random rng = new Random(42);

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
        btnExport.setOnClickListener(v ->
                Toast.makeText(this, "CSV export queued (mock)", Toast.LENGTH_SHORT).show());

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

        tabOverview.setOnClickListener(v -> switchTo(Section.OVERVIEW));
        tabCatalog.setOnClickListener(v -> switchTo(Section.CATALOG));
        tabSubs.setOnClickListener(v -> switchTo(Section.SUBSCRIPTIONS));

        chip7 = findViewById(R.id.chip_7d);
        chip30 = findViewById(R.id.chip_30d);
        chip90 = findViewById(R.id.chip_90d);
        chip7.setOnClickListener(v -> { period = 7; refreshChips(); refreshOverview(); refreshSubscriptions(); });
        chip30.setOnClickListener(v -> { period = 30; refreshChips(); refreshOverview(); refreshSubscriptions(); });
        chip90.setOnClickListener(v -> { period = 90; refreshChips(); refreshOverview(); refreshSubscriptions(); });

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

        bindCatalog();
        bindStaticSubscriptionHealth();
        refreshChips();
        refreshOverview();
        refreshSubscriptions();
        switchTo(Section.OVERVIEW);
    }

    private void switchTo(Section section) {
        currentSection = section;
        sectionOverview.setVisibility(section == Section.OVERVIEW ? View.VISIBLE : View.GONE);
        sectionCatalog.setVisibility(section == Section.CATALOG ? View.VISIBLE : View.GONE);
        sectionSubs.setVisibility(section == Section.SUBSCRIPTIONS ? View.VISIBLE : View.GONE);

        styleTab(iconOverview, labelOverview, section == Section.OVERVIEW);
        styleTab(iconCatalog, labelCatalog, section == Section.CATALOG);
        styleTab(iconSubs, labelSubs, section == Section.SUBSCRIPTIONS);

        switch (section) {
            case OVERVIEW:
                tvSectionTitle.setText("Overview");
                tvSectionSubtitle.setText("Engagement KPIs and trends over the selected window");
                break;
            case CATALOG:
                tvSectionTitle.setText("Catalog");
                tvSectionSubtitle.setText("Film distribution, category coverage and most-viewed titles");
                break;
            case SUBSCRIPTIONS:
                tvSectionTitle.setText("Subscriptions");
                tvSectionSubtitle.setText("Revenue, package mix and renewal health");
                break;
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

    // ============================ OVERVIEW ============================

    private void refreshOverview() {
        bindKpi(findViewById(R.id.kpi_users),
                "TOTAL USERS", "12,418", "+3.2%", true,
                "all-time (users)", R.color.brand_primary);
        int signupsInPeriod = 60 + period * 12 + rng.nextInt(80);
        bindKpi(findViewById(R.id.kpi_signups),
                "NEW SIGN-UPS", formatNumber(signupsInPeriod),
                deltaPct(8.5f), true,
                "last " + period + " days", R.color.status_info);
        long totalViews = 9_847_000L + (long) period * 38_000L;
        bindKpi(findViewById(R.id.kpi_views),
                "EPISODE VIEWS", formatNumber(totalViews),
                "+6.8%", true,
                "SUM(episodes.view_count)", R.color.badge_movie);
        int sessions = 184_000 + period * 9_500;
        bindKpi(findViewById(R.id.kpi_watch_sessions),
                "WATCH SESSIONS", formatNumber(sessions),
                deltaPct(-2.1f), false,
                "last " + period + " days · watch_history",
                R.color.badge_series);

        List<LineChartView.Point> signupSeries = generateSeries(period, 18, 12, 1.2f);
        chartSignups.setData(signupSeries, getColor(R.color.brand_primary));
        int total = 0;
        for (LineChartView.Point p : signupSeries) total += p.value;
        tvSignupsTotal.setText(formatNumber(total));

        List<LineChartView.Point> watchSeries = generateSeries(period, 6_200, 1_900, 1.05f);
        chartWatch.setData(watchSeries, getColor(R.color.badge_movie));
        int watchTotal = 0;
        for (LineChartView.Point p : watchSeries) watchTotal += p.value;
        tvWatchTotal.setText(formatNumber(watchTotal));
    }

    // ============================ CATALOG ============================

    private void bindCatalog() {
        int single = 312, series = 84, live = 14;
        List<DonutChartView.Slice> filmSlices = new ArrayList<>();
        filmSlices.add(new DonutChartView.Slice("Movies (SINGLE)", single, getColor(R.color.badge_movie)));
        filmSlices.add(new DonutChartView.Slice("Series", series, getColor(R.color.badge_series)));
        filmSlices.add(new DonutChartView.Slice("Live", live, getColor(R.color.badge_live)));
        donutFilmType.setSlices(filmSlices);
        donutFilmType.setCenterText(String.valueOf(single + series + live), "films");
        renderLegend(legendFilmType, filmSlices);

        List<BarChartView.Bar> catBars = new ArrayList<>();
        catBars.add(new BarChartView.Bar("Action", 142, "142", getColor(R.color.brand_primary)));
        catBars.add(new BarChartView.Bar("Drama", 98, "98", getColor(R.color.badge_series)));
        catBars.add(new BarChartView.Bar("Adventure", 81, "81", getColor(R.color.badge_movie)));
        catBars.add(new BarChartView.Bar("Comedy", 76, "76", getColor(R.color.status_warning)));
        catBars.add(new BarChartView.Bar("Thriller", 65, "65", getColor(R.color.badge_live)));
        catBars.add(new BarChartView.Bar("Romance", 61, "61", getColor(R.color.badge_premium)));
        barsCategories.setBars(catBars);

        List<BarChartView.Bar> filmBars = new ArrayList<>();
        filmBars.add(new BarChartView.Bar("Oppenheimer", 482_104, "482.1k", getColor(R.color.brand_primary)));
        filmBars.add(new BarChartView.Bar("Dune: Part Two", 396_337, "396.3k", getColor(R.color.badge_series)));
        filmBars.add(new BarChartView.Bar("The Bear S3", 318_572, "318.6k", getColor(R.color.badge_movie)));
        filmBars.add(new BarChartView.Bar("Inside Out 2", 287_415, "287.4k", getColor(R.color.status_warning)));
        filmBars.add(new BarChartView.Bar("Squid Game S2", 251_900, "251.9k", getColor(R.color.badge_live)));
        filmBars.add(new BarChartView.Bar("Furiosa", 198_044, "198.0k", getColor(R.color.badge_premium)));
        barsTopFilms.setBars(filmBars);
    }

    // ============================ SUBSCRIPTIONS ============================

    private void refreshSubscriptions() {
        bindKpi(findViewById(R.id.kpi_premium),
                "PREMIUM USERS", "1,754", "+5.1%", true,
                "active subscriptions", R.color.badge_premium);
        long revenue = 75_000_000L + (long) period * 2_400_000L;
        bindKpi(findViewById(R.id.kpi_revenue),
                "REVENUE", formatVnd(revenue), "+12.4%", true,
                "last " + period + " days · packages.price",
                R.color.status_success);
    }

    private void bindStaticSubscriptionHealth() {
        int monthly = 1248, annual = 506, trial = 92;
        List<DonutChartView.Slice> subSlices = new ArrayList<>();
        subSlices.add(new DonutChartView.Slice("Premium Monthly", monthly, getColor(R.color.brand_primary)));
        subSlices.add(new DonutChartView.Slice("Premium Annual", annual, getColor(R.color.badge_premium)));
        subSlices.add(new DonutChartView.Slice("Free Trial", trial, getColor(R.color.status_info)));
        donutSubs.setSlices(subSlices);
        int activeTotal = monthly + annual + trial;
        donutSubs.setCenterText(formatNumber(activeTotal), "active");
        renderLegend(legendSubs, subSlices);

        tvSubsActive.setText(formatNumber(activeTotal));
        tvSubsExpiring.setText("147");
        tvSubsExpired.setText("89");
    }

    // ============================ HELPERS ============================

    private void bindKpi(View kpi, String label, String value,
                         String delta, boolean up, String sub, int accentColor) {
        TextView tvLabel = kpi.findViewById(R.id.tv_kpi_label);
        TextView tvValue = kpi.findViewById(R.id.tv_kpi_value);
        TextView tvDelta = kpi.findViewById(R.id.tv_kpi_delta);
        TextView tvSub = kpi.findViewById(R.id.tv_kpi_sub);
        View vAccent = kpi.findViewById(R.id.v_kpi_accent);

        tvLabel.setText(label);
        tvValue.setText(value);
        tvDelta.setText((up ? "▲ " : "▼ ") + delta);
        tvDelta.setBackgroundResource(up
                ? R.drawable.bg_kpi_pill_up : R.drawable.bg_kpi_pill_down);
        tvDelta.setTextColor(getColor(up ? R.color.status_success : R.color.status_error));
        tvSub.setText(sub);
        vAccent.setBackgroundColor(getColor(accentColor));
    }

    private void renderLegend(LinearLayout container, List<DonutChartView.Slice> slices) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        float total = 0;
        for (DonutChartView.Slice s : slices) total += s.value;

        for (DonutChartView.Slice s : slices) {
            View row = inflater.inflate(R.layout.item_admin_legend, container, false);
            View dot = row.findViewById(R.id.v_legend_dot);
            TextView label = row.findViewById(R.id.tv_legend_label);
            TextView value = row.findViewById(R.id.tv_legend_value);
            TextView share = row.findViewById(R.id.tv_legend_share);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(s.color);
            dot.setBackground(bg);

            label.setText(s.label);
            value.setText(formatNumber((long) s.value));
            if (total > 0) {
                share.setText(String.format(Locale.ROOT,
                        "(%.1f%%)", s.value * 100f / total));
            } else {
                share.setVisibility(View.GONE);
            }
            container.addView(row);
        }
    }

    private List<LineChartView.Point> generateSeries(int days, float base,
                                                     float swing, float trend) {
        List<LineChartView.Point> data = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days + 1);
        for (int i = 0; i < days; i++) {
            float seasonal = (float) Math.sin(i * 0.45) * swing * 0.4f;
            float noise = (rng.nextFloat() - 0.4f) * swing;
            float value = base + seasonal + noise + i * (trend - 1) * 4f;
            if (value < 1) value = 1;
            String label = String.format(Locale.ROOT, "%02d/%02d",
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1);
            data.add(new LineChartView.Point(label, value));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return data;
    }

    private String deltaPct(float v) {
        return String.format(Locale.ROOT, "%.1f%%", Math.abs(v));
    }

    private String formatNumber(long n) {
        return NumberFormat.getNumberInstance(Locale.US).format(n);
    }

    private String formatNumber(int n) { return formatNumber((long) n); }

    private String formatVnd(long n) {
        if (n >= 1_000_000_000L) {
            return String.format(Locale.US, "%.2fB ₫", n / 1_000_000_000.0);
        }
        if (n >= 1_000_000L) {
            return String.format(Locale.US, "%.1fM ₫", n / 1_000_000.0);
        }
        return formatNumber(n) + " ₫";
    }
}

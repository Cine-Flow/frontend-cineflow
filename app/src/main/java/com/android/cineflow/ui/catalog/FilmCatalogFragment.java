package com.android.cineflow.ui.catalog;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.FilmDto;
import com.android.cineflow.ui.base.BaseFragment;
import com.android.cineflow.ui.detail.FilmDetailActivity;

import java.util.ArrayList;
import java.util.List;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

/**
 * Generic catalog fragment used by Series and Movies tabs.
 * Distinct from HomeFragment: a single grid view with header + filter pills,
 * not a stack of horizontal rows.
 */
public abstract class FilmCatalogFragment extends BaseFragment {

    private static final String FILTER_ALL = "all";
    private static final String FILTER_NEW = "new";
    private static final String FILTER_PREMIUM = "premium";
    private static final String FILTER_FREE = "free";

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvCatalog;
    private ProgressBar progress;
    private TextView tvError;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private LinearLayout llFilters;
    private View headerView;

    private FilmCatalogAdapter adapter;
    private final List<FilmDto> allFilms = new ArrayList<>();
    private String currentFilter = FILTER_ALL;

    /** Backend film type, e.g. "SERIES" or "SINGLE". */
    protected abstract String getFilmType();

    /** Title shown at the top of the catalog. */
    protected abstract String getCatalogTitle();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_film_catalog;
    }

    @Override
    protected void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh_catalog);
        rvCatalog    = view.findViewById(R.id.rv_catalog);
        progress     = view.findViewById(R.id.progress_catalog);
        tvError      = view.findViewById(R.id.tv_catalog_error);
        tvTitle      = view.findViewById(R.id.tv_catalog_title_header);
        tvSubtitle   = view.findViewById(R.id.tv_catalog_subtitle);
        llFilters    = view.findViewById(R.id.ll_catalog_filters);
        headerView   = view.findViewById(R.id.header_catalog);

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.surface_secondary);

        tvTitle.setText(getCatalogTitle());

        // 2-column grid with even spacing
        GridLayoutManager glm = new GridLayoutManager(requireContext(), 2);
        rvCatalog.setLayoutManager(glm);
        rvCatalog.addItemDecoration(new GridSpacingDecoration(dp(12)));

        adapter = new FilmCatalogAdapter(requireContext());
        adapter.setOnFilmClickListener(film -> {
            Intent intent = new Intent(requireContext(), FilmDetailActivity.class);
            intent.putExtra(FilmDetailActivity.EXTRA_FILM_ID, String.valueOf(film.getId()));
            startActivity(intent);
        });
        rvCatalog.setAdapter(adapter);

        // Reserve space for sticky header above the grid
        headerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int h = headerView.getHeight();
                if (h > 0) {
                    rvCatalog.setPadding(
                            dp(8), h + dp(8), dp(8), rvCatalog.getPaddingBottom());
                    headerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        buildFilters();

        swipeRefresh.setOnRefreshListener(this::fetch);
    }

    @Override
    protected void initData() {
        fetch();
    }

    private void buildFilters() {
        llFilters.removeAllViews();
        addFilterChip("Tất cả", FILTER_ALL);
        addFilterChip("Mới nhất", FILTER_NEW);
        addFilterChip("Premium", FILTER_PREMIUM);
        addFilterChip("Miễn phí", FILTER_FREE);
    }

    private void addFilterChip(String label, String filterKey) {
        TextView chip = new TextView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(8));
        chip.setLayoutParams(lp);
        chip.setBackgroundResource(R.drawable.bg_filter_chip);
        chip.setPadding(dp(16), dp(8), dp(16), dp(8));
        chip.setText(label);
        chip.setTextColor(getResources().getColor(R.color.text_primary));
        chip.setTextSize(12f);
        chip.setSelected(filterKey.equals(currentFilter));
        chip.setOnClickListener(v -> {
            currentFilter = filterKey;
            for (int i = 0; i < llFilters.getChildCount(); i++) {
                llFilters.getChildAt(i).setSelected(llFilters.getChildAt(i) == v);
            }
            applyFilter();
        });
        llFilters.addView(chip);
    }

    private void fetch() {
        if (adapter.getItemCount() == 0) {
            progress.setVisibility(View.VISIBLE);
        }
        tvError.setVisibility(View.GONE);

        ApiClient.getFilmApiService().getFilmsByType(getFilmType())
                .enqueue(new Callback<ApiResponseDto<List<FilmDto>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponseDto<List<FilmDto>>> call,
                                           @NonNull Response<ApiResponseDto<List<FilmDto>>> response) {
                        progress.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            allFilms.clear();
                            allFilms.addAll(response.body().getData());
                            applyFilter();
                        } else {
                            showError("Lỗi tải dữ liệu");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponseDto<List<FilmDto>>> call,
                                          @NonNull Throwable t) {
                        progress.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        if (adapter.getItemCount() == 0) {
                            showError("Không thể kết nối đến máy chủ");
                        } else {
                            Toast.makeText(requireContext(),
                                    "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void applyFilter() {
        List<FilmDto> filtered = new ArrayList<>();
        for (FilmDto f : allFilms) {
            switch (currentFilter) {
                case FILTER_NEW:
                    if (f.getReleaseYear() >= 2025) filtered.add(f);
                    break;
                case FILTER_PREMIUM:
                    if (f.getIsPremium()) filtered.add(f);
                    break;
                case FILTER_FREE:
                    if (!f.getIsPremium()) filtered.add(f);
                    break;
                case FILTER_ALL:
                default:
                    filtered.add(f);
                    break;
            }
        }
        adapter.submit(filtered);
        tvSubtitle.setText(filtered.size() + " tựa phim");

        if (filtered.isEmpty() && !allFilms.isEmpty()) {
            tvError.setText("Không có phim phù hợp với bộ lọc");
            tvError.setVisibility(View.VISIBLE);
        } else {
            tvError.setVisibility(View.GONE);
        }
    }

    private void showError(String msg) {
        if (adapter.getItemCount() == 0) {
            tvError.setText(msg);
            tvError.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private static class GridSpacingDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        GridSpacingDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
            outRect.left = spacing / 2;
            outRect.right = spacing / 2;
            outRect.top = spacing / 2;
            outRect.bottom = spacing / 2;
        }
    }
}

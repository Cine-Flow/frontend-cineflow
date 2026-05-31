package com.android.cineflow.ui.home;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.cineflow.R;
import com.android.cineflow.data.common.uimodel.HomeSection;
import com.android.cineflow.ui.base.BaseFragment;
import com.android.cineflow.ui.category.CategoryActivity;

import java.util.ArrayList;

public class HomeFragment extends BaseFragment {

    private HomeViewModel viewModel;
    private HomeAdapter homeAdapter;

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressLoading;
    private TextView tvError;
    private ListView lvHome;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initViews(View view) {
        swipeRefresh   = view.findViewById(R.id.swipe_refresh);
        lvHome         = view.findViewById(R.id.lv_home);
        progressLoading = view.findViewById(R.id.progress_loading);
        tvError        = view.findViewById(R.id.tv_error);

        // Style the swipe-refresh spinner to match the dark theme
        swipeRefresh.setColorSchemeColors(0xFFE50914); // FPT Play red
        swipeRefresh.setProgressBackgroundColorSchemeColor(0xFF1A1A1A);

        homeAdapter = new HomeAdapter(requireContext(), new ArrayList<>());

        // Navigate to appropriate activity on card tap
        homeAdapter.setOnItemClickListener((section, card) -> {
            if ("LIVE".equals(card.getContentType())) {
                String streamUrl = card.getStreamUrl();
                if (streamUrl != null && !streamUrl.isEmpty()) {
                    Intent intent = new Intent(requireContext(), com.android.cineflow.ui.player.PlayerActivity.class);
                    intent.putExtra(com.android.cineflow.ui.player.PlayerActivity.EXTRA_VIDEO_URL, streamUrl);
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), 
                        "Sự kiện chưa bắt đầu phát sóng", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent intent = new Intent(requireContext(), com.android.cineflow.ui.detail.FilmDetailActivity.class);
                intent.putExtra(com.android.cineflow.ui.detail.FilmDetailActivity.EXTRA_FILM_ID, card.getId());
                startActivity(intent);
            }
        });

        // Navigate to category list on "Xem thêm" tap
        homeAdapter.setOnSeeMoreClickListener(section -> {
            String categoryKey = mapSectionToCategoryKey(section);
            if (categoryKey != null) {
                Intent intent = new Intent(requireContext(), CategoryActivity.class);
                intent.putExtra(CategoryActivity.EXTRA_CATEGORY_TITLE, section.getTitle());
                intent.putExtra(CategoryActivity.EXTRA_CATEGORY_TYPE, categoryKey);
                startActivity(intent);
            }
        });

        lvHome.setAdapter(homeAdapter);

        // Pull-to-refresh
        swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Observe sections
        viewModel.getSections().observe(getViewLifecycleOwner(), sections -> {
            homeAdapter.setSections(sections);
            // Hide empty-state views when data arrives
            if (sections != null && !sections.isEmpty()) {
                lvHome.setVisibility(View.VISIBLE);
                tvError.setVisibility(View.GONE);
            }
        });

        // Observe loading state
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                // Show spinner only if the list is still empty (first load)
                boolean listEmpty = homeAdapter.getCount() == 0;
                progressLoading.setVisibility(listEmpty ? View.VISIBLE : View.GONE);
                tvError.setVisibility(View.GONE);
            } else {
                progressLoading.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
            }
        });

        // Observe error state
        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                if (homeAdapter.getCount() == 0) {
                    // No data loaded yet — show error TextView
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText(errorMsg);
                    lvHome.setVisibility(View.GONE);
                } else {
                    // Data already shown — show a Toast instead
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Maps a HomeSection title to the film type key expected by the backend
     * (GET /films?type=SERIES or type=SINGLE).
     */
    private String mapSectionToCategoryKey(HomeSection section) {
        if (section.getTitle() == null) return null;
        switch (section.getTitle()) {
            case "Phim bộ hot":
                return "SERIES";
            case "Cày phim hay mỗi ngày":
                return "SINGLE";
            case "Sự kiện Thể thao":
                return "LIVE";
            default:
                return null;
        }
    }
}

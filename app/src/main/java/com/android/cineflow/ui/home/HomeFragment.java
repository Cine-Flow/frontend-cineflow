package com.android.cineflow.ui.home;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.android.cineflow.R;
import com.android.cineflow.ui.base.BaseFragment;

import java.util.ArrayList;

public class HomeFragment extends BaseFragment {

    private HomeViewModel viewModel;
    private HomeAdapter homeAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initViews(View view) {
        ListView lvHome = view.findViewById(R.id.lv_home);

        homeAdapter = new HomeAdapter(requireContext(), new ArrayList<>());
        homeAdapter.setOnItemClickListener((section, card) -> {
            android.content.Intent intent = new android.content.Intent(requireContext(), com.android.cineflow.ui.player.PlayerActivity.class);
            // Defaulting to empty url, so PlayerActivity will use its fallback playable video
            intent.putExtra(com.android.cineflow.ui.player.PlayerActivity.EXTRA_VIDEO_URL, "");
            startActivity(intent);
        });
        lvHome.setAdapter(homeAdapter);
    }

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.getSections().observe(getViewLifecycleOwner(), sections -> {
            homeAdapter.setSections(sections);
        });
    }
}

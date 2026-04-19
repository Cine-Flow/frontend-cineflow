package com.android.cineflow.ui.premierleague;

import android.view.View;
import android.widget.ListView;

import androidx.lifecycle.ViewModelProvider;

import com.android.cineflow.R;
import com.android.cineflow.ui.base.BaseFragment;

import java.util.ArrayList;

public class PremierLeagueFragment extends BaseFragment {

    private PremierLeagueViewModel viewModel;
    private PremierLeagueAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_premier_league;
    }

    @Override
    protected void initViews(View view) {
        ListView lvPremierLeague = view.findViewById(R.id.lv_premier_league);
        adapter = new PremierLeagueAdapter(requireContext(), new ArrayList<>());
        lvPremierLeague.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(PremierLeagueViewModel.class);
        viewModel.getSections().observe(getViewLifecycleOwner(), sections -> {
            adapter.setSections(sections);
        });
    }
}

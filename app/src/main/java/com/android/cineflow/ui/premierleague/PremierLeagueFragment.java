package com.android.cineflow.ui.premierleague;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

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
        adapter = new PremierLeagueAdapter(requireContext(), new ArrayList<>(),
                mode -> viewModel.expandSection(mode),
                (apiDate, displayDate) -> viewModel.loadFixturesForDate(apiDate, displayDate));
        lvPremierLeague.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(PremierLeagueViewModel.class);
        viewModel.getSections().observe(getViewLifecycleOwner(), sections -> {
            adapter.setSections(sections);
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }
}

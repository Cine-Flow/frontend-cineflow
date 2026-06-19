package com.android.cineflow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.android.cineflow.ui.catalog.MoviesFragment;
import com.android.cineflow.ui.catalog.SeriesFragment;
import com.android.cineflow.ui.home.HomeFragment;
import com.android.cineflow.ui.more.MoreFragment;
import com.android.cineflow.ui.premierleague.PremierLeagueFragment;
import com.android.cineflow.ui.shorts.ShortsFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new HomeFragment();
            case 1: return new ShortsFragment();
            case 2: return new SeriesFragment();
            case 3: return new MoviesFragment();
            case 4: return new PremierLeagueFragment();
            case 5: return new MoreFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
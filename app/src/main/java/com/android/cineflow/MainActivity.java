package com.android.cineflow;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.android.cineflow.data.repository.PremierLeagueRepository;

public class MainActivity extends AppCompatActivity {

    private static final int PREMIER_LEAGUE_TAB_POSITION = 4;

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;
    private int selectedPagePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.view_pager);
        bottomNav = findViewById(R.id.bottom_nav);

        MainPagerAdapter adapter = new MainPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Synchronize ViewPager2 swipe with BottomNavigationView selection
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (selectedPagePosition == PREMIER_LEAGUE_TAB_POSITION
                        && position != PREMIER_LEAGUE_TAB_POSITION) {
                    PremierLeagueRepository.getInstance().fetchData();
                }
                selectedPagePosition = position;
                bottomNav.getMenu().getItem(position).setChecked(true);
            }
        });

        // Synchronize BottomNavigationView click with ViewPager2 scroll
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.homeFragment) {
                    viewPager.setCurrentItem(0);
                    return true;
                } else if (itemId == R.id.shortsFragment) {
                    viewPager.setCurrentItem(1);
                    return true;
                } else if (itemId == R.id.seriesFragment) {
                    viewPager.setCurrentItem(2);
                    return true;
                } else if (itemId == R.id.moviesFragment) {
                    viewPager.setCurrentItem(3);
                    return true;
                } else if (itemId == R.id.premierLeagueFragment) {
                    viewPager.setCurrentItem(4);
                    return true;
                } else if (itemId == R.id.moreFragment) {
                    viewPager.setCurrentItem(5);
                    return true;
                }
                return false;
            }
        });

        bottomNav.setOnItemReselectedListener(item -> {
            if (item.getItemId() == R.id.premierLeagueFragment) {
                PremierLeagueRepository.getInstance().fetchData();
            }
        });

        // Optional: Disable smooth scroll if you want instant tab switching
        // bottomNav.setOnItemSelectedListener(item -> {
        //     viewPager.setCurrentItem(index, false);
        //     return true;
        // });
        // Verify saved authentication token on startup
        com.android.cineflow.data.auth.AuthManager authManager = com.android.cineflow.data.auth.AuthManager.getInstance();
        if (authManager != null && authManager.isLoggedIn()) {
            com.android.cineflow.data.network.ApiClient.getFilmApiService().getProfile()
                    .enqueue(new com.android.cineflow.data.network.Callback<com.android.cineflow.data.network.dto.ApiResponseDto<com.android.cineflow.data.network.dto.UserProfileDto>>() {
                        @Override
                        public void onResponse(com.android.cineflow.data.network.Call<com.android.cineflow.data.network.dto.ApiResponseDto<com.android.cineflow.data.network.dto.UserProfileDto>> call,
                                               com.android.cineflow.data.network.Response<com.android.cineflow.data.network.dto.ApiResponseDto<com.android.cineflow.data.network.dto.UserProfileDto>> response) {
                            if (!response.isSuccessful() && response.code() == 401) {
                                authManager.clearSession();
                            }
                        }

                        @Override
                        public void onFailure(com.android.cineflow.data.network.Call<com.android.cineflow.data.network.dto.ApiResponseDto<com.android.cineflow.data.network.dto.UserProfileDto>> call, Throwable t) {
                            // Offline or network error: keep session to allow offline access
                        }
                    });
        }
    }
}

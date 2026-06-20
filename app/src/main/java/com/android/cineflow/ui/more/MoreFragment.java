package com.android.cineflow.ui.more;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import androidx.viewpager2.widget.ViewPager2;

import com.android.cineflow.MainActivity;
import com.android.cineflow.R;
import com.android.cineflow.data.auth.AuthManager;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.UserProfileDto;
import com.android.cineflow.data.settings.SettingsManager;
import com.android.cineflow.ui.admin.AdminDashboardActivity;
import com.android.cineflow.ui.auth.LoginActivity;
import com.android.cineflow.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

public class MoreFragment extends BaseFragment {

    private MoreAdapter moreAdapter;
    private static final int REQUEST_LOGIN = 1001;
    private boolean isFirstLoad = true;

    private final AuthManager.AuthListener authListener = new AuthManager.AuthListener() {
        @Override
        public void onAuthStatusChanged(boolean isLoggedIn) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    buildSections();
                    if (isLoggedIn) {
                        loadProfileStats();
                    } else {
                        moreAdapter.setProfileStats(0, 0);
                    }
                });
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_more;
    }

    @Override
    protected void initViews(View view) {
        ListView lvMore = view.findViewById(R.id.lv_more);
        moreAdapter = new MoreAdapter(requireContext(), new ArrayList<>(), this::handleGridItem);
        lvMore.setAdapter(moreAdapter);

        lvMore.setOnItemClickListener((parent, v, position, id) -> {
            MoreSection section = (MoreSection) moreAdapter.getItem(position);
            if (section == null) return;

            switch (section.getType()) {
                case MoreSection.TYPE_HEADER_LOGIN:
                    openAccountOrLogin(null);
                    break;
                case MoreSection.TYPE_DOWNLOAD_BANNER:
                    startActivity(new Intent(requireContext(), DownloadManagerActivity.class));
                    break;
                case MoreSection.TYPE_ADMIN_ENTRY:
                    startActivity(new Intent(requireContext(), AdminDashboardActivity.class));
                    break;
                case MoreSection.TYPE_LOGOUT:
                    handleSignButton();
                    break;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        AuthManager auth = AuthManager.getInstance();
        if (auth != null) {
            auth.addListener(authListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AuthManager auth = AuthManager.getInstance();
        if (auth != null) {
            auth.removeListener(authListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstLoad) {
            isFirstLoad = false;
            return;
        }
        buildSections();
        loadProfileStats();
    }

    @Override
    protected void initData() {
        buildSections();
        loadProfileStats();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN && resultCode == getActivity().RESULT_OK) {
            buildSections();
            loadProfileStats();
        }
    }

    private void buildSections() {
        List<MoreSection> data = new ArrayList<>();
        AuthManager authManager = AuthManager.getInstance();
        boolean isLoggedIn = authManager != null && authManager.isLoggedIn();

        // 0. Admin Panel (only if user is admin)
        if (isLoggedIn && authManager.isAdmin()) {
            data.add(new MoreSection(MoreSection.TYPE_ADMIN_ENTRY, null, null));
        }

        // 1. Header (shows "Đăng nhập" or username)
        String headerTitle = isLoggedIn ? authManager.getUsername() : null;
        data.add(new MoreSection(MoreSection.TYPE_HEADER_LOGIN, headerTitle, null));

        // 2. Download
        data.add(new MoreSection(MoreSection.TYPE_DOWNLOAD_BANNER, null, null));

        // 3. Action Grid
        data.add(new MoreSection(MoreSection.TYPE_ACTION_GRID, null, Arrays.asList(
                new MoreSection.MoreItem(getString(R.string.more_library), R.drawable.ic_library, null, MoreSection.ACTION_LIBRARY),
                new MoreSection.MoreItem(getString(R.string.more_analytics), R.drawable.ic_admin_stats, null, MoreSection.ACTION_ANALYTICS),
                new MoreSection.MoreItem(getString(R.string.more_favorites), R.drawable.ic_heart, null, MoreSection.ACTION_FAVORITES)
        )));

        // 4. App Settings & Support
        data.add(new MoreSection(MoreSection.TYPE_ACTION_GRID, getString(R.string.more_settings_support), Arrays.asList(
                new MoreSection.MoreItem(getString(R.string.more_settings), R.drawable.ic_settings, null, MoreSection.ACTION_SETTINGS),
                new MoreSection.MoreItem(getString(R.string.more_theme), R.drawable.ic_launcher_foreground, null, MoreSection.ACTION_THEME),
                new MoreSection.MoreItem(getString(R.string.more_help), R.drawable.ic_help, null, MoreSection.ACTION_SUPPORT),
                new MoreSection.MoreItem(getString(R.string.more_terms), R.drawable.ic_shield, null, MoreSection.ACTION_TERMS)
        )));

        // 5. Sign In / Sign Out button (always visible)
        boolean isLogged = authManager != null && authManager.isLoggedIn();
        data.add(new MoreSection(MoreSection.TYPE_LOGOUT, isLogged ? getString(R.string.more_sign_out) : getString(R.string.more_sign_in), null));

        moreAdapter.setData(data);
    }

    private void handleSignButton() {
        AuthManager auth = AuthManager.getInstance();
        boolean isLoggedIn = auth != null && auth.isLoggedIn();

        if (isLoggedIn) {
            performSignOut();
        } else {
            startActivityForResult(
                    new Intent(requireContext(), LoginActivity.class),
                    REQUEST_LOGIN);
        }
    }

    private void handleGridItem(MoreSection.MoreItem item) {
        if (MoreSection.ACTION_LIBRARY.equals(item.getAction())) {
            openProtectedList(UserContentListActivity.MODE_LIBRARY);
        } else if (MoreSection.ACTION_FAVORITES.equals(item.getAction())) {
            openProtectedList(UserContentListActivity.MODE_FAVORITES);
        } else if (MoreSection.ACTION_ANALYTICS.equals(item.getAction())) {
            openProtectedAnalytics();
        } else if (MoreSection.ACTION_SETTINGS.equals(item.getAction())) {
            openAccountOrLogin("settings");
        } else if (MoreSection.ACTION_THEME.equals(item.getAction())) {
            showThemeSelectionDialog();
        } else if (MoreSection.ACTION_SUPPORT.equals(item.getAction())) {
            openAccountOrLogin("support");
        } else if (MoreSection.ACTION_TERMS.equals(item.getAction())) {
            openAccountOrLogin("terms");
        } else {
            String label = item.getLabel();
            if (label != null) {
                android.widget.Toast.makeText(requireContext(), getString(R.string.toast_feature_under_development, label), android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showThemeSelectionDialog() {
        String[] options = {
                getString(R.string.theme_system),
                getString(R.string.theme_light),
                getString(R.string.theme_dark)
        };
        int currentMode = SettingsManager.getInstance().getThemeMode();
        int checkedItem = 0;
        if (currentMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO) {
            checkedItem = 1;
        } else if (currentMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) {
            checkedItem = 2;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_theme_title)
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    int selectedMode;
                    if (which == 1) {
                        selectedMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
                    } else if (which == 2) {
                        selectedMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
                    } else {
                        selectedMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    }
                    SettingsManager.getInstance().setThemeMode(selectedMode);
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(selectedMode);
                    dialog.dismiss();
                })
                .show();
    }

    private void openProtectedAnalytics() {
        AuthManager auth = AuthManager.getInstance();
        if (auth == null || !auth.isLoggedIn()) {
            openLogin();
            return;
        }
        startActivity(new Intent(requireContext(), UserAnalyticsActivity.class));
    }

    private void openProtectedList(String mode) {
        AuthManager auth = AuthManager.getInstance();
        if (auth == null || !auth.isLoggedIn()) {
            openLogin();
            return;
        }
        Intent intent = new Intent(requireContext(), UserContentListActivity.class);
        intent.putExtra(UserContentListActivity.EXTRA_MODE, mode);
        startActivity(intent);
    }

    private void openAccountOrLogin(String targetSection) {
        AuthManager auth = AuthManager.getInstance();
        if (auth == null || !auth.isLoggedIn()) {
            openLogin();
        } else {
            Intent intent = new Intent(requireContext(), AccountActivity.class);
            if (targetSection != null) {
                intent.putExtra("target_section", targetSection);
            }
            startActivity(intent);
        }
    }

    private void openLogin() {
        startActivityForResult(new Intent(requireContext(), LoginActivity.class), REQUEST_LOGIN);
    }

    private void performSignOut() {
        AuthManager auth = AuthManager.getInstance();
        if (auth != null) auth.clearSession();
        buildSections();
        moreAdapter.setProfileStats(0, 0);

        // Navigate to Home tab
        if (getActivity() instanceof MainActivity) {
            ViewPager2 viewPager = ((MainActivity) getActivity()).findViewById(R.id.view_pager);
            if (viewPager != null) {
                viewPager.setCurrentItem(0, false);
            }
        }
    }

    private void loadProfileStats() {
        AuthManager auth = AuthManager.getInstance();
        if (auth == null || !auth.isLoggedIn()) return;
        ApiClient.getFilmApiService().getProfile().enqueue(new Callback<ApiResponseDto<UserProfileDto>>() {
            @Override public void onResponse(Call<ApiResponseDto<UserProfileDto>> call, Response<ApiResponseDto<UserProfileDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserProfileDto profile = response.body().getData();
                    moreAdapter.setProfileStats(profile.getFullName(), profile.getFavoriteCount(), profile.getWatchHistoryCount());
                } else if (response.code() == 401) {
                    AuthManager auth = AuthManager.getInstance();
                    if (auth != null) {
                        auth.clearSession();
                    }
                }
            }
            @Override public void onFailure(Call<ApiResponseDto<UserProfileDto>> call, Throwable t) {}
        });
    }
}

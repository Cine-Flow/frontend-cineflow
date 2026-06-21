package com.android.cineflow.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.cineflow.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends com.android.cineflow.ui.base.BaseActivity {

    public static final String EXTRA_MODULE_ID = "module_id";
    public static final String MODULE_FILMS = "films";
    public static final String MODULE_USERS = "users";
    public static final String MODULE_CATEGORIES = "categories";
    public static final String MODULE_STATS = "stats";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Admin Panel");
        }

        buildModuleGrid();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_admin_logout) {
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Admins can't return to a user-facing screen — back from dashboard exits the app
        finishAffinity();
    }

    private void confirmLogout() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất khỏi tài khoản quản trị?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đăng xuất", (d, w) -> performLogout())
                .show();
    }

    private void performLogout() {
        com.android.cineflow.data.auth.AuthManager auth =
                com.android.cineflow.data.auth.AuthManager.getInstance();
        if (auth != null) auth.clearSession();
        Intent intent = new Intent(this, com.android.cineflow.ui.auth.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void buildModuleGrid() {
        android.widget.GridLayout grid = findViewById(R.id.grid_admin_modules);
        grid.removeAllViews();

        List<AdminModule> modules = new ArrayList<>();
        modules.add(new AdminModule(MODULE_FILMS, "Films", "Manage movies & series", R.drawable.ic_admin_films, R.color.brand_primary));
        modules.add(new AdminModule(MODULE_USERS, "Users", "Manage accounts & roles", R.drawable.ic_admin_users, R.color.badge_series));
        modules.add(new AdminModule(MODULE_CATEGORIES, "Categories", "Manage genres & tags", R.drawable.ic_admin_categories, R.color.status_success));
        modules.add(new AdminModule(MODULE_STATS, "Statistics", "View analytics & reports", R.drawable.ic_admin_stats, R.color.badge_movie));

        LayoutInflater inflater = LayoutInflater.from(this);
        for (AdminModule module : modules) {
            View card = inflater.inflate(R.layout.item_admin_module, grid, false);

            ImageView ivIcon = card.findViewById(R.id.iv_module_icon);
            TextView tvLabel = card.findViewById(R.id.tv_module_label);
            TextView tvDesc = card.findViewById(R.id.tv_module_description);

            ivIcon.setImageResource(module.iconRes);
            ivIcon.setColorFilter(getColor(module.tintRes));
            tvLabel.setText(module.label);
            tvDesc.setText(module.description);

            card.setOnClickListener(v -> onModuleClicked(module.id));
            grid.addView(card);
        }
    }

    private void onModuleClicked(String moduleId) {
        switch (moduleId) {
            case MODULE_FILMS:
                startActivity(new Intent(this, AdminFilmListActivity.class));
                break;
            case MODULE_USERS:
                startActivity(new Intent(this, AdminUserListActivity.class));
                break;
            case MODULE_CATEGORIES:
                startActivity(new Intent(this, AdminCategoryListActivity.class));
                break;
            case MODULE_STATS:
                startActivity(new Intent(this, AdminAnalyticsActivity.class));
                break;
        }
    }

    static class AdminModule {
        final String id;
        final String label;
        final String description;
        final int iconRes;
        final int tintRes;

        AdminModule(String id, String label, String description, int iconRes, int tintRes) {
            this.id = id;
            this.label = label;
            this.description = description;
            this.iconRes = iconRes;
            this.tintRes = tintRes;
        }
    }
}

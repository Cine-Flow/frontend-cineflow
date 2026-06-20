package com.android.cineflow.ui.admin;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminUserListActivity extends com.android.cineflow.ui.base.BaseActivity
        implements AdminUserAdapter.OnUserActionListener {

    private AdminUserAdapter adapter;
    private TextView tvUserCount;
    private EditText etSearch;
    private final List<AdminUserAdapter.MockUser> allUsers = new ArrayList<>();
    private String filter = "ALL"; // ALL | PREMIUM | FREE
    private TextView chipAll, chipPremium, chipFree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Management");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvUserCount = findViewById(R.id.tv_user_count);
        etSearch = findViewById(R.id.et_search);
        chipAll = findViewById(R.id.chip_all);
        chipPremium = findViewById(R.id.chip_premium);
        chipFree = findViewById(R.id.chip_free);
        ImageView fab = findViewById(R.id.fab_add_user);

        RecyclerView rv = findViewById(R.id.rv_users);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(this);
        rv.setAdapter(adapter);

        seedMockUsers();
        refreshChips();
        applyFilter();

        chipAll.setOnClickListener(v -> { filter = "ALL"; refreshChips(); applyFilter(); });
        chipPremium.setOnClickListener(v -> { filter = "PREMIUM"; refreshChips(); applyFilter(); });
        chipFree.setOnClickListener(v -> { filter = "FREE"; refreshChips(); applyFilter(); });

        fab.setOnClickListener(v -> showForm(null));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { applyFilter(); }
        });
    }

    private void refreshChips() {
        setChipState(chipAll, "ALL".equals(filter));
        setChipState(chipPremium, "PREMIUM".equals(filter));
        setChipState(chipFree, "FREE".equals(filter));
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

    private void applyFilter() {
        String q = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<AdminUserAdapter.MockUser> filtered = new ArrayList<>();
        for (AdminUserAdapter.MockUser u : allUsers) {
            boolean filterMatch;
            switch (filter) {
                case "PREMIUM": filterMatch = u.subscriptionPlan != null; break;
                case "FREE":    filterMatch = u.subscriptionPlan == null; break;
                default:        filterMatch = true;
            }
            boolean searchMatch = q.isEmpty()
                    || u.username.toLowerCase(Locale.ROOT).contains(q)
                    || u.email.toLowerCase(Locale.ROOT).contains(q)
                    || (u.fullName != null && u.fullName.toLowerCase(Locale.ROOT).contains(q))
                    || (u.phoneNumber != null && u.phoneNumber.contains(q));
            if (filterMatch && searchMatch) filtered.add(u);
        }
        adapter.setUsers(filtered);
        tvUserCount.setText(filtered.size() + " of " + allUsers.size() + " users");
    }

    @Override
    public void onEditUser(AdminUserAdapter.MockUser user) {
        showForm(user);
    }

    @Override
    public void onResetPassword(AdminUserAdapter.MockUser user) {
        new AlertDialog.Builder(this)
                .setTitle("Send password reset?")
                .setMessage("A reset link will be emailed to " + user.email
                        + ". The user must set a new password before signing in again.")
                .setPositiveButton("Send link", (d, w) -> Toast.makeText(this,
                        "Reset email queued for " + user.email, Toast.LENGTH_SHORT).show())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteUser(AdminUserAdapter.MockUser user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete user")
                .setMessage("Permanently delete @" + user.username + "?\n\n"
                        + "Their subscriptions, watch history, and favorites will be removed.")
                .setPositiveButton("Delete", (d, w) -> {
                    allUsers.remove(user);
                    applyFilter();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showForm(AdminUserAdapter.MockUser existing) {
        UserFormDialogFragment d = UserFormDialogFragment.newInstance(existing);
        d.setOnUserSavedListener((user, isNew) -> {
            if (isNew) allUsers.add(0, user);
            applyFilter();
            Toast.makeText(this,
                    (isNew ? "Created @" : "Updated @") + user.username,
                    Toast.LENGTH_SHORT).show();
        });
        d.show(getSupportFragmentManager(), "user_form");
    }

    private void seedMockUsers() {
        allUsers.add(new AdminUserAdapter.MockUser(
                "8f1c-...-a01", "ankhang", "ankhang@cineflow.io",
                "An Khang", "+84 901 234 567",
                "https://i.pravatar.cc/150?img=12",
                "ROLE_ADMIN", "Jan 12, 2024",
                null, null));
        allUsers.add(new AdminUserAdapter.MockUser(
                "8f1c-...-a02", "linh.tran", "linh.tran@gmail.com",
                "Linh Tran", "+84 912 345 678",
                "https://i.pravatar.cc/150?img=32",
                "ROLE_USER", "Feb 04, 2024",
                "Premium Monthly", "Jul 04, 2026"));
        allUsers.add(new AdminUserAdapter.MockUser(
                "8f1c-...-a03", "minhpham", "minh.pham@yahoo.com",
                "Minh Pham", null,
                "https://i.pravatar.cc/150?img=15",
                "ROLE_USER", "Mar 22, 2024",
                null, null));
        allUsers.add(new AdminUserAdapter.MockUser(
                "8f1c-...-a04", "hoang.nguyen", "hoa.nguyen@outlook.com",
                "Hoa Nguyen", "+84 938 776 221", null,
                "ROLE_USER", "Apr 09, 2024",
                "Premium Annual", "Apr 09, 2026"));
        allUsers.add(new AdminUserAdapter.MockUser(
                "8f1c-...-a05", "tuanle", "tuan.le@cineflow.io",
                "Tuan Le", "+84 905 112 998",
                "https://i.pravatar.cc/150?img=8",
                "ROLE_ADMIN", "May 15, 2024",
                null, null));
        allUsers.add(new AdminUserAdapter.MockUser(
                "8f1c-...-a06", "maibui", "mai.bui@gmail.com",
                "Mai Bui", null, null,
                "ROLE_USER", "Jun 01, 2024",
                "Premium Monthly", "Jun 18, 2026"));
        allUsers.add(new AdminUserAdapter.MockUser(
                "8f1c-...-a07", "khoavu", "khoa.vu@hotmail.com",
                null, "+84 916 442 003", null,
                "ROLE_USER", "Jul 18, 2024",
                null, null));
        allUsers.add(new AdminUserAdapter.MockUser(
                "8f1c-...-a08", "trangdo", "trang.do@gmail.com",
                "Trang Do", null,
                "https://i.pravatar.cc/150?img=47",
                "ROLE_USER", "Aug 27, 2024",
                "Premium Annual", "Aug 27, 2026"));
        allUsers.add(new AdminUserAdapter.MockUser(
                "8f1c-...-a09", "baohoang", "bao.hoang@cineflow.io",
                "Bao Hoang", "+84 902 884 117", null,
                "ROLE_USER", "Sep 30, 2024",
                null, null));
        allUsers.add(new AdminUserAdapter.MockUser(
                "8f1c-...-a10", "yenphan", "yen.phan@gmail.com",
                "Yen Phan", "+84 977 339 020",
                "https://i.pravatar.cc/150?img=25",
                "ROLE_USER", "Oct 11, 2024",
                "Premium Monthly", "Jul 11, 2026"));
    }
}

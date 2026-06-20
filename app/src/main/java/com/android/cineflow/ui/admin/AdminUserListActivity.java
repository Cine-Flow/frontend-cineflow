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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;
import com.android.cineflow.data.network.dto.AdminUserDto;
import com.android.cineflow.data.network.dto.AdminUserRequestDto;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AdminUserListActivity extends com.android.cineflow.ui.base.BaseActivity
        implements AdminUserAdapter.OnUserActionListener {

    private AdminUserAdapter adapter;
    private TextView tvUserCount;
    private EditText etSearch;
    private final List<AdminUserDto> allUsers = new ArrayList<>();
    private String filter = "ALL";
    private String roleFilter = "ALL";
    private TextView chipAll, chipPremium, chipFree;
    private TextView chipRoleAll, chipRoleAdmin, chipRoleUser;

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
        chipRoleAll = findViewById(R.id.chip_role_all);
        chipRoleAdmin = findViewById(R.id.chip_role_admin);
        chipRoleUser = findViewById(R.id.chip_role_user);
        ImageView fab = findViewById(R.id.fab_add_user);

        RecyclerView rv = findViewById(R.id.rv_users);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(this);
        rv.setAdapter(adapter);

        refreshChips();
        chipAll.setOnClickListener(v -> { filter = "ALL"; refreshChips(); loadUsers(); });
        chipPremium.setOnClickListener(v -> { filter = "PREMIUM"; refreshChips(); loadUsers(); });
        chipFree.setOnClickListener(v -> { filter = "FREE"; refreshChips(); loadUsers(); });
        chipRoleAll.setOnClickListener(v -> { roleFilter = "ALL"; refreshChips(); loadUsers(); });
        chipRoleAdmin.setOnClickListener(v -> { roleFilter = "ROLE_ADMIN"; refreshChips(); loadUsers(); });
        chipRoleUser.setOnClickListener(v -> { roleFilter = "ROLE_USER"; refreshChips(); loadUsers(); });
        fab.setOnClickListener(v -> showForm(null));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { loadUsers(); }
        });

        loadUsers();
    }

    private void refreshChips() {
        setChipState(chipAll, "ALL".equals(filter));
        setChipState(chipPremium, "PREMIUM".equals(filter));
        setChipState(chipFree, "FREE".equals(filter));
        setChipState(chipRoleAll, "ALL".equals(roleFilter));
        setChipState(chipRoleAdmin, "ROLE_ADMIN".equals(roleFilter));
        setChipState(chipRoleUser, "ROLE_USER".equals(roleFilter));
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

    private void loadUsers() {
        String q = etSearch.getText().toString().trim();
        ApiClient.getFilmApiService().getAdminUsers(q, roleFilter, filter)
                .enqueue(new Callback<ApiResponseDto<List<AdminUserDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<List<AdminUserDto>>> call,
                                           Response<ApiResponseDto<List<AdminUserDto>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            allUsers.clear();
                            allUsers.addAll(response.body().getData());
                            adapter.setUsers(allUsers);
                            tvUserCount.setText(allUsers.size() + " users");
                        } else {
                            Toast.makeText(AdminUserListActivity.this,
                                    "Cannot load users", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<List<AdminUserDto>>> call, Throwable t) {
                        Toast.makeText(AdminUserListActivity.this,
                                "Server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onEditUser(AdminUserDto user) {
        showForm(user);
    }

    @Override
    public void onResetPassword(AdminUserDto user) {
        new AlertDialog.Builder(this)
                .setTitle("Send password reset?")
                .setMessage("A reset link will be emailed to " + user.getEmail()
                        + ". The user must set a new password before signing in again.")
                .setPositiveButton("Send link", (d, w) -> resetPassword(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteUser(AdminUserDto user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete user")
                .setMessage("Permanently delete @" + user.getUsername() + "?\n\n"
                        + "Their subscriptions, watch history, and favorites will be removed.")
                .setPositiveButton("Delete", (d, w) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showForm(AdminUserDto existing) {
        UserFormDialogFragment d = UserFormDialogFragment.newInstance(existing);
        d.setOnUserSavedListener((request, editing) -> {
            if (editing == null) {
                createUser(request);
            } else {
                updateUser(editing.getId(), request);
            }
        });
        d.show(getSupportFragmentManager(), "user_form");
    }

    private void createUser(AdminUserRequestDto request) {
        ApiClient.getFilmApiService().createUser(request).enqueue(userMutationCallback("Created user"));
    }

    private void updateUser(String id, AdminUserRequestDto request) {
        ApiClient.getFilmApiService().updateUser(id, request).enqueue(userMutationCallback("Updated user"));
    }

    private void resetPassword(AdminUserDto user) {
        ApiClient.getFilmApiService().resetUserPassword(user.getId())
                .enqueue(new Callback<ApiResponseDto<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<Void>> call, Response<ApiResponseDto<Void>> response) {
                        Toast.makeText(AdminUserListActivity.this,
                                response.isSuccessful() ? "Reset email sent" : "Cannot send reset email",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                        Toast.makeText(AdminUserListActivity.this,
                                "Server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteUser(AdminUserDto user) {
        ApiClient.getFilmApiService().deleteUser(user.getId())
                .enqueue(new Callback<ApiResponseDto<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<Void>> call, Response<ApiResponseDto<Void>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminUserListActivity.this, "Deleted user", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        } else {
                            Toast.makeText(AdminUserListActivity.this, "Cannot delete user", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                        Toast.makeText(AdminUserListActivity.this,
                                "Server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Callback<ApiResponseDto<AdminUserDto>> userMutationCallback(String successMessage) {
        return new Callback<ApiResponseDto<AdminUserDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<AdminUserDto>> call,
                                   Response<ApiResponseDto<AdminUserDto>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminUserListActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    Toast.makeText(AdminUserListActivity.this, "Cannot save user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<AdminUserDto>> call, Throwable t) {
                Toast.makeText(AdminUserListActivity.this,
                        "Server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }
}

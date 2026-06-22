package com.android.cineflow.ui.admin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.cineflow.R;
import com.android.cineflow.data.network.dto.AdminUserDto;
import com.android.cineflow.data.repository.AdminUserRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class AdminUserListActivity extends com.android.cineflow.ui.base.BaseActivity
        implements AdminUserAdapter.OnUserActionListener {

    private AdminUserRepository repository;
    private AdminUserAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvUserCount;
    private EditText etSearch;
    private MaterialButton btnPrevPage;
    private MaterialButton btnNextPage;
    private TextView tvPageInfo;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        repository = AdminUserRepository.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.admin_title_user_management);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progress_bar);
        tvUserCount = findViewById(R.id.tv_user_count);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        etSearch = findViewById(R.id.et_search);
        btnPrevPage = findViewById(R.id.btn_prev_page);
        btnNextPage = findViewById(R.id.btn_next_page);
        tvPageInfo = findViewById(R.id.tv_page_info);

        RecyclerView rv = findViewById(R.id.rv_users);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(this);
        rv.setAdapter(adapter);

        swipeRefresh.setColorSchemeColors(getColor(R.color.brand_primary));
        swipeRefresh.setOnRefreshListener(() -> {
            repository.fetchFirstPage(etSearch.getText().toString().trim());
            swipeRefresh.setRefreshing(false);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> repository.fetchFirstPage(s.toString().trim());
                searchHandler.postDelayed(searchRunnable, 400);
            }
        });

        btnPrevPage.setOnClickListener(v -> repository.fetchPrevPage());
        btnNextPage.setOnClickListener(v -> repository.fetchNextPage());

        observeData();
        repository.fetchFirstPage(null);
    }

    private void observeData() {
        repository.getUsers().observe(this, users -> {
            adapter.setUsers(users);
            updatePaginationUI();
        });

        repository.getLoading().observe(this, loading -> {
            progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });

        repository.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                tvUserCount.setText(error);
            }
        });
    }

    private void updatePaginationUI() {
        long total = repository.getTotalElements();
        int page = repository.getCurrentPage();
        int totalPages = repository.getTotalPages();

        tvUserCount.setText(total + " users total");

        int start = total > 0 ? page * 10 + 1 : 0;
        int end = (int) Math.min((page + 1) * 10L, total);
        tvPageInfo.setText((page + 1) + " / " + totalPages + "  (" + start + "-" + end + ")");

        btnPrevPage.setEnabled(page > 0);
        btnNextPage.setEnabled(repository.hasMore());
    }

    @Override
    public void onEditUser(AdminUserDto user) {
        showForm(user);
    }

    @Override
    public void onResetPassword(AdminUserDto user) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_dialog_send_reset_title)
                .setMessage(getString(R.string.admin_dialog_send_reset_msg, user.getEmail()))
                .setPositiveButton(R.string.admin_button_send_link, (d, w) ->
                        repository.resetUserPassword(user.getId(), new AdminUserRepository.OnResultListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(AdminUserListActivity.this,
                                        R.string.admin_toast_reset_email_sent, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(AdminUserListActivity.this,
                                        getString(R.string.admin_toast_server_error_format, message), Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton(R.string.admin_button_cancel, null)
                .show();
    }

    @Override
    public void onDeleteUser(AdminUserDto user) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_dialog_delete_user_title)
                .setMessage(getString(R.string.admin_dialog_delete_user_msg, user.getUsername()))
                .setPositiveButton(R.string.admin_button_delete, (d, w) ->
                        repository.deleteUser(user.getId(), new AdminUserRepository.OnResultListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(AdminUserListActivity.this,
                                        R.string.admin_toast_deleted_user, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(AdminUserListActivity.this,
                                        R.string.admin_toast_cannot_delete_user, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton(R.string.admin_button_cancel, null)
                .show();
    }

    private void showForm(AdminUserDto existing) {
        UserFormDialogFragment d = UserFormDialogFragment.newInstance(existing);
        d.setOnUserSavedListener((request, editing) ->
                repository.updateUser(editing.getId(), request, new AdminUserRepository.OnResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AdminUserListActivity.this,
                                R.string.admin_toast_updated_user, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AdminUserListActivity.this,
                                R.string.admin_toast_cannot_save_user, Toast.LENGTH_SHORT).show();
                    }
                }));
        d.show(getSupportFragmentManager(), "user_form");
    }
}

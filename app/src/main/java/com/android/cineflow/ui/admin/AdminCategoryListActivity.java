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
import com.android.cineflow.data.network.dto.AdminCategoryDto;
import com.android.cineflow.data.network.dto.AdminCategoryRequestDto;
import com.android.cineflow.data.repository.AdminCategoryRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class AdminCategoryListActivity extends com.android.cineflow.ui.base.BaseActivity
        implements AdminCategoryAdapter.OnCategoryActionListener {

    private AdminCategoryRepository repository;
    private AdminCategoryAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvCategoryCount;
    private EditText etSearch;
    private MaterialButton btnPrevPage;
    private MaterialButton btnNextPage;
    private TextView tvPageInfo;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category_list);

        repository = AdminCategoryRepository.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.admin_title_category_management);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progress_bar);
        tvCategoryCount = findViewById(R.id.tv_category_count);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        etSearch = findViewById(R.id.et_search);
        btnPrevPage = findViewById(R.id.btn_prev_page);
        btnNextPage = findViewById(R.id.btn_next_page);
        tvPageInfo = findViewById(R.id.tv_page_info);

        RecyclerView rv = findViewById(R.id.rv_categories);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCategoryAdapter(this);
        rv.setAdapter(adapter);

        swipeRefresh.setColorSchemeColors(getColor(R.color.brand_primary));
        swipeRefresh.setOnRefreshListener(() -> {
            repository.fetchFirstPage(etSearch.getText().toString().trim());
            swipeRefresh.setRefreshing(false);
        });

        findViewById(R.id.fab_add_category).setOnClickListener(v -> showForm(null));

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
        repository.getCategories().observe(this, categories -> {
            adapter.setCategories(categories);
            updatePaginationUI();
        });

        repository.getLoading().observe(this, loading -> {
            progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });

        repository.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                tvCategoryCount.setText(error);
            }
        });
    }

    private void updatePaginationUI() {
        long total = repository.getTotalElements();
        int page = repository.getCurrentPage();
        int totalPages = repository.getTotalPages();

        tvCategoryCount.setText(total + " categories total");

        int start = total > 0 ? page * 10 + 1 : 0;
        int end = (int) Math.min((page + 1) * 10L, total);
        tvPageInfo.setText((page + 1) + " / " + totalPages + "  (" + start + "-" + end + ")");

        btnPrevPage.setEnabled(page > 0);
        btnNextPage.setEnabled(repository.hasMore());
    }

    @Override
    public void onEditCategory(AdminCategoryDto category) {
        showForm(category);
    }

    @Override
    public void onDeleteCategory(AdminCategoryDto category) {
        String msg = category.getFilmCount() == 0
                ? getString(R.string.admin_dialog_delete_category_simple, category.getName())
                : getString(R.string.admin_dialog_delete_category_warning, category.getName(), category.getFilmCount());
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_dialog_delete_category_title)
                .setMessage(msg)
                .setPositiveButton(R.string.admin_button_delete, (d, w) ->
                        repository.deleteCategory(category.getId(), new AdminCategoryRepository.OnResultListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(AdminCategoryListActivity.this,
                                        R.string.admin_toast_deleted_category, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(AdminCategoryListActivity.this,
                                        R.string.admin_toast_cannot_delete_category, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton(R.string.admin_button_cancel, null)
                .show();
    }

    private void showForm(AdminCategoryDto existing) {
        CategoryFormDialogFragment d = CategoryFormDialogFragment.newInstance(existing);
        d.setOnCategorySavedListener((request, editing) -> {
            if (editing == null) {
                repository.createCategory(request, new AdminCategoryRepository.OnResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AdminCategoryListActivity.this,
                                R.string.admin_toast_created_category, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AdminCategoryListActivity.this,
                                R.string.admin_toast_cannot_save_category, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                repository.updateCategory(editing.getId(), request, new AdminCategoryRepository.OnResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AdminCategoryListActivity.this,
                                R.string.admin_toast_updated_category, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AdminCategoryListActivity.this,
                                R.string.admin_toast_cannot_save_category, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        d.show(getSupportFragmentManager(), "category_form");
    }
}

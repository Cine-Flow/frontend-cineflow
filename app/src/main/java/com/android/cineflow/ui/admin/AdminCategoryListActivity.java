package com.android.cineflow.ui.admin;

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
import com.android.cineflow.data.network.dto.AdminCategoryDto;
import com.android.cineflow.data.network.dto.AdminCategoryRequestDto;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminCategoryListActivity extends com.android.cineflow.ui.base.BaseActivity
        implements AdminCategoryAdapter.OnCategoryActionListener {

    private AdminCategoryAdapter adapter;
    private TextView tvCategoryCount;
    private TextView tvSummaryTotal, tvSummaryFilms, tvSummaryEmpty;
    private EditText etSearch;
    private final List<AdminCategoryDto> all = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Category Management");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvCategoryCount = findViewById(R.id.tv_category_count);
        tvSummaryTotal = findViewById(R.id.tv_summary_total);
        tvSummaryFilms = findViewById(R.id.tv_summary_films);
        tvSummaryEmpty = findViewById(R.id.tv_summary_empty);
        etSearch = findViewById(R.id.et_search);
        ImageView fab = findViewById(R.id.fab_add_category);

        RecyclerView rv = findViewById(R.id.rv_categories);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCategoryAdapter(this);
        rv.setAdapter(adapter);

        fab.setOnClickListener(v -> showForm(null));
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { applyFilter(); }
        });

        loadCategories();
    }

    private void loadCategories() {
        ApiClient.getFilmApiService().getAdminCategories()
                .enqueue(new Callback<ApiResponseDto<List<AdminCategoryDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<List<AdminCategoryDto>>> call,
                                           Response<ApiResponseDto<List<AdminCategoryDto>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            all.clear();
                            all.addAll(response.body().getData());
                            refreshSummary();
                            applyFilter();
                        } else {
                            Toast.makeText(AdminCategoryListActivity.this,
                                    "Cannot load categories", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<List<AdminCategoryDto>>> call, Throwable t) {
                        Toast.makeText(AdminCategoryListActivity.this,
                                "Server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applyFilter() {
        String q = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<AdminCategoryDto> filtered = new ArrayList<>();
        for (AdminCategoryDto c : all) {
            boolean match = q.isEmpty()
                    || c.getName().toLowerCase(Locale.ROOT).contains(q)
                    || (c.getDescription() != null
                    && c.getDescription().toLowerCase(Locale.ROOT).contains(q));
            if (match) filtered.add(c);
        }
        adapter.setCategories(filtered);
        tvCategoryCount.setText(filtered.size() + " of " + all.size() + " categories");
    }

    private void refreshSummary() {
        long totalFilms = 0;
        int empty = 0;
        for (AdminCategoryDto c : all) {
            totalFilms += c.getFilmCount();
            if (c.getFilmCount() == 0) empty++;
        }
        tvSummaryTotal.setText(String.valueOf(all.size()));
        tvSummaryFilms.setText(String.valueOf(totalFilms));
        tvSummaryEmpty.setText(String.valueOf(empty));
    }

    @Override
    public void onEditCategory(AdminCategoryDto category) {
        showForm(category);
    }

    @Override
    public void onDeleteCategory(AdminCategoryDto category) {
        String msg = category.getFilmCount() == 0
                ? "Delete \"" + category.getName() + "\"?"
                : "Delete \"" + category.getName() + "\"?\n\n"
                + category.getFilmCount() + " film tag"
                + (category.getFilmCount() == 1 ? "" : "s")
                + " will be removed.";
        new AlertDialog.Builder(this)
                .setTitle("Delete category")
                .setMessage(msg)
                .setPositiveButton("Delete", (d, w) -> deleteCategory(category))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showForm(AdminCategoryDto existing) {
        CategoryFormDialogFragment d = CategoryFormDialogFragment.newInstance(existing);
        d.setOnCategorySavedListener((request, editing) -> {
            if (editing == null) {
                createCategory(request);
            } else {
                updateCategory(editing.getId(), request);
            }
        });
        d.show(getSupportFragmentManager(), "category_form");
    }

    private void createCategory(AdminCategoryRequestDto request) {
        ApiClient.getFilmApiService().createCategory(request).enqueue(categoryMutationCallback("Created category"));
    }

    private void updateCategory(Integer id, AdminCategoryRequestDto request) {
        ApiClient.getFilmApiService().updateCategory(id, request).enqueue(categoryMutationCallback("Updated category"));
    }

    private void deleteCategory(AdminCategoryDto category) {
        ApiClient.getFilmApiService().deleteCategory(category.getId())
                .enqueue(new Callback<ApiResponseDto<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<Void>> call, Response<ApiResponseDto<Void>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminCategoryListActivity.this,
                                    "Deleted category", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        } else {
                            Toast.makeText(AdminCategoryListActivity.this,
                                    "Cannot delete category", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                        Toast.makeText(AdminCategoryListActivity.this,
                                "Server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Callback<ApiResponseDto<AdminCategoryDto>> categoryMutationCallback(String successMessage) {
        return new Callback<ApiResponseDto<AdminCategoryDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<AdminCategoryDto>> call,
                                   Response<ApiResponseDto<AdminCategoryDto>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminCategoryListActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else {
                    Toast.makeText(AdminCategoryListActivity.this,
                            "Cannot save category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<AdminCategoryDto>> call, Throwable t) {
                Toast.makeText(AdminCategoryListActivity.this,
                        "Server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }
}

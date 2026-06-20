package com.android.cineflow.ui.admin;

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

public class AdminCategoryListActivity extends com.android.cineflow.ui.base.BaseActivity
        implements AdminCategoryAdapter.OnCategoryActionListener {

    private AdminCategoryAdapter adapter;
    private TextView tvCategoryCount;
    private TextView tvSummaryTotal, tvSummaryFilms, tvSummaryEmpty;
    private EditText etSearch;
    private final List<AdminCategoryAdapter.MockCategory> all = new ArrayList<>();

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

        seedMock();
        refreshSummary();
        applyFilter();

        fab.setOnClickListener(v -> showForm(null));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { applyFilter(); }
        });
    }

    private void applyFilter() {
        String q = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<AdminCategoryAdapter.MockCategory> filtered = new ArrayList<>();
        for (AdminCategoryAdapter.MockCategory c : all) {
            boolean match = q.isEmpty()
                    || c.name.toLowerCase(Locale.ROOT).contains(q)
                    || (c.description != null
                        && c.description.toLowerCase(Locale.ROOT).contains(q));
            if (match) filtered.add(c);
        }
        adapter.setCategories(filtered);
        tvCategoryCount.setText(filtered.size() + " of " + all.size() + " categories");
    }

    private void refreshSummary() {
        int totalFilms = 0, empty = 0;
        for (AdminCategoryAdapter.MockCategory c : all) {
            totalFilms += c.filmCount;
            if (c.filmCount == 0) empty++;
        }
        tvSummaryTotal.setText(String.valueOf(all.size()));
        tvSummaryFilms.setText(String.valueOf(totalFilms));
        tvSummaryEmpty.setText(String.valueOf(empty));
    }

    @Override
    public void onEditCategory(AdminCategoryAdapter.MockCategory category) {
        showForm(category);
    }

    @Override
    public void onDeleteCategory(AdminCategoryAdapter.MockCategory category) {
        String msg = category.filmCount == 0
                ? "Delete \"" + category.name + "\"?"
                : "Delete \"" + category.name + "\"?\n\n"
                        + category.filmCount + " film tag"
                        + (category.filmCount == 1 ? "" : "s")
                        + " will be removed (ON DELETE CASCADE on film_categories).";
        new AlertDialog.Builder(this)
                .setTitle("Delete category")
                .setMessage(msg)
                .setPositiveButton("Delete", (d, w) -> {
                    all.remove(category);
                    refreshSummary();
                    applyFilter();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showForm(AdminCategoryAdapter.MockCategory existing) {
        CategoryFormDialogFragment d = CategoryFormDialogFragment.newInstance(existing);
        d.setOnCategorySavedListener((category, isNew) -> {
            if (isNew) {
                category.id = nextId();
                all.add(0, category);
            }
            refreshSummary();
            applyFilter();
            Toast.makeText(this,
                    (isNew ? "Created " : "Updated ") + category.name,
                    Toast.LENGTH_SHORT).show();
        });
        d.show(getSupportFragmentManager(), "category_form");
    }

    private int nextId() {
        int max = 0;
        for (AdminCategoryAdapter.MockCategory c : all) {
            if (c.id != null && c.id > max) max = c.id;
        }
        return max + 1;
    }

    private void seedMock() {
        all.add(new AdminCategoryAdapter.MockCategory(1, "Action",
                "High-octane films packed with chase scenes, fights, and stunts.", 142));
        all.add(new AdminCategoryAdapter.MockCategory(2, "Drama",
                "Character-driven stories built on emotional conflict.", 98));
        all.add(new AdminCategoryAdapter.MockCategory(3, "Comedy",
                "Light-hearted films intended to make the audience laugh.", 76));
        all.add(new AdminCategoryAdapter.MockCategory(4, "Sci-Fi",
                "Speculative fiction set in futuristic or alternate worlds.", 54));
        all.add(new AdminCategoryAdapter.MockCategory(5, "Horror",
                "Films designed to provoke fear and suspense.", 38));
        all.add(new AdminCategoryAdapter.MockCategory(6, "Romance",
                "Stories centered on love and relationships.", 61));
        all.add(new AdminCategoryAdapter.MockCategory(7, "Documentary",
                "Non-fiction films covering real people, places, and events.", 22));
        all.add(new AdminCategoryAdapter.MockCategory(8, "Animation",
                null, 47));
        all.add(new AdminCategoryAdapter.MockCategory(9, "Thriller",
                "Suspenseful films with tense, twisting plots.", 65));
        all.add(new AdminCategoryAdapter.MockCategory(10, "Adventure",
                "Journeys, quests, and exploration-driven stories.", 81));
        all.add(new AdminCategoryAdapter.MockCategory(11, "K-Drama",
                "Korean serial dramas, typically romance or family-oriented.", 33));
        all.add(new AdminCategoryAdapter.MockCategory(12, "Anime",
                "Japanese animated films and series.", 0));
    }
}

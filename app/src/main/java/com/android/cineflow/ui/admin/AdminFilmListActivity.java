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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.cineflow.R;
import com.android.cineflow.data.network.dto.FilmDetailDto;
import com.android.cineflow.data.repository.AdminFilmRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdminFilmListActivity extends com.android.cineflow.ui.base.BaseActivity implements AdminFilmAdapter.OnFilmActionListener {

    private AdminFilmRepository repository;
    private AdminFilmAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvFilmCount;
    private EditText etSearch;
    private MaterialButton btnPrevPage;
    private MaterialButton btnNextPage;
    private TextView tvPageInfo;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_film_list);

        repository = AdminFilmRepository.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.admin_title_film_management);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progress_bar);
        tvFilmCount = findViewById(R.id.tv_film_count);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        etSearch = findViewById(R.id.et_search);
        btnPrevPage = findViewById(R.id.btn_prev_page);
        btnNextPage = findViewById(R.id.btn_next_page);
        tvPageInfo = findViewById(R.id.tv_page_info);

        RecyclerView rvFilms = findViewById(R.id.rv_films);
        rvFilms.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminFilmAdapter(this);
        rvFilms.setAdapter(adapter);

        swipeRefresh.setColorSchemeColors(getColor(R.color.brand_primary));
        swipeRefresh.setOnRefreshListener(() -> {
            repository.fetchFirstPage(etSearch.getText().toString().trim());
            swipeRefresh.setRefreshing(false);
        });

        findViewById(R.id.fab_add_film).setOnClickListener(v -> showFilmFormDialog(null));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
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
        repository.getFilms().observe(this, films -> {
            adapter.setFilms(films);
            updatePaginationUI();
        });

        repository.getLoading().observe(this, loading -> {
            progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });

        repository.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                tvFilmCount.setText(error);
            }
        });
    }

    private void updatePaginationUI() {
        long total = repository.getTotalElements();
        int page = repository.getCurrentPage();
        int totalPages = repository.getTotalPages();

        tvFilmCount.setText(total + " films total");

        int start = total > 0 ? page * 10 + 1 : 0;
        int end = (int) Math.min((page + 1) * 10L, total);
        tvPageInfo.setText((page + 1) + " / " + totalPages + "  (" + start + "-" + end + ")");

        btnPrevPage.setEnabled(page > 0);
        btnNextPage.setEnabled(repository.hasMore());
    }

    @Override
    public void onEditFilm(FilmDetailDto film) {
        showFilmFormDialog(film);
    }

    @Override
    public void onDeleteFilm(FilmDetailDto film) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.admin_dialog_delete_film_title)
                .setMessage(getString(R.string.admin_dialog_delete_film_msg, film.getTitle()))
                .setPositiveButton(R.string.admin_button_delete, (d, w) -> repository.deleteFilm(film.getId(), new AdminFilmRepository.OnResultListener() {
                    @Override
                    public void onSuccess(FilmDetailDto result) {}

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> tvFilmCount.setText(message));
                    }
                }))
                .setNegativeButton(R.string.admin_button_cancel, null)
                .show();
    }

    private void showFilmFormDialog(FilmDetailDto existingFilm) {
        FilmFormDialogFragment dialog = FilmFormDialogFragment.newInstance(existingFilm);
        dialog.show(getSupportFragmentManager(), "film_form");
    }
}

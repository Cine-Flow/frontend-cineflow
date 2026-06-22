package com.android.cineflow.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.FilmDto;
import com.android.cineflow.ui.detail.FilmDetailActivity;

import java.util.List;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

/**
 * Displays a full grid of films for a given category (film type).
 * Launched from the "Xem thêm" action on the home page.
 */
public class CategoryActivity extends com.android.cineflow.ui.base.BaseActivity {

    public static final String EXTRA_CATEGORY_TITLE = "extra_category_title";
    public static final String EXTRA_CATEGORY_TYPE  = "extra_category_type";

    private GridView gvCategory;
    private ProgressBar progressBar;
    private TextView tvError;
    private CategoryGridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // ── Toolbar ──────────────────────────────────────────────────────────
        Toolbar toolbar = findViewById(R.id.toolbar_category);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String title = getIntent().getStringExtra(EXTRA_CATEGORY_TITLE);
        if (title != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        // ── Views ────────────────────────────────────────────────────────────
        gvCategory  = findViewById(R.id.gv_category);
        progressBar = findViewById(R.id.progress_category);
        tvError     = findViewById(R.id.tv_category_error);

        adapter = new CategoryGridAdapter(this);
        gvCategory.setAdapter(adapter);

        // Card click → film detail
        gvCategory.setOnItemClickListener((parent, view, position, id) -> {
            FilmDto film = adapter.getItem(position);
            if (film != null) {
                Intent intent = new Intent(this, FilmDetailActivity.class);
                intent.putExtra(FilmDetailActivity.EXTRA_FILM_ID, String.valueOf(film.getId()));
                startActivity(intent);
            }
        });

        // ── Load data ────────────────────────────────────────────────────────
        String type = getIntent().getStringExtra(EXTRA_CATEGORY_TYPE);
        if (type != null) {
            fetchFilms(type);
        }
    }

    private void fetchFilms(String type) {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        ApiClient.getFilmApiService().getFilmsByType(type)
                .enqueue(new Callback<ApiResponseDto<List<FilmDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<List<FilmDto>>> call,
                                           Response<ApiResponseDto<List<FilmDto>>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            List<FilmDto> films = response.body().getData();
                            adapter.setFilms(films);
                            if (films.isEmpty()) {
                                tvError.setText(R.string.category_no_films);
                                tvError.setVisibility(View.VISIBLE);
                            }
                        } else {
                            tvError.setText(R.string.category_load_error);
                            tvError.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<List<FilmDto>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        tvError.setText(R.string.category_connection_error);
                        tvError.setVisibility(View.VISIBLE);
                        Toast.makeText(CategoryActivity.this,
                                getString(R.string.category_error_format, t.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

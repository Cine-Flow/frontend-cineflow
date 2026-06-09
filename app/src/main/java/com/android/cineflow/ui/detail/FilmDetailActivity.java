package com.android.cineflow.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.EpisodeDto;
import com.android.cineflow.data.network.dto.FilmDetailDto;
import com.android.cineflow.ui.player.PlayerActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilmDetailActivity extends AppCompatActivity {

    public static final String EXTRA_FILM_ID = "extra_film_id";

    private ImageView ivCover;
    private TextView tvTitle;
    private TextView tvYear;
    private TextView tvType;
    private TextView tvDescription;
    private Button btnPlayMain;
    private Button btnAddFavorite;
    private TextView tvEpisodesTitle;
    private RecyclerView rvEpisodes;
    private EpisodeAdapter episodeAdapter;

    private FilmDetailDto currentFilm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ivCover = findViewById(R.id.iv_film_cover);
        tvTitle = findViewById(R.id.tv_film_title);
        tvYear = findViewById(R.id.tv_film_year);
        tvType = findViewById(R.id.tv_film_type);
        tvDescription = findViewById(R.id.tv_film_description);
        btnPlayMain = findViewById(R.id.btn_play_main);
        btnAddFavorite = findViewById(R.id.btn_add_favorite);
        tvEpisodesTitle = findViewById(R.id.tv_episodes_title);
        rvEpisodes = findViewById(R.id.rv_episodes);

        btnPlayMain.setVisibility(android.view.View.GONE);
        btnAddFavorite.setVisibility(android.view.View.GONE);
        tvEpisodesTitle.setVisibility(android.view.View.GONE);
        rvEpisodes.setVisibility(android.view.View.GONE);

        rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
        episodeAdapter = new EpisodeAdapter(new ArrayList<>(), this::playEpisode);
        rvEpisodes.setAdapter(episodeAdapter);

        String filmIdStr = getIntent().getStringExtra(EXTRA_FILM_ID);
        if (filmIdStr != null) {
            fetchFilmDetails(Integer.parseInt(filmIdStr));
        }
    }

    private void fetchFilmDetails(int filmId) {
        ApiClient.getFilmApiService().getFilmById(filmId).enqueue(new Callback<ApiResponseDto<FilmDetailDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<FilmDetailDto>> call, Response<ApiResponseDto<FilmDetailDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindData(response.body().getData());
                } else {
                    Toast.makeText(FilmDetailActivity.this, "Lỗi tải thông tin phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<FilmDetailDto>> call, Throwable t) {
                Toast.makeText(FilmDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindData(FilmDetailDto film) {
        this.currentFilm = film;
        btnAddFavorite.setOnClickListener(v -> addFavorite(film.getId()));
        tvTitle.setText(film.getTitle());
        tvYear.setText(String.valueOf(film.getReleaseYear()));
        
        if ("SPORTS".equals(film.getType())) {
            if (film.getTrailerUrl() == null || film.getTrailerUrl().isEmpty()) {
                tvType.setText("Tin tức");
            } else {
                tvType.setText("Bóng đá");
            }
        } else {
            tvType.setText(film.getType());
        }
        
        tvDescription.setText(film.getDescription());

        Glide.with(this)
                .load(film.getThumbnailUrl())
                .centerCrop()
                .into(ivCover);

        boolean hasEpisodes = film.getEpisodes() != null && !film.getEpisodes().isEmpty();
        boolean hasVideoUrl = film.getTrailerUrl() != null && !film.getTrailerUrl().isEmpty();

        if (hasEpisodes) {
            btnPlayMain.setVisibility(android.view.View.VISIBLE);
            btnAddFavorite.setVisibility(android.view.View.VISIBLE);
            tvEpisodesTitle.setVisibility(android.view.View.VISIBLE);
            rvEpisodes.setVisibility(android.view.View.VISIBLE);

            episodeAdapter.setFilmContext(String.valueOf(film.getId()), film.getTitle(), film.getThumbnailUrl());
            episodeAdapter.setEpisodes(film.getEpisodes());
            btnPlayMain.setOnClickListener(v -> playEpisode(film.getEpisodes().get(0)));
        } else if (hasVideoUrl) {
            btnPlayMain.setVisibility(android.view.View.VISIBLE);
            btnAddFavorite.setVisibility(android.view.View.VISIBLE);
            tvEpisodesTitle.setVisibility(android.view.View.GONE);
            rvEpisodes.setVisibility(android.view.View.GONE);

            if ("LIVE".equals(film.getType())) {
                btnPlayMain.setText("▶  Xem trực tiếp");
            } else {
                btnPlayMain.setText(" Xem ngay");
            }
            btnPlayMain.setEnabled(true);
            btnPlayMain.setOnClickListener(v -> {
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_VIDEO_URL, film.getTrailerUrl());
                intent.putExtra(PlayerActivity.EXTRA_TITLE, film.getTitle());
                intent.putExtra(PlayerActivity.EXTRA_BADGE, film.getType());
                startActivity(intent);
            });
        } else {
            // News article / Text content with no video
            btnPlayMain.setVisibility(android.view.View.GONE);
            btnAddFavorite.setVisibility(android.view.View.GONE);
            tvEpisodesTitle.setVisibility(android.view.View.GONE);
            rvEpisodes.setVisibility(android.view.View.GONE);
        }
    }

    private void playEpisode(EpisodeDto episode) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_URL, episode.getVideoUrl());
        intent.putExtra(PlayerActivity.EXTRA_EPISODE_ID, episode.getId());
        if (currentFilm != null) {
            intent.putExtra("extra_film_id", currentFilm.getId());
            intent.putExtra(PlayerActivity.EXTRA_TITLE, currentFilm.getTitle() + " - " + (episode.getTitle() != null ? episode.getTitle() : ("Tập " + episode.getEpisodeNumber())));
        }
        startActivity(intent);
    }

    private void addFavorite(Integer filmId) {
        ApiClient.getFilmApiService().addFavorite(filmId).enqueue(new Callback<ApiResponseDto<com.android.cineflow.data.network.dto.FavoriteDto>>() {
            @Override public void onResponse(Call<ApiResponseDto<com.android.cineflow.data.network.dto.FavoriteDto>> call, Response<ApiResponseDto<com.android.cineflow.data.network.dto.FavoriteDto>> response) {
                Toast.makeText(FilmDetailActivity.this,
                        response.isSuccessful() ? "Đã thêm vào yêu thích" : "Vui lòng đăng nhập",
                        Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<ApiResponseDto<com.android.cineflow.data.network.dto.FavoriteDto>> call, Throwable t) {
                Toast.makeText(FilmDetailActivity.this, "Không thể thêm yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.android.cineflow.ui.detail;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.auth.AuthManager;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.CreateCommentRequestDto;
import com.android.cineflow.data.network.dto.EpisodeDto;
import com.android.cineflow.data.network.dto.FilmCommentDto;
import com.android.cineflow.data.network.dto.FilmDetailDto;
import com.android.cineflow.ui.auth.LoginActivity;
import com.android.cineflow.ui.player.PlayerActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

public class FilmDetailActivity extends com.android.cineflow.ui.base.BaseActivity {

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

    // ── Comments ──
    private RecyclerView rvComments;
    private FilmCommentAdapter commentAdapter;
    private ProgressBar progressComments;
    private TextView tvCommentsEmpty;
    private TextView tvCommentCount;
    private LinearLayout llCommentInput;
    private EditText etCommentInput;
    private Button btnSendComment;
    private TextView tvCommentLoginHint;

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

        setupCommentsSection();

        String filmIdStr = getIntent().getStringExtra(EXTRA_FILM_ID);
        if (filmIdStr != null) {
            int filmId = Integer.parseInt(filmIdStr);
            fetchFilmDetails(filmId);
            fetchComments(filmId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh input state in case user logged in from another activity
        updateCommentInputState();
    }

    private void setupCommentsSection() {
        rvComments         = findViewById(R.id.rv_comments);
        progressComments   = findViewById(R.id.progress_comments);
        tvCommentsEmpty    = findViewById(R.id.tv_comments_empty);
        tvCommentCount     = findViewById(R.id.tv_comment_count);
        llCommentInput     = findViewById(R.id.ll_comment_input);
        etCommentInput     = findViewById(R.id.et_comment_input);
        btnSendComment     = findViewById(R.id.btn_send_comment);
        tvCommentLoginHint = findViewById(R.id.tv_comment_login_hint);

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new FilmCommentAdapter();
        AuthManager auth = AuthManager.getInstance();
        if (auth != null) commentAdapter.setCurrentUserId(auth.getUserId());
        commentAdapter.setOnCommentActionListener(this::confirmDeleteComment);
        rvComments.setAdapter(commentAdapter);

        btnSendComment.setOnClickListener(v -> submitComment());
        updateCommentInputState();
    }

    private void updateCommentInputState() {
        AuthManager auth = AuthManager.getInstance();
        boolean loggedIn = auth != null && auth.isLoggedIn();
        llCommentInput.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        tvCommentLoginHint.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        if (!loggedIn) {
            tvCommentLoginHint.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
        }
        if (auth != null) commentAdapter.setCurrentUserId(auth.getUserId());
    }

    private void fetchComments(int filmId) {
        progressComments.setVisibility(View.VISIBLE);
        tvCommentsEmpty.setVisibility(View.GONE);
        ApiClient.getFilmApiService().getFilmComments(filmId)
                .enqueue(new Callback<ApiResponseDto<List<FilmCommentDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<List<FilmCommentDto>>> call,
                                           Response<ApiResponseDto<List<FilmCommentDto>>> response) {
                        progressComments.setVisibility(View.GONE);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            commentAdapter.submit(response.body().getData());
                        }
                        renderCommentCount();
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<List<FilmCommentDto>>> call, Throwable t) {
                        progressComments.setVisibility(View.GONE);
                        renderCommentCount();
                    }
                });
    }

    private void submitComment() {
        if (currentFilm == null) return;
        String text = etCommentInput.getText().toString().trim();
        if (text.isEmpty()) {
            etCommentInput.setError("Nội dung không được để trống");
            return;
        }
        btnSendComment.setEnabled(false);
        ApiClient.getFilmApiService()
                .postFilmComment(currentFilm.getId(), new CreateCommentRequestDto(text))
                .enqueue(new Callback<ApiResponseDto<FilmCommentDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<FilmCommentDto>> call,
                                           Response<ApiResponseDto<FilmCommentDto>> response) {
                        btnSendComment.setEnabled(true);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            commentAdapter.prepend(response.body().getData());
                            etCommentInput.setText("");
                            renderCommentCount();
                        } else if (response.code() == 401 || response.code() == 403) {
                            Toast.makeText(FilmDetailActivity.this,
                                    "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FilmDetailActivity.this,
                                    "Không thể đăng bình luận", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<FilmCommentDto>> call, Throwable t) {
                        btnSendComment.setEnabled(true);
                        Toast.makeText(FilmDetailActivity.this,
                                "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeleteComment(FilmCommentDto comment) {
        new AlertDialog.Builder(this)
                .setMessage("Xóa bình luận này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> deleteComment(comment))
                .show();
    }

    private void deleteComment(FilmCommentDto comment) {
        ApiClient.getFilmApiService().deleteFilmComment(comment.getId())
                .enqueue(new Callback<ApiResponseDto<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<Void>> call,
                                           Response<ApiResponseDto<Void>> response) {
                        if (response.isSuccessful()) {
                            commentAdapter.remove(comment);
                            renderCommentCount();
                        } else {
                            Toast.makeText(FilmDetailActivity.this,
                                    "Không thể xóa bình luận", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                        Toast.makeText(FilmDetailActivity.this,
                                "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderCommentCount() {
        int n = commentAdapter.size();
        tvCommentCount.setText(n + (n == 1 ? " bình luận" : " bình luận"));
        tvCommentsEmpty.setVisibility(n == 0 ? View.VISIBLE : View.GONE);
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
        boolean isLive = "LIVE".equals(film.getType());

        if (isLive && hasVideoUrl) {
            btnPlayMain.setVisibility(android.view.View.VISIBLE);
            btnAddFavorite.setVisibility(android.view.View.VISIBLE);
            tvEpisodesTitle.setVisibility(android.view.View.GONE);
            rvEpisodes.setVisibility(android.view.View.GONE);

            btnPlayMain.setText("▶  Xem trực tiếp");
            btnPlayMain.setEnabled(true);
            btnPlayMain.setOnClickListener(v -> {
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_VIDEO_URL, film.getTrailerUrl());
                intent.putExtra(PlayerActivity.EXTRA_TITLE, film.getTitle());
                intent.putExtra(PlayerActivity.EXTRA_BADGE, film.getType());
                startActivity(intent);
            });
        } else if (hasEpisodes) {
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

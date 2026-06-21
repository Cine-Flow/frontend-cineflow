package com.android.cineflow.ui.player;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.android.cineflow.data.network.dto.FavoriteDto;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.PlayerView;

import com.android.cineflow.R;
import com.android.cineflow.data.auth.AuthManager;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.EpisodeDto;
import com.android.cineflow.data.network.dto.FilmDetailDto;
import com.android.cineflow.data.network.dto.UpdateWatchHistoryRequestDto;
import com.android.cineflow.data.settings.SettingsManager;

import java.util.List;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

@UnstableApi
public class PlayerActivity extends com.android.cineflow.ui.base.BaseActivity {

    public static final String EXTRA_VIDEO_URL = "extra_video_url";
    public static final String EXTRA_EPISODE_ID = "extra_episode_id";
    public static final String EXTRA_RESUME_POSITION_SECONDS = "extra_resume_position_seconds";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_BADGE = "extra_badge";

    private PlayerView playerView;
    private ExoPlayer player;
    private String videoUrl;
    private int episodeId = -1;
    private int filmId = -1;
    private ImageButton btnBack;
    private ImageButton btnOrientation;
    private TextView tvDetailTitle;
    private TextView tvContentBadge;
    private View playerContainer;
    private View playerDetailScroll;
    private Button btnPlayerFavorite;
    private Button btnPlayerShare;
    private boolean isFavorited = false;
    
    // State management
    private boolean playWhenReady = true;
    private int currentItem = 0;
    private long playbackPosition = 0L;
    private DefaultTrackSelector trackSelector;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        
        playerView = findViewById(R.id.player_view);
        playerContainer = findViewById(R.id.player_container);
        playerDetailScroll = findViewById(R.id.player_detail_scroll);
        btnBack = findViewById(R.id.btn_back);
        btnOrientation = findViewById(R.id.btn_orientation);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvContentBadge = findViewById(R.id.tv_content_badge);
        
        videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        episodeId = getIntent().getIntExtra(EXTRA_EPISODE_ID, -1);
        filmId = getIntent().getIntExtra("extra_film_id", -1);
        playbackPosition = getIntent().getIntExtra(EXTRA_RESUME_POSITION_SECONDS, 0) * 1000L;
        bindMetadata();

        btnPlayerFavorite = findViewById(R.id.btn_player_favorite);
        btnPlayerShare = findViewById(R.id.btn_player_share);

        if (filmId < 0) {
            btnPlayerFavorite.setVisibility(View.GONE);
        } else {
            btnPlayerFavorite.setVisibility(View.VISIBLE);
        }

        btnPlayerFavorite.setOnClickListener(v -> toggleFavorite());
        btnPlayerShare.setOnClickListener(v -> shareVideo());

        btnBack.setOnClickListener(v -> finish());
        btnOrientation.setOnClickListener(v -> toggleOrientation());
        applyPlayerLayoutForOrientation(getResources().getConfiguration().orientation);

        // Đồng bộ ẩn/hiện nút Back với Controller của Player bằng hiệu ứng fade mượt mà
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            if (visibility == View.VISIBLE) {
                btnBack.setVisibility(View.VISIBLE);
                btnOrientation.setVisibility(View.VISIBLE);
                btnBack.animate().alpha(1f).setDuration(100).start();
                btnOrientation.animate().alpha(1f).setDuration(100).start();
            } else {
                btnBack.animate().alpha(0f).setDuration(100).withEndAction(() -> {
                    btnBack.setVisibility(View.GONE);
                }).start();
                btnOrientation.animate().alpha(0f).setDuration(100).withEndAction(() -> {
                    btnOrientation.setVisibility(View.GONE);
                }).start();
            }
        });
    }

    private void toggleOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyPlayerLayoutForOrientation(newConfig.orientation);
        hideSystemUi();
    }

    private void applyPlayerLayoutForOrientation(int orientation) {
        boolean isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;

        ViewGroup.LayoutParams playerParams = playerContainer.getLayoutParams();
        playerParams.height = isLandscape
                ? ViewGroup.LayoutParams.MATCH_PARENT
                : (int) (240 * getResources().getDisplayMetrics().density);
        playerContainer.setLayoutParams(playerParams);

        playerDetailScroll.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
        btnOrientation.setImageResource(isLandscape ? R.drawable.ic_fullscreen_exit : R.drawable.ic_fullscreen);
        btnOrientation.setContentDescription(getString(
                isLandscape ? R.string.player_exit_fullscreen_desc : R.string.player_fullscreen_desc));
    }

    private void bindMetadata() {
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title != null && !title.isEmpty()) {
            tvDetailTitle.setText(title);
        }

        String badge = getIntent().getStringExtra(EXTRA_BADGE);
        boolean hasBadge = badge != null && !badge.isEmpty();
        tvContentBadge.setVisibility(hasBadge ? View.VISIBLE : View.GONE);
        if (hasBadge) {
            tvContentBadge.setText(badge);
        }
    }

    private String resolveLocalhostUrl(String url) {
        if (url != null && url.contains("localhost:9000")) {
            return url.replace("localhost:9000", "10.0.2.2:9000");
        }
        return url;
    }

    private void initializePlayer() {
        if (player == null) {
            trackSelector = new DefaultTrackSelector(this);
            configureVideoQuality();
            
            player = new ExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();
            playerView.setPlayer(player);
            
            String urlToPlay = resolveLocalhostUrl(videoUrl);
            if (urlToPlay == null || urlToPlay.isEmpty()) {
                Toast.makeText(this, "Video URL is missing", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            MediaItem mediaItem = MediaItem.fromUri(urlToPlay);
            player.setMediaItem(mediaItem);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentItem, playbackPosition);
            
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_ENDED) {
                        handleVideoEnded();
                    }
                }
            });
            
            player.prepare();
            player.play();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            saveWatchHistory();
            currentItem = player.getCurrentMediaItemIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    private void saveWatchHistory() {
        AuthManager auth = AuthManager.getInstance();
        if (episodeId < 0 || auth == null || !auth.isLoggedIn()) return;
        ApiClient.getFilmApiService().updateWatchHistory(
                episodeId, new UpdateWatchHistoryRequestDto((int) (playbackPosition / 1000L)))
                .enqueue(new com.android.cineflow.data.network.Callback<>() {
                    @Override public void onResponse(com.android.cineflow.data.network.Call<com.android.cineflow.data.network.dto.ApiResponseDto<com.android.cineflow.data.network.dto.WatchHistoryDto>> call,
                                                     com.android.cineflow.data.network.Response<com.android.cineflow.data.network.dto.ApiResponseDto<com.android.cineflow.data.network.dto.WatchHistoryDto>> response) {}
                    @Override public void onFailure(com.android.cineflow.data.network.Call<com.android.cineflow.data.network.dto.ApiResponseDto<com.android.cineflow.data.network.dto.WatchHistoryDto>> call, Throwable t) {
                        android.util.Log.w("PlayerActivity", "Không thể lưu tiến độ xem", t);
                    }
                });
    }

    private void checkFavoriteStatus() {
        AuthManager auth = AuthManager.getInstance();
        if (filmId < 0 || auth == null || !auth.isLoggedIn()) {
            return;
        }
        ApiClient.getFilmApiService().getFavorites().enqueue(new Callback<ApiResponseDto<List<FavoriteDto>>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<List<FavoriteDto>>> call, Response<ApiResponseDto<List<FavoriteDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    isFavorited = false;
                    for (FavoriteDto fav : response.body().getData()) {
                        if (fav.getFilm() != null && fav.getFilm().getId() == filmId) {
                            isFavorited = true;
                            break;
                        }
                    }
                    updateFavoriteButtonState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<List<FavoriteDto>>> call, Throwable t) {
                // Ignore failure
            }
        });
    }

    private void updateFavoriteButtonState() {
        if (isFavorited) {
            btnPlayerFavorite.setText("Đã thích");
            btnPlayerFavorite.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E91E63")));
        } else {
            btnPlayerFavorite.setText(R.string.player_btn_favorite);
            btnPlayerFavorite.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2D2D2D")));
        }
    }

    private void toggleFavorite() {
        AuthManager auth = AuthManager.getInstance();
        if (filmId < 0) {
            Toast.makeText(this, "Không tìm thấy thông tin phim", Toast.LENGTH_SHORT).show();
            return;
        }
        if (auth == null || !auth.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, com.android.cineflow.ui.auth.LoginActivity.class));
            return;
        }

        btnPlayerFavorite.setEnabled(false);
        if (isFavorited) {
            ApiClient.getFilmApiService().deleteFavorite(filmId).enqueue(new Callback<ApiResponseDto<Void>>() {
                @Override
                public void onResponse(Call<ApiResponseDto<Void>> call, Response<ApiResponseDto<Void>> response) {
                    btnPlayerFavorite.setEnabled(true);
                    if (response.isSuccessful()) {
                        isFavorited = false;
                        updateFavoriteButtonState();
                        Toast.makeText(PlayerActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PlayerActivity.this, "Không thể xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                    btnPlayerFavorite.setEnabled(true);
                    Toast.makeText(PlayerActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ApiClient.getFilmApiService().addFavorite(filmId).enqueue(new Callback<ApiResponseDto<FavoriteDto>>() {
                @Override
                public void onResponse(Call<ApiResponseDto<FavoriteDto>> call, Response<ApiResponseDto<FavoriteDto>> response) {
                    btnPlayerFavorite.setEnabled(true);
                    if (response.isSuccessful()) {
                        isFavorited = true;
                        updateFavoriteButtonState();
                        Toast.makeText(PlayerActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PlayerActivity.this, "Không thể thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponseDto<FavoriteDto>> call, Throwable t) {
                    btnPlayerFavorite.setEnabled(true);
                    Toast.makeText(PlayerActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void shareVideo() {
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title == null || title.isEmpty()) {
            title = "Video";
        }
        String textToShare = "Xem \"" + title + "\" trên CineFlow!\n" + videoUrl;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);

        startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
        }
        if (filmId >= 0) {
            checkFavoriteStatus();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void configureVideoQuality() {
        if (trackSelector == null) return;
        SettingsManager settings = SettingsManager.getInstance();
        if (settings == null) return;

        String quality = settings.getVideoQuality();
        DefaultTrackSelector.Parameters.Builder parametersBuilder = trackSelector.buildUponParameters();

        if (SettingsManager.QUALITY_FHD.equals(quality)) {
            parametersBuilder.setMaxVideoSize(1920, 1080);
        } else if (SettingsManager.QUALITY_HD.equals(quality)) {
            parametersBuilder.setMaxVideoSize(1280, 720);
        } else if (SettingsManager.QUALITY_SD.equals(quality)) {
            parametersBuilder.setMaxVideoSize(854, 480);
        } // Tự động (Auto) stays default dynamic selection

        trackSelector.setParameters(parametersBuilder.build());
    }

    private void handleVideoEnded() {
        SettingsManager settings = SettingsManager.getInstance();
        if (settings == null || !settings.isAutoplayEnabled()) {
            return;
        }

        if (filmId < 0) {
            return;
        }

        // Fetch film details to find the next episode
        ApiClient.getFilmApiService().getFilmById(filmId).enqueue(new Callback<ApiResponseDto<FilmDetailDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<FilmDetailDto>> call, Response<ApiResponseDto<FilmDetailDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    FilmDetailDto film = response.body().getData();
                    List<EpisodeDto> episodes = film.getEpisodes();
                    if (episodes != null && !episodes.isEmpty()) {
                        int currentIndex = -1;
                        for (int i = 0; i < episodes.size(); i++) {
                            if (episodes.get(i).getId() == episodeId) {
                                currentIndex = i;
                                break;
                            }
                        }

                        if (currentIndex != -1 && currentIndex < episodes.size() - 1) {
                            EpisodeDto nextEpisode = episodes.get(currentIndex + 1);
                            triggerAutoplayCountdown(nextEpisode, film.getTitle());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<FilmDetailDto>> call, Throwable t) {
                android.util.Log.e("PlayerActivity", "Lỗi tải thông tin tập tiếp theo", t);
            }
        });
    }

    private void triggerAutoplayCountdown(final EpisodeDto nextEpisode, final String filmTitle) {
        Toast.makeText(this, "Tự động phát tập tiếp theo sau 5 giây...", Toast.LENGTH_LONG).show();
        
        mainHandler.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            
            // Switch to next video URL and update metadata
            videoUrl = nextEpisode.getVideoUrl();
            episodeId = nextEpisode.getId();
            playbackPosition = 0;
            currentItem = 0;
            
            String nextTitle = filmTitle + " - " + (nextEpisode.getTitle() != null ? nextEpisode.getTitle() : ("Tập " + nextEpisode.getEpisodeNumber()));
            tvDetailTitle.setText(nextTitle);
            
            if (player != null) {
                String urlToPlay = resolveLocalhostUrl(videoUrl);
                if (urlToPlay == null || urlToPlay.isEmpty()) {
                    Toast.makeText(this, "Next episode video URL is missing", Toast.LENGTH_SHORT).show();
                    return;
                }
                MediaItem mediaItem = MediaItem.fromUri(urlToPlay);
                player.setMediaItem(mediaItem);
                player.seekTo(0, 0);
                player.prepare();
                player.play();
            }
        }, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), playerView);
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
}

package com.android.cineflow.ui.player;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
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
    private TextView tvDetailTitle;
    private TextView tvContentBadge;
    
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
        btnBack = findViewById(R.id.btn_back);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvContentBadge = findViewById(R.id.tv_content_badge);
        
        videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        episodeId = getIntent().getIntExtra(EXTRA_EPISODE_ID, -1);
        filmId = getIntent().getIntExtra("extra_film_id", -1);
        playbackPosition = getIntent().getIntExtra(EXTRA_RESUME_POSITION_SECONDS, 0) * 1000L;
        bindMetadata();

        btnBack.setOnClickListener(v -> finish());

        // Đồng bộ ẩn/hiện nút Back với Controller của Player bằng hiệu ứng fade mượt mà
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            if (visibility == View.VISIBLE) {
                btnBack.setVisibility(View.VISIBLE);
                btnBack.animate().alpha(1f).setDuration(100).start();
            } else {
                btnBack.animate().alpha(0f).setDuration(100).withEndAction(() -> {
                    btnBack.setVisibility(View.GONE);
                }).start();
            }
        });
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
                urlToPlay = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
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
                String urlToPlay = resolveLocalhostUrl(videoUrl != null && !videoUrl.isEmpty() ? videoUrl : "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
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

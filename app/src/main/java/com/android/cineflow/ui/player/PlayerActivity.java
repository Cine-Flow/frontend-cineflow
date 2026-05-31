package com.android.cineflow.ui.player;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.Util;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.android.cineflow.R;
import com.android.cineflow.data.auth.AuthManager;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.UpdateWatchHistoryRequestDto;

public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_URL = "extra_video_url";
    public static final String EXTRA_EPISODE_ID = "extra_episode_id";
    public static final String EXTRA_RESUME_POSITION_SECONDS = "extra_resume_position_seconds";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_BADGE = "extra_badge";

    private PlayerView playerView;
    private ExoPlayer player;
    private String videoUrl;
    private int episodeId = -1;
    private ImageButton btnBack;
    private TextView tvDetailTitle;
    private TextView tvContentBadge;
    
    // State management
    private boolean playWhenReady = true;
    private int currentItem = 0;
    private long playbackPosition = 0L;

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

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);
            
            String urlToPlay = videoUrl;
            if (urlToPlay == null || urlToPlay.isEmpty()) {
                urlToPlay = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
            }
            
            MediaItem mediaItem = MediaItem.fromUri(urlToPlay);
            player.setMediaItem(mediaItem);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentItem, playbackPosition);
            
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
                .enqueue(new retrofit2.Callback<>() {
                    @Override public void onResponse(retrofit2.Call<com.android.cineflow.data.network.dto.ApiResponseDto<com.android.cineflow.data.network.dto.WatchHistoryDto>> call,
                                                     retrofit2.Response<com.android.cineflow.data.network.dto.ApiResponseDto<com.android.cineflow.data.network.dto.WatchHistoryDto>> response) {}
                    @Override public void onFailure(retrofit2.Call<com.android.cineflow.data.network.dto.ApiResponseDto<com.android.cineflow.data.network.dto.WatchHistoryDto>> call, Throwable t) {
                        android.util.Log.w("PlayerActivity", "Không thể lưu tiến độ xem", t);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), playerView);
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
}

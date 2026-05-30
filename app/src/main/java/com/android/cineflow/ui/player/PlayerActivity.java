package com.android.cineflow.ui.player;

import android.annotation.SuppressLint;
import android.os.Bundle;

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

    private PlayerView playerView;
    private ExoPlayer player;
    private String videoUrl;
    private int episodeId = -1;
    
    // State management for best practices
    private boolean playWhenReady = true;
    private int currentItem = 0;
    private long playbackPosition = 0L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        
        playerView = findViewById(R.id.player_view);
        videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        episodeId = getIntent().getIntExtra(EXTRA_EPISODE_ID, -1);
        playbackPosition = getIntent().getIntExtra(EXTRA_RESUME_POSITION_SECONDS, 0) * 1000L;
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);
            
            String urlToPlay = videoUrl;
            if (urlToPlay == null || urlToPlay.isEmpty()) {
                // Fallback to sample video if none is provided
                urlToPlay = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
            }
            
            MediaItem mediaItem = MediaItem.fromUri(urlToPlay);
            player.setMediaItem(mediaItem);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentItem, playbackPosition);
            
            // Add listener to catch errors
            player.addListener(new androidx.media3.common.Player.Listener() {
                @Override
                public void onPlayerError(androidx.media3.common.PlaybackException error) {
                    android.util.Log.e("ExoPlayer", "Lỗi phát video: " + error.getMessage(), error);
                    android.widget.Toast.makeText(PlayerActivity.this, "Lỗi phát video: " + error.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                }
                
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == androidx.media3.common.Player.STATE_BUFFERING) {
                        android.util.Log.d("ExoPlayer", "Đang tải (Buffering)...");
                    } else if (playbackState == androidx.media3.common.Player.STATE_READY) {
                        android.util.Log.d("ExoPlayer", "Video đã sẵn sàng (Ready)");
                    }
                }
            });
            
            player.prepare();
            player.play(); // Explicitly call play()
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

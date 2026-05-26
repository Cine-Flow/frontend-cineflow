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

public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_URL = "extra_video_url";

    private PlayerView playerView;
    private ExoPlayer player;
    private String videoUrl;
    
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
            currentItem = player.getCurrentMediaItemIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
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

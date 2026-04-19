package com.android.cineflow.ui.shorts;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.android.cineflow.R;
import com.android.cineflow.data.model.ShortVideo;
import com.android.cineflow.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class ShortsFragment extends BaseFragment {

    private ListView lvShorts;
    private ShortsAdapter adapter;
    private ShortsViewModel viewModel;
    
    private ExoPlayer player;
    private int currentPlayingPosition = -1;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_shorts;
    }

    @Override
    protected void initViews(View view) {
        lvShorts = view.findViewById(R.id.lv_shorts);
        adapter = new ShortsAdapter(requireContext(), new ArrayList<>());
        lvShorts.setAdapter(adapter);

        player = new ExoPlayer.Builder(requireContext()).build();
        player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);

        lvShorts.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    playVisibleVideo();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // We can also trigger here, but triggering on IDLE is usually smoother.
            }
        });
    }

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(ShortsViewModel.class);
        viewModel.shortVideos.observe(getViewLifecycleOwner(), shortVideos -> {
            adapter.setItems(shortVideos);
            // Post a runnable to wait for layout pass before playing first video
            lvShorts.post(() -> playVisibleVideo());
        });
    }

    private void playVisibleVideo() {
        int firstVisiblePosition = lvShorts.getFirstVisiblePosition();
        int lastVisiblePosition = lvShorts.getLastVisiblePosition();

        int targetPosition = -1;
        float maxVisibility = 0f;

        // Find the item with the maximum visible area
        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
            View child = lvShorts.getChildAt(i - firstVisiblePosition);
            if (child != null) {
                float visibility = getVisibilityPercentage(child);
                if (visibility > maxVisibility) {
                    maxVisibility = visibility;
                    targetPosition = i;
                }
            }
        }

        if (targetPosition != -1 && targetPosition != currentPlayingPosition) {
            currentPlayingPosition = targetPosition;
            attachPlayerToItem(targetPosition);
        }
    }

    private float getVisibilityPercentage(View view) {
        Rect rect = new Rect();
        if (!view.getGlobalVisibleRect(rect)) {
            return 0f;
        }
        int visibleHeight = rect.height();
        return (float) visibleHeight / view.getHeight();
    }

    private void attachPlayerToItem(int position) {
        // Detach player from current view
        View child = lvShorts.getChildAt(position - lvShorts.getFirstVisiblePosition());
        if (child != null && child.getTag() instanceof ShortsAdapter.ViewHolder) {
            ShortsAdapter.ViewHolder holder = (ShortsAdapter.ViewHolder) child.getTag();
            
            ShortVideo video = (ShortVideo) adapter.getItem(position);
            
            player.stop();
            player.clearMediaItems();
            
            holder.playerView.setPlayer(null); // Clear previous if any
            holder.playerView.setPlayer(player);
            
            MediaItem mediaItem = MediaItem.fromUri(video.getVideoUrl());
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null && !player.isPlaying()) {
            player.play();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}

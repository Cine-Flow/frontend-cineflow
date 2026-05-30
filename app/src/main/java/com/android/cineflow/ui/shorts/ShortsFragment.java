package com.android.cineflow.ui.shorts;

import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.android.cineflow.R;
import com.android.cineflow.data.model.ShortVideo;
import com.android.cineflow.ui.base.BaseFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShortsFragment extends BaseFragment implements ShortsAdapter.OnShortInteractionListener {

    private static final String TAG = "ShortsFragment";

    private ListView lvShorts;
    private ShortsAdapter adapter;
    private ShortsViewModel viewModel;

    private ExoPlayer player;
    private int currentPlayingPosition = -1;
    private boolean isLoadingMore = false;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_shorts;
    }

    @Override
    protected void initViews(View view) {
        lvShorts = view.findViewById(R.id.lv_shorts);
        adapter = new ShortsAdapter(requireContext(), new ArrayList<>());
        adapter.setListener(this);
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
                if (totalItemCount > 0
                        && firstVisibleItem + visibleItemCount >= totalItemCount
                        && !isLoadingMore) {
                    isLoadingMore = true;
                    Log.d(TAG, "Load more triggered");
                    viewModel.loadMore();
                }
            }
        });
    }

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(ShortsViewModel.class);

        viewModel.shortVideos.observe(getViewLifecycleOwner(), shortVideos -> {
            adapter.setItems(shortVideos);
            isLoadingMore = false;
            lvShorts.post(() -> playVisibleVideo());
        });

        viewModel.error.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playVisibleVideo() {
        int firstVisiblePosition = lvShorts.getFirstVisiblePosition();
        int lastVisiblePosition = lvShorts.getLastVisiblePosition();

        int targetPosition = -1;
        float maxVisibility = 0f;

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
        View child = lvShorts.getChildAt(position - lvShorts.getFirstVisiblePosition());
        if (child != null && child.getTag() instanceof ShortsAdapter.ViewHolder) {
            ShortsAdapter.ViewHolder holder = (ShortsAdapter.ViewHolder) child.getTag();
            ShortVideo video = (ShortVideo) adapter.getItem(position);

            player.stop();
            player.clearMediaItems();

            holder.playerView.setPlayer(null);
            holder.playerView.setPlayer(player);

            MediaItem mediaItem = MediaItem.fromUri(video.getVideoUrl());
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }
    }

    @Override
    public void onVideoClick(int position) {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        }
    }

    @Override
    public void onLikeClick(int position) {
        ShortVideo video = (ShortVideo) adapter.getItem(position);
        boolean newState = !video.isLiked();
        
        video.setLiked(newState);
        video.setLikeCount(video.getLikeCount() + (newState ? 1 : -1));
        
        adapter.notifyDataSetChanged();
        viewModel.toggleLike(video, newState);
    }

    @Override
    public void onCommentClick(int position) {
        ShortVideo video = (ShortVideo) adapter.getItem(position);
        showCommentsDialog(video);
    }

    @Override
    public void onShareClick(int position) {
        ShortVideo video = (ShortVideo) adapter.getItem(position);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Xem video \"" + video.getTitle() + "\" trên CineFlow!\n" + video.getVideoUrl());
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
    }

    private void showCommentsDialog(ShortVideo video) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_comments, null);

        ListView lvComments = dialogView.findViewById(R.id.lv_comments);
        
        // Show dialog first with loading state or empty
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .show();

        viewModel.getComments(video.getId()).observe(getViewLifecycleOwner(), commentsDto -> {
            List<String> commentStrings = new ArrayList<>();
            if (commentsDto != null && !commentsDto.isEmpty()) {
                for (com.android.cineflow.data.network.dto.CommentDto c : commentsDto) {
                    commentStrings.add(c.getAuthorName() + ": " + c.getContent());
                }
            } else {
                commentStrings.add("Chưa có bình luận nào.");
            }
            
            ArrayAdapter<String> commentAdapter = new ArrayAdapter<>(
                    requireContext(),
                    R.layout.item_comment,
                    R.id.tv_comment_content,
                    commentStrings);

            lvComments.setAdapter(commentAdapter);
        });
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

package com.android.cineflow.ui.shorts;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.cineflow.R;
import com.android.cineflow.data.model.ShortVideo;
import com.android.cineflow.ui.base.BaseFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ShortsFragment extends BaseFragment implements ShortsAdapter.OnShortInteractionListener {

    private static final String TAG = "ShortsFragment";

    private ViewPager2 viewPagerShorts;
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
        viewPagerShorts = view.findViewById(R.id.view_pager_shorts);
        adapter = new ShortsAdapter(requireContext(), new ArrayList<>());
        adapter.setListener(this);
        viewPagerShorts.setAdapter(adapter);

        player = new ExoPlayer.Builder(requireContext()).build();
        player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);

        viewPagerShorts.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPlayingPosition = position;
                viewPagerShorts.post(() -> attachPlayerToItem(position));

                // Check if we need to load more items
                if (adapter != null && position >= adapter.getItemCount() - 2 && !isLoadingMore) {
                    isLoadingMore = true;
                    Log.d(TAG, "Load more triggered at page: " + position);
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
            if (currentPlayingPosition == -1 && shortVideos != null && !shortVideos.isEmpty()) {
                currentPlayingPosition = 0;
                viewPagerShorts.post(() -> attachPlayerToItem(0));
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attachPlayerToItem(int position) {
        if (viewPagerShorts == null || adapter == null || adapter.getItemCount() == 0 || position < 0 || position >= adapter.getItemCount()) {
            return;
        }

        RecyclerView recyclerView = (RecyclerView) viewPagerShorts.getChildAt(0);
        if (recyclerView == null) return;

        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder instanceof ShortsAdapter.ViewHolder) {
            ShortsAdapter.ViewHolder holder = (ShortsAdapter.ViewHolder) viewHolder;
            ShortVideo video = adapter.getItem(position);

            player.stop();
            player.clearMediaItems();

            holder.playerView.setPlayer(null);
            holder.playerView.setPlayer(player);

            MediaItem mediaItem = MediaItem.fromUri(com.android.cineflow.data.network.ApiClient.resolveLocalhostUrl(video.getVideoUrl()));
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        } else {
            // ViewHolder is not yet bound/created by RecyclerView, retry after short delay
            viewPagerShorts.postDelayed(() -> {
                if (currentPlayingPosition == position) {
                    attachPlayerToItem(position);
                }
            }, 100);
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
        ShortVideo video = adapter.getItem(position);
        boolean newState = !video.isLiked();

        video.setLiked(newState);
        video.setLikeCount(video.getLikeCount() + (newState ? 1 : -1));

        adapter.notifyItemChanged(position);
        viewModel.toggleLike(video, newState);
    }

    @Override
    public void onCommentClick(int position) {
        ShortVideo video = adapter.getItem(position);
        showCommentsDialog(video);
    }

    @Override
    public void onShareClick(int position) {
        ShortVideo video = adapter.getItem(position);
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
        View llCommentInput = dialogView.findViewById(R.id.ll_comment_input_shorts);
        EditText etCommentInput = dialogView.findViewById(R.id.et_comment_input_shorts);
        View btnSendComment = dialogView.findViewById(R.id.btn_send_comment_shorts);
        TextView tvCommentLoginHint = dialogView.findViewById(R.id.tv_comment_login_hint_shorts);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .show();

        com.android.cineflow.data.auth.AuthManager auth = com.android.cineflow.data.auth.AuthManager.getInstance();
        boolean loggedIn = auth != null && auth.isLoggedIn();

        if (loggedIn) {
            llCommentInput.setVisibility(View.VISIBLE);
            tvCommentLoginHint.setVisibility(View.GONE);
        } else {
            llCommentInput.setVisibility(View.GONE);
            tvCommentLoginHint.setVisibility(View.VISIBLE);
            tvCommentLoginHint.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(requireContext(), com.android.cineflow.ui.auth.LoginActivity.class));
            });
        }

        List<String> commentStrings = new ArrayList<>();
        ArrayAdapter<String> commentAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_comment,
                R.id.tv_comment_content,
                commentStrings);
        lvComments.setAdapter(commentAdapter);

        // Define runnable to load and refresh comments
        Runnable loadComments = new Runnable() {
            @Override
            public void run() {
                viewModel.getComments(video.getId()).observe(getViewLifecycleOwner(), commentsDto -> {
                    commentStrings.clear();
                    if (commentsDto != null && !commentsDto.isEmpty()) {
                        for (com.android.cineflow.data.network.dto.CommentDto c : commentsDto) {
                            String author = c.getAuthorName() != null ? c.getAuthorName() : "Người dùng";
                            commentStrings.add(author + ": " + c.getContent());
                        }
                    } else {
                        commentStrings.add("Chưa có bình luận nào.");
                    }
                    commentAdapter.notifyDataSetChanged();
                });
            }
        };

        // Load initially
        loadComments.run();

        btnSendComment.setOnClickListener(v -> {
            String text = etCommentInput.getText().toString().trim();
            if (text.isEmpty()) {
                etCommentInput.setError("Nội dung không được để trống");
                return;
            }
            btnSendComment.setEnabled(false);
            viewModel.postComment(video.getId(), text).observe(getViewLifecycleOwner(), newComment -> {
                btnSendComment.setEnabled(true);
                if (newComment != null) {
                    etCommentInput.setText("");
                    loadComments.run();
                } else {
                    Toast.makeText(requireContext(), "Không thể gửi bình luận", Toast.LENGTH_SHORT).show();
                }
            });
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

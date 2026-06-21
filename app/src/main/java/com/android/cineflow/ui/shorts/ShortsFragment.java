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
        com.android.cineflow.data.auth.AuthManager auth = com.android.cineflow.data.auth.AuthManager.getInstance();
        if (auth == null || !auth.isLoggedIn()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), com.android.cineflow.ui.auth.LoginActivity.class));
            return;
        }

        ShortVideo video = adapter.getItem(position);
        boolean newState = !video.isLiked();

        video.setLiked(newState);
        video.setLikeCount(video.getLikeCount() + (newState ? 1 : -1));

        // Update visible holder directly to avoid PlayerView re-bind (black screen)
        androidx.recyclerview.widget.RecyclerView rv =
                (androidx.recyclerview.widget.RecyclerView) viewPagerShorts.getChildAt(0);
        if (rv != null) {
            RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(position);
            if (vh instanceof ShortsAdapter.ViewHolder) {
                ((ShortsAdapter.ViewHolder) vh).bind(video);
            }
        }

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
                getString(R.string.share_video_text, video.getTitle(), video.getVideoUrl()));
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
    }

    private void showCommentsDialog(ShortVideo video) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_comments, null);

        ListView lvComments = dialogView.findViewById(R.id.lv_comments);
        View llCommentInput = dialogView.findViewById(R.id.ll_comment_input_shorts);
        EditText etCommentInput = dialogView.findViewById(R.id.et_comment_input_shorts);
        View btnSendComment = dialogView.findViewById(R.id.btn_send_comment_shorts);
        TextView tvCommentLoginHint = dialogView.findViewById(R.id.tv_comment_login_hint_shorts);
        TextView tvCommentsCount = dialogView.findViewById(R.id.tv_comments_count);
        View ivCommentsClose = dialogView.findViewById(R.id.iv_comments_close);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setBackgroundInsetTop(0)
                .setBackgroundInsetBottom(0)
                .show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setGravity(android.view.Gravity.BOTTOM);
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        ivCommentsClose.setOnClickListener(v -> dialog.dismiss());

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

        List<com.android.cineflow.data.network.dto.CommentDto> commentList = new ArrayList<>();
        ArrayAdapter<com.android.cineflow.data.network.dto.CommentDto> commentAdapter =
                new ArrayAdapter<com.android.cineflow.data.network.dto.CommentDto>(
                        requireContext(), R.layout.item_comment, commentList) {
                    @Override
                    public View getView(int position, View convertView, android.view.ViewGroup parent) {
                        View v = convertView != null ? convertView
                                : LayoutInflater.from(getContext()).inflate(R.layout.item_comment, parent, false);
                        TextView tvUser = v.findViewById(R.id.tv_comment_user);
                        TextView tvContent = v.findViewById(R.id.tv_comment_content);
                        com.android.cineflow.data.network.dto.CommentDto c = getItem(position);
                        if (c == null) return v;
                        boolean isEmpty = c.getId() == null;
                        String author = c.getAuthorName() != null ? "@" + c.getAuthorName() : "@user";
                        tvUser.setText(author);
                        tvContent.setText(c.getContent() != null ? c.getContent() : "");
                        tvUser.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                        if (isEmpty) {
                            tvContent.setTextColor(getResources().getColor(R.color.text_tertiary));
                            tvContent.setGravity(android.view.Gravity.CENTER);
                        } else {
                            tvContent.setTextColor(getResources().getColor(R.color.text_primary));
                            tvContent.setGravity(android.view.Gravity.START);
                        }
                        return v;
                    }
                };
        lvComments.setAdapter(commentAdapter);

        // Define runnable to load and refresh comments
        Runnable loadComments = new Runnable() {
            @Override
            public void run() {
                viewModel.getComments(video.getId()).observe(getViewLifecycleOwner(), commentsDto -> {
                    commentList.clear();
                    int count = 0;
                    if (commentsDto != null && !commentsDto.isEmpty()) {
                        commentList.addAll(commentsDto);
                        count = commentsDto.size();
                    } else {
                        com.android.cineflow.data.network.dto.CommentDto empty =
                                new com.android.cineflow.data.network.dto.CommentDto();
                        empty.setContent("Chưa có bình luận nào.");
                        commentList.add(empty);
                    }
                    tvCommentsCount.setText(String.valueOf(count));
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

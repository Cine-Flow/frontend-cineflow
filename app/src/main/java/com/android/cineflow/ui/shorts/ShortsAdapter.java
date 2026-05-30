package com.android.cineflow.ui.shorts;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.media3.ui.PlayerView;

import com.android.cineflow.R;
import com.android.cineflow.data.model.ShortVideo;

import java.util.List;

public class ShortsAdapter extends BaseAdapter {

    private final Context context;
    private List<ShortVideo> items;
    private OnShortInteractionListener listener;

    public interface OnShortInteractionListener {
        void onVideoClick(int position);
        void onLikeClick(int position);
        void onCommentClick(int position);
        void onShareClick(int position);
    }

    public ShortsAdapter(Context context, List<ShortVideo> items) {
        this.context = context;
        this.items = items;
    }

    public void setListener(OnShortInteractionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ShortVideo> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_short, parent, false);
            holder = new ViewHolder(convertView);

            // Make each item full screen in a ListView
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int screenHeight = displayMetrics.heightPixels;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenHeight);
            convertView.setLayoutParams(params);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ShortVideo video = items.get(position);
        holder.bind(video);

        // Click on entire video area -> play/pause
        final View finalConvertView = convertView;
        finalConvertView.setOnClickListener(v -> {
            if (listener != null) listener.onVideoClick(position);
        });

        // Like button
        holder.btnLike.setOnClickListener(v -> {
            if (listener != null) listener.onLikeClick(position);
        });

        // Comment button
        holder.btnComment.setOnClickListener(v -> {
            if (listener != null) listener.onCommentClick(position);
        });

        // Share button
        holder.btnShare.setOnClickListener(v -> {
            if (listener != null) listener.onShareClick(position);
        });

        return convertView;
    }

    public static class ViewHolder {
        public final PlayerView playerView;
        public final TextView tvUploader;
        public final TextView tvTitle;
        public final TextView tvDescription;
        public final ImageView btnLike;
        public final TextView tvLikeCount;
        public final ImageView btnComment;
        public final ImageView btnShare;

        public ViewHolder(View view) {
            playerView = view.findViewById(R.id.player_view);
            tvUploader = view.findViewById(R.id.tv_short_uploader);
            tvTitle = view.findViewById(R.id.tv_short_title);
            tvDescription = view.findViewById(R.id.tv_short_description);
            btnLike = view.findViewById(R.id.btn_like);
            tvLikeCount = view.findViewById(R.id.tv_like_count);
            btnComment = view.findViewById(R.id.btn_comment);
            btnShare = view.findViewById(R.id.btn_share);
        }

        public void bind(ShortVideo video) {
            tvUploader.setText(video.getUploader());
            tvTitle.setText(video.getTitle());
            tvDescription.setText(video.getDescription());

            // Like state
            tvLikeCount.setText(formatCount(video.getLikeCount()));
            if (video.isLiked()) {
                btnLike.setImageResource(R.drawable.ic_heart_filled);
            } else {
                btnLike.setImageResource(R.drawable.ic_heart_outline);
            }

            // We do not set the player here. The Fragment will manage the single ExoPlayer instance
            // and attach it to the visible ViewHolder's PlayerView.
        }

        private String formatCount(int count) {
            if (count >= 1_000_000) {
                return String.format("%.1fM", count / 1_000_000.0);
            } else if (count >= 1_000) {
                return String.format("%.1fK", count / 1_000.0);
            }
            return String.valueOf(count);
        }
    }
}

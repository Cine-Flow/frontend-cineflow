package com.android.cineflow.ui.detail;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.network.dto.FilmCommentDto;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FilmCommentAdapter extends RecyclerView.Adapter<FilmCommentAdapter.VH> {

    public interface OnCommentActionListener {
        void onDelete(FilmCommentDto comment);
    }

    private final List<FilmCommentDto> comments = new ArrayList<>();
    private String currentUserId;
    private OnCommentActionListener listener;

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public void setOnCommentActionListener(OnCommentActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<FilmCommentDto> data) {
        comments.clear();
        if (data != null) comments.addAll(data);
        notifyDataSetChanged();
    }

    public void prepend(FilmCommentDto comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
    }

    public void remove(FilmCommentDto comment) {
        int idx = comments.indexOf(comment);
        if (idx >= 0) {
            comments.remove(idx);
            notifyItemRemoved(idx);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_film_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FilmCommentDto c = comments.get(position);

        String username = c.getUsername();
        h.tvUsername.setText(username);
        h.tvContent.setText(c.getContent());

        // Avatar initial
        String initial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase(Locale.ROOT);
        h.tvInitial.setText(initial);

        h.tvTime.setText(formatRelative(c.getCreatedAt()));

        boolean canDelete = currentUserId != null && currentUserId.equals(c.getUserId());
        h.ivDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
        h.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(c);
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public int size() {
        return comments.size();
    }

    private static String formatRelative(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        // Backend serializes LocalDateTime as e.g. "2026-06-19T14:30:00.123"
        SimpleDateFormat[] formats = new SimpleDateFormat[] {
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        };
        Date date = null;
        for (SimpleDateFormat fmt : formats) {
            ParsePosition pos = new ParsePosition(0);
            Date parsed = fmt.parse(iso, pos);
            if (parsed != null) { date = parsed; break; }
        }
        if (date == null) return iso;
        long now = System.currentTimeMillis();
        long t = date.getTime();
        if (now - t < DateUtils.MINUTE_IN_MILLIS) return "vừa xong";
        return DateUtils.getRelativeTimeSpanString(
                t, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvInitial;
        final TextView tvUsername;
        final TextView tvTime;
        final TextView tvContent;
        final ImageView ivDelete;

        VH(@NonNull View v) {
            super(v);
            tvInitial  = v.findViewById(R.id.tv_comment_initial);
            tvUsername = v.findViewById(R.id.tv_comment_username);
            tvTime     = v.findViewById(R.id.tv_comment_time);
            tvContent  = v.findViewById(R.id.tv_comment_content);
            ivDelete   = v.findViewById(R.id.iv_comment_delete);
        }
    }
}

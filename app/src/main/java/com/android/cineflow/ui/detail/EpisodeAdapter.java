package com.android.cineflow.ui.detail;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.download.OfflineDownloadManager;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.EpisodeDto;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    private List<EpisodeDto> episodes;
    private final OnEpisodeClickListener listener;
    
    private String filmId;
    private String filmTitle;
    private String filmCoverUrl;

    public interface OnEpisodeClickListener {
        void onEpisodeClick(EpisodeDto episode);
    }

    public EpisodeAdapter(List<EpisodeDto> episodes, OnEpisodeClickListener listener) {
        this.episodes = episodes;
        this.listener = listener;
    }

    public void setEpisodes(List<EpisodeDto> episodes) {
        this.episodes = episodes;
        notifyDataSetChanged();
    }

    public void setFilmContext(String filmId, String filmTitle, String filmCoverUrl) {
        this.filmId = filmId;
        this.filmTitle = filmTitle;
        this.filmCoverUrl = filmCoverUrl;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        EpisodeDto episode = episodes.get(position);
        holder.tvTitle.setText(episode.getTitle() != null ? episode.getTitle() : "Tập " + episode.getEpisodeNumber());
        int duration = episode.getDuration() != null ? episode.getDuration() : 0;
        holder.tvDuration.setText((duration / 60) + " phút");
        String thumbnailUrl = ApiClient.resolveLocalhostUrl(filmCoverUrl);
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            Glide.with(holder.ivThumbnail)
                    .load(thumbnailUrl)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_card)
                    .error(R.drawable.placeholder_card)
                    .into(holder.ivThumbnail);
        } else {
            Glide.with(holder.ivThumbnail).clear(holder.ivThumbnail);
            holder.ivThumbnail.setImageResource(R.drawable.placeholder_card);
        }

        holder.itemView.setOnClickListener(v -> listener.onEpisodeClick(episode));

        // --- Offline Download Logic ---
        if (filmId == null) {
            holder.btnDownload.setVisibility(View.GONE);
            holder.progressDownload.setVisibility(View.GONE);
            holder.tvProgressText.setVisibility(View.GONE);
            return;
        }

        holder.btnDownload.setVisibility(View.VISIBLE);
        holder.progressDownload.setVisibility(View.GONE);
        holder.tvProgressText.setVisibility(View.GONE);

        boolean isDownloaded = OfflineDownloadManager.getInstance().isEpisodeDownloaded(filmId, episode.getEpisodeNumber());

        if (isDownloaded) {
            holder.btnDownload.setImageResource(R.drawable.ic_shield);
            holder.btnDownload.setImageTintList(ColorStateList.valueOf(
                    holder.itemView.getContext().getColor(R.color.brand_primary)
            ));
            holder.btnDownload.setOnClickListener(v -> {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Quản lý bản tải")
                        .setMessage("Tập phim này đã được tải xuống ngoại tuyến. Bạn có muốn xóa bản tải này không?")
                        .setPositiveButton("Xóa ngoại tuyến", (dialog, which) -> {
                            OfflineDownloadManager.getInstance().deleteDownloadedEpisode(filmId, episode.getEpisodeNumber());
                            Toast.makeText(holder.itemView.getContext(), "Đã xóa tập phim ngoại tuyến", Toast.LENGTH_SHORT).show();
                            notifyItemChanged(position);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        } else {
            holder.btnDownload.setImageResource(R.drawable.ic_download);
            holder.btnDownload.setImageTintList(ColorStateList.valueOf(
                    holder.itemView.getContext().getColor(R.color.text_secondary)
            ));
            holder.btnDownload.setOnClickListener(v -> {
                holder.btnDownload.setVisibility(View.GONE);
                holder.progressDownload.setVisibility(View.VISIBLE);
                holder.tvProgressText.setVisibility(View.VISIBLE);
                holder.tvProgressText.setText("0%");

                OfflineDownloadManager.getInstance().startDownloadEpisode(
                        filmId, filmTitle, filmCoverUrl, episode.getEpisodeNumber(),
                        episode.getTitle() != null ? episode.getTitle() : "Tập " + episode.getEpisodeNumber(),
                        (duration / 60) + " phút",
                        episode.getVideoUrl(),
                        new OfflineDownloadManager.OnDownloadProgressListener() {
                            @Override
                            public void onProgress(int progress) {
                                holder.tvProgressText.setText(progress + "%");
                            }

                            @Override
                            public void onComplete(OfflineDownloadManager.OfflineEpisode offlineEp) {
                                holder.progressDownload.setVisibility(View.GONE);
                                holder.tvProgressText.setVisibility(View.GONE);
                                holder.btnDownload.setVisibility(View.VISIBLE);
                                holder.btnDownload.setImageResource(R.drawable.ic_shield);
                                holder.btnDownload.setImageTintList(ColorStateList.valueOf(
                                        holder.itemView.getContext().getColor(R.color.brand_primary)
                                ));
                                Toast.makeText(holder.itemView.getContext(), "Tải xuống tập phim thành công!", Toast.LENGTH_SHORT).show();
                                notifyItemChanged(position);
                            }

                            @Override
                            public void onError(String error) {
                                holder.progressDownload.setVisibility(View.GONE);
                                holder.tvProgressText.setVisibility(View.GONE);
                                holder.btnDownload.setVisibility(View.VISIBLE);
                                Toast.makeText(holder.itemView.getContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            });
        }
    }

    @Override
    public int getItemCount() {
        return episodes != null ? episodes.size() : 0;
    }

    static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDuration;
        ImageView ivThumbnail;
        ImageView btnDownload;
        ProgressBar progressDownload;
        TextView tvProgressText;

        public EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_episode_title);
            tvDuration = itemView.findViewById(R.id.tv_episode_duration);
            ivThumbnail = itemView.findViewById(R.id.iv_episode_thumbnail);
            btnDownload = itemView.findViewById(R.id.btn_download_episode);
            progressDownload = itemView.findViewById(R.id.progress_download);
            tvProgressText = itemView.findViewById(R.id.tv_download_progress_text);
        }
    }
}

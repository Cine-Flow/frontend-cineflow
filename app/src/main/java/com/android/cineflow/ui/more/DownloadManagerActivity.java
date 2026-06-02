package com.android.cineflow.ui.more;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.download.OfflineDownloadManager;
import com.android.cineflow.data.download.OfflineDownloadManager.OfflineEpisode;
import com.android.cineflow.ui.player.PlayerActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class DownloadManagerActivity extends AppCompatActivity {

    private RecyclerView rvDownloads;
    private View layoutEmptyState;
    private TextView btnClearAll;
    private DownloadAdapter adapter;
    private List<OfflineEpisode> downloadedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);

        initViews();
        loadDownloads();
    }

    private void initViews() {
        rvDownloads = findViewById(R.id.rv_downloads);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        btnClearAll = findViewById(R.id.btn_clear_all);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rvDownloads.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadAdapter();
        rvDownloads.setAdapter(adapter);

        btnClearAll.setOnClickListener(v -> showClearAllConfirmDialog());
    }

    private void loadDownloads() {
        downloadedList = OfflineDownloadManager.getInstance().getDownloadedEpisodes();
        if (downloadedList == null || downloadedList.isEmpty()) {
            rvDownloads.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.GONE);
        } else {
            rvDownloads.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void playOfflineEpisode(OfflineEpisode episode) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_URL, episode.localPath);
        intent.putExtra(PlayerActivity.EXTRA_TITLE, episode.filmTitle + " - " + episode.episodeTitle + " [Ngoại tuyến]");
        intent.putExtra("extra_film_id", episode.filmId);
        startActivity(intent);
    }

    private void confirmDeleteEpisode(OfflineEpisode episode) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tập phim")
                .setMessage("Bạn có chắc chắn muốn xóa bản tải ngoại tuyến của '" + episode.episodeTitle + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    OfflineDownloadManager.getInstance().deleteDownloadedEpisode(episode.filmId, episode.episodeIndex);
                    Toast.makeText(this, "Đã xóa tập phim ngoại tuyến thành công", Toast.LENGTH_SHORT).show();
                    loadDownloads();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showClearAllConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tất cả bản tải")
                .setMessage("Bạn có chắc chắn muốn xóa sạch toàn bộ các tập phim đã tải ngoại tuyến trên thiết bị không?")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                    OfflineDownloadManager.getInstance().clearAllDownloads();
                    Toast.makeText(this, "Đã xóa toàn bộ bản tải ngoại tuyến", Toast.LENGTH_SHORT).show();
                    loadDownloads();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- Inner Download Adapter ---
    private class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {

        @NonNull
        @Override
        public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download_row, parent, false);
            return new DownloadViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
            OfflineEpisode episode = downloadedList.get(position);

            holder.tvFilmTitle.setText(episode.filmTitle);
            holder.tvEpisodeTitle.setText(episode.episodeTitle);
            holder.tvMeta.setText(episode.size + " • Đã tải " + episode.downloadDate);

            Glide.with(DownloadManagerActivity.this)
                    .load(episode.filmCoverUrl)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_card)
                    .into(holder.ivCover);

            holder.layoutItem.setOnClickListener(v -> playOfflineEpisode(episode));
            holder.btnDelete.setOnClickListener(v -> confirmDeleteEpisode(episode));
        }

        @Override
        public int getItemCount() {
            return downloadedList != null ? downloadedList.size() : 0;
        }

        class DownloadViewHolder extends RecyclerView.ViewHolder {
            View layoutItem;
            ImageView ivCover;
            TextView tvFilmTitle;
            TextView tvEpisodeTitle;
            TextView tvMeta;
            ImageView btnDelete;

            public DownloadViewHolder(@NonNull View itemView) {
                super(itemView);
                layoutItem = itemView.findViewById(R.id.layout_download_item);
                ivCover = itemView.findViewById(R.id.iv_download_cover);
                tvFilmTitle = itemView.findViewById(R.id.tv_download_film_title);
                tvEpisodeTitle = itemView.findViewById(R.id.tv_download_episode_title);
                tvMeta = itemView.findViewById(R.id.tv_download_meta);
                btnDelete = itemView.findViewById(R.id.btn_delete_download);
            }
        }
    }
}

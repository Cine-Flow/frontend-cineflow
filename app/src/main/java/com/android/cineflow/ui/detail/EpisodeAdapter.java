package com.android.cineflow.ui.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.network.dto.EpisodeDto;

import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    private List<EpisodeDto> episodes;
    private final OnEpisodeClickListener listener;

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

        holder.itemView.setOnClickListener(v -> listener.onEpisodeClick(episode));
    }

    @Override
    public int getItemCount() {
        return episodes != null ? episodes.size() : 0;
    }

    static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDuration;

        public EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_episode_title);
            tvDuration = itemView.findViewById(R.id.tv_episode_duration);
        }
    }
}

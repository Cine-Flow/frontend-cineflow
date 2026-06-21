package com.android.cineflow.ui.admin;

import android.graphics.drawable.GradientDrawable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.network.dto.FilmDetailDto;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AdminFilmAdapter extends RecyclerView.Adapter<AdminFilmAdapter.FilmViewHolder> {

    private List<FilmDetailDto> films = new ArrayList<>();
    private OnFilmActionListener listener;

    public interface OnFilmActionListener {
        void onEditFilm(FilmDetailDto film);
        void onDeleteFilm(FilmDetailDto film);
    }

    public AdminFilmAdapter(OnFilmActionListener listener) {
        this.listener = listener;
    }

    public void setFilms(List<FilmDetailDto> films) {
        this.films = films != null ? films : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_film, parent, false);
        return new FilmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        FilmDetailDto film = films.get(position);
        holder.bind(film);
    }

    @Override
    public int getItemCount() { return films.size(); }

    class FilmViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivThumbnail;
        private final TextView tvTitle;
        private final TextView tvType;
        private final TextView tvYear;
        private final TextView tvPremium;
        private final TextView tvDescription;
        private final ImageView btnEdit;
        private final ImageView btnDelete;

        FilmViewHolder(View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_film_title);
            tvType = itemView.findViewById(R.id.tv_film_type);
            tvYear = itemView.findViewById(R.id.tv_film_year);
            tvPremium = itemView.findViewById(R.id.tv_film_premium);
            tvDescription = itemView.findViewById(R.id.tv_film_description);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(FilmDetailDto film) {
            tvTitle.setText(film.getTitle());
            tvType.setText(formatType(film.getType()));
            tvYear.setText(String.valueOf(film.getReleaseYear()));
            tvDescription.setText(film.getDescription());

            if (film.getIsPremium()) {
                tvPremium.setVisibility(View.VISIBLE);
                tvPremium.setText(R.string.admin_premium_badge);
            } else {
                tvPremium.setVisibility(View.GONE);
            }

            tintTypeBadge(film.getType());

            Glide.with(itemView.getContext())
                    .load(com.android.cineflow.data.network.ApiClient.resolveLocalhostUrl(film.getThumbnailUrl()))
                    .centerCrop()
                    .placeholder(R.color.surface_tertiary)
                    .into(ivThumbnail);

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditFilm(film);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteFilm(film);
            });
        }

        private String formatType(String type) {
            if (type == null) return "";
            switch (type) {
                case "SINGLE": return "Movie";
                case "SERIES": return "Series";
                case "LIVE": return "Live";
                default: return type;
            }
        }

        private void tintTypeBadge(String type) {
            int bgColor;
            if ("SINGLE".equals(type)) {
                bgColor = itemView.getContext().getColor(R.color.badge_movie);
            } else if ("SERIES".equals(type)) {
                bgColor = itemView.getContext().getColor(R.color.badge_series);
            } else if ("LIVE".equals(type)) {
                bgColor = itemView.getContext().getColor(R.color.badge_live);
            } else {
                bgColor = itemView.getContext().getColor(R.color.text_tertiary);
            }
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(24f);
            bg.setColor(bgColor);
            tvType.setBackground(bg);
            tvType.setTextColor(itemView.getContext().getColor(R.color.text_primary));
        }
    }
}

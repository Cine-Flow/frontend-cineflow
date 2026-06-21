package com.android.cineflow.ui.catalog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.network.dto.FilmDto;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class FilmCatalogAdapter extends RecyclerView.Adapter<FilmCatalogAdapter.VH> {

    public interface OnFilmClickListener {
        void onFilmClick(FilmDto film);
    }

    private final Context context;
    private final List<FilmDto> films = new ArrayList<>();
    private OnFilmClickListener listener;

    public FilmCatalogAdapter(Context context) {
        this.context = context;
    }

    public void setOnFilmClickListener(OnFilmClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<FilmDto> data) {
        films.clear();
        if (data != null) films.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_film_catalog, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FilmDto film = films.get(position);

        Glide.with(context)
                .load(com.android.cineflow.data.network.ApiClient.resolveLocalhostUrl(film.getThumbnailUrl()))
                .placeholder(R.drawable.placeholder_card)
                .centerCrop()
                .into(h.ivThumb);

        h.tvTitle.setText(film.getTitle());

        String typeLabel = "SERIES".equalsIgnoreCase(film.getType()) ? "Phim bộ" : "Phim lẻ";
        int year = film.getReleaseYear();
        if (year > 0) {
            h.tvMeta.setText(typeLabel + " • " + year);
            h.tvYear.setText(String.valueOf(year));
            h.tvYear.setVisibility(View.VISIBLE);
        } else {
            h.tvMeta.setText(typeLabel);
            h.tvYear.setVisibility(View.GONE);
        }

        h.tvPremium.setVisibility(film.getIsPremium() ? View.VISIBLE : View.GONE);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFilmClick(film);
        });
    }

    @Override
    public int getItemCount() {
        return films.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivThumb;
        final TextView tvTitle;
        final TextView tvMeta;
        final TextView tvYear;
        final TextView tvPremium;

        VH(@NonNull View itemView) {
            super(itemView);
            ivThumb   = itemView.findViewById(R.id.iv_catalog_thumbnail);
            tvTitle   = itemView.findViewById(R.id.tv_catalog_title);
            tvMeta    = itemView.findViewById(R.id.tv_catalog_meta);
            tvYear    = itemView.findViewById(R.id.tv_catalog_year);
            tvPremium = itemView.findViewById(R.id.tv_catalog_premium);
        }
    }
}

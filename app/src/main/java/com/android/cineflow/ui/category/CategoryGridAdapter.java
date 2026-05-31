package com.android.cineflow.ui.category;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.cineflow.R;
import com.android.cineflow.data.network.dto.FilmDto;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple BaseAdapter that renders FilmDto items in a GridView
 * using the existing item_card_portrait layout.
 */
public class CategoryGridAdapter extends BaseAdapter {

    private final Context context;
    private List<FilmDto> films = new ArrayList<>();

    public CategoryGridAdapter(Context context) {
        this.context = context;
    }

    public void setFilms(List<FilmDto> newFilms) {
        this.films = newFilms != null ? newFilms : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return films.size();
    }

    @Override
    public FilmDto getItem(int position) {
        return films.get(position);
    }

    @Override
    public long getItemId(int position) {
        return films.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_card_portrait, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FilmDto film = films.get(position);

        Glide.with(context)
                .load(film.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_card)
                .centerCrop()
                .into(holder.ivThumbnail);

        if (holder.tvTitle != null) {
            holder.tvTitle.setText(film.getTitle());
            holder.tvTitle.setVisibility(View.VISIBLE);
        }

        if (holder.tvBadge != null) {
            boolean isPremium = film.getIsPremium();
            boolean isNew = film.getReleaseYear() >= 2025;
            if (isPremium) {
                holder.tvBadge.setText("4K");
                holder.tvBadge.setVisibility(View.VISIBLE);
            } else if (isNew) {
                holder.tvBadge.setText("NEW");
                holder.tvBadge.setVisibility(View.VISIBLE);
            } else {
                holder.tvBadge.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    static class ViewHolder {
        final ImageView ivThumbnail;
        final TextView tvTitle;
        final TextView tvBadge;

        ViewHolder(View view) {
            ivThumbnail = view.findViewById(R.id.iv_thumbnail);
            tvTitle     = view.findViewById(R.id.tv_card_title);
            tvBadge     = view.findViewById(R.id.tv_card_badge);
        }
    }
}

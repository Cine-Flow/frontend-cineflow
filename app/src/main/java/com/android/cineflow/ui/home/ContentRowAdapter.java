package com.android.cineflow.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.cineflow.R;
import com.android.cineflow.data.common.uimodel.ContentCard;
import com.bumptech.glide.Glide;

import java.util.List;

public class ContentRowAdapter extends BaseAdapter {

    public interface OnCardClickListener {
        void onClick(ContentCard card);
    }

    private final Context context;
    private final List<ContentCard> items;
    private final int cardStyle;
    private final OnCardClickListener listener;

    public ContentRowAdapter(Context context, List<ContentCard> items,
                             int cardStyle, OnCardClickListener listener) {
        this.context   = context;
        this.items     = items;
        this.cardStyle = cardStyle;
        this.listener  = listener;
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
        CardViewHolder holder;
        if (convertView == null) {
            int layoutRes;
            switch (cardStyle) {
                case ContentCard.STYLE_LANDSCAPE: layoutRes = R.layout.item_card_landscape; break;
                case ContentCard.STYLE_BANNER:    layoutRes = R.layout.item_card_banner;    break;
                default:                          layoutRes = R.layout.item_card_portrait;  break;
            }
            convertView = LayoutInflater.from(context).inflate(layoutRes, parent, false);
            holder = new CardViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (CardViewHolder) convertView.getTag();
        }

        ContentCard card = items.get(position);
        holder.bind(card);
        convertView.setOnClickListener(v -> { if (listener != null) listener.onClick(card); });

        return convertView;
    }

    static class CardViewHolder {
        private final ImageView ivThumbnail;
        private final TextView tvTitle;
        private final TextView tvBadge;

        CardViewHolder(View itemView) {
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvTitle     = itemView.findViewById(R.id.tv_card_title);
            tvBadge     = itemView.findViewById(R.id.tv_card_badge);
        }

        void bind(ContentCard card) {
            Glide.with(ivThumbnail.getContext())
                    .load(com.android.cineflow.data.network.ApiClient.resolveLocalhostUrl(card.getThumbnailUrl()))
                    .placeholder(R.drawable.placeholder_card)
                    .centerCrop()
                    .into(ivThumbnail);

            if (tvTitle != null) {
                boolean hasTitle = card.getTitle() != null && !card.getTitle().isEmpty();
                tvTitle.setVisibility(hasTitle ? View.VISIBLE : View.GONE);
                if (hasTitle) tvTitle.setText(card.getTitle());
            }

            if (tvBadge != null) {
                boolean hasBadge = card.getBadgeLabel() != null && !card.getBadgeLabel().isEmpty();
                tvBadge.setVisibility(hasBadge ? View.VISIBLE : View.GONE);
                if (hasBadge) tvBadge.setText(card.getBadgeLabel());
            }
        }
    }
}

package com.android.cineflow.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.common.uimodel.ContentCard;
import com.bumptech.glide.Glide;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final Context context;
    private final List<ContentCard> cards;
    private final OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(ContentCard card);
    }

    public BannerAdapter(Context context, List<ContentCard> cards, OnBannerClickListener listener) {
        this.context = context;
        this.cards = cards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        ContentCard card = cards.get(position);
        
        boolean hasTitle = card.getTitle() != null && !card.getTitle().isEmpty();
        holder.tvTitle.setVisibility(hasTitle ? View.VISIBLE : View.GONE);
        if (hasTitle) holder.tvTitle.setText(card.getTitle());
        
        Glide.with(context)
             .load(com.android.cineflow.data.network.ApiClient.resolveLocalhostUrl(card.getThumbnailUrl()))
             .into(holder.ivThumbnail);
             
        boolean hasBadge = card.getBadgeLabel() != null && !card.getBadgeLabel().isEmpty();
        holder.tvBadge.setVisibility(hasBadge ? View.VISIBLE : View.GONE);
        if (hasBadge) holder.tvBadge.setText(card.getBadgeLabel());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBannerClick(card);
        });

        // Restore root itemView dimensions to MATCH_PARENT to satisfy ViewPager2 strict checks
        ViewGroup.LayoutParams rootParams = holder.itemView.getLayoutParams();
        if (rootParams != null) {
            rootParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            rootParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            if (rootParams instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) rootParams).setMargins(16, 0, 16, 0);
            }
            holder.itemView.setLayoutParams(rootParams);
        }

        // Adjust layout params for the inner card container instead of root itemView or ImageView
        View cardContainer = holder.itemView.findViewById(R.id.banner_card_container);
        if (cardContainer != null) {
            ViewGroup.LayoutParams cardParams = cardContainer.getLayoutParams();
            if (cardParams != null) {
                cardParams.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85); // 85% width
                cardParams.height = (int) (200 * (context.getResources().getDisplayMetrics().densityDpi / 160f)); // approx 200dp height
                cardContainer.setLayoutParams(cardParams);
            }
        }
    }

    @Override
    public int getItemCount() {
        return cards != null ? cards.size() : 0;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle;
        TextView tvBadge;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_card_title);
            tvBadge = itemView.findViewById(R.id.tv_card_badge);
        }
    }
}

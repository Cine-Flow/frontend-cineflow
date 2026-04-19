package com.android.cineflow.ui.shorts;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.media3.ui.PlayerView;

import com.android.cineflow.R;
import com.android.cineflow.data.model.ShortVideo;

import java.util.List;

public class ShortsAdapter extends BaseAdapter {

    private final Context context;
    private List<ShortVideo> items;

    public ShortsAdapter(Context context, List<ShortVideo> items) {
        this.context = context;
        this.items = items;
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
            
            // To make each item full screen in a ListView, we need to set its layout params
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int screenHeight = displayMetrics.heightPixels;
            // Subtract bottom nav height roughly (e.g. 150 pixels or dynamically) if we want, 
            // but match_parent or passing display height works. For now, let's use display height.
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenHeight);
            convertView.setLayoutParams(params);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ShortVideo video = items.get(position);
        holder.bind(video);

        return convertView;
    }

    public static class ViewHolder {
        public final PlayerView playerView;
        public final TextView tvUploader;
        public final TextView tvTitle;

        public ViewHolder(View view) {
            playerView = view.findViewById(R.id.player_view);
            tvUploader = view.findViewById(R.id.tv_short_uploader);
            tvTitle = view.findViewById(R.id.tv_short_title);
        }

        public void bind(ShortVideo video) {
            tvUploader.setText(video.getUploader());
            tvTitle.setText(video.getTitle());
            // We do not set the player here. The Fragment will manage the single ExoPlayer instance
            // and attach it to the visible ViewHolder's PlayerView.
        }
    }
}

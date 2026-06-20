package com.android.cineflow.ui.more;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.cineflow.R;

import java.util.ArrayList;
import java.util.List;

public class MoreAdapter extends BaseAdapter {
    public interface OnMoreItemClickListener {
        void onClick(MoreSection.MoreItem item);
    }

    private final Context context;
    private final OnMoreItemClickListener listener;
    private long favoriteCount;
    private long historyCount;
    private String userDisplayName;
    private List<MoreSection> sections;

    public MoreAdapter(Context context, List<MoreSection> sections) {
        this(context, sections, null);
    }

    public MoreAdapter(Context context, List<MoreSection> sections, OnMoreItemClickListener listener) {
        this.context = context;
        this.sections = sections != null ? sections : new ArrayList<>();
        this.listener = listener;
    }

    public void setData(List<MoreSection> data) {
        this.sections = data != null ? data : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setProfileStats(long favoriteCount, long historyCount) {
        setProfileStats(null, favoriteCount, historyCount);
    }

    public void setProfileStats(String displayName, long favoriteCount, long historyCount) {
        this.userDisplayName = displayName;
        this.favoriteCount = favoriteCount;
        this.historyCount = historyCount;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return sections.size(); }

    @Override
    public Object getItem(int position) { return sections.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public int getViewTypeCount() { return 7; }

    @Override
    public int getItemViewType(int position) {
        return sections.get(position).getType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        MoreSection section = sections.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            switch (type) {
                case MoreSection.TYPE_HEADER_LOGIN:
                    convertView = inflater.inflate(R.layout.item_more_login_header, parent, false);
                    break;
                case MoreSection.TYPE_DOWNLOAD_BANNER:
                    convertView = inflater.inflate(R.layout.item_more_download_banner, parent, false);
                    break;
                case MoreSection.TYPE_ACTION_GRID:
                case MoreSection.TYPE_APPS_SECTION:
                    convertView = inflater.inflate(R.layout.item_more_section_grid, parent, false);
                    break;
                case MoreSection.TYPE_HELP_ITEM:
                    convertView = inflater.inflate(R.layout.item_more_help_row, parent, false);
                    break;
                case MoreSection.TYPE_ADMIN_ENTRY:
                    convertView = inflater.inflate(R.layout.item_more_admin_entry, parent, false);
                    break;
                case MoreSection.TYPE_LOGOUT:
                    convertView = inflater.inflate(R.layout.item_more_logout, parent, false);
                    break;
                default:
                    convertView = new View(context);
            }
        }

        switch (type) {
            case MoreSection.TYPE_ACTION_GRID:
            case MoreSection.TYPE_APPS_SECTION:
                bindGridView(convertView, section);
                break;
            case MoreSection.TYPE_HELP_ITEM:
                bindHelpItem(convertView, section);
                break;
            case MoreSection.TYPE_HEADER_LOGIN:
                bindLoginHeader(convertView, section);
                break;
            case MoreSection.TYPE_LOGOUT:
                bindSignButton(convertView, section);
                break;
        }

        return convertView;
    }

    private void bindGridView(View v, MoreSection section) {
        TextView tvTitle = v.findViewById(R.id.tv_more_section_title);
        ViewGroup container = v.findViewById(R.id.more_grid_container);

        if (section.getTitle() != null) {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(section.getTitle());
        } else {
            tvTitle.setVisibility(View.GONE);
        }

        if (container != null) {
            container.removeAllViews();
            GridItemAdapter gridAdapter = new GridItemAdapter(context, section.getItems(), section.getType());
            for (int i = 0; i < gridAdapter.getCount(); i++) {
                container.addView(gridAdapter.getView(i, null, container));
            }
        }
    }

    private void bindHelpItem(View v, MoreSection section) {
        TextView tvLabel = v.findViewById(R.id.tv_help_label);
        if (section.getItems() != null && !section.getItems().isEmpty()) {
            tvLabel.setText(section.getItems().get(0).getLabel());
        }
    }

    private void bindLoginHeader(View v, MoreSection section) {
        TextView tvLoginText = v.findViewById(R.id.tv_login_text);
        TextView tvSubtitle = v.findViewById(R.id.tv_login_subtitle);
        View quickStats = v.findViewById(R.id.layout_quick_stats);
        TextView tvFavoriteCount = v.findViewById(R.id.tv_favorite_count);
        TextView tvHistoryCount = v.findViewById(R.id.tv_history_count);
        TextView tvDownloadCount = v.findViewById(R.id.tv_download_count);

        if (section.getTitle() != null) {
            String nameToDisplay = (userDisplayName != null && !userDisplayName.isEmpty()) ? userDisplayName : section.getTitle();
            tvLoginText.setText(nameToDisplay);
            tvSubtitle.setText(context.getString(R.string.more_manage_account));
            if (quickStats != null) {
                quickStats.setVisibility(View.VISIBLE);
            }
            tvFavoriteCount.setText(String.valueOf(favoriteCount));
            tvHistoryCount.setText(String.valueOf(historyCount));
            int downloadCount = com.android.cineflow.data.download.OfflineDownloadManager.getInstance().getDownloadedCount();
            tvDownloadCount.setText(String.valueOf(downloadCount));
        } else {
            tvLoginText.setText(context.getString(R.string.more_sign_in));
            tvSubtitle.setText(context.getString(R.string.more_tap_to_sign_in));
            if (quickStats != null) {
                quickStats.setVisibility(View.GONE);
            }
        }
    }

    private void bindSignButton(View v, MoreSection section) {
        TextView btnLogout = v.findViewById(R.id.btn_logout);
        if (btnLogout == null) return;

        boolean isSignOut = com.android.cineflow.data.auth.AuthManager.getInstance() != null 
                && com.android.cineflow.data.auth.AuthManager.getInstance().isLoggedIn();
        btnLogout.setText(section.getTitle() != null ? section.getTitle() : context.getString(R.string.more_sign_in));

        if (isSignOut) {
            btnLogout.setTextColor(context.getColor(R.color.status_error));
            GradientDrawable bg = (GradientDrawable) context.getDrawable(R.drawable.bg_more_logout_button);
            btnLogout.setBackground(bg);
        } else {
            btnLogout.setTextColor(context.getColor(R.color.brand_primary));
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(12f * context.getResources().getDisplayMetrics().density);
            bg.setColor(context.getColor(R.color.surface_secondary));
            bg.setStroke(1, context.getColor(R.color.brand_primary));
            btnLogout.setBackground(bg);
        }
    }

    // --- Inner Grid Adapter ---
    class GridItemAdapter extends BaseAdapter {
        Context ctx;
        List<MoreSection.MoreItem> items;
        int sectionType;

        GridItemAdapter(Context ctx, List<MoreSection.MoreItem> items, int sectionType) {
            this.ctx = ctx;
            this.items = items;
            this.sectionType = sectionType;
        }

        @Override public int getCount() { return items != null ? items.size() : 0; }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                int layoutId = (sectionType == MoreSection.TYPE_APPS_SECTION)
                        ? R.layout.item_more_app_card
                        : R.layout.item_more_action_card;
                convertView = LayoutInflater.from(ctx).inflate(layoutId, parent, false);
            }

            MoreSection.MoreItem item = items.get(position);
            TextView tv = convertView.findViewById(R.id.tv_item_label);
            ImageView iv = convertView.findViewById(R.id.iv_item_icon);

            tv.setText(item.getLabel());
            if (item.getIconRes() != 0) {
                iv.setImageResource(item.getIconRes());
            } else {
                iv.setImageResource(R.drawable.ic_launcher_foreground);
            }
            convertView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(item);
            });
            return convertView;
        }
    }
}

package com.android.cineflow.ui.more;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.cineflow.R;

import java.util.ArrayList;
import java.util.List;

public class MoreAdapter extends BaseAdapter {

    private final Context context;
    private List<MoreSection> sections;

    public MoreAdapter(Context context, List<MoreSection> sections) {
        this.context = context;
        this.sections = sections != null ? sections : new ArrayList<>();
    }

    public void setData(List<MoreSection> data) {
        this.sections = data != null ? data : new ArrayList<>();
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

        if (type == MoreSection.TYPE_ACTION_GRID || type == MoreSection.TYPE_APPS_SECTION) {
            bindGridView(convertView, section);
        } else if (type == MoreSection.TYPE_HELP_ITEM) {
            bindHelpItem(convertView, section);
        } else if (type == MoreSection.TYPE_HEADER_LOGIN) {
            bindLoginHeader(convertView, section);
        }
        // TYPE_ADMIN_ENTRY and TYPE_HEADER_LOGIN/TYPE_DOWNLOAD_BANNER are static layouts — no binding needed

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
        if (tvLoginText == null) {
            // Fallback: find the hardcoded "Đăng nhập" text view
            // The layout has a single TextView with no id — find it by traversing children
            if (v instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) v;
                for (int i = 0; i < group.getChildCount(); i++) {
                    View child = group.getChildAt(i);
                    if (child instanceof TextView) {
                        tvLoginText = (TextView) child;
                        break;
                    }
                }
            }
        }
        if (tvLoginText != null && section.getTitle() != null) {
            tvLoginText.setText(section.getTitle());
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
            return convertView;
        }
    }
}

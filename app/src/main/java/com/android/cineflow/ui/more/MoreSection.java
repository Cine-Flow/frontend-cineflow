package com.android.cineflow.ui.more;

import java.util.List;

public class MoreSection {
    public static final int TYPE_HEADER_LOGIN = 0;
    public static final int TYPE_DOWNLOAD_BANNER = 1;
    public static final int TYPE_ACTION_GRID = 2;
    public static final int TYPE_APPS_SECTION = 3;
    public static final int TYPE_HELP_ITEM = 4;

    private final int type;
    private final String title;
    private final List<MoreItem> items;

    public MoreSection(int type, String title, List<MoreItem> items) {
        this.type = type;
        this.title = title;
        this.items = items;
    }

    public int getType() { return type; }
    public String getTitle() { return title; }
    public List<MoreItem> getItems() { return items; }

    public static class MoreItem {
        private final String label;
        private final int iconRes;
        private final String subLabel;

        public MoreItem(String label, int iconRes, String subLabel) {
            this.label = label;
            this.iconRes = iconRes;
            this.subLabel = subLabel;
        }

        public String getLabel() { return label; }
        public int getIconRes() { return iconRes; }
        public String getSubLabel() { return subLabel; }
    }
}

package com.android.cineflow.data.common.uimodel;

import java.util.List;

public class HomeSection {

    public static final int TYPE_BANNER      = 0;
    public static final int TYPE_SECTION_ROW = 1;
    public static final int TYPE_SPORT_ROW   = 2;

    private final int viewType;
    private final String title;
    private final String actionLabel;
    private final List<ContentCard> items;

    public HomeSection(int viewType, String title, String actionLabel,
                       List<ContentCard> items) {
        this.viewType = viewType;
        this.title = title;
        this.actionLabel = actionLabel;
        this.items = items;
    }

    public int getViewType() { return viewType; }
    public String getTitle() { return title; }
    public String getActionLabel() { return actionLabel; }
    public List<ContentCard> getItems() { return items; }
}

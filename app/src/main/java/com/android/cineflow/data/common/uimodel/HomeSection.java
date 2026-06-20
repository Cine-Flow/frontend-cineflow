package com.android.cineflow.data.common.uimodel;

import java.util.List;

public class HomeSection {

    public static final int TYPE_BANNER      = 0;
    public static final int TYPE_SECTION_ROW = 1;
    public static final int TYPE_SPORT_ROW   = 2;

    private final int viewType;
    private final int titleResId;
    private final int actionLabelResId;
    private final List<ContentCard> items;

    public HomeSection(int viewType, int titleResId, int actionLabelResId,
                       List<ContentCard> items) {
        this.viewType = viewType;
        this.titleResId = titleResId;
        this.actionLabelResId = actionLabelResId;
        this.items = items;
    }

    public int getViewType() { return viewType; }
    public int getTitleResId() { return titleResId; }
    public int getActionLabelResId() { return actionLabelResId; }
    public List<ContentCard> getItems() { return items; }
}

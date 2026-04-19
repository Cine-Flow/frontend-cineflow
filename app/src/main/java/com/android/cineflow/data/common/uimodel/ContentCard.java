package com.android.cineflow.data.common.uimodel;

public class ContentCard {

    public static final int STYLE_PORTRAIT  = 0;
    public static final int STYLE_LANDSCAPE = 1;
    public static final int STYLE_BANNER    = 2;

    private final String id;
    private final String title;
    private final String thumbnailUrl;
    private final String badgeLabel;   // "NEW", "4K", "LIVE" — empty = hidden
    private final int cardStyle;

    public ContentCard(String id, String title, String thumbnailUrl,
                       String badgeLabel, int cardStyle) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.badgeLabel = badgeLabel;
        this.cardStyle = cardStyle;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getBadgeLabel() { return badgeLabel; }
    public int getCardStyle() { return cardStyle; }
}

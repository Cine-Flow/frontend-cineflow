package com.android.cineflow.data.model;

public class SportEvent {
    private String id;
    private String title;
    private String thumbnailUrl;
    private String league;
    private boolean isLive;
    private String broadcastTime; // e.g. "11:30 - Ngày 1/3"

    public SportEvent(String id, String title, String thumbnailUrl,
                      String league, boolean isLive, String broadcastTime) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.league = league;
        this.isLive = isLive;
        this.broadcastTime = broadcastTime;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getLeague() { return league; }
    public boolean isLive() { return isLive; }
    public String getBroadcastTime() { return broadcastTime; }
}

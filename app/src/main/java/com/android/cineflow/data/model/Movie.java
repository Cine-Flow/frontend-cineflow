package com.android.cineflow.data.model;

public class Movie {
    private String id;
    private String title;
    private String thumbnailUrl;
    private String genre;
    private int year;
    private boolean isNew;
    private boolean is4K;

    public Movie(String id, String title, String thumbnailUrl,
                 String genre, int year, boolean isNew, boolean is4K) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.genre = genre;
        this.year = year;
        this.isNew = isNew;
        this.is4K = is4K;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getGenre() { return genre; }
    public int getYear() { return year; }
    public boolean isNew() { return isNew; }
    public boolean is4K() { return is4K; }
}

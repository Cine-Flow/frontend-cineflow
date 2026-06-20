package com.android.cineflow.data.network.dto;

public class AdminCategoryDto {
    private Integer id;
    private String name;
    private String description;
    private Long filmCount;

    public AdminCategoryDto() {}

    public AdminCategoryDto(Integer id, String name, String description, long filmCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.filmCount = filmCount;
    }

    public Integer getId() { return id; }
    public String getName() { return name != null ? name : ""; }
    public String getDescription() { return description; }
    public long getFilmCount() { return filmCount != null ? filmCount : 0L; }
}

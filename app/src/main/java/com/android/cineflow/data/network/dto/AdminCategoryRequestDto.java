package com.android.cineflow.data.network.dto;

public class AdminCategoryRequestDto {
    private final String name;
    private final String description;

    public AdminCategoryRequestDto(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

package com.android.cineflow.data.network.dto;

public class UpdateProfileRequestDto {
    private String fullName;

    public UpdateProfileRequestDto(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() { return fullName; }
}

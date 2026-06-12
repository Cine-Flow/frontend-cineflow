package com.android.cineflow.data.network.dto;

public class UpdateProfileRequestDto {
    private String fullName;
    private String phoneNumber;

    public UpdateProfileRequestDto(String fullName, String phoneNumber) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
}

package com.android.cineflow.data.network.dto;

public class UpdateProfileRequestDto {
    private String fullName;
    private String email;
    private String phoneNumber;

    public UpdateProfileRequestDto(String fullName, String email, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
}

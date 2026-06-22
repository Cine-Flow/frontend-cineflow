package com.android.cineflow.data.network.dto;

public class AdminUserRequestDto {
    private final String username;
    private final String email;
    private final String password;
    private final String fullName;
    private final String phoneNumber;
    private final String avatarUrl;

    public AdminUserRequestDto(String username, String email, String password,
                               String fullName, String phoneNumber, String avatarUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
    }
}

package com.android.cineflow.data.network.dto;

import com.google.gson.annotations.SerializedName;

public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String id;
    private String username;
    private String email;
    private String role;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}

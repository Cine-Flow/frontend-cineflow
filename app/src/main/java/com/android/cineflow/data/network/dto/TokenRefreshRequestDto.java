package com.android.cineflow.data.network.dto;

public class TokenRefreshRequestDto {
    private final String refreshToken;

    public TokenRefreshRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}

package com.android.cineflow.data.network.dto;

public class ForgotPasswordRequestDto {
    private String email;

    public ForgotPasswordRequestDto(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
}

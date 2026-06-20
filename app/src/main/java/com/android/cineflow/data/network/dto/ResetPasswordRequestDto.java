package com.android.cineflow.data.network.dto;

public class ResetPasswordRequestDto {
    private String token;
    private String newPassword;
    private String confirmPassword;

    public ResetPasswordRequestDto(String token, String newPassword, String confirmPassword) {
        this.token = token;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getToken() { return token; }
    public String getNewPassword() { return newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
}

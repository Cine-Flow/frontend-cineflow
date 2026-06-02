package com.android.cineflow.data.network.dto;

public class ChangePasswordRequestDto {
    private String oldPassword;
    private String newPassword;

    public ChangePasswordRequestDto(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() { return oldPassword; }
    public String getNewPassword() { return newPassword; }
}

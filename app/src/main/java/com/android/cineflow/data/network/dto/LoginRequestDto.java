package com.android.cineflow.data.network.dto;

public class LoginRequestDto {
    private String identifier;
    private String password;

    public LoginRequestDto(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() { return identifier; }
    public String getPassword() { return password; }
}

package com.android.cineflow.data.network.dto;

public class RegisterRequestDto {
    private String username;
    private String email;
    private String password;

    public RegisterRequestDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

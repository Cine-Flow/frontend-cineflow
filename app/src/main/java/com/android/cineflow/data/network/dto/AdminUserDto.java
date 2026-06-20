package com.android.cineflow.data.network.dto;

public class AdminUserDto {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
    private String createdAt;
    private String subscriptionPlan;
    private String subscriptionEndDate;

    public String getId() { return id != null ? id : ""; }
    public String getUsername() { return username != null ? username : ""; }
    public String getEmail() { return email != null ? email : ""; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getRole() { return role != null ? role : "ROLE_USER"; }
    public String getCreatedAt() { return createdAt != null ? createdAt : ""; }
    public String getSubscriptionPlan() { return subscriptionPlan; }
    public String getSubscriptionEndDate() { return subscriptionEndDate; }
}

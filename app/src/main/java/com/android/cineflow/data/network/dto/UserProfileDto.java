package com.android.cineflow.data.network.dto;

public class UserProfileDto {
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private long favoriteCount;
    private long watchHistoryCount;
    private SubscriptionDto currentSubscription;

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public long getFavoriteCount() { return favoriteCount; }
    public long getWatchHistoryCount() { return watchHistoryCount; }
    public SubscriptionDto getCurrentSubscription() { return currentSubscription; }
}

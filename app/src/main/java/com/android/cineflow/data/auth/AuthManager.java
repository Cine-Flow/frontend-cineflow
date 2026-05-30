package com.android.cineflow.data.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {

    private static final String PREFS_NAME = "cineflow_auth";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private static volatile AuthManager instance;
    private SharedPreferences prefs;

    /**
     * Must be called once at app startup (Application.onCreate)
     * before any network call is made.
     */
    public static void init(Context context) {
        if (instance == null) {
            synchronized (AuthManager.class) {
                if (instance == null) {
                    instance = new AuthManager(context.getApplicationContext());
                }
            }
        }
    }

    /**
     * Returns the singleton. Safe to call from Interceptors etc.
     * Returns null-safe instance — if init() hasn't been called yet,
     * token reads will simply return null (unauthenticated).
     */
    public static AuthManager getInstance() {
        return instance;
    }

    private AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, String userId, String username, String email, String role) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public String getToken() { return prefs != null ? prefs.getString(KEY_TOKEN, null) : null; }
    public String getUserId() { return prefs != null ? prefs.getString(KEY_USER_ID, null) : null; }
    public String getUsername() { return prefs != null ? prefs.getString(KEY_USERNAME, null) : null; }
    public String getEmail() { return prefs != null ? prefs.getString(KEY_EMAIL, null) : null; }
    public String getRole() { return prefs != null ? prefs.getString(KEY_ROLE, null) : null; }

    public boolean isLoggedIn() { return getToken() != null; }

    public boolean isAdmin() {
        String role = getRole();
        return role != null && role.equals("ROLE_ADMIN");
    }
}

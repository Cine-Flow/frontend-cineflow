package com.android.cineflow.data.auth;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuthManager {

    public interface AuthListener {
        void onAuthStatusChanged(boolean isLoggedIn);
    }

    private static final String PREFS_NAME = "cineflow_auth";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private static volatile AuthManager instance;
    private SharedPreferences prefs;
    private final List<AuthListener> listeners = new CopyOnWriteArrayList<>();

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

    public void addListener(AuthListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(AuthListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(boolean isLoggedIn) {
        for (AuthListener listener : listeners) {
            listener.onAuthStatusChanged(isLoggedIn);
        }
    }

    public void saveSession(String token, String refreshToken, String userId, String username, String email, String role) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
        notifyListeners(true);
    }

    public void updateTokens(String accessToken, String refreshToken) {
        if (prefs != null) {
            prefs.edit()
                    .putString(KEY_TOKEN, accessToken)
                    .putString(KEY_REFRESH_TOKEN, refreshToken)
                    .apply();
        }
    }

    public void clearSession() {
        prefs.edit().clear().apply();
        notifyListeners(false);
    }

    public String getToken() { return prefs != null ? prefs.getString(KEY_TOKEN, null) : null; }
    public String getRefreshToken() { return prefs != null ? prefs.getString(KEY_REFRESH_TOKEN, null) : null; }
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


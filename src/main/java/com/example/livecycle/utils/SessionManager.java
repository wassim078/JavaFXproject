package com.example.livecycle.utils;

import java.util.prefs.Preferences;

public class SessionManager {
    private static final Preferences prefs = Preferences.userNodeForPackage(SessionManager.class);
    private static final String SESSION_USER_ID = "currentUserId";
    private static final String SESSION_EXPIRY = "sessionExpiry";

    public static void saveSession(int userId) {
        prefs.putInt(SESSION_USER_ID, userId);
        prefs.putLong(SESSION_EXPIRY, System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)); // 1 week expiry
    }

    public static int getCurrentUserId() {
        return prefs.getInt(SESSION_USER_ID, -1);
    }

    public static boolean isSessionValid() {
        return prefs.getLong(SESSION_EXPIRY, 0) > System.currentTimeMillis();
    }

    public static void clearSession() {
        prefs.remove(SESSION_USER_ID);
        prefs.remove(SESSION_EXPIRY);
    }
}
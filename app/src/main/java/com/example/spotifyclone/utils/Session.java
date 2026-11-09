package com.example.spotifyclone.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.spotifyclone.model.TokenResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Session {

    public static final String PREFS = "spotify_prefs";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";
    private static final String KEY_EXPIRES_AT = "expires_at";
    private static final String KEY_SCOPES = "granted_scopes";

    private static final long EXPIRY_MARGIN_MS = 60_000;

    private static volatile String cachedToken;

    private Session() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getAccessToken(Context context) {
        if (cachedToken == null && context != null) {
            cachedToken = prefs(context).getString(KEY_ACCESS, null);
        }
        return cachedToken;
    }

    public static String getRefreshToken(Context context) {
        return prefs(context).getString(KEY_REFRESH, null);
    }

    public static long getExpiresAt(Context context) {
        return prefs(context).getLong(KEY_EXPIRES_AT, 0);
    }

    /** Tempo que falta até o token precisar de ser renovado (negativo se já expirou). */
    public static long millisUntilExpired(Context context) {
        return getExpiresAt(context) - EXPIRY_MARGIN_MS - System.currentTimeMillis();
    }

    public static boolean isExpired(Context context) {
        return millisUntilExpired(context) < 0;
    }

    public static boolean hasScopes(Context context, String requiredScopes) {
        String granted = prefs(context).getString(KEY_SCOPES, "");
        Set<String> grantedSet = new HashSet<>(Arrays.asList(granted.split(" ")));
        String[] requiredArray = requiredScopes.split(" ");

        for (String scope : requiredArray) {
            if (!scope.isEmpty() && !grantedSet.contains(scope)) {
                return false;
            }
        }
        return true;
    }

    public static void save(Context context, TokenResponse token) {
        SharedPreferences.Editor editor = prefs(context).edit();
        editor.putString(KEY_ACCESS, token.getAccessToken());

        if (token.getRefreshToken() != null) {
            editor.putString(KEY_REFRESH, token.getRefreshToken());
        }
        if (token.getScope() != null) {
            editor.putString(KEY_SCOPES, token.getScope());
        }

        long expiresAt = System.currentTimeMillis() + (token.getExpiresIn() * 1000L);
        editor.putLong(KEY_EXPIRES_AT, expiresAt);
        editor.apply();

        cachedToken = token.getAccessToken();
    }

    public static void clear(Context context) {
        prefs(context).edit()
                .remove(KEY_ACCESS)
                .remove(KEY_REFRESH)
                .remove(KEY_EXPIRES_AT)
                .remove(KEY_SCOPES)
                .apply();
        cachedToken = null;
    }
}
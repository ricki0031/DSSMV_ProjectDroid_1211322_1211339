package com.example.spotifyclone.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.spotifyclone.model.TokenResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Guarda a sessão do Spotify (access token, refresh token, validade e scopes) nas SharedPreferences.
 * Qualquer Fragment/Activity pode pedir o token mais recente com Session.getAccessToken(context),
 * em vez de depender de um token antigo passado por Bundle (que expira ao fim de 1 hora).
 */
public final class Session {

    public static final String PREFS = "spotify_prefs";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";
    private static final String KEY_EXPIRES_AT = "expires_at";
    private static final String KEY_SCOPES = "granted_scopes";

    // Margem de segurança: consideramos o token expirado 60s antes da hora real
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

    public static boolean isExpired(Context context) {
        return System.currentTimeMillis() > getExpiresAt(context) - EXPIRY_MARGIN_MS;
    }

    /** Verifica se a sessão guardada já tem todos os scopes que a app precisa. */
    public static boolean hasScopes(Context context, String requiredScopes) {
        String granted = prefs(context).getString(KEY_SCOPES, "");
        Set<String> grantedSet = new HashSet<>(Arrays.asList(granted.split(" ")));
        for (String scope : requiredScopes.split(" ")) {
            if (!scope.isEmpty() && !grantedSet.contains(scope)) return false;
        }
        return true;
    }

    public static void save(Context context, TokenResponse token) {
        SharedPreferences.Editor editor = prefs(context).edit();
        editor.putString(KEY_ACCESS, token.getAccessToken());
        // O Spotify nem sempre devolve um refresh token novo no refresh: nesse caso mantemos o antigo
        if (token.getRefreshToken() != null) {
            editor.putString(KEY_REFRESH, token.getRefreshToken());
        }
        if (token.getScope() != null) {
            editor.putString(KEY_SCOPES, token.getScope());
        }
        editor.putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + token.getExpiresIn() * 1000L);
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

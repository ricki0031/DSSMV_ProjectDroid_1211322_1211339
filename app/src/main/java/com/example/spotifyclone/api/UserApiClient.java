package com.example.spotifyclone.api;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class UserApiClient {
    static OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://api.spotify.com/v1";

    /** GET /me: perfil do utilizador autenticado (usado para saber se uma playlist é nossa). */
    public static void getCurrentUserProfile(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }
}

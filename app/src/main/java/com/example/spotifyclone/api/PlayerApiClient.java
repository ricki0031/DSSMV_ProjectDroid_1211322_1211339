package com.example.spotifyclone.api;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;

public class PlayerApiClient {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://api.spotify.com/v1";

    // Nome alterado para estar de acordo com a documentação: Start/Resume Playback
    public static void startResumePlayback(String accessToken, String trackId, Callback callback) {
        String jsonBody = "{\"uris\": [\"spotify:track:" + trackId + "\"]}";

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/play")
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(body)
                .build();

        client.newCall(request).enqueue(callback);
    }


    // Futuramente, adicionar aqui:
    // public static void pausePlayback(...)
    // public static void skipToNext(...)

    // Pede as últimas 7 músicas tocadas (Endpoint do Player)
    public static void getRecentlyPlayed(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/recently-played?limit=50")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }
}

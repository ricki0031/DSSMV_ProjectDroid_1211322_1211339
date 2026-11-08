
package com.example.spotifyclone.api;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class TrackApiClient {

    private static final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://api.spotify.com/v1";



    // Endpoint: Get Track (GET /tracks/{id})
    public static void getTrack(String accessToken, String trackId, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/tracks/" + trackId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void getSavedTracks(
            String accessToken,
            int offset,
            Callback callback) {

        Request request = new Request.Builder()
                .url(BASE_URL + "/me/tracks?limit=50&offset=" + offset)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }
}

package com.example.spotifyclone.api;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ArtistApiClient {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://api.spotify.com/v1";

    // Endpoint: Get Artist (GET /artists/{id})
    public static void getArtist(String accessToken, String artistId, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/artists/" + artistId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Endpoint: Get Artist's Albums (GET /artists/{id}/albums)
    public static void getArtistAlbums(String accessToken, String artistId, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/artists/" + artistId + "/albums?include_groups=album,single&limit=50")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }
}
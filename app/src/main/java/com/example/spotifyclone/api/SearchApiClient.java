package com.example.spotifyclone.api;
import android.net.Uri;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SearchApiClient {
    static OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://api.spotify.com/v1";

    // Endpoint: Search for Item (GET /search)
    public static void search(String accessToken, String query, String types, int limit, Callback callback) {
        String encodedQuery = Uri.encode(query);
        String encodedTypes = Uri.encode(types, ",");

        Request request = new Request.Builder()
                .url(BASE_URL + "/search?q=" + encodedQuery + "&type=" + encodedTypes + "&limit=" + limit)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Versão simplificada que pesquisa por tudo (músicas, artistas, álbuns e playlists)
    public static void search(String accessToken, String query, Callback callback) {
        search(accessToken, query, "track,artist,album,playlist", 10, callback);
    }
}

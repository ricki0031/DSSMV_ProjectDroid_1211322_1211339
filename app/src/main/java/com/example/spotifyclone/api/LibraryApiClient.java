package com.example.spotifyclone.api;

import android.net.Uri;
import android.text.TextUtils;
import java.util.List;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LibraryApiClient {
    static OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://api.spotify.com/v1";

    // Endpoint (Secção Library): Save Items to Library (PUT /me/library?uris=...)
    public static void saveItemsToLibrary(String accessToken, List<String> uris, Callback callback) {
        String joinedUris = Uri.encode(TextUtils.join(",", uris), ",");

        Request request = new Request.Builder()
                .url(BASE_URL + "/me/library?uris=" + joinedUris)
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(RequestBody.create(new byte[0])) // PUT com corpo vazio
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Endpoint (Secção Library): Remove Items from Library (DELETE /me/library?uris=...)
    public static void removeItemsFromLibrary(String accessToken, List<String> uris, Callback callback) {
        String joinedUris = Uri.encode(TextUtils.join(",", uris), ",");

        Request request = new Request.Builder()
                .url(BASE_URL + "/me/library?uris=" + joinedUris)
                .addHeader("Authorization", "Bearer " + accessToken)
                .delete()
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Endpoint (Secção Library): Check User's Saved Items (GET /me/library/contains?uris=...)
    public static void checkUserSavedItems(String accessToken, List<String> uris, Callback callback) {
        String joinedUris = Uri.encode(TextUtils.join(",", uris), ",");

        Request request = new Request.Builder()
                .url(BASE_URL + "/me/library/contains?uris=" + joinedUris)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }
}
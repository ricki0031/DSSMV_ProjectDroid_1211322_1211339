package com.example.spotifyclone.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.List;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PlaylistApiClient {
    static OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://api.spotify.com/v1";

    // Endpoint: Get Playlist (GET /playlists/{playlist_id})
    public static void getPlaylist(String accessToken, String playlistId, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/playlists/" + playlistId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Endpoint: Get Playlist Items (GET /playlists/{playlist_id}/items)
    public static void getPlaylistItems(String accessToken, String playlistId, int offset, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/playlists/" + playlistId + "/items?limit=50&offset=" + offset)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Endpoint: Get Current User's Playlists (GET /me/playlists)
    public static void getCurrentUserPlaylists(String accessToken, int limit, int offset, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/playlists?limit=" + limit + "&offset=" + offset)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void getCurrentUserPlaylists(String accessToken, Callback callback) {
        getCurrentUserPlaylists(accessToken, 50, 0, callback);
    }

    // Endpoint: Create Playlist (POST /me/playlists)
    public static void createPlaylist(String accessToken, String name, String description, boolean isPublic, Callback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);

        if (description != null) {
            json.addProperty("description", description);
        } else {
            json.addProperty("description", "");
        }

        json.addProperty("public", isPublic);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/me/playlists")
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void createPlaylist(String accessToken, String name, String description, Callback callback) {
        createPlaylist(accessToken, name, description, false, callback);
    }

    // Endpoint: Change Playlist Details (PUT /playlists/{playlist_id})
    public static void changePlaylistDetails(String accessToken, String playlistId, String name, String description, Callback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        if (description != null) {
            json.addProperty("description", description);
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/playlists/" + playlistId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Endpoint: Add Items to Playlist (POST /playlists/{playlist_id}/items)
    public static void addItemsToPlaylist(String accessToken, String playlistId, List<String> uris, Callback callback) {
        JsonObject json = new JsonObject();
        JsonArray urisArray = new JsonArray();
        if (uris != null) {
            for (String uri : uris) {
                urisArray.add(uri);
            }
        }
        json.add("uris", urisArray);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/playlists/" + playlistId + "/items")
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Endpoint: Remove Playlist Items (DELETE /playlists/{playlist_id}/items)
    public static void removePlaylistItems(String accessToken, String playlistId, List<String> uris, String snapshotId, Callback callback) {
        JsonObject json = new JsonObject();
        JsonArray itemsArray = new JsonArray();
        if (uris != null) {
            for (String uri : uris) {
                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("uri", uri);
                itemsArray.add(itemObj);
            }
        }
        json.add("items", itemsArray);
        if (snapshotId != null && !snapshotId.isEmpty()) {
            json.addProperty("snapshot_id", snapshotId);
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/playlists/" + playlistId + "/items")
                .addHeader("Authorization", "Bearer " + accessToken)
                .delete(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Endpoint: Add Custom Playlist Cover Image (PUT /playlists/{playlist_id}/images)
    public static void addCustomPlaylistCoverImage(String accessToken, String playlistId, String base64Jpeg, Callback callback) {
        MediaType JPEG = MediaType.parse("image/jpeg");
        RequestBody body = RequestBody.create(base64Jpeg.getBytes(StandardCharsets.UTF_8), JPEG);

        Request request = new Request.Builder()
                .url(BASE_URL + "/playlists/" + playlistId + "/images")
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(body)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
package com.example.spotifyclone.api;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class AlbumApiClient {
    // Mantemos o client e a BASE_URL aqui para esta classe gerir os seus próprios pedidos
    private static final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://api.spotify.com/v1";

    public static void getAlbum(String accessToken, String albumId, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/albums/" + albumId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    /*
    // Pede os álbuns mais recentes (New Releases)
    public static void getNewReleases(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/browse/new-releases?limit=20") // limit=20 para trazer 20 álbuns
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }*/

    // Pede os álbuns guardados ("Gostos") da biblioteca do utilizador
    public static void getUserSavedAlbums(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/albums?limit=50") // Endpoint da tua biblioteca
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }
}
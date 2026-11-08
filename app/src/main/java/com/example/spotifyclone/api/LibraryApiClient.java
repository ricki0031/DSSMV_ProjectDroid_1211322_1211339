package com.example.spotifyclone.api;

import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Biblioteca do utilizador. Desde fevereiro de 2026, guardar/remover/verificar usa um endpoint único
 * (/me/library) que recebe URIs de qualquer tipo ("spotify:track:...", "spotify:album:...", ...)
 * como parâmetro de query "uris" separado por vírgulas (máx. 40).
 */
public class LibraryApiClient {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://api.spotify.com/v1";

    private static Request.Builder authed(String url, String accessToken) {
        return new Request.Builder().url(url).addHeader("Authorization", "Bearer " + accessToken);
    }

    private static String urisQuery(List<String> uris) {
        return "uris=" + Uri.encode(TextUtils.join(",", uris), ",");
    }

    /** GET /me/tracks: as Liked Songs. */
    public static void getSavedTracks(String accessToken, int offset, Callback callback) {
        String url = BASE_URL + "/me/tracks?limit=50&offset=" + offset;
        client.newCall(authed(url, accessToken).get().build()).enqueue(callback);
    }

    /** PUT /me/library: guarda (ex: dar "like" numa faixa ou guardar um álbum). */
    public static void saveToLibrary(String accessToken, List<String> uris, Callback callback) {
        String url = BASE_URL + "/me/library?" + urisQuery(uris);
        client.newCall(authed(url, accessToken).put(RequestBody.create(new byte[0])).build()).enqueue(callback);
    }

    /** DELETE /me/library: remove da biblioteca. */
    public static void removeFromLibrary(String accessToken, List<String> uris, Callback callback) {
        String url = BASE_URL + "/me/library?" + urisQuery(uris);
        client.newCall(authed(url, accessToken).delete().build()).enqueue(callback);
    }

    /** GET /me/library/contains: devolve um array de booleanos, ex: [true,false]. */
    public static void checkLibraryContains(String accessToken, List<String> uris, Callback callback) {
        String url = BASE_URL + "/me/library/contains?" + urisQuery(uris);
        client.newCall(authed(url, accessToken).get().build()).enqueue(callback);
    }
}

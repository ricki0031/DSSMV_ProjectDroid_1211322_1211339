package com.example.spotifyclone.api;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;

public class PlayerApiClient {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://api.spotify.com/v1";

    public static void getPlaybackState(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }
    public static void transferPlayback(String accessToken, String deviceId, Callback callback) {
        String jsonBody = "{\"device_ids\": [\"" + deviceId + "\"], \"play\": true}";

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player")
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(body)
                .build();

        client.newCall(request).enqueue(callback);
    }
    public static void getAvailableDevices(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/devices")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }
    public static void getCurrentlyPlayingTrack(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/currently-playing")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Nome alterado para estar de acordo com a documentação: Start/Resume Playback
    public static void startResumePlayback(String accessToken, String trackId, String deviceId, Callback callback) {
        RequestBody body;
        if (trackId != null && !trackId.isEmpty()) {
            String jsonBody = "{\"uris\": [\"spotify:track:" + trackId + "\"]}";
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            body = RequestBody.create(jsonBody, JSON);
        } else {
            body = RequestBody.create(null, new byte[0]);
        }

        // Anexar o ID do dispositivo à rota original
        String url = BASE_URL + "/me/player/play";
        if (deviceId != null && !deviceId.isEmpty()) {
            url += "?device_id=" + deviceId;
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void pausePlayback(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/pause")
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(RequestBody.create(new byte[0])) // PUT request with empty body
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void skipToNext(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/next")
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create(new byte[0])) // POST request with empty body
                .build();

        client.newCall(request).enqueue(callback);
    }
    public static void skipToPrevious(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/previous")
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create(new byte[0])) // POST request with empty body
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void seekToPosition(String accessToken, int positionMs, Callback callback) {
        int safePosition = Math.max(0, positionMs); //garantir que a posição não é negativa

        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/seek?position_ms=" + safePosition)
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(RequestBody.create(null, new byte[0])) // PUT request with empty body
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void setRepeatMode(String accessToken, String state, Callback callback) {
        String safeState = "off"; // Default Value para evitar erros 400 Bad Request
        if (state.equals("track") || state.equals("context") || state.equals("off")) {
            safeState = state;
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/repeat?state=" + safeState)
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(RequestBody.create(null, new byte[0])) // PUT request with empty body
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void setPlaybackVolume(String accessToken, int volumePercent, Callback callback) {
        int safeVolume = Math.max(0, Math.min(100, volumePercent));

        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/volume?volume_percent=" + safeVolume)
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(RequestBody.create(null, new byte[0])) // PUT request with empty body
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void togglePlaybackShuffle(String accessToken, boolean state, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/shuffle?state=" + state)
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(RequestBody.create(null, new byte[0])) // PUT request with empty body
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Pede as últimas 7 músicas tocadas (Endpoint do Player)
    public static void getRecentlyPlayedTracks(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/recently-played?limit=50")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void getTheUserQueue(String accessToken, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/queue")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void addItemToPlaybackQueue(String accessToken, String trackUri, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/me/player/queue?uri=" + trackUri)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create(new byte[0])) // POST request with empty body
                .build();

        client.newCall(request).enqueue(callback);
    }
}

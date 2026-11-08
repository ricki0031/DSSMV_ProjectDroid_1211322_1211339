package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
public class PlaybackStateResponse {
    @SerializedName("device")
    public Device device;
    @SerializedName("repeat_state")
    public String repeatState;
    @SerializedName("shuffle_state")
    public boolean shuffleState;

    @SerializedName("is_playing")
    public boolean isPlaying;

    @SerializedName("progress_ms")
    public long progressMs;

    @SerializedName("item")
    public Track item;

    // Nulo quando a música foi iniciada sem contexto (lista de "uris", ex.: Liked Songs)
    @SerializedName("context")
    public PlaybackContext context;

    public static class PlaybackContext {
        @SerializedName("uri")
        public String uri;
    }
}

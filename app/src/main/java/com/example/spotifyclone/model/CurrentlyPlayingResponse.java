package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
public class CurrentlyPlayingResponse {
    @SerializedName("is_playing")
    public boolean isPlaying;

    @SerializedName("progress_ms")
    public long progressMs; //barra de progresso
    @SerializedName("item")
    public Track item;
}

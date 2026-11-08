package com.example.spotifyclone.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QueueResponse {
    @SerializedName("currently_playing")
    public Track currentlyPlaying;

    @SerializedName("queue")
    public List<Track> queue;
}

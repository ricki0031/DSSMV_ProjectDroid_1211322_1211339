package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Track {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("track_number")
    public int trackNumber;

    // As duas propriedades que estavam a faltar:
    @SerializedName("duration_ms")
    public long durationMs;

    @SerializedName("artists")
    public List<Artist> artists;
}
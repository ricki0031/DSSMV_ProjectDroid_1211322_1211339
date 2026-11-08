package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;

/** Uma entrada de GET /me/tracks (Liked Songs). */
public class SavedTrack {
    @SerializedName("added_at")
    public String addedAt;

    @SerializedName("track")
    public Track track;
}

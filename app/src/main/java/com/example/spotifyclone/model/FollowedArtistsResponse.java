package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;

/** GET /me/following?type=artist devolve { "artists": { items, next, total, cursors } }. */
public class FollowedArtistsResponse {
    @SerializedName("artists")
    public Page<Artist> artists;
}

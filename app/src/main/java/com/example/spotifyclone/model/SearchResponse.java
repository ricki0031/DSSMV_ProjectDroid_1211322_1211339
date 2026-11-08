package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;

/** GET /search: cada tipo pedido vem numa página própria. */
public class SearchResponse {
    @SerializedName("tracks")
    public Page<Track> tracks;

    @SerializedName("artists")
    public Page<Artist> artists;

    @SerializedName("albums")
    public Page<Album> albums;

    @SerializedName("playlists")
    public Page<Playlist> playlists;
}

package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Objeto de paginação genérico do Spotify ({ href, items, limit, next, offset, total }).
 * Serve para /me/playlists, /me/tracks, /playlists/{id}/items, /artists/{id}/albums e /search.
 * Para ler um Page no topo do JSON usa: new TypeToken<Page<Playlist>>(){}.getType()
 */
public class Page<T> {
    @SerializedName("items")
    public List<T> items;

    @SerializedName("total")
    public int total;

    @SerializedName("next")
    public String next;

    @SerializedName("offset")
    public int offset;
}

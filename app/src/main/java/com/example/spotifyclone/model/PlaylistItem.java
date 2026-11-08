package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;

/** Uma entrada de GET /playlists/{id}/items. A faixa vem dentro de "item" (antes era "track"). */
public class PlaylistItem {
    @SerializedName("added_at")
    public String addedAt;

    @SerializedName("is_local")
    public boolean isLocal;

    @SerializedName(value = "item", alternate = {"track"})
    public Track item;
}

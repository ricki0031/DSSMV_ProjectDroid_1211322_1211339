package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Playlist do Spotify. Ao contrário do Album, é editável (nome, descrição, capa, faixas).
 * Nota (alterações de fevereiro de 2026): o antigo campo "tracks" passou a chamar-se "items",
 * e só vem preenchido para playlists que o utilizador criou ou onde colabora.
 */
public class Playlist {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("images")
    public List<ImageObject> images;

    @SerializedName("owner")
    public UserProfile owner;

    @SerializedName("public")
    public Boolean isPublic;

    @SerializedName("collaborative")
    public boolean collaborative;

    @SerializedName("snapshot_id")
    public String snapshotId;

    @SerializedName("uri")
    public String uri;

    // "alternate" permite ler respostas antigas que ainda usem "tracks"
    @SerializedName(value = "items", alternate = {"tracks"})
    public Page<PlaylistItem> items;

    public String firstImageUrl() {
        return (images != null && !images.isEmpty()) ? images.get(0).url : null;
    }
}

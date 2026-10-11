package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SavedAlbumsResponse {
    @SerializedName("items")
    public List<SavedAlbumItem> items;

    // O Spotify devolve a data em que guardaste e o álbum lá dentro
    public static class SavedAlbumItem {
        @SerializedName("album")
        public Album album;
    }
}
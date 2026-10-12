package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RecentlyPlayedResponse {
    @SerializedName("items")
    public List<PlayHistoryObject> items;

    public static class PlayHistoryObject {
        @SerializedName("track")
        public TrackItem track;
    }

    public static class TrackItem {
        public String id;
        public String name;
        @SerializedName("album")
        public Album album; // Reutilizamos a tua classe Album para ir buscar a capa!
    }
}
package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Album {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("album_type")
    public String albumType;

    @SerializedName("total_tracks")
    public int totalTracks;

    // Usa a nova classe externa
    @SerializedName("images")
    public List<ImageObject> images;

    // Usa a nova classe externa
    @SerializedName("artists")
    public List<Artist> artists;

    @SerializedName("tracks")
    public TracksPage tracks;

    // Apenas esta classe de paginação permanece, pois agrupa a lista de Tracks (exclusivo deste endpoint)
    public static class TracksPage {
        @SerializedName("items")
        public List<Track> items;
    }
}
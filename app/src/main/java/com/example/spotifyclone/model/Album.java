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

    @SerializedName("release_date")
    public String releaseDate;

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

    // Devolve o URL da primeira imagem da capa do álbum
    public String firstImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).url;
        }
        return null;
    }

    // Junta o nome dos artistas do álbum
    public String artistNames() {
        if (artists == null || artists.isEmpty()) {
            return "";
        }
        String names = "";
        for (int i = 0; i < artists.size(); i++) {
            if (i > 0) {
                names += ", ";
            }
            names += artists.get(i).name;
        }
        return names;
    }
}
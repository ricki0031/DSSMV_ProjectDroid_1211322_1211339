package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Track {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("uri")
    public String uri;

    @SerializedName("track_number")
    public int trackNumber;

    // As duas propriedades que estavam a faltar:
    @SerializedName("duration_ms")
    public long durationMs;

    @SerializedName("artists")
    public List<Artist> artists;

    @SerializedName("album")
    public Album album;

    // Devolve o URI da música (ex: "spotify:track:6y4y...")
    public String uriOrBuild() {
        if (uri != null && !uri.isEmpty()) {
            return uri;
        }
        if (id != null) {
            return "spotify:track:" + id;
        }
        return "";
    }

    // Junta o nome dos artistas separados por vírgula (ex: "Pitbull, Sensato")
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

    // Devolve o URL da imagem da capa do álbum a que a música pertence
    public String coverUrl() {
        if (album != null && album.images != null && !album.images.isEmpty()) {
            return album.images.get(0).url;
        }
        return null;
    }
}
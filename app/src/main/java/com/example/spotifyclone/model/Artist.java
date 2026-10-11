package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Artist {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    // Campos que só aparecem quando procuras pelo artista diretamente
    @SerializedName("genres")
    public List<String> genres;

    @SerializedName("images")
    public List<ImageObject> images;

    @SerializedName("popularity")
    public Integer popularity;
}
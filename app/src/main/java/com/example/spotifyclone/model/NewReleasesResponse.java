package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewReleasesResponse {
    @SerializedName("albums")
    public AlbumsPage albums;

    public static class AlbumsPage {
        @SerializedName("items")
        public List<Album> items;
    }
}
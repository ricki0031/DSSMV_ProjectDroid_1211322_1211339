package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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

    @SerializedName(value = "items", alternate = {"tracks"})
    public Page<PlaylistItem> items;

    public String firstImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).url;
        } else {
            return null;
        }
    }
}
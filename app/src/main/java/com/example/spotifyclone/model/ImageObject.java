package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;

public class ImageObject {
    @SerializedName("url")
    public String url;

    @SerializedName("height")
    public Integer height;

    @SerializedName("width")
    public Integer width;
}
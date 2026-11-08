package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
public class Device {
    @SerializedName("id")
    public String id;

    @SerializedName("is_active")
    public boolean isActive;

    @SerializedName("is_private_session")
    public boolean isPrivateSession;

    @SerializedName("name")
    public String name;

    @SerializedName("type")
    public String type; // "Computer", "Smartphone", "speaker"

    @SerializedName("volume_percent")
    public int volumePercent;


}

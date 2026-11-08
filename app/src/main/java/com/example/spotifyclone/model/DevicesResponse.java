package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DevicesResponse {
    @SerializedName("devices")
    public List<Device> devices;
}
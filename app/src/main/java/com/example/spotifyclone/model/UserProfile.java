package com.example.spotifyclone.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Perfil do utilizador (GET /me) ou dono de uma playlist. */
public class UserProfile {
    @SerializedName("id")
    public String id;

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("images")
    public List<ImageObject> images;
}

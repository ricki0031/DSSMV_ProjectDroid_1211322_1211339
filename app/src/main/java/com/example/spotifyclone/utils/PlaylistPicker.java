package com.example.spotifyclone.utils;

import android.app.Activity;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.spotifyclone.api.PlaylistApiClient;
import com.example.spotifyclone.api.UserApiClient;
import com.example.spotifyclone.model.Page;
import com.example.spotifyclone.model.Playlist;
import com.example.spotifyclone.model.UserProfile;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlaylistPicker {

    private PlaylistPicker() {}

    public static void show(Activity activity, String trackUri, String trackName) {
        String token = Session.getAccessToken(activity);

        UserApiClient.getCurrentUserProfile(token, new ApiCallback() {
            @Override
            protected void onSuccess(String profileJson) {
                UserProfile me = GSON.fromJson(profileJson, UserProfile.class);

                PlaylistApiClient.getCurrentUserPlaylists(token, 50, 0, new ApiCallback() {
                    @Override
                    protected void onSuccess(String body) {
                        if (activity.isFinishing()) {
                            return;
                        }

                        Page<Playlist> page = GSON.fromJson(body, new TypeToken<Page<Playlist>>() {}.getType());
                        List<Playlist> editable = new ArrayList<>();

                        if (page != null && page.items != null) {
                            for (Playlist p : page.items) {
                                if (p != null) {
                                    boolean isMine = false;
                                    if (p.owner != null && me != null && me.id != null && me.id.equals(p.owner.id)) {
                                        isMine = true;
                                    }

                                    if (isMine || p.collaborative) {
                                        editable.add(p);
                                    }
                                }
                            }
                        }

                        showDialog(activity, token, editable, trackUri, trackName);
                    }
                });
            }
        });
    }

    private static void showDialog(Activity activity, String token, List<Playlist> playlists, String trackUri, String trackName) {
        if (playlists.isEmpty()) {
            Toast.makeText(activity, "Ainda não tens playlists. Cria uma na Library (+).", Toast.LENGTH_LONG).show();
            return;
        }

        String[] names = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) {
            names[i] = playlists.get(i).name;
        }

        List<String> urisToAdd = Collections.singletonList(trackUri);

        new AlertDialog.Builder(activity)
                .setTitle("Adicionar \"" + trackName + "\" a…")
                .setItems(names, (dialog, which) -> {
                    Playlist chosen = playlists.get(which);
                    PlaylistApiClient.addItemsToPlaylist(token, chosen.id, urisToAdd, new ApiCallback() {
                        @Override
                        protected void onSuccess(String body) {
                            Toast.makeText(activity, "Adicionado a " + chosen.name, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        protected void onError(int code, String body) {
                            super.onError(code, body);
                            Toast.makeText(activity, "Não foi possível adicionar (" + code + ")", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
package com.example.spotifyclone.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.example.spotifyclone.R;

import com.bumptech.glide.Glide;
import com.example.spotifyclone.adapter.TrackAdapter;
import com.example.spotifyclone.api.AlbumApiClient;
import com.example.spotifyclone.api.PlayerApiClient;
import com.example.spotifyclone.model.Album;
import com.example.spotifyclone.MainActivity; // IMPORTA A MAINACTIVITY PARA ACEDER AO ID!


public class AlbumFragment extends Fragment {

    private ImageView imageAlbumCover;
    private TextView textAlbumName;
    private TextView textAlbumArtist;
    private RecyclerView recyclerViewTracks;

    private String accessToken;
    private String albumId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        if (getArguments() != null) {
            accessToken = getArguments().getString("ACCESS_TOKEN");
            albumId = getArguments().getString("ALBUM_ID");
        }

        imageAlbumCover = view.findViewById(R.id.imageAlbumCover);
        textAlbumName = view.findViewById(R.id.textAlbumName);
        textAlbumArtist = view.findViewById(R.id.textAlbumArtist);

        recyclerViewTracks = view.findViewById(R.id.recyclerViewTracks);
        recyclerViewTracks.setLayoutManager(new LinearLayoutManager(getContext()));

        if (accessToken != null && albumId != null) {
            loadAlbum();
        }

        return view;
    }

    private void loadAlbum() {
        AlbumApiClient.getAlbum(accessToken, albumId, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    Album album = gson.fromJson(json, Album.class);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            textAlbumName.setText(album.name);
                            if (album.artists != null && !album.artists.isEmpty()) {
                                textAlbumArtist.setText(album.artists.get(0).name);
                            }

                            if (album.images != null && !album.images.isEmpty()) {
                                Glide.with(AlbumFragment.this)
                                        .load(album.images.get(0).url)
                                        .into(imageAlbumCover);
                            }

                            if (album.tracks != null && album.tracks.items != null) {
                                TrackAdapter adapter = new TrackAdapter(album.tracks.items, track -> {

                                    // 1. Extrair o ID do telemóvel descoberto pela MainActivity
                                    String targetDevice = "";
                                    if (getActivity() instanceof MainActivity) {
                                        targetDevice = ((MainActivity) getActivity()).globalDeviceId;
                                    }

                                    // 2. Tocar o álbum a partir desta música, para a fila e o shuffle terem as restantes faixas
                                    String albumUri = "spotify:album:" + albumId;
                                    PlayerApiClient.startContextPlaybackAtTrack(accessToken, albumUri, track.uriOrBuild(), targetDevice, new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                            System.out.println("Erro de Rede ao Tocar");
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                            if (response.isSuccessful()) {
                                                System.out.println("SUCESSO! A tocar: " + track.name);
                                            } else {
                                                System.out.println("ERRO NA API (" + response.code() + "): " + response.body().string());
                                            }

                                            // 3. Avisar a MainActivity para voltar a sincronizar o Mini-Player,
                                            // dando 1 segundo de intervalo para os servidores do Spotify atualizarem.
                                            if (getActivity() instanceof MainActivity) {
                                                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                                                ((MainActivity) getActivity()).syncMiniPlayer(accessToken);
                                            }
                                        }
                                    });
                                });
                                recyclerViewTracks.setAdapter(adapter);
                            }
                        });
                    }
                }
            }
        });
    }
}
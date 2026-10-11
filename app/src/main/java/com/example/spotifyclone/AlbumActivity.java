package com.example.spotifyclone;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import com.bumptech.glide.Glide;
import com.example.spotifyclone.adapter.TrackAdapter;
import com.example.spotifyclone.api.AlbumApiClient;
import com.example.spotifyclone.api.PlayerApiClient;
import com.example.spotifyclone.model.Album;
import com.example.spotifyclone.model.Track;

public class AlbumActivity extends AppCompatActivity {

    private ImageView imageAlbumCover;
    private TextView textAlbumName;
    private TextView textAlbumArtist;
    private RecyclerView recyclerViewTracks;

    private String accessToken;
    private String albumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // 1. Receber os dados passados pelo Intent da MainActivity
        accessToken = getIntent().getStringExtra("ACCESS_TOKEN");
        albumId = getIntent().getStringExtra("ALBUM_ID");

        // 2. Vincular as views
        imageAlbumCover = findViewById(R.id.imageAlbumCover);
        textAlbumName = findViewById(R.id.textAlbumName);
        textAlbumArtist = findViewById(R.id.textAlbumArtist);
        recyclerViewTracks = findViewById(R.id.recyclerViewTracks);
        recyclerViewTracks.setLayoutManager(new LinearLayoutManager(this));

        // 3. Carregar o álbum dinamicamente!
        if (accessToken != null && albumId != null) {
            loadAlbum();
        }
    }

    private void loadAlbum() {
        AlbumApiClient.getAlbum(accessToken, albumId, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("ERRO: Falha na rede.");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    Album album = gson.fromJson(json, Album.class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textAlbumName.setText(album.name);
                            if (album.artists != null && !album.artists.isEmpty()) {
                                textAlbumArtist.setText(album.artists.get(0).name);
                            }

                            if (album.images != null && !album.images.isEmpty()) {
                                Glide.with(AlbumActivity.this)
                                        .load(album.images.get(0).url)
                                        .into(imageAlbumCover);
                            }

                            if (album.tracks != null && album.tracks.items != null) {
                                TrackAdapter adapter = new TrackAdapter(album.tracks.items, new TrackAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Track track) {
                                        PlayerApiClient.startResumePlayback(accessToken, track.id, new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) { }

                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    System.out.println("SUCESSO! A tocar: " + track.name);
                                                }
                                            }
                                        });
                                    }
                                });
                                recyclerViewTracks.setAdapter(adapter);
                            }
                        }
                    });
                }
            }
        });
    }
}

package com.example.spotifyclone.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.spotifyclone.MainActivity;
import com.example.spotifyclone.R;
import com.example.spotifyclone.adapter.MediaCardAdapter;
import com.example.spotifyclone.api.ArtistApiClient;
import com.example.spotifyclone.api.PlayerApiClient;
import com.example.spotifyclone.model.Album;
import com.example.spotifyclone.model.Artist;
import com.example.spotifyclone.model.MediaItem;
import com.example.spotifyclone.model.Page;
import com.example.spotifyclone.utils.ApiCallback;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class ArtistFragment extends BaseFragment {

    public static final String ARG_ARTIST_ID = "ARTIST_ID";

    private ImageView imageArtist;
    private TextView textName, textGenres;
    private MediaCardAdapter albumsAdapter;
    private String artistId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist, container, false);

        if (getArguments() != null) {
            artistId = getArguments().getString(ARG_ARTIST_ID);
        }

        imageArtist = view.findViewById(R.id.imageArtist);
        textName = view.findViewById(R.id.textArtistName);
        textGenres = view.findViewById(R.id.textArtistGenres);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewArtistAlbums);
        albumsAdapter = new MediaCardAdapter(null, MediaCardAdapter.STYLE_LIST, new MediaCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MediaItem item) {
                open(item);
            }
        });
        recyclerView.setAdapter(albumsAdapter);

        View btnPlay = view.findViewById(R.id.btnArtistPlay);
        btnPlay.setOnClickListener(v -> {
            String contextUri = "spotify:artist:" + artistId;
            PlayerApiClient.startContextPlayback(token(), contextUri, 0, deviceId(), new ApiCallback() {
                @Override
                protected void onSuccess(String body) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        MainActivity activity = main();
                        if (activity != null) {
                            activity.syncMiniPlayer(token());
                        }
                    }, 1000);
                }

                @Override
                protected void onError(int code, String body) {
                    super.onError(code, body);
                    if (alive()) {
                        Toast.makeText(requireContext(), "Não foi possível tocar (" + code + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        if (artistId != null) {
            loadArtist();
            loadAlbums();
        }

        return view;
    }

    private void loadArtist() {
        ArtistApiClient.getArtist(token(), artistId, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                if (!alive()) {
                    return;
                }

                Artist artist = GSON.fromJson(body, Artist.class);
                textName.setText(artist.name);

                if (artist.genres != null && !artist.genres.isEmpty()) {
                    textGenres.setText(TextUtils.join(", ", artist.genres));
                }

                if (artist.images != null && !artist.images.isEmpty()) {
                    Glide.with(ArtistFragment.this)
                            .load(artist.images.get(0).url)
                            .transform(new CircleCrop())
                            .into(imageArtist);
                }
            }
        });
    }

    private void loadAlbums() {
        ArtistApiClient.getArtistAlbums(token(), artistId, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                if (!alive()) {
                    return;
                }

                Page<Album> page = GSON.fromJson(body, new TypeToken<Page<Album>>() {}.getType());
                List<MediaItem> items = new ArrayList<>();

                if (page != null && page.items != null) {
                    for (Album album : page.items) {
                        MediaItem item = MediaItem.fromAlbum(album);

                        String year = "";
                        if (album.releaseDate != null && album.releaseDate.length() >= 4) {
                            year = album.releaseDate.substring(0, 4);
                        }

                        String type;
                        if ("single".equals(album.albumType)) {
                            type = "Single";
                        } else {
                            type = "Álbum";
                        }

                        if (year.isEmpty()) {
                            item.subtitle = type;
                        } else {
                            item.subtitle = year + " • " + type;
                        }

                        items.add(item);
                    }
                }

                albumsAdapter.submit(items);
            }
        });
    }
}
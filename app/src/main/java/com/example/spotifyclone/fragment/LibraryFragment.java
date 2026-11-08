
package com.example.spotifyclone.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifyclone.R;
import com.example.spotifyclone.adapter.AlbumAdapter;
import com.example.spotifyclone.adapter.MediaCardAdapter;
import com.example.spotifyclone.api.AlbumApiClient;
import com.example.spotifyclone.api.PlaylistApiClient;
import com.example.spotifyclone.model.Album;
import com.example.spotifyclone.model.MediaItem;
import com.example.spotifyclone.model.Page;
import com.example.spotifyclone.model.Playlist;
import com.example.spotifyclone.model.SavedAlbumsResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LibraryFragment extends Fragment {

    private RecyclerView recyclerViewGrid;
    private RecyclerView recyclerViewPlaylists;
    private MediaCardAdapter playlistAdapter;
    private String accessToken;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        recyclerViewGrid = view.findViewById(R.id.recyclerViewLibrary);
        recyclerViewPlaylists = view.findViewById(R.id.recyclerViewPlaylists);

        recyclerViewGrid.setLayoutManager(
                new GridLayoutManager(requireContext(), 2)
        );

        recyclerViewPlaylists.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        playlistAdapter = new MediaCardAdapter(
                new ArrayList<>(),
                MediaCardAdapter.STYLE_LIST,
                item -> {
                    if (MediaItem.TYPE_PLAYLIST.equals(item.type)) {
                        openPlaylist(item.id);
                    }
                }
        );

        recyclerViewPlaylists.setAdapter(playlistAdapter);

        if (getArguments() != null) {
            accessToken = getArguments().getString("ACCESS_TOKEN");
        }

        if (accessToken != null && !accessToken.isEmpty()) {
            fetchSavedAlbums(accessToken);
            fetchPlaylists(accessToken);
        }
        View buttonLikedSongs = view.findViewById(R.id.buttonLikedSongs);

        if (buttonLikedSongs != null) {
            buttonLikedSongs.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("ACCESS_TOKEN", accessToken);
                args.putBoolean(PlaylistFragment.ARG_LIKED_SONGS, true);

                PlaylistFragment fragment = new PlaylistFragment();
                fragment.setArguments(args);

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        View buttonCreatePlaylist = view.findViewById(R.id.buttonCreatePlaylist);

        if (buttonCreatePlaylist != null) {
            buttonCreatePlaylist.setOnClickListener(
                    v -> showCreatePlaylistDialog()
            );
        }

        return view;
    }

    private void fetchPlaylists(String token) {
        PlaylistApiClient.getCurrentUserPlaylists(
                token,
                50,
                0,
                new Callback() {
                    @Override
                    public void onFailure(
                            @NonNull Call call,
                            @NonNull IOException e
                    ) {
                        showPlaylistMessage(
                                "Não foi possível carregar as playlists"
                        );
                    }

                    @Override
                    public void onResponse(
                            @NonNull Call call,
                            @NonNull Response response
                    ) throws IOException {
                        try (Response ignored = response) {
                            if (!response.isSuccessful() || response.body() == null) {
                                showPlaylistMessage(
                                        "Erro ao carregar playlists: HTTP "
                                                + response.code()
                                );
                                return;
                            }

                            String json = response.body().string();

                            Type type = new TypeToken<Page<Playlist>>() {}.getType();

                            Page<Playlist> page = new Gson().fromJson(json, type);

                            List<MediaItem> items = new ArrayList<>();

                            if (page != null && page.items != null) {
                                for (Playlist playlist : page.items) {
                                    if (playlist != null) {
                                        items.add(
                                                MediaItem.fromPlaylist(playlist)
                                        );
                                    }
                                }
                            }

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (isAdded() && playlistAdapter != null) {
                                        playlistAdapter.submit(items);
                                    }
                                });
                            }
                        }
                    }
                }
        );
    }

    private void showCreatePlaylistDialog() {
        if (accessToken == null || accessToken.isEmpty()) {
            showPlaylistMessage("Sessão Spotify indisponível");
            return;
        }

        EditText input = new EditText(requireContext());
        input.setHint("Nome da playlist");
        input.setSingleLine(true);

        new AlertDialog.Builder(requireContext())
                .setTitle("Criar nova playlist")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Criar", (dialog, which) -> {
                    String name = input.getText().toString().trim();

                    if (name.isEmpty()) {
                        showPlaylistMessage(
                                "Introduza um nome para a playlist"
                        );
                        return;
                    }

                    PlaylistApiClient.createPlaylist(
                            accessToken,
                            name,
                            "Playlist criada no SpotifyClone",
                            false,
                            new Callback() {
                                @Override
                                public void onFailure(
                                        @NonNull Call call,
                                        @NonNull IOException e
                                ) {
                                    showPlaylistMessage(
                                            "Erro de ligação ao Spotify"
                                    );
                                }

                                @Override
                                public void onResponse(
                                        @NonNull Call call,
                                        @NonNull Response response
                                ) {
                                    try (Response ignored = response) {
                                        if (response.isSuccessful()) {
                                            showPlaylistMessage(
                                                    "Playlist criada com sucesso!"
                                            );

                                            fetchPlaylists(accessToken);
                                        } else {
                                            showPlaylistMessage(
                                                    "Erro ao criar playlist: HTTP "
                                                            + response.code()
                                            );
                                        }
                                    }
                                }
                            }
                    );
                })
                .show();
    }

    private void showPlaylistMessage(String message) {
        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            if (isAdded()) {
                Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void fetchSavedAlbums(String token) {
        AlbumApiClient.getUserSavedAlbums(
                token,
                new Callback() {
                    @Override
                    public void onFailure(
                            @NonNull Call call,
                            @NonNull IOException e
                    ) {
                        showPlaylistMessage(
                                "Não foi possível carregar os álbuns"
                        );
                    }

                    @Override
                    public void onResponse(
                            @NonNull Call call,
                            @NonNull Response response
                    ) throws IOException {
                        try (Response ignored = response) {
                            if (!response.isSuccessful() || response.body() == null) {
                                return;
                            }

                            SavedAlbumsResponse savedResponse =
                                    new Gson().fromJson(
                                            response.body().string(),
                                            SavedAlbumsResponse.class
                                    );

                            List<Album> albums = new ArrayList<>();

                            if (savedResponse != null
                                    && savedResponse.items != null) {
                                for (SavedAlbumsResponse.SavedAlbumItem item
                                        : savedResponse.items) {
                                    if (item != null && item.album != null) {
                                        albums.add(item.album);
                                    }
                                }
                            }

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (!isAdded() || recyclerViewGrid == null) {
                                        return;
                                    }

                                    AlbumAdapter adapter = new AlbumAdapter(
                                            albums,
                                            album -> openAlbum(album, token)
                                    );

                                    recyclerViewGrid.setAdapter(adapter);
                                });
                            }
                        }
                    }
                }
        );
    }

    private void openAlbum(Album album, String token) {
        if (!isAdded() || album == null) {
            return;
        }

        Bundle args = new Bundle();
        args.putString("ACCESS_TOKEN", token);
        args.putString("ALBUM_ID", album.id);

        AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(args);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void openPlaylist(String playlistId) {
        if (!isAdded() || playlistId == null) {
            return;
        }

        Bundle args = new Bundle();
        args.putString("ACCESS_TOKEN", accessToken);
        args.putString("PLAYLIST_ID", playlistId);

        PlaylistFragment fragment = new PlaylistFragment();
        fragment.setArguments(args);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}

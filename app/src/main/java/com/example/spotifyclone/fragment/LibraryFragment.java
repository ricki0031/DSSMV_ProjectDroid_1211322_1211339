
package com.example.spotifyclone.fragment;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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

    private TextView textPlaylistsTitle;
    private TextView textAlbumsTitle;

    private Button buttonFilterPlaylists;
    private Button buttonFilterAlbums;
    private ImageButton buttonLibraryViewMode;

    private MediaCardAdapter playlistAdapter;
    private AlbumAdapter albumAdapter;

    private final List<MediaItem> playlists = new ArrayList<>();
    private final List<Album> albums = new ArrayList<>();

    private String accessToken;

    private boolean showingPlaylists = true;
    private boolean gridMode = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_library,
                container,
                false
        );

        recyclerViewGrid = view.findViewById(R.id.recyclerViewLibrary);
        recyclerViewPlaylists = view.findViewById(R.id.recyclerViewPlaylists);

        textPlaylistsTitle = view.findViewById(R.id.textPlaylistsTitle);
        textAlbumsTitle = view.findViewById(R.id.textAlbumsTitle);

        buttonFilterPlaylists = view.findViewById(R.id.buttonFilterPlaylists);
        buttonFilterAlbums = view.findViewById(R.id.buttonFilterAlbums);
        buttonLibraryViewMode = view.findViewById(R.id.buttonLibraryViewMode);

        if (getArguments() != null) {
            accessToken = getArguments().getString("ACCESS_TOKEN");
        }

        buttonFilterPlaylists.setOnClickListener(v -> {
            showingPlaylists = true;
            gridMode = false;
            updateLibraryView();
        });

        buttonFilterAlbums.setOnClickListener(v -> {
            showingPlaylists = false;
            gridMode = true;
            updateLibraryView();
        });

        buttonLibraryViewMode.setOnClickListener(v -> {
            gridMode = !gridMode;
            updateLibraryView();
        });

        View buttonLikedSongs = view.findViewById(R.id.buttonLikedSongs);

        if (buttonLikedSongs != null) {
            buttonLikedSongs.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("ACCESS_TOKEN", accessToken);
                args.putBoolean(
                        PlaylistFragment.ARG_LIKED_SONGS,
                        true
                );

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

        updateLibraryView();

        if (accessToken != null && !accessToken.isEmpty()) {
            fetchSavedAlbums(accessToken);
            fetchPlaylists(accessToken);
        }

        return view;
    }

    private void updateLibraryView() {
        if (recyclerViewGrid == null || recyclerViewPlaylists == null) {
            return;
        }

        buttonFilterPlaylists.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor(
                                showingPlaylists ? "#1DB954" : "#333333"
                        )
                )
        );

        buttonFilterAlbums.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor(
                                showingPlaylists ? "#333333" : "#1DB954"
                        )
                )
        );

        if (showingPlaylists) {
            textPlaylistsTitle.setVisibility(View.VISIBLE);
            textAlbumsTitle.setVisibility(View.GONE);

            recyclerViewPlaylists.setVisibility(View.VISIBLE);
            recyclerViewGrid.setVisibility(View.GONE);

            if (gridMode) {
                recyclerViewPlaylists.setLayoutManager(
                        new GridLayoutManager(requireContext(), 2)
                );

                playlistAdapter = new MediaCardAdapter(
                        new ArrayList<>(playlists),
                        MediaCardAdapter.STYLE_CARD,
                        item -> {
                            if (MediaItem.TYPE_PLAYLIST.equals(item.type)) {
                                openPlaylist(item.id);
                            }
                        }
                );
            } else {
                recyclerViewPlaylists.setLayoutManager(
                        new LinearLayoutManager(requireContext())
                );

                playlistAdapter = new MediaCardAdapter(
                        new ArrayList<>(playlists),
                        MediaCardAdapter.STYLE_LIST,
                        item -> {
                            if (MediaItem.TYPE_PLAYLIST.equals(item.type)) {
                                openPlaylist(item.id);
                            }
                        }
                );
            }

            recyclerViewPlaylists.setAdapter(playlistAdapter);

        } else {
            textPlaylistsTitle.setVisibility(View.GONE);
            textAlbumsTitle.setVisibility(View.VISIBLE);

            recyclerViewPlaylists.setVisibility(View.GONE);
            recyclerViewGrid.setVisibility(View.VISIBLE);

            recyclerViewGrid.setLayoutManager(
                    gridMode
                            ? new GridLayoutManager(requireContext(), 2)
                            : new LinearLayoutManager(requireContext())
            );

            albumAdapter = new AlbumAdapter(
                    new ArrayList<>(albums),
                    album -> openAlbum(album, accessToken)
            );

            recyclerViewGrid.setAdapter(albumAdapter);
        }
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
                            if (!response.isSuccessful()
                                    || response.body() == null) {
                                showPlaylistMessage(
                                        "Erro ao carregar playlists: HTTP "
                                                + response.code()
                                );
                                return;
                            }

                            String json = response.body().string();

                            Type type =
                                    new TypeToken<Page<Playlist>>() {}.getType();

                            Page<Playlist> page =
                                    new Gson().fromJson(json, type);

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
                                    if (!isAdded()) {
                                        return;
                                    }

                                    playlists.clear();
                                    playlists.addAll(items);

                                    if (showingPlaylists) {
                                        updateLibraryView();
                                    }
                                });
                            }
                        }
                    }
                }
        );
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
                            if (!response.isSuccessful()
                                    || response.body() == null) {
                                showPlaylistMessage(
                                        "Erro ao carregar álbuns: HTTP "
                                                + response.code()
                                );
                                return;
                            }

                            SavedAlbumsResponse savedResponse =
                                    new Gson().fromJson(
                                            response.body().string(),
                                            SavedAlbumsResponse.class
                                    );

                            List<Album> loadedAlbums = new ArrayList<>();

                            if (savedResponse != null
                                    && savedResponse.items != null) {
                                for (SavedAlbumsResponse.SavedAlbumItem item
                                        : savedResponse.items) {
                                    if (item != null && item.album != null) {
                                        loadedAlbums.add(item.album);
                                    }
                                }
                            }

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (!isAdded()) {
                                        return;
                                    }

                                    albums.clear();
                                    albums.addAll(loadedAlbums);

                                    if (!showingPlaylists) {
                                        updateLibraryView();
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


package com.example.spotifyclone.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifyclone.R;
import com.example.spotifyclone.adapter.HorizontalCardAdapter;
import com.example.spotifyclone.adapter.MediaCardAdapter;
import com.example.spotifyclone.adapter.RecentGridAdapter;
import com.example.spotifyclone.api.AlbumApiClient;
import com.example.spotifyclone.api.PlayerApiClient;
import com.example.spotifyclone.api.PlaylistApiClient;
import com.example.spotifyclone.model.Album;
import com.example.spotifyclone.model.MediaItem;
import com.example.spotifyclone.model.Page;
import com.example.spotifyclone.model.Playlist;
import com.example.spotifyclone.model.RecentlyPlayedResponse;
import com.example.spotifyclone.model.SavedAlbumsResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewGrid;
    private RecyclerView recyclerViewRecommended;
    private RecyclerView recyclerViewJumpBackIn;
    private RecyclerView recyclerViewRecents;

    private String accessToken;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewGrid = view.findViewById(R.id.recyclerViewHomeGrid);
        recyclerViewRecommended = view.findViewById(R.id.recyclerViewRecommended);
        recyclerViewJumpBackIn = view.findViewById(R.id.recyclerViewJumpBackIn);
        recyclerViewRecents = view.findViewById(R.id.recyclerViewRecents);

        recyclerViewGrid.setLayoutManager(
                new GridLayoutManager(requireContext(), 2)
        );

        recyclerViewRecommended.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        recyclerViewJumpBackIn.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        recyclerViewRecents.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        // O cartão Liked Songs fica sempre disponível.
        showHomeGrid(new ArrayList<>());

        if (getArguments() != null) {
            accessToken = getArguments().getString("ACCESS_TOKEN");
        }

        if (accessToken != null && !accessToken.isEmpty()) {
            fetchSavedAlbums(accessToken);
            fetchPlaylists(accessToken);
            fetchRecentlyPlayed(accessToken);
        } else {
            showMessage("Sessão Spotify indisponível");
        }

        return view;
    }

    private void fetchSavedAlbums(String token) {
        AlbumApiClient.getUserSavedAlbums(token, new Callback() {
            @Override
            public void onFailure(
                    @NonNull Call call,
                    @NonNull IOException e
            ) {
                showMessage("Não foi possível carregar os álbuns");
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

                    SavedAlbumsResponse result = new Gson().fromJson(
                            response.body().string(),
                            SavedAlbumsResponse.class
                    );

                    List<Album> albums = new ArrayList<>();

                    if (result != null && result.items != null) {
                        for (SavedAlbumsResponse.SavedAlbumItem item
                                : result.items) {
                            if (item != null && item.album != null) {
                                albums.add(item.album);
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        showHomeGrid(albums);

                        recyclerViewJumpBackIn.setAdapter(
                                new HorizontalCardAdapter(
                                        albums,
                                        album -> openAlbum(album.id)
                                )
                        );
                    });
                }
            }
        });
    }

    private void showHomeGrid(List<Album> albums) {
        List<RecentGridAdapter.GridItem> items = new ArrayList<>();

        RecentGridAdapter.GridItem likedSongs =
                new RecentGridAdapter.GridItem();

        likedSongs.title = "Liked Songs";
        likedSongs.isLikedSongs = true;
        items.add(likedSongs);

        int count = 0;

        for (Album album : albums) {
            if (album == null || album.id == null) {
                continue;
            }

            RecentGridAdapter.GridItem item =
                    new RecentGridAdapter.GridItem();

            item.id = album.id;
            item.title = album.name;
            item.isLikedSongs = false;

            if (album.images != null && !album.images.isEmpty()) {
                item.imageUrl = album.images.get(0).url;
            }

            items.add(item);

            count++;
            if (count >= 7) {
                break;
            }
        }

        recyclerViewGrid.setAdapter(
                new RecentGridAdapter(items, item -> {
                    if (item.isLikedSongs) {
                        openLibrary();
                    } else if (item.id != null) {
                        openAlbum(item.id);
                    }
                })
        );
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
                        showMessage(
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
                                return;
                            }

                            Type type =
                                    new TypeToken<Page<Playlist>>() {}.getType();

                            Page<Playlist> page = new Gson().fromJson(
                                    response.body().string(),
                                    type
                            );

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

                            runOnUiThread(() -> {
                                recyclerViewRecommended.setAdapter(
                                        new MediaCardAdapter(
                                                items,
                                                MediaCardAdapter.STYLE_CARD,
                                                item -> {
                                                    if (MediaItem.TYPE_PLAYLIST
                                                            .equals(item.type)) {
                                                        openPlaylist(item.id);
                                                    }
                                                }
                                        )
                                );
                            });
                        }
                    }
                }
        );
    }

    private void fetchRecentlyPlayed(String token) {
        PlayerApiClient.getRecentlyPlayedTracks(
                token,
                new Callback() {
                    @Override
                    public void onFailure(
                            @NonNull Call call,
                            @NonNull IOException e
                    ) {
                        showMessage(
                                "Histórico de reprodução indisponível"
                        );
                    }

                    @Override
                    public void onResponse(
                            @NonNull Call call,
                            @NonNull Response response
                    ) throws IOException {
                        try (Response ignored = response) {
                            android.util.Log.e(
                                    "SPOTIFY_RECENTS",
                                    "Resposta da API: HTTP " + response.code()
                            );

                            // Diagnóstico temporário da secção Recents.
                            if (!response.isSuccessful()
                                    || response.body() == null) {

                                int statusCode = response.code();

                                runOnUiThread(() ->
                                        Toast.makeText(
                                                requireContext(),
                                                "Recents: erro HTTP " + statusCode,
                                                Toast.LENGTH_LONG
                                        ).show()
                                );

                                return;
                            }

                            RecentlyPlayedResponse result =
                                    new Gson().fromJson(
                                            response.body().string(),
                                            RecentlyPlayedResponse.class
                                    );
                            android.util.Log.e(
                                    "SPOTIFY_RECENTS",
                                    "Itens recebidos: " +
                                            (result != null && result.items != null
                                                    ? result.items.size()
                                                    : "null")
                            );
                            List<Album> albums = new ArrayList<>();
                            Set<String> seenIds = new HashSet<>();

                            if (result != null && result.items != null) {
                                for (RecentlyPlayedResponse.PlayHistoryObject history
                                        : result.items) {

                                    if (history == null
                                            || history.track == null
                                            || history.track.album == null) {
                                        continue;
                                    }

                                    Album album = history.track.album;

                                    if (album.id != null
                                            && seenIds.add(album.id)) {
                                        albums.add(album);
                                    }
                                }
                            }

                            runOnUiThread(() -> {
                                recyclerViewRecents.setAdapter(
                                        new HorizontalCardAdapter(
                                                albums,
                                                album -> openAlbum(album.id)
                                        )
                                );
                            });
                        }
                    }
                }
        );
    }

    private void openAlbum(String albumId) {
        if (!isAdded() || albumId == null) {
            return;
        }

        Bundle args = new Bundle();
        args.putString("ACCESS_TOKEN", accessToken);
        args.putString("ALBUM_ID", albumId);

        AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(args);

        navigateTo(fragment);
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

        navigateTo(fragment);
    }

    private void openLibrary() {
        if (!isAdded()) {
            return;
        }

        Bundle args = new Bundle();
        args.putString("ACCESS_TOKEN", accessToken);

        LibraryFragment fragment = new LibraryFragment();
        fragment.setArguments(args);

        navigateTo(fragment);
    }

    private void navigateTo(Fragment fragment) {
        if (!isAdded()) {
            return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void runOnUiThread(Runnable action) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    action.run();
                }
            });
        }
    }

    private void showMessage(String message) {
        runOnUiThread(() ->
                Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_SHORT
                ).show()
        );
    }
}

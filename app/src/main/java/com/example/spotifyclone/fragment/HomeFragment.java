
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
import com.example.spotifyclone.adapter.MediaCardAdapter;
import com.example.spotifyclone.adapter.RecentGridAdapter;
import com.example.spotifyclone.api.AlbumApiClient;
import com.example.spotifyclone.api.ArtistApiClient;
import com.example.spotifyclone.api.PlayerApiClient;
import com.example.spotifyclone.api.PlaylistApiClient;
import com.example.spotifyclone.model.Album;
import com.example.spotifyclone.model.Artist;
import com.example.spotifyclone.model.MediaItem;
import com.example.spotifyclone.model.Page;
import com.example.spotifyclone.model.Playlist;
import com.example.spotifyclone.model.RecentlyPlayedResponse;
import com.example.spotifyclone.model.SavedAlbumsResponse;
import com.example.spotifyclone.utils.ApiCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends BaseFragment {

    // A grelha tem 8 posições: Músicas favoritas + 7 álbuns/artistas
    private static final int GRID_MEDIA_ITEMS = 7;

    private List<MediaItem> savedAlbums = new ArrayList<>();
    private List<MediaItem> topArtists = new ArrayList<>();
    private List<MediaItem> userPlaylists = new ArrayList<>();
    private List<RecentlyPlayedResponse.TrackItem> recentTracks = new ArrayList<>();

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

        // O cartão Músicas favoritas fica sempre disponível.
        showQuickAccess();

        if (getArguments() != null) {
            accessToken = getArguments().getString("ACCESS_TOKEN");
        }

        if (accessToken != null && !accessToken.isEmpty()) {
            fetchSavedAlbums(accessToken);
            fetchTopArtists(accessToken);
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

                    List<MediaItem> albums = new ArrayList<>();

                    if (result != null && result.items != null) {
                        for (SavedAlbumsResponse.SavedAlbumItem item
                                : result.items) {
                            if (item != null
                                    && item.album != null
                                    && item.album.id != null) {
                                albums.add(MediaItem.fromAlbum(item.album));
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        savedAlbums = albums;
                        showQuickAccess();
                    });
                }
            }
        });
    }

    private void fetchTopArtists(String token) {
        ArtistApiClient.getTopArtists(token, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                if (!alive()) {
                    return;
                }

                Page<Artist> page = GSON.fromJson(
                        body,
                        new TypeToken<Page<Artist>>() {}.getType()
                );

                List<MediaItem> artists = new ArrayList<>();

                if (page != null && page.items != null) {
                    for (Artist artist : page.items) {
                        if (artist != null && artist.id != null) {
                            artists.add(MediaItem.fromArtist(artist));
                        }
                    }
                }

                topArtists = artists;
                showQuickAccess();
                showRecents();
            }
        });
    }

    /** Preenche a grelha e o "Voltar a ouvir" com álbuns, artistas e playlists alternados. */
    private void showQuickAccess() {
        List<MediaItem> mixed = interleave(Arrays.asList(savedAlbums, topArtists, userPlaylists));

        List<MediaItem> gridItems = new ArrayList<>();
        gridItems.add(MediaItem.likedSongs());
        gridItems.addAll(
                mixed.subList(0, Math.min(GRID_MEDIA_ITEMS, mixed.size()))
        );

        recyclerViewGrid.setAdapter(new RecentGridAdapter(gridItems, this::open));

        recyclerViewJumpBackIn.setAdapter(
                new MediaCardAdapter(
                        mixed,
                        MediaCardAdapter.STYLE_CARD,
                        this::open
                )
        );
    }

    private List<MediaItem> interleave(List<List<MediaItem>> lists) {
        int longest = 0;
        for (List<MediaItem> list : lists) {
            longest = Math.max(longest, list.size());
        }

        List<MediaItem> result = new ArrayList<>();
        for (int i = 0; i < longest; i++) {
            for (List<MediaItem> list : lists) {
                if (i < list.size()) {
                    result.add(list.get(i));
                }
            }
        }

        return result;
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
                                userPlaylists = items;
                                showQuickAccess();

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
                            List<RecentlyPlayedResponse.TrackItem> tracks =
                                    new ArrayList<>();

                            if (result != null && result.items != null) {
                                for (RecentlyPlayedResponse.PlayHistoryObject history
                                        : result.items) {
                                    if (history != null && history.track != null) {
                                        tracks.add(history.track);
                                    }
                                }
                            }

                            runOnUiThread(() -> {
                                recentTracks = tracks;
                                showRecents();
                            });
                        }
                    }
                }
        );
    }

    private String primaryArtistId(RecentlyPlayedResponse.TrackItem track) {
        if (track.artists == null || track.artists.isEmpty()) {
            return null;
        }
        return track.artists.get(0).id;
    }

    /**
     * Mostra álbuns e artistas pela ordem em que foram ouvidos, sem repetir.
     * O histórico não traz imagens de artistas, por isso só entram os que vieram no top de artistas.
     */
    private void showRecents() {
        Map<String, MediaItem> artistsById = new HashMap<>();
        for (MediaItem artist : topArtists) {
            artistsById.put(artist.id, artist);
        }

        List<MediaItem> items = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (RecentlyPlayedResponse.TrackItem track : recentTracks) {
            Album album = track.album;
            if (album != null && album.id != null && seenIds.add(album.id)) {
                items.add(MediaItem.fromAlbum(album));
            }

            MediaItem artist = artistsById.get(primaryArtistId(track));
            if (artist != null && seenIds.add(artist.id)) {
                items.add(artist);
            }
        }

        recyclerViewRecents.setAdapter(
                new MediaCardAdapter(
                        items,
                        MediaCardAdapter.STYLE_CARD,
                        this::open
                )
        );
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

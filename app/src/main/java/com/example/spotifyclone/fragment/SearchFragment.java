package com.example.spotifyclone.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifyclone.MainActivity;
import com.example.spotifyclone.R;
import com.example.spotifyclone.adapter.MediaCardAdapter;
import com.example.spotifyclone.adapter.TrackAdapter;
import com.example.spotifyclone.api.PlaylistApiClient;
import com.example.spotifyclone.api.SearchApiClient;
import com.example.spotifyclone.model.Album;
import com.example.spotifyclone.model.Artist;
import com.example.spotifyclone.model.MediaItem;
import com.example.spotifyclone.model.Playlist;
import com.example.spotifyclone.model.SearchResponse;
import com.example.spotifyclone.model.Track;
import com.example.spotifyclone.utils.ApiCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFragment extends BaseFragment {

    public static final String ARG_ADD_TO_PLAYLIST_ID = "ADD_TO_PLAYLIST_ID";
    public static final String ARG_ADD_TO_PLAYLIST_NAME = "ADD_TO_PLAYLIST_NAME";

    private String addToPlaylistId;
    private String addToPlaylistName;

    private final List<Track> tracks = new ArrayList<>();
    private TrackAdapter trackAdapter;
    private MediaCardAdapter mediaAdapter;
    private TextView textEmpty;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        if (getArguments() != null) {
            addToPlaylistId = getArguments().getString(ARG_ADD_TO_PLAYLIST_ID);
            addToPlaylistName = getArguments().getString(ARG_ADD_TO_PLAYLIST_NAME);
        }

        TextView title = findByName(view, "textSearchTitle");
        if (title != null && addToPlaylistId != null) {
            if (addToPlaylistName != null) {
                title.setText("Adicionar a " + addToPlaylistName);
            } else {
                title.setText("Adicionar a Playlist");
            }
        }

        textEmpty = findByName(view, "textSearchEmpty");
        EditText editSearch = findByName(view, "editSearch");

        View layoutExploreGenres = findByName(view, "layoutExploreGenres");

        if (editSearch != null) {

            String[][] genres = {
                    {"genrePop", "pop"},
                    {"genreHipHop", "hip hop"},
                    {"genreRock", "rock"},
                    {"genreElectronic", "electronic"},
                    {"genreRelax", "chill"},
                    {"genreDiscover", "indie"}
            };

            for (String[] genre : genres) {
                View card = findByName(view, genre[0]);

                if (card != null) {
                    card.setOnClickListener(v -> {
                        editSearch.setText(genre[1]);
                        editSearch.setSelection(editSearch.getText().length());
                    });
                }
            }

            editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(
                        CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(
                        CharSequence s, int start, int before, int count) {

                    boolean empty = s.toString().trim().isEmpty();

                    if (layoutExploreGenres != null) {
                        layoutExploreGenres.setVisibility(
                                empty ? View.VISIBLE : View.GONE
                        );
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        RecyclerView rvTracks = findByName(view, "recyclerViewSearchTracks");
        if (rvTracks != null) {
            rvTracks.setLayoutManager(new LinearLayoutManager(getContext()));
            trackAdapter = new TrackAdapter(tracks, track -> onTrackClicked(track));
            trackAdapter.showCovers(true);
            trackAdapter.setOnTrackMenuListener((anchor, track, pos) -> {
                MainActivity activity = main();
                if (activity != null) {
                    activity.showTrackMenu(anchor, track, null, null);
                }
            });
            rvTracks.setAdapter(trackAdapter);
        }

        RecyclerView rvMedia = findByName(view, "recyclerViewSearchMedia");
        if (rvMedia == null) {
            rvMedia = findByName(view, "recyclerViewSearchResults");
        }
        if (rvMedia != null) {
            rvMedia.setLayoutManager(new LinearLayoutManager(getContext()));
            mediaAdapter = new MediaCardAdapter(null, MediaCardAdapter.STYLE_LIST, item -> open(item));
            rvMedia.setAdapter(mediaAdapter);
        }

        if (editSearch != null) {
            editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (pendingSearch != null) {
                        searchHandler.removeCallbacks(pendingSearch);
                    }
                    String query = s.toString().trim();
                    pendingSearch = () -> performSearch(query);
                    searchHandler.postDelayed(pendingSearch, 350);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        return view;
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T findByName(View root, String idName) {
        int id = root.getResources().getIdentifier(idName, "id", root.getContext().getPackageName());
        if (id != 0) {
            return (T) root.findViewById(id);
        } else {
            return null;
        }
    }

    private void onTrackClicked(Track track) {
        if (addToPlaylistId != null) {
            List<String> uris = Collections.singletonList(track.uriOrBuild());
            PlaylistApiClient.addItemsToPlaylist(token(), addToPlaylistId, uris, new ApiCallback() {
                @Override
                protected void onSuccess(String body) {
                    if (!alive()) {
                        return;
                    }
                    Toast.makeText(requireContext(), "Adicionada \"" + track.name + "\"", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            MainActivity activity = main();
            if (activity != null) {
                activity.playTrack(track, tracks);
            }
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            tracks.clear();
            if (trackAdapter != null) {
                trackAdapter.notifyDataSetChanged();
            }
            if (mediaAdapter != null) {
                mediaAdapter.submit(Collections.emptyList());
            }
            return;
        }

        String types;
        if (addToPlaylistId != null) {
            types = "track";
        } else {
            types = "track,artist,album,playlist";
        }

        SearchApiClient.search(token(), query, types, 10, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                if (!alive()) {
                    return;
                }

                SearchResponse res = GSON.fromJson(body, SearchResponse.class);
                tracks.clear();

                if (res != null && res.tracks != null && res.tracks.items != null) {
                    for (Track t : res.tracks.items) {
                        if (t != null && t.id != null) {
                            tracks.add(t);
                        }
                    }
                }

                if (trackAdapter != null) {
                    trackAdapter.notifyDataSetChanged();
                }

                if (mediaAdapter != null && addToPlaylistId == null && res != null) {
                    List<MediaItem> items = new ArrayList<>();

                    if (res.artists != null && res.artists.items != null) {
                        for (Artist a : res.artists.items) {
                            if (a != null) {
                                items.add(MediaItem.fromArtist(a));
                            }
                        }
                    }

                    if (res.albums != null && res.albums.items != null) {
                        for (Album al : res.albums.items) {
                            if (al != null) {
                                items.add(MediaItem.fromAlbum(al));
                            }
                        }
                    }

                    if (res.playlists != null && res.playlists.items != null) {
                        for (Playlist p : res.playlists.items) {
                            if (p != null) {
                                items.add(MediaItem.fromPlaylist(p));
                            }
                        }
                    }

                    mediaAdapter.submit(items);
                }

                if (textEmpty != null) {
                    if (tracks.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textEmpty.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}
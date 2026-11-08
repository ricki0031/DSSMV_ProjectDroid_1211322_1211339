package com.example.spotifyclone.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spotifyclone.MainActivity;
import com.example.spotifyclone.R;
import com.example.spotifyclone.adapter.TrackAdapter;
import com.example.spotifyclone.api.LibraryApiClient;
import com.example.spotifyclone.api.PlayerApiClient;
import com.example.spotifyclone.api.PlaylistApiClient;
import com.example.spotifyclone.api.TrackApiClient;
import com.example.spotifyclone.api.UserApiClient;
import com.example.spotifyclone.model.Page;
import com.example.spotifyclone.model.Playlist;
import com.example.spotifyclone.model.PlaylistItem;
import com.example.spotifyclone.model.SavedTrack;
import com.example.spotifyclone.model.Track;
import com.example.spotifyclone.model.UserProfile;
import com.example.spotifyclone.utils.ApiCallback;
import com.example.spotifyclone.utils.ImageUtils;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistFragment extends BaseFragment {

    public static final String ARG_PLAYLIST_ID = "PLAYLIST_ID";
    public static final String ARG_LIKED_SONGS = "LIKED_SONGS";

    private ImageView imageCover, btnEdit, btnAddSongs;
    private TextView textName, textDescription, textMeta, textEmpty;
    private ProgressBar progress;

    private String playlistId;
    private boolean likedSongs;
    private Playlist playlist;
    private String myUserId;
    private boolean editable = false;

    private final List<Track> tracks = new ArrayList<>();
    private TrackAdapter adapter;
    private int nextOffset = 0;
    private int total = 0;
    private boolean loading = false;
    private boolean hasMore = true;

    private Uri pendingCoverUri;
    private ImageView dialogCoverPreview;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pendingCoverUri = uri;
                    if (dialogCoverPreview != null) {
                        dialogCoverPreview.setImageURI(uri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        tracks.clear();
        nextOffset = 0;
        total = 0;
        hasMore = true;
        loading = false;

        if (getArguments() != null) {
            playlistId = getArguments().getString(ARG_PLAYLIST_ID);
            likedSongs = getArguments().getBoolean(ARG_LIKED_SONGS, false);
        }

        imageCover = view.findViewById(R.id.imagePlaylistCover);
        textName = view.findViewById(R.id.textPlaylistName);
        textDescription = view.findViewById(R.id.textPlaylistDescription);
        textMeta = view.findViewById(R.id.textPlaylistMeta);
        textEmpty = view.findViewById(R.id.textPlaylistEmpty);
        btnEdit = view.findViewById(R.id.btnPlaylistEdit);
        btnAddSongs = view.findViewById(R.id.btnPlaylistAddSongs);
        progress = view.findViewById(R.id.progressPlaylist);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPlaylistTracks);
        adapter = new TrackAdapter(tracks, track -> playTrack(track));
        adapter.showCovers(true);
        adapter.setOnTrackMenuListener((anchor, track, position) -> showTrackMenu(anchor, track, position));
        recyclerView.setAdapter(adapter);

        NestedScrollView scroll = view.findViewById(R.id.playlistScroll);
        scroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, sx, sy, osx, osy) -> {
            if (!v.canScrollVertically(1)) {
                loadMoreTracks();
            }
        });

        view.findViewById(R.id.btnPlaylistPlay).setOnClickListener(v -> playAll());
        btnEdit.setOnClickListener(v -> showEditDialog());
        btnAddSongs.setOnClickListener(v -> openSearchToAddSongs());

        if (likedSongs) {
            setupLikedSongsHeader();
            loadMoreTracks();
        } else if (playlistId != null) {
            loadPlaylist();
        }

        return view;
    }

    private void setupLikedSongsHeader() {
        textName.setText("Liked Songs");
        imageCover.setImageResource(R.drawable.bg_liked_songs);
        textMeta.setText("A carregar…");
    }

    private void loadPlaylist() {
        UserApiClient.getCurrentUserProfile(token(), new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                UserProfile me = GSON.fromJson(body, UserProfile.class);
                if (me != null) {
                    myUserId = me.id;
                } else {
                    myUserId = null;
                }
                fetchPlaylistDetails();
            }

            @Override
            protected void onError(int code, String body) {
                super.onError(code, body);
                fetchPlaylistDetails();
            }
        });
    }

    private void fetchPlaylistDetails() {
        PlaylistApiClient.getPlaylist(token(), playlistId, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                if (!alive()) {
                    return;
                }

                playlist = GSON.fromJson(body, Playlist.class);

                boolean isOwner = false;
                if (playlist.owner != null && playlist.owner.id != null && playlist.owner.id.equals(myUserId)) {
                    isOwner = true;
                }

                editable = playlist.collaborative || isOwner;
                renderPlaylistHeader();
                loadMoreTracks();
            }

            @Override
            protected void onError(int code, String body) {
                super.onError(code, body);
                if (!alive()) {
                    return;
                }
                textName.setText("Playlist indisponível");
                if (code == 404) {
                    showEmpty("O Spotify já não disponibiliza esta playlist a apps em modo de desenvolvimento.");
                } else {
                    showEmpty("Não foi possível carregar a playlist (" + code + ").");
                }
            }
        });
    }

    private void renderPlaylistHeader() {
        textName.setText(playlist.name);

        if (!TextUtils.isEmpty(playlist.description)) {
            String cleanDesc = Html.fromHtml(playlist.description, Html.FROM_HTML_MODE_COMPACT).toString();
            textDescription.setText(cleanDesc);
            textDescription.setVisibility(View.VISIBLE);
        } else {
            textDescription.setVisibility(View.GONE);
        }

        String cover = playlist.firstImageUrl();
        if (cover != null) {
            Glide.with(this).load(cover).into(imageCover);
        } else {
            imageCover.setImageResource(R.drawable.ic_music_note);
        }

        if (playlist.items != null) {
            total = playlist.items.total;
        }
        updateMeta();

        if (editable) {
            btnEdit.setVisibility(View.VISIBLE);
            btnAddSongs.setVisibility(View.VISIBLE);
        } else {
            btnEdit.setVisibility(View.GONE);
            btnAddSongs.setVisibility(View.GONE);
        }
    }

    private void updateMeta() {
        String owner = "";
        if (likedSongs) {
            owner = "Tu";
        } else if (playlist != null && playlist.owner != null && playlist.owner.displayName != null) {
            owner = playlist.owner.displayName;
        }

        String count;
        if (total == 1) {
            count = "1 song";
        } else {
            count = total + " songs";
        }

        if (owner.isEmpty()) {
            textMeta.setText(count);
        } else {
            textMeta.setText(owner + " • " + count);
        }
    }

    private void loadMoreTracks() {
        if (loading || !hasMore) {
            return;
        }
        loading = true;
        progress.setVisibility(View.VISIBLE);

        ApiCallback callback = new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                loading = false;
                if (!alive()) {
                    return;
                }
                progress.setVisibility(View.GONE);
                int before = tracks.size();

                if (likedSongs) {
                    Page<SavedTrack> page = GSON.fromJson(body, new TypeToken<Page<SavedTrack>>() {}.getType());
                    total = page.total;
                    hasMore = (page.next != null);

                    if (page.items != null) {
                        for (SavedTrack saved : page.items) {
                            if (saved.track != null) {
                                tracks.add(saved.track);
                            }
                        }
                        nextOffset += page.items.size();
                    }
                } else {
                    Page<PlaylistItem> page = GSON.fromJson(body, new TypeToken<Page<PlaylistItem>>() {}.getType());
                    total = page.total;
                    hasMore = (page.next != null);

                    if (page.items != null) {
                        for (PlaylistItem entry : page.items) {
                            if (entry.item != null && entry.item.id != null && !entry.isLocal) {
                                tracks.add(entry.item);
                            }
                        }
                        nextOffset += page.items.size();
                    }
                }

                adapter.notifyItemRangeInserted(before, tracks.size() - before);
                updateMeta();

                if (tracks.isEmpty()) {
                    if (editable) {
                        showEmpty("Esta playlist ainda não tem músicas. Toca no + para adicionar.");
                    } else {
                        showEmpty("Esta playlist ainda não tem músicas.");
                    }
                } else {
                    textEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(int code, String body) {
                super.onError(code, body);
                loading = false;
                hasMore = false;
                if (!alive()) {
                    return;
                }
                progress.setVisibility(View.GONE);
                if (code == 403) {
                    showEmpty("A Web API do Spotify só mostra as músicas de playlists que criaste ou onde colaboras.");
                } else {
                    showEmpty("Não foi possível carregar as músicas (" + code + ").");
                }
            }
        };

        if (likedSongs) {
            TrackApiClient.getSavedTracks(token(), nextOffset, callback);
        } else {
            PlaylistApiClient.getPlaylistItems(token(), playlistId, nextOffset, callback);
        }
    }

    private void showEmpty(String message) {
        textEmpty.setText(message);
        textEmpty.setVisibility(View.VISIBLE);
    }

    private void playTrack(Track track) {
        MainActivity activity = main();
        if (activity != null) {
            activity.playTrack(track, null);
        }
    }

    private void playAll() {
        MainActivity activity = main();
        if (activity == null || tracks.isEmpty()) {
            return;
        }

        Track first = tracks.get(0);
        activity.updateMiniPlayerInstantly(first.id, first.name, first.artistNames(), first.coverUrl());

        ApiCallback after = new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    MainActivity a = main();
                    if (a != null) {
                        a.syncMiniPlayer(token());
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
        };

        if (likedSongs) {
            List<String> uris = new ArrayList<>();
            for (Track t : tracks) {
                uris.add(t.uriOrBuild());
            }
            PlayerApiClient.startUrisPlayback(token(), uris, 0, deviceId(), after);
        } else {
            String contextUri = "spotify:playlist:" + playlistId;
            PlayerApiClient.startContextPlayback(token(), contextUri, 0, deviceId(), after);
        }
    }

    private void showTrackMenu(View anchor, Track track, int position) {
        MainActivity activity = main();
        if (activity == null) {
            return;
        }

        if (likedSongs) {
            activity.showTrackMenu(anchor, track, "Remover das Liked Songs", () -> removeFromLikedSongs(track, position));
        } else if (editable) {
            activity.showTrackMenu(anchor, track, "Remover desta playlist", () -> removeFromPlaylist(track, position));
        } else {
            activity.showTrackMenu(anchor, track, null, null);
        }
    }

    private void removeFromPlaylist(Track track, int position) {
        String currentSnapshot = null;
        if (playlist != null) {
            currentSnapshot = playlist.snapshotId;
        }

        List<String> urisToRemove = Collections.singletonList(track.uriOrBuild());

        PlaylistApiClient.removePlaylistItems(token(), playlistId, urisToRemove, currentSnapshot, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                JsonObject json = GSON.fromJson(body, JsonObject.class);
                if (playlist != null && json != null && json.has("snapshot_id")) {
                    playlist.snapshotId = json.get("snapshot_id").getAsString();
                }
                onTrackRemoved(position, "Removido da playlist");
            }
        });
    }

    private void removeFromLikedSongs(Track track, int position) {
        List<String> urisToRemove = Collections.singletonList(track.uriOrBuild());

        LibraryApiClient.removeItemsFromLibrary(token(), urisToRemove, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                onTrackRemoved(position, "Removido das Liked Songs");
            }
        });
    }

    private void onTrackRemoved(int position, String message) {
        if (!alive()) {
            return;
        }
        adapter.removeAt(position);
        adapter.notifyItemRangeChanged(position, tracks.size() - position);
        total = Math.max(0, total - 1);
        nextOffset = Math.max(0, nextOffset - 1);
        updateMeta();
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void openSearchToAddSongs() {
        if (playlist == null) {
            return;
        }
        SearchFragment search = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(SearchFragment.ARG_ADD_TO_PLAYLIST_ID, playlistId);
        args.putString(SearchFragment.ARG_ADD_TO_PLAYLIST_NAME, playlist.name);
        search.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, search)
                .addToBackStack(null)
                .commit();
    }

    private void showEditDialog() {
        if (playlist == null) {
            return;
        }
        pendingCoverUri = null;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_playlist, null);
        EditText editName = dialogView.findViewById(R.id.editPlaylistName);
        EditText editDescription = dialogView.findViewById(R.id.editPlaylistDescription);
        dialogCoverPreview = dialogView.findViewById(R.id.imageEditCover);

        editName.setText(playlist.name);
        if (playlist.description != null) {
            String cleanDesc = Html.fromHtml(playlist.description, Html.FROM_HTML_MODE_COMPACT).toString();
            editDescription.setText(cleanDesc);
        }
        if (playlist.firstImageUrl() != null) {
            Glide.with(this).load(playlist.firstImageUrl()).into(dialogCoverPreview);
        }

        View.OnClickListener pickListener = v -> pickImage.launch("image/*");
        dialogCoverPreview.setOnClickListener(pickListener);
        dialogView.findViewById(R.id.textChangeCover).setOnClickListener(pickListener);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Editar playlist")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newName = editName.getText().toString().trim();
                if (newName.isEmpty()) {
                    editName.setError("A playlist precisa de um nome");
                    return;
                }
                String newDescription = editDescription.getText().toString().trim();
                saveDetails(newName, newDescription);
                dialog.dismiss();
            });
        });

        dialog.setOnDismissListener(d -> {
            dialogCoverPreview = null;
        });

        dialog.show();
    }

    private void saveDetails(String newName, String newDescription) {
        PlaylistApiClient.changePlaylistDetails(token(), playlistId, newName, newDescription, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                if (!alive()) {
                    return;
                }
                playlist.name = newName;
                playlist.description = newDescription;
                renderPlaylistHeader();

                if (pendingCoverUri != null) {
                    uploadCover(pendingCoverUri);
                } else {
                    Toast.makeText(requireContext(), "Playlist atualizada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void onError(int code, String body) {
                super.onError(code, body);
                if (alive()) {
                    Toast.makeText(requireContext(), "Erro ao guardar (" + code + ")", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadCover(Uri imageUri) {
        Context appContext = requireContext().getApplicationContext();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        String accessToken = token();
        imageCover.setImageURI(imageUri);

        new Thread(() -> {
            try {
                String base64 = ImageUtils.toSpotifyCoverBase64(appContext, imageUri);
                PlaylistApiClient.addCustomPlaylistCoverImage(accessToken, playlistId, base64, new ApiCallback() {
                    @Override
                    protected void onSuccess(String body) {
                        if (alive()) {
                            Toast.makeText(requireContext(), "Capa atualizada", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    protected void onError(int code, String body) {
                        super.onError(code, body);
                        if (alive()) {
                            Toast.makeText(requireContext(), "Erro ao enviar a capa (" + code + ")", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (alive()) {
                        Toast.makeText(requireContext(), "Não foi possível ler a imagem", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
}
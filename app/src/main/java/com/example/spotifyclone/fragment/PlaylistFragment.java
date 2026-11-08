package com.example.spotifyclone.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

/**
 * Página de uma Playlist. Também é usada para as "Liked Songs" (que no fundo são uma playlist especial,
 * mas vêm de GET /me/tracks). Se a playlist for do utilizador, permite editar nome, descrição e capa,
 * adicionar músicas (via pesquisa) e remover músicas.
 */
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

    // Edição da capa
    private Uri pendingCoverUri;
    private ImageView dialogCoverPreview;
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pendingCoverUri = uri;
                    if (dialogCoverPreview != null) dialogCoverPreview.setImageURI(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        // Ao voltar da pesquisa ("adicionar músicas") a View é recriada: recomeçamos a lista do zero
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
        adapter = new TrackAdapter(tracks, this::playTrack)
                .showCovers(true)
                .setOnTrackMenuListener(this::showTrackMenu);
        recyclerView.setAdapter(adapter);

        // Carregar mais músicas quando o utilizador chega ao fim (paginação de 50 em 50)
        NestedScrollView scroll = view.findViewById(R.id.playlistScroll);
        scroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, sx, sy, osx, osy) -> {
            if (!v.canScrollVertically(1)) loadMoreTracks();
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

    // ---------------------------------------------------------------------------------------------
    // CARREGAR DADOS
    // ---------------------------------------------------------------------------------------------

    private void setupLikedSongsHeader() {
        textName.setText("Liked Songs");
        imageCover.setImageResource(R.drawable.bg_liked_songs);
        textMeta.setText("A carregar…");
    }

    private void loadPlaylist() {
        // 1º: quem sou eu? (para saber se a playlist é minha e pode ser editada)
        UserApiClient.getCurrentUserProfile(token(), new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                UserProfile me = GSON.fromJson(body, UserProfile.class);
                myUserId = me != null ? me.id : null;
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
                if (!alive()) return;
                playlist = GSON.fromJson(body, Playlist.class);
                editable = playlist.collaborative
                        || (playlist.owner != null && playlist.owner.id != null && playlist.owner.id.equals(myUserId));
                renderPlaylistHeader();
                loadMoreTracks();
            }

            @Override
            protected void onError(int code, String body) {
                super.onError(code, body);
                if (!alive()) return;
                textName.setText("Playlist indisponível");
                showEmpty(code == 404
                        ? "O Spotify já não disponibiliza esta playlist a apps em modo de desenvolvimento (ex: Daily Mix, Radios)."
                        : "Não foi possível carregar a playlist (" + code + ").");
            }
        });
    }

    private void renderPlaylistHeader() {
        textName.setText(playlist.name);

        if (!TextUtils.isEmpty(playlist.description)) {
            // A descrição pode vir com HTML (ex: links): mostramos só o texto
            textDescription.setText(android.text.Html.fromHtml(playlist.description, android.text.Html.FROM_HTML_MODE_COMPACT).toString());
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

        if (playlist.items != null) total = playlist.items.total;
        updateMeta();

        btnEdit.setVisibility(editable ? View.VISIBLE : View.GONE);
        btnAddSongs.setVisibility(editable ? View.VISIBLE : View.GONE);
    }

    private void updateMeta() {
        String owner = likedSongs ? "Tu"
                : (playlist != null && playlist.owner != null && playlist.owner.displayName != null ? playlist.owner.displayName : "");
        String count = total == 1 ? "1 song" : total + " songs";
        textMeta.setText(owner.isEmpty() ? count : owner + " • " + count);
    }

    private void loadMoreTracks() {
        if (loading || !hasMore) return;
        loading = true;
        progress.setVisibility(View.VISIBLE);

        ApiCallback callback = new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                loading = false;
                if (!alive()) return;
                progress.setVisibility(View.GONE);
                int before = tracks.size();

                if (likedSongs) {
                    Page<SavedTrack> page = GSON.fromJson(body, new TypeToken<Page<SavedTrack>>() {}.getType());
                    total = page.total;
                    hasMore = page.next != null;
                    if (page.items != null) {
                        for (SavedTrack saved : page.items) if (saved.track != null) tracks.add(saved.track);
                    }
                    nextOffset += page.items != null ? page.items.size() : 0;
                } else {
                    Page<PlaylistItem> page = GSON.fromJson(body, new TypeToken<Page<PlaylistItem>>() {}.getType());
                    total = page.total;
                    hasMore = page.next != null;
                    if (page.items != null) {
                        for (PlaylistItem entry : page.items) {
                            // Ignora episódios de podcast e ficheiros locais (não têm ID para tocar)
                            if (entry.item != null && entry.item.id != null && !entry.isLocal) tracks.add(entry.item);
                        }
                    }
                    nextOffset += page.items != null ? page.items.size() : 0;
                }

                adapter.notifyItemRangeInserted(before, tracks.size() - before);
                updateMeta();
                if (tracks.isEmpty()) {
                    showEmpty(editable ? "Esta playlist ainda não tem músicas. Toca no + para adicionar."
                            : "Esta playlist ainda não tem músicas.");
                } else {
                    textEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(int code, String body) {
                super.onError(code, body);
                loading = false;
                hasMore = false;
                if (!alive()) return;
                progress.setVisibility(View.GONE);
                if (code == 403) {
                    showEmpty("A Web API do Spotify só mostra as músicas de playlists que criaste ou onde colaboras.");
                } else {
                    showEmpty("Não foi possível carregar as músicas (" + code + ").");
                }
            }
        };

        if (likedSongs) {
            LibraryApiClient.getSavedTracks(token(), nextOffset, callback);
        } else {
            PlaylistApiClient.getPlaylistItems(token(), playlistId, nextOffset, callback);
        }
    }

    private void showEmpty(String message) {
        textEmpty.setText(message);
        textEmpty.setVisibility(View.VISIBLE);
    }

    // ---------------------------------------------------------------------------------------------
    // REPRODUÇÃO
    // ---------------------------------------------------------------------------------------------

    private void playTrack(Track track) {
        MainActivity activity = main();
        if (activity != null) activity.playTrack(track, null);
    }

    /** Botão verde: toca a playlist inteira (ou as Liked Songs) desde o início. */
    private void playAll() {
        MainActivity activity = main();
        if (activity == null || tracks.isEmpty()) return;

        Track first = tracks.get(0);
        activity.updateMiniPlayerInstantly(first.id, first.name, first.artistNames(), first.coverUrl());

        ApiCallback after = new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    MainActivity a = main();
                    if (a != null) a.syncMiniPlayer(token());
                }, 1000);
            }

            @Override
            protected void onError(int code, String body) {
                super.onError(code, body);
                if (alive()) Toast.makeText(requireContext(), "Não foi possível tocar (" + code + ")", Toast.LENGTH_SHORT).show();
            }
        };

        if (likedSongs) {
            List<String> uris = new ArrayList<>();
            for (Track t : tracks) uris.add(t.uriOrBuild());
            PlayerApiClient.startUrisPlayback(token(), uris, 0, deviceId(), after);
        } else {
            PlayerApiClient.startContextPlayback(token(), "spotify:playlist:" + playlistId, 0, deviceId(), after);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // REMOVER MÚSICAS
    // ---------------------------------------------------------------------------------------------

    private void showTrackMenu(View anchor, Track track, int position) {
        MainActivity activity = main();
        if (activity == null) return;

        if (likedSongs) {
            activity.showTrackMenu(anchor, track, "Remover das Liked Songs", () -> removeFromLikedSongs(track, position));
        } else if (editable) {
            activity.showTrackMenu(anchor, track, "Remover desta playlist", () -> removeFromPlaylist(track, position));
        } else {
            activity.showTrackMenu(anchor, track, null, null);
        }
    }

    private void removeFromPlaylist(Track track, int position) {
        PlaylistApiClient.removeItemsFromPlaylist(token(), playlistId, Collections.singletonList(track.uriOrBuild()),
                playlist != null ? playlist.snapshotId : null, new ApiCallback() {
                    @Override
                    protected void onSuccess(String body) {
                        // A resposta traz o snapshot_id novo, que usamos no próximo pedido
                        JsonObject json = GSON.fromJson(body, JsonObject.class);
                        if (playlist != null && json != null && json.has("snapshot_id")) {
                            playlist.snapshotId = json.get("snapshot_id").getAsString();
                        }
                        onTrackRemoved(position, "Removido da playlist");
                    }
                });
    }

    private void removeFromLikedSongs(Track track, int position) {
        LibraryApiClient.removeFromLibrary(token(), Collections.singletonList(track.uriOrBuild()), new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                onTrackRemoved(position, "Removido das Liked Songs");
            }
        });
    }

    private void onTrackRemoved(int position, String message) {
        if (!alive()) return;
        adapter.removeAt(position);
        // Os números das faixas abaixo mudam: atualizamos o resto da lista
        adapter.notifyItemRangeChanged(position, tracks.size() - position);
        total = Math.max(0, total - 1);
        nextOffset = Math.max(0, nextOffset - 1);
        updateMeta();
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    // ---------------------------------------------------------------------------------------------
    // ADICIONAR MÚSICAS (reutiliza o ecrã de pesquisa em "modo adicionar")
    // ---------------------------------------------------------------------------------------------

    private void openSearchToAddSongs() {
        if (playlist == null) return;
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

    // ---------------------------------------------------------------------------------------------
    // EDITAR NOME / DESCRIÇÃO / CAPA
    // ---------------------------------------------------------------------------------------------

    private void showEditDialog() {
        if (playlist == null) return;
        pendingCoverUri = null;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_playlist, null);
        EditText editName = dialogView.findViewById(R.id.editPlaylistName);
        EditText editDescription = dialogView.findViewById(R.id.editPlaylistDescription);
        dialogCoverPreview = dialogView.findViewById(R.id.imageEditCover);

        editName.setText(playlist.name);
        if (playlist.description != null) {
            editDescription.setText(android.text.Html.fromHtml(playlist.description, android.text.Html.FROM_HTML_MODE_COMPACT).toString());
        }
        if (playlist.firstImageUrl() != null) {
            Glide.with(this).load(playlist.firstImageUrl()).into(dialogCoverPreview);
        }

        View.OnClickListener pick = v -> pickImage.launch("image/*");
        dialogCoverPreview.setOnClickListener(pick);
        dialogView.findViewById(R.id.textChangeCover).setOnClickListener(pick);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Editar playlist")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            if (newName.isEmpty()) {
                editName.setError("A playlist precisa de um nome");
                return;
            }
            String newDescription = editDescription.getText().toString().trim();
            saveDetails(newName, newDescription);
            dialog.dismiss();
        }));
        dialog.setOnDismissListener(d -> dialogCoverPreview = null);
        dialog.show();
    }

    private void saveDetails(String newName, String newDescription) {
        PlaylistApiClient.changePlaylistDetails(token(), playlistId, newName, newDescription, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                if (!alive()) return;
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
                if (alive()) Toast.makeText(requireContext(), "Erro ao guardar (" + code + ")", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadCover(Uri imageUri) {
        android.content.Context appContext = requireContext().getApplicationContext();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        String accessToken = token();
        imageCover.setImageURI(imageUri); // mostra já a capa nova enquanto envia

        // Converter a imagem é pesado: fazemos fora da Main Thread
        new Thread(() -> {
            try {
                String base64 = ImageUtils.toSpotifyCoverBase64(appContext, imageUri);
                PlaylistApiClient.uploadCoverImage(accessToken, playlistId, base64, new ApiCallback() {
                    @Override
                    protected void onSuccess(String body) {
                        if (alive()) Toast.makeText(requireContext(), "Capa atualizada", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void onError(int code, String body) {
                        super.onError(code, body);
                        if (alive()) Toast.makeText(requireContext(), "Erro ao enviar a capa (" + code + ")", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (alive()) Toast.makeText(requireContext(), "Não foi possível ler a imagem", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}

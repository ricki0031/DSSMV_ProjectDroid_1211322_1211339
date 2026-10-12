package com.example.spotifyclone.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.example.spotifyclone.R;
import com.example.spotifyclone.adapter.RecentGridAdapter;
import com.example.spotifyclone.adapter.HorizontalCardAdapter;
import com.example.spotifyclone.api.PlayerApiClient;
import com.example.spotifyclone.model.RecentlyPlayedResponse;
import com.example.spotifyclone.model.Album;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewGrid, recyclerViewJumpBackIn;
    private String accessToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewGrid = view.findViewById(R.id.recyclerViewHomeGrid);
        recyclerViewGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));

        recyclerViewJumpBackIn = view.findViewById(R.id.recyclerViewJumpBackIn);
        recyclerViewJumpBackIn.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        if (getArguments() != null) {
            accessToken = getArguments().getString("ACCESS_TOKEN");
            // Fazer o pedido à API para alimentar as duas listas
            fetchActivityAndDistribute(accessToken);
        }

        return view;
    }

    private void fetchActivityAndDistribute(String token) {
        PlayerApiClient.getRecentlyPlayed(token, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("ERRO REDE RECENTES: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();

                if (response.isSuccessful() && getActivity() != null) {
                    try {
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        RecentlyPlayedResponse recentResponse = gson.fromJson(json, RecentlyPlayedResponse.class);

                        List<RecentGridAdapter.GridItem> gridItems = new ArrayList<>();
                        List<Album> jumpBackInAlbums = new ArrayList<>();
                        Set<String> processedAlbumIds = new HashSet<>(); // Para evitar capas repetidas

                        // 1. O botão estático Liked Songs sempre em 1º
                        RecentGridAdapter.GridItem likedSongs = new RecentGridAdapter.GridItem();
                        likedSongs.title = "Liked Songs";
                        likedSongs.isLikedSongs = true;
                        gridItems.add(likedSongs);

                        if (recentResponse.items != null) {
                            for (RecentlyPlayedResponse.PlayHistoryObject item : recentResponse.items) {
                                Album currentAlbum = item.track.album;

                                // Se a música tem álbum e ainda não o processámos
                                if (currentAlbum != null && currentAlbum.id != null && !processedAlbumIds.contains(currentAlbum.id)) {
                                    processedAlbumIds.add(currentAlbum.id);

                                    // Enchemos a grelha primeiro (até ter 8 itens no total)
                                    if (gridItems.size() < 8) {
                                        RecentGridAdapter.GridItem trackItem = new RecentGridAdapter.GridItem();
                                        trackItem.title = currentAlbum.name;
                                        trackItem.id = currentAlbum.id;
                                        trackItem.isLikedSongs = false;

                                        if (currentAlbum.images != null && !currentAlbum.images.isEmpty()) {
                                            trackItem.imageUrl = currentAlbum.images.get(0).url;
                                        }
                                        gridItems.add(trackItem);
                                    }
                                    // Quando a grelha estiver cheia, mandamos os restantes para o Jump Back In (até 10)
                                    else if (jumpBackInAlbums.size() < 10) {
                                        jumpBackInAlbums.add(currentAlbum);
                                    }
                                }
                            }
                        }

                        // Atualizar a UI com as duas listas e os respetivos Listeners de cliques
                        getActivity().runOnUiThread(() -> {

                            // Adaptador da Grelha 2x4
                            RecentGridAdapter gridAdapter = new RecentGridAdapter(gridItems, new RecentGridAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(RecentGridAdapter.GridItem item) {
                                    if (item.isLikedSongs) {
                                        System.out.println("CLIQUE NAS LIKED SONGS -> A desenvolver futuramente");
                                    } else {
                                        Intent intent = new Intent(getActivity(), com.example.spotifyclone.AlbumActivity.class);
                                        intent.putExtra("ACCESS_TOKEN", accessToken);
                                        intent.putExtra("ALBUM_ID", item.id);
                                        startActivity(intent);
                                    }
                                }
                            });
                            recyclerViewGrid.setAdapter(gridAdapter);

                            // Adaptador da Lista Horizontal (Jump back in)
                            HorizontalCardAdapter horizontalAdapter = new HorizontalCardAdapter(jumpBackInAlbums, new HorizontalCardAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(Album album) {
                                    Intent intent = new Intent(getActivity(), com.example.spotifyclone.AlbumActivity.class);
                                    intent.putExtra("ACCESS_TOKEN", accessToken);
                                    intent.putExtra("ALBUM_ID", album.id);
                                    startActivity(intent);
                                }
                            });
                            recyclerViewJumpBackIn.setAdapter(horizontalAdapter);

                        });

                    } catch (Exception e) {
                        System.out.println("ERRO GSON PROCESSING: " + e.getMessage());
                    }
                } else {
                    System.out.println("ERRO API (" + response.code() + "): " + json);
                }
            }
        });
    }
}
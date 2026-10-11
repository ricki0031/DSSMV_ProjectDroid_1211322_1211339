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

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.example.spotifyclone.AlbumActivity;
import com.example.spotifyclone.R;
import com.example.spotifyclone.adapter.AlbumAdapter;
import com.example.spotifyclone.api.AlbumApiClient;
import com.example.spotifyclone.model.Album;
import com.example.spotifyclone.model.SavedAlbumsResponse;

public class LibraryFragment extends Fragment {

    private RecyclerView recyclerViewGrid;
    private String accessToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Os Fragments não usam setContentView. Eles "insuflam" (inflate) uma view.
        // Vamos aproveitar o activity_main antigo que tinha a grelha, mas precisamos criar um layout xml próprio.
        // Para simplificar, vou pedir-te para criares o fragment_library.xml a seguir.
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        recyclerViewGrid = view.findViewById(R.id.recyclerViewLibrary);
        recyclerViewGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Recebemos o token que a MainActivity nos vai passar
        if (getArguments() != null) {
            accessToken = getArguments().getString("ACCESS_TOKEN");
            fetchSavedAlbums(accessToken);
        }

        return view;
    }

    private void fetchSavedAlbums(String token) {
        AlbumApiClient.getUserSavedAlbums(token, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) { }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    SavedAlbumsResponse savedResponse = gson.fromJson(json, SavedAlbumsResponse.class);

                    List<Album> meusAlbums = new ArrayList<>();
                    if (savedResponse.items != null) {
                        for (SavedAlbumsResponse.SavedAlbumItem item : savedResponse.items) {
                            meusAlbums.add(item.album);
                        }
                    }

                    // Num Fragment, usamos getActivity().runOnUiThread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlbumAdapter adapter = new AlbumAdapter(meusAlbums, new AlbumAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Album album) {
                                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                                        intent.putExtra("ACCESS_TOKEN", token);
                                        intent.putExtra("ALBUM_ID", album.id);
                                        startActivity(intent);
                                    }
                                });
                                recyclerViewGrid.setAdapter(adapter);
                            }
                        });
                    }
                }
            }
        });
    }
}
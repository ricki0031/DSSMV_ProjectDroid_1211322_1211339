package com.example.spotifyclone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spotifyclone.R;
import com.example.spotifyclone.model.Album;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private List<Album> albumList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Album album);
    }

    public AlbumAdapter(List<Album> albumList, OnItemClickListener listener) {
        this.albumList = albumList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_grid, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albumList.get(position);

        holder.textGridTitle.setText(album.name);

        if (album.artists != null && !album.artists.isEmpty()) {
            holder.textGridArtist.setText(album.artists.get(0).name);
        }

        if (album.images != null && !album.images.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(album.images.get(0).url)
                    .into(holder.imageGridCover);
        }

        // Detetar o clique no álbum
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(album);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (albumList != null) {
            return albumList.size();
        } else {
            return 0;
        }
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView imageGridCover;
        TextView textGridTitle, textGridArtist;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            imageGridCover = itemView.findViewById(R.id.imageGridCover);
            textGridTitle = itemView.findViewById(R.id.textGridTitle);
            textGridArtist = itemView.findViewById(R.id.textGridArtist);
        }
    }
}
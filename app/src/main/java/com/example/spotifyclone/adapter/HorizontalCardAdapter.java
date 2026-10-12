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

public class HorizontalCardAdapter extends RecyclerView.Adapter<HorizontalCardAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Album album);
    }
    private List<Album> items;
    private OnItemClickListener listener;

    public HorizontalCardAdapter(List<Album> items,OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horizontal_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = items.get(position);
        holder.title.setText(album.name);

        if (album.artists != null && !album.artists.isEmpty()) {
            holder.subtitle.setText(album.artists.get(0).name);
        }

        if (album.images != null && !album.images.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(album.images.get(0).url)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, subtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageCardCover);
            title = itemView.findViewById(R.id.textCardTitle);
            subtitle = itemView.findViewById(R.id.textCardSubtitle);
        }
    }
}
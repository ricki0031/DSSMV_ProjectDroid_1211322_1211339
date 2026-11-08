package com.example.spotifyclone.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.spotifyclone.R;
import com.example.spotifyclone.model.MediaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter único para álbuns, playlists, artistas e Liked Songs.
 *  - STYLE_CARD: cartão de 140dp para linhas horizontais (Jump back in, Your playlists, ...)
 *  - STYLE_LIST: linha com capa pequena + título + subtítulo (Library, resultados da pesquisa)
 * Os artistas aparecem sempre com a imagem redonda, como no Spotify.
 */
public class MediaCardAdapter extends RecyclerView.Adapter<MediaCardAdapter.ViewHolder> {

    public static final int STYLE_CARD = 0;
    public static final int STYLE_LIST = 1;

    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
    }

    private final List<MediaItem> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private final int style;

    public MediaCardAdapter(List<MediaItem> items, int style, OnItemClickListener listener) {
        if (items != null) this.items.addAll(items);
        this.style = style;
        this.listener = listener;
    }

    public void submit(List<MediaItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = style == STYLE_LIST ? R.layout.item_media_row : R.layout.item_horizontal_card;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view, style);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = items.get(position);
        holder.title.setText(item.title);
        holder.subtitle.setText(item.subtitle);

        if (item.isLikedSongs()) {
            Glide.with(holder.itemView).clear(holder.image);
            holder.image.setImageResource(R.drawable.bg_liked_songs);
        } else if (item.imageUrl == null) {
            Glide.with(holder.itemView).clear(holder.image);
            holder.image.setImageResource(R.drawable.ic_music_note);
        } else if (item.isArtist()) {
            Glide.with(holder.itemView).load(item.imageUrl).transform(new CircleCrop()).into(holder.image);
        } else {
            Glide.with(holder.itemView).load(item.imageUrl).into(holder.image);
        }

        // Nos cartões de artista o nome fica centrado debaixo do círculo
        int gravity = item.isArtist() && style == STYLE_CARD ? Gravity.CENTER_HORIZONTAL : Gravity.START;
        holder.title.setGravity(gravity);
        holder.subtitle.setGravity(gravity);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, subtitle;

        ViewHolder(@NonNull View itemView, int style) {
            super(itemView);
            if (style == STYLE_LIST) {
                image = itemView.findViewById(R.id.imageRowCover);
                title = itemView.findViewById(R.id.textRowTitle);
                subtitle = itemView.findViewById(R.id.textRowSubtitle);
            } else {
                image = itemView.findViewById(R.id.imageCardCover);
                title = itemView.findViewById(R.id.textCardTitle);
                subtitle = itemView.findViewById(R.id.textCardSubtitle);
            }
        }
    }
}

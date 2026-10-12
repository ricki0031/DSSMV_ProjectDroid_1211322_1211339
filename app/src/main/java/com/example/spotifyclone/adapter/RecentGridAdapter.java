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
import java.util.List;

public class RecentGridAdapter extends RecyclerView.Adapter<RecentGridAdapter.ViewHolder> {

    // O nosso modelo visual que mistura botões locais com músicas da API
    public static class GridItem {
        public String title;
        public String imageUrl;
        public boolean isLikedSongs; // Para sabermos se é o botão especial
        public String id; // Para no futuro sabermos o que tocar
    }

    public interface OnItemClickListener {
        void onItemClick(GridItem item);
    }

    private List<GridItem> items;
    private OnItemClickListener listener; // 2. Variável para guardar o listener
    public RecentGridAdapter(List<GridItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GridItem item = items.get(position);
        holder.title.setText(item.title);

        if (item.isLikedSongs) {
            // Se for o Liked Songs, metemos um ícone de sistema do Android (um coração ou estrela)
            holder.image.clearColorFilter();
            holder.image.setImageResource(R.drawable.bg_liked_songs);
        } else {
            // Se for da API, carregamos a capa do álbum
            Glide.with(holder.itemView.getContext())
                    .load(item.imageUrl)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageRecentCover);
            title = itemView.findViewById(R.id.textRecentTitle);
        }
    }
}
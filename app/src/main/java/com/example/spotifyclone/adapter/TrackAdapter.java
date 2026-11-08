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
import com.example.spotifyclone.model.Track;

import java.util.List;
import java.util.Locale;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Track track);
    }

    public interface OnTrackMenuListener {
        void onTrackMenu(View anchor, Track track, int position);
    }

    private List<Track> trackList;
    private OnItemClickListener listener;
    private OnTrackMenuListener menuListener;
    private boolean showCovers = false;

    public TrackAdapter(List<Track> trackList, OnItemClickListener listener) {
        this.trackList = trackList;
        this.listener = listener;
    }

    public TrackAdapter showCovers(boolean show) {
        this.showCovers = show;
        return this;
    }

    public TrackAdapter setOnTrackMenuListener(OnTrackMenuListener menuListener) {
        this.menuListener = menuListener;
        return this;
    }

    public void removeAt(int position) {
        if (trackList != null && position >= 0 && position < trackList.size()) {
            trackList.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = trackList.get(position);

        int number;
        if (track.trackNumber > 0) {
            number = track.trackNumber;
        } else {
            number = position + 1;
        }

        holder.textTrackNumber.setText(String.valueOf(number));
        holder.textTrackName.setText(track.name);

        // Conversão de milissegundos para minutos e segundos (ex: "3:42")
        long minutes = (track.durationMs / 1000) / 60;
        long seconds = (track.durationMs / 1000) % 60;
        holder.textTrackDuration.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));

        // Preenche o nome dos artistas
        String artists = track.artistNames();
        if (!artists.isEmpty()) {
            holder.textTrackArtist.setText(artists);
        } else {
            holder.textTrackArtist.setText("Artista Desconhecido");
        }

        // Mostrar ou esconder a capa da música (usado nas Playlists e na Pesquisa)
        if (holder.imageTrackCover != null) {
            if (showCovers && track.coverUrl() != null) {
                holder.imageTrackCover.setVisibility(View.VISIBLE);
                holder.textTrackNumber.setVisibility(View.GONE);
                Glide.with(holder.itemView.getContext())
                        .load(track.coverUrl())
                        .into(holder.imageTrackCover);
            } else {
                holder.imageTrackCover.setVisibility(View.GONE);
                holder.textTrackNumber.setVisibility(View.VISIBLE);
            }
        }

        // Botão de menu (três pontos) de cada faixa
        if (holder.btnTrackMore != null) {
            if (menuListener != null) {
                holder.btnTrackMore.setVisibility(View.VISIBLE);
                holder.btnTrackMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentPos = holder.getBindingAdapterPosition();
                        if (currentPos != RecyclerView.NO_POSITION) {
                            menuListener.onTrackMenu(v, trackList.get(currentPos), currentPos);
                        }
                    }
                });
            } else {
                holder.btnTrackMore.setVisibility(View.GONE);
            }
        }

        // Clique na linha da música para tocar
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(track);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (trackList != null) {
            return trackList.size();
        } else {
            return 0;
        }
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView textTrackNumber, textTrackName, textTrackArtist, textTrackDuration;
        ImageView imageTrackCover, btnTrackMore;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            textTrackNumber = itemView.findViewById(R.id.textTrackNumber);
            textTrackName = itemView.findViewById(R.id.textTrackName);
            textTrackArtist = itemView.findViewById(R.id.textTrackArtist);
            textTrackDuration = itemView.findViewById(R.id.textTrackDuration);
            imageTrackCover = itemView.findViewById(R.id.imageTrackCover);
            btnTrackMore = itemView.findViewById(R.id.btnTrackMore);
        }
    }
}
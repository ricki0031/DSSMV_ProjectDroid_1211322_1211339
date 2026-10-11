package com.example.spotifyclone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifyclone.R;
import com.example.spotifyclone.model.Track;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<Track> trackList;
    private OnItemClickListener listener;

    // 1. Criar a interface para passar o clique para a MainActivity
    public interface OnItemClickListener {
        void onItemClick(Track track);
    }

    // Construtor: recebe a lista de músicas que vem da API
    public TrackAdapter(List<Track> trackList, OnItemClickListener listener) {
        this.trackList = trackList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Pega no teu design de XML (item_track) e "insufla-o" para a memória
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        // Pega na música específica desta linha
        Track track = trackList.get(position);

        // Preenche os campos de texto com os dados da API
        holder.textTrackNumber.setText(String.valueOf(track.trackNumber));
        holder.textTrackName.setText(track.name);

        // Conversão de milissegundos para o formato de minutos "3:42"
        long minutes = (track.durationMs / 1000) / 60;
        long seconds = (track.durationMs / 1000) % 60;
        holder.textTrackDuration.setText(String.format("%d:%02d", minutes, seconds));

        // Preenche o nome do artista (pega no 1º artista da lista dessa faixa)
        if (track.artists != null && !track.artists.isEmpty()) {
            holder.textTrackArtist.setText(track.artists.get(0).name);
        } else {
            holder.textTrackArtist.setText("Artista Desconhecido");
        }

        // 3. Detetar o clique na linha inteira e enviar a faixa clicada
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
        return trackList != null ? trackList.size() : 0;
    }

    // A classe que guarda as referências aos elementos do ecrã para não estar sempre a procurar
    static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView textTrackNumber, textTrackName, textTrackArtist, textTrackDuration;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            textTrackNumber = itemView.findViewById(R.id.textTrackNumber);
            textTrackName = itemView.findViewById(R.id.textTrackName);
            textTrackArtist = itemView.findViewById(R.id.textTrackArtist);
            textTrackDuration = itemView.findViewById(R.id.textTrackDuration);
        }
    }
}
package com.example.spotifyclone.adapter;

import static com.example.spotifyclone.TestEnvironment.layoutList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifyclone.R;
import com.example.spotifyclone.model.Album;
import com.example.spotifyclone.model.Artist;
import com.example.spotifyclone.model.ImageObject;
import com.example.spotifyclone.model.Track;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class TrackAdapterTest {

    private final ContextThemeWrapper context =
            new ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_SpotifyClone);
    private final RecyclerView parent = new RecyclerView(context);

    @Before
    public void setUp() {
        parent.setLayoutManager(new LinearLayoutManager(context));
    }

    private static Track track() {
        Artist artist = new Artist();
        artist.name = "Parcels";
        Track track = new Track();
        track.name = "Ifyoucall";
        track.trackNumber = 7;
        track.durationMs = 241_000;
        track.artists = Collections.singletonList(artist);
        return track;
    }

    private TrackAdapter.TrackViewHolder bind(TrackAdapter adapter, int position) {
        TrackAdapter.TrackViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, position);
        return holder;
    }

    private static String text(RecyclerView.ViewHolder holder, int id) {
        return ((TextView) holder.itemView.findViewById(id)).getText().toString();
    }

    @Test
    public void getItemCount_isZeroForNullList() {
        assertEquals(0, new TrackAdapter(null, null).getItemCount());
    }

    @Test
    public void onBindViewHolder_showsTrackDetails() {
        TrackAdapter adapter = new TrackAdapter(Collections.singletonList(track()), null);

        TrackAdapter.TrackViewHolder holder = bind(adapter, 0);

        assertEquals("7", text(holder, R.id.textTrackNumber));
        assertEquals("Ifyoucall", text(holder, R.id.textTrackName));
        assertEquals("Parcels", text(holder, R.id.textTrackArtist));
        assertEquals("4:01", text(holder, R.id.textTrackDuration));
    }

    @Test
    public void onBindViewHolder_usesPositionWithoutTrackNumber() {
        Track noNumber = track();
        noNumber.trackNumber = 0;
        TrackAdapter adapter = new TrackAdapter(Arrays.asList(track(), noNumber), null);

        assertEquals("2", text(bind(adapter, 1), R.id.textTrackNumber));
    }

    @Test
    public void onBindViewHolder_showsPlaceholderWithoutArtists() {
        Track noArtist = track();
        noArtist.artists = null;
        TrackAdapter adapter = new TrackAdapter(Collections.singletonList(noArtist), null);

        assertEquals("Artista Desconhecido", text(bind(adapter, 0), R.id.textTrackArtist));
    }

    @Test
    public void showCovers_replacesTheNumberWithTheCover() {
        ImageObject image = new ImageObject();
        image.url = "cover";
        Track withCover = track();
        withCover.album = new Album();
        withCover.album.images = Collections.singletonList(image);
        TrackAdapter adapter = new TrackAdapter(Collections.singletonList(withCover), null).showCovers(true);

        TrackAdapter.TrackViewHolder holder = bind(adapter, 0);

        assertEquals(View.VISIBLE, holder.itemView.findViewById(R.id.imageTrackCover).getVisibility());
        assertEquals(View.GONE, holder.itemView.findViewById(R.id.textTrackNumber).getVisibility());
    }

    @Test
    public void showCovers_keepsTheNumberWhenThereIsNoCover() {
        TrackAdapter adapter = new TrackAdapter(Collections.singletonList(track()), null).showCovers(true);

        TrackAdapter.TrackViewHolder holder = bind(adapter, 0);

        assertEquals(View.GONE, holder.itemView.findViewById(R.id.imageTrackCover).getVisibility());
        assertEquals(View.VISIBLE, holder.itemView.findViewById(R.id.textTrackNumber).getVisibility());
    }

    @Test
    public void onBindViewHolder_notifiesListenerOnClick() {
        Track track = track();
        List<Track> clicked = new ArrayList<>();
        TrackAdapter adapter = new TrackAdapter(Collections.singletonList(track), clicked::add);

        bind(adapter, 0).itemView.performClick();

        assertSame(track, clicked.get(0));
    }

    @Test
    public void onBindViewHolder_hidesMenuButtonWithoutListener() {
        TrackAdapter adapter = new TrackAdapter(Collections.singletonList(track()), null);

        assertEquals(View.GONE, bind(adapter, 0).itemView.findViewById(R.id.btnTrackMore).getVisibility());
    }

    @Test
    public void setOnTrackMenuListener_reportsTheClickedRow() {
        Track track = track();
        List<Track> menuTracks = new ArrayList<>();
        List<Integer> menuPositions = new ArrayList<>();
        TrackAdapter adapter = new TrackAdapter(Collections.singletonList(track), null)
                .setOnTrackMenuListener((anchor, selected, position) -> {
                    menuTracks.add(selected);
                    menuPositions.add(position);
                });
        parent.setAdapter(adapter);
        layoutList(parent);

        parent.findViewHolderForAdapterPosition(0).itemView.findViewById(R.id.btnTrackMore).performClick();

        assertSame(track, menuTracks.get(0));
        assertEquals(Integer.valueOf(0), menuPositions.get(0));
    }

    @Test
    public void removeAt_removesTheTrack() {
        List<Track> tracks = new ArrayList<>(Arrays.asList(track(), track()));
        TrackAdapter adapter = new TrackAdapter(tracks, null);

        adapter.removeAt(0);

        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void removeAt_ignoresPositionsOutsideTheList() {
        List<Track> tracks = new ArrayList<>(Collections.singletonList(track()));
        TrackAdapter adapter = new TrackAdapter(tracks, null);

        adapter.removeAt(-1);
        adapter.removeAt(5);

        assertEquals(1, adapter.getItemCount());
    }
}

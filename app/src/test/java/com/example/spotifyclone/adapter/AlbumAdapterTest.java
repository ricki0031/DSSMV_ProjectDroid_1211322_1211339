package com.example.spotifyclone.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import android.view.ContextThemeWrapper;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.spotifyclone.R;
import com.example.spotifyclone.model.Album;
import com.example.spotifyclone.model.Artist;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class AlbumAdapterTest {

    private final FrameLayout parent = new FrameLayout(
            new ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_SpotifyClone));

    private static Album album() {
        Artist artist = new Artist();
        artist.name = "Parcels";
        Album album = new Album();
        album.name = "LOVED";
        album.artists = Collections.singletonList(artist);
        return album;
    }

    private static String text(AlbumAdapter.AlbumViewHolder holder, int id) {
        return ((TextView) holder.itemView.findViewById(id)).getText().toString();
    }

    @Test
    public void getItemCount_isZeroForNullList() {
        assertEquals(0, new AlbumAdapter(null, null).getItemCount());
    }

    @Test
    public void getItemCount_returnsListSize() {
        assertEquals(1, new AlbumAdapter(Collections.singletonList(album()), null).getItemCount());
    }

    @Test
    public void onBindViewHolder_showsTitleAndFirstArtist() {
        AlbumAdapter adapter = new AlbumAdapter(Collections.singletonList(album()), null);
        AlbumAdapter.AlbumViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);

        assertEquals("LOVED", text(holder, R.id.textGridTitle));
        assertEquals("Parcels", text(holder, R.id.textGridArtist));
    }

    @Test
    public void onBindViewHolder_notifiesListenerOnClick() {
        Album album = album();
        List<Album> clicked = new ArrayList<>();
        AlbumAdapter adapter = new AlbumAdapter(Collections.singletonList(album), clicked::add);
        AlbumAdapter.AlbumViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        holder.itemView.performClick();

        assertSame(album, clicked.get(0));
    }
}

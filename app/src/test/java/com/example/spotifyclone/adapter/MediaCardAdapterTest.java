package com.example.spotifyclone.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.spotifyclone.R;
import com.example.spotifyclone.model.Artist;
import com.example.spotifyclone.model.MediaItem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class MediaCardAdapterTest {

    private final FrameLayout parent = new FrameLayout(
            new ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_SpotifyClone));

    private static MediaItem artistItem() {
        Artist artist = new Artist();
        artist.name = "The Strokes";
        return MediaItem.fromArtist(artist);
    }

    private MediaCardAdapter.ViewHolder bind(MediaCardAdapter adapter) {
        MediaCardAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);
        return holder;
    }

    @Test
    public void getItemCount_isZeroForNullList() {
        assertEquals(0, new MediaCardAdapter(null, MediaCardAdapter.STYLE_LIST, null).getItemCount());
    }

    @Test
    public void submit_replacesTheItems() {
        MediaCardAdapter adapter = new MediaCardAdapter(
                Collections.singletonList(artistItem()), MediaCardAdapter.STYLE_LIST, null);

        adapter.submit(Arrays.asList(artistItem(), artistItem()));

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void submit_acceptsNullToClearTheList() {
        MediaCardAdapter adapter = new MediaCardAdapter(
                Collections.singletonList(artistItem()), MediaCardAdapter.STYLE_LIST, null);

        adapter.submit(null);

        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void onBindViewHolder_listStyleShowsTitleAndSubtitle() {
        MediaCardAdapter.ViewHolder holder = bind(new MediaCardAdapter(
                Collections.singletonList(artistItem()), MediaCardAdapter.STYLE_LIST, null));

        assertEquals("The Strokes", ((TextView) holder.itemView.findViewById(R.id.textRowTitle)).getText().toString());
        assertEquals("Artista", ((TextView) holder.itemView.findViewById(R.id.textRowSubtitle)).getText().toString());
    }

    @Test
    public void onBindViewHolder_cardStyleCentersArtistNames() {
        MediaCardAdapter.ViewHolder holder = bind(new MediaCardAdapter(
                Collections.singletonList(artistItem()), MediaCardAdapter.STYLE_CARD, null));

        TextView title = holder.itemView.findViewById(R.id.textCardTitle);
        assertEquals("The Strokes", title.getText().toString());
        assertEquals(Gravity.CENTER_HORIZONTAL, title.getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK);
    }

    @Test
    public void onBindViewHolder_cardStyleKeepsOtherItemsAligned() {
        MediaCardAdapter.ViewHolder holder = bind(new MediaCardAdapter(
                Collections.singletonList(MediaItem.likedSongs()), MediaCardAdapter.STYLE_CARD, null));

        TextView title = holder.itemView.findViewById(R.id.textCardTitle);
        assertEquals(Gravity.START, title.getGravity() & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK);
    }

    @Test
    public void onBindViewHolder_notifiesListenerOnClick() {
        MediaItem item = artistItem();
        List<MediaItem> clicked = new ArrayList<>();
        MediaCardAdapter.ViewHolder holder = bind(new MediaCardAdapter(
                Collections.singletonList(item), MediaCardAdapter.STYLE_LIST, clicked::add));

        holder.itemView.performClick();

        assertSame(item, clicked.get(0));
    }
}

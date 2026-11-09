package com.example.spotifyclone.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.robolectric.Shadows.shadowOf;

import android.view.ContextThemeWrapper;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.spotifyclone.R;
import com.example.spotifyclone.model.MediaItem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class RecentGridAdapterTest {

    private final FrameLayout parent = new FrameLayout(
            new ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_SpotifyClone));

    private RecentGridAdapter.ViewHolder bind(RecentGridAdapter adapter) {
        RecentGridAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);
        return holder;
    }

    @Test
    public void getItemCount_isZeroForNullList() {
        assertEquals(0, new RecentGridAdapter(null, null).getItemCount());
    }

    @Test
    public void getItemCount_returnsListSize() {
        assertEquals(1, new RecentGridAdapter(Collections.singletonList(MediaItem.likedSongs()), null).getItemCount());
    }

    @Test
    public void onBindViewHolder_showsTheTitle() {
        RecentGridAdapter.ViewHolder holder = bind(
                new RecentGridAdapter(Collections.singletonList(MediaItem.likedSongs()), null));

        assertEquals("Músicas favoritas", ((TextView) holder.itemView.findViewById(R.id.textRecentTitle)).getText().toString());
    }

    @Test
    public void onBindViewHolder_usesTheFavouritesImageForLikedSongs() {
        RecentGridAdapter.ViewHolder holder = bind(
                new RecentGridAdapter(Collections.singletonList(MediaItem.likedSongs()), null));

        ImageView image = holder.itemView.findViewById(R.id.imageRecentCover);
        assertEquals(R.drawable.bg_liked_songs, shadowOf(image.getDrawable()).getCreatedFromResId());
    }

    @Test
    public void onBindViewHolder_notifiesListenerOnClick() {
        MediaItem item = MediaItem.likedSongs();
        List<MediaItem> clicked = new ArrayList<>();
        RecentGridAdapter.ViewHolder holder = bind(
                new RecentGridAdapter(Collections.singletonList(item), clicked::add));

        holder.itemView.performClick();

        assertSame(item, clicked.get(0));
    }
}

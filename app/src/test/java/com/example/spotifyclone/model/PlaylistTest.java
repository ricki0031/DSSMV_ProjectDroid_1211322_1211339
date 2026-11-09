package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.Collections;

public class PlaylistTest {

    @Test
    public void firstImageUrl_isNullWithoutImages() {
        Playlist playlist = new Playlist();
        assertNull(playlist.firstImageUrl());

        playlist.images = Collections.emptyList();
        assertNull(playlist.firstImageUrl());
    }

    @Test
    public void firstImageUrl_returnsTheFirstImage() {
        ImageObject image = new ImageObject();
        image.url = "cover";
        Playlist playlist = new Playlist();
        playlist.images = Collections.singletonList(image);

        assertEquals("cover", playlist.firstImageUrl());
    }

    @Test
    public void gson_readsAllFields() {
        Playlist playlist = new Gson().fromJson(
                "{\"id\":\"p1\",\"name\":\"Tryouts\",\"description\":\"desc\",\"public\":false,"
                        + "\"collaborative\":true,\"snapshot_id\":\"s1\",\"uri\":\"spotify:playlist:p1\","
                        + "\"owner\":{\"id\":\"u1\",\"display_name\":\"Ricki\"}}",
                Playlist.class
        );

        assertEquals("p1", playlist.id);
        assertEquals("Tryouts", playlist.name);
        assertEquals("desc", playlist.description);
        assertFalse(playlist.isPublic);
        assertTrue(playlist.collaborative);
        assertEquals("s1", playlist.snapshotId);
        assertEquals("spotify:playlist:p1", playlist.uri);
        assertEquals("Ricki", playlist.owner.displayName);
    }

    @Test
    public void gson_readsTheCurrentItemsField() {
        Playlist playlist = new Gson().fromJson("{\"items\":{\"total\":14}}", Playlist.class);

        assertEquals(14, playlist.items.total);
    }

    @Test
    public void gson_stillAcceptsTheOldTracksField() {
        Playlist playlist = new Gson().fromJson("{\"tracks\":{\"total\":7}}", Playlist.class);

        assertEquals(7, playlist.items.total);
    }
}

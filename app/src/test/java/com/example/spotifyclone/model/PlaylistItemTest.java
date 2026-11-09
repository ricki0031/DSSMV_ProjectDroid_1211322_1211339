package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

public class PlaylistItemTest {

    @Test
    public void gson_readsTheCurrentItemField() {
        PlaylistItem item = new Gson().fromJson(
                "{\"added_at\":\"2026-01-01\",\"is_local\":true,\"item\":{\"id\":\"t1\"}}", PlaylistItem.class);

        assertEquals("2026-01-01", item.addedAt);
        assertTrue(item.isLocal);
        assertEquals("t1", item.item.id);
    }

    @Test
    public void gson_stillAcceptsTheOldTrackField() {
        PlaylistItem item = new Gson().fromJson("{\"track\":{\"id\":\"old\"}}", PlaylistItem.class);

        assertEquals("old", item.item.id);
        assertFalse(item.isLocal);
    }

    @Test
    public void gson_itemIsNullWhenMissing() {
        assertNull(new Gson().fromJson("{}", PlaylistItem.class).item);
    }
}

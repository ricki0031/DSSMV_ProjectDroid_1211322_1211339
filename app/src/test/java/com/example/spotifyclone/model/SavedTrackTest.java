package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class SavedTrackTest {

    @Test
    public void gson_readsAllFields() {
        SavedTrack saved = new Gson().fromJson(
                "{\"added_at\":\"2026-01-01\",\"track\":{\"id\":\"t1\"}}", SavedTrack.class);

        assertEquals("2026-01-01", saved.addedAt);
        assertEquals("t1", saved.track.id);
    }

    @Test
    public void gson_leavesTrackNullWhenMissing() {
        assertNull(new Gson().fromJson("{}", SavedTrack.class).track);
    }
}

package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

public class CurrentlyPlayingResponseTest {

    @Test
    public void gson_readsAllFields() {
        CurrentlyPlayingResponse response = new Gson().fromJson(
                "{\"is_playing\":true,\"progress_ms\":5000,\"item\":{\"id\":\"t1\"}}",
                CurrentlyPlayingResponse.class
        );

        assertTrue(response.isPlaying);
        assertEquals(5000L, response.progressMs);
        assertEquals("t1", response.item.id);
    }

    @Test
    public void gson_usesDefaultsWhenNothingIsPlaying() {
        CurrentlyPlayingResponse response = new Gson().fromJson("{}", CurrentlyPlayingResponse.class);

        assertFalse(response.isPlaying);
        assertNull(response.item);
    }
}

package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

public class PlaybackStateResponseTest {

    @Test
    public void gson_readsAllFields() {
        PlaybackStateResponse state = new Gson().fromJson(
                "{\"is_playing\":true,\"shuffle_state\":true,\"repeat_state\":\"context\",\"progress_ms\":1000,"
                        + "\"device\":{\"name\":\"Web Player\"},\"context\":{\"uri\":\"spotify:playlist:p1\"},"
                        + "\"item\":{\"id\":\"t1\"}}",
                PlaybackStateResponse.class
        );

        assertTrue(state.isPlaying);
        assertTrue(state.shuffleState);
        assertEquals("context", state.repeatState);
        assertEquals(1000L, state.progressMs);
        assertEquals("Web Player", state.device.name);
        assertEquals("spotify:playlist:p1", state.context.uri);
        assertEquals("t1", state.item.id);
    }

    @Test
    public void gson_contextIsNullWhenPlaybackHasNoContext() {
        PlaybackStateResponse state = new Gson().fromJson("{\"context\":null}", PlaybackStateResponse.class);

        assertNull(state.context);
        assertFalse(state.isPlaying);
    }
}

package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class QueueResponseTest {

    @Test
    public void gson_readsCurrentTrackAndQueue() {
        QueueResponse response = new Gson().fromJson(
                "{\"currently_playing\":{\"id\":\"now\"},\"queue\":[{\"id\":\"a\"},{\"id\":\"b\"}]}",
                QueueResponse.class
        );

        assertEquals("now", response.currentlyPlaying.id);
        assertEquals(2, response.queue.size());
        assertEquals("b", response.queue.get(1).id);
    }

    @Test
    public void gson_leavesFieldsNullWhenMissing() {
        QueueResponse response = new Gson().fromJson("{}", QueueResponse.class);

        assertNull(response.currentlyPlaying);
        assertNull(response.queue);
    }
}

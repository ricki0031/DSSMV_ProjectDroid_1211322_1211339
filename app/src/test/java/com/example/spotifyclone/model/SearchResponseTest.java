package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

public class SearchResponseTest {

    @Test
    public void gson_readsEveryResultType() {
        SearchResponse response = new Gson().fromJson(
                "{\"tracks\":{\"items\":[{\"id\":\"t1\"}]},\"artists\":{\"items\":[{\"id\":\"a1\"}]},"
                        + "\"albums\":{\"items\":[{\"id\":\"al1\"}]},\"playlists\":{\"items\":[{\"id\":\"p1\"}]}}",
                SearchResponse.class
        );

        assertEquals("t1", response.tracks.items.get(0).id);
        assertEquals("a1", response.artists.items.get(0).id);
        assertEquals("al1", response.albums.items.get(0).id);
        assertEquals("p1", response.playlists.items.get(0).id);
    }

    @Test
    public void gson_leavesTypesNotRequestedNull() {
        SearchResponse response = new Gson().fromJson("{\"tracks\":{\"items\":[]}}", SearchResponse.class);

        assertTrue(response.tracks.items.isEmpty());
        assertNull(response.artists);
        assertNull(response.albums);
        assertNull(response.playlists);
    }
}

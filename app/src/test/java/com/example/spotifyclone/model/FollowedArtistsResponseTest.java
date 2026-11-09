package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class FollowedArtistsResponseTest {

    @Test
    public void gson_readsArtistsPage() {
        FollowedArtistsResponse response = new Gson().fromJson(
                "{\"artists\":{\"items\":[{\"id\":\"a1\"}],\"total\":1}}", FollowedArtistsResponse.class);

        assertEquals(1, response.artists.total);
        assertEquals("a1", response.artists.items.get(0).id);
    }

    @Test
    public void gson_leavesPageNullWhenMissing() {
        assertNull(new Gson().fromJson("{}", FollowedArtistsResponse.class).artists);
    }
}

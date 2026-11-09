package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class RecentlyPlayedResponseTest {

    @Test
    public void gson_readsTrackAlbumAndArtists() {
        RecentlyPlayedResponse response = new Gson().fromJson(
                "{\"items\":[{\"track\":{\"id\":\"t1\",\"name\":\"Song\",\"album\":{\"id\":\"al1\"},"
                        + "\"artists\":[{\"id\":\"a1\"}]}}]}",
                RecentlyPlayedResponse.class
        );

        RecentlyPlayedResponse.TrackItem track = response.items.get(0).track;
        assertEquals("t1", track.id);
        assertEquals("Song", track.name);
        assertEquals("al1", track.album.id);
        assertEquals("a1", track.artists.get(0).id);
    }

    @Test
    public void gson_leavesItemsNullWhenMissing() {
        assertNull(new Gson().fromJson("{}", RecentlyPlayedResponse.class).items);
    }
}

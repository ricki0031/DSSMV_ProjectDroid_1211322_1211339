package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class NewReleasesResponseTest {

    @Test
    public void gson_readsAlbumItems() {
        NewReleasesResponse response = new Gson().fromJson(
                "{\"albums\":{\"items\":[{\"id\":\"al1\"}]}}", NewReleasesResponse.class);

        assertEquals("al1", response.albums.items.get(0).id);
    }

    @Test
    public void gson_leavesAlbumsNullWhenMissing() {
        assertNull(new Gson().fromJson("{}", NewReleasesResponse.class).albums);
    }
}

package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class SavedAlbumsResponseTest {

    @Test
    public void gson_readsAlbumInsideEachItem() {
        SavedAlbumsResponse response = new Gson().fromJson(
                "{\"items\":[{\"album\":{\"id\":\"al1\",\"name\":\"LOVED\"}}]}", SavedAlbumsResponse.class);

        assertEquals("al1", response.items.get(0).album.id);
        assertEquals("LOVED", response.items.get(0).album.name);
    }

    @Test
    public void gson_leavesItemsNullWhenMissing() {
        assertNull(new Gson().fromJson("{}", SavedAlbumsResponse.class).items);
    }
}

package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class ArtistTest {

    @Test
    public void gson_readsAllFields() {
        Artist artist = new Gson().fromJson(
                "{\"id\":\"a1\",\"name\":\"Parcels\",\"genres\":[\"disco\"],"
                        + "\"images\":[{\"url\":\"photo\"}],\"popularity\":70}",
                Artist.class
        );

        assertEquals("a1", artist.id);
        assertEquals("Parcels", artist.name);
        assertEquals("disco", artist.genres.get(0));
        assertEquals("photo", artist.images.get(0).url);
        assertEquals(Integer.valueOf(70), artist.popularity);
    }

    @Test
    public void gson_leavesOptionalFieldsNullWhenMissing() {
        Artist artist = new Gson().fromJson("{\"id\":\"a1\"}", Artist.class);

        assertNull(artist.genres);
        assertNull(artist.images);
        assertNull(artist.popularity);
    }
}

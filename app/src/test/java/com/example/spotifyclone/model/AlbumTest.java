package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class AlbumTest {

    private static ImageObject image(String url) {
        ImageObject image = new ImageObject();
        image.url = url;
        return image;
    }

    private static Artist artist(String name) {
        Artist artist = new Artist();
        artist.name = name;
        return artist;
    }

    @Test
    public void firstImageUrl_isNullWithoutImages() {
        Album album = new Album();
        assertNull(album.firstImageUrl());

        album.images = Collections.emptyList();
        assertNull(album.firstImageUrl());
    }

    @Test
    public void firstImageUrl_returnsTheFirstImage() {
        Album album = new Album();
        album.images = Arrays.asList(image("large"), image("small"));

        assertEquals("large", album.firstImageUrl());
    }

    @Test
    public void artistNames_isEmptyWithoutArtists() {
        Album album = new Album();
        assertEquals("", album.artistNames());

        album.artists = Collections.emptyList();
        assertEquals("", album.artistNames());
    }

    @Test
    public void artistNames_joinsNamesWithCommas() {
        Album album = new Album();
        album.artists = Arrays.asList(artist("Lou Reed"), artist("John Cale"));

        assertEquals("Lou Reed, John Cale", album.artistNames());
    }

    @Test
    public void gson_readsAllFields() {
        Album album = new Gson().fromJson(
                "{\"id\":\"al1\",\"name\":\"LOVED\",\"album_type\":\"single\",\"release_date\":\"2026-01-01\","
                        + "\"total_tracks\":2,\"tracks\":{\"items\":[{\"id\":\"t1\"},{\"id\":\"t2\"}]}}",
                Album.class
        );

        assertEquals("al1", album.id);
        assertEquals("LOVED", album.name);
        assertEquals("single", album.albumType);
        assertEquals("2026-01-01", album.releaseDate);
        assertEquals(2, album.totalTracks);
        assertEquals("t2", album.tracks.items.get(1).id);
    }
}

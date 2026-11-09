package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class TrackTest {

    private static Artist artist(String name) {
        Artist artist = new Artist();
        artist.name = name;
        return artist;
    }

    private static ImageObject image(String url) {
        ImageObject image = new ImageObject();
        image.url = url;
        return image;
    }

    @Test
    public void uriOrBuild_returnsUriWhenPresent() {
        Track track = new Track();
        track.uri = "spotify:track:abc";
        track.id = "other";

        assertEquals("spotify:track:abc", track.uriOrBuild());
    }

    @Test
    public void uriOrBuild_buildsUriFromIdWhenUriIsMissing() {
        Track track = new Track();
        track.id = "abc";

        assertEquals("spotify:track:abc", track.uriOrBuild());
    }

    @Test
    public void uriOrBuild_ignoresEmptyUri() {
        Track track = new Track();
        track.uri = "";
        track.id = "abc";

        assertEquals("spotify:track:abc", track.uriOrBuild());
    }

    @Test
    public void uriOrBuild_isEmptyWithoutUriOrId() {
        assertEquals("", new Track().uriOrBuild());
    }

    @Test
    public void artistNames_isEmptyWithoutArtists() {
        Track track = new Track();
        assertEquals("", track.artistNames());

        track.artists = new ArrayList<>();
        assertEquals("", track.artistNames());
    }

    @Test
    public void artistNames_returnsSingleName() {
        Track track = new Track();
        track.artists = Arrays.asList(artist("Parcels"));

        assertEquals("Parcels", track.artistNames());
    }

    @Test
    public void artistNames_joinsNamesWithCommas() {
        Track track = new Track();
        track.artists = Arrays.asList(artist("Pitbull"), artist("Sensato"));

        assertEquals("Pitbull, Sensato", track.artistNames());
    }

    @Test
    public void coverUrl_isNullWithoutAlbumOrImages() {
        Track track = new Track();
        assertNull(track.coverUrl());

        track.album = new Album();
        assertNull(track.coverUrl());

        track.album.images = new ArrayList<>();
        assertNull(track.coverUrl());
    }

    @Test
    public void coverUrl_returnsTheFirstAlbumImage() {
        Track track = new Track();
        track.album = new Album();
        track.album.images = Arrays.asList(image("large"), image("small"));

        assertEquals("large", track.coverUrl());
    }

    @Test
    public void gson_readsAllFields() {
        Track track = new Gson().fromJson(
                "{\"id\":\"t1\",\"name\":\"Ifyoucall\",\"uri\":\"spotify:track:t1\",\"track_number\":2,"
                        + "\"duration_ms\":241000,\"artists\":[{\"name\":\"Parcels\"}],"
                        + "\"album\":{\"id\":\"al1\"}}",
                Track.class
        );

        assertEquals("t1", track.id);
        assertEquals("Ifyoucall", track.name);
        assertEquals("spotify:track:t1", track.uri);
        assertEquals(2, track.trackNumber);
        assertEquals(241000L, track.durationMs);
        assertEquals("Parcels", track.artists.get(0).name);
        assertEquals("al1", track.album.id);
    }
}

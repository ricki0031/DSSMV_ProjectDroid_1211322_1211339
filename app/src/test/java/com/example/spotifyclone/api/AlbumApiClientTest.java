package com.example.spotifyclone.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import okhttp3.Request;

public class AlbumApiClientTest {

    private final FakeSpotifyApi api = new FakeSpotifyApi();

    @Before
    public void setUp() {
        api.install();
    }

    private void assertAuthorizedGet(String url) {
        Request request = api.lastRequest();
        assertEquals("GET", request.method());
        assertEquals(url, request.url().toString());
        assertEquals("Bearer token", request.header("Authorization"));
    }

    @Test
    public void getAlbum_requestsTheAlbumById() {
        AlbumApiClient.getAlbum("token", "al1", FakeSpotifyApi.IGNORE_RESPONSE);

        assertAuthorizedGet("https://api.spotify.com/v1/albums/al1");
    }

    @Test
    public void getNewReleases_requestsTwentyAlbums() {
        AlbumApiClient.getNewReleases("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertAuthorizedGet("https://api.spotify.com/v1/browse/new-releases?limit=20");
    }

    @Test
    public void getUserSavedAlbums_requestsFiftyAlbums() {
        AlbumApiClient.getUserSavedAlbums("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertAuthorizedGet("https://api.spotify.com/v1/me/albums?limit=50");
    }
}

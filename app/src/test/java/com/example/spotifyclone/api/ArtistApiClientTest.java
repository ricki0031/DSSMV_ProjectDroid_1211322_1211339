package com.example.spotifyclone.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import okhttp3.Request;

public class ArtistApiClientTest {

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
    public void getArtist_requestsTheArtistById() {
        ArtistApiClient.getArtist("token", "a1", FakeSpotifyApi.IGNORE_RESPONSE);

        assertAuthorizedGet("https://api.spotify.com/v1/artists/a1");
    }

    @Test
    public void getTopArtists_requestsShortTermArtists() {
        ArtistApiClient.getTopArtists("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertAuthorizedGet("https://api.spotify.com/v1/me/top/artists?time_range=short_term&limit=20");
    }

    @Test
    public void getArtistAlbums_staysWithinSpotifyLimitOfTen() {
        ArtistApiClient.getArtistAlbums("token", "a1", FakeSpotifyApi.IGNORE_RESPONSE);

        assertAuthorizedGet("https://api.spotify.com/v1/artists/a1/albums?include_groups=album,single&limit=10");
    }
}

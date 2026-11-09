package com.example.spotifyclone.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import okhttp3.Request;

public class TrackApiClientTest {

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
    public void getTrack_requestsTheTrackById() {
        TrackApiClient.getTrack("token", "t1", FakeSpotifyApi.IGNORE_RESPONSE);

        assertAuthorizedGet("https://api.spotify.com/v1/tracks/t1");
    }

    @Test
    public void getSavedTracks_sendsOffset() {
        TrackApiClient.getSavedTracks("token", 50, FakeSpotifyApi.IGNORE_RESPONSE);

        assertAuthorizedGet("https://api.spotify.com/v1/me/tracks?limit=50&offset=50");
    }
}

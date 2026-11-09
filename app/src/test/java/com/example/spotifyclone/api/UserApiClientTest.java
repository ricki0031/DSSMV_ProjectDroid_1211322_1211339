package com.example.spotifyclone.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import okhttp3.Request;

public class UserApiClientTest {

    private final FakeSpotifyApi api = new FakeSpotifyApi();

    @Before
    public void setUp() {
        api.install();
    }

    @Test
    public void getCurrentUserProfile_requestsTheAuthenticatedUser() {
        UserApiClient.getCurrentUserProfile("token", FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = api.lastRequest();
        assertEquals("GET", request.method());
        assertEquals("https://api.spotify.com/v1/me", request.url().toString());
        assertEquals("Bearer token", request.header("Authorization"));
    }
}

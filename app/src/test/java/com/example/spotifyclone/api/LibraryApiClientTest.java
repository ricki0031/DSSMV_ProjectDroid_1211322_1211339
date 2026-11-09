package com.example.spotifyclone.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import okhttp3.Request;

@RunWith(RobolectricTestRunner.class)
public class LibraryApiClientTest {

    private static final List<String> URIS = Arrays.asList("spotify:track:a", "spotify:track:b");
    private static final String ENCODED_URIS = "spotify%3Atrack%3Aa,spotify%3Atrack%3Ab";

    private final FakeSpotifyApi api = new FakeSpotifyApi();

    @Before
    public void setUp() {
        api.install();
    }

    private void assertRequest(String method, String url) {
        Request request = api.lastRequest();
        assertEquals(method, request.method());
        assertEquals(url, request.url().toString());
        assertEquals("Bearer token", request.header("Authorization"));
    }

    @Test
    public void saveItemsToLibrary_sendsPutWithEncodedUris() {
        LibraryApiClient.saveItemsToLibrary("token", URIS, FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("PUT", "https://api.spotify.com/v1/me/library?uris=" + ENCODED_URIS);
    }

    @Test
    public void removeItemsFromLibrary_sendsDeleteWithEncodedUris() {
        LibraryApiClient.removeItemsFromLibrary("token", URIS, FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("DELETE", "https://api.spotify.com/v1/me/library?uris=" + ENCODED_URIS);
    }

    @Test
    public void checkUserSavedItems_sendsGetToContainsEndpoint() {
        LibraryApiClient.checkUserSavedItems("token", URIS, FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("GET", "https://api.spotify.com/v1/me/library/contains?uris=" + ENCODED_URIS);
    }
}

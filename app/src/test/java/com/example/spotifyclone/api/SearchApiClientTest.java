package com.example.spotifyclone.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import okhttp3.Request;

@RunWith(RobolectricTestRunner.class)
public class SearchApiClientTest {

    private final FakeSpotifyApi api = new FakeSpotifyApi();

    @Before
    public void setUp() {
        api.install();
    }

    @Test
    public void search_encodesQueryAndSendsTypesAndLimit() {
        SearchApiClient.search("token", "hip hop", "track,artist", 5, FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = api.lastRequest();
        assertEquals("GET", request.method());
        assertEquals("https://api.spotify.com/v1/search?q=hip%20hop&type=track,artist&limit=5", request.url().toString());
        assertEquals("Bearer token", request.header("Authorization"));
    }

    @Test
    public void search_withoutTypesSearchesEverythingWithLimitTen() {
        SearchApiClient.search("token", "rock", FakeSpotifyApi.IGNORE_RESPONSE);

        assertEquals(
                "https://api.spotify.com/v1/search?q=rock&type=track,artist,album,playlist&limit=10",
                api.lastRequest().url().toString()
        );
    }
}

package com.example.spotifyclone.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import okhttp3.Request;

public class PlaylistApiClientTest {

    private static final String BASE = "https://api.spotify.com/v1";

    private final FakeSpotifyApi api = new FakeSpotifyApi();

    @Before
    public void setUp() {
        api.install();
    }

    private Request assertRequest(String method, String url) {
        Request request = api.lastRequest();
        assertEquals(method, request.method());
        assertEquals(url, request.url().toString());
        assertEquals("Bearer token", request.header("Authorization"));
        return request;
    }

    @Test
    public void getPlaylist_requestsThePlaylistById() {
        PlaylistApiClient.getPlaylist("token", "p1", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("GET", BASE + "/playlists/p1");
    }

    @Test
    public void getPlaylistItems_sendsOffset() {
        PlaylistApiClient.getPlaylistItems("token", "p1", 100, FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("GET", BASE + "/playlists/p1/items?limit=50&offset=100");
    }

    @Test
    public void getCurrentUserPlaylists_sendsLimitAndOffset() {
        PlaylistApiClient.getCurrentUserPlaylists("token", 20, 40, FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("GET", BASE + "/me/playlists?limit=20&offset=40");
    }

    @Test
    public void getCurrentUserPlaylists_usesDefaultsWithoutPaging() {
        PlaylistApiClient.getCurrentUserPlaylists("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("GET", BASE + "/me/playlists?limit=50&offset=0");
    }

    @Test
    public void createPlaylist_sendsAllDetails() throws IOException {
        PlaylistApiClient.createPlaylist("token", "Name", "Desc", true, FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("POST", BASE + "/me/playlists");
        assertEquals("{\"name\":\"Name\",\"description\":\"Desc\",\"public\":true}", FakeSpotifyApi.bodyOf(request));
    }

    @Test
    public void createPlaylist_usesEmptyDescriptionWhenNull() throws IOException {
        PlaylistApiClient.createPlaylist("token", "Name", null, false, FakeSpotifyApi.IGNORE_RESPONSE);

        assertEquals(
                "{\"name\":\"Name\",\"description\":\"\",\"public\":false}",
                FakeSpotifyApi.bodyOf(api.lastRequest())
        );
    }

    @Test
    public void createPlaylist_isPrivateByDefault() throws IOException {
        PlaylistApiClient.createPlaylist("token", "Name", "Desc", FakeSpotifyApi.IGNORE_RESPONSE);

        assertEquals(
                "{\"name\":\"Name\",\"description\":\"Desc\",\"public\":false}",
                FakeSpotifyApi.bodyOf(api.lastRequest())
        );
    }

    @Test
    public void changePlaylistDetails_sendsNameAndDescription() throws IOException {
        PlaylistApiClient.changePlaylistDetails("token", "p1", "Name", "Desc", FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("PUT", BASE + "/playlists/p1");
        assertEquals("{\"name\":\"Name\",\"description\":\"Desc\"}", FakeSpotifyApi.bodyOf(request));
    }

    @Test
    public void changePlaylistDetails_omitsNullDescription() throws IOException {
        PlaylistApiClient.changePlaylistDetails("token", "p1", "Name", null, FakeSpotifyApi.IGNORE_RESPONSE);

        assertEquals("{\"name\":\"Name\"}", FakeSpotifyApi.bodyOf(api.lastRequest()));
    }

    @Test
    public void addItemsToPlaylist_sendsUris() throws IOException {
        PlaylistApiClient.addItemsToPlaylist(
                "token", "p1", Collections.singletonList("uri1"), FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("POST", BASE + "/playlists/p1/items");
        assertEquals("{\"uris\":[\"uri1\"]}", FakeSpotifyApi.bodyOf(request));
    }

    @Test
    public void removePlaylistItems_sendsUrisAndSnapshot() throws IOException {
        PlaylistApiClient.removePlaylistItems(
                "token", "p1", Collections.singletonList("uri1"), "s1", FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("DELETE", BASE + "/playlists/p1/items");
        assertEquals("{\"items\":[{\"uri\":\"uri1\"}],\"snapshot_id\":\"s1\"}", FakeSpotifyApi.bodyOf(request));
    }

    @Test
    public void removePlaylistItems_omitsMissingSnapshot() throws IOException {
        PlaylistApiClient.removePlaylistItems(
                "token", "p1", Collections.singletonList("uri1"), null, FakeSpotifyApi.IGNORE_RESPONSE);

        assertEquals("{\"items\":[{\"uri\":\"uri1\"}]}", FakeSpotifyApi.bodyOf(api.lastRequest()));
    }

    @Test
    public void addCustomPlaylistCoverImage_sendsJpegBody() throws IOException {
        PlaylistApiClient.addCustomPlaylistCoverImage("token", "p1", "base64", FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("PUT", BASE + "/playlists/p1/images");
        assertEquals("base64", FakeSpotifyApi.bodyOf(request));
        assertEquals("image/jpeg", request.body().contentType().toString());
    }
}

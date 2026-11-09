package com.example.spotifyclone.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Request;

public class PlayerApiClientTest {

    private static final String BASE = "https://api.spotify.com/v1";
    private static final String PLAY_URL = BASE + "/me/player/play";

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
    public void getPlaybackState_sendsGet() {
        PlayerApiClient.getPlaybackState("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("GET", BASE + "/me/player");
    }

    @Test
    public void transferPlayback_sendsDeviceAndStartsPlaying() throws IOException {
        PlayerApiClient.transferPlayback("token", "d1", FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("PUT", BASE + "/me/player");
        assertEquals("{\"device_ids\": [\"d1\"], \"play\": true}", FakeSpotifyApi.bodyOf(request));
    }

    @Test
    public void getAvailableDevices_sendsGet() {
        PlayerApiClient.getAvailableDevices("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("GET", BASE + "/me/player/devices");
    }

    @Test
    public void getCurrentlyPlayingTrack_sendsGet() {
        PlayerApiClient.getCurrentlyPlayingTrack("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("GET", BASE + "/me/player/currently-playing");
    }

    @Test
    public void startResumePlayback_sendsTrackAndDevice() throws IOException {
        PlayerApiClient.startResumePlayback("token", "t1", "d1", FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("PUT", PLAY_URL + "?device_id=d1");
        assertEquals("{\"uris\": [\"spotify:track:t1\"]}", FakeSpotifyApi.bodyOf(request));
    }

    @Test
    public void startResumePlayback_sendsEmptyBodyWithoutTrack() throws IOException {
        PlayerApiClient.startResumePlayback("token", null, null, FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("PUT", PLAY_URL);
        assertEquals("", FakeSpotifyApi.bodyOf(request));
    }

    @Test
    public void startContextPlayback_sendsContextAndOffset() throws IOException {
        PlayerApiClient.startContextPlayback(
                "token", "spotify:playlist:p1", 3, "d1", FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("PUT", PLAY_URL + "?device_id=d1");
        assertEquals(
                "{\"context_uri\": \"spotify:playlist:p1\", \"offset\": {\"position\": 3}}",
                FakeSpotifyApi.bodyOf(request)
        );
    }

    @Test
    public void startContextPlayback_omitsOffsetForArtists() throws IOException {
        PlayerApiClient.startContextPlayback(
                "token", "spotify:artist:a1", 3, null, FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("PUT", PLAY_URL);
        assertEquals("{\"context_uri\": \"spotify:artist:a1\"}", FakeSpotifyApi.bodyOf(request));
    }

    @Test
    public void startContextPlaybackAtTrack_startsAtTheGivenTrack() throws IOException {
        PlayerApiClient.startContextPlaybackAtTrack(
                "token", "spotify:album:al1", "spotify:track:t1", "d1", FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("PUT", PLAY_URL + "?device_id=d1");
        assertEquals(
                "{\"context_uri\": \"spotify:album:al1\", \"offset\": {\"uri\": \"spotify:track:t1\"}}",
                FakeSpotifyApi.bodyOf(request)
        );
    }

    @Test
    public void startUrisPlayback_sendsAllUrisAndOffset() throws IOException {
        PlayerApiClient.startUrisPlayback(
                "token", Arrays.asList("a", "b"), 1, null, FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = assertRequest("PUT", PLAY_URL);
        assertEquals("{\"uris\": [\"a\", \"b\"], \"offset\": {\"position\": 1}}", FakeSpotifyApi.bodyOf(request));
    }

    @Test
    public void pausePlayback_sendsPut() {
        PlayerApiClient.pausePlayback("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("PUT", BASE + "/me/player/pause");
    }

    @Test
    public void skipToNext_sendsPost() {
        PlayerApiClient.skipToNext("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("POST", BASE + "/me/player/next");
    }

    @Test
    public void skipToPrevious_sendsPost() {
        PlayerApiClient.skipToPrevious("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("POST", BASE + "/me/player/previous");
    }

    @Test
    public void seekToPosition_sendsPosition() {
        PlayerApiClient.seekToPosition("token", 1500, FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("PUT", BASE + "/me/player/seek?position_ms=1500");
    }

    @Test
    public void seekToPosition_replacesNegativePositionWithZero() {
        PlayerApiClient.seekToPosition("token", -10, FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("PUT", BASE + "/me/player/seek?position_ms=0");
    }

    @Test
    public void setRepeatMode_sendsValidState() {
        PlayerApiClient.setRepeatMode("token", "track", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("PUT", BASE + "/me/player/repeat?state=track");
    }

    @Test
    public void setRepeatMode_replacesInvalidStateWithOff() {
        PlayerApiClient.setRepeatMode("token", "invalid", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("PUT", BASE + "/me/player/repeat?state=off");
    }

    @Test
    public void setPlaybackVolume_sendsVolume() {
        PlayerApiClient.setPlaybackVolume("token", 40, FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("PUT", BASE + "/me/player/volume?volume_percent=40");
    }

    @Test
    public void setPlaybackVolume_clampsValuesOutsideTheRange() {
        PlayerApiClient.setPlaybackVolume("token", 150, FakeSpotifyApi.IGNORE_RESPONSE);
        assertRequest("PUT", BASE + "/me/player/volume?volume_percent=100");

        PlayerApiClient.setPlaybackVolume("token", -5, FakeSpotifyApi.IGNORE_RESPONSE);
        assertRequest("PUT", BASE + "/me/player/volume?volume_percent=0");
    }

    @Test
    public void togglePlaybackShuffle_sendsState() {
        PlayerApiClient.togglePlaybackShuffle("token", true, FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("PUT", BASE + "/me/player/shuffle?state=true");
    }

    @Test
    public void getRecentlyPlayedTracks_requestsFiftyTracks() {
        PlayerApiClient.getRecentlyPlayedTracks("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("GET", BASE + "/me/player/recently-played?limit=50");
    }

    @Test
    public void getTheUserQueue_sendsGet() {
        PlayerApiClient.getTheUserQueue("token", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("GET", BASE + "/me/player/queue");
    }

    @Test
    public void addItemToPlaybackQueue_sendsTheTrackUri() {
        PlayerApiClient.addItemToPlaybackQueue("token", "spotify:track:t1", FakeSpotifyApi.IGNORE_RESPONSE);

        assertRequest("POST", BASE + "/me/player/queue?uri=spotify:track:t1");
    }
}

package com.example.spotifyclone.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import okhttp3.FormBody;
import okhttp3.Request;

public class SpotifyAuthClientTest {

    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    private final FakeSpotifyApi api = new FakeSpotifyApi();

    @Before
    public void setUp() {
        api.install();
    }

    private static String formValue(Request request, String name) {
        FormBody form = (FormBody) request.body();
        for (int i = 0; i < form.size(); i++) {
            if (form.name(i).equals(name)) {
                return form.value(i);
            }
        }
        return null;
    }

    @Test
    public void getAccessToken_sendsAuthorizationCodeForm() {
        SpotifyAuthClient.getAccessToken(
                "client", "code", "app://callback", "verifier", FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = api.lastRequest();
        assertEquals("POST", request.method());
        assertEquals(TOKEN_URL, request.url().toString());
        assertEquals("client", formValue(request, "client_id"));
        assertEquals("authorization_code", formValue(request, "grant_type"));
        assertEquals("code", formValue(request, "code"));
        assertEquals("app://callback", formValue(request, "redirect_uri"));
        assertEquals("verifier", formValue(request, "code_verifier"));
    }

    @Test
    public void refreshAccessToken_sendsRefreshTokenForm() {
        SpotifyAuthClient.refreshAccessToken("client", "refresh", FakeSpotifyApi.IGNORE_RESPONSE);

        Request request = api.lastRequest();
        assertEquals("POST", request.method());
        assertEquals(TOKEN_URL, request.url().toString());
        assertEquals("client", formValue(request, "client_id"));
        assertEquals("refresh_token", formValue(request, "grant_type"));
        assertEquals("refresh", formValue(request, "refresh_token"));
    }
}

package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class TokenResponseTest {

    private final TokenResponse token = new Gson().fromJson(
            "{\"access_token\":\"access\",\"token_type\":\"Bearer\",\"expires_in\":3600,"
                    + "\"refresh_token\":\"refresh\",\"scope\":\"a b\"}",
            TokenResponse.class
    );

    @Test
    public void getAccessToken_returnsTheJsonValue() {
        assertEquals("access", token.getAccessToken());
    }

    @Test
    public void getTokenType_returnsTheJsonValue() {
        assertEquals("Bearer", token.getTokenType());
    }

    @Test
    public void getExpiresIn_returnsTheJsonValue() {
        assertEquals(3600, token.getExpiresIn());
    }

    @Test
    public void getRefreshToken_returnsTheJsonValue() {
        assertEquals("refresh", token.getRefreshToken());
    }

    @Test
    public void getRefreshToken_isNullWhenSpotifyDoesNotSendOne() {
        assertNull(new Gson().fromJson("{\"access_token\":\"a\"}", TokenResponse.class).getRefreshToken());
    }

    @Test
    public void getScope_returnsTheJsonValue() {
        assertEquals("a b", token.getScope());
    }
}

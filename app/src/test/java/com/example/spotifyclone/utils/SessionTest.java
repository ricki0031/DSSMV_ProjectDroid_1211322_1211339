package com.example.spotifyclone.utils;

import static com.example.spotifyclone.TestEnvironment.token;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class SessionTest {

    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        Session.clear(context);
    }

    @Test
    public void save_storesAccessAndRefreshTokens() {
        Session.save(context, token("access", "refresh", 3600, "a b"));

        assertEquals("access", Session.getAccessToken(context));
        assertEquals("refresh", Session.getRefreshToken(context));
    }

    @Test
    public void save_keepsRefreshTokenWhenTheNewResponseHasNone() {
        Session.save(context, token("access1", "refresh", 3600, "a"));
        Session.save(context, token("access2", null, 3600, "a"));

        assertEquals("access2", Session.getAccessToken(context));
        assertEquals("refresh", Session.getRefreshToken(context));
    }

    @Test
    public void getAccessToken_isNullWithoutSession() {
        assertNull(Session.getAccessToken(context));
    }

    @Test
    public void getExpiresAt_addsTheTokenLifetimeToTheCurrentTime() {
        long before = System.currentTimeMillis();
        Session.save(context, token("access", "refresh", 3600, "a"));
        long after = System.currentTimeMillis();

        long expiresAt = Session.getExpiresAt(context);
        assertTrue(expiresAt >= before + 3_600_000);
        assertTrue(expiresAt <= after + 3_600_000);
    }

    @Test
    public void millisUntilExpired_subtractsTheOneMinuteMargin() {
        Session.save(context, token("access", "refresh", 3600, "a"));

        long maximum = (3600 - 60) * 1000L;
        long remaining = Session.millisUntilExpired(context);
        assertTrue(remaining <= maximum);
        assertTrue(remaining > maximum - 5000);
    }

    @Test
    public void isExpired_isFalseForNewToken() {
        Session.save(context, token("access", "refresh", 3600, "a"));

        assertFalse(Session.isExpired(context));
    }

    @Test
    public void isExpired_isTrueInsideTheMargin() {
        Session.save(context, token("access", "refresh", 30, "a"));

        assertTrue(Session.isExpired(context));
    }

    @Test
    public void isExpired_isTrueWithoutSession() {
        assertTrue(Session.isExpired(context));
    }

    @Test
    public void hasScopes_isTrueWhenAllScopesWereGranted() {
        Session.save(context, token("access", "refresh", 3600, "a b c"));

        assertTrue(Session.hasScopes(context, "a c"));
    }

    @Test
    public void hasScopes_isFalseWhenOneScopeIsMissing() {
        Session.save(context, token("access", "refresh", 3600, "a b c"));

        assertFalse(Session.hasScopes(context, "a d"));
    }

    @Test
    public void clear_removesTheWholeSession() {
        Session.save(context, token("access", "refresh", 3600, "a"));

        Session.clear(context);

        assertNull(Session.getAccessToken(context));
        assertNull(Session.getRefreshToken(context));
        assertEquals(0L, Session.getExpiresAt(context));
    }
}

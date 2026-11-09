package com.example.spotifyclone.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class PKCEUtilTest {

    private static final String BASE64_URL_WITHOUT_PADDING = "^[A-Za-z0-9_-]+$";

    @Test
    public void generateCodeVerifier_returnsValidPkceFormat() {
        String verifier = PKCEUtil.generateCodeVerifier();

        assertEquals(43, verifier.length());
        assertTrue(verifier.matches(BASE64_URL_WITHOUT_PADDING));
    }

    @Test
    public void generateCodeVerifier_isDifferentEachTime() {
        assertNotEquals(PKCEUtil.generateCodeVerifier(), PKCEUtil.generateCodeVerifier());
    }

    /** Official test vector from the PKCE standard (RFC 7636, Appendix B). */
    @Test
    public void generateCodeChallenge_matchesRfc7636Vector() {
        String challenge = PKCEUtil.generateCodeChallenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk");

        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM", challenge);
    }

    @Test
    public void generateCodeChallenge_isDeterministic() {
        String verifier = PKCEUtil.generateCodeVerifier();

        assertEquals(PKCEUtil.generateCodeChallenge(verifier), PKCEUtil.generateCodeChallenge(verifier));
    }
}

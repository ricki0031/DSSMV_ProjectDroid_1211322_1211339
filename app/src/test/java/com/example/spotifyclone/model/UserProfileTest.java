package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class UserProfileTest {

    @Test
    public void gson_readsAllFields() {
        UserProfile profile = new Gson().fromJson(
                "{\"id\":\"u1\",\"display_name\":\"Ricki\",\"images\":[{\"url\":\"photo\"}]}", UserProfile.class);

        assertEquals("u1", profile.id);
        assertEquals("Ricki", profile.displayName);
        assertEquals("photo", profile.images.get(0).url);
    }

    @Test
    public void gson_leavesOptionalFieldsNullWhenMissing() {
        UserProfile profile = new Gson().fromJson("{\"id\":\"u1\"}", UserProfile.class);

        assertNull(profile.displayName);
        assertNull(profile.images);
    }
}

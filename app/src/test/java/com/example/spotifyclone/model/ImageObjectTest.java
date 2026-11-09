package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class ImageObjectTest {

    @Test
    public void gson_readsAllFields() {
        ImageObject image = new Gson().fromJson(
                "{\"url\":\"cover\",\"height\":640,\"width\":320}", ImageObject.class);

        assertEquals("cover", image.url);
        assertEquals(Integer.valueOf(640), image.height);
        assertEquals(Integer.valueOf(320), image.width);
    }

    @Test
    public void gson_keepsDimensionsNullWhenUnknown() {
        ImageObject image = new Gson().fromJson("{\"url\":\"cover\",\"height\":null}", ImageObject.class);

        assertNull(image.height);
        assertNull(image.width);
    }
}

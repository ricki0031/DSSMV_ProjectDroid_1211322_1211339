package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

public class PageTest {

    @Test
    public void gson_readsPaginationFields() {
        Page<Artist> page = new Gson().fromJson(
                "{\"items\":[{\"id\":\"a1\"}],\"total\":30,\"next\":\"url\",\"offset\":20}",
                new TypeToken<Page<Artist>>() {}.getType()
        );

        assertEquals("a1", page.items.get(0).id);
        assertEquals(30, page.total);
        assertEquals("url", page.next);
        assertEquals(20, page.offset);
    }

    @Test
    public void gson_nextIsNullOnTheLastPage() {
        Page<Track> page = new Gson().fromJson(
                "{\"items\":[],\"next\":null}", new TypeToken<Page<Track>>() {}.getType());

        assertNull(page.next);
        assertEquals(0, page.items.size());
    }
}

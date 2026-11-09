package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class DevicesResponseTest {

    @Test
    public void gson_readsDeviceList() {
        DevicesResponse response = new Gson().fromJson(
                "{\"devices\":[{\"id\":\"d1\"},{\"id\":\"d2\"}]}", DevicesResponse.class);

        assertEquals(2, response.devices.size());
        assertEquals("d2", response.devices.get(1).id);
    }

    @Test
    public void gson_leavesListNullWhenMissing() {
        assertNull(new Gson().fromJson("{}", DevicesResponse.class).devices);
    }
}

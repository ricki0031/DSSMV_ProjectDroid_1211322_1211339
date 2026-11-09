package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

public class DeviceTest {

    @Test
    public void gson_readsAllFields() {
        Device device = new Gson().fromJson(
                "{\"id\":\"d1\",\"is_active\":true,\"is_private_session\":true,"
                        + "\"name\":\"Phone\",\"type\":\"Smartphone\",\"volume_percent\":40}",
                Device.class
        );

        assertEquals("d1", device.id);
        assertTrue(device.isActive);
        assertTrue(device.isPrivateSession);
        assertEquals("Phone", device.name);
        assertEquals("Smartphone", device.type);
        assertEquals(40, device.volumePercent);
    }

    @Test
    public void gson_defaultsToInactive() {
        Device device = new Gson().fromJson("{}", Device.class);

        assertFalse(device.isActive);
        assertNull(device.id);
    }
}

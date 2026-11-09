package com.example.spotifyclone.utils;

import static com.example.spotifyclone.TestEnvironment.runMainThread;
import static com.example.spotifyclone.TestEnvironment.saveValidSession;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

import com.example.spotifyclone.MainActivity;
import com.example.spotifyclone.api.FakeSpotifyApi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;

import java.io.IOException;

import okhttp3.Request;

@RunWith(RobolectricTestRunner.class)
public class PlaylistPickerTest {

    private static final String PLAYLISTS_PATH = "/v1/me/playlists";

    private final FakeSpotifyApi api = new FakeSpotifyApi();
    private MainActivity activity;

    @Before
    public void setUp() {
        api.install();
        saveValidSession(RuntimeEnvironment.getApplication());
        activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        runMainThread();
        api.stub("/v1/me", "{\"id\":\"me\"}");
        api.clearRequests();
    }

    private AlertDialog showPicker() {
        PlaylistPicker.show(activity, "spotify:track:t1", "Song");
        runMainThread();
        return (AlertDialog) ShadowDialog.getLatestDialog();
    }

    @Test
    public void show_listsOnlyOwnedOrCollaborativePlaylists() {
        api.stub(PLAYLISTS_PATH, "{\"items\":["
                + "{\"id\":\"mine\",\"name\":\"Mine\",\"owner\":{\"id\":\"me\"}},"
                + "{\"id\":\"shared\",\"name\":\"Shared\",\"collaborative\":true,\"owner\":{\"id\":\"x\"}},"
                + "{\"id\":\"other\",\"name\":\"Other\",\"owner\":{\"id\":\"x\"}}]}");

        AlertDialog dialog = showPicker();

        assertEquals(2, dialog.getListView().getAdapter().getCount());
        assertEquals("Mine", dialog.getListView().getAdapter().getItem(0));
        assertEquals("Shared", dialog.getListView().getAdapter().getItem(1));
    }

    @Test
    public void show_addsTrackToTheChosenPlaylist() throws IOException {
        api.stub(PLAYLISTS_PATH, "{\"items\":[{\"id\":\"mine\",\"name\":\"Mine\",\"owner\":{\"id\":\"me\"}}]}");
        ListView list = showPicker().getListView();

        list.performItemClick(list.getAdapter().getView(0, null, list), 0, 0);
        runMainThread();

        Request request = api.lastRequestTo("/v1/playlists/mine/items");
        assertNotNull(request);
        assertTrue(FakeSpotifyApi.bodyOf(request).contains("spotify:track:t1"));
        assertEquals("Adicionado a Mine", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void show_warnsWhenThereAreNoEditablePlaylists() {
        api.stub(PLAYLISTS_PATH, "{\"items\":[]}");
        ShadowDialog.reset();

        PlaylistPicker.show(activity, "spotify:track:t1", "Song");
        runMainThread();

        assertNull(ShadowDialog.getLatestDialog());
        assertEquals("Ainda não tens playlists. Cria uma na Library (+).", ShadowToast.getTextOfLatestToast());
    }
}

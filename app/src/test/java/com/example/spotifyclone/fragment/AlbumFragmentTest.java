package com.example.spotifyclone.fragment;

import static com.example.spotifyclone.TestEnvironment.clickRow;
import static com.example.spotifyclone.TestEnvironment.runMainThread;
import static com.example.spotifyclone.TestEnvironment.saveValidSession;
import static com.example.spotifyclone.TestEnvironment.show;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifyclone.MainActivity;
import com.example.spotifyclone.R;
import com.example.spotifyclone.api.FakeSpotifyApi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import java.io.IOException;

@RunWith(RobolectricTestRunner.class)
public class AlbumFragmentTest {

    private static final String ALBUM_PATH = "/v1/albums/al1";
    private static final String ALBUM_JSON = "{\"id\":\"al1\",\"name\":\"LOVED\",\"artists\":[{\"name\":\"Parcels\"}],"
            + "\"tracks\":{\"items\":[{\"id\":\"t1\",\"uri\":\"spotify:track:t1\",\"name\":\"One\"},"
            + "{\"id\":\"t2\",\"uri\":\"spotify:track:t2\",\"name\":\"Two\"}]}}";

    private final FakeSpotifyApi api = new FakeSpotifyApi();
    private MainActivity activity;

    @Before
    public void setUp() {
        api.install();
        saveValidSession(RuntimeEnvironment.getApplication());
        activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        runMainThread();
        api.clearRequests();
        ShadowToast.reset();
    }

    private void openAlbum(String albumId) {
        Bundle args = new Bundle();
        args.putString("ACCESS_TOKEN", "token");
        args.putString("ALBUM_ID", albumId);
        AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(args);
        show(activity, fragment);
    }

    private String text(int id) {
        return ((TextView) activity.findViewById(id)).getText().toString();
    }

    @Test
    public void onCreateView_showsAlbumDetails() {
        api.stub(ALBUM_PATH, ALBUM_JSON);

        openAlbum("al1");

        assertEquals("LOVED", text(R.id.textAlbumName));
        assertEquals("Parcels", text(R.id.textAlbumArtist));
    }

    @Test
    public void onCreateView_listsTheTracks() {
        api.stub(ALBUM_PATH, ALBUM_JSON);

        openAlbum("al1");

        RecyclerView tracks = activity.findViewById(R.id.recyclerViewTracks);
        assertEquals(2, tracks.getAdapter().getItemCount());
    }

    @Test
    public void onCreateView_showsErrorWhenAlbumFails() {
        api.stub(ALBUM_PATH, 429, "{}");

        openAlbum("al1");

        assertEquals("Não foi possível carregar o álbum (429)", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void onCreateView_doesNotRequestWithoutAlbumId() {
        AlbumFragment fragment = new AlbumFragment();
        show(activity, fragment);

        assertNull(api.lastRequestTo(ALBUM_PATH));
    }

    @Test
    public void trackClick_playsTheAlbumFromThatTrack() throws IOException {
        api.stub(ALBUM_PATH, ALBUM_JSON);
        openAlbum("al1");

        clickRow(activity.findViewById(R.id.recyclerViewTracks), 1);

        assertNotNull(api.lastRequestTo("/v1/me/player/play"));
        assertEquals(
                "{\"context_uri\": \"spotify:album:al1\", \"offset\": {\"uri\": \"spotify:track:t2\"}}",
                FakeSpotifyApi.bodyOf(api.lastRequestTo("/v1/me/player/play"))
        );
    }
}

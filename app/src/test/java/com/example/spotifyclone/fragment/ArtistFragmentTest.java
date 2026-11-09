package com.example.spotifyclone.fragment;

import static com.example.spotifyclone.TestEnvironment.clickRow;
import static com.example.spotifyclone.TestEnvironment.currentFragment;
import static com.example.spotifyclone.TestEnvironment.rowText;
import static com.example.spotifyclone.TestEnvironment.runMainThread;
import static com.example.spotifyclone.TestEnvironment.saveValidSession;
import static com.example.spotifyclone.TestEnvironment.show;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
public class ArtistFragmentTest {

    private static final String ARTIST_PATH = "/v1/artists/a1";
    private static final String ALBUMS_PATH = "/v1/artists/a1/albums";
    private static final String PLAY_PATH = "/v1/me/player/play";

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

    private void openArtist(String artistId) {
        Bundle args = new Bundle();
        args.putString(ArtistFragment.ARG_ARTIST_ID, artistId);
        ArtistFragment fragment = new ArtistFragment();
        fragment.setArguments(args);
        show(activity, fragment);
    }

    private RecyclerView albums() {
        return activity.findViewById(R.id.recyclerViewArtistAlbums);
    }

    @Test
    public void onCreateView_showsArtistNameAndGenres() {
        api.stub(ARTIST_PATH, "{\"name\":\"Parcels\",\"genres\":[\"disco\",\"pop\"]}");

        openArtist("a1");

        assertEquals("Parcels", ((TextView) activity.findViewById(R.id.textArtistName)).getText().toString());
        assertEquals("disco, pop", ((TextView) activity.findViewById(R.id.textArtistGenres)).getText().toString());
    }

    @Test
    public void onCreateView_showsErrorWhenArtistFails() {
        api.stub(ARTIST_PATH, 429, "{}");

        openArtist("a1");

        assertEquals("Não foi possível carregar o artista (429)", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void onCreateView_doesNotRequestWithoutArtistId() {
        show(activity, new ArtistFragment());

        assertEquals(0, api.countRequestsTo("/v1/artists/"));
    }

    @Test
    public void onCreateView_showsYearAndTypeOfEachAlbum() {
        api.stub(ALBUMS_PATH, "{\"items\":["
                + "{\"id\":\"al1\",\"name\":\"First\",\"album_type\":\"album\",\"release_date\":\"2024-05-01\"},"
                + "{\"id\":\"al2\",\"name\":\"Second\",\"album_type\":\"single\"}]}");

        openArtist("a1");

        assertEquals("2024 • Álbum", rowText(albums(), 0, R.id.textRowSubtitle));
        assertEquals("Single", rowText(albums(), 1, R.id.textRowSubtitle));
    }

    @Test
    public void albumClick_opensTheAlbum() {
        api.stub(ALBUMS_PATH, "{\"items\":[{\"id\":\"al1\",\"name\":\"First\"}]}");
        openArtist("a1");

        clickRow(albums(), 0);

        assertTrue(currentFragment(activity) instanceof AlbumFragment);
    }

    @Test
    public void playButton_playsTheArtist() throws IOException {
        openArtist("a1");

        activity.findViewById(R.id.btnArtistPlay).performClick();

        assertEquals(
                "{\"context_uri\": \"spotify:artist:a1\"}",
                FakeSpotifyApi.bodyOf(api.lastRequestTo(PLAY_PATH))
        );
    }

    @Test
    public void playButton_showsErrorWhenPlaybackFails() {
        api.stub(PLAY_PATH, 403, "{}");
        openArtist("a1");

        activity.findViewById(R.id.btnArtistPlay).performClick();
        runMainThread();

        assertEquals("Não foi possível tocar (403)", ShadowToast.getTextOfLatestToast());
    }
}

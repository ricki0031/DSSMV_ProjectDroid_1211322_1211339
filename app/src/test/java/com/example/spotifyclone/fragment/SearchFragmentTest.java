package com.example.spotifyclone.fragment;

import static com.example.spotifyclone.TestEnvironment.clickRow;
import static com.example.spotifyclone.TestEnvironment.currentFragment;
import static com.example.spotifyclone.TestEnvironment.runMainThread;
import static com.example.spotifyclone.TestEnvironment.saveValidSession;
import static com.example.spotifyclone.TestEnvironment.show;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
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
import java.time.Duration;

@RunWith(RobolectricTestRunner.class)
public class SearchFragmentTest {

    private static final String SEARCH_PATH = "/v1/search";
    private static final String RESULTS = "{\"tracks\":{\"items\":[{\"id\":\"t1\",\"uri\":\"spotify:track:t1\","
            + "\"name\":\"Song\",\"album\":{\"id\":\"al1\",\"total_tracks\":5}}]},"
            + "\"artists\":{\"items\":[{\"id\":\"a1\",\"name\":\"Artist\"}]},"
            + "\"albums\":{\"items\":[{\"id\":\"al1\",\"name\":\"Album\"}]},"
            + "\"playlists\":{\"items\":[{\"id\":\"p1\",\"name\":\"Playlist\"}]}}";

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
        api.stub(SEARCH_PATH, RESULTS);
    }

    private void openSearch(Bundle args) {
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        show(activity, fragment);
    }

    private static Bundle addToPlaylistArgs() {
        Bundle args = new Bundle();
        args.putString(SearchFragment.ARG_ADD_TO_PLAYLIST_ID, "p1");
        args.putString(SearchFragment.ARG_ADD_TO_PLAYLIST_NAME, "Tryouts");
        return args;
    }

    private EditText input() {
        return activity.findViewById(R.id.editSearch);
    }

    private RecyclerView tracks() {
        return activity.findViewById(R.id.recyclerViewSearchTracks);
    }

    private RecyclerView media() {
        return activity.findViewById(R.id.recyclerViewSearchMedia);
    }

    private static void waitForSearchDelay() {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(350));
    }

    private void search(String query) {
        input().setText(query);
        waitForSearchDelay();
        runMainThread();
    }

    @Test
    public void typing_waitsBeforeSearching() {
        openSearch(null);

        input().setText("rock");
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100));

        assertNull(api.lastRequestTo(SEARCH_PATH));
    }

    @Test
    public void typing_onlySearchesTheLastText() {
        openSearch(null);

        input().setText("ro");
        input().setText("rock");
        waitForSearchDelay();

        assertEquals(1, api.countRequestsTo(SEARCH_PATH));
        assertEquals("rock", api.lastRequestTo(SEARCH_PATH).url().queryParameter("q"));
    }

    @Test
    public void typing_requestsAllResultTypes() {
        openSearch(null);

        search("rock");

        assertEquals("track,artist,album,playlist", api.lastRequestTo(SEARCH_PATH).url().queryParameter("type"));
    }

    @Test
    public void results_fillTracksAndMediaLists() {
        openSearch(null);

        search("rock");

        assertEquals(1, tracks().getAdapter().getItemCount());
        assertEquals(3, media().getAdapter().getItemCount());
    }

    @Test
    public void emptyResults_showTheEmptyMessage() {
        api.stub(SEARCH_PATH, "{\"tracks\":{\"items\":[]}}");
        openSearch(null);

        search("zzzz");

        assertEquals(View.VISIBLE, activity.findViewById(R.id.textSearchEmpty).getVisibility());
    }

    @Test
    public void clearingTheText_clearsResultsWithoutSearching() {
        openSearch(null);
        search("rock");
        api.clearRequests();

        search("");

        assertEquals(0, tracks().getAdapter().getItemCount());
        assertEquals(0, media().getAdapter().getItemCount());
        assertNull(api.lastRequestTo(SEARCH_PATH));
    }

    @Test
    public void typing_hidesGenresAndClearingShowsThem() {
        openSearch(null);
        View genres = activity.findViewById(R.id.layoutExploreGenres);

        input().setText("rock");
        assertEquals(View.GONE, genres.getVisibility());

        input().setText("");
        assertEquals(View.VISIBLE, genres.getVisibility());
    }

    @Test
    public void genreCard_fillsTheSearchText() {
        openSearch(null);

        activity.findViewById(R.id.genreHipHop).performClick();

        assertEquals("hip hop", input().getText().toString());
    }

    @Test
    public void mediaClick_opensTheItem() {
        openSearch(null);
        search("rock");

        clickRow(media(), 0);

        assertTrue(currentFragment(activity) instanceof ArtistFragment);
    }

    @Test
    public void trackClick_playsTheTrackInsideItsAlbum() throws IOException {
        openSearch(null);
        search("rock");

        clickRow(tracks(), 0);

        assertEquals(
                "{\"context_uri\": \"spotify:album:al1\", \"offset\": {\"uri\": \"spotify:track:t1\"}}",
                FakeSpotifyApi.bodyOf(api.lastRequestTo("/v1/me/player/play"))
        );
    }

    @Test
    public void addMode_showsThePlaylistName() {
        openSearch(addToPlaylistArgs());

        assertEquals("Adicionar a Tryouts", ((TextView) activity.findViewById(R.id.textSearchTitle)).getText().toString());
    }

    @Test
    public void addMode_onlySearchesTracks() {
        openSearch(addToPlaylistArgs());

        search("rock");

        assertEquals("track", api.lastRequestTo(SEARCH_PATH).url().queryParameter("type"));
    }

    @Test
    public void addMode_trackClickAddsToThePlaylist() throws IOException {
        openSearch(addToPlaylistArgs());
        search("rock");

        clickRow(tracks(), 0);
        runMainThread();

        assertTrue(FakeSpotifyApi.bodyOf(api.lastRequestTo("/v1/playlists/p1/items")).contains("spotify:track:t1"));
        assertNull(api.lastRequestTo("/v1/me/player/play"));
        assertEquals("Adicionada \"Song\"", ShadowToast.getTextOfLatestToast());
    }
}

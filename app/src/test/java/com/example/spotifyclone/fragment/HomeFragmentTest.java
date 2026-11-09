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

@RunWith(RobolectricTestRunner.class)
public class HomeFragmentTest {

    private static final String ALBUMS_PATH = "/v1/me/albums";
    private static final String TOP_ARTISTS_PATH = "/v1/me/top/artists";
    private static final String PLAYLISTS_PATH = "/v1/me/playlists";
    private static final String RECENT_PATH = "/v1/me/player/recently-played";

    private static final String TWO_ALBUMS = "{\"items\":[{\"album\":{\"id\":\"al1\",\"name\":\"Album1\"}},"
            + "{\"album\":{\"id\":\"al2\",\"name\":\"Album2\"}}]}";
    private static final String TWO_ARTISTS = "{\"items\":[{\"id\":\"a1\",\"name\":\"Artist1\"},"
            + "{\"id\":\"a2\",\"name\":\"Artist2\"}]}";
    private static final String TWO_PLAYLISTS = "{\"items\":[{\"id\":\"p1\",\"name\":\"Playlist1\"},"
            + "{\"id\":\"p2\",\"name\":\"Playlist2\"}]}";

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

    private void openHome(String token) {
        Bundle args = new Bundle();
        args.putString("ACCESS_TOKEN", token);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        show(activity, fragment);
    }

    private RecyclerView list(int id) {
        return activity.findViewById(id);
    }

    @Test
    public void onCreateView_alwaysShowsFavourites() {
        openHome("token");

        assertEquals("Músicas favoritas", rowText(list(R.id.recyclerViewHomeGrid), 0, R.id.textRecentTitle));
    }

    @Test
    public void onCreateView_warnsAndSkipsRequestsWithoutToken() {
        openHome(null);

        assertEquals("Sessão Spotify indisponível", ShadowToast.getTextOfLatestToast());
        assertEquals(0, api.requests().size());
    }

    @Test
    public void onCreateView_makesOneRequestPerSection() {
        openHome("token");

        assertEquals(4, api.requests().size());
        assertEquals(0, api.countRequestsTo("/v1/artists/"));
    }

    @Test
    public void grid_alternatesAlbumsAndArtists() {
        api.stub(ALBUMS_PATH, TWO_ALBUMS);
        api.stub(TOP_ARTISTS_PATH, TWO_ARTISTS);

        openHome("token");

        RecyclerView grid = list(R.id.recyclerViewHomeGrid);
        assertEquals(5, grid.getAdapter().getItemCount());
        assertEquals("Album1", rowText(grid, 1, R.id.textRecentTitle));
        assertEquals("Artist1", rowText(grid, 2, R.id.textRecentTitle));
        assertEquals("Album2", rowText(grid, 3, R.id.textRecentTitle));
        assertEquals("Artist2", rowText(grid, 4, R.id.textRecentTitle));
    }

    @Test
    public void grid_alternatesAlbumsArtistsAndPlaylists() {
        api.stub(ALBUMS_PATH, TWO_ALBUMS);
        api.stub(TOP_ARTISTS_PATH, TWO_ARTISTS);
        api.stub(PLAYLISTS_PATH, TWO_PLAYLISTS);

        openHome("token");

        RecyclerView grid = list(R.id.recyclerViewHomeGrid);
        assertEquals(7, grid.getAdapter().getItemCount());
        assertEquals("Album1", rowText(grid, 1, R.id.textRecentTitle));
        assertEquals("Artist1", rowText(grid, 2, R.id.textRecentTitle));
        assertEquals("Playlist1", rowText(grid, 3, R.id.textRecentTitle));
        assertEquals("Album2", rowText(grid, 4, R.id.textRecentTitle));
        assertEquals("Artist2", rowText(grid, 5, R.id.textRecentTitle));
        assertEquals("Playlist2", rowText(grid, 6, R.id.textRecentTitle));
    }

    @Test
    public void grid_playlistOpensThePlaylist() {
        api.stub(PLAYLISTS_PATH, TWO_PLAYLISTS);
        openHome("token");

        clickRow(list(R.id.recyclerViewHomeGrid), 1);

        assertEquals("p1", currentFragment(activity).getArguments().getString(PlaylistFragment.ARG_PLAYLIST_ID));
    }

    @Test
    public void onCreateView_doesNotRequestPlaylistsTwice() {
        api.stub(PLAYLISTS_PATH, TWO_PLAYLISTS);

        openHome("token");

        assertEquals(1, api.countRequestsTo(PLAYLISTS_PATH));
    }

    @Test
    public void jumpBackIn_includesPlaylists() {
        api.stub(ALBUMS_PATH, TWO_ALBUMS);
        api.stub(TOP_ARTISTS_PATH, TWO_ARTISTS);
        api.stub(PLAYLISTS_PATH, TWO_PLAYLISTS);

        openHome("token");

        RecyclerView jumpBackIn = list(R.id.recyclerViewJumpBackIn);
        assertEquals(6, jumpBackIn.getAdapter().getItemCount());
        assertEquals("Playlist1", rowText(jumpBackIn, 2, R.id.textCardTitle));
    }

    @Test
    public void grid_neverHasMoreThanEightItems() {
        StringBuilder albums = new StringBuilder("{\"items\":[");
        for (int i = 0; i < 12; i++) {
            albums.append(i > 0 ? "," : "").append("{\"album\":{\"id\":\"al").append(i).append("\"}}");
        }
        api.stub(ALBUMS_PATH, albums.append("]}").toString());

        openHome("token");

        assertEquals(8, list(R.id.recyclerViewHomeGrid).getAdapter().getItemCount());
    }

    @Test
    public void grid_favouritesOpensTheFavouritesList() {
        openHome("token");

        clickRow(list(R.id.recyclerViewHomeGrid), 0);

        assertTrue(currentFragment(activity) instanceof PlaylistFragment);
    }

    @Test
    public void grid_artistOpensTheArtist() {
        api.stub(TOP_ARTISTS_PATH, TWO_ARTISTS);
        openHome("token");

        clickRow(list(R.id.recyclerViewHomeGrid), 1);

        assertTrue(currentFragment(activity) instanceof ArtistFragment);
    }

    @Test
    public void jumpBackIn_alternatesAlbumsAndArtists() {
        api.stub(ALBUMS_PATH, TWO_ALBUMS);
        api.stub(TOP_ARTISTS_PATH, TWO_ARTISTS);

        openHome("token");

        RecyclerView jumpBackIn = list(R.id.recyclerViewJumpBackIn);
        assertEquals(4, jumpBackIn.getAdapter().getItemCount());
        assertEquals("Artist1", rowText(jumpBackIn, 1, R.id.textCardTitle));
    }

    @Test
    public void recommended_listsPlaylists() {
        api.stub(PLAYLISTS_PATH, "{\"items\":[{\"id\":\"p1\",\"name\":\"Tryouts\"}]}");

        openHome("token");

        assertEquals("Tryouts", rowText(list(R.id.recyclerViewRecommended), 0, R.id.textCardTitle));
    }

    @Test
    public void recents_showsAlbumsAndKnownArtistsInListeningOrder() {
        api.stub(TOP_ARTISTS_PATH, TWO_ARTISTS);
        api.stub(RECENT_PATH, "{\"items\":["
                + "{\"track\":{\"album\":{\"id\":\"al1\",\"name\":\"Album1\"},\"artists\":[{\"id\":\"a1\"}]}},"
                + "{\"track\":{\"album\":{\"id\":\"al1\",\"name\":\"Album1\"},\"artists\":[{\"id\":\"a1\"}]}},"
                + "{\"track\":{\"album\":{\"id\":\"al2\",\"name\":\"Album2\"},\"artists\":[{\"id\":\"unknown\"}]}}]}");

        openHome("token");

        RecyclerView recents = list(R.id.recyclerViewRecents);
        assertEquals(3, recents.getAdapter().getItemCount());
        assertEquals("Album1", rowText(recents, 0, R.id.textCardTitle));
        assertEquals("Artist1", rowText(recents, 1, R.id.textCardTitle));
        assertEquals("Album2", rowText(recents, 2, R.id.textCardTitle));
    }

    @Test
    public void recents_warnsWhenHistoryFails() {
        api.stub(RECENT_PATH, 429, "{}");

        openHome("token");

        assertEquals("Recents: erro HTTP 429", ShadowToast.getTextOfLatestToast());
    }
}

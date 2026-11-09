package com.example.spotifyclone.fragment;

import static com.example.spotifyclone.TestEnvironment.clickRow;
import static com.example.spotifyclone.TestEnvironment.currentFragment;
import static com.example.spotifyclone.TestEnvironment.layoutList;
import static com.example.spotifyclone.TestEnvironment.runMainThread;
import static com.example.spotifyclone.TestEnvironment.saveValidSession;
import static com.example.spotifyclone.TestEnvironment.show;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
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
import org.robolectric.shadows.ShadowPopupMenu;
import org.robolectric.shadows.ShadowToast;

import java.io.IOException;

@RunWith(RobolectricTestRunner.class)
public class PlaylistFragmentTest {

    private static final String PROFILE_PATH = "/v1/me";
    private static final String PLAYLIST_PATH = "/v1/playlists/p1";
    private static final String ITEMS_PATH = "/v1/playlists/p1/items";
    private static final String SAVED_PATH = "/v1/me/tracks";
    private static final String PLAY_PATH = "/v1/me/player/play";

    private static final String TWO_TRACKS = "{\"items\":["
            + "{\"item\":{\"id\":\"t1\",\"uri\":\"spotify:track:t1\",\"name\":\"One\"}},"
            + "{\"item\":{\"id\":\"t2\",\"uri\":\"spotify:track:t2\",\"name\":\"Two\"}}],\"total\":2}";
    private static final String TWO_SAVED = "{\"items\":["
            + "{\"track\":{\"id\":\"t1\",\"uri\":\"spotify:track:t1\",\"name\":\"One\"}},"
            + "{\"track\":{\"id\":\"t2\",\"uri\":\"spotify:track:t2\",\"name\":\"Two\"}}],\"total\":2}";

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
        api.stub(PROFILE_PATH, "{\"id\":\"me\"}");
    }

    private static String playlistJson(String ownerId) {
        return "{\"id\":\"p1\",\"name\":\"Tryouts\",\"snapshot_id\":\"s1\","
                + "\"owner\":{\"id\":\"" + ownerId + "\",\"display_name\":\"Ricki\"},\"items\":{\"total\":2}}";
    }

    private void openPlaylist() {
        Bundle args = new Bundle();
        args.putString(PlaylistFragment.ARG_PLAYLIST_ID, "p1");
        PlaylistFragment fragment = new PlaylistFragment();
        fragment.setArguments(args);
        show(activity, fragment);
    }

    private void openLikedSongs() {
        Bundle args = new Bundle();
        args.putBoolean(PlaylistFragment.ARG_LIKED_SONGS, true);
        PlaylistFragment fragment = new PlaylistFragment();
        fragment.setArguments(args);
        show(activity, fragment);
    }

    private String text(int id) {
        return ((TextView) activity.findViewById(id)).getText().toString();
    }

    private RecyclerView tracks() {
        return activity.findViewById(R.id.recyclerViewPlaylistTracks);
    }

    private PopupMenu openTrackMenu() {
        RecyclerView list = tracks();
        if (list.getLayoutManager() == null) {
            list.setLayoutManager(new LinearLayoutManager(activity));
        }
        layoutList(list);
        list.findViewHolderForAdapterPosition(0).itemView.findViewById(R.id.btnTrackMore).performClick();
        return ShadowPopupMenu.getLatestPopupMenu();
    }

    private void chooseLastMenuOption(PopupMenu menu) {
        shadowOf(menu).getOnMenuItemClickListener().onMenuItemClick(menu.getMenu().findItem(4));
        runMainThread();
    }

    @Test
    public void onCreateView_likedSongsUsesPortugueseTitle() {
        api.stub(SAVED_PATH, TWO_SAVED);

        openLikedSongs();

        assertEquals("Músicas favoritas", text(R.id.textPlaylistName));
        assertEquals("Tu • 2 músicas", text(R.id.textPlaylistMeta));
        assertEquals(2, tracks().getAdapter().getItemCount());
    }

    @Test
    public void onCreateView_showsPlaylistHeader() {
        api.stub(PLAYLIST_PATH, playlistJson("me"));
        api.stub(ITEMS_PATH, TWO_TRACKS);

        openPlaylist();

        assertEquals("Tryouts", text(R.id.textPlaylistName));
        assertEquals("Ricki • 2 músicas", text(R.id.textPlaylistMeta));
    }

    @Test
    public void onCreateView_usesSingularForOneSong() {
        api.stub(PLAYLIST_PATH, playlistJson("me"));
        api.stub(ITEMS_PATH, "{\"items\":[{\"item\":{\"id\":\"t1\"}}],\"total\":1}");

        openPlaylist();

        assertEquals("Ricki • 1 música", text(R.id.textPlaylistMeta));
    }

    @Test
    public void onCreateView_ownerCanEdit() {
        api.stub(PLAYLIST_PATH, playlistJson("me"));

        openPlaylist();

        assertEquals(View.VISIBLE, activity.findViewById(R.id.btnPlaylistEdit).getVisibility());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.btnPlaylistAddSongs).getVisibility());
    }

    @Test
    public void onCreateView_otherUsersCannotEdit() {
        api.stub(PLAYLIST_PATH, playlistJson("someone"));

        openPlaylist();

        assertEquals(View.GONE, activity.findViewById(R.id.btnPlaylistEdit).getVisibility());
        assertEquals(View.GONE, activity.findViewById(R.id.btnPlaylistAddSongs).getVisibility());
    }

    @Test
    public void onCreateView_explainsWhenPlaylistIsUnavailable() {
        api.stub(PLAYLIST_PATH, 404, "{}");

        openPlaylist();

        assertEquals("Playlist indisponível", text(R.id.textPlaylistName));
        assertEquals(
                "O Spotify já não disponibiliza esta playlist a apps em modo de desenvolvimento.",
                text(R.id.textPlaylistEmpty)
        );
    }

    @Test
    public void onCreateView_explainsWhenTracksAreForbidden() {
        api.stub(PLAYLIST_PATH, playlistJson("someone"));
        api.stub(ITEMS_PATH, 403, "{}");

        openPlaylist();

        assertEquals(
                "A Web API do Spotify só mostra as músicas de playlists que criaste ou onde colaboras.",
                text(R.id.textPlaylistEmpty)
        );
    }

    @Test
    public void onCreateView_invitesOwnerToAddSongsToEmptyPlaylist() {
        api.stub(PLAYLIST_PATH, playlistJson("me"));
        api.stub(ITEMS_PATH, "{\"items\":[],\"total\":0}");

        openPlaylist();

        assertEquals("Esta playlist ainda não tem músicas. Toca no + para adicionar.", text(R.id.textPlaylistEmpty));
    }

    @Test
    public void trackClick_playsPlaylistFromThatPosition() throws IOException {
        api.stub(PLAYLIST_PATH, playlistJson("me"));
        api.stub(ITEMS_PATH, TWO_TRACKS);
        openPlaylist();

        clickRow(tracks(), 1);

        assertEquals(
                "{\"context_uri\": \"spotify:playlist:p1\", \"offset\": {\"position\": 1}}",
                FakeSpotifyApi.bodyOf(api.lastRequestTo(PLAY_PATH))
        );
    }

    @Test
    public void trackClick_playsFavouritesAsAListOfTracks() throws IOException {
        api.stub(SAVED_PATH, TWO_SAVED);
        openLikedSongs();

        clickRow(tracks(), 1);

        assertEquals(
                "{\"uris\": [\"spotify:track:t1\", \"spotify:track:t2\"], \"offset\": {\"position\": 1}}",
                FakeSpotifyApi.bodyOf(api.lastRequestTo(PLAY_PATH))
        );
    }

    @Test
    public void playButton_playsFromTheFirstTrack() throws IOException {
        api.stub(PLAYLIST_PATH, playlistJson("me"));
        api.stub(ITEMS_PATH, TWO_TRACKS);
        openPlaylist();

        activity.findViewById(R.id.btnPlaylistPlay).performClick();

        assertEquals(
                "{\"context_uri\": \"spotify:playlist:p1\", \"offset\": {\"position\": 0}}",
                FakeSpotifyApi.bodyOf(api.lastRequestTo(PLAY_PATH))
        );
    }

    @Test
    public void trackMenu_ownerCanRemoveTrackFromPlaylist() throws IOException {
        api.stub(PLAYLIST_PATH, playlistJson("me"));
        api.stub(ITEMS_PATH, TWO_TRACKS);
        openPlaylist();

        chooseLastMenuOption(openTrackMenu());

        assertTrue(FakeSpotifyApi.bodyOf(api.lastRequestTo(ITEMS_PATH)).contains("spotify:track:t1"));
        assertEquals(1, tracks().getAdapter().getItemCount());
        assertEquals("Removido da playlist", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void trackMenu_canRemoveTrackFromFavourites() {
        api.stub(SAVED_PATH, TWO_SAVED);
        openLikedSongs();

        chooseLastMenuOption(openTrackMenu());

        assertNotNull(api.lastRequestTo("/v1/me/library"));
        assertEquals("DELETE", api.lastRequestTo("/v1/me/library").method());
        assertEquals(1, tracks().getAdapter().getItemCount());
        assertEquals("Removido das Músicas favoritas", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void addSongsButton_opensSearchInAddMode() {
        api.stub(PLAYLIST_PATH, playlistJson("me"));
        openPlaylist();

        activity.findViewById(R.id.btnPlaylistAddSongs).performClick();

        assertTrue(currentFragment(activity) instanceof SearchFragment);
        assertEquals("p1", currentFragment(activity).getArguments().getString(SearchFragment.ARG_ADD_TO_PLAYLIST_ID));
    }
}

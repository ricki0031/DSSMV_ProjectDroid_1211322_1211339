package com.example.spotifyclone.fragment;

import static com.example.spotifyclone.TestEnvironment.clickRow;
import static com.example.spotifyclone.TestEnvironment.currentFragment;
import static com.example.spotifyclone.TestEnvironment.runMainThread;
import static com.example.spotifyclone.TestEnvironment.saveValidSession;
import static com.example.spotifyclone.TestEnvironment.show;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifyclone.MainActivity;
import com.example.spotifyclone.R;
import com.example.spotifyclone.adapter.AlbumAdapter;
import com.example.spotifyclone.adapter.MediaCardAdapter;
import com.example.spotifyclone.api.FakeSpotifyApi;
import com.example.spotifyclone.customization.WallpaperStyle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;

import java.io.IOException;

import okhttp3.Request;

@RunWith(RobolectricTestRunner.class)
public class LibraryFragmentTest {

    private static final String PLAYLISTS_PATH = "/v1/me/playlists";
    private static final String ALBUMS_PATH = "/v1/me/albums";

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
        api.stub(PLAYLISTS_PATH, "{\"items\":[{\"id\":\"p1\",\"name\":\"Tryouts\"}]}");
        api.stub(ALBUMS_PATH, "{\"items\":[{\"album\":{\"id\":\"al1\",\"name\":\"LOVED\"}}]}");
    }

    private void openLibrary() {
        Bundle args = new Bundle();
        args.putString("ACCESS_TOKEN", "token");
        LibraryFragment fragment = new LibraryFragment();
        fragment.setArguments(args);
        show(activity, fragment);
    }

    private RecyclerView playlists() {
        return activity.findViewById(R.id.recyclerViewPlaylists);
    }

    private RecyclerView albums() {
        return activity.findViewById(R.id.recyclerViewLibrary);
    }

    private void click(int id) {
        activity.findViewById(id).performClick();
        runMainThread();
    }

    private int tintOf(int id) {
        return activity.findViewById(id).getBackgroundTintList().getDefaultColor();
    }

    @Test
    public void onCreateView_startsWithPlaylistRows() {
        openLibrary();

        assertEquals(View.VISIBLE, playlists().getVisibility());
        assertEquals(View.GONE, albums().getVisibility());
        assertEquals(1, playlists().getAdapter().getItemCount());
        assertFalse(playlists().getLayoutManager() instanceof GridLayoutManager);
    }

    @Test
    public void onCreateView_usesWallpaperColorsOnButtons() {
        openLibrary();

        assertEquals(WallpaperStyle.BUTTON_ACTIVE_COLOR, tintOf(R.id.buttonFilterPlaylists));
        assertEquals(WallpaperStyle.BUTTON_INACTIVE_COLOR, tintOf(R.id.buttonFilterAlbums));
        assertEquals(WallpaperStyle.BUTTON_ACTIVE_COLOR, tintOf(R.id.buttonCreatePlaylist));
    }

    @Test
    public void viewModeButton_togglesPlaylistsBetweenRowsAndGrid() {
        openLibrary();

        click(R.id.buttonLibraryViewMode);
        assertTrue(playlists().getLayoutManager() instanceof GridLayoutManager);

        click(R.id.buttonLibraryViewMode);
        assertFalse(playlists().getLayoutManager() instanceof GridLayoutManager);
    }

    @Test
    public void albumsFilter_showsAlbumsInAGridAndSwapsColors() {
        openLibrary();

        click(R.id.buttonFilterAlbums);

        assertEquals(View.VISIBLE, albums().getVisibility());
        assertEquals(View.GONE, playlists().getVisibility());
        assertTrue(albums().getLayoutManager() instanceof GridLayoutManager);
        assertEquals(WallpaperStyle.BUTTON_ACTIVE_COLOR, tintOf(R.id.buttonFilterAlbums));
        assertEquals(WallpaperStyle.BUTTON_INACTIVE_COLOR, tintOf(R.id.buttonFilterPlaylists));
    }

    @Test
    public void viewModeButton_cyclesAlbumsThroughGridLargeAndList() {
        openLibrary();
        click(R.id.buttonFilterAlbums);

        click(R.id.buttonLibraryViewMode);
        assertFalse(albums().getLayoutManager() instanceof GridLayoutManager);
        assertTrue(albums().getAdapter() instanceof AlbumAdapter);

        click(R.id.buttonLibraryViewMode);
        assertTrue(albums().getAdapter() instanceof MediaCardAdapter);

        click(R.id.buttonLibraryViewMode);
        assertTrue(albums().getLayoutManager() instanceof GridLayoutManager);
        assertTrue(albums().getAdapter() instanceof AlbumAdapter);
    }

    @Test
    public void albumGridClick_opensTheAlbum() {
        openLibrary();
        click(R.id.buttonFilterAlbums);

        clickRow(albums(), 0);

        assertTrue(currentFragment(activity) instanceof AlbumFragment);
    }

    @Test
    public void albumListRowClick_opensTheAlbum() {
        openLibrary();
        click(R.id.buttonFilterAlbums);
        click(R.id.buttonLibraryViewMode);
        click(R.id.buttonLibraryViewMode);

        clickRow(albums(), 0);

        assertTrue(currentFragment(activity) instanceof AlbumFragment);
    }

    @Test
    public void playlistClick_opensThePlaylist() {
        openLibrary();

        clickRow(playlists(), 0);

        assertTrue(currentFragment(activity) instanceof PlaylistFragment);
    }

    @Test
    public void likedSongsButton_opensTheFavouritesList() {
        openLibrary();

        click(R.id.buttonLikedSongs);

        assertTrue(currentFragment(activity) instanceof PlaylistFragment);
    }

    @Test
    public void onCreateView_warnsWhenPlaylistsFail() {
        api.stub(ALBUMS_PATH, "{}");
        api.stub(PLAYLISTS_PATH, 401, "{}");

        openLibrary();

        assertEquals("Erro ao carregar playlists: HTTP 401", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void onCreateView_warnsWhenAlbumsFail() {
        api.stub(ALBUMS_PATH, 429, "{}");

        openLibrary();
        runMainThread();

        assertTrue(ShadowToast.showedToast("Erro ao carregar álbuns: HTTP 429"));
    }

    private AlertDialog openCreateDialog(String name) {
        openLibrary();
        click(R.id.buttonCreatePlaylist);
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ((EditText) shadowOf(dialog).getView()).setText(name);
        return dialog;
    }

    @Test
    public void createPlaylist_sendsTheName() throws IOException {
        AlertDialog dialog = openCreateDialog("My list");

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        runMainThread();

        Request createRequest = null;
        for (Request request : api.requests()) {
            if ("POST".equals(request.method())) {
                createRequest = request;
            }
        }
        assertNotNull(createRequest);
        assertTrue(FakeSpotifyApi.bodyOf(createRequest).contains("My list"));
        assertEquals("Playlist criada com sucesso!", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void createPlaylist_requiresAName() {
        AlertDialog dialog = openCreateDialog("   ");
        api.clearRequests();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        runMainThread();

        assertNull(api.lastRequestTo(PLAYLISTS_PATH));
        assertEquals("Introduza um nome para a playlist", ShadowToast.getTextOfLatestToast());
    }
}

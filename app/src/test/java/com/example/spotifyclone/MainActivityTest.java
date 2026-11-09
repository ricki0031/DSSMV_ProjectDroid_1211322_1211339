package com.example.spotifyclone;

import static com.example.spotifyclone.TestEnvironment.ACCESS_TOKEN;
import static com.example.spotifyclone.TestEnvironment.currentFragment;
import static com.example.spotifyclone.TestEnvironment.runMainThread;
import static com.example.spotifyclone.TestEnvironment.saveExpiredSession;
import static com.example.spotifyclone.TestEnvironment.saveValidSession;
import static com.example.spotifyclone.TestEnvironment.setInternetAvailable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.example.spotifyclone.api.FakeSpotifyApi;
import com.example.spotifyclone.fragment.AlbumFragment;
import com.example.spotifyclone.fragment.ArtistFragment;
import com.example.spotifyclone.fragment.HomeFragment;
import com.example.spotifyclone.fragment.LibraryFragment;
import com.example.spotifyclone.fragment.PlaylistFragment;
import com.example.spotifyclone.fragment.SearchFragment;
import com.example.spotifyclone.model.Album;
import com.example.spotifyclone.model.MediaItem;
import com.example.spotifyclone.model.Track;
import com.example.spotifyclone.utils.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowPopupMenu;
import org.robolectric.shadows.ShadowToast;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    private static final String TOKEN_PATH = "/api/token";
    private static final String PLAY_PATH = "/v1/me/player/play";
    private static final String NEW_TOKEN_JSON = "{\"access_token\":\"new-token\",\"expires_in\":3600,"
            + "\"scope\":\"" + TestEnvironment.ALL_SCOPES + "\"}";

    private final Context context = RuntimeEnvironment.getApplication();
    private final FakeSpotifyApi api = new FakeSpotifyApi();

    @Before
    public void setUp() {
        api.install();
        Session.clear(context);
        ShadowToast.reset();
    }

    private MainActivity launch() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        runMainThread();
        return activity;
    }

    private MainActivity launchLoggedIn() {
        saveValidSession(context);
        setInternetAvailable(context);
        return launch();
    }

    private static Track track(int albumTracks) {
        Track track = new Track();
        track.id = "t1";
        track.uri = "spotify:track:t1";
        track.name = "Song";
        track.album = new Album();
        track.album.id = "al1";
        track.album.totalTracks = albumTracks;
        return track;
    }

    private static MediaItem item(String type) {
        MediaItem item = new MediaItem();
        item.type = type;
        item.id = "id1";
        return item;
    }

    private static String text(FragmentActivity activity, int id) {
        return ((TextView) activity.findViewById(id)).getText().toString();
    }

    @Test
    public void onCreate_withoutInternetWarnsTheUser() {
        saveValidSession(context);

        launch();

        assertEquals(
                "Sem ligação à Internet. Verifica o Wi-Fi ou os dados móveis.",
                ShadowToast.getTextOfLatestToast()
        );
    }

    @Test
    public void onCreate_withValidSessionOpensHome() {
        MainActivity activity = launchLoggedIn();

        assertTrue(currentFragment(activity) instanceof HomeFragment);
    }

    @Test
    public void onCreate_withoutSessionOpensSpotifyLogin() {
        setInternetAvailable(context);

        MainActivity activity = launch();

        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertEquals(Intent.ACTION_VIEW, intent.getAction());
        assertEquals("accounts.spotify.com", intent.getData().getHost());
        assertEquals("S256", intent.getData().getQueryParameter("code_challenge_method"));
        assertNotNull(context.getSharedPreferences(MainActivity.SHARED_PREFS, Context.MODE_PRIVATE)
                .getString(MainActivity.CODE_VERIFIER_KEY, null));
    }

    @Test
    public void onCreate_withExpiredSessionRefreshesTheToken() {
        saveExpiredSession(context);
        setInternetAvailable(context);
        api.stub(TOKEN_PATH, NEW_TOKEN_JSON);

        MainActivity activity = launch();

        assertEquals("new-token", Session.getAccessToken(context));
        assertTrue(currentFragment(activity) instanceof HomeFragment);
    }

    @Test
    public void onCreate_failedRefreshOpensSpotifyLogin() {
        saveExpiredSession(context);
        setInternetAvailable(context);
        api.stub(TOKEN_PATH, 400, "{}");

        MainActivity activity = launch();

        assertEquals("accounts.spotify.com", shadowOf(activity).getNextStartedActivity().getData().getHost());
    }

    @Test
    public void onResume_refreshesAnExpiredToken() {
        saveValidSession(context);
        setInternetAvailable(context);
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup();
        runMainThread();
        controller.pause();
        saveExpiredSession(context);
        api.stub(TOKEN_PATH, NEW_TOKEN_JSON);

        controller.resume();
        runMainThread();

        assertEquals("new-token", Session.getAccessToken(context));
    }

    @Test
    public void tokenTimer_refreshesBeforeExpiring() {
        launchLoggedIn();
        api.stub(TOKEN_PATH, NEW_TOKEN_JSON);

        shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofMinutes(59));
        runMainThread();

        assertEquals("new-token", Session.getAccessToken(context));
    }

    @Test
    public void authRedirect_exchangesTheCodeForAToken() {
        context.getSharedPreferences(MainActivity.SHARED_PREFS, Context.MODE_PRIVATE)
                .edit().putString(MainActivity.CODE_VERIFIER_KEY, "verifier").commit();
        api.stub(TOKEN_PATH, NEW_TOKEN_JSON);
        setInternetAvailable(context);

        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).create();
        Intent redirect = new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.REDIRECT_URI + "?code=abc"));
        controller.newIntent(redirect);
        runMainThread();

        assertEquals("new-token", Session.getAccessToken(context));
    }

    @Test
    public void newIntent_opensTheRequestedAlbum() {
        saveValidSession(context);
        setInternetAvailable(context);
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup();
        runMainThread();

        Intent intent = new Intent().putExtra(MainActivity.EXTRA_OPEN_ALBUM_ID, "al1");
        controller.newIntent(intent);

        assertTrue(currentFragment(controller.get()) instanceof AlbumFragment);
    }

    @Test
    public void bottomNavigation_opensEveryTab() {
        MainActivity activity = launchLoggedIn();
        BottomNavigationView navigation = activity.findViewById(R.id.bottom_navigation);

        navigation.setSelectedItemId(R.id.nav_search);
        assertTrue(currentFragment(activity) instanceof SearchFragment);

        navigation.setSelectedItemId(R.id.nav_library);
        assertTrue(currentFragment(activity) instanceof LibraryFragment);

        navigation.setSelectedItemId(R.id.nav_home);
        assertTrue(currentFragment(activity) instanceof HomeFragment);
    }

    @Test
    public void bottomNavigation_clearsTheHistoryOfThePreviousTab() {
        MainActivity activity = launchLoggedIn();
        activity.openMedia(item(MediaItem.TYPE_ALBUM));
        currentFragment(activity);
        assertEquals(1, activity.getSupportFragmentManager().getBackStackEntryCount());

        ((BottomNavigationView) activity.findViewById(R.id.bottom_navigation)).setSelectedItemId(R.id.nav_library);
        currentFragment(activity);

        assertEquals(0, activity.getSupportFragmentManager().getBackStackEntryCount());
    }

    @Test
    public void openMedia_opensTheScreenOfEachType() {
        MainActivity activity = launchLoggedIn();

        activity.openMedia(item(MediaItem.TYPE_ALBUM));
        assertTrue(currentFragment(activity) instanceof AlbumFragment);

        activity.openMedia(item(MediaItem.TYPE_ARTIST));
        assertTrue(currentFragment(activity) instanceof ArtistFragment);

        activity.openMedia(item(MediaItem.TYPE_PLAYLIST));
        assertTrue(currentFragment(activity) instanceof PlaylistFragment);

        activity.openMedia(MediaItem.likedSongs());
        assertTrue(currentFragment(activity).getArguments().getBoolean(PlaylistFragment.ARG_LIKED_SONGS));
    }

    @Test
    public void openMedia_ignoresNull() {
        MainActivity activity = launchLoggedIn();

        activity.openMedia(null);

        assertTrue(currentFragment(activity) instanceof HomeFragment);
    }

    @Test
    public void playTrack_usesTheAlbumWhenItHasMoreTracks() throws IOException {
        MainActivity activity = launchLoggedIn();
        Track track = track(5);

        activity.playTrack(track, Collections.singletonList(track));

        assertEquals(
                "{\"context_uri\": \"spotify:album:al1\", \"offset\": {\"uri\": \"spotify:track:t1\"}}",
                FakeSpotifyApi.bodyOf(api.lastRequestTo(PLAY_PATH))
        );
    }

    @Test
    public void playTrack_usesTheListForSingleTrackAlbums() throws IOException {
        MainActivity activity = launchLoggedIn();
        Track first = track(1);
        Track second = track(1);
        second.uri = "spotify:track:t2";

        activity.playTrack(second, Arrays.asList(first, second));

        assertEquals(
                "{\"uris\": [\"spotify:track:t1\", \"spotify:track:t2\"], \"offset\": {\"position\": 1}}",
                FakeSpotifyApi.bodyOf(api.lastRequestTo(PLAY_PATH))
        );
    }

    @Test
    public void playTrack_ignoresNull() {
        MainActivity activity = launchLoggedIn();

        activity.playTrack(null, Collections.emptyList());

        assertNull(api.lastRequestTo(PLAY_PATH));
    }

    @Test
    public void playTrack_showsErrorWhenPlaybackFails() {
        MainActivity activity = launchLoggedIn();
        api.stub(PLAY_PATH, 403, "{}");
        Track track = track(5);

        activity.playTrack(track, Collections.singletonList(track));
        runMainThread();

        assertEquals("Erro ao tocar: HTTP 403", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void updateMiniPlayerInstantly_showsTitleAndArtist() {
        MainActivity activity = launchLoggedIn();

        activity.updateMiniPlayerInstantly("t1", "Song", "Parcels", null);

        assertEquals("Song", text(activity, R.id.miniPlayerTitle));
        assertEquals("Parcels", text(activity, R.id.miniPlayerArtist));
    }

    @Test
    public void syncMiniPlayer_showsTheCurrentTrack() {
        MainActivity activity = launchLoggedIn();
        api.stub("/v1/me/player/currently-playing",
                "{\"is_playing\":true,\"item\":{\"id\":\"t1\",\"name\":\"Now\",\"artists\":[{\"name\":\"Parcels\"}]}}");

        activity.syncMiniPlayer(ACCESS_TOKEN);
        runMainThread();

        assertEquals("Now", text(activity, R.id.miniPlayerTitle));
        assertEquals("Parcels", text(activity, R.id.miniPlayerArtist));
    }

    @Test
    public void fetchActiveDevice_prefersTheActiveDevice() {
        MainActivity activity = launchLoggedIn();
        activity.globalDeviceId = "";
        api.stub("/v1/me/player/devices", "{\"devices\":[{\"id\":\"d1\"},{\"id\":\"d2\",\"is_active\":true}]}");

        activity.fetchActiveDevice(ACCESS_TOKEN);
        runMainThread();

        assertEquals("d2", activity.globalDeviceId);
    }

    @Test
    public void fetchActiveDevice_fallsBackToTheFirstDevice() {
        MainActivity activity = launchLoggedIn();
        activity.globalDeviceId = "";
        api.stub("/v1/me/player/devices", "{\"devices\":[{\"id\":\"d1\"},{\"id\":\"d2\"}]}");

        activity.fetchActiveDevice(ACCESS_TOKEN);
        runMainThread();

        assertEquals("d1", activity.globalDeviceId);
    }

    @Test
    public void miniPlayer_opensThePlayer() {
        MainActivity activity = launchLoggedIn();
        activity.globalDeviceId = "d1";

        activity.findViewById(R.id.mini_player).performClick();

        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertEquals(PlayerActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals("d1", intent.getStringExtra("DEVICE_ID"));
    }

    @Test
    public void miniPlayerButton_pausesWhenPlaying() {
        MainActivity activity = launchLoggedIn();
        activity.updateMiniPlayerInstantly("t1", "Song", "Parcels", null);

        activity.findViewById(R.id.miniPlayerPlayPause).performClick();

        assertEquals("PUT", api.lastRequestTo("/v1/me/player/pause").method());
    }

    @Test
    public void showTrackMenu_hasThreeOptionsByDefault() {
        MainActivity activity = launchLoggedIn();

        activity.showTrackMenu(new View(activity), track(5), null, null);

        assertEquals(3, ShadowPopupMenu.getLatestPopupMenu().getMenu().size());
    }

    @Test
    public void showTrackMenu_addsTheExtraOption() {
        MainActivity activity = launchLoggedIn();
        boolean[] ran = {false};

        activity.showTrackMenu(new View(activity), track(5), "Extra", () -> ran[0] = true);
        PopupMenu menu = ShadowPopupMenu.getLatestPopupMenu();
        shadowOf(menu).getOnMenuItemClickListener().onMenuItemClick(menu.getMenu().findItem(4));

        assertEquals(4, menu.getMenu().size());
        assertTrue(ran[0]);
    }

    @Test
    public void showTrackMenu_savesToFavourites() {
        MainActivity activity = launchLoggedIn();

        activity.showTrackMenu(new View(activity), track(5), null, null);
        PopupMenu menu = ShadowPopupMenu.getLatestPopupMenu();
        shadowOf(menu).getOnMenuItemClickListener().onMenuItemClick(menu.getMenu().findItem(2));
        runMainThread();

        assertEquals("PUT", api.lastRequestTo("/v1/me/library").method());
        assertEquals("Guardado nas Músicas favoritas", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void showTrackMenu_addsToTheQueue() {
        MainActivity activity = launchLoggedIn();

        activity.showTrackMenu(new View(activity), track(5), null, null);
        PopupMenu menu = ShadowPopupMenu.getLatestPopupMenu();
        shadowOf(menu).getOnMenuItemClickListener().onMenuItemClick(menu.getMenu().findItem(3));
        runMainThread();

        assertEquals("POST", api.lastRequestTo("/v1/me/player/queue").method());
        assertEquals("Adicionado à fila", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void showTrackMenu_ignoresNullTrack() {
        MainActivity activity = launchLoggedIn();
        PopupMenu before = ShadowPopupMenu.getLatestPopupMenu();

        activity.showTrackMenu(new View(activity), null, null, null);

        assertSame(before, ShadowPopupMenu.getLatestPopupMenu());
    }
}

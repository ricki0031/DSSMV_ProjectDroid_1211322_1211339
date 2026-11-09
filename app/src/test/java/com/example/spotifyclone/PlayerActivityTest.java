package com.example.spotifyclone;

import static com.example.spotifyclone.TestEnvironment.runMainThread;
import static com.example.spotifyclone.TestEnvironment.saveValidSession;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.spotifyclone.api.FakeSpotifyApi;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;

import java.io.IOException;
import java.time.Duration;

import okhttp3.Request;

@RunWith(RobolectricTestRunner.class)
public class PlayerActivityTest {

    private static final String STATE_PATH = "/v1/me/player";
    private static final String QUEUE_PATH = "/v1/me/player/queue";
    private static final String LIBRARY_PATH = "/v1/me/library";
    private static final String CONTAINS_PATH = "/v1/me/library/contains";

    private final FakeSpotifyApi api = new FakeSpotifyApi();
    private ActivityController<PlayerActivity> controller;
    private PlayerActivity activity;

    @Before
    public void setUp() {
        api.install();
        saveValidSession(RuntimeEnvironment.getApplication());
        api.stub(CONTAINS_PATH, "[false]");
        ShadowToast.reset();
        ShadowDialog.reset();
    }

    private static String state(boolean playing, boolean shuffle, String repeat, String context) {
        String contextJson = context == null ? "null" : "{\"uri\":\"" + context + "\"}";
        return "{\"is_playing\":" + playing + ",\"shuffle_state\":" + shuffle + ",\"repeat_state\":\"" + repeat + "\","
                + "\"progress_ms\":10000,\"device\":{\"id\":\"d2\",\"name\":\"Web Player\"},\"context\":" + contextJson + ","
                + "\"item\":{\"id\":\"t1\",\"uri\":\"spotify:track:t1\",\"name\":\"Song\",\"duration_ms\":100000,"
                + "\"artists\":[{\"name\":\"Parcels\"}],\"album\":{\"id\":\"al1\",\"name\":\"LOVED\"}}}";
    }

    private void open(String stateJson) {
        api.stub(STATE_PATH, stateJson);
        Intent intent = new Intent(RuntimeEnvironment.getApplication(), PlayerActivity.class)
                .putExtra("DEVICE_ID", "d1");
        controller = Robolectric.buildActivity(PlayerActivity.class, intent).setup();
        activity = controller.get();
        runMainThread();
    }

    private void openPlaying() {
        open(state(true, false, "off", null));
    }

    private String text(int id) {
        return ((TextView) activity.findViewById(id)).getText().toString();
    }

    private void click(int id) {
        activity.findViewById(id).performClick();
        runMainThread();
    }

    private static void waitMillis(long millis) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis));
    }

    private BottomSheetDialog openOptions() {
        click(R.id.btnPlayerMore);
        return (BottomSheetDialog) ShadowDialog.getLatestDialog();
    }

    private void chooseOption(BottomSheetDialog sheet, int optionId) {
        sheet.findViewById(optionId).performClick();
        runMainThread();
    }

    @Test
    public void fetchPlaybackState_showsTheTrack() {
        openPlaying();

        assertEquals("Song", text(R.id.textPlayerTitle));
        assertEquals("Parcels", text(R.id.textPlayerArtist));
        assertEquals("LOVED", text(R.id.textPlayingFrom));
        assertEquals("A tocar em Web Player", text(R.id.textDevice));
    }

    @Test
    public void fetchPlaybackState_showsElapsedAndRemainingTime() {
        openPlaying();

        assertEquals("0:10", text(R.id.textCurrentTime));
        assertEquals("-1:30", text(R.id.textRemainingTime));
    }

    @Test
    public void fetchPlaybackState_ignoresEmptyResponse() {
        open("");

        assertEquals("", text(R.id.textPlayerTitle));
    }

    @Test
    public void polling_repeatsEveryFiveSeconds() {
        openPlaying();
        api.clearRequests();

        waitMillis(5000);

        assertNotNull(api.lastRequestTo(STATE_PATH));
    }

    @Test
    public void onStop_stopsPolling() {
        openPlaying();
        controller.stop();
        api.clearRequests();

        waitMillis(6000);

        assertNull(api.lastRequestTo(STATE_PATH));
    }

    @Test
    public void tick_advancesTheProgressEverySecond() {
        openPlaying();

        waitMillis(1000);

        assertEquals("0:11", text(R.id.textCurrentTime));
    }

    @Test
    public void closeButton_finishesTheActivity() {
        openPlaying();

        click(R.id.btnPlayerClose);

        assertTrue(activity.isFinishing());
    }

    @Test
    public void backButton_finishesTheActivity() {
        openPlaying();

        activity.getOnBackPressedDispatcher().onBackPressed();

        assertTrue(activity.isFinishing());
    }

    @Test
    public void playPause_pausesWhenPlaying() {
        openPlaying();

        click(R.id.btnPlayPause);

        assertNotNull(api.lastRequestTo("/v1/me/player/pause"));
    }

    @Test
    public void playPause_resumesOnTheCurrentDeviceWhenPaused() {
        open(state(false, false, "off", null));

        click(R.id.btnPlayPause);

        assertEquals("d2", api.lastRequestTo("/v1/me/player/play").url().queryParameter("device_id"));
    }

    @Test
    public void nextButton_skipsAndRefreshes() {
        openPlaying();
        api.clearRequests();

        click(R.id.btnNext);
        waitMillis(600);

        assertEquals("POST", api.lastRequestTo("/v1/me/player/next").method());
        assertNotNull(api.lastRequestTo(STATE_PATH));
    }

    @Test
    public void previousButton_skipsBack() {
        openPlaying();

        click(R.id.btnPrevious);

        assertEquals("POST", api.lastRequestTo("/v1/me/player/previous").method());
    }

    @Test
    public void shuffleButton_turnsShuffleOnAndRefreshesTheQueue() {
        openPlaying();
        api.clearRequests();

        click(R.id.btnShuffle);
        waitMillis(600);

        assertEquals("true", api.lastRequestTo("/v1/me/player/shuffle").url().queryParameter("state"));
        assertNotNull(api.lastRequestTo(QUEUE_PATH));
    }

    @Test
    public void shuffleButton_turnsShuffleOffWhenActive() {
        open(state(true, true, "off", null));

        click(R.id.btnShuffle);

        assertEquals("false", api.lastRequestTo("/v1/me/player/shuffle").url().queryParameter("state"));
    }

    private String repeatStateAfterClick() {
        click(R.id.btnRepeat);
        return api.lastRequestTo("/v1/me/player/repeat").url().queryParameter("state");
    }

    @Test
    public void repeatButton_cyclesThroughAllModes() {
        openPlaying();

        assertEquals("context", repeatStateAfterClick());
        assertEquals("track", repeatStateAfterClick());
        assertEquals("off", repeatStateAfterClick());
    }

    @Test
    public void seekBar_seeksWhenTheUserLetsGo() {
        openPlaying();
        SeekBar seekBar = activity.findViewById(R.id.seekBarPlayer);

        seekBar.setProgress(50000);
        shadowOf(seekBar).getOnSeekBarChangeListener().onStopTrackingTouch(seekBar);

        assertEquals("50000", api.lastRequestTo("/v1/me/player/seek").url().queryParameter("position_ms"));
    }

    @Test
    public void likeButton_savesAnUnsavedTrack() {
        api.stub(CONTAINS_PATH, "[false]");
        openPlaying();

        click(R.id.btnPlayerLike);

        assertEquals("PUT", api.lastRequestTo(LIBRARY_PATH).method());
        assertEquals("Adicionado às Músicas favoritas", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void likeButton_removesASavedTrack() {
        api.stub(CONTAINS_PATH, "[true]");
        openPlaying();

        click(R.id.btnPlayerLike);

        assertEquals("DELETE", api.lastRequestTo(LIBRARY_PATH).method());
        assertEquals("Removido das Músicas favoritas", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void queue_listsUpcomingTracks() {
        api.stub(QUEUE_PATH, "{\"queue\":[{\"id\":\"t2\",\"name\":\"Two\",\"artists\":[{\"name\":\"B\"}]},"
                + "{\"id\":\"t3\",\"name\":\"Three\"}]}");

        openPlaying();

        assertEquals("1. Two — B\n2. Three", text(R.id.textQueue));
    }

    @Test
    public void queue_hidesTheCurrentTrackRepeated() {
        api.stub(QUEUE_PATH, "{\"queue\":[{\"id\":\"t1\",\"name\":\"Song\"},{\"id\":\"t1\",\"name\":\"Song\"}]}");

        openPlaying();

        assertEquals("A fila está vazia.", text(R.id.textQueue));
    }

    @Test
    public void queue_showsAtMostFiveTracks() {
        StringBuilder queue = new StringBuilder("{\"queue\":[");
        for (int i = 0; i < 8; i++) {
            queue.append(i > 0 ? "," : "").append("{\"id\":\"x").append(i).append("\",\"name\":\"N").append(i).append("\"}");
        }
        api.stub(QUEUE_PATH, queue.append("]}").toString());

        openPlaying();

        assertEquals(5, text(R.id.textQueue).split("\n").length);
    }

    @Test
    public void optionsButton_doesNothingBeforeTheTrackLoads() {
        open("");

        click(R.id.btnPlayerMore);

        assertNull(ShadowDialog.getLatestDialog());
    }

    @Test
    public void options_showTrackAndAlbum() {
        openPlaying();

        BottomSheetDialog sheet = openOptions();

        assertEquals("Song", ((TextView) sheet.findViewById(R.id.textOptionsTitle)).getText().toString());
        assertEquals("Parcels • LOVED", ((TextView) sheet.findViewById(R.id.textOptionsSubtitle)).getText().toString());
    }

    @Test
    public void options_offerToSaveAnUnsavedTrack() {
        api.stub(CONTAINS_PATH, "[false]");
        openPlaying();

        BottomSheetDialog sheet = openOptions();

        assertEquals(
                "Guardar nas Músicas favoritas",
                ((TextView) sheet.findViewById(R.id.optionToggleLiked)).getText().toString()
        );
    }

    @Test
    public void options_offerToRemoveASavedTrack() {
        api.stub(CONTAINS_PATH, "[true]");
        openPlaying();

        BottomSheetDialog sheet = openOptions();

        assertEquals(
                "Remover das Músicas favoritas",
                ((TextView) sheet.findViewById(R.id.optionToggleLiked)).getText().toString()
        );
    }

    @Test
    public void options_addToQueue() {
        openPlaying();

        chooseOption(openOptions(), R.id.optionAddToQueue);

        Request request = api.lastRequestTo(QUEUE_PATH);
        assertEquals("POST", request.method());
        assertEquals("spotify:track:t1", request.url().queryParameter("uri"));
        assertEquals("Adicionado à fila", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void options_addToQueueReportsErrors() {
        openPlaying();
        api.stub(QUEUE_PATH, 403, "{}");

        chooseOption(openOptions(), R.id.optionAddToQueue);

        assertEquals("Não foi possível adicionar à fila (403)", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void options_removeIsDimmedOutsideAPlaylist() {
        openPlaying();

        BottomSheetDialog sheet = openOptions();
        View option = sheet.findViewById(R.id.optionRemoveFromPlaylist);

        assertEquals(0.4f, option.getAlpha(), 0.001f);
        chooseOption(sheet, R.id.optionRemoveFromPlaylist);
        assertEquals("Esta música não está a tocar a partir de uma playlist", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void options_removeDeletesFromTheCurrentPlaylist() throws IOException {
        open(state(true, false, "off", "spotify:playlist:p1"));

        chooseOption(openOptions(), R.id.optionRemoveFromPlaylist);

        Request request = api.lastRequestTo("/v1/playlists/p1/items");
        assertEquals("DELETE", request.method());
        assertTrue(FakeSpotifyApi.bodyOf(request).contains("spotify:track:t1"));
        assertEquals("Removido da playlist", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void options_removeExplainsForbiddenPlaylists() {
        open(state(true, false, "off", "spotify:playlist:p1"));
        api.stub("/v1/playlists/p1/items", 403, "{}");

        chooseOption(openOptions(), R.id.optionRemoveFromPlaylist);

        assertEquals("Só podes remover músicas das tuas playlists", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void options_goToAlbumOpensItInMainActivity() {
        openPlaying();

        chooseOption(openOptions(), R.id.optionGoToAlbum);

        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertEquals(MainActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals("al1", intent.getStringExtra(MainActivity.EXTRA_OPEN_ALBUM_ID));
        assertTrue(activity.isFinishing());
    }

    @Test
    public void options_toggleLikedDismissesTheSheet() {
        openPlaying();
        BottomSheetDialog sheet = openOptions();

        chooseOption(sheet, R.id.optionToggleLiked);

        assertFalse(sheet.isShowing());
    }
}

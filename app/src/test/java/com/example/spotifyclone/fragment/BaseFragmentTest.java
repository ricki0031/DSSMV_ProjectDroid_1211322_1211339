package com.example.spotifyclone.fragment;

import static com.example.spotifyclone.TestEnvironment.ACCESS_TOKEN;
import static com.example.spotifyclone.TestEnvironment.currentFragment;
import static com.example.spotifyclone.TestEnvironment.runMainThread;
import static com.example.spotifyclone.TestEnvironment.saveValidSession;
import static com.example.spotifyclone.TestEnvironment.show;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;

import com.example.spotifyclone.MainActivity;
import com.example.spotifyclone.api.FakeSpotifyApi;
import com.example.spotifyclone.model.MediaItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class BaseFragmentTest {

    public static class SimpleFragment extends BaseFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return new View(inflater.getContext());
        }
    }

    private MainActivity activity;
    private final SimpleFragment fragment = new SimpleFragment();

    @Before
    public void setUp() {
        new FakeSpotifyApi().install();
        saveValidSession(RuntimeEnvironment.getApplication());
        activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        runMainThread();
    }

    @Test
    public void token_returnsTheSessionToken() {
        show(activity, fragment);

        assertEquals(ACCESS_TOKEN, fragment.token());
    }

    @Test
    public void alive_isFalseBeforeBeingShown() {
        assertFalse(fragment.alive());
    }

    @Test
    public void alive_isTrueWhileOnScreen() {
        show(activity, fragment);

        assertTrue(fragment.alive());
    }

    @Test
    public void alive_isFalseAfterBeingReplaced() {
        show(activity, fragment);

        show(activity, new SimpleFragment());

        assertFalse(fragment.alive());
    }

    @Test
    public void main_returnsTheMainActivity() {
        show(activity, fragment);

        assertSame(activity, fragment.main());
    }

    @Test
    public void main_isNullInAnotherActivity() {
        FragmentActivity other = Robolectric.buildActivity(FragmentActivity.class).setup().get();
        other.getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commitNow();

        assertNull(fragment.main());
        assertNull(fragment.deviceId());
    }

    @Test
    public void deviceId_returnsTheActivityDevice() {
        activity.globalDeviceId = "d1";
        show(activity, fragment);

        assertEquals("d1", fragment.deviceId());
    }

    @Test
    public void open_navigatesToTheItem() {
        show(activity, fragment);
        MediaItem album = new MediaItem();
        album.type = MediaItem.TYPE_ALBUM;
        album.id = "al1";

        fragment.open(album);

        assertTrue(currentFragment(activity) instanceof AlbumFragment);
    }
}

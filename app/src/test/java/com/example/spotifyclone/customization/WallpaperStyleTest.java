package com.example.spotifyclone.customization;

import static org.junit.Assert.assertArrayEquals;

import android.graphics.drawable.GradientDrawable;

import com.example.spotifyclone.MainActivity;
import com.example.spotifyclone.R;
import com.example.spotifyclone.api.FakeSpotifyApi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class WallpaperStyleTest {

    @Test
    public void apply_usesTheWallpaperColorsOnNavigationAndMiniPlayer() {
        new FakeSpotifyApi().install();
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();

        WallpaperStyle.apply(activity);

        GradientDrawable navigation =
                (GradientDrawable) activity.findViewById(R.id.bottom_navigation).getBackground();
        GradientDrawable miniPlayer =
                (GradientDrawable) activity.findViewById(R.id.mini_player).getBackground();

        assertArrayEquals(new int[] {0x000B0B09, 0xE60B0B09}, navigation.getColors());
        assertArrayEquals(new int[] {0xFF0B0B09, 0xFF322E28}, miniPlayer.getColors());
    }
}

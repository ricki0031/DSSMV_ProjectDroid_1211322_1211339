package com.example.spotifyclone.customization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.graphics.drawable.GradientDrawable;
import android.view.ContextThemeWrapper;

import com.example.spotifyclone.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class BottomNavigationFadeTest {

    private BottomNavigationView navigation;

    @Before
    public void setUp() {
        navigation = new BottomNavigationView(
                new ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_SpotifyClone));
    }

    @Test
    public void apply_fadesFromTransparentToTheGivenColor() {
        BottomNavigationFade.apply(navigation, 0xFF102030);

        GradientDrawable background = (GradientDrawable) navigation.getBackground();
        assertEquals(GradientDrawable.Orientation.TOP_BOTTOM, background.getOrientation());
        assertArrayEquals(new int[] {0x00102030, 0xE6102030}, background.getColors());
    }

    @Test
    public void apply_removesTheElevationShadow() {
        BottomNavigationFade.apply(navigation, 0xFF102030);

        assertEquals(0f, navigation.getElevation(), 0f);
    }

    @Test
    public void apply_usesTranslucentWhiteForTheActiveIndicator() {
        BottomNavigationFade.apply(navigation, 0xFF102030);

        assertEquals(0x40FFFFFF, navigation.getItemActiveIndicatorColor().getDefaultColor());
    }
}

package com.example.spotifyclone.customization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.graphics.drawable.GradientDrawable;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class MiniPlayerColorsTest {

    @Test
    public void apply_paintsTheBackgroundWithAHorizontalGradient() {
        View miniPlayer = new View(RuntimeEnvironment.getApplication());
        miniPlayer.setBackground(new GradientDrawable());

        MiniPlayerColors.apply(miniPlayer, 0xFF111111, 0xFF222222);

        GradientDrawable background = (GradientDrawable) miniPlayer.getBackground();
        assertEquals(GradientDrawable.Orientation.LEFT_RIGHT, background.getOrientation());
        assertArrayEquals(new int[] {0xFF111111, 0xFF222222}, background.getColors());
    }
}

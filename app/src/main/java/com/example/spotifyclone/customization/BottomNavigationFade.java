package com.example.spotifyclone.customization;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;

import androidx.core.graphics.ColorUtils;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Faz a barra de navegação desvanecer, de transparente em cima para a cor do wallpaper em baixo. */
public final class BottomNavigationFade {

    private static final int BOTTOM_ALPHA = 0xE6;
    private static final int ACTIVE_INDICATOR_COLOR = 0x40FFFFFF;

    private BottomNavigationFade() {}

    public static void apply(BottomNavigationView navigation, int fadeColor) {
        navigation.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {
                        ColorUtils.setAlphaComponent(fadeColor, 0),
                        ColorUtils.setAlphaComponent(fadeColor, BOTTOM_ALPHA)
                }
        ));
        navigation.setElevation(0f);
        navigation.setItemActiveIndicatorColor(ColorStateList.valueOf(ACTIVE_INDICATOR_COLOR));
    }
}

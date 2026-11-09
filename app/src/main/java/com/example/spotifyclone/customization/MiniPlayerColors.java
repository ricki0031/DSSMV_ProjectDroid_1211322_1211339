package com.example.spotifyclone.customization;

import android.graphics.drawable.GradientDrawable;
import android.view.View;

/** Pinta o mini player com as cores predominantes do wallpaper, mantendo os cantos do XML. */
public final class MiniPlayerColors {

    private MiniPlayerColors() {}

    public static void apply(View miniPlayer, int startColor, int endColor) {
        GradientDrawable background = (GradientDrawable) miniPlayer.getBackground().mutate();
        background.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        background.setColors(new int[] {startColor, endColor});
    }
}

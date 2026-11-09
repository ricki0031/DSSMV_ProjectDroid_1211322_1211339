package com.example.spotifyclone.customization;

import android.app.Activity;

import com.example.spotifyclone.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** As cores dos elementos que acompanham o wallpaper da app (gira-discos). */
public final class WallpaperStyle {

    public static final int BUTTON_ACTIVE_COLOR = 0xFF1DB954;
    public static final int BUTTON_INACTIVE_COLOR = 0xFF333333;

    private static final int NAVIGATION_FADE_COLOR = 0xFF0B0B09;
    private static final int MINI_PLAYER_START_COLOR = 0xFF0B0B09;
    private static final int MINI_PLAYER_END_COLOR = 0xFF322E28;

    private WallpaperStyle() {}

    public static void apply(Activity activity) {
        BottomNavigationView navigation = activity.findViewById(R.id.bottom_navigation);
        BottomNavigationFade.apply(navigation, NAVIGATION_FADE_COLOR);
        MiniPlayerColors.apply(
                activity.findViewById(R.id.mini_player),
                MINI_PLAYER_START_COLOR,
                MINI_PLAYER_END_COLOR
        );
    }
}

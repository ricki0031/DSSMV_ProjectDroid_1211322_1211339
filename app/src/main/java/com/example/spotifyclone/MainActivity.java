package com.example.spotifyclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.spotifyclone.api.LibraryApiClient;
import com.example.spotifyclone.api.PlayerApiClient;
import com.example.spotifyclone.api.SpotifyAuthClient;
import com.example.spotifyclone.fragment.AlbumFragment;
import com.example.spotifyclone.fragment.ArtistFragment;
import com.example.spotifyclone.fragment.HomeFragment;
import com.example.spotifyclone.fragment.LibraryFragment;
import com.example.spotifyclone.fragment.PlaylistFragment;
import com.example.spotifyclone.fragment.SearchFragment;
import com.example.spotifyclone.model.CurrentlyPlayingResponse;
import com.example.spotifyclone.model.Device;
import com.example.spotifyclone.model.DevicesResponse;
import com.example.spotifyclone.model.MediaItem;
import com.example.spotifyclone.model.TokenResponse;
import com.example.spotifyclone.model.Track;
import com.example.spotifyclone.utils.ApiCallback;
import com.example.spotifyclone.utils.PKCEUtil;
import com.example.spotifyclone.utils.PlaylistPicker;
import com.example.spotifyclone.utils.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = Session.PREFS;
    public static final String CODE_VERIFIER_KEY = "code_verifier";

    private static final String REQUIRED_SCOPES =
            "user-modify-playback-state user-read-playback-state user-read-currently-playing " +
                    "user-library-read user-library-modify user-read-recently-played user-top-read " +
                    "user-follow-read playlist-read-private playlist-read-collaborative " +
                    "playlist-modify-public playlist-modify-private ugc-image-upload";

    private TextView miniPlayerTitle, miniPlayerArtist;
    private ImageView miniPlayerPlayPause, miniPlayerCover, miniPlayerAdd;
    private LinearLayout miniPlayerButton;

    private boolean isPlaying = false;
    private String currentTrackId = "";
    private String globalAccessToken;
    public String globalDeviceId = "";
    private boolean navInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupMiniPlayerControls();

        if (getIntent() != null && getIntent().getData() != null
                && getIntent().getData().toString().startsWith(BuildConfig.REDIRECT_URI)) {
            handleAuthRedirect(getIntent());
        } else {
            restoreOrAuthenticate();
        }
    }

    private void restoreOrAuthenticate() {
        String savedToken = Session.getAccessToken(this);
        String refreshToken = Session.getRefreshToken(this);

        if (savedToken != null && Session.hasScopes(this, REQUIRED_SCOPES)) {
            if (!Session.isExpired(this)) {
                onAuthenticated(savedToken);
                return;
            } else if (refreshToken != null) {
                SpotifyAuthClient.refreshAccessToken(BuildConfig.SPOTIFY_CLIENT_ID, refreshToken, new ApiCallback() {
                    @Override
                    protected void onSuccess(String body) {
                        TokenResponse tokenResponse = GSON.fromJson(body, TokenResponse.class);
                        Session.save(MainActivity.this, tokenResponse);
                        onAuthenticated(tokenResponse.getAccessToken());
                    }

                    @Override
                    protected void onError(int code, String body) {
                        authenticateWithSpotify();
                    }
                });
                return;
            }
        }

        authenticateWithSpotify();
    }

    private void onAuthenticated(String token) {
        this.globalAccessToken = token;
        fetchActiveDevice(token);

        if (!navInitialized) {
            navInitialized = true;
            setupBottomNavigation(token);
        }

        syncMiniPlayer(token);
    }

    private void authenticateWithSpotify() {
        String codeVerifier = PKCEUtil.generateCodeVerifier();
        String codeChallenge = PKCEUtil.generateCodeChallenge(codeVerifier);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CODE_VERIFIER_KEY, codeVerifier);
        editor.apply();

        Uri uri = Uri.parse("https://accounts.spotify.com/authorize")
                .buildUpon()
                .appendQueryParameter("client_id", BuildConfig.SPOTIFY_CLIENT_ID)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("redirect_uri", BuildConfig.REDIRECT_URI)
                .appendQueryParameter("code_challenge_method", "S256")
                .appendQueryParameter("code_challenge", codeChallenge)
                .appendQueryParameter("scope", REQUIRED_SCOPES)
                .build();

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(browserIntent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleAuthRedirect(intent);
    }

    private void handleAuthRedirect(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith(BuildConfig.REDIRECT_URI)) {
            String code = uri.getQueryParameter("code");
            if (code != null) {
                getTokenFromSpotify(code);
            }
        }
    }

    private void getTokenFromSpotify(String code) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String codeVerifier = prefs.getString(CODE_VERIFIER_KEY, null);
        if (codeVerifier == null) {
            return;
        }

        SpotifyAuthClient.getAccessToken(
                BuildConfig.SPOTIFY_CLIENT_ID,
                code,
                BuildConfig.REDIRECT_URI,
                codeVerifier,
                new ApiCallback() {
                    @Override
                    protected void onSuccess(String body) {
                        TokenResponse tokenResponse = GSON.fromJson(body, TokenResponse.class);
                        Session.save(MainActivity.this, tokenResponse);
                        onAuthenticated(tokenResponse.getAccessToken());
                    }
                }
        );
    }

    public void fetchActiveDevice(String token) {
        PlayerApiClient.getAvailableDevices(token, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                DevicesResponse devicesResponse = GSON.fromJson(body, DevicesResponse.class);
                if (devicesResponse != null && devicesResponse.devices != null && !devicesResponse.devices.isEmpty()) {
                    for (Device device : devicesResponse.devices) {
                        if (device.isActive || "Smartphone".equalsIgnoreCase(device.type)) {
                            globalDeviceId = device.id;
                            break;
                        }
                    }
                    if (globalDeviceId.isEmpty()) {
                        globalDeviceId = devicesResponse.devices.get(0).id;
                    }
                }
            }
        });
    }

    private void setupBottomNavigation(String accessToken) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.nav_library) {
                selectedFragment = new LibraryFragment();
            }

            if (selectedFragment != null) {
                Bundle args = new Bundle();
                args.putString("ACCESS_TOKEN", Session.getAccessToken(this));
                selectedFragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    public void openMedia(MediaItem item) {
        if (item == null) {
            return;
        }

        Fragment target = null;
        Bundle args = new Bundle();
        args.putString("ACCESS_TOKEN", Session.getAccessToken(this));

        if ("liked_songs".equals(item.type)) {
            target = new PlaylistFragment();
            args.putBoolean(PlaylistFragment.ARG_LIKED_SONGS, true);
        } else if ("playlist".equals(item.type)) {
            target = new PlaylistFragment();
            args.putString(PlaylistFragment.ARG_PLAYLIST_ID, item.id);
        } else if ("artist".equals(item.type)) {
            target = new ArtistFragment();
            args.putString(ArtistFragment.ARG_ARTIST_ID, item.id);
        } else if ("album".equals(item.type)) {
            target = new AlbumFragment();
            args.putString("ALBUM_ID", item.id);
        }

        if (target != null) {
            target.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, target)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public void playTrack(Track track, String contextUri) {
        if (track == null) {
            return;
        }

        String token = Session.getAccessToken(this);
        updateMiniPlayerInstantly(track.id, track.name, track.artistNames(), track.coverUrl());

        PlayerApiClient.startResumePlayback(token, track.id, null, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                android.util.Log.e(
                        "SPOTIFY_PLAYBACK",
                        "Pedido de reprodução aceite pelo Spotify"
                );

                new Handler(Looper.getMainLooper()).postDelayed(
                        () -> syncMiniPlayer(token),
                        800
                );
            }

            @Override
            protected void onError(int code, String body) {
                super.onError(code, body);

                android.util.Log.e(
                        "SPOTIFY_PLAYBACK",
                        "Erro HTTP " + code + " | " + body
                );

                Toast.makeText(
                        MainActivity.this,
                        "Erro ao tocar: HTTP " + code,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    public void updateMiniPlayerInstantly(String trackId, String title, String artist, String coverUrl) {
        if (trackId != null) {
            currentTrackId = trackId;
        }
        isPlaying = true;

        if (miniPlayerTitle != null && title != null) {
            miniPlayerTitle.setText(title);
        }
        if (miniPlayerArtist != null && artist != null) {
            miniPlayerArtist.setText(artist);
        }
        if (miniPlayerPlayPause != null) {
            miniPlayerPlayPause.setImageResource(R.drawable.ic_pause);
        }
        if (miniPlayerCover != null && coverUrl != null && !isFinishing()) {
            Glide.with(this).load(coverUrl).into(miniPlayerCover);
        }
    }

    public void showTrackMenu(View anchor, Track track, String extraLabel, Runnable extraAction) {
        if (track == null) {
            return;
        }

        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, "Adicionar a playlist");
        menu.getMenu().add(0, 2, 1, "Guardar nas Liked Songs");
        menu.getMenu().add(0, 3, 2, "Adicionar à fila");

        if (extraLabel != null && extraAction != null) {
            menu.getMenu().add(0, 4, 3, extraLabel);
        }

        menu.setOnMenuItemClickListener(item -> {
            String token = Session.getAccessToken(this);
            int id = item.getItemId();

            if (id == 1) {
                PlaylistPicker.show(this, track.uriOrBuild(), track.name);
            } else if (id == 2) {
                LibraryApiClient.saveItemsToLibrary(token, Collections.singletonList(track.uriOrBuild()), new ApiCallback() {
                    @Override
                    protected void onSuccess(String body) {
                        Toast.makeText(MainActivity.this, "Guardado nas Liked Songs", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (id == 3) {
                PlayerApiClient.addItemToPlaybackQueue(token, track.uriOrBuild(), new ApiCallback() {
                    @Override
                    protected void onSuccess(String body) {
                        Toast.makeText(MainActivity.this, "Adicionado à fila", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (id == 4 && extraAction != null) {
                extraAction.run();
            }
            return true;
        });

        menu.show();
    }

    public void syncMiniPlayer(String accessToken) {
        String token;
        if (accessToken != null) {
            token = accessToken;
        } else {
            token = Session.getAccessToken(this);
        }

        if (token == null) {
            return;
        }
        this.globalAccessToken = token;

        PlayerApiClient.getCurrentlyPlayingTrack(token, new ApiCallback() {
            @Override
            protected void onSuccess(String json) {
                if (json.isEmpty() || isFinishing()) {
                    return;
                }

                CurrentlyPlayingResponse current = GSON.fromJson(json, CurrentlyPlayingResponse.class);
                if (current != null && current.item != null) {
                    isPlaying = current.isPlaying;
                    currentTrackId = current.item.id;

                    miniPlayerTitle.setText(current.item.name);
                    miniPlayerArtist.setText(current.item.artistNames());

                    if (isPlaying) {
                        miniPlayerPlayPause.setImageResource(R.drawable.ic_pause);
                    } else {
                        miniPlayerPlayPause.setImageResource(R.drawable.ic_play);
                    }

                    String cover = current.item.coverUrl();
                    if (cover != null) {
                        Glide.with(MainActivity.this).load(cover).into(miniPlayerCover);
                    }
                }
            }
        });
    }

    private void setupMiniPlayerControls() {
        miniPlayerTitle = findViewById(R.id.miniPlayerTitle);
        miniPlayerArtist = findViewById(R.id.miniPlayerArtist);
        miniPlayerPlayPause = findViewById(R.id.miniPlayerPlayPause);
        miniPlayerCover = findViewById(R.id.miniPlayerImage);
        miniPlayerAdd = findViewById(R.id.miniPlayerAdd);
        miniPlayerButton = findViewById(R.id.mini_player);

        miniPlayerPlayPause.setOnClickListener(v -> {
            String token = Session.getAccessToken(this);
            if (token == null) {
                return;
            }

            if (isPlaying) {
                PlayerApiClient.pausePlayback(token, new ApiCallback() {
                    @Override
                    protected void onSuccess(String body) {
                        isPlaying = false;
                        miniPlayerPlayPause.setImageResource(R.drawable.ic_play);
                    }
                });
            } else {
                PlayerApiClient.startResumePlayback(token, null, globalDeviceId, new ApiCallback() {
                    @Override
                    protected void onSuccess(String body) {
                        isPlaying = true;
                        miniPlayerPlayPause.setImageResource(R.drawable.ic_pause);
                    }
                });
            }
        });

        if (miniPlayerAdd != null) {
            miniPlayerAdd.setOnClickListener(v -> {
                if (currentTrackId == null || currentTrackId.isEmpty()) {
                    return;
                }
                String trackUri = "spotify:track:" + currentTrackId;
                String trackTitle = miniPlayerTitle.getText().toString();
                PlaylistPicker.show(this, trackUri, trackTitle);
            });
        }

        miniPlayerButton.setOnClickListener(v -> {
            String token = Session.getAccessToken(this);
            if (token == null) {
                return;
            }
            Intent playerIntent = new Intent(this, PlayerActivity.class);
            playerIntent.putExtra("ACCESS_TOKEN", token);
            playerIntent.putExtra("DEVICE_ID", globalDeviceId);
            startActivity(playerIntent);
            if (Build.VERSION.SDK_INT < 34) {
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String token = Session.getAccessToken(this);
        if (token != null) {
            syncMiniPlayer(token);
        }
    }
}
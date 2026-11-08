package com.example.spotifyclone;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.spotifyclone.api.LibraryApiClient;
import com.example.spotifyclone.api.PlayerApiClient;
import com.example.spotifyclone.model.PlaybackStateResponse;
import com.example.spotifyclone.model.QueueResponse;
import com.example.spotifyclone.model.Track;
import com.example.spotifyclone.utils.ApiCallback;
import com.example.spotifyclone.utils.PlaylistPicker;
import com.example.spotifyclone.utils.Session;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    private static final long POLL_INTERVAL_MS = 5000;
    private static final long TICK_INTERVAL_MS = 1000;

    private ImageView imagePlayerCover, btnPlayPauseIcon, btnShuffle, btnRepeat, btnPlayerLike;
    private TextView textPlayerTitle, textPlayerArtist, textCurrentTime, textRemainingTime;
    private TextView textPlayingFrom, textDevice, textQueue;
    private SeekBar seekBarPlayer;

    private String deviceId;
    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private boolean isLiked = false;
    private boolean userIsSeeking = false;
    private String repeatState = "off";
    private int progressMs = 0;
    private int durationMs = 0;
    private Track currentTrack;
    private String lastCoverUrl;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (isPlaying && !userIsSeeking && durationMs > 0) {
                progressMs = Math.min(durationMs, progressMs + (int) TICK_INTERVAL_MS);
                renderProgress();
                if (progressMs >= durationMs) {
                    fetchPlaybackState();
                }
            }
            handler.postDelayed(this, TICK_INTERVAL_MS);
        }
    };

    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            fetchPlaybackState();
            handler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_up, R.anim.stay);
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.stay, R.anim.slide_out_down);
        }

        deviceId = getIntent().getStringExtra("DEVICE_ID");

        bindViews();
        setupListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                closePlayer();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(poll);
        handler.postDelayed(tick, TICK_INTERVAL_MS);
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(poll);
        handler.removeCallbacks(tick);
        super.onStop();
    }

    private String token() {
        return Session.getAccessToken(this);
    }

    private void bindViews() {
        imagePlayerCover = findViewById(R.id.imagePlayerCover);
        textPlayerTitle = findViewById(R.id.textPlayerTitle);
        textPlayerArtist = findViewById(R.id.textPlayerArtist);
        textPlayingFrom = findViewById(R.id.textPlayingFrom);
        textDevice = findViewById(R.id.textDevice);
        textQueue = findViewById(R.id.textQueue);
        seekBarPlayer = findViewById(R.id.seekBarPlayer);
        textCurrentTime = findViewById(R.id.textCurrentTime);
        textRemainingTime = findViewById(R.id.textRemainingTime);
        btnPlayPauseIcon = findViewById(R.id.btnPlayPauseIcon);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnPlayerLike = findViewById(R.id.btnPlayerLike);
        textPlayerTitle.setSelected(true);
    }

    private void setupListeners() {
        ImageView btnClose = findViewById(R.id.btnPlayerClose);
        btnClose.setOnClickListener(v -> closePlayer());

        FrameLayout btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPlayPause.setOnClickListener(v -> togglePlayPause());

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            PlayerApiClient.skipToNext(token(), refreshSoon());
        });

        findViewById(R.id.btnPrevious).setOnClickListener(v -> {
            PlayerApiClient.skipToPrevious(token(), refreshSoon());
        });

        btnShuffle.setOnClickListener(v -> {
            boolean newState = !isShuffle;
            PlayerApiClient.togglePlaybackShuffle(token(), newState, new ApiCallback() {
                @Override
                protected void onSuccess(String body) {
                    isShuffle = newState;
                    renderShuffleRepeat();
                }
            });
        });

        btnRepeat.setOnClickListener(v -> {
            String nextState;
            if ("off".equals(repeatState)) {
                nextState = "context";
            } else if ("context".equals(repeatState)) {
                nextState = "track";
            } else {
                nextState = "off";
            }

            PlayerApiClient.setRepeatMode(token(), nextState, new ApiCallback() {
                @Override
                protected void onSuccess(String body) {
                    repeatState = nextState;
                    renderShuffleRepeat();
                }
            });
        });

        btnPlayerLike.setOnClickListener(v -> toggleLike());

        findViewById(R.id.btnPlayerMore).setOnClickListener(v -> {
            if (currentTrack == null) {
                return;
            }

            PopupMenu menu = new PopupMenu(this, v);
            menu.getMenu().add(0, 1, 0, "Adicionar a playlist");

            if (isLiked) {
                menu.getMenu().add(0, 2, 1, "Remover das Liked Songs");
            } else {
                menu.getMenu().add(0, 2, 1, "Guardar nas Liked Songs");
            }

            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    PlaylistPicker.show(this, currentTrack.uriOrBuild(), currentTrack.name);
                } else {
                    toggleLike();
                }
                return true;
            });
            menu.show();
        });

        seekBarPlayer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateTimeLabels(progress, durationMs);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userIsSeeking = false;
                progressMs = seekBar.getProgress();
                PlayerApiClient.seekToPosition(token(), progressMs, new ApiCallback() {
                    @Override
                    protected void onSuccess(String body) {}
                });
            }
        });
    }

    private void togglePlayPause() {
        if (isPlaying) {
            PlayerApiClient.pausePlayback(token(), new ApiCallback() {
                @Override
                protected void onSuccess(String body) {
                    isPlaying = false;
                    btnPlayPauseIcon.setImageResource(R.drawable.ic_play);
                }
            });
        } else {
            PlayerApiClient.startResumePlayback(token(), null, deviceId, new ApiCallback() {
                @Override
                protected void onSuccess(String body) {
                    isPlaying = true;
                    btnPlayPauseIcon.setImageResource(R.drawable.ic_pause);
                }
            });
        }
    }

    private ApiCallback refreshSoon() {
        return new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                handler.postDelayed(() -> fetchPlaybackState(), 600);
            }
        };
    }

    private void fetchPlaybackState() {
        PlayerApiClient.getPlaybackState(token(), new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                if (isFinishing() || body.isEmpty()) {
                    return;
                }

                PlaybackStateResponse state = GSON.fromJson(body, PlaybackStateResponse.class);
                if (state == null || state.item == null) {
                    return;
                }

                boolean trackChanged = false;
                if (currentTrack == null || !state.item.id.equals(currentTrack.id)) {
                    trackChanged = true;
                }

                currentTrack = state.item;
                isPlaying = state.isPlaying;
                isShuffle = state.shuffleState;

                if (state.repeatState != null) {
                    repeatState = state.repeatState;
                } else {
                    repeatState = "off";
                }

                durationMs = (int) state.item.durationMs;
                if (!userIsSeeking) {
                    progressMs = (int) state.progressMs;
                }

                renderTrack();
                renderProgress();
                renderShuffleRepeat();

                if (isPlaying) {
                    btnPlayPauseIcon.setImageResource(R.drawable.ic_pause);
                } else {
                    btnPlayPauseIcon.setImageResource(R.drawable.ic_play);
                }

                if (state.device != null) {
                    deviceId = state.device.id;
                    textDevice.setText("A tocar em " + state.device.name);
                }

                if (trackChanged) {
                    refreshLiked();
                    fetchQueue();
                }
            }
        });
    }

    private void renderTrack() {
        textPlayerTitle.setText(currentTrack.name);
        textPlayerArtist.setText(currentTrack.artistNames());

        if (currentTrack.album != null) {
            textPlayingFrom.setText(currentTrack.album.name);
        }

        String cover = currentTrack.coverUrl();
        if (cover != null && !cover.equals(lastCoverUrl)) {
            lastCoverUrl = cover;
            Glide.with(this).load(cover).into(imagePlayerCover);
        }
    }

    private void renderProgress() {
        seekBarPlayer.setMax(Math.max(durationMs, 1));
        if (!userIsSeeking) {
            seekBarPlayer.setProgress(progressMs);
        }
        updateTimeLabels(progressMs, durationMs);
    }

    private void renderShuffleRepeat() {
        int green = 0xFF1ED760;
        int white = 0xFFFFFFFF;

        if (isShuffle) {
            btnShuffle.setColorFilter(green);
        } else {
            btnShuffle.setColorFilter(white);
        }

        if ("track".equals(repeatState)) {
            btnRepeat.setImageResource(R.drawable.ic_repeat_one);
        } else {
            btnRepeat.setImageResource(R.drawable.ic_repeat);
        }

        if ("off".equals(repeatState)) {
            btnRepeat.setColorFilter(white);
        } else {
            btnRepeat.setColorFilter(green);
        }
    }

    private void refreshLiked() {
        List<String> uris = Collections.singletonList(currentTrack.uriOrBuild());
        LibraryApiClient.checkUserSavedItems(token(), uris, new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                Boolean[] result = GSON.fromJson(body, Boolean[].class);
                if (result != null && result.length > 0 && Boolean.TRUE.equals(result[0])) {
                    isLiked = true;
                } else {
                    isLiked = false;
                }

                if (isLiked) {
                    btnPlayerLike.setImageResource(R.drawable.ic_check_circle);
                    btnPlayerLike.setColorFilter(0xFF1ED760);
                } else {
                    btnPlayerLike.setImageResource(R.drawable.ic_add_circle);
                    btnPlayerLike.setColorFilter(0xFFFFFFFF);
                }
            }
        });
    }

    private void toggleLike() {
        if (currentTrack == null) {
            return;
        }

        ApiCallback after = new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                String message;
                if (isLiked) {
                    message = "Removido das Liked Songs";
                } else {
                    message = "Adicionado às Liked Songs";
                }
                Toast.makeText(PlayerActivity.this, message, Toast.LENGTH_SHORT).show();
                refreshLiked();
            }
        };

        List<String> uris = Collections.singletonList(currentTrack.uriOrBuild());
        if (isLiked) {
            LibraryApiClient.removeItemsFromLibrary(token(), uris, after);
        } else {
            LibraryApiClient.saveItemsToLibrary(token(), uris, after);
        }
    }

    private void fetchQueue() {
        PlayerApiClient.getTheUserQueue(token(), new ApiCallback() {
            @Override
            protected void onSuccess(String body) {
                QueueResponse queue = GSON.fromJson(body, QueueResponse.class);
                if (queue == null || queue.queue == null || queue.queue.isEmpty()) {
                    textQueue.setText("A fila está vazia.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                int count = Math.min(5, queue.queue.size());
                for (int i = 0; i < count; i++) {
                    Track t = queue.queue.get(i);
                    if (i > 0) {
                        sb.append('\n');
                    }
                    sb.append(i + 1).append(". ").append(t.name);

                    String artists = t.artistNames();
                    if (!artists.isEmpty()) {
                        sb.append(" — ").append(artists);
                    }
                }
                textQueue.setText(sb.toString());
            }
        });
    }

    private void updateTimeLabels(int currentMs, int totalMs) {
        textCurrentTime.setText(formatTime(currentMs));
        int remainingMs = Math.max(0, totalMs - currentMs);
        textRemainingTime.setText("-" + formatTime(remainingMs));
    }

    private String formatTime(int ms) {
        int totalSeconds = ms / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    private void closePlayer() {
        finish();
        if (Build.VERSION.SDK_INT < 34) {
            overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
        }
    }
}
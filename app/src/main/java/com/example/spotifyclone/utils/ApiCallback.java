package com.example.spotifyclone.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class ApiCallback implements Callback {

    public static final Gson GSON = new Gson();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final String TAG = "SpotifyApi";

    @Override
    public final void onFailure(@NonNull Call call, @NonNull IOException e) {
        if (call.isCanceled()) {
            return;
        }
        MAIN.post(() -> onError(-1, e.getMessage()));
    }

    @Override
    public final void onResponse(@NonNull Call call, @NonNull Response response) {
        int code = response.code();
        String body = "";

        if (code == 429) {
            Log.w(TAG, "Limite do Spotify atingido. Retry-After (s): " + response.header("Retry-After"));
        }

        try (ResponseBody responseBody = response.body()) {
            if (responseBody != null) {
                body = responseBody.string();
            }
        } catch (IOException e) {
            MAIN.post(() -> onError(-1, e.getMessage()));
            return;
        }

        final String finalBody = body;
        if (response.isSuccessful()) {
            MAIN.post(() -> onSuccess(finalBody));
        } else {
            MAIN.post(() -> onError(code, finalBody));
        }
    }

    protected abstract void onSuccess(String body);

    protected void onError(int code, String body) {
        Log.w(TAG, "HTTP " + code + ": " + body);
    }
}
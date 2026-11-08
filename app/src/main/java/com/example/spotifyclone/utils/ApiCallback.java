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

/**
 * Callback do OkHttp que:
 *  1. lê o corpo da resposta (e fecha-a sempre),
 *  2. entrega o resultado JÁ na Main Thread (não precisas de runOnUiThread),
 *  3. separa sucesso (2xx) de erro.
 */
public abstract class ApiCallback implements Callback {

    public static final Gson GSON = new Gson();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final String TAG = "SpotifyApi";

    @Override
    public final void onFailure(@NonNull Call call, @NonNull IOException e) {
        if (call.isCanceled()) return;
        MAIN.post(() -> onError(-1, e.getMessage()));
    }

    @Override
    public final void onResponse(@NonNull Call call, @NonNull Response response) {
        int code = response.code();
        String body;
        try (ResponseBody responseBody = response.body()) {
            body = responseBody != null ? responseBody.string() : "";
        } catch (IOException e) {
            MAIN.post(() -> onError(-1, e.getMessage()));
            return;
        }
        if (response.isSuccessful()) {
            MAIN.post(() -> onSuccess(body));
        } else {
            MAIN.post(() -> onError(code, body));
        }
    }

    /** Corre na Main Thread. O body pode vir vazio (ex: 204 No Content). */
    protected abstract void onSuccess(String body);

    /** Corre na Main Thread. code = -1 significa erro de rede. */
    protected void onError(int code, String body) {
        Log.w(TAG, "HTTP " + code + ": " + body);
    }
}

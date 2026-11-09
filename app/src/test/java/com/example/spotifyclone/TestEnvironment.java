package com.example.spotifyclone;

import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifyclone.model.TokenResponse;
import com.example.spotifyclone.utils.Session;
import com.google.gson.Gson;

import org.robolectric.shadows.ShadowNetworkCapabilities;

/** Shared setup for the tests that need a session, a network or a screen. */
public final class TestEnvironment {

    public static final String ACCESS_TOKEN = "test-access-token";
    public static final String REFRESH_TOKEN = "test-refresh-token";
    public static final String ALL_SCOPES =
            "user-modify-playback-state user-read-playback-state user-read-currently-playing "
                    + "user-library-read user-library-modify user-read-recently-played user-top-read "
                    + "user-follow-read playlist-read-private playlist-read-collaborative "
                    + "playlist-modify-public playlist-modify-private ugc-image-upload";

    private TestEnvironment() {}

    public static TokenResponse token(String access, @Nullable String refresh, int expiresIn, String scope) {
        String refreshJson = "";
        if (refresh != null) {
            refreshJson = "\"refresh_token\":\"" + refresh + "\",";
        }

        return new Gson().fromJson(
                "{\"access_token\":\"" + access + "\"," + refreshJson
                        + "\"expires_in\":" + expiresIn + ",\"scope\":\"" + scope + "\"}",
                TokenResponse.class
        );
    }

    public static void saveValidSession(Context context) {
        Session.save(context, token(ACCESS_TOKEN, REFRESH_TOKEN, 3600, ALL_SCOPES));
    }

    public static void saveExpiredSession(Context context) {
        Session.save(context, token(ACCESS_TOKEN, REFRESH_TOKEN, 0, ALL_SCOPES));
    }

    public static void setInternetAvailable(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = ShadowNetworkCapabilities.newInstance();
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        shadowOf(manager).setNetworkCapabilities(manager.getActiveNetwork(), capabilities);
    }

    /** Creates and binds the row at the position, without needing the list to be on screen. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static RecyclerView.ViewHolder bindRow(RecyclerView list, int position) {
        RecyclerView.Adapter adapter = list.getAdapter();
        RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(list, adapter.getItemViewType(position));
        adapter.onBindViewHolder(holder, position);
        return holder;
    }

    public static String rowText(RecyclerView list, int position, int textViewId) {
        TextView text = bindRow(list, position).itemView.findViewById(textViewId);
        return text.getText().toString();
    }

    public static void clickRow(RecyclerView list, int position) {
        bindRow(list, position).itemView.performClick();
    }

    /** Lays the list out so its rows behave as they do on a real screen. */
    public static void layoutList(RecyclerView list) {
        int width = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY);
        int height = View.MeasureSpec.makeMeasureSpec(2400, View.MeasureSpec.EXACTLY);
        list.measure(width, height);
        list.layout(0, 0, 1080, 2400);
    }

    /** Returns the fragment on screen after finishing any pending navigation. */
    public static Fragment currentFragment(FragmentActivity activity) {
        activity.getSupportFragmentManager().executePendingTransactions();
        runMainThread();
        return activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    /** Runs everything already posted to the main thread. */
    public static void runMainThread() {
        shadowOf(Looper.getMainLooper()).idle();
    }

    /** Shows the fragment in the activity's container and waits for it to be on screen. */
    public static void show(FragmentActivity activity, Fragment fragment) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();
        runMainThread();
    }
}

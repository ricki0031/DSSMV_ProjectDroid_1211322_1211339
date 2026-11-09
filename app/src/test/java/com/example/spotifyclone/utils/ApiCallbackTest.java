package com.example.spotifyclone.utils;

import static com.example.spotifyclone.TestEnvironment.runMainThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@RunWith(RobolectricTestRunner.class)
public class ApiCallbackTest {

    private static final Request REQUEST = new Request.Builder().url("https://example.test/").build();

    private String successBody;
    private Integer errorCode;
    private String errorBody;

    private final ApiCallback callback = new ApiCallback() {
        @Override
        protected void onSuccess(String body) {
            successBody = body;
        }

        @Override
        protected void onError(int code, String body) {
            errorCode = code;
            errorBody = body;
        }
    };

    private final Call call = new OkHttpClient().newCall(REQUEST);

    private static Response response(int code, String body) {
        return new Response.Builder()
                .request(REQUEST)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("test")
                .body(ResponseBody.create(body, MediaType.parse("application/json")))
                .build();
    }

    @Test
    public void onResponse_callsOnSuccessFor2xx() {
        callback.onResponse(call, response(200, "{\"ok\":true}"));
        runMainThread();

        assertEquals("{\"ok\":true}", successBody);
        assertNull(errorCode);
    }

    @Test
    public void onResponse_callsOnErrorForErrorStatus() {
        callback.onResponse(call, response(404, "not found"));
        runMainThread();

        assertNull(successBody);
        assertEquals(Integer.valueOf(404), errorCode);
        assertEquals("not found", errorBody);
    }

    @Test
    public void onResponse_reportsRateLimitAsError() {
        callback.onResponse(call, response(429, "limit"));
        runMainThread();

        assertEquals(Integer.valueOf(429), errorCode);
    }

    @Test
    public void onFailure_callsOnErrorWithMinusOne() {
        callback.onFailure(call, new IOException("no network"));
        runMainThread();

        assertEquals(Integer.valueOf(-1), errorCode);
        assertEquals("no network", errorBody);
    }

    @Test
    public void onFailure_ignoresCancelledCalls() {
        Call cancelled = new OkHttpClient().newCall(REQUEST);
        cancelled.cancel();

        callback.onFailure(cancelled, new IOException("cancelled"));
        runMainThread();

        assertNull(successBody);
        assertNull(errorCode);
    }
}

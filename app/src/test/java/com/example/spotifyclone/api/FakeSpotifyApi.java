package com.example.spotifyclone.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Test helper that replaces the HTTP client of every API class, so no test reaches Spotify.
 * It records each request and answers synchronously with the stubbed response.
 */
public final class FakeSpotifyApi {

    public static final Callback IGNORE_RESPONSE = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {}

        @Override
        public void onResponse(Call call, Response response) {
            response.close();
        }
    };

    private static final class Stub {
        final int code;
        final String body;

        Stub(int code, String body) {
            this.code = code;
            this.body = body;
        }
    }

    private static final class SameThreadExecutor extends AbstractExecutorService {
        @Override
        public void execute(Runnable command) {
            command.run();
        }

        @Override
        public void shutdown() {}

        @Override
        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }
    }

    private final List<Request> requests = new ArrayList<>();
    private final Map<String, Stub> stubs = new HashMap<>();

    /** Replaces the HTTP client of all the API classes with this fake. */
    public void install() {
        OkHttpClient fakeClient = new OkHttpClient.Builder()
                .dispatcher(new Dispatcher(new SameThreadExecutor()))
                .addInterceptor(chain -> respond(chain.request()))
                .build();

        AlbumApiClient.client = fakeClient;
        ArtistApiClient.client = fakeClient;
        LibraryApiClient.client = fakeClient;
        PlayerApiClient.client = fakeClient;
        PlaylistApiClient.client = fakeClient;
        SearchApiClient.client = fakeClient;
        SpotifyAuthClient.client = fakeClient;
        TrackApiClient.client = fakeClient;
        UserApiClient.client = fakeClient;
    }

    /** Sets the response for a URL path such as "/v1/albums/a1". Other paths answer 200 with "{}". */
    public void stub(String path, int code, String body) {
        stubs.put(path, new Stub(code, body));
    }

    public void stub(String path, String body) {
        stub(path, 200, body);
    }

    public void clearRequests() {
        requests.clear();
    }

    public List<Request> requests() {
        return requests;
    }

    public Request lastRequest() {
        return requests.get(requests.size() - 1);
    }

    /** Returns the most recent request to the path, or null if there was none. */
    public Request lastRequestTo(String path) {
        for (int i = requests.size() - 1; i >= 0; i--) {
            if (requests.get(i).url().encodedPath().equals(path)) {
                return requests.get(i);
            }
        }
        return null;
    }

    public int countRequestsTo(String pathPrefix) {
        int count = 0;
        for (Request request : requests) {
            if (request.url().encodedPath().startsWith(pathPrefix)) {
                count++;
            }
        }
        return count;
    }

    public static String bodyOf(Request request) throws IOException {
        Buffer buffer = new Buffer();
        request.body().writeTo(buffer);
        return buffer.readUtf8();
    }

    private Response respond(Request request) {
        requests.add(request);

        Stub stub = stubs.get(request.url().encodedPath());
        int code = stub != null ? stub.code : 200;
        String body = stub != null ? stub.body : "{}";

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("fake")
                .body(ResponseBody.create(body, MediaType.parse("application/json")))
                .build();
    }
}

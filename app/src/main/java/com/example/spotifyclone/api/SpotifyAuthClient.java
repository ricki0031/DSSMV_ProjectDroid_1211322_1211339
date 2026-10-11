package com.example.spotifyclone.api;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SpotifyAuthClient {
    // O cliente HTTP que vai gerir as ligações
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * PASSO 4: Trocar o código PKCE pelo Access Token.
     * Como é um pedido de rede, tem de ser feito de forma assíncrona (usando um Callback).
     */
    public static void getAccessToken(String clientId, String code, String redirectUri, String codeVerifier, Callback callback) {

        // Constrói o corpo do pedido (os parâmetros que o Spotify exige no POST)
        RequestBody formBody = new FormBody.Builder()
                .add("client_id", clientId)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .add("code_verifier", codeVerifier)
                .build();

        // Constrói o pedido apontando para o endpoint de tokens
        Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build();

        // Executa o pedido de forma invisível (em background)
        client.newCall(request).enqueue(callback);
    }
}
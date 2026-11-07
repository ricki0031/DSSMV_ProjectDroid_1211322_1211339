package com.example.spotifyclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bumptech.glide.Glide;
import androidx.recyclerview.widget.LinearLayoutManager;


import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;


import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.example.spotifyclone.api.SpotifyAuthClient;
import com.example.spotifyclone.api.PlayerApiClient;
import com.example.spotifyclone.model.TokenResponse;
import com.example.spotifyclone.model.CurrentlyPlayingResponse;
import com.example.spotifyclone.model.Device;
import com.example.spotifyclone.model.DevicesResponse;
import com.example.spotifyclone.utils.PKCEUtil;
import com.example.spotifyclone.fragment.LibraryFragment;
import com.example.spotifyclone.fragment.HomeFragment;
import com.example.spotifyclone.fragment.SearchFragment;


public class MainActivity extends AppCompatActivity {

    // Constantes para guardar o verifier
    public static final String SHARED_PREFS = "spotify_prefs";
    public static final String CODE_VERIFIER_KEY = "code_verifier";


    private TextView miniPlayerTitle, miniPlayerArtist;
    private ImageView miniPlayerPlayPause, miniPlayerCover;

    private LinearLayout miniPlayerButton;

    private boolean isPlaying = false;
    private String currentTrackId = ""; // Guarda o ID da música atual
    private String globalAccessToken;

    public String globalDeviceId = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupMiniPlayerControls();

        if (getIntent().getData() == null) {
            authenticateWithSpotify();
        }
    }


    private void authenticateWithSpotify() {
        // 1. Gerar o Verifier e o Challenge usando a tua classe de Utils
        String codeVerifier = PKCEUtil.generateCodeVerifier();
        String codeChallenge = PKCEUtil.generateCodeChallenge(codeVerifier);

        // 2. Guardar o codeVerifier nas SharedPreferences (precisaremos dele no Passo 4)
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CODE_VERIFIER_KEY, codeVerifier);
        editor.apply();

        // 3. Definir as permissões (scopes) que a tua app precisa.
        // Para controlar o player, pesquisar e ver álbuns/artistas guardados.
        String scopes = "user-modify-playback-state user-read-playback-state user-read-currently-playing user-library-read user-library-modify user-read-recently-played";

        // 4. Construir o URL de autorização de forma segura usando o Uri.Builder
        Uri uri = Uri.parse("https://accounts.spotify.com/authorize")
                .buildUpon()
                .appendQueryParameter("client_id", BuildConfig.SPOTIFY_CLIENT_ID)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("redirect_uri", BuildConfig.REDIRECT_URI)
                .appendQueryParameter("code_challenge_method", "S256")
                .appendQueryParameter("code_challenge", codeChallenge)
                .appendQueryParameter("scope", scopes)
                .build();

        // 5. Criar o Intent e abrir o browser nativo
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(browserIntent);
    }


    // PASSO 3: Intercetar a resposta quando o browser fecha e volta à app
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith(BuildConfig.REDIRECT_URI)) {
            String code = uri.getQueryParameter("code");

            if (code != null) {
                // SUCESSO! Apanhámos o Código PKCE
                System.out.println("PASSO 3 SUCESSO - CÓDIGO RECEBIDO: " + code);

                // Aqui vamos avançar para o PASSO 4 (Trocar o código pelo token)
                getTokenFromSpotify(code);

            } else {
                String error = uri.getQueryParameter("error");
                System.out.println("ERRO NA AUTORIZAÇÃO: " + error);
            }
        }
    }

    // PASSO 4: Enviar o código para a API e receber o JSON com os Tokens
    private void getTokenFromSpotify(String code) {
        // Recuperar o verifier original das SharedPreferences
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String codeVerifier = prefs.getString(CODE_VERIFIER_KEY, null);

        if (codeVerifier != null) {
            SpotifyAuthClient.getAccessToken(
                    BuildConfig.SPOTIFY_CLIENT_ID,
                    code,
                    BuildConfig.REDIRECT_URI,
                    codeVerifier,
                    new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            System.out.println("ERRO DE REDE: Falha ao comunicar com o Spotify.");
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful() && response.body() != null) {
                                // SUCESSO ABSOLUTO! Aqui está o JSON que contém o teu Access Token!
                                String jsonResponse = response.body().string();

                                // 1. Converter o texto JSON para o nosso objeto Java limpo
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                TokenResponse tokenResponse = gson.fromJson(jsonResponse, TokenResponse.class);

                                // 2. Guardar os tokens nas SharedPreferences de forma permanente
                                SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("access_token", tokenResponse.getAccessToken());
                                editor.putString("refresh_token", tokenResponse.getRefreshToken());
                                editor.apply();

                                System.out.println("TOKENS GUARDADOS COM SUCESSO!");

                                globalAccessToken = tokenResponse.getAccessToken();

                                fetchActiveDevice(globalAccessToken);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setupBottomNavigation(globalAccessToken);
                                        syncMiniPlayer(globalAccessToken);
                                    }
                                });


                                // Aqui, futuramente, redirecionarás o utilizador para o HomeFragment/Ecrã Principal
                            } else {
                                System.out.println("ERRO DO SERVIDOR: " + response.code());
                            }
                        }
                    }
            );
        } else {
            System.out.println("Erro: code_verifier não encontrado nas SharedPreferences.");
        }
    }

    public void fetchActiveDevice(String token) {
        PlayerApiClient.getAvailableDevices(token, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    try {
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        DevicesResponse devicesResponse = gson.fromJson(json, DevicesResponse.class);

                        if (devicesResponse != null && devicesResponse.devices != null) {
                            for (Device device : devicesResponse.devices) {
                                if (device.isActive || "Smartphone".equalsIgnoreCase(device.type)) {
                                    globalDeviceId = device.id;
                                    System.out.println("DISPOSITIVO DETETADO: " + globalDeviceId);
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("ERRO GSON (Dispositivos): " + e.getMessage());
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
                // Passar o token para a Home
                Bundle args = new Bundle();
                args.putString("ACCESS_TOKEN", accessToken);
                selectedFragment.setArguments(args);

            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
                // Passar o token para a Search
                Bundle args = new Bundle();
                args.putString("ACCESS_TOKEN", accessToken);
                selectedFragment.setArguments(args);

            } else if (itemId == R.id.nav_library) {
                selectedFragment = new LibraryFragment();
                // Passar o token para a Library
                Bundle args = new Bundle();
                args.putString("ACCESS_TOKEN", accessToken);
                selectedFragment.setArguments(args);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Agora simulamos o clique na Home logo após o login para carregar a página principal!
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    public void syncMiniPlayer(String accessToken) {
        this.globalAccessToken = accessToken;

        PlayerApiClient.getCurrentlyPlayingTrack(accessToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("ERRO: Falha ao obter estado do Mini-Player.");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();

                    // Se o JSON estiver vazio (código 204), não há nada a tocar
                    if (json.isEmpty()) {
                        runOnUiThread(() -> {
                            // Podes esconder o Mini-Player aqui se quiseres
                            System.out.println("Nenhuma música a tocar no momento.");
                        });
                        return;
                    }

                    try {
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        // Aqui o Gson usa o teu novo modelo!
                        CurrentlyPlayingResponse current = gson.fromJson(json, CurrentlyPlayingResponse.class);

                        if (current != null && current.item != null) {
                            isPlaying = current.isPlaying;
                            currentTrackId = current.item.id;

                            // Atualizar a UI tem de ser no runOnUiThread
                            runOnUiThread(() -> {
                                // 1. Atualizar Título e Artista
                                miniPlayerTitle.setText(current.item.name);
                                if (current.item.artists != null && !current.item.artists.isEmpty()) {
                                    miniPlayerArtist.setText(current.item.artists.get(0).name);
                                }

                                // 2. Atualizar Ícone de Play/Pause
                                if (isPlaying) {
                                    miniPlayerPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                                } else {
                                    miniPlayerPlayPause.setImageResource(android.R.drawable.ic_media_play);
                                }

                                // 3. Carregar a Capa (reaproveitando a lógica da classe Track/Album)
                                if (current.item.album != null && current.item.album.images != null && !current.item.album.images.isEmpty()) {
                                    Glide.with(MainActivity.this)
                                            .load(current.item.album.images.get(0).url)
                                            .into(miniPlayerCover);
                                }
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("ERRO GSON no Mini-Player: " + e.getMessage());
                    }
                }
            }
        });
    }
    private void setupMiniPlayerControls() {
        // Substitui pelos IDs reais do teu ficheiro XML
        miniPlayerTitle = findViewById(R.id.miniPlayerTitle);
        miniPlayerArtist = findViewById(R.id.miniPlayerArtist);
        miniPlayerPlayPause = findViewById(R.id.miniPlayerPlayPause);
        miniPlayerCover = findViewById(R.id.miniPlayerImage); // Caso tenhas a imagem da capa
        miniPlayerButton = findViewById(R.id.mini_player); // Se tiveres um botão para abrir o player completo

        miniPlayerPlayPause.setOnClickListener(v -> {
            if (globalAccessToken == null) return;

            if (isPlaying) {
                // Se está a tocar, chama o Endpoint para Pausar
                PlayerApiClient.pausePlayback(globalAccessToken, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {}

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            isPlaying = false;
                            runOnUiThread(() -> miniPlayerPlayPause.setImageResource(android.R.drawable.ic_media_play));
                        }
                    }
                });
            } else {
                // Se está pausado, chama o Endpoint para Retomar (Play)
                // Passamos "null" ou vazio caso o teu método suporte retomar sem especificar a faixa
                PlayerApiClient.startResumePlayback(globalAccessToken, null, globalDeviceId, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {}

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            isPlaying = true;
                            runOnUiThread(() -> miniPlayerPlayPause.setImageResource(android.R.drawable.ic_media_pause));
                        }
                    }
                });
            }
        });

        miniPlayerButton.setOnClickListener(v -> {
            if (globalAccessToken == null) return;
            Intent PlayerIntent = new Intent(this, PlayerActivity.class);
            PlayerIntent.putExtra("ACCESS_TOKEN", globalAccessToken);
            startActivity(PlayerIntent);
        });
    }

    // 3. Adiciona o onResume para atualizar a música sempre que voltares à app!
    @Override
    protected void onResume() {
        super.onResume();
        if (globalAccessToken != null) {
            syncMiniPlayer(globalAccessToken);
        }
    }
}








    /*
    private void testFetchAlbum(String accessToken) {
        String albumId = "4aawyAB9vmqN3uQ7FjRGTy"; // Global Warming (Pitbull)

        AlbumApiClient.getAlbum(accessToken, albumId, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("TESTE_ALBUM: Falha na rede.");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    com.google.gson.Gson gson = new com.google.gson.Gson();

                    // Converte o JSON para a classe Album
                    Album album = gson.fromJson(json, Album.class);

                    // IMPORTANTE: Atualizar a UI tem de ser na Thread Principal
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 1. Preencher os textos do ecrã
                            textAlbumName.setText(album.name);
                            if (album.artists != null && !album.artists.isEmpty()) {
                                textAlbumArtist.setText(album.artists.get(0).name);
                            }

                            // 2. Carregar a imagem com o Glide
                            if (album.images != null && !album.images.isEmpty()) {
                                Glide.with(MainActivity.this)
                                        .load(album.images.get(0).url)
                                        .into(imageAlbumCover);
                            }

                            // 3. Injetar a lista de músicas na RecyclerView e lidar com o CLIQUE!
                            if (album.tracks != null && album.tracks.items != null) {
                                TrackAdapter adapter = new TrackAdapter(album.tracks.items, new TrackAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Track track) {
                                        System.out.println("CLICASTE NA MÚSICA: " + track.name);

                                        // Chama a API do Player para tocar esta música!
                                        PlayerApiClient.startResumePlayback(accessToken, track.id, new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                System.out.println("ERRO: Não foi possível contactar o servidor.");
                                            }

                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    System.out.println("SUCESSO! A música '" + track.name + "' devia estar a tocar agora!");
                                                } else {
                                                    // O response.code() ajuda a perceber o que falhou (403, 404, etc.)
                                                    System.out.println("ERRO AO TOCAR (Código " + response.code() + ").");
                                                    System.out.println("Atenção: Precisas de Spotify Premium e da app oficial aberta no fundo.");
                                                }
                                            }
                                        });
                                    }
                                });
                                recyclerViewTracks.setAdapter(adapter);
                            }
                        }
                    });
                } else {
                    System.out.println("TESTE_ALBUM ERRO: " + response.code());
                }
            }
        });
    }*/




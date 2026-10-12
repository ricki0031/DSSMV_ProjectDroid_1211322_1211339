package com.example.spotifyclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.example.spotifyclone.api.SpotifyAuthClient;
import com.example.spotifyclone.model.TokenResponse;
import com.example.spotifyclone.utils.PKCEUtil;
import com.example.spotifyclone.fragment.LibraryFragment;
import com.example.spotifyclone.fragment.HomeFragment;
import com.example.spotifyclone.fragment.SearchFragment;



public class MainActivity extends AppCompatActivity {

    // Constantes para guardar o verifier
    public static final String SHARED_PREFS = "spotify_prefs";
    public static final String CODE_VERIFIER_KEY = "code_verifier";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setupBottomNavigation(tokenResponse.getAccessToken());
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
}
    /*
    private void fetchNewReleases(String accessToken) {
        AlbumApiClient.getNewReleases(accessToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("ERRO DE REDE: Falha ao carregar novos lançamentos. " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Lemos a resposta IMEDIATAMENTE e imprimimos no Logcat para ver o que o Spotify enviou
                String json = response.body().string();
                System.out.println("RESPOSTA SPOTIFY: " + json);

                if (response.isSuccessful()) {
                    try {
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        NewReleasesResponse releases = gson.fromJson(json, NewReleasesResponse.class);

                        System.out.println("TESTE GRELHA: JSON convertido! Álbuns recebidos: " + releases.albums.items.size());

                        // Atualizar a UI na Thread Principal
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (releases.albums != null && releases.albums.items != null) {
                                    AlbumAdapter adapter = new AlbumAdapter(releases.albums.items, new AlbumAdapter.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(Album album) {
                                            Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
                                            intent.putExtra("ACCESS_TOKEN", accessToken);
                                            intent.putExtra("ALBUM_ID", album.id);
                                            startActivity(intent);
                                        }
                                    });
                                    recyclerViewGrid.setAdapter(adapter);
                                    System.out.println("TESTE GRELHA: Adapter associado à RecyclerView com sucesso!");
                                } else {
                                    System.out.println("TESTE GRELHA: Os itens dos álbuns estão a null!");
                                }
                            }
                        });
                    } catch (Exception e) {
                        System.out.println("ERRO GSON (O JSON não bate certo com o nosso Modelo): " + e.getMessage());
                    }
                } else {
                    System.out.println("ERRO API SPOTIFY: Código " + response.code());
                }
            }
        });
    }*/








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




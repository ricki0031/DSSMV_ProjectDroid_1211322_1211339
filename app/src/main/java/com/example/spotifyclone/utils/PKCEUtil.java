package com.example.spotifyclone.utils;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class PKCEUtil {

    /**
     * Passo 1.1: Gera o code_verifier.
     * Cria uma string aleatória criptograficamente segura (tamanho recomendado pelo Spotify e OAuth 2.0).
     */
    public static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);

        // O encoding tem de ser URL_SAFE, sem quebras de linha (NO_WRAP) e sem preenchimento (NO_PADDING)
        return Base64.encodeToString(codeVerifier, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    /**
     * Passo 1.2: Gera o code_challenge a partir do code_verifier.
     * Aplica um hash SHA-256 ao verifier e codifica o resultado em Base64-URL.
     */
    public static String generateCodeChallenge(String codeVerifier) {
        try {
            // Converte a string num array de bytes usando US-ASCII (exigência da norma PKCE)
            byte[] bytes = codeVerifier.getBytes("US-ASCII");

            // Aplica o algoritmo de hash SHA-256
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes, 0, bytes.length);
            byte[] digest = messageDigest.digest();

            // Codifica o hash em Base64-URL seguro
            return Base64.encodeToString(digest, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
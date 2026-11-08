package com.example.spotifyclone.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImageUtils {

    private static final int MAX_BASE64_BYTES = 256 * 1024;

    private ImageUtils() {}

    public static String toSpotifyCoverBase64(Context context, Uri imageUri) throws IOException {
        Bitmap original;
        try (InputStream in = context.getContentResolver().openInputStream(imageUri)) {
            original = BitmapFactory.decodeStream(in);
        }

        if (original == null) {
            throw new IOException("Imagem inválida");
        }

        int side = Math.min(original.getWidth(), original.getHeight());
        int x = (original.getWidth() - side) / 2;
        int y = (original.getHeight() - side) / 2;

        Bitmap square = Bitmap.createBitmap(original, x, y, side, side);
        int targetSize = Math.min(640, side);
        Bitmap scaled = Bitmap.createScaledBitmap(square, targetSize, targetSize, true);

        int quality = 90;
        String base64;
        do {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out);
            base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
            quality -= 10;
        } while (base64.length() > MAX_BASE64_BYTES && quality > 10);

        return base64;
    }
}
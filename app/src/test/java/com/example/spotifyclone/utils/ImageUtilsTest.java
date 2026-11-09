package com.example.spotifyclone.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.GraphicsMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ImageUtilsTest {

    private final Context context = RuntimeEnvironment.getApplication();

    private Uri registerImage(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        Uri uri = Uri.parse("content://test/image");
        shadowOf(context.getContentResolver()).registerInputStream(uri, new ByteArrayInputStream(out.toByteArray()));
        return uri;
    }

    private static Bitmap decode(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Test
    public void toSpotifyCoverBase64_cropsImageToSquare() throws IOException {
        Bitmap result = decode(ImageUtils.toSpotifyCoverBase64(context, registerImage(300, 200)));

        assertEquals(200, result.getWidth());
        assertEquals(200, result.getHeight());
    }

    @Test
    public void toSpotifyCoverBase64_limitsLargeImagesTo640Pixels() throws IOException {
        Bitmap result = decode(ImageUtils.toSpotifyCoverBase64(context, registerImage(1000, 1000)));

        assertEquals(640, result.getWidth());
    }

    @Test
    public void toSpotifyCoverBase64_staysWithinSpotifySizeLimit() throws IOException {
        String base64 = ImageUtils.toSpotifyCoverBase64(context, registerImage(1000, 1000));

        assertTrue(base64.length() <= 256 * 1024);
    }

    @Test
    public void toSpotifyCoverBase64_throwsForInvalidImage() {
        Uri uri = Uri.parse("content://test/invalid");
        shadowOf(context.getContentResolver())
                .registerInputStream(uri, new ByteArrayInputStream(new byte[] {1, 2, 3}));

        assertThrows(IOException.class, () -> ImageUtils.toSpotifyCoverBase64(context, uri));
    }
}

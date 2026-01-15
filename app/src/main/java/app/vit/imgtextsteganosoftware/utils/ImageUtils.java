package app.vit.imgtextsteganosoftware.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class ImageUtils {

  private ImageUtils() {
  }

  public static Bitmap decodeUriToBitmap(@NonNull Context context, @NonNull Uri uri, int maxSize) throws IOException {
    ContentResolver resolver = context.getContentResolver();
    Bitmap bitmap;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      ImageDecoder.Source source = ImageDecoder.createSource(resolver, uri);
      bitmap = ImageDecoder.decodeBitmap(source, (decoder, info, src) -> decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE));
    } else {
      try (InputStream input = resolver.openInputStream(uri)) {
        bitmap = BitmapFactory.decodeStream(input);
      }
    }

    if (bitmap == null) {
      throw new IOException("Bitmap decode failed for uri: " + uri);
    }

    // square center crop then scale to maxSize
    int dimension = Math.min(bitmap.getWidth(), bitmap.getHeight());
    Bitmap square = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - dimension) / 2, (bitmap.getHeight() - dimension) / 2, dimension, dimension);

    if (dimension > maxSize) {
      bitmap = Bitmap.createScaledBitmap(square, maxSize, maxSize, true);
      square.recycle();
    } else {
      bitmap = square;
    }
    bitmap.setPremultiplied(false);
    return bitmap;
  }

  public static long estimateCapacityBytes(@NonNull Bitmap cover) {
    // Algorithm stores up to roughly 2 bits per pixel.
    long bits = (long) cover.getWidth() * cover.getHeight() * 2L;
    return bits / 8L;
  }

  public static String formatBytes(long bytes) {
    if (bytes < 1024) return bytes + " B";
    double kb = bytes / 1024.0;
    if (kb < 1024) return String.format("%.1f KB", kb);
    double mb = kb / 1024.0;
    return String.format("%.2f MB", mb);
  }

  public static Uri saveBitmapToPictures(@NonNull Context context, @NonNull Bitmap bitmap, @NonNull String displayName) throws IOException {
    ContentResolver resolver = context.getContentResolver();
    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
    values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Stego");

    Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    if (uri == null) {
      throw new IOException("Unable to create MediaStore entry");
    }

    try (OutputStream out = resolver.openOutputStream(uri)) {
      if (out == null || !bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
        throw new IOException("Failed to write bitmap");
      }
    }
    return uri;
  }
}

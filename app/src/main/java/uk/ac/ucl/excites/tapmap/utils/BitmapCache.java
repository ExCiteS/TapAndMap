package uk.ac.ucl.excites.tapmap.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.LruCache;
import timber.log.Timber;

/**
 * Created by Michalis Vitos on 21/05/2018.
 */
public class BitmapCache {

  private static BitmapCache ourInstance;

  public static BitmapCache getInstance() {

    if (ourInstance == null)
      ourInstance = new BitmapCache();

    return ourInstance;
  }

  // Rest of the code

  private LruCache<String, Bitmap> memoryCache;
  private final int cacheSize;

  private BitmapCache() {

    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    cacheSize = maxMemory / 8;

    memoryCache = getMemoryCache(cacheSize);

    Timber.d("Created Bitmap cache with %s MB memory out of %s MB", cacheSize / 1024,
        maxMemory / 1024);
  }

  @NonNull
  private LruCache<String, Bitmap> getMemoryCache(final int cacheSize) {
    return new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap bitmap) {
        // The cache size will be measured in kilobytes rather than
        // number of items.
        return bitmap.getByteCount() / 1024;
      }
    };
  }

  public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    if (getBitmapFromMemCache(key) == null) {
      memoryCache.put(key, bitmap);
    }
  }

  public Bitmap getBitmapFromMemCache(String key) {
    return memoryCache.get(key);
  }

  public void clear() {
    memoryCache = null;
    memoryCache = getMemoryCache(cacheSize);
  }
}

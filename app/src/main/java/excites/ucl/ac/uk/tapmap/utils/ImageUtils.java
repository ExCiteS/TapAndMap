package excites.ucl.ac.uk.tapmap.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class for Images
 *
 * Created by Michalis Vitos on 18/05/2018.
 */
public class ImageUtils {

  private static final int THUMBNAIL_SIZE = 600;

  public static Bitmap getThumbnail(String filePath) throws IOException {

    InputStream input = new FileInputStream(new File(filePath));

    BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
    onlyBoundsOptions.inJustDecodeBounds = true;
    onlyBoundsOptions.inDither = true;//optional
    onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
    BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
    input.close();

    if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
      return null;
    }

    int originalSize =
        (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
            : onlyBoundsOptions.outWidth;

    double ratio = (originalSize > THUMBNAIL_SIZE) ? ((double) originalSize / THUMBNAIL_SIZE) : 1.0;

    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
    bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
    bitmapOptions.inDither = true; //optional
    bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
    input = new FileInputStream(new File(filePath));
    Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
    input.close();
    return bitmap;
  }

  private static int getPowerOfTwoForSampleRatio(double ratio) {
    int k = Integer.highestOneBit((int) Math.floor(ratio));
    if (k == 0) {
      return 1;
    } else {
      return k;
    }
  }
}

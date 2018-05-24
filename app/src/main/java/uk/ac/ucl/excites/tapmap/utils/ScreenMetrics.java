package uk.ac.ucl.excites.tapmap.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by Michalis Vitos on 24/05/2018.
 */
public class ScreenMetrics {

  private ScreenMetrics() {
    // Do nothing
  }

  public static float convertPixelsToDp(float px) {
    DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
    float dp = px / (metrics.densityDpi / 160f);
    return Math.round(dp);
  }

  public static float convertDpToPixel(float dp) {
    DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
    float px = dp * (metrics.densityDpi / 160f);
    return Math.round(px);
  }
}

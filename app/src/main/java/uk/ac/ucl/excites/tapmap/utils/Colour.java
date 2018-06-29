/*
 * Tap and Map is part of the Sapelli platform: http://sapelli.org
 * <p/>
 * Copyright 2012-2018 University College London - ExCiteS group
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package uk.ac.ucl.excites.tapmap.utils;

import android.graphics.Color;
import lombok.NoArgsConstructor;

/**
 * Math Utils
 *
 * Adepted from https://stackoverflow.com/questions/7779621/how-to-get-programmatically-a-list-of-colors-from-a-gradient-on-android
 */
@NoArgsConstructor
public class Colour {

  public static int getColorFromGradient(int[] colors, float[] positions, float v) {

    if (colors.length == 0 || colors.length != positions.length) {
      throw new IllegalArgumentException();
    }

    if (colors.length == 1) {
      return colors[0];
    }

    if (v <= positions[0]) {
      return colors[0];
    }

    if (v >= positions[positions.length - 1]) {
      return colors[positions.length - 1];
    }

    for (int i = 1; i < positions.length; ++i) {
      if (v <= positions[i]) {
        float t = (v - positions[i - 1]) / (positions[i] - positions[i - 1]);
        return lerpColor(colors[i - 1], colors[i], t);
      }
    }

    //should never make it here
    throw new RuntimeException();
  }

  private static int lerpColor(int colorA, int colorB, float t) {
    int alpha = (int) Math.floor(Color.alpha(colorA) * (1 - t) + Color.alpha(colorB) * t);
    int red = (int) Math.floor(Color.red(colorA) * (1 - t) + Color.red(colorB) * t);
    int green = (int) Math.floor(Color.green(colorA) * (1 - t) + Color.green(colorB) * t);
    int blue = (int) Math.floor(Color.blue(colorA) * (1 - t) + Color.blue(colorB) * t);

    return Color.argb(alpha, red, green, blue);
  }
}

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

import java.util.Locale;
import lombok.NoArgsConstructor;

/**
 * Math Utils
 *
 * Created by Michalis Vitos on 29/06/2018.
 */
@NoArgsConstructor
public class Time {

  /**
   * Get a human readable time from seconds
   */
  public static String convertSecondsToHumanTime(int totalSeconds) {

    int hours = totalSeconds / 3600;
    int minutes = (totalSeconds % 3600) / 60;
    int seconds = totalSeconds % 60;

    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
  }
}

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

import android.support.design.widget.Snackbar;
import android.view.View;
import lombok.NoArgsConstructor;

/**
 * UI Utils
 */
@NoArgsConstructor
public class UiUtils {

  /**
   * Show a Snackbar with a given message and a Dismiss button
   */
  public static void showSnackBarWithDismiss(View view, String message) {

    final Snackbar snackbar = Snackbar.make(view,
        message,
        Snackbar.LENGTH_INDEFINITE);
    snackbar.setAction("Dismiss", v -> snackbar.dismiss());
    snackbar.show();
  }
}

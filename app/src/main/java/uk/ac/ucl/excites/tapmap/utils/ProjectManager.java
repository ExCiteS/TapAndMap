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

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import lombok.NoArgsConstructor;
import timber.log.Timber;

/**
 * Created by Michalis Vitos on 03/09/2018.
 */
@NoArgsConstructor
public class ProjectManager {

  private static final String IMAGES_DIRECTORY = "images";
  private static final String SETTINGS_NAME = "settings.json";

  public static File getImagesDirectory(Context context) {
    return context.getDir(IMAGES_DIRECTORY, Context.MODE_PRIVATE);
  }

  public static JsonObject loadSettingsInDirectory(File directory) throws FileNotFoundException {

    final String settings = directory + File.separator + SETTINGS_NAME;
    Timber.d("Try to load Settings at: %s", settings);
    final BufferedReader bufferedReader = new BufferedReader(new FileReader(settings));

    final Gson gson = new Gson();
    final JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);

    Timber.d("JSON: %s", json.toString());

    return json;
  }
}

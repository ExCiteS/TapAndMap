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

package uk.ac.ucl.excites.tapmap.project;

import android.content.Context;
import android.support.annotation.NonNull;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import timber.log.Timber;

/**
 * Created by Michalis Vitos on 03/09/2018.
 */
public class ProjectManager {

  private ProjectManager() {
    throw new IllegalStateException("Utility class");
  }

  private static final String IMAGES_DIRECTORY = "images";
  private static final String SETTINGS_NAME = "settings.json";

  public static File getImagesDirectory(Context context) {
    return context.getDir(IMAGES_DIRECTORY, Context.MODE_PRIVATE);
  }

  @NonNull
  private static File getSettingsFile(File directory) {
    return new File(directory + File.separator + SETTINGS_NAME);
  }

  public static Settings loadSettingsFromDirectory(File directory) throws FileNotFoundException {

    final File settingsFile = getSettingsFile(directory);
    Timber.d("Try to load Settings at: %s", settingsFile);
    final BufferedReader bufferedReader = new BufferedReader(new FileReader(settingsFile));

    final Gson gson = new Gson();
    final Settings settings = gson.fromJson(bufferedReader, Settings.class);

    Timber.d(settings.toString());

    return settings;
  }

  public static void writeSettingsToDirectory(Settings settings, File directory)
      throws IOException {

    final File settingsFile = getSettingsFile(directory);
    Timber.d("Try to store Settings at: %s", settingsFile);
    final Gson gson = new Gson();
    final String json = gson.toJson(settings);

    // Write settings to file
    try (FileWriter writer = new FileWriter(settingsFile)) {
      writer.write(json);
    }
  }
}

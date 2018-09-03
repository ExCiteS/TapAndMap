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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.NoArgsConstructor;
import timber.log.Timber;

/**
 * Created by Michalis Vitos on 03/09/2018.
 */
@NoArgsConstructor
public class FileUtils {

  public static void copyFile(InputStream inputStream, File destinationFile) {

    FileOutputStream outputStream = null;

    try {
      outputStream = new FileOutputStream(destinationFile);

      // Copy file
      byte[] buffer = new byte[1024];
      int len;
      while ((len = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, len);
      }
    } catch (Exception e) {
      Timber.e(e, "Error while copying the file.");
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          Timber.e(e, "Error while copying the file and closing the FileOutputStream.");
        }
      }
    }
  }
}

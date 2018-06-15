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

package uk.ac.ucl.excites.tapmap.storage;

import android.arch.persistence.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Convert Gson objects to Room compatible Strings and vice versa
 *
 * Created by Michalis Vitos on 15/06/2018.
 */
public class MetaTypeConverter {

  @TypeConverter
  public static JsonObject toJsonObject(String string) {
    final Gson gson = new Gson();
    return gson.fromJson(string, JsonObject.class);
  }

  @TypeConverter
  public static String toString(JsonObject json) {
    return json == null ? null : json.toString();
  }
}

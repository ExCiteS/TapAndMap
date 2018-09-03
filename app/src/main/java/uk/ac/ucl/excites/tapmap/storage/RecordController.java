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

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Date;
import uk.ac.ucl.excites.tapmap.TapMap;

/**
 * Created by Michalis Vitos on 01/06/2018.
 */
public class RecordController {

  private RecordDao recordDao;
  private Gson gson;

  public RecordController(Context context) {

    TapMap tapMap = (TapMap) context.getApplicationContext();

    this.recordDao = tapMap.getAppDatabase().recordDao();
    this.gson = new Gson();
  }

  /**
   * Store the card and return the id or -1 if the operation fails
   */
  public long storeCard(ImageCard imageCard, JsonObject meta) {

    if (imageCard == null)
      return -1;

    final Record record = new Record();
    // Set time to now
    record.setDate(new Date());
    // Set tag
    record.setTag(imageCard.getTag());
    // Set meta
    if (meta != null)
      record.setMeta(meta);

    // Store and return the id of the record
    return recordDao.insert(record);
  }
}

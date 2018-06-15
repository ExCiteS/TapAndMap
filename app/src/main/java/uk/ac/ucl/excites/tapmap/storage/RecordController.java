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
import lombok.AllArgsConstructor;
import uk.ac.ucl.excites.tapmap.TapMap;

/**
 * Created by Michalis Vitos on 01/06/2018.
 */
@AllArgsConstructor
public class RecordController {

  private NfcCardDao cardDao;
  private RecordDao recordDao;
  private Gson gson;

  public RecordController(Context context) {

    TapMap tapMap = (TapMap) context.getApplicationContext();

    this.cardDao = tapMap.getAppDatabase().nfcCardDao();
    this.recordDao = tapMap.getAppDatabase().recordDao();
    this.gson = new Gson();
  }

  public long storeCard(NfcCard card) {
    final Record record = new Record();
    // Set time to now
    record.setDate(new Date());
    // Set tag
    record.setTag(card.getTag());
    // Set meta
    record.setMeta(generateMeta(card));

    // Store and return the id of the record
    return recordDao.insert(record);
  }

  /**
   * Build the JsonObject for a card
   */
  private JsonObject generateMeta(NfcCard card) {

    JsonObject meta = new JsonObject();
    meta.add("card", gson.toJsonTree(card));

    // TODO: 15/06/2018 In the future keep adding elements to the meta object

    return meta;
  }
}

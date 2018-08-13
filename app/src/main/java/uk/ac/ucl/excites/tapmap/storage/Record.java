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

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import com.google.gson.JsonObject;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Michalis Vitos on 15/06/2018.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Record {

  /**
   * Auto-generated ID
   */
  @PrimaryKey(autoGenerate = true)
  private long id;

  /**
   * The date (converted to timestamp in Room) of the record
   */
  @ColumnInfo(name = "date")
  private Date date;

  /**
   * A tag that describes the record. This comes from the tag of the NFC card. For example this
   * could be "banana" etc.
   */
  @ColumnInfo(name = "tag")
  private String tag;

  /**
   * A {@link JsonObject} meta object to store various meta information for this record. This could
   * for example be the NFC card info, the location captured, the media captured etc.
   */
  @ColumnInfo(name = "meta")
  private JsonObject meta;

  public String[] toCSV() {
    final String dateField = (date != null) ? date.toString() : "";
    final String metaField = (meta != null) ? meta.toString() : "";
    return new String[] {
        String.valueOf(id), dateField, tag, metaField
    };
  }
}

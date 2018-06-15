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
import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Representation of a NFC card stored in the database. Each NFC card has an ID that was is the
 * actual NFC id that comes with each card. Apart from the ID, each card has also an image associated
 * with the card.
 *
 * Created by Michalis Vitos on 21/05/2018.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class NfcCard {

  /**
   * ID of the card. This is the actual ID that each NFC card comes with.
   */
  @PrimaryKey
  private String id;

  /**
   * This is the location where the image associated to this NFC card is stored. Usually this will be
   * something like /data/user/0/uk.ac.ucl.excites.tapmap/files/<card_id>. This is excluded from a GSON
   * conversion toJson.
   */
  @Expose(serialize = false, deserialize = false)
  @ColumnInfo(name = "image_path")
  private String imagePath;

  /**
   * The actual file name of the picked image for this NFC card. For example this could be banana.png
   * This could be used later to map the results on a map and use the same images as icons.
   */
  @ColumnInfo(name = "image_filename")
  private String filename;

  /**
   * This is a textual tag used on the NFC card for later analysis. For example this could be banana etc.
   */
  @ColumnInfo(name = "tag")
  private String tag;
}

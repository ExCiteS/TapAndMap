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
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representation of a NFC card stored in the database. Each NFC card has an ID that was is the
 * actual NFC id that comes with each card. Apart from the ID, each card has also an image associated
 * with the card.
 *
 * Created by Michalis Vitos on 21/05/2018.
 */
@Entity(foreignKeys = @ForeignKey(
    entity = ImageCard.class,
    parentColumns = "filename",
    childColumns = "image_card_id",
    onDelete = ForeignKey.CASCADE
), indices = { @Index("image_card_id") }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NfcCard {

  /**
   * ID of the card. This is the actual ID that each NFC card comes with.
   */
  @NonNull
  @PrimaryKey
  private String id;

  @ColumnInfo(name = "image_card_id")
  public String imageCardId;

  /**
   * Convert Card to Json
   */
  public JsonObject toJson() {
    final Gson gson = new Gson();
    return gson.toJsonTree(this).getAsJsonObject();
  }
}

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
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Representation of a collection session. This could be associated with for example a participant
 * collecting some data before handing the device back etc. Each session has a start and end time
 * as well as a description that explains the purpose of the session.
 *
 * Created by Michalis Vitos on 21/05/2018.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Session {

  /**
   * Auto-generated ID
   */
  @PrimaryKey(autoGenerate = true)
  private long id;

  /**
   * The description that explains the purpose of this session. For example: "Michalis session collecting trees" etc
   */
  @ColumnInfo(name = "description")
  private String description;

  /**
   * The start time of the session
   */
  @ColumnInfo(name = "start_time")
  private Date startTime;

  /**
   * The end time of the session
   */
  @ColumnInfo(name = "end_time")
  private Date endTime;

  public String[] toCSV() {
    final String start = (startTime != null) ? startTime.toString() : "";
    final String end = (endTime != null) ? endTime.toString() : "";
    return new String[] {
        String.valueOf(id), description, start, end
    };
  }
}

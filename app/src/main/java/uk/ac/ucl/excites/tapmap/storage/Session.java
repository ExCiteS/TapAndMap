package uk.ac.ucl.excites.tapmap.storage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Michalis Vitos on 21/05/2018.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Session {

  @PrimaryKey(autoGenerate = true)
  private long id;

  @ColumnInfo(name = "description")
  private String description;

  @ColumnInfo(name = "start_time")
  private Date startTime;

  @ColumnInfo(name = "end_time")
  private Date endTime;
}

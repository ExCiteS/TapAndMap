package uk.ac.ucl.excites.tapmap.storage;

import android.arch.persistence.room.TypeConverter;
import java.util.Date;

/**
 * Created by Michalis Vitos on 01/06/2018.
 */
public class DateTypeConverter {

  @TypeConverter
  public static Date toDate(Long value) {
    return value == null ? null : new Date(value);
  }

  @TypeConverter
  public static Long toLong(Date value) {
    return value == null ? null : value.getTime();
  }
}
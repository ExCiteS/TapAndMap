package uk.ac.ucl.excites.tapmap.storage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by Michalis Vitos on 21/05/2018.
 */
@Database(entities = { NfcCard.class }, version = 1)
public abstract class AppDatabase extends RoomDatabase {

  public static final String DATABASE_NAME = "TapMapDatabase.sqlite";

  public abstract NfcCardDao nfcCardDao();

  public static AppDatabase init(Context context) {
    return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
        .build();
  }
}

package uk.ac.ucl.excites.tapmap.storage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

/**
 * Created by Michalis Vitos on 21/05/2018.
 */
@Database(entities = { NfcCard.class, Session.class }, version = 1)
@TypeConverters({ DateTypeConverter.class })
public abstract class AppDatabase extends RoomDatabase {

  public static final String DATABASE_NAME = "TapMapDatabase.sqlite";

  public abstract NfcCardDao nfcCardDao();

  public abstract SessionDao sessionDao();

  public static AppDatabase init(Context context) {
    return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
        .allowMainThreadQueries() // TODO: 21/05/2018 Maybe remove this in the future
        .fallbackToDestructiveMigration() // TODO: 01/06/2018 Remove this in the future
        .build();
  }
}

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

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

/**
 * Created by Michalis Vitos on 21/05/2018.
 */
@Database(entities = { NfcCard.class, ImageCard.class, Session.class, Record.class }, version = 1)
@TypeConverters({ DateTypeConverter.class, MetaTypeConverter.class })
public abstract class AppDatabase extends RoomDatabase {

  public static final String DATABASE_NAME = "TapMapDatabase.sqlite";

  public abstract NfcCardDao nfcCardDao();

  public abstract ImageCardDao imageCardDao();

  public abstract SessionDao sessionDao();

  public abstract RecordDao recordDao();

  public static AppDatabase init(Context context) {
    return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
        .allowMainThreadQueries() // TODO: 21/05/2018 Maybe remove this in the future
        .fallbackToDestructiveMigration() // TODO: 01/06/2018 Remove this in the future
        .build();
  }
}

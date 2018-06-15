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

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import io.reactivex.Single;
import java.util.List;

/**
 * Created by Michalis Vitos on 21/05/2018.
 */
@Dao
public interface NfcCardDao {

  @Query("SELECT * FROM NfcCard")
  Single<List<NfcCard>> getAll();

  @Query("SELECT * FROM NfcCard WHERE id IN (:nfcIds)")
  List<NfcCard> findAllByIds(int[] nfcIds);

  @Query("SELECT * FROM NfcCard WHERE id = :id LIMIT 1")
  NfcCard findById(String id);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insert(NfcCard card);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  List<Long> insert(NfcCard... cards);

  @Delete
  void delete(NfcCard card);
}

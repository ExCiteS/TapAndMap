package uk.ac.ucl.excites.tapmap.storage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import io.reactivex.Flowable;
import io.reactivex.Observable;
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
  void insert(NfcCard card);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(NfcCard... cards);

  @Delete
  void delete(NfcCard card);
}

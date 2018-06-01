package uk.ac.ucl.excites.tapmap.storage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import java.util.List;

/**
 * Created by Michalis Vitos on 21/05/2018.
 */
@Dao
public interface SessionDao {

  @Query("SELECT * FROM Session")
  List<Session> getAll();

  @Query("SELECT * FROM Session WHERE id IN (:sessions)")
  List<Session> findAllByIds(long[] sessions);

  @Query("SELECT * FROM Session WHERE id = :id LIMIT 1")
  Session findById(long id);

  @Query("SELECT * FROM Session ORDER BY id DESC LIMIT 1;")
  Session findLast();

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insert(Session session);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long[] insert(Session... session);

  @Delete
  void delete(Session card);
}

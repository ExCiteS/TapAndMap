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
  List<Session> findAllByIds(int[] sessions);

  @Query("SELECT * FROM Session WHERE id = :id LIMIT 1")
  Session findById(String id);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Session session);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Session... session);

  @Delete
  void delete(Session card);
}

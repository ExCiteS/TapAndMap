package excites.ucl.ac.uk.tapmap.storage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import java.util.List;

/**
 * Created by Michalis Vitos on 21/05/2018.
 */
@Dao
public interface NfcCardDao {

  @Query("SELECT * FROM NfcCard")
  List<NfcCard> getAll();

  @Query("SELECT * FROM NfcCard WHERE id IN (:nfcIds)")
  List<NfcCard> findAllByIds(int[] nfcIds);

  @Query("SELECT * FROM NfcCard WHERE id = :id LIMIT 1")
  NfcCard findById(String id);

  @Insert
  void insert(NfcCard card);

  @Insert
  void insert(NfcCard... cards);

  @Delete
  void delete(NfcCard card);
}

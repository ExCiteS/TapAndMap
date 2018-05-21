package uk.ac.ucl.excites.tapmap.storage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Michalis Vitos on 21/05/2018.
 */
@Entity
@Getter
@Setter
@ToString
public class NfcCard {

  @NonNull
  @PrimaryKey
  private String id;

  @ColumnInfo(name = "image_path")
  private String imagePath;
}

package uk.ac.ucl.excites.tapmap.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;

/**
 * Created by Michalis Vitos on 10/08/2018.
 */
public class ExportActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_export);
    ButterKnife.bind(this);

    // Get database daos
    final TapMap app = (TapMap) getApplication();
  }

}

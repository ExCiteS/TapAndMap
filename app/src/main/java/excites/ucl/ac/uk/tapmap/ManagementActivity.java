package excites.ucl.ac.uk.tapmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import timber.log.Timber;

public class ManagementActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_managment);

    Timber.d("Hi from the ManagementActivity");
  }
}

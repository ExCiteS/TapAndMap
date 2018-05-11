package excites.ucl.ac.uk.tapmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import excites.ucl.ac.uk.tapmap.activities.NFCActivity;
import timber.log.Timber;

public class ManagementActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_managment);

    Timber.d("Hi from the ManagementActivity");
  }

  public void openNFCActivity(View view) {
    Timber.d("Go to NFC activity");

    Intent intent = new Intent(this, NFCActivity.class);
    startActivity(intent);
  }
}

package uk.ac.ucl.excites.tapmap.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.utils.Logger;

public class MainActivity extends AppCompatActivity {

  private static final Integer READ_WRITE_EXTERNAL_STORAGE = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    checkStoragePermission();
  }

  private void checkStoragePermission() {

    final MainActivity activity = MainActivity.this;
    final String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    final String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;
    final String[] permissions = { writeExternalStorage, readExternalStorage };

    final boolean writeAccess = ContextCompat.checkSelfPermission(activity, writeExternalStorage)
        == PackageManager.PERMISSION_GRANTED;
    final boolean readAccess = ContextCompat.checkSelfPermission(activity, readExternalStorage)
        == PackageManager.PERMISSION_GRANTED;

    if (!writeAccess || !readAccess) {

      // Should we show an explanation?
      final boolean writeAskForExplanation =
          ActivityCompat.shouldShowRequestPermissionRationale(activity, writeExternalStorage);
      final boolean readAskForExplanation =
          ActivityCompat.shouldShowRequestPermissionRationale(activity, readExternalStorage);

      if (writeAskForExplanation || readAskForExplanation) {
        // TODO: 08/06/2018 Might want to show our own dialog why we need storage permissions here
      }
      ActivityCompat.requestPermissions(activity, permissions, READ_WRITE_EXTERNAL_STORAGE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {

    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (ActivityCompat.checkSelfPermission(this, permissions[0])
        == PackageManager.PERMISSION_GRANTED) {

      Timber.d("Granted permissions: %s", permissions);
      Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();

      // Set up Logger now
      Logger logger = Logger.getInstance();
      logger.log("Storage permission is granted");
    } else {
      Toast.makeText(this, "Permission denied, you can't use our app.", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  @OnClick(R.id.btn_collectData)
  public void openSessionActivity() {
    Timber.d("Go to NFC activity");

    Intent intent = new Intent(this, SessionActivity.class);
    startActivity(intent);
  }

  @OnClick(R.id.btn_manageCards)
  public void openListActivity() {
    Timber.d("Go to List activity");

    Intent intent = new Intent(this, ListActivity.class);
    startActivity(intent);
  }
}

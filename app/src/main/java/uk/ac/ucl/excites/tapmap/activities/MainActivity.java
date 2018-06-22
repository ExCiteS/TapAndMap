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

package uk.ac.ucl.excites.tapmap.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.Observable;
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

    // Check if we have the appropriate permissions before we do anything else
    checkStoragePermission();
  }

  /**
   * Check if we have the appropriate permissions
   */
  @SuppressLint("CheckResult")
  private void checkStoragePermission() {

    final MainActivity activity = MainActivity.this;
    final RxPermissions rxPermissions = new RxPermissions(activity);
    final String[] permissions = {
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    };

    Observable.fromArray(permissions)
        .doOnNext(permission -> Timber.d("Checking permission: %s", permission))
        // Filter permissions that are already granted and keep just the denied
        .filter(permission -> !rxPermissions.isGranted(permission))
        .doOnNext(permission -> Timber.d("Remained permission: %s", permission))
        // Request each of them
        .flatMap(rxPermissions::request)
        .subscribe(granted -> {
              if (granted) {
                // All requested permissions are granted
                onPermissionsGranted();
              } else {
                // At least one permission is denied
                Toast.makeText(this, "Permission denied, you can't use our app.", Toast.LENGTH_SHORT)
                    .show();
                finish();
              }
            },
            error -> Timber.d("error: %s", error.getLocalizedMessage()),
            () -> Timber.d("Completed")
        );
  }

  /**
   * Actions to be performed when we have all the necessary permissions
   */
  private void onPermissionsGranted() {

    Timber.d("We have all the permissions! Yeah!");
    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();

    // Set up Logger now
    Logger.getInstance();

    // Add shortcuts now
    createShortcutOfTapAndMapActivity();
  }

  /**
   * Create a shortcut for the Tap and Map activity
   */
  private void createShortcutOfTapAndMapActivity() {

    final Intent shortcutIntent = new Intent(getApplicationContext(), TapAndMapActivity.class);
    shortcutIntent.setAction(Intent.ACTION_MAIN);

    final Intent addIntent = new Intent();
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Tap and Map Collect");
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
        Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_collect));

    addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
    // Don't duplicate the shortcut, if it already exists
    addIntent.putExtra("duplicate", false);
    getApplicationContext().sendBroadcast(addIntent);
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

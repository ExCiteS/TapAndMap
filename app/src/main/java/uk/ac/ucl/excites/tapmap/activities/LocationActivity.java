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

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.gson.JsonObject;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.controllers.NavigationController;

import static uk.ac.ucl.excites.tapmap.controllers.NavigationController.Screens.LOCATION;

public class LocationActivity extends AppCompatActivity implements LocationListener {

  public static int MIN_ACCURACY_IN_METRES = 30;
  public static int TIMEOUT_IN_MINS = 5;

  @BindView(R.id.progressBar)
  protected ProgressBar progressBar;

  private LocationManager locationManager;

  @SuppressLint("MissingPermission")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_location);
    ButterKnife.bind(this);

    // Acquire a reference to the system Location Manager
    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    // Register the listener with the Location Manager to receive location updates
    if (locationManager != null)
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
          0,
          0,
          this);

    // Add a GPS Timeout, call tick every 10 seconds
    final int timeoutInMillis = TIMEOUT_IN_MINS * 60 * 1000;
    final CountDownTimer timeout = new CountDownTimer(timeoutInMillis, 10 * 1000) {

      public void onTick(long millisUntilFinished) {
        Timber.d("Seconds Remaining: %s", millisUntilFinished / 1000);
      }

      public void onFinish() {

        // Store location
        final JsonObject meta = new JsonObject();
        meta.addProperty("message",
            "Could not fix GPS, the app timed out after " + TIMEOUT_IN_MINS + " minutes.");

        // Go to next screen
        final NavigationController navigationController = NavigationController.getInstance();
        navigationController.setCurrentScreen(LOCATION, meta);
        navigationController.goToNextScreen(LocationActivity.this);
      }
    };
    timeout.start();
  }

  @Override
  public void onLocationChanged(Location location) {
    Timber.d("Location: %s", location);

    if (location.getAccuracy() <= MIN_ACCURACY_IN_METRES) {

      // Hide Progress bar
      progressBar.setVisibility(View.GONE);

      // Store location
      final JsonObject meta = new JsonObject();
      meta.addProperty("lat", location.getLatitude());
      meta.addProperty("lon", location.getLongitude());
      meta.addProperty("alt", location.getAltitude());
      meta.addProperty("acc", location.getAccuracy());
      meta.addProperty("speed", location.getSpeed());
      meta.addProperty("provider", location.getProvider());
      meta.addProperty("bearing", location.getBearing());
      meta.addProperty("time", location.getTime());

      // Go to next screen
      final NavigationController navigationController = NavigationController.getInstance();
      navigationController.setCurrentScreen(LOCATION, meta);
      navigationController.goToNextScreen(this);
    }
  }

  @Override
  public void onStatusChanged(String s, int i, Bundle bundle) {

  }

  @Override
  public void onProviderEnabled(String s) {

  }

  @Override
  public void onProviderDisabled(String s) {

  }

  @Override
  protected void onPause() {
    super.onPause();
    locationManager.removeUpdates(this);
  }
}

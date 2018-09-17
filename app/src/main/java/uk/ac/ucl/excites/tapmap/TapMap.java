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

package uk.ac.ucl.excites.tapmap;

import android.app.Application;
import com.facebook.stetho.Stetho;
import gr.michalisvitos.timberutils.CrashlyticsTree;
import gr.michalisvitos.timberutils.DebugTree;
import lombok.Getter;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.storage.AppDatabase;
import uk.ac.ucl.excites.tapmap.utils.LifeCycleMonitor;

/**
 * Created by Michalis Vitos
 */
public class TapMap extends Application {

  @Getter
  AppDatabase appDatabase;

  @Override
  public void onCreate() {
    super.onCreate();

    // Initialise development tools
    initDevTools();

    // Init the database
    appDatabase = AppDatabase.init(this);

    // Set a LifeCycleMonitor
    new LifeCycleMonitor(this);
  }

  public void initDevTools() {
    // Set up Crashlytics, disabled for debug builds
    // setFabric();

    // Set up Facebook Stetho for debugging
    setStetho();

    // Set Timber
    setTimber();
  }

  private void setStetho() {

    // Enable Stetho in Debug versions
    if (!BuildConfig.DEBUG)
      return;

    Timber.d("Initialize Stetho...");
    Stetho.initializeWithDefaults(this);
  }

  private void setTimber() {

    if (BuildConfig.DEBUG) {
      Timber.plant(new DebugTree());
    } else {
      Timber.plant(new CrashlyticsTree());
    }
  }
}

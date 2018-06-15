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

package uk.ac.ucl.excites.tapmap.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.storage.SessionController;

import static uk.ac.ucl.excites.tapmap.utils.Logger.TAG.ANDROID;

/**
 * Created by Michalis Vitos on 08/06/2018.
 */
public class LifeCycleMonitor
    implements Application.ActivityLifecycleCallbacks, ComponentCallbacks, ComponentCallbacks2 {

  public LifeCycleMonitor(Application application) {
    application.registerActivityLifecycleCallbacks(this);
    application.registerComponentCallbacks(this);
  }

  public void release(Application application) {
    application.unregisterActivityLifecycleCallbacks(this);
    application.unregisterComponentCallbacks(this);
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    // Do nothing
  }

  @Override
  public void onActivityStarted(Activity activity) {
    log(activity, "onActivityStarted()");
  }

  @Override
  public void onActivityResumed(Activity activity) {
    log(activity, "onActivityResumed()");
  }

  @Override
  public void onActivityPaused(Activity activity) {
    log(activity, "onActivityPaused()");
  }

  @Override
  public void onActivityStopped(Activity activity) {
    // Do nothing
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    // Do nothing
  }

  @Override
  public void onActivityDestroyed(Activity activity) {
    log(activity, "onActivityDestroyed()");
  }

  private void log(Activity activity, String method) {
    final SessionController sessionController = new SessionController(activity);
    final String session = "SESSION:" + sessionController.getActiveSessionId();
    Logger.getInstance().log(ANDROID, session, activity.getLocalClassName() + "." + method);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    // Do nothing
  }

  @Override
  public void onLowMemory() {
    // Do nothing
  }

  @Override
  public void onTrimMemory(int level) {
    // Do nothing
  }
}
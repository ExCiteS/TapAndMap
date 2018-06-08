package uk.ac.ucl.excites.tapmap.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.storage.SessionDao;

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
    final TapMap app = (TapMap) activity.getApplication();
    final SessionDao sessionDao = app.getAppDatabase().sessionDao();
    final String session = "SESSION:" + sessionDao.findLast().getId();
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
package uk.ac.ucl.excites.tapmap;

import android.app.Application;
import com.facebook.stetho.Stetho;
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
      // TODO Enable Crashlytics
      // Timber.plant(new CrashlyticsTree());
    }
  }
}

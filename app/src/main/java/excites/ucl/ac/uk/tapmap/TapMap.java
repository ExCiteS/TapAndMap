package excites.ucl.ac.uk.tapmap;

import android.app.Application;
import com.facebook.stetho.Stetho;
import timber.log.Timber;

/**
 * Created by Michalis Vitos
 */
public class TapMap extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Initialise development tools
    initDevTools();
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
    if (!BuildConfig.DEBUG) return;

    Timber.d("Initialize Stetho...");
    Stetho.initializeWithDefaults(this);
  }

  private void setTimber() {

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    } else {
      // TODO Enable Crashlytics
      // Timber.plant(new CrashlyticsTree());
    }
  }
}

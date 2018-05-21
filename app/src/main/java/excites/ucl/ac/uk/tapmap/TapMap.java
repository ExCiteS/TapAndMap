package excites.ucl.ac.uk.tapmap;

import android.app.Application;
import com.facebook.stetho.Stetho;
import excites.ucl.ac.uk.tapmap.storage.AppDatabase;
import lombok.Getter;
import timber.log.Timber;

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

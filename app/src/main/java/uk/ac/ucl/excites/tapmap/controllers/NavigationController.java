package uk.ac.ucl.excites.tapmap.controllers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.activities.AudioRecordingActivity;
import uk.ac.ucl.excites.tapmap.activities.ListActivity;
import uk.ac.ucl.excites.tapmap.activities.SessionActivity;
import uk.ac.ucl.excites.tapmap.activities.SettingsActivity;
import uk.ac.ucl.excites.tapmap.activities.TapAndMapActivity;

import static uk.ac.ucl.excites.tapmap.controllers.NavigationController.Screens.AUDIO;
import static uk.ac.ucl.excites.tapmap.controllers.NavigationController.Screens.LOCATION;
import static uk.ac.ucl.excites.tapmap.controllers.NavigationController.Screens.MAIN;
import static uk.ac.ucl.excites.tapmap.controllers.NavigationController.Screens.NFC;
import static uk.ac.ucl.excites.tapmap.controllers.NavigationController.Screens.SESSION;

/**
 * Created by Michalis Vitos on 02/08/2018.
 */
public class NavigationController {

  private static final NavigationController ourInstance = new NavigationController();

  public enum Screens {
    MAIN, SESSION, AUDIO, NFC, LOCATION
  }

  public static NavigationController getInstance() {
    return ourInstance;
  }

  private List<Screens> screensOrder;
  private SharedPreferences sharedPreferences;

  private NavigationController() {
    // Do nothing
  }

  public void init(Context context) {

    // Set Screens Order
    screensOrder = new ArrayList<>();
    screensOrder.add(MAIN);
    screensOrder.add(SESSION);
    screensOrder.add(AUDIO);
    screensOrder.add(NFC);
    screensOrder.add(LOCATION);

    // Get preferences
    if (sharedPreferences == null)
      sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  public Screens getNextScreen(Screens currentScreen) {

    boolean session = sharedPreferences.getBoolean("session_screen", false);
    boolean audio = sharedPreferences.getBoolean("audio_screen", false);
    boolean location = sharedPreferences.getBoolean("location_screen", false);

    // Get current index
    int currentScreenIndex = screensOrder.indexOf(currentScreen);
    // Move to next
    currentScreenIndex++;

    if (!session)
      currentScreenIndex++;
    if (!audio)
      currentScreenIndex++;
    if (!location)
      currentScreenIndex++;

    // Return the next Screen or loop at the beginning
    final Screens nextScreen;
    if (currentScreenIndex >= screensOrder.size())
      nextScreen = screensOrder.get(0);
    else
      nextScreen = screensOrder.get(currentScreenIndex);

    Timber.d("Next screen is: %s", nextScreen);

    return nextScreen;
  }

  public void goToNextScreen(Context context, Screens screen) {
    switch (screen) {
      case MAIN:
        // TODO: 02/08/2018
        break;
      case SESSION:
        openSessionActivity(context);
        break;
      case AUDIO:
        openAudioActivity(context);
        break;
      case NFC:
        openNfcActivity(context);
        break;
      case LOCATION:
        break;
    }
  }

  public static void openSessionActivity(Context context) {
    Timber.d("Go to the Session activity");

    Intent intent = new Intent(context, SessionActivity.class);
    context.startActivity(intent);
  }

  public static void openAudioActivity(Context context) {
    Timber.d("Go to the Audio activity");

    Intent intent = new Intent(context, AudioRecordingActivity.class);
    context.startActivity(intent);
  }

  public static void openNfcActivity(Context context) {
    Timber.d("Go to the NFC activity");

    Intent intent = new Intent(context, TapAndMapActivity.class);
    context.startActivity(intent);
  }

  public static void openListActivity(Context context) {
    Timber.d("Go to the List activity");

    Intent intent = new Intent(context, ListActivity.class);
    context.startActivity(intent);
  }

  public static void openSettingsActivity(Context context) {
    Timber.d("Go to the Settings activity");

    Intent intent = new Intent(context, SettingsActivity.class);
    context.startActivity(intent);
  }
}

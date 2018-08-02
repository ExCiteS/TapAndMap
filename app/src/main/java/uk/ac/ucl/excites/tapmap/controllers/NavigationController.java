package uk.ac.ucl.excites.tapmap.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

  private SharedPreferences sharedPreferences;
  private List<Screens> screensOrder;
  private Screens currentScreen;
  private JsonObject currentMeta;

  private NavigationController() {
    // Do nothing
  }

  public void init(Context context) {

    // Init the current Meta
    if (currentMeta == null)
      currentMeta = new JsonObject();

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

  /**
   * Set the current screen along with the collected meta json
   */
  public void setCurrentScreen(Screens currentScreen, JsonElement meta) {

    this.currentScreen = currentScreen;

    if (meta != null)
      switch (currentScreen) {
        case MAIN:
          // Do nothing
          break;
        case SESSION:
          // Do nothing
          break;
        case AUDIO:
          currentMeta.add("audio", meta);
          break;
        case NFC:
          currentMeta.add("card", meta);
          break;
        case LOCATION:
          currentMeta.add("location", meta);
          break;
      }

    Timber.d("Current Screen: %s, Meta: %s", currentScreen, currentMeta);
  }

  private Screens getNextScreen() {

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

  public void goToNextScreen(Activity activity, boolean finishActivity) {

    // Get next screen
    Screens nextScreen = getNextScreen();

    switch (nextScreen) {
      case MAIN:
        // TODO: 02/08/2018 add Action
        break;
      case SESSION:
        openSessionActivity(activity);
        break;
      case AUDIO:
        openAudioActivity(activity);
        break;
      case NFC:
        openNfcActivity(activity);
        break;
      case LOCATION:
        // TODO: 02/08/2018 add Action
        break;
    }

    // Finish activity if necessary
    if (finishActivity && activity != null)
      activity.finish();
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

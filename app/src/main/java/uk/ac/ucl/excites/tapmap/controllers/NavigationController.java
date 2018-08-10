package uk.ac.ucl.excites.tapmap.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.activities.AudioRecordingActivity;
import uk.ac.ucl.excites.tapmap.activities.ExportActivity;
import uk.ac.ucl.excites.tapmap.activities.ListActivity;
import uk.ac.ucl.excites.tapmap.activities.LocationActivity;
import uk.ac.ucl.excites.tapmap.activities.SessionActivity;
import uk.ac.ucl.excites.tapmap.activities.SettingsActivity;
import uk.ac.ucl.excites.tapmap.activities.TapAndMapActivity;
import uk.ac.ucl.excites.tapmap.storage.NfcCard;
import uk.ac.ucl.excites.tapmap.storage.RecordController;

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
  private RecordController recordController;
  private List<Screens> screensOrder;
  private Screens startingScreen;
  private Screens currentScreen;
  private NfcCard currentNfcCard;
  private JsonObject currentMeta;

  private NavigationController() {
    // Do nothing
  }

  public void init(Context context) {

    // Init the current Meta
    if (currentMeta == null)
      currentMeta = new JsonObject();

    if (recordController == null)
      recordController = new RecordController(context);

    // Set the Original Screens Order
    setScreensOrder();

    // Get preferences
    if (sharedPreferences == null)
      sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  private void setScreensOrder() {

    // Set the screens in order
    screensOrder = new ArrayList<>();
    screensOrder.add(MAIN);
    screensOrder.add(SESSION);
    screensOrder.add(AUDIO);
    screensOrder.add(NFC);
    screensOrder.add(LOCATION);

    // Set the starting screen
    startingScreen = AUDIO;
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

  /**
   * Set the current NfcCard
   */
  public void setCurrentNfcCard(NfcCard nfcCard) {
    currentNfcCard = nfcCard;
  }

  private Screens getNextScreen() {

    // Set the Original Screens Order
    setScreensOrder();

    boolean session = sharedPreferences.getBoolean("session_screen", false);
    boolean audio = sharedPreferences.getBoolean("audio_screen", false);
    boolean location = sharedPreferences.getBoolean("location_screen", false);

    // Remove disabled screens
    if (!session)
      screensOrder.removeAll(Collections.singleton(SESSION));
    if (!audio)
      screensOrder.removeAll(Collections.singleton(AUDIO));
    if (!location)
      screensOrder.removeAll(Collections.singleton(LOCATION));

    // Get current index
    int currentScreenIndex = screensOrder.indexOf(currentScreen);
    // Move to next
    currentScreenIndex++;

    // Return the next Screen or loop at the beginning
    final Screens nextScreen;
    if (currentScreenIndex >= screensOrder.size()) {

      // Start again at 0
      nextScreen = screensOrder.get(0);

      // Store current record
      recordController.storeCard(currentNfcCard, currentMeta);

      // Clear the current NfcCard
      currentNfcCard = null;
    } else {
      nextScreen = screensOrder.get(currentScreenIndex);
    }

    Timber.d("Next screen is: %s", nextScreen);

    return nextScreen;
  }

  public void goToNextScreen(Activity activity) {

    // Get next screen
    Screens nextScreen = getNextScreen();

    // Ensure that we start from the staring screen if we we reach at the beginning.
    if (nextScreen == screensOrder.get(0))
      nextScreen = startingScreen;

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
        openLocationActivity(activity);
        break;
    }

    // Finish activity if necessary
    if (activity != null)
      activity.finish();
  }

  public void cancel(Activity activity) {

    // Clear the current meta
    currentMeta = new JsonObject();

    // Clear the current NfcCard
    currentNfcCard = null;

    // Finish activity if necessary
    if (activity != null)
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

  public static void openLocationActivity(Context context) {
    Timber.d("Go to the Location activity");

    Intent intent = new Intent(context, LocationActivity.class);
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

  public static void openExportActivity(Context context) {
    Timber.d("Go to the Settings activity");

    Intent intent = new Intent(context, ExportActivity.class);
    context.startActivity(intent);
  }
}

package excites.ucl.ac.uk.tapmap.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import excites.ucl.ac.uk.tapmap.nfc.NfcCard;
import timber.log.Timber;

public abstract class NfcBaseActivity extends AppCompatActivity {

  public static final String MIME_TEXT_PLAIN = "text/plain";

  private NfcAdapter nfcAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    nfcAdapter = NfcAdapter.getDefaultAdapter(this);

    if (nfcAdapter == null) {
      // Stop here, we definitely need NFC
      Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    if (!nfcAdapter.isEnabled()) {
      Toast.makeText(this, "NFC is disabled. Please enable it!", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    onNewIntent(getIntent());
  }

  @Override
  protected void onResume() {
    super.onResume();

    /**
     * It's important, that the activity is in the foreground (resumed). Otherwise
     * an IllegalStateException is thrown.
     */
    setupForegroundDispatch(this, nfcAdapter);
  }

  @Override
  protected void onPause() {
    /**
     * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
     */
    stopForegroundDispatch(this, nfcAdapter);

    super.onPause();
  }

  /**
   * This method gets called, when a new Intent gets associated with the current activity instance.
   * Instead of creating a new activity, onNewIntent will be called. For more information have a look
   * at the documentation.
   *
   * In our case this method gets called, when the user attaches a Tag to the device.
   */
  @Override
  protected void onNewIntent(Intent intent) {

    // Get the NFC Tag
    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

    if (tag == null) return;

    // Get the NFC card
    NfcCard nfcCard = new NfcCard(tag);
    Timber.d(nfcCard.toString());

    // Decide what to do with the card on the implementation of each Activity
    handleNfcCard(nfcCard);
  }

  protected abstract void handleNfcCard(NfcCard nfcCard);

  /**
   * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
   * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
   */
  public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
    final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

    final PendingIntent pendingIntent =
        PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

    IntentFilter[] filters = new IntentFilter[1];

    // Notice that this is the same filter as in our manifest.
    filters[0] = new IntentFilter();
    filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
    filters[0].addCategory(Intent.CATEGORY_DEFAULT);
    try {
      filters[0].addDataType(MIME_TEXT_PLAIN);
    } catch (IntentFilter.MalformedMimeTypeException e) {
      throw new RuntimeException("Check your mime type.");
    }

    adapter.enableForegroundDispatch(activity, pendingIntent, null, null);
  }

  /**
   * @param activity The corresponding requesting to stop the foreground dispatch.
   * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
   */
  public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
    adapter.disableForegroundDispatch(activity);
  }
}
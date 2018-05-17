package excites.ucl.ac.uk.tapmap.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import excites.ucl.ac.uk.tapmap.R;
import java.io.IOException;
import timber.log.Timber;

public class NFCActivity extends AppCompatActivity {

  public static final String MIME_TEXT_PLAIN = "text/plain";
  public static final String TAG = "NfcDemo";

  @BindView(R.id.textView_explanation)
  protected TextView textViewExplanation;
  private NfcAdapter nfcAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_nfc);
    ButterKnife.bind(this);
    // TODO Use fields...

    nfcAdapter = NfcAdapter.getDefaultAdapter(this);

    if (nfcAdapter == null) {
      // Stop here, we definitely need NFC
      Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    if (!nfcAdapter.isEnabled()) {
      textViewExplanation.setText("NFC is disabled.");
    } else {
      textViewExplanation.setText(R.string.explanation);
    }

    handleIntent(getIntent());
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

  @Override
  protected void onNewIntent(Intent intent) {
    /**
     * This method gets called, when a new Intent gets associated with the current activity instance.
     * Instead of creating a new activity, onNewIntent will be called. For more information have a look
     * at the documentation.
     *
     * In our case this method gets called, when the user attaches a Tag to the device.
     */
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {

    String action = intent.getAction();
    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

    if (tag == null) return;
    Timber.d("Handle TAG: %s, Action: %s, Intent: %s", tag, action, intent);

    final String message = dumpTagData(tag);
    textViewExplanation.setText(message);
    Timber.d(message);
  }

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
    String[][] techList = new String[][] {};

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

  private String dumpTagData(Tag tag) {
    StringBuilder sb = new StringBuilder();
    byte[] id = tag.getId();
    sb.append("ID (hex): ").append(toHex(id, ":")).append('\n');
    sb.append("ID (reversed hex): ").append(toReversedHex(id, ":")).append('\n');
    sb.append("ID (dec): ").append(toDec(id)).append('\n');
    sb.append("ID (reversed dec): ").append(toReversedDec(id)).append('\n');

    String prefix = "android.nfc.tech.";
    sb.append("Technologies: ");
    for (String tech : tag.getTechList()) {
      sb.append(tech.substring(prefix.length()));
      sb.append(", ");
    }

    sb.delete(sb.length() - 2, sb.length());

    for (String tech : tag.getTechList()) {
      if (tech.equals(MifareClassic.class.getName())) {
        sb.append('\n');
        String type = "Unknown";

        try {
          MifareClassic mifareTag = MifareClassic.get(tag);

          switch (mifareTag.getType()) {
            case MifareClassic.TYPE_CLASSIC:
              type = "Classic";
              break;
            case MifareClassic.TYPE_PLUS:
              type = "Plus";
              break;
            case MifareClassic.TYPE_PRO:
              type = "Pro";
              break;
          }
          sb.append("Mifare Classic type: ");
          sb.append(type);
          sb.append('\n');

          sb.append("Mifare size: ");
          sb.append(mifareTag.getSize() + " bytes");
          sb.append('\n');

          sb.append("Mifare sectors: ");
          sb.append(mifareTag.getSectorCount());
          sb.append('\n');

          sb.append("Mifare blocks: ");
          sb.append(mifareTag.getBlockCount());
        } catch (Exception e) {
          sb.append("Mifare classic error: " + e.getMessage());
        }
      }

      if (tech.equals(MifareUltralight.class.getName())) {
        sb.append('\n');
        MifareUltralight mifareUlTag = MifareUltralight.get(tag);
        String type = "Unknown";
        switch (mifareUlTag.getType()) {
          case MifareUltralight.TYPE_ULTRALIGHT:
            type = "Ultralight";
            break;
          case MifareUltralight.TYPE_ULTRALIGHT_C:
            type = "Ultralight C";
            break;
        }
        sb.append("Mifare Ultralight type: ");
        sb.append(type).append('\n');
        try {
          mifareUlTag.connect();
          byte[] payload = mifareUlTag.readPages(3);
          sb.append("Mifare hex payload: ").append(toReversedHex(payload, ":")).append('\n');
          final int cardId = Integer.parseInt(toReversedHex(payload, "").substring(0, 8), 16);
          sb.append("Mifare first digits: ").append(cardId).append('\n');
        } catch (IOException e) {
          Timber.e(e, "IOException while writing MifareUltralight message...");
        }
      }
    }

    return sb.toString();
  }

  private String toHex(byte[] bytes, String divider) {
    StringBuilder sb = new StringBuilder();
    for (int i = bytes.length - 1; i >= 0; --i) {
      int b = bytes[i] & 0xff;
      if (b < 0x10) sb.append('0');
      sb.append(Integer.toHexString(b));
      if (i > 0) {
        sb.append(divider);
      }
    }
    return sb.toString();
  }

  private String toReversedHex(byte[] bytes, String divider) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bytes.length; ++i) {
      if (i > 0) {
        sb.append(divider);
      }
      int b = bytes[i] & 0xff;
      if (b < 0x10) sb.append('0');
      sb.append(Integer.toHexString(b));
    }
    return sb.toString();
  }

  private long toDec(byte[] bytes) {
    long result = 0;
    long factor = 1;
    for (int i = 0; i < bytes.length; ++i) {
      long value = bytes[i] & 0xffl;
      result += value * factor;
      factor *= 256l;
    }
    return result;
  }

  private long toReversedDec(byte[] bytes) {
    long result = 0;
    long factor = 1;
    for (int i = bytes.length - 1; i >= 0; --i) {
      long value = bytes[i] & 0xffl;
      result += value * factor;
      factor *= 256l;
    }
    return result;
  }
}

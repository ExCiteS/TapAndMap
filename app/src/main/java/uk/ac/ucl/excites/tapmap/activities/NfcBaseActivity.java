package uk.ac.ucl.excites.tapmap.activities;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import uk.ac.ucl.excites.tapmap.nfc.NfcTagParser;

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

    Bundle options = new Bundle();
    options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 100);

    nfcAdapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {
      @Override
      public void onTagDiscovered(final Tag tag) {

        if (tag == null)
          return;

        // Get the NFC card
        final NfcTagParser nfcTagParser = new NfcTagParser(tag);

        runOnUiThread(new Runnable() {
          public void run() {
            // Decide what to do with the card on the implementation of each Activity
            handleNfcCard(nfcTagParser);
          }
        });
      }
    }, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, options);
  }

  protected abstract void handleNfcCard(NfcTagParser nfcTagParser);
}

/*
 * Tap and Map is part of the Sapelli platform: http://sapelli.org
 * <p/>
 * Copyright 2012-2018 University College London - ExCiteS group
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package uk.ac.ucl.excites.tapmap.activities;

import android.nfc.NfcAdapter;
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

    nfcAdapter.enableReaderMode(this, tag -> {

      if (tag == null)
        return;

      // Get the NFC card
      final NfcTagParser nfcTagParser = new NfcTagParser(tag);
      // Decide what to do with the card on the implementation of each Activity
      runOnUiThread(() -> handleNfcCard(nfcTagParser));
    }, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, options);
  }

  protected abstract void handleNfcCard(NfcTagParser nfcTagParser);
}

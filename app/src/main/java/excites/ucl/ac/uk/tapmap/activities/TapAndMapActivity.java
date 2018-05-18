package excites.ucl.ac.uk.tapmap.activities;

import android.os.Bundle;
import butterknife.ButterKnife;
import excites.ucl.ac.uk.tapmap.R;
import excites.ucl.ac.uk.tapmap.nfc.NfcCard;
import timber.log.Timber;

public class TapAndMapActivity extends NfcBaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tap_and_map);
    ButterKnife.bind(this);
  }

  @Override
  protected void handleNfcCard(NfcCard nfcCard) {

    Timber.d(nfcCard.toString());
  }
}

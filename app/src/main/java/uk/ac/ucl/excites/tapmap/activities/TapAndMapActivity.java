package uk.ac.ucl.excites.tapmap.activities;

import android.os.Bundle;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.nfc.NfcCard;
import uk.ac.ucl.excites.tapmap.nfc.NfcManagement;
import uk.ac.ucl.excites.tapmap.utils.ImageUtils;
import timber.log.Timber;

public class TapAndMapActivity extends NfcBaseActivity {

  @BindView(R.id.nfc)
  protected ImageView nfcImageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tap_and_map);
    ButterKnife.bind(this);
  }

  @Override
  protected void handleNfcCard(NfcCard nfcCard) {

    Timber.d(nfcCard.toString());

    final String imagePath = NfcManagement.getInstance().getImagePath(nfcCard);
    if (imagePath != null && !imagePath.isEmpty()) {
      try {
        nfcImageView.setImageBitmap(ImageUtils.getThumbnail(imagePath));
      } catch (Exception e) {
        Timber.e(e, "Cannot load icon.");
      }
    }
  }
}

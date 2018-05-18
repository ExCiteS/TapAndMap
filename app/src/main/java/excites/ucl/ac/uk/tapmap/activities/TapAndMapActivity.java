package excites.ucl.ac.uk.tapmap.activities;

import android.os.Bundle;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import excites.ucl.ac.uk.tapmap.R;
import excites.ucl.ac.uk.tapmap.nfc.NfcCard;
import excites.ucl.ac.uk.tapmap.nfc.NfcManagement;
import excites.ucl.ac.uk.tapmap.utils.ImageUtils;
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

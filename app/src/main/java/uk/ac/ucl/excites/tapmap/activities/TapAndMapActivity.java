package uk.ac.ucl.excites.tapmap.activities;

import android.os.Bundle;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.nfc.NfcTagParser;
import uk.ac.ucl.excites.tapmap.storage.NfcCardDao;
import uk.ac.ucl.excites.tapmap.utils.ImageUtils;

public class TapAndMapActivity extends NfcBaseActivity {

  @BindView(R.id.nfc)
  protected ImageView nfcImageView;

  private NfcCardDao nfcCardDao;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tap_and_map);
    ButterKnife.bind(this);

    final TapMap app = (TapMap) getApplication();
    nfcCardDao = app.getAppDatabase().nfcCardDao();
  }

  @Override
  protected void handleNfcCard(NfcTagParser nfcTagParser) {

    Timber.d(nfcTagParser.toString());

    try {

      final String imagePath = nfcCardDao.findById(nfcTagParser.getCardID()).getImagePath();
      if (imagePath != null && !imagePath.isEmpty()) {
        nfcImageView.setImageBitmap(ImageUtils.getThumbnail(imagePath));
      }
    } catch (Exception e) {
      Timber.e(e, "Cannot load icon.");
    }
  }
}

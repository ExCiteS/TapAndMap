package uk.ac.ucl.excites.tapmap.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.nfc.NfcTagParser;
import uk.ac.ucl.excites.tapmap.storage.NfcCardDao;
import uk.ac.ucl.excites.tapmap.utils.BitmapCache;
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

    Timber.d("NFC ID: %s", nfcTagParser.getId());

    try {

      final String cardID = nfcTagParser.getId();
      final String imagePath = nfcCardDao.findById(cardID).getImagePath();
      if (imagePath != null && !imagePath.isEmpty()) {

        Bitmap bitmap = BitmapCache.getInstance().getBitmapFromMemCache(cardID);
        if (bitmap == null) {
          bitmap = ImageUtils.getThumbnail(imagePath);
          BitmapCache.getInstance().addBitmapToMemoryCache(cardID, bitmap);
          Timber.d("Load bitmap from memory: %s", bitmap);
        }

        nfcImageView.setImageBitmap(bitmap);
      }
    } catch (Exception e) {
      Timber.e(e, "Cannot load icon.");
    }
  }
}

package uk.ac.ucl.excites.tapmap.activities;

import android.os.Bundle;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;
import java.io.File;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.BuildConfig;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.nfc.NfcTagParser;
import uk.ac.ucl.excites.tapmap.storage.NfcCardDao;

public class TapAndMapActivity extends NfcBaseActivity {

  public static final int MAX_SIZE = 500;

  @BindView(R.id.nfc)
  protected ImageView nfcImageView;

  private NfcCardDao nfcCardDao;
  private Picasso picasso;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tap_and_map);
    ButterKnife.bind(this);

    final TapMap app = (TapMap) getApplication();
    nfcCardDao = app.getAppDatabase().nfcCardDao();

    // Set up Picasso
    picasso = Picasso.get();
    picasso.setIndicatorsEnabled(BuildConfig.DEBUG);
  }

  @Override
  protected void handleNfcCard(NfcTagParser nfcTagParser) {

    Timber.d("NFC ID: %s", nfcTagParser.getId());

    try {

      final String cardID = nfcTagParser.getId();
      final String imagePath = nfcCardDao.findById(cardID).getImagePath();
      if (imagePath != null && !imagePath.isEmpty()) {

        picasso.load(new File(imagePath))
            .placeholder(R.drawable.ic_refresh_black_24dp)
            .resize(MAX_SIZE, MAX_SIZE)
            .centerCrop()
            .into(nfcImageView);
      }
    } catch (Exception e) {
      Timber.e(e, "Cannot load icon.");
    }
  }
}

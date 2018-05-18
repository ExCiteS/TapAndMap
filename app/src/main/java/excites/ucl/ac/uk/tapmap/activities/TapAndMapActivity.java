package excites.ucl.ac.uk.tapmap.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import excites.ucl.ac.uk.tapmap.R;
import excites.ucl.ac.uk.tapmap.nfc.NfcCard;
import excites.ucl.ac.uk.tapmap.nfc.NfcManagement;
import java.io.File;
import timber.log.Timber;

public class TapAndMapActivity extends NfcBaseActivity {

  private static final int THUMBNAIL_SIZE = 64;

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

      File file = new File(imagePath);
      Bitmap pickedIcon = BitmapFactory.decodeFile(file.getPath());
      nfcImageView.setImageBitmap(pickedIcon);
    }
  }
}

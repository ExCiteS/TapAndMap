package uk.ac.ucl.excites.tapmap.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
  @BindView(R.id.confirm)
  protected ImageButton confirmButton;
  @BindView(R.id.cancel)
  protected ImageButton cancelButton;

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
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Start with the default UI
    resetUI();
  }

  private void resetUI() {

    // Reset image
    nfcImageView.animate().alpha(1.0f);
    nfcImageView.setImageResource(R.drawable.ic_nfc);

    // Hide Buttons
    hideButtons();
  }

  private void hideButtons() {
    confirmButton.animate().alpha(0.0f);
    cancelButton.animate().alpha(0.0f);
    confirmButton.setVisibility(View.INVISIBLE);
    cancelButton.setVisibility(View.INVISIBLE);
  }

  private void showButtons() {
    confirmButton.animate().alpha(1.0f);
    cancelButton.animate().alpha(1.0f);
    confirmButton.setVisibility(View.VISIBLE);
    cancelButton.setVisibility(View.VISIBLE);
  }

  @Override
  protected void handleNfcCard(NfcTagParser nfcTagParser) {

    final String cardID = nfcTagParser.getId();
    String imagePath = "";

    try {
      imagePath = nfcCardDao.findById(cardID).getImagePath();
    } catch (Exception e) {
      Timber.e(e, "Cannot get icon.");
    }

    Timber.d("NFC ID: %s. Path: %s", cardID, imagePath);

    // Show NFC card
    picasso.load(new File(imagePath))
        .placeholder(R.drawable.progress_animation)
        .error(R.drawable.ic_error)
        .resize(MAX_SIZE, MAX_SIZE)
        .centerCrop()
        .into(nfcImageView);

    // Show buttons
    showButtons();
  }

  @OnClick(R.id.confirm)
  protected void onConfirmClicked() {

    // Reset UI
    resetUI();

    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();

    // TODO: 24/05/2018 Log click, and store record

  }

  @OnClick(R.id.cancel)
  protected void onCancelClicked() {

    // Reset the UI
    resetUI();

    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();

    // TODO: 24/05/2018 Log click
  }
}

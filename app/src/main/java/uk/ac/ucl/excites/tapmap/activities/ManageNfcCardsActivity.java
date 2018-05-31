package uk.ac.ucl.excites.tapmap.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.nfc.NfcTagParser;
import uk.ac.ucl.excites.tapmap.storage.NfcCard;
import uk.ac.ucl.excites.tapmap.storage.NfcCardDao;

public class ManageNfcCardsActivity extends NfcBaseActivity {

  public static final int PICK_IMAGE = 1;

  // UI
  @BindView(R.id.card_id)
  protected TextView cardId;
  @BindView(R.id.two)
  protected TextView two;
  @BindView(R.id.nfc)
  protected ImageView nfcImage;
  @BindView(R.id.three)
  protected TextView three;
  @BindView(R.id.tag)
  protected EditText tag;
  @BindView(R.id.confirm)
  protected ImageButton confirmButton;
  @BindView(R.id.cancel)
  protected ImageButton cancelButton;

  private NfcTagParser currentNfcTagParser;
  private NfcCardDao nfcCardDao;
  private Picasso picasso;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_manage_nfc_cards);
    ButterKnife.bind(this);

    final TapMap app = (TapMap) getApplication();
    nfcCardDao = app.getAppDatabase().nfcCardDao();

    // Set up Picasso
    picasso = Picasso.get();

    // Start with the default UI
    resetUI();
  }

  private void resetUI() {

    cardId.setText("");
    two.setVisibility(View.GONE);
    nfcImage.setVisibility(View.GONE);
    three.setVisibility(View.GONE);
    tag.setVisibility(View.GONE);
    confirmButton.setVisibility(View.GONE);
    cancelButton.setVisibility(View.GONE);
  }

  private void showStepTwo(String cardIdText) {

    cardId.setText(cardIdText);
    two.setVisibility(View.VISIBLE);
    nfcImage.setVisibility(View.VISIBLE);
  }

  private void showStepThree() {

    three.setVisibility(View.VISIBLE);
    tag.setVisibility(View.VISIBLE);
    confirmButton.setVisibility(View.VISIBLE);
    cancelButton.setVisibility(View.VISIBLE);
  }

  @Override
  protected void handleNfcCard(NfcTagParser nfcTagParser) {

    Timber.d(nfcTagParser.toString());
    currentNfcTagParser = nfcTagParser;

    // 1. Check if Card is already associated with an icon
    final NfcCard nfcCard = nfcCardDao.findById(nfcTagParser.getId());

    if (nfcCard != null && !nfcCard.getImagePath().isEmpty())
      showAlreadyExistsDialog(nfcCard.getId());
    else
      showStepTwo(nfcCard.getId());
  }

  private void showAlreadyExistsDialog(final String cardIdText) {

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setTitle("Replace card?")
        .setMessage("This card has been already setup. Do you want to replace it?")
        .setPositiveButton("Replace", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            showStepTwo(cardIdText);
          }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Do nothing
          }
        })
        .show();
  }

  /**
   * Open an ImagePicker
   */
  @OnClick(R.id.nfc)
  protected void onPickIconClicked() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select Icon"), PICK_IMAGE);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {

      // Get URI
      final Uri uri = data.getData();
      if (uri == null)
        return;

      InputStream inputStream;
      try {
        inputStream = getContentResolver().openInputStream(uri);

        FileOutputStream outputStream =
            openFileOutput(currentNfcTagParser.getId(), Context.MODE_PRIVATE);

        // Copy file
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, len);
        }
        outputStream.close();
      } catch (Exception e) {
        Timber.e(e, "Error while copying the file.");
      }

      // Load image
      File imageFile = new File(this.getFilesDir(), currentNfcTagParser.getId());
      picasso.invalidate(imageFile);
      picasso.load(imageFile)
          .placeholder(R.drawable.progress_animation)
          .error(R.drawable.ic_error)
          .resize(TapAndMapActivity.MAX_SIZE, TapAndMapActivity.MAX_SIZE)
          .centerCrop()
          .into(nfcImage);

      // Go to step 3
      showStepTwo(currentNfcTagParser.getId());
      showStepThree();
    }
  }

  @OnClick(R.id.confirm)
  protected void confirmCard() {

    if (currentNfcTagParser == null) {
      Toast.makeText(this, "You have to setup a card first.", Toast.LENGTH_LONG).show();
      return;
    }

    // Get path
    final String imagePath = new File(this.getFilesDir(), currentNfcTagParser.getId()).getPath();
    // Store to DB
    nfcCardDao.insert(currentNfcTagParser.toNfcCard(imagePath));
    Toast.makeText(this, "Card stored.", Toast.LENGTH_LONG).show();

    // Reset UI
    resetUI();
  }

  @OnClick(R.id.cancel)
  protected void cancelCard() {

    if (currentNfcTagParser == null) {
      Toast.makeText(this, "You have to setup a card first.", Toast.LENGTH_LONG).show();
      return;
    }

    // Clean up by deleting the file
    final File file = new File(this.getFilesDir(), currentNfcTagParser.getId());
    if (file.exists())
      file.delete();
    Toast.makeText(this, "Card deleted. Try again.", Toast.LENGTH_LONG).show();

    // Reset UI
    resetUI();
  }
}
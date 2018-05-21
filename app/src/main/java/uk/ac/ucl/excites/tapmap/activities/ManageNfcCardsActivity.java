package uk.ac.ucl.excites.tapmap.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.BuildConfig;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.nfc.NfcTagParser;
import uk.ac.ucl.excites.tapmap.storage.NfcCard;
import uk.ac.ucl.excites.tapmap.storage.NfcCardDao;

public class ManageNfcCardsActivity extends NfcBaseActivity {

  public static final int PICK_IMAGE = 1;

  @BindView(R.id.nfc)
  protected ImageView nfcImageView;

  private NfcTagParser currentNfcTagParser;
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

    Timber.d(nfcTagParser.toString());
    currentNfcTagParser = nfcTagParser;

    // 1. Check if Card is already associated with an icon
    final NfcCard nfcCard = nfcCardDao.findById(nfcTagParser.getId());

    if (nfcCard != null && !nfcCard.getImagePath().isEmpty()) {
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
      alertDialogBuilder.setTitle("Replace card icon")
          .setMessage("This card has already an icon")
          .setPositiveButton("Replace", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              pickIcon();
            }
          })
          .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              // Do nothing
            }
          })
          .show();
    } else {
      pickIcon();
    }
  }

  /**
   * Open an ImagePicker
   */
  private void pickIcon() {
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
          .error(R.drawable.ic_error_outline_black_24dp)
          .resize(TapAndMapActivity.MAX_SIZE, TapAndMapActivity.MAX_SIZE)
          .centerCrop()
          .into(nfcImageView);
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
  }
}
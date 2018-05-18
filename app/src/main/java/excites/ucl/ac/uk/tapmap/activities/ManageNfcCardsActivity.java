package excites.ucl.ac.uk.tapmap.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import excites.ucl.ac.uk.tapmap.R;
import excites.ucl.ac.uk.tapmap.nfc.NfcCard;
import excites.ucl.ac.uk.tapmap.nfc.NfcManagement;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import timber.log.Timber;

public class ManageNfcCardsActivity extends NfcBaseActivity {

  public static final int PICK_IMAGE = 1;

  @BindView(R.id.nfc)
  protected ImageView nfcImageView;

  private NfcCard currentNfcCard;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tap_and_map);
    ButterKnife.bind(this);
  }

  @Override
  protected void handleNfcCard(NfcCard nfcCard) {

    Timber.d(nfcCard.toString());
    currentNfcCard = nfcCard;

    // 1. Check if Card is already associated with an icon
    String path = NfcManagement.getInstance().getImagePath(nfcCard);

    if (path != null && !path.isEmpty()) {
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
      if (uri == null) return;

      InputStream inputStream;
      try {
        Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
        inputStream = getContentResolver().openInputStream(uri);

        FileOutputStream outputStream =
            openFileOutput(currentNfcCard.getCardID(), Context.MODE_PRIVATE);

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

      File file = new File(this.getFilesDir(), currentNfcCard.getCardID());
      Bitmap pickedIcon = BitmapFactory.decodeFile(file.getPath());
      nfcImageView.setImageBitmap(pickedIcon);
    }
  }

  @OnClick(R.id.confirm)
  protected void confirmCard() {

    if (currentNfcCard == null) {
      Toast.makeText(this, "You have to setup a card first.", Toast.LENGTH_LONG).show();
      return;
    }

    final String path = new File(this.getFilesDir(), currentNfcCard.getCardID()).getPath();
    NfcManagement.getInstance().storeNfcCard(currentNfcCard, path);
    Toast.makeText(this, "Card stored.", Toast.LENGTH_LONG).show();
  }

  @OnClick(R.id.cancel)
  protected void cancelCard() {

    if (currentNfcCard == null) {
      Toast.makeText(this, "You have to setup a card first.", Toast.LENGTH_LONG).show();
      return;
    }

    // Clean up by deleting the file
    final File file = new File(this.getFilesDir(), currentNfcCard.getCardID());
    if (file.exists()) file.delete();
    Toast.makeText(this, "Card deleted. Try again.", Toast.LENGTH_LONG).show();
  }
}
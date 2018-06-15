/*
 * Tap and Map is part of the Sapelli platform: http://sapelli.org
 * <p/>
 * Copyright 2012-2018 University College London - ExCiteS group
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package uk.ac.ucl.excites.tapmap.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
  private String imageFileName = "";

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
      showAlreadyExistsDialog(nfcCard);
    else
      showStepTwo(currentNfcTagParser.getId());
  }

  private void showAlreadyExistsDialog(final NfcCard nfcCard) {

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setTitle("Replace card " + nfcCard.getTag() + "?")
        .setMessage("This card has been already setup. Do you want to replace it?")
        .setPositiveButton("Replace", (dialog, which) -> showStepTwo(nfcCard.getId()))
        .setNegativeButton("Cancel", (dialog, which) -> { /* Do nothing */ })
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

      // Get the filename of the picked icon
      imageFileName = getImageName(uri);

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
      final File imageFile = new File(this.getFilesDir(), currentNfcTagParser.getId());
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

  /**
   * Get the filename of a uri
   */
  private String getImageName(Uri uri) {

    String name = "";

    try {
      Cursor cursor = getContentResolver().query(uri, null, null, null, null);
      /*
       * Get the column indexes of the data in the Cursor,
       * move to the first row in the Cursor and get the data
       */
      final int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
      cursor.moveToFirst();
      name = cursor.getString(nameIndex);
      cursor.close();

      Timber.d("Found image with name: %s.", name);
    } catch (Exception e) {
      Timber.e(e);
    }

    return name;
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
    final String tagText = this.tag.getText().toString();
    nfcCardDao.insert(currentNfcTagParser.toNfcCard(imagePath, imageFileName, tagText));
    Toast.makeText(this, "Card stored.", Toast.LENGTH_LONG).show();

    // Close activity
    finish();
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

    nfcCardDao.delete(currentNfcTagParser.toNfcCard());

    // Close activity
    finish();
  }
}
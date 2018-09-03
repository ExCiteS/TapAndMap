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
import java.io.IOException;
import java.io.InputStream;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.nfc.NfcTagParser;
import uk.ac.ucl.excites.tapmap.project.ProjectManager;
import uk.ac.ucl.excites.tapmap.storage.ImageCard;
import uk.ac.ucl.excites.tapmap.storage.ImageCardDao;
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
  private ImageCardDao imageCardDao;
  private NfcCardDao nfcCardDao;
  private Picasso picasso;

  // Files and Paths
  private File imagesDirectory;
  private File currentImageFilePath;
  private String currentImageFileName = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_manage_nfc_cards);
    ButterKnife.bind(this);

    final TapMap app = (TapMap) getApplication();
    nfcCardDao = app.getAppDatabase().nfcCardDao();
    imageCardDao = app.getAppDatabase().imageCardDao();
    imagesDirectory = ProjectManager.getImagesDirectory(this);

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

    if (nfcCard != null && nfcCard.getImageCardId() > 0)
      showAlreadyExistsDialog(nfcCard);
    else
      showStepTwo(currentNfcTagParser.getId());
  }

  private void showAlreadyExistsDialog(final NfcCard nfcCard) {

    final ImageCard imageCard = imageCardDao.findById(nfcCard.getImageCardId());

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setTitle("Replace card " + imageCard.getTag() + "?")
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

      InputStream inputStream = null;
      FileOutputStream outputStream = null;
      try {
        inputStream = getContentResolver().openInputStream(uri);

        // Get the Filename of the current Image and the Path
        currentImageFileName = getImageName(uri);
        currentImageFilePath = new File(imagesDirectory + File.separator + currentImageFileName);
        outputStream = new FileOutputStream(currentImageFilePath);

        // Copy file
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, len);
        }
      } catch (Exception e) {
        Timber.e(e, "Error while copying the file.");
      } finally {
        if (outputStream != null) {
          try {
            outputStream.close();
          } catch (IOException e) {
            Timber.e(e, "Error while copying the file and closing the FileOutputStream.");
          }
        }
      }

      // Load image
      picasso.invalidate(currentImageFilePath);
      final int maxSize = TapAndMapActivity.MAX_SIZE / 3;
      picasso.load(currentImageFilePath)
          .placeholder(R.drawable.progress_animation)
          .error(R.drawable.ic_error)
          .resize(maxSize, maxSize)
          .centerInside()
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
    // Store to DB
    final String tagText = this.tag.getText().toString();
    final long imageCardId =
        imageCardDao.insert(getImageCard(currentImageFilePath.toString(),
            currentImageFileName,
            tagText));
    nfcCardDao.insert(getNfcCard(currentNfcTagParser.getId(), imageCardId));
    Toast.makeText(this, "Card stored.", Toast.LENGTH_LONG).show();

    // Close activity
    finish();
  }

  private ImageCard getImageCard(String imagePath, String filename, String tag) {

    final ImageCard imageCard = new ImageCard();
    imageCard.setFilename(filename);
    imageCard.setImagePath(imagePath);
    imageCard.setTag(tag);

    return imageCard;
  }

  private NfcCard getNfcCard(String id, long imageCardId) {

    final NfcCard nfcCard = new NfcCard();
    nfcCard.setId(id);
    nfcCard.setImageCardId(imageCardId);

    return nfcCard;
  }

  @OnClick(R.id.cancel)
  protected void cancelCard() {

    if (currentNfcTagParser == null) {
      Toast.makeText(this, "You have to setup a card first.", Toast.LENGTH_LONG).show();
      return;
    }

    // Clean up by deleting the file
    final String cardId = currentNfcTagParser.getId();
    final File file = new File(this.getFilesDir(), cardId);
    if (file.exists())
      file.delete();
    Toast.makeText(this, "Card deleted. Try again.", Toast.LENGTH_LONG).show();

    final NfcCard card = nfcCardDao.findById(cardId);
    imageCardDao.delete(imageCardDao.findById(card.getImageCardId()));
    nfcCardDao.delete(card);

    // Close activity
    finish();
  }
}
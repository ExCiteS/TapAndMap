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
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lib.folderpicker.FolderPicker;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.nfc.NfcTagParser;
import uk.ac.ucl.excites.tapmap.project.Card;
import uk.ac.ucl.excites.tapmap.project.ProjectManager;
import uk.ac.ucl.excites.tapmap.project.Settings;
import uk.ac.ucl.excites.tapmap.storage.ImageCard;
import uk.ac.ucl.excites.tapmap.storage.ImageCardDao;
import uk.ac.ucl.excites.tapmap.storage.NfcCard;
import uk.ac.ucl.excites.tapmap.storage.NfcCardDao;
import uk.ac.ucl.excites.tapmap.utils.FileUtils;
import uk.ac.ucl.excites.tapmap.utils.UiUtils;

/**
 * Created by Michalis Vitos on 24/05/2018.
 */
public class ImportSettingsActivity extends NfcBaseActivity {

  public static final int PICK_DIRECTORY = 1;

  @BindView(R.id.root)
  protected View root;
  @BindView(R.id.name)
  protected TextView name;
  @BindView(R.id.image)
  protected ImageView image;
  @BindView(R.id.list_of_ids)
  protected TextView listOfCardIdsText;

  private NfcTagParser currentNfcTagParser;
  private Picasso picasso;
  private ImageCardDao imageCardDao;
  private NfcCardDao nfcCardDao;
  private Set<String> setOfCardIds;
  private List<Card> cards;
  private Card currentCard;

  // Files and Paths
  private File imagesDirectory;
  private File currentImageFilePath;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_import_settings);
    ButterKnife.bind(this);

    // Set up Picasso
    picasso = Picasso.get();

    // Get database dao
    final TapMap app = (TapMap) getApplication();
    imageCardDao = app.getAppDatabase().imageCardDao();
    nfcCardDao = app.getAppDatabase().nfcCardDao();

    // Set the images directory
    imagesDirectory = ProjectManager.getImagesDirectory(this);
    Timber.d("Set images directory to: %s", imagesDirectory.toString());

    setOfCardIds = new HashSet<>();

    // Start with selecting a folder
    pickFolder();
  }

  /**
   * Start another activity to pick a Folder where the project is defined as a settings.json file
   */
  private void pickFolder() {
    Intent intent = new Intent(this, FolderPicker.class);
    intent.putExtra("title", "Select folder with Tap And Map project");
    startActivityForResult(intent, PICK_DIRECTORY);
  }

  /**
   * Callback for the selected folder in the {@code pickFolder()} method
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == PICK_DIRECTORY && resultCode == Activity.RESULT_OK && data != null) {

      // Ensure extras exist
      final Bundle extras = data.getExtras();
      if (extras == null)
        return;

      // Ensure the selected folder is not null or empty
      final String selectedDirString = extras.getString("data");
      if (selectedDirString == null || selectedDirString.isEmpty())
        return;

      // Ensure the selected folder exists
      final File selectedDir = new File(selectedDirString);
      if (!selectedDir.exists())
        return;

      Timber.d("Selected folder: %s", selectedDir);

      Settings settings = null;
      try {
        settings = ProjectManager.loadSettingsFromDirectory(selectedDir);
      } catch (FileNotFoundException e) {
        Timber.e(e);
        Toast.makeText(this,
            "There is no 'settings.json' file in the selected folder",
            Toast.LENGTH_LONG).show();
        finish();
      }

      // Ensure settings exist
      if (settings == null)
        return;

      // Ensure cards exist
      cards = settings.getCards();
      if (cards == null)
        return;

      // Copy the cards to the images directory
      try {
        copyCardsToImagesDir(selectedDir, cards);
      } catch (IOException e) {
        Timber.e(e);
      }

      // Finally process the cards
      processCards();
    } else {
      finish();
    }
  }

  private void copyCardsToImagesDir(File inputDirectory, List<Card> cards) throws IOException {

    for (Card card : cards) {

      final File inputFile = new File(inputDirectory + File.separator + card.getImage());
      final File outputFile = new File(imagesDirectory + File.separator + card.getImage());

      InputStream inputStream = null;
      try {
        inputStream = new FileInputStream(inputFile);
      } catch (FileNotFoundException e) {
        final String message = "The '" + card.getImage() + "' does not exist.";
        UiUtils.showSnackBarWithDismiss(root, message);
        throw new IOException(message);
      }
      FileUtils.copyFile(inputStream, outputFile);
      Timber.d("Copied: %s To: %s", inputFile, outputFile);
    }
  }

  /**
   * Handle the NFC card touched on the device
   */
  @Override
  protected void handleNfcCard(NfcTagParser nfcTagParser) {
    Timber.d(nfcTagParser.toString());
    currentNfcTagParser = nfcTagParser;

    // Check if Card is already associated with an icon
    final NfcCard nfcCard = nfcCardDao.findById(nfcTagParser.getId());

    if (nfcCard != null && !nfcCard.getImageCardId().isEmpty())
      showAlreadyExistsDialog(nfcCard);
    else
      addNfcCardIdsToUI(currentNfcTagParser.getId());
  }

  private void showAlreadyExistsDialog(final NfcCard nfcCard) {

    final ImageCard imageCard = imageCardDao.findById(nfcCard.getImageCardId());

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setTitle("Replace card " + imageCard.getTag() + "?")
        .setMessage("This card has been already setup. Do you want to replace it?")
        .setPositiveButton("Replace", (dialog, which) -> addNfcCardIdsToUI(nfcCard.getId()))
        .setNegativeButton("Cancel", (dialog, which) -> { /* Do nothing */ })
        .show();
  }

  private void addNfcCardIdsToUI(String id) {

    if (id == null || id.isEmpty())
      return;

    setOfCardIds.add(id);
    StringBuilder cardsBuilder = new StringBuilder();
    int counter = 1;
    for (String card : setOfCardIds) {
      cardsBuilder.append(counter).append(") ").append(card).append("\n");
      counter++;
    }

    listOfCardIdsText.setText(cardsBuilder.toString());
  }

  /**
   * Method to process the cards described in the settings.json
   */
  private void processCards() {

    // Process the rest of the cards
    if (cards.isEmpty()) {
      Toast.makeText(this, "All cards have been imported.", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    // Get first card
    currentCard = cards.get(0);
    currentImageFilePath = new File(imagesDirectory + File.separator + currentCard.getImage());

    // Check if card already has an id
    if (currentCard.getIds() != null && !currentCard.getIds().isEmpty()) {
      final ImageCard imageCard = new ImageCard();
      final String filename = currentCard.getImage();
      imageCard.setFilename(filename);
      imageCard.setTag(currentCard.getTag());
      imageCard.setImagePath(currentImageFilePath.toString());
      imageCardDao.insert(imageCard);

      for (String id : currentCard.getIds()) {
        final NfcCard nfcCard = new NfcCard();
        nfcCard.setImageCardId(filename);
        nfcCard.setId(id);
        nfcCardDao.insert(nfcCard);
      }

      // Loop back
      cards.remove(currentCard);
      processCards();
    }

    // Show card
    updateUIwithCard(currentCard, currentImageFilePath);
  }

  private void updateUIwithCard(Card card, File currentImageFilePath) {

    name.setText(card.getImage());
    // Load image
    picasso.invalidate(card.getImage());
    final int maxSize = TapAndMapActivity.MAX_SIZE / 2;
    picasso.load(currentImageFilePath)
        .placeholder(R.drawable.progress_animation)
        .error(R.drawable.ic_error)
        .resize(maxSize, maxSize)
        .centerInside()
        .into(image);
  }

  @OnClick(R.id.clear)
  public void onClearClicked() {
    Timber.d("Clear Clicked");
    setOfCardIds.clear();
    listOfCardIdsText.setText("");
    UiUtils.showSnackBarWithDismiss(root, "Cleared cards, add them again!");
  }

  @OnClick(R.id.next_card)
  public void onNextCardClicked() {
    Timber.d("Next Clicked");
    if (setOfCardIds.isEmpty()) {
      UiUtils.showSnackBarWithDismiss(root, "Please add NFC cards for the current image first!");
      return;
    }

    // Store cards and image files
    final ImageCard imageCard = new ImageCard();
    final String filename = currentCard.getImage();
    imageCard.setFilename(filename);
    imageCard.setTag(currentCard.getTag());
    imageCard.setImagePath(currentImageFilePath.toString());
    imageCardDao.insert(imageCard);

    for (String id : setOfCardIds) {
      final NfcCard nfcCard = new NfcCard();
      nfcCard.setImageCardId(filename);
      nfcCard.setId(id);
      nfcCardDao.insert(nfcCard);
    }

    // Remove current card from the list
    cards.remove(currentCard);
    setOfCardIds.clear();
    listOfCardIdsText.setText("");

    // Process the rest of the cards
    if (!cards.isEmpty())
      processCards();
    else {
      Toast.makeText(this, "All cards have been imported.", Toast.LENGTH_LONG).show();
      finish();
    }
  }
}

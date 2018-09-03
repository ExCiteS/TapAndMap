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
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lib.folderpicker.FolderPicker;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.project.Card;
import uk.ac.ucl.excites.tapmap.project.ProjectManager;
import uk.ac.ucl.excites.tapmap.project.Settings;
import uk.ac.ucl.excites.tapmap.storage.ImageCardDao;
import uk.ac.ucl.excites.tapmap.storage.NfcCardDao;
import uk.ac.ucl.excites.tapmap.utils.FileUtils;

/**
 * Created by Michalis Vitos on 24/05/2018.
 */
public class ImportSettingsActivity extends AppCompatActivity {

  public static final int PICK_DIRECTORY = 1;

  @BindView(R.id.root)
  protected View root;
  @BindView(R.id.name)
  protected TextView name;
  @BindView(R.id.image)
  protected ImageView image;
  @BindView(R.id.recycler_view_cards)
  protected RecyclerView recyclerView;

  private Picasso picasso;
  private ImageCardDao imageCardDao;
  private NfcCardDao nfcCardDao;

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

    // Start with selecting a folder
    pickFolder();
  }

  private void pickFolder() {
    Intent intent = new Intent(this, FolderPicker.class);
    intent.putExtra("title", "Select folder with Tap And Map project");
    startActivityForResult(intent, PICK_DIRECTORY);
  }

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
        settings = ProjectManager.loadSettingsInDirectory(selectedDir);
      } catch (FileNotFoundException e) {
        Timber.e(e);
        showSnackBar(root, "There is no settings.json file in the selected folder");
      }

      // Ensure settings exist
      if (settings == null)
        return;

      // Ensure cards exist
      final List<Card> cards = settings.getCards();
      if (cards == null)
        return;

      for (Card card : cards) {

        // 1. Copy the card to the images directory
        try {
          copyCardToImagesDirectory(selectedDir, card);
        } catch (IOException e) {
          Timber.e(e);
          break;
        }

        // TODO: 03/09/2018 check if card already has an id

        // 3. Show card
        currentImageFilePath = new File(imagesDirectory + File.separator + card.getImage());
        showCard(card);
      }
    }
  }

  private void copyCardToImagesDirectory(File selectedDir, Card card) throws IOException {

    final File inputFile = new File(selectedDir + File.separator + card.getImage());
    final File outputFile = new File(imagesDirectory + File.separator + card.getImage());

    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(inputFile);
    } catch (FileNotFoundException e) {
      final String message = "The '" + card.getImage() + "' does not exist.";
      showSnackBar(root, message);
      throw new IOException(message);
    }
    FileUtils.copyFile(inputStream, outputFile);
    Timber.d("Copied: %s To: %s", inputFile, outputFile);
  }

  private void showCard(Card card) {

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

  private void showSnackBar(View view, String message) {

    final Snackbar snackbar = Snackbar.make(view,
        message,
        Snackbar.LENGTH_INDEFINITE);
    snackbar.setAction("Dismiss", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        snackbar.dismiss();
      }
    });
    snackbar.show();
  }

  @OnClick(R.id.clear)
  protected void onClearClicked() {
    Timber.d("Clear Clicked");
  }

  @OnClick(R.id.next_card)
  protected void onNextCardClicked() {
    Timber.d("Next Clicked");
  }
}

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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
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
public class ExportSettingsActivity extends AppCompatActivity {

  public static final int PICK_DIRECTORY = 1;

  @BindView(R.id.root)
  protected View root;
  @BindView(R.id.exportDirectory)
  protected TextView exportDirectory;
  @BindView(R.id.title)
  protected TextView title;
  @BindView(R.id.progressBar)
  protected ProgressBar progressBar;

  private ImageCardDao imageCardDao;
  private NfcCardDao nfcCardDao;

  // Files and Paths
  private File imagesDirectory;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_export_settings);
    ButterKnife.bind(this);

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

  /**
   * Start another activity to pick a Folder where the project is defined as a settings.json file
   */
  private void pickFolder() {
    Intent intent = new Intent(this, FolderPicker.class);
    intent.putExtra("title", "Select folder to export project");
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
      if (!selectedDir.exists()) {
        Toast.makeText(this, "The directory does not exist.", Toast.LENGTH_LONG).show();
        finish();
      }

      Timber.d("Selected folder: %s", selectedDir);

      // Start showing the progress bar
      progressBar.setVisibility(View.VISIBLE);
      exportSettings(selectedDir);
    } else {
      finish();
    }
  }

  private void exportSettings(File exportsDir) {

    Timber.d("Export settings to : %s", exportsDir);

    // Export settings
    imageCardDao.getAll()
        .subscribeOn(Schedulers.io())
        .toObservable()
        .flatMap(Observable::fromIterable)
        .map(imageCard -> {
          // Get Cards IDs
          final List<String> ids = nfcCardDao.findCardIdsByImageId(imageCard.getFilename());

          // Create our card
          final Card card = new Card();
          card.setIds(ids);
          card.setImage(imageCard.getFilename());
          card.setTag(imageCard.getTag());

          return card;
        })
        .map(card -> {
          copyCardsFromImagesDir(exportsDir, card);
          return card;
        })
        .toList()
        .map(cards -> {
          // Map to Settings
          final Settings settings = new Settings();
          settings.setCards(cards);
          return settings;
        })
        .toObservable()
        .doOnNext(settings -> ProjectManager.writeSettingsToDirectory(settings, exportsDir))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DisposableObserver<Settings>() {
          @Override
          public void onNext(Settings settings) {
            Timber.d(settings.toString());
          }

          @Override
          public void onError(Throwable e) {
            Timber.e(e);
            final Activity activity = ExportSettingsActivity.this;
            Toast.makeText(activity, "Could not export settings.", Toast.LENGTH_LONG).show();
            activity.finish();
          }

          @Override
          public void onComplete() {
            progressBar.setVisibility(View.GONE);
            title.setVisibility(View.VISIBLE);
            exportDirectory.setVisibility(View.VISIBLE);
            exportDirectory.setText(exportsDir.toString());
          }
        });
  }

  private void copyCardsFromImagesDir(File exportDirectory, Card card) throws IOException {

    final File inputFile = new File(imagesDirectory + File.separator + card.getImage());
    final File outputFile = new File(exportDirectory + File.separator + card.getImage());

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
}

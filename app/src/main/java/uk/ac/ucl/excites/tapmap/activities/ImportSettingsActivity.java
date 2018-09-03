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
import butterknife.BindView;
import butterknife.ButterKnife;
import java.io.File;
import java.io.FileNotFoundException;
import lib.folderpicker.FolderPicker;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.project.ProjectManager;
import uk.ac.ucl.excites.tapmap.project.Settings;
import uk.ac.ucl.excites.tapmap.storage.ImageCardDao;
import uk.ac.ucl.excites.tapmap.storage.NfcCardDao;

/**
 * Created by Michalis Vitos on 24/05/2018.
 */
public class ImportSettingsActivity extends AppCompatActivity {

  public static final int PICK_DIRECTORY = 1;

  @BindView(R.id.root)
  protected View root;

  private ImageCardDao imageCardDao;
  private NfcCardDao nfcCardDao;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_import_settings);
    ButterKnife.bind(this);

    // Get database dao
    final TapMap app = (TapMap) getApplication();
    imageCardDao = app.getAppDatabase().imageCardDao();
    nfcCardDao = app.getAppDatabase().nfcCardDao();

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
    }
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

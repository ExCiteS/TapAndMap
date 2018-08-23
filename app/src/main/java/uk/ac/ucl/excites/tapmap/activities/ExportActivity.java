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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.opencsv.CSVWriter;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.storage.Record;
import uk.ac.ucl.excites.tapmap.storage.RecordDao;
import uk.ac.ucl.excites.tapmap.storage.Session;
import uk.ac.ucl.excites.tapmap.storage.SessionDao;
import uk.ac.ucl.excites.tapmap.utils.Time;

import static uk.ac.ucl.excites.tapmap.activities.MainActivity.DEFAULT_PREFERENCES;
import static uk.ac.ucl.excites.tapmap.activities.MainActivity.GUID;

/**
 * Created by Michalis Vitos on 10/08/2018.
 */
public class ExportActivity extends RxAppCompatActivity {

  private File exportDirectory;
  private SessionDao sessionDao;
  private RecordDao recordDao;
  private CSVWriter sessionsWriter;
  private CSVWriter recordsWriter;
  private File sessionsFile;
  private File recordsFile;
  private ProgressDialog progress;
  private String guid;

  @BindView(R.id.exportDirectory)
  protected TextView exportDirectoryTxt;

  @BindView(R.id.exportData)
  protected Button exportDataButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_export);
    ButterKnife.bind(this);

    // Get database daos
    final TapMap app = (TapMap) getApplication();
    sessionDao = app.getAppDatabase().sessionDao();
    recordDao = app.getAppDatabase().recordDao();

    try {
      // Create export directory
      final String downloads =
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
      final String exportDirectoryString = downloads + "/TapAndMap/exports/";
      exportDirectory = new File(exportDirectoryString);
      exportDirectory.mkdirs();

      // Create files
      Date date = new Date();
      sessionsFile = getExportFile("sessions", date);
      recordsFile = getExportFile("records", date);

      // Show the exportDirectory to the user
      String text = "1. " + sessionsFile.getAbsoluteFile() + "\n\n";
      text += "2. " + recordsFile.getAbsoluteFile() + "\n";
      exportDirectoryTxt.setText(text);
    } catch (Exception e) {

      Timber.e(e, "Error   : %s", e.getLocalizedMessage());
      String message =
          "Cannot set the export directory, make sure the app has Storage permissions.";
      exportDirectoryTxt.setText(message);
      exportDataButton.setEnabled(false);
    }

    final SharedPreferences prefs = getSharedPreferences(DEFAULT_PREFERENCES, Context.MODE_PRIVATE);
    guid = prefs.getString(GUID, "");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    try {
      if (sessionsWriter != null)
        sessionsWriter.close();
      if (recordsWriter != null)
        recordsWriter.close();
    } catch (IOException e) {
      Timber.e(e);
    }
  }

  @NonNull
  private File getExportFile(String filename, Date date) throws IOException {

    final String pathname = exportDirectory
        + File.separator
        + filename
        + "-"
        + Time.getTimeForFile(date)
        + ".csv";
    Timber.d("Creating file: %s", pathname);
    final File file = new File(pathname);
    file.createNewFile();
    return file;
  }

  @SuppressLint("CheckResult")
  @OnClick(R.id.exportData)
  protected void onExportDataClicked() {
    Timber.d("Start Exporting");

    progress = new ProgressDialog(this);
    progress.setMessage("Exporting records");
    progress.setCancelable(false);
    progress.show();

    final Observable<Session> sessionsObservable = sessionDao.getAll()
        .toObservable()
        .flatMap(Observable::fromIterable);

    final Observable<Record> recordsObservable = recordDao.getAll()
        .toObservable()
        .flatMap(Observable::fromIterable);

    // Set Files and Writers
    try {
      if (!sessionsFile.exists())
        sessionsFile.createNewFile();

      if (!recordsFile.exists())
        recordsFile.createNewFile();

      sessionsWriter = new CSVWriter(new FileWriter(sessionsFile));
      recordsWriter = new CSVWriter(new FileWriter(recordsFile));
    } catch (IOException e) {
      Timber.e(e);
    }

    // Add Headers
    if (sessionsFile.length() == 0)
      sessionsWriter.writeNext(Session.getCsvHeader());
    if (recordsFile.length() == 0)
      recordsWriter.writeNext(Record.getCsvHeader());

    sessionsObservable
        .compose(bindToLifecycle())
        .subscribeOn(Schedulers.io())
        .doOnNext(this::exportSession)
        .toList()
        .toObservable()
        .flatMap(sessions -> recordsObservable)
        .doOnNext(this::exportRecord)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            onNext -> {
              // Do nothing
            },
            onError -> {
              // Inform the user
              Toast.makeText(ExportActivity.this,
                  "Cannot export records.",
                  Toast.LENGTH_SHORT)
                  .show();

              // Delete files
              sessionsFile.delete();
              recordsFile.delete();
            },
            () -> {
              Timber.d("Completed!!!");

              Timber.d("Flushing CSV writers");
              try {
                sessionsWriter.close();
                recordsWriter.close();
              } catch (IOException e) {
                Timber.e(e);
              }

              // Delete empty files
              Timber.d("Delete empty files");
              deleteFile(sessionsFile, true);
              deleteFile(recordsFile, true);

              // Hide progress bar
              progress.cancel();
            }
        );
  }

  @OnClick(R.id.deleteExports)
  protected void onDeleteExportsClicked() {

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setTitle("Delete exports")
        .setMessage("Are you sure you want to delete all exports from the device?")
        .setPositiveButton("Yes",
            (dialog, which) -> {
              // Delete all the files from the directory
              for (File file : exportDirectory.listFiles())
                deleteFile(file, false);
            })
        .setNegativeButton("No",
            (dialog, which) -> {
              // Do nothing
            })
        .show();
  }

  private void deleteFile(File file, boolean onlyIfEmpty) {

    if (onlyIfEmpty) {
      if (file.length() == 0) {
        Timber.d("Delete '%s' since it is empty.", file.getName());
        file.delete();
      }
    } else {
      Timber.d("Delete '%s'.", file.getName());
      file.delete();
    }
  }

  private void exportSession(Session session) {
    Timber.d("Exporting session: %s", session.getId());
    sessionsWriter.writeNext(session.toCSV(guid));
  }

  private void exportRecord(Record record) {
    Timber.d("Exporting record: %s", record.getId());
    recordsWriter.writeNext(record.toCSV(guid));
  }
}

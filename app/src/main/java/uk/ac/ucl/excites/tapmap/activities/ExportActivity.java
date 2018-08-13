package uk.ac.ucl.excites.tapmap.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.storage.Record;
import uk.ac.ucl.excites.tapmap.storage.RecordDao;
import uk.ac.ucl.excites.tapmap.storage.Session;
import uk.ac.ucl.excites.tapmap.storage.SessionDao;

/**
 * Created by Michalis Vitos on 10/08/2018.
 */
public class ExportActivity extends RxAppCompatActivity {

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

  private File exportDirectory;
  private SessionDao sessionDao;
  private RecordDao recordDao;
  private CSVWriter sessionsWriter;
  private CSVWriter recordsWriter;
  private File sessionsFile;
  private File recordsFile;
  private ProgressDialog progress;

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

      sessionsWriter = new CSVWriter(new FileWriter(sessionsFile));
      recordsWriter = new CSVWriter(new FileWriter(recordsFile));
    } catch (Exception e) {

      Timber.e(e, "Error   : %s", e.getLocalizedMessage());
      String message =
          "Cannot set the export directory, make sure the app has Storage permissions.";
      exportDirectoryTxt.setText(message);
      exportDataButton.setEnabled(false);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    try {
      sessionsWriter.close();
      recordsWriter.close();
    } catch (IOException e) {
      Timber.e(e);
    }

    // Delete empty files
    deleteEmptyFile(sessionsFile);
    deleteEmptyFile(recordsFile);

    // Check for other emtpy files
    for (File file : exportDirectory.listFiles())
      deleteEmptyFile(file);
  }

  private void deleteEmptyFile(File file) {
    if (file.length() == 0) {
      Timber.d("Delete '%s' since it is empty.", file.getName());
      file.delete();
    }
  }

  @NonNull
  private File getExportFile(String filename, Date date) throws IOException {

    final String pathname = exportDirectory
        + File.separator
        + filename
        + "-"
        + dateFormat.format(date)
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

              try {
                sessionsWriter.flush();
                recordsWriter.flush();
              } catch (IOException e) {
                Timber.e(e);
              }

              progress.cancel();
            }
        );
  }

  private void exportSession(Session session) {
    Timber.d("Exporting session: %s", session.getId());
    sessionsWriter.writeNext(session.toCSV());
  }

  private void exportRecord(Record record) {
    Timber.d("Exporting record: %s", record.getId());
    recordsWriter.writeNext(record.toCSV());
  }
}

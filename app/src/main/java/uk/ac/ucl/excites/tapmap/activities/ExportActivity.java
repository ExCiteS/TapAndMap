package uk.ac.ucl.excites.tapmap.activities;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.storage.RecordDao;
import uk.ac.ucl.excites.tapmap.storage.SessionDao;

/**
 * Created by Michalis Vitos on 10/08/2018.
 */
public class ExportActivity extends AppCompatActivity {

  private File exportDirectory;
  private SessionDao sessionDao;
  private RecordDao recordDao;
  private CSVWriter sessionsWriter;
  private CSVWriter recordsWriter;
  private File sessionsFile;
  private File recordsFile;

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
      sessionsFile = getExportFile("sessions");
      recordsFile = getExportFile("records");

      // Show the exportDirectory to the user
      String text = "1. " + sessionsFile.getAbsoluteFile() + "\n";
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

  @NonNull
  private File getExportFile(String filename) throws IOException {

    final String pathname = exportDirectory
        + File.separator
        + filename
        + "-"
        + new Date().getTime()
        + ".csv";
    Timber.d("Creating file: %s", pathname);
    final File file = new File(pathname);
    file.createNewFile();
    return file;
  }
}

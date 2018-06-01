package uk.ac.ucl.excites.tapmap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.storage.Session;
import uk.ac.ucl.excites.tapmap.storage.SessionController;
import uk.ac.ucl.excites.tapmap.storage.SessionDao;

/**
 * Created by Michalis Vitos on 01/06/2018.
 */
public class SessionActivity extends AppCompatActivity {

  // UI
  @BindView(R.id.sessionId)
  protected TextView sessionId;

  private SessionDao sessionDao;
  private SessionController sessionController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_session);
    ButterKnife.bind(this);

    final TapMap app = (TapMap) getApplication();
    sessionDao = app.getAppDatabase().sessionDao();
    sessionController = new SessionController(sessionDao);
  }

  @Override
  protected void onResume() {
    super.onResume();

    setActiveSession();
  }

  private void setActiveSession() {

    final Session activeSession = sessionController.getActiveSession();
    sessionId.setText(String.valueOf(activeSession.getId()));
  }

  public void openNFCActivity() {
    Timber.d("Go to NFC activity");

    Intent intent = new Intent(this, TapAndMapActivity.class);
    startActivity(intent);
  }
}

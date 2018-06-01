package uk.ac.ucl.excites.tapmap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
  @BindView(R.id.sessionDesc)
  protected EditText sessionDesc;

  private SessionDao sessionDao;
  private Session activeSession;
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

    setActiveSession(sessionController.getActiveSession());
  }

  @OnClick(R.id.newSession)
  protected void onNewSessionClicked() {

    updateActiveSession();

    activeSession = sessionController.openNewSession();
    setActiveSession(activeSession);
  }

  @OnClick(R.id.collectData)
  protected void onCollectDataClicked() {

    updateActiveSession();

    openNFCActivity();
  }

  private void setActiveSession(Session activeSession) {

    sessionId.setText(String.valueOf(activeSession.getId()));
    sessionDesc.setText(String.valueOf(activeSession.getDescription()));
  }

  private void openNFCActivity() {
    Timber.d("Go to NFC activity");

    Intent intent = new Intent(this, TapAndMapActivity.class);
    startActivity(intent);
  }

  /**
   * Update Active Session
   */
  private void updateActiveSession() {
    activeSession.setDescription(sessionDesc.getText().toString());
    sessionDao.insert(activeSession);
  }
}

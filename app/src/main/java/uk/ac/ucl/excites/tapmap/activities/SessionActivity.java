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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.controllers.NavigationController;
import uk.ac.ucl.excites.tapmap.controllers.NavigationController.Screens;
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
    sessionController = new SessionController(this);
  }

  @Override
  protected void onResume() {
    super.onResume();

    activeSession = sessionController.getActiveSession();
    setActiveSessionUI(activeSession);
  }

  @OnClick(R.id.newSession)
  protected void onNewSessionClicked() {

    updateActiveSession();

    activeSession = sessionController.openNewSession();
    setActiveSessionUI(activeSession);
  }

  @OnClick(R.id.collectData)
  protected void onCollectDataClicked() {

    updateActiveSession();

    final NavigationController navigationController = NavigationController.getInstance();
    navigationController.setCurrentScreen(Screens.SESSION, null);
    navigationController.goToNextScreen(this, false);
  }

  private void setActiveSessionUI(Session activeSession) {

    sessionId.setText(String.valueOf(activeSession.getId()));
    sessionDesc.setText(String.valueOf(activeSession.getDescription()));
  }

  /**
   * Update Active Session
   */
  private void updateActiveSession() {
    activeSession.setDescription(sessionDesc.getText().toString());
    sessionDao.insert(activeSession);
  }
}

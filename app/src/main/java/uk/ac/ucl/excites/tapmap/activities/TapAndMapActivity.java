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
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import java.io.File;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.controllers.NavigationController;
import uk.ac.ucl.excites.tapmap.controllers.NavigationController.Screens;
import uk.ac.ucl.excites.tapmap.nfc.NfcTagParser;
import uk.ac.ucl.excites.tapmap.storage.ImageCard;
import uk.ac.ucl.excites.tapmap.storage.ImageCardDao;
import uk.ac.ucl.excites.tapmap.storage.NfcCard;
import uk.ac.ucl.excites.tapmap.storage.NfcCardDao;
import uk.ac.ucl.excites.tapmap.storage.RecordController;
import uk.ac.ucl.excites.tapmap.storage.SessionController;
import uk.ac.ucl.excites.tapmap.utils.Logger;

import static uk.ac.ucl.excites.tapmap.utils.Logger.TAG.CLICK;
import static uk.ac.ucl.excites.tapmap.utils.Logger.TAG.TOUCHED;

public class TapAndMapActivity extends NfcBaseActivity {

  public static final int MAX_SIZE = 700;

  @BindView(R.id.nfc)
  protected ImageView nfcImageView;
  @BindView(R.id.confirm)
  protected ImageButton confirmButton;
  @BindView(R.id.cancel)
  protected ImageButton cancelButton;

  private RecordController recordController;
  private ImageCardDao imageCardDao;
  private NfcCardDao nfcCardDao;
  private String session;
  private ImageCard imageCard;
  private NfcCard nfcCard;
  private Picasso picasso;
  private Gson gson = new Gson();

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tap_and_map);
    ButterKnife.bind(this);

    final TapMap app = (TapMap) getApplication();
    recordController = new RecordController(this);
    imageCardDao = app.getAppDatabase().imageCardDao();
    nfcCardDao = app.getAppDatabase().nfcCardDao();

    final SessionController sessionController = new SessionController(this);
    session = "SESSION:" + sessionController.getActiveSessionId();

    // Set up Picasso
    picasso = Picasso.get();
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Start with the default UI
    resetUI();
  }

  private void resetUI() {

    // Reset image
    nfcImageView.animate().alpha(1.0f);
    nfcImageView.setImageResource(R.drawable.ic_nfc);

    // Hide Buttons
    hideButtons();
  }

  private void hideButtons() {
    confirmButton.animate().alpha(0.0f);
    cancelButton.animate().alpha(0.0f);
    confirmButton.setVisibility(View.INVISIBLE);
    cancelButton.setVisibility(View.INVISIBLE);
  }

  private void showButtons() {
    confirmButton.animate().alpha(1.0f);
    cancelButton.animate().alpha(1.0f);
    confirmButton.setVisibility(View.VISIBLE);
    cancelButton.setVisibility(View.VISIBLE);
  }

  @Override
  protected void handleNfcCard(NfcTagParser nfcTagParser) {

    final String cardID = nfcTagParser.getId();
    String imagePath = "";

    try {
      nfcCard = nfcCardDao.findById(cardID);
      imageCard = imageCardDao.findById(nfcCard.getImageCardId());
      imagePath = imageCard.getImagePath();

      // Log card
      Logger.getInstance().log(TOUCHED, session, nfcCard.toJson().toString());
    } catch (Exception e) {
      Timber.e(e, "Cannot get icon.");
    }

    Timber.d("NFC ID: %s. Path: %s", cardID, imagePath);

    // Show NFC card
    picasso.load(new File(imagePath))
        .placeholder(R.drawable.progress_animation)
        .error(R.drawable.ic_error)
        .resize(MAX_SIZE, MAX_SIZE)
        .centerInside()
        .into(nfcImageView);

    // Show buttons
    showButtons();
  }

  @OnClick(R.id.confirm)
  protected void onConfirmClicked() {

    // Reset UI
    resetUI();

    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();

    // Log click
    if (nfcCard != null)
      Logger.getInstance().log(CLICK, session, "Confirm: ", nfcCard.toJson().toString());

    final JsonObject meta = (JsonObject) gson.toJsonTree(nfcCard);
    meta.addProperty("tag", imageCard.getTag());

    // Go to next
    final NavigationController navigationController = NavigationController.getInstance();
    navigationController.setCurrentScreen(Screens.NFC, meta);
    navigationController.setCurrentImageCard(imageCard);
    navigationController.goToNextScreen(this);
  }

  @OnClick(R.id.cancel)
  protected void onCancelClicked() {

    // Log click
    if (nfcCard != null) {
      Logger.getInstance().log(CLICK, session, "Cancel", nfcCard.toJson().toString());
    }

    // Clear activity and finish
    NavigationController.getInstance().cancel(this);
  }
}

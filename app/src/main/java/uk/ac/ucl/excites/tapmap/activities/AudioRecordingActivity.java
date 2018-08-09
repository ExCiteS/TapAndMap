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

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.github.piasy.rxandroidaudio.AudioRecorder;
import com.github.piasy.rxandroidaudio.RxAmplitude;
import com.google.gson.JsonArray;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.controllers.NavigationController;
import uk.ac.ucl.excites.tapmap.controllers.NavigationController.Screens;
import uk.ac.ucl.excites.tapmap.utils.Colour;
import uk.ac.ucl.excites.tapmap.utils.MathUtils;
import uk.ac.ucl.excites.tapmap.utils.ScreenMetrics;
import uk.ac.ucl.excites.tapmap.utils.Time;

public class AudioRecordingActivity extends RxAppCompatActivity
    implements AudioRecorder.OnErrorListener {

  // Static
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh.mm.ss");
  private static final String AUDIO_EXTENSION = "m4a";

  private static final ButterKnife.Action<View> INVISIBLE =
      (view, index) -> view.setVisibility(View.INVISIBLE);
  private static final ButterKnife.Action<View> VISIBLE
      = (view, index) -> view.setVisibility(View.VISIBLE);

  // Private
  private AudioRecorder audioRecorder = AudioRecorder.getInstance();
  //private RxAudioPlayer audioPlayer = RxAudioPlayer.getInstance();
  private List<File> recordings;
  private File currentAudioFile;
  private Disposable recordDisposable;
  private int maxAmplitude = 0;
  private boolean isRecording;

  // UI
  @BindView(R.id.recordButton)
  protected ImageView recordButton;
  @BindView(R.id.status)
  protected TextView status;
  @BindView(R.id.voice_indicator)
  protected LinearLayout voiceIndicatorLayout;
  @BindView(R.id.confirm)
  protected ImageButton confirmButton;
  @BindView(R.id.cancel)
  protected ImageButton cancelButton;
  private List<View> voiceIndicators;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_audio_recording);
    ButterKnife.bind(this);

    // List of recordings
    recordings = new ArrayList<>();

    // Set UI
    ButterKnife.apply(confirmButton, INVISIBLE);
    ButterKnife.apply(cancelButton, INVISIBLE);
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);

    // Use the onWindowFocusChanged method to get the correct dimensions for the elements
    setVoiceIndicators();
  }

  /**
   * Collect the resource ids of the voice indicators
   */
  private void setVoiceIndicators() {

    final LayoutInflater inflater = LayoutInflater.from(this);
    voiceIndicators = new ArrayList<>();

    final int height = voiceIndicatorLayout.getHeight();
    final int width = voiceIndicatorLayout.getWidth();
    Timber.d("Voice Indicator: %sx%s pixels.", width, height);
    final float voiceIndicatorHeight = ScreenMetrics.convertDpToPixel(3);
    Timber.d("Each voice indicator is 10 dp = %s pixels", voiceIndicatorHeight);
    final int countVoiceIndicators = (int) (height / voiceIndicatorHeight);
    Timber.d("We can fit %s voice indicators in the screen.", countVoiceIndicators);

    // Add colours
    int[] colors = {
        ContextCompat.getColor(this, R.color.voice_indicator_high),
        ContextCompat.getColor(this, R.color.voice_indicator_medium),
        ContextCompat.getColor(this, R.color.voice_indicator_low)
    };
    float[] positions = { 0, countVoiceIndicators / 2, countVoiceIndicators };

    // Inflate views
    for (int i = 0; i < countVoiceIndicators; i++) {

      // Create line indicator and add colour
      View view = inflater.inflate(R.layout.voice_indicator, voiceIndicatorLayout, false);
      view.setBackgroundColor(Colour.getColorFromGradient(colors, positions, i));

      voiceIndicatorLayout.addView(view);
      voiceIndicators.add(view);
    }

    // Reverse the list, as we need the bottom items to be last
    Collections.reverse(voiceIndicators);

    // Hide the indicators
    ButterKnife.apply(voiceIndicators, INVISIBLE);
  }

  @OnClick(R.id.recordButton)
  public void onButtonClicked() {
    if (!isRecording)
      startRecording();
    else
      stopRecording();
  }

  @OnClick(R.id.confirm)
  public void onConfirmClicked() {

    Timber.d("Recorded so far: %s", recordings);

    // Create the meta
    final JsonArray meta = new JsonArray();
    for (File recording : recordings)
      meta.add(recording.getName());

    final NavigationController navigationController = NavigationController.getInstance();
    navigationController.setCurrentScreen(Screens.AUDIO, meta);
    navigationController.goToNextScreen(this);
  }

  @OnClick(R.id.cancel)
  public void onCancelClicked() {

    Timber.d("Cancel recording, must delete: %s", recordings);
    for (File recording : recordings) {
      boolean deleted = recording.delete();
      if (deleted)
        Timber.d("Deleted: %s", recording);
      else
        Timber.d("Could not delete: %s", recording);
    }

    // Clear activity and finish
    NavigationController.getInstance().cancel(this);
  }

  private void updateRecordingUI() {
    Timber.d("Update recording UI");
    recordButton.setImageResource(R.drawable.ic_stop);
    ButterKnife.apply(confirmButton, INVISIBLE);
    ButterKnife.apply(cancelButton, INVISIBLE);
  }

  private void updateStoppedUI() {
    Timber.d("Update stopping UI");

    // Change icon
    recordButton.setImageResource(R.drawable.ic_record);

    // Show buttons
    ButterKnife.apply(confirmButton, VISIBLE);
    ButterKnife.apply(cancelButton, VISIBLE);
    // Hide the indicators
    ButterKnife.apply(voiceIndicators, INVISIBLE);
  }

  private void startRecording() {

    Timber.d("Clicked recording...");

    recordDisposable = Observable
        .fromCallable(() -> {
          Timber.d("Prepare record...");
          currentAudioFile = getAudioFile();
          return audioRecorder.prepareRecord(
              MediaRecorder.AudioSource.MIC,
              MediaRecorder.OutputFormat.MPEG_4,
              MediaRecorder.AudioEncoder.AAC,
              currentAudioFile);
        })
        .doOnComplete(() -> {
          Timber.d("Start recording...");
          audioRecorder.startRecord();
        })
        .doOnNext(b -> Timber.d("Start recording successfully..."))
        .flatMap(b -> RxAmplitude.from(audioRecorder, 250))
        .compose(bindUntilEvent(ActivityEvent.STOP))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            //onNext
            level -> {
              int progress = audioRecorder.progress();
              refreshAudioAmplitudeView(level, progress);

              if (!isRecording) {
                updateRecordingUI();
                isRecording = true;
              }
            },
            // onError
            Timber::e,
            // onComplete
            this::stopRecording
        );
  }

  private void refreshAudioAmplitudeView(int amplitudeLevel, int progressInSeconds) {

    // Capture max amplitude
    maxAmplitude = amplitudeLevel > maxAmplitude ? amplitudeLevel : maxAmplitude;

    // Log Amplitude progressInSeconds every 5 seconds
    if (progressInSeconds % 5 == 0) {
      Timber.d("Refresh UI with amplitudeLevel: %s/%s, progressInSeconds: %s",
          amplitudeLevel,
          maxAmplitude,
          progressInSeconds);
    }

    // Update time
    status.setText(Time.convertSecondsToHumanTime(progressInSeconds));

    // Update Voice Indicators
    final int voiceIndicatorSize = voiceIndicators.size();
    int adjustedLevel = (int) MathUtils.map(amplitudeLevel, 0, maxAmplitude, 0, voiceIndicatorSize);
    // Add a random element to the level
    if (adjustedLevel == 0) {
      Random random = new Random();
      int randomInt = random.nextInt(voiceIndicatorSize / 10) + 1;
      // 10% of voiceIndicatorSize]
      adjustedLevel += randomInt;
    }
    int max = adjustedLevel > voiceIndicatorSize ? voiceIndicatorSize : adjustedLevel;
    // Timber.d("Level (actual/adjusted/max): %s/%s/%s", amplitudeLevel, adjustedLevel, maxAmplitude);

    ButterKnife.apply(voiceIndicators.subList(0, max), VISIBLE);
    ButterKnife.apply(voiceIndicators.subList(max, voiceIndicatorSize), INVISIBLE);
  }

  private void stopRecording() {

    if (recordDisposable != null && !recordDisposable.isDisposed()) {
      recordDisposable.dispose();
      recordDisposable = null;
    }

    recordDisposable = Observable
        .fromCallable(() -> {
          final int seconds = audioRecorder.stopRecord();
          Timber.d("Stopped recording. Recorded %s seconds.", seconds);
          recordings.add(currentAudioFile);
          return seconds >= 0;
        })
        .compose(bindToLifecycle())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(b -> updateStoppedUI())
        .doOnNext(b -> isRecording = false)
        .subscribe(
            b -> Timber.d("File: %s | MaxAmplitude: %s",
                currentAudioFile.getName(),
                maxAmplitude),
            Timber::e);
  }

  @NonNull
  private File getAudioFile() throws IOException {
    final String downloads =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    final String recordingsDir = downloads + "/TapAndMap/audio/";
    final String fileName = "audio-" + dateFormat.format(new Date()) + "." + AUDIO_EXTENSION;
    final String filePath = recordingsDir + fileName;
    final File file = new File(filePath);
    Timber.d("Record at: %s", filePath);
    if (!file.exists()) {
      final boolean mkdirs = file.getParentFile().mkdirs();
      final boolean newFile = file.createNewFile();
      if (mkdirs)
        Timber.d("Created directory: %s", file.getParentFile());
      if (newFile)
        Timber.d("Created file: %s", file.getAbsoluteFile());
    }
    return file;
  }

  @Override
  public void onError(int error) {
    Timber.d("Error while recording: %s", error);
  }
}
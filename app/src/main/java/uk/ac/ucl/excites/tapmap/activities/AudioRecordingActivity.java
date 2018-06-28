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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.github.piasy.rxandroidaudio.AudioRecorder;
import com.github.piasy.rxandroidaudio.RxAmplitude;
import com.github.piasy.rxandroidaudio.RxAudioPlayer;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;

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
  private RxAudioPlayer audioPlayer = RxAudioPlayer.getInstance();
  private File currentAudioFile;
  private Disposable recordDisposable;

  // UI
  @BindView(R.id.recordButton)
  protected ImageView recordButton;
  @BindView(R.id.status)
  protected TextView status;
  private List<ImageView> voiceIndicators;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_audio_recording);
    ButterKnife.bind(this);

    setVoiceIndicators();
  }

  /**
   * Collect the resource ids of the voice indicators
   */
  private void setVoiceIndicators() {

    voiceIndicators = new ArrayList<>();

    // TODO: 28/06/2018 Add exact number of items
    for (int i = 1; i <= 20; i++) {
      String voiceIndicatorId = "indication_" + i;
      int resID = getResources().getIdentifier(voiceIndicatorId, "id", getPackageName());
      voiceIndicators.add(this.findViewById(resID));
    }

    // Hide the indicators
    ButterKnife.apply(voiceIndicators, INVISIBLE);
  }

  @OnClick(R.id.recordButton)
  public void onRecordClicked() {

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
        .flatMap(b -> RxAmplitude.from(audioRecorder))
        .compose(bindToLifecycle())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            level -> {
              int progress = audioRecorder.progress();
              refreshAudioAmplitudeView(level, progress);
            },
            Timber::e);
  }

  private void refreshAudioAmplitudeView(int level, int progress) {

    // Log Amplitude progress every 5 seconds
    if (progress % 5 == 0)
      Timber.d("Refresh UI with level: %s, progress: %s", level, progress);

    // TODO: 28/06/2018
    status.setText(convertSecondsToHumanTime(progress));

    int end = level < voiceIndicators.size() ? level : voiceIndicators.size();
    ButterKnife.apply(voiceIndicators.subList(0, end), VISIBLE);
    ButterKnife.apply(voiceIndicators.subList(end, voiceIndicators.size()), INVISIBLE);
  }

  public String convertSecondsToHumanTime(int totalSeconds) {

    int hours = totalSeconds / 3600;
    int minutes = (totalSeconds % 3600) / 60;
    int seconds = totalSeconds % 60;

    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
  }

  @OnClick(R.id.stopButton)
  public void onStopClicked() {

    if (recordDisposable != null && !recordDisposable.isDisposed()) {
      recordDisposable.dispose();
      recordDisposable = null;
    }

    recordDisposable = Observable
        .fromCallable(() -> {
          final int seconds = audioRecorder.stopRecord();
          Timber.d("Stopped recording. Recorded %s seconds.", seconds);
          return seconds >= 0;
        })
        .compose(bindToLifecycle())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            added -> {
              if (added)
                Timber.d("File: %s recorded.", currentAudioFile.getName());

              status.setText(R.string.zero_time);
            },
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
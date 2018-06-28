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
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.github.piasy.rxandroidaudio.AudioRecorder;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;

public class AudioRecordingActivity extends AppCompatActivity {

  // Static
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd_hh.mm.ss");
  private static final String AUDIO_EXTENSION = "m4a";

  // Private
  private AudioRecorder audioRecorder;
  private File currentAudioFile;

  // UI
  @BindView(R.id.recordButton)
  protected ImageView recordButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_audio_recording);
    ButterKnife.bind(this);

    try {
      setupRecorder();
    } catch (Exception e) {
      Timber.e(e);
    }
  }

  private void setupRecorder() throws IOException {

    audioRecorder = AudioRecorder.getInstance();
    currentAudioFile = getAudioFile();
    audioRecorder.prepareRecord(
        MediaRecorder.AudioSource.MIC,
        MediaRecorder.OutputFormat.MPEG_4,
        MediaRecorder.AudioEncoder.AAC,
        currentAudioFile
    );
  }

  private void animateVoice(final float maxPeak) {
    recordButton.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(10).start();
  }

  @OnClick(R.id.recordButton)
  public void onRecordClicked() {
    Timber.d("Start recording...");
    audioRecorder.startRecord();
  }

  @OnClick(R.id.stopButton)
  public void onStopClicked() {
    final int seconds = audioRecorder.stopRecord();
    Timber.d("Stopped recording. Recorded %s seconds.", seconds);
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
      Timber.d("Created directory: %s and file: %s", mkdirs, newFile);
    }
    return file;
  }
}
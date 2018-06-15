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

package uk.ac.ucl.excites.tapmap.utils;

import android.os.Environment;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.StatusPrinter;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import timber.log.Timber;

/**
 * Created by Michalis Vitos on 08/06/2018.
 */
@Slf4j
public class Logger {

  private static final String LOG_PREFIX = "tam-log";
  private static final Logger ourInstance = new Logger();

  public enum TAG {
    ANDROID, CLICK,
  }

  public static Logger getInstance() {
    return ourInstance;
  }

  private Logger() {

    // Set Logger
    final String downloads =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    final String logDirectory = downloads + "/TapAndMap/logs";
    setLogback(logDirectory);
  }

  private void setLogback(String logDirectory) {

    // reset the default context (which may already have been initialized)
    // since we want to reconfigure it
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    loggerContext.reset();

    RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
    rollingFileAppender.setContext(loggerContext);
    rollingFileAppender.setAppend(true);
    rollingFileAppender.setFile(logDirectory + "/" + LOG_PREFIX + "-latest.csv");

    SizeAndTimeBasedFNATP<ILoggingEvent> fileNamingPolicy = new SizeAndTimeBasedFNATP<>();
    fileNamingPolicy.setContext(loggerContext);
    fileNamingPolicy.setMaxFileSize("10MB");

    TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
    rollingPolicy.setContext(loggerContext);
    rollingPolicy.setFileNamePattern(logDirectory + "/" + LOG_PREFIX + ".%d{yyyy-MM-dd}.%i.html");
    rollingPolicy.setMaxHistory(100);
    rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(fileNamingPolicy);
    rollingPolicy.setParent(rollingFileAppender);  // parent and context required!
    rollingPolicy.start();

    PatternLayout patternLayout = new PatternLayout();
    patternLayout.setContext(loggerContext);
    patternLayout.setPattern("%date%level%thread%msg");
    patternLayout.start();

    // Alternative text encoder - very clean pattern, takes up less space
    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(loggerContext);
    encoder.setCharset(Charset.forName("UTF-8"));
    encoder.setPattern("\"%date\",\"%level\",%msg%n");
    encoder.start();

    rollingFileAppender.setRollingPolicy(rollingPolicy);
    rollingFileAppender.setEncoder(encoder);
    rollingFileAppender.start();

    // add the newly created appenders to the root logger;
    // qualify Logger to disambiguate from org.slf4j.Logger
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
        ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.DEBUG);
    root.addAppender(rollingFileAppender);

    // print any status messages (warnings, etc) encountered in logback config
    StatusPrinter.print(loggerContext);
  }

  public void log(String... messages) {

    if (messages.length <= 0)
      return;

    StringBuilder builder = new StringBuilder();
    for (String message : messages)
      builder.append("\"").append(message).append("\"").append(",");

    builder.setLength(builder.length() - 1);

    final String log = builder.toString();
    Logger.log.info(log);
    Timber.d("LOG: %s", log);
  }

  public void log(TAG tag, String... messages) {

    String[] newMessages = new String[messages.length + 1];
    newMessages[0] = tag.toString();
    System.arraycopy(messages, 0, newMessages, 1, messages.length);

    log(newMessages);
  }
}

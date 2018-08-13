package uk.ac.ucl.excites.tapmap.utils;

import org.apache.commons.text.StringEscapeUtils;
import timber.log.Timber;

/**
 * Created by Michalis Vitos on 13/07/2018.
 */
public class CSVUtils {

  public String toCSVColumn (String... messages) {

    if (messages.length <= 0)
      return "";

    StringBuilder builder = new StringBuilder();
    for (String message : messages)
      builder.append(StringEscapeUtils.escapeCsv(message)).append(",");

    builder.setLength(builder.length() - 1);

    final String log = builder.toString();
    //Logger.log.info(log);
    Timber.d("LOG: %s", log);

    return null;
  }
}

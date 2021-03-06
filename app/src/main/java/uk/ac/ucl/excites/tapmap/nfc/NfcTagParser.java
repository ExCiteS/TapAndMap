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

package uk.ac.ucl.excites.tapmap.nfc;

import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import lombok.Getter;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.storage.ImageCard;
import uk.ac.ucl.excites.tapmap.storage.NfcCard;

/**
 * Class that represents an NFC card
 *
 * Created by Michalis Vitos on 17/05/2018.
 */
public class NfcTagParser {

  public static final String DEFAULT_SEPARATOR = ":";

  @Getter
  private String id = "";
  private String idHex = "";
  private long idDec = -1;

  public NfcTagParser(Tag tag) {
    this(tag, DEFAULT_SEPARATOR);
  }

  public NfcTagParser(Tag tag, String separator) {

    final byte[] tagId = tag.getId();
    idHex = toHex(tagId, separator);
    idDec = toDec(tagId);

    id = getCardID();
  }

  /**
   * Parse the tag for a MifareUltralight ID
   */
  private int getMifareUltralightID(Tag tag) {

    int ultralightId = -1;

    MifareUltralight mifareUlTag = MifareUltralight.get(tag);

    try {
      mifareUlTag.connect();
      byte[] payload = mifareUlTag.readPages(3);
      ultralightId = Integer.parseInt(toReversedHex(payload, "").substring(0, 8), 16);
    } catch (Exception e) {
      Timber.e(e, "Read MifareUltralight.");
    } finally {
      try {
        mifareUlTag.close();
      } catch (Exception e) {
        Timber.e(e, "Cannot close MifareUltralight");
      }
    }

    return ultralightId;
  }

  private String toHex(byte[] bytes, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int i = bytes.length - 1; i >= 0; --i) {
      int b = bytes[i] & 0xff;
      if (b < 0x10)
        sb.append('0');
      sb.append(Integer.toHexString(b));
      if (i > 0) {
        sb.append(separator);
      }
    }
    return sb.toString();
  }

  private String toReversedHex(byte[] bytes, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bytes.length; ++i) {
      if (i > 0) {
        sb.append(separator);
      }
      int b = bytes[i] & 0xff;
      if (b < 0x10)
        sb.append('0');
      sb.append(Integer.toHexString(b));
    }
    return sb.toString();
  }

  private long toDec(byte[] bytes) {
    long result = 0;
    long factor = 1;
    for (int i = 0; i < bytes.length; ++i) {
      long value = bytes[i] & 0xffl;
      result += value * factor;
      factor *= 256l;
    }
    return result;
  }

  private long toReversedDec(byte[] bytes) {
    long result = 0;
    long factor = 1;
    for (int i = bytes.length - 1; i >= 0; --i) {
      long value = bytes[i] & 0xffl;
      result += value * factor;
      factor *= 256l;
    }
    return result;
  }

  /**
   * Method to find the most appropriate id for a card. This can be used to uniquely identify the card.
   */
  private String getCardID() {

    // Calculate once and cache
    if (!id.isEmpty())
      return id;

    if (idDec > 0) {
      id = String.valueOf(idDec);
    } else if (!idHex.isEmpty()) {
      id = idHex;
    }

    return id;
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("NFC Info:").append('\n');
    sb.append("NFC ID:").append(id).append('\n');
    sb.append("ID (hex): ").append(idHex).append('\n');
    sb.append("ID (dec): ").append(idDec).append('\n');

    return sb.toString();
  }
}

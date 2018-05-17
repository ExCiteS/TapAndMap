package excites.ucl.ac.uk.tapmap.nfc;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import timber.log.Timber;

/**
 * Class that represents an NFC card
 *
 * Created by Michalis Vitos on 17/05/2018.
 */
@Getter
public class NfcCard {

  public static final String DEFAULT_SEPARATOR = ":";

  private Tag tag;
  private String idHex = "";
  private String idHexReverse = "";
  private long idDec = -1;
  private long idDecReverse = -1;
  private long idMifareUltralight = -1;
  private List<String> technologies = new ArrayList<>();

  public NfcCard(Tag tag) {
    this(tag, DEFAULT_SEPARATOR);
  }

  public NfcCard(Tag tag, String separator) {

    this.tag = tag;

    byte[] id = tag.getId();
    idHex = toHex(id, separator);
    idHexReverse = toReversedHex(id, separator);
    idDec = toDec(id);
    idDecReverse = toReversedDec(id);

    // Get Technologies
    String prefix = "android.nfc.tech.";
    for (String tech : tag.getTechList()) {

      // Store list of technologies
      technologies.add(tech.substring(prefix.length()));

      // Read and convert the ID for MifareUltralight
      if (tech.equals(MifareUltralight.class.getName())) {
        idMifareUltralight = getMifareUltralightID(tag);
      }
    }
  }

  /**
   * Parse the tag for a MifareUltralight ID
   */
  private int getMifareUltralightID(Tag tag) {

    int id = -1;

    MifareUltralight mifareUlTag = MifareUltralight.get(tag);

    try {
      mifareUlTag.connect();
      byte[] payload = mifareUlTag.readPages(3);
      id = Integer.parseInt(toReversedHex(payload, "").substring(0, 8), 16);
    } catch (IOException e) {
      Timber.e(e, "Read MifareUltralight.");
    } finally {
      try {
        mifareUlTag.close();
      } catch (IOException e) {
        Timber.e(e, "Cannot close MifareUltralight");
      }
    }

    return id;
  }

  private String toHex(byte[] bytes, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int i = bytes.length - 1; i >= 0; --i) {
      int b = bytes[i] & 0xff;
      if (b < 0x10) sb.append('0');
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
      if (b < 0x10) sb.append('0');
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

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("NFC Info:").append('\n');
    sb.append("ID (hex): ").append(idHex).append('\n');
    sb.append("ID (reversed hex): ").append(idHexReverse).append('\n');
    sb.append("ID (dec): ").append(idDec).append('\n');
    sb.append("ID (reversed dec): ").append(idDecReverse).append('\n');

    sb.append("Technologies: ");
    for (String tech : technologies)
      sb.append(tech).append(", ");

    sb.delete(sb.length() - 2, sb.length());

    // Loop Technologies
    for (String tech : tag.getTechList()) {
      if (tech.equals(MifareClassic.class.getName())) {
        sb.append('\n');
        String type = "Unknown";

        try {
          MifareClassic mifareTag = MifareClassic.get(tag);

          switch (mifareTag.getType()) {
            case MifareClassic.TYPE_CLASSIC:
              type = "Classic";
              break;
            case MifareClassic.TYPE_PLUS:
              type = "Plus";
              break;
            case MifareClassic.TYPE_PRO:
              type = "Pro";
              break;
          }
          sb.append("Mifare Classic type: ");
          sb.append(type);
          sb.append('\n');

          sb.append("Mifare size: ");
          sb.append(mifareTag.getSize() + " bytes");
          sb.append('\n');

          sb.append("Mifare sectors: ");
          sb.append(mifareTag.getSectorCount());
          sb.append('\n');

          sb.append("Mifare blocks: ");
          sb.append(mifareTag.getBlockCount());
        } catch (Exception e) {
          sb.append("Mifare classic error: " + e.getMessage());
        }
      }

      if (tech.equals(MifareUltralight.class.getName())) {
        sb.append('\n');
        MifareUltralight mifareUlTag = MifareUltralight.get(tag);
        String type = "Unknown";
        switch (mifareUlTag.getType()) {
          case MifareUltralight.TYPE_ULTRALIGHT:
            type = "Ultralight";
            break;
          case MifareUltralight.TYPE_ULTRALIGHT_C:
            type = "Ultralight C";
            break;
        }
        sb.append("Mifare Ultralight type: ").append(type).append('\n');
        sb.append("Mifare id: ").append(idMifareUltralight).append('\n');
      }
    }

    return sb.toString();
  }
}

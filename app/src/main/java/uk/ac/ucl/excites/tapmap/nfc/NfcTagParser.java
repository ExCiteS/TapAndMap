package uk.ac.ucl.excites.tapmap.nfc;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.storage.NfcCard;

/**
 * Class that represents an NFC card
 *
 * Created by Michalis Vitos on 17/05/2018.
 */
@Getter
public class NfcTagParser {

  public static final String DEFAULT_SEPARATOR = ":";

  private Tag tag;
  private String id = "";
  private String idHex = "";
  private String idHexReverse = "";
  private long idDec = -1;
  private long idDecReverse = -1;
  private long idMifareUltralight = -1;
  private List<String> technologies = new ArrayList<>();

  public NfcTagParser(Tag tag) {
    this(tag, DEFAULT_SEPARATOR);
  }

  public NfcTagParser(Tag tag, String separator) {

    this.tag = tag;

    byte[] tagId = tag.getId();
    idHex = toHex(tagId, separator);
    idHexReverse = toReversedHex(tagId, separator);
    idDec = toDec(tagId);
    idDecReverse = toReversedDec(tagId);

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

  /**
   * Method to find the most appropriate id for a card. This can be used to uniquely identify the card.
   */
  public String getCardID() {

    // Calculate once and cache
    if (!id.isEmpty()) return id;

    if (idMifareUltralight > 0) {
      id = String.valueOf(idMifareUltralight);
    } else if (idDec > 0) {
      id = String.valueOf(idDec);
    } else if (idDecReverse > 0) {
      id = String.valueOf(idDecReverse);
    } else if (!idHex.isEmpty()) {
      id = idHex;
    } else if (!idHexReverse.isEmpty()) {
      id = idHexReverse;
    }

    return id;
  }

  /**
   * Get an NFC Card from the {@link NfcTagParser}
   *
   * @return NfcCard
   */
  public NfcCard toNfcCard(String imagePath) {
    return new NfcCard(getCardID(), imagePath);
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("NFC Info:").append('\n');
    sb.append("NFC ID:").append(getCardID()).append('\n');
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

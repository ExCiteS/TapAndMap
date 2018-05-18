package excites.ucl.ac.uk.tapmap.nfc;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michalis Vitos on 18/05/2018.
 */
public class NfcManagement {

  // -- Singleton
  private static NfcManagement ourInstance;

  public static NfcManagement getInstance() {

    if (ourInstance == null) ourInstance = new NfcManagement();
    return ourInstance;
  }

  // -- Rest of the code
  private Map<String, String> savedNfcCards;

  private NfcManagement() {
    savedNfcCards = new HashMap<>();
  }

  public void storeNfcCardID(String nfcCardID, String imagePath) {

    savedNfcCards.put(nfcCardID, imagePath);
  }

  public void storeNfcCard(NfcCard nfcCard, String imagePath) {

    savedNfcCards.put(nfcCard.getCardID(), imagePath);
  }

  public String getImagePath(NfcCard nfcCard) {

    return savedNfcCards.get(nfcCard.getCardID());
  }
}

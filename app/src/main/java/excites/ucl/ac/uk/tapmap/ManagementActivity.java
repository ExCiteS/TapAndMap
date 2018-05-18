package excites.ucl.ac.uk.tapmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;
import excites.ucl.ac.uk.tapmap.activities.ManageNfcCardsActivity;
import excites.ucl.ac.uk.tapmap.activities.TapAndMapActivity;
import timber.log.Timber;

public class ManagementActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_managment);
    ButterKnife.bind(this);

    Timber.d("Hi from the ManagementActivity");

    //openNFCActivity();
  }

  @OnClick(R.id.btn_collectData)
  public void openNFCActivity() {
    Timber.d("Go to NFC activity");

    Intent intent = new Intent(this, TapAndMapActivity.class);
    startActivity(intent);
  }

  @OnClick(R.id.btn_manageCards)
  public void openManageNfcCardsActivity() {
    Timber.d("Go to NFC activity");

    Intent intent = new Intent(this, ManageNfcCardsActivity.class);
    startActivity(intent);
  }
}

package uk.ac.ucl.excites.tapmap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import uk.ac.ucl.excites.tapmap.R;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    Timber.d("Hi from the MainActivity");
  }

  @OnClick(R.id.btn_collectData)
  public void openNFCActivity() {
    Timber.d("Go to NFC activity");

    Intent intent = new Intent(this, TapAndMapActivity.class);
    startActivity(intent);
  }

  @OnClick(R.id.btn_manageCards)
  public void openListActivity() {
    Timber.d("Go to List activity");

    Intent intent = new Intent(this, ListActivity.class);
    startActivity(intent);
  }
}

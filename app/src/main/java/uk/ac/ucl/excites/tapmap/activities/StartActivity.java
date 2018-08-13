package uk.ac.ucl.excites.tapmap.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.controllers.NavigationController;

import static uk.ac.ucl.excites.tapmap.controllers.NavigationController.Screens.START;

public class StartActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_start);
  }

  @Override
  protected void onResume() {
    super.onResume();
    final NavigationController navigationController = NavigationController.getInstance();
    navigationController.setCurrentScreen(START, null);
    navigationController.goToNextScreen(this);
  }
}

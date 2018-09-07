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

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.adapters.NfcCardItem;
import uk.ac.ucl.excites.tapmap.controllers.NavigationController;
import uk.ac.ucl.excites.tapmap.storage.ImageCard;
import uk.ac.ucl.excites.tapmap.storage.ImageCardDao;

/**
 * Created by Michalis Vitos on 24/05/2018.
 */
public class ListActivity extends AppCompatActivity {

  @BindView(R.id.nfcCardsRecyclerView)
  protected RecyclerView nfcCardsRecyclerView;

  @BindView(R.id.progressBar)
  protected ProgressBar progressBar;

  private ItemAdapter<NfcCardItem> itemAdapter;

  private ImageCardDao imageCardDao;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list);
    ButterKnife.bind(this);

    // Get database dao
    final TapMap app = (TapMap) getApplication();
    imageCardDao = app.getAppDatabase().imageCardDao();
  }

  @Override
  protected void onResume() {
    super.onResume();

    setRecyclerView();
  }

  private void setRecyclerView() {
    //create the ItemAdapter holding your Items
    itemAdapter = new ItemAdapter<>();
    //create the managing FastAdapter, by passing in the itemAdapter
    FastAdapter<NfcCardItem> fastAdapter = FastAdapter.with(itemAdapter);

    //set our adapters to the RecyclerView
    nfcCardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    nfcCardsRecyclerView.setItemAnimator(new DefaultItemAnimator());
    nfcCardsRecyclerView.setAdapter(fastAdapter);

    imageCardDao.getAll()
        .subscribeOn(Schedulers.io())
        .toObservable()
        .flatMap(Observable::fromIterable)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<ImageCard>() {
          @Override
          public void onSubscribe(Disposable d) {
            // Do nothing
          }

          @Override
          public void onNext(ImageCard imageCard) {
            itemAdapter.add(new NfcCardItem(imageCard));
          }

          @Override
          public void onError(Throwable e) {
            Toast.makeText(ListActivity.this, "Cannot load NFC Cards", Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onComplete() {
            progressBar.setVisibility(View.GONE);
          }
        });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.list_of_cards, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {

      case R.id.action_add_card:
        NavigationController.openManageNfcCardsActivity(this);
        return true;

      case R.id.action_clear_all:
        clearAllCards();
        return true;

      case R.id.action_load_project:
        NavigationController.openImportSettingsActivity(this);
        return true;

      case R.id.action_export_project:
        NavigationController.openExportSettingsActivity(this);
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void clearAllCards() {

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setTitle("Delete all cards")
        .setMessage(
            "This will delete all cards from the application. Are you sure you want to delete them?")
        .setPositiveButton("Delete", (dialog, which) -> {
          imageCardDao.deleteAll();
          itemAdapter.clear();
        })
        .setNegativeButton("Cancel", (dialog, which) -> { /* Do nothing */ })
        .show();
  }
}

package uk.ac.ucl.excites.tapmap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.TapMap;
import uk.ac.ucl.excites.tapmap.adapters.NfcCardItem;
import uk.ac.ucl.excites.tapmap.storage.NfcCard;
import uk.ac.ucl.excites.tapmap.storage.NfcCardDao;

/**
 * Created by Michalis Vitos on 24/05/2018.
 */
public class ListActivity extends AppCompatActivity {

  @BindView(R.id.nfcCardsRecyclerView)
  protected RecyclerView nfcCardsRecyclerView;

  @BindView(R.id.progressBar)
  protected ProgressBar progressBar;

  //save our FastAdapter
  private FastAdapter<NfcCardItem> fastAdapter;
  private ItemAdapter<NfcCardItem> itemAdapter;

  private NfcCardDao nfcCardDao;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list);
    ButterKnife.bind(this);

    // Get database dao
    final TapMap app = (TapMap) getApplication();
    nfcCardDao = app.getAppDatabase().nfcCardDao();
  }

  @Override
  protected void onResume() {
    super.onResume();

    setRecyclerView();
  }

  private void setRecyclerView() {
    //create the ItemAdapter holding your Items
    itemAdapter = new ItemAdapter();
    //create the managing FastAdapter, by passing in the itemAdapter
    fastAdapter = FastAdapter.with(itemAdapter);

    //set our adapters to the RecyclerView
    nfcCardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    nfcCardsRecyclerView.setItemAnimator(new DefaultItemAnimator());
    nfcCardsRecyclerView.setAdapter(fastAdapter);

    nfcCardDao.getAll()
        .subscribeOn(Schedulers.io())
        .toObservable()
        .flatMap(new Function<List<NfcCard>, ObservableSource<NfcCard>>() {
          @Override
          public ObservableSource<NfcCard> apply(List<NfcCard> nfcCards) {
            return Observable.fromIterable(nfcCards);
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<NfcCard>() {
          @Override
          public void onSubscribe(Disposable d) {
            // Do nothing
          }

          @Override
          public void onNext(NfcCard nfcCard) {
            itemAdapter.add(new NfcCardItem(nfcCard));
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

  @OnClick(R.id.fabAddCard)
  protected void onAddCardClicked() {
    Intent intent = new Intent(this, ManageNfcCardsActivity.class);
    startActivity(intent);
  }
}

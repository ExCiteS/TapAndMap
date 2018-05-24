package uk.ac.ucl.excites.tapmap.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.adapters.NfcCardItem;

/**
 * Created by Michalis Vitos on 24/05/2018.
 */
public class ListActivity extends AppCompatActivity {

  @BindView(R.id.nfcCardsRecyclerView)
  protected RecyclerView nfcCardsRecyclerView;

  //save our FastAdapter
  private FastAdapter<NfcCardItem> fastAdapter;
  private ItemAdapter<NfcCardItem> itemAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list);
    ButterKnife.bind(this);

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

    //set the items to your ItemAdapter
    itemAdapter.add(new NfcCardItem("Test"));
    itemAdapter.add(new NfcCardItem("Test"));
    itemAdapter.add(new NfcCardItem("Test"));
  }
}

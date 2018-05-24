package uk.ac.ucl.excites.tapmap.adapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import java.util.List;
import lombok.AllArgsConstructor;
import uk.ac.ucl.excites.tapmap.R;

/**
 * Created by Michalis Vitos on 24/05/2018.
 */
@AllArgsConstructor
public class NfcCardItem extends AbstractItem<NfcCardItem, NfcCardItem.ViewHolder> {

  public String id;

  @Override
  public int getType() {
    return R.id.fastadapter_nfc_card_id;
  }

  @Override
  public int getLayoutRes() {
    return R.layout.nfc_card_item;
  }

  @NonNull
  @Override
  public ViewHolder getViewHolder(@NonNull View view) {
    return new ViewHolder(view);
  }

  public class ViewHolder extends FastAdapter.ViewHolder<NfcCardItem> {

    @BindView(R.id.id)
    TextView id;
    @BindView(R.id.description)
    TextView description;

    ViewHolder(View view) {
      super(view);
      ButterKnife.bind(this, view);
    }

    @Override
    public void bindView(NfcCardItem item, List<Object> payloads) {
      // TODO: 24/05/2018
      id.setText(item.id);
    }

    @Override
    public void unbindView(NfcCardItem item) {
      // TODO: 24/05/2018
      id.setText(null);
    }
  }
}

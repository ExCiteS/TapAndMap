package uk.ac.ucl.excites.tapmap.adapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.List;
import lombok.AllArgsConstructor;
import uk.ac.ucl.excites.tapmap.R;
import uk.ac.ucl.excites.tapmap.storage.NfcCard;
import uk.ac.ucl.excites.tapmap.utils.ScreenMetrics;

/**
 * Created by Michalis Vitos on 24/05/2018.
 */
@AllArgsConstructor
public class NfcCardItem extends AbstractItem<NfcCardItem, NfcCardItem.ViewHolder> {

  public static final int MAX_SIZE = 32;

  public NfcCard card;

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

    @BindView(R.id.card_image)
    ImageView nfcImageView;
    @BindView(R.id.id)
    TextView id;
    @BindView(R.id.description)
    TextView description;

    ViewHolder(View view) {
      super(view);
      ButterKnife.bind(this, view);
    }

    @Override
    public void bindView(@NonNull NfcCardItem item, @NonNull List<Object> payloads) {

      id.setText(item.card.getId());

      final int size = (int) ScreenMetrics.convertDpToPixel(MAX_SIZE);
      // Show NFC card
      Picasso.get()
          .load(new File(item.card.getImagePath()))
          .placeholder(R.drawable.progress_animation)
          .error(R.drawable.ic_error)
          .resize(size, size)
          .centerCrop()
          .into(nfcImageView);
    }

    @Override
    public void unbindView(@NonNull NfcCardItem item) {
      // TODO: 24/05/2018
      id.setText(null);
    }
  }
}

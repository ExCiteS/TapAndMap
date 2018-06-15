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
    @BindView(R.id.card_tag)
    TextView tag;
    @BindView(R.id.card_id)
    TextView id;

    ViewHolder(View view) {
      super(view);
      ButterKnife.bind(this, view);
    }

    @Override
    public void bindView(@NonNull NfcCardItem item, @NonNull List<Object> payloads) {

      tag.setText(item.card.getTag());
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
      tag.setText(null);
      id.setText(null);
      nfcImageView.setImageDrawable(null);
    }
  }
}

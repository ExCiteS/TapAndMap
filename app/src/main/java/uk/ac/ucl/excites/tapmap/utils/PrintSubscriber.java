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

package uk.ac.ucl.excites.tapmap.utils;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Created by Michalis Vitos on 14/06/2018.
 */
public class PrintSubscriber implements Observer {

  private final String name;

  public PrintSubscriber() {
    this.name = "DEBUG";
  }

  @Override
  public void onSubscribe(Disposable d) {
    Timber.d("%s : onSubscribe : %s", name, d);
  }

  public PrintSubscriber(String name) {
    this.name = name;
  }

  @Override
  public void onComplete() {
    Timber.d("%s : Completed", name);
  }

  @Override
  public void onError(Throwable e) {
    Timber.d("%s : Error : %s", name, e.getMessage());
  }

  @Override
  public void onNext(Object v) {
    Timber.d("%s : %s", name, v);
  }
}
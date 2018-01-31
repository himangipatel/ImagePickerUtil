package com.filepicker;

import android.app.Application;
import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by HIMANGI--- Patel on 31/1/18.
 */

public class App extends Application {

  @Override public void onCreate() {
    super.onCreate();
    Fresco.initialize(this);
  }
}

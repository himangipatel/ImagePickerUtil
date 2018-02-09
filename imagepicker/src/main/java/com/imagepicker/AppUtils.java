package com.imagepicker;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import java.io.File;
import java.io.IOException;

/**
 * Created by HIMANGI--- Patel on 31/1/18.
 */

public class AppUtils {

  static File getWorkingDirectory(Activity activity) {
    File directory =
        new File(Environment.getExternalStorageDirectory(), activity.getApplicationContext().getPackageName());
    if (!directory.exists()) {
      directory.mkdir();
    }
    return directory;
  }

  static FileUri createImageFile(Activity activity, String prefix) {
    FileUri fileUri = new FileUri();

    File image = null;
    try {
      image = File.createTempFile(prefix + String.valueOf(System.currentTimeMillis()), ".jpg",
          getWorkingDirectory(activity));
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (image != null) {
      fileUri.setFile(image);
      //
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        fileUri.setImageUrl(FileProvider.getUriForFile(activity,
            activity.getApplicationContext().getPackageName(), image));
      } else {
        fileUri.setImageUrl(Uri.parse("file:" + image.getAbsolutePath()));
      }
    }
    return fileUri;
  }
}

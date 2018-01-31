package com.imagepicker;

import android.content.Context;
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
  static File getWorkingDirectory() {
    File directory =
        new File(Environment.getExternalStorageDirectory(), BuildConfig.APPLICATION_ID);
    if (!directory.exists()) {
      directory.mkdir();
    }
    return directory;
  }

  static FileUri createImageFile(Context context, String prefix) {
    FileUri fileUri = new FileUri();

    File image = null;
    try {
      image = File.createTempFile(prefix + String.valueOf(System.currentTimeMillis()), ".jpg",
          getWorkingDirectory());
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (image != null) {
      fileUri.setFile(image);
      //
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        fileUri.setImageUrl(FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, image));
      } else {
        fileUri.setImageUrl(Uri.parse("file:" + image.getAbsolutePath()));
      }
    }
    return fileUri;
  }
}

package com.filepicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.imagepicker.FilePickUtils;
import com.imagepicker.LifeCycleCallBackManager;
import java.io.File;

import static com.imagepicker.FilePickUtils.CAMERA_PERMISSION;
import static com.imagepicker.FilePickUtils.STORAGE_PERMISSION_IMAGE;

public class MainActivity extends AppCompatActivity {

  private SimpleDraweeView ivImage;

  private FilePickUtils filePickUtils;
  private BottomDialog bottomDialog;
  private LifeCycleCallBackManager lifeCycleCallBackManager;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ivImage = findViewById(R.id.ivImage);
    showImagePickerDialog(onFileChoose);
  }

  private FilePickUtils.OnFileChoose onFileChoose = new FilePickUtils.OnFileChoose() {
    @Override public void onFileChoose(String fileUri, int requestCode) {
      bottomDialog.dismiss();
      ivImage.setImageURI(Uri.fromFile(new File(fileUri)));
    }
  };

  public void showImagePickerDialog(FilePickUtils.OnFileChoose onFileChoose) {
    filePickUtils = new FilePickUtils(this, onFileChoose);
    lifeCycleCallBackManager = filePickUtils.getCallBackManager();
    View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_photo_selector, null);
    bottomDialog = new BottomDialog(MainActivity.this);
    bottomDialog.setContentView(bottomSheetView);
    final TextView tvCamera = bottomSheetView.findViewById(R.id.tvCamera);
    final TextView tvGallery = bottomSheetView.findViewById(R.id.tvGallery);
    tvCamera.setOnClickListener(onCameraListener);
    tvGallery.setOnClickListener(onGalleryListener);
    bottomDialog.show();
  }
  private View.OnClickListener onCameraListener = new View.OnClickListener() {
    @Override public void onClick(View view) {
      filePickUtils.requestImageCamera(CAMERA_PERMISSION, true, true);
    }
  };

  private View.OnClickListener onGalleryListener = new View.OnClickListener() {
    @Override public void onClick(View view) {
      filePickUtils.requestImageGallery(STORAGE_PERMISSION_IMAGE, true, true);
    }
  };

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (lifeCycleCallBackManager != null) {
      lifeCycleCallBackManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (lifeCycleCallBackManager != null) {
      lifeCycleCallBackManager.onActivityResult(requestCode, resultCode, data);
    }
  }


}

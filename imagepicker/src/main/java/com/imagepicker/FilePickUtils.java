package com.imagepicker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * Created by krupal on 10/10/16.
 */

public class FilePickUtils implements LifeCycleCallBackManager {

    private static final int CAMERA_PICTURE = 10;
    private static final int GALLERY_PICTURE = 11;
    public static final int STORAGE_PERMISSION_IMAGE = 111;
    private static final int STORAGE_PERMISSION_CAMERA = 112;
    public static final int CAMERA_PERMISSION = 115;
    private static final int CAMERA_BUT_STORAGE_PERMISSION = 116;
    private static final int SETTING_SCREEN_FOR_PERMISSION = 117;
    private OnFileChoose mOnFileChoose;
    private Uri imageUrl;
    private int requestCode;
    private Activity activity;
    private Fragment fragment;
    private boolean allowCrop;
    private boolean allowDelete;
    private float MAX_HEIGHT = 616.0f;
    private float MAX_WIDTH = 816.0f;

    private List<String> fileUrls = new ArrayList<>();
    private boolean isFixedRatio;

    public FilePickUtils(Activity activity, OnFileChoose mOnFileChoose) {
        super();
        this.activity = activity;
        this.mOnFileChoose = mOnFileChoose;
    }

    public FilePickUtils(Fragment fragment, OnFileChoose mOnFileChoose) {
        super();
        this.fragment = fragment;
        this.activity = fragment.getActivity();
        this.mOnFileChoose = mOnFileChoose;
    }

    public LifeCycleCallBackManager getCallBackManager() {
        return this;
    }

    public void requestImageGallery(int requestCode, boolean allowCrop, boolean isFixedRatio) {
        this.requestCode = requestCode;
        this.allowCrop = allowCrop;
        this.isFixedRatio = isFixedRatio;
        boolean hasStoragePermission = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasStoragePermission) {
            selectImageFromGallery();
        } else {
            requestPermissionForExternalStorage();
        }
    }

    public void selectImageFromGallery() {
        Intent pictureActionIntent =
                new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pictureActionIntent, GALLERY_PICTURE);
    }

    public void requestImageCamera(int requestCode, boolean allowCrop, boolean isFixedRatio) {
        this.requestCode = requestCode;
        this.allowCrop = allowCrop;
        this.isFixedRatio = isFixedRatio;
        boolean hasCameraPermission = checkPermission(Manifest.permission.CAMERA);
        boolean hasStoragePermission = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (hasCameraPermission && hasStoragePermission) {
            selectImageFromCamera();
        } else if (!hasCameraPermission && !hasStoragePermission) {
            requestPermissionForCameraStorage();
        } else if (!hasCameraPermission) {
            requestPermissionForCamera();
        } else {
            requestPermissionForCameraButStorage();
        }
    }


    public void setMaxHeight(float maxHeight) {
        MAX_HEIGHT = maxHeight;
    }

    public void setMaxWidth(float maxWidth) {
        MAX_WIDTH = maxWidth;
    }

    public void selectImageFromCamera() {
        File photoFile = null;

        FileUri fileUri = null;
        if (activity != null) {
            fileUri = AppUtils.createImageFile(activity, "CAMERA");
        }
        if (fileUri == null) {
            return;
        }
        photoFile = fileUri.getFile();
        imageUrl = fileUri.getImageUrl();

        if (photoFile != null) {
            /*Uri photoURI = FileProvider.getUriForFile(activity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile);*/
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrl);//imageUrl
            intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_PICTURE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionForCamera() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        requestPermissionWithRationale(permissions, CAMERA_PERMISSION, "Camera");
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionForCameraButStorage() {
        final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        requestPermissionWithRationale(permissions, CAMERA_BUT_STORAGE_PERMISSION, "Storage");
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionForExternalStorage() {
        final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        requestPermissionWithRationale(permissions, STORAGE_PERMISSION_IMAGE, "Storage");
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionForCameraStorage() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        requestPermissionWithRationale(permissions, STORAGE_PERMISSION_CAMERA, "Camera & Storage");
    }

    //Activity and Fragment Base Methods
    private void startActivityForResult(Intent intent, int requestCode) {
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else if (activity != null) {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private boolean checkPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ActivityCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissionWithRationale(final String permissions[], final int requestCode,
                                                String rationaleDialogText) {
        boolean showRationale = false;
        for (String permission : permissions) {
            if (activity.shouldShowRequestPermissionRationale(permission)) {
                showRationale = true;
            }
        }

        if (showRationale) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(activity).setPositiveButton("AGREE",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    requestPermissions(permissions, requestCode);
                                }
                            })
                            .setNegativeButton("DENY", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setMessage("Allow "
                                    + activity.getString(R.string.app_name)
                                    + " to access "
                                    + rationaleDialogText
                                    + "?");
            builder.create().show();
        } else {
            requestPermissions(permissions, requestCode);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions(String[] permissions, int requestCode) {
        if (fragment != null) {
            fragment.requestPermissions(permissions, requestCode);
        } else if (activity != null) {
            activity.requestPermissions(permissions, requestCode);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_IMAGE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImageFromGallery();
        } else if (requestCode == STORAGE_PERMISSION_CAMERA
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            selectImageFromCamera();
        } else if (requestCode == CAMERA_PERMISSION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImageFromCamera();
        } else if (requestCode == CAMERA_BUT_STORAGE_PERMISSION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImageFromCamera();
        } else {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean showRationale = activity.shouldShowRequestPermissionRationale(permission);
                    if (Manifest.permission.CAMERA.equals(permission)) {
                        if (!showRationale) {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(activity).setPositiveButton("GO TO SETTING",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                                    intent.setData(uri);
                                                    startActivityForResult(intent, 121);
                                                }
                                            })
                                            .setNegativeButton("DENY", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .setTitle("Permission denied")
                                            .setMessage("Without camera permission the app is unable to capture photos from camera. Are you sure want to deny this permission?");
                            builder.create().show();
                        } else
                            activity.shouldShowRequestPermissionRationale(permission);
                    } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)
                            || Manifest.permission.READ_EXTERNAL_STORAGE.equals(permission)) {
                        if (!showRationale) {
                            showAlertDialog("Without storage permission the app is unable to open gallery or to save photos. Are you sure want to deny this permission?");
                        } else
                            activity.shouldShowRequestPermissionRationale(permission);
                    }
                }
            }
        }
    }

    private void showAlertDialog(String message) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(activity).setPositiveButton("GO TO SETTING",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, SETTING_SCREEN_FOR_PERMISSION);
                            }
                        })
                        .setNegativeButton("DENY", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setTitle("Permission denied")
                        .setMessage(message);
        builder.create().show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        boolean hasStoragePermission =
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && checkPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE);
        if (resultCode == Activity.RESULT_OK && hasStoragePermission) {
            Uri uri;
            switch (requestCode) {
                case GALLERY_PICTURE:
                    if (allowCrop) {
                        performCrop(data.getData());
                    } else {
                        /*onFileChoose(data.getData().toString());*/
                        performImageProcessing(data.getData().toString(),
                                FileType.IMG_FILE);
                    }
                    break;
                case CAMERA_PICTURE:
                    uri = imageUrl;
                    if (allowCrop) {
                        performCrop(uri);
                    } else {
                        /*onFileChoose(uri.getPath());*/

                        performImageProcessing(uri.toString(),
                                FileType.IMG_FILE);
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri resultUri = result.getUri();
                    performImageProcessing(resultUri.toString(),
                            FileType.IMG_FILE);
                    break;
            }
        }
    }

    public void onDestroy() {
        activity = null;
        fragment = null;
        mOnFileChoose = null;
        //To delete Files on exit
        if (fileUrls != null && !fileUrls.isEmpty() && allowDelete) {
            for (String fileUrl : fileUrls) {
                File file = new File(fileUrl);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    @Override
    public void onStartActivity() {

    }

    //This method is for compress image
    private void performImageProcessing(final String imageUrl,
                                        final FileType type) {
        Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                return Observable.just(compressImage(imageUrl));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        onFileChoose(s);
                    }
                });
    }

    private String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        //		by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        //		you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

       /* if (bmp == null) {
            bmp = BitmapFactory.decodeFile(AppUtils.getWorkingDirectory() + "/" + "CROP" + imageUri.split("CROP")[imageUri.split("CROP").length - 1]);
        }
*/
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        //		max Height and width values of the compressed image is taken as 816x612

        float maxHeight = MAX_HEIGHT;
        float maxWidth = MAX_WIDTH;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;


        //		width and height values are set maintaining the aspect ratio of the image
        Log.d("IMAGE", "actualHeight=" + actualHeight + "actualWidth=" + actualWidth + "");
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

        //		setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

        //		inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

        //		this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            //			load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2,
                new Paint(Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = AppUtils.createImageFile(activity, "").getFile().getAbsolutePath();
        try {
            out = new FileOutputStream(filename);

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        File file = new File(filePath);
//        file.delete();

        return filename;
    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = activity.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            if (index > 0) {
                return cursor.getString(index);
            }
            return AppUtils.getWorkingDirectory(activity) + "/" + cursor.getString(0);
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    private void onFileChoose(String uri) {
        if (mOnFileChoose != null) {
            mOnFileChoose.onFileChoose(uri, requestCode);
        }
    }

    private void performCrop(Uri uri) {
        FileUri cropFile = AppUtils.createImageFile(activity, "CROP");
        if (fragment != null) {
            CropImage.activity(uri).setOutputUri(cropFile.getImageUrl())
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setFixAspectRatio(true)
                    .start(activity, fragment);
        } else {
            CropImage.activity(uri).setOutputUri(cropFile.getImageUrl())
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setFixAspectRatio(true)
                    .start(activity);
        }
    }

    private enum FileType {
        IMG_FILE,
    }

    public interface OnFileChoose {
        void onFileChoose(String fileUri, int requestCode);
    }
}

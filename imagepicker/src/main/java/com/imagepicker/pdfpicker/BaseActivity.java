package com.imagepicker.pdfpicker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Vincent Woo
 * Date: 2016/10/12
 * Time: 16:21
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final int RC_READ_EXTERNAL_STORAGE = 123;
    private static final String TAG = BaseActivity.class.getName();

    protected FolderListHelper mFolderHelper;
    protected boolean isNeedFolderList;
    public static final String IS_NEED_FOLDER_LIST = "isNeedFolderList";

//    abstract void permissionGranted();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isNeedFolderList = getIntent().getBooleanExtra(IS_NEED_FOLDER_LIST, false);
        if (isNeedFolderList) {
            mFolderHelper = new FolderListHelper();
            mFolderHelper.initFolderListView(this);
        }
//        permissionGranted();
    }

    public void onBackClick(View view) {
        finish();
    }
}

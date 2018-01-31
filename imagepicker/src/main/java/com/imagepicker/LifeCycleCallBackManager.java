package com.imagepicker;

import android.content.Intent;
import android.support.annotation.NonNull;

public interface LifeCycleCallBackManager {

    public void onActivityResult(int requestCode, int resultCode, Intent data);

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults);

    public void onDestroy();

    public void onStartActivity();

}
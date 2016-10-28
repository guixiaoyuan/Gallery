package com.tct.gallery3d.app;

import android.content.Intent;

import com.tct.gallery3d.data.Log;
import com.tct.gallery3d.util.PermissionUtil;

public class MoviePermissionActivity extends PermissionActivity {
    private static final String TAG = "MoviePermissionActivity";

    protected void initializeByIntent() {
        Intent intent = getIntent();
        Log.d(TAG, "intent=" + intent);

        intent.setClassName(getApplicationContext(),
                PermissionUtil.com_tct_gallery3d_app_MovieActivity);
        startActivity(intent);
        finish();
    }
}

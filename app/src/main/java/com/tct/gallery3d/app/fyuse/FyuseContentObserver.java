/* ----------|----------------------|----------------------|----------------- */
/* 01/20/2016| jian.pan1            |[ALM]Defect:1271879   |[Android 6.0][Gallery]Fyuse doesn't update in time
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.app.fyuse;

import android.database.ContentObserver;
import android.os.Handler;

import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.ui.Log;

public class FyuseContentObserver extends ContentObserver {

    private static final String TAG = "FyuseContentObserver";
    private AbstractGalleryActivity mActivity;

    public FyuseContentObserver(Handler handler,
            AbstractGalleryActivity activity) {
        super(handler);
        this.mActivity = activity;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.i(TAG, "FyuseContentObserver onChange() selfChange = " + selfChange);
        mActivity.getDataManager().updateFyuseView();
    }

}

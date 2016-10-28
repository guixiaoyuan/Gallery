package com.tct.gallery3d.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Gallery;

public class BurstShotView extends Gallery {

    private static final String TAG = "BurstShotView";

    public BurstShotView(Context context) {
        this(context, null);
    }

    public BurstShotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("deprecation")
    public BurstShotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        return false;
    }
}

package com.tct.gallery3d.fastjumper;

import android.util.DisplayMetrics;

public class ScrollerHelper {

    private static final float MILLISECONDS_PER_INCH = 500f;

    public static float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
    }
}

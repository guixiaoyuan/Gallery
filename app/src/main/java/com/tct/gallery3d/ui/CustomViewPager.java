/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.gallery3d.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {

    private boolean mEnabled = true;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPagingEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public boolean getPagingEnabled() {
        return mEnabled;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (mEnabled) {
            super.scrollTo(x, y);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnabled) return false;
        int pointCount = event.getPointerCount();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_POINTER_DOWN:
        case MotionEvent.ACTION_MOVE:
            if (pointCount > 1) {
                return false;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mEnabled) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }
}

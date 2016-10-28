/******************************************************************************/
/*                                                               Date:10/2013 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  Fan.Hu                                                          */
/*  Email  :   fan.hu@tct.com                                                 */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :                                                                */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 12/17/2013|        Fan.Hu        |                      |Creation          */
/* ----------|----------------------|----------------------|----------------- */
/* 03/04/2014|      ping.wang       |      PR-612788       |zoom issue        */
/* ----------|----------------------|----------------------|----------------- */
/* 11/03/2015|    jialiang.ren     |      PR-945190       |[Android5.0][Gallery_v5.1.9.1.0102.0][Monitor]          */
/*                                                         [Force Close]Gallery force close when switching language*/
/* ----------|---------------------|----------------------|------------------------------------------------------- */
/* 21/04/2015|    jialiang.ren     |      PR-981911       |[Android5.0][Gallery_v5.1.9.1.0113.0]Dubble clicking */
/*                                                         to zoom in and zoom out image is not smooth enough   */
/* ----------|---------------------|----------------------|-----------------------------------------------------*/
/* 05/04/2014|     chengbin.du     |      PR-986925       | [Pixi3-5 4G][Gallery_v5.1.9.1.0204.0][one finger zoom][SW]one finger zoom not smooth */
/* ----------|---------------------|----------------------|----------------- */

package com.android.gallery3d.gesture;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class TOneFingerZoomer {
    public static final String TAG = "TOneFingerZoomer";
    public static int VERSION = 3;
    private final int TOUCH_SLOP;

    private Context mContext = null;
    private boolean mIsZooming = false;
    private float mOneTimeDistance = 400;
    private OnZoomerListener mListener = null;
    private float mLastY = 0;
    private float mFactor = 1.0f;
    private float mFocusedX = -1.0f;
    private float mFocusedY = -1.0f;
    private float mFocusedRawX = -1.0f;
    private float mFocusedRawY = -1.0f;
    private long mBeginTimeMillis = -1;
    private boolean mDoubleTapDetected = false;

    public interface OnZoomerListener {
        /**
         * Begin zooming
         * @param e touch event when zooming is started
         */
        void onZoomBegin(MotionEvent e);

        /**
         * End zooming
         */
        void onZoomEnd();

        /**
         * Zooming value changed, based on last zoom.
         * @param zoom zoom factor, based on last call of onZoom(). If you want
         *            to get the factor since from onZoomBegin(), please refer
         *            to {@link TOneFingerZoomer#getFactor()}
         */
        void onZoom(float zoom);
    }

    private GestureDetector mGestureDetector = null;
    private GestureDetector.SimpleOnGestureListener mGestureDetectorListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mDoubleTapDetected = true;
            mIsZooming = false;
            mLastY = e.getRawY();
            mFactor = 1.0f;
            mFocusedX = e.getX();
            mFocusedY = e.getY();
            mFocusedRawX = e.getRawX();
            mFocusedRawY = e.getRawY();
            mBeginTimeMillis = System.currentTimeMillis();
            if (mListener != null) {
                mListener.onZoomBegin(e);
            }
            return false; //[BUGFIX]-MOD by ping.wang,3/4/14,PR-612788
        }
    };

    /**
     * Constructor Method
     * @param context context
     * @param listener {@link OnZoomerListener}
     */
    public TOneFingerZoomer(Context context, OnZoomerListener listener) {
        mContext = context;
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-11,PR945190
        //initPage();
        TOUCH_SLOP = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mGestureDetector = new GestureDetector(mContext, mGestureDetectorListener);
        mGestureDetector.setOnDoubleTapListener(mGestureDetectorListener);
        mOneTimeDistance = (float) (context.getResources().getDisplayMetrics().heightPixels) / 2.0f;

        mListener = listener;
    }

    /**
     * Check whether zooming is in progress
     * @return true: zooming. false: otherwise
     */
    public boolean isZooming() {
        return mIsZooming;
    }

    public boolean isDoubleTapDetected() {
        return mDoubleTapDetected;
    }
    /**
     * Get the factor since onZoomBegin() is called.
     * @return factor of zooming since last onZoomBegin() is called
     */
    public float getFactor() {
        return mFactor;
    }

    /**
     * Get focused x, which is the x position of user double tapped
     * @return -1: not focused
     */
    public float getFocusedX() {
        return mFocusedX;
    }

    /**
     * Get focused y, which is the y position of user double tapped
     * @return -1: not focused
     */
    public float getFocusedY() {
        return mFocusedY;
    }

    /**
     * Get the time when zoom begin.
     * @return time in ms
     */
    public long getZoomBeginTimeMillis() {
        return mBeginTimeMillis;
    }

    /**
     * Process the touch event
     * @param ev touch event
     * @return true: event processed. false: otherwise
     */
    public boolean onTouchEvent(MotionEvent ev) {
        boolean isZoomingUp = false;
        if (mGestureDetector.onTouchEvent(ev)) {
            // double tap detected. unnecessary to do others
            return mIsZooming;
        }

        if (mDoubleTapDetected) {
            switch (ev.getAction()) {
                //[BUGFIX]-MOD-begin by ping.wang,3/4/14,PR-612788
                case MotionEvent.ACTION_UP:
                    isZoomingUp = true;
                    if(mDoubleTapDetected) {
                        if (mListener != null) {
                            mListener.onZoomEnd();
                        }
                        mFocusedX = -1.0f;
                        mFocusedY = -1.0f;
                        mBeginTimeMillis = -1;
                        mIsZooming = false;
                        mDoubleTapDetected = false;
                    }
                case MotionEvent.ACTION_DOWN:
                    mFocusedX = ev.getX();
                    mFocusedY = ev.getY();
                    mLastY = ev.getRawY();
                    break;
                    //[BUGFIX]-MOD-end by ping.wang
                case MotionEvent.ACTION_MOVE:
                    // check whether enter zooming mode
                    if (!mIsZooming) {
                        float distanceX = ev.getRawX() - mFocusedRawX;
                        float distanceY = ev.getRawY() - mFocusedRawY;
                        float distance = (float)Math.sqrt(distanceX*distanceX + distanceY*distanceY);
                        System.out.println("distance start" + mIsZooming);
                        //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-04,PR986925 Begin
                        if (distance > TOUCH_SLOP * 3) {
                            mIsZooming = true;
                        }
                        //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-04,PR986925 End
                    }
                    // do zooming
                    if (mIsZooming) {
                        float deltaY = ev.getRawY() - mLastY;
                        float zoomRate = 1 + (deltaY / mOneTimeDistance);
                        mFactor *= zoomRate;
                        if (mListener != null) {
                            mListener.onZoom(zoomRate);
                        }
                        mLastY = ev.getRawY();
                    }
                    break;
            }
        }

        return mIsZooming || isZoomingUp;
    }
}

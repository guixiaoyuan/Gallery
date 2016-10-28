/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.tcl.com
 * PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* 20/11/2014|chengqun.sun            |FR826631              |Multi screen interaction*/  
            /* ----------|----------------------|----------------------|------------------*/
package com.android.gallery3d.app;

import android.content.Context;
import android.util.Log;

public class DLNAStatusController{
    private static final String TAG = "DLNAStatusController";
    private static DLNAManager mDLNAManager = null;
    private Context mContext;
    private PlayStateListener mPlayStateListener = null;
    private ProgressListener mProgressListener = null;
    private String mIdentification = null;
    private PositionAndStatusSyncThread mSyncThread;
    private long mLastPlayingPosition = 0;
    private long mCurrentPlayingPosition = 0;
    private long mDuration = 0;
    private boolean isStartingPlay = false;
    private int mErrorStatusCount = 0;
    private int mTvLastStatus = TV_STATUS_OK;
    private volatile boolean isSeeking = false;
    private volatile boolean isTvInSeeking = false;

    private int mTvStatus = -1;

    private volatile int mPhoneStatus = STATUS_BLANK;
    
    public static final int STATUS_PREPARING_PLAY = 0;
    public static final int STATUS_PLAYING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_STOPED = 3;
    public static final int STATUS_ERROR = 100;
    public static final int STATUS_BLANK = 4; // original status
    
    public static final int TV_STATUS_OK = 0;
    public static final int TV_STATE_PLAYING = 1;
    public static final int TV_STATE_STOPPED = 2;
    public static final int TV_STATE_TRANSITIONING = 3;
    public static final int TV_STATE_PAUSED_PLAYBACK = 4;

    private static DLNAStatusController mDLNAStatusController = null;

    private DLNAStatusController(Context context){
        this.mContext = context;
        mDLNAManager = DLNAManager.getInstance(context);
        Log.d(TAG, "mDLNAManager: " + mDLNAManager);
    }

    public static DLNAStatusController getInstance(Context context) {
        if (mDLNAStatusController == null) {
            mDLNAStatusController = new DLNAStatusController(context);
        }
        return mDLNAStatusController;
    }

    public void SetDlnaListener(String identification,PlayStateListener listener, ProgressListener progressListener){
        //TODO
        mIdentification = identification;
        mPlayStateListener = listener;
        mProgressListener = progressListener;
        Log.d(TAG, 
                "mIdentification: "+identification+"; StateListener:"
                        +listener+"; progressListener:"+progressListener);
        
        if ((null == listener) && (null == progressListener)){
            //cancel listener
            if (mSyncThread != null) {
                mSyncThread.exitThread();
                mSyncThread = null;
            }
        }else{
            //set new listener
            if (mSyncThread != null) {
                mSyncThread.exitThread();
                mSyncThread = null;
            }
            
            mSyncThread = new PositionAndStatusSyncThread();
            mSyncThread.start();
        }
        
        
    }

    private class PositionAndStatusSyncThread extends Thread {
        private volatile boolean mIsActive = true;
        public static final int TIME_SLICE = 500;
        private static final String TagThread = TAG;
        private int tvLastStatus = 2;

        @SuppressWarnings("static-access")
        public void run() {
            // sync mPosition isPlaying
            while (mIsActive) {
                try {
                    Thread.currentThread().sleep(TIME_SLICE);
                    if (!mDLNAManager.hasConnected()) {
                        continue;
                    }

                    long t1 = System.currentTimeMillis();
                    mTvStatus = mDLNAManager.mediaControlGetPlayState(mIdentification);
                    long t2 = System.currentTimeMillis();
                    Log.d(TAG, ">>>TV now Status:" + getTvStatusString(mTvStatus)
                            + ",fetch TV status cost time:" + (t2 - t1));

                    long t3 = System.currentTimeMillis();
                    mCurrentPlayingPosition = mDLNAManager.mediaControlGetCurPlayPosition(mIdentification);
                    long t4 = System.currentTimeMillis();
                    Log.d(TAG, ">>>>TV now Position:" + mCurrentPlayingPosition
                            + ",fetch TV position cost time:" + (t4 - t3) + "----Thread:"
                            + Thread.currentThread().getName());

                    mDuration = mDLNAManager.mediaControlGetMediaDuration(mIdentification);

                    if(mTvStatus != mTvLastStatus){
                        Log.d(TAG, ">>>TV Status now changed from: "+mTvLastStatus+" to: " + mTvStatus);
                        mTvLastStatus = mTvStatus;
                        //call back
                        if(mPlayStateListener != null){
                            mPlayStateListener.onPlayStateChanged(mTvStatus);
                        }
                    }

                    if (mLastPlayingPosition > 0 && mCurrentPlayingPosition == 0) { 
                        // unexcepted
                        // position
                        // 0,ignore
                        // it
                        Log.d(TAG, ">>>> now TV Status:"
                                + getTvStatusString(mTvStatus)
                                + ",unexceped position 0,ignore");
                        mLastPlayingPosition = mCurrentPlayingPosition;
                        continue;
                    }
                    
                    if(mCurrentPlayingPosition != mLastPlayingPosition){
                        Log.d(TAG, ">>>TV Position now changed from: "+mLastPlayingPosition+" to: " + mCurrentPlayingPosition);
                        mLastPlayingPosition = mCurrentPlayingPosition;
                        //call back
                        if(mProgressListener != null){
                            mProgressListener.onProgressChanged(mCurrentPlayingPosition, mDuration);
                        }
}
                    // Once tv status appears nomal,reset the
                    // mErrorStatusCount,we should only focus on continued Error
                    // Status
                    if (!hasErrorHappen(mTvStatus)) {
                        mErrorStatusCount = 0;
                    }else {
                            Log.d(TAG,
                                    ">>>>TV Status has Some Error: "+ getTvStatusString(mTvStatus));
                            // ignore device offline suddenly,we should focus on
                            // continued error status
                            if (mErrorStatusCount < 4) {
                                mErrorStatusCount++;
                            } else {
                                this.exitThread();
                                mPhoneStatus = STATUS_ERROR;
                            }
                        continue;
                    }
                    // --------------------------------------------------------------------------
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e(TagThread, "sleep error !");
                }
            }
        }

        private boolean hasErrorHappen(int nowStatus) {
            if (nowStatus < 0 || nowStatus > 4) {
                return true;
            }
            return false;
        }

        public void exitThread() {
            Log.d(TagThread, ">> ...... exit Thread");
            mIsActive = false;
        }

        public boolean isInAlive() {
            return mIsActive;
        }
    }
    
    private String getTvStatusString(int status) {
        switch (status) {
            case TV_STATE_PAUSED_PLAYBACK:
                return "TV_STATE_PAUSED_PLAYBACK";
            case TV_STATE_PLAYING:
                return "TV_STATE_PLAYING";
            case TV_STATE_STOPPED:
                return "TV_STATE_STOPPED";
            case TV_STATE_TRANSITIONING:
                return "TV_STATE_TRANSITIONING";
            case TV_STATUS_OK:
                return "TV_STATUS_OK";
            default:
                return "OTHERS:" + status;
        }
    }
    
    
    
}
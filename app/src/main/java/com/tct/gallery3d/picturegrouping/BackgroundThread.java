package com.tct.gallery3d.picturegrouping;

import android.os.HandlerThread;
import android.util.Log;


public class BackgroundThread extends HandlerThread {
    private static BackgroundThread sInstance;
    
    static BackgroundThread getInstance(){
        if (sInstance == null){
            sInstance = new BackgroundThread();
        }
        return sInstance;
    }
    
    private BackgroundThread(){
        super("GroupLoader");
        Log.i(PictureGrouping.TAG, "new BackgroundThread()");
        start();
    }
}

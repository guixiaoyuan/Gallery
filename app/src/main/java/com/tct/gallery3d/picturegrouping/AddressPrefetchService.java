/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 11/17/2015|chengbin.du-nb        |ALM-913700            |[DRM] Gallery force closed happen when open gif DRM file*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 01/18/2016| jun.xie-nb           |[ALM]Defect:958124    |Memory Leak
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.picturegrouping;

import com.tct.gallery3d.util.PermissionUtil;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;

public class AddressPrefetchService extends Service {
    private Handler mFgHandler = new Handler();
    private Context mContext;
    
    private class AddressPrefetchRunnable implements Runnable, AddressCache.Listener {
        private Uri mPictureURI;
        
        AddressPrefetchRunnable(Uri pictureURI){
            mPictureURI = pictureURI;
        }
        
        @Override
        public void run() {
            boolean checkResult = PermissionUtil.checkPermissions(AddressPrefetchService.this,
                    AddressPrefetchService.this.getClass().getName());
            if(!checkResult) {
                return;
            }
            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 begin
            Cursor cursor = null;
            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 end

            boolean waitingForCompletion = false;
            Log.i(PictureGrouping.TAG, "AddressPrefetchService.AddressPrefetchRunnable.run(URI: " + mPictureURI + "){");
            try {
                AddressCache addressCache = AddressCache.getInstance(getApplicationContext());
                addressCache.addListener(this);
                
                ContentResolver cr = mContext.getContentResolver();
                //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 begin
                cursor = cr.query(mPictureURI,
                                         new String[] {
                                            ImageColumns.LATITUDE,
                                            ImageColumns.LONGITUDE
                                         },
                                         null, null, null);
                //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 end
                if (cursor != null && cursor.moveToNext()){
                    int latitudeIndex     = cursor.getColumnIndex(ImageColumns.LATITUDE);
                    int longitudeIndex    = cursor.getColumnIndex(ImageColumns.LONGITUDE);
                    boolean hasCoordinates = ! cursor.isNull(latitudeIndex) && ! cursor.isNull(longitudeIndex);
                    if (hasCoordinates){
                        float latitude = (float) cursor.getDouble(latitudeIndex);
                        float longitude = (float) cursor.getDouble(longitudeIndex);
                        waitingForCompletion = addressCache.prefetchAddress(latitude, longitude);
                        Log.i(PictureGrouping.TAG, "AddressPrefetchService.AddressPrefetchRunnable.run() => waitingForCompletion: " + waitingForCompletion);
                    }
                    else {
                        Log.i(PictureGrouping.TAG, "AddressPrefetchService.AddressPrefetchRunnable.run() => picture doesn't have coordinates");
                    }
                }
                else {
                    Log.i(PictureGrouping.TAG, "*** AddressPrefetchService.AddressPrefetchRunnable.run() => cursor is null or empty");
                }
            }
            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 begin
            catch (Exception e){
                e.printStackTrace();
            } finally {
                if(cursor != null) {
                    cursor.close();
                }
            }
            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 end
            if (! waitingForCompletion){
                mFgHandler.post(mAddressPrefetchComplete);
            }
            Log.i(PictureGrouping.TAG, "} AddressPrefetchService.AddressPrefetchRunnable.run() => waitingForCompletion: " + waitingForCompletion);
        }

        @Override
        public void onAddressCacheUpdated(boolean successful){
            mFgHandler.post(mAddressPrefetchComplete);
        }
    }
    
    private Runnable mAddressPrefetchComplete = new Runnable(){
        @Override
        public void run() {
            Log.i(PictureGrouping.TAG, "AddressPrefetchService.mAddressPrefetchComplete()");
            stopSelf();
        }
    };
    
    @Override
    public void onCreate(){
        super.onCreate();
        
        Log.i(PictureGrouping.TAG, "AddressPrefetchService.onCreate()");
        mContext = this;
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(PictureGrouping.TAG, "AddressPrefetchService.onDestroy()");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        try {
            Uri pictureURI = intent.getData();
            Log.i(PictureGrouping.TAG, "AddressPrefetchService.onStartCommand(" + intent + ", URI: " + pictureURI + ")");
            
            HandlerThread bgThread = BackgroundThread.getInstance();
            Handler handler = new Handler(bgThread.getLooper());
            handler.post(new AddressPrefetchRunnable(pictureURI));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        return Service.START_NOT_STICKY;
    }

    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

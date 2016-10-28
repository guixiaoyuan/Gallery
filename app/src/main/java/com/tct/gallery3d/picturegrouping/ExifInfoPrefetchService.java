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
/* 05/11/2015|chengbin.du-nb        |ALM-865458            |[Android 5.1][Gallery_v5.2.3.1.0310.0]burstshot image can not display in moment view*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 11/11/2015|chengbin.du-nb        |ALM-899577            |[Android5.1][Gallery_v5.2.3.1.0310.0]retrieve slowmotion video and micro video info*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 11/17/2015|chengbin.du-nb        |ALM-913700            |[DRM] Gallery force closed happen when open gif DRM file*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
package com.tct.gallery3d.picturegrouping;

import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.data.GappTypeInfo;
import com.tct.gallery3d.util.PermissionUtil;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images.ImageColumns;

public class ExifInfoPrefetchService extends Service {
    private static final String TAG = "ExifInfoPrefetch";
    private Context mContext;
    private Handler mFgHandler;
    private Handler mBgHandler;

    private static final String[] PROJECTION = {
            FileColumns._ID,
            FileColumns.DATA,
            FileColumns.DATE_MODIFIED,
            ImageColumns.DATE_TAKEN };

    private static final String[] NEWPROJECTION = {
            FileColumns._ID,
            FileColumns.DATA,
            FileColumns.DATE_MODIFIED,
            ImageColumns.DATE_TAKEN,
            GappTypeInfo.GAPP_MEDIA_TYPE,
            GappTypeInfo.GAPP_BURST_ID,
            GappTypeInfo.GAPP_BURST_INDEX };

    public ExifInfoPrefetchService() {
        mContext = this;
        mFgHandler = new Handler();
        //[BUGFIX]-Add by TCTNJ,jun.xie-nb, 2016-1-14,ALM-1207652 begin
        HandlerThread handlerThread = BackgroundThread.getInstance();
        //[BUGFIX]-Add by TCTNJ,jun.xie-nb, 2016-1-14,ALM-1207652 end
        mBgHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri pictureUri = intent.getData();

        Log.d(TAG, "ExifInfoPrefetchService.onStartCommand " + pictureUri);
        mBgHandler.post(new ExifInfoPrefetchRunnable(pictureUri));
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class ExifInfoPrefetchRunnable implements Runnable {
        private Uri mMediaURI;

        public ExifInfoPrefetchRunnable(Uri uri) {
            this.mMediaURI = uri;
        }

        @Override
        public void run() {
            Log.d(TAG, "ExifInfoPrefetchRunnable.run {");
            boolean checkResult = PermissionUtil.checkPermissions(ExifInfoPrefetchService.this,
                    ExifInfoPrefetchService.this.getClass().getName());
            if(!checkResult) {
                return;
            }

            ContentResolver cr = mContext.getContentResolver();
            Cursor cs = null;
            boolean isOld = false;
            try {
                try {
                    cs = cr.query(mMediaURI, NEWPROJECTION, null, null, null);
                }catch (SQLiteException e){
                    cs = cr.query(mMediaURI, PROJECTION, null, null, null);
                    isOld = true;
                }

                if(cs != null && cs.moveToNext()) {
                    int idIndex = cs.getColumnIndex(FileColumns._ID);
                    int dataIndex = cs.getColumnIndex(FileColumns.DATA);
                    int dateModifiedIndex = cs.getColumnIndex(FileColumns.DATE_MODIFIED);
                    int dateTakenIndex = cs.getColumnIndex(ImageColumns.DATE_TAKEN);

                    long id = cs.getLong(idIndex);
                    String path = cs.getString(dataIndex);
                    long timestamp = cs.getLong(dateModifiedIndex) * 1000;
                    if(!cs.isNull(dateTakenIndex)) {
                        timestamp = cs.getLong(dateTakenIndex);
                    }

                    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-05,ALM-865458 begin
                    int mediaType = FileColumns.MEDIA_TYPE_NONE;
                    String uri = mMediaURI.toString();
                    if(uri.contains("images")) {
                        mediaType = FileColumns.MEDIA_TYPE_IMAGE;
                    } else if(uri.contains("video")) {
                        mediaType = FileColumns.MEDIA_TYPE_VIDEO;
                    }
                    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-05,ALM-865458 end
                    int type = 0;
                    if(isOld){
                        type = ExifInfoFilter.getInstance(mContext).filter(String.valueOf(id), path, mediaType, timestamp, true, true, null);
                    }else{
                        int gappMediaTypeIndex  = cs.getColumnIndex(GappTypeInfo.GAPP_MEDIA_TYPE);
                        int burstIdIndex  = cs.getColumnIndex(GappTypeInfo.GAPP_BURST_ID);
                        int burstIndexIndex  = cs.getColumnIndex(GappTypeInfo.GAPP_BURST_INDEX);
                        GappTypeInfo gappTypeInfo = new GappTypeInfo();
                        gappTypeInfo.setType(cs.getInt(gappMediaTypeIndex));
                        gappTypeInfo.setBurstshotId(cs.getInt(burstIdIndex));
                        gappTypeInfo.setBurstshotIndex(cs.getInt(burstIndexIndex));
                        type = ExifInfoFilter.getInstance(mContext).filter(String.valueOf(id), path, mediaType, timestamp, true, true, gappTypeInfo);
                    }

                    Log.i(TAG, "id = " + id + " path = " + path + " timestamp = " + timestamp + " ret = " + type);

                }
            } catch(Exception e) {
                Log.e(TAG, "", e);
            } finally {
                if(cs != null) {
                    cs.close();
                }
            }

            mFgHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
                }
            });
            Log.d(TAG, "} ExifInfoPrefetchRunnable.run");
        }
    }
}

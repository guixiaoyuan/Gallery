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
/* 11/11/2015|chengbin.du-nb        |ALM-899577            |[Android5.1][Gallery_v5.2.3.1.0310.0]retrieve slowmotion video and micro video info*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 19/11/2015|chengbin.du-nb        |ALM-940132            |[Android 6.0][Gallery_v5.2.3.1.1.0307.0]Gallery moments view should display images&videos include "Pictures" folder*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 16/12/2015|chengbin.du-nb        |ALM-1170791           |Momments display vGallery.*/
/* ----------|----------------------|----------------------|----------------- */
/* 21/12/2015|chengbin.du-nb        |ALM-1121296           |[Gallery]Fyuse pictures can't display in gallery after capture a Fyuse picture
/* ----------|----------------------|----------------------|----------------- */
/* 26/12/2015|chengbin.du-nb        |ALM-1003219           |[Gallery]The moments display wrong date of pictures
/* ----------|----------------------|----------------------|----------------- */
/* 12/26/2015| jian.pan1            |[ALM]Defect:1048523   |[Gallery]There is no show list as Fyuse in moment of gallery
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.picturegrouping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.data.GappTypeInfo;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.util.TctLog;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video.VideoColumns;

public class PictureGroupLoader extends Loader<List<PictureGroup>> implements AddressCache.Listener, GroupMerger.Client {

    private static Handler sBgHandler;
    private static long sBgThreadId;

    private Handler mFgHandler;
    private long mFgThreadId;
    private List<PictureGroup> mFgGroups; // Used to share with clients
    private List<PictureGroup> mBgGroups = new ArrayList<PictureGroup>(); // Used by BG thread to work with, then copied into mFgGroups
    private AddressCache mAddressCache;
    private boolean mGoodEnoughForNow;
    private GroupMerger mGroupMerger;
    private Cursor mCursor;
    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
    private boolean mFirstRound = true;
    private boolean mRoundStarted;
    private boolean mRoundFinished;
    private boolean mAddressCacheUpdated;
    private boolean mStopped;
    private long mPrevTimestamp = -1;
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-26,Defect:1048523

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-26,Defect:1048523 begin
    public PictureGroupLoader(Context context) {
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
        super(context);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-26,Defect:1048523 end
        mFgHandler = new Handler();
        mFgThreadId = Thread.currentThread().getId();

        HandlerThread bgThread = BackgroundThread.getInstance();
        sBgHandler = new Handler(bgThread.getLooper());
        sBgThreadId = bgThread.getId();
    }

    @Override
    protected void onAbandon(){
        checkFgThread();
        onReset();
    }

    @Override
    protected boolean onCancelLoad(){
        checkFgThread();
        onReset(); // Not sure what else to do...
        return false;
    }

    @Override
    protected void onForceLoad(){
        checkFgThread();
        onStartLoading(); // Not sure what else to do...
    }

    @Override
    protected void onReset(){
        checkFgThread();
        sBgHandler.post(mBgResetLoadingRunnable);
    }

    @Override
    protected void onStartLoading(){
        checkFgThread();

        // If we have some results from previous query, deliver them immediately...
        synchronized(this){
            try {
                if (mFgGroups != null){
                    deliverResult(mFgGroups);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        sBgHandler.post(mBgStartLoadingRunnable);
    }

    @Override
    protected void onStopLoading(){
        checkFgThread();
        sBgHandler.post(mBgStopLoadingRunnable);
    }

    private void checkFgThread(){
        long threadId = Thread.currentThread().getId();
        if (threadId != mFgThreadId){
            throw new Error("Not on foreground thread !");
        }
    }

    private void checkBgThread(){
        long threadId = Thread.currentThread().getId();
        if (threadId != sBgThreadId){
            throw new Error("Not on background thread !");
        }
    }


    private Runnable mFgDeliverResultRunnable = new Runnable(){
        @Override
        public void run(){
            try {
                checkFgThread();
                synchronized(this){
                    try {
                        if (mFgGroups != null){
                            deliverResult(mFgGroups);
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };


    private Runnable mBgResetLoadingRunnable = new Runnable(){
        @Override
        public void run(){
            try {
                checkBgThread();

                mStopped = true;
                mRoundStarted = false;
                mRoundFinished = false;
                mFirstRound = true;
                mAddressCacheUpdated = false;
                mGoodEnoughForNow = true;

                mBgGroups.clear();

                mAddressCache = AddressCache.getInstance(getContext());
                mAddressCache.removeListener(PictureGroupLoader.this);
                mAddressCache = null; // Not very useful line...

                mGroupMerger = null;

                if (mCursor != null){
                    mCursor.close();
                    mCursor = null;
                }

                synchronized(this){
                    mFgGroups = new ArrayList<PictureGroup>(mBgGroups);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };


    private Runnable mBgStartLoadingRunnable = new Runnable(){
        @Override
        public void run(){
            try {
                checkBgThread();

                if (! mRoundFinished){
                    mStopped = false;

                    if (! mRoundStarted){
                        mRoundStarted = true;

                        mAddressCacheUpdated = false;
                        mBgGroups.clear();

                        if (mAddressCache == null){
                            mAddressCache = AddressCache.getInstance(getContext());
                            mAddressCache.addListener(PictureGroupLoader.this);
                        }

                        if (mGroupMerger == null){
                            mGroupMerger = new GroupMerger(getContext(), PictureGroupLoader.this);
                        }
                    }

                    bgLoad();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private Runnable mBgStopLoadingRunnable = new Runnable(){
        @Override
        public void run(){
            try {
                checkBgThread();

                mStopped = true;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private Runnable mBgContinueLoadingRunnable = new Runnable(){
        @Override
        public void run(){
            try {
                if (! mStopped){
                    bgLoad();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };


    private void bgLoad(){
        TctLog.i("Grouping", "bgLoad+");
        checkBgThread();

        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-26,Defect:1048523 begin
/* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
//        if (mIsFyuse) {
//            queryFuyse();
//        } else {
            queryMediaStore();
//        }
/* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-26,Defect:1048523 end
        synchronized(this){
            mFgGroups = new ArrayList<PictureGroup>(mBgGroups);
            Collections.sort(mFgGroups,
                             new Comparator<PictureGroup>(){
                                @Override
                                public int compare(PictureGroup lhs, PictureGroup rhs){
                                    if (rhs.mPictures.get(0).mTimestamp > lhs.mPictures.get(0).mTimestamp) return  1;
                                    if (rhs.mPictures.get(0).mTimestamp < lhs.mPictures.get(0).mTimestamp) return -1;
                                    return 0;
                                }

            });
        }
        TctLog.i("Grouping", "group size " + mFgGroups.size());
        mFgHandler.removeCallbacks(mFgDeliverResultRunnable);
        mFgHandler.post(mFgDeliverResultRunnable);
        TctLog.i("Grouping", "bgLoad-");
    }


    @Override
    public void onAddressCacheUpdated(boolean successful){
        if (successful){
            mAddressCacheUpdated = true;
            if (mRoundFinished){
                mRoundFinished = false;
                sBgHandler.post(mBgStartLoadingRunnable);
            }
        }
    }


    @Override
    public void onNewGroup(PictureGroup group) {
        mBgGroups.add(group);
    }

    private void queryMediaStore(){
        if (! ImageColumns.LATITUDE.equals(VideoColumns.LATITUDE) ||
            ! ImageColumns.LONGITUDE.equals(VideoColumns.LONGITUDE) ||
            ! ImageColumns.DATE_TAKEN.equals(VideoColumns.DATE_TAKEN)){
            // This code has been compiled assuming these two are equal
            // if such is the not case, just crash and patch the code
            throw new Error("ImageColumns.LATITUDE/LONGITUDE/DATE_TAKEN != VideoColumns.LATITUDE/LONGITUDE/DATE_TAKEN");
        }

        TctLog.i(PictureGrouping.TAG, "PictureGroupLoader.queryMediaStore(){");

        mGoodEnoughForNow = false;
        boolean isOld = false;

        try {
            final String VOLUME_NAME = "external"; // FIXME: find a constant in Android API ??
//            ArrayList<Picture> parallaxArray = ExifInfoFilter.getInstance(getContext()).queryParallax(getContext()); // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330

            if (mCursor == null){
                ContentResolver cr = getContext().getContentResolver();
                Uri uri = MediaStore.Files.getContentUri(VOLUME_NAME);

                // every column, although that is huge waste, you probably need
                // BaseColumns.DATA (the path) only.
                String[] projection = {
                        BaseColumns._ID,
                        FileColumns.MEDIA_TYPE,
                        FileColumns.DATA,
                        FileColumns.DATE_MODIFIED,
                        ImageColumns.LATITUDE,
                        ImageColumns.LONGITUDE,
                        ImageColumns.DATE_TAKEN,
                        ImageColumns.WIDTH,
                        ImageColumns.HEIGHT,
                        VideoColumns.RESOLUTION,
                        ImageColumns.ORIENTATION
                };

                String[] newProjection = {
                        BaseColumns._ID,
                        FileColumns.MEDIA_TYPE,
                        FileColumns.DATA,
                        FileColumns.DATE_MODIFIED,
                        ImageColumns.LATITUDE,
                        ImageColumns.LONGITUDE,
                        ImageColumns.DATE_TAKEN,
                        ImageColumns.WIDTH,
                        ImageColumns.HEIGHT,
                        VideoColumns.RESOLUTION,
                        ImageColumns.ORIENTATION,
                        GappTypeInfo.GAPP_MEDIA_TYPE,
                        GappTypeInfo.GAPP_BURST_ID,
                        GappTypeInfo.GAPP_BURST_INDEX
                };

                // exclude media files, they would be here also.
                String selection =
                        "( " +
                        FileColumns.MEDIA_TYPE + "=" +
                        FileColumns.MEDIA_TYPE_IMAGE + " OR " +
                        FileColumns.MEDIA_TYPE + "=" +
                        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-19,BUG-2208330*/
                        FileColumns.MEDIA_TYPE_VIDEO + " )";// +
//                        " AND " +
//                        "( ";
//                final String sdcard0 = Environment.getExternalStorageDirectory().toString() + "/";
//                final String sdcard1 = "/storage/sdcard1/";
//                String[] directores = new String[] {
//                        sdcard0 + Environment.DIRECTORY_DCIM,
//                        sdcard0 + Environment.DIRECTORY_PICTURES,
//                        sdcard0 + Environment.DIRECTORY_DOWNLOADS,
//                        sdcard1 + Environment.DIRECTORY_DCIM,
//                        sdcard1 + Environment.DIRECTORY_PICTURES,
//                        sdcard1 + Environment.DIRECTORY_DOWNLOADS };
//                for (int i = 0; i < directores.length; i++) {
//                    selection += FileColumns.DATA + " like \"" + directores[i] + "%\"";
//                    if (i < directores.length - 1) {
//                        selection += " OR ";
//                    }
//                }
//                selection += " )";
/* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
                String selectionDrm = "";
                if (DrmManager.isDrmEnable && GalleryActivity.TV_LINK_DRM_HIDE_FLAG) {
                    selectionDrm = " AND (" + DrmManager.TCT_IS_DRM + "=0 OR " + DrmManager.TCT_IS_DRM + " IS NULL)";
                }
                selection += selectionDrm;

                String[] selectionArgs = null; // there is no ? in selection so null here

                // Use
                String sortOrder = FileColumns.DATE_MODIFIED + " DESC";
                try {
                    mCursor = cr.query(uri, newProjection, selection, selectionArgs, sortOrder);
                }catch (SQLiteException e){
                    mCursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
                    isOld = true;
                }

                mPrevTimestamp = -1;
            }

            int fileIdIndex       = mCursor.getColumnIndex(BaseColumns._ID);
            int mediaTypeIndex    = mCursor.getColumnIndex(FileColumns.MEDIA_TYPE);
            int dataIndex         = mCursor.getColumnIndex(MediaColumns.DATA);
            int dateModifiedIndex = mCursor.getColumnIndex(FileColumns.DATE_MODIFIED);
            int latitudeIndex     = mCursor.getColumnIndex(ImageColumns.LATITUDE);
            int longitudeIndex    = mCursor.getColumnIndex(ImageColumns.LONGITUDE);
            int dateTakenIndex    = mCursor.getColumnIndex(ImageColumns.DATE_TAKEN);
            int widthIndex        = mCursor.getColumnIndex(ImageColumns.WIDTH);
            int heightIndex       = mCursor.getColumnIndex(ImageColumns.HEIGHT);
            int resolutionIndex   = mCursor.getColumnIndex(VideoColumns.RESOLUTION);
            int orientationIndex  = mCursor.getColumnIndex(ImageColumns.ORIENTATION);

            ExifInfoFilter filter = ExifInfoFilter.getInstance(getContext()); // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330
            while(mCursor.moveToNext()){
                long fileId = mCursor.getLong(fileIdIndex);
                int mediaType = mCursor.getInt(mediaTypeIndex);
                String path = mCursor.getString(dataIndex);
                boolean hasCoordinates = ! mCursor.isNull(latitudeIndex) && ! mCursor.isNull(longitudeIndex);
                long timestamp = mCursor.getLong(dateModifiedIndex);

                /**
                 * Note: this section code is not suitable for this gallery (by chengbin.du-nb@jrdcom.com)
                 **
                long now = Utils.now();
                if (timestamp > now + Poladroid.ONE_WEEK){
                    Log.i(Poladroid.TAG, "Fixing timestamp (too far in the future) for fileId: " + fileId);
                    timestamp = now - 4 * Poladroid.ONE_WEEK;
                }
                if (timestamp > mPrevTimestamp && mPrevTimestamp != -1){
                    Log.i(Poladroid.TAG, "Fixing timestamp (not sorted correctly) for fileId: " + fileId);
                    timestamp = mPrevTimestamp;
                }
                mPrevTimestamp = timestamp;
                */

                int width = 0;
                if(!mCursor.isNull(widthIndex)) {
                    width = mCursor.getInt(widthIndex);
                }
                int height = 0;
                if(!mCursor.isNull(heightIndex)) {
                    height = mCursor.getInt(heightIndex);
                }
                String resolution = null;
                if(width == 0 && height == 0 && !mCursor.isNull(resolutionIndex)) {
                    resolution = mCursor.getString(resolutionIndex);
                    int pos = resolution.indexOf("x");
                    width = Integer.parseInt(resolution.substring(0, pos));
                    height = Integer.parseInt(resolution.substring(pos + 1));
                }
                int orientation = 0;
                if(!mCursor.isNull(orientationIndex)) {
                    orientation = mCursor.getInt(orientationIndex);
                }
                int type = 0;
                if(!isOld) {
                    int gappMediaTypeIndex  = mCursor.getColumnIndex(GappTypeInfo.GAPP_MEDIA_TYPE);
                    int burstIdIndex  = mCursor.getColumnIndex(GappTypeInfo.GAPP_BURST_ID);
                    int burstIndexIndex  = mCursor.getColumnIndex(GappTypeInfo.GAPP_BURST_INDEX);
                    GappTypeInfo gappTypeInfo = new GappTypeInfo();
                    gappTypeInfo.setType(mCursor.getInt(gappMediaTypeIndex));
                    gappTypeInfo.setBurstshotId(mCursor.getInt(burstIdIndex));
                    gappTypeInfo.setBurstshotIndex(mCursor.getInt(burstIndexIndex));
                    type = filter.filter(String.valueOf(fileId), path, mediaType, timestamp, true, false, gappTypeInfo);
                }else{
                    type = filter.filter(String.valueOf(fileId), path, mediaType, timestamp, true, false, null);
                }
                /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/

                if (type == ExifInfoFilter.BURSTSHOTSHIDDEN) { // MODIFIED by Yaoyu.Yang, 2016-08-09,BUG-2208330
                    continue;
                    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
                }


                Picture picture;
                if (hasCoordinates){
                    float latitude = (float) mCursor.getDouble(latitudeIndex);
                    float longitude = (float) mCursor.getDouble(longitudeIndex);
                    picture = new Picture(VOLUME_NAME, mediaType, fileId, latitude, longitude, timestamp, width, height, orientation);
                }
                else {
                    picture = new Picture(VOLUME_NAME, mediaType, fileId, timestamp, width, height, orientation);
                }
                if (PictureGrouping.DEBUG_LOADER){
                    TctLog.i(PictureGrouping.TAG, "File: " + mCursor.getString(fileIdIndex) +
                          ", " + mCursor.getLong(dateModifiedIndex) +
                          ", " + mCursor.getString(dataIndex) +
                          ", latitude: " + (mCursor.isNull(latitudeIndex) ? null : mCursor.getDouble(latitudeIndex)) +
                          ", longitude: " + (mCursor.isNull(longitudeIndex) ? null : mCursor.getDouble(longitudeIndex))); Utils.sleep();
                }

                mGroupMerger.consumePicture(picture);
                mPrevTimestamp = timestamp;
                if (mGoodEnoughForNow){
                    break;
                }
            }
            /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
            filter.notifySaveExifCache();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if (!mGoodEnoughForNow) {
            if (mCursor != null) {
            /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
                mCursor.close();
                mCursor = null;
            }
            mGroupMerger.end();
            mRoundStarted = false;
            mRoundFinished = true;
            mFirstRound = false;

            if (mAddressCacheUpdated) { // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330
                sBgHandler.post(mBgStartLoadingRunnable);
            }
        }
        else {
            sBgHandler.post(mBgContinueLoadingRunnable);
        }

        TctLog.i(PictureGrouping.TAG, "} PictureGroupLoader.queryMediaStore() => mGoodEnoughForNow: " + mGoodEnoughForNow + ", mRoundFinished: " + mRoundFinished);
    }
} // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330

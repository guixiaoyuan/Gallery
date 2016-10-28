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
/* ----------|----------------------|----------------------|----------------- */
/* 05/05/2015|chengbin.du           |PR993696              |[Gallery_02][Video Category Layout] */
/* ----------|----------------------|----------------------|----------------- */
/* 19/05/2015|chengbin.du           |PR1001124             |[SW][Gallery][ANR]Gallery will ANR when slideshow. */
/*-----------|----------------------|----------------------|----------------------------------------*/
/* 06/07/2015|dongliang.feng        |PR999019              |[5.0][Gallery] wrong toast when entering Video category */
/* ----------|----------------------|----------------------|----------------- */
/* 21/07/2015|dongliang.feng        |PR1045643             |[Android 5.1][Gallery_v5.1.13.1.0213.0]The video can‘t delete */
/* ----------|----------------------|----------------------|----------------- */
/* 2016/02/22|  caihong.gu-nb       |  PR-1623780          |[Translation][Gallery][Video]The translation problem of  gallery*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/

package com.tct.gallery3d.data;

import java.util.ArrayList;
import java.util.Comparator;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.data.BucketHelper.BucketEntry;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter; // MODIFIED by Yaoyu.Yang, 2016-07-28,BUG-2208330
import com.tct.gallery3d.util.Future;
import com.tct.gallery3d.util.FutureListener;
import com.tct.gallery3d.util.MediaSetUtils;
import com.tct.gallery3d.util.ThreadPool;
import com.tct.gallery3d.util.ThreadPool.JobContext;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images.ImageColumns;

public class LocalVideoAlbum extends MediaSet implements
        FutureListener<MediaSet> {

    private static final String TAG = "LocalVideoAlbum";
    private static final int INVALID_COUNT = -1;
    private final String mOrderClause;
    private long mLastModified;

    private static final Uri[] mWatchUris =
        { Video.Media.EXTERNAL_CONTENT_URI };

    private final GalleryApp mApplication;
    private final ChangeNotifier mNotifier;
    private final Handler mHandler;
    private boolean mIsLoading;

    private Future<MediaSet> mLoadTask;
    private MediaSet mLoadBuffer;
    private MediaSet mVideoMergeAlbum;

    private int mCachedCount = INVALID_COUNT;
    //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 begin
    private String mMediaSetName;
    //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 end

    public LocalVideoAlbum(Path path, GalleryApp application) {
        super(path, nextVersionNumber());
        Log.i(TAG, "create video album, path=" + path.toString());

        mOrderClause = FileColumns.DATE_MODIFIED + " DESC, "
                + FileColumns._ID + " DESC";

        mApplication = application;
        mNotifier = new ChangeNotifier(this, mWatchUris, application);
        mHandler = new Handler(application.getMainLooper());

        mMediaSetName = mApplication.getResources().getString(R.string.left_title_videos);
        mMediaSetType = MEDIASET_TYPE_VIDEO;//[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-19,PR1001124
    }

    @Override
    public String getName() {
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 begin
        mMediaSetName = mApplication.getResources().getString(R.string.left_title_videos);
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 end
        return mMediaSetName;
    }

    @Override
    public synchronized long reload() {
        Log.i(TAG, "video album reload.");

        if (mNotifier.isDirty()) {
            Log.i(TAG, "submit a AlbumsLoader task.");

            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-06, PR999019 begin
            /*if (mLoadTask != null) mLoadTask.cancel();*/
            mIsLoading = true;
            mCachedCount = INVALID_COUNT;
            mLoadTask = mApplication.getThreadPool().submit(new AlbumsLoader(), this);
            Utils.waitWithoutInterrupt(this);
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-06, PR999019 end
        }
        if (mLoadBuffer != null) {
            Log.i(TAG, "load buffer.");

            mVideoMergeAlbum = mLoadBuffer;
            //mLoadBuffer = null;
            mDataVersion = mVideoMergeAlbum.reload();
            //mDataVersion = nextVersionNumber();
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-21, PR1045643 begin
        else {
            mDataVersion = 0;
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-21, PR1045643 end
        return mDataVersion;
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        Log.i(TAG, "get media item from start=" + start + " to count=" + count);
        return (mVideoMergeAlbum == null) ? new ArrayList<MediaItem>() : mVideoMergeAlbum.getMediaItem(start, count);
    }

    @Override
    public int getMediaItemCount() {
        String whereClause = FileColumns.MEDIA_TYPE + " = ?";
        int mediaType = FileColumns.MEDIA_TYPE_VIDEO;
        Cursor cursor = null;
        boolean isOld = false;
        try {
            try {
                cursor = mApplication.getContentResolver().query(EXTERNAL_URI, NEWPROJECTION, whereClause,
                        new String[]{String.valueOf(mediaType)}, mOrderClause);
            }catch (SQLiteException exception){
                cursor = mApplication.getContentResolver().query(EXTERNAL_URI, PROJECTION, whereClause,
                        new String[]{String.valueOf(mediaType)}, mOrderClause);
                isOld = true;
            }
            if (cursor != null) {
                ExifInfoFilter filter = ExifInfoFilter.getInstance(mApplication.getAndroidContext());

                if (cursor.moveToFirst()) {
                    int dateModifiedIndex = cursor.getColumnIndex(FileColumns.DATE_MODIFIED);
                    long timestamp = cursor.getLong(dateModifiedIndex) * 1000;
                    mLastModified = timestamp;
                }
                do {
                    int idIndex = cursor.getColumnIndex(FileColumns._ID);
                    long id = cursor.getLong(idIndex);
                    int type = filter.queryType(String.valueOf(id));

                    if (type == ExifInfoFilter.NONE) {
                        int dataIndex = cursor.getColumnIndex(FileColumns.DATA);
                        int dateModifiedIndex = cursor.getColumnIndex(FileColumns.DATE_MODIFIED);
                        int dateTakenIndex = cursor.getColumnIndex(ImageColumns.DATE_TAKEN);
                        String path = cursor.getString(dataIndex);
                        long timestamp = cursor.getLong(dateModifiedIndex) * 1000;
                        if (!cursor.isNull(dateTakenIndex)) {
                            timestamp = cursor.getLong(dateTakenIndex);
                        }

                        if(!isOld){
                            int gappMediaTypeIndex  = cursor.getColumnIndex(GappTypeInfo.GAPP_MEDIA_TYPE);
                            int burstIdIndex  = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_ID);
                            int burstIndexIndex  = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_INDEX);
                            GappTypeInfo gappTypeInfo = new GappTypeInfo();
                            gappTypeInfo.setType(cursor.getInt(gappMediaTypeIndex));
                            gappTypeInfo.setBurstshotId(cursor.getInt(burstIdIndex));
                            gappTypeInfo.setBurstshotIndex(cursor.getInt(burstIndexIndex));
                            filter.filter(String.valueOf(id), path, mediaType, timestamp, true, false, gappTypeInfo);
                        }else {
                            filter.filter(String.valueOf(id), path, mediaType, timestamp, true, false, null);
                        }
                    }
                } while (cursor.moveToNext());
                filter.notifySaveExifCache();

                mCachedCount = cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return mCachedCount;
    }

    public long getLastModified() {
        return mLastModified;
    }

    @Override
    public int getSubMediaSetCount() {
        Log.i(TAG, "getSubMediaSetCount");
//        return (mVideoMergeAlbum == null) ? 0 : 1;
        return 0;
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        return mVideoMergeAlbum;
    }

    @Override
    public synchronized boolean isLoading() {
        return mIsLoading;
    }

    @Override
    public synchronized void onFutureDone(Future<MediaSet> future) {
        if (mLoadTask != future) return; // ignore, wait for the latest task

        Log.i(TAG, "AlbumsLoader Task Done.");
        mLoadBuffer = future.get();
        Log.i(TAG, mLoadBuffer == null ? "LoadBuffer is null" : "LoadBuffer is not null");
        if(mLoadBuffer == null) mVideoMergeAlbum = null;
        mIsLoading = false;
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-06, PR999019 begin
        this.notify();
        /*mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyContentChanged();
            }
        });*/
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-06, PR999019 end
    }

    private class AlbumsLoader implements ThreadPool.Job<MediaSet> {

        @Override
        @SuppressWarnings("unchecked")
        public MediaSet run(JobContext jc) {
            // Note: it will be faster if we only select media_type and bucket_id.
            //       need to test the performance if that is worth
            BucketEntry[] entries = BucketHelper.loadBucketEntries(
                    jc, mApplication.getContentResolver(), MediaObject.MEDIA_TYPE_VIDEO);
            Log.i(TAG, "AlbumsLoader Task Start.");

            if (jc.isCancelled()) return null;
            if (entries.length == 0) return null;

            int offset = 0;
            // Move camera and download bucket to the front, while keeping the
            // order of others.
            int index = findBucket(entries, MediaSetUtils.CAMERA_BUCKET_ID);
            if (index != -1) {
                circularShiftRight(entries, offset++, index);
            }
            index = findBucket(entries, MediaSetUtils.SDCARD_CAMERA_BUCKET_ID);
            if (index != -1) {
                circularShiftRight(entries, offset++, index);
            }
            index = findBucket(entries, MediaSetUtils.DOWNLOAD_BUCKET_ID);
            if (index != -1) {
                circularShiftRight(entries, offset++, index);
            }

            DataManager dataManager = mApplication.getDataManager();
            ArrayList<MediaSet> albums = new ArrayList<MediaSet>();
            for (BucketEntry entry : entries) {
                Path path = mPath.getChild(entry.bucketId);
                MediaObject object = dataManager.peekMediaObject(path);
                if(object == null) {
                    object = new LocalAlbum(path, mApplication, entry.bucketId, false, entry.bucketName);
                }
                albums.add((MediaSet)object);
            }
            MediaSet[] mediasets = new MediaSet[albums.size()];
            albums.toArray(mediasets);

            Comparator<MediaItem> comp = DataManager.sDateTakenComparator;
            Path mergealbum = mPath.getChild("merge");
            MediaObject obj = dataManager.peekMediaObject(mergealbum);
            if(obj != null) {
                mergealbum.setObject();
            }
            MediaSet mediaset =  new LocalMergeAlbum(mergealbum, comp, mediasets, 0);
            return mediaset;
        }
    }

    private static int findBucket(BucketEntry entries[], int bucketId) {
        for (int i = 0, n = entries.length; i < n; ++i) {
            if (entries[i].bucketId == bucketId) return i;
        }
        return -1;
    }

    // Circular shift the array range from a[i] to a[j] (inclusive). That is,
    // a[i] -> a[i+1] -> a[i+2] -> ... -> a[j], and a[j] -> a[i]
    private static <T> void circularShiftRight(T[] array, int i, int j) {
        T temp = array[j];
        for (int k = j; k > i; k--) {
            array[k] = array[k - 1];
        }
        array[i] = temp;
    }

    @Override
    public int getMediaSetType() {
        return MEDIASET_TYPE_VIDEO;
    }
}

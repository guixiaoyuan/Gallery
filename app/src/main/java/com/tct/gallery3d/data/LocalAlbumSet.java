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
/* 22/04/2015|dongliang.feng        |CR979027              |[5.0][Gallery] album sorting method */
/* ----------|----------------------|----------------------|----------------- */
/* 05/06/2015|dongliang.feng        |CR1003828             |[5.0][Gallery] Only camera album should be */
/*           |                      |                      |pinned to the first position on Album screen */
/* ----------|----------------------|----------------------|----------------- */
/* 2016/01/22|  caihong.gu-nb       |  PR-1431112          | [GAPP][Android6.0][Gallery]The picture not display on albums interface*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/
/* 02/29/2016| jian.pan1            |[ALM]Defect:1533170   |[GAPP][Android 6.0][Gallery]It will appear some grey pictures on the bottom after delete some pictures/videos on the camera folder of albums
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.display.DisplayManagerGlobal;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.data.BucketHelper.BucketEntry;
import com.tct.gallery3d.data.DataManager.ParallaxSourceListener;
import com.tct.gallery3d.db.DataBaseManager;
import com.tct.gallery3d.db.DataBaseManager.FavoriteDBListener;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter.FilterSourceListener;
import com.tct.gallery3d.util.Future;
import com.tct.gallery3d.util.FutureListener;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.MediaSetUtils;
import com.tct.gallery3d.util.ThreadPool;
import com.tct.gallery3d.util.ThreadPool.JobContext;

import com.tct.gallery3d.util.AlbumSortUtils;
import com.tct.gallery3d.util.AlbumSortUtils.SortListener;

import com.tct.gallery3d.app.constant.GalleryConstant;


// LocalAlbumSet lists all image or video albums in the local storage.
// The path should be "/local/image", "local/video" or "/local/all"
public class LocalAlbumSet extends MediaSet
        implements FutureListener<ArrayList<MediaSet>> {
    @SuppressWarnings("unused")
    private static final String TAG = "LocalAlbumSet";

    public static final Path PATH_ALL = Path.fromString("/local/all");
    public static final Path PATH_IMAGE = Path.fromString("/local/image");
    public static final Path PATH_VIDEO = Path.fromString("/local/video");
    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
    public static final Path PATH_FACESHOW = Path.fromString("/local/faceshow");
    public static final Path PATH_SLOWMOTION = Path.fromString("/local/slowmotion");
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    private static final Uri[] mWatchUris =
            {Images.Media.EXTERNAL_CONTENT_URI, Video.Media.EXTERNAL_CONTENT_URI};

    private final GalleryApp mApplication;
    private final int mType;
    private ArrayList<MediaSet> mAlbums = new ArrayList<MediaSet>();
    private final ChangeNotifier mNotifier;
    private final String mName;
    private final Handler mHandler;
    private boolean mIsLoading;

    private Future<ArrayList<MediaSet>> mLoadTask;
    private ArrayList<MediaSet> mLoadBuffer;
    private DataBaseManager mDataBaseManager = null;

    private final FaceShowListenter mFaceShowListener = new FaceShowListenter();
    private final FavoriteDBObserver mFavoriteObserver = new FavoriteDBObserver();
    private final ParallaxObserver mParallaxObserver = new ParallaxObserver();
    private final SortObserver mSortObserver = new SortObserver();

    private int bucketID = 0;   //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909

    public class FaceShowListenter implements FilterSourceListener {
        public boolean isDirty = false;

        @Override
        public void onSourceChanged() {
            isDirty = true;
            reload();
        }
    }

    public class FavoriteDBObserver implements FavoriteDBListener {
        public boolean isDirty = false;

        @Override
        public void onDBChanged() {
            isDirty = true;
            reload();
        }
    }

    public class ParallaxObserver implements ParallaxSourceListener {
        public boolean isDirty = false;

        @Override
        public void onParallaxChanged() {
            isDirty = true;
            reload();
        }
    }

    public class SortObserver implements SortListener {
        public boolean isDirty = false;

        @Override
        public void sortTypeChanged() {
            isDirty = true;
            reload();
        }
    }

    public LocalAlbumSet(Path path, GalleryApp application) {
        super(path, nextVersionNumber());
        mApplication = application;
        mHandler = new Handler(application.getMainLooper());
        mType = getTypeFromPath(path);
        mNotifier = new ChangeNotifier(this, mWatchUris, application);
        mName = application.getResources().getString(
                R.string.set_label_local_albums);
        mDataBaseManager = mApplication.getDataBaseManager();

        ExifInfoFilter.getInstance(mApplication.getAndroidContext()).registerFilterSourceListener(mFaceShowListener);
        mDataBaseManager.registerFavoriteDBListener(mFavoriteObserver);
        mApplication.getDataManager().registerParallaxListener(mParallaxObserver);
        AlbumSortUtils.getAlbumSort().setListener(mSortObserver);
    }

    private static int getTypeFromPath(Path path) {
        String name[] = path.split();
        if (name.length < 2) {
            throw new IllegalArgumentException(path.toString());
        }
        return getTypeFromString(name[1]);
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        return mAlbums.get(index);
    }

    @Override
    public int getSubMediaSetCount() {
        return mAlbums.size();
    }

    @Override
    public String getName() {
        return mName;
    }

    private static int findBucket(BucketEntry entries[], int bucketId) {
        for (int i = 0, n = entries.length; i < n; ++i) {
            if (entries[i].bucketId == bucketId) return i;
        }
        return -1;
    }

    private int queryCameraCount() {
        int count = 0;
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] column = new String[]{"count(*)"};
        //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909 start
        String where = null;
        if (bucketID == MediaSetUtils.CAMERA_BUCKET_ID)
            where = "bucket_id=" + MediaSetUtils.CAMERA_BUCKET_ID + " and "
                    + FileColumns.MEDIA_TYPE + "=" + FileColumns.MEDIA_TYPE_IMAGE
                    + " or " + FileColumns.MEDIA_TYPE + "=" + FileColumns.MEDIA_TYPE_VIDEO;
        if (bucketID == MediaSetUtils.SDCARD_CAMERA_BUCKET_ID)
            where = "bucket_id=" + MediaSetUtils.SDCARD_CAMERA_BUCKET_ID + " and "
                    + FileColumns.MEDIA_TYPE + "=" + FileColumns.MEDIA_TYPE_IMAGE
                    + " or " + FileColumns.MEDIA_TYPE + "=" + FileColumns.MEDIA_TYPE_VIDEO;
        //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909 end
        Cursor cursor = mApplication.getContentResolver().query(uri, column, where, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            Utils.assertTrue(cursor.moveToNext());
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }


    private class AlbumsLoader implements ThreadPool.Job<ArrayList<MediaSet>> {

        @Override
        @SuppressWarnings("unchecked")
        public ArrayList<MediaSet> run(JobContext jc) {

            android.util.Log.d(TAG, "AlbumsLoader  run ");
            // Note: it will be faster if we only select media_type and bucket_id.
            //       need to test the performance if that is worth
            BucketEntry[] entries = BucketHelper.loadBucketEntries(
                    jc, mApplication.getContentResolver(), mType);

            if (jc.isCancelled()) return null;

            int offset = 0;
            // Move camera and download bucket to the front, while keeping the
            // order of others.
            int index = findBucket(entries, MediaSetUtils.CAMERA_BUCKET_ID);
            if (index != -1) {
                circularShiftRight(entries, offset++, index);
            }
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-22, CR979027 begin
            int otherIndex = findBucket(entries, MediaSetUtils.SDCARD_CAMERA_BUCKET_ID);
            if (otherIndex != -1) {
                circularShiftRight(entries, offset++, otherIndex);
            }
            /*int screenShotIndex = findBucket(entries, MediaSetUtils.SNAPSHOT_BUCKET_ID);
            if (screenShotIndex != -1) {
                circularShiftRight(entries, offset++, screenShotIndex);
            }
            int downloadIndex = findBucket(entries, MediaSetUtils.DOWNLOAD_BUCKET_ID);
            if (downloadIndex != -1) {
                circularShiftRight(entries, offset++, downloadIndex);
            }
            if (index != -1 && otherIndex != -1 && screenShotIndex != -1 && downloadIndex != -1) {
                shift(entries, offset-4);
            }*/
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-22, CR979027 end

            ArrayList<MediaSet> albums = new ArrayList<MediaSet>();
            int albumCount = 0;
            DataManager dataManager = mApplication.getDataManager();
            boolean hasQueryFaceshow = false;// [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170
            for (BucketEntry entry : entries) {
                MediaSet album = getLocalAlbum(dataManager,
                        mType, mPath, entry.bucketId, entry.bucketName);
                Log.i(TAG, "entry bucketName:" + entry.bucketName);
                int count = album.getMediaItemCount();
                if (count == 0) continue;
                albumCount++;
                album.mAlbumFilePath = entry.getAlbumPath(); // MODIFIED by Yaoyu.Yang, 2016-08-17,BUG-2208330
                //[DEFECT]-modified by dekuan.liu,01/22/2016,Defect 1392909 start
                if (entry.bucketId == MediaSetUtils.CAMERA_BUCKET_ID || entry.bucketId == MediaSetUtils.SDCARD_CAMERA_BUCKET_ID) {
                    bucketID = entry.bucketId;
                    List<String> faceshowList = ExifInfoFilter.getInstance(mApplication.getAndroidContext())
                            .queryFaceshow(mApplication.getAndroidContext(), entry.bucketId);
 /*MODIFIED-BEGIN by caihong.gu-nb, 2016-03-30,BUG-1871616*/
//                    int faceshowCount = faceshowList == null ? 0 : faceshowList.size();
                    int allCameraCount = queryCameraCount();
                    if (entry.bucketId == MediaSetUtils.CAMERA_BUCKET_ID) {
                        Log.i(TAG, "cameraCountFromPhone = " + allCameraCount);
                    } else {
                        Log.i(TAG, "cameraCountFromSDcard = " + allCameraCount);
                    }
                    //[DEFECT]-modified by dekuan.liu,01/22/2016,Defect 1392909 end
                    hasQueryFaceshow = true;// [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170
                    if (allCameraCount > 0) {
                     /*MODIFIED-END by caihong.gu-nb,BUG-1871616*/
                        album.mAlbumType = DataSourceType.ALBUM_CAMERA;
                        albums.add(album);
                    } else {
                        offset--;
                    }
                } else if (entry.bucketId == MediaSetUtils.SNAPSHOT_BUCKET_ID) {
                    album.mAlbumType = DataSourceType.ALBUH_SCREENSHOT;
                    albums.add(album);
                } else {
                    album.mAlbumType = DataSourceType.ALBUM_NORMAL;
                    SharedPreferences sharedPreferences = mApplication.getAndroidContext().getSharedPreferences(GalleryConstant.COLLAPSE_DATA_NAME, Context.MODE_PRIVATE);
                    if (sharedPreferences.getString(album.getAlbumFilePath(), null) == null) {
                        albums.add(album);
                    }
                }
            }

            if (albumCount > albums.size()) {
                showCollapseAlbum = true;
            } else {
                showCollapseAlbum = false;
            }
            if ((mType & MEDIA_TYPE_IMAGE) == MEDIA_TYPE_IMAGE) {
                Path faceShowPath = Path.fromString(dataManager.getTopSetPath(DataManager.INCLUDE_LOCAL_FACESHOW));
                MediaSet faceShowAlbum = dataManager.getMediaSet(faceShowPath);
                if (faceShowAlbum.getMediaItemCount() > 0) {
                    faceShowAlbum.mAlbumType = DataSourceType.ALBUM_FACESHOW;
                    albums.add(faceShowAlbum);
                    if (!hasQueryFaceshow) {
                        offset--;
                    }
                    circularShiftRight(albums, offset++, albums.size() - 1);
                }
            }

//            Path favoritePath = Path.fromString(dataManager.getTopSetPath(DataManager.INCLUDE_LOCAL_FAVORITE));
//            MediaSet favoriteAlbum = dataManager.getMediaSet(favoritePath);
            Path favoritePath = null;
            if (mType == MEDIA_TYPE_IMAGE) {
                favoritePath = FavoriteAlbum.FAVORITE_IMAGE_PATH;
            } else if (mType == MEDIA_TYPE_VIDEO) {
                favoritePath = FavoriteAlbum.FAVORITE_VIDEO_PATH;
            } else if (mType == MEDIA_TYPE_ALL) { // MODIFIED by Yaoyu.Yang, 2016-08-09,BUG-2208330
                favoritePath = FavoriteAlbum.FAVORITE_ALL_PATH;
            } else {
                throw new IllegalArgumentException("The type is invallidate.");
            }
            MediaSet favoriteAlbum = getFavoriteAlbum(dataManager, mType, favoritePath);
            if (favoriteAlbum.getMediaItemCount() > 0) {
                favoriteAlbum.mAlbumType = DataSourceType.ALBUM_FAVORITE;
                albums.add(favoriteAlbum);
                circularShiftRight(albums, offset++, albums.size() - 1);
            }

            if (GalleryAppImpl.getTctPrivacyModeHelperInstance(mApplication.getAndroidContext()).isPrivacyModeEnable()
                    && GalleryAppImpl.getTctPrivacyModeHelperInstance(mApplication.getAndroidContext()).isInPrivacyMode()) {
                Path privateAlbumPath = Path.fromString(dataManager.getTopSetPath(DataManager.INCLUDE_LOCAL_PRIVATE));
                MediaSet privateAlbum = dataManager.getMediaSet(privateAlbumPath);
                if (privateAlbum.getMediaItemCount() > 0) {
                    privateAlbum.mAlbumType = DataSourceType.ALBUM_PRIVATE;
                    albums.add(privateAlbum);
                    circularShiftRight(albums, offset++, albums.size() - 1);
                }
            }

            if ((mType & MEDIA_TYPE_VIDEO) == MEDIA_TYPE_VIDEO) {
                Path videoTopPath = Path.fromString(dataManager.getTopSetPath(DataManager.INCLUDE_LOCAL_ALL_VIDEO));
                MediaSet allVideoAlbum = dataManager.getMediaSet(videoTopPath);
                if (allVideoAlbum.getMediaItemCount() > 0) {
                    allVideoAlbum.mAlbumType = DataSourceType.ALBUM_VIDEO;
                    albums.add(allVideoAlbum);
                    circularShiftRight(albums, offset++, albums.size() - 1);
                }
            }
            AlbumSortUtils.getAlbumSort().sort(albums, mApplication.getAndroidContext());
            return albums;
        }
    }

    private MediaSet getFavoriteAlbum(DataManager manager, int type, Path path) {
        synchronized (DataManager.LOCK) {
            MediaObject object = manager.peekMediaObject(path);
            if (object != null) return (MediaSet) object;
            switch (type) {
                case MEDIA_TYPE_IMAGE:
                    return new FavoriteAlbum(path, mApplication, true);
                case MEDIA_TYPE_VIDEO:
                    return new FavoriteAlbum(path, mApplication, false);
                case MEDIA_TYPE_ALL: // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330
                    Comparator<MediaItem> comp = DataManager.sDateTakenComparator;
                    return new LocalMergeAlbum(path, comp, new MediaSet[]{
                            getFavoriteAlbum(manager, MEDIA_TYPE_IMAGE, FavoriteAlbum.FAVORITE_IMAGE_PATH),
                            getFavoriteAlbum(manager, MEDIA_TYPE_VIDEO, FavoriteAlbum.FAVORITE_VIDEO_PATH)}, 0);
            }
            throw new IllegalArgumentException(String.valueOf(type));
        }
    }

    private MediaSet getLocalAlbum(
            DataManager manager, int type, Path parent, int id, String name) {
        synchronized (DataManager.LOCK) {
            Path path = parent.getChild(id);
            MediaObject object = manager.peekMediaObject(path);
            if (object != null) {
                //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/22,PR1431112 begin
                MediaSet mediaSet = (MediaSet) object;
                mediaSet.reload();
                return mediaSet;
                //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/22,PR1431112 end
            }
            switch (type) {
                case MEDIA_TYPE_IMAGE:
                    return new LocalAlbum(path, mApplication, id, true, name);
                case MEDIA_TYPE_VIDEO:
                    return new LocalAlbum(path, mApplication, id, false, name);
                case MEDIA_TYPE_ALL: // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330
                    Comparator<MediaItem> comp = DataManager.sDateTakenComparator;
                    return new LocalMergeAlbum(path, comp, new MediaSet[]{
                            getLocalAlbum(manager, MEDIA_TYPE_IMAGE, PATH_IMAGE, id, name),
                            getLocalAlbum(manager, MEDIA_TYPE_VIDEO, PATH_VIDEO, id, name)}, id);
            }
            throw new IllegalArgumentException(String.valueOf(type));
        }
    }

    @Override
    public synchronized boolean isLoading() {
        return mIsLoading;
    }

    @Override
    // synchronized on this function for
    //   1. Prevent calling reload() concurrently.
    //   2. Prevent calling onFutureDone() and reload() concurrently
    public synchronized long reload() {
        int wifiDisplayStatus = DisplayManagerGlobal.getInstance().getWifiDisplayStatus().getActiveDisplayState();
        if (mFaceShowListener.isDirty || mNotifier.isDirty() || mFavoriteObserver.isDirty || mParallaxObserver.isDirty
                || GalleryActivity.LAST_WIFI_DISPLAY_STATE != wifiDisplayStatus || mSortObserver.isDirty) {
            GalleryActivity.LAST_WIFI_DISPLAY_STATE = wifiDisplayStatus;
            if (mLoadTask != null) mLoadTask.cancel();
            mIsLoading = true;
            mLoadTask = mApplication.getThreadPool().submit(new AlbumsLoader(), this);
            mFaceShowListener.isDirty = false;
            mFavoriteObserver.isDirty = false;
            mParallaxObserver.isDirty = false;
            mSortObserver.isDirty = false;
        }
        if (mLoadBuffer != null) {
            mAlbums = mLoadBuffer;
            mLoadBuffer = null;
            for (MediaSet album : mAlbums) {
                album.reload();
            }
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    @Override
    public synchronized void onFutureDone(Future<ArrayList<MediaSet>> future) {
        if (mLoadTask != future) return; // ignore, wait for the latest task
        mLoadBuffer = future.get();
        mIsLoading = false;
        if (mLoadBuffer == null) mLoadBuffer = new ArrayList<MediaSet>();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyContentChanged();
            }
        });
    }

    // For debug only. Fake there is a ContentObserver.onChange() event.
    void fakeChange() {
        mNotifier.fakeChange();
    }

    // Circular shift the array range from a[i] to a[j] (inclusive). That is,
    // a[i] -> a[i+1] -> a[i+2] -> ... -> a[j], and a[j] -> a[i]
    private static <T> void circularShiftRight(T[] array, int i, int j) {
        T temp = array[j];
        i = i < 0 ? 0 : i;
        for (int k = j; k > i; k--) {
            array[k] = array[k - 1];
        }
        array[i] = temp;
    }

    private static <T> void circularShiftRight(ArrayList<T> list, int i, int j) {
        T temp = list.get(j);
        i = i < 0 ? 0 : i;
        for (int k = j; k > i; k--) {
            list.set(k, list.get(k - 1));
        }
        list.set(i, temp);
    }

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-22, CR979027 begin
    private void shift(BucketEntry[] array, int i) {
        BucketEntry temp = array[i];
        BucketEntry temp1 = array[i + 1];
        if (temp.lastModifiedTime < temp1.lastModifiedTime) {
            array[i] = temp1;
            array[i + 1] = temp;
        }
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-22, CR979027 end
}

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
/* 03/03/2015|ye.chen               |CR938437              |    Swipe to the very right to the first photo/video, do not go into camera.
/* ----------|----------------------|----------------------|----------------- */
/* 10/03/2015|ye.chen               |PR916400              |[GenericApp][Gallery]MTK DRM adaptation
/* ----------|----------------------|----------------------|----------------- */
/* 21/04/2015|dongliang.feng        |PR974323              |[SMC][gallery]com.tct.gallery3d happen crash due to */
/*           |                      |                      |android.database.CursorWindowAllocationException */
/* ----------|----------------------|----------------------|----------------- */
/* 23/04/2015|    jialiang.ren      |      PR-979658       |[5.0][Gallery] photo sorting method*/
/*-----------|----------------------|----------------------|-----------------------------------*/
/* 19/05/2015|chengbin.du           |PR1001124             |[SW][Gallery][ANR]Gallery will ANR when slideshow. */
/*-----------|----------------------|----------------------|----------------------------------------*/
/* 26/05/2015|chengbin.du           |PR1008429             |[Android][Gallery_v5.1.13.1.0204.0][REG]The slideshow function disappear in locations */
/*-----------|-------------- -------|----------------------|----------------------------------------*/
/* 22/07/2015|chengbin.du           |PR1048866             |[Android 5.0][Gallery_v5.1.13.1.0213.0_polaroid]The camera folder store in SD card doesn't display name in Albums */
/*-----------|-------------- -------|----------------------|----------------------------------------*/
/* 14/08/2015|dongliang.feng        |PR1068133             |[Android5.1][Gallery_v5.2.0.1.1.0303.0]Gif picture can not slide show */
/*-----------|-------------- -------|----------------------|----------------------------------------*/
/* 06/11/2015|    su.jiang          |  PR-857659           |[Android5.1][Gallery_v5.2.3.1.0310.0]The picture which display not the select one*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/
/* 27/10/2015|dongliang.feng        |PR786159              |[Gallery]The display is abnormal when press back key on view picture interface */
/* ----------|----------------------|----------------------|----------------- */
/* 14/11/2015|    su.jiang          |  PR-857659           |[Android5.1][Gallery_v5.2.3.1.0310.0]The picture which display not the select one*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/
/* 06/01/2015|    su.jiang          |  PR-1274456          |[Camera]The number of photo displays error in gallery screen after Burst shoot*/
/*-----------|----------------------|----------------------|------------------------------------------------------------------------------*/
/* 2016/01/22|  caihong.gu-nb       |  PR-1431112          | [GAPP][Android6.0][Gallery]The picture not display on albums interface*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/
/* 02/29/2016| jian.pan1            |[ALM]Defect:1533170   |[GAPP][Android 6.0][Gallery]It will appear some grey pictures on the bottom after delete some pictures/videos on the camera folder of albums
/* ----------|----------------------|----------------------|----------------- */


package com.tct.gallery3d.data;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorWindowAllocationException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Files.FileColumns; // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330
import android.provider.MediaStore.Video.VideoColumns;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.util.BucketNames;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.MediaSetUtils;

import java.io.File;
import java.util.ArrayList;

// LocalAlbumSet lists all media items in one bucket on local storage.
// The media items need to be all images or all videos, but not both.
public class LocalAlbum extends MediaSet {
    private static final String TAG = "LocalAlbum";
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170 begin
    private static final String[] COUNT_PROJECTION = { "_id" };
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170 end

    private static final int INVALID_COUNT = -1;
    private String mWhereClause;
    private final String mOrderClause;
    private final Uri mBaseUri;
    private String[] mProjection;

    private final GalleryApp mApplication;
    private final ContentResolver mResolver;
    private final int mBucketId;
    private final String mName;
    private final boolean mIsImage;
    private final ChangeNotifier mNotifier;
    private final Path mItemPath;
    private int mCachedCount = INVALID_COUNT;
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170 begin
    private int mMediaDispalyCount = 0;
    private boolean mReLoadFlag = false;
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170 end

    public LocalAlbum(Path path, GalleryApp application, int bucketId,
            boolean isImage, String name) {
        super(path, nextVersionNumber());
        mApplication = application;
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170 begin
        mReLoadFlag = mApplication.getDataManager().isFaceShowAction;
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170 end
        mResolver = application.getContentResolver();
        mBucketId = bucketId;
        mName = name;
        mIsImage = isImage;
        String selection = "";
        if (DrmManager.isDrmEnable && GalleryActivity.TV_LINK_DRM_HIDE_FLAG) {
            Log.i(TAG, "wifi display drm hide flag is true");
            selection = " AND (" + DrmManager.TCT_IS_DRM + "=0 OR " + DrmManager.TCT_IS_DRM + " IS NULL)";
        }

        if (isImage) {
            mWhereClause = ImageColumns.BUCKET_ID + " = ?" + selection;
            mOrderClause = ImageColumns.DATE_MODIFIED + " DESC, "//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-23,PR979658
                    + ImageColumns._ID + " DESC";
            mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
            //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400

            mProjection = LocalImage.getImageProjection();

            mItemPath = LocalImage.ITEM_PATH;
          //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
        } else {
            mWhereClause = VideoColumns.BUCKET_ID + " = ?" + selection;
            mOrderClause = VideoColumns.DATE_MODIFIED + " DESC, "//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-23,PR979658
                    + VideoColumns._ID + " DESC";
            mBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
          //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400

            mProjection = LocalVideo.getVideoProjection();

          //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
            mItemPath = LocalVideo.ITEM_PATH;
        }

        mNotifier = new ChangeNotifier(this, mBaseUri, application);
    }

    public LocalAlbum(Path path, GalleryApp application, int bucketId,
            boolean isImage) {
        this(path, application, bucketId, isImage,
                BucketHelper.getBucketName(
                application.getContentResolver(), bucketId));
    }

    @Override
    public boolean isCameraRoll() {
         return super.isCameraRoll();//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-03,CR938437 begin
    }

    @Override
    public Uri getContentUri() {
        if (mIsImage) {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendQueryParameter(LocalSource.KEY_BUCKET_ID,
                            String.valueOf(mBucketId)).build();
        } else {
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendQueryParameter(LocalSource.KEY_BUCKET_ID,
                            String.valueOf(mBucketId)).build();
        }
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        DataManager dataManager = mApplication.getDataManager();
        /*Uri uri = mBaseUri.buildUpon()
                .appendQueryParameter("limit", start + "," + count).build();*/
        Uri uri = mBaseUri;
        ArrayList<MediaItem> list = new ArrayList<MediaItem>();
        GalleryUtils.assertNotInRenderThread();
      //[BUGFIX]-Modified by TCTNJ,ye.chen, 2014-12-3,PR857779 begain
        Cursor cursor = null;
        updateWhereClause();
        try{
             cursor = mResolver.query(
                    uri, mProjection, mWhereClause,
                    new String[]{String.valueOf(mBucketId)},
                    mOrderClause);
        }catch(SQLiteException e)
        {
            e.printStackTrace();
            if (GalleryAppImpl.sHasPrivateColumn) {

                if (mIsImage) {
                    mProjection = LocalImage.PRIVATE_PROJECTION;
                } else {
                    mProjection = LocalVideo.PRIVATE_PROJECTION;
                }

            } else {
                if (mIsImage) {
                    mProjection = LocalImage.PROJECTION;
                } else {
                    mProjection = LocalVideo.PROJECTION;
                }
            }
            cursor = mResolver.query(
                    uri, mProjection, mWhereClause,
                    new String[]{String.valueOf(mBucketId)},
                    mOrderClause);
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-21, PR974323 begin
        catch (CursorWindowAllocationException e) {
            e.printStackTrace();
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-21, PR974323 end
      //[BUGFIX]-Modified by TCTNJ,ye.chen, 2014-12-3,PR857779 end

        if (cursor == null) {
            Log.w(TAG, "query fail: " + uri);
            return list;
        }

        try {
            int offset = 0;
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);  // _id must be in the first column
                int type = ExifInfoFilter.getInstance(mApplication.getAndroidContext()).queryType(String.valueOf(id));
                // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170 begin
                if (isNeedHideBurstShot(mBucketId, type)) { // MODIFIED by Yaoyu.Yang, 2016-07-28,BUG-2208330
                    continue;
                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170 end
                } else {
                    if(offset >= start) {
                        Path childPath = mItemPath.getChild(id);
                        MediaItem item = loadOrUpdateItem(childPath, cursor,
                                dataManager, mApplication, mIsImage);
                        list.add(item);
                        if(list.size() >= count) {
                            break;
                        }
                    } else {
                        offset++;
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-06,PR1274456 begin
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-06,PR857659 begin
    private boolean isNeedHideFaceShow(int type) {// [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170
         /*MODIFIED-BEGIN by caihong.gu-nb, 2016-03-30,BUG-1871616*/
        return type == ExifInfoFilter.FACESHOW; // MODIFIED by Yaoyu.Yang, 2016-07-28,BUG-2208330
//        boolean isFaceShowAction = mApplication.getDataManager().isFaceShowAction;
//        boolean isNotFromThirdPart = mApplication.getDataManager().isNotFromThirdPart;
//        return !isFaceShowAction && isNotFromThirdPart && type == ExifInfoFilter.FACESHOW;
 /*MODIFIED-END by caihong.gu-nb,BUG-1871616*/
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-06,PR857659 end

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-28,BUG-2208330*/
    private boolean isHideSlowMotion(int type) {
       return type == ExifInfoFilter.SLOWMOTION;
    }
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
    private boolean isNeedHideBurstShot(int currBucketId, int type){
        boolean isFaceShowAction = mApplication.getDataManager().isFaceShowAction;
        boolean isNeedBurstShot = (type == ExifInfoFilter.BURSTSHOTSHIDDEN);
        if (isFaceShowAction) {
            return false;
        } else {
            return isNeedBurstShot;
        }
    }
    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-06,PR1274456 end

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-14, PR1068133 begin
    @Override
    public int getMediaSetType() {
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-10-27, PR786159 begin
        if (getMediaItemCount() > 0) {
            if (mIsImage) {
                Cursor cursor = null;
                try {
                    String selection = ImageColumns.BUCKET_ID + " = ? AND " + ImageColumns.MIME_TYPE + " NOT LIKE 'image/gif'";
                    String[] selectionArgs = new String[]{String.valueOf(mBucketId)};
                    cursor = mResolver.query(mBaseUri, COUNT_PROJECTION, selection, selectionArgs, null);
                    if (cursor != null) {
                        cursor.moveToNext();
                        if (cursor.getInt(0) > 0) {
                            mMediaSetType = MEDIASET_TYPE_IMAGE;
                        } else {
                            mMediaSetType = MEDIASET_TYPE_GIT;
                        }
                    } else {
                        mMediaSetType = MEDIASET_TYPE_IMAGE;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != cursor) cursor.close();
                }
            } else {
                mMediaSetType = MEDIASET_TYPE_VIDEO;
            }
        } else {
            mMediaSetType = MEDIASET_TYPE_UNKNOWN;
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-10-27, PR786159 end
        return mMediaSetType;
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-14, PR1068133 end

    private static MediaItem loadOrUpdateItem(Path path, Cursor cursor,
            DataManager dataManager, GalleryApp app, boolean isImage) {
        synchronized (DataManager.LOCK) {
            LocalMediaItem item = (LocalMediaItem) dataManager.peekMediaObject(path);
            if (item == null) {
                if (isImage) {
                    item = new LocalImage(path, app, cursor);
                } else {
                    item = new LocalVideo(path, app, cursor);
                }
            } else {
                item.updateContent(cursor);
            }
            return item;
        }
    }

    // The pids array are sorted by the (path) id.
    public static MediaItem[] getMediaItemById(
            GalleryApp application, boolean isImage, ArrayList<Integer> ids) {
        // get the lower and upper bound of (path) id
        MediaItem[] result = new MediaItem[ids.size()];
        if (ids.isEmpty()) return result;
        int idLow = ids.get(0);
        int idHigh = ids.get(ids.size() - 1);

        // prepare the query parameters
        Uri baseUri;
        String[] projection;
        Path itemPath;

        if (GalleryAppImpl.sHasPrivateColumn) {
            if (isImage) {
                baseUri = Images.Media.EXTERNAL_CONTENT_URI;
                projection = LocalImage.PRIVATE_PROJECTION;
                itemPath = LocalImage.ITEM_PATH;
            } else {
                baseUri = Video.Media.EXTERNAL_CONTENT_URI;
                projection = LocalVideo.PRIVATE_PROJECTION;
                itemPath = LocalVideo.ITEM_PATH;
            }

        } else {
            if (isImage) {
                baseUri = Images.Media.EXTERNAL_CONTENT_URI;
                projection = LocalImage.PROJECTION;
                itemPath = LocalImage.ITEM_PATH;
            } else {
                baseUri = Video.Media.EXTERNAL_CONTENT_URI;
                projection = LocalVideo.PROJECTION;
                itemPath = LocalVideo.ITEM_PATH;
            }
        }

        ContentResolver resolver = application.getContentResolver();
        DataManager dataManager = application.getDataManager();
        Cursor cursor = resolver.query(baseUri, projection, "_id BETWEEN ? AND ?",
                new String[]{String.valueOf(idLow), String.valueOf(idHigh)},
                "_id");
        if (cursor == null) {
            Log.w(TAG, "query fail" + baseUri);
            return result;
        }
        try {
            int n = ids.size();
            int i = 0;

            while (i < n && cursor.moveToNext()) {
                int id = cursor.getInt(0);  // _id must be in the first column

                // Match id with the one on the ids list.
                if (ids.get(i) > id) {
                    continue;
                }

                while (ids.get(i) < id) {
                    if (++i >= n) {
                        return result;
                    }
                }

                Path childPath = itemPath.getChild(id);
                MediaItem item = loadOrUpdateItem(childPath, cursor, dataManager,
                        application, isImage);
                result[i] = item;
                ++i;
            }
            return result;
        } finally {
            cursor.close();
        }
    }

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri,
            String[] projection, int id) {
        return resolver.query(uri, projection, "_id=?",
                new String[]{String.valueOf(id)}, null);
    }

    @Override
    public int getMediaItemCount() {
        int hiddencount = 0;
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170 begin
        if (mApplication.getDataManager().isFaceShowAction) hiddencount = 0;
        if (mCachedCount == INVALID_COUNT || GalleryActivity.TV_LINK_DRM_HIDE_FLAG ||
                mReLoadFlag != mApplication.getDataManager().isFaceShowAction) {
            mReLoadFlag = mApplication.getDataManager().isFaceShowAction;
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-21, PR974323 begin
            Cursor cursor = null;
            boolean isOld = false;
            updateWhereClause();
            /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
            int mediaType = FileColumns.MEDIA_TYPE_NONE;
            String whereClause = null;
            if (mIsImage) {
                mediaType = FileColumns.MEDIA_TYPE_IMAGE;
                whereClause = FileColumns.MEDIA_TYPE + " = ? AND " + mWhereClause;
            } else{
                mediaType = FileColumns.MEDIA_TYPE_VIDEO;
                whereClause = FileColumns.MEDIA_TYPE + " = ? AND " + mWhereClause;
            }
            try {
//                cursor = mResolver.query(
//                        mBaseUri, COUNT_PROJECTION, mWhereClause,
//                        new String[]{String.valueOf(mBucketId)}, null);
                cursor = mResolver.query(EXTERNAL_URI, NEWPROJECTION, whereClause,
                        new String[] { String.valueOf(mediaType), String.valueOf(mBucketId) }, null);
                        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
            } catch (CursorWindowAllocationException e) {
                e.printStackTrace();
            } catch (SQLiteException e){
                cursor = mResolver.query(EXTERNAL_URI, PROJECTION, whereClause,
                        new String[] { String.valueOf(mediaType), String.valueOf(mBucketId) }, null);
                isOld = true;
            }
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-21, PR974323 end
            if (cursor == null) {
                Log.w(TAG, "getMediaItemCount query fail");
                return 0;
            }
            try {
                hiddencount = 0;
                if (!mApplication.getDataManager().isFaceShowAction) {
                    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
                    ExifInfoFilter filter = ExifInfoFilter.getInstance(mApplication.getAndroidContext());
                    while (cursor.moveToNext()) {
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
                                type = filter.filter(String.valueOf(id), path, mediaType, timestamp, true, false, gappTypeInfo);
                            }else {
                                type = filter.filter(String.valueOf(id), path, mediaType, timestamp, true, false, null);
                            }

                        }
                        if (isNeedHideBurstShot(mBucketId, type)) { // MODIFIED by Yaoyu.Yang, 2016-07-28,BUG-2208330
                            hiddencount++;
                        }
                    }
                    filter.notifySaveExifCache();
                    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
                } else {
                    Log.i(TAG, "Current is faceshow action.");
                }
                mCachedCount = cursor.getCount();
            } finally {
                cursor.close();
            }
            mMediaDispalyCount = mCachedCount - hiddencount;
        }
        return mMediaDispalyCount;
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-29,Defect:1533170 end
    }

    @Override
    public int getTotalMediaItemCount() {
        return getMediaItemCount();
    }

    @Override
    public String getName() {
        return getLocalizedName(mApplication.getResources(), mBucketId, mName);
    }

    @Override
    public long reload() {
        if (mNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            mCachedCount = INVALID_COUNT;
        }
        return mDataVersion;
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_INFO;
    }

    @Override
    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        updateWhereClause();
        mResolver.delete(mBaseUri, mWhereClause,
                new String[]{String.valueOf(mBucketId)});
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

    private void updateWhereClause() {
        String selection = "";
        if (DrmManager.isDrmEnable && GalleryActivity.TV_LINK_DRM_HIDE_FLAG) {
            Log.i(TAG, "wifi display drm hide flag is true");
            selection = " AND (" + DrmManager.TCT_IS_DRM + "=0 OR " + DrmManager.TCT_IS_DRM + " IS NULL)";
        }

        if (mIsImage) {
            mWhereClause = ImageColumns.BUCKET_ID + " = ?" + selection;
        } else {
            mWhereClause = VideoColumns.BUCKET_ID + " = ?" + selection;
        }
    }

    public static String getLocalizedName(Resources res, int bucketId,
            String name) {
        if (bucketId == MediaSetUtils.CAMERA_BUCKET_ID) {
            return res.getString(R.string.folder_camera);
        } else if (bucketId == MediaSetUtils.DOWNLOAD_BUCKET_ID) {
            return res.getString(R.string.folder_download);
        } else if (bucketId == MediaSetUtils.IMPORTED_BUCKET_ID) {
            return res.getString(R.string.folder_imported);
        } else if (bucketId == MediaSetUtils.SNAPSHOT_BUCKET_ID) {
            return res.getString(R.string.folder_screenshot);
        } else if (bucketId == MediaSetUtils.EDITED_ONLINE_PHOTOS_BUCKET_ID) {
            return res.getString(R.string.folder_edited_online_photos);
        } else if (bucketId == MediaSetUtils.SDCARD_CAMERA_BUCKET_ID){//[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-07-22,PR1048866
            return res.getString(R.string.folder_camera);
        } else{
            return name;
        }
    }

    // Relative path is the absolute path minus external storage path
    public static String getRelativePath(int bucketId) {
        String relativePath = "/";
        if (bucketId == MediaSetUtils.CAMERA_BUCKET_ID) {
            relativePath += BucketNames.CAMERA;
        } else if (bucketId == MediaSetUtils.DOWNLOAD_BUCKET_ID) {
            relativePath += BucketNames.DOWNLOAD;
        } else if (bucketId == MediaSetUtils.IMPORTED_BUCKET_ID) {
            relativePath += BucketNames.IMPORTED;
        } else if (bucketId == MediaSetUtils.SNAPSHOT_BUCKET_ID) {
            relativePath += BucketNames.SCREENSHOTS;
        } else if (bucketId == MediaSetUtils.EDITED_ONLINE_PHOTOS_BUCKET_ID) {
            relativePath += BucketNames.EDITED_ONLINE_PHOTOS;
        } else {
            // If the first few cases didn't hit the matching path, do a
            // thorough search in the local directories.
            File extStorage = Environment.getExternalStorageDirectory();
            String path = GalleryUtils.searchDirForPath(extStorage, bucketId);
            if (path == null) {
                Log.w(TAG, "Relative path for bucket id: " + bucketId + " is not found.");
                relativePath = null;
            } else {
                relativePath = path.substring(extStorage.getAbsolutePath().length());
            }
        }
        return relativePath;
    }

}

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
/* 09/01/2015|dongliang.feng        |FR898833              |[TMO]new feature of Gallery
/* ----------|----------------------|----------------------|----------------- */
/* ----------|----------------------|----------------------|----------------- */
/* 06/02/2015|qiang.ding1           |FR926794              |[Gallery v5.1.4.1.0112.0][Gallery]Gallery exits automatically when open a picture(NREP)
/* ----------|----------------------|----------------------|----------------- */
/* 03/09/2015|ye.chen               |PR938397              |[Android5.0][Gallery_v5.1.4.1.0117.0][REG]The Current Constraint shows error in count file
/* ----------|----------------------|----------------------|----------------- */
/* 03/12/2015|ye.chen               |PR945450              |[Performance][Response time][Gallery] It take more time to launch Gallery than reference phone
/* ----------|----------------------|----------------------|----------------- */
/* 03/16/2015|ye.chen               |PR936956              |[Clone][4.7][Downloads]Sometimes set the sd image as wallpaper will display "cannot load the image!"
/* ----------|----------------------|----------------------|----------------- */
/* 04/01/2015|ye.chen               |PR916400              |[GenericApp][Gallery]MTK DRM adaptation
/* ----------|----------------------|----------------------|----------------- */
/* 14/04/2015|    jialiang.ren     |      PR-186187       |Global_v5.1.9.1.0109.0_signed_platformkey_alldpi*/
/* ----------|---------------------|----------------------|------------------------------------------------*/
/* 23/04/2015|    qiang.ding1       |      PR-186130       |[Download][DRM]Can't set DRM image as wallpaper*/
/* ----------|------------------- --|----------------------|------------------------------------------------*/
/* 13/07/2015 |    su.jiang         |      PR-1038196      |[Android 5.1][Gallery_v5.1.13.1.0211.0]The Current Constraint shows error in--------*/
/*------------|---------------------|----------------------|count file after sliding show-------------------------------------------------------*/
/* ----------|----------------------|----------------------|----------------- */
/* 21/08/2015|dongliang.feng        |PR1069861             |[Gallery]Can't rotate left/right after ".gif "rename ".jpg" format */
/* ----------|----------------------|----------------------|----------------- */
/* 14/10/2015|chengbin.du-nb        |ALM-676093            |[Android 5.1][Gallery_v5.2.2.1.1.0305.0]The response time of switching Month view to Day view is too long.*/
/* ----------|----------------------|----------------------|----------------- */
/* 06/11/2015|    su.jiang          |  PR-861359           |[Android5.1][Gallery_v5.2.3.1.0310.0][Force Close]Gallery force close when tap selfies album*/
/*-----------|----------------------|----------------------|--------------------------------------------------------------------------------------------*/
/* 17/11/2015|    su.jiang          |  PR-897711           |[Android5.1][Gallery_v5.2.3.1.0311.0]It can't delete burst shot photo*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------*/
/* 11/18/2015| jian.pan1            | [ALM]Defect:934056   |[Idol4]Gallery force close when filter photos
/* ----------|----------------------|----------------------|----------------- */
/* 16/12/2015|chengbin.du-nb        |ALM-1170791           |Momments display vGallery.*/
/* ----------|----------------------|----------------------|----------------- */
/* 03/05/2016| jian.pan1            |[ALM]Defect:1550574   |[GAPP][Android6.0][Gallery]The key icon of DRM picture doesn't display in Moments interface.
/* ----------|----------------------|----------------------|----------------- */
/* 2016/03/15|  caihong.gu-nb       |  PR-1719169   |    [Gallery]Pop up gallery force closed after delete some pictures of moment
/*-----------|----------------------|---------------|---------------------------------------------------------------------------------*/
package com.tct.gallery3d.data;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.Log;

import com.mediatek.omadrm.MtkDrmManager;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.PanoramaMetadataSupport;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.common.ApiHelper;
import com.tct.gallery3d.common.BitmapUtils;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.exif.ExifInterface;
import com.tct.gallery3d.exif.ExifTag;
import com.tct.gallery3d.filtershow.tools.SaveImage;
import com.tct.gallery3d.filtershow.tools.SaveImage.ContentResolverQueryCallback;
import com.tct.gallery3d.picturegrouping.ExifInfoCache.ExifItem;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.PLFUtils;
import com.tct.gallery3d.util.ThreadPool.Job;
import com.tct.gallery3d.util.ThreadPool.JobContext;
import com.tct.gallery3d.util.UpdateHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-17,BUG-2208330*/
/* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
/* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-17,BUG-2208330*/
/* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

// LocalImage represents an image in the local storage.
public class LocalImage extends LocalMediaItem {
    private static final String TAG = "LocalImage";
    private static Map<String, Bitmap> mTctDrmCaches = new HashMap<String, Bitmap>(1000);
    public static final Path ITEM_PATH = Path.fromString("/local/image/item");

    // Must preserve order between these indices and the order of the terms in
    // the following PROJECTION array.

    private static final int INDEX_ID = 0;
    private static final int INDEX_CAPTION = 1;
    private static final int INDEX_MIME_TYPE = 2;
    private static final int INDEX_LATITUDE = 3;
    private static final int INDEX_LONGITUDE = 4;
    private static final int INDEX_DATE_TAKEN = 5;
    private static final int INDEX_DATE_ADDED = 6;
    private static final int INDEX_DATE_MODIFIED = 7;
    private static final int INDEX_DATA = 8;
    private static final int INDEX_ORIENTATION = 9;
    private static final int INDEX_BUCKET_ID = 10;
    private static final int INDEX_SIZE = 11;
    private static final int INDEX_WIDTH = 12;
    private static final int INDEX_HEIGHT = 13;
    private static final int INDEX_IS_PRIATE = 14;
    private static final int INDEX_IS_DRM = 15;
    private static final int INDEX_DRM_TYPE = 16;
    private static final int INDEX_DRM_RIGHT_TYPE = 17;
    private static final int INDEX_DRM_VALID = 18;

    static final String[] PROJECTION = {
            ImageColumns._ID,           // 0
            ImageColumns.TITLE,         // 1
            ImageColumns.MIME_TYPE,     // 2
            ImageColumns.LATITUDE,      // 3
            ImageColumns.LONGITUDE,     // 4
            ImageColumns.DATE_TAKEN,    // 5
            ImageColumns.DATE_ADDED,    // 6
            ImageColumns.DATE_MODIFIED, // 7
            ImageColumns.DATA,          // 8
            ImageColumns.ORIENTATION,   // 9
            ImageColumns.BUCKET_ID,     // 10
            ImageColumns.SIZE,          // 11
            GalleryConstant.NO_COLUMN,  // 12
            GalleryConstant.NO_COLUMN,  // 13
            GalleryConstant.NO_COLUMN,  // 14

    };

    static final String[] PROJECTIONDRM = {
            ImageColumns._ID,               // 0
            ImageColumns.TITLE,             // 1
            ImageColumns.MIME_TYPE,         // 2
            ImageColumns.LATITUDE,          // 3
            ImageColumns.LONGITUDE,         // 4
            ImageColumns.DATE_TAKEN,        // 5
            ImageColumns.DATE_ADDED,        // 6
            ImageColumns.DATE_MODIFIED,     // 7
            ImageColumns.DATA,              // 8
            ImageColumns.ORIENTATION,       // 9
            ImageColumns.BUCKET_ID,         // 10
            ImageColumns.SIZE,              // 11
            GalleryConstant.NO_COLUMN,      // 12
            GalleryConstant.NO_COLUMN,      // 13
            GalleryConstant.NO_COLUMN,      // 14
            DrmManager.TCT_IS_DRM,          //15
            DrmManager.TCT_DRM_TYPE,        //16
            DrmManager.TCT_DRM_RIGHT_TYPE,  //17
            DrmManager.TCT_DRM_VALID        //18
    };
    //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
    private static final int INDEX_DRM_METHOD = 15;//fengke change
    static final String[] PROJECTIONDRM_MTK = {
            ImageColumns._ID,               // 0
            ImageColumns.TITLE,             // 1
            ImageColumns.MIME_TYPE,         // 2
            ImageColumns.LATITUDE,          // 3
            ImageColumns.LONGITUDE,         // 4
            ImageColumns.DATE_TAKEN,        // 5
            ImageColumns.DATE_ADDED,        // 6
            ImageColumns.DATE_MODIFIED,     // 7
            ImageColumns.DATA,              // 8
            ImageColumns.ORIENTATION,       // 9
            ImageColumns.BUCKET_ID,         // 10
            ImageColumns.SIZE,              // 11
            GalleryConstant.NO_COLUMN,      // 12
            GalleryConstant.NO_COLUMN,      // 13
            GalleryConstant.NO_COLUMN,      // 14
            DrmManager.TCT_IS_DRM,          //15
            DrmManager.TCT_DRM_METHOD,      //16
    };

    static final String[] PRIVATE_PROJECTION = {
            ImageColumns._ID,               // 0
            ImageColumns.TITLE,             // 1
            ImageColumns.MIME_TYPE,         // 2
            ImageColumns.LATITUDE,          // 3
            ImageColumns.LONGITUDE,         // 4
            ImageColumns.DATE_TAKEN,        // 5
            ImageColumns.DATE_ADDED,        // 6
            ImageColumns.DATE_MODIFIED,     // 7
            ImageColumns.DATA,              // 8
            ImageColumns.ORIENTATION,       // 9
            ImageColumns.BUCKET_ID,         // 10
            ImageColumns.SIZE,              // 11
            GalleryConstant.NO_COLUMN,      // 12
            GalleryConstant.NO_COLUMN,      // 13
            GalleryConstant.IS_PRIVATE,     // 14

    };

    static final String[] PRIVATE_PROJECTIONDRM = {
            ImageColumns._ID,               // 0
            ImageColumns.TITLE,             // 1
            ImageColumns.MIME_TYPE,         // 2
            ImageColumns.LATITUDE,          // 3
            ImageColumns.LONGITUDE,         // 4
            ImageColumns.DATE_TAKEN,        // 5
            ImageColumns.DATE_ADDED,        // 6
            ImageColumns.DATE_MODIFIED,     // 7
            ImageColumns.DATA,              // 8
            ImageColumns.ORIENTATION,       // 9
            ImageColumns.BUCKET_ID,         // 10
            ImageColumns.SIZE,              // 11
            GalleryConstant.NO_COLUMN,      // 12
            GalleryConstant.NO_COLUMN,      // 13
            GalleryConstant.IS_PRIVATE,     // 14
            DrmManager.TCT_IS_DRM,          // 15
            DrmManager.TCT_DRM_TYPE,        // 16
            DrmManager.TCT_DRM_RIGHT_TYPE,  // 17
            DrmManager.TCT_DRM_VALID        // 18
    };

    static final String[] PRIVATE_PROJECTIONDRM_MTK = {
            ImageColumns._ID,               // 0
            ImageColumns.TITLE,             // 1
            ImageColumns.MIME_TYPE,         // 2
            ImageColumns.LATITUDE,          // 3
            ImageColumns.LONGITUDE,         // 4
            ImageColumns.DATE_TAKEN,        // 5
            ImageColumns.DATE_ADDED,        // 6
            ImageColumns.DATE_MODIFIED,     // 7
            ImageColumns.DATA,              // 8
            ImageColumns.ORIENTATION,       // 9
            ImageColumns.BUCKET_ID,         // 10
            ImageColumns.SIZE,              // 11
            GalleryConstant.NO_COLUMN,      // 12
            GalleryConstant.NO_COLUMN,      // 13
            GalleryConstant.IS_PRIVATE,     // 14
            DrmManager.TCT_IS_DRM,          // 15
            DrmManager.TCT_DRM_METHOD,      // 16
    };

    static {
        updateWidthAndHeightProjection();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void updateWidthAndHeightProjection() {
        if (ApiHelper.HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT) {
            PROJECTION[INDEX_WIDTH] = MediaColumns.WIDTH;
            PROJECTION[INDEX_HEIGHT] = MediaColumns.HEIGHT;
            PROJECTIONDRM[INDEX_WIDTH] = MediaColumns.WIDTH;
            PROJECTIONDRM[INDEX_HEIGHT] = MediaColumns.HEIGHT;
            //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
            PROJECTIONDRM_MTK[INDEX_WIDTH] = MediaColumns.WIDTH;
            PROJECTIONDRM_MTK[INDEX_HEIGHT] = MediaColumns.HEIGHT;
            //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
        }
    }

    private final GalleryApp mApplication;

    public int rotation;

    private String mCopyGoalFile;

    private PanoramaMetadataSupport mPanoramaMetadata = new PanoramaMetadataSupport(this);

    public LocalImage(Path path, GalleryApp application, Cursor cursor) {
        super(path, application, nextVersionNumber());
        mApplication = application;
        loadFromCursor(cursor);
        initDrm();
    }

    public LocalImage(Path path, GalleryApp application, Cursor cursor, boolean fromMoments) {
        super(path, application, nextVersionNumber());
        mApplication = application;
        loadFromCursor(cursor, fromMoments);
        initDrm();
    }

    public LocalImage(Path path, GalleryApp application, int id) {
        super(path, application, nextVersionNumber());
        mApplication = application;
        ContentResolver resolver = mApplication.getContentResolver();
        Uri uri = Images.Media.EXTERNAL_CONTENT_URI;
        //[BUGFIX]-Modified by TCTNJ,ye.chen, 2014-12-3,PR857779 begain
        Cursor cursor = null;
        try {
            cursor = LocalAlbum.getItemCursor(resolver, uri, getImageProjection(), id);
        } catch (SQLiteException e) {
            e.printStackTrace();
            if (GalleryAppImpl.sHasPrivateColumn) {
                cursor = LocalAlbum.getItemCursor(resolver, uri, PRIVATE_PROJECTION, id);
            } else {
                cursor = LocalAlbum.getItemCursor(resolver, uri, PROJECTION, id);
            }

        }
        //[BUGFIX]-Modified by TCTNJ,ye.chen, 2014-12-3,PR857779 end
        if (cursor == null) {
            throw new RuntimeException("cannot get cursor for: " + path);
        }
        try {
            if (cursor.moveToNext()) {
                loadFromCursor(cursor);
            } else {
                throw new RuntimeException("cannot find data for: " + path);
            }
        } finally {
            cursor.close();
        }
        initDrm();//[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
//        initCaptureMode(); //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-01-09, FR898833
        //[BUGFIX]-Modified by TCTNJ,ye.chen, 2015-03-12,PR945450 begain
    }

    public static String[] getImageProjection() {
        String projection[] = null;
        if (GalleryAppImpl.sHasPrivateColumn) {
            if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
                projection = LocalImage.PRIVATE_PROJECTIONDRM;
            } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                projection = LocalImage.PRIVATE_PROJECTIONDRM_MTK;
            } else {
                projection = LocalImage.PRIVATE_PROJECTION;
            }
        } else {
            if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
                projection = LocalImage.PROJECTIONDRM;
            } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                projection = LocalImage.PROJECTIONDRM_MTK;
            } else {
                projection = LocalImage.PROJECTION;
            }
        }
        return projection;
    }

    //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
    private void initDrm() {
        if (isDrmEnable) {
            //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
            if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
                isSupportForward = (isDrm == 1) && (mTctDrmType == DrmManager.DRM_SCHEME_OMA1_SD);
                isSupportSetWallpaper = (isDrm == 1) && (!"count".equals(mTctDrmRightType));
                isRightValid = (isDrm == 1) && (mTctDrmRightValid == 1);
            } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                isSupportForward = (isDrm == 1) && (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                        DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.TRANSFER));
                //[ALM][BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-04-23,PR186130 begin
                if (DrmManager.mCurrentDrm == DrmManager.MTK_DRM) {
                    isSupportSetWallpaper = false;
                    if (MtkDrmManager.checkRightsStatusValid(mApplication.getAndroidContext(), filePath, MtkDrmManager.Action.WALLPAPER)) {
                        isSupportSetWallpaper = true;
                    } else if (MtkDrmManager.RightsStatus.RIGHTS_VALID == DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.DISPLAY)
                            && !DrmManager.getInstance().hasCountConstraint(filePath)) {
                        isSupportSetWallpaper = true;
                    }
                } else {
                    isSupportSetWallpaper = (isDrm == 1)
                            && (MtkDrmManager.RightsStatus.RIGHTS_VALID == DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.WALLPAPER));
                }
                //[ALM][BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-04-23,PR186130 end
                isRightValid = (isDrm == 1) && (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                        DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.DISPLAY));
            }
            //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
        }
    }
    //[FEATURE]-Add-END by ye.chen

    private void loadFromCursor(Cursor cursor) {
        id = cursor.getInt(INDEX_ID);
        caption = cursor.getString(INDEX_CAPTION);
        mimeType = cursor.getString(INDEX_MIME_TYPE);
        latitude = cursor.getDouble(INDEX_LATITUDE);
        longitude = cursor.getDouble(INDEX_LONGITUDE);
        dateTakenInMs = cursor.getLong(INDEX_DATE_TAKEN);
        dateAddedInSec = cursor.getLong(INDEX_DATE_ADDED);
        dateModifiedInSec = cursor.getLong(INDEX_DATE_MODIFIED);
        filePath = cursor.getString(INDEX_DATA);
        rotation = cursor.getInt(INDEX_ORIENTATION);
        bucketId = cursor.getInt(INDEX_BUCKET_ID);
        fileSize = cursor.getLong(INDEX_SIZE);
        width = cursor.getInt(INDEX_WIDTH);
        height = cursor.getInt(INDEX_HEIGHT);
        if (!cursor.isNull(INDEX_IS_PRIATE)) {
            mPrivate = cursor.getInt(INDEX_IS_PRIATE);
        }
        //[BUGFIX]-Modified by TCTNJ,ye.chen, 2014-12-3,PR857779 begain
        //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
        if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
            if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                isDrm = cursor.getInt(INDEX_IS_DRM);
                mTctDrmType = cursor.getInt(INDEX_DRM_TYPE);
                mTctDrmRightType = cursor.getString(INDEX_DRM_RIGHT_TYPE);
                mTctDrmRightValid = cursor.getInt(INDEX_DRM_VALID);
            }
        } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
            if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                isDrm = cursor.getInt(INDEX_IS_DRM);
                mDrmMethod = cursor.getInt(INDEX_DRM_METHOD);
                if (isDrm == 1) {
                    mTctDrmType = DrmManager.getInstance().getDrmScheme(filePath);
                    if (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                            DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.DISPLAY)) {
                        mTctDrmRightValid = 1;
                    } else {
                        mTctDrmRightValid = -1;
                    }
                } else {
                    mTctDrmRightValid = -1;
                    mTctDrmType = -1;
                }
            }
        }
    }

    private void loadFromCursor(Cursor cursor, boolean fromMoments) {
        if (fromMoments) {
            id = cursor.getInt(MomentsNewAlbum.INDEX_ID);
            caption = cursor.getString(MomentsNewAlbum.INDEX_CAPTION);
            mimeType = cursor.getString(MomentsNewAlbum.INDEX_MIME_TYPE);
            latitude = cursor.getDouble(MomentsNewAlbum.INDEX_LATITUDE);
            longitude = cursor.getDouble(MomentsNewAlbum.INDEX_LONGITUDE);
            dateTakenInMs = cursor.getLong(MomentsNewAlbum.INDEX_DATE_TAKEN);
            dateAddedInSec = cursor.getLong(MomentsNewAlbum.INDEX_DATE_ADDED);
            dateModifiedInSec = cursor.getLong(MomentsNewAlbum.INDEX_DATE_MODIFIED);
            filePath = cursor.getString(MomentsNewAlbum.INDEX_DATA);
            rotation = cursor.getInt(MomentsNewAlbum.INDEX_ORIENTATION);
            bucketId = cursor.getInt(MomentsNewAlbum.INDEX_BUCKET_ID);
            fileSize = cursor.getLong(MomentsNewAlbum.INDEX_SIZE);
            width = cursor.getInt(MomentsNewAlbum.INDEX_WIDTH);
            height = cursor.getInt(MomentsNewAlbum.INDEX_HEIGHT);
            if (!cursor.isNull(MomentsNewAlbum.INDEX_IS_PRIVATE)) {
                mPrivate = cursor.getInt(MomentsNewAlbum.INDEX_IS_PRIVATE);
            }
            if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
                if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                    isDrm = cursor.getInt(MomentsNewAlbum.INDEX_IS_DRM);
                    mTctDrmType = cursor.getInt(MomentsNewAlbum.INDEX_DRM_TYPE);
                    mTctDrmRightType = cursor.getString(MomentsNewAlbum.INDEX_DRM_RIGHT_TYPE);
                    mTctDrmRightValid = cursor.getInt(MomentsNewAlbum.INDEX_DRM_VALID);
                }
            } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                    isDrm = cursor.getInt(MomentsNewAlbum.INDEX_IS_DRM);
                    mDrmMethod = cursor.getInt(MomentsNewAlbum.INDEX_DRM_METHOD);
                    if (isDrm == 1) {
                        mTctDrmType = DrmManager.getInstance().getDrmScheme(filePath);
                        if (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                                DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.DISPLAY)) {
                            mTctDrmRightValid = 1;
                        } else {
                            mTctDrmRightValid = -1;
                        }
                    } else {
                        mTctDrmRightValid = -1;
                        mTctDrmType = -1;
                    }
                }
            }
        } else {
            loadFromCursor(cursor);
        }
    }

    @Override
    protected boolean updateFromCursor(Cursor cursor) {
        UpdateHelper uh = new UpdateHelper();
        id = uh.update(id, cursor.getInt(INDEX_ID));
        caption = uh.update(caption, cursor.getString(INDEX_CAPTION));
        mimeType = uh.update(mimeType, cursor.getString(INDEX_MIME_TYPE));
        latitude = uh.update(latitude, cursor.getDouble(INDEX_LATITUDE));
        longitude = uh.update(longitude, cursor.getDouble(INDEX_LONGITUDE));
        dateTakenInMs = uh.update(
                dateTakenInMs, cursor.getLong(INDEX_DATE_TAKEN));
        dateAddedInSec = uh.update(
                dateAddedInSec, cursor.getLong(INDEX_DATE_ADDED));
        dateModifiedInSec = uh.update(
                dateModifiedInSec, cursor.getLong(INDEX_DATE_MODIFIED));
        filePath = uh.update(filePath, cursor.getString(INDEX_DATA));
        rotation = uh.update(rotation, cursor.getInt(INDEX_ORIENTATION));
        bucketId = uh.update(bucketId, cursor.getInt(INDEX_BUCKET_ID));
        fileSize = uh.update(fileSize, cursor.getLong(INDEX_SIZE));
        width = uh.update(width, cursor.getInt(INDEX_WIDTH));
        height = uh.update(height, cursor.getInt(INDEX_HEIGHT));
        if (!cursor.isNull(INDEX_IS_PRIATE)) {
            mPrivate = cursor.getInt(INDEX_IS_PRIATE);
        }
        if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
            if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                isDrm = uh.update(isDrm, cursor.getInt(INDEX_IS_DRM));
                mTctDrmType = uh.update(mTctDrmType, cursor.getInt(INDEX_DRM_TYPE));
                mTctDrmRightType = uh.update(mTctDrmRightType, cursor.getString(INDEX_DRM_RIGHT_TYPE));
                mTctDrmRightValid = uh.update(mTctDrmRightValid, cursor.getInt(INDEX_DRM_VALID));
            }
        } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
            if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                isDrm = uh.update(isDrm, cursor.getInt(INDEX_IS_DRM));
                mDrmMethod = uh.update(mDrmMethod, cursor.getInt(INDEX_DRM_METHOD));
                if (isDrm == 1) {
                    mTctDrmType = DrmManager.getInstance().getDrmScheme(filePath);
                    if (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                            DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.DISPLAY)) {
                        mTctDrmRightValid = 1;
                    } else {
                        mTctDrmRightValid = -1;
                    }
                } else {
                    mTctDrmRightValid = -1;
                    mTctDrmType = -1;
                }
            }
        }
        return uh.isUpdated();
    }

    @Override
    protected boolean updateFromCursor(Cursor cursor, boolean fromMoments) {
        if (fromMoments) {
            UpdateHelper uh = new UpdateHelper();
            id = uh.update(id, cursor.getInt(MomentsNewAlbum.INDEX_ID));
            caption = uh.update(caption, cursor.getString(MomentsNewAlbum.INDEX_CAPTION));
            mimeType = uh.update(mimeType, cursor.getString(MomentsNewAlbum.INDEX_MIME_TYPE));
            latitude = uh.update(latitude, cursor.getDouble(MomentsNewAlbum.INDEX_LATITUDE));
            longitude = uh.update(longitude, cursor.getDouble(MomentsNewAlbum.INDEX_LONGITUDE));
            dateTakenInMs = uh.update(dateTakenInMs, cursor.getLong(MomentsNewAlbum.INDEX_DATE_TAKEN));
            dateAddedInSec = uh.update(dateAddedInSec, cursor.getLong(MomentsNewAlbum.INDEX_DATE_ADDED));
            dateModifiedInSec = uh.update(dateModifiedInSec, cursor.getLong(MomentsNewAlbum.INDEX_DATE_MODIFIED));
            filePath = uh.update(filePath, cursor.getString(MomentsNewAlbum.INDEX_DATA));
            rotation = uh.update(rotation, cursor.getInt(MomentsNewAlbum.INDEX_ORIENTATION));
            bucketId = uh.update(bucketId, cursor.getInt(MomentsNewAlbum.INDEX_BUCKET_ID));
            fileSize = uh.update(fileSize, cursor.getLong(MomentsNewAlbum.INDEX_SIZE));
            width = uh.update(width, cursor.getInt(MomentsNewAlbum.INDEX_WIDTH));
            height = uh.update(height, cursor.getInt(MomentsNewAlbum.INDEX_HEIGHT));
            if (!cursor.isNull(MomentsNewAlbum.INDEX_IS_PRIVATE)) {
                mPrivate = cursor.getInt(MomentsNewAlbum.INDEX_IS_PRIVATE);
            }
            if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
                if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                    isDrm = uh.update(isDrm, cursor.getInt(MomentsNewAlbum.INDEX_IS_DRM));
                    mTctDrmType = uh.update(mTctDrmType, cursor.getInt(MomentsNewAlbum.INDEX_DRM_TYPE));
                    mTctDrmRightType = uh.update(mTctDrmRightType, cursor.getString(MomentsNewAlbum.INDEX_DRM_RIGHT_TYPE));
                    mTctDrmRightValid = uh.update(mTctDrmRightValid, cursor.getInt(MomentsNewAlbum.INDEX_DRM_VALID));
                }
            } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                    isDrm = uh.update(isDrm, cursor.getInt(MomentsNewAlbum.INDEX_IS_DRM));
                    mDrmMethod = uh.update(mDrmMethod, cursor.getInt(MomentsNewAlbum.INDEX_DRM_METHOD));
                    if (isDrm == 1) {
                        mTctDrmType = DrmManager.getInstance().getDrmScheme(filePath);
                        if (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                                DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.DISPLAY)) {
                            mTctDrmRightValid = 1;
                        } else {
                            mTctDrmRightValid = -1;
                        }
                    } else {
                        mTctDrmRightValid = -1;
                        mTctDrmType = -1;
                    }
                }
            }
            return uh.isUpdated();
        } else {
            return updateFromCursor(cursor);
        }
    }

    @Override
    public Job<Bitmap> requestImage(int type) {
        return new LocalImageRequest(mApplication, mPath, dateModifiedInSec,
                type, filePath);
    }

    public static class LocalImageRequest extends ImageCacheRequest {
        private String mLocalFilePath;

        LocalImageRequest(GalleryApp application, Path path, long timeModified,
                          int type, String localFilePath) {
            super(application, path, timeModified, type,
                    MediaItem.getTargetSize(type));
            mLocalFilePath = localFilePath;
        }

        @Override
        public Bitmap onDecodeOriginal(JobContext jc, final int type) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            int targetSize = MediaItem.getTargetSize(type);
            MediaObject item = mPath.getObject();
            Bitmap mBitmap = null;
            // try to decode from JPEG EXIF
            if (type == MediaItem.TYPE_MICROTHUMBNAIL) {
                //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
                if (DrmManager.isDrmEnable) {
                    //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400
                    if (item.isDrm() == 1) {
                        mBitmap = DrmManager.getInstance().getDrmRealThumbnail(mLocalFilePath, options, 108);
                        return mBitmap;
                    }
                    //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400
                }
                //[FEATURE]-Add-END by ye.chen

                ExifInterface exif = new ExifInterface();
                byte[] thumbData = null;
                try {
                    exif.readExif(mLocalFilePath);
                    thumbData = exif.getThumbnail();
                } catch (FileNotFoundException e) {
                    Log.w(TAG, "failed to find file to read thumbnail: " + mLocalFilePath);
                } catch (IOException e) {
                    Log.w(TAG, "failed to get thumbnail from: " + mLocalFilePath);
                }
                if (thumbData != null) {
                    Bitmap bitmap = DecodeUtils.decodeIfBigEnough(
                            jc, thumbData, options, targetSize);
                    if (bitmap != null) return bitmap;
                }
            }
            //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
            if (DrmManager.isDrmEnable) {
                if (item.isDrm() == 1) {
                    if (item.getMediaType() == MEDIA_TYPE_GIF) {
                        //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-09,PR938397
                        return DrmManager.getInstance().getDrmRealThumbnail(mLocalFilePath, options, 108);
                        //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-09,PR938397
                    } else {
                        Bitmap bitmap = DecodeUtils.requestDecode(
                                jc, mLocalFilePath, options, getTargetSize(type));
                        //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400
                        if (bitmap == null)
                            return getDrmThumbnails(jc, mApplication.getAndroidContext(), mLocalFilePath, (MediaItem) item);
                        return bitmap;
                    }
                }
            }
            //[FEATURE]-Add-END by ye.chen
            return DecodeUtils.decodeThumbnail(jc, mLocalFilePath, options, targetSize, type);
        }
    }

    @Override
    public Job<BitmapRegionDecoder> requestLargeImage() {
        return new LocalLargeImageRequest(filePath);
    }

    public static class LocalLargeImageRequest
            implements Job<BitmapRegionDecoder> {
        String mLocalFilePath;

        public LocalLargeImageRequest(String localFilePath) {
            mLocalFilePath = localFilePath;
        }

        @Override
        public BitmapRegionDecoder run(JobContext jc) {
            return DecodeUtils.createBitmapRegionDecoder(jc, mLocalFilePath, false);
        }
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 begin
    @Override
    public Job<Bitmap> requestMomentsImage(int targetWidth, int targetHeight) {
        return new MomentsImageRequest(targetWidth, targetHeight,
                mApplication, filePath, dateModifiedInSec, rotation, mPath);
    }

    public static class MomentsImageRequest implements Job<Bitmap> {
        private int mTargetWidth = 0;
        private int mTargetHeight = 0;
        private GalleryApp mApplication;
        private String mLocalFilePath;
        private long mDateModifiedInSec;
        private int mOrientation;
        private Path mPath;

        public MomentsImageRequest(int targetWidth, int targetHeight,
                                   GalleryApp application, String filePath, long dateModifiedInSec, int orientation, Path path) {
            mTargetWidth = targetWidth;
            mTargetHeight = targetHeight;
            mApplication = application;
            mLocalFilePath = filePath;
            mDateModifiedInSec = dateModifiedInSec;
            mOrientation = orientation;
            mPath = path;
        }

        @Override
        public Bitmap run(JobContext jc) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            MediaObject item = mPath.getObject();
            Bitmap bitmap = null;

            if (jc.isCancelled()) {
                return null;
            }
            if (DrmManager.isDrmEnable) {
                if (item.isDrm() == 1) {
                    DecodeUtils.decodeBounds(jc, mLocalFilePath, options);
                    int bitmapSize = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
                    bitmapSize = (bitmapSize > 0 && bitmapSize < DRM_MOMENTS_THUMBNAIL_SIZE) ? bitmapSize
                            : DRM_MOMENTS_THUMBNAIL_SIZE;
                    bitmap = DrmManager.getInstance().getDrmRealThumbnail(mLocalFilePath, options, bitmapSize);
                    bitmap = BitmapUtils.resizeAndCropCenter(bitmap, mTargetWidth, mTargetHeight, mOrientation, true,
                            true);
                    return bitmap;
                }
            }

            options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = DecodeUtils.decodeThumbnail(jc, mLocalFilePath, mTargetWidth, mTargetHeight, options,
                    mOrientation);
            if (jc.isCancelled()) {
                return null;
            }
            return bitmap;
        }
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 end

    @Override
    public int getSupportedOperations() {
        //[FEATURE]-Add-BEGIN by ye.chen,11/11/2014,support drm
        int operation = SUPPORT_MOVE | SUPPORT_COPY | SUPPORT_INFO | SUPPORT_DELETE | SUPPORT_FAVOURITE | SUPPORT_PRIVATE; // MODIFIED by Yaoyu.Yang, 2016-08-09,BUG-2208330
        if (isDrm() == 1) {
            if (isSupportSetWallpaper() && isAvlid(mApplication.getAndroidContext(), filePath))
                operation |= SUPPORT_SETAS;
            if (isSupportForward()) operation |= SUPPORT_SHARE;
        } else {
            //[BUGFIX]-Modified by TCTNJ,qiang.ding1, 2014-11-28,PR839194 begain
            operation |= SUPPORT_SHARE | SUPPORT_CROP | SUPPORT_PRINT
                    | SUPPORT_SETAS | SUPPORT_ROTATE;
            //[BUGFIX]-Modified by TCTNJ,qiang.ding1, 2014-11-28,PR839194 end
            if (BitmapUtils.isRotationSupported(mimeType)) {
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-20,PR844577 begin
                if (mimeType.endsWith("bmp")) {
                    operation |= SUPPORT_EDIT;
                } else {
                    operation |= SUPPORT_EDIT | SUPPORT_FULL_IMAGE;
                }
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-20,PR844577 end
            }
        }
        if (isDrm() != 1) {
            if (BitmapUtils.isSupportedByRegionDecoder(mimeType)) {
                operation |= SUPPORT_FULL_IMAGE | SUPPORT_EDIT;
            }
        }
        //[BUGFIX]-Modified by TCTNJ,jialiang.ren, 2015-04-14,PR186187 end
        if (GalleryUtils.isValidLocation(latitude, longitude) && !PLFUtils.getBoolean(mApplication.getAndroidContext(), "def_show_on_map_hide")) {
            operation |= SUPPORT_SHOW_ON_MAP;
        }
        return operation;
    }

    @Override
    public void getPanoramaSupport(PanoramaSupportCallback callback) {
        mPanoramaMetadata.getPanoramaSupport(mApplication, callback);
    }

    @Override
    public void clearCachedPanoramaSupport() {
        mPanoramaMetadata.clearCachedValues();
    }

    @Override
    public void delete() {
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/03/15,PR1719169 begin
        if (isFavorite()) {
            super.delete();
        }
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/03/15,PR1719169 end
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-11-17,PR897711 begin
        String idString = String.valueOf(this.id);
        ExifInfoFilter exifInfoFilter = ExifInfoFilter.getInstance(mApplication.getAndroidContext());
        int type = exifInfoFilter.queryType(idString);
        if (type == ExifInfoFilter.BURSTSHOTS) {
            String burstShotId = exifInfoFilter.queryBurstShotId(idString);
            if (burstShotId != null) {
                List<String> burstShotList = exifInfoFilter.queryBurstShots(burstShotId);
                for (String id : burstShotList) {
                    Log.i(TAG, "delete burstShot media id:" + id);
                    deleteMediaStore(id, false);
                }
                exifInfoFilter.removeBurstShot(burstShotId);
            }
        } else {
            Log.i(TAG, "delete media id:" + idString);
            deleteMediaStore(idString, true);
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-11-17,PR897711 end
    }

    private void deleteMediaStore(String idString, boolean deleteExif) {
        GalleryUtils.assertNotInRenderThread();
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = mApplication.getContentResolver();
        SaveImage.deleteAuxFiles(contentResolver, getContentUri());
        contentResolver.delete(baseUri, "_id=?",
                new String[]{idString});
        if (deleteExif) {
            ExifInfoFilter.getInstance(mApplication.getAndroidContext()).removeImageTypeId(idString);//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-06,PR861359
        }
    }

    public void copy(String destPath) {
        String idString = String.valueOf(this.id);
        ExifInfoFilter exifInfoFilter = ExifInfoFilter.getInstance(mApplication.getAndroidContext());
        int type = exifInfoFilter.queryType(idString);
        if (type == ExifInfoFilter.BURSTSHOTS) {
            String burstShotId = exifInfoFilter.queryBurstShotId(idString);
            if (burstShotId != null) {
                List<String> srcBurstShotList = exifInfoFilter.queryBurstShotsPath(burstShotId);
                Log.d(TAG, "copy--------srcBurstShotList = " + srcBurstShotList);
                final String[] destBurstShotList = new String[srcBurstShotList.size()];
                int index = 0;
                //TODO: temp solution to fix cancel copy/move function.
                boolean needDel = false;
                for (String srcPath : srcBurstShotList) {
                    copyMediaStore(destPath);
                    if (mCopyGoalFile == null) {
                        needDel = true;
                        break;
                    }
                    destBurstShotList[index++] = mCopyGoalFile;
                }
                if (needDel) {
                    for (String delPath : destBurstShotList) {
                        if (TextUtils.isEmpty(delPath)) {
                            continue;
                        }
                        File file = new File(delPath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                } else {
                    Log.d(TAG, "copy--------before scanFile");
                    MediaScannerConnection.scanFile((Context) mApplication, destBurstShotList, null, null);
                    Log.d(TAG, "copy--------after scanFile");
                }
            }
        } else {
            copyMediaStore(destPath);
        }
    }

    private void copyMediaStore(String destPath) {
        GalleryUtils.assertNotInRenderThread();
        String copyGoalFile = null;

        ContentResolver contentResolver = mApplication.getContentResolver();
        final String[] fullPath = new String[1];
        String[] queryProjection = new String[]{ImageColumns.DATA};

        Cursor cursor = contentResolver.query(getContentUri(), queryProjection, null, null,
                null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                fullPath[0] = cursor.getString(0);
            }
            cursor.close();
        }

        if (fullPath[0] != null) {
            File fromFile = new File(fullPath[0]);
            copyGoalFile = SaveImage.copyFile(fromFile, destPath, this);
        }
        setCopyGoalPath(copyGoalFile);
    }

    public void setCopyGoalPath(String copyGoalFile) {
        mCopyGoalFile = copyGoalFile;
    }

    public String getGoalFile() {
        return mCopyGoalFile;
    }

    @Override
    public void move(String objectPath) {
        String idString = String.valueOf(this.id);
        ExifInfoFilter exifInfoFilter = ExifInfoFilter.getInstance(mApplication.getAndroidContext());
        int type = exifInfoFilter.queryType(idString);
        if (type == ExifInfoFilter.BURSTSHOTS) {
            String burstShotId = exifInfoFilter.queryBurstShotId(idString);

            if (burstShotId != null) {
                List<ExifItem> srcBurstShotList = exifInfoFilter.getBurstShotItem(burstShotId);
                String[] destBurstShotList = new String[srcBurstShotList.size()];
                String[] destBurstShotId = new String[srcBurstShotList.size()];
                int index = 0;
                //TODO: temp solution to fix cancel copy/move function.
                boolean needDel = false;
                for (ExifItem item : srcBurstShotList) {
                    copyMediaStore(objectPath);
                    if (mCopyGoalFile == null) {
                        needDel = true;
                        break;
                    }
                    destBurstShotList[index] = mCopyGoalFile;
                    destBurstShotId[index] = item.id;
                    index++;
                }
                if (needDel) {
                    for (String delPath : destBurstShotList) {
                        if (TextUtils.isEmpty(delPath)) {
                            continue;
                        }
                        File file = new File(delPath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                } else {
                    for (String itemId : destBurstShotId) {
                        if (itemId != null) {
                            deleteMediaStore(itemId, false);
                        }
                    }
                    exifInfoFilter.removeBurstShot(burstShotId);
                    MediaScannerConnection.scanFile((Context) mApplication, destBurstShotList, null, null);
                }
            }
        } else {
            copyMediaStore(objectPath);
            if (mCopyGoalFile != null) {
                deleteMediaStore(idString, true);
            }
        }
    }

    @Override
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
    public void rotate(int degrees) {
        GalleryUtils.assertNotInRenderThread();
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
        ContentValues values = new ContentValues();
        int rotation = (this.rotation + degrees) % 360;
        if (rotation < 0) rotation += 360;

        if (mimeType.equalsIgnoreCase("image/jpeg")) {
            ExifInterface exifInterface = new ExifInterface();
            ExifTag tag = exifInterface.buildTag(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.getOrientationValueForRotation(rotation));
            if (tag != null) {
                exifInterface.setTag(tag);
                try {
                    exifInterface.forceRewriteExif(filePath);
                    fileSize = new File(filePath).length();
                    values.put(Images.Media.SIZE, fileSize);
                } catch (FileNotFoundException e) {
                    Log.w(TAG, "cannot find file to set exif: " + filePath);
                } catch (IOException e) {
                    Log.w(TAG, "cannot set exif data: " + filePath);
                } catch (Exception e) { //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-21, PR1069861 begin
                    Log.w(TAG, "failed set exif data: " + filePath);
                }
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-21, PR1069861 end
            } else {
                Log.w(TAG, "Could not build tag: " + ExifInterface.TAG_ORIENTATION);
            }
        }

        values.put(Images.Media.ORIENTATION, rotation);
        mApplication.getContentResolver().update(baseUri, values, "_id=?",
                new String[]{String.valueOf(id)});
    }

    @Override
    public Uri getContentUri() {
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
        return baseUri.buildUpon().appendPath(String.valueOf(id)).build();
    }

    @Override
    public int getMediaType() {
        return "image/gif".equalsIgnoreCase(getMimeType()) ? MEDIA_TYPE_GIF : MEDIA_TYPE_IMAGE;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-09,PR938397
    }

    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(MediaDetails.INDEX_ORIENTATION, Integer.valueOf(rotation));
        if (MIME_TYPE_JPEG.equals(mimeType)) {
            // ExifInterface returns incorrect values for photos in other format.
            // For example, the width and height of an webp images is always '0'.
            MediaDetails.extractExifInfo(details, filePath);
        }
        if (isDrm == 1) {
            setDrmPropertyDialog(mApplication.getAndroidContext(), details, filePath, DrmStore.Action.DISPLAY);
        } else {
            details.addDetail(MediaDetails.INDEX_ORIENTATION, Integer.valueOf(rotation));
            MediaDetails.extractExifInfo(details, filePath);
        }
        return details;
    }

    @Override
    public int getRotation() {
        return rotation;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }
}

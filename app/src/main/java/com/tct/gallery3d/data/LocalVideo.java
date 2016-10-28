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
/* 04/01/2015|ye.chen               |PR916400              |[GenericApp][Gallery]MTK DRM adaptation
/* ----------|----------------------|----------------------|----------------- */
/* 06/11/2015|    su.jiang          |PR-1016732            |[Android 5.0][Gallery_v5.1.13.1.0206.0]The details don't have 'Duration' if the video is less than 1s*/
/*-----------|----------------------|----------------------|-------------------------------------------------------------*/
/* 14/10/2015|chengbin.du-nb        |ALM-676093            |[Android 5.1][Gallery_v5.2.2.1.1.0305.0]The response time of switching Month view to Day view is too long.*/
/* ----------|----------------------|----------------------|----------------- */
/* 16/12/2015|chengbin.du-nb        |ALM-1170791           |Momments display vGallery.*/
/* ----------|----------------------|----------------------|----------------- */
/* 17/02/2016|    su.jiang          |  PR-1431083          |[GAPP][Android6.0][Gallery]The DRM video play interface display not same as preview interface.*/
/*-----------|----------------------|----------------------|----------------------------------------------------------------------------------------------*/
/* 03/05/2016| jian.pan1            |[ALM]Defect:1550574   |[GAPP][Android6.0][Gallery]The key icon of DRM picture doesn't display in Moments interface.
/* ----------|----------------------|----------------------|----------------- */
/* 2016/03/15|  caihong.gu-nb       |  PR-1719169          |[Gallery]Pop up gallery force closed after delete some pictures of moment
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/
package com.tct.gallery3d.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;

import com.mediatek.omadrm.MtkDrmManager;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.common.BitmapUtils;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.filtershow.tools.SaveImage;
import com.tct.gallery3d.filtershow.tools.SaveImage.ContentResolverQueryCallback;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.ThreadPool.Job;
import com.tct.gallery3d.util.ThreadPool.JobContext;
import com.tct.gallery3d.util.UpdateHelper;

import java.io.File;

// LocalVideo represents a video in the local storage.
public class LocalVideo extends LocalMediaItem {
    private static final String TAG = "LocalVideo";
    public static final Path ITEM_PATH = Path.fromString("/local/video/item");

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
    private static final int INDEX_DURATION = 9;
    private static final int INDEX_BUCKET_ID = 10;
    private static final int INDEX_SIZE = 11;
    private static final int INDEX_RESOLUTION = 12;
    private static final int INDEX_IS_PRIATE = 13;
    private static final int INDEX_IS_TCT_DRM = 14;
    private static final int INDEX_TCT_DRM_TYPE = 15;
    private static final int INDEX_TCT_DRM_RIGHT_TYPE = 16;
    private static final int INDEX_TCT_DRM_VALID = 17;

    static final String[] PROJECTIONDRM = new String[]{
            VideoColumns._ID,               //0
            VideoColumns.TITLE,             //1
            VideoColumns.MIME_TYPE,         //2
            VideoColumns.LATITUDE,          //3
            VideoColumns.LONGITUDE,         //4
            VideoColumns.DATE_TAKEN,        //5
            VideoColumns.DATE_ADDED,        //6
            VideoColumns.DATE_MODIFIED,     //7
            VideoColumns.DATA,              //8
            VideoColumns.DURATION,          //9
            VideoColumns.BUCKET_ID,         //10
            VideoColumns.SIZE,              //11
            VideoColumns.RESOLUTION,        //12
            GalleryConstant.NO_COLUMN,      //13
            DrmManager.TCT_IS_DRM,          //14
            DrmManager.TCT_DRM_TYPE,        //15
            DrmManager.TCT_DRM_RIGHT_TYPE,  //16
            DrmManager.TCT_DRM_VALID,       //17
    };
    static final String[] PROJECTION = new String[]{
            VideoColumns._ID,                //0
            VideoColumns.TITLE,              //1
            VideoColumns.MIME_TYPE,          //2
            VideoColumns.LATITUDE,           //3
            VideoColumns.LONGITUDE,          //4
            VideoColumns.DATE_TAKEN,         //5
            VideoColumns.DATE_ADDED,         //6
            VideoColumns.DATE_MODIFIED,      //7
            VideoColumns.DATA,               //8
            VideoColumns.DURATION,           //9
            VideoColumns.BUCKET_ID,          //10
            VideoColumns.SIZE,               //11
            VideoColumns.RESOLUTION,         //12
            GalleryConstant.NO_COLUMN,       //13
    };
    //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
    private static final int INDEX_DRM_METHOD = 14;//fengke change
    static final String[] PROJECTIONDRM_MTK = {
            VideoColumns._ID,               //0
            VideoColumns.TITLE,             //1
            VideoColumns.MIME_TYPE,         //2
            VideoColumns.LATITUDE,          //3
            VideoColumns.LONGITUDE,         //4
            VideoColumns.DATE_TAKEN,        //5
            VideoColumns.DATE_ADDED,        //6
            VideoColumns.DATE_MODIFIED,     //7
            VideoColumns.DATA,              //8
            VideoColumns.DURATION,          //9
            VideoColumns.BUCKET_ID,         //10
            VideoColumns.SIZE,              //11
            VideoColumns.RESOLUTION,        //12
            GalleryConstant.NO_COLUMN,      //13
            DrmManager.TCT_IS_DRM,          //14
            DrmManager.TCT_DRM_METHOD,      //15
    };

    static final String[] PRIVATE_PROJECTIONDRM = new String[]{
            VideoColumns._ID,                   //0
            VideoColumns.TITLE,                 //1
            VideoColumns.MIME_TYPE,             //2
            VideoColumns.LATITUDE,              //3
            VideoColumns.LONGITUDE,             //4
            VideoColumns.DATE_TAKEN,            //5
            VideoColumns.DATE_ADDED,            //6
            VideoColumns.DATE_MODIFIED,         //7
            VideoColumns.DATA,                  //8
            VideoColumns.DURATION,              //9
            VideoColumns.BUCKET_ID,             //10
            VideoColumns.SIZE,                  //11
            VideoColumns.RESOLUTION,            //12
            GalleryConstant.IS_PRIVATE,         //13
            DrmManager.TCT_IS_DRM,              //14
            DrmManager.TCT_DRM_TYPE,            //15
            DrmManager.TCT_DRM_RIGHT_TYPE,      //16
            DrmManager.TCT_DRM_VALID,           //17
    };
    static final String[] PRIVATE_PROJECTION = new String[]{
            VideoColumns._ID,                   //0
            VideoColumns.TITLE,                 //1
            VideoColumns.MIME_TYPE,             //2
            VideoColumns.LATITUDE,              //3
            VideoColumns.LONGITUDE,             //4
            VideoColumns.DATE_TAKEN,            //5
            VideoColumns.DATE_ADDED,            //6
            VideoColumns.DATE_MODIFIED,         //7
            VideoColumns.DATA,                  //8
            VideoColumns.DURATION,              //9
            VideoColumns.BUCKET_ID,             //10
            VideoColumns.SIZE,                  //11
            VideoColumns.RESOLUTION,            //12
            GalleryConstant.IS_PRIVATE,         //13
    };

    static final String[] PRIVATE_PROJECTIONDRM_MTK = {
            VideoColumns._ID,                   //0
            VideoColumns.TITLE,                 //1
            VideoColumns.MIME_TYPE,             //2
            VideoColumns.LATITUDE,              //3
            VideoColumns.LONGITUDE,             //4
            VideoColumns.DATE_TAKEN,            //5
            VideoColumns.DATE_ADDED,            //6
            VideoColumns.DATE_MODIFIED,         //7
            VideoColumns.DATA,                  //8
            VideoColumns.DURATION,              //9
            VideoColumns.BUCKET_ID,             //10
            VideoColumns.SIZE,                  //11
            VideoColumns.RESOLUTION,            //12
            GalleryConstant.IS_PRIVATE,         //13
            DrmManager.TCT_IS_DRM,              //14
            DrmManager.TCT_DRM_METHOD,          //15
    };


    //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
    private final GalleryApp mApplication;

    public int durationInSec;

    public LocalVideo(Path path, GalleryApp application, Cursor cursor) {
        super(path, application, nextVersionNumber());
        mApplication = application;
        loadFromCursor(cursor);
        initDrm();
    }

    public LocalVideo(Path path, GalleryApp application, Cursor cursor, boolean fromMoments) {
        super(path, application, nextVersionNumber());
        mApplication = application;
        loadFromCursor(cursor, fromMoments);
        initDrm();
    }

    public LocalVideo(Path path, GalleryApp context, int id) {
        super(path, context, nextVersionNumber());
        mApplication = context;
        ContentResolver resolver = mApplication.getContentResolver();
        Uri uri = Video.Media.EXTERNAL_CONTENT_URI;
        //[BUGFIX]-Modified by TCTNJ,ye.chen, 2014-12-3,PR857779 begain
        Cursor cursor = null;
        try {

            cursor = LocalAlbum.getItemCursor(resolver, uri, getVideoProjection(), id);

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
    }

    public static String[] getVideoProjection() {
        String projection[] = null;
        if (GalleryAppImpl.sHasPrivateColumn) {
            if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
                projection = LocalVideo.PRIVATE_PROJECTIONDRM;
            } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                projection = LocalVideo.PRIVATE_PROJECTIONDRM_MTK;
            } else {
                projection = LocalVideo.PRIVATE_PROJECTION;
            }
        } else {
            if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
                projection = LocalVideo.PROJECTIONDRM;
            } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                projection = LocalVideo.PROJECTIONDRM_MTK;
            } else {
                projection = LocalVideo.PROJECTION;
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
                isSupportSetWallpaper = (isDrm == 1) && (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                        DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.WALLPAPER));
                isRightValid = (isDrm == 1) && (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                        DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.PLAY));
            }
            //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
        }
    }

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
        durationInSec = cursor.getInt(INDEX_DURATION) / 1000;
        Log.i(TAG, "loadFromCursor() durationInSec = " + durationInSec
                + " id = " + id);
        bucketId = cursor.getInt(INDEX_BUCKET_ID);
        fileSize = cursor.getLong(INDEX_SIZE);
        if (!cursor.isNull(INDEX_IS_PRIATE)) {
            mPrivate = cursor.getInt(INDEX_IS_PRIATE);
        }
        //[BUGFIX]-Modified by TCTNJ,ye.chen, 2014-12-3,PR857779 begain
        //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
        if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
            if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                isDrm = cursor.getInt(INDEX_IS_TCT_DRM);
                mTctDrmType = cursor.getInt(INDEX_TCT_DRM_TYPE);
                mTctDrmRightType = cursor.getString(INDEX_TCT_DRM_RIGHT_TYPE);
                mTctDrmRightValid = cursor.getInt(INDEX_TCT_DRM_VALID);
            }
        } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
            if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                isDrm = cursor.getInt(INDEX_IS_TCT_DRM);
                mDrmMethod = cursor.getInt(INDEX_DRM_METHOD);
                if (isDrm == 1) {
                    mTctDrmType = DrmManager.getInstance().getDrmScheme(filePath);
                    if (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                            DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.PLAY)) {
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
        parseResolution(cursor.getString(INDEX_RESOLUTION));
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
            durationInSec = cursor.getInt(MomentsNewAlbum.INDEX_DURATION) / 1000;
            Log.i(TAG, "loadFromCursor() durationInSec = " + durationInSec
                    + " id = " + id);
            bucketId = cursor.getInt(MomentsNewAlbum.INDEX_BUCKET_ID);
            fileSize = cursor.getLong(MomentsNewAlbum.INDEX_SIZE);
            if (!cursor.isNull(MomentsNewAlbum.INDEX_IS_PRIVATE)) {
                mPrivate = cursor.getInt(MomentsNewAlbum.INDEX_IS_PRIVATE);
            }
            //[BUGFIX]-Modified by TCTNJ,ye.chen, 2014-12-3,PR857779 begain
            //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
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
                                DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.PLAY)) {
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
            parseResolution(cursor.getString(MomentsNewAlbum.INDEX_RESOLUTION));
        } else {
            loadFromCursor(cursor);
        }
    }

    private void parseResolution(String resolution) {
        if (resolution == null) return;
        int m = resolution.indexOf('x');
        if (m == -1) return;
        try {
            int w = Integer.parseInt(resolution.substring(0, m));
            int h = Integer.parseInt(resolution.substring(m + 1));
            width = w;
            height = h;
        } catch (Throwable t) {
            Log.w(TAG, t);
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
        int updateDurationInSec = cursor.getInt(INDEX_DURATION) / 1000;
        durationInSec = uh.update(durationInSec, updateDurationInSec);
        bucketId = uh.update(bucketId, cursor.getInt(INDEX_BUCKET_ID));
        fileSize = uh.update(fileSize, cursor.getLong(INDEX_SIZE));
        if (!cursor.isNull(INDEX_IS_PRIATE)) {
            mPrivate = cursor.getInt(INDEX_IS_PRIATE);
        }
        if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
            if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                isDrm = uh.update(isDrm, cursor.getInt(INDEX_IS_TCT_DRM));
                mTctDrmType = uh.update(mTctDrmType, cursor.getInt(INDEX_TCT_DRM_TYPE));
                mTctDrmRightType = uh.update(mTctDrmRightType, cursor.getString(INDEX_TCT_DRM_RIGHT_TYPE));
                mTctDrmRightValid = uh.update(mTctDrmRightValid, cursor.getInt(INDEX_TCT_DRM_VALID));
            }
        } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
            if (cursor.getColumnIndex(DrmManager.TCT_IS_DRM) != -1) {
                isDrm = uh.update(isDrm, cursor.getInt(INDEX_IS_TCT_DRM));
                mDrmMethod = uh.update(mDrmMethod, cursor.getInt(INDEX_DRM_METHOD));
                if (isDrm == 1) {
                    mTctDrmType = DrmManager.getInstance().getDrmScheme(filePath);
                    if (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                            DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.PLAY)) {
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
            int updateDurationInSec = cursor.getInt(MomentsNewAlbum.INDEX_DURATION) / 1000;
            durationInSec = uh.update(durationInSec, updateDurationInSec);
            bucketId = uh.update(bucketId, cursor.getInt(MomentsNewAlbum.INDEX_BUCKET_ID));
            fileSize = uh.update(fileSize, cursor.getLong(MomentsNewAlbum.INDEX_SIZE));
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
                        if (MtkDrmManager.RightsStatus.RIGHTS_VALID == DrmManager.getInstance().checkRightsStatus(filePath, MtkDrmManager.Action.PLAY)) {
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
        return new LocalVideoRequest(mApplication, getPath(), dateModifiedInSec,
                type, filePath);
    }

    public static class LocalVideoRequest extends ImageCacheRequest {
        private String mLocalFilePath;

        LocalVideoRequest(GalleryApp application, Path path, long timeModified,
                          int type, String localFilePath) {
            super(application, path, timeModified, type,
                    MediaItem.getTargetSize(type));
            mLocalFilePath = localFilePath;
        }

        @Override
        public Bitmap onDecodeOriginal(JobContext jc, int type) {
            //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
            if (DrmManager.isDrmEnable) {
                MediaObject item = mPath.getObject();
                Bitmap mBitmap = null;
                if (item.isDrm() == 1) {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    try {
                        retriever.setDataSource(mLocalFilePath);
                        mBitmap = retriever.getFrameAtTime(0);
                    } catch (RuntimeException e) {
                    }
                    return DrmManager.getInstance().getDrmVideoThumbnail(mBitmap, mLocalFilePath, 108);
                }
            }
            //[FEATURE]-Add-END by ye.chen
            Bitmap bitmap = BitmapUtils.createVideoThumbnail(mLocalFilePath);
            if (bitmap == null || jc.isCancelled()) return null;
            return bitmap;
        }
    }

    @Override
    public Job<BitmapRegionDecoder> requestLargeImage() {
        throw new UnsupportedOperationException("Cannot regquest a large image"
                + " to a local video!");
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 begin
    @Override
    public Job<Bitmap> requestMomentsImage(int targetWidth, int targetHeight) {
        return new MomentsVideoRequest(targetWidth, targetHeight,
                mApplication, filePath, dateModifiedInSec, mPath);
    }

    public static class MomentsVideoRequest implements Job<Bitmap> {
        private int mTargetWidth;
        private int mTargetHeight;
        private GalleryApp mApplication;
        private String mLocalFilePath;
        private long mDateModifiedInSec;
        private Path mPath;

        public MomentsVideoRequest(int targetWidth, int targetHeight,
                                   GalleryApp application, String filePath, long dateModifiedInSec, Path path) {
            mTargetWidth = targetWidth;
            mTargetHeight = targetHeight;
            mApplication = application;
            mLocalFilePath = filePath;
            mDateModifiedInSec = dateModifiedInSec;
            mPath = path;
        }

        @Override
        public Bitmap run(JobContext jc) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            MediaObject item = mPath.getObject();
            Bitmap bitmap = null;

            bitmap = BitmapUtils.createVideoThumbnail(mLocalFilePath);
            boolean isDrm = false;
            if (DrmManager.isDrmEnable) {
                if (item.isDrm() == 1) {
                    isDrm = true;
                    int bitmapSize = bitmap.getWidth() > bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
                    bitmapSize = (bitmapSize > 0 && bitmapSize < DRM_MOMENTS_THUMBNAIL_SIZE) ? bitmapSize
                            : DRM_MOMENTS_THUMBNAIL_SIZE;
                    Bitmap tmp = DrmManager.getInstance().getDrmVideoThumbnail(bitmap, mLocalFilePath, bitmapSize);
                    bitmap.recycle();
                    bitmap = tmp;
                    Log.i(TAG, "item is drm result:" + bitmap);
                }
            }
            bitmap = BitmapUtils.resizeAndCropCenter(bitmap, mTargetWidth, mTargetHeight, 0, true, isDrm);
            return bitmap;
        }
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 end

    @Override
    public int getSupportedOperations() {
//        return SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_PLAY | SUPPORT_INFO | SUPPORT_TRIM | SUPPORT_MUTE;
        //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-09,BUG-2208330*/
        int operation = SUPPORT_MOVE | SUPPORT_COPY;
        if (isDrm() != 1) {
            //[BUGFIX]-Modified by TCTNJ,qiang.ding1, 2014-11-28,PR839194 begain
            Log.e(TAG, "LocalVideo mime type" + this.getMimeType());
            operation |= SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_PLAY | SUPPORT_INFO
                    | SUPPORT_TRIM | SUPPORT_MUTE | SUPPORT_FAVOURITE | SUPPORT_EDIT | SUPPORT_PRIVATE;
            if ("video/mp4".equals(this.getMimeType())) {
//                operation |= this.SUPPORT_MIX_VIDEO;
            }
            return operation;
            //[BUGFIX]-Modified by TCTNJ,qiang.ding1, 2014-11-28,PR839194 end
        } else {
            operation |= SUPPORT_DELETE | SUPPORT_PLAY | SUPPORT_INFO | SUPPORT_FAVOURITE | SUPPORT_PRIVATE;
            /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
            if (isSupportForward()) operation |= SUPPORT_SHARE;
            return operation;
        }
        //[FEATURE]-Add-END by ye.chen
    }

    @Override
    public void delete() {
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/03/15,PR1719169 begin
        if (isFavorite()) {
            super.delete();
        }
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/03/15,PR1719169 end
        GalleryUtils.assertNotInRenderThread();
        Log.i(TAG, "delete video id:" + id);
        Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;
        mApplication.getContentResolver().delete(baseUri, "_id=?",
                new String[]{String.valueOf(id)});
    }

    @Override
    public void copy(String objectPath) {
        GalleryUtils.assertNotInRenderThread();
        String copyGoalFile = null;
        ContentResolver contentResolver = mApplication.getContentResolver();
        final String[] fullPath = new String[1];
        String[] queryProjection = new String[]{VideoColumns.DATA};
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
            copyGoalFile = SaveImage.copyFile(fromFile, objectPath, this);
            setCopyGoalPath(copyGoalFile);
        }
    }

    public String mCopyGoalFile;

    public void setCopyGoalPath(String copyGoalFile) {
        mCopyGoalFile = copyGoalFile;
    }

    public String getGoalFile() {
        return mCopyGoalFile;
    }

    @Override
    public void move(String objectPath) {
        GalleryUtils.assertNotInRenderThread();
        Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;
        String copyGoalPath = null;
        ContentResolver contentResolver = mApplication.getContentResolver();
        final String[] fullPath = new String[1];
        String[] queryProjection = new String[]{VideoColumns.DATA};
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
            copyGoalPath = SaveImage.copyFile(fromFile, objectPath, this);
            setCopyGoalPath(copyGoalPath);
            if (copyGoalPath != null) {
                mApplication.getContentResolver().delete(baseUri, "_id=?", new String[]{String.valueOf(id)});
            }
        }
    }

    @Override
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
    public void rotate(int degrees) {
        // TODO
    }

    @Override
    public Uri getContentUri() {
        Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;
        return baseUri.buildUpon().appendPath(String.valueOf(id)).build();
    }

    @Override
    public Uri getPlayUri() {
        return getContentUri();
    }

    @Override
    public int getMediaType() {
        return MEDIA_TYPE_VIDEO;
    }

    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
        if (isDrm() == 1) {
            setDrmPropertyDialog(mApplication.getAndroidContext(), details, filePath, DrmStore.Action.PLAY);
        }
        //[FEATURE]-Add-END by ye.chen
        int s = durationInSec;
        if (s >= 0) {    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-11,PR1016732 // MODIFIED by caihong.gu-nb, 2016-04-27,BUG-1992251
            details.addDetail(MediaDetails.INDEX_DURATION, GalleryUtils.formatDuration(
                    mApplication.getAndroidContext(), durationInSec));
        }
        return details;
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

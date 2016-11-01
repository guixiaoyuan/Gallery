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
/* 04/01/2015|ye.chen               |PR916400              |[GenericApp][Gallery]MTK DRM adaptation
/* ----------|----------------------|----------------------|----------------- */
/* 04/22/2015| jian.pan1            | CR979742             |[5.0][Gallery] picture detail should show "Date taken" instead of current "Time"
/* ----------|----------------------|----------------------|----------------- */
/* 23/04/2015 |    jialiang.ren     |      PR-979658       |[5.0][Gallery] photo sorting method*/
/*------------|---------------------|----------------------|-----------------------------------*/
/* 07/09/2015|dongliang.feng        |PR1080140             |[UE][Gallery]The time display wrong */
/* ----------|----------------------|----------------------|----------------- */
/* 03/05/2016| jian.pan1            |[ALM]Defect:1550574   |[GAPP][Android6.0][Gallery]The key icon of DRM picture doesn't display in Moments interface.
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mtk.drm.frameworks.MtkDrmManager;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.db.DataBaseManager;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.util.GalleryUtils;

import android.database.Cursor;
import android.provider.MediaStore.Files.FileColumns;

//
// LocalMediaItem is an abstract class captures those common fields
// in LocalImage and LocalVideo.
//
public abstract class LocalMediaItem extends MediaItem {

    @SuppressWarnings("unused")
    private static final String TAG = "LocalMediaItem";

    // database fields
    public int id;
    public String caption;
    public String mimeType;
    public long fileSize;
    public double latitude = INVALID_LATLNG;
    public double longitude = INVALID_LATLNG;
    public long dateTakenInMs;
    public long dateAddedInSec;
    public long dateModifiedInSec;
    public String filePath;
    public int bucketId;
    public int width;
    public int height;
    public String captureMode; //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-01-09, FR898833
    public static final int DRM_MOMENTS_THUMBNAIL_SIZE = 400;// [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-05,Defect:1550574

    private DataBaseManager mDataBaseManager = null;

    public boolean mIsFavorite = false;

    public boolean isFavorite() {
        mIsFavorite = mDataBaseManager.isFavorite(id);
        return mIsFavorite;
    }

    public void toogleFavorite() {
        mIsFavorite = !mIsFavorite;
        if(mIsFavorite) {
            int mediaType = getMediaType() == MediaObject.MEDIA_TYPE_VIDEO ? FileColumns.MEDIA_TYPE_VIDEO : FileColumns.MEDIA_TYPE_IMAGE;
            mDataBaseManager.insertDataOfFavourite(String.valueOf(this.id), mediaType);
        } else {
            mDataBaseManager.deleteDataOfFavourite(String.valueOf(this.id));
        }
    }

    public LocalMediaItem(Path path, GalleryApp application, long version) {
        super(path, version);
        mDataBaseManager = application.getDataBaseManager();
    }

    @Override
    public long getDateInMs() {
        return dateTakenInMs;
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-23,PR979658 begin
    @Override
    public long getDateModifiedInMs() {
        return dateModifiedInSec * 1000;
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-23,PR979658 end

    @Override
    public String getName() {
        return caption;
    }

    @Override
    public void getLatLong(double[] latLong) {
        latLong[0] = latitude;
        latLong[1] = longitude;
    }
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400
    public static void updateDrmRight(MediaObject item) {
        if (item instanceof LocalImage) {
            LocalMediaItem drmItem = (LocalMediaItem)item;
            if (DrmManager.isDrmEnable) {
                drmItem.isRightValid = (drmItem.isDrm == 1) && (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                DrmManager.getInstance().checkRightsStatus(drmItem.filePath,MtkDrmManager.Action.DISPLAY));
                Log.w(TAG, "DRM updateDrmRight1 drmItem.isRightValid: " + drmItem.isRightValid);
            }
        } else if (item instanceof LocalVideo) {
            LocalMediaItem drmItem = (LocalMediaItem)item;
            if (DrmManager.isDrmEnable) {
               drmItem.isRightValid = (drmItem.isDrm == 1) && (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
               DrmManager.getInstance().checkRightsStatus(drmItem.filePath,MtkDrmManager.Action.PLAY));
               Log.w(TAG, "DRM updateDrmRight2 drmItem.isRightValid: " + drmItem.isRightValid);
            }

        }
    }

    protected abstract boolean updateFromCursor(Cursor cursor);

    public int getBucketId() {
        return bucketId;
    }

    protected void updateContent(Cursor cursor) {
        if (updateFromCursor(cursor)) {
            mDataVersion = nextVersionNumber();
        }
    }

    // Update the item data from cursor and use the moments album index.
    protected abstract boolean updateFromCursor(Cursor cursor, boolean fromMoments);

    protected void updateContent(Cursor cursor, boolean fromMoments) {
        if (updateFromCursor(cursor, fromMoments)) {
            mDataVersion = nextVersionNumber();
        }
    }

    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(MediaDetails.INDEX_PATH, filePath);
        details.addDetail(MediaDetails.INDEX_TITLE, caption);
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-07, PR1080140 begin
        if (GalleryUtils.getSystemDateFormat() != null) {
            Date date = new Date(dateModifiedInSec * 1000);
            String time = null;
            String dateString = GalleryUtils.getSystemDateFormat().format(date);
            String timeString = null;
            if (GalleryUtils.getSystemIs24HourFormat()) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                timeString = timeFormat.format(date);
            } else {
                SimpleDateFormat formater = new SimpleDateFormat("hh:mm:ss a");
                timeString = formater.format(date);
            }
            time = dateString + " " + timeString;
            details.addDetail(MediaDetails.INDEX_MODIFIED, time);
        } else {
            DateFormat formater = DateFormat.getDateTimeInstance();
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-22,CR979742 begin
             details.addDetail(MediaDetails.INDEX_MODIFIED,
                     formater.format(new Date(dateModifiedInSec * 1000)));
             //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-22,CR979742 end
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-07, PR1080140 end
        details.addDetail(MediaDetails.INDEX_WIDTH, width);
        details.addDetail(MediaDetails.INDEX_HEIGHT, height);

        if (GalleryUtils.isValidLocation(latitude, longitude)) {
            details.addDetail(MediaDetails.INDEX_LOCATION, new double[] {latitude, longitude});
        }
        if (fileSize > 0) details.addDetail(MediaDetails.INDEX_SIZE, fileSize);
        return details;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public long getSize() {
        return fileSize;
    }

    @Override
    public void delete() {
        mDataBaseManager.deleteDataOfFavourite(String.valueOf(this.id));
    }
}

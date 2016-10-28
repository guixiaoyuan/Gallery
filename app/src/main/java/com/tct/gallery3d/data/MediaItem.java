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
/* 23/04/2015|    jialiang.ren      |PR-979658             |[5.0][Gallery] photo sorting method*/
/*-----------|----------------------|----------------------|-----------------------------------*/
/* 14/10/2015|chengbin.du-nb        |ALM-676093            |[Android 5.1][Gallery_v5.2.2.1.1.0305.0]The response time of switching Month view to Day view is too long.*/
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.data;

import android.content.ContentValues;
import android.content.Context;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.filtershow.cache.ImageLoader;
import com.tct.gallery3d.util.ThreadPool.Job;
import com.tct.gallery3d.util.ThreadPool.JobContext;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

// MediaItem represents an image or a video item.
public abstract class MediaItem extends MediaObject {
    // NOTE: These type numbers are stored in the image cache, so it should not
    // not be changed without resetting the cache.
    public static final int TYPE_THUMBNAIL = 1;
    public static final int TYPE_MICROTHUMBNAIL = 2;
    public static final int TYPE_MOMENTSTHUMBNAIL = 3;

    public static final int CACHED_IMAGE_QUALITY = 95;

    public static final int IMAGE_READY = 0;
    public static final int IMAGE_WAIT = 1;
    public static final int IMAGE_ERROR = -1;

    public static final String MIME_TYPE_JPEG = "image/jpeg";

    private static final int BYTESBUFFE_POOL_SIZE = 4;
    private static final int BYTESBUFFER_SIZE = 200 * 1024;

    private static int sMicrothumbnailTargetSize = 200;
    private static final BytesBufferPool sMicroThumbBufferPool =
            new BytesBufferPool(BYTESBUFFE_POOL_SIZE, BYTESBUFFER_SIZE);

    private static int sThumbnailTargetSize = 640;

    // TODO: fix default value for latlng and change this.
    public static final double INVALID_LATLNG = 0f;

    public abstract Job<Bitmap> requestImage(int type);
    public abstract Job<BitmapRegionDecoder> requestLargeImage();
    public abstract Job<Bitmap> requestMomentsImage(int targetWidth, int targetHeight);//[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093

    // [FEATURE]-Add-BEGIN by NJHR.chengqun.sun For PR826943——2014.11.04
    public static final String MIME_TYPE_GIF = "image/gif";
    // [FEATURE]-Add-END by NJHR.chengqun.sun For PR826943——2014.11.04

  //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
    boolean isDrmEnable;
    int isDrm;
    boolean isSupportSetWallpaper;
    boolean isSupportForward;
    boolean isRightValid;
    int mTctDrmType;
    String mTctDrmRightType;
    int mTctDrmRightValid;
    //[FEATURE]-Add-END by ye.chen
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400
    int mDrmMethod;
    public boolean isConsume = false;
    public boolean enteredConsumeMode = false;
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400
    public MediaItem(Path path, long version) {
        super(path, version);
        isDrmEnable = DrmManager.isDrmEnable;//[FEATURE]-Add by ye.chen,11/10/2014,support drm
    }

    public long getDateInMs() {
        return 0;
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-23,PR979658 begin
    public long getDateModifiedInMs() {
        return 0;
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-23,PR979658 end

    public String getName() {
        return null;
    }

  //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
    public int isDrm(){
        return isDrm;
    }

    public boolean isDrmEnable(){
        return isDrmEnable;
    }

    public boolean isSupportSetWallpaper(){
        return isSupportSetWallpaper;
    }
    public boolean isSupportForward(){
        return isSupportForward;
    }

    public boolean isRightValid(){
        return isRightValid;
    }

    public int mTctDrmType(){
        return mTctDrmType;
    }

    public String mTctDrmRightType(){
        return "";
    }

    public int isTctDrmRightValid(){
        return mTctDrmRightValid;
    }

    public void getLatLong(double[] latLong) {
        latLong[0] = INVALID_LATLNG;
        latLong[1] = INVALID_LATLNG;
    }

    public String[] getTags() {
        return null;
    }

    public Face[] getFaces() {
        return null;
    }

    // The rotation of the full-resolution image. By default, it returns the value of
    // getRotation().
    public int getFullImageRotation() {
        return getRotation();
    }

    public int getRotation() {
        return 0;
    }

    public long getSize() {
        return 0;
    }

    public abstract String getMimeType();

    public String getFilePath() {
        return "";
    }

  //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
    protected void setDrmPropertyDialog(Context mContext, MediaDetails details,String path,int action) {
      //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
        if(DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM){
            int schema = DrmManager.getInstance().getDrmScheme(path);
            if (schema ==DrmManager.DRM_SCHEME_OMA1_FL) {
                details.addDetail(MediaDetails.INDEX_DRM_RIGHT, mContext.getText(R.string.unlimited_usage));
            } else if (schema == DrmManager.DRM_SCHEME_OMA1_CD
                    || schema == DrmManager.DRM_SCHEME_OMA1_SD) {
                if (schema == DrmManager.DRM_SCHEME_OMA1_SD) {
                    ContentValues contentValue = DrmManager.getInstance().getMetadata(path);
                    if (contentValue != null) {
                        details.addDetail(MediaDetails.INDEX_DRM_RIGHT_ISSUER_TEXT, contentValue
                                .getAsString(DrmManager.RIGHTS_ISSUER));
                        details.addDetail(MediaDetails.INDEX_DRM_VENDOR_URL_TEXT, contentValue
                                .getAsString(DrmManager.CONTENT_VENDOR));
                    }
                }
                if (!DrmManager.getInstance().isRightsStatus(path)) {
                    details.addDetail(MediaDetails.INDEX_DRM_RIGHT, mContext.getText(R.string.not_available));
                    return;
                }
                ContentValues cv = DrmManager.getInstance().getConstraints(path, action);
                if (null != cv) {
                    String constrainType = cv.getAsString(DrmManager.CONSTRAINT_TYPE);
                    if (null == constrainType) {
                        details.addDetail(MediaDetails.INDEX_DRM_RIGHT, mContext.getText(R.string.not_available));
                        return;
                    }
                    if (constrainType.equalsIgnoreCase("count")) {
                        String useTime = null;
                        try {
                            int times = Integer.parseInt
                                    (cv.getAsString(DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT));
                            useTime = String.format(
                                    mContext.getString(R.string.use_times),
                                    "" + times);
                        } catch (Exception e) {
                        }
                        if (null != useTime) {
                            details.addDetail(MediaDetails.INDEX_DRM_REMAINING_REPEAT_COUNT, useTime);
                        } else {
                            details.addDetail(MediaDetails.INDEX_DRM_REMAINING_REPEAT_COUNT, mContext.getText(R.string.not_available));
                        }
                    } else if (constrainType.equalsIgnoreCase("datetime")) {
                        String startTime = mContext
                                .getString(R.string.valid_after)
                                + " "
                                + cv.getAsString(DrmStore.ConstraintsColumns.LICENSE_START_TIME);
                        String endTime = mContext.getString(R.string.valid_until)
                                + " " + cv.getAsString(DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME);
                        if (null == startTime || startTime.length() == 0) {
                            startTime = mContext.getString(R.string.unlimited_usage);
                        }
                        if (null != endTime && endTime.length() != 0) {
                            startTime += "\n"+endTime;
                        } else {
                            startTime += "\n"+mContext.getString(R.string.unlimited_usage);
                        }
                        details.addDetail(MediaDetails.INDEX_DRM_DATETIME_STARTTIME, startTime);
                    } else if (constrainType.equalsIgnoreCase("interval")) {
                        String interval = cv
                                .getAsString(DrmStore.ConstraintsColumns.LICENSE_AVAILABLE_TIME);
                        int line_index = interval.indexOf("-");
                        String year = interval.substring(0, line_index);
                        line_index = interval.indexOf("-", line_index + 1);
                        String month = interval.substring(line_index - 2, line_index);
                        String day = interval.substring(line_index + 1, line_index + 3);
                        int colon_index = interval.indexOf(":");
                        String hour = interval.substring(colon_index - 2, colon_index);
                        colon_index = interval.indexOf(":", colon_index + 1);
                        String minute = interval.substring(colon_index - 2, colon_index);
                        String second = interval.substring(colon_index + 1, colon_index + 3);
                        year = (year.equalsIgnoreCase("0000") ? ""
                                : ("" + Integer.parseInt(year) + "Year-"));
                        month = (month.equalsIgnoreCase("00") ? ""
                                : ("" + Integer.parseInt(month) + "Month-"));
                        day = (day.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(day) + "Day "));
                        hour = (hour.equalsIgnoreCase("00") ? ""
                                : ("" + Integer.parseInt(hour) + "Hour "));
                        minute = (minute.equalsIgnoreCase("00") ? ""
                                : ("" + Integer.parseInt(minute) + "Minute "));
                        second = (second.equalsIgnoreCase("00") ? ""
                                : ("" + Integer.parseInt(second) + "Second"));
                        interval = "Valid for "+year + month + day + hour + minute + second+" after first time use";
                        if (null != interval && interval.length() != 0) {
                            details.addDetail(MediaDetails.INDEX_DRM_INTERVAL, interval);
                        } else {
                            details.addDetail(MediaDetails.INDEX_DRM_INTERVAL, mContext.getText(R.string.not_available));
                        }
                    }
                }
            }
        }else{
            boolean isRightValid = DrmManager.getInstance().isRightsStatus(path);
            try {
                boolean rightsStatus = DrmManager.getInstance().canTransfer(path);
                if (rightsStatus == true) {
                    details.addDetail(MediaDetails.INDEX_DRM_RIGHT, mContext.getText(R.string.drm_can_forward));
                } else{
                    details.addDetail(MediaDetails.INDEX_DRM_RIGHT, mContext.getText(R.string.drm_can_not_forward));
                }
                ContentValues values = DrmManager.getInstance().getConstraints(path, action);
                if (values == null || values.size() == 0) {
                    details.addDetail(MediaDetails.INDEX_DRM_RIGHT, mContext.getText(R.string.drm_no_license));
                } else {
                    if (values.containsKey(DrmStore.ConstraintsColumns.LICENSE_START_TIME)) {
                        Long startL = values.getAsLong(DrmStore.ConstraintsColumns.LICENSE_START_TIME);
                        if (startL != null) {
                            if (startL == -1) {
                                details.addDetail(MediaDetails.INDEX_DRM_DATETIME_STARTTIME, mContext.getText(R.string.drm_no_limitation));
                            } else {
                                details.addDetail(MediaDetails.INDEX_DRM_DATETIME_STARTTIME, toDateTimeString(startL));
                            }
                        } else {
                        }
                    } else {
                        details.addDetail(MediaDetails.INDEX_DRM_DATETIME_STARTTIME, mContext.getText(R.string.drm_no_limitation));
                    }
                    if (values.containsKey(DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME)) {
                        Long endL = values.getAsLong(DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME);
                        if (endL != null) {
                            if (endL == -1) {
                                details.addDetail(MediaDetails.INDEX_DRM_DATETIME_ENDTIME, mContext.getText(R.string.drm_no_limitation));
                            } else {
                                details.addDetail(MediaDetails.INDEX_DRM_DATETIME_ENDTIME, toDateTimeString(endL));
                            }
                        } else {
                        }
                    } else {
                        details.addDetail(MediaDetails.INDEX_DRM_DATETIME_ENDTIME, mContext.getText(R.string.drm_no_limitation));
                    }
                    if (values.containsKey(DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT)
                        && values.containsKey(DrmStore.ConstraintsColumns.MAX_REPEAT_COUNT)) {
                        Long remainCount = values.getAsLong(DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT);
                        Long maxCount = values.getAsLong(DrmStore.ConstraintsColumns.MAX_REPEAT_COUNT);
                        if (remainCount != null &&  maxCount != null) {
                            if (remainCount == -1 || maxCount == -1) {
                                details.addDetail(MediaDetails.INDEX_DRM_REMAINING_REPEAT_COUNT, mContext.getText(R.string.drm_no_limitation));
                            } else {
                                details.addDetail(MediaDetails.INDEX_DRM_REMAINING_REPEAT_COUNT, remainCount.toString() + "/" + maxCount.toString());
                            }
                        }
                    } else {
                        details.addDetail(MediaDetails.INDEX_DRM_REMAINING_REPEAT_COUNT, mContext.getText(R.string.drm_no_limitation));
                    }
                }
            } catch (Exception e) {
            }
        }//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
    }
    private String toDateTimeString(Long sec) {
        Date date = new Date(sec.longValue() * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        String str = dateFormat.format(date);
        return str;
    }
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400
    protected static Bitmap getDrmThumbnails(JobContext jc,Context context,String path){
        return createDrmThumbnails(jc, context,DrmManager.getInstance().isRightsStatus(path), path, null);
    }

    protected static Bitmap getDrmThumbnails(JobContext jc,Context context,Uri uri){
        String path = ImageLoader.getLocalPathFromUri(context, uri);
        return createDrmThumbnails(jc, context,DrmManager.getInstance().isRightsStatus(path), path, null);
    }

    protected static Bitmap getDrmThumbnails(JobContext jc,Context context,String path, MediaItem item){
        return createDrmThumbnails(jc, context,DrmManager.getInstance().isRightsStatus(path), path,item);
    }

    protected static Bitmap getDrmThumbnails(JobContext jc,Context context,Uri uri, MediaItem item){
        String path = ImageLoader.getLocalPathFromUri(context, uri);
        return createDrmThumbnails(jc, context,DrmManager.getInstance().isRightsStatus(path), path, item);
    }

    private static Bitmap createDrmThumbnails(JobContext jc,Context context,boolean valid, String path, MediaItem item) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        byte[] b;
        InputStream input = null;
        try {
            Bitmap bitmap = null;
            if (valid) {
                if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM && item != null &&
                    (item.enteredConsumeMode == true || (item instanceof LocalVideo) || item.mTctDrmType == DrmManager.DRM_SCHEME_OMA1_FL)) {
                    if (item.isConsume == false) {
                        bitmap = DrmManager.getInstance().getThumbnailConsume(path);
                        item.isConsume = true;
                    } else {
                        bitmap = DrmManager.getInstance().getDrmThumbnail(path,640);
                    }
                    Log.w("MediaItem", "DRM PhotoPage createDrmThumbnails............... bitmap1 =" + bitmap);
                }
                if (bitmap == null) {
                    Log.w("MediaItem", "DRM  createDrmThumbnails 2");
                    System.out.println("unlock");
                    input = context.getResources().openRawResource(R.drawable.drm_thumbnail_unlock);
                    b = new byte[input.available()];
                    input.read(b);
                    bitmap =  DecodeUtils.requestDecode(jc, b, options);
                }
            } else {
                System.out.println("locked");
                input = context.getResources().openRawResource(R.drawable.drm_thumbnail_locked);
                b = new byte[input.available()];
                input.read(b);
                bitmap =  DecodeUtils.requestDecode(jc, b, options);
            }
            if(input != null) input.close();
            return bitmap;
        } catch (IOException e) {
        }
        return null;
    }
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400
    public boolean isAvlid(Context context,String filePath){
        return DrmManager.getInstance().isRightsStatus(filePath);
    }

    public boolean isAvlid(Context context,Uri uri){
        return DrmManager.getInstance().isRightsStatus(ImageLoader.getLocalPathFromUri(context, uri));
    }

    private static Bitmap createDrmThumbnails(JobContext jc,Context context,boolean valid){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        byte[] b;
        InputStream input;
        try {
            Bitmap bitmap = null;
            if (valid) {
                System.out.println("unlock");
                input = context.getResources().openRawResource(R.drawable.drm_thumbnail_unlock);
                b = new byte[input.available()];
                input.read(b);
                bitmap =  DecodeUtils.requestDecode(jc, b, options);
            } else {
                System.out.println("locked");
                input = context.getResources().openRawResource(R.drawable.drm_thumbnail_locked);
                b = new byte[input.available()];
                input.read(b);
                bitmap =  DecodeUtils.requestDecode(jc, b, options);
            }
            return bitmap;
        } catch (IOException e) {
        }
        return null;
    }
  //[FEATURE]-Add-END by ye.chen
    // Returns width and height of the media item.
    // Returns 0, 0 if the information is not available.
    public abstract int getWidth();
    public abstract int getHeight();

    public static int getTargetSize(int type) {
        switch (type) {
            case TYPE_THUMBNAIL:
                return sThumbnailTargetSize;
            case TYPE_MICROTHUMBNAIL:
                return sMicrothumbnailTargetSize;
            default:
                throw new RuntimeException(
                    "should only request thumb/microthumb from cache");
        }
    }

    public static BytesBufferPool getBytesBufferPool() {
        return sMicroThumbBufferPool;
    }

    public static void setThumbnailSizes(int size, int microSize) {
        sThumbnailTargetSize = size;
        if (sMicrothumbnailTargetSize != microSize) {
            sMicrothumbnailTargetSize = microSize;
        }
    }

    @Override
    public String toString() {
        Uri uri = getContentUri();
        if (uri != null) return uri.toString();
        String filepath = getFilePath();
        if (filepath != null && !filepath.equals("")) return filepath;
        return getPath().getSuffix();
    }
}

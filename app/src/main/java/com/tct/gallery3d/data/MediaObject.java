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
/* 20/11/2014|chengqun.sun            |FR826631              |Multi screen interaction*/
/* ----------|----------------------|----------------------|----------------- */
/* 14/08/2015|dongliang.feng        |PR1068133             |[Android5.1][Gallery_v5.2.0.1.1.0303.0]Gif picture can not slide show */
/*-----------|-------------- -------|----------------------|----------------------------------------*/

package com.tct.gallery3d.data;

import android.net.Uri;

import com.tct.gallery3d.app.constant.GalleryConstant;

public abstract class MediaObject {
    private static final String TAG = "MediaObject";
    public static final long INVALID_DATA_VERSION = -1;

    // These are the bits returned from getSupportedOperations():
    public static final int SUPPORT_DELETE = 1 << 0;
    public static final int SUPPORT_ROTATE = 1 << 1;
    public static final int SUPPORT_SHARE = 1 << 2;
    public static final int SUPPORT_CROP = 1 << 3;
    public static final int SUPPORT_SHOW_ON_MAP = 1 << 4;
    public static final int SUPPORT_SETAS = 1 << 5;
    public static final int SUPPORT_FULL_IMAGE = 1 << 6;
    public static final int SUPPORT_PLAY = 1 << 7;
    public static final int SUPPORT_CACHE = 1 << 8;
    public static final int SUPPORT_EDIT = 1 << 9;
    public static final int SUPPORT_INFO = 1 << 10;
    public static final int SUPPORT_TRIM = 1 << 11;
    public static final int SUPPORT_UNLOCK = 1 << 12;
    public static final int SUPPORT_BACK = 1 << 13;
    public static final int SUPPORT_ACTION = 1 << 14;
    public static final int SUPPORT_CAMERA_SHORTCUT = 1 << 15;
    public static final int SUPPORT_MUTE = 1 << 16;
    public static final int SUPPORT_PRINT = 1 << 17;
    public static final int SUPPORT_WFD_SHARE = 1 << 18;//[FEATURE]-by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    public static final int SUPPORT_MIX_VIDEO = 1 << 19;
    public static final int SUPPORT_FAVOURITE = 1 << 20;
    public static final int SUPPORT_GETMULTICONTENT = 1 << 21;
    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-09,BUG-2208330*/
    public static final int SUPPORT_MOVE = 1 << 22;
    public static final int SUPPORT_COPY = 1 << 23;
    public static final int SUPPORT_PRIVATE = 1 << 24;
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
    public static final int SUPPORT_ALL = 0xffffffff;

    // These are the bits returned from getMediaType():
    public static final int MEDIA_TYPE_UNKNOWN = 1;
    public static final int MEDIA_TYPE_IMAGE = 2;
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-14, PR1068133
    public static final int MEDIA_TYPE_GIF = 8;//[FEATURE]-Add- by ye.chen,11/10/2014,support drm
    public static final int MEDIA_TYPE_VIDEO = 4;
    public static final int MEDIA_TYPE_ALL = MEDIA_TYPE_IMAGE | MEDIA_TYPE_VIDEO; // MODIFIED by Yaoyu.Yang, 2016-08-09,BUG-2208330

    public static final String MEDIA_TYPE_IMAGE_STRING = "image";
    public static final String MEDIA_TYPE_VIDEO_STRING = "video";
    public static final String MEDIA_TYPE_ALL_STRING = "all";
    public static final String MEDIA_TYPE_SELECT_STRING = "select";
    public static final String MEDIA_TYPE_COLLAPSED_STRING = "collapsed";
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    // These are flags for cache() and return values for getCacheFlag():
    public static final int CACHE_FLAG_NO = 0;
    public static final int CACHE_FLAG_SCREENNAIL = 1;
    public static final int CACHE_FLAG_FULL = 2;

    // These are return values for getCacheStatus():
    public static final int CACHE_STATUS_NOT_CACHED = 0;
    public static final int CACHE_STATUS_CACHING = 1;
    public static final int CACHE_STATUS_CACHED_SCREENNAIL = 2;
    public static final int CACHE_STATUS_CACHED_FULL = 3;

    private static long sVersionSerial = 0;

    public int mPrivate = GalleryConstant.PUBLIC_ITEM;

    protected long mDataVersion;

    protected final Path mPath;

    private boolean mIsCancel = false;

    public interface PanoramaSupportCallback {
        void panoramaInfoAvailable(MediaObject mediaObject, boolean isPanorama,
                boolean isPanorama360);
    }

    public MediaObject(Path path, long version) {
        path.setObject(this);
        mPath = path;
        mDataVersion = version;
    }
    //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
    public int isDrm(){
        return 0;
    }

    public int isPrivate() {
        return mPrivate;
    }

    public void setPrivate(int isPrivate) {
        mPrivate = isPrivate;
    }
    public int mTctDrmType(){
        return 0;
    }
    public String mTctDrmRightType(){
        return "";
    }
    public int isTctDrmRightValid(){
        return 0;
    }
//[FEATURE]-Add-END by ye.chen
    public Path getPath() {
        return mPath;
    }

    public int getSupportedOperations() {
        return 0;
    }

    public void getPanoramaSupport(PanoramaSupportCallback callback) {
        callback.panoramaInfoAvailable(this, false, false);
    }

    public void clearCachedPanoramaSupport() {
    }

    public void delete() {
        throw new UnsupportedOperationException();
    }

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-17,BUG-2208330*/
    public void move(String objectPath) {
        throw new UnsupportedOperationException();
    }

    public void copy(String objectPath) {
        throw new UnsupportedOperationException();
    }

    public String getGoalFile() {
        return null;
    }
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    public void rotate(int degrees) {
        throw new UnsupportedOperationException();
    }

    public Uri getContentUri() {
        String className = getClass().getName();
        Log.e(TAG, "Class " + className + "should implement getContentUri.");
        Log.e(TAG, "The object was created from path: " + getPath());
        throw new UnsupportedOperationException();
    }

    public Uri getPlayUri() {
        throw new UnsupportedOperationException();
    }

    public int getMediaType() {
        return MEDIA_TYPE_UNKNOWN;
    }

    public MediaDetails getDetails() {
        MediaDetails details = new MediaDetails();
        return details;
    }

    public long getDataVersion() {
        return mDataVersion;
    }

    public int getCacheFlag() {
        return CACHE_FLAG_NO;
    }

    public int getCacheStatus() {
        throw new UnsupportedOperationException();
    }

    public long getCacheSize() {
        throw new UnsupportedOperationException();
    }

    public void cache(int flag) {
        throw new UnsupportedOperationException();
    }

    public static synchronized long nextVersionNumber() {
        return ++MediaObject.sVersionSerial;
    }

    public static int getTypeFromString(String s) {
        if (MEDIA_TYPE_ALL_STRING.equals(s)) return MediaObject.MEDIA_TYPE_ALL;
        if (MEDIA_TYPE_IMAGE_STRING.equals(s)) return MediaObject.MEDIA_TYPE_IMAGE;
        if (MEDIA_TYPE_VIDEO_STRING.equals(s)) return MediaObject.MEDIA_TYPE_VIDEO;
        if (MEDIA_TYPE_SELECT_STRING.equals(s)) return MediaObject.MEDIA_TYPE_ALL;
        if (MEDIA_TYPE_COLLAPSED_STRING.equals(s)) return MediaObject.MEDIA_TYPE_ALL;
        throw new IllegalArgumentException(s);
    }

    public static String getTypeString(int type) {
        switch (type) {
            case MEDIA_TYPE_IMAGE: return MEDIA_TYPE_IMAGE_STRING;
            case MEDIA_TYPE_VIDEO: return MEDIA_TYPE_VIDEO_STRING;
            case MEDIA_TYPE_ALL: return MEDIA_TYPE_ALL_STRING;
        }
        throw new IllegalArgumentException();
    }
  //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
    public String getFilePath(){
        return "";
    }
//[FEATURE]-Add-END by ye.chen

    public void setCancelStatus(boolean isCancel) {
        mIsCancel = isCancel;
    }

    public boolean getCancelStatus() {
        return mIsCancel;
    }
}

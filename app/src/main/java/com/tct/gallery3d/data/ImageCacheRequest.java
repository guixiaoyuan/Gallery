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
/* 19/12/2014|ye.chen               |PR870970              |[Gallery2][DRM]CD count files can be open many times
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.data;

import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.common.BitmapUtils;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.filtershow.cache.ImageLoader;
import com.tct.gallery3d.util.ThreadPool.JobContext;

import android.graphics.Bitmap;
import android.net.Uri;

public abstract class ImageCacheRequest extends ImageRequest {
    private static final String TAG = "ImageCacheRequest";

    protected volatile boolean mIsCancelled = false;
    protected GalleryApp mApplication;
    protected Path mPath; //[FEATURE]-Mod private to protected by ye.chen,11/10/2014,support drm
    private int mType;
    private int mTargetSize;
    private long mTimeModified;

    public ImageCacheRequest(GalleryApp application,
            Path path, long timeModified, int type, int targetSize) {
        mApplication = application;
        mPath = path;
        mType = type;
        mTargetSize = targetSize;
        mTimeModified = timeModified;
    }

    private String debugTag() {
        return mPath + "," + mTimeModified + "," +
                ((mType == MediaItem.TYPE_THUMBNAIL) ? "THUMB" :
                (mType == MediaItem.TYPE_MICROTHUMBNAIL) ? "MICROTHUMB" : "?");
    }

    @Override
    public Bitmap run(JobContext jc) {
        Bitmap bitmap = onDecodeOriginal(jc, mType);
        if (jc.isCancelled()) {
            return null;
        }
        if (DrmManager.isDrmEnable) {
            Uri uri = mPath.getObject().getContentUri();
            if (DrmManager.getInstance().isDrm(ImageLoader.getLocalPathFromUri(mApplication.getAndroidContext(), uri)))
                return bitmap;
        }
        if (jc.isCancelled()) {
            return null;
        }
        if (bitmap == null) {
            Log.w(TAG, "decode orig failed " + debugTag());
            return null;
        }
        bitmap = BitmapUtils.resizeDownBySideLength(bitmap, mTargetSize, true);
        if (jc.isCancelled()) {
            return null;
        }
        return bitmap;
    }

    public abstract Bitmap onDecodeOriginal(JobContext jc, int targetSize);
}

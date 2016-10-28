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
/* 21/12/2015|chengbin.du-nb        |ALM-1121296           |[Gallery]Fyuse pictures can't display in gallery after capture a Fyuse picture
/* ----------|----------------------|----------------------|----------------- */
/* 01/13/2016| jian.pan1            |[ALM]Defect:1270104   |[Android6.0][Gallery]The fyuse picture display same as normal picture
/* ----------|----------------------|----------------------|----------------- */
/* 01/14/2016| jian.pan1            |[ALM]Defect:791983    |can not delete a parallax photo
/* ----------|----------------------|----------------------|----------------- */
/* 01/18/2016| jun.xie-nb           |[ALM]Defect:958124    |Memory Leak
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.data;

import java.io.FileNotFoundException;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.fyuse.ContentConstants;
 /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1892017*/
import com.tct.gallery3d.common.BitmapUtils;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.data.BytesBufferPool.BytesBuffer;
 /*MODIFIED-END by jian.pan1,BUG-1892017*/
import com.tct.gallery3d.util.ThreadPool.Job;
import com.tct.gallery3d.util.ThreadPool.JobContext;

public class ParallaxItem extends MediaItem {
    private static final String TAG = "ParallaxItem";

    public static final Path ITEM_PATH = Path.fromString("/local/parallax/item");

    private GalleryApp mApplication;
    private Uri mUri;
     /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1892017*/
    private long mDateModifiedInSec;

    public ParallaxItem(GalleryApp application, Path path, String filePath, long dateModifiedInSec) {
        super(path, nextVersionNumber());
        mApplication = Utils.checkNotNull(application);
        mUri = Uri.parse(filePath);
        mDateModifiedInSec = dateModifiedInSec;
         /*MODIFIED-END by jian.pan1,BUG-1892017*/
    }

    @Override
    public int getSupportedOperations() {
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,Defect:1270104 begin
        return SUPPORT_DELETE | SUPPORT_SHARE;
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,Defect:1270104 end
    }

    @Override
    public int getMediaType() {
          return MEDIA_TYPE_IMAGE;
    }

    @Override
    public Uri getContentUri() {
        return mUri;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE_JPEG;
    }

    @Override
    public void delete() {
        String selection = mUri.toString();
        ContentResolver cr = mApplication.getContentResolver();
        cr.delete(Uri.parse(ContentConstants.CONTENT_URI), selection, null);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-14,Defect:791983 begin
        mApplication.getDataManager().updateFyuseView();
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-14,Defect:791983 end
    }

    @Override
    public int getWidth() {
        //TODO
        return 0;
    }

    @Override
    public int getHeight() {
        //TODO
        return 0;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Job<Bitmap> requestImage(int type) {
        return new BitmapJob(type);
    }

    private class BitmapJob extends ImageRequest {
        private int mType;

        public BitmapJob(int type) {
            mType = type;
        }

        @Override
        public Bitmap run(JobContext jc) {
             /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1892017*/
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = null;

            if (jc.isCancelled())
                return null;
            ParcelFileDescriptor parcelFd = null;
            try {
                parcelFd = mApplication.getContentResolver().openFileDescriptor(mUri, "r");
                int targetSize = MediaItem.getTargetSize(mType);
                bitmap = DecodeUtils.decodeThumbnail(jc, parcelFd.getFileDescriptor(), options, targetSize, mType);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "ParallaxItem BitmapJob", e);
            } finally {
                Utils.closeSilently(parcelFd);
            }

            return bitmap;
        }
   }

    @Override
    public Job<BitmapRegionDecoder> requestLargeImage() {
        return null;
    }

    @Override
    public Job<Bitmap> requestMomentsImage(int targetWidth, int targetHeight) {
        return new MomentsImageRequest(mPath, targetWidth, targetHeight, mApplication, mUri, mDateModifiedInSec); //MODIFIED by jian.pan1, 2016-04-05,BUG-1892017
    }

    public static class MomentsImageRequest implements Job<Bitmap> {
        private int mTargetWidth = 0;
        private int mTargetHeight = 0;
        private GalleryApp mApplication;
        private Uri mUri;
         /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1892017*/
        private Path mPath;
        private long mDateModifiedInSec;

        public MomentsImageRequest(Path path, int targetWidth, int targetHeight,
                GalleryApp application, Uri uri, long dateModifiedInSec) {
            mTargetWidth = targetWidth;
            mTargetHeight = targetHeight;
            mApplication = application;
            mUri = uri;
            mPath = path;
            mDateModifiedInSec = dateModifiedInSec;
        }

        @Override
        public Bitmap run(JobContext jc) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = null;

            if (jc.isCancelled()) return null;
              //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 begin
            ParcelFileDescriptor parcelFd = null;
            try {
                parcelFd = mApplication.getContentResolver().openFileDescriptor(mUri, "r");
                bitmap = DecodeUtils.decodeThumbnail(jc, parcelFd.getFileDescriptor(), mTargetWidth, mTargetHeight,
                        options, 0);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "ParallaxItem MomentsImageRequest", e);
            } finally {
                Utils.closeSilently(parcelFd);
            }
            return bitmap;
             /*MODIFIED-END by jian.pan1,BUG-1892017*/
        }
    }
}
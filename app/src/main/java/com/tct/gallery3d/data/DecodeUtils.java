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
/* 14/10/2015|chengbin.du-nb        |ALM-676093            |[Android 5.1][Gallery_v5.2.2.1.1.0305.0]The response time of switching Month view to Day view is too long.*/
/* ----------|----------------------|----------------------|----------------- */
/* 16/12/2015|chengbin.du-nb        |ALM-1170791           |Momments display vGallery.*/
/* ----------|----------------------|----------------------|----------------- */
/* 03/05/2016| jian.pan1            |[ALM]Defect:1550574   |[GAPP][Android6.0][Gallery]The key icon of DRM picture doesn't display in Moments interface.
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.data;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import com.android.photos.data.GalleryBitmapPool;
import com.tct.gallery3d.common.ApiHelper;
import com.tct.gallery3d.common.BitmapUtils;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.ui.Log;
import com.tct.gallery3d.util.ThreadPool.CancelListener;
import com.tct.gallery3d.util.ThreadPool.JobContext;

public class DecodeUtils {
    private static final String TAG = "DecodeUtils";
    private static final int MOMENTS_IRREGULAR_THUMBNAIL_TARGET_WIDTH = 370;// MODIFIED by dongliang.feng, 2016-03-21, BUG-1173487

    private static class DecodeCanceller implements CancelListener {
        Options mOptions;

        public DecodeCanceller(Options options) {
            mOptions = options;
        }

        @Override
        public void onCancel() {
            mOptions.requestCancelDecode();
        }
    }

    @TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
    public static void setOptionsMutable(Options options) {
        if (ApiHelper.HAS_OPTIONS_IN_MUTABLE) options.inMutable = true;
    }

    public static Bitmap decode(JobContext jc, FileDescriptor fd, Options options) {
        if (options == null) options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));
        setOptionsMutable(options);
        return ensureGLCompatibleBitmap(
                BitmapFactory.decodeFileDescriptor(fd, null, options));
    }

    public static void decodeBounds(JobContext jc, FileDescriptor fd,
            Options options) {
        Utils.assertTrue(options != null);
        options.inJustDecodeBounds = true;
        jc.setCancelListener(new DecodeCanceller(options));
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        options.inJustDecodeBounds = false;
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-17,ALM-676093 begin
    public static void decodeBounds(JobContext jc, String filePath,
            Options options) {
        Utils.assertTrue(options != null);
        options.inJustDecodeBounds = true;
        jc.setCancelListener(new DecodeCanceller(options));
        BitmapFactory.decodeFile(filePath, options);
        options.inJustDecodeBounds = false;
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-17,ALM-676093 end

    public static Bitmap decode(JobContext jc, byte[] bytes, Options options) {
        return decode(jc, bytes, 0, bytes.length, options);
    }

    public static Bitmap decode(JobContext jc, byte[] bytes, int offset,
            int length, Options options) {
        if (options == null) options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));
        setOptionsMutable(options);
        return ensureGLCompatibleBitmap(
                BitmapFactory.decodeByteArray(bytes, offset, length, options));
    }

    public static void decodeBounds(JobContext jc, byte[] bytes, int offset,
            int length, Options options) {
        Utils.assertTrue(options != null);
        options.inJustDecodeBounds = true;
        jc.setCancelListener(new DecodeCanceller(options));
        BitmapFactory.decodeByteArray(bytes, offset, length, options);
        options.inJustDecodeBounds = false;
    }

    public static Bitmap decodeThumbnail(
            JobContext jc, String filePath, Options options, int targetSize, int type) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            FileDescriptor fd = fis.getFD();
            return decodeThumbnail(jc, fd, options, targetSize, type);
        } catch (Exception ex) {
            Log.w(TAG, ex);
            return null;
        } finally {
            Utils.closeSilently(fis);
        }
    }

    public static Bitmap decodeThumbnail(
            JobContext jc, FileDescriptor fd, Options options, int targetSize, int type) {
        if (options == null) options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (jc.isCancelled()) return null;

        int w = options.outWidth;
        int h = options.outHeight;

        if (type == MediaItem.TYPE_MICROTHUMBNAIL) {
            // We center-crop the original image as it's micro thumbnail. In this case,
            // we want to make sure the shorter side >= "targetSize".
            float scale = (float) targetSize / Math.min(w, h);
            options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);

            // For an extremely wide image, e.g. 300x30000, we may got OOM when decoding
            // it for TYPE_MICROTHUMBNAIL. So we add a max number of pixels limit here.
            final int MAX_PIXEL_COUNT = 640000; // 400 x 1600
            if ((w / options.inSampleSize) * (h / options.inSampleSize) > MAX_PIXEL_COUNT) {
                options.inSampleSize = BitmapUtils.computeSampleSize(
                        (float) Math.sqrt((float) MAX_PIXEL_COUNT / (w * h)));
            }
        } else {
            // For screen nail, we only want to keep the longer side >= targetSize.
            float scale = (float) targetSize / Math.max(w, h);
            options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
        }

        options.inJustDecodeBounds = false;
        setOptionsMutable(options);

        Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (result == null) return null;

        // We need to resize down if the decoder does not support inSampleSize
        // (For example, GIF images)
        float scale = (float) targetSize / (type == MediaItem.TYPE_MICROTHUMBNAIL
                ? Math.min(result.getWidth(), result.getHeight())
                : Math.max(result.getWidth(), result.getHeight()));

        if (scale <= 0.5) result = BitmapUtils.resizeBitmapByScale(result, scale, true);
        return ensureGLCompatibleBitmap(result);
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 begin
    //for moments image
    public static Bitmap decodeThumbnail(
            JobContext jc, String filePath, int targetWidth, int targetHeight, Options options, int orientation) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            FileDescriptor fd = fis.getFD();
            return decodeThumbnail(jc, fd, targetWidth, targetHeight, options, orientation);
        } catch (Exception ex) {
            Log.w(TAG, ex);
            return null;
        } finally {
            Utils.closeSilently(fis);
        }
    }

    //for moments image
    public static Bitmap decodeThumbnail(
            JobContext jc, FileDescriptor fd, int targetWidth, int targetHeight, Options options, int orientation) {
        if (options == null) options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));

        DecodeUtils.decodeBounds(jc, fd, options);
        int w, h = 0;
        if(orientation == 0 || orientation == 180) {
            w = options.outWidth;
            h = options.outHeight;
        } else {
            w = options.outHeight;
            h = options.outWidth;
        }
        /* MODIFIED-BEGIN by dongliang.feng, 2016-03-21, BUG-1173487 */
        targetWidth = MOMENTS_IRREGULAR_THUMBNAIL_TARGET_WIDTH;
        float ratio = (float) w / (float) h;
        targetHeight = (int) (targetWidth / ratio);
        /* MODIFIED-END by dongliang.feng,BUG-1173487 */

        float scale = Math.max((float) targetWidth / w, (float) targetHeight / h);
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
        setOptionsMutable(options);

        Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (result == null) return null;

        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-05,Defect:1550574 begin
        result = BitmapUtils.resizeAndCropCenter(result, targetWidth, targetHeight, orientation, true, false);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-05,Defect:1550574 end
        return ensureGLCompatibleBitmap(result);
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 end

    /**
     * Decodes the bitmap from the given byte array if the image size is larger than the given
     * requirement.
     *
     * Note: The returned image may be resized down. However, both width and height must be
     * larger than the <code>targetSize</code>.
     */
    public static Bitmap decodeIfBigEnough(JobContext jc, byte[] data,
            Options options, int targetSize) {
        if (options == null) options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if (jc.isCancelled()) return null;
        if (options.outWidth < targetSize || options.outHeight < targetSize) {
            return null;
        }
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(
                options.outWidth, options.outHeight, targetSize);
        options.inJustDecodeBounds = false;
        setOptionsMutable(options);

        return ensureGLCompatibleBitmap(
                BitmapFactory.decodeByteArray(data, 0, data.length, options));
    }

  //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
    public static Bitmap requestDecode(JobContext jc, final String filePath,
             Options options) {
         if (options == null) options = new Options();
         jc.setCancelListener(new DecodeCanceller(options));
         return ensureGLCompatibleBitmap(
                 BitmapFactory.decodeFile(filePath, options));
     }

     public static Bitmap requestDecode(JobContext jc, FileDescriptor fd, Options options) {
         if (options == null) options = new Options();
         jc.setCancelListener(new DecodeCanceller(options));
         return ensureGLCompatibleBitmap(
                 BitmapFactory.decodeFileDescriptor(fd, null, options));
     }

     public static Bitmap requestDecode(JobContext jc, byte[] bytes,
             Options options) {
         return requestDecode(jc, bytes, 0, bytes.length, options);
     }

     public static Bitmap requestDecode(JobContext jc, byte[] bytes, int offset,
             int length, Options options) {
         if (options == null) options = new Options();
         jc.setCancelListener(new DecodeCanceller(options));
         return ensureGLCompatibleBitmap(
                 BitmapFactory.decodeByteArray(bytes, offset, length, options));
     }

     public static Bitmap requestDecode(JobContext jc, final String filePath,
             Options options, int targetSize) {
         FileInputStream fis = null;
         try {
             fis = new FileInputStream(filePath);
             FileDescriptor fd = fis.getFD();
             return requestDecode(jc, fd, options, targetSize);
         } catch (Exception ex) {
             return null;
         } finally {
             Utils.closeSilently(fis);
         }
     }

     public static Bitmap requestDecode(JobContext jc, FileDescriptor fd,
             Options options, int targetSize) {
         if (options == null) options = new Options();
         jc.setCancelListener(new DecodeCanceller(options));

         options.inJustDecodeBounds = true;
         BitmapFactory.decodeFileDescriptor(fd, null, options);
         if (jc.isCancelled()) return null;

         options.inSampleSize = BitmapUtils.computeSampleSizeLarger(
                 options.outWidth, options.outHeight, targetSize);
         options.inJustDecodeBounds = false;

         Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
         // We need to resize down if the decoder does not support inSampleSize.
         // (For example, GIF images.)
         //[BUGFIX] Modify by Xianzhong.Zhang for PR768642 begin 08/27/2014
         if (result != null) {
             result = BitmapUtils.resizeDownIfTooBig(result, targetSize, true);
         }
         //[BUGFIX] Modify by Xianzhong.Zhang for PR768642 end 08/27/2014
         return ensureGLCompatibleBitmap(result);
     }

     /**
      * Decodes the bitmap from the given byte array if the image size is larger than the given
      * requirement.
      *
      * Note: The returned image may be resized down. However, both width and height must be
      * larger than the <code>targetSize</code>.
      */
     public static Bitmap requestDecodeIfBigEnough(JobContext jc, byte[] data,
             Options options, int targetSize) {
         if (options == null) options = new Options();
         jc.setCancelListener(new DecodeCanceller(options));

         options.inJustDecodeBounds = true;
         BitmapFactory.decodeByteArray(data, 0, data.length, options);
         if (jc.isCancelled()) return null;
         if (options.outWidth < targetSize || options.outHeight < targetSize) {
             return null;
         }
         options.inSampleSize = BitmapUtils.computeSampleSizeLarger(
                 options.outWidth, options.outHeight, targetSize);
         options.inJustDecodeBounds = false;
         return ensureGLCompatibleBitmap(
                 BitmapFactory.decodeByteArray(data, 0, data.length, options));
     }

     public static Bitmap requestDecode(JobContext jc,
             FileDescriptor fileDescriptor, Rect paddings, Options options) {
         if (options == null) options = new Options();
         jc.setCancelListener(new DecodeCanceller(options));
         return ensureGLCompatibleBitmap(BitmapFactory.decodeFileDescriptor
                 (fileDescriptor, paddings, options));
     }
 //[FEATURE]-Add-END by ye.chen

    // TODO: This function should not be called directly from
    // DecodeUtils.requestDecode(...), since we don't have the knowledge
    // if the bitmap will be uploaded to GL.
    public static Bitmap ensureGLCompatibleBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.getConfig() != null) return bitmap;
        Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, false);
        bitmap.recycle();
        return newBitmap;
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(
            JobContext jc, byte[] bytes, int offset, int length,
            boolean shareable) {
        if (offset < 0 || length <= 0 || offset + length > bytes.length) {
            throw new IllegalArgumentException(String.format(
                    "offset = %s, length = %s, bytes = %s",
                    offset, length, bytes.length));
        }

        try {
            return BitmapRegionDecoder.newInstance(
                    bytes, offset, length, shareable);
        } catch (Throwable t)  {
            Log.w(TAG, t);
            return null;
        }
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(
            JobContext jc, String filePath, boolean shareable) {
        try {
            return BitmapRegionDecoder.newInstance(filePath, shareable);
        } catch (Throwable t)  {
            Log.w(TAG, t);
            return null;
        }
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(
            JobContext jc, FileDescriptor fd, boolean shareable) {
        try {
            return BitmapRegionDecoder.newInstance(fd, shareable);
        } catch (Throwable t)  {
            Log.w(TAG, t);
            return null;
        }
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(
            JobContext jc, InputStream is, boolean shareable) {
        try {
            return BitmapRegionDecoder.newInstance(is, shareable);
        } catch (Throwable t)  {
            // We often cancel the creating of bitmap region decoder,
            // so just log one line.
            Log.w(TAG, "requestCreateBitmapRegionDecoder: " + t);
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Bitmap decodeUsingPool(JobContext jc, byte[] data, int offset,
            int length, BitmapFactory.Options options) {
        if (options == null) options = new BitmapFactory.Options();
        if (options.inSampleSize < 1) options.inSampleSize = 1;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inBitmap = (options.inSampleSize == 1)
                ? findCachedBitmap(jc, data, offset, length, options) : null;
        try {
            Bitmap bitmap = decode(jc, data, offset, length, options);
            if (options.inBitmap != null && options.inBitmap != bitmap) {
                GalleryBitmapPool.getInstance().put(options.inBitmap);
                options.inBitmap = null;
            }
            return bitmap;
        } catch (IllegalArgumentException e) {
            if (options.inBitmap == null) throw e;

            Log.w(TAG, "decode fail with a given bitmap, try decode to a new bitmap");
            GalleryBitmapPool.getInstance().put(options.inBitmap);
            options.inBitmap = null;
            return decode(jc, data, offset, length, options);
        }
    }

    // This is the same as the method above except the source data comes
    // from a file descriptor instead of a byte array.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Bitmap decodeUsingPool(JobContext jc,
            FileDescriptor fileDescriptor, Options options) {
        if (options == null) options = new BitmapFactory.Options();
        if (options.inSampleSize < 1) options.inSampleSize = 1;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inBitmap = (options.inSampleSize == 1)
                ? findCachedBitmap(jc, fileDescriptor, options) : null;
        try {
            Bitmap bitmap = DecodeUtils.decode(jc, fileDescriptor, options);
            if (options.inBitmap != null && options.inBitmap != bitmap) {
                GalleryBitmapPool.getInstance().put(options.inBitmap);
                options.inBitmap = null;
            }
            return bitmap;
        } catch (IllegalArgumentException e) {
            if (options.inBitmap == null) throw e;

            Log.w(TAG, "decode fail with a given bitmap, try decode to a new bitmap");
            GalleryBitmapPool.getInstance().put(options.inBitmap);
            options.inBitmap = null;
            return decode(jc, fileDescriptor, options);
        }
    }

    private static Bitmap findCachedBitmap(JobContext jc, byte[] data,
            int offset, int length, Options options) {
        decodeBounds(jc, data, offset, length, options);
        return GalleryBitmapPool.getInstance().get(options.outWidth, options.outHeight);
    }

    private static Bitmap findCachedBitmap(JobContext jc, FileDescriptor fileDescriptor,
            Options options) {
        decodeBounds(jc, fileDescriptor, options);
        return GalleryBitmapPool.getInstance().get(options.outWidth, options.outHeight);
    }

  //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
    public static BitmapRegionDecoder requestCreateBitmapRegionDecoder(
            JobContext jc, Uri uri, ContentResolver resolver,
            boolean shareable) {
        ParcelFileDescriptor pfd = null;
        try {
            pfd = resolver.openFileDescriptor(uri, "r");
            return BitmapRegionDecoder.newInstance(
                    pfd.getFileDescriptor(), shareable);
        } catch (Throwable t) {
            return null;
        } finally {
            Utils.closeSilently(pfd);
        }
    }
//[FEATURE]-Add-END by ye.chen
}

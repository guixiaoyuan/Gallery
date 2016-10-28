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
/* 03/07/2016| jian.pan1            |[ALM]Defect:1649868   |[VF17213][1a - Fatal][CTC][Gallery]'Gallery: Some thumbnails are blurry when viewing but actual image is ok
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.common;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever; // MODIFIED by caihong.gu-nb, 2016-04-27,BUG-1992251
import android.os.Build;
import android.util.Log;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";
    private static final int DEFAULT_JPEG_QUALITY = 100;// [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-07,Defect:1649868
    public static final int UNCONSTRAINED = -1;

    private BitmapUtils(){}

    /*
     * Compute the sample size as a function of minSideLength
     * and maxNumOfPixels.
     * minSideLength is used to specify that minimal width or height of a
     * bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     *
     * The function returns a sample size based on the constraints.
     * Both size and minSideLength can be passed in as UNCONSTRAINED,
     * which indicates no care of the corresponding constraint.
     * The functions prefers returning a sample size that
     * generates a smaller bitmap, unless minSideLength = UNCONSTRAINED.
     *
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because BitmapFactory only honors sample size this way.
     * For example, BitmapFactory downsamples an image by 2 even though the
     * request is 3. So we round up the sample size to avoid OOM.
     */
    public static int computeSampleSize(int width, int height,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(
                width, height, minSideLength, maxNumOfPixels);

        return initialSize <= 8
                ? Utils.nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    private static int computeInitialSampleSize(int w, int h,
            int minSideLength, int maxNumOfPixels) {
        if (maxNumOfPixels == UNCONSTRAINED
                && minSideLength == UNCONSTRAINED) return 1;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                (int) Math.ceil(Math.sqrt((float) (w * h) / maxNumOfPixels));

        if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            int sampleSize = Math.min(w / minSideLength, h / minSideLength);
            return Math.max(sampleSize, lowerBound);
        }
    }

    // This computes a sample size which makes the longer side at least
    // minSideLength long. If that's not possible, return 1.
    public static int computeSampleSizeLarger(int w, int h,
            int minSideLength) {
        int initialSize = Math.max(w / minSideLength, h / minSideLength);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? Utils.prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    // Find the min x that 1 / x >= scale
    public static int computeSampleSizeLarger(float scale) {
        int initialSize = (int) Math.floor(1f / scale);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? Utils.prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    // Find the max x that 1 / x <= scale.
    public static int computeSampleSize(float scale) {
        Utils.assertTrue(scale > 0);
        int initialSize = Math.max(1, (int) Math.ceil(1 / scale));
        return initialSize <= 8
                ? Utils.nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    public static Bitmap resizeBitmapByScale(
            Bitmap bitmap, float scale, boolean recycle) {
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth()
                && height == bitmap.getHeight()) return bitmap;
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    public static Bitmap resizeDownBySideLength(
            Bitmap bitmap, int maxLength, boolean recycle) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float scale = Math.min(
                (float) maxLength / srcWidth, (float) maxLength / srcHeight);
        if (scale >= 1.0f) return bitmap;
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == size && h == size) return bitmap;

        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float scale = (float) size / Math.min(w,  h);

        Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        canvas.translate((size - width) / 2f, (size - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 begin
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-05,Defect:1550574 begin
    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int targetWidth, int targetHeight, int orientation, boolean recycle, boolean isDrm) {
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-05,Defect:1550574 end
        if(bitmap == null) return null; // MODIFIED by caihong.gu-nb, 2016-04-27,BUG-1992251
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if(orientation == 90 || orientation == 270) {
            int tmp = targetWidth;
            targetWidth = targetHeight;
            targetHeight = tmp;
        }
        if (w == targetWidth && h == targetHeight) return bitmap;

        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float xScale = (float) targetWidth / w;
        float yScale = (float) targetHeight / h;
        float scale = Math.max(xScale, yScale);

        Bitmap target = Bitmap.createBitmap(targetWidth, targetHeight, getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-05,Defect:1550574 begin
        if (isDrm) {
            canvas.translate((targetWidth - width), (targetHeight - height));
        } else {
            canvas.translate((targetWidth - width) / 2f, (targetHeight - height) / 2f);
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-05,Defect:1550574 end
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 end

    //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
    public static Bitmap resizeDownIfTooBig(Bitmap bitmap, int targetSize, boolean recycle) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float scale = Math.max(
                      (float) targetSize / srcWidth, (float) targetSize / srcHeight);
        if (scale > 0.5f) return bitmap;
        return resizeBitmapByScale(bitmap, scale, recycle);
    }
   //[FEATURE]-Add-END by ye.chen

    public static void recycleSilently(Bitmap bitmap) {
        if (bitmap == null) return;
        try {
            bitmap.recycle();
        } catch (Throwable t) {
            Log.w(TAG, "unable recycle bitmap", t);
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, int rotation, boolean recycle) {
        if (rotation == 0 || source == null) return source;
        int w = source.getWidth();
        int h = source.getHeight();
        Matrix m = new Matrix();
        m.postRotate(rotation);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, w, h, m, true);
        if (recycle) source.recycle();
        return bitmap;
    }

    public static Bitmap createVideoThumbnail(String filePath) {
        // MediaMetadataRetriever is available on API Level 8
        // but is hidden until API Level 10
        Class<?> clazz = null;
        Object instance = null;
        try {
            clazz = Class.forName("android.media.MediaMetadataRetriever");
            instance = clazz.newInstance();

            Method method = clazz.getMethod("setDataSource", String.class);
            method.invoke(instance, filePath);

            // The method name changes between API Level 9 and 10.
            if (Build.VERSION.SDK_INT <= 9) {
                return (Bitmap) clazz.getMethod("captureFrame").invoke(instance);
            } else {
                byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture").invoke(instance);
                if (data != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (bitmap != null) return bitmap;
                }
                return (Bitmap) clazz.getMethod("getFrameAtTime").invoke(instance);
            }
        } catch (IllegalArgumentException ex) {
            /* MODIFIED-BEGIN by caihong.gu-nb, 2016-04-27,BUG-1992251*/
            Log.e(TAG, "createVideoThumbnail", ex);
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            Log.e(TAG, "createVideoThumbnail", ex);
            /* MODIFIED-END by caihong.gu-nb,BUG-1992251*/
            // Assume this is a corrupt video file.
        } catch (InstantiationException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } finally {
            try {
                if (instance != null) {
                    clazz.getMethod("release").invoke(instance);
                }
            } catch (Exception ignored) {
            }
        }
        /* MODIFIED-BEGIN by caihong.gu-nb, 2016-04-27,BUG-1992251*/
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap bitmap = null;
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        } catch (Exception e) {
        }

        if(bitmap != null){
            return bitmap;
        }
        /* MODIFIED-END by caihong.gu-nb,BUG-1992251*/
        return null;
    }

    public static byte[] compressToBytes(Bitmap bitmap) {
        return compressToBytes(bitmap, DEFAULT_JPEG_QUALITY);
    }

    public static byte[] compressToBytes(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
        bitmap.compress(CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    public static boolean isSupportedByRegionDecoder(String mimeType) {
        if (mimeType == null) return false;
        mimeType = mimeType.toLowerCase();
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-20,PR844577 begin
        return mimeType.startsWith("image/") &&
                (!mimeType.equals("image/gif"));
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-20,PR844577 end
    }

    public static boolean isRotationSupported(String mimeType) {
        if (mimeType == null) return false;
        mimeType = mimeType.toLowerCase();
        return mimeType.equals("image/jpeg");
    }
}

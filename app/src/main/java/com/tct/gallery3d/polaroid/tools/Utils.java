/* ----------|----------------------|----------------------|----------------- */
/* 07/14/2015| chengbin.du          | PR1026715            |[Android5.1][Gallery_Polaroid_v5.1.13.1.0209.0]The slogan is not in the middle of the screen.
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.polaroid.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.renderscript.Allocation;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import com.tct.gallery3d.exif.ExifInterface;
import com.tct.gallery3d.polaroid.Poladroid;

public class Utils {

    public static final String STORAGE_EXTERNAL = Environment.getExternalStorageDirectory()
            .toString();
    public static final String STORAGE_SDCARD1 = "/storage/sdcard1";
    public static final String POLAROID_PATH_SDCARD0 = STORAGE_EXTERNAL + "/DCIM/Polaroid";
    public static final String POLAROID_PATH_SDCARD1 = STORAGE_SDCARD1 + "/DCIM/Polaroid";
    public static String mCurrentSavePath = STORAGE_EXTERNAL;

    public static void setCurrentStoragePath(String path) {
        if (!TextUtils.isEmpty(path)) {
            if (path.contains(STORAGE_EXTERNAL)) {
                mCurrentSavePath = STORAGE_EXTERNAL;
            } else if (path.contains(STORAGE_SDCARD1)) {
                mCurrentSavePath = STORAGE_SDCARD1;
            }
        }
    }

    public static String getCurrentLocalPath() {
        return mCurrentSavePath;
    }

    public static BitmapDrawable getScaledCroppedDrawable(Context context,
            BitmapDrawable inDrawable, Rect inCrop, Point outResolution) {
        Log.d(Poladroid.TAG, "Utils.getScaledCroppedDrawable(" + inCrop + ", " + outResolution + "){");
        BitmapDrawable outDrawable = null;

        try {
            Bitmap outBitmap = Bitmap.createBitmap(outResolution.x, outResolution.y,
                    Bitmap.Config.ARGB_8888);
            Canvas outCanvas = new Canvas(outBitmap);
            RectF outCrop = new RectF(0, 0, outResolution.x, outResolution.y);
            Bitmap inBitmap = inDrawable.getBitmap();

            Paint paint = new Paint();
            paint.setDither(true);
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);

            outCanvas.drawBitmap(inBitmap, inCrop, outCrop, paint);
            outDrawable = new BitmapDrawable(context.getResources(), outBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(Poladroid.TAG, "} Utils.getScaledCroppedDrawable()");

        return outDrawable;
    }

    public static Rect getCenteredScaledCrop(BitmapDrawable inDrawable, Point outResolution) {
        Rect inCrop = new Rect();

        int inWidth = inDrawable.getIntrinsicWidth();
        int inHeight = inDrawable.getIntrinsicHeight();
        if (inWidth * outResolution.y <= inHeight * outResolution.x) {
            // Scale and Fit Width / Scale and Crop Height
            inCrop.left = 0;
            inCrop.right = inWidth;
            float top = (1.0f * inHeight - outResolution.y * inWidth / outResolution.x) / 2;
            float bottom = top + 1.0f * outResolution.y * inWidth / outResolution.x;
            inCrop.top = (int) Math.floor(top);
            inCrop.bottom = (int) Math.ceil(bottom);
        } else {
            // Scale and Fit Height / Scale and Crop Width
            inCrop.top = 0;
            inCrop.bottom = inHeight;
            float left = (1.0f * inWidth - outResolution.x * inHeight / outResolution.y) / 2;
            float right = left + 1.0f * outResolution.x * inHeight / outResolution.y;
            inCrop.left = (int) Math.floor(left);
            inCrop.right = (int) Math.ceil(right);
        }

        Log.d(Poladroid.TAG, "Utils.getCenteredScaledCrop(inDrawable: " + inWidth + "x" + inHeight
                + ", outResolution: " + outResolution + ") => inCrop: " + inCrop);

        return inCrop;
    }

    public static int DEFAULT_JPEG_QUALITY = 100;
    static int DEFAULT_PNG_QUALITY = 90;

    static void saveAllocation(Context context, Allocation allocation, String filename) {
        Bitmap bitmap = Bitmap.createBitmap(allocation.getType().getX(), allocation.getType()
                .getY(), Bitmap.Config.ARGB_8888);
        allocation.copyTo(bitmap);
        saveBitmap(context, bitmap, filename);
        bitmap.recycle();
    }

    static String saveBitmap(Context context, Bitmap bitmap, String filename) {
        return saveBitmap(context, bitmap, filename, Bitmap.CompressFormat.PNG,
                DEFAULT_PNG_QUALITY, null);
    }

    public static String saveBitmap(Context context, Bitmap bitmap, String filename,
            Bitmap.CompressFormat compressFormat, int quality, ExifInterface exif) {
        String outFilename = null;

        try {
            String extension = compressFormat.toString().toLowerCase(Locale.US);
            File myDir = new File(getCurrentLocalPath() + "/DCIM/Polaroid");
            myDir.mkdirs();
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);

            String name = filename + "." + extension;

            File file = new File(myDir, name);
            if (file.exists())
                file.delete();

            OutputStream out = null;
            if (exif != null) {
                out = exif.getExifWriterStream(file.getAbsolutePath());
            } else {
                out = new FileOutputStream(file);
            }
            bitmap.compress(compressFormat, quality, out);
            out.flush();
            out.close();

            Log.i(Poladroid.TAG, "Utils.saveBitmap(" + compressFormat + ") => " + myDir + "/" + name);
            outFilename = myDir + "/" + name;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return outFilename;
    }

    static String getGregorianString(long epochTime) {
        // GregorianCalendar calendar = new GregorianCalendar();
        SimpleDateFormat timeDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",
                Locale.getDefault());
        String timeDate = timeDateFormat.format(new Date(epochTime * 1000));
        return timeDate;
    }

    public static String getGregorianString() {
        return getGregorianString(System.currentTimeMillis() / 1000);
    }

    // [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-07-14,PR1026715 begin
    public static float autofitTextSize(CharSequence text, TextPaint paint,
            int targetWidth, int targetHeight, Layout.Alignment alignment, float maxTextSize) {
        if(text.length() == 0 || maxTextSize == 0) return 0.0f;
        paint.setTextSize(maxTextSize);

        float size = getAutofitTextSize(text, paint, targetWidth, targetHeight, alignment, maxTextSize);
        return size;
    }

    public static float getAutofitTextSize(CharSequence text, TextPaint paint,
            int targetWidth, int targetHeight, Layout.Alignment alignment, float maxTextSize) {
        StaticLayout layout = new StaticLayout(text, paint, targetWidth, alignment, 1.0f, 0.0f, true);
        int height = layout.getHeight();
        FontMetrics fm = paint.getFontMetrics();
        Log.d("AutofitTextSize", "current text size " + paint.getTextSize());
        Log.d("AutofitTextSize", "targetHeight=" + targetHeight);
        Log.d("AutofitTextSize", "current fm ascent=" + fm.ascent + " descent=" + fm.descent + " top=" + fm.top + " bottom=" + fm.bottom + " leading=" + fm.leading);
        if(height > targetHeight) {
            float midTextSize = maxTextSize - 2;
            paint.setTextSize(midTextSize);
            return getAutofitTextSize(text, paint, targetWidth, targetHeight, alignment, midTextSize);
        } else {
            return paint.getTextSize();
        }
    }
    // [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-07-14,PR1026715 end
}

/* EOF */

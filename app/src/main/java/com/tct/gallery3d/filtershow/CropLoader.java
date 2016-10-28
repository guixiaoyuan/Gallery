/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.gallery3d.filtershow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;

import com.tct.gallery3d.common.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public abstract class CropLoader {

    private static final String TAG = "CropLoader";

    public static Bitmap getConstrainedBitmap(Uri paramUri, Context paramContext, int paramInt, Rect paramRect) {
        if ((paramInt <= 0) || (paramRect == null) || (paramUri == null) || (paramContext == null))
            throw new IllegalArgumentException("bad argument to getScaledBitmap");

        InputStream localInputStream = null;

        try {
            localInputStream = paramContext.getContentResolver().openInputStream(paramUri);
            BitmapFactory.Options localOptions1 = new BitmapFactory.Options();
            localOptions1.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(localInputStream, null, localOptions1);
            int width = localOptions1.outWidth;
            int height = localOptions1.outHeight;
            paramRect.set(0, 0, width, height);

            if ((width <= 0) || (height <= 0))
                return null;

            BitmapFactory.Options localOptions2 = new BitmapFactory.Options();
            int k = Math.max(width, height);
            localOptions2.inSampleSize = 1;
            if (k > paramInt) {
                int m = 1 + Integer.numberOfLeadingZeros(paramInt) - Integer.numberOfLeadingZeros(k);
                localOptions2.inSampleSize <<= m;
            }
            if (localOptions2.inSampleSize <= 0) {
                return null;
            }
            localOptions2.inMutable = true;
            localInputStream.close();
            localInputStream = paramContext.getContentResolver().openInputStream(paramUri);
            Bitmap localBitmap = BitmapFactory.decodeStream(localInputStream, null, localOptions2);
            return localBitmap;

        } catch (FileNotFoundException localFileNotFoundException) {
            Log.e(TAG, "FileNotFoundException: " + paramUri, localFileNotFoundException);
            return null;
        } catch (IOException localIOException) {
            Log.e(TAG, "IOException: " + paramUri, localIOException);
            return null;
        } finally {
            Utils.closeSilently(localInputStream);
        }
    }

    public static int getMetadataRotation(Uri paramUri, Context paramContext) {
        return 0;
    }

    public static File getFinalSaveDirectory(Context paramContext, Uri paramUri) {
        File localFile = getSaveDirectory(paramContext, paramUri);
        if ((localFile == null) || (!localFile.canWrite()))
            localFile = new File(Environment.getExternalStorageDirectory(), "EditedOnlinePhotos");
        if (!localFile.exists())
            localFile.mkdirs();
        return localFile;
    }

    public static File getNewFile(Context paramContext, Uri paramUri, String paramString) {
        return new File(getFinalSaveDirectory(paramContext, paramUri), paramString + ".JPG");
    }

    public static String getNewFileName(long paramLong) {
        return new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss").format(new Date(paramLong));
    }

    private static File getSaveDirectory(Context paramContext, Uri paramUri) {
        final File[] arrayOfFile = new File[1];
        querySource(paramContext, paramUri, new String[] { "_data" }, new ContentResolverQueryCallback() {
            public void onCursorResult(Cursor paramCursor) {
                arrayOfFile[0] = new File(paramCursor.getString(0)).getParentFile();
            }
        });

        return arrayOfFile[0];
    }

    // mActivity, Uri.fromFile(newpic), newpic, str, 1000L *
    // System.currentTimeMillis() , newpic (file), str(directory name)
    public static Uri insertContent(Context paramContext, Uri paramUri, File paramFile, String paramString,
            long paramLong) {
        long l = paramLong / 1000L;
        final ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramString);
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "image/jpeg");
        localContentValues.put("datetaken", Long.valueOf(l));
        localContentValues.put("date_modified", Long.valueOf(l));
        localContentValues.put("date_added", Long.valueOf(l));
        localContentValues.put("orientation", Integer.valueOf(0));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));

        querySource(paramContext, paramUri, new String[] { "datetaken", "latitude", "longitude" },
                new ContentResolverQueryCallback() {
                    public void onCursorResult(Cursor paramCursor) {
                        localContentValues.put("datetaken", Long.valueOf(paramCursor.getLong(0)));
                        double d1 = paramCursor.getDouble(1);
                        double d2 = paramCursor.getDouble(2);
                        if ((d1 != 0.0D) || (d2 != 0.0D)) {
                            localContentValues.put("latitude", Double.valueOf(d1));
                            localContentValues.put("longitude", Double.valueOf(d2));
                        }
                    }
                });

        Log.e(TAG, "CropLoader insert new item");

        return paramContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                localContentValues);
    }

    public static Uri makeAndInsertUri(Context paramContext, Uri paramUri) {
        long l = System.currentTimeMillis();
        String str = getNewFileName(l);
        return insertContent(paramContext, paramUri, getNewFile(paramContext, paramUri, str), str, l);
    }

    private static void querySource(Context paramContext, Uri paramUri, String[] paramArrayOfString,
            ContentResolverQueryCallback mCallback) {
        ContentResolver mResolver = paramContext.getContentResolver();
        Cursor localCursor = null;
        try {
            localCursor = mResolver.query(paramUri, paramArrayOfString, null, null, null);
            if ((localCursor != null) && (localCursor.moveToNext()))
                mCallback.onCursorResult(localCursor);
        } catch (Exception localException) {

        } finally {
            if (localCursor != null)
                localCursor.close();
        }
    }

    private static abstract interface ContentResolverQueryCallback {
        public abstract void onCursorResult(Cursor paramCursor);
    }
}

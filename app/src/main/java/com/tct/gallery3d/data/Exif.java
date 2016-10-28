/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.tct.gallery3d.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.exif.ExifInterface;
import com.tct.gallery3d.filtershow.cache.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Exif {
    private static final String TAG = "GalleryExif";

    /**
     * Returns the degrees in clockwise. Values are 0, 90, 180, or 270.
     */
    public static int getOrientation(InputStream is) {
        if (is == null) {
            return 0;
        }
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(is);
            Integer val = exif.getTagIntValue(ExifInterface.TAG_ORIENTATION);
            if (val == null) {
                return 0;
            } else {
                return ExifInterface.getRotationForOrientationValue(val.shortValue());
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to read EXIF orientation", e);
            return 0;
        }
    }

    /**
     * Returns an exif interface instance for the given JPEG image.
     *
     * @param jpegData a valid JPEG image containing EXIF data
     */
    public static ExifInterface getExif(byte[] jpegData) {
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(jpegData);
        } catch (IOException e) {
            Log.w(TAG, "Failed to read EXIF data", e);
        }
        return exif;
    }

    /**
     * Returns the degrees in clockwise. Values are 0, 90, 180, or 270.
     */
    public static int getOrientation(ExifInterface exif) {
        Integer val = exif.getTagIntValue(ExifInterface.TAG_ORIENTATION);
        if (val == null) {
            return 0;
        } else {
            return ExifInterface.getRotationForOrientationValue(val.shortValue());
        }
    }

    /**
     * See {@link #getOrientation(byte[])}, but using the picture bytes instead.
     */
    public static int getOrientation(byte[] jpegData) {
        if (jpegData == null)
            return 0;

        ExifInterface exif = getExif(jpegData);
        return getOrientation(exif);
    }

    public static ExifInterface getExifData(Uri mOriginalUri, Context context) {
        ExifInterface exif = new ExifInterface();
        String mimeType = context.getContentResolver().getType(mOriginalUri);
        if (mimeType == null) {
            mimeType = ImageLoader.getMimeType(mOriginalUri);
            if (mimeType == null) {
                return exif;
            }
        }
        if (mimeType.equals(ImageLoader.JPEG_MIME_TYPE)) {
            InputStream inStream = null;
            try {
                inStream = context.getContentResolver().openInputStream(
                        mOriginalUri);
                exif.readExif(inStream);
            } catch (FileNotFoundException e) {
                Log.w(TAG, "Cannot find file: " + mOriginalUri, e);
            } catch (IOException e) {
                Log.w(TAG, "Cannot read exif for: " + mOriginalUri, e);
            } finally {
                Utils.closeSilently(inStream);
            }
        } else {
            return null;
        }
        return exif;
    }

    public static boolean saveExifDataToBitmap(File file, Uri mOriginalUri,
            Bitmap bitmap, ExifInterface exif, int quality) {
        OutputStream s = null;
        boolean reset = false;
        try {
            s = exif.getExifWriterStream(file.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, (quality > 0) ? quality
                    : 1, s);
            s.flush();
            s.close();
            reset = true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            Log.e(TAG, "Cannot read exif for: " + mOriginalUri, e);
        } finally {
            Utils.closeSilently(s);
        }
        return reset;
    }
}

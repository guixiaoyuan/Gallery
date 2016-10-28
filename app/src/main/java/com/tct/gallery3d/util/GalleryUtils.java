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
/* 06/03/2015|    jialiang.ren     |      PR-937067       |[5.0][Gallery] pitch-to-zoom is not correctly 2x max */
/* ----------|---------------------|----------------------|---------------------------------------------------- */
/* 23/03/2015|dongliang.feng        |PR956418              |[Android5.0][Gallery_v5.1.9.1.0107.0][REG]There are double messages in share list */
/* ----------|----------------------|----------------------|----------------- */
/* 23/04/2015 |    jialiang.ren     |      PR-981838       |[Android5.0][Gallery_v5.1.9.1.0113.0]The amplification*/
/*                                                          factors in gallery and camera are different           */
/*------------|---------------------|----------------------|------------------------------------------------------*/
/* 25/05/2015|dongliang.feng        |PR1009795             |[SW][Camera][US]The Scanner/HDR will launcher the front camera in some condition */
/* ----------|----------------------|----------------------|----------------- */
/* 07/08/2015|    su.jiang          |  PR-1062955          |[Monitor][Email]The screen will be black when open the picture attachment via gallery in Draft*/
/*-----------|----------------------|----------------------|----------------------------------------------------------------------------------------------*/
/* 07/09/2015|dongliang.feng        |PR1080140             |[UE][Gallery]The time display wrong */
/* ----------|----------------------|----------------------|----------------- */
/* 11/11/2015|    su.jiang          |  PR-839832           |[Android 5.1][Gallery_v5.2.3.1.0309.0]The video interface will grey after clicking edit button*/
/*-----------|----------------------|----------------------|----------------------------------------------------------------------------------------------*/
/* 20/11/2015|dongliang.feng        |PR839655              |[Gallery]Gallery would force close after edited the renamed picture */
/* ----------|----------------------|----------------------|----------------- */
/* 08/01/2015|    su.jiang          |  PR-1159002          |[Gallery][Video]After the video saved to gallery ,press the "X" should back to the video preview screen.*/
/*-----------|----------------------|----------------------|--------------------------------------------------------------------------------------------------------*/
/* 16/01/2015|    su.jiang          |  PR-1401750          |[GAPP][Android6.0][Gallery]The video play interface display not same as preview interface.*/
/*-----------|----------------------|----------------------|------------------------------------------------------------------------------------------*/
/* 01/26/2016| jian.pan1            |[ALM]Defect:1496305   |Gallery fyuse function control
/* ----------|----------------------|----------------------|----------------- */
/* 03/07/2016| jian.pan1            |[ALM]Defect:1649868   |[VF17213][1a - Fatal][CTC][Gallery]'Gallery: Some thumbnails are blurry when viewing but actual image is ok
/* ----------|----------------------|----------------------|----------------- */
/* 03/09/2016|    su.jiang          |  PR-1759891          |[GAPP][Android6.0][Gallery]The video duration display is different from details.*/
/*-----------|----------------------|----------------------|--------------------------------------------------------------------------------*/
/* 03/10/2016|    su.jiang          |  PR-1649864          |[GAPP][Android 6.0][Gallery]The boomkey tooltips also display when close boomkey effects.*/
/*-----------|----------------------|----------------------|-----------------------------------------------------------------------------------------*/

package com.tct.gallery3d.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.WifiDisplayStatus;
import android.net.Uri;
import android.os.ConditionVariable;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.PackagesMonitor;
import com.tct.gallery3d.app.fyuse.FyuseAPI;
import com.tct.gallery3d.common.ApiHelper;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaObject;
import com.tct.gallery3d.data.MomentsNewAlbum;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.util.ThreadPool.CancelListener;
import com.tct.gallery3d.util.ThreadPool.JobContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GalleryUtils {
    private static final String TAG = "GalleryUtils";
    private static final String MAPS_PACKAGE_NAME = "com.google.android.apps.maps";
    private static final String MAPS_CLASS_NAME = "com.google.android.maps.MapsActivity";
    private static final String CAMERA_LAUNCHER_NAME = "com.android.camera.CameraLauncher";

    public static final String MIME_TYPE_IMAGE = "image/*";
    public static final String MIME_TYPE_VIDEO = "video/*";
    public static final String MIME_TYPE_GIF = "image/gif"; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956418
    public static final String MIME_TYPE_PANORAMA360 = "application/vnd.google.panorama360+jpg";
    public static final String MIME_TYPE_ALL = "*/*";
    public static final String INTENT_OPERATION_START = "com.gallery.operation.start";
    public static final String INTENT_OPERATION_FINISH = "com.gallery.operation.finish";

    private static final String DIR_TYPE_IMAGE = "vnd.android.cursor.dir/image";
    private static final String DIR_TYPE_VIDEO = "vnd.android.cursor.dir/video";

    private static final String PREFIX_PHOTO_EDITOR_UPDATE = "editor-update-";
    private static final String PREFIX_HAS_PHOTO_EDITOR = "has-editor-";

    private static final String KEY_CAMERA_UPDATE = "camera-update";
    private static final String KEY_HAS_CAMERA = "has-camera";

    public static int KEY_FILE_EXISTS = 0;
    public static int KEY_FILE_CREATE_SUCCESS = 1;
    public static int KEY_FILE_CREATE_FAIL = -1;

    /**
     * [BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-06,PR937067
     * <p>
     * If this constant is "true",the picture of camera will scale by special limit:double screen size.
     * But if it is "false",the picture of camera will scale by the normal limit as other picture:quadruple size of picture.
     */
    public static final boolean SPECIAL_SCALE_FOR_CAMERA_PICTURE = false;//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-23,PR981838

    private static float sPixelDensity = -1f;
    private static boolean sCameraAvailableInitialized = false;
    private static boolean sCameraAvailable;

    public static void initialize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        sPixelDensity = metrics.density;
        Resources r = context.getResources();
        initializeThumbnailSizes(metrics, r);
    }

    private static void initializeThumbnailSizes(DisplayMetrics metrics, Resources r) {
        int maxPixels = Math.max(metrics.heightPixels, metrics.widthPixels);

        // For screen-nails, we never need to completely fill the screen
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-07,Defect:1649868 begin
        MediaItem.setThumbnailSizes(maxPixels / 2, maxPixels / 5);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-07,Defect:1649868 end
    }

    public static float[] intColorToFloatARGBArray(int from) {
        return new float[]{
                Color.alpha(from) / 255f,
                Color.red(from) / 255f,
                Color.green(from) / 255f,
                Color.blue(from) / 255f
        };
    }

    public static float dpToPixel(float dp) {
        return sPixelDensity * dp;
    }

    public static int dpToPixel(int dp) {
        return Math.round(dpToPixel((float) dp));
    }

    public static int meterToPixel(float meter) {
        // 1 meter = 39.37 inches, 1 inch = 160 dp.
        return Math.round(dpToPixel(meter * 39.37f * 160));
    }

    public static byte[] getBytes(String in) {
        byte[] result = new byte[in.length() * 2];
        int output = 0;
        for (char ch : in.toCharArray()) {
            result[output++] = (byte) (ch & 0xFF);
            result[output++] = (byte) (ch >> 8);
        }
        return result;
    }

    // Below are used the detect using database in the render thread. It only
    // works most of the time, but that's ok because it's for debugging only.

    private static volatile Thread sCurrentThread;
    private static volatile boolean sWarned;

    public static void setRenderThread() {
        sCurrentThread = Thread.currentThread();
    }

    public static void assertNotInRenderThread() {
        if (!sWarned) {
            if (Thread.currentThread() == sCurrentThread) {
                sWarned = true;
                Log.w(TAG, new Throwable("Should not do this in render thread"));
            }
        }
    }

    private static final double RAD_PER_DEG = Math.PI / 180.0;
    private static final double EARTH_RADIUS_METERS = 6367000.0;

    public static double fastDistanceMeters(double latRad1, double lngRad1,
                                            double latRad2, double lngRad2) {
        if ((Math.abs(latRad1 - latRad2) > RAD_PER_DEG)
                || (Math.abs(lngRad1 - lngRad2) > RAD_PER_DEG)) {
            return accurateDistanceMeters(latRad1, lngRad1, latRad2, lngRad2);
        }
        // Approximate sin(x) = x.
        double sineLat = (latRad1 - latRad2);

        // Approximate sin(x) = x.
        double sineLng = (lngRad1 - lngRad2);

        // Approximate cos(lat1) * cos(lat2) using
        // cos((lat1 + lat2)/2) ^ 2
        double cosTerms = Math.cos((latRad1 + latRad2) / 2.0);
        cosTerms = cosTerms * cosTerms;
        double trigTerm = sineLat * sineLat + cosTerms * sineLng * sineLng;
        trigTerm = Math.sqrt(trigTerm);

        // Approximate arcsin(x) = x
        return EARTH_RADIUS_METERS * trigTerm;
    }

    public static double accurateDistanceMeters(double lat1, double lng1,
                                                double lat2, double lng2) {
        double dlat = Math.sin(0.5 * (lat2 - lat1));
        double dlng = Math.sin(0.5 * (lng2 - lng1));
        double x = dlat * dlat + dlng * dlng * Math.cos(lat1) * Math.cos(lat2);
        return (2 * Math.atan2(Math.sqrt(x), Math.sqrt(Math.max(0.0,
                1.0 - x)))) * EARTH_RADIUS_METERS;
    }


    public static final double toMile(double meter) {
        return meter / 1609;
    }

    // For debugging, it will block the caller for timeout millis.
    public static void fakeBusy(JobContext jc, int timeout) {
        final ConditionVariable cv = new ConditionVariable();
        jc.setCancelListener(new CancelListener() {
            @Override
            public void onCancel() {
                cv.open();
            }
        });
        cv.block(timeout);
        jc.setCancelListener(null);
    }

    public static boolean isEditorAvailable(Context context, String mimeType) {
        int version = PackagesMonitor.getPackagesVersion(context);

        String updateKey = PREFIX_PHOTO_EDITOR_UPDATE + mimeType;
        String hasKey = PREFIX_HAS_PHOTO_EDITOR + mimeType;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt(updateKey, 0) != version) {
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> infos = packageManager.queryIntentActivities(
                    new Intent(Intent.ACTION_EDIT).setType(mimeType), 0);
            prefs.edit().putInt(updateKey, version)
                    .putBoolean(hasKey, !infos.isEmpty())
                    .commit();
        }

        return prefs.getBoolean(hasKey, true);
    }

    public static boolean isAnyCameraAvailable(Context context) {
        int version = PackagesMonitor.getPackagesVersion(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt(KEY_CAMERA_UPDATE, 0) != version) {
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> infos = packageManager.queryIntentActivities(
                    new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA), 0);
            prefs.edit().putInt(KEY_CAMERA_UPDATE, version)
                    .putBoolean(KEY_HAS_CAMERA, !infos.isEmpty())
                    .commit();
        }
        return prefs.getBoolean(KEY_HAS_CAMERA, true);
    }

    public static boolean isCameraAvailable(Context context) {
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-14,PR839331 begin
        if (sCameraAvailableInitialized) return sCameraAvailable;
        PackageManager pm = context.getPackageManager();
        ComponentName name = new ComponentName(context, CAMERA_LAUNCHER_NAME);
        int state = pm.getComponentEnabledSetting(name);
        sCameraAvailableInitialized = true;
        sCameraAvailable =
                (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
                        || (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
        return sCameraAvailable;
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-14,PR839331 end
    }

    public static void startCameraActivity(Context context) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-05-25, PR1009795
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // This will only occur if Camera was disabled while Gallery is open
            // since we cache our availability check. Just abort the attempt.
            Log.e(TAG, "Camera activity previously detected but cannot be found", e);
        }
    }

    public static void startGalleryActivity(Context context) {
        Intent intent = new Intent(context, GalleryActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isValidLocation(double latitude, double longitude) {
        // TODO: change || to && after we fix the default location issue
        return (latitude != MediaItem.INVALID_LATLNG || longitude != MediaItem.INVALID_LATLNG);
    }

    public static String formatLatitudeLongitude(String format, double latitude,
                                                 double longitude) {
        // We need to specify the locale otherwise it may go wrong in some language
        // (e.g. Locale.FRENCH)
        return String.format(Locale.ENGLISH, format, latitude, longitude);
    }

    public static void showOnMap(Context context, double latitude, double longitude) {
        try {
            // We don't use "geo:latitude,longitude" because it only centers
            // the MapView to the specified location, but we need a marker
            // for further operations (routing to/from).
            // The q=(lat, lng) syntax is suggested by geo-team.
            String uri = formatLatitudeLongitude("http://maps.google.com/maps?f=q&q=(%f,%f)",
                    latitude, longitude);
            ComponentName compName = new ComponentName(MAPS_PACKAGE_NAME,
                    MAPS_CLASS_NAME);
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(uri)).setComponent(compName);
            context.startActivity(mapsIntent);
        } catch (ActivityNotFoundException e) {
            // Use the "geo intent" if no GMM is installed
            Log.e(TAG, "GMM activity not found!", e);
            String url = formatLatitudeLongitude("geo:%f,%f", latitude, longitude);
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(mapsIntent);
        }
    }

    public static void setViewPointMatrix(
            float matrix[], float x, float y, float z) {
        // The matrix is
        // -z,  0,  x,  0
        //  0, -z,  y,  0
        //  0,  0,  1,  0
        //  0,  0,  1, -z
        Arrays.fill(matrix, 0, 16, 0);
        matrix[0] = matrix[5] = matrix[15] = -z;
        matrix[8] = x;
        matrix[9] = y;
        matrix[10] = matrix[11] = 1;
    }

    public static int getBucketId(String path) {
        return path.toLowerCase().hashCode();
    }

    // Return the local path that matches the given bucketId. If no match is
    // found, return null
    public static String searchDirForPath(File dir, int bucketId) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String path = file.getAbsolutePath();
                    if (GalleryUtils.getBucketId(path) == bucketId) {
                        return path;
                    } else {
                        path = searchDirForPath(file, bucketId);
                        if (path != null) return path;
                    }
                }
            }
        }
        return null;
    }

    // Returns a (localized) string for the given duration (in seconds).
    public static String formatDuration(final Context context, int duration) {
        int h = duration / 3600;
        int m = (duration - h * 3600) / 60;
        int s = duration - (h * 3600 + m * 60);
        String durationValue;
        if (h == 0) {
            if (m == 0 && s == 0) s = 1;//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-09,PR1759891
            durationValue = String.format(context.getString(R.string.details_ms), m, s);
        } else {
            durationValue = String.format(context.getString(R.string.details_hms), h, m, s);
        }
        return durationValue;
    }

    @TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
    public static int determineTypeBits(Context context, Intent intent) {
        int typeBits = 0;
        String type = intent.resolveType(context);

        if (MIME_TYPE_ALL.equals(type)) {
            typeBits = DataManager.INCLUDE_ALL;
        } else if (MIME_TYPE_IMAGE.equals(type) ||
                DIR_TYPE_IMAGE.equals(type)) {
            typeBits = DataManager.INCLUDE_IMAGE;
        } else if (MIME_TYPE_VIDEO.equals(type) ||
                DIR_TYPE_VIDEO.equals(type)) {
            typeBits = DataManager.INCLUDE_VIDEO;
        } else {
            typeBits = DataManager.INCLUDE_ALL;
        }

        if (ApiHelper.HAS_INTENT_EXTRA_LOCAL_ONLY) {
            if (intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false)) {
                typeBits |= DataManager.INCLUDE_LOCAL_ONLY;
            }
        }

        return typeBits;
    }

    public static int getSelectionModePrompt(int typeBits) {
        if ((typeBits & DataManager.INCLUDE_VIDEO) != 0) {
            return (typeBits & DataManager.INCLUDE_IMAGE) == 0
                    ? R.string.select_video
                    : R.string.select_item;
        }
        return R.string.select_image;
    }

    public static boolean hasSpaceForSize(long size) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        }

        String path = Environment.getExternalStorageDirectory().getPath();
        try {
            StatFs stat = new StatFs(path);
            return stat.getAvailableBlocks() * (long) stat.getBlockSize() > size;
        } catch (Exception e) {
            Log.i(TAG, "Fail to access external storage", e);
        }
        return false;
    }

    public static boolean isPanorama(MediaItem item) {
        if (item == null) return false;
        int w = item.getWidth();
        int h = item.getHeight();
        return (h > 0 && w / h >= 2);
    }

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-11-20, PR839655 begin
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-08-07,PR1062955 begin
    public static String getRealType(GalleryApp application, Path itemPath) {
        String type = "";
        String[] segment = itemPath.split();
        String uriStr = null;
        try {
            uriStr = URLDecoder.decode(segment[1], "utf-8");
            type = URLDecoder.decode(segment[2], "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String realType = getRealType(application, uriStr);
        if (realType != null) {
            return realType;
        }
        return type;
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-08-07,PR1062955 end

    public static String getRealType(GalleryApp application, String filePath) {
        if (filePath == null || filePath.length() == 0) {
            return null;
        }

        String type = null;
        try {
            InputStream is = application.getContentResolver().openInputStream(Uri.parse(filePath));
            byte[] b = new byte[3];
            is.read(b, 0, b.length);

            if (b == null || b.length <= 0) {
                return "image/*";
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < b.length; i++) {
                int v = b[i] & 0xFF;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hv);
            }
            String head = stringBuilder.toString();
            switch (head) {
                case "ffd8ff":
                    type = "image/jpg";
                    break;
                case "89504e47":
                    type = "image/png";
                    break;
                case "474946":
                    type = "image/gif";
                    break;
                case "424d":
                    type = "image/bmp";
                    break;
                default:
                    type = "image/*";
                    break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return type;
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-11-20, PR839655 end

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-07, PR1080140 begin
    private static boolean sSystemIs24HourFormat = false;

    public static boolean getSystemIs24HourFormat() {
        return sSystemIs24HourFormat;
    }

    public static void getSystemHourFormat(Context context) {
        sSystemIs24HourFormat = DateFormat.is24HourFormat(context);
    }

    private static java.text.DateFormat sDateFormat = null;

    public static java.text.DateFormat getSystemDateFormat() {
        return sDateFormat;
    }

    public static void getSystemDateFormat(Context context) {
        sDateFormat = DateFormat.getDateFormat(context);
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-07, PR1080140 end

    public static StringBuffer buildStringByList(List<String> list) {
        StringBuffer sb = new StringBuffer();
        if (list == null || list.size() == 0) {
            sb.append("(-1)");
        } else {
            sb.append('(');
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));
                if (i == list.size() - 1) {
                    sb.append(')');
                } else {
                    sb.append(',');
                }
            }
        }
        return sb;
    }

    public static boolean hasFaceShowAPK(Context context) {
        boolean result = false;
        Intent intent = new Intent();
        intent.setAction("com.muvee.faceshow.FACE_SHOW");
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> info = manager.queryIntentActivities(intent,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        if (info.size() > 0) {
            result = true;
        } else {
            Log.e(TAG, "The faceshow APK is not found.");
        }
        return result;
    }

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-26,Defect:1496305 begin
    public static boolean hasFyusionApk(Context context) {
        String fyusionPackageName = FyuseAPI.FYUSE_PACKAGE_NAME;
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(
                    fyusionPackageName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "The Fyusion Apk is not found.");
        }
        return info != null;
    }
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-26,Defect:1496305 end

    public static boolean hasSlowMotionApk(Context context) {
        String packageName = "com.muvee.slowmoedit";
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "The SlowMotion Apk is not found.");
        }
        return info != null ? true : false;
    }

    public static boolean hasSlideShowApk(Context context) {
        String packageName = "com.muvee.boomkey.picture";
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "The SlideShow Apk is not found.");
        }
        return info != null ? true : false;
    }

    public static boolean hasVideoEditApk(Context context) {
        String packageName = "com.muvee.iclipeditor";
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "The Video Edit Apk is not found.");
        }
        return info != null ? true : false;
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-09,PR839832 begin
    public static boolean isVideoEditorAvailable(Object duration) {
        boolean videoCanEdit = false;
        if (duration instanceof String) {
            String[] time = ((String) duration).split(":");
            if (Integer.parseInt(time[time.length - 2]) > 0 || Integer.parseInt(time[time.length - 1]) >= 3) {//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-16,PR1401750
                videoCanEdit = true;
            }
        }
        return videoCanEdit;
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-09,PR839832 end

    //[BUGFIX]-Modify by TCTNJ,hao.yin, 2016-03-05,PR1716893 begin
    public static boolean isVideoTrimAvailable(Object duration) {
        boolean videoCanTrim = false;
        if (duration instanceof String) {
            String[] time = ((String) duration).split(":");
            if (Integer.parseInt(time[time.length - 2]) > 0 || Integer.parseInt(time[time.length - 1]) >= 3) {//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-08,PR1758674
                videoCanTrim = true;
            }
        }
        return videoCanTrim;
    }
    //[BUGFIX]-Modify by TCTNJ,hao.yin, 2016-03-05,PR1716893 end

    public static void needShowDrmWifidisplyDiaog(final Context context) {
        WifiDisplayStatus wifiDisplayStatus = DisplayManagerGlobal.getInstance().getWifiDisplayStatus();
        int HDCP_ENABLE = Settings.Global.getInt(context.getContentResolver(), GalleryActivity.TCT_HDCP_DRM_NOTIFY, 0);
        if ((wifiDisplayStatus.getActiveDisplayState() == WifiDisplayStatus.DISPLAY_STATE_CONNECTED)
                && (GalleryActivity.WIFI_DISPLAY_DRM_NOTIFY == 1) && (0 == HDCP_ENABLE)) {
            GalleryActivity.WIFI_DISPLAY_DRM_NOTIFY = 0;

            new AlertDialog.Builder(context).setIcon(R.drawable.ic_dialog_alert_holo_light)
                    .setTitle(R.string.drm_wifidisplay_title).setMessage(R.string.drm_wifidisplay_message)
                    .setPositiveButton(R.string.drm_wifidisplay_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dlg, int sumthin) {
                            DisplayManagerGlobal.getInstance().disconnectWifiDisplay();
                            Toast.makeText(context, R.string.tv_link_close, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.drm_wifidisplay_continue, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dlg, int sumthin) {
                        }
                    }).show();
        }
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-01-08,PR1159002 begin
    public static Uri parseUriFromPath(String path, Activity context) {
        Uri uri = Uri.parse(path);
        ;
        if (path != null) {
            path = Uri.decode(path);
            Cursor cursor = null;
            try {
                Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;
                String[] projection = new String[]{VideoColumns._ID};
                String selection = "(" + VideoColumns.DATA + "=" + "'"
                        + path + "'" + ")";
                cursor = context.getContentResolver().query(baseUri, projection,
                        selection, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(projection[0]);
                    uri = Uri.parse("content://media/external/video/media/"
                            + cursor.getInt(index));
                    Log.e(TAG, "uri = " + uri);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        return uri;
    }

    public static boolean isBoomkeyAvaiable(Activity context) {
        return Settings.System.getInt(context.getContentResolver(), "boom_key_unlock_enable", 0) == 1 ?
                true : false;
    }

    public static int getScreenWidth(Context context) {
        WindowManager manager = ((Activity) context).getWindowManager();
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    public static int getScreenHeight(Context context) {
        WindowManager manager = ((Activity) context).getWindowManager();
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }

    public static int createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {
            return KEY_FILE_EXISTS;
        }
        if (dir.mkdirs()) {
            return KEY_FILE_CREATE_SUCCESS;
        } else {
            return KEY_FILE_CREATE_FAIL;
        }
    }

    public static String[] getProjection() {
        String[] projection = null;
        if (!GalleryAppImpl.sHasPrivateColumn) {
            if (DrmManager.mCurrentDrm == DrmManager.QCOM_DRM) {
                if (GalleryAppImpl.sHasNewColumn) {
                    projection = MomentsNewAlbum.NEW_PROJECTION_DRM;
                } else {
                    projection = MomentsNewAlbum.PROJECTION_DRM;
                }
            } else if (DrmManager.mCurrentDrm == DrmManager.MTK_DRM) {
                if (GalleryAppImpl.sHasNewColumn) {
                    projection = MomentsNewAlbum.NEW_PROJECTION_MTK;
                } else {
                    projection = MomentsNewAlbum.PROJECTION_MTK;
                }
            } else {
                if (GalleryAppImpl.sHasNewColumn) {
                    projection = MomentsNewAlbum.NEW_PROJECTION;
                } else {
                    projection = MomentsNewAlbum.PROJECTION;
                }
            }
        } else {
            if (DrmManager.mCurrentDrm == DrmManager.QCOM_DRM) {
                if (GalleryAppImpl.sHasNewColumn) {
                    projection = MomentsNewAlbum.PRIVATE_NEW_PROJECTION_DRM;
                } else {
                    projection = MomentsNewAlbum.PRIVATE_PROJECTION_DRM;
                }
            } else if (DrmManager.mCurrentDrm == DrmManager.MTK_DRM) {
                if (GalleryAppImpl.sHasNewColumn) {
                    projection = MomentsNewAlbum.PRIVATE_NEW_PROJECTION_MTK;
                } else {
                    projection = MomentsNewAlbum.PRIVATE_PROJECTION_MTK;
                }
            } else {
                if (GalleryAppImpl.sHasNewColumn) {
                    projection = MomentsNewAlbum.PRIVATE_NEW_PROJECTION;
                } else {
                    projection = MomentsNewAlbum.PRIVATE_PROJECTION;
                }
            }
        }
        return projection;
    }

    public static ArrayList<MediaObject> getMediaObjectsByPath(ArrayList<Path> unexpandedPaths, AbstractGalleryActivity activity) {
        if (unexpandedPaths.isEmpty()) {
            return null;
        }
        ArrayList<MediaObject> selected = new ArrayList<MediaObject>();
        DataManager manager = activity.getDataManager();
        for (Path path : unexpandedPaths) {
            MediaObject mediaObject = manager.getMediaObject(path);
            if (mediaObject != null) {
                selected.add(mediaObject);
            }
        }
        return selected;
    }

}

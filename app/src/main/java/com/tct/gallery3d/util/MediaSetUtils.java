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
/* 04/21/2015|chengbin.du           |PR970033              |[Enduser Feedback][Monitor][SDCard]NO images available after set SD card as default storage */
/* ----------|----------------------|----------------------|----------------- */
/* 22/04/2015|dongliang.feng        |CR979027              |[5.0][Gallery] album sorting method */
/* ----------|----------------------|----------------------|----------------- */
/* 19/11/2015|chengbin.du-nb        |ALM-940132            |[Android 6.0][Gallery_v5.2.3.1.1.0307.0]Gallery moments view should display images&videos include "Pictures" folder*/
/* ----------|----------------------|----------------------|----------------- */
/* 16/12/2015|chengbin.du-nb        |ALM-1170791           |Momments display vGallery.*/
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;

import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.data.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class MediaSetUtils {
    private static final String TAG = "MediaSetUtils";
    public static final Comparator<MediaSet> NAME_COMPARATOR = new NameComparator();

    public static final int CAMERA_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
            + BucketNames.CAMERA);
    public static final int DOWNLOAD_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
            + BucketNames.DOWNLOAD);
    public static final int EDITED_ONLINE_PHOTOS_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
            + BucketNames.EDITED_ONLINE_PHOTOS);
    public static final int IMPORTED_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
            + BucketNames.IMPORTED);
    public static final int SNAPSHOT_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() +
            "/" + BucketNames.SCREENSHOTS);
    public static final int PICTURES_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() +
            "/" + BucketNames.PICTURES);
    public static final int SDCARD_CAMERA_BUCKET_ID = GalleryUtils.getBucketId("/storage/sdcard1/DCIM/Camera");//[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-04-21,PR970033
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-22, CR979027 begin
    public static final int SDCARD_DOWNLOAD_BUCKET_ID = GalleryUtils.getBucketId("/storage/sdcard1/Download");
    public static final int SDCARD_SNAPSHOT_BUCKET_ID = GalleryUtils.getBucketId("/storage/sdcard1/Pictures/Screenshots");
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-22, CR979027 end
    public static final int SDCARD_PICTURES_BUCKET_ID = GalleryUtils.getBucketId("/storage/sdcard1/Pictures");

    private static final Path[] CAMERA_PATHS = {
            Path.fromString("/local/all/" + CAMERA_BUCKET_ID),
            Path.fromString("/local/image/" + CAMERA_BUCKET_ID),
            Path.fromString("/local/video/" + CAMERA_BUCKET_ID),

            //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-04-21,PR970033 begin
            Path.fromString("/local/all/" + SDCARD_CAMERA_BUCKET_ID),
            Path.fromString("/local/image/" + SDCARD_CAMERA_BUCKET_ID),
            Path.fromString("/local/video/" + SDCARD_CAMERA_BUCKET_ID),
            Path.fromString("/local/camera")};
            //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-04-21,PR970033 end

    public static boolean isCameraSource(Path path) {
        //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-04-21,PR970033 begin
        return CAMERA_PATHS[0] == path || CAMERA_PATHS[1] == path
                || CAMERA_PATHS[2] == path || CAMERA_PATHS[3] == path
                || CAMERA_PATHS[4] == path || CAMERA_PATHS[5] == path
                || CAMERA_PATHS[6] == path;
        //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-04-21,PR970033 end
    }

    // Sort MediaSets by name
    public static class NameComparator implements Comparator<MediaSet> {
        @Override
        public int compare(MediaSet set1, MediaSet set2) {
            int result = set1.getName().compareToIgnoreCase(set2.getName());
            if (result != 0) return result;
            return set1.getPath().toString().compareTo(set2.getPath().toString());
        }
    }

    public static ArrayList<MediaItem> subList(ArrayList<MediaItem> list, int start, int end) {
        ArrayList<MediaItem> result = new ArrayList<MediaItem>();
        for (int i = start; i < end; i++) {
            result.add(list.get(i));
        }
        return result;
    }

    public static ArrayList<Integer> getMomentsBucketsId(ContentResolver contentResolver, Uri uri) {
        final String sdcard0 = Environment.getExternalStorageDirectory().toString() + "/";
        final String sdcard1 = "/storage/sdcard1/";
        String[] directores = new String[] {
                sdcard0 + Environment.DIRECTORY_DCIM,
                sdcard0 + Environment.DIRECTORY_PICTURES,
                sdcard0 + Environment.DIRECTORY_DOWNLOADS,
                sdcard1 + Environment.DIRECTORY_DCIM,
                sdcard1 + Environment.DIRECTORY_PICTURES,
                sdcard1 + Environment.DIRECTORY_DOWNLOADS };
        String[] projection = {
                ImageColumns.BUCKET_ID,
                FileColumns.DATA
        };
        String selection =
                FileColumns.MEDIA_TYPE + "=" +
                FileColumns.MEDIA_TYPE_IMAGE + " OR " +
                FileColumns.MEDIA_TYPE + "=" +
                FileColumns.MEDIA_TYPE_VIDEO +
                " ) GROUP BY ( " + ImageColumns.BUCKET_ID;
        String[] selectionArgs = null;
        String sortOrder = null;
        Cursor cursor = null;
        ArrayList<Integer> all = new ArrayList<Integer>();
        try {
            cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int bucketIdIndex = cursor.getColumnIndex(ImageColumns.BUCKET_ID);
                    int dataIndex = cursor.getColumnIndex(FileColumns.DATA);
                    int bucketId = cursor.getInt(bucketIdIndex);
                    String path = cursor.getString(dataIndex);
                    if (path.startsWith(directores[0]) ||
                            path.startsWith(directores[1]) ||
                            path.startsWith(directores[2]) ||
                            path.startsWith(directores[3]) ||
                            path.startsWith(directores[4]) ||
                            path.startsWith(directores[5])) {
                        all.add(Integer.valueOf(bucketId));
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "MediaSetUtils.getMomentsBucketsId error : " + e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return all;
    }

    public static int[] getMomentsBucketsId() {
        Log.d(TAG, "MediaSetUtils.getMomentsBucketsId {");
        final String sdcard0 = Environment.getExternalStorageDirectory().toString() + "/";
        final String sdcard1 = "/storage/sdcard1/";
        String[] directores = new String[] {
                sdcard0 + Environment.DIRECTORY_DCIM,
                sdcard0 + Environment.DIRECTORY_PICTURES,
                sdcard0 + Environment.DIRECTORY_DOWNLOADS,
                sdcard1 + Environment.DIRECTORY_DCIM,
                sdcard1 + Environment.DIRECTORY_PICTURES,
                sdcard1 + Environment.DIRECTORY_DOWNLOADS };

        ArrayList<String> result = new ArrayList<String>();
        for(String path : directores) {
            ArrayList<String> list = getAllDirectories(path);
            if(list.size() > 0) {
                result.addAll(list);
            }
        }

        int[] all = new int[result.size()];
        for(int i = 0; i < all.length; i++) {
            all[i] = GalleryUtils.getBucketId(result.get(i));
        }
        Log.d(TAG, "} MediaSetUtils.getMomentsBucketsId");
        return all;
    }

    private static ArrayList<String> getAllDirectories(String path) {
        ArrayList<String> result = new ArrayList<String>();
        File root = new File(path);
        if(root.isDirectory()) {
            result.add(root.getPath());
            ArrayList<String> list = getSubDirectories(path);
            if(list.size() > 0) {
                result.addAll(list);
            }
        }
        return result;
    }

    private static ArrayList<String> getSubDirectories(String path) {
        ArrayList<String> directories = new ArrayList<String>();
        int bucketId = GalleryUtils.getBucketId(path);
        if(CAMERA_BUCKET_ID == bucketId || SDCARD_CAMERA_BUCKET_ID == bucketId) {
            Log.e(TAG, "getSubDirectories from camera");
            return directories;
        }

        Log.e(TAG, "getSubDirectories path=" + path);
        File root = new File(path);
        if(root.isDirectory()) {
            File files[] = root.listFiles();
            if(files != null) {
                for(File file : files) {
                    if(file.isDirectory() && !file.isHidden()) {
                        directories.add(file.getPath());
                        directories.addAll(getSubDirectories(file.getPath()));
                    }
                }
            }
        }
        return directories;
    }
}

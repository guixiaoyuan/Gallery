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
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 04/11/2015|chengbin.du-nb        |ALM-814521            |[Android 5.1][Gallery_v5.2.3.1.0309.0][Monitor]The pictures in camera album don't display in moments*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 05/11/2015|chengbin.du-nb        |ALM-865458            |[Android 5.1][Gallery_v5.2.3.1.0310.0]burstshot image can not display in moment view*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 11/11/2015|chengbin.du-nb        |ALM-899577            |[Android5.1][Gallery_v5.2.3.1.0310.0]retrieve slowmotion video and micro video info*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 12/02/2015| jian.pan1            | [ALM]Defect:1018372  |[Android6.0][Gallery_v5.2.4.1.0317.0][GD]The slo.mo vide not display duration on moments and albums interface
/* ----------|----------------------|----------------------|----------------- */
/* 21/12/2015|chengbin.du-nb        |ALM-1121296           |[Gallery]Fyuse pictures can't display in gallery after capture a Fyuse picture
/* ----------|----------------------|----------------------|----------------- */
/* 24/12/2015|chengbin.du-nb        |ALM-1157354           |[Monitor][Gallery]Gallery stop when delete burst mode picture in file manager
/* ----------|----------------------|----------------------|----------------- */
/* 02/22/2016| jian.pan1            |[ALM]Defect:1606899   |[Gallery][FC] Popup"Unfortunately,Gallery has stopped" when select Month view
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.picturegrouping;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference; //MODIFIED by jian.pan1, 2016-04-05,BUG-1892017
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.tct.gallery3d.BuildConfig;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.fyuse.ContentConstants;
import com.tct.gallery3d.data.GappTypeInfo;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.exif.ExifInterface;
import com.tct.gallery3d.picturegrouping.ExifInfoCache.ExifItem;
import com.tct.gallery3d.util.MediaSetUtils;
import com.tct.gallery3d.util.PermissionUtil;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.util.JsonReader;
import android.util.Log;

public class ExifInfoFilter {
    private static final String TAG = "ExifInfoFilter";
    private static final boolean Debug = false & BuildConfig.DEBUG;
    private static final String VOLUME_NAME = "external";

    private static final String BurstShotId = "burst_shot_id";
    private static final String BurstShotIndex = "burst_shot_index";
    private static final String CaptureMode = "capture_mode";
    private static final String Parallax = "parallax";
    private static final String Panorama = "panorama";
    private static final String FaceShow = "faceshow";

    private static final String METADATA_KEY_CAPTURE_FRAMERATE = "METADATA_KEY_CAPTURE_FRAMERATE";
    private static final String METADATA_KEY_VIDEO_WIDTH = "METADATA_KEY_VIDEO_WIDTH";
    private static final String METADATA_KEY_VIDEO_HEIGHT = "METADATA_KEY_VIDEO_HEIGHT";

    private static final int SlowMotionFramerate = 90;

    public static final int NONE = 0;
    public static final int NORMAL = 1;
    public static final int PARALLAX = 8;
    public static final int PANORAMA = 4;
    public static final int NORMALVIDEO = 5;
    public static final int FACESHOW = 3;
    public static final int SLOWMOTION = 6;
    public static final int MICROVIDEO = 7;
    public static final int BURSTSHOTS = 2;
    public static final int BURSTSHOTSHIDDEN = 9;

    //private Handler mBgHandler;
    //private Handler mFgHandler;

    private static ExifInfoFilter mInstance = null;
    private Hashtable<String, ArrayList<ExifItem>> mBurstShotCache;
    private ExifInfoCache mExifInfoCache = null;
    private ArrayList<FilterSourceListener> mFilterSourceListener = null;
    private ContentResolver mContentResolver = null;
    private Uri mUri;

    public static interface FilterSourceListener {
        public void onSourceChanged();
    }

    private ExifInfoFilter(Context context) {
        mExifInfoCache = new ExifInfoCache(context);
        mBurstShotCache = mExifInfoCache.prepareBurstCache();
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-15,ALM-1551584 begin
        int[] grantResult = PermissionUtil.checkPermissions(context, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE} );
        if(grantResult[0] == PackageManager.PERMISSION_GRANTED) {
            checkBurstShotImageAvailability(context);
        }
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-15,ALM-1551584 end
        mFilterSourceListener = new ArrayList<FilterSourceListener>();
        mContentResolver = context.getContentResolver();
        mUri = MediaStore.Files.getContentUri(VOLUME_NAME);
    }

    public static ExifInfoFilter getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new ExifInfoFilter(context);
        }
        return mInstance;
    }

    public void registerFilterSourceListener(FilterSourceListener listener) {
        synchronized(mFilterSourceListener) {
            mFilterSourceListener.add(listener);
        }
    }

    public void removeFilterSourceListener(FilterSourceListener listener) {
        synchronized(mFilterSourceListener) {
            mFilterSourceListener.remove(listener);
        }
    }

    public void notifySaveExifCache() {
        mExifInfoCache.saveToDatabase();
    }

    public synchronized int filter(String id, String path, int mediaType, long modifyTime, boolean needSave, boolean fromCamera, GappTypeInfo gappTypeInfo) {
        int type = queryType(id);
        if(type != NONE) {
            return type;
        }
        if (gappTypeInfo != null && gappTypeInfo.getType()!= NONE) {
            type = queryTypeFast(gappTypeInfo, id, path, modifyTime, needSave, fromCamera);
            if (type != NONE) {
                Log.d(TAG, "query from db -----");
                return type;
            }
        }
        if(mediaType == FileColumns.MEDIA_TYPE_IMAGE) {
            type = filterImage(id, path, modifyTime, fromCamera);
        } else if(mediaType == FileColumns.MEDIA_TYPE_VIDEO) {
            type = filterVideo(id, path, modifyTime);
        } else {
            throw new IllegalArgumentException("mediaType error");
        }

        if (type != BURSTSHOTS && type != BURSTSHOTSHIDDEN) {
            try {
                if (GalleryAppImpl.sHasNewColumn) {
                    String whereClause = FileColumns._ID + " = ? ";
                    ContentValues values = new ContentValues();
                    values.put(GappTypeInfo.GAPP_MEDIA_TYPE, type);
                    mContentResolver.update(mUri, values, whereClause, new String[]{id});
                }
            }catch (Exception e){
                Log.d(TAG,e.getMessage());
            }
            mExifInfoCache.prefetchExifInfo(id, type, needSave, path);   //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909
        }
        return type;
    }

    private int queryTypeFast(GappTypeInfo gappTypeInfo, String id, String path, long modifyTime, boolean needSave, boolean fromCamera) {
        int type = NONE;
        try {
            if (gappTypeInfo.getType() != NONE && gappTypeInfo.getType() != BURSTSHOTS) {
                type = gappTypeInfo.getType();
            } else if(gappTypeInfo.getType() == BURSTSHOTS) {
                int burstId = gappTypeInfo.getBurstshotId();
                int burstIndex = gappTypeInfo.getBurstshotIndex();
                if (burstId != 0) {
                    ExifItem item = new ExifItem(id, NORMAL, String.valueOf(burstId), String.valueOf(burstIndex), path, String.valueOf(modifyTime));
                    type = addNewBurstShotCache(item, fromCamera);
                    mExifInfoCache.prefetchBurstExif(item);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (type != BURSTSHOTS && type != BURSTSHOTSHIDDEN && type != NONE) {
            mExifInfoCache.prefetchExifInfo(id, type, needSave, path);   //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909
        }
        return type;
    }

    private int filterImage(String id, String path, long modifyTime, boolean fromCamera) {
        int type = NORMAL;
        String burstShotId = null;
        String burstShotIndex = null;
        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
        String userComment = null;
        try {
            userComment = getUserComment(path);
        } catch (Exception e) {
        }
        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
        if(userComment == null || userComment.length() == 0) {
            type = NORMAL;
        } else {
            HashMap<String, String> result = parseUserComment(userComment);
            if(result.size() > 0) {
                if(result.containsKey(BurstShotId) && result.containsKey(BurstShotIndex)) {
                    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-05,ALM-865458 begin
                    burstShotId = result.get(BurstShotId);
                    burstShotIndex = result.get(BurstShotIndex);
                    if(burstShotId != null && burstShotIndex != null) {
                        if(burstShotIndex.length() == 0) {
                            burstShotIndex = "0";
                        }
                        ExifItem item = new ExifItem(id, NORMAL, burstShotId, burstShotIndex, path, String.valueOf(modifyTime));
                        type = addNewBurstShotCache(item, fromCamera);
                        mExifInfoCache.prefetchBurstExif(item);
                    } else {
                        Log.e(TAG, "BurstShotId value or BurstShotIndex value is null!");
                    }
                    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-05,ALM-865458 end
                } else if(result.containsKey(CaptureMode)) {
                    String captureMode = result.get(CaptureMode);
                    if(captureMode != null) {
                        Log.d(TAG, "capture mode is " + captureMode);
                        if(Parallax.equals(captureMode)) {
                            type = PARALLAX;
                        } else if(Panorama.equals(captureMode)) {
                            type = PANORAMA;
                        } else if(FaceShow.equals(captureMode)) {
                            type = FACESHOW;
                            notifySourceChange();
                        }
                    } else {
                        Log.e(TAG, "CaptureMode value is null!");
                    }
                } else {
                    type = NORMAL;
                }
            } else {
                type = NORMAL;
            }
        }
        try {
            if (GalleryAppImpl.sHasNewColumn) {
                if (type == BURSTSHOTS || type == BURSTSHOTSHIDDEN) {
                    String whereClause = FileColumns._ID + " = ? ";
                    ContentValues values = new ContentValues();
                    values.put(GappTypeInfo.GAPP_MEDIA_TYPE, BURSTSHOTS);
                    values.put(GappTypeInfo.GAPP_BURST_ID, Integer.valueOf(burstShotId));
                    values.put(GappTypeInfo.GAPP_BURST_INDEX, Integer.valueOf(burstShotIndex));
                    mContentResolver.update(mUri, values, whereClause, new String[]{id});
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return type;
    }
    private int filterVideo(String id, String path, long modifyTime) {
        int type = NONE;
        HashMap<String, Integer> metadataMap = retrieveMediaMetadata(path);

        Integer framerate = metadataMap.get(METADATA_KEY_CAPTURE_FRAMERATE);
        // [ALM][BUGFIX]-Add by TSNJ,zhe.xu, 2015-12.30, 1208338.
        if(framerate != null && framerate > SlowMotionFramerate && framerate < 1000) {
            type = SLOWMOTION;
        } else {
            Integer videoWidth = metadataMap.get(METADATA_KEY_VIDEO_WIDTH);
            Integer videoHeight = metadataMap.get(METADATA_KEY_VIDEO_HEIGHT);
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-02,Defect:1018372 begin
            if (videoWidth != null && videoHeight != null
                    && videoWidth.compareTo(videoHeight) == 0
                        // [ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2016-1-12,ALM-1252865 begin
                        && !DrmManager.getInstance().isDrm(path)) {
                         // [ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2016-1-12,ALM-1252865 end
                // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-02,Defect:1018372 end
                type = MICROVIDEO;
            } else {
                type = NORMALVIDEO;
            }
        }
        return type;
    }

    public int queryType(String id) {
        Integer type = mExifInfoCache.getType(id);
        return (type == null) ? NONE : type;
    }

    public ArrayList<String> queryBurstShots(String burstShotId) {
        ArrayList<String> burstShots = null;
        ArrayList<ExifItem> burstShotArray = (ArrayList<ExifItem>) (mBurstShotCache.get(burstShotId));
        if(burstShotArray != null) {
            burstShots = new  ArrayList<String>(burstShotArray.size());
            for(ExifItem item : burstShotArray) {
                burstShots.add(item.id);
                if(Debug) {
                    Log.d(TAG, "queryBurstShots path " + item.path);
                }
            }

            //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-05,ALM-865458 end
            if(burstShots.size() > 1) {
                Collections.sort(burstShots, new Comparator<String>() {
                    @Override
                    public int compare(String lhs, String rhs) {
                        int lValue = Integer.parseInt(lhs);
                        int rValue = Integer.parseInt(rhs);
                        if(lValue < rValue) return -1;
                        else if(lValue > rValue) return 1;
                        return 0;
                    }
                });
            }
            //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-05,ALM-865458 end
        }
        return burstShots;
    }

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-17,BUG-2208330*/
    public ArrayList<String> queryBurstShotsPath(String burstShotId) {
        ArrayList<String> burstShotsPath = null;
        ArrayList<ExifItem> burstShotPathArray = (ArrayList<ExifItem>) (mBurstShotCache.get(burstShotId));
        if (burstShotPathArray != null) {
            burstShotsPath = new ArrayList<String>();
            for (ExifItem item : burstShotPathArray) {
                burstShotsPath.add(item.path);
                if (Debug) {
                    Log.d(TAG, "queryBurstShots path " + item.path);
                }
            }
        }
        return burstShotsPath;
    }

    public ArrayList<ExifItem> getBurstShotItem(String burstShotId) {
        ArrayList<ExifItem> burstShotPathArray = (ArrayList<ExifItem>) (mBurstShotCache.get(burstShotId));
        return burstShotPathArray;
    }
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    public String queryBurstShotId(String contentId) {
        if(contentId == null || contentId.length() == 0) return null;

        ExifItem exifItem = mExifInfoCache.getExifItem(contentId);
        if (exifItem != null) {
            return exifItem.burstId;
        }
        return null;
    }

    public void cleanBurstShotImageCache() {
        Set<String> keys = mBurstShotCache.keySet();
        Iterator<String> iter = keys.iterator();
        while(iter.hasNext()) {
            String key = (String)iter.next();
            ArrayList<ExifItem> items = mBurstShotCache.get(key);
            for(int i = 0; i < items.size(); i++) {
                mExifInfoCache.removeCachedExifInfo(items.get(i).id);
            }
        }
        mBurstShotCache.clear();
    }

    public void checkBurstShotImageAvailability(Context context) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = { FileColumns._ID };

        Set<String> keys = mBurstShotCache.keySet();
        Iterator<String> iter = keys.iterator();
        while(iter.hasNext()) {
            String key = (String)iter.next();
            ArrayList<ExifItem> items = mBurstShotCache.get(key);
            ArrayList<String> idArray = new ArrayList<String>(items.size());

            StringBuffer buf = new StringBuffer();
            buf.append(FileColumns._ID);
            buf.append(" IN ");
            buf.append("(");
            for(int i = 0; i < items.size(); i++) {
                idArray.add(items.get(i).id);

                buf.append(items.get(i).id);
                if(i < items.size() - 1)
                   buf.append(",");
            }
            buf.append(")");
            String selection = buf.toString();
          //[BUGFIX]-modify by TCTNJ,xinrong.wang, 2016-02-03,PR1541166 begin
            Cursor cursor = null;
                try {
                    cursor=cr.query(uri, projection, selection, null, null);
                    if(cursor != null) {
                    while(cursor.moveToNext()) {
                        String id = cursor.getString(0);
                        idArray.remove(id);
                    }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "checkBurstShotImageAvailability", e);
                } finally {
                    if(cursor != null) {
                    cursor.close();
                    }
                }
            //[BUGFIX]-modify by TCTNJ,xinrong.wang, 2016-02-03,PR1541166 end
            if (idArray.size() > 0) {
                ArrayList<ExifItem> needRemove = new ArrayList<ExifItem>(idArray.size());
                boolean hiddenChange = false;
                for (int i = 0; i < idArray.size(); i++) {
                    String id = idArray.get(i);
                    for (int j = 0; j < items.size(); j++) {
                        ExifItem exifItem = items.get(j);
                        if (id.endsWith(exifItem.id)) {
                            if (exifItem.type == BURSTSHOTS) {
                                hiddenChange = true;
                            }
                            needRemove.add(exifItem);
                            mExifInfoCache.removeCachedExifInfo(exifItem.id);
                            break;
                        }
                    }
                }
                items.removeAll(needRemove);

                if (hiddenChange && items.size() > 0) {
                    ExifItem exifItem = items.get(0);
                    exifItem.type = BURSTSHOTS;
                    mExifInfoCache.updateBurstExifType(exifItem, true);
                }
            }
        }
    }

    public void removeBurstShot(String burstShotId) {
        if(burstShotId == null || burstShotId.length() == 0)
            return;

        ArrayList<ExifItem> burstShotArray = (ArrayList<ExifItem>) (mBurstShotCache.get(burstShotId));
        for (int i = 0; i < burstShotArray.size(); i++) {
            ExifItem item = burstShotArray.get(i);
            mExifInfoCache.removeCachedExifInfo(item.id);
        }
        mBurstShotCache.remove(burstShotId);
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-05,ALM-865458 begin
    public void removeImageTypeId(String id) {
        if(id == null || id.length() == 0) return;

        mExifInfoCache.removeCachedExifInfo(id);
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-05,ALM-865458 end

    public ArrayList<String> queryFaceshow(Context context,int mMediaSetUtils) {
        Log.i(TAG, "mMediaSetUtils = " + mMediaSetUtils);
        ArrayList<String> faceshowArray = new ArrayList<String>();
        Set<Map.Entry<String,ExifItem>> entrys = mExifInfoCache.getEntrySet();
        Iterator<Map.Entry<String,ExifItem>> iter = entrys.iterator();
        while(iter.hasNext()) {
            Map.Entry<String,ExifItem> entry = iter.next();
            if(((ExifItem)entry.getValue()).type == FACESHOW) {
                //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909 start
                if (mMediaSetUtils == MediaSetUtils.CAMERA_BUCKET_ID) {
                    if(((ExifItem)entry.getValue()).path.startsWith("/storage/emulated"))
                        faceshowArray.add(entry.getKey());
                } else if(mMediaSetUtils == MediaSetUtils.SDCARD_CAMERA_BUCKET_ID) {
                    Log.i(TAG, "sdcardPath:"+((ExifItem)entry.getValue()).path);
                    if(((ExifItem)entry.getValue()).path.startsWith("/storage/sdcard1"))
                        faceshowArray.add(entry.getKey());
                } else if (mMediaSetUtils == 0){
                    faceshowArray.add(entry.getKey());
                }
                //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909 end
                if(Debug) {
                    Log.d(TAG, "queryFaceshow id " + entry.getKey());
                }
            }
        }

        faceshowArray = checkMediaItemAvailability(context, faceshowArray);
        return faceshowArray;
    }

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-28,BUG-2208330*/
    public ArrayList<String> querySlowMotion(Context context,int mMediaSetUtils) {
        Log.i(TAG, "mMediaSetUtils = " + mMediaSetUtils);
        ArrayList<String> slowMotionArray = new ArrayList<String>();
        Set<Map.Entry<String,ExifItem>> entrys = mExifInfoCache.getEntrySet();
        Iterator<Map.Entry<String,ExifItem>> iter = entrys.iterator();
        while(iter.hasNext()) {
            Map.Entry<String,ExifItem> entry = iter.next();
            if(((ExifItem)entry.getValue()).type == SLOWMOTION) {
                //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909 start
                if (mMediaSetUtils == MediaSetUtils.CAMERA_BUCKET_ID) {
                    if(((ExifItem)entry.getValue()).path.startsWith("/storage/emulated"))
                        slowMotionArray.add(entry.getKey());
                } else if(mMediaSetUtils == MediaSetUtils.SDCARD_CAMERA_BUCKET_ID) {
                    Log.i(TAG, "sdcardPath:"+((ExifItem)entry.getValue()).path);
                    if(((ExifItem)entry.getValue()).path.startsWith("/storage/sdcard1"))
                        slowMotionArray.add(entry.getKey());
                } else if (mMediaSetUtils == 0){
                    slowMotionArray.add(entry.getKey());
                }
                //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909 end
                if(Debug) {
                    Log.d(TAG, "querySlowmotion id " + entry.getKey());
                }
            }
        }

        slowMotionArray = checkMediaItemAvailability(context, slowMotionArray);
        return slowMotionArray;
    }
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    private HashMap<String, SoftReference<Picture>> mParallaxCache = new HashMap<>(); //MODIFIED by jian.pan1, 2016-04-05,BUG-1892017
    public ArrayList<Picture> queryParallax(Context context) {
        ContentResolver cr = context.getContentResolver();
        ArrayList<Picture> result = new ArrayList<Picture>(50);

        String projection[] = new String[] { ContentConstants.KEY_ID, ContentConstants.FILE, ContentConstants.METADATA };
        Cursor cursor = null;
        try {
            cursor = cr.query(Uri.parse(ContentConstants.CONTENT_URI), projection, null, null, null);

            if(cursor != null) {
                int keyIdIndex = cursor.getColumnIndex(ContentConstants.KEY_ID);
 /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1892017*/
//                int fileIndex = cursor.getColumnIndex(ContentConstants.FILE);
                int metadataIndex = cursor.getColumnIndex(ContentConstants.METADATA);

                while(cursor.moveToNext()) {
                    String keyId = cursor.getString(keyIdIndex);
                    SoftReference<Picture> pictureCache = mParallaxCache.get(keyId);
//                    String file = cursor.getString(fileIndex);
                    Picture picture = null;
                    if (pictureCache != null) {
                        picture = pictureCache.get();
                    }
                    long fileId;
                    if (picture != null) {
                        result.add(picture);
                        fileId = picture.mFileId;
                    } else {
                        String metadata = cursor.getString(metadataIndex);
                        HashMap<String, String> keyValues = parseParallaxMetadata(metadata);
                        fileId = Long.valueOf(keyValues.get("id").replaceAll("_", "")).longValue();
                        float latitude = Float.valueOf(keyValues.get("latitude")).floatValue();
                        float longitude = Float.valueOf(keyValues.get("longitude")).floatValue();
                        long timestamp = Long.valueOf(keyValues.get("date_modified")).longValue() / 1000;
                        int width = Integer.valueOf(keyValues.get("width")).intValue();
                        int height = Integer.valueOf(keyValues.get("height")).intValue();
                        Picture pic = new Picture( VOLUME_NAME, FileColumns.MEDIA_TYPE_IMAGE, fileId, latitude, longitude, timestamp, width, height, 0 );
                        result.add(pic);
                        SoftReference<Picture> cache = new SoftReference<Picture>(pic);
                        mParallaxCache.put(keyId, cache);
                    }
                     /*MODIFIED-END by jian.pan1,BUG-1892017*/
                    mExifInfoCache.prefetchExifInfo(fileId + "", PARALLAX, false);
                }
            }
        } catch(Exception e) {
            Log.e(TAG, "queryParallax", e);
        } finally {
            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        if(result.size() > 1) {
            Collections.sort(result, new Comparator<Picture>() {
                @Override
                public int compare(Picture lhs, Picture rhs) {
                    if(lhs.mTimestamp > rhs.mTimestamp) return -1;
                    else if(lhs.mTimestamp < rhs.mTimestamp) return 1;
                    else return 0;
                }
            });
        }
        return result;
    }

    private HashMap<String, String> parseParallaxMetadata(String metadata) {
        HashMap<String, String> map = new HashMap<String, String>(12);
        String[] keyValues = metadata.split("&");
        for(int i = 0; i < keyValues.length; i++) {
            String keyValue = keyValues[i];
            int pos = keyValue.indexOf("=");
            String key = keyValue.substring(0, pos);
            String value = keyValue.substring(pos + 1);
            map.put(key, value);
        }
        return map;
    }

    public void removeParallax(Context context, String id) {
        ContentResolver cr = context.getContentResolver();
        cr.delete(Uri.parse(ContentConstants.CONTENT_URI), null, null);
    }


    private ArrayList<String> checkMediaItemAvailability(Context context, ArrayList<String> idArray) {
        if(idArray == null || idArray.size() == 0)
           return null;

        ArrayList<String> newIdArray = new ArrayList<String>();
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = { FileColumns._ID };
        StringBuffer buf = new StringBuffer();
        buf.append(FileColumns._ID);
        buf.append(" IN ");
        buf.append("(");
        for(int i = 0; i < idArray.size(); i++) {
           buf.append(idArray.get(i));
            if(i < idArray.size() - 1)
               buf.append(",");
        }
        buf.append(")");
        String selection = buf.toString();
        String sortOrder = FileColumns.DATE_MODIFIED + " DESC";

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, projection, selection, null, sortOrder);
        if(cursor != null) {
            try {
                while(cursor.moveToNext()) {
                    String id = String.valueOf(cursor.getLong(0));
                    if(idArray.contains(id)) {
                        newIdArray.add(id);
                        idArray.remove(id);
                    }
                }
            } finally {
                cursor.close();
            }
        }

        for (int i = 0; i < idArray.size(); i++) {
            mExifInfoCache.removeCachedExifInfo(idArray.get(i));
        }

        return newIdArray;
    }

    private HashMap<String, Integer> retrieveMediaMetadata(String path) {
        HashMap<String, Integer> metadataMap = new HashMap<String, Integer>();

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-20,ALM-1430061 begin
        Log.d(TAG, "retrieveMediaMetadata path = " + path);
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-20,ALM-1430061 end
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-22,Defect:1606899 begin
        try {
            retriever.setDataSource(path);
        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
            return metadataMap;
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-22,Defect:1606899 end

        int CAPTURE_FRAMERATE = 25;//MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE
        String framerate = retriever.extractMetadata(CAPTURE_FRAMERATE);
        if(framerate != null) {
            int value = (int)Float.parseFloat(framerate);
            metadataMap.put(METADATA_KEY_CAPTURE_FRAMERATE, Integer.valueOf(value));
        }

        String videoWidth = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        if(videoWidth != null) {
            metadataMap.put(METADATA_KEY_VIDEO_WIDTH, Integer.parseInt(videoWidth));
        }

        String videoHeight = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        if(videoHeight != null) {
            metadataMap.put(METADATA_KEY_VIDEO_HEIGHT, Integer.parseInt(videoHeight));
        }

        retriever.release();
        return metadataMap;
    }

    private String getUserComment(String path) {
        if(path == null || path.length() == 0)
            throw new IllegalArgumentException("ExifInfoFilter.getUserComment");

        // read exif from file
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(path);
        } catch (Exception e) {

        }
        String userComment = exif.getUserComment();

        return userComment;
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-05,ALM-865458 begin
    private void parseBurstShot(JsonReader reader, HashMap<String, String> result) throws IOException {
        String burstShotId = reader.nextString();

        if(reader.hasNext()) {
            String token = reader.nextName();
            if(BurstShotIndex.equals(token)) {
                String burstShotIndex = reader.nextString();

                result.put(BurstShotIndex, burstShotIndex);
                result.put(BurstShotId, burstShotId);
            }
        }
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-05,ALM-865458 end

    private HashMap<String, String> parseUserComment(String userComment) {
        if(userComment == null || userComment.length() == 0)
            throw new IllegalArgumentException("ExifInfoFilter.parseUserComment");

        JsonReader reader = null;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(userComment.getBytes());
        HashMap<String, String> result = new HashMap<String, String>();

        try {
            reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            reader.beginObject();
            if(reader.hasNext()) {
                String token = reader.nextName();
                if(BurstShotId.equals(token)) {
                    parseBurstShot(reader, result);//[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-05,ALM-865458
                } else if(CaptureMode.equals(token)) {
                    String captureMode = reader.nextString();
                    result.put(CaptureMode, captureMode);
                }
            }
            reader.endObject();

        //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-04,ALM-814521 begin
        } catch (Exception e) {

        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-11-04,ALM-814521 end

        return result;
    }

    private int addNewBurstShotCache(ExifItem exifItem, boolean fromCamera) {
        ArrayList<ExifItem> burstShotArray = (ArrayList<ExifItem>) (mBurstShotCache.get(exifItem.burstId));
        if (burstShotArray == null) {
            burstShotArray = new ArrayList<ExifItem>();
            mBurstShotCache.put(exifItem.burstId, burstShotArray);
        }

        int type = BURSTSHOTSHIDDEN;
        if (burstShotArray.size() == 0) {
            type = BURSTSHOTS;
            exifItem.type = BURSTSHOTS;
            burstShotArray.add(exifItem);
        } else {
            if (fromCamera) {
                int size = burstShotArray.size();
                for (int i = 0; i < size; i++) {
                    ExifItem temp = burstShotArray.get(i);
                    if (Integer.valueOf(exifItem.burstIndex) > Integer.valueOf(temp.burstIndex)) {
                        if (i == 0) {
                            temp.type = BURSTSHOTSHIDDEN;
                            mExifInfoCache.updateBurstExifType(temp, false);

                            type = BURSTSHOTS;
                            exifItem.type = BURSTSHOTS;
                        } else {
                            type = BURSTSHOTSHIDDEN;
                            exifItem.type = BURSTSHOTSHIDDEN;
                        }
                        burstShotArray.add(i, exifItem);
                        break;
                    }

                    if (i == size-1) {
                        type = BURSTSHOTSHIDDEN;
                        exifItem.type = BURSTSHOTSHIDDEN;
                        burstShotArray.add(exifItem);
                    }
                }
            } else {
                type = BURSTSHOTSHIDDEN;
                exifItem.type = BURSTSHOTSHIDDEN;
                burstShotArray.add(exifItem);
            }
        }
//        TctLog.i("pillar", "add burst shot, id = " + exifItem.id + ", burstId = " + exifItem.burstId
//                + ", burstIndex = " + exifItem.burstIndex + ", type = " + exifItem.type);
        return type;
    }

    private void notifySourceChange() {
        synchronized(mFilterSourceListener) {
            for(FilterSourceListener listener : mFilterSourceListener) {
                listener.onSourceChanged();
            }
        }
    }
}

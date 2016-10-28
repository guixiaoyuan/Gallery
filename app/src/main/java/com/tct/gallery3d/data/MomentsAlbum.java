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
/* 28/10/2015|chengbin.du-nb        |ALM-833287            |modify item disPlaye sytle in moments interface.*/
/* ----------|----------------------|----------------------|----------------- */
/* 11/11/2015|chengbin.du-nb        |ALM-899577            |[Android5.1][Gallery_v5.2.3.1.0310.0]retrieve slowmotion video and micro video info*/
/* ----------|----------------------|----------------------|----------------- */
/* 17/11/2015|chengbin.du-nb        |ALM-823102            |[Android5.1][Gallery_v5.2.3.1.0309.0]There are some Oct27 pictures display in October 22.*/
/* ----------|----------------------|----------------------|----------------- */
/* 19/11/2015|chengbin.du-nb        |ALM-940132            |[Android 6.0][Gallery_v5.2.3.1.1.0307.0]Gallery moments view should display images&videos include "Pictures" folder*/
/* ----------|----------------------|----------------------|----------------- */
/* 11/30/2015| jian.pan1            | [ALM]Defect:930094   |Gallery NullPointerException
/* ----------|----------------------|----------------------|----------------- */
/* 16/12/2015|chengbin.du-nb        |ALM-1170791           |Momments display vGallery.*/
/* ----------|----------------------|----------------------|----------------- */
/* 21/12/2015|chengbin.du-nb        |ALM-1121296           |[Gallery]Fyuse pictures can't display in gallery after capture a Fyuse picture
/* ----------|----------------------|----------------------|----------------- */
/* 12/26/2015| jian.pan1            |[ALM]Defect:1048523   |[Gallery]There is no show list as Fyuse in moment of gallery
/* ----------|----------------------|----------------------|----------------- */
/* 01/07/2016| jian.pan1            |[ALM]Defect:1270007   |[Android6.0][Gallery][Force Close]Gallery force close after switch to month view
/* ----------|----------------------|----------------------|----------------- */
/* 01/14/2016| jian.pan1            |[ALM]Defect:1270036   |[Android6.0][Gallery]It auto exit slideshow when tap slideshow on fyuse category
/* ----------|----------------------|----------------------|----------------- */
/* 01/14/2016| jian.pan1            |[ALM]Defect:791983    |can not delete a parallax photo
/* ----------|----------------------|----------------------|----------------- */
/* 01/18/2016| jian.pan1            |[ALM]Defect:1305323   |[GAPP][Android6.0][Gallery]Data tag display '1 january'when change month view to day view
/* ----------|----------------------|----------------------|----------------- */
/* 03/08/2016|    su.jiang          |  PR-1615695          |[GAPP][Android6.0][Gallery]It display '0 January' on screen after switch to day view.*/
/*-----------|----------------------|----------------------|-------------------------------------------------------------------------------------*/

package com.tct.gallery3d.data;

import java.lang.ref.SoftReference; //MODIFIED by jian.pan1, 2016-04-05,BUG-1892017
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
import com.tct.gallery3d.R;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.fyuse.ContentConstants;
import com.tct.gallery3d.app.vgallery.GalleryLayout;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.data.DataManager.ParallaxSourceListener;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.picturegrouping.Picture;
import com.tct.gallery3d.picturegrouping.PictureGroup;
import com.tct.gallery3d.picturegrouping.PictureGroupLoader;
import com.tct.gallery3d.util.MediaSetUtils;
/* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
/*MODIFIED-BEGIN by hao.yin, 2016-03-29,BUG-1871412*/
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
/*MODIFIED-END by hao.yin,BUG-1871412*/
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;

public class MomentsAlbum extends MediaSet implements LoaderManager.LoaderCallbacks<List<PictureGroup>>{

    private static final String TAG = "MomentsAlbum";
    private static final String VOLUME_NAME = "external";
    private static final long MillisSecondInOneDay = 1000 * 60 * 60 * 24;

    public static final int FILTER_ALL = 1;
    public static final int FILTER_PHOTO = 2;
    public static final int FILTER_VIDEO = 3;
    public static final int FILTER_FYUSE = 4;

    public static final int DAY_MODE = 0x1;
    public static final int MONTH_MODE = 0x2;

    private final GalleryApp mApplication;
    private final GroupBuilder mGroupBuilder = new GroupBuilder();
    private final StyleLayoutManager mStyleLayoutManager;
    private final ChangeNotifier mImageNotifier;
    private final ChangeNotifier mVideoNotifier;
    private Handler mfgHandler;
    private Activity mActivity;
    private int mCurrentMode = DAY_MODE;
    private int mCurrentFilterType = FILTER_ALL;
    private boolean mIsPictureGroupingUpdated = false;
    private boolean mIsNeedPictureGrouping = true;
    private long mFgThreadId;
    private int mScreenWidth = 0;
    private int mScreenPadding = 0;
    private float mDensity = 1.f;//[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-28,ALM-833287

    private List<PictureGroup> mPictureGroupList = null;
    private List<GroupBaseInfo> mCurrentGroupList = null;
    private final ParallaxObserver mParallaxObserver = new ParallaxObserver();
    private HashMap<String, SoftReference<ParallaxItemInfo>> mParallaxCache = new HashMap<>();

    public MomentsAlbum(Path path, GalleryApp application) {
        super(path, nextVersionNumber());

        mApplication = application;
        mStyleLayoutManager = new StyleLayoutManager(application.getAndroidContext());
        mImageNotifier = new ChangeNotifier(this, Images.Media.EXTERNAL_CONTENT_URI, application);
        mVideoNotifier = new ChangeNotifier(this, Video.Media.EXTERNAL_CONTENT_URI, application);
        mApplication.getDataManager().registerParallaxListener(mParallaxObserver);
    }

    public class ParallaxObserver implements ParallaxSourceListener {
        public boolean isDirty = false;
        @Override
        public void onParallaxChanged() {
            isDirty = true;
        }
    }
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-14,Defect:791983 end

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public void reset() {
        mIsNeedPictureGrouping = true;
        mfgHandler = new Handler();
        mCurrentMode = DAY_MODE;
        mFgThreadId = Thread.currentThread().getId();
        setScreenWidth();
        setScreenPadding();
    }

    @Override
    public String getName() {
        return "MomentsAlbum";
    }

    @Override
    public synchronized long reload() {
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-14,Defect:791983 begin
        boolean isVideoDirty = mVideoNotifier.isDirty();
        boolean isImageDirty = mImageNotifier.isDirty();
        if (isVideoDirty || isImageDirty || mIsNeedPictureGrouping || mParallaxObserver.isDirty) {
            if (!checkFgThread()) {
                startPictureGroupingLoader(mCurrentMode == DAY_MODE);
            }
            mIsNeedPictureGrouping = false;
            mParallaxObserver.isDirty = false;
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-14,Defect:791983 end
            mDataVersion = nextVersionNumber();
            mCurrentGroupList = mGroupBuilder.buildGroup(mCurrentMode);
        }
        return mDataVersion;
    }

    public List<GroupBaseInfo> buildGroupByMode(int mode) {
        List<GroupBaseInfo> list = mGroupBuilder.buildGroup(mode);
        return list;
    }

    @Override
    public int getMediaItemCount() {
        int totalCount = 0;

        if(mCurrentGroupList != null) {
            List<GroupBaseInfo> groupInfoList = mCurrentGroupList;

            for(GroupBaseInfo info : groupInfoList) {
                totalCount += info.getPictureListSize();
            }
        }
        return totalCount;
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_INFO;
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        if(start < 0 || count <= 0) return new ArrayList<MediaItem>();

        ExifInfoFilter filter = ExifInfoFilter.getInstance(mActivity);
        //confirm list group by mode
        ArrayList<MediaItem> list = new ArrayList<MediaItem>(count);
        List<GroupBaseInfo> groupInfoList = mCurrentGroupList;

        //get id list
        int totalCount = 0;
        int cursorIndex = -1;
        ArrayList<Long> imageIdArray = new ArrayList<Long>(1000);
        ArrayList<Long> videoIdArray = new ArrayList<Long>(50);
        ArrayList<Long> parallaxIdArray = new ArrayList<Long>(50);
        ArrayList<Long> idArray = new ArrayList<Long>(count);
        boolean isFinished = false;
        for(GroupBaseInfo group : groupInfoList) {
            if(isFinished) break;

            totalCount += group.getPictureListSize();
            if(cursorIndex == -1 && start < totalCount) {
                cursorIndex = start;
            }
            if(cursorIndex >= 0) {
                for(int index = (cursorIndex - (totalCount - group.getPictureListSize()));
                        index < group.getPictureListSize(); index++) {
                    long id = group.getIdByIndex(index);
                    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
                    int type = filter.queryType(Long.toString(id));
                    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
                    int mediaType = group.getMediaTypeByIndex(index);
                    if(type == ExifInfoFilter.PARALLAX) {
                        parallaxIdArray.add(id);
                    } else if(mediaType == FileColumns.MEDIA_TYPE_IMAGE) {
                        imageIdArray.add(id);
                    } else if(mediaType == FileColumns.MEDIA_TYPE_VIDEO) {
                        videoIdArray.add(id);
                    }
                    idArray.add(id);
                    cursorIndex++;
                    if(cursorIndex == start + count) {
                        isFinished = true;
                        break;
                    }
                }
            }
        }

        //load mediaitem from db
        HashMap<Long, MediaItem> all = new HashMap<Long, MediaItem>();
        HashMap<Long, LocalMediaItem> imageMap = loadMediaItemArray(imageIdArray, true);
        if(imageMap != null) {
            all.putAll(imageMap);
        }
        HashMap<Long, LocalMediaItem> videoMap = loadMediaItemArray(videoIdArray, false);
        if(videoMap != null) {
            all.putAll(videoMap);
        }
        HashMap<Long, ParallaxItem> parallaxMap = loadParallaxItemArray(parallaxIdArray);
        if(parallaxMap != null) {
            all.putAll(parallaxMap);
        }

        for(Long id : idArray) {
            list.add(all.get(id));
        }
        return list;
    }

    @Override
    public int getMediaSetType() {
        if(mCurrentGroupList != null) {
            mMediaSetType = MediaSet.MEDIASET_TYPE_UNKNOWN;

            for(GroupBaseInfo groupBaseInfo : mCurrentGroupList) {
                List<ItemBaseInfo> list = groupBaseInfo.mPictureIdList;
                for(ItemBaseInfo itemBaseInfo : list) {
                    if(itemBaseInfo.mediaType == FileColumns.MEDIA_TYPE_IMAGE) {
                        mMediaSetType = MediaSet.MEDIASET_TYPE_IMAGE;
                        break;
                    }
                }
                if(mMediaSetType != MediaSet.MEDIASET_TYPE_UNKNOWN)
                    break;
            }
        }

        return mMediaSetType;
    }

    public void switchCurrentMode(int mode) {
        mCurrentMode = mode;
        mIsNeedPictureGrouping = true;
    }

    public void switchCurrentFilter(int filterType) {
        if(mCurrentFilterType != filterType) {
            mCurrentFilterType = filterType;
            mIsNeedPictureGrouping = true;
        }
    }

    public void calculateLayout() {
        setScreenWidth();
        setScreenPadding();
        mIsNeedPictureGrouping = true;
    }

    public List<GroupBaseInfo> achieveGroupBaseInfoList() {
        List<GroupBaseInfo> groupInfoList = mCurrentGroupList;
        if (groupInfoList != null) {
            List<GroupBaseInfo> newList = new ArrayList<>(groupInfoList.size());
            newList.addAll(groupInfoList);
            return newList;
        } else{
            return null;
        }
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 begin
    public ItemBaseInfo findItemBaseInfo(int index) {
        List<GroupBaseInfo> groupInfoList = mCurrentGroupList;
        for(GroupBaseInfo group : groupInfoList) {
            for(ItemBaseInfo item :group.mPictureIdList) {
                if(index == 0) return item;
                index--;
            }
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-11-30,Defect:930094 begin
        Log.e(TAG, "can't find item, It's out of range. index = " + index);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-11-30,Defect:930094 end
        return null;
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 end

    private void setScreenWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)
                mApplication.getAndroidContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;

        mDensity = metrics.density;//[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-28,ALM-833287
    }

    private void setScreenPadding() {
        mScreenPadding = (int)(2 * mDensity);
    }

    private HashMap<Long, LocalMediaItem> loadMediaItemArray(ArrayList<Long> idArray, boolean isImage) {
        if(idArray.size() == 0) return null;

        HashMap<Long, LocalMediaItem> mediaMap = new HashMap<Long, LocalMediaItem>(idArray.size());
        DataManager dataManager = mApplication.getDataManager();

        String[] projection = isImage ? LocalImage.getImageProjection() : LocalVideo.getVideoProjection();
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

        ContentResolver resolver = mApplication.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri(VOLUME_NAME);
        Cursor cursor = resolver.query(uri, projection, selection, null, null);

        int idIndex = cursor.getColumnIndex(FileColumns._ID);
        Path itemPath = isImage ? LocalImage.ITEM_PATH : LocalVideo.ITEM_PATH;

        try {
            while(cursor.moveToNext()) {
                long id = cursor.getLong(idIndex);
                Path childPath = itemPath.getChild(id);
                LocalMediaItem item = loadOrUpdateItem(childPath, cursor, dataManager, mApplication, isImage);
                mediaMap.put(id, item);
            }
        } finally {
            cursor.close();
        }

        return mediaMap;
    }

     /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1892017*/
    private class ParallaxItemInfo {
        private long fileId;
        private ParallaxItem item;

        public long getFileId() {
            return fileId;
        }

        public void setFileId(long fileId) {
            this.fileId = fileId;
        }

        public ParallaxItem getItem() {
            return item;
        }

        public void setItem(ParallaxItem item) {
            this.item = item;
        }
    }
     /*MODIFIED-END by jian.pan1,BUG-1892017*/

    private HashMap<Long, ParallaxItem> loadParallaxItemArray(ArrayList<Long> idArray) {
        if(idArray.size() == 0) return null;

        HashMap<Long, ParallaxItem> mediaMap = new HashMap<Long, ParallaxItem>(idArray.size());
        DataManager dataManager = mApplication.getDataManager();
        Path itemPath = ParallaxItem.ITEM_PATH;
        String projection[] = new String[] { ContentConstants.KEY_ID, ContentConstants.FILE, ContentConstants.METADATA };
        ContentResolver cr = mActivity.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = cr.query(Uri.parse(ContentConstants.CONTENT_URI), projection, null, null, null);

            if(cursor != null) {
                int keyIdIndex = cursor.getColumnIndex(ContentConstants.KEY_ID);
                int fileIndex = cursor.getColumnIndex(ContentConstants.FILE);
                int metadataIndex = cursor.getColumnIndex(ContentConstants.METADATA);

                while(cursor.moveToNext()) {
 /*MODIFIED-BEGIN by hao.yin, 2016-03-29,BUG-1871412*/
                     /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1892017*/
                    String keyId = cursor.getString(keyIdIndex);
                    String file = cursor.getString(fileIndex);
                    SoftReference<ParallaxItemInfo> parallaxItemCache = mParallaxCache.get(keyId);
                    ParallaxItemInfo itemInfo = null;
                    if (parallaxItemCache != null) {
                        itemInfo = parallaxItemCache.get();
                    }
                    if (itemInfo != null) {
                        mediaMap.put(itemInfo.getFileId(), itemInfo.getItem());
                    } else {
                        String metadata = cursor.getString(metadataIndex);
                        HashMap<String, String> keyValues = parseParallaxMetadata(metadata);
                         /*MODIFIED-END by jian.pan1,BUG-1887087*/
                        long fileId = 0;
                        try{
                           fileId = Long.valueOf(keyValues.get("id").replace("_", "")).longValue();
                           long timestamp = Long.valueOf(keyValues.get("date_modified")).longValue();
                           Path childPath = itemPath.getChild(fileId);
                           ParallaxItem item = loadOrUpdateParallaxItem(childPath, file, timestamp, dataManager, mApplication);
                           mediaMap.put(fileId, item);
                            /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1887087*/
                           ParallaxItemInfo info = new ParallaxItemInfo();
                           info.setFileId(fileId);
                           info.setItem(item);
                           SoftReference<ParallaxItemInfo> cache = new SoftReference<ParallaxItemInfo>(info);
                           mParallaxCache.put(keyId, cache);
                        }catch(NumberFormatException e){
                            Log.i(TAG, "NumberFormatException fileId:"+fileId);
                        }
                    }
                     /*MODIFIED-END by jian.pan1,BUG-1892017*/
                }
            }
        } finally {
             /* MODIFIED-BEGIN by hao.yin, 2016-03-24,BUG-1861293 */
             if(cursor != null){
              cursor.close();
           }
           /* MODIFIED-END by hao.yin,BUG-1861293 */
        }
        return mediaMap;
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

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-14,Defect:1270036 begin
    public static MediaItem[] getFyuseItemsByPaths(DataManager dataManager,
            ArrayList<Path> paths) {
        MediaItem[] result = new MediaItem[paths.size()];
        if (paths.isEmpty())
            return result;

        for (int i = 0; i < paths.size(); i++) {
            synchronized (DataManager.LOCK) {
                ParallaxItem item = (ParallaxItem) dataManager
                        .peekMediaObject(paths.get(i));
                if (item == null) {
                    Log.e(TAG, "getMediaItemById() item is NULL. Path is "
                            + paths.get(i));
                }
                result[i] = item;
            }
        }
        return result;
    }
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-14,Defect:1270036 end

    private LocalMediaItem loadOrUpdateItem(Path path, Cursor cursor,
            DataManager dataManager, GalleryApp app, boolean isImage) {
        synchronized (DataManager.LOCK) {
            LocalMediaItem item = (LocalMediaItem) dataManager.peekMediaObject(path);
            if (item == null) {
                if (isImage) {
                    item = new LocalImage(path, app, cursor);
                } else {
                    item = new LocalVideo(path, app, cursor);
                }
            } else {
                item.updateContent(cursor);
            }
            return item;
        }
    }

    private ParallaxItem loadOrUpdateParallaxItem(Path path, String filePath, long dateModifiedInSec,
            DataManager dataManager, GalleryApp app) {
        synchronized (DataManager.LOCK) {
            ParallaxItem item = (ParallaxItem)dataManager.peekMediaObject(path);
            if(item == null) {
                item = new ParallaxItem(app, path, filePath, dateModifiedInSec);
            }
            return item;
        }
    }

    private void sortGalleryLayout(List<GroupBaseInfo> list) {
        GalleryLayout layout = new GalleryLayout(this.mScreenWidth, 0, this.mScreenPadding, Configuration.ORIENTATION_PORTRAIT);
        layout.setRichPatternScope(0, 16);
        for(GroupBaseInfo group : list) {
            layout.addNewGroup();
            for(ItemBaseInfo item : group.mPictureIdList) {
                layout.addImage(item);
            }
            layout.addNewGroupFinish();
        }
    }

    private void startPictureGroupingLoader(boolean isWaitingForLoadFinish) {
        Utils.assertTrue(mActivity != null);

        mIsPictureGroupingUpdated = false;
        mfgHandler.post(new Runnable() {
            @Override
            public void run() {
                mActivity.getLoaderManager().restartLoader(0, null, MomentsAlbum.this);
            }
        });
        if(isWaitingForLoadFinish) {
            while(!mIsPictureGroupingUpdated) {
                try {
                    Log.d(TAG, "waiting");
                    wait(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkFgThread() {
        long threadId = Thread.currentThread().getId();
        return (threadId == mFgThreadId);
    }

    @Override
    public Loader<List<PictureGroup>> onCreateLoader(int id, Bundle args) {
        return new PictureGroupLoader(mApplication.getAndroidContext()); // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330
    }

    @Override
    public void onLoadFinished(Loader<List<PictureGroup>> loader,
            List<PictureGroup> data) {
        mPictureGroupList = new ArrayList<>(data);
        mIsPictureGroupingUpdated = true;
        Log.d(TAG, "MomentsAlbum.onLoadFinished");
    }

    @Override
    public void onLoaderReset(Loader<List<PictureGroup>> loader) {
        mIsPictureGroupingUpdated = true;
    }

    private class GroupBuilder {

        public GroupBuilder() {
        }

        public synchronized List<GroupBaseInfo> buildGroup(int mode) {
            Log.d(TAG, "GroupBuilder.buildGroup {");
            List<GroupBaseInfo> groupList = null;
            if(mode == DAY_MODE) {
                groupList = buildDayModeGroup();
                sortGalleryLayout(groupList);
            } else if(mode == MONTH_MODE) {
                groupList = buildMonthModeGroup();
            }
            Log.d(TAG, "} GroupBuilder.buildGroup");
            return groupList;
        }

        private List<GroupBaseInfo> buildDayModeGroup() {
            List<GroupBaseInfo> list = convertPictureGroupToGroupBaseInfoList();
            return list;
        }

        private List<GroupBaseInfo> buildMonthModeGroup() {
            List<GroupBaseInfo> list = queryMediaItemBaseInfoOnMonthMode();
            return list;
        }

        private List<GroupBaseInfo> convertPictureGroupToGroupBaseInfoList() {
          //[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-02-29,PR1663397 begin
            List<PictureGroup> pictureGroupList = new ArrayList<PictureGroup>();
            if (mPictureGroupList != null) { // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330
                pictureGroupList.addAll(mPictureGroupList);
            }
          //[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-02-29,PR1663397 end
            List<GroupBaseInfo> result = new ArrayList<GroupBaseInfo>();
            Calendar calendar = Calendar.getInstance();
            for(PictureGroup group : pictureGroupList) {
                Picture picture = group.mPictures.get(0);

                calendar.setTimeInMillis(picture.mTimestamp * 1000);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                String address = "";
                if(group.mAddress.mPointOfInterest != null) {
                    address += group.mAddress.mPointOfInterest;
                }
                if(group.mAddress.mAdminArea != null) {
                    if(address.length() != 0)
                        address += " ";
                    address += group.mAddress.mAdminArea;
                }
                GroupBaseInfo baseInfo = new GroupBaseInfo(year, month, day, address);

                for(Picture pic : group.mPictures) {
                    //filter
                    if(mCurrentFilterType == FILTER_ALL) {
                        //nothing to do
                    } else if(mCurrentFilterType == FILTER_PHOTO) {
                        if(pic.mMediaType == FileColumns.MEDIA_TYPE_VIDEO)
                            continue;
                    } else if(mCurrentFilterType == FILTER_VIDEO) {
                        if(pic.mMediaType == FileColumns.MEDIA_TYPE_IMAGE)
                            continue;
                    }
                    baseInfo.addPicture(new ItemBaseInfo(pic.mFileId, pic.mMediaType, pic.mWidth, pic.mHeight));
                }
                if(baseInfo.getPictureListSize() > 0) {
                    result.add(baseInfo);
                }
            }

            return result;
        }

        private List<GroupBaseInfo> queryMediaItemBaseInfoOnMonthMode() {
            Log.d(TAG, "GroupBuilder.queryMediaItemBaseInfoOnMonthMode {");

            ExifInfoFilter filter = ExifInfoFilter.getInstance(mActivity); // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-07,Defect:1270007 begin
            final int initialCapacity = 50;
            long mPrevTimestamp = -1;
            List<GroupBaseInfo> result = new ArrayList<GroupBaseInfo>();
            Calendar calendar = Calendar.getInstance();
            SparseArray<GroupBaseInfo> groupArray = new SparseArray<GroupBaseInfo>(initialCapacity);
            ArrayList<Integer> keyArray = new ArrayList<Integer>(initialCapacity);
            ArrayList<Picture> parallaxArray = filter.queryParallax(mApplication.getAndroidContext()); // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330
            if (mCurrentFilterType == FILTER_FYUSE) {
                for (int i = 0; i < parallaxArray.size(); i++) {
                    Picture pic = parallaxArray.get(i);
                    calendar.setTimeInMillis(pic.mTimestamp * 1000);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int key = year * 100 + month;
                    Log.d(TAG, "parallax file 1" + pic.toString() + " timestamp " + key);
                    GroupBaseInfo baseInfo = groupArray.get(key);
                    if(baseInfo == null) {
                        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-18,Defect:1305323 begin
                        baseInfo = new GroupBaseInfo(year, month, 0, "");
                        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-18,Defect:1305323 end
                        groupArray.put(key, baseInfo);
                        keyArray.add(key);
                    }
                    baseInfo.addPicture(new ItemBaseInfo(pic.mFileId, pic.mMediaType, 0, 0));
                }
            } else {
                ContentResolver resolver = mApplication.getContentResolver();
                Uri uri = MediaStore.Files.getContentUri(VOLUME_NAME);

                String[] projection = {
                        FileColumns._ID,
                        FileColumns.DATA,
                        FileColumns.MEDIA_TYPE,
                        FileColumns.DATE_MODIFIED,
                        ImageColumns.DATE_TAKEN,
                        ImageColumns.BUCKET_ID};

                String[] newProjection = {
                        FileColumns._ID,
                        FileColumns.DATA,
                        FileColumns.MEDIA_TYPE,
                        FileColumns.DATE_MODIFIED,
                        ImageColumns.DATE_TAKEN,
                        ImageColumns.BUCKET_ID,
                        GappTypeInfo.GAPP_MEDIA_TYPE,
                        GappTypeInfo.GAPP_BURST_ID,
                        GappTypeInfo.GAPP_BURST_INDEX};

                StringBuffer buffer = new StringBuffer();
                //filter
                if(mCurrentFilterType == FILTER_ALL) {
                    buffer.append("( ")
                          .append(FileColumns.MEDIA_TYPE).append("=").append(FileColumns.MEDIA_TYPE_IMAGE)
                          .append(" OR ")
                          .append(FileColumns.MEDIA_TYPE).append("=").append(FileColumns.MEDIA_TYPE_VIDEO)
                          .append(" )");
                } else if(mCurrentFilterType == FILTER_PHOTO) {
                    buffer.append("( ")
                          .append(FileColumns.MEDIA_TYPE).append("=").append(FileColumns.MEDIA_TYPE_IMAGE)
                          .append(" )");
                } else if(mCurrentFilterType == FILTER_VIDEO) {
                    buffer.append("( ")
                          .append(FileColumns.MEDIA_TYPE).append("=").append(FileColumns.MEDIA_TYPE_VIDEO)
                          .append(" )");
                }
              //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-29,PR1490877 begin
                //buffer.append(" AND ")
                //      .append("( ");
/* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-19,BUG-2208330*/
//               ArrayList<Integer> bucketsId = MediaSetUtils.getMomentsBucketsId(resolver, uri);
//               if(bucketsId!=null&&bucketsId.size()>0)
//               {
//                   buffer.append(" AND ")
//                   .append("( ");
//                   for(int i = 0; i < bucketsId.size(); i++) {
//                       buffer.append(ImageColumns.BUCKET_ID).append("=").append(bucketsId.get(i));
//                       if(i < (bucketsId.size() - 1))
//                           buffer.append(" OR ");
//                   }
//                   buffer.append(" )");
//               //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-10,ALM-1761553 begin
//               } else {
//                   return result;
//               }
/* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
               //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-10,ALM-1761553 end
                //for(int i = 0; i < bucketsId.size(); i++) {
                  //  buffer.append(ImageColumns.BUCKET_ID).append("=").append(bucketsId.get(i));
                 //   if(i < (bucketsId.size() - 1))
                 //       buffer.append(" OR ");
                //}
                //buffer.append(" )");
              //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-29,PR1490877 end
                String selection = buffer.toString();
                String selectionDrm = "";
                if (DrmManager.isDrmEnable && GalleryActivity.TV_LINK_DRM_HIDE_FLAG) {
                    selectionDrm = " AND (" + DrmManager.TCT_IS_DRM + "=0 OR " + DrmManager.TCT_IS_DRM + " IS NULL)";
                }
                selection += selectionDrm;

                String sortOrder = FileColumns.DATE_MODIFIED + " DESC";
                boolean isOld = false;
                Cursor cursor = null;
                try {
                    cursor = resolver.query(uri, newProjection, selection, null, sortOrder);

                } catch (SQLiteException e) {
                    isOld = true;
                    cursor = resolver.query(uri, projection, selection, null, sortOrder);
                }
                Log.e(TAG, "cursor = " + cursor.getCount());

                int idIndex = cursor.getColumnIndex(FileColumns._ID);
                int dataIndex = cursor.getColumnIndex(FileColumns.DATA);//file path
                int mediaTypeIndex = cursor.getColumnIndex(FileColumns.MEDIA_TYPE);
                int dateModifiedIndex = cursor.getColumnIndex(FileColumns.DATE_MODIFIED);
                int dateTakenIndex = cursor.getColumnIndex(ImageColumns.DATE_TAKEN);
                int bucketIdIndex = cursor.getColumnIndex(ImageColumns.BUCKET_ID);

                try {
                    while(cursor.moveToNext()) {
                        //get columns from cursor
                        long id = cursor.getLong(idIndex);
                        String data = cursor.getString(dataIndex);
                        int mediaType = cursor.getInt(mediaTypeIndex);
                        long timeStamp = cursor.getLong(dateModifiedIndex) * 1000;
                        //if(!cursor.isNull(dateTakenIndex))
                        //    timeStamp = cursor.getLong(dateTakenIndex);
                        //long bucketId = cursor.getLong(bucketIdIndex);

                        int type = filter.queryType(String.valueOf(id));
                        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
                        if (type == ExifInfoFilter.NONE) {
                            if(!isOld) {
                                int gappMediaTypeIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_MEDIA_TYPE);
                                int burstIdIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_ID);
                                int burstIndexIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_INDEX);
                                GappTypeInfo gappTypeInfo = new GappTypeInfo();
                                gappTypeInfo.setType(cursor.getInt(gappMediaTypeIndex));
                                gappTypeInfo.setBurstshotId(cursor.getInt(burstIdIndex));
                                gappTypeInfo.setBurstshotIndex(cursor.getInt(burstIndexIndex));
                                type = filter.filter(String.valueOf(id), data, mediaType, timeStamp, true, false, gappTypeInfo);
                            }else {
                                type = filter.filter(String.valueOf(id), data, mediaType, timeStamp, true, false, null);
                            }
                        }
                        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
                        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-28,BUG-2208330*/
                        if (type == ExifInfoFilter.BURSTSHOTSHIDDEN) {
                                /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
                            Log.e(TAG, "queryMediaItemBaseInfoOnMonthMode filter id = " + id + " path = " + data);
                            continue;
                        }

                        while(parallaxArray.size() > 0 && mCurrentFilterType != FILTER_VIDEO) {
                            Picture pic = parallaxArray.get(0);
                            long picTime = pic.mTimestamp * 1000;
                            if((picTime < mPrevTimestamp || mPrevTimestamp == -1) && picTime >= timeStamp) {
                                //caculate time
                                calendar.setTimeInMillis(picTime);
                                int year = calendar.get(Calendar.YEAR);
                                int month = calendar.get(Calendar.MONTH) + 1;
                                int key = year * 100 + month;
                                Log.d(TAG, "parallax file 2" + pic.toString() + " timestamp " + key);

                                //group
                                GroupBaseInfo baseInfo = groupArray.get(key);
                                if(baseInfo == null) {
                                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-18,Defect:1305323 begin
                                    baseInfo = new GroupBaseInfo(year, month, 0, "");
                                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-18,Defect:1305323 end
                                    groupArray.put(key, baseInfo);
                                    keyArray.add(key);
                                }
                                baseInfo.addPicture(new ItemBaseInfo(pic.mFileId, pic.mMediaType, 0, 0));
                                parallaxArray.remove(0);
                            } else {
                                break;
                            }
                        }
                        //caculate time
                        calendar.setTimeInMillis(timeStamp);
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH) + 1;
                        int key = year * 100 + month;
                        Log.d(TAG, "file " + data + " timestamp " + key);
                        mPrevTimestamp = timeStamp;
                        //group
                        GroupBaseInfo baseInfo = groupArray.get(key);
                        if(baseInfo == null) {
                            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-18,Defect:1305323 begin
                            baseInfo = new GroupBaseInfo(year, month, 0, "");
                            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-18,Defect:1305323 end
                            groupArray.put(key, baseInfo);
                            keyArray.add(key);
                        }
                        //filter
                        if(mCurrentFilterType == FILTER_ALL) {
                            //nothing to do
                        } else if(mCurrentFilterType == FILTER_PHOTO) {
                            if(mediaType == FileColumns.MEDIA_TYPE_VIDEO)
                                continue;
                        } else if(mCurrentFilterType == FILTER_VIDEO) {
                            if(mediaType == FileColumns.MEDIA_TYPE_IMAGE)
                                continue;
                        }
                        baseInfo.addPicture(new ItemBaseInfo(id, mediaType, 0, 0));
                    }
                } finally {
                    cursor.close();
                }

                filter.notifySaveExifCache(); // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330

                while(parallaxArray.size() > 0 && mCurrentFilterType != FILTER_VIDEO) {
                    Picture pic = parallaxArray.get(0);
                    calendar.setTimeInMillis(pic.mTimestamp * 1000);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int key = year * 100 + month;
                    Log.d(TAG, "parallax file 3" + pic.toString() + " timestamp " + key);

                    //group
                    GroupBaseInfo baseInfo = groupArray.get(key);
                    if(baseInfo == null) {
                        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-18,Defect:1305323 begin
                        baseInfo = new GroupBaseInfo(year, month, 0, "");
                        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-18,Defect:1305323 end
                        groupArray.put(key, baseInfo);
                        keyArray.add(key);
                    }
                    baseInfo.addPicture(new ItemBaseInfo(pic.mFileId, pic.mMediaType, 0, 0));
                    parallaxArray.remove(0);
                }
            }

            if(keyArray.size() > 0) {
                Collections.sort(keyArray, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer lhs, Integer rhs) {
                        if(lhs < rhs) return 1;
                        else if(lhs > rhs) return -1;
                        else return 0;
                    }
                });
            }

            if(groupArray.size() > 0) {
                for(Integer key : keyArray) {
                    GroupBaseInfo baseInfo = groupArray.get(key);
                    if(baseInfo.getPictureListSize() > 0) {
                        result.add(baseInfo);
                    }
                }
            }
            Log.d(TAG, "} GroupBuilder.queryMediaItemBaseInfoOnMonthMode");
            return result;
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-07,Defect:1270007 end
        }
    }

    public class GroupBaseInfo {
        private int mYear;
        private int mMonth;
        private int mDay;
        private String mAddress;
        private List<ItemBaseInfo> mPictureIdList;

        public GroupBaseInfo(int year, int month, int day, String address) {
            this.mYear = year;
            this.mMonth = month;
            this.mDay = day;
            this.mAddress = address;
            mPictureIdList = new ArrayList<ItemBaseInfo>(20);
        }

        public void addPicture(ItemBaseInfo item) {
            mPictureIdList.add(item);
        }
        //[BugFix]-Add-Begin by TSNJ Junyong.Sun 2016/01/14 PR-1271651
        public List<ItemBaseInfo> getItemBaseInfoList(){
            return mPictureIdList;
        }
        //[BugFix]-Add-END by TSNJ Junyong.Sun 2016/01/14 PR-1271651
        public int getPictureListSize() {
            return mPictureIdList.size();
        }

        public long getIdByIndex(int index) {
            if(index < 0 || index > mPictureIdList.size() - 1)
                throw new IndexOutOfBoundsException("GroupBaseInfo.getIdByIndex index out of list's bounds");

            return mPictureIdList.get(index).id;
        }

        public int getMediaTypeByIndex(int index) {
            if(index < 0 || index > mPictureIdList.size() - 1)
                  throw new IndexOutOfBoundsException("GroupBaseInfo.getMediaTypeByIndex index out of list's bounds");

            return mPictureIdList.get(index).mediaType;
        }

        public ItemBaseInfo getItemBaseInfoByIndex(int index) {
            if(index < 0 || index > mPictureIdList.size() - 1)
                throw new IndexOutOfBoundsException("GroupBaseInfo.getItemBaseInfoByIndex index out of list's bounds");
            return mPictureIdList.get(index);
        }

        public String getAddress() {
            return this.mAddress;
        }

        public String getTime() {
            StringBuffer time = new StringBuffer();
            //[BUGFIX]-Add by TCTNJ,yuanxi.jiang-nb, 2015/01/12,PR1394356  begin
            String[] MONTH_NAME = mApplication.getResources().getStringArray(R.array.month);
            //[BUGFIX]-Add by TCTNJ,yuanxi.jiang-nb, 2015/01/12,PR1394356  end
            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);
            if(mYear == currentYear) {
                if(mCurrentMode == DAY_MODE) {
                    if(mMonth == currentMonth + 1) {
                        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                        calendar.set(currentYear, currentMonth, currentDay, 0, 0, 0);
                        long currentTimeInMillis = calendar.getTimeInMillis();
                        calendar.set(mYear, mMonth - 1, mDay, 0, 0, 0);
                        long groupTimeInMillis = calendar.getTimeInMillis();
                        int distance = (int)((currentTimeInMillis - groupTimeInMillis) / MillisSecondInOneDay);
                        if(distance == 0) {
                            //Add by TSNJ,chunhua.liu, 2016-01-12,defect 1353125 begin
                            time.append(mApplication.getResources().getString(R.string.moment_album_today));
                        } else if(distance == 1) {
                            time.append(mApplication.getResources().getString(R.string.moment_album_yesterday));
                            //Add by TSNJ,chunhua.liu, 2016-01-12,defect 1353125 end
                        } else {
                            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-18,Defect:1305323 begin
                            if (mDay == 0) {
                                time.append(MONTH_NAME[mMonth - 1]);
                            } else {
                                time.append(mDay).append(" ").append(MONTH_NAME[mMonth - 1]);
                            }
                            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-18,Defect:1305323 end
                        }
                    } else {
                        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-08,PR1615695 begin
                        if (mDay == 0) {
                            time.append(MONTH_NAME[mMonth - 1]);
                        } else {
                            time.append(mDay).append(" ").append(MONTH_NAME[mMonth - 1]);
                        }
                        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-08,PR1615695 end
                    }
                } else {
                    time.append(MONTH_NAME[mMonth - 1]);
                }
            } else {
                if(mCurrentMode == DAY_MODE) {
                    time = time.append(mDay).append(" ").append(MONTH_NAME[mMonth - 1]).append(" ").append(mYear);
                } else {
                    time = time.append(MONTH_NAME[mMonth - 1]).append(" ").append(mYear);
                }
            }
            return time.toString();
        }

        public int getBottomOfGroup() {
            int bottomOfGroup = 0;
            if(mPictureIdList.size() > 0) {
                for(ItemBaseInfo item : mPictureIdList) {
                    if((item.y + item.h) > bottomOfGroup) {
                        bottomOfGroup = item.y + item.h;
                    }
                }
            }
            return bottomOfGroup;
        }
    }

    public class ItemBaseInfo {
        public static final int IMAGE_PANORAMA = 0x00000001;
        public static final int IMAGE_LANDSCAPE = 0x00000002;
        public static final int IMAGE_SQUARE = 0x00000004;
        public static final int IMAGE_PORTRAIT = 0x00000008;
        public static final int IMAGE_SLIM = 0x00000010;

        public long id;
        public int mediaType;
        public int bmpWidth;
        public int bmpHeight;
        public float ratio;
        public int layoutType;

        //for layout
        public int x = 0;
        public int y = 0;
        public int w = 0;
        public int h = 0;

        public ItemBaseInfo(long id, int mediaType, int bmpWidth, int bmpHeight) {
            this.id = id;
            this.mediaType = mediaType;
            this.bmpWidth = bmpWidth;
            this.bmpHeight = bmpHeight;
            this.ratio = (float)this.bmpHeight / this.bmpWidth;
            setImageType();
        }

        private void setImageType() {

            if (ratio <= 0.4f) {
                layoutType = IMAGE_PANORAMA;
            } else if (ratio <= 0.85f) {
                layoutType = IMAGE_LANDSCAPE;
            } else if (ratio <= 1.15f) {
                layoutType = IMAGE_SQUARE;
            } else if (ratio <= 2.5f) {
                layoutType = IMAGE_PORTRAIT;
            } else {
                layoutType = IMAGE_SLIM;
            }
        }
    }

}

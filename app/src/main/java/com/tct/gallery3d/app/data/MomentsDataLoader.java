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
package com.tct.gallery3d.app.data;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.LoadingListener;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.data.ContentListener;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaObject;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.data.MomentsAlbum;
import com.tct.gallery3d.data.MomentsAlbum.GroupBaseInfo;
import com.tct.gallery3d.data.MomentsAlbum.ItemBaseInfo;
import com.tct.gallery3d.data.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


public class MomentsDataLoader {

    private static final String TAG = "MomentsDataLoader";

    private static final int DATA_CACHE_SIZE = 3500;

    private static final int MSG_LOAD_START = 1;
    private static final int MSG_LOAD_FINISH = 2;
    private static final int MSG_RUN_OBJECT = 3;

    private static final int MIN_LOAD_COUNT = 256;
    private static final int MAX_LOAD_COUNT = 512;

    private final MediaItem[] mDataCache;
    private final long[] mItemVersion;
    private final long[] mSetVersion;

    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    private int mContentStart = 0;
    private int mContentEnd = 0;

    private int mAlbumSize = 0;
    private long mAlbumVersion = MediaObject.INVALID_DATA_VERSION;
    private long mFailedVersion = MediaObject.INVALID_DATA_VERSION;

    private final Activity mActivityContext;
    private final Handler mMainHandler;
    private DataListener mDataListener;
    private MediaSetListener mMediaSetListener = new MediaSetListener();
    private LoadingListener mLoadingListener;
    private MomentsAlbum mMomentsAlbum;
    private ReloadTask mReloadTask;
    private int mCurrentMode = MomentsAlbum.DAY_MODE;
    private int mCurrentFilterType = MomentsAlbum.FILTER_ALL;

    private boolean isModeChanged = false;

    public interface DataListener {
        void onContentChanged(int index);

        void onSizeChanged(List<GroupBaseInfo> monthGroup, List<GroupBaseInfo> dayGroup, int totalCount, int mode);

        void setCurrentMode(int mode);
    }

    private class MediaSetListener implements ContentListener {
        @Override
        public void onContentDirty() {
            if (mReloadTask != null) {
                mReloadTask.notifyDirty();
            }
        }
    }

    public MomentsDataLoader(AbstractGalleryActivity context) {
        mActivityContext = context;
        GalleryApp application = (GalleryApp) context.getApplication();
        DataManager dataManager = application.getDataManager();
        String mediaSetPath = dataManager.getTopSetPath(DataManager.INCLUDE_LOCAL_MOMENTS);
        mMomentsAlbum = (MomentsAlbum) dataManager.getMediaSet(mediaSetPath);
        mMomentsAlbum.setActivity(mActivityContext);
        mMomentsAlbum.reset();

        mDataCache = new MediaItem[DATA_CACHE_SIZE];
        mItemVersion = new long[DATA_CACHE_SIZE];
        mSetVersion = new long[DATA_CACHE_SIZE];
        Arrays.fill(mItemVersion, MediaObject.INVALID_DATA_VERSION);
        Arrays.fill(mSetVersion, MediaObject.INVALID_DATA_VERSION);

        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_RUN_OBJECT:
                        ((Runnable) message.obj).run();
                        return;
                    case MSG_LOAD_START:
                        if (mLoadingListener != null) mLoadingListener.onLoadingStarted();
                        return;
                    case MSG_LOAD_FINISH:
                        if (mLoadingListener != null) {
                            mLoadingListener.onLoadingFinished(false);
                        }
                        return;
                }
            }
        };
    }

    public void resume() {
        //ExifInfoFilter.getInstance(mActivityContext).checkBurstShotImageAvailability(mActivityContext);
        mReloadTask = new ReloadTask();
        mReloadTask.start();
        mMomentsAlbum.addContentListener(mMediaSetListener);
    }

    public void pause() {
        mMomentsAlbum.removeContentListener(mMediaSetListener);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-09,Defect:1001112 begin
        if (mReloadTask != null) {
            mReloadTask.terminate();
            mReloadTask = null;
        } else {
            Log.e(TAG, "pause() mReloadTask is NULL.");
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-09,Defect:1001112 end
    }

    public void calculateLayout() {
        mMomentsAlbum.calculateLayout();
        if (mReloadTask != null) {
            mReloadTask.notifyDirty();
        }
    }

    public int getMediaSetType() {
        return mMomentsAlbum == null ? MediaSet.MEDIASET_TYPE_UNKNOWN : mMomentsAlbum.getMediaSetType();
    }

    public int getMediaItemCount() {
        return mMomentsAlbum == null ? 0 : mMomentsAlbum.getMediaItemCount();
    }

    public MediaItem get(int index) {
        if (!isActive(index)) {
            return mMomentsAlbum.getMediaItem(index, 1).get(0);
        }
        return mDataCache[index % mDataCache.length];
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 begin
    public ItemBaseInfo getItemBaseInfo(int index) {
        return mMomentsAlbum.findItemBaseInfo(index);
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 end

    public void setCurrentMode(int mode) {
        if (mCurrentMode != mode) {
            mCurrentMode = mode;
            mMomentsAlbum.switchCurrentMode(mCurrentMode);
        }
    }
//    public void setCurrentMode(int mode) {
//        if (mode != MomentsAlbum.DAY_MODE && mode != MomentsAlbum.MONTH_MODE)
//            throw new IllegalArgumentException("MomentsDataLoader.setCurrentMode mode value is invalid");
//        if (mCurrentMode != mode) {
//            mCurrentMode = mode;
//            isModeChanged = true;
//        }
//        mMomentsAlbum.switchCurrentMode(mCurrentMode);
//        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-09,Defect:1001112 begin
//        if (mReloadTask != null) {
//            mReloadTask.notifyDirty();
//        } else {
//            Log.e(TAG, "setCurrentMode() ERROR mReloadTask is NULL. mode:"
//                    + mode);
//        }
//        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-09,Defect:1001112 end
//    }
//
//    public void setCurrentFilter(int filterType) {
//        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-26,Defect:1048523 begin
//        if (filterType < MomentsAlbum.FILTER_ALL || filterType > MomentsAlbum.FILTER_FYUSE)
//            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-26,Defect:1048523 end
//            throw new IllegalArgumentException("MomentsDataLoader.setCurrentFilter filter type is invalid");
//        if (mCurrentFilterType != filterType) {
//            mCurrentFilterType = filterType;
//            mMomentsAlbum.switchCurrentFilter(filterType);
//            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-09,Defect:1001112 begin
//            if (mReloadTask != null) {
//                mReloadTask.notifyDirty();
//            } else {
//                Log.e(TAG, "setCurrentFilter() ERROR mReloadTask is NULL. filterType:"
//                        + filterType);
//            }
//            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-09,Defect:1001112 end
//        }
//    }

    public boolean isActive(int index) {
        return index >= mActiveStart && index < mActiveEnd;
    }

    public int size() {
        return mAlbumSize;
    }

    private void clearSlotItem(int slotIndex) {
        mDataCache[slotIndex] = null;
        mItemVersion[slotIndex] = MediaObject.INVALID_DATA_VERSION;
        mSetVersion[slotIndex] = MediaObject.INVALID_DATA_VERSION;
    }

    public int findItem(Path id) {
        for (int i = mContentStart; i < mContentEnd; i++) {
            MediaItem item = mDataCache[i % DATA_CACHE_SIZE];
            if (item != null && id == item.getPath()) {
                return i;
            }
        }
        return -1;
    }

    public void setDataListener(DataListener listener) {
        mDataListener = listener;
    }

    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    public void setActiveWindow(int start, int end) {
        if (start == mActiveStart && end == mActiveEnd) return;

        Utils.assertTrue(start <= end
                && end - start <= mDataCache.length && end <= mAlbumSize);

        int length = mDataCache.length;
        mActiveStart = start;
        mActiveEnd = end;

        // If no data is visible, keep the cache content
        if (start == end) return;

        int contentStart = Utils.clamp((start + end) / 2 - length / 2,
                0, Math.max(0, mAlbumSize - length));
        int contentEnd = Math.min(contentStart + length, mAlbumSize);
        if (mContentStart > start || mContentEnd < end
                || Math.abs(contentStart - mContentStart) > MIN_LOAD_COUNT) {
            setContentWindow(contentStart, contentEnd);
        }
    }

    private void setContentWindow(int newContentStart, int newContentEnd) {
        if (newContentStart == mContentStart && newContentEnd == mContentEnd) return;

        int oldContentEnd = mContentEnd;
        int oldContentStart = mContentStart;

        // We need change the content window before calling reloadData(...)
        synchronized (this) {
            mContentStart = newContentStart;
            mContentEnd = newContentEnd;
        }

        if (newContentStart >= oldContentEnd || oldContentStart >= newContentEnd) {
            for (int i = oldContentStart, n = oldContentEnd; i < n; ++i) {
                clearSlotItem(i % DATA_CACHE_SIZE);
            }
        } else {
            for (int i = oldContentStart; i < newContentStart; ++i) {
                clearSlotItem(i % DATA_CACHE_SIZE);
            }
            for (int i = newContentEnd, n = oldContentEnd; i < n; ++i) {
                clearSlotItem(i % DATA_CACHE_SIZE);
            }
        }
        if (mReloadTask != null) mReloadTask.notifyDirty();
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_RUN_OBJECT, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static class UpdateInfo {
        public long version;
        public int reloadStart;
        public int reloadCount;

        public int size;
        public ArrayList<MediaItem> items;
    }

    private class GetUpdateInfo implements Callable<UpdateInfo> {
        private final long mVersion;

        public GetUpdateInfo(long version) {
            mVersion = version;
        }

        @Override
        public UpdateInfo call() throws Exception {
            if (mFailedVersion == mVersion) {
                // previous loading failed, return null to pause loading
                return null;
            }
            UpdateInfo info = new UpdateInfo();
            long version = mVersion;
            info.version = mAlbumVersion;
            info.size = mAlbumSize;
            long setVersion[] = mSetVersion;
            if (mAlbumVersion != mVersion) {
                info.reloadStart = 0;
                info.reloadCount = 0;
            } else {
                for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                    int index = i % DATA_CACHE_SIZE;
                    if (setVersion[index] != version) {
                        info.reloadStart = i;
                        info.reloadCount = Math.min(MAX_LOAD_COUNT, n - i);
                        return info;
                    }
                }
            }
            return mAlbumVersion == mVersion ? null : info;
        }
    }

    private class UpdateContent implements Callable<Void> {

        private UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            mUpdateInfo = info;
        }

        @Override
        public Void call() throws Exception {
            UpdateInfo info = mUpdateInfo;
            mAlbumVersion = info.version;
            if (mAlbumSize != info.size || isModeChanged) {
                mAlbumSize = info.size;
                if (mDataListener != null) {
                    List<GroupBaseInfo> monthGroup = mMomentsAlbum.buildGroupByMode(MomentsAlbum.MONTH_MODE);
                    List<GroupBaseInfo> dayGroup = mMomentsAlbum.buildGroupByMode(MomentsAlbum.DAY_MODE);
                    mDataListener.onSizeChanged(monthGroup, dayGroup, mAlbumSize, mCurrentMode);
                }
                if (mContentEnd > mAlbumSize) mContentEnd = mAlbumSize;
                if (mActiveEnd > mAlbumSize) mActiveEnd = mAlbumSize;
            }

            ArrayList<MediaItem> items = info.items;

            mFailedVersion = MediaObject.INVALID_DATA_VERSION;
            if ((items == null) || items.isEmpty()) {
                if (info.reloadCount > 0) {
                    mFailedVersion = info.version;
                    Log.d(TAG, "loading failed: " + mFailedVersion);
                }
                return null;
            }
            int start = Math.max(info.reloadStart, mContentStart);
            int end = Math.min(info.reloadStart + items.size(), mContentEnd);

            for (int i = start; i < end; ++i) {
                int index = i % DATA_CACHE_SIZE;
                mSetVersion[index] = info.version;
                MediaItem updateItem = items.get(i - info.reloadStart);
                //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,PR676093 begin
                if (updateItem != null) {
                    long itemVersion = updateItem.getDataVersion();
                    if (mItemVersion[index] != itemVersion) {
                        mItemVersion[index] = itemVersion;
                        mDataCache[index] = updateItem;
                    }
                    if (mDataListener != null && i >= mActiveStart && i < mActiveEnd) {
                        mDataListener.onContentChanged(i);
                    }
                }
                //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,PR676093 end
            }
            return null;
        }
    }

    private class ReloadTask extends Thread {

        private volatile boolean mIsActive = true;
        private volatile boolean mIsDirty = true;
        private boolean mIsRunning = true;

        private void updateLoading(boolean loading) {
            if (mIsRunning == loading) return;
            mIsRunning = loading;
            mMainHandler.sendEmptyMessage(loading ? MSG_LOAD_START : MSG_LOAD_FINISH);
        }

        @Override
        public void run() {
            boolean updateComplete = false;
            while (mIsActive) {
                synchronized (this) {
                    if (mIsActive && !mIsDirty && updateComplete) {
                        updateLoading(false);
                        if (mFailedVersion != MediaObject.INVALID_DATA_VERSION) {
                            Log.d(TAG, "reload pause");
                        }
                        Utils.waitWithoutInterrupt(this);
                        if (mIsActive && (mFailedVersion != MediaObject.INVALID_DATA_VERSION)) {
                            Log.d(TAG, "reload resume");
                        }
                        continue;
                    }
                    mIsDirty = false;
                }
                updateLoading(true);
                long version = mMomentsAlbum.reload();
                UpdateInfo info = executeAndWait(new GetUpdateInfo(version));
                updateComplete = info == null;
                if (updateComplete) continue;
                if (info.version != version) {
                    info.size = mMomentsAlbum.getMediaItemCount();
                    info.version = version;
                }
                if (info.reloadCount > 0) {
                    info.items = mMomentsAlbum.getMediaItem(info.reloadStart, info.reloadCount);
                }
                executeAndWait(new UpdateContent(info));
            }
            updateLoading(false);
        }

        public synchronized void notifyDirty() {
            mIsDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            mIsActive = false;
            notifyAll();
        }
    }

    /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1892017*/
    public void updateItem() {
        if (mReloadTask != null)
            mReloadTask.notifyDirty();
    }
     /*MODIFIED-END by jian.pan1,BUG-1892017*/

    public long getmVersion() {
        return mAlbumVersion;
    }

    public int getActiveStart() {
        return mActiveStart;
    }

    public int getActiveEnd() {
        return mActiveEnd;
    }
}

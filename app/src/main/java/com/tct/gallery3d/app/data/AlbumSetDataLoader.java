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
/* 12/04/2015|    jialiang.ren     |      PR-947998       |[Monkey][Crash]com.tct.gallery3d java.lang.IllegalArgumentException*/
/* ----------|---------------------|----------------------|-------------------------------------------------------------------*/
/* 16/04/2015|    ye.chen     |      PR-977156            |[Gallery][SMC][5.1.9.1.0111.0][Monitor]Gallery crash when daily use./
/* ----------|---------------------|----------------------|-------------------------------------------------------------------*/
package com.tct.gallery3d.app.data;

import android.os.Handler;
import android.os.Message;
import android.os.Process;

import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.LoadingListener;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.data.ContentListener;
import com.tct.gallery3d.data.LocalAlbumSet;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaObject;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.data.Path;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/*MODIFIED-END by jian.pan1,BUG-1871401*/

public class AlbumSetDataLoader {
    private static final String TAG = "AlbumSetDataAdapter";

    private static final int INDEX_NONE = -1;

    private static final int MIN_LOAD_COUNT = 4;

    private static final int MSG_LOAD_START = 1;
    private static final int MSG_LOAD_FINISH = 2;
    private static final int MSG_RUN_OBJECT = 3;

    public interface DataListener {
        void onContentChanged(int index);

        void onSizeChanged(int size, boolean showCollapse);
    }
    public interface AllAlbumSetListener{
        void getAllAlbums(List<MediaSet> list);
    }
    private final MediaSet[] mData;
    private final MediaItem[] mCoverItem;
    private final int[] mTotalCount;
    private final long[] mItemVersion;
    private final long[] mSetVersion;

    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    private int mContentStart = 0;
    private int mContentEnd = 0;

    private final MediaSet mSource;
    private long mSourceVersion = MediaObject.INVALID_DATA_VERSION;
    private int mSize;

    private DataListener mDataListener;
    private AllAlbumSetListener mAllAlbumSetListener;
    private LoadingListener mLoadingListener;
    private ReloadTask mReloadTask;

    private final Handler mMainHandler;

    private final MySourceListener mSourceListener = new MySourceListener();

    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-14,ALM-1719066 begin
    private final AbstractGalleryActivity mActivity;
    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-14,ALM-1719066 end

    public AlbumSetDataLoader(AbstractGalleryActivity activity, MediaSet albumSet, int cacheSize) {
        mSource = Utils.checkNotNull(albumSet);
        mCoverItem = new MediaItem[cacheSize];
        mData = new MediaSet[cacheSize];
        mTotalCount = new int[cacheSize];
        mItemVersion = new long[cacheSize];
        mSetVersion = new long[cacheSize];
        Arrays.fill(mItemVersion, MediaObject.INVALID_DATA_VERSION);
        Arrays.fill(mSetVersion, MediaObject.INVALID_DATA_VERSION);

        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-14,ALM-1719066 begin
        mActivity = activity;
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-14,ALM-1719066 end

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
                        if (mLoadingListener != null) mLoadingListener.onLoadingFinished(false);
                        return;
                }
            }
        };
    }

    public void pause() {
        if (mReloadTask != null) {
            mReloadTask.terminate();
        }
        mReloadTask = null;
        mSource.removeContentListener(mSourceListener);
    }

    public void resume() {
        mSource.addContentListener(mSourceListener);
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-14,ALM-1719066 begin
         /*MODIFIED-BEGIN by jian.pan1, 2016-03-29,BUG-1871401*/
//        SharedPreferences sp = mActivity.getSharedPreferences("Gallery", mActivity.MODE_PRIVATE);
//        boolean firstLoad = sp.getBoolean("albumset_first_load", true);
//        if (!firstLoad) {
//            ExifInfoFilter.getInstance(mActivity).checkBurstShotImageAvailability(mActivity);
//        } else {
//            Editor editor = sp.edit();
//            editor.putBoolean("albumset_first_load", false);
//            editor.commit();
//        }
         /*MODIFIED-END by jian.pan1,BUG-1871401*/
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-14,ALM-1719066 end
        mReloadTask = new ReloadTask();
        mReloadTask.start();
    }

    private void assertIsActive(int index) throws Exception {
        if (index < mActiveStart || index >= mActiveEnd) {
            throw new IllegalArgumentException(String.format(
                    "%s not in (%s, %s)", index, mActiveStart, mActiveEnd));
        }
    }

    public int getMediaSetCount() {
        return mData.length;
    }

    public MediaSet getMediaSet(int index) {
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-14,PR947998 begin
        try {
            assertIsActive(index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-14,PR947998 end
        return mData[index % mData.length];
    }

    public MediaItem getCoverItem(int index) {
        try {
            assertIsActive(index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return mCoverItem[index % mCoverItem.length];
    }

    public int getTotalCount(int index) {
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-14,PR947998 begin
        try {
            assertIsActive(index);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-14,PR947998 end
        return mTotalCount[index % mTotalCount.length];
    }

    public int getActiveStart() {
        return mActiveStart;
    }

    public int getActiveEnd() {
        return mActiveEnd;
    }

    public boolean isActive(int index) {
        return index >= mActiveStart && index < mActiveEnd;
    }

    public int size() {
        return mSize;
    }

    // Returns the index of the MediaSet with the given path or
    // -1 if the path is not cached
    public int findSet(Path id) {
        int length = mData.length;
        for (int i = 0; i < mData.length; i++) {
            MediaSet set = mData[i % length];
            if (set == null) continue;
            int count = set.getMediaItemCount();
            List<MediaItem> itemList = set.getMediaItem(0, count);
            for (int j = 0; j < itemList.size(); j++) {
                MediaItem item = itemList.get(i % itemList.size());
                if (item != null && id == item.getPath()) {
                    return i;
                }
            }
        }
        /*for (int i = mContentStart; i < mContentEnd; i++) {
            MediaSet set = mData[i % length];
            if (set != null && id == set.getPath()) {
                return i;
            }
        }*/
        return -1;
    }

    private void clearSlot(int slotIndex) {
        mData[slotIndex] = null;
        mCoverItem[slotIndex] = null;
        mTotalCount[slotIndex] = 0;
        mItemVersion[slotIndex] = MediaObject.INVALID_DATA_VERSION;
        mSetVersion[slotIndex] = MediaObject.INVALID_DATA_VERSION;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd) return;
        int length = mCoverItem.length;

        int start = this.mContentStart;
        int end = this.mContentEnd;

        mContentStart = contentStart;
        mContentEnd = contentEnd;

        if (contentStart >= end || start >= contentEnd) {
            for (int i = start, n = end; i < n; ++i) {
                clearSlot(i % length);
            }
        } else {
            for (int i = start; i < contentStart; ++i) {
                clearSlot(i % length);
            }
            for (int i = contentEnd, n = end; i < n; ++i) {
                clearSlot(i % length);
            }
        }
        if (mReloadTask != null) {
            mReloadTask.notifyDirty();
        }
    }

    public void setActiveWindow(int start, int end) {
        if (start == mActiveStart && end == mActiveEnd) return;

        Utils.assertTrue(start <= end
                && end - start <= mCoverItem.length && end <= mSize);

        mActiveStart = start;
        mActiveEnd = end;

        int length = mCoverItem.length;
        // If no data is visible, keep the cache content
        if (start == end) return;

        int contentStart = Utils.clamp((start + end) / 2 - length / 2,
                0, Math.max(0, mSize - length));
        int contentEnd = Math.min(contentStart + length, mSize);
        if (mContentStart > start || mContentEnd < end
                || Math.abs(contentStart - mContentStart) > MIN_LOAD_COUNT) {
            setContentWindow(contentStart, contentEnd);
        }
    }

    private class MySourceListener implements ContentListener {
        @Override
        public void onContentDirty() {
            mReloadTask.notifyDirty();
        }
    }

    public void setModelListener(DataListener listener) {
        mDataListener = listener;
    }

    public void setAllAlbumSetListener(AllAlbumSetListener listener) {
        this.mAllAlbumSetListener = listener;
    }

    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    private static class UpdateInfo {
        public long version;
        public int index;

        public int size;
        public MediaSet item;
        public List<MediaItem> cover;
        public int totalCount;
    }

    private class GetUpdateInfo implements Callable<UpdateInfo> {

        private final long mVersion;

        public GetUpdateInfo(long version) {
            mVersion = version;
        }

        private int getInvalidIndex(long version) {
            long setVersion[] = mSetVersion;
            int length = setVersion.length;
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                int index = i % length;
                if (setVersion[i % length] != version) return i;
            }
            return INDEX_NONE;
        }

        @Override
        public UpdateInfo call() throws Exception {
            //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-24,PR1856762 begin
            com.tct.gallery3d.util.Log.i("DEBUG FOR 1856762", mVersion + " " + mSourceVersion);
            //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-24,PR1856762 end
            int index = getInvalidIndex(mVersion);
            if (index == INDEX_NONE && mSourceVersion == mVersion) return null;
            UpdateInfo info = new UpdateInfo();
            info.version = mSourceVersion;
            info.index = index;
            info.size = mSize;
            return info;
        }
    }

    private class UpdateContent implements Callable<Void> {
        private final UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            mUpdateInfo = info;
        }

        @Override
        public Void call() {
            // Avoid notifying listeners of status change after pause
            // Otherwise gallery will be in inconsistent state after resume.
            if (mReloadTask == null) return null;
            UpdateInfo info = mUpdateInfo;
            mSourceVersion = info.version;
            //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-24,PR1856762 begin
            com.tct.gallery3d.util.Log.i("DEBUG FOR 1856762", mSize + " " + info.size + " " + mSourceVersion);
            //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-24,PR1856762 end
            if (mSize != info.size) {
                mSize = info.size;

                boolean showCollapse = false;
                if (mSource != null && mSource instanceof LocalAlbumSet) {
                    showCollapse = mSource.getShowCollapseAlbum();
                    mAllAlbumSetListener.getAllAlbums(((LocalAlbumSet) mSource).getAllALbums());
                }
                if (mDataListener != null) mDataListener.onSizeChanged(mSize, showCollapse);
                if (mContentEnd > mSize) mContentEnd = mSize;
                if (mActiveEnd > mSize) mActiveEnd = mSize;
            }
            // Note: info.index could be INDEX_NONE, i.e., -1
            if (info.index >= mContentStart && info.index < mContentEnd) {
                int pos = info.index % mCoverItem.length;
                mSetVersion[pos] = info.version;
                long itemVersion = info.item.getDataVersion();
                if (mItemVersion[pos] == itemVersion) return null;
                mItemVersion[pos] = itemVersion;
                mData[pos] = info.item;
                if (info.cover != null) {
                    mCoverItem[pos] = info.cover.get(0);
                }
                mTotalCount[pos] = info.totalCount;
                if (mDataListener != null
                        && info.index >= mActiveStart && info.index < mActiveEnd) {
                    mDataListener.onContentChanged(info.index);
                }
            }
            return null;
        }
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        mMainHandler.sendMessage(
                mMainHandler.obtainMessage(MSG_RUN_OBJECT, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: load active range first
    private class ReloadTask extends Thread {
        private volatile boolean mActive = true;
        private volatile boolean mDirty = true;
        private volatile boolean mIsLoading = false;

        private void updateLoading(boolean loading) {
            if (mIsLoading == loading) return;
            mIsLoading = loading;
            mMainHandler.sendEmptyMessage(loading ? MSG_LOAD_START : MSG_LOAD_FINISH);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            boolean updateComplete = false;
            while (mActive) {
                synchronized (this) {
                    if (mActive && !mDirty && updateComplete) {
                        if (!mSource.isLoading()) updateLoading(false);
                        Utils.waitWithoutInterrupt(this);
                        continue;
                    }
                }
                mDirty = false;
                updateLoading(true);
                long version = mSource.reload();
                if (!mActive) break;//[BUGFIX]-begin by TCTNJ.ye.chen,04/16/2015,977156
                UpdateInfo info = executeAndWait(new GetUpdateInfo(version));
                updateComplete = info == null;
                if (updateComplete) continue;
                if (info.version != version) {
                    info.version = version;
                    info.size = mSource.getSubMediaSetCount();

                    // If the size becomes smaller after reload(), we may
                    // receive from GetUpdateInfo an index which is too
                    // big. Because the main thread is not aware of the size
                    // change until we call UpdateContent.
                    if (info.index >= info.size) {
                        info.index = INDEX_NONE;
                    }
                }
                if (info.index != INDEX_NONE) {
                    info.item = mSource.getSubMediaSet(info.index);
                    if (info.item == null) continue;
                    info.cover = info.item.getCoverMediaItem();
                    info.totalCount = info.item.getTotalMediaItemCount();
                    Log.i(TAG, "ReloadTask info.totalCount = "
                            + info.totalCount);
                }
                executeAndWait(new UpdateContent(info));
            }
            updateLoading(false);
        }

        public synchronized void notifyDirty() {
            mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            mActive = false;
            notifyAll();
        }
    }

    // [BUGFIX]-Add by TCTNJ,xiangyu.liu, 2016-03-12,PR1783373 -start
    public long getSourceVersion() {
        return mSourceVersion;
    }
    // [BUGFIX]-Add by TCTNJ,xiangyu.liu, 2016-03-12,PR1783373 -end

}



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
/* ----------|----------------------|----------------------|------------------------------------------------*/
/* 31/01/2015|    ye.chen           |      PR-904080       |DRM processing in Gallery
/* ----------|----------------------|----------------------|------------------------------------------------------- */
/* ----------|----------------------|----------------------|------------------------------------------------*/
/* 21/03/2015|    qiang.ding1       |      PR-954845       |[Monitor][Force close][Gallery]Occur gallery force close after delete some pictures from PC.*/
/* ----------|----------------------|----------------------|------------------------------------------------------- */
/* 19/05/2015|chengbin.du           |PR1001124             |[SW][Gallery][ANR]Gallery will ANR when slideshow. */
/*-----------|----------------------|----------------------|----------------------------------------*/
/* 10/11/2015|dongliang.feng        |PR790408              |[Android5.1][Gallery_v5.2.3.1.1.0307.0][GD]It has 5 picture in one row in the day view in the landscape mode */
/* ----------|----------------------|----------------------|----------------- */
/* 27/10/2015|dongliang.feng        |PR786159              |[Gallery]The display is abnormal when press back key on view picture interface */
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.data;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.util.Future;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images.ImageColumns;
/* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

// MediaSet is a directory-like data structure.
// It contains MediaItems and sub-MediaSets.
//
// The primary interface are:
// getMediaItemCount(), getMediaItem() and
// getSubMediaSetCount(), getSubMediaSet().
//
// getTotalMediaItemCount() returns the number of all MediaItems, including
// those in sub-MediaSets.
public abstract class MediaSet extends MediaObject {
    private static final String TAG = "MediaSet";

    public static final int MEDIAITEM_BATCH_FETCH_COUNT = 28;
    public static final int INDEX_NOT_FOUND = -1;

    public static final int SYNC_RESULT_SUCCESS = 0;
    public static final int SYNC_RESULT_CANCELLED = 1;
    public static final int SYNC_RESULT_ERROR = 2;

    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-19,PR1001124 begin
    public static final int MEDIASET_TYPE_UNKNOWN = 0;
    public static final int MEDIASET_TYPE_IMAGE = 2;
    public static final int MEDIASET_TYPE_VIDEO = 4;
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-10-27, PR786159 begin
    public static final int MEDIASET_TYPE_GIT = 8;
    public static final int MEDIASET_TYPE_ALL = MEDIASET_TYPE_IMAGE | MEDIASET_TYPE_VIDEO | MEDIASET_TYPE_GIT | MEDIASET_TYPE_UNKNOWN;
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-10-27, PR786159 end

    protected int mMediaSetType = MEDIASET_TYPE_UNKNOWN;
    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-19,PR1001124 end

    protected int mAlbumType;
    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-17,BUG-2208330*/
    protected String mAlbumFilePath;
    public int getAlbumType() {
        return mAlbumType;
    }

    public String getAlbumFilePath() {
        return mAlbumFilePath;
    }
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
    /** Listener to be used with requestSync(SyncListener). */

    protected boolean showCollapseAlbum = false;

    public static interface SyncListener {
        /**
         * Called when the sync task completed. Completion may be due to normal termination,
         * an exception, or cancellation.
         *
         * @param mediaSet the MediaSet that's done with sync
         * @param resultCode one of the SYNC_RESULT_* constants
         */
        void onSyncDone(MediaSet mediaSet, int resultCode);
    }

    public MediaSet(Path path, long version) {
        super(path, version);
    }

    public int getMediaItemCount() {
        return 0;
    }

    public boolean getShowCollapseAlbum() {
        return showCollapseAlbum;
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-19,PR1001124 begin
    public int getMediaSetType() {
        Log.d(TAG, "MediaSetType = " + mMediaSetType);
        return mMediaSetType;
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-19,PR1001124 end

    // Returns the media items in the range [start, start + count).
    //
    // The number of media items returned may be less than the specified count
    // if there are not enough media items available. The number of
    // media items available may not be consistent with the return value of
    // getMediaItemCount() because the contents of database may have already
    // changed.
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        return new ArrayList<MediaItem>();
    }

    public List<MediaItem> getCoverMediaItem() {
        List<MediaItem> items = getMediaItem(0, 12); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-11-10, PR790408
        if (items.size() > 0) return items;
        for (int i = 0, n = getSubMediaSetCount(); i < n; i++) {
            List<MediaItem> cover = getSubMediaSet(i).getCoverMediaItem();
            if (cover != null) return cover;
        }
        return null;
    }

    public int getSubMediaSetCount() {
        return 0;
    }

    public MediaSet getSubMediaSet(int index) {
        throw new IndexOutOfBoundsException();
    }

    public boolean isLeafAlbum() {
        return false;
    }

    public boolean isCameraRoll() {
        return false;
    }

    /**
     * Method {@link #reload()} may process the loading task in background, this method tells
     * its client whether the loading is still in process or not.
     */
    public boolean isLoading() {
        return false;
    }

    public int getTotalMediaItemCount() {
        int total = getMediaItemCount();
        for (int i = 0, n = getSubMediaSetCount(); i < n; i++) {
            total += getSubMediaSet(i).getTotalMediaItemCount();
        }
        return total;
    }
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-01-31,PR904080 begin
    public int getDrmCount(){
        int count = 0;
        ArrayList<MediaItem> list = getMediaItem(
                0, MEDIAITEM_BATCH_FETCH_COUNT);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isDrm == 1){
                count++;
            }
        }
        return count;
    }
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-01-31,PR904080 begin
    // TODO: we should have better implementation of sub classes
    public int getIndexOfItem(Path path, int hint) {
        // hint < 0 is handled below
        // first, try to find it around the hint
        int start = Math.max(0,
                hint - MEDIAITEM_BATCH_FETCH_COUNT / 2);
        ArrayList<MediaItem> list = getMediaItem(
                start, MEDIAITEM_BATCH_FETCH_COUNT);
        int index = getIndexOf(path, list);
        if (index != INDEX_NOT_FOUND) return start + index;

        // try to find it globally
        start = start == 0 ? MEDIAITEM_BATCH_FETCH_COUNT : 0;
        list = getMediaItem(start, MEDIAITEM_BATCH_FETCH_COUNT);
        while (true) {
            index = getIndexOf(path, list);
            if (index != INDEX_NOT_FOUND) return start + index;
            if (list.size() < MEDIAITEM_BATCH_FETCH_COUNT) return INDEX_NOT_FOUND;
            start += MEDIAITEM_BATCH_FETCH_COUNT;
            list = getMediaItem(start, MEDIAITEM_BATCH_FETCH_COUNT);
        }
    }

    protected int getIndexOf(Path path, ArrayList<MediaItem> list) {
        for (int i = 0, n = list.size(); i < n; ++i) {
            // item could be null only in ClusterAlbum
            MediaObject item = list.get(i);
            if (item != null && item.mPath == path) return i;
        }
        return INDEX_NOT_FOUND;
    }

    public abstract String getName();

    private WeakHashMap<ContentListener, Object> mListeners =
            new WeakHashMap<ContentListener, Object>();

    // NOTE: The MediaSet only keeps a weak reference to the listener. The
    // listener is automatically removed when there is no other reference to
    // the listener.
    public void addContentListener(ContentListener listener) {
        mListeners.put(listener, null);
    }

    public void removeContentListener(ContentListener listener) {
        mListeners.remove(listener);
    }

    // This should be called by subclasses when the content is changed.
    public void notifyContentChanged() {
        for (ContentListener listener : mListeners.keySet()) {
            listener.onContentDirty();
        }
    }

    // Reload the content. Return the current data version. reload() should be called
    // in the same thread as getMediaItem(int, int) and getSubMediaSet(int).
    public abstract long reload();

    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(MediaDetails.INDEX_TITLE, getName());
        return details;
    }

    // Enumerate all media items in this media set (including the ones in sub
    // media sets), in an efficient order. ItemConsumer.consumer() will be
    // called for each media item with its index.
    public void enumerateMediaItems(ItemConsumer consumer) {
        enumerateMediaItems(consumer, 0);
    }

    public void enumerateTotalMediaItems(ItemConsumer consumer) {
        enumerateTotalMediaItems(consumer, 0);
    }

    public static interface ItemConsumer {
        void consume(int index, MediaItem item);
    }

    // The default implementation uses getMediaItem() for enumerateMediaItems().
    // Subclasses may override this and use more efficient implementations.
    // Returns the number of items enumerated.
    protected int enumerateMediaItems(ItemConsumer consumer, int startIndex) {
        int total = getMediaItemCount();
        int start = 0;
        while (start < total) {
            int count = Math.min(MEDIAITEM_BATCH_FETCH_COUNT, total - start);
            ArrayList<MediaItem> items = getMediaItem(start, count);
            for (int i = 0, n = items.size(); i < n; i++) {
                MediaItem item = items.get(i);
                consumer.consume(startIndex + start + i, item);
            }
            start += count;
        }
        return total;
    }

    // Recursively enumerate all media items under this set.
    // Returns the number of items enumerated.
    protected int enumerateTotalMediaItems(
            ItemConsumer consumer, int startIndex) {
        int start = 0;
        start += enumerateMediaItems(consumer, startIndex);
        int m = getSubMediaSetCount();
        for (int i = 0; i < m; i++) {
     //[BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-03-21,PR954845  begin
            try{
                start += getSubMediaSet(i).enumerateTotalMediaItems(
                        consumer, startIndex + start);
            }catch (NullPointerException e){
                Log.e(TAG, "getSubMediaSet(i) is null!");
            }
      //[BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-03-21,PR954845  end
        }
        return start;
    }

    /**
     * Requests sync on this MediaSet. It returns a Future object that can be used by the caller
     * to query the status of the sync. The sync result code is one of the SYNC_RESULT_* constants
     * defined in this class and can be obtained by Future.get().
     *
     * Subclasses should perform sync on a different thread.
     *
     * The default implementation here returns a Future stub that does nothing and returns
     * SYNC_RESULT_SUCCESS by get().
     */
    public Future<Integer> requestSync(SyncListener listener) {
        listener.onSyncDone(this, SYNC_RESULT_SUCCESS);
        return FUTURE_STUB;
    }

    private static final Future<Integer> FUTURE_STUB = new Future<Integer>() {
        @Override
        public void cancel() {}

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Integer get() {
            return SYNC_RESULT_SUCCESS;
        }

        @Override
        public void waitDone() {}
    };

    protected Future<Integer> requestSyncOnMultipleSets(MediaSet[] sets, SyncListener listener) {
        return new MultiSetSyncFuture(sets, listener);
    }

    private class MultiSetSyncFuture implements Future<Integer>, SyncListener {
        @SuppressWarnings("hiding")
        private static final String TAG = "Gallery.MultiSetSync";

        private final SyncListener mListener;
        private final Future<Integer> mFutures[];

        private boolean mIsCancelled = false;
        private int mResult = -1;
        private int mPendingCount;

        @SuppressWarnings("unchecked")
        MultiSetSyncFuture(MediaSet[] sets, SyncListener listener) {
            mListener = listener;
            mPendingCount = sets.length;
            mFutures = new Future[sets.length];

            synchronized (this) {
                for (int i = 0, n = sets.length; i < n; ++i) {
                    mFutures[i] = sets[i].requestSync(this);
                    Log.d(TAG, "  request sync: " + Utils.maskDebugInfo(sets[i].getName()));
                }
            }
        }

        @Override
        public synchronized void cancel() {
            if (mIsCancelled) return;
            mIsCancelled = true;
            for (Future<Integer> future : mFutures) future.cancel();
            if (mResult < 0) mResult = SYNC_RESULT_CANCELLED;
        }

        @Override
        public synchronized boolean isCancelled() {
            return mIsCancelled;
        }

        @Override
        public synchronized boolean isDone() {
            return mPendingCount == 0;
        }

        @Override
        public synchronized Integer get() {
            waitDone();
            return mResult;
        }

        @Override
        public synchronized void waitDone() {
            try {
                while (!isDone()) wait();
            } catch (InterruptedException e) {
                Log.d(TAG, "waitDone() interrupted");
            }
        }

        // SyncListener callback
        @Override
        public void onSyncDone(MediaSet mediaSet, int resultCode) {
            SyncListener listener = null;
            synchronized (this) {
                if (resultCode == SYNC_RESULT_ERROR) mResult = SYNC_RESULT_ERROR;
                --mPendingCount;
                if (mPendingCount == 0) {
                    listener = mListener;
                    notifyAll();
                }
                Log.d(TAG, "onSyncDone: " + Utils.maskDebugInfo(mediaSet.getName())
                        + " #pending=" + mPendingCount);
            }
            if (listener != null) listener.onSyncDone(MediaSet.this, mResult);
        }
    }

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
    protected static final String[] PROJECTION = { FileColumns._ID, FileColumns.DATA, FileColumns.DATE_MODIFIED,
            FileColumns.MEDIA_TYPE, ImageColumns.DATE_TAKEN };
    protected static final String[] NEWPROJECTION = {FileColumns._ID, FileColumns.DATA, FileColumns.DATE_MODIFIED,
            FileColumns.MEDIA_TYPE, ImageColumns.DATE_TAKEN,
            GappTypeInfo.GAPP_MEDIA_TYPE,
            GappTypeInfo.GAPP_BURST_ID,
            GappTypeInfo.GAPP_BURST_INDEX};

    protected static final String SELECTION = "( " + FileColumns.MEDIA_TYPE + "=" + FileColumns.MEDIA_TYPE_IMAGE + " OR "
            + FileColumns.MEDIA_TYPE + "=" + FileColumns.MEDIA_TYPE_VIDEO + " )";
    protected static final String VOLUME_NAME = "external";
    protected static final Uri EXTERNAL_URI =  MediaStore.Files.getContentUri(VOLUME_NAME);

    public static void filterExif(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                filterExif(context, EXTERNAL_URI);
            }
        }).start();
    }

    protected static void filterExif(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        final ExifInfoFilter filter = ExifInfoFilter.getInstance(context);
        boolean isOld = false ;
        Cursor cursor = null;
        try{
            cursor = resolver.query(uri, NEWPROJECTION, SELECTION, null, FileColumns._ID + " DESC");
        }catch (SQLiteException e){
            cursor = resolver.query(uri, PROJECTION, SELECTION, null, FileColumns._ID + " DESC");
            isOld = true;
        }

        try {
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    int idIndex = cursor.getColumnIndex(FileColumns._ID);
                    int dataIndex = cursor.getColumnIndex(FileColumns.DATA);
                    int dateModifiedIndex = cursor.getColumnIndex(FileColumns.DATE_MODIFIED);
                    int dateTakenIndex = cursor.getColumnIndex(ImageColumns.DATE_TAKEN);
                    int mediaTypeIndex = cursor.getColumnIndex(FileColumns.MEDIA_TYPE);

                    long id = cursor.getLong(idIndex);
                    final String path = cursor.getString(dataIndex);
                    long timestamp = cursor.getLong(dateModifiedIndex) * 1000;
                    if (!cursor.isNull(dateTakenIndex)) {
                        timestamp = cursor.getLong(dateTakenIndex);
                    }
                    final int mediaType = cursor.getInt(mediaTypeIndex);
                    final long modifyTime = timestamp;
                    final String idStr = String.valueOf(id);
                    int type = filter.queryType(idStr);
                    if (type == ExifInfoFilter.NONE) {
                        if(!isOld){
                            int gappMediaTypeIndex  = cursor.getColumnIndex(GappTypeInfo.GAPP_MEDIA_TYPE);
                            int burstIdIndex  = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_ID);
                            int burstIndexIndex  = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_INDEX);
                            GappTypeInfo gappTypeInfo = new GappTypeInfo();
                            gappTypeInfo.setType(cursor.getInt(gappMediaTypeIndex));
                            gappTypeInfo.setBurstshotId(cursor.getInt(burstIdIndex));
                            gappTypeInfo.setBurstshotIndex(cursor.getInt(burstIndexIndex));
                            filter.filter(idStr, path, mediaType, modifyTime, true, false, gappTypeInfo);
                        }else{
                            filter.filter(idStr, path, mediaType, modifyTime, true, false, null);
                        }
                    }
                } while (cursor.moveToNext());
                filter.notifySaveExifCache();
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
}

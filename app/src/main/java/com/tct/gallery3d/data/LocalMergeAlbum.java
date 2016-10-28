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
/* 03/23/2015| jian.pan1            | PR956360             |com.tct.gallery3d happen crash due to java.lang.NullPointerException.
/* ----------|----------------------|----------------------|--------------------------------------------------- */
/* ----------|----------------------|----------------------|------------------------------------------------------- */
/* 03/31/2015| qiang.ding1          | PR959260             |[Android5.0][Gallery_v5.1.9.1.0108.0][Force close][Monitor]It
 *           |                      |                      |will pop up gallery force close when copying mang pictures from PC
/* ----------|----------------------|----------------------|--------------------------------------------------- */
/* 19/05/2015 |chengbin.du          |PR1001124             |[SW][Gallery][ANR]Gallery will ANR when slideshow. */
/*------------|---------------------|----------------------|----------------------------------------*/
/* 25/08/2015 |    jialiang.ren     |      PR-1010585         | [Gallery]The time to display rubbish bin is too long*/
/*------------|---------------------|-------------------------|-----------------------------------------------------*/
/* 02/06/2015 |    jialiang.ren     |      PR-1006320         |[Android][Gallery_v5.1.13.1.0203.0]There will appear a  */
/*                                                             animation that deleted picture back to original location*/
/*------------|---------------------|-------------------------|--------------------------------------------------------*/

package com.tct.gallery3d.data;

import android.net.Uri;
import android.provider.MediaStore;

import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.common.ApiHelper;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

// MergeAlbum merges items from two or more MediaSets. It uses a Comparator to
// determine the order of items. The items are assumed to be sorted in the input
// media sets (with the same order that the Comparator uses).
//
// This only handles MediaItems, not SubMediaSets.
public class LocalMergeAlbum extends MediaSet implements ContentListener {
    private static final String TAG = "LocalMergeAlbum";
    //[BUGFIX]-Add by TCTNJ,jun.xie-nb, 2016-01-04,ALM-1140847 begin
    private static final int PAGE_SIZE = 64;//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-09,PR1010585
    //[BUGFIX]-Add by TCTNJ,jun.xie-nb, 2016-01-04,ALM-1140847 end
    private final Comparator<MediaItem> mComparator;
    private final MediaSet[] mSources;

    private FetchCache[] mFetcher;
    private int mSupportedOperation;
    private int mBucketId;
    private String mAlbumName = null;

    // mIndex maps global position to the position of each underlying media sets.
    private TreeMap<Integer, int[]> mIndex = new TreeMap<Integer, int[]>();

    public LocalMergeAlbum(
            Path path, Comparator<MediaItem> comparator, MediaSet[] sources, int bucketId) {
        super(path, INVALID_DATA_VERSION);
        mComparator = comparator;
        mSources = sources;
        mBucketId = bucketId;
        for (MediaSet set : mSources) {
            set.addContentListener(this);
        }
        reload();
    }

    @Override
    public boolean isCameraRoll() {
        if (mSources.length == 0) return false;
        for(MediaSet set : mSources) {
            if (!set.isCameraRoll()) return false;
        }
        return true;
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-19,PR1001124 begin
    @Override
    public int getMediaSetType() {
        mMediaSetType = 0;
        for(MediaSet set : mSources) {
            mMediaSetType |= set.getMediaSetType();
        }
        Log.d(TAG, "MediaSetType = " + mMediaSetType);
        return mMediaSetType;
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-19,PR1001124 end

    private void updateData() {
        ArrayList<MediaSet> matches = new ArrayList<MediaSet>();
        int supported = mSources.length == 0 ? 0 : MediaItem.SUPPORT_ALL;
        mFetcher = new FetchCache[mSources.length];
        for (int i = 0, n = mSources.length; i < n; ++i) {
            mFetcher[i] = new FetchCache(mSources[i]);
            supported &= mSources[i].getSupportedOperations();
        }
        mSupportedOperation = supported;
        mIndex.clear();
        mIndex.put(0, new int[mSources.length]);
    }

    private void invalidateCache() {
        for (int i = 0, n = mSources.length; i < n; i++) {
            mFetcher[i].invalidate();
        }
        mIndex.clear();
        mIndex.put(0, new int[mSources.length]);
    }

    @Override
    public Uri getContentUri() {
        String bucketId = String.valueOf(mBucketId);
        if (ApiHelper.HAS_MEDIA_PROVIDER_FILES_TABLE) {
            return MediaStore.Files.getContentUri("external").buildUpon()
                    .appendQueryParameter(LocalSource.KEY_BUCKET_ID, bucketId)
                    .build();
        } else {
            // We don't have a single URL for a merged image before ICS
            // So we used the image's URL as a substitute.
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendQueryParameter(LocalSource.KEY_BUCKET_ID, bucketId)
                    .build();
        }
    }

    @Override
    public String getName() {
        if(mAlbumName != null) return mAlbumName;
        return mSources.length == 0 ? "" : mSources[0].getName();
    }

    @Override
    public int getMediaItemCount() {
        return getTotalMediaItemCount();
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        // First find the nearest mark position <= start.
        SortedMap<Integer, int[]> head = mIndex.headMap(start + 1);
        /* MODIFIED-BEGIN by hao.yin, 2016-03-24,BUG-1859740 */
        if(head == null || head.isEmpty()){
            Log.d(TAG, "=====getMediaItem=====;head is null or empty;start="+start+";count="+count);
               return new ArrayList<MediaItem>();
       }
       /* MODIFIED-END by hao.yin,BUG-1859740 */
        int markPos = head.lastKey();
        int[] subPos = head.get(markPos).clone();
        MediaItem[] slot = new MediaItem[mSources.length];

        int size = mSources.length;

        // fill all slots
        for (int i = 0; i < size; i++) {
            if (mFetcher[i] != null)//[BUGFIX]-Add by TCTNJ,xiangyu.liu, 2016-03-17,PR1839821
                slot[i] = mFetcher[i].getItem(subPos[i]);
        }

        ArrayList<MediaItem> result = new ArrayList<MediaItem>();

        for (int i = markPos; i < start + count; i++) {
            int k = -1;  // k points to the best slot up to now.
            for (int j = 0; j < size; j++) {
                if (slot[j] != null) {
                    if (k == -1 || mComparator.compare(slot[j], slot[k]) < 0) {
                        k = j;
                    }
                }
            }

            // If we don't have anything, all streams are exhausted.
            if (k == -1) break;

            // Pick the best slot and refill it.
            subPos[k]++;
            if (i >= start) {
                //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-01-31,PR904080 begin
                // [BUGFIX]-Add-BEGIN by TCTNJ.ye.chen,04/02/2015,PR922192
                if(slot[k].isDrm == 1 && GalleryActivity.TV_LINK_DRM_HIDE_FLAG){
                    continue;
                }
              //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-01-31,PR904080 begin
                result.add(slot[k]);
            }
            if (mFetcher[k] != null)//[BUGFIX]-Add by TCTNJ,xiangyu.liu, 2016-03-17,PR1839821
                slot[k] = mFetcher[k].getItem(subPos[k]);

            // Periodically leave a mark in the index, so we can come back later.
            if ((i + 1) % PAGE_SIZE == 0) {
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-23,PR956360 begin
                try {
                    mIndex.put(i + 1, subPos.clone());
                } catch (Exception e) {
                    Log.e(TAG, "getMediaItem error:"+e.getMessage());
                    reload();
                }
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-23,PR956360 end
            }
        }

        return result;
    }

    @Override
    public int getTotalMediaItemCount() {
        int count = 0;
        for (MediaSet set : mSources) {
            //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-01-31,PR904080 begin
            /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
            // BUG-FIX For PR1192699 by TCTNJ,kaiyuan.ma begin
            if (null == set)
                continue;
            // BUG-FIX For PR1192699 by TCTNJ,kaiyuan.ma end
            /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
            count += set.getTotalMediaItemCount();
            if(GalleryActivity.TV_LINK_DRM_HIDE_FLAG){
                count -= set.getDrmCount();
            }
            Log.i(TAG, "-----combo++sub--count-"+set.getTotalMediaItemCount());
          //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-01-31,PR904080 begin
        }
        return count;
    }

    @Override
    public long reload() {
        boolean changed = false;
        for (int i = 0, n = mSources.length; i < n; ++i) {
            if (mSources[i].reload() > mDataVersion) changed = true;
        }
        if (changed) {
            mDataVersion = nextVersionNumber();
            updateData();
            invalidateCache();
        }
        return mDataVersion;
    }

    @Override
    public void onContentDirty() {
        notifyContentChanged();
    }

    @Override
    public int getSupportedOperations() {
        return mSupportedOperation;
    }

    @Override
    public void delete() {
        for (MediaSet set : mSources) {
            set.delete();
        }
    }

    @Override
    public void rotate(int degrees) {
        for (MediaSet set : mSources) {
            set.rotate(degrees);
        }
    }

    private static class FetchCache {
        private MediaSet mBaseSet;
        private SoftReference<ArrayList<MediaItem>> mCacheRef;
        private int mStartPos;

        public FetchCache(MediaSet baseSet) {
            mBaseSet = baseSet;
        }

        public void invalidate() {
            mCacheRef = null;
        }

        public MediaItem getItem(int index) {
            boolean needLoading = false;
            ArrayList<MediaItem> cache = null;
            if (mCacheRef == null
                    || index < mStartPos || index >= mStartPos + PAGE_SIZE) {
                needLoading = true;
            } else {
                cache = mCacheRef.get();
                if (cache == null) {
                    needLoading = true;
                }
            }

            if (needLoading) {
                cache = mBaseSet.getMediaItem(index, PAGE_SIZE);
                mCacheRef = new SoftReference<ArrayList<MediaItem>>(cache);
                mStartPos = index;
            }

            if (index < mStartPos || index >= mStartPos + cache.size()) {
                return null;
            }
          //[BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-03-27,PR959260 begin
          //this may caused by threads,the "index - mStartPos"'s value do not match
          //the size of cache..so add try catch.and return null has no influence to the result
            try{
                return cache.get(index - mStartPos);
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "Catched IndexOutOfBoundsException");
                e.printStackTrace();
                return null;
            }
          //[BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-03-27,PR959260 end
        }
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

    public void setAlbumName(String albumName) {
        mAlbumName = albumName;
    }

    public String getAlbumName() {
        return mAlbumName;
    }
}

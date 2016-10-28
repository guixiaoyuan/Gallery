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
/* ----------|----------------------|----------------------|-----------------------------------*/
/* 24/01/2015|    ye.chen           |      PR-905241       | 	[Android5.0][Gallery_v5.1.4.1.0105.0]There in no picture in the locations albums
/* ----------|----------------------|----------------------|-----------------------------------------*/
/* ----------|----------------------|--------------- ------|-------------------*/
/* 09/02/2015|qiang.ding1           |PR927143              |[Gallery]Delete all albums fail according Times group method*/
/* ----------|----------------------|---------------- -----|-------------------*/
/* 26/05/2015|chengbin.du           |PR1008429             |[Android][Gallery_v5.1.13.1.0204.0][REG]The slideshow function disappear in locations */
/*-----------|-------------- -------|----------------------|----------------------------------------*/
package com.tct.gallery3d.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.tct.gallery3d.ui.MenuExecutor;

public class ClusterAlbum extends MediaSet implements ContentListener {
    @SuppressWarnings("unused")
    private static final String TAG = "ClusterAlbum";
    private ArrayList<Path> mPaths = new ArrayList<Path>();
  //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-09,PR927143 begin
    private ArrayList<Path> mDeletePaths = new ArrayList<Path>();
  //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-09,PR927143 end
    private String mName = "";
    private DataManager mDataManager;
    private MediaSet mClusterAlbumSet;
    private List<MediaItem> mCover;

    public ClusterAlbum(Path path, DataManager dataManager,
            MediaSet clusterAlbumSet) {
        super(path, nextVersionNumber());
        mDataManager = dataManager;
        mClusterAlbumSet = clusterAlbumSet;
        mClusterAlbumSet.addContentListener(this);
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-26,PR1008429 begin
    @Override
    public int getMediaSetType() {
        mMediaSetType = 0;
        Iterator<Path> iterator = mPaths.iterator();
        while(iterator.hasNext()) {
            Path path = iterator.next();
            MediaObject mediaItem = (MediaObject)mDataManager.peekMediaObject(path);
            int mediaType = mediaItem.getMediaType();
            if(mediaType == MediaObject.MEDIA_TYPE_IMAGE || mediaType == MediaObject.MEDIA_TYPE_GIF){
                mMediaSetType |= MediaSet.MEDIASET_TYPE_IMAGE;
                break;//this break just for image slidshow menu
            } else if(mediaType == MediaObject.MEDIA_TYPE_VIDEO) {
                mMediaSetType |= MediaSet.MEDIASET_TYPE_VIDEO;
            } else if(mediaType == MediaObject.MEDIA_TYPE_UNKNOWN) {
                mMediaSetType |= MediaSet.MEDIASET_TYPE_UNKNOWN;
            } else if((mMediaSetType & MediaSet.MEDIASET_TYPE_ALL) == MEDIASET_TYPE_ALL) break;
        }
        Log.d(TAG, "MediaSetType = " + mMediaSetType);
        return mMediaSetType;
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-26,PR1008429 end

    public void setCoverMediaItem(List<MediaItem> cover) {
        mCover = cover;
    }
  //[BUGFIX]-Add-END by TCTNJ.ye.chen,24/01/2015,905241,[Android5.0][Gallery_v5.1.4.1.0105.0]There in no picture in the locations albums
    public void nextVersion()
    {
        mDataVersion = nextVersionNumber();
    }
  //[BUGFIX]-Add-END by TCTNJ.ye.chen,24/01/2015,905241,[Android5.0][Gallery_v5.1.4.1.0105.0]There in no picture in the locations albums
    @Override
    public List<MediaItem> getCoverMediaItem() {
        return mCover != null ? mCover : super.getCoverMediaItem();
    }

    void setMediaItems(ArrayList<Path> paths) {
        mPaths = paths;
    }

    ArrayList<Path> getMediaItems() {
        return mPaths;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public int getMediaItemCount() {
        return mPaths.size();
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-09,PR927143 begin
        if(!MenuExecutor.mIsDelete){
            mDeletePaths = mPaths;
        }
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-09,PR927143 end
        return getMediaItemFromPath(mPaths, start, count, mDataManager);
    }

    public static ArrayList<MediaItem> getMediaItemFromPath(
            ArrayList<Path> paths, int start, int count,
            DataManager dataManager) {
        if (start >= paths.size()) {
            return new ArrayList<MediaItem>();
        }
        int end = Math.min(start + count, paths.size());
        ArrayList<Path> subset = new ArrayList<Path>(paths.subList(start, end));
        final MediaItem[] buf = new MediaItem[end - start];
        ItemConsumer consumer = new ItemConsumer() {
            @Override
            public void consume(int index, MediaItem item) {
                buf[index] = item;
            }
        };
        dataManager.mapMediaItems(subset, consumer, 0);
        ArrayList<MediaItem> result = new ArrayList<MediaItem>(end - start);
        for (int i = 0; i < buf.length; i++) {
            result.add(buf[i]);
        }
        return result;
    }

    @Override
    protected int enumerateMediaItems(ItemConsumer consumer, int startIndex) {
        mDataManager.mapMediaItems(mPaths, consumer, startIndex);
        return mPaths.size();
    }

    @Override
    public int getTotalMediaItemCount() {
        return mPaths.size();
    }

    @Override
    public long reload() {
        if (mClusterAlbumSet.reload() > mDataVersion) {
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    @Override
    public void onContentDirty() {
        notifyContentChanged();
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_SHARE | SUPPORT_DELETE | SUPPORT_INFO;
    }

    @Override
    public void delete() {
        ItemConsumer consumer = new ItemConsumer() {
            @Override
            public void consume(int index, MediaItem item) {
                if ((item.getSupportedOperations() & SUPPORT_DELETE) != 0) {
                    item.delete();
                }
            }
        };
      //[BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-02-09,PR927143 begin
        mDataManager.mapMediaItems(mDeletePaths, consumer, 0);
      //[BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-02-09,PR927143 end
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }
}

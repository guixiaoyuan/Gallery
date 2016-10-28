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
/* 26/05/2015|dongliang.feng        |PR1008580             |[Android][Gallery_v5.1.13.1.0204.0][Monitor]There is same albums in locations */
/* ----------|----------------------|----------------------|----------------- */
/* 27/05/2015|ye.chen               |PR1011333             |[Android5.1][Gallery_Global_v5.1.13.1.0204.0]Gallery display black when rotating the picture
/* ----------|----------------------|----------------------|----------------- */
/* 30/06/2015|     su.jiang         |      PR-1018325      |[Android 5.0][Gallery_v5.1.13.1.0206.0][Monitor][Force close] */
/*-----------| ---------------------|----------------------|Gallery will stop when pausing a large number of pictures-----*/
/* 16/12/2015|dongliang.feng        |PR1120444             |[Monkey][Crash][Gallery]Gallery crashes when running monkey test */
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.data;

import android.content.Context;
import android.net.Uri;

import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.ui.MenuExecutor;

import java.util.ArrayList;
import java.util.HashSet;

public class ClusterAlbumSet extends MediaSet implements ContentListener {
    @SuppressWarnings("unused")
    private static final String TAG = "ClusterAlbumSet";
    private GalleryApp mApplication;
    private MediaSet mBaseSet;
    private int mKind;
    private ArrayList<ClusterAlbum> mAlbums = new ArrayList<ClusterAlbum>();
    private boolean mFirstReloadDone;

    public ClusterAlbumSet(Path path, GalleryApp application,
            MediaSet baseSet, int kind) {
        super(path, INVALID_DATA_VERSION);
        mApplication = application;
        mBaseSet = baseSet;
        mKind = kind;
        baseSet.addContentListener(this);
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        return mAlbums.get(index);
    }

    @Override
    public int getSubMediaSetCount() {
        return mAlbums.size();
    }

    @Override
    public String getName() {
        return mBaseSet.getName();
    }

    @Override
    public long reload() {
        if (mBaseSet.reload() > mDataVersion) {
            //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-07,PR922709 begin
            if(!MenuExecutor.mIsDelete){
                updateClusters();
            }
            if (mFirstReloadDone) {
              //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-05-27,PR1011333 begin
                if(!MenuExecutor.mIsRotate){
                    updateClustersContents();
                }
              //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-05-27,PR1011333 begin
            } else {
                mFirstReloadDone = true;
            }
            //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-07,PR922709 end
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    @Override
    public void onContentDirty() {
        notifyContentChanged();
    }

    private void updateClusters() {
        Clustering clustering;
        Context context = mApplication.getAndroidContext();
        switch (mKind) {
            case ClusterSource.CLUSTER_ALBUMSET_TIME:
                clustering = new TimeClustering(context);
                break;
            case ClusterSource.CLUSTER_ALBUMSET_LOCATION:
                clustering = new LocationClustering(context);
                break;
            case ClusterSource.CLUSTER_ALBUMSET_TAG:
                clustering = new TagClustering(context);
                break;
            case ClusterSource.CLUSTER_ALBUMSET_FACE:
                clustering = new FaceClustering(context);
                break;
            default: /* CLUSTER_ALBUMSET_SIZE */
                clustering = new SizeClustering(context);
                break;
        }

        clustering.run(mBaseSet);
        int n = clustering.getNumberOfClusters();
        DataManager dataManager = mApplication.getDataManager();
        mAlbums.clear(); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-05-26, PR1008580
        for (int i = 0; i < n; i++) {
            Path childPath;
            String childName = clustering.getClusterName(i);
            if (mKind == ClusterSource.CLUSTER_ALBUMSET_TAG) {
                childPath = mPath.getChild(Uri.encode(childName));
            } else if (mKind == ClusterSource.CLUSTER_ALBUMSET_SIZE) {
                long minSize = ((SizeClustering) clustering).getMinSize(i);
                childPath = mPath.getChild(minSize);
            } else {
                childPath = mPath.getChild(i);
            }

            ClusterAlbum album;
            synchronized (DataManager.LOCK) {
                album = (ClusterAlbum) dataManager.peekMediaObject(childPath);
                if (album == null) {
                    album = new ClusterAlbum(childPath, dataManager, this);
                }
            }
          //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen,24/01/2015,905241,[Android5.0][Gallery_v5.1.4.1.0105.0]There in no picture in the locations albums
            album.nextVersion();
          //[BUGFIX]-Add-END by TCTNJ.ye.chen,24/01/2015,905241,[Android5.0][Gallery_v5.1.4.1.0105.0]There in no picture in the locations albums
            album.setMediaItems(clustering.getCluster(i));
            album.setName(childName);
//            album.setCoverMediaItem(clustering.getClusterCover(i));
            album.setCoverMediaItem(null);
            mAlbums.add(album);
        }
    }

    private void updateClustersContents() {
        final HashSet<Path> existing = new HashSet<Path>();
        mBaseSet.enumerateTotalMediaItems(new MediaSet.ItemConsumer() {
            @Override
            public void consume(int index, MediaItem item) {
                existing.add(item.getPath());
            }
        });

        int n = mAlbums.size();

        // The loop goes backwards because we may remove empty albums from
        // mAlbums.
        for (int i = n - 1; i >= 0; i--) {
            if (mAlbums.size() <= i) return; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-12-16, PR1120444
            ArrayList<Path> oldPaths = mAlbums.get(i).getMediaItems();
            ArrayList<Path> newPaths = new ArrayList<Path>();
            int m = oldPaths.size();
            for (int j = 0; j < m; j++) {
                Path p = oldPaths.get(j);
                if (existing.contains(p)) {
                    newPaths.add(p);
                }
            }
            //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-30,PR1018325 begin
            if(mAlbums.size()!=0&&mAlbums.get(i)!=null){
                mAlbums.get(i).setMediaItems(newPaths);
            }
            //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-30,PR1018325 end
            if (newPaths.isEmpty()) {
                mAlbums.remove(i);
            }
        }
    }
}

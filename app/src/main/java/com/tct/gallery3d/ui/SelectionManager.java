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
/* 14/01/2015| jian.pan1            | PR900430             |"Select all" is invalid*/
/* ----------|----------------------|----------------------|----------------- */
/* 28/01/2015| jian.pan1            | FR911300             |The count of picture */
/*           |                      |                      |is not true         */
/* ----------|----------------------|----------------------|-------------------- */
/* 26/02/2015| jian.pan1            | FR931363             |The select album function display error when entering time album */
/* ----------|----------------------|----------------------|-------------------- */
/* 27/11/2015|    su.jiang          |  PR-902958           |[Android5.1][Gallery_v5.2.3.1.0312.0][Force Close]Gallery force close when slide screen to select pictures*/
/*-----------|----------------------|----------------------|----------------------------------------------------------------------------------------------------------*/
/* 03/07/2016|    su.jiang          |  PR-1749305          |[CodeSync]Sync GApp Code from Gallery_01 to Gallery_Rel3_03(007-008) 2016-03-07.*/
/*-----------|----------------------|----------------------|--------------------------------------------------------------------------------*/

package com.tct.gallery3d.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.data.Path;

public class SelectionManager {
    private static final String TAG = "SelectionManager";

    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    public static final int SELECT_ALL_MODE = 3;

    private AbstractGalleryActivity mContext;
    private Set<Path> mClickedSet;
    private MediaSet mSourceMediaSet;
    private SelectionListener mListener;
    private DataManager mDataManager;
    private boolean mInverseSelection;
    private boolean mIsAlbumSet;
    private boolean mInSelectionMode;
    private boolean mAutoLeave = true;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 begin
    private boolean mNeedLeaveSectionMode = false;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 end
    private int mTotal;
    private boolean mIsDeleteAll = false;
    public interface SelectionListener {
        public void onSelectionModeChange(int mode);
        public void onSelectionChange(Path path, boolean selected);
    }

    public SelectionManager(AbstractGalleryActivity context, boolean isAlbumSet) {
        mContext = context;
        mDataManager = context.getDataManager();
        mIsAlbumSet = isAlbumSet;
        mClickedSet = new HashSet<Path>();
        mTotal = -1;
    }

    // Whether we will leave selection mode automatically once the number of
    // selected items is down to zero.
    public void setAutoLeaveSelectionMode(boolean enable) {
        mAutoLeave = enable;
    }

    public boolean getAutoLeaveSelectionMode() {
        return mAutoLeave;
    }

    public void setSelectionListener(SelectionListener listener) {
        mListener = listener;
    }

    public void selectAll() {
        mInverseSelection = true;
        mClickedSet.clear();
        mTotal = -1;
        enterSelectionMode();
        if (mListener != null) mListener.onSelectionModeChange(SELECT_ALL_MODE);
    }

    public void deSelectAll() {
        leaveSelectionMode();
        mInverseSelection = false;
        mClickedSet.clear();
    }

    public boolean inSelectAllMode() {
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-14,PR900430 begin
        int selectedCount = getSelectedCount();
        int totalCount = getTotalCount();
        return (selectedCount == totalCount);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-14,PR900430 end
    }

    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 begin
    public boolean needLeaveSectionMode() {
        return mNeedLeaveSectionMode;
    }

    public void setNeedLeaveSectionMode(boolean needLeaveSectionMode) {
        this.mNeedLeaveSectionMode = needLeaveSectionMode;
    }
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 end

    public boolean inSelectionMode() {
        return mInSelectionMode;
    }

    public void enterSelectionMode() {
        mContext.setViewPagerState(false);
        if (mInSelectionMode) return;

        mInSelectionMode = true;
        if (mListener != null) mListener.onSelectionModeChange(ENTER_SELECTION_MODE);
    }

    public void leaveSelectionMode() {
        mContext.setViewPagerState(true);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 begin
        mNeedLeaveSectionMode = false;
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 end
        if (!mInSelectionMode) return;

        mInSelectionMode = false;
        mInverseSelection = false;
        mClickedSet.clear();
        if (mListener != null) mListener.onSelectionModeChange(LEAVE_SELECTION_MODE);
    }

    public boolean isItemSelected(Path itemId) {
        return mInverseSelection ^ mClickedSet.contains(itemId);
    }

    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 begin
    public void refreshTotalCount() {
        mTotal = mIsAlbumSet
                ? mSourceMediaSet.getSubMediaSetCount()
                : mSourceMediaSet.getMediaItemCount();
        Log.i(TAG, "refreshTotalCount mTotal:"+mTotal);
    }
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 end

    private int getTotalCount() {
        if (mSourceMediaSet == null) return -1;

        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-26,PR931363 begin
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-07,PR1749305 end
        //if (mTotal <= 0) {
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-26,PR931363 end
          //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 begin
            refreshTotalCount();
          //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 end
        //}
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-07,PR1749305 end
        return mTotal;
    }

    public int getSelectedCount() {
        int count = mClickedSet.size();
        if (mInverseSelection) {
            count = getTotalCount() - count;
        }
        return count;
    }

    private static List<Path> extendsSelectedList = null;
    public void cacheSelectedList() {
        extendsSelectedList = getSelected(false);
    }
    public void extendsSelected() {
        if(extendsSelectedList == null) return;
        synchronized (mClickedSet) {
            int count = extendsSelectedList.size();
            enterSelectionMode();
            for (int i = 0; i < count; i++) {
                Path path = extendsSelectedList.get(i);
                if(!mClickedSet.contains(path)) {
                    mClickedSet.add(path);
                    if (mListener != null) mListener.onSelectionChange(path, true);
                }
            }
            extendsSelectedList = null;
        }
    }
    public void removeCacheSelected(Path path) {
        if(extendsSelectedList != null && extendsSelectedList.contains(path)) {
            extendsSelectedList.remove(path);
        }
    }

    public void removePath(Path path){
        synchronized (mClickedSet){
            mClickedSet.remove(path);
        }
    }
    public void toggle(Path path) {
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-11-27,PR902958 begin
        synchronized (mClickedSet) {
            if (mClickedSet.contains(path)) {
                mClickedSet.remove(path);
                String id = path.getSuffix();
            } else {
                enterSelectionMode();
                mClickedSet.add(path);
                String id = path.getSuffix();
            }
            // Convert to inverse selection mode if everything is selected.
            int count = getSelectedCount();
//            if (count == getTotalCount()) {
//                selectAll();
//            }

            if (mListener != null) mListener.onSelectionChange(path, isItemSelected(path));
            if (count == 0 && mAutoLeave) {
                leaveSelectionMode();
            }
            if (count == getTotalCount()) {
                mIsDeleteAll = true;
            } else {
                mIsDeleteAll = false;
            }
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-11-27,PR902958 end
    }

    private static boolean expandMediaSet(ArrayList<Path> items, MediaSet set, int maxSelection) {
        int subCount = set.getSubMediaSetCount();
        for (int i = 0; i < subCount; i++) {
            if (!expandMediaSet(items, set.getSubMediaSet(i), maxSelection)) {
                return false;
            }
        }
        int total = set.getMediaItemCount();
        int batch = 50;
        int index = 0;

        while (index < total) {
            int count = index + batch < total
                    ? batch
                    : total - index;
            ArrayList<MediaItem> list = set.getMediaItem(index, count);
            if (list != null
                    && list.size() > (maxSelection - items.size())) {
                return false;
            }
            for (MediaItem item : list) {
                items.add(item.getPath());
            }
            index += batch;
        }
        return true;
    }

    public ArrayList<Path> getSelected(boolean expandSet) {
        return getSelected(expandSet, Integer.MAX_VALUE);
    }

    public ArrayList<Path> getSelected(boolean expandSet, int maxSelection) {
        ArrayList<Path> selected = new ArrayList<Path>();
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-11-27,PR902958 begin
        synchronized (mClickedSet) {
            if (mIsAlbumSet) {
                if (mInverseSelection) {
                    int total = getTotalCount();
                    for (int i = 0; i < total; i++) {
                        MediaSet set = mSourceMediaSet.getSubMediaSet(i);
                        Path id = set.getPath();
                        if (!mClickedSet.contains(id)) {
                            if (expandSet) {
                                if (!expandMediaSet(selected, set, maxSelection)) {
                                    return null;
                                }
                            } else {
                                selected.add(id);
                                if (selected.size() > maxSelection) {
                                    return null;
                                }
                            }
                        }
                    }
                } else {
                    for (Path id : mClickedSet) {
                        if (expandSet) {
                            if (!expandMediaSet(selected, mDataManager.getMediaSet(id),
                                    maxSelection)) {
                                return null;
                            }
                        } else {
                            selected.add(id);
                            if (selected.size() > maxSelection) {
                                return null;
                            }
                        }
                    }
                }
            } else {
                if (mInverseSelection) {
                    int total = getTotalCount();
                    int index = 0;
                    while (index < total) {
                        int count = Math.min(total - index, MediaSet.MEDIAITEM_BATCH_FETCH_COUNT);
                        ArrayList<MediaItem> list = mSourceMediaSet.getMediaItem(index, count);
                        for (MediaItem item : list) {
                            Path id = item.getPath();
                            if (!mClickedSet.contains(id)) {
                                selected.add(id);
                                if (selected.size() > maxSelection) {
                                    return null;
                                }
                            }
                        }
                        index += count;
                    }
                } else {
                    for (Path id : mClickedSet) {
                        selected.add(id);
                        if (selected.size() > maxSelection) {
                            return null;
                        }
                    }
                }
            }
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-11-27,PR902958 end
        return selected;
    }

    public void setSourceMediaSet(MediaSet set) {
        mSourceMediaSet = set;
        mTotal = -1;
    }

    public boolean isDeleteAll() {
        return mIsDeleteAll;
    }
}

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
/* ----------|----------------------|----------------------|----------------------------------- */
/* 13/01/2015|jian.pan1             |PR895965 PR900430     |[Android5.0][Gallery_v5.1.1.0103.0]The
 *           |                      |                      |"deselect all" key display error when
 *           |                      |                      |selecting all pictures
/* ----------|----------------------|----------------------|----------------------------------- */
/* 28/01/2015|jian.pan1             |FR911300              |The count of picture */
/*           |                      |                      |is not true         */
/* ----------|----------------------|----------------------|-------------------- */
/* ----------|----------------------|----------------------|----------------------------------- */
/* 16/03/2015|jian.pan1             |PR949520              |[Android5.0][Gallery_v5.1.9.1.0103.0]The*/
/*           |                      |                      |sharing button is useless when more*/
/*           |                      |                      |than 1000 pictures are choosen*/
/*           |                      |                      |is not true         */
/* ----------|----------------------|----------------------|-------------------- */
/* 08/04/2015|    jialiang.ren      |      PR-956102       |[Android5.0][Gallery_v5.1.9.1.0106.0]The picture can't */
/*                                                         keep selected as albums when tapping back key          */
/* ----------|----------------------|----------------------|-------------------------------------------------------*/
/* 26/05/2015|chengbin.du           |CR1003130             |[5.0][Gallery] Remove 'edit' and 'crop' in overflow menu */
/* ----------|----------------------|----------------------|-------------------------------------------------------*/
/* 28/05/2015 |    jialiang.ren     |      PR-1013262         |[SW][Gallery][FC][ANR]Many pictures in gallery , select all will FC and ANR*/
/*------------|---------------------|-------------------------|---------------------------------------------------------------------------*/
/* 18/06/2015 |    su.jiang         |      PR-1026971         |[Gallery]Failure to share large number of pictures in gallery */
/*------------|---------------------|-------------------------|--------------------------------------------------------------*/
/* 14/07/2015 |    su.jiang         |      PR-1043111      |[Android 5.1][Gallery_v5.1.13.1.0212.0]The share button should be hide when opening the DRM files*/
/*------------|---------------------|----------------------|-------------------------------------------------------------------------------------------------*/
/* 07/15/2015| jian.pan1            | PR1006938            |[5.0][Gallery] camera roll picture repeatative refreshing
/* ----------|----------------------|----------------------|----------------- */
/* 20/07/2015 |    su.jiang     |      PR-1048998   |   [Android 5.0][Gallery_v5.1.13.1.0213.0_polaroid][REG]Can not share album in Gallery*/
/*------------|-----------------|-------------------|--------------------------------------------------------------------------------------*/
/* 22/07/2015 |    su.jiang     |      PR-1050329   |   [Android 5.1][Gallery_v5.1.13.1.0214.0]It not have share icon on download interface*/
/*------------|-----------------|-------------------|--------------------------------------------------------------------------------------*/
/* 06/11/2015 |    su.jiang     |  PR-745655        |[Force Close][Android 5.1][Gallery_v5.2.3.1.1.0307.0]The Gallery pop up Force Close and error interface */
/*------------|-----------------|-------------------|when tapping Share icon and tapping interface continually.----------------------------------------------*/
/* 11/19/2015| jian.pan1            | [ALM]Defect:942618   |[Android6.0][Gallery_v5.2.3.1.0313.0]The select frame colour don't match with GD
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AbstractGalleryFragment;
import com.tct.gallery3d.app.AlbumSelectActivity;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.app.SystemBarTintManager;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.fragment.NewAlbumFragment;
import com.tct.gallery3d.collage.CollageProcessActivity;
import com.tct.gallery3d.common.ApiHelper;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.MediaObject;
import com.tct.gallery3d.data.MediaObject.PanoramaSupportCallback;
import com.tct.gallery3d.data.MediaSource;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.ui.MenuExecutor.ProgressListener;
import com.tct.gallery3d.util.Future;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.ScreenUtils;
import com.tct.gallery3d.util.ThreadPool.JobContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-17,BUG-2208330*/
/* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

public class ActionModeHandler implements Callback, PopupList.OnPopupItemClickListener {

    private static final String TAG = "ActionModeHandler";

    private static final int MAX_SELECTED_ITEMS_FOR_SHARE_INTENT = 300;
    private static final int MAX_SELECTED_ITEMS_FOR_GETMULTICONTENT_INTENT = 300;
    private static final int MAX_SELECTED_ITEMS_FOR_PANORAMA_SHARE_INTENT = 10;

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-09,BUG-2208330*/
    private static final int SUPPORT_MULTIPLE_MASK = MediaObject.SUPPORT_DELETE | MediaObject.SUPPORT_ROTATE
            | MediaObject.SUPPORT_SHARE | MediaObject.SUPPORT_CACHE | MediaObject.SUPPORT_MIX_VIDEO
            | MediaObject.SUPPORT_MOVE | MediaObject.SUPPORT_COPY | MediaObject.SUPPORT_PRIVATE;
            /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    public interface ActionModeListener {
        public boolean onActionItemClicked(MenuItem item);
    }

    private final AbstractGalleryActivity mActivity;
    private final MenuExecutor mMenuExecutor;
    private final SelectionManager mSelectionManager;
    private final NfcAdapter mNfcAdapter;
    private AbstractGalleryFragment mFragment;
    private Menu mMenu;
    private MenuItem mSharePanoramaMenuItem;
    private MenuItem mShareMenuItem;
    private MenuItem mCopyMenuItem;
    private MenuItem mMoveMenuItem;
    private MenuItem mPrivateMenu;
    private MenuItem mCollageMenuItem;
    private MenuItem mSelectAllItem;
    private ShareActionProvider mSharePanoramaActionProvider;
    private ShareActionProvider mShareActionProvider;
//    private SelectionMenu mSelectionMenu;
    private ActionModeListener mListener;
    private Future<?> mMenuTask;
    private final Handler mMainHandler;
    private ActionMode mActionMode;
    private TextView mActionModeTv;
    private Intent mShareIntent;
    private Intent mGetMultiContentIntent;
    private boolean mGetMultiContent;
    private boolean mNewAlbumSelectTag = false;

    private static class GetAllPanoramaSupports implements PanoramaSupportCallback {
        private int mNumInfoRequired;
        private JobContext mJobContext;
        public boolean mAllPanoramas = true;
        public boolean mAllPanorama360 = true;
        public boolean mHasPanorama360 = false;
        private Object mLock = new Object();

        public GetAllPanoramaSupports(ArrayList<MediaObject> mediaObjects, JobContext jc) {
            mJobContext = jc;
            mNumInfoRequired = mediaObjects.size();
            for (MediaObject mediaObject : mediaObjects) {
                mediaObject.getPanoramaSupport(this);
            }
        }

        @Override
        public void panoramaInfoAvailable(MediaObject mediaObject, boolean isPanorama,
                boolean isPanorama360) {
            synchronized (mLock) {
                mNumInfoRequired--;
                mAllPanoramas = isPanorama && mAllPanoramas;
                mAllPanorama360 = isPanorama360 && mAllPanorama360;
                mHasPanorama360 = mHasPanorama360 || isPanorama360;
                if (mNumInfoRequired == 0 || mJobContext.isCancelled()) {
                    mLock.notifyAll();
                }
            }
        }

        public void waitForPanoramaSupport() {
            synchronized (mLock) {
                while (mNumInfoRequired != 0 && !mJobContext.isCancelled()) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        // May be a cancelled job context
                    }
                }
            }
        }
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1026971 begin
    private boolean isNeedShare = true;
    public void isNeedShare(boolean isNeedShare){
        this.isNeedShare = isNeedShare;
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1026971 end
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-21,PR1048998 begin
    private boolean isFromAlbumSetPage = false;
    public void setFromAlbumSetPage(boolean isFromAlbumSetPage) {
        this.isFromAlbumSetPage = isFromAlbumSetPage;
    }

    public void setNewAlbumSelectTag(boolean newAlbumSelectTag) {
        mNewAlbumSelectTag = newAlbumSelectTag;
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-21,PR1048998 end

    public ActionModeHandler(AbstractGalleryActivity activity, SelectionManager selectionManager) {
        mActivity = Utils.checkNotNull(activity);
        mSelectionManager = Utils.checkNotNull(selectionManager);
        mMenuExecutor = new MenuExecutor(activity, selectionManager);
        mMainHandler = new Handler(activity.getMainLooper());
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mActivity.getAndroidContext());
	}

    public ActionModeHandler(AbstractGalleryActivity activity, AbstractGalleryFragment fragment, SelectionManager selectionManager) {
        this(activity, selectionManager);
        mFragment = Utils.checkNotNull(fragment);
    }

    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1006938 begin
    public void setMenuExecutorListener(ProgressListener listener) {
        mMenuExecutor.setDeleteListener(listener);
    }
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1006938 end

    public void startActionMode() {
        Activity a = mActivity;
        mActionMode = a.startActionMode(this);
        View customView = LayoutInflater.from(a).inflate(
                R.layout.action_mode, null);
        mActionMode.setCustomView(customView);
//        mSelectionMenu = new SelectionMenu(a,
//                (Button) customView.findViewById(R.id.selection_menu), this);

        mActivity.setMargin(getActionMode(), true);
        mActionModeTv = (TextView) customView.findViewById(R.id.action_mode_text);
        updateSelectionMenu();

        if (ScreenUtils.splitScreenIsAtBottom(mActivity,mActivity.getToolbar())) {
            mActivity.setStatusEnable(false);
        }else{
            mActivity.setStatusEnable(true);
            mActivity.setStatusColor(SystemBarTintManager.STATUSBAR_COLOR_BLACK);
        }
        if (!isFromAlbumSetPage) {
            ((GalleryActivity) mActivity).removeTabsView();
            mActivity.setViewPagerState(false);
        }
    }

    public View getActionMode() {
        if (mActionMode == null) {
            return null;
        }
        return (View) mActionMode.getCustomView().getParent();
    }

    public void finishActionMode() {
        mActionMode.finish();
        mActivity.resetToolBarPosition();
        if (ScreenUtils.splitScreenIsAtBottom(mActivity,mActivity.getToolbar())) {
            mActivity.setStatusEnable(false);
        }else{
            mActivity.setStatusColor(SystemBarTintManager.DEFAULT_TINT_COLOR);
        }
        mActivity.showToolBarView();
        if (!isFromAlbumSetPage) {
            ((GalleryActivity) mActivity).displayTabsView();
            mActivity.setViewPagerState(true);
        }
    }

    public void setGetMultiContent(boolean getMultiContent) {
        mGetMultiContent = getMultiContent;
    }

    boolean mAllItemIsPrivate = false;

    @TargetApi(Build.VERSION_CODES.M)
    public void setTitle(String title) {
        Log.e(TAG, "title:" + title);
        if ("0".equals(title)) {
            if (!mSelectionManager.getAutoLeaveSelectionMode()) {
                if (mNewAlbumSelectTag) {
                    mActionModeTv.setText(mActivity.getString(R.string.new_album_select_title));
                } else {
                    mActionModeTv.setText(mActivity.getString(R.string.select_item));
                }
            } else  {
                mSelectionManager.leaveSelectionMode();
            }
        } else {
            mActionModeTv.setText(title);
        }
        mActionModeTv.setTextColor(mActivity.getColor(R.color.toolbar_title_select_color));
        updatePrivateItemTitle();
    }

    /**
     * refresh select count when delete pictures from filemanager
     */
    public synchronized void refreshSelectedCount() {
        List<Path> mPathList = mSelectionManager.getSelected(false);
        if (null != mPathList) {
            Iterator iterator = mPathList.iterator();
            while (iterator.hasNext()) {
                Path path = (Path) iterator.next();
                String filepath = getStringFromPath(path);
                File file = new File(filepath);
                if (!file.exists()) {
                    mSelectionManager.removePath(path);
                }
            }
            setTitle(String.valueOf(mSelectionManager.getSelectedCount()));
            if (mSelectionManager.getSelectedCount() == 0) {
                mMenuExecutor.hideDialog();
            }
        }
    }

    private void updatePrivateItemTitle() {
        ArrayList<MediaObject> selectedMediaObjects = GalleryUtils.getMediaObjectsByPath(mSelectionManager.getSelected(false), mActivity);
        if (mPrivateMenu != null && mPrivateMenu.isVisible() && selectedMediaObjects != null) {
            for (MediaObject mediaObject : selectedMediaObjects) {
                if (mediaObject.isPrivate() == GalleryConstant.PUBLIC_ITEM) {
                    mAllItemIsPrivate = false;
                    break;
                } else {
                    mAllItemIsPrivate = true;
                }
            }
            if (mAllItemIsPrivate) {
                mPrivateMenu.setTitle(R.string.set_public);
            } else {
                mPrivateMenu.setTitle(R.string.set_private);
            }
        }
    }

    public void setActionModeListener(ActionModeListener listener) {
        mListener = listener;
    }

    private WakeLockHoldingProgressListener mDeleteProgressListener;
    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-17,BUG-2208330*/
    private WakeLockHoldingProgressListener mMoveProgressListener;
    private WakeLockHoldingProgressListener mCopyProgressListener;
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    public boolean isUpdatingMenu = false;//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-28,PR1013262
    private boolean isTouchShare = false;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-06,PR745655

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        if(isUpdatingMenu) {
            return false;
        }
        // If touch the share icon, other icons could not be touched.
        if (isTouchShare) {
            return false;
        }
        try {
            boolean result;
            // Give listener a chance to process this command before it's routed to
            // ActionModeHandler, which handles command only based on the action id.
            // Sometimes the listener may have more background information to handle
            // an action command.
            if (mListener != null) {
                result = mListener.onActionItemClicked(item);
                if (result) {
                    mSelectionManager.leaveSelectionMode();
                    return result;
                }
            }
            String confirmMsg;
            Bundle data;
            Intent intent;
            int action = item.getItemId();
            switch (action){
                case R.id.action_delete:
                    confirmMsg = mActivity.getResources().getQuantityString(
                            R.plurals.delete_selection, mSelectionManager.getSelectedCount());
                    if (mDeleteProgressListener == null) {
                        mDeleteProgressListener = new WakeLockHoldingProgressListener(mActivity,
                                "Gallery Delete Progress Listener");
                    }
                    mMenuExecutor.onMenuClicked(item, confirmMsg, mDeleteProgressListener, true);
                    break;
                case R.id.action_share:
                    Log.i(TAG, "action_share");
                    isTouchShare = true;
                    if (mShareIntent != null) {
                        launchShareByIntent(mShareIntent);
                        mSelectionManager.setNeedLeaveSectionMode(false);
                    }
                    //Wait 1500 ms to touch other icon.
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isTouchShare = false;
                        }
                    }, 1500);
                    break;
                case R.id.action_multi_getcontent:
                    if (mNewAlbumSelectTag) {
                        ((NewAlbumFragment) mFragment).startCopyOrMove();
                    } else if (mGetMultiContentIntent != null) {
                        mSelectionManager.setNeedLeaveSectionMode(false);
                        mActivity.setResult(Activity.RESULT_OK, mGetMultiContentIntent);
                        mActivity.finish();
                    }
                    break;
                case R.id.action_move:
                    data = new Bundle();
                    data.putString(GalleryConstant.KEY_MEDIA_PATH, MediaSource.LOCAL_SELECT_PATH);
                    data.putString(GalleryConstant.KEY_SET_TITLE,
                            mActivity.getResources().getString(R.string.move_to_album));
                    intent = new Intent(mActivity, AlbumSelectActivity.class);
                    intent.putExtras(data);
                    mActivity.startActivityForResult(intent, GalleryConstant.REQUEST_MOVE);
                    break;
                case R.id.action_copy:
                    data = new Bundle();
                    data.putString(GalleryConstant.KEY_MEDIA_PATH, MediaSource.LOCAL_SELECT_PATH);
                    data.putString(GalleryConstant.KEY_SET_TITLE,
                            mActivity.getResources().getString(R.string.copy_to_album));
                    intent = new Intent(mActivity, AlbumSelectActivity.class);
                    intent.putExtras(data);
                    mActivity.startActivityForResult(intent, GalleryConstant.REQUEST_COPY);
                    break;
                case R.id.action_private:
                    ArrayList<MediaObject> selectedMediaObjects = GalleryUtils.getMediaObjectsByPath(mSelectionManager.getSelected(false), mActivity);
                    ArrayList<String> privateData = new ArrayList<>();

                    for (MediaObject mediaObject : selectedMediaObjects) {
                        privateData.add(mediaObject.getFilePath());
                        mediaObject.setPrivate(!mAllItemIsPrivate ? GalleryConstant.PRIVATE_ITEM : GalleryConstant.PUBLIC_ITEM);
                    }
                    GalleryAppImpl.getTctPrivacyModeHelperInstance(mActivity).setFilePrivateFlag(mActivity.getPackageName(), privateData, !mAllItemIsPrivate);

                    mActivity.getDataManager().notifyPrivateMode();
                    this.finishActionMode();
                    break;
                 case R.id.action_collage:
                    if( mSelectionManager.getSelectedCount()<2){
                        Toast.makeText(mActivity,"At least 2 photos",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if( mSelectionManager.getSelectedCount()>9){
                        Toast.makeText(mActivity,"At Top 9 photos",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    data = new Bundle();
                    intent = new Intent(mActivity, CollageProcessActivity.class);
                    intent.putStringArrayListExtra("photo_path", getPhotoAbsPath());
                    intent.putExtra("piece_size", mSelectionManager.getSelectedCount());

                    mActivity.startActivity(intent);
                    break;
            }
        } finally {
        }
        return true;
    }

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-17,BUG-2208330*/
    public void move() {
        String confirmMsg = mActivity.getResources().getQuantityString(R.plurals.move_selection,
                mSelectionManager.getSelectedCount());
        if (mMoveProgressListener == null) {
            mMoveProgressListener = new WakeLockHoldingProgressListener(mActivity, "Gallery Move Progress Listener");
        }
        mMenuExecutor.onMenuClicked(R.id.action_move, confirmMsg, mMoveProgressListener, true);
    }

    public void copy() {
        String confirmMsg = mActivity.getResources().getQuantityString(R.plurals.copy_selection,
                mSelectionManager.getSelectedCount());
        if (mCopyProgressListener == null) {
            mCopyProgressListener = new WakeLockHoldingProgressListener(mActivity, "Gallery Copy Progress Listener");
        }
        mMenuExecutor.onMenuClicked(R.id.action_copy, confirmMsg, mCopyProgressListener, true);
    }
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    @Override
    public boolean onPopupItemClick(int itemId) {
        try {
            if (itemId == R.id.action_select_all) {
                updateSupportedOperation();
                mMenuExecutor.onMenuClicked(itemId, null, false, true);
            }
            return true;
        } finally {
        }
    }

    private void updateSelectionMenu() {
        // update title
        int count = mSelectionManager.getSelectedCount();
//        String format = mActivity.getResources().getQuantityString(
//                R.plurals.number_of_items_selected, count);
//        setTitle(String.format(format, count));
        setTitle(String.valueOf(count));
        // For clients who call SelectionManager.selectAll() directly, we need to ensure the
        // menu status is consistent with selection manager.
//        mSelectionMenu.updateSelectAllMode(mSelectionManager.inSelectAllMode());

//        updateSelectAllMode(mSelectionManager.inSelectAllMode());
    }

    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR895965 PR900430 begin
    public void updateSelectAllMode(boolean inSelectAllMode) {
        if (mSelectAllItem != null) {
            mSelectAllItem.setTitle(mActivity
                    .getString(inSelectAllMode ? R.string.deselect_all
                            : R.string.select_all));
        }
    }
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-01-13,PR895965 PR900430 end

    private final OnShareTargetSelectedListener mShareTargetSelectedListener =
            new OnShareTargetSelectedListener() {
        @Override
        public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
            mSelectionManager.leaveSelectionMode();
            return false;
        }
    };

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        updatePrivateItemTitle();
        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.operation, menu);
        mMenu = menu;
//        mSharePanoramaMenuItem = menu.findItem(R.id.action_share_panorama);
//        if (mSharePanoramaMenuItem != null) {
//            mSharePanoramaActionProvider = (ShareActionProvider) mSharePanoramaMenuItem
//                .getActionProvider();
//            mSharePanoramaActionProvider.setOnShareTargetSelectedListener(
//                    mShareTargetSelectedListener);
//            mSharePanoramaActionProvider.setShareHistoryFileName("panorama_share_history.xml");
//        }
        mShareMenuItem = menu.findItem(R.id.action_share);

        mMoveMenuItem = menu.findItem(R.id.action_move);
        mCopyMenuItem = menu.findItem(R.id.action_copy);
        mPrivateMenu = menu.findItem(R.id.action_private);
        mCollageMenuItem = menu.findItem(R.id.action_collage);
        if (GalleryAppImpl.getTctPrivacyModeHelperInstance(mActivity).isInPrivacyMode()) {
            mPrivateMenu.setVisible(true);
        } else {
            mPrivateMenu.setVisible(false);
        }
        int count = mSelectionManager.getSelectedCount();
        if (count == 0) {
            mCopyMenuItem.setVisible(false);
            mMoveMenuItem.setVisible(false);
            mCollageMenuItem.setVisible(false);
            mPrivateMenu.setVisible(false);
        } else {
            mCopyMenuItem.setVisible(true);
            mMoveMenuItem.setVisible(true);
            mCollageMenuItem.setVisible(true);
            mPrivateMenu.setVisible(true);
        }

//        if (mShareMenuItem != null) {
//            mShareActionProvider = (ShareActionProvider) mShareMenuItem
//                .getActionProvider();
//            mShareActionProvider.setOnShareTargetSelectedListener(
//                    mShareTargetSelectedListener);
//            mShareActionProvider.setShareHistoryFileName("share_history.xml");
//        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-13,PR895965 PR900430 begin
        mSelectAllItem = menu.findItem(R.id.action_select_all);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-01-13,PR895965 PR900430 end
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mSelectionManager.leaveSelectionMode();
    }

    private ArrayList<MediaObject> getSelectedMediaObjects() {
        ArrayList<Path> unexpandedPaths = mSelectionManager.getSelected(false);
        if (unexpandedPaths.isEmpty()) {
            // This happens when starting selection mode from overflow menu
            // (instead of long press a media object)
            return null;
        }
        ArrayList<MediaObject> selected = new ArrayList<MediaObject>();
        DataManager manager = mActivity.getDataManager();
        for (Path path : unexpandedPaths) {
              //[BUGFIX]-modify by TCTNJ,xinrong.wang, 2016-01-19,PR1440384 begin
               MediaObject mo=manager.getMediaObject(path);
               if(mo!=null)
               {
                      selected.add(mo);
               }
              //selected.add(manager.getMediaObject(path));
              //[BUGFIX]-modify by TCTNJ,xinrong.wang, 2016-01-19,PR1440384 end

        }

        return selected;
    }

    /**
     * get absPath from path
     * @param path
     * @return
     */
    public String getStringFromPath(Path path) {
        DataManager manager = mActivity.getDataManager();
        MediaObject mo = manager.getMediaObject(path);
        if (null != mo) {
            return mo.getFilePath().toString();
        }
        return null;
    }

    public ArrayList<String> getPhotoAbsPath() {
        ArrayList<Path> unexpandedPaths = mSelectionManager.getSelected(false);
        if (unexpandedPaths.isEmpty()) {
            // This happens when starting selection mode from overflow menu
            // (instead of long press a media object)
            return null;
        }
        ArrayList<String> selected = new ArrayList<String>();
        DataManager manager = mActivity.getDataManager();
        for (Path path : unexpandedPaths) {
            //[BUGFIX]-modify by TCTNJ,xinrong.wang, 2016-01-19,PR1440384 begin
            MediaObject mo=manager.getMediaObject(path);
            if(mo!=null)
            {
                selected.add(mo.getFilePath());
            }
            //selected.add(manager.getMediaObject(path));
            //[BUGFIX]-modify by TCTNJ,xinrong.wang, 2016-01-19,PR1440384 end

        }

        return selected;
    }
    // Menu options are determined by selection set itself.
    // We cannot expand it because MenuExecuter executes it based on
    // the selection set instead of the expanded result.
    // e.g. LocalImage can be rotated but collections of them (LocalAlbum) can't.
    private int computeMenuOptions(ArrayList<MediaObject> selected) {
        int operation = MediaObject.SUPPORT_ALL;
        if (mGetMultiContent || mNewAlbumSelectTag) {
            operation &= MediaObject.SUPPORT_GETMULTICONTENT;
            return operation;
        }
        int type = 0;
        boolean hasDrm = false;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-14,PR1043111
        for (MediaObject mediaObject: selected) {
            //[BUGFIX]-Add by TSNJ,zhe.xu, 2016-01-29,PR1533366 begin
            if (mediaObject == null) {
                return 0;
            }
            //[BUGFIX]-Add by TSNJ,zhe.xu, 2016-01-29,PR1533366 end
            int support = mediaObject.getSupportedOperations();
            type |= mediaObject.getMediaType();
            operation &= support;
          //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-22,PR1050329 begin
            if(mediaObject.isDrm() == 1 && !DrmManager.getInstance().isDrmSDFile(mediaObject.getFilePath())) hasDrm = true;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-14,PR1043111
          //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-22,PR1050329 end
        }
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-21,PR1048998 begin
        if(!isFromAlbumSetPage){
            //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-14,PR1043111 begin
            if(hasDrm){
                isNeedShare(false);
            }else{
                isNeedShare(true);
            }
            //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-14,PR1043111 end
        }
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-21,PR1048998 end
        switch (selected.size()) {
            case 1:
                final String mimeType = MenuExecutor.getMimeType(type);
                if (!GalleryUtils.isEditorAvailable(mActivity, mimeType)) {
                    operation &= ~MediaObject.SUPPORT_EDIT;
                }
                //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-26,CR1003130 begin
                if(selected.get(0).getMediaType() == MediaObject.MEDIA_TYPE_IMAGE) {
                    operation &= ~MediaObject.SUPPORT_CROP;
                    operation &= ~MediaObject.SUPPORT_EDIT;
                }
                //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-26,CR1003130 end
                break;
            default:
                operation &= SUPPORT_MULTIPLE_MASK;
        }

        return operation;
    }

    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-26,ALM-1663517 begin
    @SuppressLint("NewApi")
    @TargetApi(ApiHelper.VERSION_CODES.JELLY_BEAN)
    private void setNfcBeamPushUris(Uri[] uris) {
        if (mNfcAdapter != null && ApiHelper.HAS_SET_BEAM_PUSH_URIS && !mActivity.isDestroyed()) {
            mNfcAdapter.setBeamPushUrisCallback(null, mActivity);
            mNfcAdapter.setBeamPushUris(uris, mActivity);
        }
    }
    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-26,ALM-1663517 end

    // Share intent needs to expand the selection set so we can get URI of
    // each media item
    private Intent computePanoramaSharingIntent(JobContext jc, int maxItems) {
        ArrayList<Path> expandedPaths = mSelectionManager.getSelected(true, maxItems);
        if (expandedPaths == null || expandedPaths.size() == 0) {
            return new Intent();
        }
        final ArrayList<Uri> uris = new ArrayList<Uri>();
        DataManager manager = mActivity.getDataManager();
        final Intent intent = new Intent();
        for (Path path : expandedPaths) {
            if (jc.isCancelled()) return null;
            uris.add(manager.getContentUri(path));
        }

        final int size = uris.size();
        if (size > 0) {
            if (size > 1) {
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.setType(GalleryUtils.MIME_TYPE_PANORAMA360);
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            } else {
                intent.setAction(Intent.ACTION_SEND);
                intent.setType(GalleryUtils.MIME_TYPE_PANORAMA360);
                intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        return intent;
    }

    private Intent computeSharingIntent(int maxItems) {
        ArrayList<Path> expandedPaths = mSelectionManager.getSelected(true, maxItems);
        if (expandedPaths == null || expandedPaths.size() == 0) {
            setNfcBeamPushUris(null);
            return new Intent();
        }
        final ArrayList<Uri> uris = new ArrayList<Uri>();
        DataManager manager = mActivity.getDataManager();
        int type = 0;
        final Intent intent = new Intent();
        for (Path path : expandedPaths) {
            int support = manager.getSupportedOperations(path);
            type |= manager.getMediaType(path);

            if ((support & MediaObject.SUPPORT_SHARE) != 0) {
                uris.add(manager.getContentUri(path));
            }
        }

        final int size = uris.size();
        if (size > 0) {
            final String mimeType = MenuExecutor.getMimeType(type);
            if (size > 1) {
                intent.setAction(Intent.ACTION_SEND_MULTIPLE).setType(mimeType);
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            } else {
                intent.setAction(Intent.ACTION_SEND).setType(mimeType);
                intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setNfcBeamPushUris(uris.toArray(new Uri[uris.size()]));
        } else {
            setNfcBeamPushUris(null);
        }

        return intent;
    }

    public Intent computeGetMultiContentIntent(int maxItems) {
        ArrayList<Path> expandedPaths = mSelectionManager.getSelected(true, maxItems);
        if (expandedPaths == null || expandedPaths.size() == 0) {
            return new Intent();
        }
        final ArrayList<Uri> uris = new ArrayList<Uri>();
        DataManager manager = mActivity.getDataManager();
        int type = 0;
        final Intent intent = new Intent();
        for (Path path : expandedPaths) {
            type |= manager.getMediaType(path);
            uris.add(manager.getContentUri(path));
        }

        final int size = uris.size();
        if (size > 0) {
            final String mimeType = MenuExecutor.getMimeType(type);
            if (size > 1) {
                intent.setType(mimeType);
                ClipData clip = ClipData.newPlainText("URI", "");
                for (int i = 0; i < uris.size(); i++) {
                    ClipData.Item item = new ClipData.Item(uris.get(i));
                    clip.addItem(item);
                }
                intent.setClipData(clip);
            } else {
                intent.setType(mimeType);
                intent.setData(uris.get(0));
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        return intent;
    }

    public void updateSupportedOperation(Path path, boolean selected) {
        // TODO: We need to improve the performance
        updateSupportedOperation();
    }

    public void updateSupportedOperation() {
        // Interrupt previous unfinished task, mMenuTask is only accessed in main thread
        if (mMenuTask != null) mMenuTask.cancel();

        updateSelectionMenu();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<MediaObject> selected = GalleryUtils.getMediaObjectsByPath(mSelectionManager.getSelected(false), mActivity);
                if (selected == null) {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mMenuTask = null;
                            MenuExecutor.updateMenuOperation(mMenu, 0, false, GalleryAppImpl.getTctPrivacyModeHelperInstance(mActivity).isInPrivacyMode());
                        }
                    });
                    return;
                }
                final int operation = computeMenuOptions(selected);
                int numSelected = selected.size();
                final boolean canShare =
                        numSelected < MAX_SELECTED_ITEMS_FOR_SHARE_INTENT;
                final boolean canGetMultiContent =
                        numSelected < MAX_SELECTED_ITEMS_FOR_GETMULTICONTENT_INTENT;

                mShareIntent = canShare ? computeSharingIntent(MAX_SELECTED_ITEMS_FOR_SHARE_INTENT) : new Intent();
                mGetMultiContentIntent = canGetMultiContent ? computeGetMultiContentIntent(MAX_SELECTED_ITEMS_FOR_GETMULTICONTENT_INTENT) : new Intent();

                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMenuTask = null;
                        MenuExecutor.updateMenuOperation(mMenu, operation, false, GalleryAppImpl.getTctPrivacyModeHelperInstance(mActivity).isInPrivacyMode());
                        if (mShareMenuItem.isVisible() && mShareMenuItem != null && !mGetMultiContent) {
                            mShareMenuItem.setEnabled(canShare);
                            if(!isNeedShare){
                                mShareMenuItem.setVisible(canShare&&isNeedShare);
                            }else {
                                mShareMenuItem.setVisible(canShare);
                            }
                        }
                        Log.e(TAG, "## finish updating menu.");
                        isUpdatingMenu = false;
                    }
                });
            }
        }).start();
    }

    /**
     * lunch to photo share
     */
    public void launchShareByIntent(Intent intent) {
        try {
            mActivity.startActivity(Intent.createChooser(intent, mActivity
                    .getResources().getString(R.string.photo_share)));
        } catch (android.content.ActivityNotFoundException e) {
            Log.e(TAG, "Cannot find any activity", e);
        }
    }

    public void pause() {
        if (mMenuTask != null) {
            mMenuTask.cancel();
            mMenuTask = null;
        }
        mMenuExecutor.pause();
    }

    public void destroy() {
        mMenuExecutor.destroy();
    }

    public void resume() {
        if (mSelectionManager.inSelectionMode()) updateSupportedOperation();
        mMenuExecutor.resume();
    }
}

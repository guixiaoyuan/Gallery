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
/* 20/11/2014|chengqun.sun            |FR826631              |Multi screen interaction*/
/* ----------|----------------------|----------------------|----------------- */
/* 28/01/2015|jian.pan1             |FR911300              |The count of picture */
/*           |                      |                      |is not true         */
/* ----------|----------------------|----------------------|-------------------- */
/* ----------|----------------------|---------------------|-------------------*/
/* 13/02/2015|qiang.ding1           |PR926021             |[Android 5.0][Gallery_v5.1.4.1.0111.0]
 *           |                      |                     |[REG][UI]Deleting should be delete*/
/* ----------|----------------------|---------------------|-------------------*/
/* 13/02/2015|chengbin.du           |PR931927              |[FileManager]Can't update the crop picture timely from Category */
/* ----------|----------------------|----------------------|----------------- */
/* 23/03/2015|dongliang.feng        |PR956418              |[Android5.0][Gallery_v5.1.9.1.0107.0][REG]There are double messages in share list */
/* ----------|----------------------|----------------------|----------------- */
/* 08/04/2015|    jialiang.ren     |      PR-956102       |[Android5.0][Gallery_v5.1.9.1.0106.0]The picture can't */
/*                                                         keep selected as albums when tapping back key          */
/* ----------|---------------------|----------------------|-------------------------------------------------------*/
/* 27/05/2015|ye.chen               |PR1011333             |[Android5.1][Gallery_Global_v5.1.13.1.0204.0]Gallery display black when rotating the picture
/* ----------|----------------------|----------------------|----------------- */
/* 07/15/2015| jian.pan1            | PR1006938            |[5.0][Gallery] camera roll picture repeatative refreshing
/* ----------|----------------------|----------------------|----------------- */
/* 16/10/2015|    su.jiang          |  PR-677868           |[Android5.1][Gallery_v5.2.2.1.1.0305.0]The menu list display error on video interface*/
/*-----------|----------------------|----------------------|-------------------------------------------------------------------------------------*/
package com.tct.gallery3d.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.print.PrintHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaObject;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.filtershow.crop.CropActivity;
import com.tct.gallery3d.util.Future;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.ThreadPool.Job;
import com.tct.gallery3d.util.ThreadPool.JobContext;

import java.util.ArrayList;

public class MenuExecutor {
    private static final String TAG = "MenuExecutor";

    private static final int MSG_TASK_COMPLETE = 1;
    private static final int MSG_TASK_UPDATE = 2;
    private static final int MSG_TASK_START = 3;
    private static final int MSG_DO_SHARE = 4;

    public static final int EXECUTION_RESULT_SUCCESS = 1;
    public static final int EXECUTION_RESULT_FAIL = 2;
    public static final int EXECUTION_RESULT_CANCEL = 3;
    public static boolean mIsDelete = false;//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-07,PR922709
    public static boolean mIsRotate = false;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-05-27,PR1011333 begin
    private ProgressDialog mDialog;
    private Future<?> mTask;
    // wait the operation to finish when we want to stop it.
    private boolean mWaitOnStop;
    private boolean mPaused;
    private String mCopyPath; // MODIFIED by Yaoyu.Yang, 2016-08-17,BUG-2208330
    /* dingqiang  2014-11-12 add for bug 833895 begin */
    private Handler mDelayHandler = new Handler( );
    /* dingqiang  2014-11-12 add for bug 833895 end */

    private final AbstractGalleryActivity mActivity;
    private final SelectionManager mSelectionManager;
    private final Handler mHandler;
    private AlertDialog mAlertdialog;
    private AlertDialog.Builder mBuilder;

    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1006938 begin
    private ProgressListener mProgressListener;

    private boolean mIsCopyOrMove = false;
    private ArrayList<String> mNeedSetPrivateList = new ArrayList<>();

    private static int sOperation;
    private static boolean mCancelCopyOrMove = false;
    private static Path mCopyOrMovePath = null;

    public void setDeleteListener(ProgressListener listener) {
        mProgressListener = listener;
    }
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1006938 end

    public static ProgressDialog createProgressDialog( //MODIFIED by jian.pan1, 2016-04-05,BUG-1892017
            Context context, int titleId, int progressMax) {
        ProgressDialog dialog = new ProgressDialog(context);
        if (sOperation == R.id.action_copy || sOperation == R.id.action_move) {
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int i) {
                    MediaObject mediaObject = mCopyOrMovePath.getObject();
                    if (mediaObject != null) {
                        mediaObject.setCancelStatus(true);
                    }
                    mCancelCopyOrMove = true;
                    if (d != null) {
                        d.dismiss();
                    }
                }
            });
            dialog.setProgressNumberFormat(null);
            dialog.setProgressPercentFormat(null);
        }
        dialog.setTitle(titleId);
        dialog.setMax(progressMax);
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        if (progressMax > 1) {
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        return dialog;
    }

    public interface ProgressListener {
        public void onConfirmDialogShown();
        public void onConfirmDialogDismissed(boolean confirmed);
        public void onProgressStart();
        public void onProgressUpdate(int index);
        public void onProgressComplete(int result);
    }

    public MenuExecutor(
            AbstractGalleryActivity activity, SelectionManager selectionManager) {
        mActivity = Utils.checkNotNull(activity);
        mSelectionManager = Utils.checkNotNull(selectionManager);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_TASK_START: {
                        Log.d(TAG, "task start-----------");
                        mActivity.sendBroadcast(new Intent(GalleryUtils.INTENT_OPERATION_START));
                        if (message.obj != null) {
                            ProgressListener listener = (ProgressListener) message.obj;
                            listener.onProgressStart();
                            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1006938 begin
                            if (mProgressListener != null) {
                                mProgressListener.onProgressStart();
                            }
                            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1006938 end
                        }
                        break;
                    }
                    case MSG_TASK_COMPLETE: {
                        mActivity.sendBroadcast(new Intent(GalleryUtils.INTENT_OPERATION_FINISH));
                        Log.d(TAG, "task finish-----------");
                        stopTaskAndDismissDialog();
                        if (message.obj != null) {
                            ProgressListener listener = (ProgressListener) message.obj;
                            listener.onProgressComplete(message.arg1);
                            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1006938 begin
                            if (mProgressListener != null) {
                                mProgressListener.onProgressComplete(message.arg1);
                            }
                            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1006938 end
                        }
                        /* dingqiang  2014-11-12 add for bug 833895 begin */
                        mDelayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                 mSelectionManager.leaveSelectionMode();
                            }
                        }, 150);
                        /* dingqiang  2014-11-12 add for bug 833895 end */
                        break;
                    }
                    case MSG_TASK_UPDATE: {
                        if (mDialog != null && !mPaused) mDialog.setProgress(message.arg1);
                        if (message.obj != null) {
                            ProgressListener listener = (ProgressListener) message.obj;
                            listener.onProgressUpdate(message.arg1);
                            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1006938 begin
                            if (mProgressListener != null) {
                                mProgressListener.onProgressUpdate(message.arg1);
                            }
                            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1006938 end
                        }
                        break;
                    }
                    case MSG_DO_SHARE: {
                        mActivity.startActivity((Intent) message.obj);
                        break;
                    }
                }
            }
        };
    }

    private void stopTaskAndDismissDialog() {
        if (mTask != null) {
            if (!mWaitOnStop) mTask.cancel();
            if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
            mDialog = null;
            mTask = null;
        }
    }

    public void resume() {
        mPaused = false;
        if (mDialog != null) mDialog.show();
    }

    public void pause() {
        mPaused = true;
        if (mDialog != null && mDialog.isShowing()) mDialog.hide();
    }

    public void destroy() {
        stopTaskAndDismissDialog();
    }

    private void onProgressUpdate(int index, ProgressListener listener) {
        mHandler.sendMessage(
                mHandler.obtainMessage(MSG_TASK_UPDATE, index, 0, listener));
    }

    private void onProgressStart(ProgressListener listener) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_TASK_START, listener));
    }

    private void onProgressComplete(int result, ProgressListener listener) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_TASK_COMPLETE, result, 0, listener));
    }

    public static void updateMenuOperation(Menu menu, int supported, boolean isCameraReview, boolean isPrivateMode) {
        Log.d(TAG, " isPrivateMode = " + isPrivateMode);
        boolean supportDelete = (supported & MediaObject.SUPPORT_DELETE) != 0;
        //boolean supportRotate = (supported & MediaObject.SUPPORT_ROTATE) != 0;
        boolean supportCrop = (supported & MediaObject.SUPPORT_CROP) != 0;
        boolean supportShare = (supported & MediaObject.SUPPORT_SHARE) != 0;
        boolean supportSetAs = (supported & MediaObject.SUPPORT_SETAS) != 0;
        boolean supportShowOnMap = (supported & MediaObject.SUPPORT_SHOW_ON_MAP) != 0;
        boolean supportEdit = (supported & MediaObject.SUPPORT_EDIT) != 0;
        boolean supportInfo = (supported & MediaObject.SUPPORT_INFO) != 0;
        boolean supportPrint = (supported & MediaObject.SUPPORT_PRINT) != 0;
        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-09,BUG-2208330*/
        boolean supportMove = (supported & MediaObject.SUPPORT_MOVE) != 0;
        boolean supportCopy = (supported & MediaObject.SUPPORT_COPY) != 0;
        boolean supportPrivate = (supported & MediaObject.SUPPORT_PRIVATE) != 0;
        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
        setMenuItemVisible(menu, R.id.action_dlna, supportDelete); //[FEATURE]-by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        supportPrint &= PrintHelper.systemSupportsPrint();
        boolean supportMixVideo = (supported & MediaObject.SUPPORT_MIX_VIDEO) != 0;
        boolean supportGetMultiContent = (supported & MediaObject.SUPPORT_GETMULTICONTENT) != 0;

        setMenuItemVisible(menu, R.id.action_delete, isCameraReview ? true : supportDelete);
        //setMenuItemVisible(menu, R.id.action_rotate_ccw, supportRotate && !isCameraReview);
        //setMenuItemVisible(menu, R.id.action_rotate_cw, supportRotate && !isCameraReview);
        setMenuItemVisible(menu, R.id.action_select_all, true && !isCameraReview);

        setMenuItemVisible(menu, R.id.action_crop, supportCrop && !isCameraReview);
        // Hide panorama until call to updateMenuForPanorama corrects it
        setMenuItemVisible(menu, R.id.action_share_panorama, false);
        setMenuItemVisible(menu, R.id.action_share, supportShare && !isCameraReview);
        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-09,BUG-2208330*/
        setMenuItemVisible(menu, R.id.action_move, supportMove && !isCameraReview);
        setMenuItemVisible(menu, R.id.action_copy, supportCopy && !isCameraReview);
        setMenuItemVisible(menu, R.id.action_private, supportPrivate && isPrivateMode);
        // add by liuxiaoyu
        setMenuItemVisible(menu, R.id.action_create_collage, false);
        setMenuItemVisible(menu, R.id.action_collage, false);
        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
        setMenuItemVisible(menu, R.id.action_setas, supportSetAs && !isCameraReview);
        setMenuItemVisible(menu, R.id.action_show_on_map, supportShowOnMap && !isCameraReview);
        setMenuItemVisible(menu, R.id.action_edit, supportEdit && !isCameraReview);
        // setMenuItemVisible(menu, R.id.action_simple_edit, supportEdit);
        setMenuItemVisible(menu, R.id.action_details, supportInfo && !isCameraReview);
        setMenuItemVisible(menu, R.id.print, supportPrint && !isCameraReview);
        setMenuItemVisible(menu, R.id.action_mix_video, supportMixVideo && !isCameraReview);
        setMenuItemVisible(menu, R.id.action_multi_getcontent, supportGetMultiContent && !isCameraReview);
    }

    public static void updateMenuForPanorama(Menu menu, boolean shareAsPanorama360,
            boolean disablePanorama360Options) {
        setMenuItemVisible(menu, R.id.action_share_panorama, shareAsPanorama360);
        if (disablePanorama360Options) {
//            setMenuItemVisible(menu, R.id.action_rotate_ccw, false);
//            setMenuItemVisible(menu, R.id.action_rotate_cw, false);
        }
    }

    private static void setMenuItemVisible(Menu menu, int itemId, boolean visible) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) item.setVisible(visible);
    }

    private Path getSingleSelectedPath() {
        ArrayList<Path> ids = mSelectionManager.getSelected(true);
        Utils.assertTrue(ids.size() == 1);
        return ids.get(0);
    }

    private Intent getIntentBySingleSelectedPath(String action) {
        DataManager manager = mActivity.getDataManager();
        Path path = getSingleSelectedPath();
        String mimeType = getMimeType(manager.getMediaType(path));
        return new Intent(action).setDataAndType(manager.getContentUri(path), mimeType);
    }

     /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1892017*/
    private void onMenuClicked(int action, ProgressListener listener, boolean showDialog) {
        onMenuClicked(action, listener, false, showDialog);
         /*MODIFIED-END by jian.pan1,BUG-1892017*/
    }

    public void onMenuClicked(int action, ProgressListener listener,
            boolean waitOnStop, boolean showDialog) {
        int title;
        sOperation = action;
        switch (action) {
            case R.id.action_select_all:
                if (mSelectionManager.inSelectAllMode()) {
                    mSelectionManager.deSelectAll();
                } else {
                    mSelectionManager.selectAll();
                }
                return;
            case R.id.action_crop: {
                Intent intent = getIntentBySingleSelectedPath(CropActivity.CROP_ACTION);
                mActivity.startActivity(intent);
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 begin
                mSelectionManager.setNeedLeaveSectionMode(true);
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 end
                return;
            }
            case R.id.action_edit: {
                Intent intent = getIntentBySingleSelectedPath(Intent.ACTION_EDIT)
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mActivity.startActivity(Intent.createChooser(intent, null));
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 begin
                mSelectionManager.setNeedLeaveSectionMode(false);//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-08,PR956102
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 end
                return;
            }
            case R.id.action_setas: {
                Intent intent = getIntentBySingleSelectedPath(Intent.ACTION_ATTACH_DATA)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra("mimeType", intent.getType());
                Activity activity = mActivity;
                activity.startActivity(Intent.createChooser(
                        intent, activity.getString(R.string.set_as)));
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 begin
                mSelectionManager.setNeedLeaveSectionMode(false);//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-08,PR956102
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-28,PR911300 end
                return;
            }
          //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-13,PR926021  begin
            case R.id.action_delete:
                title = R.string.deleting;
                break;
            /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-17,BUG-2208330*/
            case R.id.action_copy:
                title = R.string.copying;
                break;
            case R.id.action_move:
                title = R.string.moving;
                break;
                /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
          //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-13,PR926021  end
            /*case R.id.action_rotate_cw:
                title = R.string.rotate_right;
                break;
            case R.id.action_rotate_ccw:
                title = R.string.rotate_left;
                break;*/
            case R.id.action_show_on_map:
                title = R.string.show_on_map;
                break;
            default:
                return;
        }
        startAction(action, title, listener, waitOnStop, showDialog);
    }

    private class ConfirmDialogListener implements OnClickListener, OnCancelListener {
        private final int mActionId;
        private final ProgressListener mListener;
         /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1892017*/
        private boolean mShowDialog = false;

        public ConfirmDialogListener(int actionId, ProgressListener listener, boolean showDialog) {
            mActionId = actionId;
            mListener = listener;
            mShowDialog = showDialog;
             /*MODIFIED-END by jian.pan1,BUG-1892017*/
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                if (mListener != null) {
                    mListener.onConfirmDialogDismissed(true);
                }
                onMenuClicked(mActionId, mListener, mShowDialog); //MODIFIED by jian.pan1, 2016-04-05,BUG-1892017
            } else {
                if (mListener != null) {
                    mListener.onConfirmDialogDismissed(false);
                }
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            if (mListener != null) {
                mListener.onConfirmDialogDismissed(false);
            }
        }
    }

    /**
     * hide dialog when update selected counts from filemanager
     */
    public void hideDialog() {
        if (null != mAlertdialog) {
            mAlertdialog.dismiss();
        }
    }

    public void onMenuClicked(MenuItem menuItem, String confirmMsg,
             /*MODIFIED-BEGIN by jian.pan1, 2016-04-05,BUG-1892017*/
            final ProgressListener listener, boolean showDialog) {
        final int action = menuItem.getItemId();
        onMenuClicked(action, confirmMsg, listener, showDialog);
    }

    public void onMenuClicked(int actionId, String confirmMsg,
                              final ProgressListener listener, boolean showDialog) {
        if (confirmMsg != null) {
            if (listener != null) listener.onConfirmDialogShown();
            ConfirmDialogListener cdl = new ConfirmDialogListener(actionId, listener, showDialog);
            if (mAlertdialog != null) {
                mAlertdialog.cancel();
            }
            mBuilder = new AlertDialog.Builder(mActivity.getAndroidContext());

            switch (actionId) {
                case R.id.action_delete:
                    mBuilder.setMessage(confirmMsg).setOnCancelListener(cdl);
                    mBuilder.setPositiveButton(R.string.delete, cdl)
                            .setNegativeButton(R.string.cancel, cdl);
                    break;
                case R.id.action_move:
                    mBuilder.setMessage(confirmMsg).setOnCancelListener(cdl);
                    mBuilder.setPositiveButton(R.string.move, cdl)
                            .setNegativeButton(R.string.cancel, cdl);
                    break;
                case R.id.action_copy:
                    mBuilder.setMessage(confirmMsg).setOnCancelListener(cdl);
                    mBuilder.setPositiveButton(R.string.copy, cdl)
                            .setNegativeButton(R.string.cancel, cdl);
                    break;

                default:
                    break;
            }
            mAlertdialog = mBuilder.create();
            mAlertdialog.show();

        } else {
            onMenuClicked(actionId, listener, showDialog);
        }
    }

    public void startAction(int action, int title, ProgressListener listener) {
        startAction(action, title, listener, false, true);
    }

    public void startAction(int action, int title, ProgressListener listener,
            boolean waitOnStop, boolean showDialog) {
        ArrayList<Path> ids = mSelectionManager.getSelected(false);
        stopTaskAndDismissDialog();

        Activity activity = mActivity;
        if (showDialog) {
            mDialog = createProgressDialog(activity, title, ids.size());
            mDialog.show();
        } else {
            mDialog = null;
        }
        MediaOperation operation = new MediaOperation(action, ids, listener);
        mTask = mActivity.getBatchServiceThreadPoolIfAvailable().submit(operation, null);
        mWaitOnStop = waitOnStop;
    }

    public void startSingleItemAction(int action, Path targetPath) {
        ArrayList<Path> ids = new ArrayList<Path>(1);
        ids.add(targetPath);
        mDialog = null;
        MediaOperation operation = new MediaOperation(action, ids, null);
        mTask = mActivity.getBatchServiceThreadPoolIfAvailable().submit(operation, null);
        mWaitOnStop = false;
    }

    public static String getMimeType(int type) {
        switch (type) {
            case MediaObject.MEDIA_TYPE_IMAGE :
                return GalleryUtils.MIME_TYPE_IMAGE;
            case MediaObject.MEDIA_TYPE_VIDEO :
                return GalleryUtils.MIME_TYPE_VIDEO;
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956418 begin
            case MediaObject.MEDIA_TYPE_GIF :
                return GalleryUtils.MIME_TYPE_GIF;
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956418 end
            default: return GalleryUtils.MIME_TYPE_ALL;
        }
    }

    private boolean execute(
            DataManager manager, JobContext jc, int cmd, Path path) {
        boolean result = true;
        Log.v(TAG, "Execute cmd: " + cmd + " for " + path);
        long startTime = System.currentTimeMillis();

        switch (cmd) {
            case R.id.action_delete:
                manager.delete(path);
                mSelectionManager.removeCacheSelected(path);
                break;
            /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-17,BUG-2208330*/
            case R.id.action_copy:
                mCopyPath = manager.copy(path);
                break;
            case R.id.action_move:
                mCopyPath = manager.move(path);
                break;
                /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
            /*case R.id.action_rotate_cw:
                mIsRotate = true;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-05-27,PR1011333 begin
                manager.rotate(path, 90);
                break;
            case R.id.action_rotate_ccw:
                mIsRotate = true;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-05-27,PR1011333 begin
                manager.rotate(path, -90);
                break;*/
            case R.id.action_toggle_full_caching: {
                MediaObject obj = manager.getMediaObject(path);
                int cacheFlag = obj.getCacheFlag();
                if (cacheFlag == MediaObject.CACHE_FLAG_FULL) {
                    cacheFlag = MediaObject.CACHE_FLAG_SCREENNAIL;
                } else {
                    cacheFlag = MediaObject.CACHE_FLAG_FULL;
                }
                obj.cache(cacheFlag);
                break;
            }
            case R.id.action_show_on_map: {
                MediaItem item = (MediaItem) manager.getMediaObject(path);
                double latlng[] = new double[2];
                item.getLatLong(latlng);
                if (GalleryUtils.isValidLocation(latlng[0], latlng[1])) {
                    GalleryUtils.showOnMap(mActivity, latlng[0], latlng[1]);
                }
                break;
            }
            default:
                throw new AssertionError();
        }
        Log.v(TAG, "It takes " + (System.currentTimeMillis() - startTime) +
                " ms to execute cmd for " + path);
        return result;
    }

    private class MediaOperation implements Job<Void> {
        private final ArrayList<Path> mItems;
        private final int mOperation;
        private final ProgressListener mListener;
        int index = 0;
        private UpdateScannerClient mUpdateScannerClient;

        public MediaOperation(int operation, ArrayList<Path> items,
                ProgressListener listener) {
            mOperation = operation;
            sOperation = operation;
            mItems = items;
            mListener = listener;
            mUpdateScannerClient = new UpdateScannerClient(mActivity);
        }

        private class UpdateScannerClient implements MediaScannerConnectionClient {
            MediaScannerConnection mScannerConnection;
            String mPath = null;

            public UpdateScannerClient(Context context) {
                mScannerConnection = new MediaScannerConnection(context, this);
                mScannerConnection.connect();
            }

            @Override
            public void onMediaScannerConnected() {
                Log.d(TAG, "scan ---- start");
                if (null != mPath) {
                    mScannerConnection.scanFile(mPath, null);
                    mPath = null;
                }
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.d(TAG, "index===== " + index + " item.size----" + mItems.size());
                onProgressUpdate(++index, mListener);
                if (index == mItems.size()) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    onProgressComplete(EXECUTION_RESULT_SUCCESS, mListener);
                    mScannerConnection.disconnect();
                    setMediaObjectPrivateIfNecessary();
                }
            }

            private synchronized void scanFile(String path) {
                Log.d(TAG, "path--------------" + path);
                if (mScannerConnection.isConnected() && null != path) {
                    mScannerConnection.scanFile(path, null);
                } else if (!mScannerConnection.isConnected() && null != path) {
                    mPath = path;
                    mScannerConnection.connect();
                    //we should make sure the mScannerConnection is connected before to scan the next file
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    onProgressComplete(EXECUTION_RESULT_FAIL, mListener);
                    mScannerConnection.disconnect();
                }
            }
        }

        @Override
        public Void run(JobContext jc) {
            index = 0;
//            String[] copyPathBuffer = new String[1000]; // MODIFIED by Yaoyu.Yang, 2016-08-17,BUG-2208330
//            List<String> paths = new ArrayList<String>();
//            paths.clear();
            DataManager manager = mActivity.getDataManager();
            int result = EXECUTION_RESULT_SUCCESS;
            boolean isScan = false;
            try {
                onProgressStart(mListener);
                ArrayList<MediaObject> selected = GalleryUtils.getMediaObjectsByPath(mItems, mActivity);
                mIsCopyOrMove = (mOperation == R.id.action_copy || mOperation == R.id.action_move);
                for (Path id : mItems) {
                    if (jc.isCancelled()) {
                        result = EXECUTION_RESULT_CANCEL;
                        break;
                    }
                    mIsDelete = true;//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-07,PR922709 begin
                    mIsRotate = false;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-05-27,PR1011333 begin
                    mCopyOrMovePath = id;
                    if (!execute(manager, jc, mOperation, id)) {
                        result = EXECUTION_RESULT_FAIL;
                    }

                    if (mIsCopyOrMove) {
                        for (MediaObject mediaObject : selected) {
                            if (mediaObject != null && id == mediaObject.getPath() && mediaObject.isPrivate() == GalleryConstant.PRIVATE_ITEM && !mediaObject.getCancelStatus()) {
                                mNeedSetPrivateList.add(mCopyPath);
                            }
                            if (mediaObject.getCancelStatus()) {
                                mediaObject.setCancelStatus(false);
                            }
                        }
                    }

                    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-17,BUG-2208330*/
//                    copyPathBuffer[index] = mCopyPath;
//                    paths.add(mCopyPath);
//                    onProgressUpdate(index++, mListener);
                    if (mIsCopyOrMove) {
//                    MediaScannerConnection.scanFile(mActivity, copyPathBuffer, null, null);
                        isScan = true;
                        mUpdateScannerClient.scanFile(mCopyPath);
                    } else {
                        onProgressUpdate(++index, mListener);
                        if (index == mItems.size()) {
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (mCancelCopyOrMove) {
                        isScan = false;
                        mCancelCopyOrMove = false;
                        break;
                    }
                }

//                if (mOperation == R.id.action_copy || mOperation == R.id.action_move) {
////                    MediaScannerConnection.scanFile(mActivity, copyPathBuffer, null, null);
//                    isScan = true;
//                    new UpdateScannerClient(mActivity, paths);
//                }
                /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
                //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-02-13,PR931927 begin
                if (mItems.size() == 1 && mOperation == R.id.action_delete) {
                    Log.i(TAG, "delete sendBroadcast UpdateFileManager");
                    Intent broadiIntent = new Intent("UpdateFileManager");
                    mActivity.sendBroadcast(broadiIntent);
                }
                //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-02-13,PR931927 end
            } catch (Throwable th) {
                Log.e(TAG, "failed to execute operation " + mOperation
                        + " : " + th);
            } finally {
                if(!isScan){
                    onProgressComplete(result, mListener);
                }
            }
            return null;
        }

        private void setMediaObjectPrivateIfNecessary() {
            if (mIsCopyOrMove && !mNeedSetPrivateList.isEmpty()) {
                GalleryAppImpl.getTctPrivacyModeHelperInstance(mActivity).setFilePrivateFlag(mActivity.getPackageName(), mNeedSetPrivateList, true);
                mNeedSetPrivateList.clear();
                mIsCopyOrMove = false;
            }
        }

    }
}

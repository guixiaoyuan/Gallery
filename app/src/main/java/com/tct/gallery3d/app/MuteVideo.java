/*
 * Copyright (C) 2012 The Android Open Source Project
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
/* ----------|----------------------|-------------------- -|-------------------*/
/* 28/01/2015|qiang.ding1           |PR908578              |Android5.0][Gallery_v5.1.4.1.0106.0]The	20
*            |                      |                      |presentation don't match with ergo*/
/* ----------|----------------------|------------------- --|-------------------*/
/* ----------|----------------------|-------------------- -|-------------------*/
/* 06/02/2015|qiang.ding1           |PR919611              |[Gallery]Preview video has a problem when trim video
/* ----------|----------------------|----------------------|----------------- */
/* 16/02/2015|chengbin.du           |PR933880              |[Android 5.0][Gallery_v5.1.4.1.0116.0]Gallery pop up selection frame when clicking mute button*/
/* ----------|----------------------|----------------------|----------------- */
/* 04/03/2015|dongliang.feng        |PR939544              |[Android5.0][Gallery_v5.1.4.1.0117.0][Translation][Arabic]]The words of mute isn't translated */
/* ----------|----------------------|----------------------|----------------- */
/* 04/09/2015| jian.pan1            |PR966225              |Video is muted even if you have canceled the process
/* ----------|----------------------|----------------------|----------------- */
/* 06/23/2015| ye.chen              |PR1027868             |[Android5.1][Gallery_v5.1.13.1.0209.0]It not return video preview interface when play finish
/* ----------|----------------------|----------------------|----------------- */
/* 17/07/2015|     su.jiang         |      PR-1042015      |[Android5.1][Gallery_v5.1.13.1.0212.0]After mute video and played,it still display original video*/
/*-----------|----------------------|----------------------|-------------------------------------------------------------------------------------------------*/
package com.tct.gallery3d.app;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.widget.Toast;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.util.SaveVideoFileInfo;
import com.tct.gallery3d.util.SaveVideoFileUtils;

import java.io.IOException;

public class MuteVideo {

    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-09,PR966225 begin
    private String TAG = "MuteVideo";
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-09,PR966225 end

    private ProgressDialog mMuteProgress;

    private String mFilePath = null;
    private Uri mUri = null;
    private SaveVideoFileInfo mDstFileInfo = null;
    private Activity mActivity = null;
    private final Handler mHandler = new Handler();
    private String mMimeType;
    ArrayList<String> mUnsupportedMuteFileTypes = new ArrayList<String>();
    private final String FILE_TYPE_DIVX = "video/divx";
    private final String FILE_TYPE_AVI = "video/avi";
    private final String FILE_TYPE_WMV = "video/x-ms-wmv";
    private final String FILE_TYPE_ASF = "video/x-ms-asf";
    private final String FILE_TYPE_WEBM = "video/webm";

    final String TIME_STAMP_NAME = "_yyyyMMdd_HHmmss"; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, PR939544
    //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2014-12-18,PR919611 begin
    private boolean mCanMute = true;
    //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2014-12-18,PR919611 end
    private boolean muteDialogHasCancel = false;
    private Uri mMuteUri = null;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-17,PR1042015

    public MuteVideo(String filePath, Uri uri, Activity activity) {
        mUri = uri;
        mFilePath = filePath;
        mActivity = activity;
        if (mUnsupportedMuteFileTypes != null) {
            mUnsupportedMuteFileTypes.add(FILE_TYPE_DIVX);
            mUnsupportedMuteFileTypes.add(FILE_TYPE_AVI);
            mUnsupportedMuteFileTypes.add(FILE_TYPE_WMV);
            mUnsupportedMuteFileTypes.add(FILE_TYPE_ASF);
            mUnsupportedMuteFileTypes.add(FILE_TYPE_WEBM);
        }
    }

    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-09,PR966225 begin
    public void muteInBackground() {
        // [BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, PR939544 begin
        String stampName = "'"
                + mActivity.getResources().getString(R.string.mute_action)
                        .toUpperCase() + "'" + TIME_STAMP_NAME;
        mDstFileInfo = SaveVideoFileUtils.getDstMp4FileInfo(stampName,
                mActivity.getContentResolver(), mUri,
                mActivity.getString(R.string.folder_download));
        // [BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, PR939544 end

        mMimeType = mActivity.getContentResolver().getType(mUri);
        if (!isValidFileForMute(mMimeType)) {
            Toast.makeText(mActivity.getApplicationContext(),
                    mActivity.getString(R.string.mute_nosupport),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // [BUGFIX]-Add by TCTNJ,qiang.ding1, 2014-12-18,PR919611 begin
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    VideoUtils.startMute(mFilePath, mDstFileInfo);
                    if (!muteDialogHasCancel) {
                        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-07-17,PR1042015 begin
                        mMuteUri = SaveVideoFileUtils.insertContent(mDstFileInfo,
                                mActivity.getContentResolver(), mUri);
                        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-07-17,PR1042015 end
                    }
                } catch (IOException e) {
                    mCanMute = false;
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    mCanMute = false;
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    mCanMute = false;
                    e.printStackTrace();
                }
                // After muting is done, trigger the UI changed.
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // [BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-01-28,PR908578 end
                        if (mMuteProgress != null) {
                            mMuteProgress.dismiss();
                            mMuteProgress = null;
                            if (!muteDialogHasCancel) {
                                if (mCanMute) {
                                    // [BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-01-28,PR908578 begin
                                    Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.mute_save_into,
                                                    mDstFileInfo.mFolderName),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(mActivity, mActivity.getString(R.string.mute_nosupport),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                            // Show the result only when the activity not stopped.
                            if (mCanMute && !muteDialogHasCancel) {
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                                // [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-02-16,PR933880 begin
                                intent.setClass(mActivity, MovieActivity.class);
                                // [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-02-16,PR933880 end
                                intent.setDataAndType(Uri.fromFile(mDstFileInfo.mFile), "video/*");
                             // [BUGFIX]-Add by TCTNJ,ye.chen, 2015-06-23,PR1027868 begin
                                intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
                             // [BUGFIX]-Add by TCTNJ,ye.chen, 2015-06-23,PR1027868 end
                                //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-17,PR1042015 begin
                                intent.putExtra(GalleryConstant.MUTE_TRIM_URI,mMuteUri.toString());
                                mActivity.startActivityForResult(intent, GalleryConstant.REQUEST_TRIM_MUTE);
                                //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-17,PR1042015 end
                            } else {
                                // muteDialogHasCancel may be changed at this point
                                if (muteDialogHasCancel) {
                                    int result = SaveVideoFileUtils.deleteContent(
                                                    Video.Media.EXTERNAL_CONTENT_URI,
                                                    mActivity.getContentResolver(),
                                                    mDstFileInfo);
                                    Log.i(TAG, "deleteContent result:" + result);
                                }
                                if (mDstFileInfo.mFile.exists()) {
                                    boolean deleteResult = mDstFileInfo.mFile.delete();
                                    Log.i(TAG, "deleteContent deleteResult:" + deleteResult);
                                }
                            }
                        }
                    }
                });
            }
        }).start();
        // [BUGFIX]-Add by TCTNJ,qiang.ding1, 2014-12-18,PR919611 end
    }
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-09,PR966225 end

    private void showProgressDialog() {
        mMuteProgress = new ProgressDialog(mActivity);
        mMuteProgress.setTitle(mActivity.getString(R.string.muting));
        mMuteProgress.setMessage(mActivity.getString(R.string.please_wait));
        mMuteProgress.setCancelable(false);
        mMuteProgress.setCanceledOnTouchOutside(false);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-09,PR966225 begin
        mMuteProgress.setButton(mActivity.getResources().getString(R.string.cancel),
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    muteDialogHasCancel = true;
                    if (mMuteProgress != null) {
                        mMuteProgress.dismiss();
                    }
                }
            });
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-09,PR966225 end
        mMuteProgress.show();
    }
    private boolean isValidFileForMute(String mimeType) {
        if (mimeType != null) {
            for (String fileType : mUnsupportedMuteFileTypes) {
               if (mimeType.equals(fileType)) {
                   return false;
               }
            }
            return true;
        } else {
            return false;
        }
    }
}

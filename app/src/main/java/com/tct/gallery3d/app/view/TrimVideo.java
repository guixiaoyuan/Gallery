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
/* ----------|----------------------|----------------------|----------------- */
/* 21/01/2015|jian.pan1             |FR904501              |Gallery Ergo 5.1.4 -
/*           |                      |                      |Fast forward and reverse
/* ----------|----------------------|----------------------|----------------- */
/* 28/01/2015|qiang.ding1           |PR908578              |Android5.0][Gallery_v5.1.4.1.0106.0]The	20
*            |                      |                      |presentation don't match with ergo*/
/* ----------|----------------------|------------------- --|-------------------*/
/* 2/02/2015 |qiang.ding1           |PR918018              |[Gallery]Trim video can not be played*/
/* ----------|----------------------|------------------- --|-------------------*/
/* 06/02/2015|qiang.ding1           |PR919611              |[Gallery]Preview video has a problem when trim video
/* ----------|----------------------|------------------- --|-------------------*/
/* 16/02/2015|chengbin.du           |PR933880              |[Android 5.0][Gallery_v5.1.4.1.0116.0]Gallery pop up selection frame when clicking mute button*/
/* ----------|----------------------|----------------------|----------------- */
/* 10/03/2015|dongliang.feng        |PR942467              |[Android5.0][Gallery_v5.1.9.1.0101.0][Translation][UI]The words of trim and mute isn't translated */
/* ----------|----------------------|----------------------|----------------- */
/* 09/06/2015 |    su.jiang     |      PR-1016969         |[Android5.0][Gallery_Global_v5.1.13.1.0206.0][UI]The             */
/*                                                         color of status bar is not match with title bar in trim interface*/
/*------------|---------------------|-------------------------|-------------------------------------------------------------*/
/* 02/07/2015 |    jialiang.ren     |      PR-1034551         |[Android 5.1][Gallery_v5.1.13.1.0211.0]There is a blank on trim bar*/
/*------------|---------------------|-------------------------|-------------------------------------------------------------------*/
/* 03/07/2015|dongliang.feng        |PR1036165             |[Android 5.1][Gallery_v5.1.13.1.0211.0]It not return video preview interface when play finish */
/* ----------|----------------------|----------------------|----------------- */
/* 17/07/2015 |    su.jiang     |      PR-1042015   |[Android5.1][Gallery_v5.1.13.1.0212.0]After mute video and played,it still display original video*/
/*------------|-----------------|-------------------|-------------------------------------------------------------------------------------------------*/
/* 24/08/2015 |    ye.chen      |      PR-1072404   |[Android 5.1][Gallery_v5.2.0.1.1.0303.0]Can't trim video successfully*/
/*------------|-----------------|-------------------|-------------------------------------------------------------------------------------------------*/
/* 22/10/2015 |    su.jiang     |  PR-765007        |[Android5.1][Gallery_v5.2.3.1.1.0307.0]It stay on trim interface after trim video*/
/*------------|-----------------|-------------------|---------------------------------------------------------------------------------*/
/* 26/10/2015 |    su.jiang     |  PR-791933        |[Amdroid5.1][Gallery_v5.2.3.1.1.0307.0][Ergo]It not display prompt when exit trim interface*/
/*------------|-----------------|-------------------|-------------------------------------------------------------------------------------------*/
/* 28/10/2015 |    su.jiang     |  PR-791930        |[Android5.1][Gallery_v5.2.3.1.1.0307.0]It can play or paused video when locking antion bar*/
/*------------|-----------------|-------------------|------------------------------------------------------------------------------------------*/
/* 12/07/2015| jian.pan1            | [ALM]Defect:1001131  |[BT][AVRCP]Video will not stop while play the music during video is playing.
/* ----------|----------------------|----------------------|----------------- */
/* 2016/02/03|  caihong.gu-nb  |  PR-1536602   |[Video Streaming]The progress bar doesn't disappear when playing live streaming */
/*-----------|-----------------|---------------|---------------------------------------------------------------------------------*/

package com.tct.gallery3d.app.view;

import java.io.File;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.ControllerOverlay;
import com.tct.gallery3d.app.MovieActivity;
import com.tct.gallery3d.app.SystemBarTintManager;
import com.tct.gallery3d.app.TrimControllerOverlay;
import com.tct.gallery3d.app.VideoUtils;
import com.tct.gallery3d.app.ControllerOverlay.Listener;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.util.SaveVideoFileInfo;
import com.tct.gallery3d.util.SaveVideoFileUtils;

public class TrimVideo extends Activity implements
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        ControllerOverlay.Listener {

    private VideoView mVideoView;
//    private ImageView mSaveVideoIv;
    private TrimControllerOverlay mController;
    private Context mContext;
    private Uri mUri;
    private final Handler mHandler = new Handler();
    public static final String TRIM_ACTION = "com.android.camera.action.TRIM";

    public ProgressDialog mProgress;

    private int mTrimStartTime = 0;
    private int mTrimEndTime = 0;
    private int mVideoPosition = 0;
    public static final String KEY_TRIM_START = "trim_start";
    public static final String KEY_TRIM_END = "trim_end";
    public static final String KEY_VIDEO_POSITION = "video_pos";
    private boolean mHasPaused = false;

    private String mSrcVideoPath = null;
    private static final String TIME_STAMP_NAME = "_yyyyMMdd_HHmmss"; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-10, PR942467
    private SaveVideoFileInfo mDstFileInfo = null;
  //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-06,PR919611 begin
    private boolean mCanTrim  = true;
  //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-06,PR919611 end
  //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-2,PR918018  begin
    private boolean mIsInProgressCheck = false;
  //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-2,PR918018  end
    private SystemBarTintManager mTintManager = null;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-09,PR1016969
    private Uri mTrimUri = null;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-17,PR1042015

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
    private AudioManager mAudioManager;
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end

   //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1312087 begin
   private boolean isComplete=false;
   //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1312087 end
 //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-15,PR1759784 begin
   private boolean isfirstsetpress=true;
 //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-15,PR1759784 end
 //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-17,PR1717742 begin
   private AlertDialog errorDialog;
 //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-17,PR1717742 end
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = getApplicationContext();
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-22,ALM-1546158 begin
        mCanTrim = true;
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-22,ALM-1546158 end
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        int displayOptions = ActionBar.DISPLAY_SHOW_HOME;
        actionBar.setDisplayOptions(0, displayOptions);
        displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        actionBar.setDisplayOptions(displayOptions, displayOptions);
        actionBar.setCustomView(R.layout.trim_menu);

        ImageView trimBack = (ImageView) findViewById(R.id.trim_back);
        /*mSaveVideoIv = (ImageView) findViewById(R.id.trim_save);
        mSaveVideoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                trimVideo();
            }
        });*/
        trimBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //[BUGFIX]-Modify by TSNJ,zhe.xu, 2016-02-01,defect 1538585 begin
                if (isModified()) {
                    showTrimCancelDialog();//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-26,PR791933
                } else {
                    finish();
                }
                //[BUGFIX]-Modify by TSNJ,zhe.xu, 2016-02-01,defect 1538585 end
            }
        });
//        mSaveVideoIv.setEnabled(false);

        Intent intent = getIntent();
        mUri = intent.getData();
        mSrcVideoPath = intent.getStringExtra(GalleryConstant.KEY_MEDIA_ITEM_PATH);
        setContentView(R.layout.trim_view);
        View rootView = findViewById(R.id.trim_view_root);

        mVideoView = (VideoView) rootView.findViewById(R.id.surface_view);

        mController = new TrimControllerOverlay(mContext);
        ((ViewGroup) rootView).addView(mController.getView());
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/03,PR1536602 begin
        mController.setIsLiveStream(false);
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/03,PR1536602 end
        mController.setListener(this);
        mController.setCanReplay(true);

        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setVideoURI(mUri);

        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getApplicationContext()
                    .getSystemService(Context.AUDIO_SERVICE);
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end
        loadVideoFirstFrame();   //[BUGFIX]-Add by TCTNJ,hao.yin, 2016-03-14,PR1785709
        playVideo();
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-09,PR1016969 begin
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mTintManager = new SystemBarTintManager(this);
        mTintManager.setStatusBarTintEnabled(true);
        mTintManager.setStatusBarTintColor(Color.BLACK);
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-09,PR1016969 end
    }

    //[BUGFIX]-Add by TCTNJ,hao.yin, 2016-03-14,PR1785709 begin
    private void loadVideoFirstFrame(){
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        try {
            mSrcVideoPath = Uri.decode(mSrcVideoPath);
            media.setDataSource(mSrcVideoPath);
            Bitmap bitmap = media.getFrameAtTime();
            mVideoView.setBackground(new BitmapDrawable(bitmap));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //[BUGFIX]-Add by TCTNJ,hao.yin, 2016-03-14,PR1785709 end

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-26,PR791933 begin
    private void showTrimCancelDialog(){
        AlertDialog.Builder dialog =  new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.trim_title));
        dialog.setMessage(getString(R.string.trim_message));
        dialog.setNegativeButton(getString(R.string.cancel), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        dialog.setPositiveButton(getString(R.string.trim_discard), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog cancelDialog = dialog.create();
        cancelDialog.show();
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-26,PR791933 begin

    @Override
    public void onResume() {
        super.onResume();
        if (mHasPaused) {
            mVideoView.seekTo(mVideoPosition);
            mVideoView.resume();
            mHasPaused = false;
        }
      //[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-03-05,PR1649927 begin
        mHandler.postDelayed(mProgressChecker,300);
      //[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-03-05,PR1649927 end
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-02,PR1034551 begin
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trim_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case R.id.save_trim_video:
            trimVideo();
            break;
        }
        return true;
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-02,PR1034551 end

    @Override
    public void onPause() {
        mHasPaused = true;
        mHandler.removeCallbacksAndMessages(null);
        mVideoPosition = mVideoView.getCurrentPosition();
        mVideoView.suspend();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
      //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-17,PR1717742 begin
        if(errorDialog!=null){
            errorDialog.dismiss();
            errorDialog=null;
        }
      //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-17,PR1717742 end
        //[Defect]-Added by Yuanxi.Jiang ,2016-01-06,defect 1306257 BEGIN
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
        //[Defect]-Added by Yuanxi.Jiang ,2016-01-06,defect 1306257 END
        mVideoView.stopPlayback();
        super.onDestroy();
    }

    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
          //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-2,PR918018  begin
            mIsInProgressCheck = true;
          //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-2,PR918018  end
            mHandler.postDelayed(mProgressChecker, 200 - (pos % 200));
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(KEY_TRIM_START, mTrimStartTime);
        savedInstanceState.putInt(KEY_TRIM_END, mTrimEndTime);
        savedInstanceState.putInt(KEY_VIDEO_POSITION, mVideoPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTrimStartTime = savedInstanceState.getInt(KEY_TRIM_START, 0);
        mTrimEndTime = savedInstanceState.getInt(KEY_TRIM_END, 0);
        mVideoPosition = savedInstanceState.getInt(KEY_VIDEO_POSITION, 0);
    }

    // This updates the time bar display (if necessary). It is called by
    // mProgressChecker and also from places where the time bar needs
    // to be updated immediately.
    private int setProgress() {
        //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1312087 begin
        //mVideoPosition = mVideoView.getCurrentPosition();
         if(isComplete)
         {
             mVideoPosition = mVideoView.getDuration();
         }else
         {
             mVideoPosition = mVideoView.getCurrentPosition(); 
         }
         //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1312087 end
       //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-03,PR1665013 begin
       //[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-03-09,PR1723353 begin
         if(mVideoPosition<=mTrimStartTime&&mTrimStartTime>0)
         {
             mVideoPosition=mTrimStartTime;
             if(!mIsInProgressCheck){
                 mController.showPaused();
                 mVideoView.pause();
             }
         }
       //[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-03-09,PR1723353 begin
       //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-03,PR1665013 end
        // If the video position is smaller than the starting point of trimming,
        // correct it.
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-2,PR918018  begin
        if (!mIsInProgressCheck && mVideoPosition < mTrimStartTime) {
            mVideoView.seekTo(mTrimStartTime);
            mVideoPosition = mTrimStartTime;
        }
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-2,PR918018  end
        // If the position is bigger than the end point of trimming, show the
        // replay button and pause.
        if (mVideoPosition >= mTrimEndTime && mTrimEndTime > 0) {
            if (mVideoPosition > mTrimEndTime) {
                mVideoView.seekTo(mTrimEndTime);
                mVideoPosition = mTrimEndTime;
            }
            mController.showEnded();
            mVideoView.pause();
        }

        int duration = mVideoView.getDuration();
        if (duration > 0 && mTrimEndTime == 0) {
          //[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-03-15,PR1759784 begin
            if(isfirstsetpress){
            mTrimEndTime = duration;
            isfirstsetpress=false;
            }else{
                mTrimEndTime=0;
                if(mVideoView.isPlaying()){
                    mController.showPaused();
                    mVideoView.pause();
                }
            }
          //[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-03-15,PR1759784 end
        }
        mController.setTimes(mVideoPosition, duration, mTrimStartTime, mTrimEndTime);
        // Enable save if there's modifications
//        mSaveVideoIv.setEnabled(isModified());
        return mVideoPosition;
    }

     /*MODIFIED-BEGIN by hao.yin, 2016-03-28,BUG-1867849*/
    private boolean isFirstPlay = false;
    private void playVideo() {
         if(isFirstPlay){
            mVideoView.postDelayed(new Runnable() {
                @Override
                   public void run() {
                    mVideoView.setBackground(null);
                }
             }, 700);
        }else{
             isFirstPlay = true;
        }
         /*MODIFIED-END by hao.yin,BUG-1867849*/
        //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1312087 begin
        isComplete=false;
        //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1312087 end
        mVideoView.start();
        mController.showPlaying();
        setProgress();
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
        mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end
    }

    private void pauseVideo() {
        mVideoView.pause();
        mController.showPaused();
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end
    }

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pauseVideo();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                break;
            }
        }
    };
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end

    private boolean isModified() {
        int delta = mTrimEndTime - mTrimStartTime;

        // Considering that we only trim at sync frame, we don't want to trim
        // when the time interval is too short or too close to the origin.
        if (delta < 100 || Math.abs(mVideoView.getDuration() - delta) < 100) {
            return false;
        } else {
            return true;
        }
    }

    private void trimVideo() {
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-10, PR942467 begin
        String stampName = "'" + mContext.getResources().getString(R.string.trim_action).toUpperCase() + "'" + TIME_STAMP_NAME;
        mDstFileInfo = SaveVideoFileUtils.getDstMp4FileInfo(stampName,
                getContentResolver(), mUri, getString(R.string.folder_download));
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-10, PR942467 end
        final File mSrcFile = new File(mSrcVideoPath);

        showProgressDialog();
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-06,PR919611 begin
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                  //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-15,PR1759784 begin
                    if(mTrimEndTime==0){
                        throw new Exception();
                    }
                  //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-15,PR1759784 end
                    VideoUtils.startTrim(mSrcFile, mDstFileInfo.mFile,
                            mTrimStartTime, mTrimEndTime);
                    // Update the database for adding a new video file.
                    mTrimUri = SaveVideoFileUtils.insertContent(mDstFileInfo,//[BUGFIX]-add by TCTNJ,su.jiang, 2015-07-17,PR1042015
                            getContentResolver(), mUri);
                    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-22,ALM-1546158 begin
                    mCanTrim = mTrimUri != null;//[BUGFIX]-Modify by TCTNJ,hao.yin, 2016-03-05,PR1717537
                    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-22,ALM-1546158 end
                } catch (IOException e) {
                    mCanTrim = false;
                    e.printStackTrace();
                }catch(IllegalStateException e){
                    mCanTrim = false;
                    e.printStackTrace();
                }catch(RuntimeException e){
                    mCanTrim = false;
                    e.printStackTrace();
                }
              //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-15,PR1759784 begin
                catch (Exception e){
                    mCanTrim = false;
                }
              //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-15,PR1759784 end
                // After trimming is done, trigger the UI changed.
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                    //[BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-01-28,PR908578 begin
                    if(mCanTrim && mTrimUri != null){//[BUGFIX]-Modify by TCTNJ,hao.yin, 2016-03-05,PR1717537
                        Toast.makeText(getApplicationContext(),
                            getString(R.string.trim_save_into, mDstFileInfo.mFolderName),
                            Toast.LENGTH_SHORT)
                            .show();
                    }else{
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.fail_trim),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                      //[BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-01-28,PR908578 end
                        // TODO: change trimming into a service to avoid
                        // this progressDialog and add notification properly.
                        if (mProgress != null) {
                            mProgress.dismiss();
                            mProgress = null;
                        }//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-22,PR765007
                            // Show the result only when the activity not stopped.
                            if(mCanTrim && mTrimUri != null){ ////[BUGFIX]-Modify by TCTNJ,hao.yin, 2016-03-05,PR1717537
                                 Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                                 //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-02-16,PR933880 begin
                                 intent.setClass(TrimVideo.this, MovieActivity.class);
                                 //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-02-16,PR933880 end
                                 intent.setDataAndType(Uri.fromFile(mDstFileInfo.mFile), "video/*");
                                 //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-03, PR1036165 begin
                                 intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
                                 //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-03, PR1036165 end
                                 //[BUGFIX]-add by TCTNJ,su.jiang, 2015-07-17,PR1042015
                                 intent.putExtra(GalleryConstant.MUTE_TRIM_URI, mTrimUri.toString());
                                 startActivityForResult(intent, GalleryConstant.REQUEST_TRIM_MUTE);
                                 //[BUGFIX]-add by TCTNJ,su.jiang, 2015-07-17,PR1042015
                            }else{
                                if(mDstFileInfo.mFile.exists()){
                                    mDstFileInfo.mFile.delete();
                                }
                            }
                            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-22,ALM-1546158 begin
                            //mCanTrim = true;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-08-24,PR1072404
                            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-22,ALM-1546158 end
//                          finish();//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-07-17,PR1042015
                    }
                });
            }
        }).start();
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-06,PR919611 end
    }

    //[BUGFIX]-add by TCTNJ,su.jiang, 2015-07-17,PR1042015 begin
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(GalleryConstant.REQUEST_TRIM_MUTE, data);
        finish();
    }
    //[BUGFIX]-add by TCTNJ,su.jiang, 2015-07-17,PR1042015 end
    private void showProgressDialog() {
        // create a background thread to trim the video.
        // and show the progress.
        if(mProgress == null) {
            mProgress = new ProgressDialog(this);
            mProgress.setTitle(getString(R.string.trimming));
            mProgress.setMessage(getString(R.string.please_wait));
            // TODO: make this cancelable.
            mProgress.setCancelable(false);
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
        }
    }

    @Override
    public void onPlayPause() {
        if (mVideoView.isPlaying()) {
            pauseVideo();
        } else {
            playVideo();
        }
    }

    @Override
    public void onSeekStart() {
         //MODIFIED-BEGIN by hao.yin, 2016-03-28, BUG-1867849
        mVideoView.postDelayed(new Runnable(){
           @Override
             public void run() {
              mVideoView.setBackground(null);
           }
        }, 500);
         /*MODIFIED-END by hao.yin,BUG-1867849*/
        pauseVideo();
    }

    @Override
    public void onSeekMove(int time) {
        mVideoView.seekTo(time);
    }

    @Override
    public void onSeekEnd(int time, int start, int end) {
        mVideoView.seekTo(time);
        mTrimStartTime = start;
        mTrimEndTime = end;
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-2,PR918018  begin
        mIsInProgressCheck = false;
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-2,PR918018  end
        setProgress();
    }

    @Override
    public void onShown() {
    }

    @Override
    public void onHidden() {
    }

    @Override
    public void onReplay() {
        mVideoView.seekTo(mTrimStartTime);
        playVideo();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1312087 begin
        isComplete=true;
        //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1312087 end
        mController.showEnded();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
      //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-17,PR1717742 begin
        int messageId;
        if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
              messageId = R.string.VideoView_error_text_invalid_progressive_playback;
        } else {
              messageId = R.string.VideoView_error_text_unknown;
        }
        mp.reset();
        if(errorDialog!=null){
            if(!errorDialog.isShowing()){
                 errorDialog.show();
            }
        }else{
            errorDialog=new AlertDialog.Builder(TrimVideo.this)
            .setMessage(messageId)
            .setPositiveButton(R.string.VideoView_error_button,
            new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
            }})
            .setCancelable(false)
            .show();
        }
        return true;
        //return false;
      //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-17,PR1717742 end
    }

    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/5/11, FR-824326 No WiFi Display Extension Mode
    @Override
    public boolean onIsRTSP() {
        // TODO Auto-generated method stub
        return false;
    }
    //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/5/11, FR-824326 No WiFi Display Extension Mode

    // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014, For FR828601 Pop-up Video play
    @Override
    public void onShowPopupVideo() {
        // TODO Auto-generated method stub
    }
    // [FEATURE]-Add-END by jian.pan1

    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 begin
    @Override
    public void onFastForward() {
    }

    @Override
    public void onReverse() {
    }
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 end

    @Override
    public void onControlSystemUI(boolean isHideSytemUI) {
        // TODO Auto-generated method stub
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-28,PR791930 begin
    @Override
    public void udpateTimeBar() {
        // TODO Auto-generated method stub
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-28,PR791930 begin
}

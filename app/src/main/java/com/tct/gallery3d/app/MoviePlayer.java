/*
 * Copyright (C) 2009 The Android Open Source Project
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
/* 20/11/2014|chengqun.sun            |FR826631              |Multi screen interaction*/
/* ----------|----------------------|----------------------|------------------*/
/* 12/08/2014|peng.tian             |PR 857286              |[Video Streaming]Mobile*/
/*           |                      |                      |screen will be black*/
/*           |                      |                      |about 8 Seconds after*/
/*           |                      |                      |stop alarm        */
/* ----------|----------------------|----------------------|------------------*/
/* 12/16/2014|haihua.zhu            |PR 872085             |[Video Streaming][Interaction]*/
/*           |                      |                      |The elaspe time stays at*/
/*           |                      |                      |00:00 for seconds */
/*           |                      |                      |after hanging up the call.*/
/* ----------|----------------------|----------------------|------------------*/
/* 10/1/2015  |ye.chen               |PR898610              |[Video Streaming]Cannot recover in 5 minutes after pause for more than 1 minutes.
/* ----------|----------------------|----------------------|----------------- */
/* 15/01/2015|dongliang.feng        |PR900979              |[Gallery][Wi-Fi display]The */
/*           |                      |                      |pause button can not control */
/*           |                      |                      |playing status of the video */
/*           |                      |                      |only can replace*/
/* ----------|----------------------|----------------------|----------------- */
/* 19/01/2015|dongliang.feng        |PR906557              |[Gallery]Video gallery */
/*           |                      |                      |display abnormal pause icon */
/* ----------|----------------------|----------------------|----------------- */
/* 21/01/2015|jian.pan1             |FR904501              |Gallery Ergo 5.1.4 -
/*           |                      |                      |Fast forward and reverse
/* ----------|----------------------|----------------------|----------------- */
/* 24/01/2015|dongliang.feng        |PR910033              |[Gallery]The phone behavior */
/*           |                      |                      |is not correct during playing */
/*           |                      |                      |video have incoming call */
/* ----------|----------------------|----------------------|----------------- */
/* ----------|----------------------|------------------- --|-------------------*/
/* 3/02/2015 |qiang.ding1           |PR901897              |[Clone][4.7][MMS][FM Radio] FM Radio
 *           |                      |                      |would stop working after
 *           |                      |                      |preview a mms with audio/video*/
/* ----------|----------------------|------------------ ---|-------------------*/
/* 3/02/2015 |jian.pan1             |PR901897              |should not support wifi_display extend mode
/* ----------|----------------------|------------------ ---|-------------------*/
/* 05/02/2015|ye.chen               |FR908268              |[Video streaming]It shouldn't pause live TV and prompt whether resume video
/* ----------|----------------------|----------------------|----------------- */
/* 10/02/2015|dongliang.feng        |PR928216              |[Video Streaming]The video */
/*           |                      |                      |continue to play directly after */
/*           |                      |                      |pause->lock->unlock screen */
/* ----------|----------------------|----------------------|----------------- */
/* 04/03/2015|dongliang.feng        |CR940102              |[Gallery_Ergo_5.1.9.pdf]Video Lock Function */
/* ----------|----------------------|----------------------|----------------- */
/* 03/12/2015|    ye.chen           |      CR-938507       |video full screen as default  */
/* ----------|----------------------|----------------------|-----------------------------------------*/
/* 14/03/2015|dongliang.feng        |PR948961              |[Android5.0][Gallery_v5.1.9.1.0103.0] */
/*           |                      |                      |[Video streaming]The device will stop */
/*           |                      |                      |loading online video when unlock the screen */
/* ----------|----------------------|----------------------|----------------- */
/* 03/19/2015|dongliang.feng        |PR953848              |[Android5.0][Gallery_v5.1.9.1.0105.0]Cannot */
/*           |                      |                      |pausevideo which is opened from file manager */
/* ----------|----------------------|----------------------|----------------------------------------------- */
/* 23/03/2015|dongliang.feng        |PR956459              |[Android5.0][Gallery_v5.1.9.1.0107.0] */
/*           |                      |                      |The softkey will popup when lock the */
/*           |                      |                      |playing video screen */
/* ----------|----------------------|----------------------|----------------- */
/* 27/03/2015|dongliang.feng        |PR958355              |[Streaming]The progress bar */
/*           |                      |                      |is bounce when set loop */
/* ----------|----------------------|----------------------|----------------- */
/* 30/03/2015|dongliang.feng        |CR962977              |add 3D Audio function to movie player */
/* ----------|----------------------|----------------------|----------------- */
/* 08/04/2015|dongliang.feng        |PR968503              |[Android5.0][Gallery_v5.1.9.1.0110.0] */
/*           |                      |                      |3D Audio still displays when plugging in headset */
/* ----------|----------------------|----------------------|----------------- */
/* 09/04/2015|dongliang.feng        |PR971974              |[Gallery]The video player interface with 3D audio icon */
/* ----------|----------------------|----------------------|----------------- */
/* 27/04/2015 |    jialiang.ren     |      PR-986309       |[HOMO][Orange][22][HLS] 1.05 - Audio Only - No default icon displayed*/
/*------------|---------------------|----------------------|---------------------------------------------------------------------*/
/* 04/29/2015|dongliang.feng        |CR989796              |[5.0][Gallery]video play backward/forward */
/* ----------|----------------------|----------------------|----------------- */
/* 08/05/2015 |    jialiang.ren     |      PR-997159       |[Android][Gallery_v5.1.13.1.0201.0]The video */
/*                                                          won't unlock when plugging off headset       */
/*------------|---------------------|----------------------|---------------------------------------------*/
/* 28/05/2015|chengbin.du           |PR1006357             |RTSP video streaming is interrupted on WiFi/LTE change */
/* ----------|----------------------|----------------------|----------------- */
/* 24/06/2015|ye.chen               |PR1029197             |[Android5.1][Gallery_Global_v5.1.13.1.0209.0]The progress bar can't be adjusted when play the video */
/* ----------|----------------------|----------------------|----------------- */
/* 07/13/2015| jian.pan1            | PR1041880            |[SW][Gallery]Subtitle can not add to video
/* ----------|----------------------|----------------------|----------------- */
/* 07/15/2015| ye.chen              | PR1043100            |[Android 5.1][Gallery_v5.1.13.1.0212.0]The screen flash when video play finish
/* ----------|----------------------|----------------------|----------------- */
/* 10/07/2015|    su.jiang          |    PR-1041703        |[UI][Gallery]The UI display error when play a video clip in gallery*/
/*-----------|----------------------|----------------------|-------------------------------------------------------------------*/
/* 29/08/2015|dongliang.feng        |PR1075502             |[Video_streaming]Progress bar is not consitent */
/*           |                      |                      |with video when click pause button */
/* ----------|----------------------|----------------------|----------------- */
/* 19/10/2015|    su.jiang          |  PR-732345           |[Android 5.1][Gallery_v5.2.0.1.1.0306.0]The DRM video can be trimed*/
/*-----------|----------------------|----------------------|-------------------------------------------------------------------*/
/* 28/10/2015|    su.jiang          |  PR-791930           |[Android5.1][Gallery_v5.2.3.1.1.0307.0]It can play or paused video when locking antion bar*/
/*-----------|----------------------|----------------------|------------------------------------------------------------------------------------------*/
/* 12/07/2015| jian.pan1            | [ALM]Defect:1001131  |[BT][AVRCP]Video will not stop while play the music during video is playing.
/* ----------|----------------------|----------------------|----------------- */
/* 12/12/2015|    su.jiang          |  PR-1134047          |[Video Streaming]Facebook icon display gray after click share when playing video streaming*/
/*-----------|----------------------|----------------------|------------------------------------------------------------------------------------------*/
/* 14/12/2015|    su.jiang          |  PR-1061587          |[Gallery]The colour of progress bar don't change when drag the progress bar at video stop playing screen*/
/*-----------|----------------------|----------------------|--------------------------------------------------------------------------------------------------------*/
/* 17/12/2015|    su.jiang          |  PR-1060028          |[Gallery][MMS]Pop up gallery force close when view the detail of video in MMS composer*/
/*-----------|----------------------|----------------------|--------------------------------------------------------------------------------------*/
/* 12/24/2015| jian.pan1            |[ALM]Defect:1126670   |[Android6.0][Gallery_v5.2.5.1.0320.0]The video will backward after sliding screen to fast forward
/* ----------|----------------------|----------------------|----------------- */
/* 30/12/2015|    su.jiang          |  PR-1248530          |[Android6.0][Gallery][REG]It have edit button when play video streaming*/
/*-----------|----------------------|----------------------|-----------------------------------------------------------------------*/
/* 11/01/2015|    su.jiang          |  PR-1356208          |[GAPP][Android6.0][Gallery]It have lock effect when it not have lock button.*/
/*-----------|----------------------|----------------------|----------------------------------------------------------------------------*/
/* 12/01/2015|    su.jiang          |  PR-1400047          |[Gallery][Boom key]Tap back in video boom key effect，press boom key in video stop screen，it willenter into boom key effect*/
/*-----------|----------------------|----------------------|----------------------------------------------------------------------------------------------------------------------------*/
/* 2016/01/19|  caihong.gu-nb       |  PR-1401316          | [Gallery][Download]Should not display the 'Share' 'Edit' and 'Cut' icons when play DRM video in Download list screen*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/
/* 2016/01/28|  yuanxi.jiang-nb     |  PR-1392879          | [GAPP][Android6.0][Gallery][Force close]Gallery force close after tap edit button*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/
/* 01/02/2016|    su.jiang          |  PR-1541451          |[GAPP][Android6.0][Gallery]In the video playing interface , 4k video can be edited.*/
/*-----------|----------------------|----------------------|-----------------------------------------------------------------------------------*/
/* 02/15/2016| jian.pan1            |[ALM]Defect:1445752   |[Camera][Video]The Micro-Video would play back at the last time slot
/* ----------|----------------------|----------------------|----------------- */
/* 17/02/2016|    su.jiang          |  PR-1431083          |[GAPP][Android6.0][Gallery]The DRM video play interface display not same as preview interface.*/
/*-----------|----------------------|----------------------|----------------------------------------------------------------------------------------------*/
/* 03/07/2016| jian.pan1            |[ALM]Defect:1476906   |[Video][Radio]Radio fail to play when the the video is suspend
/* ----------|----------------------|----------------------|----------------- */
/* 03/11/2016|    su.jiang          |  PR-1623761          |[Video Streaming]The paused video streaming will play automatically after lock and unlock screen.*/
/*-----------|----------------------|----------------------|-------------------------------------------------------------------------------------------------*/

package com.tct.gallery3d.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Map;

import com.android.gallery3d.app.DlnaService;
import com.android.gallery3d.app.VolumeDialog;
import com.android.gallery3d.ext.IContrllerOverlayExt;
import com.android.gallery3d.ext.IMovieItem;
import com.android.gallery3d.ext.IMoviePlayer;
import com.android.gallery3d.ext.MovieUtils;
import com.android.gallery3d.popupvideo.TctPopupVideoComponent;
import com.android.gallery3d.video.ScreenModeManager;
import com.android.gallery3d.video.ScreenModeManager.ScreenModeListener;
import com.tct.gallery3d.R;
import com.tct.gallery3d.app.CommonControllerOverlay.State;
import com.tct.gallery3d.app.MovieVideoView.OnShouldPause;
import com.tct.gallery3d.common.ApiHelper;
import com.tct.gallery3d.common.BlobCache;
//[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
import com.tct.gallery3d.data.LocalMediaItem;
import com.tct.gallery3d.data.LocalVideo;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.filtershow.cache.ImageLoader;
import com.tct.gallery3d.util.CacheManager;
import com.tct.gallery3d.util.Connectivity;
import com.tct.gallery3d.util.GalleryUtils;
//[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
//[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
//[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;
public class MoviePlayer implements
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        ControllerOverlay.Listener, ControllerOverlay.PlayerControlPanelListener,
      //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnSeekCompleteListener
        //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        {
    @SuppressWarnings("unused")
    private static final String TAG = "MoviePlayer";

    private static final String KEY_VIDEO_POSITION = "video-position";
    private static final String KEY_RESUMEABLE_TIME = "resumeable-timeout";
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-19, PR953848 begin
    private static final String KEY_CURRENT_VIDEO_STATE = "current-video-state";
    private Bundle mSavedInstance = null;
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-19, PR953848 end
    private static final String KEY_VIDEO_LOCK_STATE = "video-lock-state"; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459

    // These are constants in KeyEvent, appearing on API level 11.
    private static final int KEYCODE_MEDIA_PLAY = 126;
    private static final int KEYCODE_MEDIA_PAUSE = 127;

    //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    private static final int MSG_TIMEOUT = 100;
    //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction

    // Copied from MediaPlaybackService in the Music Player app.
    private static final String SERVICECMD = "com.android.music.musicservicecommand";
    private static final String CMDNAME = "command";
    private static final String CMDPAUSE = "pause";

    private static final String VIRTUALIZE_EXTRA = "virtualize";
    private static final long BLACK_TIMEOUT = 800;//[BUGFIX]-Modify by TCTNJ, hao.yin, 2016-03-08, PR1527064

    // If we resume the acitivty with in RESUMEABLE_TIMEOUT, we will keep playing.
    // Otherwise, we pause the player.
    private static final long RESUMEABLE_TIMEOUT = 3 * 60 * 1000; // 3 mins

    private Context mContext;
  //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-02,PR1527396 begin
    private LocalMediaItem mItem = null;
  //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-02,PR1527396 end
    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    //[Ergo] Video player
    private int mVideoDuration = 0;
    // mPlaybackStarted: whether or not playback is ongoing right now
    private boolean mPlaybackStarted = false;
    // mIsPlaying: whether or not media player is in playback when asynchronous event happens
    private boolean mIsPlaying = false;

    public MovieVideoView mVideoView;
    // private final MovieVideoView mVideoView;
    //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode

    private final View mRootView;
    private final Bookmarker mBookmarker;
    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    //private final Uri mUri;
    private Uri mUri;
    //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    private final Handler mHandler = new Handler();
    private final AudioBecomingNoisyReceiver mAudioBecomingNoisyReceiver;
    private HeadsetPluggedReceiver mHeadsetPluggedReceiver; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-08, PR968503
    private final MovieControllerOverlay mController;
    private Connectivity mConnectivity = null;//[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-28,PR1006357

    private long mResumeableTime = Long.MAX_VALUE;
    private int mVideoPosition = 0;
    private boolean mHasPaused = false;
    private int mLastSystemUiVis = 0;

  //[BUGFIX]-add by TCTNJ,qiang.ding1, 2014-11-26,PR846369 begain
    private int mReplayPosition = -1;
    private int mReplayDuration = -1;
  //[BUGFIX]-add by TCTNJ,qiang.ding1, 2014-11-26,PR846369 end
    //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    // If the time bar is being dragged.
    private boolean mDragging = false;
    //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction

    // If the time bar is visible.
    private boolean mShowing;

    private Virtualizer mVirtualizer;

    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    private long mSeekTo;
    public static final boolean METADATA_ALL = false;
    public static final boolean BYPASS_METADATA_FILTER = false;
    public static final int MEDIA_INFO_METADATA_CHECK_COMPLETE = 803;
    public static final int MEDIA_INFO_PAUSE_COMPLETED = 858;
    public static final int MEDIA_INFO_PLAY_COMPLETED = 859;
    public static final int MEDIA_INFO_VIDEO_NOT_SUPPORTED = 860;
    public static final int MEDIA_INFO_GET_BUFFER_DATA = 861;
    public static final int MEDIA_ERROR_CANNOT_CONNECT_TO_SERVER = 261;

    //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    // stream type
    public static final int UNKNOWN_STREAM_TYPE = -1;
    public static final int NON_LIVE_STREAM = 0;
    public static final int LIVE_STREAM = 1;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 begin
    // Fast forward or reverse time 30s
    public static final int INTERVAL_TIME = 30 * 1000;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 end
    public static final int INTERVAL_TIME_FOR_GESTURE = 10 * 1000; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796

    private final ImageView mImageView;
    private VolumeDialog volumedilaog;
    private static final String mIdentification = "Gallery_Video";
    private static int volume = 0;
    private boolean isLongPress = false;
    private int mVolumeStep = 2;
    private boolean mIsDrm = false;
    //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-15,Defect:1445752 begin
    private boolean isPlayCompletion = false;
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-15,Defect:1445752 end

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-02-01,PR1541451 begin
    private boolean mIs4KVideo = false;

    public void set4KVideo (boolean is4KVideo){
        this.mIs4KVideo = is4KVideo;
    }

    public boolean is4KVideo (){
        return mIs4KVideo;
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-02-01,PR1541451 end

    private boolean mCanResume = true;
    //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    // [FEATURE]-Add-BEGIN by jian.pan1,11/05/2014, For FR824779 Video subtitle
    public MovieVideoView getMovieVideoView() {
        return mVideoView;
    }
    // [FEATURE]-Add-END by jian.pan1

    private int mLoopMode = MovieControllerOverlay.LOOP_MODE_OFF;
    private int mLockMode = MovieControllerOverlay.LOCK_MODE_OFF; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;

    public void setAudioManager(AudioManager audioManager) {
        this.mAudioManager = audioManager;
    }

    public void setAudioFocusChangeListener(
            AudioManager.OnAudioFocusChangeListener audioFocusChangeListener) {
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 begin
        //this.mAudioFocusChangeListener = audioFocusChangeListener;
        mVideoView.setAudioFocusChangeListener(audioFocusChangeListener);
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 end
    }
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end

    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 begin
    public boolean isHasPaused() {
        return mHasPaused;
    }
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 end

    private final Runnable mPlayingChecker = new Runnable() {
        @Override
        public void run() {
            if (mVideoView.isPlaying() && (mVideoView.getCurrentPosition() > 0)) {
                mController.showPlaying();
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };

    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
          //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
            int pos = 0;
            if (DlnaService.isShare) {
                if (DlnaService.tv_state == TVState.LOADING && DlnaService.positionTV > 0) {
                    DlnaService.tv_state = TVState.PLAYING;
                }
                // add start by yaping.liu for dlna
                if (DlnaService.tv_state == TVState.PLAYING) {
                    mController.showPlaying();
                } else {
                    mController.showPaused();
                }
                // add end by yaping.liu for dlna
                pos = setProgressOnTV();
                mHandler.postDelayed(mProgressChecker, 500);
            } else {
            pos = setProgress();
            mHandler.postDelayed(mProgressChecker, 1000 - (pos % 1000));
        }
          //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        }
    };

    //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    private Runnable showDilaog = new Runnable() {
        @Override
        public void run() {
            if (!volumedilaog.isShowing()) {
                volumedilaog.dialogshow();
                volumedilaog.setProgress(volume);
                volumedilaog.show();
                Log.i("DLNA", " --- showDilaog and setProgress---" + volume);
                mHandler.removeCallbacks(dismissDilaog);
                mHandler.postDelayed(dismissDilaog, 3000);
            } else {
                volumedilaog.setProgress(volume);
                Log.i("DLNA", " --- only setProgress ---" + volume);
                mHandler.removeCallbacks(dismissDilaog);
                mHandler.postDelayed(dismissDilaog, 3000);
            }

        }
    };

    private Runnable dismissDilaog = new Runnable() {
        @Override
        public void run() {
            if (volumedilaog != null && volumedilaog.isShowing())
                volumedilaog.dismiss();
        }
    };
    //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction

    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-28,PR1006357 begin
    BroadcastReceiver mConnectivityChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive android.net.conn.CONNECTIVITY_CHANGE");
            if(mConnectivity.isConnected()) {
                Log.d(TAG, "network connected");
                if(isNetworkStreaming()) {
                    if(getMovieVideoView().isPlaying()) {
                        pauseVideo();
                        final int pos = mVideoView.getCurrentPosition();
                        new AlertDialog.Builder(mContext)
                        .setMessage(R.string.network_reconnected_when_playing)
                        .setPositiveButton(R.string.ok ,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Log.d(TAG, "click on ok button");
                                        mVideoView.setVideoURI(mUri);
                                        mVideoView.start();
                                        mVideoView.seekTo(pos);
                                        mController.showPlaying();//[BUGFIX]-Add by TCTNJ,cuihua.yang, 2015-12-26,PR1061417
                                    }
                                })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                    }
                }
            }
        }
    };
    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-28,PR1006357 end

    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    public MoviePlayer(View rootView, final MovieActivity movieActivity, IMovieItem info,
            Bundle savedInstance, boolean canReplay) {
        this(rootView, movieActivity, info, savedInstance, canReplay,true);
    }
    //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode

    ///[BUGFIX]-ADD-BEGIN BY TSNJ.LIUDEKUAN ON 2016/01/12 FOR DEFECT1126560
    public void checkPlayingIfNeeded () {
        if (mController.isLoading()) {
            mHandler.removeCallbacks(mPlayingChecker);
            mHandler.postDelayed(mPlayingChecker, 500);
        }
    }
    ///[BUGFIX]-ADD-END BY TSNJ.LIUDEKUAN

    public MoviePlayer(View rootView, final MovieActivity movieActivity,
        //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
            IMovieItem info, Bundle savedInstance, boolean canReplay,boolean canResume) {
            /*Uri videoUriIMovieItem info, Bundle savedInstance, boolean canReplay) {*/
        mCanResume = canResume;//[FEATURE]-Add by TCTNB(Haoli Zhang), 2014/11/5, FR-522167 No WiFi Display Extension Mode

        //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-28,PR1006357 begin
        mContext = movieActivity;
        mConnectivity = new Connectivity(mContext);
        //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-28,PR1006357 end

        mRootView = rootView;
        mVideoView = (MovieVideoView) rootView.findViewById(R.id.surface_view);
        mVideoView.setMoviePlayer(this); ///[BUGFIX]-ADD BY TSNJ.LIUDEKUAN ON 2016/01/12 FOR DEFECT1126560
        mBookmarker = new Bookmarker(movieActivity);

        //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        mUri = info.getUri();
        //mUri = videoUri;
        //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        mController = new MovieControllerOverlay(mContext);
        mController.setUpdateMenuListener((MovieActivity)mContext);
        //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        Log.i("DLNA", "...MoviePlayer ...");
        volumedilaog = new VolumeDialog(mContext);
        LayoutParams wrapContent =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mImageView = new ImageView(mContext);
        mImageView.setImageResource(R.drawable.ic_big_screen);
        mImageView.setScaleType(ScaleType.CENTER);
        mImageView.setVisibility(View.INVISIBLE);
        ((ViewGroup) rootView).addView(mImageView, wrapContent);
        dlnaDuration = 0;
        //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        ((ViewGroup)rootView).addView(mController.getView());
        mController.setListener(this);
        mController.setPlayerControlPanelListener(this);
        mController.setCanReplay(canReplay);

        init(movieActivity, info, canReplay);//[FEATURE]-Add by TCTNB(Haoli Zhang), 2013/10/11, FR-522167 No WiFi Display Extension Mode
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnVideoSizeChangedListener(this);//[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-12, CR938507 begin
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 begin
        mVideoView.setOnShouldPause(new OnShouldPause() {
            @Override
            public void onShouldPauseVideo() {
                mHasPaused = true;
                mController.showPaused();
            }
        });
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 end
        mVideoView.setVideoURI(mUri);

        //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-28,PR1006357 begin
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        mContext.registerReceiver(mConnectivityChangeBroadcastReceiver, filter);
        //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-28,PR1006357 end

        Intent ai = movieActivity.getIntent();
        boolean virtualize = ai.getBooleanExtra(VIRTUALIZE_EXTRA, false);
        if (virtualize) {
            int session = mVideoView.getAudioSessionId();
            if (session != 0) {
                mVirtualizer = new Virtualizer(0, session);
                mVirtualizer.setEnabled(true);
            } else {
                Log.w(TAG, "no audio session to virtualize");
            }
        }
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mController.show();
                return true;
            }
        });
        mSavedInstance = savedInstance; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-19, PR953848
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
                if (!mVideoView.canSeekForward() || !mVideoView.canSeekBackward()) {
                    mController.setSeekable(false);
                    mController.setCanScrubbing(false);
                    mController.setIsLiveStreaming(true);
//                    mController.removePlayPauseReplayView();
                } else {
                    mController.setSeekable(true);
                    mController.setCanScrubbing(true);
                    mController.setIsLiveStreaming(false);
                }
              //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
                setProgress();

                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-27, PR958355 begin
                boolean isLocalFile = MovieUtils.isLocalFile(mMovieItem.getUri(), mMovieItem.getMimeType());
                mController.updateLoopButton(isLocalFile);
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-27, PR958355 end

                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-19, PR953848 begin
                if (mSavedInstance != null) {
                    int shouldPlaying = mSavedInstance.getInt(KEY_CURRENT_VIDEO_STATE);
                    mSavedInstance = null;
                    if (State.values()[shouldPlaying] == State.PLAYING) {
                        playVideo();
                    } else if (State.values()[shouldPlaying] == State.PAUSED) {
                        pauseVideo();
                    }
                }
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-19, PR953848 end
            }
        });

        setOnSystemUiVisibilityChangeListener();
        // Hide system UI by default
        showSystemUi(false);

        mAudioBecomingNoisyReceiver = new AudioBecomingNoisyReceiver();
        mAudioBecomingNoisyReceiver.register();

        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 begin
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-08, PR968503 begin
        if (mController.m3DAudioEnable) {
            mHeadsetPluggedReceiver = new HeadsetPluggedReceiver();
            mHeadsetPluggedReceiver.register();
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-08, PR968503 end
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 end

        if (savedInstance != null) { // this is a resumed activity
            mVideoPosition = savedInstance.getInt(KEY_VIDEO_POSITION, 0);
            mResumeableTime = savedInstance.getLong(KEY_RESUMEABLE_TIME, Long.MAX_VALUE);
            mVideoView.start();
            mVideoView.suspend();
            mHasPaused = true;

            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459 begin
            mLockMode = savedInstance.getInt(KEY_VIDEO_LOCK_STATE, MovieControllerOverlay.LOOP_MODE_OFF);
            mController.updateLock(mLockMode);
            onVideoLockMode(mLockMode);
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459 end
        } else {
            //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
            /*final Integer bookmark = mBookmarker.getBookmark(mUri);
            if (bookmark != null) {
                showResumeDialog(movieActivity, bookmark);
            } else {
                startVideo();
            }*/
            mTState = TState.PLAYING;
            mFirstBePlayed = true;
            final BookmarkerInfo bookmark = mBookmarker.getBookmark(mMovieItem.getUri());
            //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
            if(!DlnaService.isShare){
            if (bookmark != null && canResume) {
                mVideoPosition = bookmark.mBookmark;
                mVideoDuration = bookmark.mDuration;
                showResumeDialog(movieActivity, bookmark);
            } else {
                doStartVideo(false, 0 , 0);
            }
            }
            //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
            //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setOnSystemUiVisibilityChangeListener() {
        if (!ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_HIDE_NAVIGATION) return;

        // When the user touches the screen or uses some hard key, the framework
        // will change system ui visibility from invisible to visible. We show
        // the media control and enable system UI (e.g. ActionBar) to be visible at this point
        mVideoView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                int diff = mLastSystemUiVis ^ visibility;
                mLastSystemUiVis = visibility;
                if ((diff & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                        && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-28,PR791930 begin
                    //[BUGFIX]-Modify by TSNJ,zhe.xu, 2016-01-28,PR 1530887
                    if(mController.mLockMode != mController.LOCK_MODE_ON && !mIsShowResumingDialog){
                        mController.show();
                    }
                    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-28,PR791930 end
                }
            }
        });
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-01-11,PR1356208 begin
    public boolean isHlsResource(){
        boolean isHlsResource = false;
        if (mVideoView != null) {
            isHlsResource = mVideoView.isHlsResource();
        }
        return isHlsResource;
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-01-11,PR1356208 end

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showSystemUi(boolean visible) { // MODIFIED by Yaoyu.Yang, 2016-08-05,BUG-2208330
        if (!ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE) return;

        int flag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102
        if (!visible  && !DlnaService.isShare) {//[FEATURE]-by NJHR(chengqun.sun),
                                                    //2014/11/20, FR-826631 Multi screen interaction
            // We used the deprecated "STATUS_BAR_HIDDEN" for unbundling
            flag |= View.STATUS_BAR_HIDDEN | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        mRootView.setSystemUiVisibility(flag); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_VIDEO_POSITION, mVideoPosition);
        outState.putLong(KEY_RESUMEABLE_TIME, mResumeableTime);
        outState.putInt(KEY_CURRENT_VIDEO_STATE, mController.mState.ordinal()); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-19, PR953848
        outState.putInt(KEY_VIDEO_LOCK_STATE, mLockMode); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459
    }

    private void showResumeDialog(Context context, final BookmarkerInfo bookmark) {
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-02-16, ALM-1532238 begin
        ResumeDialog dialog = new ResumeDialog(context, bookmark);
        dialog.show();
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-02-16, ALM-1532238 end
    }

    public boolean onPause() {
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 begin
        if (!isLocalFile()) {
            mVideoView.setShouldPause(); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-14, PR948961
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 end

        // [FEATURE]-Modify-BEGIN by jian.pan1,11/06/2014, For FR828601 Pop-up Video play
        boolean pause = true;
        //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        // mHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacks(mPlayingChecker);
//        mHandler.removeCallbacks(mRemoveBackground);
        mHandler.removeCallbacks(mProgressChecker);
        mHandler.removeCallbacks(showDilaog);
      //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        //[BUGFIX]-Add-BEGIN by TSNJ.haihua.zhu,12/16/2014,PR872085
        if((mVideoView.getDuration()) > 0){
            mVideoPosition = mVideoView.getCurrentPosition();
            mVideoDuration = mVideoView.getDuration();
            mBookmarker.setBookmark(mUri, mVideoPosition, mVideoDuration);
         }
        //[BUGFIX]-Add-END by TSNJ.haihua.zhu,12/16/2014,PR872085
        doOnPause();
        Log.d(TAG, "onPause() , return " + pause);
        return pause;
        // [FEATURE]-Modify-END by jian.pan1
    }

    //[BUGFIX]-Modify by TCTNJ, hao.yin, 2016-03-08, PR1527064 begin
    private final Runnable mSetVideoViewVisiable = new Runnable() {
        @Override
        public void run() {
           mVideoView.setVisibility(View.VISIBLE);
        }
    };
    //[BUGFIX]-Modify by TCTNJ, hao.yin, 2016-03-08, PR1527064 end

    public void onResume() {
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459 begin
        mController.hide(true);
        showSystemUi(false);
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459 end
        mController.resumePlayPauseReplayView(); // MODIFIED by Yaoyu.Yang, 2016-08-05,BUG-2208330
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-08,PR997159 begin
        if(mController.isLocked()){
            mController.unLock();
            pauseVideo();
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-08,PR997159 end

        //[BUGFIX]-Add-BEGIN by TSNJ.zhe.xu,01/22/2015,PR857286
        if (mIsShowResumingDialog) {
            mHasPaused = false;
        }
        //[BUGFIX]-Add-BEGIN by TSNJ.zhe.xu,01/22/2014,PR857286

      //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        Log.i("DLNA", "MoviePlayer  DlnaService.isShare " + DlnaService.isShare);
        if (mHasPaused && !DlnaService.isShare) {
            Log.i("DLNA", "MoviePlayer  mTState " + mTState);
            mImageView.setVisibility(View.GONE);
      //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
            //[BUGFIX]-Add-BEGIN by TSNJ.peng.tian,12/08/2014,PR857286
            String scheme = mUri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
                   if (System.currentTimeMillis() <= mResumeableTime) {
                       if (mVideoView.isPlaying() || TState.PLAYING == mTState)
                          mController.showLoading();
                   }
            }
            //[BUGFIX]-Add-END by TSNJ.peng.tian
            // [FEATURE]-Add-BEGIN by jian.pan1,11/10/2014,PR833272 Video can't continue to play
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 begin
            mHasPaused = !mVideoView.isPlaying();
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 end
            // [FEATURE]-Add-END by jian.pan1
            mVideoView.seekTo(mVideoPosition);
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-19, PR906557 begin
            mVideoView.resume(isLocalFile());

          //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen,1/10/2015,898610,[Video Streaming]Cannot recover in 5 minutes after pause for more than 1 minutes.
            // If we have slept for too long, pause the play
            if (System.currentTimeMillis() > mResumeableTime
                    && isLocalFile()) {
                pauseVideo();
            }
          //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen,1/10/2015,898610,[Video Streaming]Cannot recover in 5 minutes after pause for more than 1 minutes.
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-19, PR906557 end
        //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    } else if (DlnaService.isShare) {
        Log.i("DLNA", "MoviePlayer isShare, DlnaService.tv_state " + DlnaService.tv_state);
        // modify start by yaping for pr550578
        if (MovieActivity.dlna != null) {
            Log.i("DLNA", " MoviePlayer onResume playing on TV" + MovieActivity.mDlnaUri.toString()
                    + ", "
                    + MovieActivity.dlna.getPreviousFile(mIdentification));
            if (!MovieActivity.mDlnaUri.toString().equals(
                    MovieActivity.dlna
                            .getPreviousFile(mIdentification))) {
                DlnaService.tv_state = TVState.PLAYING;
                DlnaService.positionTV = 0;
                setProgressOnTV();
            } else {
                if (DlnaService.tv_state == TVState.PAUSED) {
                    mController.setTimes((int) DlnaService.positionTV,
                            (int) DlnaService.durationTV, 0, 0);
                }
            }
            // modify end by yaping for pr550578
            // mVideoView.setVisibility(View.INVISIBLE);
            // mImageView.setVisibility(View.VISIBLE);
            // mController.showTVIcon(false);

        }
        //if (mConsumedDrmRight) {
            doStartVideo(true, mVideoPosition, mVideoLastDuration);
        /*} else {
            doStartVideoCareDrm(true, mVideoPosition, mVideoLastDuration);
        }*/
        pauseVideoMoreThanThreeMinutes();
        /*
         * for seekbar cann't seek, shoud setCanScrubbing true;
         */
        if(mOverlayExt != null)
            mOverlayExt.setCanScrubbing(true);
        mController.setPlayPauseReplayResume();
        mHasPaused = false;
    } else {
        // mController.showTVIcon(true);
        mImageView.setVisibility(View.GONE);
        //if (mConsumedDrmRight) {
            /*[BUGFIX]-Add-BEGIN by TCTNB(Jiabao.Wu),2/07/2014,PR-593807
             *[Video]It will play the video while pop up the prompt "resume video".
             *Comment doStartVideo
             */
            //doStartVideo(true, mVideoPosition, mVideoLastDuration);
            //[BUGFIX]-Add-END by TCTNB.Wu Jiabao
        /*} else {
            doStartVideoCareDrm(true, mVideoPosition, mVideoLastDuration);
        }*/
        //pauseVideoMoreThanThreeMinutes();
        mHasPaused = false;
        mIsPlaying = true;
    //[FEATURE]-Add-END by TCTNB.wen.zhuang
    }
        //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        mHandler.postDelayed(mSetVideoViewVisiable, BLACK_TIMEOUT);//[BUGFIX]-Modify by TCTNJ, hao.yin, 2016-03-08, PR1527064
        mHandler.post(mProgressChecker);
    }

    //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    private void pauseVideoMoreThanThreeMinutes() {
        // If we have slept for too long, pause the play
        // If is live streaming, do not pause it too
        long now = System.currentTimeMillis();
        if (now > mResumeableTime// && (LIVE_STREAM != isLiveStreaming()) //temp disabled by chengqun.sun 2014.11.20
                /*&& ExtensionHelper.getMovieStrategy(mActivityContext).shouldEnableCheckLongSleep()*/) {
            if (mVideoCanPause || mVideoView.canPause()) {
                pauseVideo();
            }
        }
        Log.i(TAG, "pauseVideoMoreThanThreeMinutes() now=" + now);
    }
  //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction

    // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014,FR828601 Pop-up Video play
    public void onStop() {
        if (!mHasPaused) {
            doOnPause();
        }
        if (mIsShowResumingDialog) {
            mHasPaused = false;
            Log.d(TAG, "onStop() , mHasPaused " + mHasPaused);
        }
    }

    // [FEATURE]-Add-END by jian.pan1

    public void onDestroy() {
        if (mVirtualizer != null) {
            mVirtualizer.release();
            mVirtualizer = null;
        }
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-3,PR901897  begin
        ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE))
        .abandonAudioFocus(null);
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-02-3,PR901897  end
        mVideoView.stopPlayback();
        mAudioBecomingNoisyReceiver.unregister();
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 begin
        if (mController.m3DAudioEnable) {
            mHeadsetPluggedReceiver.unregister(); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-08, PR968503
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 end

        mContext.unregisterReceiver(mConnectivityChangeBroadcastReceiver);//[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-28,PR1006357
    }

    // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014, For FR828601 Pop-up Video play
    private void doOnPause() {
        int duration = 0;
        addBackground();
        mHasPaused = true;
        // mHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacks(mPlayingChecker);
//        mHandler.removeCallbacks(mRemoveBackground);
        mHandler.removeCallbacks(mProgressChecker);

        duration = mVideoView.getDuration();
        if ((duration > 0)){
            mVideoPosition = mVideoView.getCurrentPosition();
            mVideoDuration = duration;
            mBookmarker.setBookmark(mUri, mVideoPosition, mVideoDuration);
            // [BUGFIX]-Add by TSNJ,zhe.xu, 2015-12-21 for alm 1177313&1177406
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-15,Defect:1445752 begin
            if (!isPlayCompletion) {
                mController.setTimes(mVideoPosition, mVideoDuration, 0 , 0);
            }
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-15,Defect:1445752 end
        }
        mVideoView.suspend();
        mPlaybackStarted = false;
        mResumeableTime = System.currentTimeMillis() + RESUMEABLE_TIMEOUT;
        // The SurfaceView is transparent before drawing the first frame.
        // This makes the UI flashing when open a video. (black -> old screen
        // -> video) However, we have no way to know the timing of the first
        // frame. So, we hide the VideoView for a while to make sure the
        // video has been drawn on it.
        mHandler.removeCallbacks(mSetVideoViewVisiable);//[BUGFIX]-Modify by TCTNJ, hao.yin, 2016-03-08, PR1527064
//        mVideoView.setVisibility(View.INVISIBLE);//[BUGFIX]-Modify by TCTNJ, hao.yin, 2016-03-08, PR1527064
    }
    // [FEATURE]-Add-END by jian.pan1

    // This updates the time bar display (if necessary). It is called every
    // second by mProgressChecker and also from places where the time bar needs
    // to be updated immediately.
    private int setProgress() {
        if (mDragging || !mShowing) {
            return 0;
        }
        //[BUGFIX] -Add by TCTNJ,haihua.zhu,2014-12-16, PR872085 Begin
        //int position = mVideoView.getCurrentPosition();
        //int duration = mVideoView.getDuration();
        int position, duration;
        if(!mVideoView.isPlaying() && !mHasPaused && mVideoPosition > 0 && mVideoDuration > 0 ){
            position = mVideoPosition;
            duration = mVideoDuration;
        }else{
            position = mVideoView.getCurrentPosition();
            duration = mVideoView.getDuration();
          //[BUGFIX] -Add by TCTNJ,ye.chen,2015-06-24, PR1029197 Begin
        }
        //[BUGFIX] -Add by TCTNJ,haihua.zhu,2014-12-16, PR872085 End
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-02-01,PR1541451 begin
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-30,PR1248530 begin
        if(duration < 3000 || !isLocalFile() || !GalleryUtils.hasVideoEditApk(mContext) || isMMSVideo() || is4KVideo()) {//[BUGFIX]-Modify by TSNJ,yuanxi.jiang-nb, 2016/01/28,PR1392879
            mController.mVideoEdit.setVisibility(View.GONE);
        }
        else
            //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/19,PR1401316 begin
            if(!(DrmManager.getInstance().isDrm(ImageLoader.getFilePath(mContext, mUri)))){
                mController.mVideoEdit.setVisibility(View.VISIBLE);
              //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-02,PR1527396 begin
                if (mItem == null) {
                    updateVideoBottom(null,isLocalFile(),true);
                    } else {
                    updateVideoBottom((LocalVideo)mItem,isLocalFile(),false);
                    }
              //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-02,PR1527396 end
            }
            //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/19,PR1401316 begin
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-30,PR1248530 end
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-02-01,PR1541451 end
      //[BUGFIX]-add by TCTNJ,qiang.ding1, 2014-11-26,PR846369 begin
      //When the movie is not get  duration
        if(duration ==-1)
        {
            duration = mReplayDuration;
            position = mReplayPosition;
        }else
        //The movie get duration but do not have position
        {
            if(position ==0)
            {
               position = mReplayPosition;
            }
        }
      //[BUGFIX]-add by TCTNJ,qiang.ding1, 2014-11-26,PR846369 end
      //[BUGFIX]-ADD by TCTNJ,xinrong.wang, 2016-01-27,PR1490107 begin
        mController.setTimes(position, duration, 0, 0);
      //[BUGFIX]-ADD by TCTNJ,xinrong.wang, 2016-01-27,PR1490107 end
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-29, PR1075502 begin
        if (!mVideoView.isPlaying()) {
            return 0;
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-29, PR1075502 end
      //[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-01-27,PR1490107 begin
        //mController.setTimes(position, duration, 0, 0);
      //[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-01-27,PR1490107 end
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-15,Defect:1445752 begin
        isPlayCompletion = false;
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-15,Defect:1445752 end
        return position;
    }

    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    private MediaMetadataRetriever retriever;
    private long videoDuration;
    private void setMediaMetadataSource(Uri uri,Map<String, String> headers) {
        mSeekTo = -1;
        try {
            if (retriever != null) {
                retriever.release();
                retriever = null;
            }
            retriever = new MediaMetadataRetriever();
            if (uri != null) {
                retriever.setDataSource(mContext, uri);
            }
            String duration=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            videoDuration = Long.valueOf(duration)*1000;
        } catch (Exception ex) {
            Log.d(TAG,"setMediaMetadataSource Error:"+ex.toString());
        }
    }

    private void doStartVideo(final boolean enableFasten, final int position, final int duration, boolean start) {
        Log.d(TAG, "doStartVideo(" + enableFasten + ", " + position + ", " + duration + ", " + start + ")");
        //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        if (!DlnaService.isShare) {
        mHandler.removeCallbacks(mPlayingChecker);
        mHandler.postDelayed(mPlayingChecker, 250);
        // For streams that we expect to be slow to start up, show a
        // progress spinner until playback starts.
        String scheme = mMovieItem.getUri().getScheme();
        if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
            mController.showLoading();
            if(mOverlayExt != null) {
                mOverlayExt.setPlayingInfo(isLiveStreaming());
            }
        } else {
            mController.showPlaying();
            mController.hide(true);
        }
        /// M: add play/pause asynchronous processing
        mVideoView.setVideoURI(mMovieItem.getUri());
//      setMediaMetadataSource(mMovieItem.getUri(), null);// MODIFIED by hao.yin, 2016-03-24,BUG-1861307
        if (start) {
            mVideoView.start();
        }

        //we may start video from stopVideo,
        //this case, we should reset canReplay flag according canReplay and loop
        boolean loop = mPlayerExt.getLoop();
        boolean canReplay = loop ? loop : mCanReplay;
        mController.setCanReplay(canReplay);
        if (position > 0 && (mVideoCanSeek || mVideoView.canSeekForward())) {
            mVideoView.seekTo(position);
        }
        if (enableFasten) {
           // mVideoView.setDuration(duration);
        }
      //[BUGFIX]-add by TCTNJ,qiang.ding1, 2014-11-26,PR846369 begain
        mReplayPosition = position;
        mReplayDuration = duration;
      //[BUGFIX]-add by TCTNJ,qiang.ding1, 2014-11-26,PR846369 end
        setProgress();
        } else {
            //[BUGFIX]-Mod-BEGIN by TCTNB.yubin.ying,03/25/2014,608486,
            //mHandler.removeCallbacks(mLiveStreamingChecker); //temp disable by chengqun.sun
            //mHandler.postDelayed(mLiveStreamingChecker, 50); //temp disable by chengqun.sun
            //[BUGFIX]-Mod-END by TCTNB.yubin.ying
            onPlayOnTV();
            if (MovieActivity.mDlnaUri.toString() != null
                    && MovieActivity.dlna != null
                    && !MovieActivity.mDlnaUri.toString()
                            .equals(MovieActivity.dlna
                                    .getPreviousFile(mIdentification))) {
                Log.i("DLNA", "DlnaService.playnext(MovieActivity.mDlnaUri.toString());");
                dlnaDuration = 0;
                DlnaService.playnext(MovieActivity.mDlnaUri.toString());
                //mActivityContext.enhanceActionBar(MovieActivity.mDlnaUri); //temp disabled for FR826631
            }
        }
        //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    }

    private void doStartVideo(boolean enableFasten, int position, int duration) {
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-07,Defect:1476906 begin
//        ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(
//                mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
//                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-07,Defect:1476906 end
        doStartVideo(enableFasten, position, duration, true);
    }
    //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    private void startVideo() {
        // For streams that we expect to be slow to start up, show a
        // progress spinner until playback starts.
        String scheme = mUri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
            mController.showLoading();
            mHandler.removeCallbacks(mPlayingChecker);
            mHandler.postDelayed(mPlayingChecker, 250);
        } else {
            mController.showPlaying();
            mController.hide(true);
        }
        mVideoView.start();
        setProgress();
    }

    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 begin
    public void playVideo() {
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 end
        Log.i("MovieTAG", "-->MoviePlayer playVideo() start");
        mVideoView.start();
        Log.i("MovieTAG", "-->MoviePlayer playVideo() end");
        //[BUGFIX] -Add by TCTNJ,haihua.zhu,2014-12-16, PR872085 Begin
        mHasPaused = false;
        //[BUGFIX] -Add by TCTNJ,haihua.zhu,2014-12-16, PR872085 End
        mController.showPlaying();
        setProgress();
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 begin
        //if (mAudioManager != null) {
        //    mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
        //            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        //}
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 end
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-08,PR997159
    public void pauseVideo() {
        //[BUGFIX] -Add by TCTNJ,haihua.zhu,2014-12-16, PR872085 Begin
        mHasPaused = true;
        //[BUGFIX] -Add by TCTNJ,haihua.zhu,2014-12-16, PR872085 End
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-4,ALM-1711661 begin
        mTState = TState.PAUSED;
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-4,ALM-1711661 end
        mVideoView.pause();
        mController.showPaused();
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 begin
        //if (mAudioManager != null) {
        //    mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        //}
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 end
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end
    }

  //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode

    public void mPauseVideo() {
        pauseVideo();
    }

    //[BUGFIX]-Add-Begin by TSNJ,zhe.xu,2016-01-04, alm 1040157
    public void mPauseVideo4Call() {
        if (mVideoView.isPlaying()) {
            mController.setTimes(mVideoView.getCurrentPosition(), mVideoView.getDuration(), 0 , 0);
            mVideoPosition = mVideoView.getCurrentPosition();
            pauseVideo();
            mController.unLock();
        }
    }
    //[BUGFIX]-Add-End by TSNJ,zhe.xu,2016-01-04

    /* @hide */
    private void updateRememberedData() {
        mVideoPosition = mVideoView.getCurrentPosition();
        mVideoDuration = mVideoView.getDuration();
    }
  //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode

    // Below are notifications from VideoView
    @Override
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        mMovieItem.setError();
        if (mRetryExt.onError(player, arg1, arg2)) {
            return true;
        }
        //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        mHandler.removeCallbacksAndMessages(null);
      //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        mHandler.post(mProgressChecker);//always show progress
        //M:resume controller
        mController.setViewEnabled(true);
      //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        // VideoView will show an error dialog if we return false, so no need
        // to show more message.
        mController.showErrorMessage("");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-26,PR841080 begin
        int duration = mVideoView.getDuration();
        mController.setTimes(duration, duration, 0, 0);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-15,Defect:1445752 begin
        isPlayCompletion = true;
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-15,Defect:1445752 end
        if(this.mLoopMode == MovieControllerOverlay.LOOP_MODE_ON) {
            // Should be wait 250ms,to ensure TimeBar has been updated.
          //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-07-15,PR1043100 begin
            if(DrmManager.getInstance().isDrm(ImageLoader.getFilePath(mContext, mUri)) &&
                    !DrmManager.getInstance().isRightsStatus(ImageLoader.getFilePath(mContext, mUri))){
                Toast.makeText(mContext, R.string.drm_no_valid_right, Toast.LENGTH_SHORT).show();
                mController.showEnded();
                onCompletion();
            }else{
                mHandler.postDelayed(mRePlay, 250);
            }
          //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-07-15,PR1043100 begin
        } else {
            mController.showEnded();
            onCompletion();
        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-26,PR841080 end
    }

    public void onCompletion() {
    }

    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-26,PR841080 begin
    private final Runnable mRePlay = new Runnable() {
        @Override
        public void run() {
            onReplay();
        }
    };
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-26,PR841080 end
    //[FEATURE]-Add-BEGIN by NJHR.chengqun.sun,20/11/2014,FR-826631,
    public enum TVState {
        LOADING, PLAYING, PAUSED, COMPELTED
    }

    // private String strUuid;
    private int tvState;

    private void onPlayPauseTV() {
        Log.i("DLNA", "------- onPlayPauseTV---");
        //DlnaService.stopGetPositionTV();// delete by yaping.liu for dlna
        AsyncTask<Integer, Void, String> asy = new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer... params) {
                if (MovieActivity.dlna != null) {
                    tvState = MovieActivity.dlna.mediaControlGetPlayState(mIdentification);
                    Log.i("DLNA", "------- onPlayPauseTV, DlnaService.TVstate :" + tvState);
                    if (tvState != 1) {
                        MovieActivity.dlna.mediaControlPlay(mIdentification);
                    } else {
                        MovieActivity.dlna.mediaControlPause(mIdentification);
                    }
                }
                return null;
            }

            protected void onPostExecute(String result) {
                if (tvState != 1) {
                    //DlnaService.getPositionTV();// delete by yaping.liu for dlna
                    mController.showPlaying();
                    DlnaService.tv_state = TVState.PLAYING;
                    setProgressOnTV();
                } else {
                    mController.showPaused();
                    DlnaService.tv_state = TVState.PAUSED;
                }
                super.onPostExecute(result);
            }
        };
        asy.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onPlayOnTV() {
        mController.hideTimer();
        if (DlnaService.tv_state == TVState.PAUSED) {
            mController.showPaused();
        } else {
            mController.showPlaying();
        }
        mVideoView.stopPlayback();
        mVideoView.setVisibility(View.GONE);
        onShownforTV(true);
    }

    public void onStopOnTV() {
        if (DlnaService.isShare) {
            DlnaService.onTVStop(mContext);
            Uri uri = mMovieItem.getUri();
            Uri mTvUri;
            File file = new File(uri.toString());
            if (!file.exists()) {
                mTvUri = uri;
            } else {
                mTvUri = Uri.fromFile(file);
            }
            /*[BUGFIX]-Add-BEGIN by TCTNB(Jiabao.Wu),01/09/2014,PR-549891
             *[Streaming]Will prompt can't play this video when play some SDP files
             */
            //mVideoView.setVideoURI(mTvUri, mMime, !mWaitMetaData); //temp disabled for FR826631
            //[BUGFIX]-Add-END by TCTNB.Wu Jiabao
            mVideoView.start();
            mController.mState = State.PLAYING;
            mVideoView.seekTo((int) DlnaService.positionTV);
            mVideoView.setVisibility(View.VISIBLE);
        }
        onShownforTV(false);
        DlnaService.tv_state = TVState.PAUSED;
    }

    //[BUGFIX]-Mod-BEGIN by TCTNB.yubin.ying,04/04/2014,624747,
    //private int duration;

    //private long mSeekFirstTime = 0;

    private boolean mIsSeek = false;

    private ProgressDialog mloadingDialog = null;
    //[BUGFIX]-Mod-END by TCTNB.yubin.ying

    private int setProgressOnTV() {
        if (mDragging || !mShowing) {
            return 0;
        }
        Log.i("DLNA", "DlnaService.positionTV: " + DlnaService.positionTV
                + " durationTV: " + DlnaService.durationTV);
        //[BUGFIX]-Mod-BEGIN by TCTNB.yubin.ying,04/04/2014,624747,
        //if (mIsSeek && Math.abs(mDLNASeekTime - DlnaService.positionTV) > 5000) {
        //    if (mSeekFirstTime != 0) {
        //        mDLNASeekTime += DlnaService.positionTV - mSeekFirstTime;
        //    }
        //    mSeekFirstTime = DlnaService.positionTV;
        //} else {
        //    mIsSeek = false;
        //    mDLNASeekTime = DlnaService.positionTV;
        //    mSeekFirstTime = 0;
        //}

        if (mIsSeek) {
            return (int) DlnaService.positionTV;
        }

        if (DlnaService.positionTV > 0 && DlnaService.durationTV <= 0) {
            if (dlnaDuration == 0) {
                getVideoDurationOnTV();
            }
            mController.setTimes((int) DlnaService.positionTV,
                    (int) dlnaDuration, 0, 0);
            //mController.mTimeBar.setScrubbing(false); //temp disabled for FR826631
        } else {
            mController.setTimes((int) DlnaService.positionTV,
                    (int) DlnaService.durationTV, 0, 0);
            //[BUGFIX]-Mod-END by TCTNB.yubin.ying
        }
        return (int) DlnaService.positionTV;
    }

    private MediaMetadataRetriever dlnaRetriever;
    private long dlnaDuration = 0;

    /**
     * if positionTV > 0 and durationTV == 0, to get duration by getVideoDurationOnTV
     */
    private void getVideoDurationOnTV() {
        try {
            if (dlnaRetriever != null) {
                dlnaRetriever.release();
                dlnaRetriever = null;
            }
            dlnaRetriever = new MediaMetadataRetriever();
            Uri uri = null;
            if (MovieActivity.dlna != null && MovieActivity.dlna.getPreviousFile(mIdentification) != null) {
                uri = Uri.parse(MovieActivity.dlna.getPreviousFile(mIdentification));
            }
            if (uri != null) {
                dlnaRetriever.setDataSource(mContext, uri);
            }
            String duration=dlnaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            dlnaDuration = Long.valueOf(duration);
        } catch (Exception ex) {
            Log.e(TAG,"getVideoDurationOnTV Error:"+ex.toString());
        }
    }

    private void onShownforTV(boolean showingOnTV) {
        Log.i("DLNA", " --- onShownforTV :" + showingOnTV);
        if (showingOnTV) {
            mImageView.setVisibility(View.VISIBLE);
            if(mOverlayExt != null)
                mOverlayExt.setBottomPanel(false, false);
            mShowing = true;
            mController.showTVIcon(false);
        } else {
            mImageView.setVisibility(View.GONE);
            mShowing = true;
            mController.showTVIcon(true);
        }
        mHandler.removeCallbacks(mProgressChecker);
        mHandler.post(mProgressChecker);
        showSystemUi(true);
    }
    //[FEATURE]-Add-END by NJHR.chengqun.sun,20/11/2014,FR-826631,

    // Below are notifications from ControllerOverlay
    @Override
    public void onPlayPause() {
        //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        Log.i("DLNA", "---onPlayPause   DlnaService.isShare: "
                + DlnaService.isShare);
        if (!DlnaService.isShare) { // if play on local
            if (mVideoView.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
                mController.setViewEnabled(false);
            }
        }else {// if play on TV
            onPlayPauseTV();
        }
        //[FEATURE]-Mod-END by TCTNB.wen.zhuang

    }

    @Override
    public void onSeekStart() {
        mDragging = true;
    }

    @Override
    public void onSeekMove(int time) {
        Log.d(TAG, "onSeekMove(" + time + ") mDragging=" + mDragging);
        if (!DlnaService.isShare && !mDragging && !("rtsp".equalsIgnoreCase(mUri.getScheme()))) {
          //[FEATURE]-by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        mVideoView.seekTo(time);
    }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-14,PR1061587 begin
        mController.setTimes(time, mVideoView.getDuration(), 0, 0);
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-14,PR1061587 end
    }

  //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    @Override
    public void onSeekEnd(final int time, int start, int end) {
        if (MovieActivity.dlna != null && MovieActivity.dlna.hasConnected() && DlnaService.isShare) {
            //[BUGFIX]-Mod-BEGIN by TCTNB.yubin.ying,04/04/2014,624747,
            //mDLNASeekTime = time - 1000;// add by yaping.liu
            mIsSeek = true;// add by yaping.liu
            if (mloadingDialog == null) {
                mloadingDialog = new android.app.ProgressDialog(mActivityContext,
                        AlertDialog.THEME_TRADITIONAL);
                mloadingDialog.setCancelable(true);
                mloadingDialog.setCanceledOnTouchOutside(false);
                mloadingDialog.setMessage(mActivityContext.getString(R.string.wait_dialog));
            }
            if (!mloadingDialog.isShowing()) {
                mloadingDialog.show();
            }
            //[BUGFIX]-Mod-BEGIN by TCTNB.yubin.ying,04/25/2014,624747,
            //mController.mTimeBar.setEnableScrube(false); //temp disabled for FR826631
            AsyncTask<Integer, Void, String> asy = new AsyncTask<Integer, Void, String>() {
    @Override
                protected String doInBackground(Integer... params) {
                    // TODO Auto-generated method stub
                    MovieActivity.dlna.mediaControlSeek((long) time,
                            mIdentification);
                    Log.i("DLNA", "---time :" + time);
                    return null;
                }

                protected void onPostExecute(String result) {
                    mDragging = false;
                    // DlnaService.positionTV = time;// delete by yaping.liu
                    Log.i("DLNA", "---mDragging :" + time);
                    super.onPostExecute(result);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mloadingDialog != null && mloadingDialog.isShowing()) {
                        mloadingDialog.dismiss();
                    }
                    mIsSeek = false;
                    //mController.mTimeBar.setEnableScrube(true);//temp disabled for FR826631
                    //[BUGFIX]-Mod-END by TCTNB.yubin.ying
                }
            };

            asy.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
        mDragging = false;
        mVideoView.seekTo(time);
        setProgress();
    }
      //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    }

    @Override
    public void onShown() {
        mShowing = true;
        setProgress();
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
        if (mLockMode == MovieControllerOverlay.LOCK_MODE_OFF) {
            showSystemUi(true);
        } else {
            mController.show();
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end
    }

    @Override
    public void onHidden() {
        mShowing = false;
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
        if (mLockMode == MovieControllerOverlay.LOCK_MODE_OFF) {
            showSystemUi(false);
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end
    }

    @Override
    public void onReplay() {
        startVideo();
    }

    @Override
    public void onLoopMode(int mode) {
        mLoopMode = mode;
    }

    public void onFullScreenMode(int mode) {
        mVideoView.requestScreenMode(mode);
        if(mode == MovieControllerOverlay.FULLSCREEN_MODE_ON) {
            //mController.hide();
        }
    }

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
    @Override
    public void onVideoLockMode(int mode) {
        mLockMode = mode;

        boolean flag = (mLockMode == MovieControllerOverlay.LOCK_MODE_OFF);
        showSystemUi(flag);
        mController.mNeedPanel = flag;
        //[BUGFIX]-Modify by TCTNJ, xinrong.wang, 2016-01-06, PR1048407 begin
        //mController.requestLayout();
        //[BUGFIX]-Modify by TCTNJ, xinrong.wang, 2016-01-06, PR1048407 end

    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 begin
    @Override
    public void onVideoFastForwardOrBackward(boolean isForward) {
        int position = mVideoView.getCurrentPosition();
        int duration = mVideoView.getDuration();
        if (duration > 0 && position >= 0 && (mVideoView.canSeekForward() || mVideoView.canSeekBackward())) {
            int delta = INTERVAL_TIME_FOR_GESTURE;
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-24,Defect:1126670
            boolean isCompletion = false;
            if (isForward) {
                if (position + INTERVAL_TIME_FOR_GESTURE > duration) {
                    delta = duration - position;
                    position = duration;
                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-24,Defect:1126670
                    isCompletion = true;
                } else {
                    position += delta;
                }
            } else {
                if (position - delta < 0) {
                    delta = position;
                    position = 0;
                } else {
                    position -= delta;
                }
            }
            mReplayPosition = position;
            mReplayDuration = duration;
            mVideoPosition = position;
            mVideoDuration = duration;
            mVideoView.seekTo(position);
            setProgress();
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-24,Defect:1126670 begin
            if (isCompletion) {
                this.onCompletion();
            } else {
                mController.showTimeBar(isForward, delta);
            }
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-24,Defect:1126670 end
        }
    }

    @Override
    public void updatePlayerControlShowState(boolean showing) {
        mShowing = showing;
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 end

    // Below are key events passed from MovieActivity.
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.i("MovieTAG", "->onKeyDown in keyCode = " + keyCode);
        // Some headsets will fire off 7-10 events on a single click
      //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        if ((event.getRepeatCount() > 0)
                && !(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            return isMediaKey(keyCode);
        }
        if ((event.getEventTime() - event.getDownTime()) > 0)
        {
            isLongPress = true;
        }
      //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mVideoView.isPlaying()) {
                    pauseVideo();
                } else {
                    playVideo();
                }
                //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-15,PR1019479 begin
                mController.showplaypause();
                //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-15,PR1019479 end
                return true;
            case KEYCODE_MEDIA_PAUSE:
                if (mVideoView.isPlaying()) {
                    pauseVideo();
                }
                //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-15,PR1019479 begin
                mController.showplaypause();
                //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-15,PR1019479 end
                Log.i("MovieTAG", "=>onKeyDown in keyCode = " + keyCode+" pauseVideo");
                return true;
            case KEYCODE_MEDIA_PLAY:
                if (!mVideoView.isPlaying()) {
                    playVideo();
                }
                //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-15,PR1019479 begin
                mController.showplaypause();
                //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-15,PR1019479 end
                Log.i("MovieTAG", "->onKeyDown in keyCode = " + keyCode+" playVideo");
                return true;
                //[FEATURE]-Add-BEGIN by TCTNB.wen.zhuang,12/10/2013,FR-550507,
                case KeyEvent.KEYCODE_VOLUME_DOWN: // xuxr dlna
                    if (MovieActivity.dlna != null && DlnaService.isShare) {
                        if (!isLongPress && !volumedilaog.isShowing()) {
                            AsyncTask<Integer, Void, String> asyDown = new AsyncTask<Integer, Void, String>() {
                                @Override
                                protected String doInBackground(Integer... params) {
                                    // TODO Auto-generated method stub
                                    volume = MovieActivity.dlna
                                            .mediaControlGetVolume(mIdentification);
                                    volume = volume > mVolumeStep ? volume - mVolumeStep : 0;
                                    mHandler.post(showDilaog);
                                    return null;
                                }
                            };
                            asyDown.execute();
                        } else {
                            volume = volumedilaog.getProgress() > mVolumeStep ? volumedilaog
                                    .getProgress() - mVolumeStep : 0;
                            mHandler.post(showDilaog);
                        }
                        return true;
                    } else {
                        return false;
                    }

                case KeyEvent.KEYCODE_VOLUME_UP: // xuxr dlna
                    if (MovieActivity.dlna != null && DlnaService.isShare) {
                        if (!isLongPress && !volumedilaog.isShowing()) {
                            AsyncTask<Integer, Void, String> asyUp = new AsyncTask<Integer, Void, String>() {
                                @Override
                                protected String doInBackground(Integer... params) {
                                    // TODO Auto-generated method stub
                                    volume = MovieActivity.dlna
                                            .mediaControlGetVolume(mIdentification);
                                    volume = volume < 100 - mVolumeStep ? volume + mVolumeStep : 100;
                                    mHandler.post(showDilaog);
                                    return null;
                                }
                            };
                            asyUp.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            volume = volumedilaog.getProgress() < 100 - mVolumeStep ? volumedilaog
                                    .getProgress() + mVolumeStep : 100;
                            mHandler.post(showDilaog);
                        }
                        return true;
                    } else {
                        return false;
                    }
                //[FEATURE]-Add-END by TCTNB.wen.zhuang
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                // TODO: Handle next / previous accordingly, for now we're
                // just consuming the events.
                return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
      //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        Log.i("DLNA", "-------------onKeyUp---------");
        if (MovieActivity.dlna != null && DlnaService.isShare) { // xuxr dlna
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (volumedilaog.isShowing()) {
                        AsyncTask<Integer, Void, String> asyVolume = new AsyncTask<Integer, Void, String>() {
                            @Override
                            protected String doInBackground(Integer... params) {
                                // TODO Auto-generated method stub
                                MovieActivity.dlna
                                        .mediaControlSetVolume(
                                                volumedilaog.getProgress(),
                                                mIdentification);
                                return null;
                            }
                        };
                        asyVolume.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        return true;
                    } else {
                        return false;
                    }

                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    return true;
            }
            return false;
        } else {
        return isMediaKey(keyCode);
    }
      //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    }

    private static boolean isMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS
                || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE;
    }

    // We want to pause when the headset is unplugged.
    private class AudioBecomingNoisyReceiver extends BroadcastReceiver {

        public void register() {
            mContext.registerReceiver(this,
                    new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }

        public void unregister() {
            mContext.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mVideoView.isPlaying()) {
                //[BUGFIX]-Add by TSNJ,zhe.xu, 2015-12-16, alm-1158414
                mController.setTimes(mVideoView.getCurrentPosition(), mVideoView.getDuration(), 0 , 0);
                pauseVideo();
                mController.unLock();//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-08,PR997159
            }

            Log.i(TAG, "headset disconnected");
            mController.show3DAudioIcon(); //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977
        }
    }

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-08, PR968503 begin
    private class HeadsetPluggedReceiver extends BroadcastReceiver {
        public void register() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
            intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            mContext.registerReceiver(this, intentFilter);
        }

        public void unregister() {
            mContext.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioManager.ACTION_HEADSET_PLUG.equals(action)) {
                int state = intent.getIntExtra("state", 0);
                if (state == 1) {
                    Log.i(TAG, "headset plugged in");
                    mController.hide3DAudioIcon();
                }
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0);
                Log.i(TAG, "bluetooth connection state = " + state);
                if (state == BluetoothAdapter.STATE_CONNECTED) {
                    Log.i(TAG, "bluetooth connected");
                    mController.hide3DAudioIcon();
                }
            }
        }
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-08, PR968503 end

//[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
//[Ergo] Video player
/// M: for log flag, if set this false, will improve run speed.
  //[FEATURE]-by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
public static MovieActivity mActivityContext;//for dialog and toast context
private boolean mFirstBePlayed = false;//for toast more info

private void init(final MovieActivity movieActivity, IMovieItem info, boolean canReplay) {
    mActivityContext = movieActivity;
    mCanReplay = canReplay;
    mMovieItem = info;
    judgeStreamingType(info.getUri(), info.getMimeType());
    mUri = mMovieItem.getUri();
    //for toast more info and live streaming
    mVideoView.setOnInfoListener(this);
    mVideoView.setOnPreparedListener(this);
    //mVideoView.setOnBufferingUpdateListener(this);
    //mVideoView.setOnVideoSizeChangedListener(this);

    mRootView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mController.show();
            return true;
        }
    });
    //mOverlayExt = mController.getOverlayExt();
}

public IMoviePlayer getMoviePlayerExt() {
    return mPlayerExt;
}

public SurfaceView getVideoSurface() {
    return mVideoView;
}

/// M: for more detail in been killed case
private static final String KEY_VIDEO_CAN_SEEK = "video_can_seek";
private static final String KEY_VIDEO_CAN_PAUSE = "video_can_pause";
private static final String KEY_VIDEO_LAST_DURATION = "video_last_duration";
private static final String KEY_VIDEO_STATE = "video_state";
private static final String KEY_VIDEO_STREAMING_TYPE = "video_streaming_type";

private int mVideoLastDuration;//for duration displayed in init state
private boolean mVideoCanPause = false;
private boolean mVideoCanSeek = false;

private void onSaveInstanceStateMore(Bundle outState) {
    //for more details
    outState.putInt(KEY_VIDEO_LAST_DURATION, mVideoLastDuration);
    outState.putBoolean(KEY_VIDEO_CAN_PAUSE, mVideoView.canPause());
    outState.putBoolean(KEY_VIDEO_CAN_SEEK, mVideoView.canSeekForward());
    outState.putInt(KEY_VIDEO_STREAMING_TYPE, mStreamingType);
    outState.putString(KEY_VIDEO_STATE, String.valueOf(mTState));
    mScreenModeExt.onSaveInstanceState(outState);
    mRetryExt.onSaveInstanceState(outState);
    mPlayerExt.onSaveInstanceState(outState);
    Log.d(TAG, "onSaveInstanceState(" + outState + ")");
}

private void onRestoreInstanceState(Bundle icicle) {
    mVideoLastDuration = icicle.getInt(KEY_VIDEO_LAST_DURATION);
    mVideoCanPause = icicle.getBoolean(KEY_VIDEO_CAN_PAUSE);
    mVideoCanSeek = icicle.getBoolean(KEY_VIDEO_CAN_SEEK);
    mStreamingType = icicle.getInt(KEY_VIDEO_STREAMING_TYPE);
    mTState = TState.valueOf(icicle.getString(KEY_VIDEO_STATE));

    mScreenModeExt.onRestoreInstanceState(icicle);
    mRetryExt.onRestoreInstanceState(icicle);
    mPlayerExt.onRestoreInstanceState(icicle);
    Log.d(TAG, "onRestoreInstanceState(" + icicle + ")");
}

private void clearVideoInfo() {
    Log.d(TAG," clearVideoInfo()...");
    mVideoPosition = 0;
    mVideoLastDuration = 0;
    mIsOnlyAudio = false;
}

private void getVideoInfo(MediaPlayer mp) {
    if (!MovieUtils.isLocalFile(mMovieItem.getUri(), mMovieItem.getMimeType())) {
        /*Metadata data = mp.getMetadata(MoviePlayer.METADATA_ALL,
                MoviePlayer.BYPASS_METADATA_FILTER);
        if (data != null) {
            mPlayerExt.setVideoInfo(data);
        } else {
            Log.w(TAG, "Metadata is null!");
        }*/
        int duration = mp.getDuration();
        if (duration <= 0) {
            mStreamingType = STREAMING_SDP;//correct it
        } else {
            //correct sdp to rtsp
            if (mStreamingType == STREAMING_SDP) {
                mStreamingType = STREAMING_RTSP;
            }
        }
        Log.d(TAG, "getVideoInfo() duration=" + duration + ", mStreamingType=" + mStreamingType);
    }
}

@Override
public void onPrepared(MediaPlayer mp) {
    Log.d(TAG, "onPrepared(" + mp + ")");
    getVideoInfo(mp);
    if (!isLocalFile()) { //hear we get the correct streaming type.
        if(mOverlayExt != null)
            mOverlayExt.setPlayingInfo(isLiveStreaming());
    }
    boolean canPause = mVideoView.canPause();
    boolean canSeek = mVideoView.canSeekBackward() && mVideoView.canSeekForward();
    if(mOverlayExt != null) {
        mOverlayExt.setCanPause(canPause);
        mOverlayExt.setCanScrubbing(canSeek);
    }
    //resume play pause button (play/pause asynchronous processing)
    mController.setPlayPauseReplayResume();
   // if (!canPause && !mVideoView.isTargetPlaying()) {
    if (!canPause) {
        mVideoView.start();
    }

    if (!mCanResume) {
        mActivityContext.seekPauseVideo();
    }
}

@Override
public boolean onInfo(MediaPlayer mp, int what, int extra) {
    Log.d(TAG, "onInfo() what:" + what + " extra:" + extra);
    if (mRetryExt.onInfo(mp, what, extra)) {
        return true;
    }
    if (mFirstBePlayed && what == MoviePlayer.MEDIA_INFO_VIDEO_NOT_SUPPORTED) {
        Toast.makeText(mActivityContext, R.string.videoView_info_text_video_not_supported, Toast.LENGTH_SHORT).show();
        mFirstBePlayed = false;
        return true;
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2014-12-03,PR858861 begin
    if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
        mController.showLoading();
        return true;
    } else if(what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-19, PR906557 begin
        if (mVideoView.isPlaying()) {
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-11,PR1623761 begin
            if (mHasPaused) {
                pauseVideo();
            } else {
                mController.showPlaying();
            }
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-11,PR1623761 end
            if(sizeChanged) {
                if(mp.getDuration() <= 0 && mVideoWidth == 0 && mVideoHeight == 0) {
                    sizeChanged = false;
                    mVideoView.setBackgroundResource(R.drawable.media_default_bkg);
                } else {
                    mVideoView.setBackgroundDrawable(null);
                    mVideoView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        } else {
            mController.showPaused();
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-19, PR906557 end
        return true;
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du, 2014-12-03,PR858861 end

    return false;
}

@Override
public void onBufferingUpdate(MediaPlayer mp, int percent) {
    if (!mPlayerExt.pauseBuffering()) {
        boolean fullBuffer = isFullBuffer();
        if(mOverlayExt != null)
            mOverlayExt.showBuffering(fullBuffer, percent);
    }
    Log.d(TAG, "onBufferingUpdate(" + percent + ") pauseBuffering=" + mPlayerExt.pauseBuffering());
}

/// M: for streaming feature
//judge and support sepcial streaming type
public static final int STREAMING_LOCAL = 0;
public static final int STREAMING_HTTP = 1;
public static final int STREAMING_RTSP = 2;
public static final int STREAMING_SDP = 3;

private boolean mWaitMetaData;
private int mStreamingType = STREAMING_LOCAL;
private boolean mCanReplay;

private void judgeStreamingType(Uri uri, String mimeType) {
    Log.d(TAG, "judgeStreamingType(" + uri + ")");
    if (uri == null) {
        return;
    }
    String scheme = uri.getScheme();
    mWaitMetaData = true;
    if (MovieUtils.isSdpStreaming(uri, mimeType)) {
        mStreamingType = STREAMING_SDP;
    } else if (MovieUtils.isRtspStreaming(uri, mimeType)) {
        mStreamingType = STREAMING_RTSP;
    } else if (MovieUtils.isHttpStreaming(uri, mimeType)) {
        mStreamingType = STREAMING_HTTP;
        mWaitMetaData = false;
    } else {
        mStreamingType = STREAMING_LOCAL;
        mWaitMetaData = false;
    }
    Log.d(TAG, "mStreamingType=" + mStreamingType + " mCanGetMetaData=" + mWaitMetaData);
}

//[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-28,PR1006357 begin
public boolean isNetworkStreaming() {
    boolean isNetworkStreaming = false;
    if(mStreamingType == STREAMING_HTTP
        || mStreamingType == STREAMING_RTSP
        || mStreamingType == STREAMING_SDP) {
        isNetworkStreaming = true;
    }
    Log.d(TAG, "network streaming " + mStreamingType);
    return isNetworkStreaming;
}
//[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-05-28,PR1006357 end

public boolean isFullBuffer() {
    if (mStreamingType == STREAMING_RTSP || mStreamingType == STREAMING_SDP) {
        return false;
    }
    return true;
}

public boolean isLocalFile() {
    if (mStreamingType == STREAMING_LOCAL) {
        return true;
    }
    return false;
}

//[BUGFIX]-Modify by TSNJ,yuanxi.jiang-nb, 2016/01/28,PR1392879 begin
public boolean isMMSVideo() {
	if (mUri.toString().startsWith("content://mms/")) {
		return true;
	} else {
		return false;
	}
}
//[BUGFIX]-Modify by TSNJ,yuanxi.jiang-nb, 2016/01/28,PR1392879 end

public boolean isLiveStreaming() {
    boolean isLive = false;
    if (mStreamingType == STREAMING_SDP) {
        isLive = true;
    }
    Log.d(TAG, "isLiveStreaming() return " + isLive);
    return isLive;
}

private int mVideoWidth = 0;
private int mVideoHeight = 0;
private boolean sizeChanged = false;
/// M: for dynamic change video size(http live streaming)
private boolean mIsOnlyAudio = false;
public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    sizeChanged = true;
    mVideoWidth = width;
    mVideoHeight = height;
    //reget the audio type
    boolean isPlaying = mVideoView.isPlaying();
    if(width == 0 && height == 0 && isPlaying ) {
        mIsOnlyAudio = true;
    } else {
        mIsOnlyAudio = false;
    }
    Log.d(TAG, "onVideoSizeChanged(" + width + ", " + height + ") mIsOnlyAudio=" + mIsOnlyAudio);

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-27,PR986309 begin
    if (mIsOnlyAudio) {
        mVideoView.setBackgroundResource(R.drawable.media_default_bkg);
    } else {
        mVideoView.setBackgroundDrawable(null);
        mVideoView.setBackgroundColor(Color.TRANSPARENT);
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-27,PR986309 end
}

//for more killed case, same as videoview's state and controller's state.
//will use it to sync player's state.
//here doesn't use videoview's state and controller's state for that
//videoview's state doesn't have reconnecting state and controller's state has temporary state.
private enum TState {
    PLAYING,
    PAUSED,
    STOPED,
    COMPELTED,
    RETRY_ERROR
}

private TState mTState = TState.PLAYING;
private IMovieItem mMovieItem;
private RetryExtension mRetryExt = new RetryExtension();
private ScreenModeExt mScreenModeExt = new ScreenModeExt();
private MoviePlayerExtension mPlayerExt = new MoviePlayerExtension();
private IContrllerOverlayExt mOverlayExt;

interface Restorable {
    void onRestoreInstanceState(Bundle icicle);
    void onSaveInstanceState(Bundle outState);
}

private class RetryExtension implements Restorable, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {
    private static final String KEY_VIDEO_RETRY_COUNT = "video_retry_count";
    private int mRetryDuration;
    private int mRetryPosition;
    private int mRetryCount;
    public void retry() {
        doStartVideo(true, mRetryPosition, mRetryDuration);
        Log.d(TAG, "retry() mRetryCount=" + mRetryCount + ", mRetryPosition=" + mRetryPosition);
    }

    public void clearRetry() {
        Log.d(TAG, "clearRetry() mRetryCount=" + mRetryCount);
        mRetryCount = 0;
    }

    public boolean reachRetryCount() {
        Log.d(TAG, "reachRetryCount() mRetryCount=" + mRetryCount);
        if (mRetryCount > 3) {
            return true;
        }
        return false;
    }

    public int getRetryCount() {
        Log.d(TAG, "getRetryCount() return " + mRetryCount);
        return mRetryCount;
    }

    public boolean isRetrying() {
        boolean retry = false;
        if (mRetryCount > 0) {
            retry = true;
        }
        Log.d(TAG, "isRetrying() mRetryCount=" + mRetryCount);
        return retry;
    }

    @Override
    public void onRestoreInstanceState(Bundle icicle) {
        mRetryCount = icicle.getInt(KEY_VIDEO_RETRY_COUNT);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_VIDEO_RETRY_COUNT, mRetryCount);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (what == MoviePlayer.MEDIA_ERROR_CANNOT_CONNECT_TO_SERVER) {
            //get the last position for retry
            mRetryPosition = mVideoView.getCurrentPosition();
            mRetryDuration = mVideoView.getDuration();
            mRetryCount++;
            if (reachRetryCount()) {
                mTState = TState.RETRY_ERROR;
                if(mOverlayExt != null)
                    mOverlayExt.showReconnectingError();
            } else {
                if(mOverlayExt != null)
                    mOverlayExt.showReconnecting(mRetryCount);
                retry();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MoviePlayer.MEDIA_INFO_GET_BUFFER_DATA) {
            //this means streaming player has got the display data
            //so we can retry connect server if it has connection error.
            clearRetry();
            return true;
        /// M: receive PAUSE PLAY COMPLETED info
        //play/pause asynchronous processing
        } else if (what == MoviePlayer.MEDIA_INFO_PAUSE_COMPLETED
                || what == MoviePlayer.MEDIA_INFO_PLAY_COMPLETED) {
            Log.d(TAG, "onInfo is PAUSE PLAY COMPLETED");
            mController.setViewEnabled(true);
        }
        return false;
    }

    public boolean handleOnReplay() {
        if (isRetrying()) { //from connecting error
            clearRetry();
            int errorPosition = mVideoView.getCurrentPosition();
            int errorDuration = mVideoView.getDuration();
            doStartVideo(errorPosition > 0, errorPosition, errorDuration);
            Log.d(TAG, "onReplay() errorPosition=" + errorPosition + ", errorDuration=" + errorDuration);
            return true;
        }
        return false;
    }

    public void showRetry() {
        if(mOverlayExt != null)
            mOverlayExt.showReconnectingError();
        if (mVideoCanSeek || mVideoView.canSeekForward()) {
            mVideoView.seekTo(mVideoPosition);
        }
     //   mVideoView.setDuration(mVideoLastDuration);
        mRetryPosition = mVideoPosition;
        mRetryDuration = mVideoLastDuration;
    }
}

private class ScreenModeExt implements Restorable, ScreenModeListener {
    private static final String KEY_VIDEO_SCREEN_MODE = "video_screen_mode";
    private int mScreenMode = ScreenModeManager.SCREENMODE_BIGSCREEN;
    private ScreenModeManager mScreenModeManager = new ScreenModeManager();

    public void setScreenMode() {
       // mVideoView.setScreenModeManager(mScreenModeManager);
       // mController.setScreenModeManager(mScreenModeManager);
        mScreenModeManager.addListener(this);
        mScreenModeManager.setScreenMode(mScreenMode);//notify all listener to change screen mode
        Log.d(TAG, "setScreenMode() mScreenMode=" + mScreenMode);
    }

    @Override
    public void onScreenModeChanged(int newMode) {
        mScreenMode = newMode;//changed from controller
        Log.d(TAG, "OnScreenModeClicked(" + newMode + ")");
    }

    @Override
    public void onRestoreInstanceState(Bundle icicle) {
        mScreenMode = icicle.getInt(KEY_VIDEO_SCREEN_MODE, ScreenModeManager.SCREENMODE_BIGSCREEN);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_VIDEO_SCREEN_MODE, mScreenMode);
    }
}

private class MoviePlayerExtension implements IMoviePlayer, Restorable {
    private static final String KEY_VIDEO_IS_LOOP = "video_is_loop";

    private String mAuthor;//for detail
    private String mTitle;//for detail
    private String mCopyRight;//for detail
    private boolean mIsLoop;
    private boolean mLastPlaying;
    private boolean mLastCanPaused;
    private boolean mPauseBuffering;

    @Override
    public void stopVideo() {
        Log.d(TAG, "stopVideo()");
      //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        if (DlnaService.isShare) {
            Toast.makeText(mContext, R.string.toast_nostop,
                    Toast.LENGTH_LONG).show();
        } else {
        mTState = TState.STOPED;
       // mVideoView.clearSeek();
        //mVideoView.clearDuration();
        mVideoView.stopPlayback();
       // mVideoView.setResumed(false);
        mVideoView.setVisibility(View.INVISIBLE);
        mVideoView.setVisibility(View.VISIBLE);
        clearVideoInfo();
        mFirstBePlayed = false;
        mController.setCanReplay(true);
        mController.showEnded();
        //resume review (play/pause asynchronous processing)
        mController.setViewEnabled(true);
        setProgress();
    }
      //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    }

    @Override
    public boolean canStop() {
        boolean stopped = false;
        if (mController != null) {
            if(mOverlayExt != null)
                stopped = mOverlayExt.isPlayingEnd();
        }
        Log.d(TAG, "canStop() stopped=" + stopped);
        return !stopped;
    }

    @Override
    public boolean getLoop() {
        Log.d(TAG, "getLoop() return " + mIsLoop);
        return mIsLoop;
    }

    @Override
    public void setLoop(boolean loop) {
        Log.d(TAG, "setLoop(" + loop + ") mIsLoop=" + mIsLoop);
        if (isLocalFile()) {
            mIsLoop = loop;
            mController.setCanReplay(loop);
          //  mController.setLoopViewBackground(loop);
            DlnaService.loop = loop;  //[FEATURE]-by NJHR(chengqun.sun),
                                        //2014/11/20, FR-826631 Multi screen interaction
        }
    }

    @Override
    public void startNextVideo(IMovieItem item) {
        Log.d(TAG, "startNextVideo()");
        IMovieItem next = item;
        if (next != null && next != mMovieItem) {
          //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
            if (DlnaService.isShare) {
                clearVideoInfo();
                mMovieItem = next;

                if (mMovieItem.getUri().toString().startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = mActivityContext.getContentResolver().query(
                                mMovieItem.getUri(),
                                new String[] {
                                    MediaStore.Video.VideoColumns.DATA
                                }, null, null, null);
                        if (cursor != null && cursor.moveToNext()) {
                            MovieActivity.mDlnaUri = Uri.parse(cursor.getString(0));
                        }
                    } catch (Throwable t) {
                        Log.w(TAG, "DLNA" + "cannot get absolute path from: "
                                + MovieActivity.mDlnaUri, t);
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }

                } else if (mMovieItem.getUri().toString().startsWith("file://")) {
                    MovieActivity.mDlnaUri = Uri.parse(mMovieItem.getUri().getPath());
                    Log.i("DLNA", "MovieActivity   file-mUri:" + MovieActivity.mDlnaUri);
                }
                mActivityContext.refreshMovieInfo(mMovieItem);
                doStartVideo(false, 0 , 0);
            } else {
            int position = mVideoView.getCurrentPosition();
            int duration = mVideoView.getDuration();
            mBookmarker.setBookmark(mMovieItem.getUri(), position, duration);
            mVideoView.stopPlayback();
            mVideoView.setVisibility(View.INVISIBLE);
            clearVideoInfo();
            mMovieItem = next;
            mActivityContext.refreshMovieInfo(mMovieItem);
            doStartVideo(false, 0 , 0);
            mVideoView.setVisibility(View.VISIBLE);
            }
          //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        } else {
            Log.e(TAG, "Cannot play the next video! " + item);
        }
        mActivityContext.closeOptionsMenu();
    }

    @Override
    public void onRestoreInstanceState(Bundle icicle) {
        mIsLoop = icicle.getBoolean(KEY_VIDEO_IS_LOOP, false);
        if (mIsLoop) {
            mController.setCanReplay(true);
        }// else  will get can replay from intent.
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_VIDEO_IS_LOOP, mIsLoop);
    }

    private void pauseIfNeed() {
        mLastCanPaused = canStop() && mVideoView.canPause();
        if (mLastCanPaused) {
            mLastPlaying = (mTState == TState.PLAYING);
            if(mOverlayExt != null)
                mOverlayExt.clearBuffering();
            mPauseBuffering = true;
            pauseVideo();
        }
        Log.d(TAG, "pauseIfNeed() mLastPlaying=" + mLastPlaying + ", mLastCanPaused=" + mLastCanPaused
                + ", mPauseBuffering=" + mPauseBuffering);
    }

    private void resumeIfNeed() {
        if (mLastCanPaused) {
            if (mLastPlaying) {
                mPauseBuffering = false;
                playVideo();
            }
        }
        Log.d(TAG, "resumeIfNeed() mLastPlaying=" + mLastPlaying + ", mLastCanPaused=" + mLastCanPaused
                + ", mPauseBuffering=" + mPauseBuffering);
    }

    public boolean pauseBuffering() {
        return mPauseBuffering;
    }

    /*public void setVideoInfo(Metadata data) {
        if (data.has(Metadata.TITLE)) {
            mTitle = data.getString(Metadata.TITLE);
        }
        if (data.has(Metadata.AUTHOR)) {
            mAuthor = data.getString(Metadata.AUTHOR);
        }
        if (data.has(Metadata.COPYRIGHT)) {
            mCopyRight = data.getString(Metadata.COPYRIGHT);
        }
    }*/
};

    // M: fix hardware accelerated issue
    // Wait for any animation, ten seconds should be enough
//    private static final int DELAY_REMOVE_MS = 10000;

//    private final Runnable mRemoveBackground = new Runnable() {
//        @SuppressLint("NewApi")
//        @Override
//        public void run() {
//            Log.d(TAG, "mRemoveBackground.run()");
//            mRootView.setBackground(null);
//        }
//    };

//    private void removeBackground() {
//        Log.d(TAG, "removeBackground()");
//        mHandler.removeCallbacks(mRemoveBackground);
//        mHandler.postDelayed(mRemoveBackground, DELAY_REMOVE_MS);
//    }

    // add background for removing ghost image.
    private void addBackground() {
        Log.d(TAG, "addBackground()");
//        mHandler.removeCallbacks(mRemoveBackground);
        mRootView.setBackgroundColor(Color.BLACK);
    }

    /// M: when show resuming dialog, suspend->wake up, will play video.
    private boolean mIsShowResumingDialog = false;

    public void onSeekComplete(MediaPlayer mp) {
        setProgress();
    }

    @Override
    public boolean onIsRTSP() {
        // TODO Auto-generated method stub
        if (MovieUtils.isRtspStreaming(mMovieItem.getUri(), mMovieItem
                .getMimeType())) {
            Log.d(TAG, "onIsRTSP() is RTSP");
            return true;
        }
        Log.d(TAG, "onIsRTSP() is not RTSP");
        return false;
    }
    //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode

    // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014, For FR828601 Pop-up Video play
    @Override
    public void onShowPopupVideo() {
        if (isLocalFile()) {
            int pos = mVideoView.getCurrentPosition();
            doOnPause(); // release the current playing media.
            TctPopupVideoComponent.getInstance(mActivityContext).showPopupVideo(mMovieItem.getUri(), pos);
            mActivityContext.finish();
        } else {
            Toast.makeText(mActivityContext, R.string.popupvideo_notsupport_message, Toast.LENGTH_SHORT).show();
        }
    }
    // [FEATURE]-Add-END by jian.pan1

    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 begin
    @Override
    public void onFastForward() {
        int duration = mVideoView.getDuration();
        int currPosition = mVideoView.getCurrentPosition();
        if (duration > 0 && currPosition >= 0 && mVideoView.canSeekForward()) {
            int forwardPos = currPosition + INTERVAL_TIME;
            if (forwardPos < duration) {
                mVideoView.seekTo(forwardPos);
                mReplayPosition = forwardPos;
                mVideoPosition = forwardPos;
            } else if (forwardPos >= duration) {
                mVideoView.seekTo(duration);
                mReplayPosition = duration;
                mVideoPosition = duration;
            }
            setProgress();
            mReplayDuration = duration;
            mVideoDuration = duration;
            mController.show();
        }
    }

    @Override
    public void onReverse() {
        int duration = mVideoView.getDuration();
        int currPosition = mVideoView.getCurrentPosition();
        if (duration > 0 && currPosition > 0 && mVideoView.canSeekForward()) {
            int reversePos = currPosition - INTERVAL_TIME;
            if (reversePos > 0) {
                mVideoView.seekTo(reversePos);
                mReplayPosition = reversePos;
                mVideoPosition = reversePos;
            } else if (reversePos <= 0) {
                mVideoView.seekTo(0);
                mReplayPosition = 0;
                mVideoPosition = 0;
            }
            setProgress();
            mReplayDuration = duration;
            mVideoDuration = duration;
            mController.show();
        }
    }

    public void invalidateFavourite(boolean isFavorite){
        mController.invalidateFavourite(isFavorite);
    }

    public void invalidateLockMode(){
        mController.invalidateLockMode();
    }

    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-17,PR1060028 begin
    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-12,PR1134047 begin
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-19,PR732345 begin
    public void updateVideoBottom(LocalVideo item,boolean isLocalFile,boolean notInDataBase){
      //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-02,PR1527396 begin
        mItem=item;
      //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-02,PR1527396 end
        mController.updateVideoBottomView(item,isLocalFile,notInDataBase);
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-19,PR732345 end
    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-12,PR1134047 end
    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-17,PR1060028 end

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-01-11,PR1400047 begin
    public boolean isVideoPlaying(){
        return mController.mState == State.PLAYING ? true : false;
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-01-11,PR1400047 begin

    @Override
    public void onControlSystemUI(boolean isHideSystemUI) {
        if(isHideSystemUI){
            showSystemUi(false);
        }else{
            showSystemUi(true);
        }
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-28,PR791930 begin
    @Override
    public void udpateTimeBar() {
        mShowing = true;
        setProgress();
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-28,PR791930 end

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-02-16, ALM-1532238 begin
    private class ResumeDialog implements android.view.View.OnClickListener {
        private final BookmarkerInfo mBookmarkInfo;
        private AlertDialog mDialog = null;
        private TextView mMessage = null;
        private Button mBtnResume = null;
        private Button mBtnRestart = null;

        public ResumeDialog(Context context, BookmarkerInfo bookmarkInfo) {
            mBookmarkInfo = bookmarkInfo;

            View contentView = LayoutInflater.from(context).inflate(R.layout.movie_resume_dialog_view, null);
            mMessage = (TextView)contentView.findViewById(R.id.dialog_message);
            mBtnResume = (Button)contentView.findViewById(R.id.button_resume);
            mBtnRestart = (Button)contentView.findViewById(R.id.button_restart);
            mMessage.setText(String.format(context.getString(R.string.resume_playing_message),
                            GalleryUtils.formatDuration(context, mBookmarkInfo.mBookmark / 1000)));
            mBtnResume.setOnClickListener(this);
            mBtnRestart.setOnClickListener(this);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setView(contentView);
            mDialog = dialogBuilder.create();
            mDialog.setOnShowListener(new OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    mIsShowResumingDialog = true;
                }
            });
            mDialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface arg0) {
                    mIsShowResumingDialog = false;
                    showSystemUi(false);
                }
            });
            mDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    onCompletion();
                }
            });
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.button_resume:
                mVideoPosition = mBookmarkInfo.mBookmark;
                mVideoCanSeek = true;
                doStartVideo(true, mBookmarkInfo.mBookmark, mBookmarkInfo.mDuration);
                break;
            case R.id.button_restart:
                mVideoPosition = mVideoDuration = 0;
                doStartVideo(true, 0, mBookmarkInfo.mDuration);
                break;
            default:
                break;
            }
            dissmiss();
        }

        public void show() {
            if (mDialog != null && !mDialog.isShowing()) {
                mDialog.show();
            }
        }

        public void dissmiss() {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-02-16, ALM-1532238 end
}
//[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 end

class Bookmarker {
    private static final String TAG = "Bookmarker";

    private static final String BOOKMARK_CACHE_FILE = "bookmark";
    private static final int BOOKMARK_CACHE_MAX_ENTRIES = 100;
    private static final int BOOKMARK_CACHE_MAX_BYTES = 10 * 1024;
    private static final int BOOKMARK_CACHE_VERSION = 1;

    private static final int HALF_MINUTE = 30 * 1000;
    private static final int TWO_MINUTES = 4 * HALF_MINUTE;

    private final Context mContext;

    public Bookmarker(Context context) {
        mContext = context;
    }

    public void setBookmark(Uri uri, int bookmark, int duration) {
        try {
            BlobCache cache = CacheManager.getCache(mContext,
                    BOOKMARK_CACHE_FILE, BOOKMARK_CACHE_MAX_ENTRIES,
                    BOOKMARK_CACHE_MAX_BYTES, BOOKMARK_CACHE_VERSION);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeUTF(uri.toString());
            dos.writeInt(bookmark);
            dos.writeInt(duration);
            dos.flush();
            cache.insert(uri.hashCode(), bos.toByteArray());
        } catch (Throwable t) {
            Log.w(TAG, "setBookmark failed", t);
        }
    }

    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    public BookmarkerInfo getBookmark(Uri uri) {
        try {
            BlobCache cache = CacheManager.getCache(mContext,
                    BOOKMARK_CACHE_FILE, BOOKMARK_CACHE_MAX_ENTRIES,
                    BOOKMARK_CACHE_MAX_BYTES, BOOKMARK_CACHE_VERSION);

            byte[] data = cache.lookup(uri.hashCode());
            if (data == null) {
                Log.v(TAG, "getBookmark(" + uri + ") data=null. uri.hashCode()=" + uri.hashCode());
                return null;
            }

            DataInputStream dis = new DataInputStream(
                    new ByteArrayInputStream(data));

            String uriString = DataInputStream.readUTF(dis);
            int bookmark = dis.readInt();
            int duration = dis.readInt();
            Log.v(TAG, "getBookmark(" + uri + ") uriString=" + uriString + ", bookmark=" + bookmark
                        + ", duration=" + duration);
            if (!uriString.equals(uri.toString())) {
                return null;
            }

            if ((bookmark < HALF_MINUTE) || (duration < TWO_MINUTES)
                    || (bookmark > (duration - HALF_MINUTE))) {
                return null;
            }
            return new BookmarkerInfo(bookmark, duration);
        } catch (Throwable t) {
            Log.w(TAG, "getBookmark failed", t);
        }
        return null;
    }
    /*public Integer getBookmark(Uri uri) {
        try {
            BlobCache cache = CacheManager.getCache(mContext,
                    BOOKMARK_CACHE_FILE, BOOKMARK_CACHE_MAX_ENTRIES,
                    BOOKMARK_CACHE_MAX_BYTES, BOOKMARK_CACHE_VERSION);

            byte[] data = cache.lookup(uri.hashCode());
            if (data == null) return null;

            DataInputStream dis = new DataInputStream(
                    new ByteArrayInputStream(data));

            String uriString = DataInputStream.readUTF(dis);
            int bookmark = dis.readInt();
            int duration = dis.readInt();

            if (!uriString.equals(uri.toString())) {
                return null;
            }

            if ((bookmark < HALF_MINUTE) || (duration < TWO_MINUTES)
                    || (bookmark > (duration - HALF_MINUTE))) {
                return null;
            }
            return Integer.valueOf(bookmark);
        } catch (Throwable t) {
            Log.w(TAG, "getBookmark failed", t);
        }
        return null;
    }*/
    //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
}

//[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
class BookmarkerInfo {
    public final int mBookmark;
    public final int mDuration;

    public BookmarkerInfo(int bookmark, int duration) {
        this.mBookmark = bookmark;
        this.mDuration = duration;
    }

    @Override
    public String toString() {
         return new StringBuilder()
        .append("BookmarkInfo(bookmark=")
        .append(mBookmark)
        .append(", duration=")
        .append(mDuration)
        .append(")")
        .toString();
    }
}
//[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode

/*
 * Copyright (C) 2011 The Android Open Source Project
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
/* ----------|----------------|------------------|--------------------------- */
/* 20/11/2014|chengqun.sun      |FR826631          |Multi screen interaction    */
/*-----------|----------------|------------------|--------------------------- */
/* 21/01/2015|    jialiang.ren      |      PR-895834       |[Android5.0][Gallery_v5.1.1.0103.0]    */
/*           |                      |                      |The full screen key display incomplete */
/*           |                      |                      |when playing video in horizontal screen*/
/* ----------|----------------------|----------------------|---------------------------------------*/
/* 21/01/2015|jian.pan1             |FR904501              |Gallery Ergo 5.1.4 -
/*           |                      |                      |Fast forward and reverse
/* ----------|----------------------|----------------------|----------------- */
/* 05/02/2015|ye.chen               |FR908268              |[Video streaming]It shouldn't pause live TV and prompt whether resume video
/* ----------|----------------------|----------------------|----------------- */
/* 10/02/2015|ye.chen               |PR928280              |    [5.0][Gallery][VideoPlayer] tap to hide/unhide play control panel
/* ----------|----------------------|----------------------|----------------- */
/* 04/03/2015|dongliang.feng        |CR940102              |[Gallery_Ergo_5.1.9.pdf]Video Lock Function */
/* ----------|----------------------|----------------------|----------------- */
/* 03/16/2015| jian.pan1            | PR916254             |[GenericApp][Gallery]HDPI resolution adaptation
/* ----------|----------------------|----------------------|----------------------------------------------- */
/* 03/18/2015| jian.pan1            | PR916254             |[GenericApp][Gallery]HDPI resolution adaptation
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
/* 03/04/2015|dongliang.feng        |PR968597              |[Gallery02][Android5.0][Gallery_v5.1.9.1.0110.0] */
/*           |                      |                      |3D Audio disappear after 2s */
/* ----------|----------------------|----------------------|----------------- */
/* 08/04/2015|dongliang.feng        |PR968503              |[Android5.0][Gallery_v5.1.9.1.0110.0] */
/*           |                      |                      |3D Audio still displays when plugging in headset */
/* ----------|----------------------|----------------------|----------------- */
/* 09/04/2015|dongliang.feng        |PR971974              |[Gallery]The video player interface with 3D audio icon */
/* ----------|----------------------|----------------------|----------------- */
/* 27/04/2015|dongliang.feng        |CR987626              |[Gallery][Video]Double tap on screen when */
/*           |                      |                      |video playing(unlocked) to lock video */
/* ----------|----------------------|----------------------|----------------- */
/* 04/29/2015|dongliang.feng        |CR989796              |[5.0][Gallery]video play backward/forward */
/* ----------|----------------------|----------------------|----------------- */
/* 08/05/2015 |    jialiang.ren     |      PR-997159       |[Android][Gallery_v5.1.13.1.0201.0]The video */
/*                                                          won't unlock when plugging off headset       */
/*------------|---------------------|----------------------|---------------------------------------------*/
/* 05/20/2015| jian.pan1            | PR1003520            |[Android][Gallery_v5.1.13.1.0203.0]The video will unlock automatically when re-entering gallery
/* ----------|----------------------|----------------------|----------------- */
/* 17/06/2015|dongliang.feng        |PR1025824             |[Android 5.1][Gallery_v5.1.13.1.0208.0]The progress */
/*           |                      |                      |bar won't display in the bottom when locking the video */
/* ----------|----------------------|----------------------|----------------- */
/* 18/06/2015|    su.jiang          |   PR-1025516         |[Android 5.1][Gallery_v5.1.13.1.0208.0]The operation bar is not in */
/*-----------|--------------------- |-------------------   |the middle when playing the video----------------------------------*/
/* ----------|----------------------|----------------------|----------------- */
/* 09/09/2015|dongliang.feng        |PR1080234             |[Android 5.1][Gallery_v5.2.0.1.1.0303.0]It doesn't work for double-Click screen during play HLS streaming */
/* ----------|----------------------|----------------------|----------------- */
/* 19/10/2015|    su.jiang          |  PR-732345           |[Android 5.1][Gallery_v5.2.0.1.1.0306.0]The DRM video can be trimed*/
/*-----------|----------------------|----------------------|-------------------------------------------------------------------*/
/* 28/10/2015|    su.jiang     |  PR-791930    |[Android5.1][Gallery_v5.2.3.1.1.0307.0]It can play or paused video when locking antion bar*/
/*-----------|-----------------|---------------|------------------------------------------------------------------------------------------*/
/* 25/11/2015|dongliang.feng        |PR966881              |[Video Streaming]The buffer icon doesn't show in the center of the screen */
/* ----------|----------------------|----------------------|----------------- */
/* 08/12/2015|dongliang.feng        |PR1058734             |[Video]The time was overlapped by keyboard after locked */
/* ----------|----------------------|----------------------|----------------- */
/* 10/12/2015|    su.jiang          |  PR-1042974          |[Android6.0][Gallery_v5.2.5.1.0318.0]The lock icon will change after lock homescreen*/
/*-----------|----------------------|----------------------|------------------------------------------------------------------------------------*/
/* 12/12/2015|    su.jiang          |  PR-1134047          |[Video Streaming]Facebook icon display gray after click share when playing video streaming*/
/*-----------|----------------------|----------------------|------------------------------------------------------------------------------------------*/
/* 12/12/2015|    su.jiang          |  PR-1045697          |[Android6.0][Gallery_v5.2.5.1.0318.0]The function of lock/unlock no use*/
/*-----------|----------------------|----------------------|-----------------------------------------------------------------------*/
/* 17/12/2015|    su.jiang          |  PR-1060028          |[Gallery][MMS]Pop up gallery force close when view the detail of video in MMS composer*/
/*-----------|----------------------|----------------------|--------------------------------------------------------------------------------------*/
/* 2015/12/26|  caihong.gu-nb  |  PR-1201923   |	[Gallery]It automatic exit video play interface after disconnect wifi.*/
/*-----------|-----------------|---------------|-----------------------------------------------------------------------------------------------------------------*/
/* 2016/01/26|  caihong.gu-nb       |  PR-1507936   | [Video Player]The locked icon diplay gray. */
/*-----------|----------------------|---------------|---------------------------------------------------------------------------------*/
/* 17/02/2016|    su.jiang     |  PR-1431083   |[GAPP][Android6.0][Gallery]The DRM video play interface display not same as preview interface.*/
/*-----------|-----------------|---------------|----------------------------------------------------------------------------------------------*/
/* 18/02/2016|    su.jiang     |  PR-1537396   |[GAPP][Android6.0][Gallery]The edit button invalid on video interface.*/
/*-----------|-----------------|---------------|----------------------------------------------------------------------*/
/* 03/08/2016|    su.jiang     |  PR-1758674   |[GAPP][Android 6.0][Gallery]It have a trim button on a less than 1s video'playing interface.*/
/*-----------|-----------------|---------------|--------------------------------------------------------------------------------------------*/

package com.tct.gallery3d.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
//[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.content.res.Configuration;
import android.util.Log;
//[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end

import com.android.gallery3d.app.DlnaService;
import com.android.gallery3d.popupvideo.TctPopupVideoComponent;
import com.tct.gallery3d.R;
import com.tct.gallery3d.app.CommonControllerOverlay.State;
import com.tct.gallery3d.app.MoviePlayer.TVState; //[FEATURE]-by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
import com.tct.gallery3d.app.view.TimeBar;
import com.tct.gallery3d.common.ApiHelper;
import com.tct.gallery3d.data.LocalVideo;
import com.tct.gallery3d.data.MediaDetails;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.PLFUtils;
import com.tct.gallery3d.util.ScreenUtils;

//[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 begin
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.bluetooth.BluetoothAdapter;
import android.media.AudioManager;
//[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 end

/**
 * The playback controller for the Movie Player.
 */
// [FEATURE]-Modify-BEGIN by jian.pan1,11/06/2014, For FR828601 Pop-up Video play
public class MovieControllerOverlay extends CommonControllerOverlay implements
        AnimationListener , OnClickListener, OnGestureListener {
// [FEATURE]-Modify-END by jian.pan1

    public static final int FULLSCREEN_MODE_ON = 1;
    public static final int FULLSCREEN_MODE_OFF = 0;
    public static final int LOOP_MODE_ON = 1;
    public static final int LOOP_MODE_OFF = 0;
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
    public static final int LOCK_MODE_ON = 1;
    public static final int LOCK_MODE_OFF = 0;
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end

    private boolean hidden = true; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459
    // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014, For FR828601 Pop-up Video play
    private boolean isShowPopupVideoOn = false;
    // [FEATURE]-Add-END by jian.pan1
  //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
    private boolean mEnableScrubbing;
  //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268

    private final Handler handler;
    private final Runnable startHidingRunnable;
    private final Runnable startHidingTimebarRunnable;
    private final Animation hideAnimation;
    protected RelativeLayout mVideoBottomView;
    protected ImageButton mVideoShare;
    protected ImageButton mVideoEdit;
    protected ImageButton mVideoTrim;
    protected ImageButton mVideoFavourite;

    protected ImageButton mLoopModeBtn;
    protected ImageButton mPlayControlBtn;
    protected ImageButton mFullScreenBtn;
    // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014, For FR828601 Pop-up Video play
    protected ImageButton mShowPopupVideoBtn;
    // [FEATURE]-Add-END by jian.pan1
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 begin
    protected ImageButton mFastForward;
    protected ImageButton mReverse;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 end
    protected int mLoopMode = LOOP_MODE_OFF;
    protected int mFullscreenMode = FULLSCREEN_MODE_OFF;

    protected PlayerControlPanelListener mControlListenr;

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
    protected ImageButton mVideoLockBtn;
    protected int mLockMode = LOCK_MODE_OFF;
    private Context mContext;
    public boolean mNeedPanel = true;
    private GestureDetector mGestureScanner;
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-16,PR916254 begin
    private int mNavigationBarHeight;
    private boolean hasNavigation = false;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-16,PR916254 end

    //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 begin
    private ImageView m3DAudioIcon;
    private final int mHide3DAudioIconDelay = 5000; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-03, PR968597
    private final Runnable mHide3DAudioIconRunnable = new Runnable() {
        @Override
        public void run() {
            m3DAudioIcon.setVisibility(View.INVISIBLE);
        }
    };
    private int m3DAudioIconWidth = 0;
    private int m3DAudioIconHeigth = 0;
    private int m3DAudioIconTopMargin = 0;
    //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 end

    public boolean m3DAudioEnable = false; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 begin
    private static final float Y_EXCURSION = 200;
    private static final int TOAST_DURATION = 2000;
    private boolean mOnlyShowTimeBar = false;
    private float mMoveDistanceX = 0;
    private float mMoveDistanceY = 0;
    private int mTimeBarPadding = 0;
    private int mScreenWidth = 0;
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 end

    public MovieControllerOverlay(Context context) {
        super(context);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-16,PR916254 begin
        mNavigationBarHeight = (int)context.getResources().getDimension(R.dimen.bottom_navigation_height);
        hasNavigation = ScreenUtils.getNavigationBarHeight((Activity) context) > 0;
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-16,PR916254 end
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 begin
        mTimeBarPadding = (int)context.getResources().getDimension(R.dimen.time_bar_padding);
        mScreenWidth = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 end
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mVideoBottomView = (RelativeLayout) inflater.inflate(R.layout.videoplayer_control_panel, null);
        mVideoShare = (ImageButton) mVideoBottomView.findViewById(R.id.video_share);
        mVideoEdit = (ImageButton) mVideoBottomView.findViewById(R.id.video_edit);
        mVideoTrim = (ImageButton) mVideoBottomView.findViewById(R.id.video_trim);
        mVideoFavourite = (ImageButton) mVideoBottomView.findViewById(R.id.video_favourite);

        mVideoShare.setOnClickListener(this);
        mVideoEdit.setOnClickListener(this);
        mVideoTrim.setOnClickListener(this);
        mVideoFavourite.setOnClickListener(this);
//        mPlayControlBtn = (ImageButton)mVideoControlView.findViewById(R.id.playControlButton);
//        mPlayControlBtn.setOnClickListener(this);
//
//        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 begin
//        mFastForward = (ImageButton)mVideoControlView.findViewById(R.id.fast_go_btn);
//        mReverse = (ImageButton)mVideoControlView.findViewById(R.id.fast_back_btn);
//        mFastForward.setOnClickListener(this);
//        mReverse.setOnClickListener(this);
//        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 end
//
//        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
//        /*mFullScreenBtn = (ImageButton)mVideoControlView.findViewById(R.id.fullscreenModeButton);
//        mFullScreenBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mFullscreenMode == FULLSCREEN_MODE_ON) {
//                    mFullscreenMode = FULLSCREEN_MODE_OFF;
//                    mFullScreenBtn.setImageResource(R.drawable.videoplayer_fullscreen_on);
//                } else {
//                    mFullscreenMode = FULLSCREEN_MODE_ON;
//                    mFullScreenBtn.setImageResource(R.drawable.videoplayer_fullscreen_off);
//                }
//                if(mControlListenr != null)
//                    mControlListenr.onFullScreenMode(mFullscreenMode);
//            }
//        });*/
//
        mContext = context;
//        mVideoLockBtn = (ImageButton)mVideoControlView.findViewById(R.id.video_lock_button);
//        mVideoLockBtn.setOnClickListener(this);
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end

        // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014, For FR828601 Pop-up Video play
//        mShowPopupVideoBtn = (ImageButton) mVideoControlView.findViewById(R.id.show_popup_video_btn);
//        isShowPopupVideoOn = TctPopupVideoComponent.getInstance(context).isFeatureOn();
//        if (isShowPopupVideoOn) {
//            mShowPopupVideoBtn.setVisibility(View.VISIBLE);
//            mShowPopupVideoBtn.setOnClickListener(this);
//        } else {
//            mShowPopupVideoBtn.setVisibility(View.GONE);
//        }
        // [FEATURE]-Add-END by jian.pan1
        addView(mVideoBottomView);
        mVideoBottomView.setVisibility(View.INVISIBLE); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459
        LayoutParams wrapContent =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(mPlayPauseReplayView, wrapContent);
        mPlayPauseReplayView.setVisibility(View.INVISIBLE);
        handler = new Handler();
        startHidingRunnable = new Runnable() {
                @Override
            public void run() {
                startHiding();
            }
        };

        startHidingTimebarRunnable = new Runnable() {
            @Override
            public void run() {
                startHidingTimeBar();
            }
        };
        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.player_out);
        hideAnimation.setAnimationListener(this);

        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 begin
        m3DAudioEnable = PLFUtils.getBoolean(context, "def_gallery_3DAudio_enable");
        //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 begin
        setBackgroundColor(Color.TRANSPARENT);
        if (m3DAudioEnable) {
            m3DAudioIcon = new ImageView(mContext);
            m3DAudioIcon.setBackgroundResource(R.drawable.ic_video_3daudio);
            addView(m3DAudioIcon);
            AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
            boolean isHeadsetOn = audioManager.isWiredHeadsetOn();
            boolean isBluetoothOn = (BluetoothAdapter.getDefaultAdapter().
                    getProfileConnectionState(android.bluetooth.BluetoothProfile.HEADSET)
                    == android.bluetooth.BluetoothProfile.STATE_CONNECTED);
            if (isHeadsetOn || isBluetoothOn) {
                m3DAudioIcon.setVisibility(View.INVISIBLE);
            } else {
                show3DAudioIcon();
            }
            m3DAudioIconWidth = m3DAudioIconHeigth = (int)mContext.getResources().getDimension(R.dimen.video_3D_icon);
            m3DAudioIconTopMargin = (int)mContext.getResources().getDimension(R.dimen.video_3D_icon_top_margin);
        }
        //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 end
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 end

        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
        mGestureScanner = new GestureDetector(context, this);
        mGestureScanner.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener(){
            public boolean onDoubleTap(MotionEvent e) {
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-09, PR1080234 begin
                if (mIsLiveStreaming) {
                    return false;
                }
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-09, PR1080234 end

                if (mLockMode == LOCK_MODE_ON) {
                    mLockMode = LOCK_MODE_OFF;
                    //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/26,PR1507936 begin
                    updateView(R.drawable.ic_video_unlock, true, R.string.unlock_video);
                    //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/26,PR1507936 end
                    //[BUGFIX]-Modify by TCTNJ, xinrong.wang, 2016-01-06, PR1048407 begin
                    //show();
                    //[BUGFIX]-Modify by TCTNJ, xinrong.wang, 2016-01-06, PR1048407 end
                    isLockHideSystemUi = false;
                }
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-27, CR987626 begin
              //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-02-01,PR1537480 begin
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-05-20,PR1003520 begin
                else if (mLockMode == LOCK_MODE_OFF&&mState==State.PLAYING) {
                    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-05-20,PR1003520 end
                  //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-02-01,PR1537480 end
                    mLockMode = LOCK_MODE_ON;
                    updateView(R.drawable.ic_lock, false, R.string.lock_video);//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-10,PR1059325
                    isLockHideSystemUi = true;
                    hide(false);
                }
//                maybeStartHiding();
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-27, CR987626 end
                return false;
            }

            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }

            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (hidden) {
                    mOnlyShowTimeBar = false; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796
                    if(mLockMode == LOCK_MODE_ON){
                        showTimeBarAndSystemUi();
                        if(!mIsLiveStreaming) {
                            updateTimeBar();//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-28,PR791930
                        }
                    }else{
                        show();
                    }
                } else {
                    hide(true);
                }
                return true;
            }
        });
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end
    }

    private void showTimeBarAndSystemUi(){
        mBackground.setVisibility(View.GONE);
        mVideoBottomView.setVisibility(View.GONE);
        mTimeBar.setVisibility(mIsLiveStreaming ? View.GONE : View.VISIBLE);
        mListener.onControlSystemUI(false);
        handler.postDelayed(startHidingTimebarRunnable, 1000);
    }

    private boolean isLockHideSystemUi = false;
    @Override
    protected void createTimeBar(Context context) {
        mTimeBar = new TimeBar(context, this);
        mTimeBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hide(boolean isNeedHideTimebar) {
        if (hidden) return; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459
        //[FEATURE]-Mod-BEGIN by TCTNB.wen.zhuang,12/10/2013,FR-550507,
        if (!DlnaService.isShare) {
        boolean wasHidden = hidden;
        hidden = true;
        super.hide(isNeedHideTimebar);
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
        if (mVideoBottomView != null) {
            mVideoBottomView.setVisibility(View.INVISIBLE);
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end
        if (mListener != null && wasHidden != hidden) {
            if(!isLockHideSystemUi){
                mListener.onHidden();
            }else{
                cancelHidingTimgeBar();
                handler.postDelayed(startHidingTimebarRunnable, 1000);
                mListener.onControlSystemUI(false);
            }
        }
        isLockHideSystemUi = false;
    }
      //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    }

    public void setPlayerControlPanelListener(PlayerControlPanelListener controlListener) {
        this.mControlListenr = controlListener;
    }

 //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-15,PR1019479 begin
    public void showplaypause()
    {
    	updateTimeBar();
    	show();
        updateViews();
        mBackground.setVisibility(View.INVISIBLE);
        mTimeBar.setVisibility(View.INVISIBLE);
        mVideoBottomView.setVisibility(View.INVISIBLE);
        maybeStartHiding(false);
     } 
  //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-15,PR1019479 end

    @Override
    public void show() {
        if (!hidden) return; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459
        boolean wasHidden = hidden;
        hidden = false;
        mVideoBottomView.setVisibility(View.VISIBLE); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102
        super.show();
        if (mListener != null && wasHidden != hidden) {
            mListener.onShown();
        }
        //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        if (!DlnaService.isShare) {
        maybeStartHiding(true);
        mTimeBar.setVisibility(mIsLiveStreaming ? View.GONE : View.VISIBLE);
        mBackground.setVisibility(mIsLiveStreaming ? View.GONE : View.VISIBLE);
    }
        //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-28,PR791930 begin
    private void updateTimeBar(){
        if(mListener != null){
            mListener.udpateTimeBar();
        }
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-28,PR791930 end

    private void hidePlayButtonIfPlaying() {
        if (mState == State.PLAYING) {
            mPlayPauseReplayView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void showPlaying() {
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459 begin
        boolean fromLoading = (mMainView == mLoadingView && mLoadingView.getVisibility() == View.VISIBLE);
//        mPlayControlBtn.setImageResource(R.drawable.videoplayer_pause);
        super.showPlaying();
        if (fromLoading && mVideoBottomView.getVisibility() == View.INVISIBLE
                && !mIsLiveStreaming && mBackground.getVisibility() == View.VISIBLE) {
            mVideoBottomView.setVisibility(View.VISIBLE);
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459 end
        hidePlayButtonIfPlaying();
    }

    @Override
    public void showPaused() {
        super.showPaused();
    }

    @Override
    public void showEnded() {
        super.showEnded();
    }

    @Override
    public void showLoading() {
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 begin
        if(!((MovieActivity)mContext).isInternetConnected()) return;
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 end
        super.showLoading();
        //show();//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-12,PR1134047
    }

    @Override
    public void showErrorMessage(String message) {
        super.showErrorMessage(message);
        show();
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            if (view == mPlayPauseReplayView) {
                if (mState == State.ENDED) {
                    if(mCanReplay){
                        mListener.onReplay();
                    }
                } else if (mState == State.PAUSED || mState == State.PLAYING) {
                    mListener.onPlayPause();
                }
                updateViews();
            }
            // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014, For FR828601 Pop-up Video play
//            else if (isShowPopupVideoOn && view == mShowPopupVideoBtn) {
//                mListener.onShowPopupVideo();
//            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 begin
//            } else if (view == mFastForward) {
//                mListener.onFastForward();
//            } else if (view == mReverse) {
//                mListener.onReverse();
//            }
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 end
            // [FEATURE]-Add-END by jian.pan1
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
//            else if (view == mVideoLockBtn) {
//                int imageResource;
//                boolean enable = false;
//                int toastMessageId;
//                if (mLockMode == LOCK_MODE_ON) {
//                    mLockMode = LOCK_MODE_OFF;
//                    imageResource = R.drawable.videoplayer_unlock;
//                    enable = true;
//                    toastMessageId = R.string.unlock_video;
//                } else {
//                    mLockMode = LOCK_MODE_ON;
//                    imageResource = R.drawable.videoplayer_lock;
//                    enable = false;
//                    toastMessageId = R.string.lock_video;
//                }
//                updateView(imageResource, enable, toastMessageId);
//            }
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end

            maybeStartHiding(false); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459
        }
        if(mContext != null){
            ((MovieActivity)mContext).onBottomControlClicked(view.getId());
        }
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-08,PR997159 begin
    public boolean isLocked(){
        return mLockMode == LOCK_MODE_ON;
    }

    public void unLock() {
        int imageResource = 0;
        boolean enable = false;
        int toastMessageId = 0;
        if (mLockMode == LOCK_MODE_ON) {
            mLockMode = LOCK_MODE_OFF;
            //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/26,PR1507936 begin
            imageResource = R.drawable.ic_video_unlock;//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-10,PR1059325
            //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/26,PR1507936 end
            enable = true;
            toastMessageId = R.string.unlock_video;
            updateView(imageResource, enable, toastMessageId);
        }
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-08,PR997159 end
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1025516 begin
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if(child == mVideoBottomView) {
                if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                } else {
                    if (mLockMode == LOCK_MODE_ON) {
                        mVideoBottomView.setVisibility(View.INVISIBLE);
                        measureChild(child, widthMeasureSpec, heightMeasureSpec - mWindowInsets.bottom);
                        } else {
                            measureChild(child, widthMeasureSpec - MovieActivity.mNavigationBarHeight, heightMeasureSpec - mWindowInsets.bottom);
                                }
                        }
                }
            }
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1025516 end
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Rect insets = mWindowInsets;
        int pl = insets.left; // the left paddings
        int pr = insets.right;
        int pt = insets.top;
        int pb = insets.bottom;

        int h = bottom - top;
        int w = right - left;
        boolean error = mErrorView.getVisibility() == View.VISIBLE;

        int y = h - pb;
        // Put both TimeBar and Background just above the bottom system
        // component.
        // But extend the background to the width of the screen, since we don't
        // care if it will be covered by a system component and it looks better.
        // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014,FR828601 Pop-up Video play
        // video control panel height should get dynamically
        // [FEATURE]-Add-END by jian.pan1
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 begin
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-16,PR916254 begin
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-18,PR916254 begin
        if (mOnlyShowTimeBar) {
            mBackground.layout(0, h - mTimeBar.getBarHeight(), w, h);
            mTimeBar.layout(pl + mTimeBarPadding, h - mTimeBar.getPreferredHeight(), w - mTimeBarPadding, h);
        } else {
            if (hasNavigation) {
                if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (mNeedPanel) {
                        mBackground.layout(0, y - mTimeBar.getBarHeight() - mNavigationBarHeight, w, y);
                        mTimeBar.layout(pl + mTimeBarPadding, y - mTimeBar.getPreferredHeight() - mNavigationBarHeight,
                                w - pr - mTimeBarPadding, y - mNavigationBarHeight);
                        mVideoBottomView.layout(0, y - mNavigationBarHeight, w, y);
                    } else {
                        mBackground.layout(0, y - mTimeBar.getBarHeight(), w, h);
                        mTimeBar.layout(pl + mTimeBarPadding, y - mTimeBar.getPreferredHeight() - mNavigationBarHeight,
                                w - pr - mTimeBarPadding, y - mNavigationBarHeight);//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-12,PR1045697
                        mVideoBottomView.layout(0, y, w, h);
                    }
                    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 begin
                    //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 begin
                    if (m3DAudioEnable) {
                        int audio3DIconRigthMargin = (int)mContext.getResources().getDimension(R.dimen.video_3D_icon_right_margin_portrait);
                        m3DAudioIcon.layout(w - audio3DIconRigthMargin - m3DAudioIconWidth, m3DAudioIconTopMargin,
                                w - audio3DIconRigthMargin, m3DAudioIconTopMargin + m3DAudioIconHeigth);
                    }
                    //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 end
                    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 end
                } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-17, PR1025824 begin
                    if (mLockMode == LOCK_MODE_ON) {
                        mBackground.layout(0, bottom - mTimeBar.getBarHeight() - mNavigationBarHeight, w, bottom);
                        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-12,PR1045697 begin
                        mTimeBar.layout(pl + mTimeBarPadding, y - mTimeBar.getPreferredHeight() - mNavigationBarHeight,
                                w - pr - mTimeBarPadding, y - mNavigationBarHeight); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-12-08, PR1058734
                        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-12,PR1045697 end
                    } else {
                        mBackground.layout(0, y - mTimeBar.getBarHeight() - mNavigationBarHeight, w, y);
                    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-17, PR1025824 begin
                        mTimeBar.layout(pl + mTimeBarPadding, y - mTimeBar.getPreferredHeight() - mNavigationBarHeight,
                                w - pr - mTimeBarPadding, y - mNavigationBarHeight);
                        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1025516 begin
                        //int leftMargin = (int)mContext.getResources().getDimension(R.dimen.player_progressbar_left);
                        mVideoBottomView.layout(0, y - mNavigationBarHeight, w, y);
                        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1025516 end
                    }
                    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 begin
                    //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 begin
                    if (m3DAudioEnable) {
                        int audio3DIconRigthMargin = (int)mContext.getResources().getDimension(R.dimen.video_3D_icon_right_margin_landscape);
                        m3DAudioIcon.layout(w - audio3DIconRigthMargin - m3DAudioIconWidth, m3DAudioIconTopMargin,
                                w - audio3DIconRigthMargin, m3DAudioIconTopMargin + m3DAudioIconHeigth);
                    }
                    //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 end
                    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 end
                } else {
                    Log.d("MovieControllerOverlay", "current orientatin is ORIENTATION_UNDEFINED, layout falut");
                }
            } else {
                mBackground.layout(0, y - mTimeBar.getBarHeight() - mNavigationBarHeight, w, y);
                mTimeBar.layout(pl + mTimeBarPadding, y - mTimeBar.getPreferredHeight() - mNavigationBarHeight,
                        w - pr - mTimeBarPadding, y - mNavigationBarHeight);
                mVideoBottomView.layout(0, y - mNavigationBarHeight, w, y);
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 begin
                //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 begin
                if (m3DAudioEnable) {
                    int audio3DIconRigthMargin = (int)mContext.getResources().getDimension(R.dimen.video_3D_icon_right_margin);
                    m3DAudioIcon.layout(w - audio3DIconRigthMargin - m3DAudioIconWidth, m3DAudioIconTopMargin,
                            w - audio3DIconRigthMargin, m3DAudioIconTopMargin + m3DAudioIconHeigth);
                }
                //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 end
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974 end
            }
        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-18,PR916254 end
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-16,PR916254 end
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 end

        //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        if (DlnaService.isShare) {
            //mScreenModeExt.onHide();//temp disabled for FR826631
            //mLoopView.setVisibility(View.INVISIBLE);//temp disabled for FR826631
        }
        //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction

        // Put the play/pause/next/ previous button in the center of the screen
        layoutCenteredView(mPlayPauseReplayView, 0, 0, w, h);
        layoutCenteredView(mLoadingView, 0, 0, w, h); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-11-25, PR966881
        if (mMainView != null) {
            layoutCenteredView(mMainView, 0, 0, w, h);
        }
    }

    private void maybeStartHiding(boolean isLockMode) {
        cancelHiding();
        if(isLockMode){
            mTimeBar.setVisibility(View.VISIBLE);
        }
        if (mState == State.PLAYING) {
            handler.postDelayed(startHidingRunnable, 1000);
        }
    }

    private void startHiding() {
        startHideAnimation(mBackground);
        startHideAnimation(mTimeBar);
        startHideAnimation(mPlayPauseReplayView);
        startHideAnimation(mVideoBottomView);

        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 begin
        if (mOnlyShowTimeBar) {
            mOnlyShowTimeBar = false;
            mControlListenr.updatePlayerControlShowState(false);
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 end
    }

    private void startHidingTimeBar(){
        mListener.onControlSystemUI(true);
        mTimeBar.setVisibility(View.INVISIBLE);
        if (mOnlyShowTimeBar) {
            mOnlyShowTimeBar = false;
            mControlListenr.updatePlayerControlShowState(false);
        }
    }

    private void cancelHidingTimgeBar(){
        handler.removeCallbacks(startHidingTimebarRunnable);
        mTimeBar.setAnimation(null);
    }

    private void startHideAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.startAnimation(hideAnimation);
        }
    }

    private void cancelHiding() {
        handler.removeCallbacks(startHidingRunnable);
        mBackground.setAnimation(null);
        mTimeBar.setAnimation(null);
        mPlayPauseReplayView.setAnimation(null);
        mVideoBottomView.setAnimation(null);
    }

  //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
    public void setCanScrubbing(boolean enable) {
        mEnableScrubbing = enable;
        mTimeBar.setScrubbing(enable);
    }
    private void showTimeBarAndMovieView() {
        if (mIsLiveStreaming) {
            mVideoBottomView.setVisibility(View.INVISIBLE);
        }else {
            mVideoBottomView.setVisibility(View.VISIBLE);
        }
    }

    public void setIsLiveStreaming(boolean isLive) {
        mIsLiveStreaming = isLive;
    }
  //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
    @Override
    public void onAnimationStart(Animation animation) {
        //[BUGFIX]-begin by TCTNJ.jun.xie-nb,01/08/2016,ALM-1292446 begin
        hide(true);
        //[BUGFIX]-begin by TCTNJ.jun.xie-nb,01/08/2016,ALM-1292446 end
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        //[BUGFIX]-begin by TCTNJ.jun.xie-nb,01/08/2016,ALM-1292446 begin
        // Do nothing.
        //[BUGFIX]-begin by TCTNJ.jun.xie-nb,01/08/2016,ALM-1292446 end
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (hidden) {
            show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (super.onTouchEvent(event)) {
            return true;
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
        mGestureScanner.onTouchEvent(event);
//        switch (event.getAction()) {
//            /*
//            case MotionEvent.ACTION_DOWN:
//                cancelHiding();
//                if (mState == State.PLAYING || mState == State.PAUSED) {
//                    mListener.onPlayPause();
//                }
//                break;
//            */
//      //[BUGFIX]-begin by TCTNJ.ye.chen,02/10/2015,928280
//            case MotionEvent.ACTION_UP:
//                if (hidden) {
//                    show();
//                    return true;
//                }else{
//                    hide();
//                }
//                maybeStartHiding();
//                break;
//        }
//      //[BUGFIX]-begin by TCTNJ.ye.chen,02/10/2015,928280
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end
        handleFastForwardOrBackward(event); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796
        return true;
    }

    @Override
    protected void updateViews() {
        if (hidden) {
            return;
        }
      //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-02-01,PR1537480 begin
        if(mContext!=null)
        {
           ((MovieActivity)mContext).updatevideoLockItemVisiable();
        }
      //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-02-01,PR1537480 end
        //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        if (DlnaService.isShare) {
            //mLoopView.setVisibility(View.VISIBLE);//temp disabled for FR826631
            //mScreenModeExt.onShow();//temp disabled for FR826631
        }
        //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        super.updateViews();
        showTimeBarAndMovieView();
    }

    // TimeBar listener

    @Override
    public void onScrubbingStart() {
        cancelHiding();
        super.onScrubbingStart();
    }

    @Override
    public void onScrubbingMove(int time) {
        cancelHiding();
//[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-25,PR1490111 begin
        updateTimeBar();
//[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-25,PR1490111 end
        super.onScrubbingMove(time);
    }

    @Override
    public void onScrubbingEnd(int time, int trimStartTime, int trimEndTime) {
        maybeStartHiding(true);
        super.onScrubbingEnd(time, trimStartTime, trimEndTime);
    }

    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    @Override
    public void setViewEnabled(boolean isEnabled) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setPlayPauseReplayResume() {
        // TODO Auto-generated method stub
    }
    //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode

    //[FEATURE]-Add-BEGIN by TCTNB.wen.zhuang,12/10/2013,FR-550507,
    private boolean showingIcon = true;

    public void showTVIcon(boolean show) {
        if (show) {
            showingIcon = true;
            handler.post(startHidingRunnable);//
            // mControllerRewindAndForwardExt.setViewEnabled(false);
            //mLoopView.setVisibility(View.VISIBLE);//temp disabled for FR826631
            //mScreenModeExt.onShow();//temp disabled for FR826631

        } else {
            showingIcon = false;
            handler.removeCallbacks(startHidingRunnable);
            // mControllerRewindAndForwardExt.setViewEnabled(true);
            //mLoopView.setVisibility(View.INVISIBLE);//temp disabled for FR826631
            //mScreenModeExt.onHide();//temp disabled for FR826631
        }

    }

    public void hideTimer() {
        handler.post(startHidingRunnable);
    }
    //[FEATURE]-Add-END by TCTNB.wen.zhuang

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
    private void updateView(int imageResource, boolean enable, int toastMessageId) {
        mUpdateMenuListener.updateMenu(imageResource, enable);
//        mVideoLockBtn.setImageResource(imageResource);
//        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-27, PR958355 begin
//        if (mIsLocalFile) {
//            mLoopModeBtn.setEnabled(enable);
//        }
//        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-27, PR958355 end
//        mPlayControlBtn.setEnabled(enable);
//        mFastForward.setEnabled(enable);
//        mReverse.setEnabled(enable);
        mTimeBar.setProgressBarEnable(enable);
        showToast(toastMessageId, null);

        if (mControlListenr != null)
            mControlListenr.onVideoLockMode(mLockMode);
    }

    private Toast mToast = null;
    private void showToast(int toastMessageId, String toastMessage) {
        if (mToast != null) {
            mToast.cancel();
        }
        //[BUGFIX]-modify by TCTNJ,xinrong.wang, 2016-01-14,PR1383492 begin
        if (toastMessageId != -1) {
        	mToast=Toast.makeText(mContext, toastMessageId, Toast.LENGTH_SHORT);
        }
        if (toastMessage != null) {
    	  mToast=Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT);
        }
      //[BUGFIX]-add by TCTNJ,xinrong.wang, 2016-01-28,PR1527921 begin
        int orientaion = mContext.getResources().getConfiguration().orientation;
        if (orientaion == Configuration.ORIENTATION_PORTRAIT) {
            mToast.setGravity(android.view.Gravity.BOTTOM, 0, (int)mContext.getResources().getDimension(R.dimen.lock_toast_y_offset));
        } else if (orientaion == Configuration.ORIENTATION_LANDSCAPE) {
            int xoffset=mNavigationBarHeight/2;
            mToast.setGravity(android.view.Gravity.BOTTOM, xoffset,(int)mContext.getResources().getDimension(R.dimen.lock_toast_y_offset));
        }
      //[BUGFIX]-add by TCTNJ,xinrong.wang, 2016-01-28,PR1527921 end
        //mToast = new Toast(mContext);
        //mToast.setDuration(TOAST_DURATION);
        //RelativeLayout rl = new RelativeLayout(mContext);
        //rl.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        //        LayoutParams.WRAP_CONTENT));
        //TextView textView = new TextView(mContext);
        //textView.setTextColor(Color.WHITE);
        //textView.setBackgroundColor(Color.BLACK);
        //if (toastMessageId != -1) {
         //   textView.setText(toastMessageId);
        //}
        //if (toastMessage != null) {
        //    textView.setText(toastMessage);
        //}
        //textView.setPadding(40, 20, 40, 20);
        //rl.addView(textView);
        //mToast.setGravity(android.view.Gravity.BOTTOM, 0,
        //        (int)mContext.getResources().getDimension(R.dimen.lock_toast_y_offset));
        //mToast.setView(rl);
        //[BUGFIX]-modify by TCTNJ,xinrong.wang, 2016-01-14,PR1383492 end
        mToast.show();
    }

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459 begin
    public void updateLock(int lockMode) {
        mLockMode = lockMode;
        int imageResource;
        boolean enable = false;
        if (mLockMode == LOCK_MODE_OFF) {
            //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/26,PR1507936 begin
            imageResource = R.drawable.ic_video_unlock;//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-10,PR1059325
            //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/26,PR1507936 end
            enable = true;
        } else {
            imageResource = R.drawable.ic_lock;//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-10,PR1059325
            enable = false;
        }
//        mVideoLockBtn.setImageResource(imageResource);
//        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-27, PR958355 begin
//        if (mIsLocalFile) {
//            mLoopModeBtn.setEnabled(enable);
//        }
//        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-27, PR958355 end
//        mPlayControlBtn.setEnabled(enable);
//        mFastForward.setEnabled(enable);
//        mReverse.setEnabled(enable);
        mTimeBar.setProgressBarEnable(enable);
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459 end

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-27, PR958355 begin
    private boolean mIsLocalFile = false;
    public void updateLoopButton(boolean isLocalFile) {
        mIsLocalFile = isLocalFile;
        if (mLockMode == LOCK_MODE_OFF && mIsLocalFile) {
//            mLoopModeBtn.setEnabled(true);
        }
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-27, PR958355 end

    //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 begin
    public void show3DAudioIcon() {
        if (!m3DAudioEnable) return; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974
        handler.removeCallbacks(mHide3DAudioIconRunnable);
        m3DAudioIcon.setVisibility(View.VISIBLE);
        handler.postDelayed(mHide3DAudioIconRunnable, mHide3DAudioIconDelay);
    }
    //[FEATURE]-Modify by TCTNJ, dongliang.feng, 2015-03-30, CR962977 end

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-08, PR968503 begin
    public void hide3DAudioIcon() {
        if (!m3DAudioEnable) return; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-09, PR971974
        if (m3DAudioIcon.getVisibility() == View.VISIBLE) {
            handler.removeCallbacks(mHide3DAudioIconRunnable);
            m3DAudioIcon.setVisibility(View.INVISIBLE);
        }
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-08, PR968503 end

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 begin
    private void handleFastForwardOrBackward(MotionEvent event) {
        if (mLockMode == LOCK_MODE_OFF && mState == State.PLAYING) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mMoveDistanceX = event.getRawX();
                mMoveDistanceY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                mMoveDistanceX = event.getRawX() - mMoveDistanceX;
                mMoveDistanceY = Math.abs(event.getRawY() - mMoveDistanceY);
                if (mMoveDistanceY <= Y_EXCURSION && (int)Math.abs(mMoveDistanceX) > mScreenWidth / 2) {
                    handler.removeCallbacks(startHidingRunnable);
                    if (mMoveDistanceX > 0) {
                        mControlListenr.onVideoFastForwardOrBackward(true);
                    } else {
                        mControlListenr.onVideoFastForwardOrBackward(false);
                    }
                }
                break;
            default:
                break;
            }
        }
    }

    public void showTimeBar(boolean isForward, int seekTimeDelta) {
        int toastMessageId;
        if (isForward) {
            toastMessageId = R.string.fast_forward;
        } else {
            toastMessageId = R.string.fast_backward;
        }
        String toastMessage = String.format(this.getResources().getString(toastMessageId), (int)(seekTimeDelta / 1000));
        showToast(-1, toastMessage);

        if (mBackground.getVisibility() == View.INVISIBLE) {
            mControlListenr.updatePlayerControlShowState(true);
            mBackground.setVisibility(View.VISIBLE);
            mTimeBar.setVisibility(View.VISIBLE);
            mOnlyShowTimeBar = true;
            hidden = false;
            requestLayout();
        }
        maybeStartHiding(true);
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 end

    private UpdateMenuListener mUpdateMenuListener;
    public void setUpdateMenuListener(UpdateMenuListener listener){
        mUpdateMenuListener = listener;
    }

    public interface UpdateMenuListener{
        public void updateMenu(int imageResource, boolean enable);
        public void onBottomControlClicked(int control);
    }

    public void invalidateFavourite(boolean isFavorite){
        mVideoFavourite.setImageResource(isFavorite ? R.drawable.ic_fav_on : R.drawable.ic_fav);
        mVideoBottomView.invalidate();
    }

    public void invalidateLockMode(){
        //[BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-12-29,PR1240201 begin
        if(mState == State.PAUSED) {
            return;
        }
        //[BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-12-29,PR1240201 end

        if (mLockMode == LOCK_MODE_ON) {
            mLockMode = LOCK_MODE_OFF;
            //[BUGFIX]-begin by TCTNJ.jun.xie-nb,01/08/2016,ALM-1292446 begin
            handler.removeCallbacks(startHidingTimebarRunnable);
            //[BUGFIX]-begin by TCTNJ.jun.xie-nb,01/08/2016,ALM-1292446 end
            //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/26,PR1507936 begin
            updateView(R.drawable.ic_video_unlock, true, R.string.unlock_video);
            //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/26,PR1507936 end
            show();
            maybeStartHiding(true);
        }
        else if (mLockMode == LOCK_MODE_OFF) {
            mLockMode = LOCK_MODE_ON;
            updateView(R.drawable.ic_lock, false, R.string.lock_video);//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-10,PR1059325
            isLockHideSystemUi = true;
            hide(false);
            maybeStartHiding(false);
        }
    }

    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-17,PR1060028 begin
    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-12,PR1134047 begin
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-19,PR732345 begin
    public void updateVideoBottomView(LocalVideo item,boolean isLocalFile,boolean notInDataBase){
        if (!isLocalFile || notInDataBase) {
            hideVideoButtom();
            mVideoShare.setVisibility(View.GONE);
            mVideoFavourite.setVisibility(View.GONE);
        }
        if (item != null) {
            if (item.isDrm() == 1) {
                hideVideoButtom();
                if (!item.isSupportForward()) {
                    mVideoShare.setVisibility(View.GONE);
                }
            }
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-08,PR1758674 begin
            if (!GalleryUtils.isVideoTrimAvailable(item.getDetails().getDetail(MediaDetails.INDEX_DURATION))) {
                mVideoTrim.setVisibility(View.GONE);
            }
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-08,PR1758674 end
        }
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-19,PR732345 begin
  //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-12,PR1134047 end

    private void hideVideoButtom(){
        mVideoEdit.setVisibility(View.GONE);
        mVideoTrim.setVisibility(View.GONE);
    }
    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-17,PR1060028 end
}

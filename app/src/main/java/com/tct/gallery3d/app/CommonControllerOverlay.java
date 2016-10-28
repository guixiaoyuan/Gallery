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
/* 22/01/2015|dongliang.feng        |PR910551              |[Android5.0][Gallery_v5.1.4.1.0107.0][UI] */
/*           |                      |                      |The icons of pause/play/replay need to replace */
/* ----------|----------------------|----------------------|----------------- */
/* 30/01/2015|dongliang.feng        |PR919531              |[ERGO][Gallery feedback]Please don’t show this */
/*           |                      |                      |pause button in the middle of the screen when */
/*           |                      |                      |the video is playing the first time */
/* ----------|----------------------|----------------------|----------------- */
/* 05/02/2015|ye.chen               |FR908268              |[Video streaming]It shouldn't pause live TV and prompt whether resume video
/* ----------|----------------------|----------------------|----------------- */
/* 04/03/2015|dongliang.feng        |CR940102              |[Gallery_Ergo_5.1.9.pdf]Video Lock Function */
/* ----------|----------------------|----------------------|----------------- */
/* 17/04/2015|    jialiang.ren     |      PR-947308       |[5.0][Gallery] photo/video should be loaded directly in immersive mode*/
/* ----------|---------------------|----------------------|----------------------------------------------------------------------*/
/* 2016/01/26|  caihong.gu-nb      |  PR-1490980          | [REG][GAPP][Android 6.0][Gallery]The video's trim interface don't have trimming adjustment */
/*-----------|---------------------|----------------------|---------------------------------------------------------------------------------*/
/* 2016/02/03|  caihong.gu-nb      |  PR-1536602   |[Video Streaming]The progress bar doesn't disappear when playing live streaming */
/*-----------|---------------------|---------------|---------------------------------------------------------------------------------*/
/* 25/02/2016|    su.jiang         |  PR-1649886   |[Gallery] [video player] the video pause screen can't display after unlock.*/
/*-----------|---------------------|---------------|---------------------------------------------------------------------------*/
/* 03/11/2016|    su.jiang         |  PR-1623761   |[Video Streaming]The paused video streaming will play automatically after lock and unlock screen.*/
/*-----------|---------------------|---------------|-------------------------------------------------------------------------------------------------*/

package com.tct.gallery3d.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils; // MODIFIED by Yaoyu.Yang, 2016-08-05,BUG-2208330
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.view.TimeBar;

/**
 * The common playback controller for the Movie Player or Video Trimming.
 */
public abstract class CommonControllerOverlay extends FrameLayout implements
        ControllerOverlay,
        OnClickListener,
        TimeBar.Listener {

    protected enum State {
        PLAYING,
        PAUSED,
        ENDED,
        ERROR,
        LOADING
    }

    private static final float ERROR_MESSAGE_RELATIVE_PADDING = 1.0f / 6;

    protected Listener mListener;

    protected final View mBackground;
    protected TimeBar mTimeBar;

    protected View mMainView;
    protected final LinearLayout mLoadingView;
    protected final TextView mErrorView;
    protected final ImageView mPlayPauseReplayView;

    protected State mState;

    protected boolean mCanReplay = true;

    public void setSeekable(boolean canSeek) {
        mTimeBar.setSeekable(canSeek);
    }

    public CommonControllerOverlay(Context context) {
        super(context);

        mState = State.LOADING;
        // TODO: Move the following layout code into xml file.
        LayoutParams wrapContent =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams matchParent =
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mBackground = new View(context);
        mBackground.setBackgroundColor(context.getResources().getColor(R.color.darker_transparent));
        addView(mBackground, matchParent);

        // Depending on the usage, the timeBar can show a single scrubber, or
        // multiple ones for trimming.
        createTimeBar(context);
        addView(mTimeBar, wrapContent);
        mTimeBar.setContentDescription(
                context.getResources().getString(R.string.accessibility_time_bar));
        mLoadingView = new LinearLayout(context);
      //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-27,PR1490107 begin
        mLoadingView.setVisibility(View.INVISIBLE);
      //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-27,PR1490107 end
        mLoadingView.setOrientation(LinearLayout.VERTICAL);
        mLoadingView.setGravity(Gravity.CENTER_HORIZONTAL);
        ProgressBar spinner = new ProgressBar(context);
        spinner.setIndeterminate(true);
        mLoadingView.addView(spinner, wrapContent);
        TextView loadingText = createOverlayTextView(context);
        loadingText.setText(R.string.loading_video);
        mLoadingView.addView(loadingText, wrapContent);
        addView(mLoadingView, wrapContent);

        mPlayPauseReplayView = new ImageView(context);
        mPlayPauseReplayView.setContentDescription(
                context.getResources().getString(R.string.accessibility_play_video));
        mPlayPauseReplayView.setBackgroundResource(R.drawable.ic_video_play_big); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-22, PR910551
        mPlayPauseReplayView.setScaleType(ScaleType.CENTER);
        mPlayPauseReplayView.setFocusable(true);
        mPlayPauseReplayView.setClickable(true);
        mPlayPauseReplayView.setOnClickListener(this);
//        addView(mPlayPauseReplayView, wrapContent); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-30, PR919531

        mErrorView = createOverlayTextView(context);
        addView(mErrorView, matchParent);

        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setLayoutParams(params);
        hide(true);
    }
  //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
    public void removePlayPauseReplayView()
    {
        removeView(mPlayPauseReplayView);
    }
  //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
    abstract protected void createTimeBar(Context context);

    private TextView createOverlayTextView(Context context) {
        TextView view = new TextView(context);
        view.setGravity(Gravity.CENTER);
        view.setTextColor(0xFFFFFFFF);
        view.setPadding(0, 15, 0, 15);
        return view;
    }

    @Override
    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    @Override
    public void setCanReplay(boolean canReplay) {
        this.mCanReplay = canReplay;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void showPlaying() {
        mState = State.PLAYING;
        showMainView(mPlayPauseReplayView);
    }

    @Override
    public void showPaused() {
        mState = State.PAUSED;
        showMainView(mPlayPauseReplayView);//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-11,PR1623761
    }

    @Override
    public void showEnded() {
        mState = State.ENDED;
        if (mCanReplay) showMainView(mPlayPauseReplayView);
    }

    @Override
    public void showLoading() {
        mState = State.LOADING;
        showMainView(mLoadingView);
    }

    ///[BUGFIX]-ADD-BEGIN BY TSNJ.LIUDEKUAN ON 2016/01/12 FOR DEFECT1126560
    public boolean isLoading () {
        return mState == State.LOADING;
    }
    ///[BUGFIX]-ADD-END BY TSNJ.LIUDEKUAN ON 2016/01/12 FOR DEFECT1126560

    @Override
    public void showErrorMessage(String message) {
        mState = State.ERROR;
        int padding = (int) (getMeasuredWidth() * ERROR_MESSAGE_RELATIVE_PADDING);
        mErrorView.setPadding(
                padding, mErrorView.getPaddingTop(), padding, mErrorView.getPaddingBottom());
        mErrorView.setText(message);
        showMainView(mErrorView);
    }

    @Override
    public void setTimes(int currentTime, int totalTime,
            int trimStartTime, int trimEndTime) {
        mTimeBar.setTime(currentTime, totalTime, trimStartTime, trimEndTime);
    }

    public void hide(boolean isNeedHideTimebar) {
        mBackground.setVisibility(View.INVISIBLE);
        if(isNeedHideTimebar){
            mTimeBar.setVisibility(View.INVISIBLE);
        }
//        setVisibility(View.INVISIBLE); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102
        updateViews();
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-02-25,PR1649886 begin
        if (mState == State.PAUSED) {
            mPlayPauseReplayView.setVisibility(View.VISIBLE);
            updatePlayPauseView();
        } else {
            mPlayPauseReplayView.setVisibility(View.INVISIBLE);
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-02-25,PR1649886 end
        setFocusable(true);
        requestFocus();
    }

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-05,BUG-2208330*/
    public void resumePlayPauseReplayView() {
        mPlayPauseReplayView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.icon_resume));
    }
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    private void showMainView(View view) {
        mMainView = view;
        mErrorView.setVisibility(mMainView == mErrorView ? View.VISIBLE : View.INVISIBLE);
        mLoadingView.setVisibility(mMainView == mLoadingView ? View.VISIBLE : View.INVISIBLE);
//[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1126435 begin
         if(mLoadingView.getVisibility()==View.VISIBLE)
        {
               mPlayPauseReplayView.setVisibility(View.INVISIBLE);
        }else
        {
                mPlayPauseReplayView.setVisibility(
                     mMainView == mPlayPauseReplayView ? View.VISIBLE : View.INVISIBLE);
        }
//[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1126435 end
//        show();
         //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 begin
         updatePlayPauseView();
         //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 end
         updateViews();//[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-11,PR1623761
    }

    @Override
    public void show() {
        updateViews();
//        setVisibility(View.VISIBLE); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102
        setFocusable(false);
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            if (view == mPlayPauseReplayView) {
                if (mState == State.ENDED) {
                    if (mCanReplay) {
                        mListener.onReplay();
                    }
                } else if (mState == State.PAUSED || mState == State.PLAYING) {
                    mListener.onPlayPause();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (super.onTouchEvent(event)) {
            return true;
        }
        return false;
    }

    // The paddings of 4 sides which covered by system components. E.g.
    // +-----------------+\
    // | Action Bar | insets.top
    // +-----------------+/
    // | |
    // | Content Area | insets.right = insets.left = 0
    // | |
    // +-----------------+\
    // | Navigation Bar | insets.bottom
    // +-----------------+/
    // Please see View.fitSystemWindows() for more details.
    protected final Rect mWindowInsets = new Rect();

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        // We don't set the paddings of this View, otherwise,
        // the content will get cropped outside window
        mWindowInsets.set(insets);
        return true;
    }

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
        mBackground.layout(0, y - mTimeBar.getBarHeight(), w, y);
        mTimeBar.layout(pl, y - mTimeBar.getPreferredHeight(), w - pr, y);

        // Put the play/pause/next/ previous button in the center of the screen
//        layoutCenteredView(mPlayPauseReplayView, 0, 0, w, h); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-30, PR919531

        if (mMainView != null) {
            layoutCenteredView(mMainView, 0, 0, w, h);
        }
    }

    protected void layoutCenteredView(View view, int l, int t, int r, int b) {
        int cw = view.getMeasuredWidth();
        int ch = view.getMeasuredHeight();
        int cl = (r - l - cw) / 2;
        int ct = (b - t - ch) / 2;
        view.layout(cl, ct, cl + cw, ct + ch);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }
    //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/26,PR1490980 begin
    protected boolean mIsLiveStreaming = true;
    //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/26,PR1490980 end
    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/03,PR1536602 begin
    public void setIsLiveStream(boolean isLiveStream){
        mIsLiveStreaming = isLiveStream;
    }
    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/03,PR1536602 begin
    protected void updateViews() {
        mBackground.setVisibility(mIsLiveStreaming ? View.GONE : View.VISIBLE);
        mTimeBar.setVisibility(mIsLiveStreaming ? View.GONE : View.VISIBLE);
        updatePlayPauseView();//[BUGFIX]-Add by TCTNJ,su.jiang, 2016-02-25,PR1649886
    }

    private void updatePlayPauseView() {
        Resources resources = getContext().getResources();
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-22, PR910551 begin
        int imageResource = R.drawable.ic_video_replay;
        String contentDescription = resources.getString(R.string.accessibility_reload_video);
        if (mState == State.PAUSED) {
            imageResource = R.drawable.ic_video_play;
            contentDescription = resources.getString(R.string.accessibility_play_video);
        } else if (mState == State.PLAYING) {
            imageResource = R.drawable.ic_video_pause;
            contentDescription = resources.getString(R.string.accessibility_pause_video);
        }

        mPlayPauseReplayView.setBackgroundResource(imageResource);
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-22, PR910551 end
        mPlayPauseReplayView.setContentDescription(contentDescription);
//[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1126435 begin
               if(mLoadingView.getVisibility()==View.VISIBLE)
        {
               mPlayPauseReplayView.setVisibility(View.GONE);
        }else
        {
            mPlayPauseReplayView.setVisibility(
                    (mState != State.LOADING && mState != State.ERROR || mCanReplay)
                    ? View.VISIBLE : View.GONE);
        }
//[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1126435 end
        requestLayout();
    }

    // TimeBar listener

    @Override
    public void onScrubbingStart() {
        mListener.onSeekStart();
    }

    @Override
    public void onScrubbingMove(int time) {
        mListener.onSeekMove(time);
    }

    @Override
    public void onScrubbingEnd(int time, int trimStartTime, int trimEndTime) {
        mListener.onSeekEnd(time, trimStartTime, trimEndTime);
    }
}

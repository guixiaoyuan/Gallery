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
/* ----------|----------------------|---------------------|-------------------*/
/* 06/02/2015|qiang.ding1           |PR924708             |[Gallery]Video playback pause button disappears */
/* ----------|----------------------|---------------------|-------------------*/
/* 18/03/2015|    jialiang.ren     |      PR-947308       |[5.0][Gallery] photo/video should be loaded directly in immersive mode*/
/* ----------|---------------------|----------------------|----------------------------------------------------------------------*/
package com.tct.gallery3d.app;

import com.tct.gallery3d.app.view.TrimTimeBar;
import com.tct.gallery3d.common.ApiHelper;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/**
 * The controller for the Trimming Video.
 */
public class TrimControllerOverlay extends CommonControllerOverlay  {

    public TrimControllerOverlay(Context context) {
        super(context);
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-01-17,PR924708 begin
        LayoutParams wrapContent =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(mPlayPauseReplayView, wrapContent);
      //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-01-17,PR924708 end
    }

    @Override
    protected void createTimeBar(Context context) {
        mTimeBar = new TrimTimeBar(context, this);
    }

    private void hidePlayButtonIfPlaying() {
        if (mState == State.PLAYING) {
            mPlayPauseReplayView.setVisibility(View.INVISIBLE);
        }
        if (ApiHelper.HAS_OBJECT_ANIMATION) {
            mPlayPauseReplayView.setAlpha(1f);
        }
    }

    @Override
    public void showPlaying() {
        super.showPlaying();
        show();
        if (ApiHelper.HAS_OBJECT_ANIMATION) {
            // Add animation to hide the play button while playing.
            ObjectAnimator anim = ObjectAnimator.ofFloat(mPlayPauseReplayView, "alpha", 1f, 0f);
            anim.setDuration(200);
            anim.start();
            anim.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    hidePlayButtonIfPlaying();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    hidePlayButtonIfPlaying();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        } else {
            hidePlayButtonIfPlaying();
        }
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-18,PR947308 begin
    @Override
    public void showPaused() {
        super.showPaused();
        show();
    }

    @Override
    public void showEnded() {
        super.showEnded();
        show();
    }

    @Override
    public void showLoading() {
        super.showLoading();
        show();
    }

    @Override
    public void showErrorMessage(String message) {
        super.showErrorMessage(message);
        show();
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-18,PR947308 end

    @Override
    public void setTimes(int currentTime, int totalTime, int trimStartTime, int trimEndTime) {
        mTimeBar.setTime(currentTime, totalTime, trimStartTime, trimEndTime);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (super.onTouchEvent(event)) {
            return true;
        }

        // The special thing here is that the State.ENDED include both cases of
        // the video completed and current == trimEnd. Both request a replay.
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mState == State.PLAYING || mState == State.PAUSED) {
                    mListener.onPlayPause();
                } else if (mState == State.ENDED) {
                    if (mCanReplay) {
                        mListener.onReplay();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/5/11, FR-824326 No WiFi Display Extension Mode
    @Override
    public void setViewEnabled(boolean isEnabled) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setPlayPauseReplayResume() {
        // TODO Auto-generated method stub
    }
    //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/5/11, FR-824326 No WiFi Display Extension Mode
}

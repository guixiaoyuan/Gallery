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
/* ----------|----------------------|----------------------|----------------- */
/* 20/11/2014|chengqun.sun            |FR826631              |Multi screen interaction*/
/* ----------|----------------------|----------------------|----------------- */
/* 05/02/2015|ye.chen               |FR908268              | [Video streaming]It shouldn't pause live TV and prompt whether resume video
/* ----------|----------------------|----------------------|----------------- */
/* 13/02/2015|jian.pan1             | PR929635             |inconsistent with GD design
/* ----------|----------------------|----------------------|----------------- */
/* 04/03/2015|dongliang.feng        |CR940102              |[Gallery_Ergo_5.1.9.pdf]Video Lock Function */
/* ----------|----------------------|----------------------|----------------- */
/* 03/07/2016| jian.pan1            |[ALM]Defect:1719258   |[GAPP][Android 6.0][Gallery]The trimmed time of video is different from trimming time of video
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.android.gallery3d.app.DlnaService;
import com.tct.gallery3d.R;
import com.tct.gallery3d.common.Utils;

/**
 * The time bar view, which includes the current and total time, the progress
 * bar, and the scrubber.
 */
public class TimeBar extends View {

    public interface Listener {
        void onScrubbingStart();

        void onScrubbingMove(int time);

        void onScrubbingEnd(int time, int start, int end);
    }

    // Padding around the scrubber to increase its touch target
    private static final int SCRUBBER_PADDING_IN_DP = 10;

    // The total padding, top plus bottom
    private static final int V_PADDING_IN_DP = 30;

    private static final int TEXT_SIZE_IN_DP = 14;

    protected final Listener mListener;

    // the bars we use for displaying the progress
    protected final Rect mProgressBar;
    protected final Rect mPlayedBar;

    protected final Paint mProgressPaint;
    protected final Paint mPlayedPaint;
    protected final Paint mTimeTextPaint;

    protected final Bitmap mScrubber;
    protected int mScrubberPadding; // adds some touch tolerance around the
                                    // scrubber

    protected int mScrubberLeft;
    protected int mScrubberTop;
    protected int mScrubberCorrection;
    protected boolean mScrubbing;
    protected boolean mShowTimes;
    protected boolean mShowScrubber;
  //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
    protected boolean mEnableScrubbing;

    protected int mTotalTime;
    protected int mCurrentTime;

    protected final Rect mTimeBounds;

    protected int mVPaddingInPx;
    protected boolean mPause = false; //[FEATURE]-by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    public TimeBar(Context context, Listener listener) {
        super(context);
        mListener = Utils.checkNotNull(listener);

        mShowTimes = true;
        mShowScrubber = true;

        mProgressBar = new Rect();
        mPlayedBar = new Rect();

        mProgressPaint = new Paint();
        mProgressPaint.setColor(0xFF808080);
        mPlayedPaint = new Paint();
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-13,PR929635 begin
        mPlayedPaint.setColor(0xFF00BCD4);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-13,PR929635 end

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float textSizeInPx = metrics.density * TEXT_SIZE_IN_DP;
        mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setColor(0xFFCECECE);
        mTimeTextPaint.setTextSize(textSizeInPx);
        mTimeTextPaint.setTextAlign(Paint.Align.CENTER);

        mTimeBounds = new Rect();
        mTimeTextPaint.getTextBounds("0:00:00", 0, 7, mTimeBounds);

        mScrubber = BitmapFactory.decodeResource(getResources(), R.drawable.scrubber_knob);
        mScrubberPadding = (int) (metrics.density * SCRUBBER_PADDING_IN_DP);

        mVPaddingInPx = (int) (metrics.density * V_PADDING_IN_DP);
    }

    private void update() {
        mPlayedBar.set(mProgressBar);

        if (mTotalTime > 0) {
            mPlayedBar.right =
                    mPlayedBar.left + (int) ((mProgressBar.width() * (long) mCurrentTime) / mTotalTime);
        } else {
            mPlayedBar.right = mProgressBar.left;
        }

        if (!mScrubbing) {
            mScrubberLeft = mPlayedBar.right - mScrubber.getWidth() / 2;
        }
        invalidate();
    }

    /**
     * @return the preferred height of this view, including invisible padding
     */
    public int getPreferredHeight() {
        return mTimeBounds.height() + mVPaddingInPx + mScrubberPadding;
    }

    /**
     * @return the height of the time bar, excluding invisible padding
     */
    public int getBarHeight() {
        return mTimeBounds.height() + mVPaddingInPx;
    }

    public void setTime(int currentTime, int totalTime,
            int trimStartTime, int trimEndTime) {
         //[BUGFIX]-del by TCTNJ,qiang.ding1, 2014-11-26,PR854772 begain
//        if (mCurrentTime == currentTime && mTotalTime == totalTime) {
//            return;
//        }
         //[BUGFIX]-del by TCTNJ,qiang.ding1, 2014-11-26,PR854772 end
        //[FEATURE]-Add-BEGIN by TCTNB.wen.zhuang,12/10/2013,FR-550507,
        /*else */if (DlnaService.isShare) {
            if (mPause) {
                //[BUGFIX]-Mod-BEGIN by TCTNB.yubin.ying,04/04/2014,624747,
                //setEnableScrube(false);//temp disabled for FR826631
            } else {
                //setEnableScrube(true);//temp disabled for FR826631
                //[BUGFIX]-Mod-END by TCTNB.yubin.ying
            }
        }
        //[FEATURE]-Add-END by TCTNB.wen.zhuang
        mCurrentTime = currentTime;
        mTotalTime = totalTime;
        update();
    }

    private boolean inScrubber(float x, float y) {
        int scrubberRight = mScrubberLeft + mScrubber.getWidth();
        int scrubberBottom = mScrubberTop + mScrubber.getHeight();
        return mScrubberLeft - mScrubberPadding < x && x < scrubberRight + mScrubberPadding
                && mScrubberTop - mScrubberPadding < y && y < scrubberBottom + mScrubberPadding;
    }

    private void clampScrubber() {
        int half = mScrubber.getWidth() / 2;
        int max = mProgressBar.right - half;
        int min = mProgressBar.left - half;
        mScrubberLeft = Math.min(max, Math.max(min, mScrubberLeft));
    }

    private int getScrubberTime() {
        return (int) ((long) (mScrubberLeft + mScrubber.getWidth() / 2 - mProgressBar.left)
                * mTotalTime / mProgressBar.width());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        if (!mShowTimes && !mShowScrubber) {
            mProgressBar.set(0, 0, w, h);
        } else {
            int margin = mScrubber.getWidth() / 3;
            if (mShowTimes) {
                margin += mTimeBounds.width();
            }
            int progressY = (h + mScrubberPadding) / 2;
            mScrubberTop = progressY - mScrubber.getHeight() / 2 + 1;
            mProgressBar.set(
                    getPaddingLeft() + margin, progressY,
                    w - getPaddingRight() - margin, progressY + 4);
        }
        update();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw progress bars
        canvas.drawRect(mProgressBar, mProgressPaint);
        canvas.drawRect(mPlayedBar, mPlayedPaint);

        // draw scrubber and timers
      //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
        if (mShowScrubber && mEnableScrubbing) {
            canvas.drawBitmap(mScrubber, mScrubberLeft, mScrubberTop, null);
        }
        if (mShowTimes) {
           if(mEnableScrubbing){
               canvas.drawText(
                       stringForTime(mCurrentTime),
                               mTimeBounds.width() / 2 + getPaddingLeft(),
                               mTimeBounds.height() + mVPaddingInPx / 2 + mScrubberPadding + 1,
                       mTimeTextPaint);
               // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-07,Defect:1719258 begin
               if (mTotalTime < 1000) {
                   mTotalTime = 1000;
               }
               // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-07,Defect:1719258 end
               canvas.drawText(
                       stringForTime(mTotalTime),
                               getWidth() - getPaddingRight() - mTimeBounds.width() / 2,
                               mTimeBounds.height() + mVPaddingInPx / 2 + mScrubberPadding + 1,
                       mTimeTextPaint);
            }else{
                canvas.drawText("--:--",mTimeBounds.width() / 2 + getPaddingLeft(),
                        mTimeBounds.height() + mVPaddingInPx / 2 + mScrubberPadding + 1,mTimeTextPaint);
                canvas.drawText("--:--",getWidth() - getPaddingRight() - mTimeBounds.width() / 2,
                                mTimeBounds.height() + mVPaddingInPx / 2 + mScrubberPadding + 1,
                        mTimeTextPaint);
           }
        }
      //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
        if (!mProgressBarEnable) {
            return true;
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end

        //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
        if (mShowScrubber && mEnableScrubbing) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    mScrubberCorrection = inScrubber(x, y)
                            ? x - mScrubberLeft
                            : mScrubber.getWidth() / 2;
                    mScrubbing = true;
                    mListener.onScrubbingStart();
                }
                // fall-through
                case MotionEvent.ACTION_MOVE: {
                    mScrubberLeft = x - mScrubberCorrection;
                    clampScrubber();
                    mCurrentTime = getScrubberTime();
                    mListener.onScrubbingMove(mCurrentTime);
                    invalidate();
                    return true;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP: {
                    mListener.onScrubbingEnd(getScrubberTime(), 0, 0);
                    mScrubbing = false;
                    return true;
                }
            }
        }
        return false;
    }

    protected String stringForTime(long millis) {
        int totalSeconds = (int) millis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return String.format("%02d:%02d", minutes, seconds).toString();
        }
    }
  //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
    public void setScrubbing(boolean enable) {
        mEnableScrubbing = enable;
    }
  //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
    public void setSeekable(boolean canSeek) {
        mShowScrubber = canSeek;
    }

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 begin
    private boolean mProgressBarEnable = true;
    public void setProgressBarEnable(boolean enable) {
        mProgressBarEnable = enable;
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102 end
}

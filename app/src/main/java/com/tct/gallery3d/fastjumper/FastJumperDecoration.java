/*
 * Copyright (C) 2016 sin3hz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tct.gallery3d.fastjumper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.tct.gallery3d.R;

import static com.tct.gallery3d.fastjumper.FastJumper.STATE_DRAGGING;
import static com.tct.gallery3d.fastjumper.FastJumper.STATE_GONE;
import static com.tct.gallery3d.fastjumper.FastJumper.STATE_HIDING;
import static com.tct.gallery3d.fastjumper.FastJumper.STATE_VISIBLE;

class FastJumperDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = FastJumperDecoration.class.getSimpleName();

    private FastJumper mFastJumper;
    private RecyclerView mRecyclerView;
    private FastJumper.Callback mCallback;

    private Drawable mThumbDrawable;
    private int mThumbHeight;
    private int mThumbWidth;
    private Rect mThumbBounds = new Rect();

    private static final int[] THUMB_STATE_PRESSED = new int[]{android.R.attr.state_pressed};
    private static final int[] THUMB_STATE_NORMAL = new int[]{};

    private float mProgress;
    private int mState;

    private static final int THUMB_HIDE_DURATION = 150;
    private static final int THUMB_SHOW_DURATION = 100;

    private static final int SCROLL_IDLE_HIDE_DELAY = 2500;
    private static final int ACTION_UP_HIDE_DELAY = 3000;

    private float mThumbOffset = 1f;
    private float mThumbTop;
    private ValueAnimator mThumbShowHideAnimator;

    private int mTouchSlop;
    private Rect mSelfBounds = new Rect();
    private Rect mDirtyBounds = new Rect();
    private Rect mTempRect = new Rect();

    private static final int LABEL_SHOW_DURATION = 100;
    private static final int LABEL_HIDE_DURATION = 150;
    private static final int LABEL_EXPAND_DURATION = 200;

    private LabelDrawable mLabelDrawable;
    private Rect mLabelBounds = new Rect();
    private static final int LABEL_OFFSET = dp2px(12);
    private static final int LABEL_OFFSET_TOUCH = dp2px(12);
    private float mLabelOffset = LABEL_OFFSET_TOUCH;
    private float mLabelAlpha;
    private ValueAnimator mLabelExpandAnimator;
    private ValueAnimator mLabelShowHideAnimator;
    private boolean mLabelVisible;
    private boolean mLabelHiding;
    private boolean mAlwaysShow;

    private static final Interpolator SCROLL_SETTLE_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final int MIN_SCROLL_DURATION = 250;
    private static final int MIN_SCROLL_PIXEL = dp2px(8);
    private Scroller mThumbScroller;
    private float mSpeedPerPixel;

    public static int dp2px(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void setupScroller() {
        mThumbScroller = new Scroller(mRecyclerView.getContext(), SCROLL_SETTLE_INTERPOLATOR);
        mSpeedPerPixel = ScrollerHelper.calculateSpeedPerPixel(mRecyclerView.getContext().getResources().getDisplayMetrics());
    }

    private void setupAnimators() {
        mThumbShowHideAnimator = ValueAnimator.ofFloat();
        mThumbShowHideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                setThumbLeftOffset(value);
            }
        });
        mThumbShowHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mThumbOffset == 1f) {
                    setStateInternal(STATE_GONE);
                }
            }
        });
        mLabelExpandAnimator = ValueAnimator.ofFloat();
        mLabelExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                setLabelOffset(value);
            }
        });
        mLabelExpandAnimator.addListener(new AnimatorListenerAdapter() {

        });
        mLabelShowHideAnimator = ValueAnimator.ofFloat();
        mLabelShowHideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                setLabelAlpha(value);
            }
        });
        mLabelShowHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mLabelAlpha == 0) {
                    mLabelVisible = false;
                }
                mLabelHiding = false;
            }
        });
    }

    public FastJumperDecoration(FastJumper jumper) {
        mFastJumper = jumper;
        mCallback = mFastJumper.getCallback();
        mRecyclerView = mFastJumper.getRecyclerView();
        Context context = mRecyclerView.getContext();
        mTouchSlop = 2;
        mThumbDrawable = ContextCompat.getDrawable(context, R.drawable.ic_drag_token);
        if (FastJumper.DEBUG) Log.d(TAG, "context==" + context);
        mThumbHeight = dp2px(24);
        mThumbWidth = dp2px(16);
        mLabelDrawable = new LabelDrawable();
        setupAnimators();
        setupScroller();
    }

    void destroyCallbacks() {
        mRecyclerView.removeCallbacks(mThumbSettleRunnable);
        mRecyclerView.removeCallbacks(mHideRunnable);
    }

    private Runnable mThumbSettleRunnable = new Runnable() {
        @Override
        public void run() {
            if (mThumbScroller.computeScrollOffset()) {
                setThumbTop(mThumbScroller.getCurrY());
            }
            if (!mThumbScroller.isFinished()) {
                postThumbSettleRunnable();
            } else {
                if (mState == STATE_VISIBLE) {
                    scheduleHideForScrollIdle();
                }
            }
        }
    };

    private void postThumbSettleRunnable() {
        ViewCompat.postOnAnimation(mRecyclerView, mThumbSettleRunnable);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint mThumb = new Paint(Paint.ANTI_ALIAS_FLAG);

        int padding = dp2px(6);
        int circleRidius = dp2px(2);
        float shadowBorder = 0.5f;

        int whiteColor = ContextCompat.getColor(mRecyclerView.getContext(), R.color.white);
        int blackColor = ContextCompat.getColor(mRecyclerView.getContext(), R.color.black);

        mThumb.setColor(whiteColor);
        borderPaint.setColor(blackColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAlpha((int) (255 * 0.12));

        mDirtyBounds.setEmpty();
        if (mState == STATE_GONE) {
            return;
        }
        c.save();
        updateBounds(c);

        //draw hollow black Rect
        c.drawRoundRect(mThumbBounds.left - shadowBorder,
                mThumbBounds.top - padding - shadowBorder,
                mThumbBounds.left + mThumbWidth + shadowBorder,
                mThumbBounds.top + mThumbHeight + padding + shadowBorder,
                circleRidius, circleRidius, borderPaint);
        //draw fill white Rect
        c.drawRoundRect(mThumbBounds.left,
                mThumbBounds.top - padding,
                mThumbBounds.left + mThumbWidth,
                mThumbBounds.top + mThumbHeight + padding,
                circleRidius, circleRidius, mThumb);

        c.clipRect(mSelfBounds);
        mThumbDrawable.setBounds(mThumbBounds);
        mThumbDrawable.draw(c);
        mLabelDrawable.setBounds(mLabelBounds);
        mLabelDrawable.draw(c);
        c.restore();
    }

    private float mLastMotionY;
    private static final float RIGHT_DISTANCE_PROPORTION_THRESHOLD = 0.1f;

    private float computeProgress() {
        float progress = mThumbTop / (getContentHeight() - mThumbHeight);
        return isReverseLayout() ? 1 - progress : progress;
    }

    private float computeProgressForLabel(float progress) {

        if (isReverseLayout()) {
            progress += ((getContentHeight() - mThumbTop)) / mCallback.getScrollRange();
        } else {
            progress += mThumbTop / mCallback.getScrollRange();
        }
        return progress;
    }

    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
        return onTouchEvent(rv, event);
    }

    public boolean onTouchEvent(RecyclerView rv, MotionEvent event) {
        if (mState == STATE_GONE) {
            return false;
        }
        if (!isEnabled()) {
            return false;
        }
        if (isScrollSettling()) {
            return false;
        }

        if (mRecyclerView.hasPendingAdapterUpdates()) {
            return false;
        }
        int padding = dp2px(4);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!isPointInNearThumbBackGround(event.getX(), event.getY())) {
                    return false;
                }
                unScheduleHide();
                showThumb();
                setStateInternal(STATE_DRAGGING);
                mLastMotionY = event.getY();
                float progress = computeProgress();
                if (isSectionEnable()) {
                    updateLabel(progress);
                    showLabel();
                    //expandLabel();
                }
                rv.requestDisallowInterceptTouchEvent(true);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mState != STATE_DRAGGING) {
                    return false;
                }
                float dy = event.getY() - mLastMotionY;
                if (Math.abs(dy) < mTouchSlop) {
                    return true;
                }
                int recyclerViewWidth = mRecyclerView.getWidth() - mRecyclerView.getPaddingRight();
                float moveFraction = 1f;
                float rightDistance = recyclerViewWidth - event.getX();
                float rightDistanceProportion = rightDistance / recyclerViewWidth;
                if (rightDistanceProportion > RIGHT_DISTANCE_PROPORTION_THRESHOLD) {
                    moveFraction = (float) Math.pow(moveFraction - (rightDistanceProportion - RIGHT_DISTANCE_PROPORTION_THRESHOLD), 2);
                }
                dy = moveFraction * dy;
                float thumbTop = dy + mThumbTop;
                updateThumbTop(thumbTop, false);

                float progress = computeProgress();
                if (isSectionEnable()) {
                    updateLabel(progress);
                }
                mCallback.scrollTo(progress);
                dispatchScroll(progress);
                mLastMotionY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mState != STATE_DRAGGING) {
                    return false;
                }
                rv.requestDisallowInterceptTouchEvent(false);
                setStateInternal(STATE_VISIBLE);
                scheduleHideForActionUp();
                collapseLabel();
                return true;
            }
        }
        return true;
    }

    private void updateLabel(float progress) {
        float labelProgress = computeProgressForLabel(progress);

        String section = mCallback.getSection(labelProgress);
        setLabel(section);
    }

    private void updateBounds(Canvas c) {
        updateThumbBounds(mThumbBounds);
        updateLabelBounds(mLabelBounds);
        updateSelfBounds();
    }

    private void updateSelfBounds() {
        mSelfBounds.set(
                mRecyclerView.getPaddingLeft(),
                mRecyclerView.getPaddingTop(),
                mRecyclerView.getPaddingLeft() + getContentWidth(),
                mRecyclerView.getPaddingTop() + getContentHeight() + dp2px(24)
        );
    }

    private void updateThumbBounds(Rect bounds) {
        int paddingRight = dp2px(8);
        int padding = dp2px(4);
        bounds.top = getThumbTop() + padding;
        bounds.bottom = bounds.top + mThumbHeight;
        bounds.left = getThumbLeft() - paddingRight;
        bounds.right = bounds.left + mThumbWidth;
    }

    private void updateThumbTop(float top, boolean animate) {
        top = Math.max(0, Math.min(top, getContentHeight() - mThumbHeight));
        if (!isScrollSettling() && animate) {
            if (Math.abs(top - mThumbTop) > MIN_SCROLL_PIXEL) {
                unScheduleHide();
                interruptDragging();
                setStateInternal(STATE_VISIBLE);
                hideLabel();
                int startY = (int) mThumbTop;
                int dy = (int) (top - mThumbTop);
                int duration = computeScrollDuration(dy);
                duration = Math.max(MIN_SCROLL_DURATION, duration);
                mThumbScroller.startScroll(0, startY, 0, dy, duration);
                postThumbSettleRunnable();
            } else {
                setThumbTop(top);
            }
        } else if (isScrollSettling()) {
            int dy = (int) (top - mThumbScroller.getCurrY());
            int duration = computeScrollDuration(dy);
            int timeLeft = mThumbScroller.getDuration() - mThumbScroller.timePassed();
            if (duration - timeLeft > MIN_SCROLL_DURATION) {
                mThumbScroller.extendDuration(duration - timeLeft);
            }
            mThumbScroller.setFinalY((int) top);
        } else {
            setThumbTop(top);
        }
    }

    private void interruptDragging() {
        if (mState == STATE_DRAGGING) {
            cancelTouch();
        }
    }

    private void cancelTouch() {
        final long now = SystemClock.uptimeMillis();
        final MotionEvent cancelEvent = MotionEvent.obtain(now, now,
                MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
        mRecyclerView.onTouchEvent(cancelEvent);
        cancelEvent.recycle();
        mRecyclerView.requestDisallowInterceptTouchEvent(false);
    }

    private boolean isScrollSettling() {
        return !mThumbScroller.isFinished();
    }

    private int computeScrollDuration(int dy) {
        return (int) (Math.ceil(Math.abs(dy) * mSpeedPerPixel));
    }

    public void invalidate(boolean is) {
        if (mState == STATE_GONE || mState == STATE_HIDING) {
            return;
        }
        updatePosition(is);
    }

    private void setThumbTop(float top) {
        updateThumbBounds(mTempRect);
        mDirtyBounds.union(mTempRect);
        mThumbTop = top;
        updateThumbBounds(mTempRect);
        mDirtyBounds.union(mTempRect);
        invalidateRecyclerView();
    }

    private void setThumbLeftOffset(float offset) {
        updateThumbBounds(mTempRect);
        mDirtyBounds.union(mTempRect);
        mThumbOffset = offset;
        updateThumbBounds(mTempRect);
        mDirtyBounds.union(mTempRect);
        invalidateRecyclerView();
    }

    private void invalidateRecyclerView() {
        mRecyclerView.invalidate(mDirtyBounds);
    }

    private boolean isPointInThumbBounds(float x, float y) {
        return mThumbBounds.contains((int) x, (int) y);
    }

    private boolean isPointInNearThumbBackGround(float x, float y) {
        int padding = dp2px(4);
        int nearPadding = dp2px(24);
        if (x >= (mThumbBounds.left - padding - nearPadding) &&
                x <= (mThumbBounds.left + mThumbWidth + padding + nearPadding) &&
                y >= (mThumbBounds.top - padding - nearPadding) &&
                y <= (mThumbBounds.top + mThumbHeight + padding + nearPadding)) {
            return true;
        }
        return false;
    }

    private int getThumbLeft() {
        return mRecyclerView.getWidth() - mThumbWidth - mRecyclerView.getPaddingRight() + (int) (mThumbOffset * mThumbWidth);
    }

    private int getThumbTop() {
        return (int) (mThumbTop + mRecyclerView.getPaddingTop() + dp2px(8));
    }

    private int getContentHeight() {
        return mRecyclerView.getHeight() - mRecyclerView.getPaddingTop() - mRecyclerView.getPaddingBottom() - dp2px(24);
    }

    private int getContentWidth() {
        return mRecyclerView.getWidth() - mRecyclerView.getPaddingLeft() - mRecyclerView.getPaddingRight();
    }

    public float getProgress() {
        return mProgress;
    }

    public int getState() {
        return mState;
    }

    private void setStateInternal(int state) {
        if (mState == state) {
            return;
        }
        mState = state;
        dispatchStateChanged();
        setThumbState();
    }

    private void setThumbState() {
        int[] stateSet = mState == STATE_DRAGGING ? THUMB_STATE_PRESSED : THUMB_STATE_NORMAL;
        if (mThumbDrawable != null && mThumbDrawable.isStateful()) {
            mThumbDrawable.setState(stateSet);
        }
    }

    private void scheduleHideForScrollIdle() {
        scheduleHide(SCROLL_IDLE_HIDE_DELAY);
    }

    private void scheduleHideForActionUp() {
        scheduleHide(ACTION_UP_HIDE_DELAY);
    }

    private void scheduleHide(int delay) {
        unScheduleHide();
        mRecyclerView.postDelayed(mHideRunnable, delay);
    }

    private void unScheduleHide() {
        mRecyclerView.removeCallbacks(mHideRunnable);
    }

    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideLabel();
            if (!mAlwaysShow) {
                hideThumb();
            }
        }
    };

    private boolean isEnabled() {
        return mCallback.isEnabled();
    }

    private boolean isReverseLayout() {
        return mCallback.isReverseLayout();
    }

    private boolean isSectionEnable() {
        return mCallback.isSectionEnable();
    }

    private void updatePosition(boolean animate) {
        float progress = mCallback.getScrollOffset() / (float) mCallback.getScrollRange();
        float top = (isReverseLayout() ? 1 - progress : progress) * (getContentHeight() - mThumbHeight);
        updateThumbTop(top, animate);
        dispatchScroll(progress);
    }

    private void updateLabelBounds(Rect bounds) {
        int padding = dp2px(2);
        int left = (int) (mThumbBounds.left - mLabelOffset - mLabelDrawable.getIntrinsicWidth() * mLabelAlpha) - dp2px(8);
        //int top = mThumbBounds.centerY() - mLabelDrawable.getIntrinsicHeight() / 2;
        int top = getThumbTop() - padding;
        int right = (int) (mThumbBounds.left - mLabelOffset) - dp2px(8);
        int bottom = top + dp2px(36);

        bounds.set(left, top, right, bottom);
    }

    private void setLabel(String label) {
        float avail = getContentWidth() - mThumbWidth - LABEL_OFFSET_TOUCH;
        label = mLabelDrawable.ellipsizeText(avail, label);
        mLabelDrawable.setText(label);
    }

    private void hideLabel() {
        if (!mLabelVisible || mLabelHiding) {
            return;
        }
        mLabelHiding = true;
        mLabelShowHideAnimator.setFloatValues(mLabelAlpha, 0);
        mLabelShowHideAnimator.setDuration(LABEL_HIDE_DURATION);
        mLabelShowHideAnimator.start();
    }

    private void showLabel() {
        mLabelVisible = true;
        mLabelHiding = false;
        mLabelShowHideAnimator.setFloatValues(mLabelAlpha, 1);
        mLabelShowHideAnimator.setDuration(LABEL_SHOW_DURATION);
        mLabelShowHideAnimator.start();
    }

    private void expandLabel() {
        mLabelExpandAnimator.setDuration(LABEL_EXPAND_DURATION);
        mLabelExpandAnimator.setFloatValues(mLabelOffset, LABEL_OFFSET_TOUCH);
        mLabelExpandAnimator.start();
    }

    private void collapseLabel() {
        if (!mLabelVisible) {
            return;
        }
        mLabelExpandAnimator.setDuration(LABEL_EXPAND_DURATION);
        mLabelExpandAnimator.setFloatValues(mLabelOffset, LABEL_OFFSET);
        mLabelExpandAnimator.start();
    }

    private void setLabelOffset(float offset) {
        updateLabelBounds(mTempRect);
        mDirtyBounds.union(mTempRect);
        mLabelOffset = offset;
        updateLabelBounds(mTempRect);
        mDirtyBounds.union(mTempRect);
        invalidateRecyclerView();
    }

    private void setLabelAlpha(float alpha) {
        mLabelAlpha = alpha;
        mLabelDrawable.setAlpha((int) (255 * mLabelAlpha));
        mDirtyBounds.union(mLabelBounds);
        invalidateRecyclerView();
    }

    public void onScrolled(int dx, int dy) {
        if (FastJumper.DEBUG) Log.d(TAG, "onScrolled");
        if (dy == 0) return;
        if (mState == STATE_DRAGGING) return;
        if (!isEnabled()) return;

        updatePosition(false);
        showThumb();
        hideLabel();
        scheduleHideForScrollIdle();
    }

    private void dispatchStateChanged() {
        mCallback.onStateChanged(mState);
        mFastJumper.dispatchOnStateChange(mState);
    }

    private void dispatchScroll(float progress) {
        mProgress = progress;
        mFastJumper.dispatchOnScroll(progress);
    }

    public boolean isAlwaysShow() {
        return mAlwaysShow;
    }

    public void setAlwaysShow(boolean alwaysShow) {
        if (mAlwaysShow == alwaysShow) {
            return;
        }
        mAlwaysShow = alwaysShow;
        if (!isEnabled()) {
            return;
        }
        showThumb();
    }

    private void showThumb() {
        if (mState != STATE_HIDING && mState != STATE_GONE) {
            return;
        }
        setStateInternal(STATE_VISIBLE);
        mThumbShowHideAnimator.setFloatValues(mThumbOffset, 0f);
        mThumbShowHideAnimator.setDuration(THUMB_SHOW_DURATION);
        mThumbShowHideAnimator.start();
    }

    private void hideThumb() {
        if (mState != STATE_VISIBLE) {
            return;
        }
        setStateInternal(STATE_HIDING);
        mThumbShowHideAnimator.setFloatValues(mThumbOffset, 1f);
        mThumbShowHideAnimator.setDuration(THUMB_HIDE_DURATION);
        mThumbShowHideAnimator.start();
    }

    public void show() {
        if (mState == STATE_HIDING || mState == STATE_GONE) {
            showThumb();
            scheduleHideForScrollIdle();
        }
    }

    public void hide() {
        if (mState == STATE_VISIBLE || mState == STATE_DRAGGING) {
            if (mState == STATE_DRAGGING) {
                interruptDragging();
            }
            hideThumb();
        }
    }
}

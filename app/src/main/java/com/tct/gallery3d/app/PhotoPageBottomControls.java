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
/* 21/01/2015|    jialiang.ren      |      PR-907920       |[Android5.0][Gallery_v5.1.4.1.0106.0][UI]   */
/*           |                      |                      |The line along with the buttons need to hide*/
/* ----------|----------------------|----------------------|--------------------------------------------*/
/* 22/01/2015|    jialiang.ren      |      PR-904487       |[Ergo][Gallery][DEV]Gallery Ergo 5.1.4 - immersive mode */
/* ----------|----------------------|----------------------|------------------------------------------------------- */
/* 30/01/2015|    jialiang.ren      |      PR-915208       |Android 5.0][Gallery_v5.1.4.1.0108.0]The preview*/
/*                                                          screen will shake when clicking the picture     */
/* ----------|----------------------|----------------------|------------------------------------------------*/
/* 03/16/2015| jian.pan1            | PR916254             |[GenericApp][Gallery]HDPI resolution adaptation
/* ----------|----------------------|----------------------|----------------------------------------------- */
/* 08/04/2015|dongliang.feng        |PR952224              |[UI][Gallery]The menu list not under the screen */
/* ----------|----------------------|----------------------|----------------- */
/* 11/06/2015 |    jialiang.ren     |      PR-996464         |[Gallery]actionbar/editingbar hiding/show animation*/
/*------------|---------------------|------------------------|---------------------------------------------------*/
/* 18/06/2015 |    jialiang.ren     |      PR-1026487         |[Android 5.1][Gallery_v5.1.13.1.0209.0][Monitor]*/
/*                                                             The buttons will disappeared in landscape mode  */
/*------------|---------------------|-------------------------|------------------------------------------------*/
/* 06/19/2015| jian.pan1            | PR1025559            |[Android 5.1][Gallery_v5.1.13.1.0208.0]The virtual key overlap the edit bar
/* ----------|----------------------|----------------------|----------------- */
/* 23/06/2015|dongliang.feng        |PR1027856             |[Android5.1][Gallery_v5.1.13.1.0209.0]Double-click inlager picture is invalid */
/* ----------|----------------------|----------------------|----------------- */
/* 29/06/2015|dongliang.feng        |PR1031018             |[SW][MMS]"Delete" and "edit" option doesn't work */
/* ----------|----------------------|----------------------|----------------- */
/* 07/07/2015 |    jialiang.ren     |      PR-1038777         |[GAPP][MMS][Gallery]Gallery display not completely*/
/*                                                             when use MMS add attachment via gallery           */
/*------------|---------------------|-------------------------|--------------------------------------------------*/

package com.tct.gallery3d.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.SystemBarTintManager.SystemBarConfig;
import com.tct.gallery3d.app.view.SmoothImageView;
import com.tct.gallery3d.util.ScreenUtils;

import java.util.HashMap;
import java.util.Map;

public class PhotoPageBottomControls implements OnClickListener {
    public interface Delegate {
        boolean canDisplayBottomControls();
        boolean canDisplayBottomControl(int control);
        boolean shouldDisplayBottomControl(int control);//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-11,PR996464
        void onBottomControlClicked(int control);
        void refreshBottomControlsWhenReady();
    }

    private Delegate mDelegate;
    private ViewGroup mParentLayout;
    private ViewGroup mContainer;
    private ImageView mFavourite;

    private boolean mContainerVisible = false;
    private Map<View, Boolean> mControlsVisible = new HashMap<View, Boolean>();

    private Animation mContainerAnimIn = new AlphaAnimation(0f, 1f);
    private Animation mContainerAnimOut = new AlphaAnimation(1f, 0f);
    private static final int CONTAINER_ANIM_DURATION_MS = 200;
    private int mPaddingFor2;
    private int mPaddingFor3;
    private AnimatorSet animatorSet = null;
    private float preAnimationEndAlpha = -1f;

    private AbstractGalleryActivity mContext;// [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-19,PR1025559

    public ViewGroup getContainer() {
        return mContainer;
    }

    public PhotoPageBottomControls(Delegate delegate, Context context, ViewGroup layout) {
        mContext = (AbstractGalleryActivity) context;// [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-19,PR1025559
        mDelegate = delegate;
        mParentLayout = layout;
        mPaddingFor2 = context.getResources().getDimensionPixelSize(
                R.dimen.photopage_bottom_padding_2);
        mPaddingFor3 = context.getResources().getDimensionPixelSize(
                R.dimen.photopage_bottom_padding_3);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainer = (ViewGroup) inflater.inflate(R.layout.photopage_bottom_controls, mParentLayout, false);
        mFavourite = (ImageView) mContainer.findViewById(R.id.photopage_favourite);

        resetBottomButtons();

        mParentLayout.addView(mContainer);

        for (int i = mContainer.getChildCount() - 1; i >= 0; i--) {
            View child = mContainer.getChildAt(i);
            child.setOnClickListener(this);
            mControlsVisible.put(child, false);
        }

        mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);
    }

    private void hide() {
        mContainer.clearAnimation();
        mContainerAnimOut.reset();
        mContainer.startAnimation(mContainerAnimOut);
        mContainer.setVisibility(View.INVISIBLE);
    }

    private void show() {
        mContainer.clearAnimation();
        mContainerAnimIn.reset();
        mContainer.startAnimation(mContainerAnimIn);
        mContainer.setVisibility(View.VISIBLE);
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-11,PR996464 begin
    public void refresh() {
        boolean visible = mDelegate.canDisplayBottomControls();
        boolean containerVisibilityChanged = (visible != mContainerVisible);
        if (containerVisibilityChanged) {
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-19,PR1025559 begin
            /*if (visible) {
                mContainer.setVisibility(View.VISIBLE);
            } else {
                mContainer.setVisibility(View.GONE);
            }*/
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-19,PR1025559 end
            mContainerVisible = visible;
        }
        if (!mContainerVisible) {
            return;
        }
        boolean isControlVisiable = false;
        for (View control : mControlsVisible.keySet()) {
            Boolean prevVisibility = mControlsVisible.get(control);
            boolean curVisibility = mDelegate.canDisplayBottomControl(control.getId());
            boolean shouldDisplay = mDelegate.shouldDisplayBottomControl(control.getId());
            control.setAlpha(shouldDisplay ? 1f : 0f);
            control.setVisibility(shouldDisplay ? View.VISIBLE : View.GONE);
            mControlsVisible.put(control, curVisibility);
            if (curVisibility && !isControlVisiable) {
                isControlVisiable = true;
            }
            if (control.getId() == R.id.photopage_bottom_control_edit) {
                if (shouldDisplay) {
                    mContainer.setPadding(mPaddingFor3, 0, mPaddingFor3, 0);
                } else {
                    mContainer.setPadding(mPaddingFor2, 0, mPaddingFor2, 0);
                }
            }
        }
        // Force a layout change
        mContainer.requestLayout(); // Kick framework to draw the control.
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-11,PR996464 end

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-29, PR1031018 begin
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-18,PR1026487 begin

    public void toggleAnim(boolean show) {
        if (animatorSet != null) {
            if (preAnimationEndAlpha == (show ? 1f : 0f)) {
                return;
            } else {
                animatorSet.cancel();
            }
        }
        if (show) {
            toogleVisible(mContainer, 0f, 1f, SmoothImageView.ANIMATION_DURATION);
           /* boolean needShow = false;
            for (View control : mControlsVisible.keySet()) {
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-23, PR1027856 begin
                Boolean prevVisibility = mControlsVisible.get(control);
                needShow = needShow || prevVisibility;
                if (!prevVisibility) {
                    continue;
                }
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-23, PR1027856 end
                toogleVisible(control, 0f, 1f, SmoothImageView.ANIMATION_DURATION);
            }
            if (needShow) {
                toogleVisible(mContainer, 0f, 1f, SmoothImageView.ANIMATION_DURATION);
            }*/
            preAnimationEndAlpha = 1f;
        } else {
            toogleVisible(mContainer, 1f, 0f, SmoothImageView.ANIMATION_DURATION);
           /* for (View control : mControlsVisible.keySet()) {
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-23, PR1027856 begin
                Boolean prevVisibility = mControlsVisible.get(control);
                if (!prevVisibility) {
                    continue;
                }
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-23, PR1027856 end
                toogleVisible(control, 1f, 0f, SmoothImageView.ANIMATION_DURATION);
            }*/
            preAnimationEndAlpha = 0f;
        }
        if (animatorSet != null) {
            animatorSet.start();
        }
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-18,PR1026487 end

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-23, PR1027856 begin
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-11,PR996464 begin
    private void toogleVisible(final View view, final float startAlpha, final float endAlpha, int time) {
        if (view.getAlpha() == endAlpha) {
            return;
        }
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", startAlpha, endAlpha);
        objectAnimator.setRepeatCount(0);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setDuration(time);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (startAlpha == 0) {
                    view.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if (endAlpha == 0) {
                    view.setVisibility(View.INVISIBLE);
                }
            }
        });
        if (animatorSet == null) {
            animatorSet = new AnimatorSet();
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animatorSet = null;
                    preAnimationEndAlpha = -1f;
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                    animatorSet = null;
                    preAnimationEndAlpha = -1f;
                }
            });
        }
        animatorSet.play(objectAnimator);
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-11,PR996464 end
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-23, PR1027856 end
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-29, PR1031018 end

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-03,PR922415 begin
    public void resetBottomButtons() {
        SystemBarConfig config = mContext.mTintManager.getConfig();
        boolean hasNavigation = config.hasNavigtionBar();
        if (hasNavigation) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mContainer.getLayoutParams();
            int navigationHeight = config.getNavigationBarHeight();
            int navigationWidth = config.getNavigationBarWidth();
            boolean isAtBottom = ScreenUtils.isNavigationAtBottom(mContext);
            if (isAtBottom) {
                params.setMargins(0, 0, 0, navigationHeight);
            } else {
                params.setMargins(0, 0, navigationWidth, 0);
            }
            // if not in ScreenInPortFull,We do not need to setMargin.
            if (ScreenUtils.getScreenInfo(mContext) != ScreenUtils.tempScreenInPortFull) {
                params.setMargins(0, 0, 0, 0);
            }
            mContainer.setLayoutParams(params);
        }
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-03,PR922415 end

    public void cleanup() {
        mParentLayout.removeView(mContainer);
        mControlsVisible.clear();
    }

    @Override
    public void onClick(View view) {
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-18,PR1026487 begin
        if(view.getAlpha() == 1) {
            mDelegate.onBottomControlClicked(view.getId());
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-18,PR1026487 end
    }

    public void invalidateFavourite(boolean isFavourite){
        if(isFavourite){
            mFavourite.setImageResource(R.drawable.ic_fav_on);
        }else {
            mFavourite.setImageResource(R.drawable.ic_fav);
        }
        mContainer.invalidate();
    }
}

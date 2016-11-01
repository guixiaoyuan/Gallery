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
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 11/17/2015|chengbin.du-nb        |ALM-913700            |[DRM] Gallery force closed happen when open gif DRM file*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 11/23/2015|chengbin.du-nb        |ALM-975835            |[Android6.0][Gallery_v5.2.4.1.0315.0]Add permission flow*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 12/25/2015| jian.pan1            |[ALM]Defect:1075189   |[Android6.0][Gallery_v5.2.5.1.0319.0][Force Close]Gallery force close when preview picture
/* ----------|----------------------|----------------------|----------------- */
/* 06/01/2015|    su.jiang          |  PR-1274456          |[Camera]The number of photo displays error in gallery screen after Burst shoot*/
/*-----------|----------------------|----------------------|------------------------------------------------------------------------------*/
/* 02/02/2016| dongliang.feng       | ALM-1544920          |[GAPP][Android6.0][Gallery][Force Close]Gallery force close after tapping print button */
/* ----------|----------------------|----------------------|----------------- */
/* 02/03/2016| jian.pan1            |[ALM]Defect:1443947   |[GAPP][Gallery]The interface display error when select image and change "MOMENTS" or "ALBUMS" together
/* 03/10/2016| hua.yang1            |[ALM]Defect:1715735   |[REG][Gallery]Can't display album'name when enter the album
/* ----------|----------------------|----------------------|----------------- */
/* 12/03/2016| wei.song             |[ALM]Defect: 1795299  |[GAPP][Android6.0][Gallery]Gallery TabView top on toolbar [com.tct.gallery3d][Version  v5.2.6.1.0347.0][Other]
/* ----------|----------------------|----------------------|----------------- */
/* 2016/03/15|  jun.xie-nb          |  PR-1780030          |[Android6.0][Gallery_v5.2.6.1.0349.0][Onetouch feedback][com.tct.gallery3d][version v5.2.6.1.0343.0_0226][Other] */
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/

//
package com.tct.gallery3d.app;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.print.PrintHelper;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.SystemBarTintManager.SystemBarConfig;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.fragment.MomentsFragment;
import com.tct.gallery3d.app.fragment.PhotoFragment;
import com.tct.gallery3d.app.fragment.SlideShowFragment;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.Log;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.filtershow.cache.ImageLoader;
import com.tct.gallery3d.image.ImageWorker;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.PermissionUtil;
import com.tct.gallery3d.util.ScreenUtils;
import com.tct.gallery3d.util.ThreadPool;

import java.io.FileNotFoundException;

@SuppressLint("NewApi")
public class AbstractGalleryActivity extends FragmentActivity implements GalleryContext {

    private static final String TAG = "AbstractGalleryActivity";

    public static final int ACTION_TYPE_DEFAULT = 0;
    public static final int ACTION_TYPE_GET_CONTENT = 1;
    public static final int ACTION_TYPE_VIEW = 2;
    public static final int ACTION_TYPE_FACESHOW = 3;
    public int mActionType = ACTION_TYPE_DEFAULT;

    protected static final String KEY_TRANSLATION_Y = "translationY";
    private static final int ANIMATION_DURATION = 200;

    public boolean mGetMultiContent = false;
    private OrientationManager mOrientationManager;
    protected int mTransY = 0;
    public int mCurPos = 0;
    public int minPos = 0;
    public int maxPos = 0;

    private Toolbar mToolbar;
    private Rect toolbarRect = new Rect();

    public Handler mHandler = null;

    private ProgressDialog mDataLoaderDialog = null;

    protected AbstractGalleryFragment mContent;

    public AbstractGalleryFragment getContent() {
        return mContent;
    }

    public void setContent(AbstractGalleryFragment content) {
        mContent = content;
    }

    public SystemBarTintManager mTintManager = null;

    private boolean mPrePrivateState;

    public void setGetMultiContent(boolean getMultiContent) {
        this.mGetMultiContent = getMultiContent;
    }

    public int getActionType() {
        return mActionType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        initializeByIntent();
        doBindBatchService();
        // bindCloudService();
        initHandler();
        ScreenUtils.initData(this);
        DrmManager.getInstance().init(this);
        mOrientationManager = new OrientationManager(this);
        minPos = ScreenUtils.ALBUMSET_MIN_SCROLL;
        if (mTintManager == null) {
            mTintManager = new SystemBarTintManager(this);
        }
        mPrePrivateState = GalleryAppImpl.getTctPrivacyModeHelperInstance(this).isPrivacyModeEnable();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean currentPrivateMode = GalleryAppImpl.getTctPrivacyModeHelperInstance(this).isPrivacyModeEnable();
        Log.d(TAG, " currentPrivateMode = " + currentPrivateMode + " mPrePrivateState = " + mPrePrivateState);
        if (mPrePrivateState ^ currentPrivateMode) {
            mPrePrivateState = currentPrivateMode;
            getDataManager().notifyPrivateMode();
        }
        mOrientationManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOrientationManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindBatchService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mContent.onActionResult(requestCode, resultCode, data);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        Log.d(TAG, "onMultiWindowModeChanged" + isInMultiWindowMode);
        /*if (!isInMultiWindowMode && mToolbar != null) {
            setMargin(mToolbar, false);
        }
        initSystemBar(false);*/
        super.onMultiWindowModeChanged(isInMultiWindowMode);
    }

    @Override
    public void onBackPressed() {
        if ((mContent != null) && (mContent.isResumed())
                && mContent.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public Context getAndroidContext() {
        return this;
    }

    @Override
    public DataManager getDataManager() {
        return ((GalleryApp) getApplication()).getDataManager();
    }

    @Override
    public ThreadPool getThreadPool() {
        return ((GalleryApp) getApplication()).getThreadPool();
    }

    @Override
    public ImageWorker getImageWorker() {
        return ((GalleryApp) getApplication()).getImageWorker();
    }

    protected void initializeByIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri uri = intent.getData();
        if (uri != null) {
            Uri internal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            if (!uri.equals(internal) && !uri.equals(external)) {
                boolean checkResult = PermissionUtil.checkPermissions(this, AbstractGalleryActivity.class.getName());
                Log.e(TAG, "check " + getClass().getName() + " permission is " + checkResult);
                if (!uri.toString().startsWith("content://downloads/my_downloads/")) {
                    if (checkResult) {
                        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null) {
                            try {
                                if (!cursor.moveToFirst()) {
                                    if (Intent.ACTION_VIEW.equalsIgnoreCase(action)
                                            || GalleryConstant.ACTION_REVIEW.equalsIgnoreCase(action)) {
                                        mActionType = ACTION_TYPE_VIEW;
                                    }
                                    Toast.makeText(this, getString(R.string.no_such_item), Toast.LENGTH_SHORT).show();
                                    checkResult = false;
                                }
                            } finally {
                                cursor.close();
                            }
                        }
                    }
                }
                if (!checkResult) {
                    finish();
                    return;
                }
            }
        }

        // [BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-06,PR1274456 begin
        if (Intent.ACTION_GET_CONTENT.equalsIgnoreCase(action)) {
            mActionType = ACTION_TYPE_GET_CONTENT;
            setGetMultiContent(intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false));
        } else if (Intent.ACTION_PICK.equalsIgnoreCase(action)) {
            Log.w(TAG, "action PICK is not supported");
            String type = Utils.ensureNotNull(intent.getType());
            if (type.startsWith("vnd.android.cursor.dir/")) {
                if (type.endsWith("/image"))
                    intent.setType("image/*");
                if (type.endsWith("/video"))
                    intent.setType("video/*");
            }
            mActionType = ACTION_TYPE_GET_CONTENT;
        } else if (Intent.ACTION_VIEW.equalsIgnoreCase(action) || GalleryConstant.ACTION_REVIEW.equalsIgnoreCase
                (action)) {
            mActionType = ACTION_TYPE_VIEW;
        } else if (FaceShowActivity.FACESHOW_ACTION.equalsIgnoreCase(action)) {
            mActionType = ACTION_TYPE_FACESHOW;
        } else {
            Log.i(TAG, "start default");
            mActionType = ACTION_TYPE_DEFAULT;
        }
        // [BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-06,PR1274456 end
    }

    private BatchService mBatchService;
    private boolean mBatchServiceIsBound = false;
    private ServiceConnection mBatchServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBatchService = ((BatchService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBatchService = null;
        }
    };

    private void doBindBatchService() {
        this.bindService(new Intent(this, BatchService.class), mBatchServiceConnection, Context.BIND_AUTO_CREATE);
        mBatchServiceIsBound = true;
    }

    private void doUnbindBatchService() {
        if (mBatchServiceIsBound) {
            // Detach our existing connection.
            this.unbindService(mBatchServiceConnection);
            mBatchServiceIsBound = false;
        }
    }

    public ThreadPool getBatchServiceThreadPoolIfAvailable() {
        if (mBatchServiceIsBound && mBatchService != null) {
            return mBatchService.getThreadPool();
        } else {
            throw new RuntimeException("Batch service unavailable");
        }
    }

    public void setToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
    }

    public int getToolbarHeight() {
        if (mToolbar != null) return mToolbar.getHeight();
        return 0;
    }

    public Toolbar getToolbar() {
        if (mToolbar != null) {
            return mToolbar;
        }
        return null;
    }

    public void resetToolBarPosition() {
        if (mTransY != 0) {
            objectAnimationStart(mToolbar, KEY_TRANSLATION_Y, mTransY, 0);
            mCurPos = 0;
            mTransY = 0;
        }
    }

    public void hideToolBarPosition() {
        if (mTransY != -getToolbarHeight()) {
            objectAnimationStart(mToolbar, KEY_TRANSLATION_Y, 0, -getToolbarHeight());
            mTransY = -getToolbarHeight();
            mCurPos = getToolbarHeight();
        }
    }

    public void toolbarDisplay() {
        if (mTransY <= -(getToolbarHeight() * 3 / 5) && mTransY != -getToolbarHeight()) {
            // hide
            objectAnimationStart(mToolbar, KEY_TRANSLATION_Y, mTransY, -getToolbarHeight());
            mTransY = -getToolbarHeight();
            mCurPos = getToolbarHeight();
        } else if (mTransY > -(getToolbarHeight() * 3 / 5) && mTransY != 0) {
            // show
            objectAnimationStart(mToolbar, KEY_TRANSLATION_Y, mToolbar.getTranslationY(), 0);
            mCurPos = 0;
            mTransY = 0;
        }

    }

    protected void objectAnimationStart(Object object, String animation, float f, float end) {
        if (object == null)
            return;
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(object, animation, f, end);
        objectAnimator.setDuration(ANIMATION_DURATION);
        objectAnimator.start();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mToolbar != null) {
                mToolbar.setTranslationY(mTransY);
            }
        }
    };

    public void onScrollPositionChanged(int newPosition) {
        if (mToolbar == null) return;
        mToolbar.getGlobalVisibleRect(toolbarRect);
        mCurPos = Utils.clamp(mCurPos + newPosition, 0, getToolbarHeight());
        mTransY = -mCurPos;
        if ((mTransY == 0 && toolbarRect.bottom == getToolbarHeight())
                || (mTransY == -getToolbarHeight() && toolbarRect.bottom == 0)) {
            return;
        }

        if (mHandler == null) {
            initHandler();
        }
        mHandler.post(runnable);
    }

    public void resetToolbar() {
    }

    /**
     * //IF screen is at bottom in MutiWindow, we should reset status bar
     */
    public void resetStatusBarIfAtBottom() {
        if (null == mToolbar) {
            return;
        }
        if (ScreenUtils.splitScreenIsAtBottom(this, mToolbar)) {
            resetStatusBar(true, mToolbar);
            setMargin(mToolbar, false);
        } else {
            resetStatusBar(false, mToolbar);
            setMargin(mToolbar, false);
        }


    }

    public void setStatusEnable(boolean enable) {
        if (mTintManager != null) {
            mTintManager.setStatusBarTintEnabled(enable);
        }
    }

    public void setNavigationEnable(boolean enable) {
        if (mTintManager != null) {
            mTintManager.setNavigationBarTintEnabled(enable);
        }
    }

    public void setStatusColor(int color) {
        if (mTintManager != null) {
            mTintManager.setStatusBarTintColor(color);
        }
    }

    public void setNavigationColor(int color) {
        if (mTintManager != null) {
            mTintManager.setNavigationBarTintColor(color);
        }
    }

    public void printSelectedImage(Uri uri) {
        if (uri == null) {
            return;
        }
        String path = ImageLoader.getLocalPathFromUri(this, uri);
        if (path != null) {
            Uri localUri = Uri.parse(path);
            path = localUri.getLastPathSegment();
        } else {
            path = uri.getLastPathSegment();
        }
        if (path == null) {
            path = ImageLoader.getDisplayName(this, uri);
            if (path == null)
                return;
        }
        // [BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-02-02, ALM-1544920 end
        PrintHelper printer = new PrintHelper(this);
        try {
            printer.printBitmap(path, uri);
        } catch (FileNotFoundException fnfe) {
            Log.e(TAG, "Error printing an image", fnfe);
        }
    }

    public boolean isFullscreen() {
        return (this.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
    }

    private void initHandler() {
        if (null == mHandler) {
            mHandler = new Handler() {
                public void handleMessage(Message message) {
                    switch (message.what) {
                        case StatusMsg.LOAD_DATA_BEGIN:
                            String str = message.obj.toString();
                            if (mDataLoaderDialog == null) {
                                mDataLoaderDialog = new ProgressDialog(AbstractGalleryActivity.this);
                            }
                            mDataLoaderDialog.setMessage(str);
                            mDataLoaderDialog.setCanceledOnTouchOutside(false);
                            mDataLoaderDialog.show();
                            break;
                        case StatusMsg.LOAD_DATA_END:
                            if (mDataLoaderDialog != null) {
                                mDataLoaderDialog.dismiss();
                                mDataLoaderDialog = null;
                            }
                            break;
                        case GalleryConstant.START_SLIDE_SHOW_MSG:
                            boolean slideshowIsActive = AbstractGalleryFragment.checkSlideShowActive((AbstractGalleryActivity) getAndroidContext());
                            if (slideshowIsActive) {
                                FragmentManager fm = getSupportFragmentManager();
                                FragmentTransaction ft = fm.beginTransaction();
                                ft.setCustomAnimations(FragmentTransaction.TRANSIT_NONE, FragmentTransaction.TRANSIT_NONE);
                                AbstractGalleryFragment fragment = SlideShowFragment.getInstance();
                                fragment.setHasOptionsMenu(true);
                                fragment.setArguments(message.getData());
                                setContent(fragment);
                                ft.add(android.R.id.content, fragment, SlideShowFragment.TAG);
                                ft.commit();
                            }
                            break;
                    }
                }
            };
        }
    }

    public void setViewPagerState(boolean state) {
        // Need implemented by the derived class.
    }

    public void startPhotoPage(Bundle data) {
        startPhotoPage(data, null);
    }

    public void startPhotoPage(Bundle data, Drawable drawable) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(FragmentTransaction.TRANSIT_NONE, FragmentTransaction.TRANSIT_NONE);
        PhotoFragment fragment = new PhotoFragment();
        fragment.setHasOptionsMenu(true);
        fragment.setArguments(data);
        fragment.setDrawable(drawable);
        setContent(fragment);
        ft.add(R.id.photo_page_container, fragment, PhotoFragment.TAG);
        ft.commit();
    }

    public void startSlideShow(Bundle data) {
        Message message = Message.obtain();
        message.what = GalleryConstant.START_SLIDE_SHOW_MSG;
        message.setData(data);
        mHandler.sendMessageDelayed(message,GalleryConstant.MENU_CLOSE_ANIMATION_DURATION);
    }

    public void initSystemBar(boolean full) {
        if (Build.VERSION.SDK_INT >= 21) {
            if (full) {
                setNavigationEnable(false);
                //setStatusEnable(false);
            } else {
                setNavigationEnable(true);
               // setStatusEnable(true);
            }
        }
    }

    public void resetStatusBar(boolean needReset,Toolbar toolbar) {
        if (mTintManager != null) {
            mTintManager.resetStatusBar(needReset,toolbar);
        }
    }
    public void setMargin(View view, boolean marginTop) {
        if (view == null) {
            return;
        }
        if (mTintManager == null) {
            return;
        }
        SystemBarConfig mConfig = mTintManager.getConfig();
        mConfig.initSize();

        if (RelativeLayout.LayoutParams.class.isInstance(view.getLayoutParams())) {
            RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(view.getLayoutParams());
            if (mConfig.hasNavigtionBar() && (ScreenUtils.getScreenInfo(this)==ScreenUtils.tempScreenInLandFull)) {
                rllp.rightMargin = mConfig.getNavigationBarWidth();
            }
            if (marginTop) {
                Log.d(TAG,"RelativeLayout getStatusBarHeight" + mConfig.getStatusBarHeight());
                if(ScreenUtils.splitScreenIsAtBottom(this,mToolbar)){
                    rllp.topMargin = 0;
                }else{
                    rllp.topMargin = mConfig.getStatusBarHeight();
                }

            }
            view.setLayoutParams(rllp);
        } else if (FrameLayout.LayoutParams.class.isInstance(view.getLayoutParams())) {
            FrameLayout.LayoutParams fllp = new FrameLayout.LayoutParams(view.getLayoutParams());
            if (mConfig.hasNavigtionBar() && ScreenUtils.getScreenInfo(this)==ScreenUtils.tempScreenInLandFull) {
                fllp.rightMargin = mConfig.getNavigationBarWidth();
            }
            if (marginTop) {
                Log.d(TAG,"FrameLayout getStatusBarHeight" + mConfig.getStatusBarHeight());
                if(ScreenUtils.splitScreenIsAtBottom(this,mToolbar)){
                    fllp.topMargin = 0;
                }else{
                    fllp.topMargin = mConfig.getStatusBarHeight();
                }
            }
            view.setLayoutParams(fllp);
        } else if (CoordinatorLayout.LayoutParams.class.isInstance(view.getLayoutParams())) {
            CoordinatorLayout.LayoutParams colp = new CoordinatorLayout.LayoutParams(view.getLayoutParams());
            if (mConfig.hasNavigtionBar() && ScreenUtils.getScreenInfo(this)==ScreenUtils.tempScreenInLandFull) {
                colp.rightMargin = mConfig.getNavigationBarWidth();
            }
            if (marginTop) {
                Log.d(TAG,"CoordinatorLayout getStatusBarHeight" + mConfig.getStatusBarHeight());
                if(ScreenUtils.splitScreenIsAtBottom(this,mToolbar)){
                    colp.topMargin = 0;
                }else{
                    colp.topMargin = mConfig.getStatusBarHeight();
                }
            }
            view.setLayoutParams(colp);
        }
    }

    public void hideToolBarView() {
    }

    public void showToolBarView() {
    }

    /**
     * set status color when in actionMode
     * @param isSelected
     */
    public void setStatusColorInActionMode(boolean isSelected){
        if(ScreenUtils.getScreenInfo(this) != ScreenUtils.tempScreenInPortSplit){
            setStatusEnable(true);
            if(isSelected){
                setStatusColor(SystemBarTintManager.STATUSBAR_COLOR_BLACK);
            }else{
                setStatusColor(SystemBarTintManager.DEFAULT_TINT_COLOR);
            }
        }else{
            //if is in port-screen ,we need remove view if at bottom
            if(ScreenUtils.splitScreenIsAtBottom(this,mToolbar)){
                setStatusEnable(false);
            }else{
                if(!isSelected){
                    setStatusEnable(true);
                }
            }
        }
    }
}

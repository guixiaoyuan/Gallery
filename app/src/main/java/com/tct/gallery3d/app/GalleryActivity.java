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
/* ----------|----------------------|-------------------- -|-------------------*/
/* 29/01/2015|qiang.ding1           | PR917027             |[Chaatz]The Gallery related function doesn't work.*/
/* ----------|----------------------|------------------- --|-------------------*/
/* 31/01/2015| ye.chen              | PR-904080            |DRM processing in Gallery
/* ----------|----------------------|----------------------|------------------------------------------------------- */
/* 03/06/2015| jian.pan1            | PR939075             |No option to share, edit, "set as", etc. when viewing screenshot from notification
/* ----------|----------------------|----------------------|------------------------------------------------------------*/
/* 03/18/2015| qiang.ding1          | PR952980             |[Android5.0][Gallery_v5.1.9.1.0105.0][REG][Force close]The  */
/*           |                      |                      |DUT will pop up gallery force close when click file manager */
/*           |                      |                      |in recent applications                                      */
/* ----------|----------------------|----------------------|------------------------------------------------------------*/
/* 23/03/2015|    jialiang.ren     |      PR-956675       |[Settings][Wallpaper][Gallery]Tap the menu */
/*                                                         button，the camera roll is cut off         */
/* ----------|---------------------|----------------------|-------------------------------------------*/
/* 02/04/2015|    jialiang.ren     |      PR-960896       |[Gallery]The picture cannot show in camera */
/*                                                         roll when set storage place as SD card     */
/* ----------|---------------------|----------------------|-------------------------------------------*/
/* 16/04/2015|dongliang.feng        |PR186090              |[Download][DRM]MS display black when view */
/*           |                      |                      |the DRM image from Downloads/File Manager/Gallery */
/* ----------|----------------------|------------------- --|-------------------*/
/* 06/11/2015|chengbin.du-nb        |ALM-871912            |[Android6.0][Gallery_v5.2.3.1.0310.0]update sdkTargetVersion to 23*/
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/
/* 11/17/2015|chengbin.du-nb        |ALM-913700            |[DRM] Gallery force closed happen when open gif DRM file*/
/* ----------|----------------------|----------------------|----------------------------------------------------------------------*/
/* 11/30/2015|chengbin.du-nb        |ALM-975835            |[Android6.0][Gallery_v5.2.4.1.0315.0]Add permission flow*/
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/
/* 12/10/2015| jian.pan1            |[ALM]Defect:1003194   |[Gallery]The Lock screen didn't appeared when tap power key lock and unlock the screen in gallery
/* ----------|----------------------|----------------------|----------------- */
/* 12/11/2015| jian.pan1            |[ALM]Defect:1126522   |[Android6.0][Gallery_v5.2.5.1.0320.0][GD]The font size don't match GD in the moments interface
/* ----------|----------------------|----------------------|----------------- */
/* 12/25/2015| jian.pan1            |[ALM]Defect:1075189   |[Android6.0][Gallery_v5.2.5.1.0319.0][Force Close]Gallery force close when preview picture
/* ----------|----------------------|----------------------|----------------- */
/* 12/28/2015| jian.pan1            |[ALM]Defect:1190519   |[Monitor][Gallery]The moments of gallery show about albums ,it's abnormal
/* ----------|----------------------|----------------------|----------------- */
/* 06/01/2015|    su.jiang          |  PR-1274456          |[Camera]The number of photo displays error in gallery screen after Burst shoot*/
/*-----------|----------------------|----------------------|------------------------------------------------------------------------------*/
/* 01/07/2016| jian.pan1            |[ALM]Defect:1039231   |[Android 6.0][CameraL_v5.2.6.2.0002.0]There is a extra UI  of lock Screen when tapping boom key
/* ----------|----------------------|----------------------|----------------- */
/* 13/01/2015|    su.jiang          |  PR-1415151          |[Android6.0][Gallery]From the gallery on exit back to the camera,Add an interface.*/
/*-----------|----------------------|----------------------|----------------------------------------------------------------------------------*/
/* 02/03/2016| jian.pan1            |[ALM]Defect:1443947   |[GAPP][Gallery]The interface display error when select image and change "MOMENTS" or "ALBUMS" together
/* ----------|----------------------|----------------------|----------------- */
/* 2016/02/17|  caihong.gu-nb       |  PR-1537839          |[GAPP][Android 6.0][Gallery][REG]It is not locked interface after locking the handset when play a video*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/
/* 2016/02/25|  caihong.gu-nb       |  PR-1490902          |[GAPP][Android6.0][Gallery]Boomkey prompt location is error on idol 4s*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/
/* 2016/03/02|  caihong.gu-nb       |  PR-1614131          |[GAPP][Android6.0][Gallery]The‘press boom key’tips location display error*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/
/* 2016/03/02|  caihong.gu-nb       |  PR-1553033          |[Gallery]Can't delete all photos one-time by thumbnail in lock screen*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/

package com.tct.gallery3d.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.WifiDisplayStatus;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore.Images;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.fragment.AlbumFragment;
import com.tct.gallery3d.app.fragment.AlbumSetFragment;
import com.tct.gallery3d.app.fragment.MomentsFragment;
import com.tct.gallery3d.app.fragment.PhotoFragment;
import com.tct.gallery3d.bottombar.BottomNavigation;
import com.tct.gallery3d.bottombar.BottomNavigationAdapter;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.GappTypeInfo;
import com.tct.gallery3d.data.Log;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.data.MediaSource;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.db.DataBaseManager;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.ui.CustomViewPager;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.PLFUtils;
import com.tct.gallery3d.util.PermissionUtil;
import com.tct.gallery3d.util.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;


@SuppressLint("NewApi")
public class GalleryActivity extends AbstractGalleryActivity implements OnClickListener {

    private static final String PERFERENCES_NAME = "Gallery";
    private static final String KEY_FIRST_RUN = "first_run";

    public static final String EXTRA_SLIDESHOW = "slideshow";
    public static final String EXTRA_DREAM = "dream";
    public static final String EXTRA_CROP = "crop";

    public static final String KEY_GET_CONTENT = "get-content";
    public static final String KEY_GET_ALBUM = "get-album";
    public static final String KEY_TYPE_BITS = "type-bits";
    public static final String KEY_MEDIA_TYPES = "mediaTypes";
    public static final String KEY_DISMISS_KEYGUARD = "dismiss-keyguard";
    public static final String KEY_SINGLE_ITEM_ONLY = "is-single-item-only";
    private static final String IMAGE_INTENT_TYPE = "image/*";
    private static final String VIDEO_INTENT_TYPE = "video/*";

    public static final String CURRENT_PAGE = "current-page";

    private static final String TAG = "GalleryActivity";
    public static final String TV_LINK_CHANGE_ACTION = "android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED";
    public static boolean TV_LINK_DRM_HIDE_FLAG = false;
    public static final String TCT_HDCP_DRM_NOTIFY = "hdcp_drm_notify";
    public static int LAST_WIFI_DISPLAY_STATE = DisplayManagerGlobal.getInstance().getWifiDisplayStatus()
            .getActiveDisplayState();
    public static int WIFI_DISPLAY_DRM_NOTIFY = 1;

    //TODO
    public static final int PAGE_EXPLORE = -1;
    public static final int PAGE_MOMENTS = 0;
    public static final int PAGE_ALBUMS = 1;
    private int mCurrentPage = PAGE_MOMENTS;

    public boolean isCameraReview = false;

    private CustomViewPager mViewPager;
    private ArrayList<Fragment> mFragmentList;
    private FragmentPagerAdapter mAdapter;
    private boolean mGetContent;
    private boolean mGetMultiContent = false;
    private int mToolbarPosition = 0;
    private BottomNavigation mBottomNavigationBar;
    private BottomNavigationAdapter mBottomNavigationAdapter;
    private Toolbar mToolbar = null;
    private Menu mMenu = null;

    private void initViewPager() {
        mViewPager = (CustomViewPager) findViewById(R.id.viewpager);
        mFragmentList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            initPages(i);
        }
        mAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener((OnPageChangeListener) mAdapter);
        mViewPager.setCurrentItem(PAGE_MOMENTS);
        switchTab(PAGE_MOMENTS);
        mViewPager.setOffscreenPageLimit(0);
        mContent = (AbstractGalleryFragment) (mAdapter.getItem(0));
    }

    public Fragment getCurrentContent(int index) {
        return mAdapter.getItem(index);
    }

    private void initPages(int index) {
        Fragment fragment;
        //mGetContent = Intent.ACTION_GET_CONTENT.equals(getIntent().getAction());
        //PR 3132016 : MMS set chatWallper ,Intent action is PICK
        mGetContent = (getActionType() == AbstractGalleryActivity.ACTION_TYPE_GET_CONTENT);
        Log.d(TAG,"mGetContent" + mGetContent);
        String intentType = getIntent().getType();
        mGetMultiContent = getIntent().getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        Bundle data = new Bundle();
        switch (index){
            case PAGE_MOMENTS:
                fragment = new MomentsFragment();
                data.putString(GalleryConstant.KEY_MEDIA_PATH, MediaSource.LOCAL_MOMENTS_SET_PATH);
                data.putBoolean(GalleryActivity.KEY_GET_CONTENT, mGetContent);
                data.putBoolean(Intent.EXTRA_ALLOW_MULTIPLE, mGetMultiContent);
                fragment.setArguments(data);
                mFragmentList.add(PAGE_MOMENTS, fragment);
                break;
            case PAGE_ALBUMS:
                fragment = new AlbumSetFragment();
                if (IMAGE_INTENT_TYPE.equals(intentType)) {
                    data.putString(GalleryConstant.KEY_MEDIA_PATH, MediaSource.LOCAL_IMAGE_SET_PATH);
                } else if (VIDEO_INTENT_TYPE.equals(intentType)) {
                    data.putString(GalleryConstant.KEY_MEDIA_PATH, MediaSource.LOCAL_VIDEO_SET_PATH);
                }  else {
                    data.putString(GalleryConstant.KEY_MEDIA_PATH, MediaSource.LOCAL_SET_PATH);
                }
                data.putBoolean(GalleryActivity.KEY_GET_CONTENT, mGetContent);
                data.putBoolean(Intent.EXTRA_ALLOW_MULTIPLE, mGetMultiContent);
                fragment.setArguments(data);
                mFragmentList.add(PAGE_ALBUMS, fragment);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(KEY_DISMISS_KEYGUARD, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        if (getActionType() == ACTION_TYPE_VIEW) {
            setContentView(R.layout.gallery_common);
            startViewAction(this.getIntent());
            removeTabsView();
        } else if (getActionType() == ACTION_TYPE_FACESHOW) {
            setContentView(R.layout.gallery_common);
            startFaceshowAction(this.getIntent());
            removeTabsView();
        } else {
            setContentView(R.layout.gallery_main);

            initView();
            initViewPager();

            setMargin(mViewPager, false);
            setToolbar(mToolbar);
            initSystemBar(false);
            resetToolBarPosition();
            if (savedInstanceState != null) {
                Intent intent = getIntent();
                String action = intent.getAction();
                boolean checkResult = PermissionUtil.checkPermissions(this, GalleryActivity.class.getName());
                Log.i(TAG, "checkResult = " + checkResult + " action = " + action);
                if (!checkResult) {
                    if (action == null) {
                        intent = new Intent();
                        intent.setClass(this, PermissionActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (Intent.ACTION_VIEW.equals(action)) {
                        intent.setClass(this, PermissionActivity.class);
                        intent.putExtra("isSaveInstance", true);
                        intent.putExtras(savedInstanceState);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-28,Defect:1190519 begin
                    mCurrentPage = savedInstanceState.getInt(CURRENT_PAGE);
                    switchTab(mCurrentPage);
                }
                // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-25,Defect:1075189 end
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = null;
                        try {
                            cursor = getContentResolver().query(DataBaseManager.FILE_URI, null, null, null, null);
                            int mediaTypeIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_MEDIA_TYPE);
                            if (mediaTypeIndex != GalleryConstant.NO_COLUMN_RETURN_VALUE) {
                                GalleryAppImpl.sHasNewColumn = true;
                            } else {
                                GalleryAppImpl.sHasNewColumn = false;
                            }
                            int isPrivateIndex = cursor.getColumnIndex(GalleryConstant.IS_PRIVATE);
                            if (isPrivateIndex != GalleryConstant.NO_COLUMN_RETURN_VALUE) {
                                GalleryAppImpl.sHasPrivateColumn = true;
                            } else {
                                GalleryAppImpl.sHasPrivateColumn = false;
                            }
                            Log.i(TAG,"sHasNewColumn = "+GalleryAppImpl.sHasNewColumn+"  sHasPrivateColumn = "+GalleryAppImpl.sHasPrivateColumn);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                }).start();
                if (getActionType() == ACTION_TYPE_VIEW) {
                    startViewAction(this.getIntent());
                    removeTabsView();
                } else if (getActionType() == ACTION_TYPE_FACESHOW) {
                    startFaceshowAction(this.getIntent());
                    removeTabsView();
                } else if (getActionType() == ACTION_TYPE_GET_CONTENT) {
                    removeTabsView();
                } else {
                    initDrmHideFlag();
                    // switchMomentsPage();
                }
            }
        }
        registerReceiver(TvLinkHintOrDisplay, new IntentFilter(TV_LINK_CHANGE_ACTION));

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mSrceenOnOffReceiver, filter);
    }

    private void initView() {
        mGalleryRoot = (CoordinatorLayout) findViewById(R.id.gallery_root);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(mToolbar);
        setMargin(mToolbar, false);
        setMargin(mViewPager, false);
        initBottomBar();
    }

    private void initBottomBar() {
        mBottomNavigationBar = (BottomNavigation) findViewById(R.id.bottom_navigation);
        mBottomNavigationAdapter = new BottomNavigationAdapter(this, R.menu.bottombar_menu);
        mBottomNavigationAdapter.setupWithBottomNavigation(mBottomNavigationBar);
        int accentColor = ContextCompat.getColor(this,R.color.colorBottomNavigationAccent);
        int inactiveColor = ContextCompat.getColor(this,R.color.colorBottomNavigationInactive);
        mBottomNavigationBar.setAccentColor(accentColor);
        mBottomNavigationBar.setInactiveColor(inactiveColor);

        setBottomBarMargin();
        mBottomNavigationBar.setOnTabSelectedListener(new BottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switchPage(position);
                return true;
            }
        });
        mBottomNavigationBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    private void setBottomBarMargin() {
        if (mBottomNavigationBar == null) {
            return;
        }
        SystemBarTintManager.SystemBarConfig config = mTintManager.getConfig();
        int bottom = config.getPixelInsetBottom();
        int right = 0;
        int screenInfo = ScreenUtils.getScreenInfo(this);
        if (config.hasNavigtionBar() && screenInfo == ScreenUtils.tempScreenInLandFull)
        {
            right = config.getNavigationBarWidth();
        }

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mBottomNavigationBar.getLayoutParams();
        if (screenInfo == ScreenUtils.tempScreenInLandFull  ) {
            params.setMargins(params.leftMargin, params.topMargin, right, 0);
        }
        else if(screenInfo==ScreenUtils.tempScreenInPortFull){
            params.setMargins(params.leftMargin, params.topMargin, 0, bottom);
        }else {
                params.setMargins(params.leftMargin, params.topMargin, 0, 0);

        }
        mBottomNavigationBar.setLayoutParams(params);
    }

    public void showTipsIfNeeded() {
        boolean plfControl = PLFUtils.getBoolean(this, "def_show_boomkey_tips");
        if (!plfControl)
            return;
        SharedPreferences sp = getSharedPreferences("Gallery", MODE_PRIVATE);
        boolean firstLaunch = sp.getBoolean("first_launch", true);
        // [BUGFIX]-Modify by TCTNJ,caihong.gu-nb,2016/03/02,PR1663212
        if (firstLaunch && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Editor editor = sp.edit();
            editor.putBoolean("first_launch", false);
            editor.commit();
            showTips();
        }
    }

    private CoordinatorLayout mGalleryRoot = null;
    private View mFirstLauchView = null;

    private void showTips() {
        mFirstLauchView = LayoutInflater.from(this).inflate(R.layout.layout_tip_firstlaunch, null);
        /* MODIFIED-BEGIN by caihong.gu-nb, 2016-04-06,BUG-1913258 */
        LinearLayout tip_firstLinearLayout = (LinearLayout) mFirstLauchView.findViewById(R.id.tip_firstlauncher_lr);
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        tip_firstLinearLayout.measure(w, h);
        int height = tip_firstLinearLayout.getMeasuredHeight() / 2;
        // [BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1490902 begin
        String boomkeyTop = PLFUtils.getString(GalleryActivity.this, "def_JrdLauncher_boom_key_tip_padding_top");
        int boomkeyHeight = 825;
        if (!TextUtils.isEmpty(boomkeyTop)) {
            boomkeyHeight = Integer.parseInt(boomkeyTop);
        }
        mFirstLauchView.setPadding(0, boomkeyHeight - height, 0, 0);
        /* MODIFIED-END by caihong.gu-nb,BUG-1913258 */
        // [BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1490902 end
        mFirstLauchView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mGalleryRoot.removeView(mFirstLauchView);
            }
        });
        mGalleryRoot.addView(mFirstLauchView);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFirstLauchView.getLayoutParams();
        lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mGalleryRoot != null && mFirstLauchView != null) {
                    mGalleryRoot.removeView(mFirstLauchView);
                }
            }
        }, 5000);
    }

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-28,Defect:1190519 begin
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_PAGE, mCurrentPage);
    }
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-28,Defect:1190519 end

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setBottomBarMargin();
        setMargin(mViewPager, false);
        resetToolBarPosition();
        initSystemBar(false);
        showTabsView();
        resetStatusBarIfAtBottom();
        if (mGalleryRoot != null && mFirstLauchView != null
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mGalleryRoot.removeView(mFirstLauchView);
        }

        if (mMenu != null) mMenu.close();
    }
    // [BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/02,PR1663212 end
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        Log.d(TAG, "onMultiWindowModeChanged" + isInMultiWindowMode);
        /*setBottomBarMargin();
        setMargin(mToolbar, true);
        setMargin(mViewPager, true);*/
        super.onMultiWindowModeChanged(isInMultiWindowMode);
    }

    private void startViewAction(Intent intent) {
        Bundle data = new Bundle();
        DataManager dm = getDataManager();
        dm.isFaceShowAction = false;// [BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-06,PR857659
        dm.isNotFromThirdPart = false;
        Uri uri = intent.getData();
        String contentType = getContentType(intent);
        boolean isDownload = false;
        if (contentType == null) {
            Toast.makeText(this, R.string.no_such_item, Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }
        if (uri != null) {
            if (uri.getScheme().equals("file") && DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM
                    && DrmManager.getInstance().isDrm(uri.toString())) {
                String path = uri.getEncodedPath();
                if (path != null) {
                    path = Uri.decode(path);
                    Cursor cursor = null;
                    try {
                        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
                        String[] projection = new String[] { Images.ImageColumns._ID };
                        String selection = "(" + Images.ImageColumns.DATA + "=" + "'" + path + "'" + ")";
                        cursor = this.getContentResolver().query(baseUri, projection, selection, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(projection[0]);
                            uri = Uri.parse("content://media/external/images/media/" + cursor.getInt(index));
                            Log.d(TAG, "MTK DRM file, uri is " + uri);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                }
            }

            if (uri.getScheme().equals("content") && uri.getAuthority().contains("downloads")) {
                Cursor cursor = null;
                try {
                    isDownload = true;
                    long id = ContentUris.parseId(uri);
                    Uri downloadUri = Uri.parse("content://downloads/all_downloads");
                    downloadUri = ContentUris.withAppendedId(downloadUri, id);
                    cursor = getContentResolver().query(downloadUri,
                            new String[] { DownloadManager.COLUMN_MEDIAPROVIDER_URI }, null, null, null);
                    int index = cursor.getColumnIndex(DownloadManager.COLUMN_MEDIAPROVIDER_URI);
                    String mediaUri = null;
                    if (cursor != null && cursor.moveToFirst()) {
                        mediaUri = cursor.getString(index);
                    }
                    uri = Uri.parse(mediaUri);
                } finally {
                    if (cursor != null)
                    cursor.close();
                    cursor = null;
                }
            }

            Path itemPath = dm.findPathByUri(uri, contentType);
            if (null == itemPath && "application/*".equalsIgnoreCase(contentType)) {
                Toast.makeText(this, getString(R.string.fail_to_load), Toast.LENGTH_SHORT).show();
                this.finish();
                return;
            }
            // [ALM][BUGFIX]-Add by TCTNJ,cuihua.yang, 2015-12-26,Defect:1190498 begin
            if (null == itemPath) {
                Toast.makeText(this, getString(R.string.fail_to_load), Toast.LENGTH_SHORT).show();
                this.finish();
                return;
            }
            // [ALM][BUGFIX]-Add by TCTNJ,cuihua.yang, 2015-12-26,Defect:1190498 end
            // [BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-02-22,PR1550969 begin
            // Path albumPath = dm.getDefaultSetOf(itemPath);
            Path albumPath = null;
            ContentResolver cr = getContentResolver();
            Cursor cs = null;
            String[] projection = { Images.Media.BUCKET_ID };
            try {
                cs = cr.query(Images.Media.EXTERNAL_CONTENT_URI, projection, "_id = ?",
                        new String[] { itemPath.getSuffix() }, null);
                if (cs != null && cs.moveToFirst()) {
                    albumPath = Path.fromString(MediaSource.LOCAL_SET_PATH).getChild(cs.getInt(0));
                }
            } catch (Exception e) {
            } finally {
                if (cs != null) {
                    cs.close();
                    cs = null;
                }
            }
            if (albumPath == null) {
                albumPath = dm.getDefaultSetOf(itemPath);
            }

            // TODO: Make the parameter "SingleItemOnly" public so other
            // activities can reference it.
            boolean singleItemOnly = (albumPath == null) || intent.getBooleanExtra("SingleItemOnly", false) || isDownload;
            // [BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-02-22,PR1550969 end
            data.putString(GalleryConstant.KEY_MEDIA_ITEM_PATH, itemPath.toString());
            data.putBoolean(KEY_SINGLE_ITEM_ONLY, singleItemOnly);
            if (!singleItemOnly) {
                ArrayList<Uri> uriList = intent.getParcelableArrayListExtra("uriarray");
                // [BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-13,PR1415151 begin
                boolean fromInstantCapture = intent.getBooleanExtra("from_instant_capture", false);
                data.putBoolean("from_instant_capture", fromInstantCapture);
                data.putInt(GalleryConstant.KEY_INDEX_HINT, GalleryConstant.INVALID_INDEX);
                // [BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-13,PR1415151 end
                if (uriList != null && !uriList.isEmpty()) {
                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-10,Defect:1003194 begin
                    isCameraReview = true;
                    Log.i(TAG, "Current is Camera Review.");
                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-10,Defect:1003194 end
                    HashMap<Uri, String> map = new HashMap<Uri, String>();
                    for (int i = 0; i < uriList.size(); i++) {
                        Uri tempUri = uriList.get(i);
                        String contentId = tempUri.getLastPathSegment();
                        int type = ExifInfoFilter.getInstance(this).queryType(contentId);
                        // [BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/03/02,PR1553033 begin
                        if (type != ExifInfoFilter.BURSTSHOTSHIDDEN) {
                            Path p = dm.findPathByUri(uriList.get(i), contentType);
                            map.put(uriList.get(i), p.getSuffix());
                        }
                        // [BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/03/02,PR1553033 end
                    }
                    dm.setUriInfoMap(map);
                    albumPath = Path.fromString(dm.getTopSetPath(DataManager.INCLUDE_LOCAL_CAMERA_REVIEW));
                }
                data.putString(GalleryConstant.KEY_MEDIA_SET_PATH, albumPath.toString());
                Log.d(TAG, "startViewAction albumPath:" + albumPath.toString()); // MODIFIED by jian.pan1,
                                                                                 // 2016-04-12,BUG-1938595
                // when FLAG_ACTIVITY_NEW_TASK is set, (e.g. when intent is
                // fired
                // from notification), back button should behave the same as up
                // button
                // rather than taking users back to the home screen
            }
            startPhotoPage(data);
        }
    }

    // [BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-06,PR1274456 begin
    private void startFaceshowAction(Intent intent) {
        Bundle data = new Bundle();
        DataManager dm = getDataManager();
        dm.isFaceShowAction = true;
        Uri uri = intent.getData();
        String contentType = getContentType(intent);
        if (contentType == null) {
            Toast.makeText(this, R.string.no_such_item, Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }
        Path itemPath = dm.findPathByUri(uri, contentType);
        if (null == itemPath && "application/*".equalsIgnoreCase(contentType)) {
            Toast.makeText(this, getString(R.string.fail_to_load), Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        if (null == itemPath) {
            this.finish();
            return;
        }
        // MODIFIED-BEGIN by caihong.gu-nb, 2016-04-10, BUG-1925058
        String albumPath = "/local/faceshow";

        data.putString(GalleryConstant.KEY_MEDIA_ITEM_PATH, itemPath.toString());
//        data.putBoolean(PhotoPage.KEY_READONLY, false);
        boolean singleItemOnly = (albumPath == null) || intent.getBooleanExtra("SingleItemOnly", false);
        if (!singleItemOnly) {
            data.putString(GalleryConstant.KEY_MEDIA_SET_PATH, albumPath);
            /* MODIFIED-END by caihong.gu-nb,BUG-1925058 */
        }
    }
    // [BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-06,PR1274456 end

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-10,Defect:1003194 begin
    /**
     * Manage the Screen On / Off event
     */
    private BroadcastReceiver mSrceenOnOffReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Log.i(TAG, "mSrceenOnOffReceiver action:" + intent.getAction() + " isCameraReview = " + isCameraReview);
                if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) && isCameraReview) {
                    // If current Gallery is started for camera lock mode
                    // review, it should be finished after received
                    // ACTION_SCREEN_OFF action.
                    finish();
                }
            }
        }
    };
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-10,Defect:1003194 end

    private String getContentType(Intent intent) {
        String type = intent.getType();
        if (type != null) {
            return GalleryUtils.MIME_TYPE_PANORAMA360.equals(type) ? MediaItem.MIME_TYPE_JPEG : type;
        }

        Uri uri = intent.getData();
        try {
            return this.getContentResolver().getType(uri);
        } catch (Throwable t) {
            Log.w(TAG, "get type fail", t);
            return null;
        }
    }

    protected void onResume() {
        super.onResume();

        // [BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-06,PR1274456 begin
        if (getActionType() != ACTION_TYPE_VIEW) {
            DataManager dm = getDataManager();
            dm.isNotFromThirdPart = true;
        }
        // [BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-06,PR1274456 end
        boolean checkResult = PermissionUtil.checkPermissions(this, GalleryActivity.class.getClass().getName());
        if (checkResult) {
            if (Intent.ACTION_GET_CONTENT.equals(getIntent().getAction())
                    || Intent.ACTION_PICK.equals(getIntent().getAction())) {
                switchPage(PAGE_ALBUMS);
                hideTabsView();
                setViewPagerState(false);
            }
            if (isCameraReview) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-07,Defect:1039231 begin
                PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(
                        PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "screenwakelock");
                wl.acquire();
                wl.release();
                // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-07,Defect:1039231 end
            }
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-10,Defect:1003194 end
        }
        restoreToolbarPosition();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG,"onWindowFocusChanged");
        resetStatusBarIfAtBottom();
        if(null != mFragmentList && mFragmentList.size()>0){
            ((MomentsFragment)mFragmentList.get(0)).initSystemUI();
            ((AlbumSetFragment)mFragmentList.get(1)).initSystemUI();
        }

        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkStoragePermissionAndRepop();
        SharedPreferences sp = getSharedPreferences(PERFERENCES_NAME, MODE_PRIVATE);
        boolean firstLaunch = sp.getBoolean(KEY_FIRST_RUN, true);
        if (firstLaunch) {
            MediaSet.filterExif(this);
            Editor editor = sp.edit();
            editor.putBoolean(KEY_FIRST_RUN, false);
            editor.apply();
        }
    }

    BroadcastReceiver TvLinkHintOrDisplay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TV_LINK_CHANGE_ACTION)) {
                int wifiDisplayStatus = DisplayManagerGlobal.getInstance().getWifiDisplayStatus()
                        .getActiveDisplayState();
                int HDCP_ENABLE = Settings.Global.getInt(context.getContentResolver(), TCT_HDCP_DRM_NOTIFY, 0);
                Log.i(TAG, "WIFI_DISPLAY_STATUS_CHANGED Broadcast Receiver, status = " + wifiDisplayStatus
                        + ", HDCP enable = " + HDCP_ENABLE);
                if (wifiDisplayStatus == WifiDisplayStatus.DISPLAY_STATE_CONNECTED) {
                    WIFI_DISPLAY_DRM_NOTIFY = 1;

                    if (HDCP_ENABLE == 0) {
                        TV_LINK_DRM_HIDE_FLAG = true;
                        startDefaultPage();
                        Log.i(TAG, "TV_LINK_DRM_HIDE_FLAG1 = " + TV_LINK_DRM_HIDE_FLAG);
                    } else {
                        if (TV_LINK_DRM_HIDE_FLAG) {
                            TV_LINK_DRM_HIDE_FLAG = false;
                            startDefaultPage();
                            Log.i(TAG, "TV_LINK_DRM_HIDE_FLAG2 = " + TV_LINK_DRM_HIDE_FLAG);
                        }
                    }
                } else {
                    if (TV_LINK_DRM_HIDE_FLAG) {
                        TV_LINK_DRM_HIDE_FLAG = false;
                        startDefaultPage();
                        Log.i(TAG, "TV_LINK_DRM_HIDE_FLAG3 = " + TV_LINK_DRM_HIDE_FLAG);
                    }
                }
            }
        }
    };

    private void startDefaultPage() {
        int currentPage = mCurrentPage;
        mCurrentPage = -1;
        switchPage(currentPage);
    }

    private void initDrmHideFlag() {
        int HDCP_ENABLE = Settings.Global.getInt(getContentResolver(), TCT_HDCP_DRM_NOTIFY, 0);
        int wifiDisplayStatus = DisplayManagerGlobal.getInstance().getWifiDisplayStatus().getActiveDisplayState();
        if (wifiDisplayStatus == WifiDisplayStatus.DISPLAY_STATE_CONNECTED) {
            if (HDCP_ENABLE == 0) {
                TV_LINK_DRM_HIDE_FLAG = true;
            }
        } else {
            if (TV_LINK_DRM_HIDE_FLAG) {
                TV_LINK_DRM_HIDE_FLAG = false;
            }
        }
        Log.i(TAG, "init drm hide flag = " + TV_LINK_DRM_HIDE_FLAG + ", wifi display current status = "
                + wifiDisplayStatus + ", HECP enable falg = " + HDCP_ENABLE);
    }

    @Override
    protected void onDestroy() {
        getDataManager().release();
        unregisterReceiver(TvLinkHintOrDisplay);
        WIFI_DISPLAY_DRM_NOTIFY = 1;
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-10,Defect:1003194 begin
        unregisterReceiver(mSrceenOnOffReceiver);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-10,Defect:1003194 end

        // [ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-3,ALM-1539291 begin
//        MomentsPopupMenu popupMenu = getGalleryActionBar().getPopupMenu();
//        if (popupMenu != null) {
//            popupMenu.resetFilter();
//        }
        // [ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-3,ALM-1539291 end
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveToolBarPosition();
        LAST_WIFI_DISPLAY_STATE = DisplayManagerGlobal.getInstance().getWifiDisplayStatus().getActiveDisplayState();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        final boolean isTouchPad = (event.getSource() & InputDevice.SOURCE_CLASS_POSITION) != 0;
        if (isTouchPad) {
            float maxX = event.getDevice().getMotionRange(MotionEvent.AXIS_X).getMax();
            float maxY = event.getDevice().getMotionRange(MotionEvent.AXIS_Y).getMax();
            View decor = getWindow().getDecorView();
            float scaleX = decor.getWidth() / maxX;
            float scaleY = decor.getHeight() / maxY;
            float x = event.getX() * scaleX;
            float y = event.getY() * scaleY;
            MotionEvent touchEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), x,
                    y, event.getMetaState());
            return dispatchTouchEvent(touchEvent);
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public void onClick(View v) {
        if (!mViewPager.getPagingEnabled()) {
            return;
        }
    }

    // BUG-FIX For PR1192715 by kaiyuan.ma begin
    private void checkStoragePermissionAndRepop() {
        int[] grantResult = PermissionUtil.checkPermissions(this,
                new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE });
        if (grantResult[0] != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "check storage permission failed!!!");
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-25,Defect:1075189 begin
            Intent intent = getIntent();
            if (intent == null) {
                intent = new Intent();
            }
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-25,Defect:1075189 end
            intent.setClass(this, PermissionActivity.class);
            startActivity(intent);
            finish();
        }
    }
    // BUG-FIX For PR1192715 by kaiyuan.ma end

    private class MyPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

        public MyPagerAdapter(FragmentManager paramFragmentManager) {
            super(paramFragmentManager);
        }

        public int getCount() {
            return mFragmentList.size();
        }

        public Fragment getItem(int index) {
            return mFragmentList.get(index);
        }

        public void onPageScrollStateChanged(int index) {
            if (mMenu != null) {
                mMenu.close();
            }
        }

        public void onPageScrolled(int paramInt1, float paramFloat, int paramInt2) {
            resetToolBarPosition();
            showTabsView();
        }

        public void onPageSelected(int index) {
            if (mViewPager.getPagingEnabled()) {
                switchPage(index);
            }
        }
    }

    private void switchPage(int index) {
        if (mCurrentPage == index) {
            return;
        }
        mCurrentPage = index;
        switchTab(index);
        mContent = ((AbstractGalleryFragment) mAdapter.getItem(index));
        mViewPager.setCurrentItem(index, false);
        mAdapter.notifyDataSetChanged();
        mBottomNavigationBar.setCurrentItem(index);
    }

    public void setViewPagerState(boolean state) {
        if (mViewPager != null) {
            mViewPager.setPagingEnabled(state);
        }
    }

    public void showTabsView() {
        if(mBottomNavigationBar != null){
            mBottomNavigationBar.restoreBottomNavigation(true);
        }
    }

    public void hideTabsView() {
        if (mBottomNavigationBar != null) {
            mBottomNavigationBar.hideBottomNavigation(true);
        }
    }

    public void displayTabsView() {
        if(mBottomNavigationBar != null){
            mBottomNavigationBar.setVisibility(View.VISIBLE);
            showTabsView();
        }
    }

    public void removeTabsView() {
        if (mBottomNavigationBar != null) {
            mBottomNavigationBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void hideToolBarView() {
        if (mToolbar != null) {
            mToolbar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void showToolBarView() {
        if (mToolbar != null) {
            mToolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "GalleryActivity onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode
                + " intent=" + data);
        if (mGetContent && data != null) {
            setResult(resultCode, data);
            finish();
        }
    }

    private void saveToolBarPosition() {
        mToolbarPosition = mTransY;
    }

    private void restoreToolbarPosition() {
        mTransY = mToolbarPosition;
    }

    @Override
    public void resetToolbar() {
        super.resetToolbar();
        if (mToolbar != null) {
            setActionBar(mToolbar);
        }
    }

    private void switchTab(int tab) {
        switch (tab) {
            case PAGE_MOMENTS:
                setTitle(R.string.main_tab_moments);
                break;
            case PAGE_ALBUMS:
                setTitle(R.string.albums);
                break;
            default:
                break;
        }
    }

    @Override
    public void startPhotoPage(Bundle data, Drawable drawable) {
        super.startPhotoPage(data, drawable);
        setViewPagerState(false);
    }

    @Override
    public void startSlideShow(Bundle data) {
        super.startSlideShow(data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mContent instanceof PhotoFragment) {
            setViewPagerState(true);
        }
    }

    public void setMenu(Menu menu) {
        mMenu = menu;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

}

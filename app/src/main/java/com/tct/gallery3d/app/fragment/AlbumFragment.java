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

package com.tct.gallery3d.app.fragment;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.tct.gallery3d.BuildConfig;
import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AlbumActivity;
import com.tct.gallery3d.app.DividerPhotoItemDecoration;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.app.NewAlbumDialogActivity;
import com.tct.gallery3d.app.SystemBarTintManager.SystemBarConfig;
import com.tct.gallery3d.app.adapter.AlbumDataAdapter;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.data.DataSourceType;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.image.Utils;
import com.tct.gallery3d.util.ScreenUtils;

import java.io.File;

public class AlbumFragment extends GalleryFragment {

    public static final String TAG = "AlbumFragment";
    private static final int FIRST_PHOTO_INDEX = 0;
    private static final String PICTURES_ALBUM_PATH = Environment.getExternalStorageDirectory() +"/Pictures";
    private static final String ORIGINAL_ALBUM_PATH = Environment.getExternalStorageDirectory().toString();

    private AlbumDataAdapter mAdapter;
    private AbstractGalleryActivity mContext;
    private RecyclerView mRecyclerView;
    private ActionBar mActionBar;
    private boolean mGetContent;
    private String mMediaPath;
    private static final int DEFAULT_COLUMNS = 3;
    private static final int PORTRAIT_COLUMNS = 4;
    private static final int LANDSCAPE_COLUMNS = 6;
    private boolean mScrollUp = false;
    private MediaSet mMediaSet;
    private MenuItem mRenameItem = null;
    private MenuItem mDeleteItem = null;
    private Menu mMenu = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (AbstractGalleryActivity) getGalleryContext();
        Bundle localBundle = getArguments();
        mContext.initSystemBar(false);
        mGetContent = localBundle.getBoolean(GalleryActivity.KEY_GET_CONTENT, false);
        mMediaPath = localBundle.getString(GalleryConstant.KEY_MEDIA_PATH);
        mMediaSet = mContext.getDataManager().getMediaSet(mMediaPath);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.album_main_page, container, false);
        mNoContentView = (RelativeLayout) v.findViewById(R.id.no_content); // MODIFIED by Yaoyu.Yang, 2016-08-18,BUG-2208330
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        initSystemUI();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        GridLayoutManager manager = new GridLayoutManager(mContext, DEFAULT_COLUMNS);
        mRecyclerView.setLayoutManager(manager);
//        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // TODO :Disable the animator in album page.
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.addItemDecoration(new DividerPhotoItemDecoration(mContext));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mContext.onScrollPositionChanged(dy);
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    mScrollUp = true;
                } else {
                    mScrollUp = false;
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mContext.toolbarDisplay();
                }
                if (mAdapter.getFirstPosition() <= 1 && !mScrollUp) {
                    mContext.resetToolBarPosition();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

        });
        mAdapter = new AlbumDataAdapter(this);
        mAdapter.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        initColumns();
        initActionBar();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.resume();
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if (mAdapter.isInSelectionMode()) {
            mContext.setStatusEnable(true);
            mContext.setStatusColor(SystemBarTintManager.STATUSBAR_COLOR_BLACK);
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.clearOnScrollListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.destroy();
    }

    @Override
    protected void onActionResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == GalleryActivity.RESULT_OK) {
            switch (requestCode) {
                case GalleryConstant.REQUEST_MOVE:
                    mContext.getDataManager()
                            .setObjectPath(data.getStringExtra(GalleryConstant.KEY_PATH_RETURN));
                    mAdapter.getActionModeHandler().move();
                    break;
                case GalleryConstant.REQUEST_COPY:
                    mContext.getDataManager()
                            .setObjectPath(data.getStringExtra(GalleryConstant.KEY_PATH_RETURN));
                    mAdapter.getActionModeHandler().copy();
                    break;
                case GalleryConstant.REQUEST_RENAME_ALBUM:
                    if (data != null && data.getBooleanExtra(NewAlbumDialogActivity.KEY_RENAME_ALBUM, false))
                        mContext.onBackPressed();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if (!mGetContent) {
            inflater.inflate(R.menu.album, menu);
            mRenameItem = menu.findItem(R.id.action_rename_album);
            mDeleteItem = menu.findItem(R.id.action_delete_album);
            int albumType = mMediaSet.getAlbumType();
            int mediaSetType = mMediaSet.getMediaSetType();
            String albumName = mMediaSet.getName();
            String slowMotion = getResources().getString(R.string.slow_motion);
            if (albumType != DataSourceType.ALBUM_NORMAL || (slowMotion.equals(albumName) && mediaSetType == MediaSet.MEDIASET_TYPE_VIDEO)
                    || mMediaSet.getAlbumFilePath().equals(PICTURES_ALBUM_PATH) || mMediaSet.getAlbumFilePath().equals(ORIGINAL_ALBUM_PATH)) {
                mRenameItem.setVisible(false);
                mDeleteItem.setVisible(false);
            }
        }
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        mMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem slideShowItem = menu.findItem(R.id.action_slideshow);
        canShowSlideShow(slideShowItem);
    }

    public void canShowSlideShow(MenuItem item) {
        if (item == null) {
            return;
        }
        int mediaSetType = mMediaSet.getMediaSetType();
        if (mAdapter.getItemCount() == 0 || (mediaSetType & MediaSet.MEDIASET_TYPE_IMAGE) == 0) {
            if (item != null) {
                item.setVisible(false);
                Log.d(TAG, "This album does not contais a image.So will hide the 'SlideShow Menu'");
            }
        } else {
            if (item != null) {
                item.setVisible(true);
                Log.d(TAG, "This album does contais a image.So will show the 'SlideShow Menu'");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                mContext.onBackPressed();
                return true;
            }
            case R.id.action_cancel:
                return true;
            case R.id.action_select:
                mAdapter.enterSelectionMode(false);
                return true;
            case R.id.action_slideshow: {
                Bundle data = new Bundle();
                data.putInt(GalleryConstant.KEY_INDEX_SLOT, FIRST_PHOTO_INDEX);
                data.putInt(GalleryConstant.KEY_FROM_PAGE, GalleryConstant.FROM_ALBUM_PAGE);
                data.putString(GalleryConstant.KEY_MEDIA_SET_PATH, mMediaPath);
                mContext.startSlideShow(data);
                return true;
            }
            case R.id.action_rename_album:
                Intent intent = new Intent(mContext, NewAlbumDialogActivity.class);
                intent.putExtra(NewAlbumDialogActivity.KEY_RENAME_ALBUM,true);
                intent.putExtra(NewAlbumDialogActivity.KEY_OLD_MEDIA_PATH,mMediaPath);
                mContext.startActivityForResult(intent, GalleryConstant.REQUEST_RENAME_ALBUM);
                return true;
            case R.id.action_delete_album:
                AlertDialog deleteDialog = new AlertDialog.Builder(mContext)
                        .setTitle(R.string.delete_album_dialog_title)
                        .setPositiveButton(R.string.delete_album_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mMediaSet.delete();
                                File oldFile = new File(mMediaSet.getAlbumFilePath());
                                oldFile.delete();
                                mContext.onBackPressed();
                            }
                        }).setNegativeButton(R.string.delete_album_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                return true;
            default:
                return false;
        }
    }

    public boolean initLocation(int slotIndex, int innerIndex) {
        if (mAdapter != null) {
            return mAdapter.initLocation(slotIndex, innerIndex);
        }
        return true;
    }

    public void setContentVisible(boolean visible) {
        if (mAdapter != null) {
            mAdapter.setContentVisible(visible);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initSystemUI();
        if(null != getActivity() && (getActivity() instanceof AlbumActivity)){
            ((AlbumActivity) getActivity()).setStatusColorInActionMode(mAdapter.isInActionMode());
        }
        int length = mRecyclerView.getHeight();
        int numColumns = initSpanCount(newConfig);
        mAdapter.setNumColumns(numColumns);
        final int columnWidth = length / numColumns;
        mAdapter.setItemHeight(columnWidth);
        if (null != mMenu) {
            mMenu.close();
        }
    }

    public void initSystemUI() {
        // Calculate ActionBar height
        SystemBarConfig config = mContext.mTintManager.getConfig();
        int paddingTop = config.getPixelInsetTop(true);
        int paddingBottom = 0;
        int paddingRight = 0;
        boolean hasNavigation = config.hasNavigtionBar();
        if (hasNavigation) {
            boolean atBottom = ScreenUtils.isNavigationAtBottom(mContext);
            if (atBottom) {
                paddingBottom = config.getPixelInsetBottom();
            }
        }
        if(ScreenUtils.splitScreenIsAtBottom(mContext,mContext.getToolbar())){
            paddingTop = config.getActionBarHeight();
        }
        if(hasNavigation && ScreenUtils.getScreenInfo(mContext) == ScreenUtils.tempScreenInLandFull){
            paddingRight = config.getNavigationBarWidth();
        }
        mRecyclerView.setPadding(0, paddingTop, paddingRight, paddingBottom);
        //mRecyclerView.smoothScrollBy(0,-config.getStatusBarHeight());
        if (mAdapter != null) {
            mContext.setMargin(mAdapter.getActionModeHandler().getActionMode(), true);
        }
    }

    private void initColumns() {
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                int width = mRecyclerView.getWidth();
                int numColumns = initSpanCount(getResources().getConfiguration());
                final int columnWidth = (width / numColumns);
                mAdapter.setNumColumns(numColumns);
                mAdapter.setItemHeight(columnWidth);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                }
                if (Utils.hasJellyBean()) {
                    mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    private void initActionBar() {
        setHasOptionsMenu(true);
        mActionBar = mContext.getActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        if (mGetContent) {
            mActionBar.setTitle(R.string.select);
        } else {
            mActionBar.setTitle(getArguments().getString(GalleryConstant.KEY_MEDIA_NAME, ""));
        }
    }

    private int initSpanCount(Configuration configuration) {
       /* int count = PORTRAIT_COLUMNS;
        int orientation = configuration.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            count = PORTRAIT_COLUMNS;
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            count = LANDSCAPE_COLUMNS;
        }*/
            int width = ScreenUtils.getWidth(mContext);
            int realWidth = ScreenUtils.getRealWidth(mContext);
            int height = ScreenUtils.getHeight(mContext);
            if(width>height)
            {
                if(width== realWidth ){
                    // split-screen in portrait !!

                    return PORTRAIT_COLUMNS;
                }else{
                    //fullScreen in Landscape

                    return LANDSCAPE_COLUMNS;
                }

            }else{
                if(realWidth == width){
                    //o fullScreen in portrait
                    return PORTRAIT_COLUMNS;
                }else{
                    // split-screen in Landscape

                    return DEFAULT_COLUMNS;
                }
            }
    }
}

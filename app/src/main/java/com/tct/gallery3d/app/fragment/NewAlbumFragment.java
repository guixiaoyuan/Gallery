package com.tct.gallery3d.app.fragment;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.NewAlbumSelectActivity;
import com.tct.gallery3d.app.SystemBarTintManager.SystemBarConfig;
import com.tct.gallery3d.app.adapter.NewAlbumAdapter;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.section.MomentsCallbacks;
import com.tct.gallery3d.app.view.DragSelectRecyclerView;
import com.tct.gallery3d.image.Utils;
import com.tct.gallery3d.util.ScreenUtils;

public class NewAlbumFragment extends GalleryFragment
        implements OnTouchListener {

    public static final String TAG = "NewAlbumFragment";

    private static final int FIRST_PHOTO_INDEX = 0;
    private DragSelectRecyclerView mRecyclerView;
    private NewAlbumAdapter mAdapter;
    private AbstractGalleryActivity mContext;
    private ActionBar mActionBar;
    private boolean mGetContent;
    private GridLayoutManager mLayoutManager;
    private LoaderManager.LoaderCallbacks mLoaderCallbacks;

    //public static final int DAY_VIEW_COUNT = 4;
    //public static final int MONTH_VIEW_COUNT = 6;
    //public static final int DAY_VIEW_COUNT_LAND = 6;
    //public static final int MONTH_VIEW_COUNT_LAND = 10;
    //public static final int STAGGERED_VIEW_COUNT = 6;
    //private static final int MIN_POISITION = 1;

    //temp screen spancount in everyMode
    int mTempScreenDayModeSpanCount = 4;
    int mTempScreenMonthModeSpanCount = 6;
    int mTempScreenStagModeSpanCount = 10;

    private MenuItem mCameraItem = null;
    private MenuItem mDayViewItem = null;
    private MenuItem mMonthViewItem = null;
    private MenuItem mStaggeredViewItem = null;

    private static final int MAX_POINT = 2;
    private static final float MIN_LENGTH = 100;
    private float mStartLength = 0;
    private boolean isDoubleTouchFirst = true;
    private boolean isDoubleTouch = false;
    private boolean isSelected = false;

    private boolean isEnLarge = false;
    private boolean isLandScape = false;

    /*//TODO FastJumper
    private int mItemWidth;

    private FastJumper mFastJumper;
    private SpannableCallback mJumperCallback;
    private SpannableCallback.ScrollCalculator mLinearScrollCalculator;
    private SpannableCallback.ScrollCalculator mScrollCalculator;*/

    @Override
    public void onCreate(Bundle data) {
        super.onCreate(data);
        initializeData();
    }

    private void initializeData() {
        mContext = (AbstractGalleryActivity) getGalleryContext();
        mContext.initSystemBar(false);
        Bundle localBundle = getArguments();
        if (localBundle == null) {
            return;
        }
        mGetContent = localBundle.getBoolean(GalleryActivity.KEY_GET_CONTENT, false);
        mTargetPath = localBundle.getString(NewAlbumSelectActivity.TARGET_PATH);
    }

    private void initActionBar() {
        setHasOptionsMenu(true);
        mActionBar = mContext.getActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        mActionBar.setTitle(R.string.new_album_select_title);
    }

    private void initItemHeight() {
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    isLandScape = false;
                } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    isLandScape = true;
                }
                setTempScreenSpanCount();
                if (Utils.hasJellyBean()) {
                    mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle data) {
        View view = inflater.inflate(R.layout.moments_page, viewGroup, false);
        mNoContentView = (RelativeLayout) view.findViewById(R.id.no_content_moments);
        mRecyclerView = (DragSelectRecyclerView) view.findViewById(R.id.recycler_view);
        mContext.setMargin(mRecyclerView, false);
        initSystemUI();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new NewAlbumAdapter(this, mRecyclerView);
        mLoaderCallbacks = new MomentsCallbacks(this, mAdapter);
        mRecyclerView.setOnTouchListener(this);
        mRecyclerView.setAdapter(mAdapter);
        setupLayoutManager();

        initItemHeight();
        initActionBar();

        RecyclerView.ItemAnimator animator = new DefaultItemAnimator();
        //RecyclerView.ItemAnimator animator = new GridItemAnimator();
        mRecyclerView.setItemAnimator(animator);

        mAdapter.setPath(mTargetPath);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mContext.onBackPressed();
            default:
                return false;
        }
    }

    private void setupLayoutManager() {
        mLayoutManager = new GridLayoutManager(getContext(), mTempScreenDayModeSpanCount);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        mAdapter.resume();
        if (isSelected) {
            mContext.setStatusEnable(true);
            if (ScreenUtils.getScreenInfo(getActivity()) == ScreenUtils.tempScreenInPortSplit) {
                ((NewAlbumSelectActivity) getActivity()).setStatusEnable(false);
            }
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        mAdapter.pause();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mAdapter.destroy();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.clearOnScrollListeners();
    }

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
                default:
                    break;
            }
        }
    }


    public void setViewItemVisibility() {
        if (mGetContent) {
            return;
        }
        if (mDayViewItem == null || mMonthViewItem == null || mStaggeredViewItem == null) {
            return;
        }
        mDayViewItem.setVisible(true);
        mMonthViewItem.setVisible(true);
        mStaggeredViewItem.setVisible(true);
        switch (mState) {
            case DAY:
                mDayViewItem.setVisible(false);
                break;
        }
    }

    private void initSystemUI() {
        // Calculate ActionBar height
        SystemBarConfig config = mContext.mTintManager.getConfig();
        Resources res = mContext.getResources();
        float tabsHeight = res.getDimension(R.dimen.tab_height);
        int paddingTop = config.getPixelInsetTop(true);
        int paddingBottom = 0;
        boolean hasNavigation = config.hasNavigtionBar();
        if (hasNavigation) {
            if (ScreenUtils.getScreenInfo(getActivity()) == ScreenUtils.tempScreenInPortFull) {
                paddingBottom = (int) (config.getPixelInsetBottom() + tabsHeight);
            } else {
                paddingBottom = (int) tabsHeight;
            }
        }
        mRecyclerView.setPadding(0, paddingTop, 0, paddingBottom);
    }

    public void setTempScreenSpanCount() {
        int width = ScreenUtils.getWidth(getActivity());
        int realWidth = ScreenUtils.getRealWidth(getActivity());
        int height = ScreenUtils.getHeight(getActivity());
        if (width > height) {
            if (width == realWidth) {
                // split-screen in portrait !!
                Log.d(TAG, "go to split-screen in portrait");
                mTempScreenDayModeSpanCount = 4;
                mTempScreenMonthModeSpanCount = 6;
                mTempScreenStagModeSpanCount = 6;

                if (mState == State.DAY) {
                    mLayoutManager.setSpanCount(mTempScreenDayModeSpanCount);
                }

            } else {
                //fullScreen in Landscape
                Log.d(TAG, "go to fullScreen in Landscape");
                mTempScreenDayModeSpanCount = 6;
                mTempScreenMonthModeSpanCount = 10;
                mTempScreenStagModeSpanCount = 6;

                if (mState == State.DAY) {
                    mLayoutManager.setSpanCount(mTempScreenDayModeSpanCount);
                }
            }
        } else {
            if (realWidth == width) {
                //o fullScreen in portrait
                Log.d(TAG, "go to fullScreen in portrait");
                mTempScreenDayModeSpanCount = 4;
                mTempScreenMonthModeSpanCount = 6;
                mTempScreenStagModeSpanCount = 6;

                if (mState == State.DAY) {
                    mLayoutManager.setSpanCount(mTempScreenDayModeSpanCount);
                }

            } else {
                // split-screen in Landscape
                Log.d(TAG, "go to split-screen in Landscape");
                mTempScreenDayModeSpanCount = 3;
                mTempScreenMonthModeSpanCount = 5;
                mTempScreenStagModeSpanCount = 6;

                if (mState == State.DAY) {
                    mLayoutManager.setSpanCount(mTempScreenDayModeSpanCount);
                }

            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // mContext.initSystemBar(false);
        initSystemUI();
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            isLandScape = false;
        } else {
            isLandScape = true;
        }
        setTempScreenSpanCount();
        mContext.setMargin(mAdapter.getActionModeHandler().getActionMode(), true);
        mContext.setMargin(mRecyclerView, false);
        if (isSelected) {
            mContext.setStatusEnable(true);
            if (ScreenUtils.getScreenInfo(getActivity()) == ScreenUtils.tempScreenInPortSplit) {
                mContext.setStatusEnable(false);
            }
        }
    }

    public void setSelectionModeStatus(boolean isSelected) {
        this.isSelected = isSelected;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int pointCount = event.getPointerCount();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                if (pointCount >= MAX_POINT) {
                    float length = spacing(event);
                    if (isDoubleTouchFirst) {
                        mStartLength = length;
                    }
                    isDoubleTouchFirst = false;
                    if (isDoubleTouch || isSelected)
                        break;
                    if (length - mStartLength > MIN_LENGTH) {
                        if (mState == State.DAY) {
                            isEnLarge = true;
                            isDoubleTouch = true;
                        }
                    } else if (mStartLength - length > MIN_LENGTH) {
                        if (mState == State.DAY) {
                            isEnLarge = false;
                            isDoubleTouch = true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                isDoubleTouch = false;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDoubleTouchFirst = true;
                break;
        }
        return isDoubleTouch;
    }

    @Override
    public void onActivityCreated(Bundle paramBundle) {
        super.onActivityCreated(paramBundle);
        initLoader();
    }

    private void initLoader() {
        if (!mGetContent) {
            getLoaderManager().initLoader(MomentsCallbacks.LOADER_PHOTO, null, mLoaderCallbacks);
        }
    }

    public void restartLoader(boolean pauseWork) {
        if (pauseWork) {
            pauseWork();
        }
        Loader loader = getLoaderManager().getLoader(MomentsCallbacks.LOADER_PHOTO);
        if (loader != null) {
            loader.onContentChanged();
        } else {
            onLoadFinished();
        }
//        getLoaderManager().restartLoader(MomentsCallbacks.LOADER_PHOTO, null, mLoaderCallbacks);
    }

    private String mTargetPath;

    public void startCopyOrMove() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.new_album_confirm_title)
                .setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mTargetPath == null) {
                            return;
                        }
                        mContext.getDataManager()
                                .setObjectPath(mTargetPath);
                        mAdapter.getActionModeHandler().move();
                    }
                })
                .setNegativeButton(R.string.copy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mTargetPath == null) {
                            return;
                        }
                        mContext.getDataManager()
                                .setObjectPath(mTargetPath);
                        mAdapter.getActionModeHandler().copy();
                    }
                })
                .setNeutralButton(R.string.create_new_album_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContext.finish();
                    }
                })
                .show();
    }

    @Override
    public void onLoadFinished() {
        setViewItemVisibility();
        resumeWork();
        if (mState == State.STAGGERED) {
            mAdapter.setSpanList();
        }
        mAdapter.setState(mState);
        setTempScreenSpanCount();
//        mAdapter.notifyDataSetChanged();
    }
}
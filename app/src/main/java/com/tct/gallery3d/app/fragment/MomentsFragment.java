package com.tct.gallery3d.app.fragment;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.ActivityNotFoundException;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AlbumActivity;
import com.tct.gallery3d.app.DividerPhotoItemDecoration;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.SystemBarTintManager.SystemBarConfig;
import com.tct.gallery3d.app.adapter.MomentsAdapter;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.section.MomentsCallbacks;
import com.tct.gallery3d.app.view.DragSelectRecyclerView;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.LocalMediaItem;
import com.tct.gallery3d.data.MediaSource;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.fastjumper.FastJumper;
import com.tct.gallery3d.fastjumper.LinearScrollCalculator;
import com.tct.gallery3d.fastjumper.SpannableCallback;
import com.tct.gallery3d.image.Utils;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.PLFUtils;
import com.tct.gallery3d.util.ScreenUtils;

import java.util.ArrayList;

public class MomentsFragment extends GalleryFragment
        implements OnTouchListener {

    private static final String TAG = "MomentsFragment";

    private static final int FIRST_PHOTO_INDEX = 0;
    private DragSelectRecyclerView mRecyclerView;
    private MomentsAdapter mAdapter;
    private AbstractGalleryActivity mContext;
    private ActionBar mActionBar;
    private boolean mGetContent;
    private GridLayoutManager mLayoutManager;
    private LoaderManager.LoaderCallbacks mLoaderCallbacks;

    public static final int DAY_VIEW_COUNT = 4;
    public static final int MONTH_VIEW_COUNT = 6;
    public static final int DAY_VIEW_COUNT_LAND = 6;
    public static final int MONTH_VIEW_COUNT_LAND = 10;
    public static final int STAGGERED_VIEW_COUNT = 6;
    //private static final int MIN_POISITION = 1;

    //temp screen span count in everyMode
    private int mTempScreenDayModeSpanCount = DAY_VIEW_COUNT;
    private int mTempScreenMonthModeSpanCount = MONTH_VIEW_COUNT;
    private int mTempScreenStagModeSpanCount = STAGGERED_VIEW_COUNT;

    private MenuItem mCameraItem = null;
    private MenuItem mDayViewItem = null;
    private MenuItem mMonthViewItem = null;
    private MenuItem mStaggeredViewItem = null;
    private MenuItem mFaceShowItem = null;
    private MenuItem mAnimatorON = null;
    private MenuItem mAnimatorOFF = null;

    private static final int MAX_POINT = 2;
    private static final float MIN_LENGTH = 100;
    private float mStartLength = 0;
    private boolean isDoubleTouchFirst = true;
    private boolean isDoubleTouch = false;
    private boolean isSelected = false;

    private boolean isEnLarge = false;
    private boolean isLandScape = false;

    //TODO FastJumper
    private int mItemWidth;

    private FastJumper mFastJumper;
    private SpannableCallback mJumperCallback;
    private SpannableCallback.ScrollCalculator mLinearScrollCalculator;
    private SpannableCallback.ScrollCalculator mScrollCalculator;

    private String[] mAllFaceshowPaths = null;
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
    }

    private void initActionBar() {
        setHasOptionsMenu(true);
        mActionBar = mContext.getActionBar();
        if (mGetContent) {
            mActionBar.setDisplayShowHomeEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            mActionBar.setTitle(R.string.select);
        } else {
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        }
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
        initSystemUI();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new MomentsAdapter(this, mRecyclerView);
        mLoaderCallbacks = new MomentsCallbacks(this, mAdapter);
        mRecyclerView.setOnTouchListener(this);
        mRecyclerView.setAdapter(mAdapter);
        setupLayoutManager();

        initItemHeight();
        initActionBar();
        initFastView();
        attachFastJumper();

        // TODO :Disable the animator in album page.
//        RecyclerView.ItemAnimator animator = new DefaultItemAnimator();
        //RecyclerView.ItemAnimator animator = new GridItemAnimator();
//        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.addItemDecoration(new DividerPhotoItemDecoration(mContext));
    }

    private void setupLayoutManager() {
        mLayoutManager = new GridLayoutManager(getContext(), mTempScreenDayModeSpanCount);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter.setLayoutManager(mLayoutManager);
    }

    private void attachFastJumper() {
        mFastJumper.attachToRecyclerView(null);
        mScrollCalculator = mLinearScrollCalculator;
        mJumperCallback.setScrollCalculator(mScrollCalculator);
        mFastJumper.attachToRecyclerView(mRecyclerView);
        mFastJumper.invalidate();
    }

    private void initFastView() {
        mLinearScrollCalculator = new LinearScrollCalculator(mRecyclerView) {
            @Override
            public int getItemHeight(int position) {
                return mAdapter.getItemHeight(position);
            }

            @Override
            public int getSpanSize(int position) {
                return mAdapter.getSpanSize(position);
            }

            @Override
            public int getSpanCount() {
                return mAdapter.getSpanCount();
            }
        };

        mJumperCallback = new SpannableCallback() {
            @Override
            public boolean isSectionEnable() {
                return true;
            }

            @Override
            public String getSection(int position) {
                return mAdapter.getSectionForPosition(position).getTime();
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        mFastJumper = new FastJumper(mJumperCallback);
        mFastJumper.addListener(new FastJumper.Listener() {
            @Override
            public void onScrolled(float progress) {
                if (progress == 0 || progress >= 1.0) {
                    ((GalleryActivity) mContext).showTabsView();
                    mContext.resetToolBarPosition();
                }
            }

            @Override
            public void onStateChange(int state) {
                if (state == FastJumper.STATE_DRAGGING) {
                    pauseWork();
                } else {
                    resumeWork();
//                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        android.widget.Toolbar toolbar = mContext.getToolbar();
        if (toolbar != null) toolbar.setTitle(R.string.main_tab_moments);
        if (!mGetContent) {
            inflater.inflate(R.menu.moments_page_menu, menu);
            mCameraItem = menu.findItem(R.id.camera_switcher);
            mDayViewItem = menu.findItem(R.id.action_day_view);
            mMonthViewItem = menu.findItem(R.id.action_month_view);
            mStaggeredViewItem = menu.findItem(R.id.action_collage_view);
            mAnimatorON = menu.findItem(R.id.animator_on);
            mAnimatorOFF = menu.findItem(R.id.animator_off);
            mFaceShowItem = menu.findItem(R.id.action_face_show);
            setViewItemVisibility();
            if (GalleryUtils.isAnyCameraAvailable(mContext) && GalleryUtils.isCameraAvailable(mContext)) {
                mCameraItem.setVisible(true);
            } else {
                mCameraItem.setVisible(false);
            }
            if (ScreenUtils.isInSplitScreen(mContext)) {
                mFaceShowItem.setEnabled(false);
            } else {
                mFaceShowItem.setEnabled(true);
            }
//            RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
//            if (animator == null) {
//                mAnimatorON.setVisible(true);
//            } else {
//                mAnimatorOFF.setVisible(true);
//            }
        }
        ((GalleryActivity)mContext).setMenu(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        updateFaceShowMenuItem();
        int currentpage = ((GalleryActivity) mContext).getCurrentPage();
        if (currentpage != GalleryActivity.PAGE_MOMENTS) {
            menu.clear();
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        mAdapter.resume();
        /*if (isSelected) {
            mContext.setStatusEnable(true);
            if (ScreenUtils.getScreenInfo(mContext) == ScreenUtils.tempScreenInPortSplit) {
                mContext.setStatusEnable(false);
            }
        }*/
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
        mFastJumper.clearListeners();
    }

    protected void onActionResult(int requestCode, int resultCode, Intent data) {
        if (null != mContext && resultCode == GalleryActivity.RESULT_OK) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (((GalleryActivity) mContext).getCurrentPage() != GalleryActivity.PAGE_MOMENTS) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_create_collage:
                mAdapter.enterSelectionMode(false);
                return true;
            case R.id.action_cancel:
                return true;
            case R.id.action_select:
                mAdapter.enterSelectionMode(false);
                return true;
            case R.id.action_slideshow: {
                if (mAdapter.getItemCount() == 0 || !mAdapter.canSlideShow()) {
                    Toast.makeText(mContext, R.string.slideshow_alert, Toast.LENGTH_SHORT).show();
                    return false;
                }
                String albumSetPath = mAdapter.getAlbumPath();
                Bundle data = new Bundle();
                data.putInt(GalleryConstant.KEY_INDEX_SLOT, FIRST_PHOTO_INDEX);
                data.putInt(GalleryConstant.KEY_FROM_PAGE, GalleryConstant.FROM_MOMENTS_PAGE);
                data.putString(GalleryConstant.KEY_MEDIA_SET_PATH, albumSetPath);
                mContext.startSlideShow(data);
                return true;
            }
            case R.id.action_day_view:
                mState = State.DAY;
                restartLoader(true);
                return true;
            case R.id.action_month_view:
                mState = State.MONTH;
                restartLoader(true);
                return true;
            case R.id.action_collage_view:
                mState = State.STAGGERED;
//                mLayoutManager.setSpanCount(mTempScreenStagModeSpanCount);
                restartLoader(true);
                return true;
            case R.id.camera_switcher:
                GalleryUtils.startCameraActivity(mContext);
                return true;
            case R.id.action_face_show:
                //ArrayList<String> faceshow = ExifInfoFilter.getInstance(mContext).queryFaceshow(mContext, 0);
                initFaceShowData();
                if (null != mAllFaceshowPaths && mAllFaceshowPaths.length > 0) {
                    launchFaceShow();
                } else {
                    Toast.makeText(mContext, R.string.slideshow_alert, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_slow_motion:
                Bundle slowMotionData = new Bundle();
                slowMotionData.putString(GalleryConstant.KEY_MEDIA_PATH, MediaSource.LOCAL_SLOWMOTION_SET_PATH);
                slowMotionData.putString(GalleryConstant.KEY_MEDIA_NAME, getString(R.string.slow_motion));
                Intent slowMotionIntent = new Intent(mContext, AlbumActivity.class);
                if (mGetContent) {
                    slowMotionData.putBoolean(GalleryActivity.KEY_GET_CONTENT, mGetContent);
                }
                slowMotionIntent.putExtras(slowMotionData);
                mContext.startActivityForResult(slowMotionIntent, 0);
                return true;
            case R.id.animator_on:
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                return true;
            case R.id.animator_off:
                mRecyclerView.setItemAnimator(null);
                return true;
            default:
                return false;
        }
    }

    /**
     * init query faceshow data
     * @return
     */
    private String[] initFaceShowData() {
        ArrayList<String> faceshow = ExifInfoFilter.getInstance(mContext).queryFaceshow(mContext, 0);
        mAllFaceshowPaths = null;
        if (faceshow != null) {
            Path path = Path.fromString(GalleryConstant.DEFAULT_PATH);
            mAllFaceshowPaths = new String[faceshow.size()];
            DataManager data = DataManager.from(mContext);
            int i = 0;
            for (String id : faceshow) {
                Path childPath = path.getChild(id);
                LocalMediaItem item = (LocalMediaItem) data.getMediaObject(childPath);
                mAllFaceshowPaths[i] = item.getFilePath();
                i++;
            }
           return mAllFaceshowPaths;
        }
        return mAllFaceshowPaths;
    }

    /**
     * launch muvee app to show faceshow
     */
    private void launchFaceShow() {
        if (mAllFaceshowPaths.length < GalleryConstant.minMuveeCount) {
            Toast.makeText(mContext, R.string.muvee_picture_list_min_count_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.setAction(GalleryConstant.MUVEE_FACESHOW_FACE_SHOW);
        intent.putExtra(GalleryConstant.MUVEE_FACESHOW_THEME, GalleryConstant.MUVEE_FACESHOW_THEME_ID);
        intent.putExtra(GalleryConstant.MUVEE_FACESHOW_SELFIEFOLDER, GalleryConstant.FACESHOW_SELFIEFOLDER);
        intent.putExtra(GalleryConstant.MUVEE_FACESHOW_IMAGELIST, mAllFaceshowPaths);
        intent.putExtra(GalleryConstant.MUVEE_FACESHOW_OUTPUT_RESOLUTION, GalleryConstant.DEFAULT_FACESHOW_OUTPUT_RESOLUTION);
        intent.putExtra(GalleryConstant.MUVEE_FACESHOW_OUTPUT_BITRATE, GalleryConstant.DEFAULT_FACESHOW_OUTPUT_BITRATE);
        if (PLFUtils.getBoolean(mContext, GalleryConstant.DEF_NAME)) {
            intent.putExtra(GalleryConstant.MUVEE_SHARE_USECUSTOM, true);
        } else {
            intent.putExtra(GalleryConstant.MUVEE_SHARE_USECUSTOM, false);
        }

        // we should catch exception and remind the user if Muvee is not installed
        try{
            startActivity(intent);
        }catch (ActivityNotFoundException exception){
            Toast.makeText(mContext,R.string.no_muvee_app_toast,Toast.LENGTH_SHORT).show();
        }
    }

    private void setViewItemVisibility() {
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
            case MONTH:
                mMonthViewItem.setVisible(false);
                break;
            case STAGGERED:
                mStaggeredViewItem.setVisible(false);
                break;
        }
    }

    public boolean initLocation(int slotIndex, int innerIndex) {
        Log.d(TAG, "initLocation -- slotIndex = " + slotIndex + " innerIndex = " + innerIndex);
        int headerNum = 0;
        //TODO
        if (mAdapter != null
                && mAdapter.getItemViewType(innerIndex) != MomentsAdapter.VIEW_TYPE_SECTION) {
            return mAdapter.initLocation(slotIndex, innerIndex + headerNum);
        }
        return true;
    }

    public void setContentVisible(boolean visible) {
        if (mAdapter != null) {
            mAdapter.setContentVisible(visible);
        }
    }

    public void initSystemUI() {
        // Calculate ActionBar height
        if(null == mContext){
            return;
        }
        SystemBarConfig config = mContext.mTintManager.getConfig();
        Resources res = mContext.getResources();
        float tabsHeight = res.getDimension(R.dimen.tab_height);
        int paddingTop = config.getPixelInsetTop(true);
        int paddingBottom = 0;
        boolean hasNavigation = config.hasNavigtionBar();
        if (hasNavigation && ScreenUtils.getScreenInfo(mContext) == ScreenUtils.tempScreenInPortFull) {
            paddingBottom = (int) (config.getPixelInsetBottom() + tabsHeight);
        } else {
            paddingBottom = (int) tabsHeight;
        }

        // if screen at bottom,we should reset the postion of mRecyclerView
        if(ScreenUtils.splitScreenIsAtBottom(mContext,mContext.getToolbar())){
            paddingTop = config.getActionBarHeight();
        }
        mRecyclerView.setPadding(0, paddingTop, 0, paddingBottom);
       // mRecyclerView.smoothScrollBy(0,-config.getStatusBarHeight());
    }

    private void setTempScreenSpanCount() {
        int width = ScreenUtils.getWidth(mContext);
        int realWidth = ScreenUtils.getRealWidth(mContext);
        int height = ScreenUtils.getHeight(mContext);
        if (width > height) {
            if (width == realWidth) {
                // split-screen in portrait !!
                Log.d(TAG, "go to split-screen in portrait");
                mTempScreenDayModeSpanCount = 4;
                mTempScreenMonthModeSpanCount = 6;
                mTempScreenStagModeSpanCount = STAGGERED_VIEW_COUNT;
            } else {
                //fullScreen in Landscape
                Log.d(TAG, "go to fullScreen in Landscape");
                mTempScreenDayModeSpanCount = DAY_VIEW_COUNT_LAND;
                mTempScreenMonthModeSpanCount = MONTH_VIEW_COUNT_LAND;
                mTempScreenStagModeSpanCount = STAGGERED_VIEW_COUNT;
            }
        } else {
            if (realWidth == width) {
                //o fullScreen in portrait
                Log.d(TAG, "go to fullScreen in portrait");
                mTempScreenDayModeSpanCount = DAY_VIEW_COUNT;
                mTempScreenMonthModeSpanCount = MONTH_VIEW_COUNT;
                mTempScreenStagModeSpanCount = STAGGERED_VIEW_COUNT;
            } else {
                // split-screen in Landscape
                Log.d(TAG, "go to split-screen in Landscape");
                mTempScreenDayModeSpanCount = 3;
                mTempScreenMonthModeSpanCount = 5;
                mTempScreenStagModeSpanCount = STAGGERED_VIEW_COUNT;
            }
        }
        int spanCount = mTempScreenDayModeSpanCount;
        switch (mState) {
            case DAY:
                spanCount = mTempScreenDayModeSpanCount;
                break;
            case MONTH:
                spanCount = mTempScreenMonthModeSpanCount;
                break;
            case STAGGERED:
                spanCount = mTempScreenStagModeSpanCount;
                break;
        }
        mLayoutManager.setSpanCount(spanCount);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mFaceShowItem != null) {
            if (ScreenUtils.isInSplitScreen(mContext)) {
                mFaceShowItem.setEnabled(false);
            } else {
                mFaceShowItem.setEnabled(true);
            }
        }

        Log.d(TAG,"onConfigurationChanged" +mAdapter.isInActionMode());
        // mContext.initSystemBar(false);
        initSystemUI();
        if (null != getActivity() && (getActivity() instanceof GalleryActivity)) {
            ((GalleryActivity) getActivity()).setStatusColorInActionMode(mAdapter.isInActionMode());
        }
        hideFastJumperDateTag();
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            isLandScape = false;
        } else {
            isLandScape = true;
        }
        setTempScreenSpanCount();
        mContext.setMargin(mAdapter.getActionModeHandler().getActionMode(), true);
       /* if (isSelected) {
            mContext.setStatusEnable(true);
            if (ScreenUtils.getScreenInfo(mContext) == ScreenUtils.tempScreenInPortSplit) {
                mContext.setStatusEnable(false);
            }
        }*/
        // refresh interface for items divider
        mAdapter.notifyDataSetChanged();
    }

    public void setSelectionModeStatus(boolean isSelected) {
        this.isSelected = isSelected;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void changeViewMode() {
        hideFastJumperDateTag();
        switch (mState) {
            case MONTH:
                mState = State.DAY;
                break;
            case DAY:
                if (isEnLarge) {
                    mState = State.STAGGERED;
//                    mLayoutManager.setSpanCount(mTempScreenStagModeSpanCount);
                } else {
                    mState = State.MONTH;
                }
                break;
            case STAGGERED:
                mState = State.DAY;
                break;
            default:
                break;
        }
        restartLoader(true);
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
                        if (mState == State.MONTH) {
                            isDoubleTouch = true;
                        }
                        if (mState == State.DAY) {
                            isEnLarge = true;
                            isDoubleTouch = true;
                        }
                    } else if (mStartLength - length > MIN_LENGTH) {
                        if (mState == State.DAY) {
                            isEnLarge = false;
                            isDoubleTouch = true;
                        }
                        if (mState == State.STAGGERED) {
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
                if (isDoubleTouch) {
                    changeViewMode();
                    isDoubleTouch = false;
                }
                break;
        }
        return isDoubleTouch;
    }

    private void hideFastJumperDateTag() {
        mRecyclerView.stopScroll();
        if (mFastJumper != null) {
            mFastJumper.hide();
        }
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

    @Override
    public void onLoadFinished() {
        setViewItemVisibility();
        resumeWork();
        //if (mState == State.STAGGERED) {
        mAdapter.setSpanList();
        //}
        mAdapter.setState(mState);
        setTempScreenSpanCount();
//        mAdapter.notifyDataSetChanged();
    }

    /**
     * update menu item  if uninstall or install faceshow apk
     */
    private void updateFaceShowMenuItem(){
        if (null != mFaceShowItem) {
            if (GalleryUtils.hasFaceShowAPK(mContext)) {
                mFaceShowItem.setVisible(true);
            } else {
                mFaceShowItem.setVisible(false);
            }
        }
    }
}

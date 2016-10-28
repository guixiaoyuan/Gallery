package com.tct.gallery3d.app.fragment;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.CollapseAlbumsManageActivity;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.SystemBarTintManager;
import com.tct.gallery3d.app.adapter.CollapseAlbumSetDataAdapter;
import com.tct.gallery3d.util.ScreenUtils;

public class CollapseAlbumFragment extends GalleryFragment {
    public static final String TAG = CollapseAlbumFragment.class.getSimpleName();

    private AbstractGalleryActivity mContext;
    private ActionBar mActionBar;
    private boolean mGetContent;

    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private CollapseAlbumSetDataAdapter mAdapter;
    private static final int PORTRAIT_INDEX = 2;
    private static final int LANDSCAPE_INDEX = 3;

    private Handler mHandler = new Handler();

    private boolean mScrollUp = false;

    @Override
    public void onCreate(Bundle data) {
        super.onCreate(data);
        mContext = (AbstractGalleryActivity) getActivity();
        setHasOptionsMenu(true);
        Bundle localBundle = getArguments();
        mGetContent = localBundle.getBoolean(GalleryActivity.KEY_GET_CONTENT, false);
    }

    private void initActionBar() {
        mActionBar = mContext.getActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        mActionBar.setTitle(R.string.collapse_albums);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle data) {
        View view = inflater.inflate(R.layout.albumset_main_page, viewGroup, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mNoContentView = (RelativeLayout) view.findViewById(R.id.no_content_album_list);
        initSystemUI();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        init();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mAdapter.getLastPosition() == mAdapter.getItemCount() - 1) {
                    mContext.resetToolBarPosition();
                } else {
                    mContext.onScrollPositionChanged(dy);
                }
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
        mRecyclerView.setItemAnimator(null);
        initActionBar();
    }

    public void init() {
        int count = initSpanCount(getResources().getConfiguration());
        mLayoutManager = new GridLayoutManager(mContext, count);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CollapseAlbumSetDataAdapter(this, mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

//        RecyclerView.ItemAnimator animator = new GridItemAnimator();
//        animator.setChangeDuration(0);
//        mRecyclerView.setItemAnimator(animator);
        // TODO :Disable the animator in album page.
    }

    @Override
    public void onResume() {
        init();
        super.onResume();
        mAdapter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        mAdapter.pause();
        super.onStop();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        android.widget.Toolbar toolbar = mContext.getToolbar();
        if (toolbar != null) toolbar.setTitle(R.string.collapse_albums);
        if (!mGetContent) {
            inflater.inflate(R.menu.collapse_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.action_collapse:
                intent = new Intent(mContext, CollapseAlbumsManageActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                getActivity().onBackPressed();
            default:
                return false;
        }
    }


    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initSystemUI();
        int count = initSpanCount(newConfig);
        mLayoutManager.setSpanCount(count);
        if (mContext.getContent() != this) {
            mHandler.postDelayed(runnable, 50);
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mAdapter.notifyDataSetChanged();
        }
    };

    public void initSystemUI() {
        // Calculate ActionBar height
        if (null == mContext) {
            return;
        }
        SystemBarTintManager.SystemBarConfig config = mContext.mTintManager.getConfig();
        Resources res = mContext.getResources();
        float tabsHeight = res.getDimension(R.dimen.tab_height);
        int paddingTop = config.getPixelInsetTop(true);
        int paddingBottom = 0;
        paddingTop = 168;
        boolean hasNavigation = config.hasNavigtionBar();
        if (hasNavigation) {
            if (ScreenUtils.getScreenInfo(mContext) == ScreenUtils.tempScreenInPortFull) {
                paddingBottom = (int) (config.getPixelInsetBottom() + tabsHeight);
            } else {
                paddingBottom = (int) tabsHeight;
            }
        }

        // if screen at bottom,we should reset the postion of mRecyclerView
        if (ScreenUtils.splitScreenIsAtBottom(mContext, mContext.getToolbar())) {
            paddingTop = config.getActionBarHeight();
        }
        mRecyclerView.setPadding(0, paddingTop, 0, 0);
        // mRecyclerView.smoothScrollBy(0,-config.getStatusBarHeight());
    }

    private int initSpanCount(Configuration configuration) {
        int count = PORTRAIT_INDEX;
        if (ScreenUtils.getScreenInfo(getActivity()) == ScreenUtils.tempScreenInLandFull) {
            count = LANDSCAPE_INDEX;
        }
        return count;
    }
}

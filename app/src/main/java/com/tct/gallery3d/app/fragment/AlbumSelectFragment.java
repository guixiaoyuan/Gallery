package com.tct.gallery3d.app.fragment;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.SystemBarTintManager;
import com.tct.gallery3d.app.adapter.AlbumSelectDataAdapter;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.util.ScreenUtils;

public class AlbumSelectFragment extends GalleryFragment {
    public static final String TAG = "AlbumSelectFragment";

    private AbstractGalleryActivity mContext;
    private ActionBar mActionBar;

    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private AlbumSelectDataAdapter mAdapter;
    private static final int PORTRAIT_INDEX = 2;
    private static final int LANDSCAPE_INDEX = 3;


    @Override
    public void onCreate(Bundle paramBundle) {
        mContext = (AbstractGalleryActivity) getGalleryContext();
        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
        setHasOptionsMenu(true);
        super.onCreate(paramBundle);
    }

    private void initActionBar() {
        Bundle data = getArguments();
        mActionBar = mContext.getActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        mActionBar.setTitle(data.getString(GalleryConstant.KEY_SET_TITLE));
    }

    @Override
    protected void onActionResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == GalleryActivity.RESULT_OK) {
            switch (requestCode) {
                case GalleryConstant.REQUEST_NEW_ALBUM:
                    getActivity().setResult(GalleryActivity.RESULT_OK, data);
                    getActivity().finish();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.albumset_main_page, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        int count = initSpanCount();

        mLayoutManager = new GridLayoutManager(mContext, count);
        mRecyclerView.setLayoutManager(mLayoutManager);

        RecyclerView.ItemAnimator animator = new DefaultItemAnimator();
        mRecyclerView.setItemAnimator(animator);

        mNoContentView = (RelativeLayout) view.findViewById(R.id.no_content_album_list); // MODIFIED by Yaoyu.Yang, 2016-08-18,BUG-2208330
        mAdapter = new AlbumSelectDataAdapter(this, mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        initSystemUI();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.resume();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mContext.initSystemBar(false);
        initSystemUI();
        int count = initSpanCount();
        mLayoutManager.setSpanCount(count);
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

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private void initSystemUI() {
        // Calculate ActionBar height
        SystemBarTintManager.SystemBarConfig config = mContext.mTintManager.getConfig();
        Resources res = mContext.getResources();
        int paddingTop = (int) res.getDimension(R.dimen.tab_height);
        int paddingBottom = 0;
        boolean hasNavigation = config.hasNavigtionBar();
        if (hasNavigation) {
            boolean atBottom = ScreenUtils.isNavigationAtBottom(mContext);
            if (atBottom) {
                paddingBottom = config.getPixelInsetBottom();
            }
        }
        mRecyclerView.setPadding(0, paddingTop, 0, paddingBottom);
    }

    private int initSpanCount() {
        int count = PORTRAIT_INDEX;
        if (ScreenUtils.getScreenInfo(getActivity()) == ScreenUtils.tempScreenInLandFull) {
            count = LANDSCAPE_INDEX;
        }
        return count;
    }
}

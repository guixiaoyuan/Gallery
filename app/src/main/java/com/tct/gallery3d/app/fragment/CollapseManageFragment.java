package com.tct.gallery3d.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AbstractGalleryFragment;
import com.tct.gallery3d.app.DividerPhotoItemDecoration;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.adapter.CollapseManageDataAdapter;


public class CollapseManageFragment extends AbstractGalleryFragment {

    private RecyclerView mRecyclerView;
    protected RelativeLayout mNoContentView;
    private AbstractGalleryActivity mContext;
    private CollapseManageDataAdapter mAdapter;

    private boolean mScrollUp = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (AbstractGalleryActivity) getGalleryContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.collapse_manage_page_layout, container, false);
        mNoContentView = (RelativeLayout) v.findViewById(R.id.no_content); // MODIFIED by Yaoyu.Yang, 2016-08-18,BUG-2208330
        mRecyclerView = (RecyclerView) v.findViewById(R.id.collapse_recycler_view);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(manager);
//        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // TODO :Disable the animator in album page.
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.addItemDecoration(new DividerPhotoItemDecoration(mContext));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mAdapter.getLastPosition() == mAdapter.getItemCount() - 1) {
                    ((GalleryActivity) mContext).showTabsView();
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
        mAdapter = new CollapseManageDataAdapter(this, new LinearLayoutManager(mContext));
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onActionResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public boolean onBackPressed() {
        return false;
    }


    @Override
    public void onStart() {
        super.onStart();
        mAdapter.resume();
    }

    @Override
    public void onResume() {
        super.onResume();
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
        mContext.getDataManager().notifyPrivateMode();
        super.onDestroy();
        mAdapter.destroy();
    }


}

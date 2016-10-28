/*
 * Copyright (C) 2013 The Android Open Source Project
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
/* ----------|----------------------|----------------------|----------------- */
/* 06/12/2015| jian.pan1            | PR1003170            |[5.0][Gallery] Frame grids' position should retain the previous position after cancel/accept editing
/* ----------|----------------------|----------------------|----------------- */
/* 03/05/2016| jian.pan1            |[ALM]Defect:1533434   |[GAPP][Android6.0][Gallery][ANR]Gallery will ANR when edit a picture.
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.filtershow.category;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.filtershow.FilterShowActivity;
import com.tct.gallery3d.filtershow.category.CustomHorizontalScrollView.ScrollType;

public class CategoryPanel extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "CategoryPanel";
    private static final String PARAMETER_TAG = "currentPanel";

    private int mCurrentAdapter = MainPanel.LOOKS;
    private CategoryAdapter mAdapter;
    private IconView mAddButton;
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003170 begin
    private Handler mHandler = new Handler();
    private CustomHorizontalScrollView mScrollview;
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003170 end

    public void setAdapter(int value) {
        mCurrentAdapter = value;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        loadAdapter(mCurrentAdapter);
    }

    public void loadAdapter(int adapter) {
        FilterShowActivity activity = (FilterShowActivity) getActivity();
        switch (adapter) {
            case MainPanel.LOOKS: {
                mAdapter = activity.getCategoryLooksAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.LOOKS);
                }
                activity.updateCategories();
                break;
            }
            case MainPanel.BORDERS: {
                mAdapter = activity.getCategoryBordersAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.BORDERS);
                }
                activity.updateCategories();
                break;
            }
            case MainPanel.GEOMETRY: {
                mAdapter = activity.getCategoryGeometryAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.GEOMETRY);
                }
                break;
            }
            case MainPanel.FILTERS: {
                mAdapter = activity.getCategoryFiltersAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.FILTERS);
                }
                break;
            }
            case MainPanel.VERSIONS: {
                mAdapter = activity.getCategoryVersionsAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.VERSIONS);
                }
                break;
            }
        }
        updateAddButtonVisibility();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(PARAMETER_TAG, mCurrentAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout main = (LinearLayout) inflater.inflate(
                R.layout.filtershow_category_panel_new, container,
                false);

        if (savedInstanceState != null) {
            int selectedPanel = savedInstanceState.getInt(PARAMETER_TAG);
            loadAdapter(selectedPanel);
        }

        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003170 begin
        mScrollview = (CustomHorizontalScrollView) main.findViewById(R.id.category_scrollview);
        if (mScrollview != null) {
            mScrollview.setHandler(mHandler);
            mScrollview.setOnStateChangedListener(new CustomHorizontalScrollView.ScrollViewListener() {

                @Override
                public void onScrollChanged(ScrollType scrollType, int scrollX) {
                    if (scrollType == ScrollType.IDLE) {
                        Log.i(PARAMETER_TAG, "scrollX:" + scrollX + " adapter:" + mCurrentAdapter);
                        CategoryUtil.getCategoryPosArray().put(mCurrentAdapter, scrollX);
                    }
                }
            });
        }
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003170 end
        View panelView = main.findViewById(R.id.listItems);
        if (panelView instanceof CategoryTrack) {
            CategoryTrack panel = (CategoryTrack) panelView;
            if (mAdapter != null) {
                mAdapter.setOrientation(CategoryView.HORIZONTAL);
                panel.setAdapter(mAdapter);
                mAdapter.setContainer(panel);
            }
        } else if (mAdapter != null) {
            ListView panel = (ListView) main.findViewById(R.id.listItems);
            panel.setAdapter(mAdapter);
            mAdapter.setContainer(panel);
        }

        mAddButton = (IconView) main.findViewById(R.id.addButton);
        if (mAddButton != null) {
            mAddButton.setOnClickListener(this);
            updateAddButtonVisibility();
        }
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003170 begin
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                int scrollPos = CategoryUtil.getCategoryPosArray().get(mCurrentAdapter);
                Log.i(PARAMETER_TAG, "adapter:" + mCurrentAdapter + " scrollPos:" + scrollPos);
                if (mScrollview != null) {
                    mScrollview.scrollBy(scrollPos, 0);
                }
            }
        });
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-12,PR1003170 end
        return main;
    }

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-05,Defect:1533434 begin
    private Runnable mVersionRunnable = new Runnable() {

        @Override
        public void run() {
            FilterShowActivity activity = (FilterShowActivity) getActivity();
            if (activity != null) {
                activity.addCurrentVersion();
            } else {
                Log.e(FRAGMENT_TAG, "mVersionRunnable activity is NULL.");
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addButton:
                mHandler.removeCallbacks(mVersionRunnable);
                mHandler.postDelayed(mVersionRunnable, 200);
                break;
        }
    }
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-05,Defect:1533434 end

    public void updateAddButtonVisibility() {
        if (mAddButton == null) {
            return;
        }
        FilterShowActivity activity = (FilterShowActivity) getActivity();
        if (activity.isShowingImageStatePanel() && mAdapter.showAddButton()) {
            mAddButton.setVisibility(View.VISIBLE);
            if (mAdapter != null) {
                mAddButton.setText(mAdapter.getAddButtonText());
            }
        } else {
            mAddButton.setVisibility(View.GONE);
        }
    }
}

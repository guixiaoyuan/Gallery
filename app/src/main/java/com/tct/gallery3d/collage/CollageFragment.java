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

package com.tct.gallery3d.collage;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.fragment.GalleryFragment;
import com.tct.gallery3d.collage.collageadapter.CollageAdapter;
import com.tct.gallery3d.collage.collagemanager.CollageInterface;
import com.tct.gallery3d.collage.collagemanager.CollageManager;
import com.tct.gallery3d.collage.puzzle.PuzzleView;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.MediaObject;

import java.util.List;

/**
 * CollageFragment for creating collage
 */
public class CollageFragment extends GalleryFragment implements CollageAdapter.OnItemClickListener,View.OnClickListener {

    public static final String TAG = "CollageFragment";

    private AbstractGalleryActivity mContext;
    ;
    private ActionBar mActionBar;

    private Menu mMenu = null;

    private CollageInterface collageManager;

    private CollageAdapter mAdapter;

    private List<String> mPhotoPaths;

    private PuzzleView mPuzzleView;

    private PuzzleListView mPuzzleListView;

    private int pieceSize = 0;

    private TextView mAddInBorder;

    private TextView mAddOutBorder;

    private TextView mReduceInBorder;

    private TextView mReduceOutBorder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.activity_collage_process, container, false);
        mPuzzleView = (PuzzleView) v.findViewById(R.id.puzzle_view);

        mPuzzleListView = (PuzzleListView) v.findViewById(R.id.puzzle_list);

        mAdapter = new CollageAdapter(getActivity());
        mPuzzleListView.setAdapter(mAdapter);

        mAddInBorder = (TextView)v.findViewById(R.id.add_inBorder);
        mAddOutBorder = (TextView)v.findViewById(R.id.add_outBorder);
        mReduceInBorder = (TextView)v.findViewById(R.id.reduce_inBorder);
        mReduceOutBorder = (TextView)v.findViewById(R.id.reduce_outBorder);

        mAddInBorder.setOnClickListener(this);
        mAddOutBorder.setOnClickListener(this);
        mReduceInBorder.setOnClickListener(this);
        mReduceOutBorder.setOnClickListener(this);

        mContext = (AbstractGalleryActivity) getGalleryContext();
        mContext.initSystemBar(false);

        collageManager = new CollageManager(mContext,mPuzzleView);

        pieceSize = mContext.getIntent().getIntExtra("piece_size", 0);
        mPhotoPaths = mContext.getIntent().getStringArrayListExtra("photo_path");

        collageManager.setPieceAndPath(pieceSize,mPhotoPaths);

        /*mAdapter.setOnItemClickListener(this);
        mAdapter.setNeedDrawBorder(true);
        mAdapter.setNeedDrawOuterBorder(true);
        mAdapter.refreshData(collageManager.getTemplatesByCount(pieceSize));*/
        mAdapter.setmPieceSize(pieceSize);
        mAdapter.setOnItemClickListener(this);
        mAdapter.refreshData(collageManager.getTemplatesByCount(pieceSize));
        collageManager.setPuzzleLayout();
        collageManager.loadSelectPhotos();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initActionBar();
    }
    @Override
    protected void onActionResult(int requestCode, int resultCode, Intent data) {
        if(null != data.getStringExtra("path")){
            DataManager manager = mContext.getDataManager();
            MediaObject mo=manager.getMediaObject(data.getStringExtra("path"));
            collageManager.swapImage(mo.getFilePath());
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.collage, menu);

        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        mMenu = menu;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                mContext.onBackPressed();
                return true;
            }
            case R.id.action_save:
                collageManager.saveFile();
                return true;
            default:
                return false;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (null != mMenu) {
            mMenu.close();
        }
    }

    private void initActionBar() {
        setHasOptionsMenu(true);
        mActionBar = mContext.getActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        mActionBar.setTitle(R.string.collage);
    }

    @Override
    public void onItemClick(int themeId) {
        collageManager.changePuzzleLayout(themeId);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.add_inBorder:
                collageManager.addInsideBorderWidth();
                break;
            case R.id.add_outBorder:
                collageManager.addOutSideBorderWidth();
                break;
            case R.id.reduce_outBorder:
                collageManager.reduceOutSideBorderWidth();
                break;
            case R.id.reduce_inBorder:
                collageManager.reduceInSideBorderWidth();
                break;
        }
    }
}

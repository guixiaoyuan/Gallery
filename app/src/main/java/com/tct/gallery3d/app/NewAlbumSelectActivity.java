package com.tct.gallery3d.app;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toolbar;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.fragment.NewAlbumFragment;
import com.tct.gallery3d.data.DataManager;

public class NewAlbumSelectActivity extends AbstractGalleryActivity {
    private static final String TAG = "AlbumSelectActivity";
    public static final String TARGET_PATH = "targetpath";
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle data) {
        super.onCreate(data);
        setContentView(R.layout.new_album_select);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setToolbar(mToolbar);
        setActionBar(mToolbar);
        setMargin(mToolbar, false);
        initSystemBar(false);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mContent = new NewAlbumFragment();
        Bundle localdata = getIntent().getExtras();
        String path = getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_MOMENTS);
        localdata.putString(GalleryConstant.KEY_MEDIA_PATH, path);
        mContent.setArguments(localdata);
        transaction.add(R.id.new_album_select_container, mContent, NewAlbumFragment.TAG);
        transaction.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setMargin(mToolbar, false);
        resetToolBarPosition();
        resetStatusBarIfAtBottom();
        initSystemBar(false);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        resetStatusBarIfAtBottom();
        super.onWindowFocusChanged(hasFocus);
    }
}


package com.tct.gallery3d.app;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.fragment.AlbumSelectFragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toolbar;

public class AlbumSelectActivity extends AbstractGalleryActivity {
    private static final String TAG = "AlbumSelectActivity";
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle data) {
        super.onCreate(data);
        setContentView(R.layout.album_select);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setToolbar(mToolbar);
        setActionBar(mToolbar);
        setMargin(mToolbar, false);
        initSystemBar(false);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mContent = new AlbumSelectFragment();
        Bundle localBundle = getIntent().getExtras();
        if (localBundle == null) {
            return;
        }
        mContent.setArguments(localBundle);
        transaction.add(R.id.album_select_container, mContent, AlbumSelectFragment.TAG);
        transaction.commit();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetToolBarPosition();
        initSystemBar(false);
        resetStatusBarIfAtBottom();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        resetStatusBarIfAtBottom();
        super.onWindowFocusChanged(hasFocus);
    }
}


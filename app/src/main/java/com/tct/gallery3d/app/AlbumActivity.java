package com.tct.gallery3d.app;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.fragment.AlbumFragment;
import com.tct.gallery3d.util.ScreenUtils;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toolbar;

public class AlbumActivity extends AbstractGalleryActivity {

    public static final String TAG = AlbumActivity.class.getSimpleName();
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle data) {
        super.onCreate(data);
        setContentView(R.layout.album_page);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(mToolbar);
        setToolbar(mToolbar);
        setMargin(mToolbar, false);
        initSystemBar(false);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mContent = new AlbumFragment();
        Bundle localBundle = getIntent().getExtras();
        if (localBundle == null) {
            return;
        }
        mContent.setArguments(localBundle);
        transaction.add(R.id.album_page_container, mContent, AlbumFragment.TAG);
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        resetStatusBarIfAtBottom();
        if (null != mContent && (mContent instanceof AlbumFragment)) {
            ((AlbumFragment) mContent).initSystemUI();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetToolBarPosition();
        initSystemBar(false);
        resetStatusBarIfAtBottom();
    }

    @Override
    public void hideToolBarView() {
        if (mToolbar != null) {
            mToolbar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void showToolBarView() {
        if (mToolbar != null) {
            mToolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left_short, R.anim.slide_out_left_short);
    }
}

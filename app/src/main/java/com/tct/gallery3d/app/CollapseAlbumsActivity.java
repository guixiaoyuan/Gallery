package com.tct.gallery3d.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toolbar;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.fragment.AlbumFragment;
import com.tct.gallery3d.app.fragment.CollapseAlbumFragment;

public class CollapseAlbumsActivity extends AbstractGalleryActivity {

    public static final String TAG = "CollapseAlbumsActivity";
    private Toolbar mToolbar;
    private boolean mGetContent;

    @Override
    protected void onCreate(Bundle data) {
        android.util.Log.e(TAG,"onCreate");
        super.onCreate(data);
        setContentView(R.layout.album_page);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(mToolbar);
        setToolbar(mToolbar);
        setMargin(mToolbar, false);
        initSystemBar(false);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mContent = new CollapseAlbumFragment();
        Bundle localBundle = getIntent().getExtras();
        if (localBundle == null) {
            return;
        }
        mGetContent = localBundle.getBoolean(GalleryActivity.KEY_GET_CONTENT, false);
        mContent.setArguments(localBundle);

        transaction.add(R.id.album_page_container, mContent, CollapseAlbumFragment.TAG);
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mGetContent && data != null) {
            setResult(resultCode, data);
            finish();
        }
    }
}

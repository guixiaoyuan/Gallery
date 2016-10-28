package com.tct.gallery3d.app;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.fragment.CollapseManageFragment;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class CollapseAlbumsManageActivity extends AbstractGalleryActivity {

    public static final String TAG = "CollapseAlbumsManageActivity";
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle data) {
        super.onCreate(data);
        initSystemUI();
        setContentView(R.layout.collapse_album_manage_page);
        initToolBar();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mContent = new CollapseManageFragment();
        transaction.add(R.id.album_page_container, mContent, CollapseManageFragment.TAG);
        transaction.commit();
    }

    public void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void initSystemUI() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int[] attribute = new int[]{android.R.attr.colorPrimary};
        TypedArray array = this.obtainStyledAttributes(typedValue.resourceId, attribute);
        int color = array.getColor(0, Color.TRANSPARENT);
        array.recycle();

        window.setStatusBarColor(color);
    }
}

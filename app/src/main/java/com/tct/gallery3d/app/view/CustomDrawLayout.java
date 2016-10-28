package com.tct.gallery3d.app.view;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.View;

public class CustomDrawLayout extends DrawerLayout {

    private boolean canPopShow = true;

    public CustomDrawLayout(Context context) {
        super(context);
    }

    public CustomDrawLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void closeDrawer(View v) {
        if (!canPopShow) {
            return;
        }
        super.closeDrawer(v);
    }

    @Override
    public void openDrawer(View v) {
        if (!canPopShow) {
            return;
        }
        super.openDrawer(v);
    }

    public void setCanPopShow(boolean canPopShow) {
        this.canPopShow = canPopShow;
    }

    public void setCanDragShow(boolean canDragShow) {
        if (canDragShow) {
            this.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            this.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }
}
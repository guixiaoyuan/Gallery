package com.tct.gallery3d.app;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

import com.tct.gallery3d.R;

public class DividerPhotoItemDecoration extends RecyclerView.ItemDecoration {

    private int mSpacing = 0;

    public DividerPhotoItemDecoration(Context context) {
        if (context != null) {
            mSpacing = context.getResources().getDimensionPixelOffset(R.dimen.photo_item_spacing);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        // Set the offset to the default spacing(1dp).
        int offset = mSpacing;

        // Init the default spacing.
        int left = offset / 2;
        int right = offset / 2;
        int top = offset / 2;
        int bottom = offset / 2;
        RecyclerView.LayoutManager manager = parent.getLayoutManager();

        // Check the layout manager.
        if (manager instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) manager;
            GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();
            int spanIndex = params.getSpanIndex();
            int spanSize = params.getSpanSize();
            int spanCount = layoutManager.getSpanCount();

            // Whether the view is the first view in the line, the left set 0.
            if (spanIndex == 0) {
                left = 0;
            }
            // Whether the view is the last view in the line, the right set 0.
            if (spanSize + spanIndex == spanCount) {
                right = 0;
            }
        }

        // Set the spacing to the Rect.
        outRect.left = left;
        outRect.right = right;
        outRect.top = top;
        outRect.bottom = bottom;
    }
}
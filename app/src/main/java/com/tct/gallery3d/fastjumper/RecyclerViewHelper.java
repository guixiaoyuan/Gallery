package com.tct.gallery3d.fastjumper;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;

public class RecyclerViewHelper {
    private final static String TAG = RecyclerViewHelper.class.getSimpleName();
    private static Field sInsetsDirtyField;

    static {
        try {
            sInsetsDirtyField = RecyclerView.LayoutParams.class.getDeclaredField("mInsetsDirty");
            sInsetsDirtyField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            sInsetsDirtyField = null;
        }
    }

    /**
     * Returns whether the child insets dirty or not.
     * Usually, this happened after ItemDecorations invalidated and before layout yet.
     *
     * @param child The child view of RecyclerView
     * @return True if child view insets dirty
     */
    public static boolean isChildInsetsDirty(View child) {
        if (child == null) {
            throw new IllegalArgumentException("child cannot be null");
        }
        if (!(child.getLayoutParams() instanceof RecyclerView.LayoutParams)) {
            throw new IllegalArgumentException("view:" + child + " should be child of RecyclerView");
        }
        boolean insetsDirty = false;
        if (sInsetsDirtyField != null) {
            try {
                RecyclerView.LayoutParams lp = ((RecyclerView.LayoutParams) child.getLayoutParams());
                insetsDirty = sInsetsDirtyField.getBoolean(lp);
            } catch (Exception e) {
                insetsDirty = false;
            }
        }
        return insetsDirty;
    }

    public static int findFirstVisibleItemPosition(RecyclerView rv) {
        int firstPosition;
        RecyclerView.LayoutManager lm = rv.getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
            LinearLayoutManager llm = (LinearLayoutManager) lm;
            firstPosition = llm.findFirstVisibleItemPosition();
        } else if (lm instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager sglm = (StaggeredGridLayoutManager) lm;
            int[] firstPositions = sglm.findFirstVisibleItemPositions(null);
            firstPosition = firstPositions[0];
        } else {
            throw new IllegalStateException("Cannot find first visible item position using layout manager: " + lm.toString());
        }
        return firstPosition;
    }

    public static void scrollToPositionWithOffset(RecyclerView rv, int position, int offset) {
        if (FastJumper.DEBUG) {
            Log.d(TAG, "scrollToPositionWithOffset = " + position);
        }
        RecyclerView.LayoutManager lm = rv.getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
            ((LinearLayoutManager) lm).scrollToPositionWithOffset(position, offset);
        } else if (lm instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) lm).scrollToPositionWithOffset(position, offset);
        } else {
            throw new IllegalStateException("Cannot scroll to position using layout manager: " + lm.toString());
        }
    }

    public static void fastSmoothScrollToPosition(final RecyclerView rv, final int position) {
        if (FastJumper.DEBUG) {
            Log.d(TAG, "fastSmoothScrollToPosition");
        }
        int childCount = rv.getChildCount();
        if (childCount == 0) {
            return;
        }
        int scrollByCount = childCount * 3;
        int firstPosition = rv.getChildLayoutPosition(rv.getChildAt(0));
        if (Math.abs(firstPosition - position) < scrollByCount) {
            rv.smoothScrollToPosition(position);
        } else {
            int positionToJump = position - scrollByCount * (position > firstPosition ? 1 : -1);
            rv.scrollToPosition(positionToJump);
            ViewCompat.postOnAnimation(rv, new Runnable() {
                @Override
                public void run() {
                    rv.smoothScrollToPosition(position);
                }
            });
        }
    }

    public static boolean isReverseLayout(RecyclerView rv) {
        RecyclerView.LayoutManager lm = rv.getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) lm).getReverseLayout();
        } else if (lm instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) lm).getReverseLayout();
        }
        return false;
    }

    public static int getFixedItemHeight(RecyclerView rv, int position) {
        RecyclerView.LayoutManager lm = rv.getLayoutManager();
        View child = lm.findViewByPosition(position);
        if (child == null) {
            return -1;
        }
        boolean insetsDirty = RecyclerViewHelper.isChildInsetsDirty(child);
        if (insetsDirty) {
            return -1;
        }
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        return lm.getDecoratedMeasuredHeight(child) + params.topMargin + params.bottomMargin;
    }
}

/*
 * Copyright (C) 2016 sin3hz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tct.gallery3d.fastjumper;


import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.adapter.MomentsAdapter;

/**
 * A {} implementation which provides
 * precise, smooth scroll.
 * This callback require item size and span info to calculate scroll offset precisely.
 */


public abstract class SpannableCallback extends SimpleCallback {

    public static final String TAG = SpannableCallback.class.getSimpleName();

    public static final int INVALID_OFFSET = -1;

    private int mScrollRange = INVALID_OFFSET;
    private int mScrollOffset = INVALID_OFFSET;
    private boolean mScrollRangeInvalid;
    private boolean mScrollOffsetInvalid;

    private boolean mReverseLayout;
    private boolean mScrollUp = false;

    private ScrollCalculator mScrollCalculator;

    public SpannableCallback() {
    }

    public void setScrollCalculator(ScrollCalculator calculator) {
        if (calculator == null) {
            throw new IllegalArgumentException(ScrollCalculator.class.getSimpleName() + " can not be null");
        }
        mScrollCalculator = calculator;
        mScrollCalculator.invalidateCache();
        if (getRecyclerView() != null) {
            invalidate();
        }
    }


    public ScrollCalculator getScrollCalculator() {
        return mScrollCalculator;
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (getState() == FastJumper.STATE_DRAGGING) {
                return;
            }
            updateScrollOffsetIfNeed();
            if (mScrollOffset == INVALID_OFFSET) {
                return;
            }
            dy = mReverseLayout ? -dy : dy;
            mScrollOffset += dy;
            if (dy > 0) {
                mScrollUp = true;
            } else {
                mScrollUp = false;
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (getState() == FastJumper.STATE_DRAGGING) {
                return;
            }
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                mScrollOffsetInvalid = true;
            }
            //TODO
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                ((AbstractGalleryActivity) recyclerView.getContext()).toolbarDisplay();
            }
            MomentsAdapter adapter = (MomentsAdapter) recyclerView.getAdapter();
            if (adapter.getFirstPosition() <= 1 && !mScrollUp) {
                ((AbstractGalleryActivity) recyclerView.getContext()).resetToolBarPosition();
            }
            updateScrollRangeIfNeed();
            updateScrollOffsetIfNeed();
        }
    };

    @Override
    protected void attachedToRecyclerView(RecyclerView rv) {
        super.attachedToRecyclerView(rv);
        rv.addOnScrollListener(mOnScrollListener);
        mScrollRange = INVALID_OFFSET;
        mScrollOffset = INVALID_OFFSET;
        if (mScrollCalculator == null) {
            throw new IllegalArgumentException(ScrollCalculator.class.getSimpleName() + " can not be null");
        }
        invalidate();
    }

    @Override
    protected void detachedFromRecyclerView(RecyclerView rv) {
        super.detachedFromRecyclerView(rv);
        rv.removeOnScrollListener(mOnScrollListener);
    }

    @Override
    public boolean isEnabled() {
        return getScrollRange() > getContentHeight() * 3;
    }

    @Override
    public int getScrollRange() {
        updateScrollRangeIfNeed();
        if (mScrollRange == INVALID_OFFSET) {
            return 0;
        }
        return mScrollRange;
    }

    @Override
    public int getScrollOffset() {
        updateScrollOffsetIfNeed();
        if (mScrollOffset == INVALID_OFFSET) {
            return 0;
        }
        return mScrollOffset;
    }

    private void updateScrollOffsetIfNeed() {
        if (!mScrollOffsetInvalid) {
            return;
        }
        updateScrollOffset();
    }

    private void updateScrollRangeIfNeed() {
        if (!mScrollRangeInvalid) {
            return;
        }
        updateScrollRange();
    }

    private void updateScrollOffset() {
        int offset = computeScrollOffset();
        if (offset != INVALID_OFFSET) {
            mScrollOffset = offset;
            mScrollOffsetInvalid = false;
        }
    }

    private void updateScrollRange() {
        int range = computeScrollRange();
        if (range != INVALID_OFFSET) {
            mScrollRange = range;
            mScrollRangeInvalid = false;
        }
    }

    public int computeScrollRange() {
        if (getRecyclerView().hasPendingAdapterUpdates()) {
            return INVALID_OFFSET;
        }
        int itemCount = getAdapterItemCount();
        int offset = mScrollCalculator.getScrollOffsetByPosition(itemCount);
        return Math.max(0, offset - getContentHeight());
    }

    public int computeScrollOffset() {
        if (getRecyclerView().hasPendingAdapterUpdates()) {
            return INVALID_OFFSET;
        }
        int offset = INVALID_OFFSET;
        int firstPosition = findFirstVisibleItemPosition();
        RecyclerView.LayoutManager lm = getLayoutManager();
        View child = lm.findViewByPosition(firstPosition);

        if (child != null) {
            offset = mScrollCalculator.getScrollOffsetByPosition(firstPosition);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            if (mReverseLayout) {
                offset = offset + ((lm.getDecoratedBottom(child) + params.bottomMargin) - getRecyclerView().getHeight())
                        + getRecyclerView().getPaddingBottom();
            } else {
                offset = offset - (lm.getDecoratedTop(child) - params.topMargin)
                        + getRecyclerView().getPaddingTop();
            }
        }
        if (FastJumper.DEBUG) {
            Log.d(TAG, "computeScrollOffset = " + offset + " -- firstPosition = " + firstPosition);
        }
        return offset;
    }

    @Override
    public boolean isReverseLayout() {
        return RecyclerViewHelper.isReverseLayout(getRecyclerView());
    }

    public void invalidate() {
        mScrollOffsetInvalid = true;
        mScrollRangeInvalid = true;
        mReverseLayout = isReverseLayout();
    }

    @Override
    public void onInvalidated() {
        invalidate();
    }

    @Override
    public void onAdapterDataChanged() {
        invalidate();
        mScrollCalculator.invalidateCache();
    }

    private void setScrollOffset(int offset) {
        mScrollOffset = offset;
    }

    @Override
    public int scrollTo(float progress) {
        if (FastJumper.DEBUG) {
            Log.d(TAG, "scrollTo = " + progress);
        }
        int lastScrollOffset = getScrollOffset();
        int scrollOffset = (int) (getScrollRange() * progress);
        int offset = scrollOffset - lastScrollOffset;
        int position = mScrollCalculator.getPositionByScrollOffset(scrollOffset);
        position = mScrollCalculator.findFirstSpanPositionBefore(position);
        if (progress == 0f) {
            getRecyclerView().scrollToPosition(0);
        } else if (progress == 1f && offset <= 0) {
            getRecyclerView().scrollToPosition(getAdapterItemCount() - 1);
        } else if (Math.abs(offset) < getContentHeight()) {
            offset = mReverseLayout ? -offset : offset;
            getRecyclerView().scrollBy(0, offset);
        } else {
            int offsetFromTop = scrollOffset - mScrollCalculator.getScrollOffsetByPosition(position);
            scrollToPositionWithOffset(position, -offsetFromTop);
        }
        setScrollOffset(scrollOffset);
        return position;
    }

    /**
     * Scroll to the specified adapter position with the given offset.
     *
     * @see LinearLayoutManager#scrollToPositionWithOffset(int, int)
     * @see StaggeredGridLayoutManager#scrollToPositionWithOffset(int, int)
     */
    public void scrollToPositionWithOffset(int position, int offset) {
        RecyclerViewHelper.scrollToPositionWithOffset(getRecyclerView(), position, offset);
    }

    /**
     * @return Returns the adapter position of the first visible view.
     * @see LinearLayoutManager#findFirstVisibleItemPosition()
     * @see StaggeredGridLayoutManager#findFirstVisibleItemPositions(int[])
     */
    public int findFirstVisibleItemPosition() {
        return RecyclerViewHelper.findFirstVisibleItemPosition(getRecyclerView());
    }

    @Override
    protected int getPosition(float progress) {
        int scrollOffset = (int) (progress * getScrollRange());
        return mScrollCalculator.getPositionByScrollOffset(scrollOffset);
    }

    /**
     * Base interface of spannable layout
     */
    public interface SpanLookup {
        /**
         * Returns height of item view at <code>position</code>
         * <p>
         * Note, the height should including its decoration and margin.
         *
         * @param position Adapter position
         * @return Height of item view
         * @see RecyclerView.ItemDecoration#getItemOffsets(Rect, View, RecyclerView, RecyclerView.State)
         * @see RecyclerView.LayoutManager#getDecoratedMeasuredHeight(View)
         */
        int getItemHeight(int position);

        /**
         * Returns number of columns in the layout.
         */
        int getSpanCount();

        /**
         * Returns the number of span occupied by the item at <code>position</code>.
         */
        int getSpanSize(int position);

        /**
         * Returns the final span index of the provided position.
         */
        int getSpanIndex(int position);

        /**
         * Returns the position of first column of the row which given position belongs to.
         * <p>
         * Example:
         * [1 2 3]
         * [4 5 6]
         * findFirstSpanPositionBefore(2) = 1
         * findFirstSpanPositionBefore(1) = 1
         * findFirstSpanPositionBefore(6) = 4
         * </p>
         */
        int findFirstSpanPositionBefore(int position);

        /**
         * Returns the number of items in the adapter.
         */
        int getItemCount();
    }

    /**
     * A <code>ScrollCalculator</code> is responsible for calculating scroll offset.
     * By changing the <code>ScrollCalculator</code>, a <code>SpannableCallback</code> can
     * be adapted to different layout.
     */
    public static abstract class ScrollCalculator implements SpanLookup {

        private RecyclerView mRecyclerView;

        public ScrollCalculator(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
        }

        public ScrollCalculator() {
        }

        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }

        @Override
        public int getItemCount() {
            if (getRecyclerView() == null) {
                throw new IllegalArgumentException("getItemCount should be override");
            }
            return getRecyclerView().getAdapter().getItemCount();
        }

        @Override
        public int getSpanCount() {
            if (getRecyclerView() == null) {
                throw new IllegalArgumentException("getSpanCount should be override");
            }
            return SpanLookupHelper.getSpanCount(getRecyclerView().getLayoutManager());
        }

        @Override
        public int getSpanSize(int position) {
            if (getRecyclerView() == null) {
                throw new IllegalArgumentException("getSpanSize should be override");
            }
            return SpanLookupHelper.getSpanSize(getRecyclerView().getLayoutManager(), position);
        }

        @Override
        public int getSpanIndex(int position) {
            if (getRecyclerView() == null) {
                throw new IllegalArgumentException("getSpanIndex should be override");
            }
            return SpanLookupHelper.getSpanIndex(getRecyclerView().getLayoutManager(), position);
        }

        @Override
        public int findFirstSpanPositionBefore(int position) {
            while (getSpanIndex(position) > 0 && position > 0) {
                position--;
            }
            return position;
        }

        /**
         * Invalidate <Code>ScrollCalculator</Code> to re-calculate scroll offset.
         * <p>
         * <Code>SpannableCallback</Code> automatically calls this method when
         * adapter changes occur.
         * <p>
         * You should manually call this method, if the return value of following method changed:
         * {@link #getSpanCount()}
         * {@link #getSpanSize(int)}
         * {@link #getItemHeight(int)}
         */
        public abstract void invalidateCache();

        /**
         * Returns adapter position <code>offset</code> maps to.
         *
         * @param offset Scroll offset
         * @return Adapter position
         */
        public abstract int getPositionByScrollOffset(int offset);

        /**
         * Returns scroll offset at the adapter position.
         *
         * @param position Adapter position
         * @return Scroll offset at given position
         */
        public abstract int getScrollOffsetByPosition(int position);
    }
}
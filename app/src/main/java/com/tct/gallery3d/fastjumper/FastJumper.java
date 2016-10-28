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

import java.util.ArrayList;
import java.util.List;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;

/**
 * A Fast Scroller for RecyclerView
 */
public class FastJumper {

    public static final boolean DEBUG = false;
    public static final String TAG = FastJumper.class.getSimpleName();
    /**
     * Scroll thumb not showing.
     */
    public static final int STATE_GONE = 0;
    /**
     * Scroll thumb visible.
     */
    public static final int STATE_VISIBLE = 1;
    /**
     * Scroll thumb hiding.
     */
    public static final int STATE_HIDING = 2;
    /**
     * Scroll thumb being dragged by user.
     */
    public static final int STATE_DRAGGING = 3;

    private List<Listener> mListeners;
    private Callback mCallback;
    private RecyclerView mRecyclerView;
    private FastJumperDecoration mFastJumperDecoration;
    private FastJumperTouchListener mFastJumperTouchListener;
    private FastJumperOnScrollListener mFastJumperOnScrollListener;
    private FastJumperAdapterObserver mFastJumperAdapterObserver;

    private class FastJumperAdapterObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            onAdapterDataChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
           // onAdapterDataChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            //onAdapterDataChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            //onAdapterDataChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
           // onAdapterDataChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            //onAdapterDataChanged();
        }
    }

    /**
     * Creates an {@link FastJumper} that will work with given Callback.
     *
     * @param callback The callback which controls the scroll behavior
     */
    public FastJumper(Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback can not be null");
        }
        if (callback.mFastJumper != null) {
            throw new IllegalArgumentException("Callback " + callback +
                    " is already used with a FastJumper: " + callback.mFastJumper);
        }
        mCallback = callback;
        mCallback.setFastJumper(this);
    }

    private void checkAttach() {
        if (mRecyclerView == null) {
            throw new IllegalStateException("Can not call before attach to RecyclerView");
        }
    }

    private void checkRv() {
        if (mRecyclerView.getAdapter() == null) {
            throw new IllegalArgumentException("Adapter should set before attach to RecyclerView");
        }
        if (mRecyclerView.getLayoutManager() == null) {
            throw new IllegalArgumentException("LayoutManager should set before attach to RecyclerView");
        }
        if (!mRecyclerView.getLayoutManager().canScrollVertically()) {
            throw new IllegalArgumentException("Only support vertical layout");
        }
    }

    /**
     * Returns current state of scroller.
     *
     * @return Current state of scroller
     */
    public int getState() {
        checkAttach();
        return mFastJumperDecoration.getState();
    }

    /**
     * Returns progress of scroller.
     *
     * @return Progress of scroller
     */
    public float getProgress() {
        checkAttach();
        return mFastJumperDecoration.getProgress();
    }

    /**
     * Hide scroller by animation.
     */
    public void hide() {
        checkAttach();
        mFastJumperDecoration.hide();
    }

    /**
     * Show scroller by animation
     */
    public void show() {
        checkAttach();
        mFastJumperDecoration.show();
    }

    /**
     * Invalidate the scroller to refresh its scrollOffset and scrollRange.
     * <p/>
     * The scroller's {@link Callback} will have its
     * {@link Callback#onInvalidated()} method called.
     */
    public void invalidate() {
        checkAttach();
        mRecyclerView.removeCallbacks(mInvalidateRunnable);
        ViewCompat.postOnAnimation(mRecyclerView, mInvalidateRunnable);
    }

    private void onAdapterDataChanged() {
        checkAttach();
        mRecyclerView.removeCallbacks(mOnAdapterDataChangedRunnable);
        ViewCompat.postOnAnimation(mRecyclerView, mOnAdapterDataChangedRunnable);
    }

    private Runnable mOnAdapterDataChangedRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRecyclerView.hasPendingAdapterUpdates()) {
                ViewCompat.postOnAnimation(mRecyclerView, this);
            } else {
                mCallback.onAdapterDataChanged();
                mFastJumperDecoration.invalidate(false);
            }
        }
    };

    private Runnable mInvalidateRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRecyclerView.hasPendingAdapterUpdates()) {
                ViewCompat.postOnAnimation(mRecyclerView, this);
            }else {
                mCallback.onInvalidated();
                mFastJumperDecoration.invalidate(true);
            }
        }
    };

    /**
     * Returns the RecyclerView attached.
     *
     * @return The RecyclerView attached or null
     */
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * Set a {@link Listener}
     *
     * @param listener Listener to set
     */
    public void addListener(Listener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
    }

    /**
     * Remove a {@link Listener}
     *
     * @param listener Listener to clear
     */
    public void removeListener(Listener listener) {
        if (mListeners != null) {
            mListeners.remove(listener);
        }
    }

    public void clearListeners() {
        if (mListeners != null) {
            mListeners.clear();
        }
    }

    /**
     * Attaches the {@link FastJumper} to the provided RecyclerView. If it is already
     * attached to a RecyclerView, it will first detach from the previous one. You can call this
     * method with {@code null} to detach it from the current RecyclerView.
     * <p>
     * Upon attaching, it will add an itemDecoration, an onItemTouchListener,
     * an onScrollListener and an AdapterDataObserver to the RecyclerView.
     * </p>
     *
     * @param recyclerView The RecyclerView instance to which you want to add this helper or
     *                     {@code null} if you want to remove {@link FastJumper} from the current
     *                     RecyclerView.
     */
    public void attachToRecyclerView(RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return;
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            checkRv();
            setupCallbacks();
        }
    }

    private void destroyCallbacks() {
        mRecyclerView.removeCallbacks(mInvalidateRunnable);
        mRecyclerView.removeCallbacks(mOnAdapterDataChangedRunnable);
        mFastJumperDecoration.destroyCallbacks();
        mRecyclerView.getAdapter().unregisterAdapterDataObserver(mFastJumperAdapterObserver);
        mRecyclerView.removeItemDecoration(mFastJumperDecoration);
        mRecyclerView.removeOnItemTouchListener(mFastJumperTouchListener);
        mRecyclerView.removeOnScrollListener(mFastJumperOnScrollListener);
        mCallback.setRecyclerView(null);
    }

    private void setupCallbacks() {
        mFastJumperDecoration = new FastJumperDecoration(this);
        mFastJumperTouchListener = new FastJumperTouchListener(mFastJumperDecoration);
        mFastJumperOnScrollListener = new FastJumperOnScrollListener(mFastJumperDecoration);
        mFastJumperAdapterObserver = new FastJumperAdapterObserver();
        mRecyclerView.addItemDecoration(mFastJumperDecoration);
        mRecyclerView.addOnItemTouchListener(mFastJumperTouchListener);
        mRecyclerView.addOnScrollListener(mFastJumperOnScrollListener);
        mRecyclerView.getAdapter().registerAdapterDataObserver(mFastJumperAdapterObserver);
        mCallback.setRecyclerView(mRecyclerView);
    }

    Callback getCallback() {
        return mCallback;
    }

    void dispatchOnScroll(float progress) {
        if (mListeners != null) {
            for (int i = 0, size = mListeners.size(); i < size; i++) {
                mListeners.get(i).onScrolled(progress);
            }
        }
    }

    void dispatchOnStateChange(int state) {
        if (mListeners != null) {
            for (int i = 0, size = mListeners.size(); i < size; i++) {
                mListeners.get(i).onStateChange(state);
            }
        }
    }

    public static abstract class Callback {

        private FastJumper mFastJumper;
        private RecyclerView mRecyclerView;

        void setFastJumper(FastJumper jumper) {
            mFastJumper = jumper;
        }

        void setRecyclerView(RecyclerView rv) {
            if (mRecyclerView != null) {
                detachedFromRecyclerView(mRecyclerView);
            }
            mRecyclerView = rv;
            if (mRecyclerView != null) {
                attachedToRecyclerView(mRecyclerView);
            }
        }

        /**
         * Called when scroller attached to a RecyclerView
         *
         * @param rv The RecyclerView to attach.
         */
        protected void attachedToRecyclerView(RecyclerView rv) {
        }

        /**
         * Called when scroller detached to a RecyclerView
         *
         * @param rv The RecyclerView to detach.
         */
        protected void detachedFromRecyclerView(RecyclerView rv) {
        }

        /**
         * Returns the RecyclerView attached to this callback.
         *
         * @return The currently attached RecyclerView
         */
        protected final RecyclerView getRecyclerView() {
            return mRecyclerView;
        }

        /**
         * Returns current state of scroller.
         */
        protected final int getState() {
            return mFastJumper.getState();
        }

        /**
         * Returns current progress of scroller.
         */
        protected final float getProgress() {
            return mFastJumper.getProgress();
        }

        /**
         * Called when scroller state changes.
         */
        protected void onStateChanged(int state) {
        }

        /**
         * A convenience method return content height of RecyclerView.
         *
         * @return Content height of RecyclerView
         */
        protected final int getContentHeight() {
            return getRecyclerView().getHeight() - getRecyclerView().getPaddingTop() - getRecyclerView().getPaddingBottom();
        }

        /**
         * A convenience method return content width of RecyclerView.
         *
         * @return Content width of RecyclerView
         */
        protected final int getContentWidth() {
            return getRecyclerView().getWidth() - getRecyclerView().getPaddingLeft() -
                    getRecyclerView().getPaddingRight();
        }

        /**
         * A convenience method return layout of RecyclerView.
         *
         * @return LayoutManager of RecyclerView
         */
        protected final RecyclerView.LayoutManager getLayoutManager() {
            return getRecyclerView().getLayoutManager();
        }

        /**
         * A convenience method return item count of RecyclerView's adapter.
         *
         * @return Item count of adapter
         */
        protected final int getAdapterItemCount() {
            return getRecyclerView().getAdapter().getItemCount();
        }

        /**
         * The range RecyclerView can scroll.
         */
        public abstract int getScrollRange();

        /**
         * Returns offset RecyclerView scrolled.
         */
        public abstract int getScrollOffset();

        /**
         * Return true if has section.
         */
        public boolean isSectionEnable() {
            return false;
        }

        /**
         * Returns the section of given progress.
         *
         * @see #isSectionEnable()
         */
        public String getSection(float progress) {
            return null;
        }

        /**
         * Scroll the RecyclerView to position at progress.
         *
         * @param progress Range in [0, 1]
         * @return The adapter position scroll to
         */
        public abstract int scrollTo(float progress);

        /**
         * Returns whether {@link FastJumper} should show a scroller when user scroll.
         *
         * @return True if a scroller should be show.
         */
        public boolean isEnabled() {
            return getScrollRange() > getContentHeight();
        }

        /**
         * Returns if views are laid out from the opposite direction of the layout.
         *
         * @return If layout is reversed or not.
         * @see RecyclerView.LayoutManager.Properties#reverseLayout
         */
        public boolean isReverseLayout() {
            return RecyclerViewHelper.isReverseLayout(getRecyclerView());
        }

        /**
         * Called when scrollOffset and scrollRange should be recalculate.
         */
        public void onInvalidated() {
        }

        /**
         * Called when adapter data changed.
         */
        public void onAdapterDataChanged() {

        }
    }

    /**
     * Listener for event about scroller.
     */
    public static abstract class Listener {

        /**
         * Called when scroller position changes.
         *
         * @param progress the new progress of scroller within its range, from 0 to 1
         */
        public void onScrolled(float progress) {
        }

        /**
         * Called when scroller state changes.
         * The state will be one of {@link #STATE_GONE}, {@link #STATE_VISIBLE}, {@link #STATE_DRAGGING}
         * or {@link #STATE_HIDING}.
         *
         * @param state new scroller state
         */
        public void onStateChange(int state) {
        }
    }
}

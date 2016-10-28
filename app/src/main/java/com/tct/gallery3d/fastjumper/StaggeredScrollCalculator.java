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

import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;

import java.util.Arrays;

/**
 * A simple  implementation
 * for {@link android.support.v7.widget.StaggeredGridLayoutManager}.
 */
public abstract class StaggeredScrollCalculator extends SpannableCallback.ScrollCalculator {

    public StaggeredScrollCalculator(RecyclerView recyclerView) {
        super(recyclerView);
    }

    public StaggeredScrollCalculator() {
    }

    private SparseIntArray mGroupHeightCache;
    private int[] mSpanSize;

    @Override
    public void invalidateCache() {
        invalidateCacheInternal();
    }

    private void invalidateCacheInternal() {
        initializeCache();
    }

    public void initializeCache() {
        int itemCount = getItemCount();
        int spanCount = getSpanCount();
        mGroupHeightCache = new SparseIntArray(itemCount / spanCount);
        if (mSpanSize == null || mSpanSize.length != spanCount) {
            mSpanSize = new int[spanCount];
        }
        Arrays.fill(mSpanSize, 0, mSpanSize.length, 0);
        int totalHeight = 0;
        for (int i = 0; i <= itemCount; i++) {
            if (i == itemCount || getSpanSize(i) == spanCount) {
                int maxSpanIndex = findMax(mSpanSize);
                int gap = mSpanSize[maxSpanIndex] - mSpanSize[0];
                if (gap != 0) {
                    totalHeight += gap;
                }
                if (i != itemCount) {
                    mGroupHeightCache.put(i, totalHeight);
                    totalHeight += getItemHeight(i);
                    Arrays.fill(mSpanSize, 0, mSpanSize.length, 0);
                } else {
                    mGroupHeightCache.put(i, totalHeight);
                }
            } else {
                int minSpanIndex = findMin(mSpanSize);
                int height = getItemHeight(i);
                mSpanSize[minSpanIndex] += height;
                if (minSpanIndex == 0) {
                    mGroupHeightCache.put(i, totalHeight);
                    totalHeight += height;
                }
            }
        }
    }

    private void checkCache() {
        if (mGroupHeightCache == null) {
            initializeCache();
        }
    }

    @Override
    public int getPositionByScrollOffset(int offset) {
        checkCache();
        int position = 0;
        int key = findOffsetKeyFromCache(offset);
        if (key != -1) {
            position = key;
        }
        return position;
    }

    @Override
    public int getScrollOffsetByPosition(int position) {
        checkCache();
        int offset = 0;
        int key = findPositionKeyFromCache(position);
        if (key != -1) {
            offset = mGroupHeightCache.get(key);
        }
        return offset;
    }

    @Override
    public int findFirstSpanPositionBefore(int position) {
        return position;
    }

    private int findPositionKeyFromCache(int position) {
        int lo = 0;
        int hi = mGroupHeightCache.size() - 1;

        while (lo < hi) {
            final int mid = (lo + hi + 1) >>> 1;
            final int midVal = mGroupHeightCache.keyAt(mid);
            if (midVal <= position) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        int key = mGroupHeightCache.keyAt(lo);
        if (key <= position) {
            return key;
        }
        return -1;
    }

    private int findOffsetKeyFromCache(int offset) {
        int lo = 0;
        int hi = mGroupHeightCache.size() - 1;

        while (lo < hi) {
            final int mid = (lo + hi + 1) >>> 1;
            final int midVal = mGroupHeightCache.valueAt(mid);
            if (midVal <= offset) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        int key = mGroupHeightCache.keyAt(lo);
        int val = mGroupHeightCache.valueAt(lo);
        if (val <= offset) {
            return key;
        }
        return -1;
    }

    private static int findMax(int[] array) {
        int index = 0;
        for (int i = 1, length = array.length; i < length; i++) {
            if (array[i] > array[index]) {
                index = i;
            }
        }
        return index;
    }

    private static int findMin(int[] array) {
        int index = 0;
        for (int i = 1, length = array.length; i < length; i++) {
            if (array[i] < array[index]) {
                index = i;
            }
        }
        return index;
    }
}

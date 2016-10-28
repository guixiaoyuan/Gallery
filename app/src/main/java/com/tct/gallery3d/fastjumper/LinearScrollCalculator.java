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

/**
 * A implementation
 * for {@link android.support.v7.widget.LinearLayoutManager} and {@link android.support.v7.widget.GridLayoutManager}.
 */
public abstract class LinearScrollCalculator extends SpannableCallback.ScrollCalculator {

    private SparseIntArray mGroupHeightCache = new SparseIntArray();

    public LinearScrollCalculator(RecyclerView recyclerView) {
        super(recyclerView);
    }

    public LinearScrollCalculator() {
    }

    @Override
    public void invalidateCache() {
        invalidateCacheInternal();
    }

    private void invalidateCacheInternal() {
        mGroupHeightCache.clear();
    }

    @Override
    public int getPositionByScrollOffset(int offset) {
        if (offset <= 0) {
            return 0;
        }
        int position = 0;
        int itemCount = getItemCount();

        int startPos = 0;
        if (mGroupHeightCache.size() > 0) {
            int pos = findOffsetKeyFromCache(offset);
            if (pos != -1) {
                offset -= mGroupHeightCache.get(pos);
                startPos = pos;
                position = pos;
            }
        }

        if (offset == 0) {
            return position;
        }

        int spanCount = getSpanCount();
        int span = 0;
        int groupHeight = 0;
        for (int i = startPos; i < itemCount; i++) {
            int size = getSpanSize(i);
            span += size;
            if (span > spanCount) {
                if (offset - groupHeight < 0) {
                    span -= size;
                    break;
                }
                offset -= groupHeight;
                span = size;
                groupHeight = 0;
                position = i;
            }
            groupHeight = Math.max(groupHeight, getItemHeight(i));
            if (span == spanCount) {
                if (offset - groupHeight < 0) {
                    break;
                }
                offset -= groupHeight;
                span = 0;
                groupHeight = 0;
                position = i + 1;
            }
        }
        if (offset > 0) {
            for (int i = position; offset >= 0 && i < itemCount; i++) {
                position = i;
                int size = getSpanSize(i);
                float fraction = size / (float) span;
                offset -= groupHeight * fraction;
            }
        }
        return position;
    }

    @Override
    public int getScrollOffsetByPosition(int position) {
        if (position <= 0) {
            return 0;
        }
        int offset = 0;
        int itemCount = getItemCount();

        int spanCount = getSpanCount();
        int span = 0;
        int groupHeight = 0;
        int positionSpanSize = getSpanSize(position);
        int startPos = 0;

        if (mGroupHeightCache.size() > 0) {
            int pos = findPositionKeyFromCache(position);
            if (pos != -1) {
                startPos = pos;
                offset = mGroupHeightCache.get(pos);
            }
        }

        if (startPos == position) {
            return offset;
        }

        for (int i = startPos; i < position; i++) {
            int size = getSpanSize(i);
            span += size;
            if (span > spanCount) {
                offset += groupHeight;
                span = size;
                groupHeight = 0;
                mGroupHeightCache.put(i, offset);
            }
            groupHeight = Math.max(groupHeight, getItemHeight(i));
            if (span == spanCount) {
                offset += groupHeight;
                span = 0;
                groupHeight = 0;
                mGroupHeightCache.put(i + 1, offset);
            }
        }
        if (span + positionSpanSize > spanCount) {
            offset += groupHeight;
            mGroupHeightCache.put(position, offset);
        } else if (span != 0) {
            int spans = span;
            for (int i = position; i < itemCount; i++) {
                int size = getSpanSize(i);
                if (spans + size > spanCount) {
                    break;
                }
                groupHeight = Math.max(groupHeight, getItemHeight(i));
                spans += getSpanSize(i);
            }
            float fraction = span / (float) spans;
            offset += groupHeight * fraction;
        }
        return offset;
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
}

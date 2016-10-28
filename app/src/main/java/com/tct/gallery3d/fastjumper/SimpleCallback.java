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


/**
 * A simple implementation for
 * <p/>
 * This callback use RecyclerView's built-in function to estimate scroll offset,
 * you can use this callback with most LayoutManager.
 * But due to not knowing the exact size of item view, if your item view has different size,
 * the scroller may juddering while scrolling.
 */
public class SimpleCallback extends FastJumper.Callback {

    public SimpleCallback() {
    }

    @Override
    public boolean isEnabled() {
        return getScrollRange() > getContentHeight() * 3;
    }

    @Override
    public String getSection(float progress) {
        int position = getPosition(progress);
        int itemCount = getAdapterItemCount();
        position = Math.max(0, Math.min(position, itemCount - 1));
        return getSection(position);
    }

    @Override
    public int scrollTo(float progress) {
        int position = getPosition(progress);
        float scrollRange = getScrollRange();
        float scrollOffset = progress * scrollRange;
        float lastProgress = getProgress();
        float lastScrollOffset = lastProgress * scrollRange;
        int offset = (int) (scrollOffset - lastScrollOffset);
        if (Math.abs(offset) < getContentHeight()) {
            getRecyclerView().scrollBy(0, offset);
        } else {
            getRecyclerView().scrollToPosition(position);
        }
        return position;
    }

    @Override
    public int getScrollRange() {
        return getRecyclerView().computeVerticalScrollRange() - getRecyclerView().computeVerticalScrollExtent();
    }

    @Override
    public int getScrollOffset() {
        return getRecyclerView().computeVerticalScrollOffset();
    }

    /**
     * Returns section of <code>position</code>
     *
     * @param position Adapter position
     * @return Section of given position
     */
    public String getSection(int position) {
        return null;
    }

    /**
     * Returns adapter position at progress.
     */
    protected int getPosition(float progress) {
        int itemCount = getRecyclerView().getAdapter().getItemCount();
        return (int) (progress * (itemCount - 1));
    }
}

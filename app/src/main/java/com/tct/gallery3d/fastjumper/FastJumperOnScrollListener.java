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

import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.Log;

class FastJumperOnScrollListener extends RecyclerView.OnScrollListener {

    private FastJumperDecoration mFastJumperDecoration;

    public FastJumperOnScrollListener(FastJumperDecoration decoration) {
        mFastJumperDecoration = decoration;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        //TODO
        AbstractGalleryActivity mContext = (AbstractGalleryActivity) recyclerView.getContext();
        if (mFastJumperDecoration.getState() == FastJumper.STATE_DRAGGING) {
            if (FastJumper.DEBUG) {
                Log.d("mFastJumperDecoration.getState()", "mFastJumperDecoration.getState()");
            }
            mContext.hideToolBarPosition();
            ((GalleryActivity) mContext).hideTabsView();
        } else {
            mContext.onScrollPositionChanged(dy);
        }

        mFastJumperDecoration.onScrolled(dx, dy);
    }
}

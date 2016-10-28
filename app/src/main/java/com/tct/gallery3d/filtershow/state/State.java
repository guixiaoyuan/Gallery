/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* ----------|----------------------|----------------------|----------------- */
/* 04/03/2015| jian.pan1            | PR964225             |[Android5.0][Gallery_v5.1.9.1.0109.0]The effect value isn't update when changing frame
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.filtershow.state;

import com.tct.gallery3d.filtershow.filters.FilterColorBorderRepresentation;
import com.tct.gallery3d.filtershow.filters.FilterFxRepresentation;
import com.tct.gallery3d.filtershow.filters.FilterImageBorderRepresentation;
import com.tct.gallery3d.filtershow.filters.FilterRepresentation;

public class State {
    private String mText;
    private int mType;
    private FilterRepresentation mFilterRepresentation;

    public State(State state) {
        this(state.getText(), state.getType());
    }

    public State(String text) {
       this(text, StateView.DEFAULT);
    }

    public State(String text, int type) {
        mText = text;
        mType = type;
    }

    public boolean equals(State state) {
        if (mFilterRepresentation.getFilterClass()
                != state.mFilterRepresentation.getFilterClass()) {
            return false;
        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-03,PR964225 begin
        if (mFilterRepresentation instanceof FilterFxRepresentation
                || mFilterRepresentation instanceof FilterImageBorderRepresentation
                || mFilterRepresentation instanceof FilterColorBorderRepresentation) {
            return mFilterRepresentation
                    .equals(state.getFilterRepresentation());
        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-03,PR964225 end
        return true;
    }

    public boolean isDraggable() {
        return mFilterRepresentation != null;
    }

    String getText() {
        return mText;
    }

    void setText(String text) {
        mText = text;
    }

    int getType() {
        return mType;
    }

    void setType(int type) {
        mType = type;
    }

    public FilterRepresentation getFilterRepresentation() {
        return mFilterRepresentation;
    }

    public void setFilterRepresentation(FilterRepresentation filterRepresentation) {
        mFilterRepresentation = filterRepresentation;
    }
}

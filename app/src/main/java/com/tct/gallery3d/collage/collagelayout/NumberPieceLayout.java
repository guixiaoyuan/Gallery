package com.tct.gallery3d.collage.collagelayout;

import android.util.Log;

import com.tct.gallery3d.collage.puzzle.PuzzleLayout;

/**
 * Created by liuxiaoyu on 16-10-10.
 */
public abstract class NumberPieceLayout extends PuzzleLayout {
    public NumberPieceLayout(int theme) {
        if (theme >= getThemeCount()) {
            Log.e(TAG, "NumberPieceLayout: the most theme count is "
                    + getThemeCount() + " ,you should let theme from 0 to "
                    + (getThemeCount() - 1) + " .");
        }
        mTheme = theme;
    }

    public abstract int getThemeCount();

}

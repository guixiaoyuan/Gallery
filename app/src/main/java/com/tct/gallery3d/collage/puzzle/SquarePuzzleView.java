package com.tct.gallery3d.collage.puzzle;

import android.content.Context;
import android.util.AttributeSet;

/**
 * the square puzzle view
 * Created by liuxiaoyu on 16-10-10.
 */
public class SquarePuzzleView extends PuzzleView {

    public SquarePuzzleView(Context context) {
        super(context);
    }

    public SquarePuzzleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquarePuzzleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int length = width > height ? height : width;

        setMeasuredDimension(length, length);
    }
}

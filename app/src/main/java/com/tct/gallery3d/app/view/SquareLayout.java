package com.tct.gallery3d.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class SquareLayout extends RelativeLayout {

    private boolean mSquare = true;

    public void setSquare(boolean square) {
        mSquare = square;
    }

    public SquareLayout(Context context) {
        this(context, null);
    }

    public SquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mSquare) {
            setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
            int childWidthSize = getMeasuredWidth();
            heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

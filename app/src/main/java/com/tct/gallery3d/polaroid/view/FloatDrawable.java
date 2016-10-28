package com.tct.gallery3d.polaroid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class FloatDrawable extends Drawable {

    private Paint mLinePaint = new Paint();
    {
        mLinePaint.setARGB(200, 50, 50, 50);
        mLinePaint.setStrokeWidth(1F);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.WHITE);
    }

    public FloatDrawable(Context context) {
        super();
    }

    @Override
    public void draw(Canvas canvas) {

        int left = getBounds().left;
        int top = getBounds().top;
        int right = getBounds().right;
        int bottom = getBounds().bottom;

        Rect mRect = new Rect(left, top, right, bottom);
        canvas.drawRect(mRect, mLinePaint);
    }

    @Override
    public void setBounds(Rect bounds) {
        super.setBounds(new Rect(bounds.left, bounds.top, bounds.right, bounds.bottom));
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return 0;
    }

}

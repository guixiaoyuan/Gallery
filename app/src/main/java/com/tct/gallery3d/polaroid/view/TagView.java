/* ----------|----------------------|----------------------|----------------- */
/* 06/30/2015| chengbin.du          | PR1026689            |[Android 5.1][Gallery_Polaroid_v5.1.13.1.0209.0]The cursor change to front after tapping on tag
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.polaroid.view;

import com.tct.gallery3d.app.Log;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TagView extends View {
    private static final String TAG = "TagView";

    private Paint mTextPaint = null;
    private String mTextString = null;


    public TagView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public TagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public TagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    public TagView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // TODO Auto-generated constructor stub
    }

    public void setTextPaint(Paint textPaint) {
        this.mTextPaint = textPaint;
    }

    public void setTextString(String textString) {
        this.mTextString = textString;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mTextPaint != null && mTextString != null) {
            float baseline = mTextPaint.getFontMetrics().ascent;
            Log.d(TAG, "top=" + baseline);
            canvas.drawText(mTextString, 0, -baseline, mTextPaint);
        }
    }
}

/* ----------|----------------------|----------------------|----------------- */
/* 07/16/2015| chengbin.du          | PR1026690            |[Android5.1][Gallery_v5.1.13.1.0209.0_polaroid]Slogan change to other place when switch frame
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.polaroid.view;

import com.tct.gallery3d.ui.Log;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class SloganText extends EditText {
	private static final String TAG = "SloganText";

    public SloganText(Context context) {
        super(context);
    }

    public SloganText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SloganText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SloganText(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.setMeasuredDimension(this.getRight() - this.getLeft(), getMeasuredHeight());
        Log.e(TAG,"widthSize=" + this.getMeasuredWidth() + " heightSize=" + this.getMeasuredHeight());
    }
}

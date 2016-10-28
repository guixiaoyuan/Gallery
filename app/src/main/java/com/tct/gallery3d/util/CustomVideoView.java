/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.gallery3d.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {
    private int mVideoWidth;
    private int mVideoHeight;

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setVideoHeight(int mVideoHeight) {
        this.mVideoHeight = mVideoHeight;
    }

    public void setVideoWidth(int mVideoWidth) {
        this.mVideoWidth = mVideoWidth;
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /*
         * The following code is to make videoView view length-width based on
         * the parameters you set to decide.
         */
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}

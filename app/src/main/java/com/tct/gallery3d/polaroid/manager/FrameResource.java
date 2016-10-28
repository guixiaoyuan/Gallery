package com.tct.gallery3d.polaroid.manager;

import android.graphics.Point;
import android.graphics.Rect;

public class FrameResource {
    public int mBgResId;
    public int mFgResId;
    public Point mTargetResolution;
    public Rect mPictureLocation;
    public SloganMargin mSloganMargin;
    public float mSloganFontSize;
    public float mSloganBorderSize;
    public float mTagFontSize;

    public FrameResource(int bgResId, int fgResId, Point targetResolution, Rect pictureLocation, SloganMargin sloganMargin,
            float sloganFontSize, float sloganBorderSize, float tagFontSize) {
        mBgResId = bgResId;
        mFgResId = fgResId;
        mTargetResolution = targetResolution;
        mPictureLocation = pictureLocation;
        mSloganMargin = sloganMargin;
        mSloganFontSize = sloganFontSize;
        mSloganBorderSize = sloganBorderSize;
        mTagFontSize = tagFontSize;
    }

    @Override
    public String toString() {
        return "FrameResource { target: " + mTargetResolution.x + "x" + mTargetResolution.y + "}";
    }

    public static class SloganMargin {
        public int left = 0;
        public int right = 0;
        public int top = 0;
        public int bottom = 0;
    };
}

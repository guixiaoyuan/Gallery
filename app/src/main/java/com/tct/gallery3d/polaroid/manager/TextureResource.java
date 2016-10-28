package com.tct.gallery3d.polaroid.manager;

public class TextureResource {
    int mTargetWidth, mTargetHeight;
    int mResId;

    public TextureResource(int resId, int targetWidth, int targetHeight) {
        mResId = resId;
        mTargetWidth = targetWidth;
        mTargetHeight = targetHeight;
    }
}

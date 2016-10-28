package com.tct.gallery3d.polaroid.imageshow;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

public class PreviewParams {
    public int mImageSeq = -1;
    private BitmapDrawable mInDrawable;
    private String mFilterName;
    public Rect mInCrop;
    public Point mOutResolution;

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;
        if (!PreviewParams.class.isInstance(object))
            return false;
        PreviewParams other = (PreviewParams) object;
        if (mImageSeq != other.mImageSeq)
            return false;
        if (mFilterName == null && other.mFilterName != null)
            return false;
        if (mFilterName != null && !mFilterName.equals(other.mFilterName))
            return false;
        if (mInCrop == null && other.mInCrop != null)
            return false;
        if (mInCrop != null && !mInCrop.equals(other.mInCrop))
            return false;
        if (mOutResolution == null && other.mOutResolution != null)
            return false;
        if (mOutResolution != null && !mOutResolution.equals(other.mOutResolution))
            return false;
        if (mInDrawable != other.mInDrawable)
            return false;
        return true;
    }

    public PreviewParams(int imageSeq, BitmapDrawable inDrawable, String filterName, Rect inCrop,
            Point outResolution) {
        mImageSeq = imageSeq;
        mInDrawable = inDrawable;
        mFilterName = filterName;
        mInCrop = inCrop;
        mOutResolution = outResolution;
    }
}

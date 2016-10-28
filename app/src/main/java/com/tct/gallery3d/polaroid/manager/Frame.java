package com.tct.gallery3d.polaroid.manager;

import java.util.ArrayList;

import android.graphics.Color;
import android.util.Log;

import com.tct.gallery3d.polaroid.Poladroid;

public class Frame {
    public String mName;
    public String mFilterName;
    public Font mFont;
    public int mSloganFontColor = Color.TRANSPARENT;
    public int mSloganBorderColor = Color.TRANSPARENT;
    ArrayList<FrameResource> mResources = new ArrayList<FrameResource>();

    public Frame(String name, String filterName, Font font, int sloganFontColor,
            int sloganBorderColor, ArrayList<FrameResource> frameResources) {
        mName = name;
        mFilterName = filterName;
        mFont = font;
        mSloganFontColor = sloganFontColor;
        mSloganBorderColor = sloganBorderColor;

        if (frameResources == null) {
            Log.i(Poladroid.TAG, "*** new Frame(" + mName + "): frameResources is null");
        } else {
            Log.d(Poladroid.TAG, "new Frame(" + mName + ", frameResources size: " + frameResources.size()
                    + ")");
            mResources = frameResources;
        }
    }

    public String getName() {
        return mName;
    }

    public Filter getPreferredFilter() {
        if (mFilterName != null) {
            return FilterManager.getFilter(mFilterName);
        }
        return null;
    }

    public String getPreferredFilterName() {
        return mFilterName;
    }

    public FrameResource pickBestResourceForTarget(int targetWidth, int targetHeight) {
        FrameResource bestResource = null;
        float bestRatio = -1f; // Will be initialized by the first texture

        Log.d(Poladroid.TAG, "Frame.pickBestResourceForTarget(target: " + targetWidth + "x" + targetHeight
                + "){");

        if (targetWidth <= 0)
            targetWidth = 1;
        if (targetHeight <= 0)
            targetHeight = 1;

        for (FrameResource resource : mResources) {
            Log.d(Poladroid.TAG, "Checking " + resource);
            float widthRatio = 1f * resource.mTargetResolution.x / targetWidth;
            float heightRatio = 1f * resource.mTargetResolution.y / targetHeight;
            Log.d(Poladroid.TAG, "  Ratios: " + widthRatio + " and " + heightRatio);
            if (widthRatio < 1) {
                // Enlarging is worse than shrinking => make it look bad
                widthRatio = (float) (1f / widthRatio / Math.sqrt(widthRatio));
            }
            if (heightRatio < 1) {
                // Enlarging is worse than shrinking => make it look bad
                heightRatio = (float) (1f / heightRatio / Math.sqrt(heightRatio));
            }
            float ratio = Math.max(widthRatio, heightRatio);
            Log.d(Poladroid.TAG, "  Larger than 1 ratios: " + widthRatio + " and " + heightRatio
                    + ", overall ratio: " + ratio);

            if (bestResource == null || ratio < bestRatio) {
                bestResource = resource;
                bestRatio = ratio;
                Log.d(Poladroid.TAG, "  => Looks better...");
            }
        }

        Log.d(Poladroid.TAG, "} Frame.pickBestResourceForTarget() => chosen resource: " + bestResource);
        return bestResource;
    }

    public FrameResource pickBestResourceForPicture(int pictureWidth, int pictureHeight) {
        FrameResource bestResource = null;
        float bestRatio = -1f; // Will be initialized by the first texture

        Log.d(Poladroid.TAG, "Frame.pickBestResourceForPicture(target: " + pictureWidth + "x"
                + pictureHeight + "){");

        if (pictureWidth <= 0)
            pictureWidth = 1;
        if (pictureHeight <= 0)
            pictureHeight = 1;

        for (FrameResource resource : mResources) {
            Log.d(Poladroid.TAG, "Checking " + resource);
            float widthRatio = 1f * resource.mTargetResolution.x / pictureWidth;
            float heightRatio = 1f * resource.mTargetResolution.y / pictureHeight;
            Log.d(Poladroid.TAG, "  Ratios: " + widthRatio + " and " + heightRatio);
            if (widthRatio < 1) {
                // Enlarging is worse than shrinking => make it look bad
                widthRatio = (float) (1f / widthRatio / Math.sqrt(widthRatio));
            }
            if (heightRatio < 1) {
                // Enlarging is worse than shrinking => make it look bad
                heightRatio = (float) (1f / heightRatio / Math.sqrt(heightRatio));
            }
            float ratio = Math.max(widthRatio, heightRatio);
            Log.d(Poladroid.TAG, "  Larger than 1 ratios: " + widthRatio + " and " + heightRatio
                    + ", overall ratio: " + ratio);

            if (bestResource == null || ratio < bestRatio) {
                bestResource = resource;
                bestRatio = ratio;
                Log.d(Poladroid.TAG, "  => Looks better...");
            }
        }

        Log.d(Poladroid.TAG, "} Frame.pickBestResourceForPicture() => chosen resource: " + bestResource);
        return bestResource;
    }

    @Override
    public String toString() {
        return "Frame { " + mName + ", resource count: " + mResources.size() + "}";
    }
}

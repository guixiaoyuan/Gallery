package com.tct.gallery3d.polaroid.config;

import java.util.Comparator;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import com.tct.gallery3d.polaroid.manager.Filter;
import com.tct.gallery3d.polaroid.manager.FilterOperation.Quality;

// This is a command going from the Foreground thread to the Background thread
// It is first received by the background handler (BackgroundHandler)
// then processed in background
// then sent back to the foreground thread (ForegroundHandler) together with the processing result

public class FilterConfig {
    public static final int ALL_PRIORITIES = -1;

    // Input
    public BitmapDrawable mInDrawable;
    public Filter mFilter;
    public int mImagePrio;
    public int mSizePrio;
    public int mSequencePrio;
    public Rect mInCrop;
    public Point mOutResolution;
    public Quality mQuality;
    public FilterCompletionHandler mFilterCompletionHandler;

    // Output
    public BitmapDrawable mOutDrawable;
    public long mRenderingDuration;

    public interface FilterCompletionHandler {
        void onFilterComplete(FilterConfig filterConfig);
    }

    public FilterConfig(int imagePrio, int sizePrio, int cmdPrio) {
        // This simplistic constructor is provided for the
        // Poladroid.UI2BG_REMOVE_FILTER_CMD
        mImagePrio = imagePrio;
        mSizePrio = sizePrio;
        mSequencePrio = cmdPrio;
    }

    public FilterConfig(BitmapDrawable inDrawable, Rect inCrop, Point outResolution,
            Quality quality, Filter filter, int imagePrio, int sizePrio, int cmdPrio,
            FilterCompletionHandler filterCompletionHandler) {
        // This full-feature constructor is provided for the
        // Poladroid.UI2BG_ADD_FILTER_CMD
        mInDrawable = inDrawable;
        mInCrop = inCrop;
        mOutResolution = outResolution;
        mQuality = quality;
        mFilter = filter;
        mImagePrio = imagePrio;
        mSizePrio = sizePrio;
        mSequencePrio = cmdPrio;
        mFilterCompletionHandler = filterCompletionHandler;
    }

    // The comparison method is provided in order to sort the processing queue
    // according to the 3 defined priorities
    public static final Comparator<FilterConfig> mComparator = new Comparator<FilterConfig>() {
        @Override
        public int compare(FilterConfig lhs, FilterConfig rhs) {
            if (lhs.mImagePrio != rhs.mImagePrio)
                return rhs.mImagePrio - lhs.mImagePrio;
            if (lhs.mSizePrio != rhs.mSizePrio)
                return rhs.mSizePrio - lhs.mSizePrio;
            if (lhs.mSequencePrio != rhs.mSequencePrio)
                return lhs.mSequencePrio - rhs.mSequencePrio;
            return 0;
        }
    };

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (!FilterConfig.class.isInstance(other))
            return false;
        FilterConfig filterCfg = (FilterConfig) other;

        if (mFilter == null && filterCfg.mFilter != null)
            return false;
        if (!mFilter.getName().equals(filterCfg.mFilter.getName()))
            return false;
        if (mInDrawable != filterCfg.mInDrawable)
            return false;
        if (mImagePrio != filterCfg.mImagePrio)
            return false;
        if (mSizePrio != filterCfg.mSizePrio)
            return false;
        if (mSequencePrio != filterCfg.mSequencePrio)
            return false;
        if (mInCrop == null && filterCfg.mInCrop != null)
            return false;
        if (!mInCrop.equals(filterCfg.mInCrop))
            return false;
        if (mOutResolution == null && filterCfg.mOutResolution != null)
            return false;
        if (!mOutResolution.equals(filterCfg.mOutResolution))
            return false;
        if (mQuality != filterCfg.mQuality)
            return false;
        if (mFilterCompletionHandler != filterCfg.mFilterCompletionHandler)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 123456 * mImagePrio + 789 * mSizePrio + mSequencePrio;
    }

    @Override
    public String toString() {
        return "FilterConfig { " + ((mFilter != null) ? mFilter.getName() : "no-filter")
                + ", priority: " + mImagePrio + "/" + mSizePrio + "/" + mSequencePrio + " }";
    }
}

/* EOF */
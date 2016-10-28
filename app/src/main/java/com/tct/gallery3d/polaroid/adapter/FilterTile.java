package com.tct.gallery3d.polaroid.adapter;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.tct.gallery3d.polaroid.Poladroid;
import com.tct.gallery3d.polaroid.config.FilterConfig;
import com.tct.gallery3d.polaroid.config.FilterConfig.FilterCompletionHandler;
import com.tct.gallery3d.polaroid.manager.Filter;
import com.tct.gallery3d.polaroid.manager.FilterOperation.Quality;

public class FilterTile implements FilterCompletionHandler {
    // private final int SIZE_PRIORITY = THUMB_SIZE_PRIORITY;
    final Quality QUALITY = Quality.LOWEST;
    protected View mView;
    protected Drawable mThumb;
    BitmapDrawable mInDrawable;
    Filter mFilter;
    boolean mHasRealThumb = false;
    boolean mGettingThumb = false;
    int mImagePrio;
    int mSequencePrio;
    Rect mInCrop;
    Point mOutResolution;
    Handler mBackgroundHandler;
    ImageView mImageView;

    public FilterTile(BitmapDrawable inDrawable, Rect inCrop, Point outResolution, Filter filter,
            int imagePrio, int sequencePrio, Handler backgroundHandler) {
        int color = 0xFF404040; // TODO: get color from resources... 0xFF000000
                                // | ((int) (Math.random() * 0x00FFFFFF));
        mThumb = new ColorDrawable(color);
        mInDrawable = inDrawable;
        mFilter = filter;
        mImagePrio = imagePrio;
        mSequencePrio = sequencePrio;
        mInCrop = inCrop;
        mOutResolution = outResolution;
        mBackgroundHandler = backgroundHandler;

        Log.d(Poladroid.TAG, "new " + toString());
    }

    public void getThumb(ImageView imageView) {
        if (!mHasRealThumb && !mGettingThumb) {
            mGettingThumb = true;

            Log.d(Poladroid.TAG, "ResourceFilterTile.getThumb(" + imageView
                    + "): need to build thumb in background, for " + toString());
            mImageView = imageView;

            Message msg = Message.obtain();
            msg.what = Poladroid.UI2BG_ADD_FILTER_CMD;

            FilterConfig filterConfig = new FilterConfig(mInDrawable, mInCrop, mOutResolution,
                    QUALITY, mFilter, mImagePrio, Poladroid.THUMB_SIZE_PRIORITY, mSequencePrio, this);
            msg.obj = filterConfig;
            mBackgroundHandler.sendMessage(msg);
        } else {
            copyThumb(imageView);
        }
    }

    private void copyThumb(ImageView imageView) {
        if (imageView == null) {
            Log.i(Poladroid.TAG, "*** Tile.getThumb(): imageView is null");
        } else {
            mThumb.setDither(true);
            imageView.setImageDrawable(mThumb);
        }
    }

    public void setView(View v) {
        mView = v;
    }

    @Override
    public String toString() {
        return "FilterTile { " + mInDrawable + ", " + mInCrop + ", " + mOutResolution + ", "
                + mFilter + ", prio: " + mImagePrio + "/" + Poladroid.THUMB_SIZE_PRIORITY + "/"
                + mSequencePrio + ", mHasRealThumb: " + mHasRealThumb + ", mGettingThumb: "
                + mGettingThumb + " }";
    }

    @Override
    public void onFilterComplete(FilterConfig filterConfig) {
        if (filterConfig == null) {
            Log.i(Poladroid.TAG, "*** FilterTile.onFilterComplete(" + this + "): filterConfig is null");
            return;
        }

        Log.i(Poladroid.TAG, "FilterTile.onFilterComplete(" + this + ", " + filterConfig + ")");

        mHasRealThumb = true;
        if (filterConfig.mOutDrawable != null) {
            mThumb = filterConfig.mOutDrawable;
        }

        copyThumb(mImageView);
    }
}

/* EOF */

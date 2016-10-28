package com.tct.gallery3d.app.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AbstractGalleryFragment;
import com.tct.gallery3d.app.view.ImageSource;
import com.tct.gallery3d.app.view.PhotoDetailView;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.image.ImageResizer;
import com.tct.gallery3d.image.ImageWorker;
import com.tct.gallery3d.image.ImageWorker.OnImageLoadedListener;
import com.tct.gallery3d.image.RecyclingBitmapDrawable;

public abstract class GalleryFragment extends AbstractGalleryFragment {

    private AbstractGalleryActivity mContext;
    private ImageWorker mWorker;
    protected RelativeLayout mNoContentView;

    // The state to show the photos.
    public enum State {
        DAY, MONTH, STAGGERED
    }

    public State mState = State.DAY;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mContext = (AbstractGalleryActivity) getActivity();
        mWorker = (mContext).getImageWorker();
        showLoadingImage(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected void pauseWork() {
        if (mWorker != null) {
            mWorker.pauseWork();
        }
        Glide.with(mContext).pauseRequests();
    }

    protected void resumeWork() {
        if (mWorker != null) {
            mWorker.resumeWork();
        }
        Glide.with(mContext).resumeRequests();
    }

    public void loadThumbnail(Object data, ImageView imageView) {
        loadThumbnail(data, imageView, null);
    }

    public void loadThumbnail(Object data, ImageView imageView, OnImageLoadedListener listener) {
        if (mWorker != null) {
            mWorker.loadThumbnail(data, imageView, listener);
        }
    }

    public void loadLarge(MediaItem item, PhotoDetailView detailView) {
        if (mWorker != null) {
            mWorker.loadLarge(item, detailView);
        }
    }

    public void loadGlide(final MediaItem item, final ImageView imageView) {
        if (mWorker != null) {
            mWorker.loadGlide(mContext, item, imageView);
        }
    }

    public Fragment getFragment(String tag) {
        return ((AbstractGalleryActivity) getGalleryContext()).getSupportFragmentManager().findFragmentByTag(tag);
    }

    public void showLoadingImage(boolean show) {
        if (show) {
            mWorker.setLoadingImage(R.drawable.empty_photo);
        } else {
            mWorker.setLoadingImage(null);
        }
    }

    public void showEmptyView(boolean show) {
        if (mNoContentView != null) {
            if (show) {
                mNoContentView.setVisibility(View.VISIBLE);
            } else {
                mNoContentView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    protected void onActionResult(int requestCode, int resultCode, Intent data) {
    }

    // Add the interface for notify the UI.
    public void onLoadFinished() {

    }
}

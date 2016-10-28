/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tct.gallery3d.image;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tct.gallery3d.R;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.view.ImageSource;
import com.tct.gallery3d.app.view.PhotoDetailView;
import com.tct.gallery3d.common.BitmapUtils;
import com.tct.gallery3d.data.ImageRequest;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.drm.DrmManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

/**
 * This class wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 */
public abstract class ImageWorker {
    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 200;

    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    private Bitmap mLoadingBitmap;
    //    private boolean mFadeInBitmap = true;
    private boolean mFadeInBitmap = false;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    protected Resources mResources;

    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;

    protected int mPreviewSize;
    protected int mThumbnailSize;
    private Executor mExecutor;

    protected ImageWorker(Context context) {
        mResources = context.getResources();
        mImageCache = ImageCache.getInstance(context);
        initCache();
        mPreviewSize = mResources.getDimensionPixelSize(R.dimen.size_preview);
        mThumbnailSize = mResources.getDimensionPixelSize(R.dimen.size_thumbnail);
        mExecutor = ((GalleryApp)context.getApplicationContext()).getThreadPool().getExecutor();
    }

    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and
     * disk cache will be used if an {@link ImageCache} has been added using
     * {@link ImageWorker#addImageCache(android.support.v4.app.FragmentManager, ImageCache.ImageCacheParams)}. If the
     * image is found in the memory cache, it is set immediately, otherwise an {@link AsyncTask}
     * will be created to asynchronously load the bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     * @param listener A listener that will be called back once the image has been loaded.
     */
    private void loadImage(MediaObject data, ImageView imageView, OnImageLoadedListener listener) {
        if (data == null) {
            return;
        }
        if (imageView != null) {
            Object tag = imageView.getTag(R.id.image_view_tag);
            if (tag != null) {
                if (String.valueOf(tag).equals(String.valueOf(data))) {
                    if (listener != null) {
                        listener.onImageLoaded(true);
                    }
                    return;
                }
            }
        }

        Bitmap bitmap = null;
        BitmapDrawable value = null;
        String miniKey = data.getMiniKey();

        if (mImageCache != null) {
            value = mImageCache.getBitmapFromMemCache(miniKey);
        }
        if (value != null) {
            // Bitmap found in memory cache
            bitmap = value.getBitmap();
        }
        if (bitmap == null) {
            bitmap = mLoadingBitmap;
        }
//        if (mPauseWork) {
//            if (imageView != null) {
//                imageView.setImageDrawable(createDrawable(bitmap));
//            }
//            return;
//        }
        if (cancelPotentialWork(data, imageView)) {
            // BEGIN_INCLUDE(execute_background_task)
            final BitmapWorkerTask task = new BitmapWorkerTask(data, imageView, listener);
            if (imageView != null) {
                final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, bitmap, task);
                imageView.setImageDrawable(asyncDrawable);
            }

            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.
            task.executeOnExecutor(mExecutor);
            //END_INCLUDE(execute_background_task)
        }
    }

    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and
     * disk cache will be used if an {@link ImageCache} has been added using
     * {@link ImageWorker#addImageCache(android.support.v4.app.FragmentManager, ImageCache.ImageCacheParams)}. If the
     * image is found in the memory cache, it is set immediately, otherwise an {@link AsyncTask}
     * will be created to asynchronously load the bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    private void loadImage(Object data, ImageView imageView, OnImageLoadedListener listener, int type) {
        MediaObject object = new MediaObject(data, type);
        loadImage(object, imageView, listener);
    }

    public void loadThumbnail(Object data, ImageView imageView, OnImageLoadedListener listener) {
        if (data == null || imageView == null) {
            return;
        }
        loadImage(data, imageView, listener, TYPE_THUMBNAIL);
    }

    // Load the large bitmap.
    public void loadLarge(final MediaItem item, final PhotoDetailView detailView) {
        if (item == null) {
            return;
        }
        // Init the MediaObject.
        MediaObject object = new MediaObject(item, TYPE_THUMBNAIL);
        final Uri uri = item.getContentUri();
        OnImageLoadedListener listener = new OnImageLoadedListener() {

            @Override
            public void onImageLoaded(boolean success) {
                if (detailView == null || !success) return;
                // If the MediaItem is DRM, get the bitmap by DrmManager.
                if (item.isDrmEnable() && item.isDrm() == GalleryConstant.ITEM_IS_DRM) {
                    detailView.loadDrm(item);
                    return;
                }

                // Otherwise, load the default bitmap.
                if (detailView.isLarge()) {
                    ImageSource full = ImageSource.uri(uri);
                    detailView.setImage(full);
                } else if (detailView.getIsGif()) {
                    detailView.setImageURI(uri);
                }
            }
        };
        loadImage(object, detailView, listener);
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    /**
     * Adds an {@link ImageCache} to this {@link ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param fragmentManager
     * @param cacheParams The cache parameters to use for the image cache.
     */
    public void addImageCache(FragmentManager fragmentManager,
                              ImageCache.ImageCacheParams cacheParams) {
        mImageCacheParams = cacheParams;
        mImageCache = ImageCache.getInstance(fragmentManager, mImageCacheParams);
        initCache();
    }

    /**
     * Adds an {@link ImageCache} to this {@link ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param activity
     * @param diskCacheDirectoryName See
     * {@link ImageCache.ImageCacheParams#ImageCacheParams(android.content.Context, String)}.
     */
    public void addImageCache(FragmentActivity activity, String diskCacheDirectoryName) {
        mImageCacheParams = new ImageCache.ImageCacheParams(activity, diskCacheDirectoryName);
        mImageCache = ImageCache.getInstance(activity.getSupportFragmentManager(), mImageCacheParams);
        initCache();
    }

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    /**
     * Subclasses should override this to define any processing or work that must happen to produce
     * the final bitmap. This will be executed in a background thread and be long running. For
     * example, you could resize a large bitmap here, or pull down an image from the network.
     *
     * @param data The data to identify which image to process, as provided by
     *            {@link ImageWorker#loadImage(Object, android.widget.ImageView)}
     * @return The processed bitmap
     */
    protected abstract Bitmap processBitmap(MediaObject object);

    /**
     * @return The {@link ImageCache} object currently being used by this ImageWorker.
     */
    protected ImageCache getImageCache() {
        return mImageCache;
    }

    /**
     * Cancels any pending work attached to the provided ImageView.
     * @param imageView
     */
    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
            if (Utils.DEBUG) {
                final Object bitmapData = bitmapWorkerTask.mData;
                Log.d(TAG, "cancelWork - cancelled work for " + bitmapData);
            }
        }
    }

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    public static boolean cancelPotentialWork(Object data, ImageView imageView) {
        //BEGIN_INCLUDE(cancel_potential_work)
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.mData;
            if (bitmapData == null || !bitmapData.toString().equals(data.toString())) {
                bitmapWorkerTask.cancel(true);
                if (Utils.DEBUG) {
                    Log.d(TAG, "cancelPotentialWork - cancelled work for " + data);
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
        //END_INCLUDE(cancel_potential_work)
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<Void, BitmapDrawable, BitmapDrawable> {
        private MediaObject mData;
        private final WeakReference<ImageView> imageViewReference;
        private final OnImageLoadedListener mOnImageLoadedListener;
        private ImageRequest mRequest;

        public BitmapWorkerTask(MediaObject data, ImageView imageView, OnImageLoadedListener listener) {
            mData = data;
            if (imageView != null) {
                imageViewReference = new WeakReference<ImageView>(imageView);
            } else {
                imageViewReference = null;
            }
            mOnImageLoadedListener = listener;
        }

        /**
         * Background processing.
         */
        @Override
        protected BitmapDrawable doInBackground(Void... params) {
            //BEGIN_INCLUDE(load_bitmap_in_background)
            if (Utils.DEBUG) {
                Log.d(TAG, "doInBackground - starting work");
            }

            int type = mData.mType;
            String miniKey = mData.getMiniKey();
            String diskKey = mData.getDiskKey();
            Bitmap bitmap = null;
            BitmapDrawable drawable = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }


            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (mImageCache != null && !isCancelled() && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(diskKey);
            }


            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (bitmap == null && !isCancelled() && !mExitTasksEarly) {
                MediaItem item = mData.getItem();
                if (item != null) {
                    switch (type) {
                        case TYPE_THUMBNAIL:
                        default:
                            mRequest = (ImageRequest) item.requestImage(MediaItem.TYPE_MICROTHUMBNAIL);
                            break;
                    }
                    try {
                        bitmap = mRequest.requestBitmap();
                    } catch (Exception e) {
                        Log.d(TAG, "request bitmap failed -- item = " + item, e);
                    }
                    int rotation = item.getRotation();
                    if (bitmap != null && rotation != 0) {
                        bitmap = BitmapUtils.rotateBitmap(bitmap, rotation, true);
                    }
                }
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null) {
                addBitmapToCache(miniKey, bitmap, true);
                drawable = addBitmapToCache(diskKey, bitmap, false);
            }

            if (Utils.DEBUG) {
                Log.d(TAG, "doInBackground - finished work");
            }

            return drawable;
            //END_INCLUDE(load_bitmap_in_background)
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(BitmapDrawable value) {
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                return;
            }

            final ImageView imageView = getAttachedImageView();
            if (imageView != null) {
                imageView.setTag(R.id.image_view_tag, mData);
            }
            updateView(value, imageView, mOnImageLoadedListener);
        }

        @Override
        protected void onCancelled(BitmapDrawable value) {
            if (mRequest != null) {
                mRequest.cancelRequest();
            }
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private ImageView getAttachedImageView() {
            if (imageViewReference == null) {
                return null;
            }
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    /**
     * Interface definition for callback on image loaded successfully.
     */
    public interface OnImageLoadedListener {

        /**
         * Called once the image has been loaded.
         * @param success True if the image was loaded successfully, false if
         *                there was an error.
         */
        void onImageLoaded(boolean success);
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    public static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * Called when the processing is complete and the final drawable should be
     * set on the ImageView.
     *
     * @param imageView
     * @param drawable
     */
    private void setImageDrawable(ImageView imageView, Drawable drawable) {
        if (mFadeInBitmap) {
            // Transition drawable with a transparent drawable and the final drawable
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[] {
                            new ColorDrawable(Color.TRANSPARENT),
                            drawable
                    });
//            // Set background to loading bitmap
//            imageView.setBackgroundDrawable(
//                    new BitmapDrawable(mResources, mLoadingBitmap));

            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p>
     * If work is paused, be sure setPauseWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }

    protected void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }

    public void initCache() {
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }

    private BitmapDrawable addBitmapToCache(String dataString, Bitmap bitmap, boolean memoryCache) {
        BitmapDrawable drawable = null;
        // If the bitmap was processed and the image cache is available, then add the processed
        // bitmap to the cache for future use. Note we don't check if the task was cancelled
        // here, if it was, and the thread is still running, we may as well add the processed
        // bitmap to our cache as it might be used again in the future
        if (bitmap != null && mImageCache != null) {
            if (memoryCache) {
                if (mImageCache.getBitmapFromMemCache(dataString) != null) {
                    return null;
                } else {
                    bitmap = BitmapUtils.resizeDownBySideLength(bitmap, 100, false);
                }
            }
            drawable = createDrawable(bitmap);
            mImageCache.addBitmapToCache(dataString, drawable, memoryCache);
        }
        return drawable;
    }

    private void updateView(BitmapDrawable value, ImageView imageView, OnImageLoadedListener listener) {
        // BEGIN_INCLUDE(complete_background_work)
        boolean success = false;

        if (imageView != null) {
            if (value != null) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "onPostExecute - setting bitmap");
                }
                success = true;
                setImageDrawable(imageView, value);
            } else {
                value = new BitmapDrawable(mResources, mLoadingBitmap);
                setImageDrawable(imageView, value);
            }
        }
        if (listener != null) {
            listener.onImageLoaded(success);
        }
        // END_INCLUDE(complete_background_work)
    }

    public static final int TYPE_THUMBNAIL = 1;

    public class MediaObject {
        private static final String MINI = ":mini";
        private static final String THUMBNAIL = ":thumbnail";

        private MediaItem mItem;
        final String mPath;
        final int mType;

        public MediaObject(Object object, int type) {
            if (MediaItem.class.isInstance(object)) {
                mItem = (MediaItem) object;
                mPath = mItem.toString();
            } else {
                mPath = String.valueOf(object);
            }
            mType = type;
        }

        public MediaItem getItem() {
            return mItem;
        }

        public String getMiniKey() {
            return mPath + MINI;
        }

        public String getDiskKey() {
            return mPath + THUMBNAIL;
        }

        @Override
        public String toString() {
            return mPath;
        }
    }

    public BitmapDrawable createDrawable(Bitmap bitmap) {
        BitmapDrawable drawable;
        if (Utils.hasHoneycomb()) {
            // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
            drawable = new BitmapDrawable(mResources, bitmap);
        } else {
            // Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
            // which will recycle automagically
            drawable = new RecyclingBitmapDrawable(mResources, bitmap);
        }
        return drawable;
    }

    public void loadGlide(final Activity activity, final MediaItem item, final ImageView imageView) {
        if (activity == null || item == null || imageView == null) {
            return;
        }
        if (activity.isDestroyed()) {
            return;
        }
        Drawable drawable = imageView.getDrawable();
        BitmapTypeRequest<Uri> request = Glide.with(activity).fromUri().asBitmap();
        if (drawable != null) {
            request.placeholder(drawable);
        }
        request.load(item.getContentUri()).into(imageView);
    }

    public void resumeWork() {
//        setExitTasksEarly(false);
        setPauseWork(false);
    }

    public void pauseWork() {
//        setExitTasksEarly(true);
        setPauseWork(true);
    }
}

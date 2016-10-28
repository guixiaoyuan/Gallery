package com.tct.gallery3d.app.view;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.common.BitmapUtils;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.image.AsyncTask;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

public class PhotoDetailView extends SubsamplingScaleImageView {

    // Record the image position in media set. If the position is -1, it means
    // the image was invalidate.
    protected int mPosition = -1;

    private boolean mIsVideo = false;
    private boolean mIsGif = false;

    private Bitmap mVideoIcon = null;
    private RectF mVideoIconRect = new RectF();
    private boolean mReScale = false;

    private float mLastMotionX = 0.f;
    private float mLastMotionY = 0.f;
    private long mLastTime = 0;
    private int mOrientation;

    private GestureEventListener mGestureEventListener = null;

    public void setGestureEventListener(GestureEventListener listener) {
        mGestureEventListener = listener;
    }

    public PhotoDetailView(Context context) {
        this(context, null);
    }

    public PhotoDetailView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    private void init() {
        mVideoIcon = ((BitmapDrawable) getContext().getDrawable(R.drawable.ic_video_play_big)).getBitmap()
                .copy(Config.ARGB_8888, false);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cleanUp();
    }

    private void cleanUp() {
        setGestureEventListener(null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float x = getX() + (getWidth() / 2 - mVideoIcon.getWidth() / 2);
        float y = getY() + (getHeight() / 2 - mVideoIcon.getHeight() / 2);

        mVideoIconRect.set(x, y, x + mVideoIcon.getWidth(), y + mVideoIcon.getHeight());

        if (mReScale) {
            resetScaleAndCenter();
            mReScale = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsVideo) {
            canvas.save();
            float x = canvas.getWidth() / 2 - mVideoIcon.getWidth() / 2;
            float y = canvas.getHeight() / 2 - mVideoIcon.getHeight() / 2;
            canvas.translate(x, y);
            canvas.drawBitmap(mVideoIcon, 0, 0, null);
            canvas.restore();
        }
    }

    public void setIsVideo(boolean isVideo) {
        mIsVideo = isVideo;
        setZoomEnabled(!isVideo);
    }

    public void setIsGif(boolean isGif) {
        mIsGif = isGif;
        setZoomEnabled(!isGif);
    }

    public boolean getIsGif() {
        return mIsGif;
    }

    public boolean isLarge() {
        return !mIsVideo && !mIsGif;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsVideo) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mLastMotionX = event.getX();
                    mLastMotionY = event.getY();
                    mLastTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    if ((Math.hypot((event.getX() - mLastMotionX), (event.getY() - mLastMotionY)) < 5
                            && mVideoIconRect.contains(mLastMotionX, mLastMotionY) && mGestureEventListener != null
                            && (System.currentTimeMillis() - mLastTime) < 150)) {
                        mGestureEventListener.onPlayClicked(mPosition);
                    } else {
                        performClick();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mLastTime = 0;
                    mLastMotionX = 0.f;
                    mLastMotionY = 0.f;
                    break;
                default:
                    break;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    public interface GestureEventListener {
        void onPlayClicked(int position);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getOrientation() != newConfig.orientation) {
            mReScale = true;
        }
    }

    public void reset() {
        super.reset(true);
        mIsGif = false;
        mIsVideo = false;
        mReScale = false;
    }

    public void setRotation(int orientation) {
        mOrientation = orientation;
    }

    @Override
    protected int getExifOrientation(String sourceUri) {
        return mOrientation;
    }

    /**
     * Load drm bitmap by MediaItem.
     *
     * @param item
     */
    public void loadDrm(final MediaItem item) {
        if (item == null) {
            return;
        }

        // Load the drm bitmap by the task.
        AsyncTask task = new AsyncTask<Object, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Object... params) {
                String path = item.getFilePath();
                // Get the drm from the DrmManager.
                Bitmap bitmap = DrmManager.getInstance().getDrmBitmap(path);

                // Rotate the bitmap from the DrmManager.
                int rotation = getExifOrientation(null);
                if (bitmap != null && rotation != 0) {
                    bitmap = BitmapUtils.rotateBitmap(bitmap, rotation, true);
                }

                //Scale the Bitmap to the default size.
                if (bitmap != null && bitmap.getWidth() > 1 && bitmap.getHeight() > 1) {
                    float width = bitmap.getWidth();
                    float height = bitmap.getHeight();
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    float screenWidth = dm.widthPixels;
                    float screenHeight = dm.heightPixels;
                    setMaxScale(Math.max(screenWidth / width, screenHeight / height) * GalleryConstant.MAX_SCALE);
                    setDoubleTapZoomScale(Math.max(screenWidth / width, screenHeight / height) * GalleryConstant.MAX_SCALE);
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    onImageLoaded(bitmap, 0, false);
                }
            }
        };
        task.execute();
    }
}

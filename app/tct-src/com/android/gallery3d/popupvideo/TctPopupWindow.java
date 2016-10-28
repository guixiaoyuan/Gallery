package com.android.gallery3d.popupvideo;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

// [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014,FR828601 Pop-up Video play
/**
 * This class is created to manage the window related logic. Including: moving,
 * scaling, click, double click, etc.
 */
public class TctPopupWindow implements View.OnTouchListener {
    public static String TAG = "TctPopupWindow";

    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();

    public static final int STATUS_NONE = 0;
    public static final int STATUS_SCROLLING = 1;
    public static final int STATUS_SCALING = 2;
    public static final int STATUS_FLYING = 3;
    private int mStatus = STATUS_NONE;
    private Object mStatusLock = new Object();

    /**
     * Nothing will be changed
     */
    public static final int ADJUST_NO_LIMITE = 0;

    /**
     * The rectangle will be keep in screen
     */
    public static final int ADJUST_KEEP_IN_SCREEN = 1;

    /**
     * The rectangle can be partly out of screen
     */
    public static final int ADJUST_KEEP_IN_SIGHT = 2;

    protected float MIN_IN_SCREEN_LENGTH = 0;

    private float mFlyAwayX = 0.0f;
    private float mFlyAwayY = 0.0f;
    private float mFlyAwayWidth = 0.0f;
    private float mFlyAwayHeight = 0.0f;
    private AnimatorSet mFlyingAnimatorSet = null;

    private float mX = 0.0f;
    private float mY = 0.0f;
    private float mWidth = 200.0f;
    private float mHeight = 200.0f;

    private Handler mHandler = new Handler();

    private Context mContext;
    private View mContentView = null;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams = null;
    private boolean mIsCreated = false;
    static boolean mIsCteatePop = false;

    private OnStatusChangedListener mOnStatusChangedListener = null;
    private View.OnClickListener mOnClickListener = null;
    private View.OnLongClickListener mOnLongClickListener = null;

    private TctPopupWindowRotationWatcher mRotationWatcher = TctPopupWindowRotationWatcher
            .getInstance();

    /**
     * Here we monitor the rotation of window. Window will be auto adjusted to
     * be in screen after rotation is changed.
     */
    private Runnable mRotationWatcherListener = new Runnable() {
        @Override
        public void run() {
            if (isCreated() && isShowing()) {
                RectF rect = getWindowRect();
                rect = adjustRectangle(rect, ADJUST_KEEP_IN_SCREEN);
                setWindowRect(rect);
            }
        }
    };

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    /**
     * Within this listener, only scrolling is managed. Click & double click is
     * managed by {@link #onClick(View)}
     * @see #onClick(View)
     * @see #mClickAction
     */
    private GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        float lastRawX = 0.0f;
        float lastRawY = 0.0f;

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mHandler.postDelayed(mClickAction, DOUBLE_TAP_TIMEOUT);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mOnLongClickListener != null) {
                mOnLongClickListener.onLongClick(mContentView);
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mHandler.removeCallbacks(mClickAction);
            PointF screenSize = getScreenSize();
            flyTo((screenSize.x - getWidth()) / 2, (screenSize.y - getHeight()) / 2);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            lastRawX = e.getRawX();
            lastRawY = e.getRawY();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            if (isStatus(STATUS_NONE)) {
                setStatus(STATUS_SCROLLING);
            }

            if (isStatus(STATUS_SCROLLING)) {
                offset(e2.getRawX() - lastRawX, e2.getRawY() - lastRawY, ADJUST_KEEP_IN_SIGHT);
                lastRawX = e2.getRawX();
                lastRawY = e2.getRawY();
            }
            return true;
        }
    };

    /**
     * Within this listener, only scaling is managed.
     */
    private ScaleGestureDetector.OnScaleGestureListener mOnScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if (isStatus(STATUS_NONE) || isStatus(STATUS_SCROLLING)) {
                setStatus(STATUS_SCALING);
            }

            if (isStatus(STATUS_SCALING)) {
                scale(detector.getScaleFactor(), ADJUST_KEEP_IN_SCREEN);
            }
            return true;
        }
    };

    /**
     * This runnable will be posted for signal click
     * @see #onClick(View)
     */
    private Runnable mClickAction = new Runnable() {
        @Override
        public void run() {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(mContentView);
            }
        }
    };

    public interface OnStatusChangedListener {
        /**
         * Called when window status is changed
         * @param oldStatus old status
         * @param newStatus new status
         * @see #STATUS_NONE
         * @see #STATUS_SCROLLING
         * @see #STATUS_SCALING
         * @see #STATUS_FLYING
         */
        void onStatusChanged(int oldStatus, int newStatus);
    }

    /**
     * Constructor method
     * @param context context
     */
    public TctPopupWindow(Context context) {
        if (context == null) {
            throw new NullPointerException();
        }

        mContext = context.getApplicationContext();

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        // gestures
        mGestureDetector = new GestureDetector(mContext, mOnGestureListener);
        mScaleGestureDetector = new ScaleGestureDetector(mContext, mOnScaleGestureListener);

        MIN_IN_SCREEN_LENGTH = mContext.getResources().getDisplayMetrics().density * 50.0f; // 50dp
    }

    /**
     * Set listener to receive status changed event
     * @param listener
     */
    public void setOnStatusChangedListener(OnStatusChangedListener listener) {
        mOnStatusChangedListener = listener;
    }

    /**
     * Set listener to signal click event
     * @param listener
     */
    public void setOnClickListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    /**
     * Set window status.<br>
     * NOTE: If new status is the same with current status, nothing will be done
     * @param status status
     * @see #STATUS_NONE
     * @see #STATUS_SCROLLING
     * @see #STATUS_SCALING
     * @see #STATUS_FLYING
     */
    private void setStatus(int status) {
        synchronized (mStatusLock) {
            if (mStatus != status) {
                if (mOnStatusChangedListener != null) {
                    mOnStatusChangedListener.onStatusChanged(mStatus, status);
                }

                mStatus = status;
            }
        }
    }

    /**
     * Get current status
     * @return status
     * @see #STATUS_NONE
     * @see #STATUS_SCROLLING
     * @see #STATUS_SCALING
     * @see #STATUS_FLYING
     */
    public int getStatus() {
        synchronized (mStatusLock) {
            return mStatus;
        }
    }

    /**
     * Check whether current status match the given status
     * @param status
     * @return true: match, false: not match
     * @see #STATUS_NONE
     * @see #STATUS_SCROLLING
     * @see #STATUS_SCALING
     * @see #STATUS_FLYING
     */
    protected boolean isStatus(int status) {
        synchronized (mStatusLock) {
            return mStatus == status;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isStatus(STATUS_NONE) || isStatus(STATUS_SCALING) || isStatus(STATUS_SCROLLING)) {
            mScaleGestureDetector.onTouchEvent(event);
        }

        if (isStatus(STATUS_NONE) || isStatus(STATUS_SCROLLING)) {
            mGestureDetector.onTouchEvent(event);
        }

        boolean ret = false;
        if (!isStatus(STATUS_NONE)) {
            ret = true;
        }

        if ((event.getAction() == MotionEvent.ACTION_UP)
                || (event.getAction() == MotionEvent.ACTION_DOWN)) {
            setStatus(STATUS_NONE);
        }

        return ret;
    }

    /**
     * Set the content view, which will be displayed on window.
     * @param resId resource id
     * @return the view created
     * @see #setContentView(View)
     */
    public View setContentView(int resId) {
        return setContentView(View.inflate(mContext, resId, null));
    }

    /**
     * Set the content view, which will be displayed on window.
     * @param view view
     * @return the view created
     * @see #setContentView(int)
     */
    public View setContentView(View view) {
        mContentView = view;
        if (mContentView != null) {
            mContentView.setSoundEffectsEnabled(false);
            mContentView.setOnTouchListener(this);
        } else {
            close();
        }

        return mContentView;
    }

    /**
     * Get content view
     * @return view
     */
    public View getContentView() {
        return mContentView;
    }

    /**
     * Get x position of top-left point
     * @return
     * @see #setX(float)
     */
    public float getX() {
        return mX;
    }

    /**
     * Set x position of top-left point
     * @param x
     * @see #getX()
     */
    public void setX(float x) {
        mX = x;
        update();
    }

    /**
     * Get y position of top-left point
     * @return
     * @see #setY(float)
     */
    public float getY() {
        return mY;
    }

    /**
     * Set y position of top-left point
     * @param y
     * @see #getY()
     */
    public void setY(float y) {
        mY = y;
        update();
    }

    /**
     * Get window width
     * @return
     * @see #setWidth(float)
     */
    public float getWidth() {
        return mWidth;
    }

    /**
     * Set window width
     * @param width
     * @see #getWidth()
     */
    public void setWidth(float width) {
        mWidth = width;
        update();
    }

    /**
     * Get window height
     * @return
     * @see #setHeight(float)
     */
    public float getHeight() {
        return mHeight;
    }

    /**
     * Set window height
     * @param height
     * @see #getHeight()
     */
    public void setHeight(float height) {
        mHeight = height;
        update();
    }

    /**
     * Get rectangle of window
     * @return Rectangle of the window
     * @see #setWindowRect(RectF)
     * @see #setWindowRect(float, float, float, float)
     */
    public RectF getWindowRect() {
        return new RectF(
                mX,
                mY,
                mX + mWidth,
                mY + mHeight);
    }

    /**
     * Set rectangle of window
     * @param rect rectangle
     * @see #setWindowRect(float, float, float, float)
     * @see #getWindowRect()
     */
    public void setWindowRect(RectF rect) {
        setWindowRect(rect.left, rect.top, rect.width(), rect.height());
    }

    /**
     * Set rectangle of window
     * @param x x
     * @param y y
     * @param width width
     * @param height width
     * @see #setWindowRect(RectF)
     * @see #getWindowRect()
     */
    public void setWindowRect(float x, float y, float width, float height) {
        mX = x;
        mY = y;
        mWidth = width;
        mHeight = height;

        update();
    }

    /**
     * Whether window is showing
     * @return true: showing, false: hidden
     * @see #show()
     * @see #hide()
     */
    public boolean isShowing() {
        return (mContentView == null) ? false : (mContentView.getVisibility() == View.VISIBLE);
    }

    /**
     * Whether window is created
     * @return true: created, false: not created
     * @see #create()
     * @see #close()
     */
    public boolean isCreated() {
        return mIsCreated;
    }
     public static boolean isCteatedPop(){
          return mIsCteatePop;
     }
    /**
     * Generate window parameter
     * @return window parameter
     */
    protected WindowManager.LayoutParams getWindowParams() {
        if (mWindowParams == null) {
            mWindowParams = new WindowManager.LayoutParams();
            mWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mWindowParams.format = PixelFormat.RGBA_8888;
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
        }

        // update window rectangle
        mWindowParams.x = (int) mX;
        mWindowParams.y = (int) mY;
        mWindowParams.width = (int) mWidth;
        mWindowParams.height = (int) mHeight;

        return mWindowParams;
    }

    /**
     * Create window.<br>
     * NOTE: You must call {@link #close()} to release resource later
     * @see #close()
     * @see #show()
     * @see #hide()
     */
    public void create() {
        if (!mIsCreated && (mContentView != null)) {
            mContentView.setVisibility(View.GONE);
            mWindowManager.addView(mContentView, getWindowParams());
            mIsCreated = true;
            mIsCteatePop = true;
            Log.i(TAG,"mIsCteatedPop:"+mIsCteatePop+ "  mIsCreated:"+mIsCreated);
        }
    }

    /**
     * Show window.
     * @see #create()
     * @see #hide()
     * @see #close()
     */
    public void show() {
        if (mContentView != null) {
            mContentView.setVisibility(View.VISIBLE);

            mRotationWatcher.setOnRotationChangedListener(mRotationWatcherListener);
        }
    }

    /**
     * Hide window
     * @see #create()
     * @see #show()
     * @see #close()
     */
    public void hide() {
        if (mContentView != null) {
            mContentView.setVisibility(View.GONE);

            mRotationWatcher.setOnRotationChangedListener(null);
        }
    }

    /**
     * Close the window. It will release all the resource of the window. If
     * window is not created, nothing will happen
     * @see #create()
     * @see #hide()
     * @see #show()
     */
    public void close() {
        if (mIsCreated && (mContentView != null)) {
            mWindowManager.removeView(mContentView);
            mIsCreated = false;
            mIsCteatePop = false;
            Log.i(TAG," close()  mIsCreated: "+mIsCreated+"  mIsCteatePop:"+mIsCteatePop);
        }
    }

    /**
     * Force update window layout
     */
    public void update() {
        if (mIsCreated && (mContentView != null)) {
            mWindowManager.updateViewLayout(mContentView, getWindowParams());
        }
    }

    /**
     * Get the size of current screen
     * @return screen size
     */
    public PointF getScreenSize() {
        Point size = new Point();
        mWindowManager.getDefaultDisplay().getSize(size);
        return new PointF(size);
    }

    /**
     * Move the window by offset
     * @param dx offset x
     * @param dy offset y
     * @param adjustType adjust type
     * @see #ADJUST_NO_LIMITE
     * @see #ADJUST_KEEP_IN_SCREEN
     * @see #ADJUST_KEEP_IN_SIGHT
     */
    public void offset(float dx, float dy, int adjustType) {
        if (!isShowing()) {
            return;
        }

        RectF newRect = getWindowRect();
        newRect.offset(dx, dy);

        newRect = adjustRectangle(newRect, adjustType);

        setWindowRect(newRect);
    }

    /**
     * Scale the pop up window
     * @param factor scale factor
     * @param adjustType adjust type
     * @see #ADJUST_NO_LIMITE
     * @see #ADJUST_KEEP_IN_SCREEN
     * @see #ADJUST_KEEP_IN_SIGHT
     */
    public void scale(float factor, int adjustType) {
        if (!isShowing()) {
            return;
        }

        RectF oldRect = getWindowRect();
        RectF newRect = new RectF(
                0.0f,
                0.0f,
                oldRect.width() * factor,
                oldRect.height() * factor);

        // move new rectangle's center to the old one's
        newRect.offset(
                oldRect.centerX() - newRect.centerX(),
                oldRect.centerY() - newRect.centerY());

        // adjust in screen
        newRect = adjustRectangle(newRect, adjustType);

        setWindowRect(newRect);
    }

    /**
     * Move window to target position with animation
     * @param destX
     * @param destY
     */
    public void flyTo(float destX, float destY) {
        flyTo(destX, destY, getWidth(), getHeight());
    }

    /**
     * Move window to target rectangle with animation
     * @param rect
     */
    public void flyTo(RectF rect) {
        flyTo(rect.left, rect.top, rect.width(), rect.height());
    }

    /**
     * Move window to target rectangle with animation
     * @param destX
     * @param destY
     * @param destWidth
     * @param destHeight
     */
    public void flyTo(float destX, float destY, float destWidth, float destHeight) {
        if (isStatus(STATUS_FLYING)) {
            if (mFlyingAnimatorSet != null) {
                mFlyingAnimatorSet.cancel();
                mFlyingAnimatorSet = null;
            }
            setStatus(STATUS_NONE);
        }
        setStatus(STATUS_FLYING);

        mFlyingAnimatorSet = new AnimatorSet();
        ObjectAnimator animX = ObjectAnimator.ofFloat(this, "x", getX(), destX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(this, "y", getY(), destY);
        ObjectAnimator animWidth = ObjectAnimator.ofFloat(this, "width", getWidth(), destWidth);
        ObjectAnimator animHeight = ObjectAnimator.ofFloat(this, "height", getHeight(), destHeight);

        mFlyingAnimatorSet.playTogether(animX, animY, animWidth, animHeight);
        mFlyingAnimatorSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFlyingAnimatorSet = null;
                setStatus(STATUS_NONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });

        mFlyingAnimatorSet.start();
    }

    /**
     * Move window out of screen with animation to make it like flying away
     * @param away true: fly away, false: fly back
     */
    public void flyAway(boolean away) {
        if (away) {
            mFlyAwayX = getX();
            mFlyAwayY = getY();
            mFlyAwayWidth = getWidth();
            mFlyAwayHeight = getHeight();

            float targetX = -mFlyAwayWidth * 0.2f;
            float targetY = -mFlyAwayHeight * 0.2f;
            float targetWidth = -targetX;
            float targetHeight = -targetY;

            flyTo(targetX, targetY, targetWidth, targetHeight);
        } else {
            flyTo(mFlyAwayX, mFlyAwayY, mFlyAwayWidth, mFlyAwayHeight);
        }
    }

    /**
     * Adjust input rectangle according to the type
     * @param rect input rectangle
     * @param type adjust type
     * @return adjusted rectangle
     * @see #ADJUST_NO_LIMITE
     * @see #ADJUST_KEEP_IN_SCREEN
     * @see #ADJUST_KEEP_IN_SIGHT
     */
    protected RectF adjustRectangle(RectF rect, int type) {
        switch (type) {
            case ADJUST_KEEP_IN_SCREEN:
                return adjustInScreen(rect);
            case ADJUST_KEEP_IN_SIGHT:
                return adjustInSight(rect);
            case ADJUST_NO_LIMITE:
            default:
                return rect;
        }
    }

    /**
     * Don't use this method directly. Use {@link #adjustRectangle(RectF, int)}
     * instead.
     * @param rect
     * @return
     */
    protected RectF adjustInSight(RectF rect) {
        float newX = rect.left;
        float newY = rect.top;
        PointF screenSize = getScreenSize();

        // generate new X
        if (rect.width() + rect.left < MIN_IN_SCREEN_LENGTH) {
            newX = MIN_IN_SCREEN_LENGTH - rect.width();
        } else if (screenSize.x - rect.left < MIN_IN_SCREEN_LENGTH) {
            newX = screenSize.x - MIN_IN_SCREEN_LENGTH;
        }

        // generate new Y
        if (rect.height() + rect.top < MIN_IN_SCREEN_LENGTH) {
            newY = MIN_IN_SCREEN_LENGTH - rect.height();
        } else if (screenSize.y - rect.top < MIN_IN_SCREEN_LENGTH) {
            newY = screenSize.y - MIN_IN_SCREEN_LENGTH;
        }

        return new RectF(newX, newY, newX + rect.width(), newY + rect.height());
    }

    /**
     * Don't use this method directly. Use {@link #adjustRectangle(RectF, int)}
     * instead.
     * @param rect input rectangle
     * @return adjusted rectangle
     */
    protected RectF adjustInScreen(RectF rect) {
        RectF newRect = new RectF(rect);
        PointF screenSize = getScreenSize();

        // make sure the rectangle is not larger than screen
        if (newRect.width() > screenSize.x) {
            float newHeight = newRect.height() * screenSize.x / newRect.width();
            float newWidth = screenSize.x;
            newRect.right -= newRect.width() - newWidth;
            newRect.bottom -= newRect.height() - newHeight;
        }
        if (newRect.height() > screenSize.y) {
            float newWidth = newRect.width() * screenSize.y / newRect.height();
            float newHeight = screenSize.y;
            newRect.right -= newRect.width() - newWidth;
            newRect.bottom -= newRect.height() - newHeight;
        }

        // move new rectangle's center to the old one's
        newRect.offset(
                rect.centerX() - newRect.centerX(),
                rect.centerY() - newRect.centerY());

        // make sure it's not out of screen
        float offsetX = 0.0f;
        float offsetY = 0.0f;
        if (newRect.left < 0) {
            offsetX = -newRect.left;
        }
        if (newRect.top < 0) {
            offsetY = -newRect.top;
        }
        newRect.offset(offsetX, offsetY);
        offsetX = 0.0f;
        offsetY = 0.0f;

        if (newRect.right > screenSize.x) {
            offsetX = screenSize.x - newRect.right;
        }
        if (newRect.bottom > screenSize.y) {
            offsetY = screenSize.y - newRect.bottom;
        }
        newRect.offset(offsetX, offsetY);

        return newRect;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener) {
        mOnLongClickListener = listener;
    }
}
// [FEATURE]-Add-END by jian.pan1

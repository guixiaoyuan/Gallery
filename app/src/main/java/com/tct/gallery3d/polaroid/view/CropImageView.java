/* ----------|----------------------|----------------------|----------------- */
/* 07/08/2015| jian.pan1            | PR1026718            |[Gallery]The interface display error.
/* ----------|----------------------|----------------------|----------------- */
/* 07/15/2015| jian.pan1            | PR1040393            |[Android 5.1][Gallery_v5.1.13.1.0212.0]The division line is not display smooth when use polariud edit
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.polaroid.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tct.gallery3d.util.Log;

public class CropImageView extends View {

    private float oldX = 0;
    private float oldY = 0;

    private final int STATUS_Touch_SINGLE = 1;
    private final int STATUS_TOUCH_MULTI_START = 2;
    private final int STATUS_TOUCH_MULTI_TOUCHING = 3;

    private int mStatus = STATUS_Touch_SINGLE;

    private final int defaultCropWidth = 300;
    private final int defaultCropHeight = 300;
    private int cropWidth = defaultCropWidth;
    private int cropHeight = defaultCropHeight;

    protected float oriRationWH = 0;
    protected final float maxZoomOut = 5.0f;
    protected final float minZoomIn = 0.333333f;

    protected Drawable mDrawable;
    protected FloatDrawable mFloatDrawable;
    protected Rect mDrawableSrc = new Rect();
    protected Rect mDrawableDst = new Rect();
    protected Rect mDrawableFloat = new Rect();
    protected boolean isFrist = true;

    protected Context mContext;

    private int srcW = 0;
    private int srcH = 0;

    private final static int ORIRATION_SQUARE = 0;
    private final static int ORIRATION_PORTRAIT = 1;
    private final static int ORIRATION_LANDSCAPE = 2;
    private int currState = ORIRATION_SQUARE;

    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                this.setLayerType(LAYER_TYPE_SOFTWARE, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mFloatDrawable = new FloatDrawable(context);
    }

    public void setDrawable(Drawable mDrawable, int cropWidth, int cropHeight) {
        this.mDrawable = mDrawable;
        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;
        this.isFrist = true;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getPointerCount() > 1) {
            if (mStatus == STATUS_Touch_SINGLE) {
                mStatus = STATUS_TOUCH_MULTI_START;
            } else if (mStatus == STATUS_TOUCH_MULTI_START) {
                mStatus = STATUS_TOUCH_MULTI_TOUCHING;
            }
        } else {
            if (mStatus == STATUS_TOUCH_MULTI_START || mStatus == STATUS_TOUCH_MULTI_TOUCHING) {

                oldX = event.getX();
                oldY = event.getY();
            }

            mStatus = STATUS_Touch_SINGLE;
        }

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            oldX = event.getX();
            oldY = event.getY();
            break;

        case MotionEvent.ACTION_UP:
            checkBounds();
            break;

        case MotionEvent.ACTION_POINTER_1_DOWN:
            break;

        case MotionEvent.ACTION_POINTER_UP:
            break;

        case MotionEvent.ACTION_MOVE:
            if (mStatus == STATUS_TOUCH_MULTI_TOUCHING) {
                // more than one pointer
            } else if (mStatus == STATUS_Touch_SINGLE) {
                int dx = (int) (event.getX() - oldX);
                int dy = (int) (event.getY() - oldY);

                oldX = event.getX();
                oldY = event.getY();

                if (currState == ORIRATION_PORTRAIT) {
                    dx = 0;
                    if (mDrawableDst.top + dy > mDrawableFloat.top
                            || mDrawableDst.bottom + dy < mDrawableFloat.bottom) {
                        break;
                    }
                } else if (currState == ORIRATION_LANDSCAPE) {
                    dy = 0;
                    if (mDrawableDst.left + dx > mDrawableFloat.left
                            || mDrawableDst.right + dx < mDrawableFloat.right) {
                        break;
                    }
                } else if (currState == ORIRATION_SQUARE) {
                    break;
                }
                if (!(dx == 0 && dy == 0)) {
                    mDrawableDst.offset((int) dx, (int) dy);
                    invalidate();
                }
            }
            break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawable == null) {
            return; // couldn't resolve the URI
        }

        if (mDrawable.getIntrinsicWidth() == 0 || mDrawable.getIntrinsicHeight() == 0) {
            return; // nothing to draw (empty bounds)
        }

        configureBounds();

        mDrawable.draw(canvas);
        canvas.save();
        canvas.clipRect(mDrawableFloat, Region.Op.DIFFERENCE);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-08,PR1026718 begin
        canvas.drawColor(Color.parseColor("#FFF7F6F5"));
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-08,PR1026718 end
        canvas.restore();
        mFloatDrawable.draw(canvas);
    }

    protected void configureBounds() {
        if (isFrist) {
            oriRationWH = ((float) mDrawable.getIntrinsicWidth())
                    / ((float) mDrawable.getIntrinsicHeight());

            if (oriRationWH < 1) {
                currState = ORIRATION_PORTRAIT;
            } else if (oriRationWH > 1) {
                currState = ORIRATION_LANDSCAPE;
            } else if (oriRationWH == 1) {
                currState = ORIRATION_SQUARE;
            }

            int floatWidth = dipTopx(mContext, cropWidth);
            int floatHeight = dipTopx(mContext, cropHeight);

            if (floatWidth > getWidth()) {
                floatWidth = getWidth();
                floatHeight = cropHeight * floatWidth / cropWidth;
            }

            if (floatHeight > getHeight()) {
                floatHeight = getHeight();
                floatWidth = cropWidth * floatHeight / cropHeight;
            }

            int floatLeft = (getWidth() - floatWidth) / 2;
            int floatTop = (getHeight() - floatHeight) / 2;
            mDrawableFloat.set(floatLeft, floatTop, floatLeft + floatWidth, floatTop + floatHeight);

            int srcLeft = 0;
            int srcTop = 0;
            int srcRight = 0;
            int srcBottom = 0;
            if (currState == ORIRATION_PORTRAIT || currState == ORIRATION_SQUARE) {
                srcW = floatWidth;
                srcH = (int) (srcW / oriRationWH);
                srcLeft = floatLeft;
                srcTop = (getHeight() - srcH) / 2;
                srcRight = srcLeft + srcW;
                srcBottom = srcTop + srcH;
            } else if (currState == ORIRATION_LANDSCAPE) {
                srcH = floatHeight;
                srcW = (int) (srcH * oriRationWH);
                srcLeft = (getWidth() - srcW) / 2;
                srcTop = floatTop;
                srcRight = srcLeft + srcW;
                srcBottom = srcTop + srcH;
            }
            mDrawableSrc.set(srcLeft, srcTop, srcRight, srcBottom);
            mDrawableDst.set(mDrawableSrc);

            isFrist = false;
        }

        mDrawable.setBounds(mDrawableDst);
        mFloatDrawable.setBounds(mDrawableFloat);
    }

    protected void checkBounds() {
        int newLeft = mDrawableDst.left;
        int newTop = mDrawableDst.top;

        boolean isChange = false;
        if (mDrawableDst.left < -mDrawableDst.width()) {
            newLeft = -mDrawableDst.width();
            isChange = true;
        }

        if (mDrawableDst.top < -mDrawableDst.height()) {
            newTop = -mDrawableDst.height();
            isChange = true;
        }

        if (mDrawableDst.left > getWidth()) {
            newLeft = getWidth();
            isChange = true;
        }

        if (mDrawableDst.top > getHeight()) {
            newTop = getHeight();
            isChange = true;
        }

        if (currState == ORIRATION_PORTRAIT) {
            if (newTop > mDrawableFloat.top) {
                newTop = mDrawableFloat.top;
                isChange = true;
            }
            if (newTop + srcH < mDrawableFloat.bottom) {
                newTop = mDrawableFloat.bottom - srcH;
                isChange = true;
            }
        } else if (currState == ORIRATION_LANDSCAPE) {
            if (newLeft > mDrawableFloat.left) {
                newLeft = mDrawableFloat.left;
                isChange = true;
            }
            if (newLeft + srcW < mDrawableFloat.right) {
                newLeft = mDrawableFloat.right - srcW;
                isChange = true;
            }
        } else if (currState == ORIRATION_SQUARE) {
            Log.i(VIEW_LOG_TAG, "current can't change");
        }

        mDrawableDst.offsetTo(newLeft, newTop);
        if (isChange) {
            invalidate();
        }
    }

    public Bitmap getCropImage() {
        Bitmap tmpBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(tmpBitmap);
        mDrawable.draw(canvas);

        Matrix matrix = new Matrix();
        float scale = (float) (mDrawableSrc.width()) / (float) (mDrawableDst.width());
        matrix.postScale(scale, scale);

        Bitmap ret = Bitmap.createBitmap(tmpBitmap, mDrawableFloat.left, mDrawableFloat.top,
                mDrawableFloat.width(), mDrawableFloat.height(), matrix, true);
        tmpBitmap.recycle();
        tmpBitmap = null;

        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1040393 begin
        Bitmap newRet = Bitmap.createScaledBitmap(ret, cropWidth, cropHeight, true);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1040393 end
        ret.recycle();
        ret = newRet;

        return ret;
    }

    public int dipTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-08,PR1026718 begin
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-08,PR1026718 end
}

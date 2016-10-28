package com.tct.gallery3d.app.view;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
/* MODIFIED-BEGIN by Yaoyu.Yang, 2016-06-11,BUG-2208330*/
import android.view.animation.AccelerateInterpolator;

public class SmoothImageView extends RecyclingImageView {

    public static final int ANIMATION_DURATION = 250;
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330 */
    public static final int STATE_NORMAL = 0;
    public static final int STATE_TRANSFORM_IN = 1;
    public static final int STATE_TRANSFORM_OUT = 2;

    public static final String TYPE_SCALE = "scale";
    public static final String TYPE_LEFT = "left";
    public static final String TYPE_TOP = "top";
    public static final String TYPE_WIDTH = "width";
    public static final String TYPE_HEIGHT = "height";
    public static final String TYPE_ALPHA = "alpha";
    public static final int MAX_ALPHA = 255;
    public static final int MIN_ALPHA = 0;

    private int mOriginalWidth;
    private int mOriginalHeight;
    private int mOriginalLocationX;
    private int mOriginalLocationY;
    private int mState = STATE_NORMAL;
    private Matrix mSmoothMatrix;
    private boolean mTransformStart = false;
    private Transfrom mTransfrom;
    private final int mBgColor = 0xFF000000;
    private Paint mPaint;
    private int mBgAlpha = 0;

    public SmoothImageView(Context context) {
        this(context, null);
    }

    public SmoothImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mSmoothMatrix = new Matrix();
        mPaint = new Paint();
        mPaint.setColor(mBgColor);
        mPaint.setStyle(Style.FILL);
    }

    public void setOriginalInfo(int width, int height, int locationX, int locationY) {
        mOriginalWidth = width;
        mOriginalHeight = height;
        mOriginalLocationX = locationX;
        mOriginalLocationY = locationY;
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        java.lang.reflect.Field field = null;
        int x = 0;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
            return statusBarHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    public void transformIn() {
        mState = STATE_TRANSFORM_IN;
        mTransformStart = true;
        invalidate();
    }

    public void transformOut() {
        mState = STATE_TRANSFORM_OUT;
        mTransformStart = true;
        invalidate();
    }

    private class Transfrom {
        float startScale;
        float endScale;
        float scale;
        LocationSizeF startRect;
        LocationSizeF endRect;
        LocationSizeF rect;

        void initStartIn() {
            scale = startScale;
            try {
                rect = (LocationSizeF) startRect.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        void initStartOut() {
            scale = endScale;
            try {
                rect = (LocationSizeF) endRect.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

    }

    private void initTransform() {
        if (getDrawable() == null) {
            return;
        }
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        mTransfrom = new Transfrom();
        int width = 0;
        int height = 0;
        if(bitmap != null){
            width = bitmap.getWidth();
            height = bitmap.getHeight();
        }

        float xSScale = mOriginalWidth / ((float) width);
        float ySScale = mOriginalHeight / ((float) height);
        float startScale = xSScale > ySScale ? xSScale : ySScale;
        mTransfrom.startScale = startScale;
        float xEScale = getWidth() / ((float) width);
        float yEScale = getHeight() / ((float) height);
        float endScale = xEScale < yEScale ? xEScale : yEScale;
        mTransfrom.endScale = endScale;

        mTransfrom.startRect = new LocationSizeF();
        mTransfrom.startRect.left = mOriginalLocationX;
        mTransfrom.startRect.top = mOriginalLocationY;
        mTransfrom.startRect.width = mOriginalWidth;
        mTransfrom.startRect.height = mOriginalHeight;
        mTransfrom.endRect = new LocationSizeF();
        float bitmapEndWidth = width * mTransfrom.endScale;
        float bitmapEndHeight = height * mTransfrom.endScale;
        mTransfrom.endRect.left = (getWidth() - bitmapEndWidth) / 2;
        mTransfrom.endRect.top = (getHeight() - bitmapEndHeight) / 2;
        mTransfrom.endRect.width = bitmapEndWidth;
        mTransfrom.endRect.height = bitmapEndHeight;

        mTransfrom.rect = new LocationSizeF();
    }

    private class LocationSizeF implements Cloneable {
        float left;
        float top;
        float width;
        float height;

        @Override
        public String toString() {
            return "[left:" + left + " top:" + top + " width:" + width + " height:" + height + "]";
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

    }

    private void getBmpMatrix() {
        if (getDrawable() == null) {
            return;
        }
        if (mTransfrom == null) {
            return;
        }
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        int widht = 1;
        int height = 1;
        if (bitmap != null) {
            widht = bitmap.getWidth();
            height = bitmap.getHeight();
        }
        mSmoothMatrix.setScale(mTransfrom.scale, mTransfrom.scale);
        mSmoothMatrix.postTranslate(-(mTransfrom.scale * widht / 2 - mTransfrom.rect.width / 2),
                -(mTransfrom.scale * height / 2 - mTransfrom.rect.height / 2));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return; // couldn't resolve the URI
        }

        if (mState == STATE_TRANSFORM_IN || mState == STATE_TRANSFORM_OUT) {
            if (mTransformStart) {
                initTransform();
            }
            if (mTransfrom == null) {
                super.onDraw(canvas);
                return;
            }

            if (mTransformStart) {
                if (mState == STATE_TRANSFORM_IN) {
                    mTransfrom.initStartIn();
                } else {
                    mTransfrom.initStartOut();
                }
            }

            mPaint.setAlpha(mBgAlpha);
            canvas.drawPaint(mPaint);

            int saveCount = canvas.getSaveCount();
            canvas.save();
            getBmpMatrix();
            canvas.translate(mTransfrom.rect.left, mTransfrom.rect.top);
            canvas.clipRect(0, 0, mTransfrom.rect.width, mTransfrom.rect.height);
            canvas.concat(mSmoothMatrix);
            getDrawable().draw(canvas);
            canvas.restoreToCount(saveCount);
            if (mTransformStart) {
                mTransformStart = false;
                startTransform(mState);
            }
        } else {
            super.onDraw(canvas);
        }
    }

    private void startTransform(final int state) {
        if (mTransfrom == null) {
            return;
        }

        ValueAnimator valueAnimator = new ValueAnimator();
        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-06-11,BUG-2208330 */
        valueAnimator.setDuration(ANIMATION_DURATION);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330 */
        if (state == STATE_TRANSFORM_IN) {
            PropertyValuesHolder scaleHolder = PropertyValuesHolder.ofFloat(TYPE_SCALE, mTransfrom.startScale,
                    mTransfrom.endScale);
            PropertyValuesHolder leftHolder = PropertyValuesHolder.ofFloat(TYPE_LEFT, mTransfrom.startRect.left,
                    mTransfrom.endRect.left);
            PropertyValuesHolder topHolder = PropertyValuesHolder.ofFloat(TYPE_TOP, mTransfrom.startRect.top,
                    mTransfrom.endRect.top);
            PropertyValuesHolder widthHolder = PropertyValuesHolder.ofFloat(TYPE_WIDTH, mTransfrom.startRect.width,
                    mTransfrom.endRect.width);
            PropertyValuesHolder heightHolder = PropertyValuesHolder.ofFloat(TYPE_HEIGHT, mTransfrom.startRect.height,
                    mTransfrom.endRect.height);
            PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt(TYPE_ALPHA, MIN_ALPHA, MAX_ALPHA);
            valueAnimator.setValues(scaleHolder, leftHolder, topHolder, widthHolder, heightHolder, alphaHolder);
        } else {
            PropertyValuesHolder scaleHolder = PropertyValuesHolder.ofFloat(TYPE_SCALE, mTransfrom.endScale,
                    mTransfrom.startScale);
            PropertyValuesHolder leftHolder = PropertyValuesHolder.ofFloat(TYPE_LEFT, mTransfrom.endRect.left,
                    mTransfrom.startRect.left);
            PropertyValuesHolder topHolder = PropertyValuesHolder.ofFloat(TYPE_TOP, mTransfrom.endRect.top,
                    mTransfrom.startRect.top);
            PropertyValuesHolder widthHolder = PropertyValuesHolder.ofFloat(TYPE_WIDTH, mTransfrom.endRect.width,
                    mTransfrom.startRect.width);
            PropertyValuesHolder heightHolder = PropertyValuesHolder.ofFloat(TYPE_HEIGHT, mTransfrom.endRect.height,
                    mTransfrom.startRect.height);
            PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt(TYPE_ALPHA, MAX_ALPHA, MIN_ALPHA);
            valueAnimator.setValues(scaleHolder, leftHolder, topHolder, widthHolder, heightHolder, alphaHolder);
        }

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public synchronized void onAnimationUpdate(ValueAnimator animation) {
                mTransfrom.scale = (Float) animation.getAnimatedValue(TYPE_SCALE);
                mTransfrom.rect.left = (Float) animation.getAnimatedValue(TYPE_LEFT);
                mTransfrom.rect.top = (Float) animation.getAnimatedValue(TYPE_TOP);
                mTransfrom.rect.width = (Float) animation.getAnimatedValue(TYPE_WIDTH);
                mTransfrom.rect.height = (Float) animation.getAnimatedValue(TYPE_HEIGHT);
                mBgAlpha = (Integer) animation.getAnimatedValue(TYPE_ALPHA);
                postInvalidate(); // MODIFIED by Yaoyu.Yang, 2016-06-11,BUG-2208330
            }
        });
        valueAnimator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mTransformListener != null) {
                    mTransformListener.onTransformStart(state);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (state == STATE_TRANSFORM_IN) {
                    mState = STATE_NORMAL;
                }
                if (mTransformListener != null) {
                    mTransformListener.onTransformComplete(state);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        valueAnimator.start();
    }

    public void setOnTransformListener(TransformListener listener) {
        mTransformListener = listener;
    }

    private TransformListener mTransformListener;

    public static interface TransformListener {
        void onTransformStart(int mode);
        void onTransformComplete(int mode);
    }
}

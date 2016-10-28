/*
 * Copyright (C) 2016 sin3hz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tct.gallery3d.fastjumper;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

class LabelDrawable extends Drawable {

    private static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#00BCD4");
    private Paint mBackgroundPaint;
    private TextPaint mTextPaint;
    private int mBackgroundAlpha;
    private float mBackgroundRadius = dp2px(2);
    private int mPadding = dp2px(14);
    private int mTextSize = sp2px(14);
    private String mText;
    private Rect mTextBounds;
    private RectF mBackgroundBounds;

    private static int sp2px(int size) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, metrics);
    }

    private static int dp2px(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public LabelDrawable() {

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
       // mBackgroundPaint.setAlpha((int) (0.70 * 255));
        mBackgroundAlpha = mBackgroundPaint.getAlpha();
        mBackgroundPaint.setColor(DEFAULT_BACKGROUND_COLOR);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mTextSize);

        mTextBounds = new Rect();
        mBackgroundBounds = new RectF();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(mBackgroundBounds.left,mBackgroundBounds.top,
                 mBackgroundBounds.right,mBackgroundBounds.bottom,
                mBackgroundRadius,mBackgroundRadius,mBackgroundPaint);
        //canvas.drawRoundRect(mBackgroundBounds, mBackgroundRadius, mBackgroundRadius, mBackgroundPaint);
        if (mText == null) return;
        canvas.save();
        canvas.clipRect(mBackgroundBounds);

        //canvas.drawText(mText, mBackgroundBounds.left+dp2px(10), mBackgroundBounds.bottom-dp2px(8), mTextPaint);
        canvas.drawText(mText,
                mBackgroundBounds.left + mPadding - mTextBounds.left,
                mBackgroundBounds.top + mPadding-dp2px(2) - mTextBounds.top,
                mTextPaint);
        canvas.restore();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mBackgroundBounds.set(bounds);
    }

    @Override
    public void setAlpha(int alpha) {
        mBackgroundPaint.setAlpha((int) (alpha / 255f * mBackgroundAlpha));
        mTextPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mBackgroundPaint.setColorFilter(colorFilter);
        mTextPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setText(String text) {
        mText = text;
        if (mText != null) {
            mTextPaint.getTextBounds(text, 0, mText.length(), mTextBounds);
        } else {
            mTextBounds.setEmpty();
        }
    }

    public String ellipsizeText(float avail, String text) {
        avail -= mPadding * 2;
        return TextUtils.ellipsize(text, mTextPaint, avail, TextUtils.TruncateAt.END).toString();
    }

    @Override
    public int getIntrinsicHeight() {
        return mTextBounds.height() + mPadding * 2;
    }

    @Override
    public int getIntrinsicWidth() {
        return mTextBounds.width() + mPadding * 2;
    }
}

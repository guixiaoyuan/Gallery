/*
 * Copyright (C) 2013 The Android Open Source Project
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
/* 01/19/2015|    jialiang.ren      |      PR-904445       |[Ergo][Gallery][DEV]Gallery ergo 5.1.4*/
/*           |                      |                      | - page12 modify                      */
/* ----------|----------------------|----------------------|--------------------------------------*/
/* 05/02/2015|    jialiang.ren      |      PR-910210       |[Android5.0][Gallery_v5.1.4.1.0107.0][UI]*/
/*                                                          Some icons are missing in photo editor   */
/* ----------|----------------------|----------------------|-----------------------------------------*/
/* 03/18/2015| jian.pan1            | PR952692             |Gallery photo edit，icons exposure/vignette/graduated/etc, they are all unclear
/* ----------|----------------------|----------------------|---------------------------------------- */
/* 04/27/2015| jian.pan1            | PR950449             |[Android5.0][Gallery_v5.1.9.1.0103.0][Monitor]][Force Close]Gallery force close when clicking back key twice
/* ----------|----------------------|----------------------|----------------- */
/* 16/06/2015 |    jialiang.ren     |      PR-305070         |[Gallery]Prompt galllery stopped when edit a big picture*/
/*------------|---------------------|------------------------|--------------------------------------------------------*/

package com.tct.gallery3d.filtershow.category;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import com.tct.gallery3d.R;
import com.tct.gallery3d.filtershow.FilterShowActivity;
import com.tct.gallery3d.filtershow.imageshow.MasterImage;

public class CategoryView extends IconView
        implements View.OnClickListener, SwipableView{

    private static final String LOGTAG = "CategoryView";
    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;
    private Paint mPaint = new Paint();
    private Action mAction;
    private Paint mSelectPaint;
    CategoryAdapter mAdapter;
    private int mSelectionStroke;
    private Paint mBorderPaint;
    private int mBorderStroke;
    private float mStartTouchX = 0;
    private float mStartTouchY = 0;
    private float mDeleteSlope = 20;
    private int mSelectionColor = Color.WHITE;
    private int mSpacerColor = Color.WHITE;
    private boolean mCanBeRemoved = false;
    private long mDoubleActionLast = 0;
    private long mDoubleTapDelay = 250;
    private Bitmap mSelectedIcon;
    private boolean shadowOn = true;

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 begin
    private int position = 0;

    private static Paint paint = null;

    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-18,PR952692 begin
    private Paint mFIlterPaint = null;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-18,PR952692 end

    private Context context = null;
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 end

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 begin
    private Bitmap normalIcon = null;
    private Bitmap selectedIcon = null;
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 end

    public CategoryView(Context context) {
        super(context);
        this.context = context;
        setOnClickListener(this);
        Resources res = getResources();
        mSelectionStroke = res.getDimensionPixelSize(R.dimen.thumbnail_margin);
        mSelectPaint = new Paint();
        mSelectPaint.setStyle(Paint.Style.FILL);
        mSelectionColor = res.getColor(R.color.filtershow_category_selection);
        mSpacerColor = res.getColor(R.color.filtershow_categoryview_text);

        mSelectPaint.setColor(mSelectionColor);
        mBorderPaint = new Paint(mSelectPaint);
        mBorderPaint.setColor(Color.BLACK);
        mBorderStroke = mSelectionStroke / 3;
        mSelectedIcon = BitmapFactory.decodeResource(res,
                R.drawable.ic_edit_nofilter);

        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 begin
        if(paint == null) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(context.getResources().getDimension(R.dimen.filtershow_position_text_size));
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 end
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-18,PR952692 begin
        if (mFIlterPaint == null) {
            mFIlterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-18,PR952692 end
    }

    @Override
    public boolean isHalfImage() {
        if (mAction == null) {
            return false;
        }
        if (mAction.getType() == Action.CROP_VIEW) {
            return true;
        }
        if (mAction.getType() == Action.ADD_ACTION) {
            return true;
        }
        return false;
    }

    private boolean canBeRemoved() {
        return mCanBeRemoved;
    }

    public void setShadowOn(boolean shadowOn) {
        this.shadowOn = shadowOn;
    }

    private void drawSpacer(Canvas canvas) {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mSpacerColor);
        if (getOrientation() == CategoryView.VERTICAL) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 5, mPaint);
        } else {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 5, mPaint);
        }
    }

    @Override
    public boolean needsCenterText() {
        if (mAction != null && mAction.getType() == Action.ADD_ACTION) {
            return true;
        }
        return super.needsCenterText();
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 begin
    public void onDraw(Canvas canvas) {
        if (mAction != null) {
            if (mAction.getType() == Action.SPACER) {
                drawSpacer(canvas);
                return;
            }
            if (mAction.isDoubleAction()) {
                return;
            }
            mAction.setImageFrame(new Rect(0, 0, getWidth(), getHeight()), getOrientation());
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-27,PR950449 begin
            if (mAction.getImage() != null) {
                setBitmap(mAction.getImage());
                setEnabled(true);
            } else if (mAction.getType() == Action.ADD_ACTION) {
                if (MasterImage.getImage().getPreset() != null) {
                    setEnabled(true);
                } else {
                    setEnabled(false);
                }
            } else {
                setEnabled(false);
            }
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-27,PR950449 end
        }
        super.onDraw(canvas);

        if (mAction.getType() != Action.CROP_VIEW) {
            if (shadowOn) {
                canvas.drawARGB(128, 0, 0, 0);
            }
            if(mAdapter.getmCategory() == MainPanel.BORDERS) {
                String text = null;
                int color = 0;

                if(mAdapter.isSelected(this)) {
                    color = Color.WHITE;
                } else {
                    color = getTextColor();
                }

                if (position == 0) {
                    text = context.getResources().getString(R.string.filtershow_none);
                } else {
                    text = context.getResources().getString(R.string.filtershow_frame);
                }

                drawOutlinedText(canvas, text, color);

                if(position != 0) {
                    paint.setColor(color);
                    String positionText = String.valueOf(position);
                    canvas.drawText(positionText, (getWidth() - paint.measureText(positionText)) / 2, getHeight() >> 1, paint);
                }
            } else {
                if (mAdapter.isSelected(this)) {
                    canvas.drawBitmap(
                            mSelectedIcon,
                            new Rect(0, 0, mSelectedIcon.getWidth(), mSelectedIcon.getHeight()),
                            new Rect(getWidth() * 5 / 16,
                                    getHeight() * 4 / 16, getWidth() * 11 / 16,
                                    getWidth() * 10 / 16), mSelectPaint);

                    drawOutlinedText(canvas, getText(), Color.WHITE);
                } else {
                    drawOutlinedText(canvas, getText(), getTextColor());
                }
            }
        } else {
            //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 begin
            Bitmap icon = null;
            int color = 0;
            if(mAdapter.isSelected(this)) {
                icon = selectedIcon;
                color = Color.WHITE;
            } else {
                icon = normalIcon;
                color = getTextColor();
            }
            if(mAdapter.getmCategory() == MainPanel.FILTERS) {
                canvas.drawARGB(200, 0, 0, 0);
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-18,PR952692 begin
                canvas.drawBitmap(
                        icon,
                        getWidth() - icon.getWidth() >> 1,
                        (getHeight() - icon.getHeight() >> 1) - getHeight() / 16,
                        mFIlterPaint);
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-18,PR952692 end
            }
            drawOutlinedText(canvas, getText(), color);
            //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 end
        }
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 end

    public void setAction(Action action, CategoryAdapter adapter) {
        mAction = action;
        setText(mAction.getName());
        mAdapter = adapter;
        mCanBeRemoved = action.canBeRemoved();
        setUseOnlyDrawable(false);
        if (mAction.getType() == Action.ADD_ACTION) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.filtershow_add);
            setBitmap(bitmap);
            setUseOnlyDrawable(true);
            setText(getResources().getString(R.string.filtershow_add_button_looks));
        } else {
            setBitmap(mAction.getImage());
        }
        invalidate();
    }

    @Override
    public void onClick(View view) {
        FilterShowActivity activity = (FilterShowActivity) getContext();
        if(activity.isLoadingVisible()) return;//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-16,PR305070
        if (mAction.getType() == Action.ADD_ACTION) {
            activity.addNewPreset();
        } else if (mAction.getType() != Action.SPACER) {
            if (mAction.isDoubleAction()) {
                long current = System.currentTimeMillis() - mDoubleActionLast;
                if (current < mDoubleTapDelay) {
                    activity.showRepresentation(mAction.getRepresentation());
                }
                mDoubleActionLast = System.currentTimeMillis();
            } else {
                activity.showRepresentation(mAction.getRepresentation());
            }
            mAdapter.setSelected(this);
        } else if (mAction.getType() != Action.CROP_VIEW) {
            mAdapter.setSelected(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        FilterShowActivity activity = (FilterShowActivity) getContext();

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            activity.startTouchAnimation(this, event.getX(), event.getY());
        }
        if (!canBeRemoved()) {
            return ret;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mStartTouchY = event.getY();
            mStartTouchX = event.getX();
        }
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            setTranslationX(0);
            setTranslationY(0);
        }
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            float delta = event.getY() - mStartTouchY;
            if (getOrientation() == CategoryView.VERTICAL) {
                delta = event.getX() - mStartTouchX;
            }
            if (Math.abs(delta) > mDeleteSlope) {
                activity.setHandlesSwipeForView(this, mStartTouchX, mStartTouchY);
            }
        }
        return true;
    }

    @Override
    public void delete() {
        mAdapter.remove(mAction);
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 begin
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 end

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 begin
    public Bitmap getNormalIcon() {
        return normalIcon;
    }

    public void setNormalIcon(Bitmap normalIcon) {
        this.normalIcon = normalIcon;
    }

    public Bitmap getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(Bitmap selectedIcon) {
        this.selectedIcon = selectedIcon;
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 end
}

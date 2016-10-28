/******************************************************************************/
/*                                                               Date:12/2013 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  wen.zhuang                                                      */
/*  Email  :  wen.zhuang@tcl-mobile.com                                       */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :   DlnaService for used dlna playing movie                      */
/*  File     :                                                                */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 12/10/2013|wen.zhuang            |FR550507              |Multi screen interaction*/
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.gallery3d.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class GuideView extends FrameLayout {
    public static final int ID_TITLE_VIEW = 0;
    public static final int ID_CLING_VIEW = 1;
    public static final int ID_GUIDE_VIEW = 2;
    public static final int ID_SUBMIT_VIEW = 3;
    /*
     * Drawable mBackground; //Drawable mClingDrawable; Drawable mGuideDrawable;
     */
    Paint mCirlePaint;

    private int mCircleCentreX = 360, mCircleCentreY = 500, mCircleRadius = 100, mClingWidth = 200,
            mClingHeight = 200;
    private int mPunchThroughGraphicCenterRadius;
    private float mRevealRadius;

    public GuideView(Context context) {
        this(context, null, 0);
    }

    public GuideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /*
         * mBackground = getResources().getDrawable(R.drawable.bg_cling1);
         * //mClingDrawable = getResources().getDrawable(R.drawable.cling);
         * mGuideDrawable = getResources().getDrawable(R.drawable.ic_shake);
         */

        mCirlePaint = new Paint();
        mCirlePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        mCirlePaint.setColor(0xFFFFFF);
        mCirlePaint.setAlpha(0);

        // mRevealRadius =
        // getResources().getDimensionPixelSize(R.dimen.reveal_radius) * 1f;
        // mPunchThroughGraphicCenterRadius =
        // getResources().getDimensionPixelSize(R.dimen.clingPunchThroughGraphicCenterRadius);
        mRevealRadius = 100;
        mPunchThroughGraphicCenterRadius = 100;

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        /*
         * Bitmap b = Bitmap.createBitmap(getMeasuredWidth(),
         * getMeasuredHeight(), Config.ARGB_8888); Canvas c = new Canvas(b);
         * mBackground.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
         * mBackground.draw(c); boolean isLand = true;
         * if(getResources().getConfiguration().orientation == 1 ||
         * getResources().getConfiguration().orientation == 3){ isLand = false;
         * } if(isLand){ mCircleCentreX = 770; mCircleCentreY = 90; }else{
         * mCircleCentreX = 420; mCircleCentreY = 100; } float scale =
         * mRevealRadius / mPunchThroughGraphicCenterRadius ; mClingWidth =
         * (int) (scale * mClingDrawable.getIntrinsicWidth()); mClingHeight =
         * (int) (scale * mClingDrawable.getIntrinsicHeight());
         * c.drawCircle(mCircleCentreX, mCircleCentreY, mCircleRadius,
         * mCirlePaint); mClingDrawable.setBounds(mCircleCentreX-mClingWidth/2,
         * mCircleCentreY-mClingHeight/2, mCircleCentreX+mClingWidth/2,
         * mCircleCentreY+mClingHeight/2); mClingDrawable.draw(c);
         * canvas.drawBitmap(b, 0, 0, null); c.setBitmap(null); b = null ;
         */
        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        /*
         * double diff = Math.sqrt(Math.pow(event.getX() - mCircleCentreX, 2) +
         * Math.pow(event.getY() - mCircleCentreY, 2));
         * TctLog("DLNA","---------circleCenterX="
         * +mCircleCentreX+";-----------circleCenterY="+mCircleCentreY);
         * TctLog("DLNA"
         * ,"----------touchEventX="+event.getX()+";------------touchEventY="
         * +event.getY());
         * TctLog("DLNA","-------------------------Radius="+mRevealRadius
         * +";--------diff="+diff); TctLog("DLNA",
         * "---------------------------------------------------------------------------------------------"
         * ); if(diff < mRevealRadius){ return false; }
         */
        return true;
    }

}

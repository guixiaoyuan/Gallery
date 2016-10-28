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
/*  Author :  xihe.lu                                                         */
/*  Email  :  xihe.lu@tcl.com                                                 */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :                                                                */
/*  Labels   : OneFingerZoomer                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 12/20/2013|       xihe.lu        |       CR566425       |One finger zoom   */
/* ----------|----------------------|----------------------|----------------- */
/* 06/06/2014|       yuan.cao       |       PR-683184      |one finger zoom   */
/*           |                      |                      |image is wrong in */
/*           |                      |                      |mms               */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.gallery3d.gesture;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tct.gallery3d.R;

public class OneFingerZoomerTips {
    SharedPreferences sp;
    Context mContext;
    private WindowManager.LayoutParams mWindowParams = null;
    private WindowManager mWindowManager;
    private View mContentView = null;
    private ImageView one_finger;//[BUGFIX]-Mod by TCTNJ.yuan.cao,06/06/2014,PR-683184
    private Button done;
    private TextView tipsContent; //[BUGFIX]-Add by xuan.zhou,02/10/14,PR-599455
    private boolean isAdd;
    AnimationDrawable frameAnimation;

    public boolean isAdd() {
        return isAdd;
    }

    //[BUGFIX]-Add-BEGIN by xuan.zhou,02/10/14,PR-599455
    private boolean isCamera = false;
    public void setCamera(boolean isCameraAcitivty) {
        isCamera = isCameraAcitivty;
    }
    //[BUGFIX]-Add-END by xuan.zhou,02/10/14,PR-599455

    public OneFingerZoomerTips(Context mContext) {
        this.mContext = mContext;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mContentView = View.inflate(mContext, R.layout.tips, null);
        //[BUGFIX]-Mod-BEGIN by TCTNJ.yuan.cao,06/06/2014,PR-683184
        sp = mContext.getSharedPreferences("tips", Context.MODE_PRIVATE);
        initView();
        //[BUGFIX]-Mod-END by TCTNJ.yuan.cao
    }

    public static boolean isFirstTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences("tips", Context.MODE_PRIVATE);
        return sp.getBoolean("firstTime", true);
    }

    private void setSP() {
        SharedPreferences sp = mContext.getSharedPreferences("tips", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("firstTime", false);
        editor.commit();
    }

    private void initView() {
        one_finger = (ImageView) mContentView.findViewById(R.id.one_finger);
        done = (Button) mContentView.findViewById(R.id.done);
  //[BUGFIX]-Add-BEGIN by TCTNJ.hongda.zhu,07/08/2014,724020
      //one_finger.setBackgroundResource(R.drawable.one_finger_zoomer);
      //frameAnimation = (AnimationDrawable) one_finger.getBackground();
        frameAnimation=(AnimationDrawable)mContext.getResources().getDrawable(R.drawable.one_finger_zoomer);
        one_finger.setBackground(frameAnimation);
  //[BUGFIX]-Add-END by TCTNJ.hongda.zhu,07/08/2014,724020
        frameAnimation.start();
        done.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            //[BUGFIX]-Add-BEGIN by TCTNJ.hongda.zhu,07/08/2014,724020
                if(mContentView!=null){
                mWindowManager.removeView(mContentView);
                }
            //[BUGFIX]-Add-END by TCTNJ.hongda.zhu,07/08/2014,724020
                isAdd = false;
                setSP();
            }
        });
        //[BUGFIX]-Add-BEGIN by xuan.zhou,02/10/14,PR-599455
        tipsContent = (TextView) mContentView.findViewById(R.id.tips_content);
        //[BUGFIX]-Add-END by xuan.zhou,02/10/14,PR-599455
        mWindowManager.addView(mContentView, getWindowParams());
        isAdd = true;
    }

    //[BUGFIX]-Mod by TCTNJ.yuan.cao,06/06/2014,PR-683184
    public void onPause() {
        if (isFirstTime(mContext)) {
            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            //[BUGFIX]-Add-BEGIN by TCTNJ.hongda.zhu,07/08/2014,724020
                if(mContentView!=null){
                mWindowManager.removeView(mContentView);
                }
            //[BUGFIX]-Add-END by TCTNJ.hongda.zhu,07/08/2014,724020
                isAdd = false;
            }
        }
    }

    //[BUGFIX]-Mod by TCTNJ.yuan.cao,06/06/2014,PR-683184
    public void onResume() {
        if (isFirstTime(mContext)) {
            if (!frameAnimation.isRunning()) {
                frameAnimation.start();
                mWindowManager.addView(mContentView, getWindowParams());
                isAdd = true;
                if (isCamera) {
                    tipsContent.setText(R.string.onef_inger_zoomer_tips_camera);
                }
            }
        }
    }

    protected WindowManager.LayoutParams getWindowParams() {
        if (mWindowParams == null) {
            mWindowParams = new WindowManager.LayoutParams();
            mWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mWindowParams.format = PixelFormat.RGBA_8888;
          //[BUGFIX]-Modified by TCTNJ,qiang.ding1, 2014-11-28,PR869639 begain
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
          //[BUGFIX]-Modified by TCTNJ,qiang.ding1, 2014-11-28,PR869639 end
        }
        return mWindowParams;
    }

}

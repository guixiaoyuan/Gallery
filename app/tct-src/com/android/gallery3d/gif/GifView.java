/******************************************************************************/
/*                                                             Date:03/08/2013*/
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/* Author :  anshu.zhou                                                       */
/* Email  :  anshu.zhou@tcl.com                                               */
/* Role   :  Gallery2                                                         */
/* Reference documents :                                                      */
/* -------------------------------------------------------------------------- */
/* Comments :                                                                 */
/* File     :../tct-src/com/android/gallery3d/gif/GifView.java                */
/* Labels   :                                                                 */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* ----------|----------------------|----------------------|----------------- */
/*    date   |        Author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 08/26/2013|anshu.zhou            |FR-467310             |support gif       */
/* ----------|----------------------|----------------------|----------------- */
/* 11/06/2012|Peng.Cao              |FR-406808             |support drm       */
/* ----------|----------------------|----------------------|----------------- */
/* 06/06/2012|Peng.Cao              |FR-458910             |gif display issue */
/* ----------|----------------------|----------------------|----------------- */
/* 02/26/2014|yi.chen               |603529                |[LATAM]Animated G */
/*           |                      |                      |IF could be playe */
/*           |                      |                      |d automatically w */
/*           |                      |                      |hen enter in from */
/*           |                      |                      | Download APP     */
/* ----------|----------------------|----------------------|----------------- */
/* 12/16/2014|     peng.tian        |    PR835313          |OMA DRM image cann't be played*/
/* ----------|----------------------|----------------------|----------------- */
/* 02/06/2015|     jian.pan1        |    PR925646          |The Gallery force stop when open a gif file in the .eml file*/
/* ----------|----------------------|----------------------|----------------- */
/* 02/06/2015|dongliang.feng        |PR1013505             |[Android][Gallery_v5.1.13.1.0205.0] */
/*           |                      |                      |It need too long time to show GIF pic */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.gallery3d.gif;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
//import android.graphics.TctExtMovie;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

//[FEATURE]-Add-BEGIN by TCTNB.Peng.Cao,11/06/2012,support drm
import java.io.FileNotFoundException;
import java.io.IOException;

//import com.tct.ext.drm.TctDrmManagerClient;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
//[BUGFIX]-ADD-BEGIN by TSNJ.Peng.Tian,12/12/2014,PR835313
import android.provider.MediaStore;
import android.database.Cursor;
import com.tct.gallery3d.drm.DrmManager;
//[BUGFIX]-ADD-END by TSNJ.Peng.Tian
import com.tct.gallery3d.R;
//import android.util.TctLog;
//import com.tct.ext.drm.DrmMovie;
//[FEATURE]-Add-END by TCTNB.Peng.Cao

public class GifView extends ImageView {
    private static final String TAG = "Gallery2/GifView";
    private static final boolean DEBUG = true;

    private Movie mMovie;
    private Context mContext;
    private long mTime;
    private Uri mGifUri;
    private String mGifFilePath;
    private float mZoomValue = 1.00f;
    private int mHeight;
    private int mWidth;
    private int mMaxHeight = 356;
    private int mMaxWidth = 356;
    private int mTop = 0;
    // [BUGFIX]-Mod-BEGIN by TCTNB.yi.chen,02/26/2014,603529,
    // [LATAM]Animated GIF could be played automatically when enter in from
    // Download APP
    private static int SCREEN_HEIGHT;
    private static int SCREEN_WIDTH;

    // [BUGFIX]-Mod-END by TCTNB.yi.chen

    public GifView(Context context) {
        super(context);
        mContext = context;
        // [BUGFIX]-Mod-BEGIN by TCTNB.yi.chen,02/26/2014,603529,
        // [LATAM]Animated GIF could be played automatically when enter in from
        // Download APP
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        WindowManager manager = ((Activity) context).getWindowManager();
        Display display = manager.getDefaultDisplay();
         SCREEN_HEIGHT = display.getHeight();
         SCREEN_WIDTH = display.getWidth();
        // [BUGFIX]-Mod-END by TCTNB.yi.chen
        // TODO Auto-generated constructor stub
    }

    public void setGifFileUri(Uri uri) {
        if (mMovie != null) {
            mMovie = null;
        }

        mGifUri = uri;
        if (mGifUri != null) {
            // [BUGFIX]-Add-BEGIN by NJTS.Peng.Tian,12/16/2014,PR835313
            String filePath = DrmManager.getInstance().convertUriToPath(uri,
                    mContext);
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-06,PR925646 begin
            boolean isDrm = TextUtils.isEmpty(filePath) ? false : DrmManager
                    .getInstance().isDrm(filePath);
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-06,PR925646 end
            if (DrmManager.isDrmEnable && isDrm) {
                Log.d(TAG, "is drm .......filePath =" + filePath);
                mMovie = DrmManager.getInstance().getMovie(filePath);
            } else {
                // [BUGFIX]-Add-END by NJTS.Peng.Tian
                try {
                    InputStream file = mContext.getContentResolver()
                            .openInputStream(uri);// PR260491-ChuanCheng-001
                    byte[] b = new byte[file.available()];
                    file.read(b, 0, file.available());
                    file.close();
                    mMovie = Movie.decodeByteArray(b, 0, b.length);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long now = android.os.SystemClock.uptimeMillis();

        if (0 == mTime) {
            mTime = now;
        }
        if (mMovie != null) {
            if (mMovie.duration() > 0) {
                mMovie.setTime((int) ((now - mTime) % mMovie.duration()));
            }
            int saveCount = canvas.getSaveCount();
            canvas.save();
            float mScale = SCREEN_WIDTH / mMovie.width();
            float dy = (SCREEN_HEIGHT - mMovie.height() * mScale) / 2 - mTop;
            canvas.translate(0, dy);
            canvas.scale((float) (mScale + 0.8), (float) (mScale + 0.5));
            mMovie.draw(canvas, 0, 0);
            canvas.restoreToCount(saveCount);
            postInvalidateDelayed(100);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        // TODO Auto-generated method stub
        mTop = top;
        super.onLayout(true, left, 0, right, bottom + top);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMovie != null) {
            if (mWidth == 0 && mHeight == 0) {
                mWidth = SCREEN_WIDTH;
                mHeight = SCREEN_HEIGHT;
            }
            setMeasuredDimension(resolveSize(SCREEN_WIDTH, widthMeasureSpec),
                    resolveSize(SCREEN_HEIGHT, heightMeasureSpec));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

}

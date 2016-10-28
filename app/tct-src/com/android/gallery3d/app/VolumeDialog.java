//******************************************************************************/
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

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.MovieActivity;
import com.tct.gallery3d.app.MoviePlayer.TVState;

import android.R.layout;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.SeekBar;

public class VolumeDialog extends Dialog implements SeekBar.OnSeekBarChangeListener {

    private SeekBar seekbar;
    private Window window = null;
    private Context mContext;
    private int vTV;
    private final Handler mHandler = new Handler();
    private static final String mIdentification = "Gallery_Video";

    public VolumeDialog(Context context) {
        super(context, android.R.style.Theme_Panel);
        mContext = context;
        // TODO Auto-generated constructor stub
    }

    public void dialogshow() {
        // this.setContentView(R.layout.volume_dialog);
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.volume_dialog, null);
        this.setContentView(view);
        seekbar = (SeekBar) view.findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener(this);
        setwindow();
    }

    public void setwindow() {
        window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = null;
        lp.y = 150; // 17104970;
                    // //this.getResources().getDimensionPixelOffset(R.dimen.volume_panel_top);
        lp.width = LayoutParams.WRAP_CONTENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        lp.type = LayoutParams.TYPE_SYSTEM_ALERT;
        window.setAttributes(lp);
        window.addFlags(LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

    }

    public void setProgress(int pro) {
        if (null != seekbar) {
            seekbar.setProgress(pro);
        }
    }

    public int getProgress() {
        if (null != seekbar) {
            return seekbar.getProgress();
        } else {
            return 0;
        }
    }

    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub
        vTV = seekbar.getProgress();
        AsyncTask<Integer, Void, String> seekVolumeTask = new AsyncTask<Integer, Void, String>() {

            @Override
            protected String doInBackground(Integer... params) {
                // TODO Auto-generated method stub
                MovieActivity.dlna.mediaControlSetVolume(vTV, mIdentification);
                return "successed";
            }

            @Override
            protected void onPostExecute(String result) {
                mHandler.removeCallbacks(disPlayer);
                mHandler.postDelayed(disPlayer, 3000);
            }
        };
        seekVolumeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private Runnable disPlayer = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };
}

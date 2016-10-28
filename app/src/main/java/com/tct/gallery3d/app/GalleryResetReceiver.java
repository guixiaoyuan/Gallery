/******************************************************************************/
/*                                                               Date:12/2012 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2012 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  wen.zhuang                                                      */
/*  Email  :  wen.zhuang@tcl-mobile.com                                       */
/*  Role   :                                                                  */
/*  Reference documents : AT&T req doc                                        */
/* -------------------------------------------------------------------------- */
/*  Comments : This file is used to reset related camera configures           */
/*  File     : packages/apps/TctCamera/src/com/android/camera                 */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |     author           |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 23/09/2015| dongliang.feng       |FR1092180             |<13289Track><40.9><CDR-SEC-1320>Device wipe accounts for removable storage */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.tct.gallery3d.app;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class GalleryResetReceiver extends BroadcastReceiver {
    private final String TAG = "GalleryConfig";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();

        int count = this.getResultCode() + 1;
        this.setResultCode(count);

        Log.i(TAG,"Receive " + action);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.clear();
        editor.apply();
    }
}

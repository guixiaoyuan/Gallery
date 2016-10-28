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
/* 02/27/2014|wen.zhuang        |PR586039              |[Multi-screen interaction]*/
/*           |                  |                      |The video playing screen  */
/*           |                  |                      |exit automatically when   */
/*           |                  |                      |switch it from local to PC*/
/* ----------|------------------|----------------------|------------------------- */
/**********************************************************************************/

package com.android.gallery3d.app;

import java.util.List;

import android.R.string;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.StaticLayout;
import android.widget.RemoteViews;
import com.android.gallery3d.app.DLNAManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.widget.RemoteViews;
import com.tct.gallery3d.R;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.app.MovieActivity;
import com.tct.gallery3d.app.MoviePlayer;
import com.tct.gallery3d.app.MoviePlayer.TVState;

import com.android.gallery3d.app.PlayStateListener;
import com.android.gallery3d.app.ProgressListener;

public class DlnaService extends Service {
    public static boolean isShare = false; // Shareing
    public static long positionTV = 0;
    public static long durationTV = 0;
    public static String mUri = "";
    private static int stop = 0;
    private static PlayStateListener stateListener;
    private static ProgressListener progressListener;

    // protected static int TVstate = -1;
    public static TVState tv_state = TVState.PLAYING; // for btn of play &
                                                         // pause
    public static boolean loop = false;
    private static final Handler mHandler = new Handler();
    private static DLNAManager mDLNAManager = null;
    private Object mDLNAObject = null;
    private static final String mIdentification = "Gallery_Video";
    private static NotificationManager mNotificationManager;
    private int mServiceStartId = -1;
    private static String ns;
    private static Context thisContext;

    public static final String MOVIE_COMPLICATION = "android.jrdcom.movie.dlna.complication";
    private static String STOP_SHARE = "com.android.gallery3d.app.StopShare";
    private static String TOMOVIEACTIVITY = "com.android.gallery3d.app.toMovieActivity";
    public static String PAUSE_PLAY = "com.android.gallery3d.app.PauseOrPlay";

    public static final int TV_STATUS_OK = 0;
    public static final int TV_STATE_PLAYING = 1;
    public static final int TV_STATE_STOPPED = 2;
    public static final int TV_STATE_TRANSITIONING = 3;
    public static final int TV_STATE_PAUSED_PLAYBACK = 4;
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub

        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        Log.i("DLNA", "----------DlnaService-onStart");
        super.onStart(intent, startId);
        mDLNAObject = getSystemService("dlna");
        if (mDLNAObject != null) {
            mDLNAManager = DLNAManager.getInstance(getApplicationContext());
        }
        if (mDLNAManager != null && (mTvUri == null || !mTvUri.toString().equals(
                mDLNAManager.getPreviousFile(mIdentification)))) {
            positionTV = 0;
            durationTV = 0;
        }
        // modify end by yaping for pr550578
        thisContext = this;

        getPositionTV();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.device_add");
        filter.addAction("android.intent.device_remove");
        filter.addAction("android.intent.action.DLNA_STOP");
        filter.addAction("android.intent.action.DLNA_START");
        filter.addAction("android.intent.action.shared_inner_stop");
        registerReceiver(mdlnaReceiver, filter);
    }

    public void onCreate() {
        super.onCreate();
    }

    public static void onTVStop(Context context) {
        mUri = null;
        isShare = false;
        setCurrentDeviceName(context, android.os.Build.MODEL,
                android.os.Build.MODEL);
        Log.i("DLNA", "DlnaService onStop isShare = false");
        AsyncTask<Integer, Void, String> asy_stop = new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer... params) {
                // TODO Auto-generated method stub
                Log.i("DLNA",
                        "mDLNAManager.setCurrentDevice(null, mIdentification);");
                if (mDLNAManager != null)
                    mDLNAManager.setCurrentDevice(null, mIdentification);
                return "";
            }
        };
        asy_stop.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mHandler.removeCallbacksAndMessages(null);
        if (null != mNotificationManager)
            mNotificationManager.cancelAll();
        if (null != thisContext)
            ((Service) thisContext).stopSelf();
    }

    public static void playnext(final String u) {
        tv_state = TVState.PLAYING;
        // mHandler.removeCallbacks(mPlayState);
        stopGetPositionTV();

        AsyncTask<Integer, Void, String> asy_play = new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer... params) {
                // TODO Auto-generated method stub
                Log.i("DLNA", "---mDLNAManager.mediaControlPlayNext(...); --- " + u);
                if (mDLNAManager != null) {
                    if (u.startsWith("http") || u.startsWith("rtsp")
                            || u.startsWith("https")) {
                        mDLNAManager.mediaControlStreamPlay(u, "movie", mIdentification);
                    } else {
                        mDLNAManager.mediaControlPlayNext(u, "movie", mIdentification);
                    }
                }
                return null;
            }

            protected void onPostExecute(String result) {
                // mHandler.postDelayed(mPlayState,1000);
                getPositionTV();
                super.onPostExecute(result);
            }
        };

        asy_play.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (null != mdlnaReceiver)
            unregisterReceiver(mdlnaReceiver);
        super.onDestroy();

    }

    /*
     * private static final Runnable mPlayState = new Runnable() {
     * @Override public void run() { if(isShare && null != mDLNAManager){
     * mHandler.postDelayed(mPlayState, 400); AsyncTask<Integer, Void, String>
     * asy = new AsyncTask<Integer, Void, String>() {
     * @Override protected String doInBackground(Integer... params) { // TODO
     * Auto-generated method stub if(mDLNAManager !=null){ positionTV =
     * mDLNAManager.mediaControlGetCurPlayPosition(mIdentification);
     * if(positionTV > 0 && durationTV == 0){ durationTV =
     * mDLNAManager.mediaControlGetMediaDuration(mIdentification); }else
     * if(positionTV == 0){ durationTV = 0 ; } } return null; } protected void
     * onPostExecute(String result) { Log.v("DLNA",
     * "----------------------positionTV :" + positionTV); if (durationTV > 0 &&
     * positionTV + 1000 >= durationTV) { onCompletion(); }else if(positionTV ==
     * -2100){ Log.v("DLNA", "-----------------------2100 ------------- :" +
     * positionTV); positionTV = 0; onTVStop(thisContext); if
     * (MoviePlayer.mActivityContext != null) {
     * MoviePlayer.mActivityContext.finish(); } } super.onPostExecute(result); }
     * }; asy.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); }else{
     * Log.v("DLNA",
     * "----------------------~~~#$$$$$$$$$$$$$$$$$$$$ ------------- :" +isShare
     * + (mDLNAManager!=null)); if(thisContext != null) onTVStop(thisContext);
     * if (MoviePlayer.mActivityContext != null) {
     * MoviePlayer.mActivityContext.finish(); } } } };
     */

    public static void onCompletion() {
        // tv_state = TVState.COMPELTED;
        if (loop) {
            durationTV = 0;
            // mHandler.removeCallbacks(mPlayState);
            stopGetPositionTV();

            AsyncTask<Integer, Void, String> asy = new AsyncTask<Integer, Void, String>() {
                @Override
                protected String doInBackground(Integer... params) {
                    // TODO Auto-generated method stub

                    if (mDLNAManager != null) {
                        if (mUri.startsWith("http") || mUri.startsWith("rtsp")
                                || mUri.startsWith("https")) {
                            mDLNAManager.mediaControlStreamPlay(mUri, "movie", mIdentification);
                        } else {
                            Log.i("DLNA", "---mDLNAManager.mediaControlPlayNext(); " + mUri);
                            mDLNAManager.mediaControlPlayNext(mUri, "movie", mIdentification);
                        }
                    }
                    Log.i("DLNA", "----------------------loop :");
                    return null;
                }

                protected void onPostExecute(String result) {
                    // mHandler.post(mPlayState);
                    getPositionTV();
                    super.onPostExecute(result);
                }
            };
            asy.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i("DLNA", "Video onCompletion");
            onTVStop(thisContext);
            thisContext.sendBroadcast(new Intent(MOVIE_COMPLICATION));
            if (MoviePlayer.mActivityContext != null) {
                MoviePlayer.mActivityContext.finish();
            }
        }

    }

    private int tvState;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        //mDLNAManager = (DLNAManager) getSystemService("dlna");
        mDLNAObject = getSystemService("dlna");
        if (mDLNAObject != null) {
            mDLNAManager = DLNAManager.getInstance(getApplicationContext());
        }
        Log.i("DLNA", "-------------DlnaService   onStartCommand "
                + isEventFromMonkey() + "; mDLNAManager :" + mDLNAManager);
        mServiceStartId = startId;
        if (intent != null && !isEventFromMonkey()) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            Log.i("DLNA", "--------action :" + action);
            if (STOP_SHARE.equals(action)) {
                Log.i("DLNA", "StopDlna ");
                onTVStop(this);
                if (MoviePlayer.mActivityContext != null) {
                    MoviePlayer.mActivityContext.finish();
                }
            } else if (PAUSE_PLAY.equals(action)) {
                AsyncTask<Integer, Void, String> asy = new AsyncTask<Integer, Void, String>() {

                    @Override
                    protected String doInBackground(Integer... params) {
                        if (mDLNAManager != null) {
                            tvState = mDLNAManager.mediaControlGetPlayState(mIdentification);
                            if (tvState != TV_STATE_PLAYING) {
                                mDLNAManager.mediaControlPlay(mIdentification);
                            } else {
                                mDLNAManager.mediaControlPause(mIdentification);
                            }
                        }
                        return null;
                    }

                    protected void onPostExecute(String result) {
                        if (tvState != TV_STATE_PLAYING) {
                            tv_state = TVState.PLAYING;
                            updateNotification(null, null, null, false);
                        } else {
                            tv_state = TVState.PAUSED;
                            updateNotification(null, null, null, true);
                        }
                        super.onPostExecute(result);
                    }
                };
                asy.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        return super.onStartCommand(intent, flags, startId);

    }

    private boolean isEventFromMonkey() {
        boolean isMonkey = ActivityManager.isUserAMonkey();
        return isMonkey;
    }

    BroadcastReceiver mdlnaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.DLNA_STOP")) {
                onTVStop(thisContext);
                if (MoviePlayer.mActivityContext != null) {
                    MoviePlayer.mActivityContext.finish();
                }
                /*
                 * }else if(action.equals("android.intent.device_remove")){
                 * String uuid = intent.getStringExtra("uuid"); Log.v("DLNA",
                 * "uuid" + uuid); if
                 * (DlnaService.getCurrentDeviceUuid(context).equals(uuid)) {
                 * onTVStop(thisContext); }
                 */
            } else if (action.equals("android.intent.action.shared_inner_stop")
                    || action.equals("android.intent.device_remove")) {

                // boolean sameip = intent.getBooleanExtra("sameip", true);
                // if(true)1 phone have N apps use 1 TV;
                // if(false) N phones use 1 TV
                // Log.v("DLNA", "Service Action :" + action + ", sameip :" +
                // sameip);
                // if (sameip) {
                String identify = intent.getStringExtra("identify");
                // add start by yaping.liu for pr539858
                if (mDLNAManager == null){
                    //mDLNAManager = (DLNAManager) getSystemService("dlna");
                mDLNAObject = getSystemService("dlna");
                if (mDLNAObject != null) {
                    mDLNAManager = DLNAManager.getInstance(getApplicationContext());
                }
                }
                if (mDLNAManager == null)
                    return;
                // add end by yaping.liu for pr539858
                if (identify != null && identify.equals(mIdentification)
                        && mDLNAManager.hasConnected() && DlnaService.isShare) {
                    onTVStop(thisContext);
                    if (MoviePlayer.mActivityContext != null) {
                        MoviePlayer.mActivityContext.finish();
                    }
                }
                /*
                 * } else { onTVStop(thisContext); }
                 */

            }
        }
    };

    public static void setCurrentDeviceName(Context context, String Uuid, String name) {
        SharedPreferences sp = context.getSharedPreferences("wfd_share", Context.MODE_PRIVATE);
        sp.edit().putString("current_device_uuid", Uuid).commit();
        sp.edit().putString("current_device_name", name).commit();
    }

    public static String getCurrentDeviceUuid(Context context) {
        SharedPreferences sp = context.getSharedPreferences("wfd_share", Context.MODE_PRIVATE);
        return sp.getString("current_device_uuid", android.os.Build.MODEL);
    }

    public static String getCurrentDeviceName(Context context) {
        SharedPreferences sp = context.getSharedPreferences("wfd_share", Context.MODE_PRIVATE);
        return sp.getString("current_device_name", android.os.Build.MODEL);
    }

    public static void stopGetPositionTV() {
        // mHandler.removeCallbacks(mPlayState);
        if (null != mDLNAManager) {
            mDLNAManager.setTVListener(
                    mIdentification,
                    null, null);

        }

    }

    public static final int OPERATION_STATUS_OK = 0;
    public static final int OPERATION_STATUS_BUSY = 5;
    public static final int OPERATION_STATE_PLAYING = 1;
    public static final int OPERATION_STATE_STOPPED = 2;
    public static final int OPERATION_STATE_TRANSITIONING = 3;
    public static final int OPERATION_STATE_PAUSED_PLAYBACK = 4;

    protected static void getPositionTV() {
        Log.i("DLNA", " getPositionTV called!!!");
        // mHandler.post(mPlayState);
        stateListener = new PlayStateListener() {
            public void onPlayStateChanged(int state) {
                Log.i("DLNA", " onPlayStateChanged, state:"
                        + state);
                // TVstate = state;
                /*
                 * if( state == 2){ if(state == 2){ stop ++; if(stop == 2){
                 * Log.v("DLNA", " state is 2 more 2s, STOP"); onCompletion(); }
                 * }else{ stop = 0; } }
                 */
                switch (state) {
                    case OPERATION_STATUS_OK:
                        stop = 0;
                        break;
                    case OPERATION_STATE_PLAYING:
                        tv_state = TVState.PLAYING;
                        stop = 0;
                        break;
                    case OPERATION_STATE_STOPPED:
                        stop ++;
                        if(stop == 2){
                            Log.i("DLNA", " state is 2 more 2s, STOP");
                            onCompletion();
                        }
                        break;
                    case OPERATION_STATE_TRANSITIONING:
                        tv_state = TVState.LOADING;
                        stop = 0;
                        break;
                    case OPERATION_STATE_PAUSED_PLAYBACK:
                        tv_state = TVState.PAUSED;
                        stop = 0;
                        break;
                    case OPERATION_STATUS_BUSY:
                        stop = 0;
                        break;
                    default:
                        stop = 0;
                        // do nothing!
                }
            }

            public void onStateStart() {

            }

            public void onStateStop() {

            }
        };
        progressListener = new ProgressListener() {
            public void onProgressChanged(long p, long dur) {
                Log.i("DLNA", " onProgressChanged, p:" + p
                        + ",  dur :" + dur);
                // add by yaping for pr550578
                if (tv_state == TVState.PAUSED)
                    return;
                // Log.i("DLNA", "progressListener: " + progressListener );
                if (p > 1000 && durationTV > p) {
                    p += 1000;
                }
                positionTV = p;
                durationTV = dur;
                if (durationTV > 0 && positionTV + 1000 >= durationTV) {
                    onCompletion();
                } else if (positionTV == -2100) {
                    Log.i("DLNA", "-----------------------2100 ------------- :" + positionTV);
                    positionTV = 0;
                    onTVStop(thisContext);
                    if (MoviePlayer.mActivityContext != null) {
                        MoviePlayer.mActivityContext.finish();
                    }
                }

                // if(listener != null)
                // listener.OnProgressListener(position, duration, TVstate);
            }

            public void onSeekStart() {
            }

            public void onSeekStop() {

            }
        };

        if (isShare && null != mDLNAManager) {
            mDLNAManager.setTVListener(
                    mIdentification,
                    stateListener, progressListener);

        }

    }

    /*
     * private void setTVListener(){ Log.v("DLNA", "setTVListener , isShare : "
     * + isShare); if (isShare && null != mDLNAManager) {
     * mDLNAManager.setTVListener( mIdentification, stateListener,
     * progressListener); } }
     */

    private static String mPackName;
    private static String tv;
    private static Uri mTvUri;

    public static void updateNotification(String packName, Uri uri, String TVname,
            boolean isPause) {
        ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) MoviePlayer.mActivityContext
                .getSystemService(ns);
        if (TVname != null)
            tv = TVname;
        if (uri != null) {
            mTvUri = uri;
        }
        if (packName != null) {
            mPackName = packName;
        }
        RemoteViews views = new RemoteViews(mPackName, R.layout.dlna_statusbar);
        views.setTextViewText(R.id.txt_tv, tv);
        Intent intent;
        PendingIntent pIntent;
        // private static String STOP_SHARE =
        // "com.android.gallery3d.app.StopShare" ;
        // private static String TOMOVIEACTIVITY =
        // "com.android.gallery3d.app.toMovieActivity" ;

        intent = new Intent(TOMOVIEACTIVITY);
        intent.setDataAndType(mTvUri, "video/*");
        intent.putExtra(MovieActivity.KEY_TREAT_UP_AS_BACK, true);
        // intent.addFlags(intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (MoviePlayer.mActivityContext != null) {
            Log.i("DLNA", " intent.setClass(context, MovieActivity.class); ");
            intent.setClass(MoviePlayer.mActivityContext, MovieActivity.class);
            pIntent = PendingIntent.getActivity(MoviePlayer.mActivityContext, 0, intent, 0);
        } else {
            intent.setClass(thisContext, MovieActivity.class);
            pIntent = PendingIntent.getActivity(thisContext, 0, intent, 0);
        }
        views.setOnClickPendingIntent(R.id.into_control, pIntent);

        intent = new Intent(STOP_SHARE);
        if (MoviePlayer.mActivityContext != null) {
            intent.setClass(MoviePlayer.mActivityContext, DlnaService.class);
            pIntent = PendingIntent.getService(MoviePlayer.mActivityContext, 0, intent, 0);
        } else {
            intent.setClass(thisContext, DlnaService.class);
            pIntent = PendingIntent.getService(thisContext, 0, intent, 0);
        }

        views.setOnClickPendingIntent(R.id.stop_control, pIntent);

        intent = new Intent(PAUSE_PLAY);
        if (MoviePlayer.mActivityContext != null) {
            intent.setClass(MoviePlayer.mActivityContext, DlnaService.class);
            pIntent = PendingIntent.getService(MoviePlayer.mActivityContext, 0, intent, 0);
        } else {
            intent.setClass(thisContext, DlnaService.class);
            pIntent = PendingIntent.getService(thisContext, 0, intent, 0);
        }
        if (isPause) {
            views.setImageViewResource(R.id.btn_pause, R.drawable.dlna_stat_notif_play);
        } else {
            views.setImageViewResource(R.id.btn_pause, R.drawable.dlna_stat_notif_pause);
        }
        views.setOnClickPendingIntent(R.id.btn_pause, pIntent);

        Notification status = new Notification();
        status.contentView = views;
        status.flags |= Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_tv_screen_normal_blue_dark;
        if (MoviePlayer.mActivityContext != null) {
            status.contentIntent = PendingIntent.getService(MoviePlayer.mActivityContext, 0,
                    intent, 0);
        } else {
            status.contentIntent = PendingIntent.getService(thisContext, 0, intent, 0);
        }
        mNotificationManager.notify(1, status);
    }

}

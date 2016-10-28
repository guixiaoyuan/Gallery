/*
 * Copyright (C) 2007 The Android Open Source Project
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
/* 20/11/2014|chengqun.sun            |FR826631              |Multi screen interaction*/
            /* ----------|----------------------|----------------------|------------------*/
/* ----------|----------------------|----------------------|----------------- */
/* 26/01/2015|dongliang.feng        |PR912432              |[MMS]The phone will flash */
/*           |                      |                      |back when click "Subtitle" */
/*           |                      |                      |in video playing screen */
/* ----------|----------------------|----------------------|----------------- */
/* 29/01/2015|dongliang.feng        |PR914092              |[Wifi Display]The icon still displayed */
/*           |                      |                      |as "EM mode" when enter gallery after */
/*           |                      |                      |closed the EM mode on the notification bar */
/* ----------|----------------------|----------------------|----------------- */
/* 03/02/2015|    jialiang.ren      |      PR-919919       |[Streaming]Need to remove some interface */
/* ----------|----------------------|----------------------|-----------------------------------------*/
/* 03/02/2015|    jian.pan1         |      PR-923105       |should not support wifi_display extend mode */
/* ----------|----------------------|----------------------|-----------------------------------------*/
/* 03/12/2015|    ye.chen           |      CR-938507       |video full screen as default  */
/* ----------|----------------------|----------------------|-----------------------------------------*/
/* ----------|----------------------|----------------------|-----------------------------------------*/
/* 03/13/2015|    qiang.ding1       |      PR-949086       |[Android5.0][Gallery_v5.1.9.1.0103.0]    */
/*           |                      |                      |Subtitle is choosen after choose any file*/
/*           |                      |                      |from filemanager                         */
/* ----------|----------------------|----------------------|-----------------------------------------*/
/* 23/03/2015|dongliang.feng        |PR956459              |[Android5.0][Gallery_v5.1.9.1.0107.0] */
/*           |                      |                      |The softkey will popup when lock the */
/*           |                      |                      |playing video screen */
/* ----------|----------------------|----------------------|----------------- */
/* 04/02/2015|ye.chen               |PR916400              |[GenericApp][Gallery]MTK DRM adaptation
/* ----------|----------------------|----------------------|----------------- */
/* 21/04/2015|qiang.ding1           |PR186236              |[DRM]Expired CD/SD video still can open in Downloads/File Manager/Notification bar
/* ----------|----------------------|----------------------|----------------- */
/* 27/04/2015 |    jialiang.ren     |      PR-986309       |[HOMO][Orange][22][HLS] 1.05 - Audio Only - No default icon displayed*/
/*------------|---------------------|----------------------|---------------------------------------------------------------------*/
/* 08/05/2015 |    jialiang.ren     |      PR-997159       |[Android][Gallery_v5.1.13.1.0201.0]The video */
/*                                                          won't unlock when plugging off headset       */
/*------------|---------------------|----------------------|---------------------------------------------*/
/* 18/06/2015 |    su.jiang         |      PR-1025516      |[Android 5.1][Gallery_v5.1.13.1.0208.0]The operation bar is not in */
/*------------|---------------------|-------------------   |the middle when playing the video----------------------------------*/
/* 07/13/2015| jian.pan1            | PR1041880            |[SW][Gallery]Subtitle can not add to video
/* ----------|----------------------|----------------------|----------------- */
/* 15/07/2015 |ye.chen        |PR1044513             |[GAPP][Camera]The video plays disnormal when set it in landscape.
/* ----------|----------------------|----------------------|----------------- */
/* 17/07/2015 |    su.jiang     |      PR-1042015   |[Android5.1][Gallery_v5.1.13.1.0212.0]After mute video and played,it still display original video*/
/*------------|-----------------|-------------------|-------------------------------------------------------------------------------------------------*/
/* 21/07/2015 |    su.jiang     |      PR-1047890   |   [GAPP][Gallery][Video Player]Tip unsupported format video still can play*/
/*------------|-----------------|-------------------|---------------------------------------------------------------------------*/
/* 07/23/2015| jian.pan1            | PR1050312            |[GAPP][Gallery][Video Player]There's no message when select a subtitle file is not supported
/* ----------|----------------------|----------------------|----------------- */
/* 16/10/2015|    su.jiang     |  PR-721770    |[Android5.1][Gallery_v5.2.3.1.1.0306.0]The favorite is invalid on play video interface*/
/*-----------|-----------------|---------------|--------------------------------------------------------------------------------------*/
/* 19/10/2015|    su.jiang     |  PR-730221    |[Android 5.1][Gallery_v5.2.0.1.1.0306.0]There is a lock icon on .gif files interface*/
/*-----------|-----------------|---------------|------------------------------------------------------------------------------------*/
/* 19/10/2015|    su.jiang     |  PR-732345    |[Android 5.1][Gallery_v5.2.0.1.1.0306.0]The DRM video can be trimed*/
/*-----------|-----------------|---------------|-------------------------------------------------------------------*/
/* 26/10/2015|    su.jiang     |  PR-788215    |open a  share interface for Muvee*/
/*-----------|-----------------|---------------|---------------------------------*/
/* 28/10/2015|    su.jiang     |  PR-722285    |[Android5.1][Gallery_v5.2.3.1.1.0306.0]It exit video play interface when tap details*/
/*-----------|-----------------|---------------|------------------------------------------------------------------------------------*/
/* 11/26/2015| jian.pan1            | [ALM]Task:982555     |Boomkey video
/* ----------|----------------------|----------------------|----------------- */
/* 27/11/2015|    su.jiang     |  PR-981624    |[Android5.1][Gallery_v5.2.3.0.0310.0][Force Close]Gallery force close when tap trim icon*/
/*-----------|-----------------|---------------|----------------------------------------------------------------------------------------*/
/* 02/12/2015|    su.jiang     |  PR-1000655   |[Video Streaming][Gallery][Force Close]Gallery has stopped when press 'Detail' button during playing video streaming*/
/*-----------|-----------------|---------------|--------------------------------------------------------------------------------------------------------------------*/
/* 03/12/2015|    su.jiang     |  PR-1001422   |[Video Player]Video file can not be trimmed*/
/*-----------|-----------------|---------------|-------------------------------------------*/
/* 12/03/2015| jian.pan1       | [ALM]Defect:1020140  |[Android6.0][Gallery_v5.2.4.1.0317.0][Force Close]The galler
 * ----------|-----------------|----------------------|----------------- */
/* 04/12/2015|    su.jiang     |  PR-1018928   |[Android6.0][Gallery_v5.2.4.1.0317.0]The DRM video file has no menu*/
/*-----------|-----------------|---------------|-------------------------------------------------------------------*/
/* 04/12/2015|    su.jiang     |  PR-1034640   |[Android6.0][Gallery_v5.2.5.1.0318.0]The trimed video save to download folder after trim*/
/*-----------|-----------------|---------------|----------------------------------------------------------------------------------------*/
/* 12/07/2015| jian.pan1            | [ALM]Defect:1001131  |[BT][AVRCP]Video will not stop while play the music during video is playing.
/* ----------|----------------------|----------------------|----------------- */
/* 07/12/2015|dongliang.feng        |PR1044841             |[Gallery]Remove Wifi display feature from gallery */
/* ----------|----------------------|----------------------|----------------- */
/* 10/12/2015|dongliang.feng        |PR1044803             |[Gallery]Trim,edit and favourites can't work when view the download videos */
/* ----------|----------------------|----------------------|----------------- */
/* 12/11/2015| jian.pan1            |[ALM]Defect:1060991   |[Boom key][Gallery]Gallery force stop when press Boom key in video streaming playing screen
/* ----------|----------------------|----------------------|----------------- */
/* 12/12/2015|    su.jiang     |  PR-1134047   |[Video Streaming]Facebook icon display gray after click share when playing video streaming*/
/*-----------|-----------------|---------------|------------------------------------------------------------------------------------------*/
/* 12/14/2015| jian.pan1            |[ALM]Defect:1157514   |shloud lanuch trim for normal video and iclipedit for micro video
/* ----------|----------------------|----------------------|----------------- */
/* 17/12/2015|    su.jiang     |  PR-1060028   |[Gallery][MMS]Pop up gallery force close when view the detail of video in MMS composer*/
/*-----------|-----------------|---------------|--------------------------------------------------------------------------------------*/
/* 2015/12/26|  caihong.gu-nb  |  PR-1201923   |	[Gallery]It automatic exit video play interface after disconnect wifi.*/
/*-----------|-----------------|---------------|-----------------------------------------------------------------------------------------------------------------*/
/* 11/01/2015|    su.jiang     |  PR-1270004   |[Force close][MMS]Gallery occur force close when check video details during playback it in message.*/
/*-----------|-----------------|---------------|---------------------------------------------------------------------------------------------------*/
/* 11/01/2015|    su.jiang     |  PR-1356208   |[GAPP][Android6.0][Gallery]It have lock effect when it not have lock button.*/
/*-----------|-----------------|---------------|----------------------------------------------------------------------------*/
/* 12/01/2015|    su.jiang     |  PR-1400047   |[Gallery][Boom key]Tap back in video boom key effect，press boom key in video stop screen，it willenter into boom key effect*/
/*-----------|-----------------|---------------|----------------------------------------------------------------------------------------------------------------------------*/
/* 01/14/2016|  caihong.gu-nb  |  PR-1401316   | [Gallery][Download]Should not display the 'Share' 'Edit' and 'Cut' icons when play DRM video in Download list screen*/
/*-----------|-----------------|---------------|---------------------------------------------------------------------------------*/
/* 16/01/2015|    su.jiang     |  PR-1220379   |[Android6.0][Gallery]Exporte stop on face show interface after lock screen.*/
/*-----------|-----------------|---------------|---------------------------------------------------------------------------*/
/* 25/01/2015|    su.jiang     |  PR-1427153   |[Gallery][Func]Can not preview video in lock screen.*/
/*-----------|-----------------|---------------|----------------------------------------------------*/
/* 01/25/2016| jian.pan1            |[ALM]Defect:1452755   |[VF16667][2 - Serious][CTC][Gallery]'CR: Remove the 'sharing' screen from the Gallery
/* ----------|----------------------|----------------------|--------------------------------------------------------------- */
/* 28/01/2016|    su.jiang     |  PR-1312127   |[Video Streaming]It shows an empty screen after call interaction.*/
/*-----------|-----------------|---------------|-----------------------------------------------------------------*/
/* 01/02/2016|    su.jiang     |  PR-1541451   |[GAPP][Android6.0][Gallery]In the video playing interface , 4k video can be edited.*/
/*-----------|-----------------|---------------|-----------------------------------------------------------------------------------*/
/* 02/02/2016| jian.pan1            |[ALM]Defect:1539375   |[ERGO][Force Close][Gallery]Gallery FC when tap more in Share interface
/* ----------|----------------------|----------------------|----------------- */
/* 2016/02/17|  caihong.gu-nb  |  PR-1537839   |[GAPP][Android 6.0][Gallery][REG]It is not locked interface after locking the handset when play a video*/
/*-----------|-----------------|---------------|---------------------------------------------------------------------------------*/
/* 17/02/2016|    su.jiang     |  PR-1431083   |[GAPP][Android6.0][Gallery]The DRM video play interface display not same as preview interface.*/
/*-----------|-----------------|---------------|----------------------------------------------------------------------------------------------*/
/* 18/02/2016|    su.jiang     |  PR-1537396   |[GAPP][Android6.0][Gallery]The edit button invalid on video interface.*/
/*-----------|-----------------|---------------|----------------------------------------------------------------------*/
/* 2016/02/25|  caihong.gu-nb  |  PR-1490902   |[GAPP][Android6.0][Gallery]Boomkey prompt location is error on idol 4s*/
/*-----------|-----------------|---------------|---------------------------------------------------------------------------------*/
/* 2016/03/02|  caihong.gu-nb  |  PR-1614131   |[GAPP][Android6.0][Gallery]The‘press boom key’tips location display error*/
/*-----------|-----------------|---------------|---------------------------------------------------------------------------------*/
/* 03/07/2016| jian.pan1            |[ALM]Defect:1476906   |[Video][Radio]Radio fail to play when the the video is suspend
/* ----------|----------------------|----------------------|----------------- */
/* 03/11/2016|    su.jiang     |  PR-1783349   |[Camera]The video will play reverse when record in landscape mode.*/
/*-----------|-----------------|---------------|------------------------------------------------------------------*/

package com.tct.gallery3d.app;

import java.io.File;
import java.lang.ref.WeakReference;
import android.annotation.SuppressLint; // MODIFIED by Yaoyu.Yang, 2016-08-05,BUG-2208330
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.gallery3d.app.DLNAManager;
import com.android.gallery3d.app.DeviceInfo;
import com.android.gallery3d.app.DlnaService;
import com.android.gallery3d.ext.IMovieItem;
import com.android.gallery3d.ext.MovieItem;
import com.mediatek.omadrm.MtkDrmManager;
import com.tct.gallery3d.R;
import com.tct.gallery3d.app.MoviePlayer.TVState;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.view.TrimVideo;
import com.tct.gallery3d.common.ApiHelper;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.LocalMediaItem;
import com.tct.gallery3d.data.LocalVideo;
import com.tct.gallery3d.data.MediaDetails;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.db.DataBaseManager;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.filtershow.cache.ImageLoader;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.ui.DetailsHelper;
import com.tct.gallery3d.ui.DetailsHelper.CloseListener;
import com.tct.gallery3d.ui.DetailsHelper.DetailsSource;
import com.tct.gallery3d.util.FileManagerUtils;
import com.tct.gallery3d.util.PLFUtils;
import com.tct.gallery3d.util.ScreenUtils;
//[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
//[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
//[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
//[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction

/**
 * This activity plays a video from a specified URI.
 *
 * The client of this activity can pass a logo bitmap in the intent (KEY_LOGO_BITMAP)
 * to set the action bar logo so the playback process looks more seamlessly integrated with
 * the original activity.
 */
@SuppressLint("NewApi") // MODIFIED by Yaoyu.Yang, 2016-08-05,BUG-2208330
public class MovieActivity extends Activity implements SensorEventListener,
    MovieControllerOverlay.UpdateMenuListener{//[FEATURE]-by NJHR(chengqun.sun),
                                                                                //2014/11/20, FR-826631 Multi screen interaction
    @SuppressWarnings("unused")
    private static final String TAG = "MovieActivity";
    private static final String DLNA_TAG = "DLNA";  //[FEATURE]-by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    public static final String KEY_LOGO_BITMAP = "logo-bitmap";
    public static final String KEY_TREAT_UP_AS_BACK = "treat-up-as-back";
    public static final String KEY_MEDIA_SET_PATH = "media-set-path";
    public static final String KEY_MEDIA_ITEM_PATH = "media-item-path";
    public static final String KEY_LOCKED_CAMERA = "is-camera-review";

    private MoviePlayer mPlayer;
    private boolean mFinishOnCompletion;
    private Uri mUri;
    private boolean mTreatUpAsBack;

    //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    private static final int MSG_SENDFILE = 100;
    // private static final int MSG_CONNECTED = 101;
    private static final int MSG_GETDEVICELIST = 102;
    private static final int MSG_STOP = 103;
    private static final int MSG_PLAYSTATE = 104;
    private static final int MSG_LOADING_DIALOG = 105;
    private static final String LOCAL = android.os.Build.MODEL;

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-02-01,PR1541451 begin
    private static final int VIDEO_WIDTH_4K = 3840;
    private static final int VIDEO_HEIGHT_4K = 2160;
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-02-01,PR1541451 end

    private ProgressDialog mloadingDialog = null;
    private SensorManager mSensorMgr;
    private Context context;
    public static DLNAManager dlna;
    private Object[] mDeviceInfo = null;
    private static final int DLNA_START = 0;
    private MenuItem dlnaItem;
    private MenuItem videoLockItem;
    private MenuItem videoDelteItem;
    private MenuItem videoDetails;
    protected static Uri mDlnaUri;
    private String mCurrentDevice;
    private int mCurrentDeviceIndex = 0;
    private String mChoosedDeviceName = "";
    private MyDeviceListAdapter mAdapter;
    private AlertDialog mDeviceListDialog;
    private ListView mDeviceListView;
    private String[] Renderer_list_id;
    private String[] Renderer_list;
    AlertDialog stopShareDialog = null;
    private static final String mIdentification = "Gallery_Video";
    private String ns;
    // private NotificationManager mNotificationManager;
    //private String title;
    private SharedPreferences spre;
    private PopupWindow mShakeShareDlnaGuideWindow;
    private LinearLayout shakeLayout = null;
    private boolean isFirstTime = true;
    private boolean mIsInitActionBar = false;
    private final Handler mHandler = new Handler();
    //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    public static int mNavigationBarHeight = 0;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1025516
    private Uri mMuteOrTrimUri = null;//[BUGFIX]-add by TCTNJ,su.jiang, 2015-07-17,PR1042015
    private DataBaseManager mDataBaseManager;
    private String mVideoId = null;//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-28,PR722285
    private LocalMediaItem mItem = null;
    private DetailsHelper mDetailsHelper = null;
    private String mSrcVideoPath;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-02,PR1001422
    private Uri mFileUri;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1018928
    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 begin
    private ConnectionChangeReceiver mReceiver;
    private View mConnectionRoot;
    private boolean mIsInternetConnected;
    private ImageView mBackImageView;
    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 end
    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 begin
    private boolean isCameraReview = false;
    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 end
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setSystemUiVisibility(View rootView) {
        if (ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE) {
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459 begin
            int flag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.STATUS_BAR_HIDDEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            rootView.setSystemUiVisibility(flag);
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-23, PR956459 end
        }
    }

    private boolean isDrm; //[FEATURE]-by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    private MenuItem wfdItem;
    private int temp = 0;
    private ActionBar actionBar;
    // [FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 begin
    private boolean isAddSubtitle = false;
    private boolean isPauseAddSubtitle = false;
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 end
    private String mMimeType;
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
    private AudioManager mAudioManager;
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end

    //[BUGFIX]-Add-Begin by TSNJ,zhe.xu,2016-01-04, alm 1040157
    private TelephonyManager mTelephonyManager = null;
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-11,PR1783349 begin
    private int videoWidth;
    private int videoHeight;
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-11,PR1783349 end
    private boolean isNeedPauseVideo = true; //MODIFIED by jian.pan1, 2016-04-10,BUG-1928784

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
      //[BUGFIX]-Modify by TCTNJ, xinrong.wang, 2016-01-07, PR1107543 begin
        getActionBar().hide();
      //[BUGFIX]-Modify by TCTNJ, xinrong.wang, 2016-01-07, PR1107543 end

      //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        context = this;
        //dlna = (DLNAManager) context.getSystemService("dlna");
        dlna = DLNAManager.getInstance(getApplicationContext());
        //[BUGFIX]-Add-Begin by TSNJ,zhe.xu,2016-01-04, alm 1040157
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/14,PR1401316 begin
        DrmManager.getInstance().init(this);
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/14,PR1401316 end
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (!DlnaService.isShare) {
            DlnaService.setCurrentDeviceName(context, android.os.Build.MODEL,
                    android.os.Build.MODEL);
        } else {
            if (isDrm) {
                showFormatNotSupport();
                finish();
                return;
            }
            // modify start by yaping for pr550578
            if (mDlnaUri == null || !mDlnaUri.equals(getIntent().getData())) {
                dlnaHandler.sendEmptyMessageDelayed(MSG_LOADING_DIALOG, 100);
                mHandler.postDelayed(mCheckDLNAPlayer, 2000);
            }
            // modify end by yaping for pr550578
        }
      //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction

        setContentView(R.layout.movie_view);
        View rootView = findViewById(R.id.movie_view_root);
        // [FEATURE]-Add-BEGIN by jian.pan1,11/05/2014, For FR824779 Video subtitle
        mSubtitleText = (TextView) findViewById(R.id.SubtitleText);
        // [FEATURE]-Add-END by jian.pan1
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 begin
        mConnectionRoot = findViewById(R.id.no_connection_rl);
        mBackImageView = (ImageView)findViewById(R.id.no_connection_btn);
        mBackImageView.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNeedPauseVideo = false; //MODIFIED by jian.pan1, 2016-04-10,BUG-1928784
                finish();
            }
        });
        mConnectionRoot.setVisibility(View.GONE);
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 end
        setSystemUiVisibility(rootView);

        Intent intent = getIntent();
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 begin
        isCameraReview = intent.getBooleanExtra(KEY_LOCKED_CAMERA, false);
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 end
        initializeActionBar(intent);
        // [FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        mUri = intent.getData();
      //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        mDlnaUri = mUri;
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-21,PR1047890 begin
        if(intent != null && intent.getExtras() != null){
            //[BUGFIX]-add by TCTNJ,su.jiang, 2015-07-17,PR1042015 begin
            if(!TextUtils.isEmpty(intent.getExtras().getString(GalleryConstant.MUTE_TRIM_URI))){
                mMuteOrTrimUri = Uri.parse(intent.getExtras().getString(GalleryConstant.MUTE_TRIM_URI));
            }
            //[BUGFIX]-add by TCTNJ,su.jiang, 2015-07-17,PR1042015 end
        }
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-21,PR1047890 end
        if (mUri.toString().startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(mUri,
                        new String[] {
                            MediaStore.Video.VideoColumns.DATA
                        }, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    mDlnaUri = Uri.parse(cursor.getString(0));
                    mFileUri = mDlnaUri;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1018928
                    mSrcVideoPath = mFileUri.toString();//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-02,PR1001422
                }
            } catch (Throwable t) {
              Log.w(TAG, DLNA_TAG + "cannot get absolute path from: " + intent.getDataString(), t);
            } finally {
                if (cursor != null)
                    cursor.close();
            }

        } else if (mUri.toString().startsWith("file://")) {
            mDlnaUri = Uri.parse(mUri.getPath());
            mFileUri = mDlnaUri;//[BUGFIX]-Add by TCTNJ,su.jiang, 2016-02-18,PR1537396
            Log.i("DLNA", "MovieActivity   file-mUri:" + mUri);
            mSrcVideoPath = mFileUri.toString();
        }
        mIsInitActionBar = true;
      //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        initMovieInfo(intent);
        // [FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        mFinishOnCompletion = intent.getBooleanExtra(
                MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        mTreatUpAsBack = intent.getBooleanExtra(KEY_TREAT_UP_AS_BACK, true);//[BUGFIX]-Modify by TSNJ, chunhua.liu, 2015-12-26, defect 1239036

        // [FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        /*mPlayer = new MoviePlayer(rootView, this, intent.getData(), savedInstanceState,
                !mFinishOnCompletion) {
            @Override
            public void onCompletion() {
                if (mFinishOnCompletion) {
                    finish();
                }
            }
        };*/
        boolean flagforvideo = intent.getBooleanExtra("flagforpre", false);
        boolean flagForResume = intent.getBooleanExtra("noresume", false);
        if (flagforvideo) {
        temp = 0;
//        try {
//            temp = PhotoPage.videoPresentation.mMediaPlayer.getCurrentPosition();
//        } catch (IllegalStateException e) {
//        }

        int newTemp = intent.getIntExtra("position", 0);
        if (newTemp != 0) {
        temp = newTemp;
        }
     }

     if (flagForResume) {
          mPlayer = new MoviePlayer(rootView, this, mMovieItem,
                 savedInstanceState, !mFinishOnCompletion, false) {
               @Override
               public void onCompletion() {
                  if (mFinishOnCompletion) {
                      isNeedPauseVideo = false; //MODIFIED by jian.pan1, 2016-04-10,BUG-1928784
                      finish();
               }
            }
             //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-12, CR938507 begin
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-27,PR986309
                super.onVideoSizeChanged(mp, width, height);
                //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-11,PR1783349 begin
                videoWidth = width ;
                videoHeight = height;
                //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-11,PR1783349 end

                if(height > width){//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-28,PR1312127 begin
                    if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT != getRequestedOrientation()) {
                        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                }
                else if (height < width){//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-28,PR1312127 end
                    if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE != getRequestedOrientation()) {
                      //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-07-15, PR1044513 begin
                      //[BUGFIX]-Modify by TSNJ, chunhua.liu, 2016-01-04, defect 1251583 begin
                        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-11,PR1783349
                    }
                }
            }//[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-12, CR938507 begin
         };
     } else {
         mPlayer = new MoviePlayer(rootView, this, mMovieItem,savedInstanceState, !mFinishOnCompletion) {
            @Override
            public void onCompletion() {
                Log.d(TAG, "onCompletion");
               if (mFinishOnCompletion) {
                   /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-05,BUG-2208330*/
                   if (Build.VERSION.SDK_INT >= 24){
                       if (isInMultiWindowMode()) {
                           mPlayer.pauseVideo();
                           return;
                       }
                   }
                   /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
                   //[BUGFIX]-add by TCTNJ,su.jiang, 2015-07-17,PR1042015 begin
                   if(mMuteOrTrimUri!=null){
                    // [ALM][BUGFIX]-Add by TSNJ,chunhua.liu, 2016-01-27,Defect:1356210 begin
                       //setResult(PhotoPage.REQUEST_TRIM_MUTE, new Intent().setData(mMuteOrTrimUri));
                       Intent intent = new Intent("android.intent.action.trimvideo_play_complete");
                       intent.setData(mMuteOrTrimUri);
                       sendBroadcast(intent);
                    // [ALM][BUGFIX]-Add by TSNJ,chunhua.liu, 2016-01-27,Defect:1356210 end
                   }
                   //[BUGFIX]-add by TCTNJ,su.jiang, 2015-07-17,PR1042015 end
                   /*MODIFIED-BEGIN by jian.pan1, 2016-04-10,BUG-1928784*/
                   isNeedPauseVideo = false;
                   finish();
                   /*MODIFIED-END by jian.pan1,BUG-1928784*/
               }
            }
          //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-12, CR938507 begin
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-27,PR986309
                super.onVideoSizeChanged(mp, width, height);
                //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-11,PR1783349 begin
                videoWidth = width ;
                videoHeight = height;
                //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-11,PR1783349 end

                if(height > width){//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-28,PR1312127 begin
                    if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT != getRequestedOrientation()) {
                        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                }
                else if (height < width){//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-28,PR1312127 end
                    if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE != getRequestedOrientation()) {
                      //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-07-15, PR1044513 begin
                      //[BUGFIX]-Modify by TSNJ, chunhua.liu, 2016-01-04, defect 1251583 begin
                        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-11,PR1783349
                    }
                }
                //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1025516 begin
                boolean isAtBottom = ScreenUtils.isNavigationAtBottom(MovieActivity.this);
                if(!isAtBottom){
                    mNavigationBarHeight  = ScreenUtils.getNavigationBarHeight(MovieActivity.this);
                }
                //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1025516 begin
            }//[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-12, CR938507 begin
        };
     }
     // [FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        mDataBaseManager = ((GalleryApp)this.getApplication()).getDataBaseManager();
        //mVideoId = mUri.getLastPathSegment();
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-28,PR722285 begin
        //[BUGFIX]-Add by TSNJ,zhe.xu, 2016-01-06 alm-1190874
        if(mVideoId != null && !mVideoId.contains("[0-9]*") && mMuteOrTrimUri != null){
            mVideoId = mMuteOrTrimUri.getLastPathSegment();
        }
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-28,PR722285 end
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-16,PR721770 begin
        initLocalMediaItem();
        if(mItem != null){
            invalidateFavorite(mItem.isFavorite());
        }
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-16,PR721770 end
        obtainVideoWidthAndHeight();//[BUGFIX]-Add by TCTNJ,su.jiang, 2016-02-01,PR1541451 begin
        // [FEATURE]-Add-BEGIN by jian.pan1,11/05/2014, For FR824779 Video subtitle
        mPlayer.getMovieVideoView().setOnTimedTextListener(mOnTimedTextListener);
        // [FEATURE]-Add-END by jian.pan1

        updateVideoBottomView();//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-19,PR732345
        if (intent.hasExtra(MediaStore.EXTRA_SCREEN_ORIENTATION)) {
            int orientation = intent.getIntExtra(
                    MediaStore.EXTRA_SCREEN_ORIENTATION,
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            if (orientation != getRequestedOrientation()) {
                setRequestedOrientation(orientation);
            }
        }
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
        winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        win.setAttributes(winParams);

        // We set the background in the theme to have the launching animation.
        // But for the performance (and battery), we remove the background here.
        win.setBackgroundDrawable(null);
      //[FEATURE]-Add-BEGIN by TCTNB.ye.chen,11/17/2014,support drm
        if(mUri!=null&&DrmManager.isDrmEnable){
          //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
          //[BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-04-21,PR186236 begain
            if(DrmManager.getInstance().isDrm(ImageLoader.getFilePath(this, mUri))){
                boolean isRightValid = false;
                try {
                    isRightValid = DrmManager.getInstance().isRightsStatus(ImageLoader.getFilePath(this, mUri));
                } catch (IllegalArgumentException e) {
                    // TODO: handle exception
                }finally{
                    if (!isRightValid)
                    {
                        Toast.makeText(MovieActivity.this, R.string.drm_no_valid_right, Toast.LENGTH_SHORT).show();
                        isNeedPauseVideo = false; //MODIFIED by jian.pan1, 2016-04-10,BUG-1928784
                        finish();
                    }
                    else
                    {
                        if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                            DrmManager.getInstance().consumeRights(ImageLoader.getFilePath(this, mUri),MtkDrmManager.Action.PLAY);
                        }
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(DrmManager.DRM_TIME_OUT_ACTION);
                        this.registerReceiver(br, filter);
                    }
                }
            }
        }
        //[BUGFIX]-modify by TCTNJ,qiang.ding1, 2015-04-21,PR186236 end
        //[FEATURE]-Add-END by TCTNB.ye.chen

        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-29, PR914092 begin
        IntentFilter filter = new IntentFilter();
        filter.addAction(MOVIE_WFD_UPDATE);
        this.registerReceiver(wfdUpdateBroadcastReceiver, filter);
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-29, PR914092 end
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getApplicationContext()
                    .getSystemService(Context.AUDIO_SERVICE);
            mPlayer.setAudioManager(mAudioManager);
            mPlayer.setAudioFocusChangeListener(mOnAudioFocusChangeListener);
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 begin
        registerReceiver();
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 end
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 begin
        if (!PLFUtils.getBoolean(this, "def_gallery_custom_share_enable")) {
            registerDefaultShareReceiver();
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 end
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 begin
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOnOffReceive, screenFilter);
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 end
    }

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-29, PR914092 begin
    public static final String MOVIE_WFD_UPDATE = "Movie_Activity_WFD_Update";
    BroadcastReceiver wfdUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (wfdItem != null) {
                wfdItem.setIcon(R.drawable.wfd_em_off);
            }
        };
    };
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-29, PR914092 end

  //[FEATURE]-Add-BEGIN by TCTNB.ye.chen,11/17/2012,support drm
    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
              Toast.makeText(MovieActivity.this, R.string.drm_no_valid_right, Toast.LENGTH_SHORT).show();
        }
    };
    //[FEATURE]-Add-END by TCTNB.ye.chen

    private void setActionBarLogoFromIntent(Intent intent) {
        Bitmap logo = intent.getParcelableExtra(KEY_LOGO_BITMAP);
        if (logo != null) {
            getActionBar().setLogo(
                    new BitmapDrawable(getResources(), logo));
        }
    }

    private void initializeActionBar(Intent intent) {
        mUri = intent.getData();
        final ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            return;
        }
//        setActionBarLogoFromIntent(intent);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

//        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
//        if (title != null) {
//            actionBar.setTitle(title);
//        } else {
//            // Displays the filename as title, reading the filename from the
//            // interface: {@link android.provider.OpenableColumns#DISPLAY_NAME}.
//            AsyncQueryHandler queryHandler =
//                    new AsyncQueryHandler(getContentResolver()) {
//                @Override
//                protected void onQueryComplete(int token, Object cookie,
//                        Cursor cursor) {
//                    try {
//                        if ((cursor != null) && cursor.moveToFirst()) {
//                            String displayName = cursor.getString(0);
//
//                            // Just show empty title if other apps don't set
//                            // DISPLAY_NAME
//                            actionBar.setTitle((displayName == null) ? "" :
//                                    displayName);
//                        }
//                    } finally {
//                        Utils.closeSilently(cursor);
//                    }
//                }
//            };
//            queryHandler.startQuery(0, null, mUri,
//                    new String[] {OpenableColumns.DISPLAY_NAME}, null, null,
//                    null);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
      //[BUGFIX]-Modify by TCTNJ, xinrong.wang, 2016-01-07, PR1107543 begin
        getActionBar().show();
      //[BUGFIX]-Modify by TCTNJ, xinrong.wang, 2016-01-07, PR1107543 end

      //[FEATURE]-Add-BEGIN by ye.chen,11/19/2014,support drm
        if(mUri==null)return true;
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1018928 begin
//        if(DrmManager.isDrmEnable){
//            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-28,FR824779 begin
//            if(DrmManager.getInstance().isDrm(ImageLoader.getLocalPathFromUri(this, mUri)))
//                return true;
//            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-28,FR824779 end
//        }
        //[FEATURE]-Add-END by TCTNB.ye.chen
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1018928 end
        getMenuInflater().inflate(R.menu.movie, menu);

        // Document says EXTRA_STREAM should be a content: Uri
        // So, we only share the video if it's "content:".
//        MenuItem shareItem = menu.findItem(R.id.action_share);
//        //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
//        //if (ContentResolver.SCHEME_CONTENT.equals(mUri.getScheme())) {
//        if (ContentResolver.SCHEME_CONTENT.equals(mUri.getScheme()) && !mIsFromMms && !mIsVideoCapture) {
//        // [FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
//            shareItem.setVisible(true);
//            ((ShareActionProvider) shareItem.getActionProvider())
//                    .setShareIntent(createShareIntent());
//        } else {
//            shareItem.setVisible(false);
//        }

        // [FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-03,PR923105 begin
/*        wfdItem = menu.findItem(R.id.forwfdshow);
          if (wfdItem != null) {
            //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/7, FR-830600 TV Link
             if (GalleryReceiver.flagForPresentation != PhotoPage.WFD_MODE_NORMAL) {
                 //wfdItem.setTitle(R.string.wfd_show_off);
            	 wfdItem.setIcon(R.drawable.wfd_em_on);
             } else {
                 //wfdItem.setTitle(R.string.wfd_show_on);
                 wfdItem.setIcon(R.drawable.wfd_em_off);
             }
           //[FEATURE]-MOD-END by TCTNB(Haoli Zhang), 2014/11/7, FR-830600 TV Link
             wfdItem.setVisible(PhotoPage.displayList.size() >= 1);
          }*/
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-03,PR923105 end
         // [FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode

          //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
          dlnaItem = menu.findItem(R.id.action_dlna);
          if (dlnaItem != null) {
              dlnaItem.setIcon(DlnaService.isShare ? R.drawable.ic_tv_screen_normal_blue_dark
                      : R.drawable.ic_menu_screen_normal_holo_dark);
              dlnaItem.setVisible(dlna != null && dlna.hasConnected());
          }
          videoLockItem = menu.findItem(R.id.lockmode);
          videoDelteItem = menu.findItem(R.id.video_delete);
          videoDetails = menu.findItem(R.id.video_detail);
          //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-19,PR730221 begin
          //[BUGFIX]-Add by TCTNJ,cuihua.yang, 2015-12-26,PR1161701 begin
          //videoLockItem.setVisible(true);
          //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-02,PR1000655 begin
          if (null != mPlayer && null != mItem) {//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-17,PR1060028
              videoDelteItem.setVisible(true);
              videoDetails.setVisible(true);
          } else {
              videoDelteItem.setVisible(false);
              videoDetails.setVisible(false);
          }
          //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-01-11,PR1356208 begin
          if (mPlayer.isHlsResource()) {
              videoLockItem.setVisible(false);
          } else {
              videoLockItem.setVisible(true);
          }
          //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-01-11,PR1356208 end
          //[BUGFIX]-Add by TCTNJ,cuihua.yang, 2015-12-26,PR1161701 end
          //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-02,PR1000655 end
          //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-19,PR730221 end
          //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction

        // [FEATURE]-Add-BEGIN by jian.pan1,11/05/2014, For FR824779 Video subtitle
          // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-23,PR1050312 begin
//        MenuItem subtitleItem = menu.findItem(R.id.subtitle);
//        if(subtitleItem != null) {
//            String s = mUri.toString();
//            s = handleContentUri(s);
//            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-12-03,PR824779 begin
//            // decode special string eg.space etc.
//            s = Uri.decode(s);
//            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-12-03,PR824779 end
//            if (s.startsWith("file://")) {
//                s = s.substring(7, s.length());
//                File file = new File(s);
//                String fileName = file.getName();
//                int index = fileName.lastIndexOf(".");
//                if(index >= 0) {
//                    String namePrefix = fileName.substring(0, index);
//                    File parent = file.getParentFile();
//                    File expectedSrtFile = new File(parent, namePrefix+".srt");
//                    if(expectedSrtFile.exists() && expectedSrtFile.isFile()){
//                        subtitleItem.setChecked(true);
//                    } else {
//                        subtitleItem.setChecked(false);
//                    }
//                }
//            }
//            //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-03,PR919919 begin
//            if(mPlayer != null && mPlayer.isLocalFile()) {
//                subtitleItem.setVisible(true);
//            } else {
//                subtitleItem.setVisible(false);
//            }
//            //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-03,PR919919 end
//        }
          // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-23,PR1050312 end
        // [FEATURE]-Add-END by jian.pan1
        return true;
    }

    private Intent createShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, mUri);
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case android.R.id.home:
            isNeedPauseVideo = false; //MODIFIED by jian.pan1, 2016-04-10,BUG-1928784
            if (mTreatUpAsBack) {
                finish();
            } else {
                startActivity(new Intent(this, GalleryActivity.class));
                finish();
            }
            return true;
        case R.id.action_share:
            startActivity(Intent.createChooser(createShareIntent(),
                    getString(R.string.share)));
            return true;
        case R.id.action_dlna:
            if (isDrm) {
                showFormatNotSupport();
            } else {
                dlnaHandler.sendEmptyMessage(MSG_GETDEVICELIST);
            }
            return true;
        case R.id.video_delete:
            deleteVideo();
            return true;
        case R.id.video_detail:
            showDetails();
            return true;
        case R.id.video_wifidisplay:
            launchWifiDisplay();
            return true;
        case R.id.lockmode:
            invalidateLockMode();
            return true;
        default:
            break;
        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-03,PR923105 begin
        /*else if (id == R.id.forwfdshow) { //  [FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
                if (GalleryReceiver.flagForPresentation == PhotoPage.WFD_MODE_NORMAL) {
                 if (mPlayer.mVideoView.isPlaying()) {
                  mPlayer.mPauseVideo();
                 }
                 PhotoPage.startVideoEM(this.getApplicationContext(), mUri,
                 mPlayer.mVideoView.getCurrentPosition());
               //[FEATURE]-MOD-BEGIN by TCTNB(Haoli Zhang), 2014/11/7, FR-830600 TV Link
                 wfdItem.setIcon(R.drawable.wfd_em_on);
                 //wfdItem.setTitle(R.string.wfd_show_off);
               //[FEATURE]-MOD-END by TCTNB(Haoli Zhang), 2014/11/7, FR-830600 TV Link
                 intentForPresentation(PhotoPage.WFD_MODE_VIDEO);
                 GalleryReceiver.flagForPresentation = PhotoPage.WFD_MODE_VIDEO;
                 //Settings.System.putInt(getContentResolver(),Settings.System.TCT_WFDVIDEO, PhotoPage.WFD_MODE_VIDEO);
                 } else if (GalleryReceiver.flagForPresentation == PhotoPage.WFD_MODE_MUSIC) {
                //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/7, FR-830600 TV Link
                 //wfdItem.setTitle(R.string.wfd_show_on);
                wfdItem.setIcon(R.drawable.wfd_em_off);
                //[FEATURE]-MOD-END by TCTNB(Haoli Zhang), 2014/11/7, FR-830600 TV Link
                 intentForPresentation(PhotoPage.WFD_MODE_NORMAL);
                 GalleryReceiver.flagForPresentation = PhotoPage.WFD_MODE_NORMAL;
                 //Settings.System.putInt(getContentResolver(),Settings.System.TCT_WFDVIDEO,PhotoPage.WFD_MODE_NORMAL);
                 } else {
                 if (GalleryReceiver.flagForPresentation == PhotoPage.WFD_MODE_VIDEO) {
                 int temp = PhotoPage.videoPresentation.mMediaPlayer.getCurrentPosition();
                 mPlayer.mVideoView.seekTo(temp);
                 }
                 PhotoPage.dismissPhotoEM();
                 PhotoPage.dismissVideoEM();
               //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/7, FR-830600 TV Link
                 //wfdItem.setTitle(R.string.wfd_show_on);
                 wfdItem.setIcon(R.drawable.wfd_em_off);
                //[FEATURE]-MOD-END by TCTNB(Haoli Zhang), 2014/11/7, FR-830600 TV Link
                 intentForPresentation(PhotoPage.WFD_MODE_NORMAL);
                 GalleryReceiver.flagForPresentation = PhotoPage.WFD_MODE_NORMAL;
                 //Settings.System.putInt(getContentResolver(),Settings.System.TCT_WFDVIDEO,PhotoPage.WFD_MODE_NORMAL);
                 }
                 return true;
                 //[FEATURE]-Add-BEGIN by TCTNB.wen.zhuang,12/10/2013,FR-550507
                 }*/
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-03,PR923105 end
             // [FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
        // [FEATURE]-Add-BEGIN by jian.pan1,11/05/2014, For FR824779 Video subtitle
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-23,PR1050312 begin
//        else if(item.getItemId() == R.id.subtitle) {
//            this.mSubtitleMenuItem = item;
//            if(!item.isChecked()){
//                // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 begin
//                if (mPlayer.isHasPaused()) {
//                    isPauseAddSubtitle = true;
//                } else {
//                    isPauseAddSubtitle = false;
//                }
//                // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 end
//                selectSrtFile();
//            } else {
//                hideSubtitleText();
//                item.setChecked(false);
//            }
//        }
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-23,PR1050312 end
        // [FEATURE]-Add-END by jian.pan1
        return false;
    }

    private void deleteVideo() {
        if(mUri == null) return;
        String confirmMsg = this.getResources().getQuantityString(R.plurals.delete_selection, 1);
        new AlertDialog.Builder(this)
            .setMessage(confirmMsg)
            .setPositiveButton(R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ContentResolver contentResolver = getContentResolver();
                    Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;
                    contentResolver.delete(baseUri, "_id = ?",
                            new String[] { mVideoId });
                    finish();
                }
            })
            .setNegativeButton(R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            })
            .create().show();
    }

    // [FEATURE]-Add-BEGIN by jian.pan1,11/05/2014, For FR824779 Video subtitle
    private final static int FILE_SELECT_CODE = 123;
    private TextView mSubtitleText;
    private MenuItem mSubtitleMenuItem;

    private void selectSrtFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String supportFileManger = new FileManagerUtils(MovieActivity.this)
                .getSupportFileManager();
        intent.setPackage(supportFileManger);
        try {
            startActivityForResult(intent, FILE_SELECT_CODE);
        } catch (Exception e) {
            Log.d(TAG, "Failed to open FileManager to select subtitle file");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-19,PR824779 begin
            // mSubtitleMenuItem may be null.
          //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-03-13,PR949086 begin
          //add a judgement for the file is srt or not
            if (uri!=null&&Uri.decode(uri.toString()).endsWith(".srt")) {
                if (mSubtitleMenuItem != null) {
                    mSubtitleMenuItem.setChecked(true);
                }
            }else{
                Log.e(TAG, "is not .srt file");
            }
          //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-03-13,PR949086 end
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-19,PR824779 end
            handleSrtFile(uri);
            // If the file which be selected is not a subtitle file, mSubtitleText will
            // show original text.
            mSubtitleText.setText("");
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 begin
            isAddSubtitle = true;
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 end
        }
    }

    MediaPlayer.OnTimedTextListener mOnTimedTextListener = new MediaPlayer.OnTimedTextListener(){
        @Override
        public void onTimedText(MediaPlayer mp, TimedText text) {
            Log.i(TAG, "onTimedText--");
            if(text !=null) {
                String s = text.getText();
                if(s == null)
                    s = "";
                mSubtitleText.setText(s.trim());
            }
        }
    };

    public void hideSubtitleText() {
        mSubtitleText.setVisibility(View.GONE);
    }

    //Set the Video subtitle
    private void handleSrtFile(Uri uri) {
        mSubtitleText.setVisibility(View.VISIBLE);
        mPlayer.getMovieVideoView().setSubtitle(uri);
    }

    //If inputUri startsWith "content://", it will be converted to file
    public String handleContentUri(String inputUri) {
        if(!inputUri.startsWith("content://"))
            return inputUri;
        Uri uri = Uri.parse(inputUri);
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Video.Media.DATA};
            ContentResolver resolver = this.getContentResolver();
            cursor = resolver.query(uri, proj, null, null, null);
            int index = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(index);
            return Uri.fromFile(new File(path)).toString();
        } catch(Exception e) {
            return inputUri;
        } finally {
            if(cursor != null)
                cursor.close();
        }
    }
    // [FEATURE]-Add-END by jian.pan1

    @Override
    public void onStart() {
        //[BUGFIX]-Add-Begin by TSNJ,zhe.xu,2016-01-04, alm 1040157
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        //[BUGFIX]-Add-End by TSNJ,zhe.xu
        super.onStart();
    }

    @Override
    protected void onStop() {
        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .abandonAudioFocus(null);
        //[BUGFIX]-Add-Begin by TSNJ,zhe.xu,2016-01-04, alm 1040157
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        //[BUGFIX]-Add-End by TSNJ,zhe.xu
      //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        mPlayer.showSystemUi(false); // MODIFIED by Yaoyu.Yang, 2016-08-05,BUG-2208330
        if (mHandler != null) {
            mHandler.removeCallbacks(mCheckDLNAPlayer);
        }
        if (mloadingDialog != null && mloadingDialog.isShowing()) {
            mloadingDialog.dismiss();
        }
        if (mSensorMgr != null) {
            mSensorMgr.unregisterListener(this);
            mSensorMgr = null;
        }
        mShakeHandler.removeMessages(MSG_SHARE);
        if (!DlnaService.getCurrentDeviceName(context).equals(LOCAL)) {
            DlnaService
                    .updateNotification(
                            getPackageName(),
                            mDlnaUri,
                            mCurrentDevice,
                            dlna.mediaControlGetPlayState(mIdentification) == DlnaService.TV_STATE_PAUSED_PLAYBACK ? true
                                    : false);
        }
        if (null != mdlnaReceiver)
            unregisterReceiver(mdlnaReceiver);
      //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 begin
        //mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 end
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end

        mPlayer.onPause();
        /*MODIFIED-BEGIN by jian.pan1, 2016-04-10,BUG-1928784*/
        if (isNeedPauseVideo) {
            mPlayer.pauseVideo();//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-08,PR997159
        }
        /*MODIFIED-END by jian.pan1,BUG-1928784*/
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 begin
        if(isCameraReview){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-25,PR1427153
        }
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 end
        mPlayer.onResume();
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 begin
        if (isAddSubtitle && !isPauseAddSubtitle) {
            mPlayer.playVideo();
            isAddSubtitle = false;
        }
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-13,PR1041880 end
        //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        if (!mIsInitActionBar && !DlnaService.isShare) {
            mDlnaUri = mMovieItem.getOriginalUri();
            if (mDlnaUri.toString().startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(mDlnaUri,
                            new String[] {
                                MediaStore.Video.VideoColumns.DATA
                            }, null, null, null);
                    if (cursor != null && cursor.moveToNext()) {
                        mDlnaUri = Uri.parse(cursor.getString(0));
                    }
                } catch (Throwable t) {
                    Log.w(TAG, DLNA_TAG + "cannot get absolute path from: " + mDlnaUri, t);
                } finally {
                    if (cursor != null)
                        cursor.close();
                }

            } else if (mDlnaUri.toString().startsWith("file://")) {
                mDlnaUri = Uri.parse(mUri.getPath());
            }
        } else {
            mIsInitActionBar = false;
        }
        IntentFilter dlnaFilter = new IntentFilter();
        dlnaFilter.addAction("android.intent.device_add");
        dlnaFilter.addAction("android.intent.device_remove");
        dlnaFilter.addAction("android.intent.action.DLNA_STOP");
        dlnaFilter.addAction("android.intent.action.DLNA_START");
        dlnaFilter.addAction("android.intent.action.shared_inner_stop");
        dlnaFilter.addAction(DlnaService.MOVIE_COMPLICATION);
        registerReceiver(mdlnaReceiver, dlnaFilter);

        mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        //[BUGFIX]-Mod-BEGIN by TCTNB.chen caixia,07/09/2014,PR 723676
        mSensorMgr.registerListener(this,
                mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        //[BUGFIX]-Mod-END by TCTNB.chen caixia
        if (mDeviceListDialog != null && mDeviceListDialog.isShowing()) {
            if (dlna != null) {
                refreshDeviceList();
            } else {
                mDeviceListDialog.dismiss();
            }
        }
        //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
        super.onResume();

        //showTipsIfNeeded();
    }

    private void showTipsIfNeeded() {
        boolean plfControl = PLFUtils.getBoolean(this, "def_show_boomkey_tips");
        if(!plfControl) return;
        SharedPreferences sp = getSharedPreferences("Gallery", MODE_PRIVATE);
        boolean firstLaunch = sp.getBoolean("first_play_video", true);
        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-05,BUG-2208330*/
        if(firstLaunch && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {//[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/03/02,PR1663212
            if (Build.VERSION.SDK_INT >= 24 && isInMultiWindowMode()){
                return;
            }
            /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
            Editor editor = sp.edit();
            editor.putBoolean("first_play_video", false);
            editor.commit();
            showTips();
        }
    }

    private RelativeLayout mVideoRoot = null;
    private View mFirstPlayVideoView = null;
    private void showTips() {
        mVideoRoot = (RelativeLayout)findViewById(R.id.movie_view_root);
        mFirstPlayVideoView = LayoutInflater.from(this).inflate(R.layout.layout_tip_firstlaunch, null);
        /*MODIFIED-BEGIN by caihong.gu-nb, 2016-04-06,BUG-1913258*/
        LinearLayout tip_firstLinearLayout = (LinearLayout)mFirstPlayVideoView.findViewById(R.id.tip_firstlauncher_lr);
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        tip_firstLinearLayout.measure(w, h);
        int height = tip_firstLinearLayout.getMeasuredHeight() / 2;
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1490902 begin
        String boomkeyTop = PLFUtils.getString(MovieActivity.this, "def_JrdLauncher_boom_key_tip_padding_top");
        int boomkeyHeight = 825;
        if(!TextUtils.isEmpty(boomkeyTop)){
            boomkeyHeight = Integer.parseInt(boomkeyTop);
        }
        mFirstPlayVideoView.setPadding(0, boomkeyHeight - height, 0, 0);
        /*MODIFIED-END by caihong.gu-nb,BUG-1913258*/
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1490902 end
        mFirstPlayVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoRoot.removeView(mFirstPlayVideoView);
            }
        });
        mVideoRoot.addView(mFirstPlayVideoView);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mFirstPlayVideoView.getLayoutParams();
        lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mVideoRoot != null && mFirstPlayVideoView != null) {
                    mVideoRoot.removeView(mFirstPlayVideoView);
                }
            }
        }, 5000);
    }

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 begin
    private BroadcastReceiver defaultShareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ShareDefaultPage.ACTION_DEFAULT_SHARE.equals(action)) {
                defaultShareHandler.sendMessageDelayed(
                        defaultShareHandler.obtainMessage(0, intent), 600);
            }
        }
    };

    private Handler defaultShareHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            Intent intent = (Intent) msg.obj;
            if (intent != null) {
                Uri uri = intent.getData();
                String mimeType = intent.getType();
                Log.i(TAG, "shareIntent defaultShareReceiver uri = " + uri
                        + " mimeType = " + mimeType);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("video/*");
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share)));
            } else {
                Log.e(TAG, "defaultShareHandler intent is NULL.");
            }
        };
    };

    private void unregisterDefaultShareReceiver() {
        Log.i(TAG, "unregisterDefaultShareReceiver");
        if (defaultShareReceiver != null) {
            unregisterReceiver(defaultShareReceiver);
        }
    }
    private void registerDefaultShareReceiver() {
        Log.i(TAG, "registerDefaultShareReceiver");
        IntentFilter intentFilterShare = new IntentFilter();
        intentFilterShare.addAction(ShareDefaultPage.ACTION_DEFAULT_SHARE);
        intentFilterShare.addDataScheme("content");
        try {
            intentFilterShare.addDataType("video/*");
            intentFilterShare.addDataType("image/*");
        } catch (MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        registerReceiver(defaultShareReceiver, intentFilterShare);
    }
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 end

    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 begin
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        mReceiver = new ConnectionChangeReceiver();
        this.registerReceiver(mReceiver, filter);
    }
    private void unregisterReceiver() {
        if (mReceiver != null) {
            this.unregisterReceiver(mReceiver);
        }
    }
    public class ConnectionChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            mIsInternetConnected = mobNetInfo.isConnected() || wifiNetInfo.isConnected();
            if (mPlayer != null && !mPlayer.isLocalFile()) {
                if (!mIsInternetConnected) {
                    mPlayer.pauseVideo();
                    mConnectionRoot.setVisibility(View.VISIBLE);
                } else {
                    mConnectionRoot.setVisibility(View.GONE);
                }
            } else {
                Log.i(TAG, "onReceive() mPlayer:" + mPlayer);
            }
        }
    }

    public boolean isInternetConnected(){
        return mIsInternetConnected;
    }
    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 end
    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 begin
    private BroadcastReceiver mScreenOnOffReceive = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
                }else if(Intent.ACTION_SCREEN_OFF.equals(intent.getAction())&&isCameraReview){
                    isNeedPauseVideo = false; //MODIFIED by jian.pan1, 2016-04-10,BUG-1928784
                    finish();
                }
            }
        }
    };
    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 end
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 begin
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 begin
        int lastKnownAudioFocusState = AudioManager.AUDIOFOCUS_LOSS;
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                if(lastKnownAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
                            && mPlayer != null) {
                    mPlayer.getMovieVideoView().setAudioVolumFocus();
                } else if (lastKnownAudioFocusState == AudioManager.AUDIOFOCUS_LOSS && mPlayer != null) {
                    mPlayer.playVideo();
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
//            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mPlayer.pauseVideo();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if(mPlayer != null) {
                    mPlayer.getMovieVideoView().setAudioVolumCanDuck();
                }
                break;
            default:
                break;
            }
            lastKnownAudioFocusState = focusChange;
        }
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 end
    };
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-07,Defect:1001131 end

    //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            showFirstTime();
        }
    }
    //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPlayer.onSaveInstanceState(outState);
    }

    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/02,PR1663212 begin
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVideoRoot != null
                && mFirstPlayVideoView != null
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mVideoRoot.removeView(mFirstPlayVideoView);
        }
    }
    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/02,PR1663212 end
    @Override
    public void onDestroy() {
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-29, PR914092 begin
        if (wfdUpdateBroadcastReceiver != null) {
            this.unregisterReceiver(wfdUpdateBroadcastReceiver);
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-29, PR914092 end
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 begin
        unregisterReceiver();
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2015/12/26,PR1201923 end
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 begin
        unregisterReceiver(mScreenOnOffReceive);
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/17,PR1537839 end
        mPlayer.onDestroy();
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 begin
        if (!PLFUtils.getBoolean(this, "def_gallery_custom_share_enable")) {
            unregisterDefaultShareReceiver();
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 end
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 begin
        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 end
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mPlayer.onKeyDown(keyCode, event)
                || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mPlayer.onKeyUp(keyCode, event)
                || super.onKeyUp(keyCode, event);
    }

    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    private IMovieItem mMovieItem;
    private boolean mIsFromMms = false;
    private boolean mIsVideoCapture = false;

    private void initMovieInfo(Intent intent) {
        mIsFromMms = intent.getBooleanExtra(
                "com.android.mms.ui.MessageUtils.FROM_MMS", false);
        mIsVideoCapture = intent.getBooleanExtra("isVideoCapture", false);
        Uri original = intent.getData();
        //[FEATURE]-Add-BEGIN by TCTNB.wen.zhuang,12/10/2013,FR-550507
        if(original.toString().startsWith("content://")&&!original.toString().startsWith("content://media/")){
            //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1018928 begin
            if (DrmManager.isDrmEnable) {
                isDrm = DrmManager.getInstance().isDrm(mDlnaUri.toString());
            }
            //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1018928 end
        }else{
            if (DrmManager.isDrmEnable) {
                isDrm = DrmManager.getInstance().isDrm(mDlnaUri.toString());  //[BUGFIX]-MOD-By
                                                            //TCTNB.wen.zhuang,01/18/2014,
                                                            //PR-586698
            }

        }
        //[FEATURE]-Add-END by TCTNB.wen.zhuang
        mMimeType = intent.getType();
        // [BUGFIX]-Add by TCTNJ,hao.yin, 2016-03-04,PR1717710 begin
        mVideoId = mUri.getLastPathSegment();
        if (mMimeType == null && mVideoId.contains(":")) {
            mMimeType = mVideoId.substring(0, mVideoId.indexOf(':')) + "/*";
        }
        if (mVideoId.contains(":")) {
            mVideoId = mVideoId.substring(mVideoId.indexOf(':') + 1);
        }
        // [BUGFIX]-Add by TCTNJ,hao.yin, 2016-03-04,PR1717710 end
        mMovieItem = new MovieItem(original, mMimeType, null);
        Log.d(TAG, "initMovieInfo(" + original + ") mMovieInfo=" + mMovieItem);
    }

    public void setActionBarTitle(String title) {
        Log.d(TAG, "setActionBarTitle(" + title + ")");
        int displayOptions = ActionBar.DISPLAY_HOME_AS_UP
               | ActionBar.DISPLAY_SHOW_TITLE;
        actionBar.setDisplayOptions(displayOptions, displayOptions);
        actionBar.setLogo(R.drawable.video_app);
        if (mIsFromMms) {
            actionBar.setTitle("");
            return;
        }
        if (title != null) {
            actionBar.setTitle(title);
        }
    }

    public void actionBarHide() {
       if (actionBar != null) {
           actionBar.hide();
        }
    }

    public void actionBarShow() {
       if (actionBar != null) {
           actionBar.show();
       }
    }

    public void refreshMovieInfo(IMovieItem info) {
        mMovieItem = info;
//        setActionBarTitle(info.getTitle());
        // refreshShareProvider(info);
        Log.d(TAG, "refreshMovieInfo(" + info + ")");
    }

    private void intentForPresentation(int mode) {
        String ActionString = "com.android.gallery3d.app.forpresentation";
        Intent intent = new Intent(ActionString);
        intent.putExtra("premode", mode);
        sendBroadcast(intent);
    }

    public void seekPauseVideo() {
        mPlayer.mVideoView.seekTo(temp);
        mPlayer.mPauseVideo();
    }
    // [FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode

    //[FEATURE]-Add-BEGIN by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    private long mLastUpdateTime = -1;
    private float mLast_x, mLast_y;
    private static final int MSG_SHARE = 100;
    private long mLastShakeTime = -1;

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER /*&& dlna != null && dlna.hasConnected()
                && !isFirstTime*/) {//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-11,PR1783349
            long curTime = System.currentTimeMillis();
            long diffTime = (curTime - mLastUpdateTime);
            // only allow one update every TIME_INTERVAL(100ms).
            if (diffTime > 600) {//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-11,PR1783349
                mLastUpdateTime = curTime;
                float x = event.values[0];
                float y = event.values[1];
                float deltaX = x - mLast_x;
                float deltaY = y - mLast_y;
                // deltaX>0:shake left; deltaX<0 deltaX left
                // ignore the Z axis because we only care about the X-Y plane.
                float speed = (float) (Math.sqrt(deltaX * deltaX + deltaY * deltaY)
                        / diffTime * 10000);
                if (speed > 2000) {
                    if ((System.currentTimeMillis() - mLastShakeTime) < 1500)
                        return;
                    if (mShakeShareDlnaGuideWindow != null
                            && mShakeShareDlnaGuideWindow.isShowing())
                        return;
                    mLastShakeTime = curTime;
                    if (isDrm) {
                        showFormatNotSupport();
                    } else {
                        mShakeHandler.removeMessages(MSG_SHARE);
                        mShakeHandler.sendMessageDelayed(
                                mShakeHandler.obtainMessage(MSG_SHARE),
                                100);
                    }
                }
                mLast_x = x;
                mLast_y = y;
                //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-11,PR1783349 begin
                /*if (videoHeight < videoWidth) {
                    if (x > 0 && y < 0) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                    if (x < 0 && y < 0) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    }
                }*/
                //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-11,PR1783349 end
            }
        }

    }

    private Handler mShakeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            // We send a NEXT_BUTTON message to let it join the queue, waiting
            // for processing, just like clicking the next button.
                case MSG_SHARE: {
                    if (DlnaService.getCurrentDeviceName(context).equals(LOCAL)) {
                        // now is in local , it can share to device;
                        Log.i("DLNA", " xuxr shake  to share video playing on tv  718  ");
                        if (dlna != null) {
                            mDeviceInfo = dlna.getDevicelist();
                        }
                        if (mDeviceInfo != null && mDeviceInfo.length == 1) {
                            DeviceInfo mDlnaDeviceInfo = new DeviceInfo(mDeviceInfo[0]);
                            mCurrentDevice = mDlnaDeviceInfo.getName();
                            DlnaService.setCurrentDeviceName(context, mDlnaDeviceInfo.getUid(),
                                    mCurrentDevice);
                            mCurrentDeviceIndex = 1;
                            dlnaHandler.sendEmptyMessage(MSG_SENDFILE);
                            if (dlnaItem != null) {
                                dlnaItem.setIcon(R.drawable.ic_tv_screen_normal_blue_dark);
                            }
                        } else {
                            dlnaHandler.sendEmptyMessage(MSG_GETDEVICELIST);
                        }
                    } else if (!DlnaService.getCurrentDeviceName(context).equals(LOCAL)
                            && DlnaService.positionTV >= 0) {// stop to share
                        Log.i("DLNA", " xuxr shake  to stop video playing on tv  729  ");
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context)
                                .setMessage(R.string.stop_playing_video_on_tv)
                                .setCancelable(false)
                                .setNegativeButton(R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                stopShareDialog.dismiss();
                                                stopShareDialog.cancel();
                                                stopShareDialog = null;

                                            }
                                        })
                                .setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                mPlayer.onStopOnTV();
                                                if (dlnaItem != null) {
                                                    dlnaItem.setVisible(true);
                                                    dlnaItem.setIcon(R.drawable.ic_menu_screen_normal_holo_dark);
                                                }
                                                mCurrentDeviceIndex = 0;
                                                stopShareDialog.dismiss();
                                                stopShareDialog.cancel();
                                                stopShareDialog = null;
                                            }
                                        });
                        if (stopShareDialog == null) {
                            stopShareDialog = builder.create();
                            stopShareDialog.show();
                        }

                    }
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    };

    ProgressDialog mproDialog = null;
    boolean dialogShow = false;
    Handler dlnaHandler = new Handler() {
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            int what = msg.what;

            switch (what) {
                case MSG_SENDFILE: // send file to tv
                    context.startService(new Intent(context, DlnaService.class));
                    DlnaService.isShare = true;
                    DlnaService.mUri = mDlnaUri.toString();
                    DlnaService.tv_state = TVState.PLAYING;
                    mPlayer.onPlayOnTV();
                    dlnaHandler.sendEmptyMessage(MSG_LOADING_DIALOG);
                    AsyncTask<Integer, Void, String> asy1 = new AsyncTask<Integer, Void, String>() {
                        @Override
                        protected String doInBackground(Integer... params) {
                            if (mDeviceInfo == null || mDeviceInfo.length < mCurrentDeviceIndex - 1
                                    || mDeviceInfo[mCurrentDeviceIndex - 1] == null) {
                                Toast.makeText(context, R.string.noRenderer, Toast.LENGTH_LONG)
                                        .show();
                                return null;
                            }
                            try {
                                DeviceInfo mDlnaDeviceInfo = new DeviceInfo(mDeviceInfo[mCurrentDeviceIndex - 1]);
                                dlna.setCurrentDevice(mDlnaDeviceInfo,
                                        mIdentification);
                            } catch (Exception e) {
                                Toast.makeText(context, R.string.noRenderer, Toast.LENGTH_LONG)
                                        .show();
                                e.printStackTrace();
                                return null;
                            }
                            if (mDlnaUri.toString().startsWith("http")
                                    || mDlnaUri.toString().startsWith("rtsp")
                                    || mDlnaUri.toString().startsWith("https")) {

                                Log.i("DLNA", "---mDLNAManager.mediaControlStreamPlay(...); --- "
                                        + mDlnaUri.toString());
                                dlna.mediaControlStreamPlay(mDlnaUri.toString(), "movie",
                                        mIdentification);
                            } else {
                                Log.i("DLNA", "dlna.mediaControlPlayCurr(...); " + mDlnaUri.toString());
                                dlna.mediaControlPlayCurr(mDlnaUri.toString(), "movie", mIdentification);
                            }
                            DlnaService.tv_state = TVState.LOADING;
                            return null;
                        }

                        protected void onPostExecute(String result) {
                            // if(context != null && mloadingDialog != null)
                            // mloadingDialog.dismiss();
                            mHandler.post(mCheckDLNAPlayer);
                        }

                    };
                    asy1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;

                case MSG_GETDEVICELIST: // getDevicelist
                    if (dlna != null) {
                        mDeviceInfo = dlna.getDevicelist();
                    }
                    if (mDeviceInfo != null && mDeviceInfo.length <= 0) {
                        Toast.makeText(context,
                                context.getString(R.string.noRenderer),
                                Toast.LENGTH_SHORT).show();
                    } else if (!dialogShow) {
                        Renderer_list_id = new String[mDeviceInfo.length + 1];
                        Renderer_list = new String[mDeviceInfo.length + 1];
                        Renderer_list_id[0] = LOCAL;
                        Renderer_list[0] = LOCAL;
                        if (DlnaService.getCurrentDeviceName(context).equals(LOCAL)) {
                            mCurrentDeviceIndex = 0;
                        }
                        mCurrentDevice = DlnaService.getCurrentDeviceName(context);
                        for (int i = 1; i <= mDeviceInfo.length; i++) {
                            DeviceInfo mDlnaDeviceInfo = new DeviceInfo(mDeviceInfo[i - 1]);
                            Renderer_list_id[i] = mDlnaDeviceInfo.getUid();
                            Renderer_list[i] = mDlnaDeviceInfo.getName();
                            if (mCurrentDevice.equals(Renderer_list[i])) {
                                mCurrentDeviceIndex = i;
                            }
                        }
                        mChoosedDeviceName = mCurrentDevice;
                        mDeviceListDialog = new AlertDialog.Builder(
                                context)
                                .setTitle(R.string.selectRenderer)
                                .setCancelable(false)
                                .setNegativeButton(R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                if (dlnaItem != null)
                                                    dlnaItem.setVisible(true);
                                                dialog.dismiss();
                                                dialog.cancel();
                                                dialog = null;
                                                dialogShow = false;
                                            }
                                        })
                                .setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                if (dlnaItem != null)
                                                    dlnaItem.setVisible(true);
                                                if (mCurrentDeviceIndex == 0) {
                                                    Log.i("DLNA", " -- LOCAL---");
                                                    mPlayer.onStopOnTV();
                                                    if (dlnaItem != null) {
                                                        dlnaItem.setIcon(R.drawable.ic_menu_screen_normal_holo_dark);
                                                    }

                                                } else {
                                                    if (!mChoosedDeviceName
                                                            .equals(mCurrentDevice)) {
                                                        mCurrentDevice = mChoosedDeviceName;
                                                        DlnaService
                                                                .setCurrentDeviceName(
                                                                        context,
                                                                        Renderer_list_id[mCurrentDeviceIndex],
                                                                        mCurrentDevice);
                                                        dlnaHandler.sendEmptyMessage(MSG_SENDFILE);
                                                        dlnaItem.setIcon(R.drawable.ic_tv_screen_normal_blue_dark);
                                                    }
                                                }
                                                dialog.dismiss();
                                                dialog.cancel();
                                                dialog = null;
                                                dialogShow = false;
                                            }
                                        })
                                .create();
                        mDeviceListView = new ListView(mDeviceListDialog.getContext());
                        mAdapter = new MyDeviceListAdapter(mDeviceListDialog.getContext());
                        mDeviceListView.setAdapter(mAdapter);
                        mDeviceListView
                                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position,
                                            long id) {
                                        // TODO Auto-generated method stub
                                        mCurrentDeviceIndex = position;
                                        mChoosedDeviceName = Renderer_list[position];
                                        mAdapter.notifyDataSetChanged();
                                        // mAdapter.notifyDataSetInvalidated();
                                    }
                                });
                        mDeviceListDialog.setView(mDeviceListView);
                        if (mDeviceListDialog != null && !dialogShow) {
                            mDeviceListDialog.show();
                            dialogShow = true;
                        }
                    }
                    break;
                case MSG_LOADING_DIALOG:
                    mloadingDialog = new android.app.ProgressDialog(context,
                            AlertDialog.THEME_TRADITIONAL);
                    mloadingDialog.setCancelable(false);
                    mloadingDialog.setMessage(context.getString(R.string.wait_dialog));
                    mloadingDialog.show();
                    mCheckTime = 0;
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void refreshDeviceList() {
        mDeviceInfo = dlna.getDevicelist();
        Renderer_list_id = new String[mDeviceInfo.length + 1];
        Renderer_list = new String[mDeviceInfo.length + 1];
        Renderer_list_id[0] = LOCAL;
        Renderer_list[0] = LOCAL;
        if (DlnaService.getCurrentDeviceName(context).equals(LOCAL)) {
            mCurrentDeviceIndex = 0;
        }
        mCurrentDevice = DlnaService.getCurrentDeviceName(context);
        for (int i = 1; i <= mDeviceInfo.length; i++) {
            DeviceInfo mDlnaDeviceInfo = new DeviceInfo(mDeviceInfo[i - 1]);
            Renderer_list_id[i] = mDlnaDeviceInfo.getUid();
            Renderer_list[i] = mDlnaDeviceInfo.getName();
            if (mCurrentDevice.equals(Renderer_list[i])) {
                mCurrentDeviceIndex = i;
            }
        }
        mChoosedDeviceName = mCurrentDevice;
        mAdapter.notifyDataSetChanged();
    }

    private long mCheckTime = 0;

    private Runnable mCheckDLNAPlayer = new Runnable() {

        @Override
        public void run() {
            if (DlnaService.positionTV >= 1000 || mCheckTime >= 60000) {
                if (mloadingDialog != null && mloadingDialog.isShowing()) {
                    mloadingDialog.dismiss();
                }
            } else {
                int nowStatus = 0;
                boolean hasErrorHappen = false;
                if (dlna != null) {
                    nowStatus = dlna.mediaControlGetPlayState(mIdentification);
                }
                if (nowStatus < 0) {
                    hasErrorHappen = true;
                }
                if (hasErrorHappen) {
                    if (mloadingDialog != null && mloadingDialog.isShowing()) {
                        mloadingDialog.dismiss();
                        // modify start by yaping.liu for pr572245
                        if (mPlayer != null)
                            mPlayer.onStopOnTV();
                        if (dlnaItem != null)
                            dlnaItem.setIcon(R.drawable.ic_menu_screen_normal_holo_dark);
                        // modify end by yaping.liu for pr572245
                    }
                } else {
                    mCheckTime += 1000;
                    mHandler.postDelayed(mCheckDLNAPlayer, 1000);
                }
            }
        }

    };

    BroadcastReceiver mdlnaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("DLNA", " action :" + action);
            if (action.equals("android.intent.action.DLNA_START")
                    || action.equals("android.intent.device_add")) {
                Log.i("DLNA", " ...  DLNA_START ...........");
                /*if (!mResumed) {
                    return;
                }*/
                if (dlnaItem != null) {
                    dlnaItem.setVisible(true);
                    dlnaItem.setIcon(R.drawable.ic_menu_screen_normal_holo_dark);
                }
                showFirstTime();

            } else if (action.equals("android.intent.action.DLNA_STOP")) {
                Log.i("DLNA", " ...  DLNA_STOP  ...........");
                if (mloadingDialog != null) {
                    mloadingDialog.dismiss();
                }
                if (dlnaItem != null /*&& mResumed*/) {
                    dlnaItem.setVisible(false);
                }
                if (dlna != null && dlna.hasConnected() && DlnaService.isShare) {
                    mPlayer.onStopOnTV();
                }
                if (mDeviceListDialog != null && mDeviceListDialog.isShowing()) {
                    refreshDeviceList();
                }
            } else if (action.equals("android.intent.device_remove")) {
                if (mDeviceListDialog != null && mDeviceListDialog.isShowing()) {
                    refreshDeviceList();
                }
            }/*
              * else if(action.equals("android.intent.device_remove")){ //add by
              * xuxr@tcl.com 20130311 String uuid =
              * intent.getStringExtra("uuid"); Log.i("DLNA", "uuid" + uuid); if
              * (DlnaService.getCurrentDeviceUuid(context).equals(uuid)) { if
              * (dlnaItem != null) { dlnaItem.setVisible(false); } if
              * (dlna.hasConnected() && DlnaService.isShare) {
              * mPlayer.onStopOnTV(); } } }
              */else if (action.equals("android.intent.action.shared_inner_stop")
                    || action.equals("android.intent.device_remove")) {
                Log.i("DLNA", "Activity Action :" + action);
                if (mloadingDialog != null) {
                    mloadingDialog.dismiss();
                }
                // boolean sameip = intent.getBooleanExtra("sameip", true);
                // if(true)1 phone have N apps use 1 TV;
                // if(false) N phones use 1 TV
                // Log.i("DLNA", "Activity Action :" + action + ", sameip :" +
                // sameip);
                // if (sameip) {
                String identify = intent.getStringExtra("identify");
                if (identify != null && identify.equals(mIdentification) && dlna != null
                        && dlna.hasConnected() && DlnaService.isShare) {
                    mPlayer.onStopOnTV();
                    /*if (mResumed)*/
                    if (dlnaItem != null) {
                        dlnaItem.setIcon(R.drawable.ic_menu_screen_normal_holo_dark);
                    }
                }
                if (mDeviceListDialog != null && mDeviceListDialog.isShowing()) {
                    refreshDeviceList();
                }
                /*
                 * } else { if(dlna.hasConnected() && DlnaService.isShare){
                 * mPlayer.onStopOnTV();
                 * dlnaItem.setIcon(R.drawable.ic_menu_screen_normal_holo_dark);
                 * } }
                 */
            } else if (DlnaService.MOVIE_COMPLICATION.equals(action)) {
                /*if (mResumed)*/ {
                    isNeedPauseVideo = false; //MODIFIED by jian.pan1, 2016-04-10,BUG-1928784
                    finish();
                }
            }
        }
    };

    /*
     * private void setCurrentDeviceName(String name) { mCurrentDevice = name;
     * SharedPreferences sp = context.getSharedPreferences("wfd_share", 1);
     * sp.edit().putString("current_device_name", name).commit(); } private
     * String getCurrentDeviceName() { SharedPreferences sp =
     * context.getSharedPreferences("wfd_share", 1); return
     * sp.getString("current_device_name", LOCAL); }
     */

    private class MyDeviceListAdapter extends BaseAdapter {
        private Context mContext;
        private int nowSelectedPosition = 0;

        public MyDeviceListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return Renderer_list.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        private class MyHolder {
            ImageView mIcon;
            TextView mDeviceName;
            RadioButton mRadioButton;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            MyHolder mHolder;
            if (convertView == null) {
                mHolder = new MyHolder();
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.dlna_device_list_view_item, null);
                mHolder.mIcon = (ImageView) convertView.findViewById(R.id.type_icon);
                mHolder.mDeviceName = (TextView) convertView.findViewById(R.id.tv_device_name);
                mHolder.mRadioButton = (RadioButton) convertView.findViewById(R.id.rb_device);
                convertView.setTag(mHolder);
            } else {
                mHolder = (MyHolder) convertView.getTag();
            }
            String name = Renderer_list[position];
            if (position == 0) {
                mHolder.mIcon.setImageResource(R.drawable.ic_menu_phone);
            } else {
                mHolder.mIcon.setImageResource(R.drawable.ic_menu_tv);
            }
            mHolder.mDeviceName.setText(name);
            nowSelectedPosition = position;
            mHolder.mRadioButton.setChecked(position == mCurrentDeviceIndex);

            return convertView;
        }

    }

    private void showFirstTime() {
        Log.i("DLNA", " --- onWindowFocusChanged --- hasFocus  ");

        SharedPreferences sp = getSharedPreferences("shake", Activity.MODE_PRIVATE);
        isFirstTime = sp.getBoolean("video_first_time", true);

        if (isFirstTime && dlna != null && dlna.hasConnected()) {
            View popupView = getLayoutInflater().inflate(R.layout.dlna_shake_share_guide, null);
            TextView shake_text = (TextView) popupView.findViewById(R.id.shake_text);
            shake_text.setText(R.string.shake_video_text);
            popupView.findViewById(R.id.btn_guide_dismiss).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            if (mShakeShareDlnaGuideWindow != null
                                    && mShakeShareDlnaGuideWindow.isShowing()) {
                                SharedPreferences sp = getSharedPreferences("shake", Activity.MODE_PRIVATE);
                                sp.edit().putBoolean("video_first_time", false).commit();
                                mShakeShareDlnaGuideWindow.dismiss();
                                mShakeShareDlnaGuideWindow = null;
                            }
                        }
                    });

            //[BUGFIX]-Mod-BEGIN by TCTNB.wen.zhuang,03/03/2014,PR-609433
            if (mShakeShareDlnaGuideWindow == null) {
                mShakeShareDlnaGuideWindow = new PopupWindow(popupView,
                        WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT,
                        true);
            }
            if (!mShakeShareDlnaGuideWindow.isShowing()) {
            //[BUGFIX]-Mod-END by TCTNB.wen.zhuang
                Log.i("DLNA", " --- showAtLocation --- ");
                mShakeShareDlnaGuideWindow.showAtLocation(
                        getLayoutInflater().inflate(R.layout.movie_view, null), Gravity.CENTER, 0,
                        0);
            }
        }

    }

    WeakReference<Toast> mShowFormatNotSupport = null;

    private void showFormatNotSupport() {
        Toast toast;
        if (mShowFormatNotSupport != null) {
            toast = mShowFormatNotSupport.get();
            if (toast != null) {
                toast.show();
                return;
            }
        }

        toast = Toast.makeText(MovieActivity.this, R.string.dlna_format_not_support,
                Toast.LENGTH_LONG);
        mShowFormatNotSupport = new WeakReference<Toast>(toast);
        toast.show();
    }
    //[FEATURE]-Add-END by NJHR(chengqun.sun), 2014/11/20, FR-826631 Multi screen interaction

    @Override
    public void updateMenu(int imageResource, boolean enable) {
//        if(isDrm) return;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-19,PR732345
        videoLockItem.setIcon(imageResource);
        ActionBar actionBar = getActionBar();
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-02,PR1000655 begin
        if(enable){//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-17,PR1060028
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            //[BUGFIX]-Modify by TCTNJ,JUN.XIE-NB, 2015-12-02,ALM-1293797 begin
            actionBar.setDisplayHomeAsUpEnabled(true);
            //[BUGFIX]-Modify by TCTNJ,JUN.XIE-NB, 2015-12-02,ALM-1293797 end
            if(mPlayer.isLocalFile() && mItem != null) {
                videoDelteItem.setVisible(true);
                videoDetails.setVisible(true);
            } else {
                videoDelteItem.setVisible(false);
                videoDetails.setVisible(false);
            }
        }else{
            actionBar.setHomeAsUpIndicator(null);
            //[BUGFIX]-Modify by TCTNJ,JUN.XIE-NB, 2015-12-02,ALM-1293797 begin
            actionBar.setDisplayHomeAsUpEnabled(false);
            //[BUGFIX]-Modify by TCTNJ,JUN.XIE-NB, 2015-12-02,ALM-1293797 end
            videoDelteItem.setVisible(false);
            videoDetails.setVisible(false);
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-02,PR1000655 end
     }

    @Override
    public void onBottomControlClicked(int control) {
        switch (control) {
        case R.id.video_share:
            mPlayer.onPause();
            launchVideoShare();
            break;
        case R.id.video_edit:
            launchVideoEdit();
            break;
        case R.id.video_trim:
            launchVideoTrim();
            break;
        case R.id.video_favourite:
            if(mItem == null) return;
            mItem.toogleFavorite();
            invalidateFavorite(mItem.mIsFavorite);
            break;
        default:
            break;
        }
    }

    private void launchWifiDisplay() {
        try {
            Intent in = new Intent();
            in.setClassName("com.android.settings", "com.android.settings.wfd.WifiDisplayEnableActivity");
            in.setAction(Intent.ACTION_SEND);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-02-01,PR1541451 begin
    private void obtainVideoWidthAndHeight(){
        if (mItem != null && mPlayer != null) {
            int videoWidth = mItem.getWidth();
            int videoHeight = mItem.getHeight();
            boolean is4KVideo = videoWidth >= VIDEO_WIDTH_4K && videoHeight >= VIDEO_HEIGHT_4K;
            mPlayer.set4KVideo(is4KVideo);
        }
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-02-01,PR1541451 end

    private void launchVideoShare() {
        // [ALM][CR]-Add by TCTNJ,jian.pan1, 2016-01-25,Defect:1452755 begin
        if (PLFUtils.getBoolean(MovieActivity.this, "def_gallery_custom_share_enable")) {
            Intent intent = new Intent(this,ShareDefaultPage.class);
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-26,PR788215 begin
            intent.putExtra(Intent.EXTRA_STREAM, mUri);
//            intent.putExtra(ShareDefaultPage.GALLERY_SHARE, true);//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-16,PR1220379
            intent.setType("video/*");
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-26,PR788215 end
            startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, mUri);
            intent.setType("video/*");
            startActivity(Intent.createChooser(intent, this.getResources()
                    .getString(R.string.share)));
        }
        // [ALM][CR]-Add by TCTNJ,jian.pan1, 2016-01-25,Defect:1452755 end
    }

    private void launchVideoTrim() {
        if (mItem == null) return;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-27,PR864859
        if (mUri == null) return;
        Intent intent = new Intent(this, TrimVideo.class);
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-02,PR1001422 begin
        if (mUri.toString().startsWith("content://") && null != mSrcVideoPath) {
            intent.setData(mUri);//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1034640
            intent.putExtra(KEY_MEDIA_ITEM_PATH, mSrcVideoPath);
        } else {
            intent.setData(parseUriFromPath(mUri));//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1034640
            intent.putExtra(KEY_MEDIA_ITEM_PATH,mUri.getEncodedPath());
        }
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-12-10, PR1044803
        startActivity(intent);
        isNeedPauseVideo = false; //MODIFIED by jian.pan1, 2016-04-10,BUG-1928784
        finish();
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-02,PR1001422 end
    }

    private void launchVideoEdit() {
        if (mItem == null)
            return;
        Intent intent = new Intent();
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-14,Defect:1157514 begin
        Path path = mItem.getPath();
        int userCommentType = ExifInfoFilter.NONE;
        if (path != null) {
            String id = path.getSuffix();
            userCommentType = ExifInfoFilter.getInstance(this).queryType(id);
        } else {
            Log.e(TAG, "launchEditor() path is NULL.");
        }
        Log.i(TAG, "launchEditor() userCommentType = " + userCommentType);
        if (userCommentType == ExifInfoFilter.MICROVIDEO) {
            intent.setAction("com.muvee.iclipeditor.EDIT");
        } else {
            intent.setAction("com.muvee.iclipeditor.TRIM");
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-14,Defect:1157514 end
        Uri uri = Uri.fromFile(new File(mItem.getFilePath()));
        intent.setDataAndType(uri, mMimeType);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-12-10, PR1044803
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-15,PR1694257 begin
        if (PLFUtils.getBoolean(this,"def_gallery_custom_share_enable")) {
            intent.putExtra("com.muvee.share.usecustom", true);
        } else {
            intent.putExtra("com.muvee.share.usecustom", false);
        }
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-15,PR1694257 end
        if (this.getPackageManager()
                .queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            startActivity(intent);
        } else {
            Log.e(TAG, "Can't find muvee App.");
        }
    }

    private void invalidateFavorite(boolean isFavorite){
        mPlayer.invalidateFavourite(isFavorite);
    }

    private void invalidateLockMode(){
        mPlayer.invalidateLockMode();
    }

    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-17,PR1060028 begin
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-19,PR732345 begin
    private void updateVideoBottomView(){
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-12,PR1134047 begin
        if (mPlayer == null) return;
        if (mItem == null) {
            mPlayer.updateVideoBottom(null,mPlayer.isLocalFile(),true);
        } else {
            mPlayer.updateVideoBottom((LocalVideo)mItem,mPlayer.isLocalFile(),false);
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-12,PR1134047 end
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-19,PR732345 end
    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-17,PR1060028 end

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-16,PR721770 begin
    private void initLocalMediaItem(){
        Path path = Path.fromString("/local/video/item");
        DataManager data = DataManager.from(this);
        Path childPath = path.getChild(mVideoId);
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1018928 begin
        String strUri = mUri.toString();
        if (strUri.startsWith("content://downloads/") && mFileUri != null) {
            Uri realUri = parseUriFromPath(mFileUri);
            Log.d(TAG, "realUri = " + realUri);
            mVideoId = realUri.getLastPathSegment();
            childPath = path.getChild(mVideoId);
        }
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1018928 end
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-02-18,PR1537396 begin
        if (strUri.startsWith("file://") && mFileUri != null){
            Uri realUri = parseUriFromPath(mFileUri);
            mVideoId = realUri.getLastPathSegment();
            childPath = path.getChild(mVideoId);
        }
        //[BUGFIX]-add by TCTNJ,su.jiang, 2016-02-18,PR1537396 end
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-11,PR1270004 begin
        if (strUri.startsWith("content://mms/")) {
            mItem = null;
        } else {
            mItem = (LocalMediaItem) data
                    .getMediaObject(childPath);
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-11,PR1270004 end
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-16,PR721770 end

    private void showDetails(){
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(this, new MyDetailsSource());
            mDetailsHelper.setCloseListener(new CloseListener() {
                @Override
                public void onClose() {
                    hideDetails();
                }
            });
        }
        mDetailsHelper.show();
    }

    private void hideDetails(){
        mDetailsHelper.hide();
    }

  //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-02-01,PR1537480 begin
    public void updatevideoLockItemVisiable(){
        if(videoLockItem!=null)
        {
            if(mPlayer.isVideoPlaying())
            {
                videoLockItem.setVisible(true);
            }else
            {
                videoLockItem.setVisible(false);
            }
        }
     }
   //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-02-01,PR1537480 end
    private class MyDetailsSource implements DetailsSource {

        @Override
        public MediaDetails getDetails() {
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-03,Defect:1020140 begin
            return mItem == null ? null : mItem.getDetails();
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-03,Defect:1020140 end
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public int getIndex() {
            return 0;
        }
    }

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-11-26,Task:982555 begin
    private void boomKeyVideoStart() {
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-11,Defect:1060991 begin
        if (mItem == null || mPlayer == null) {
            Log.i(TAG, "boomKeyVideoStart click, but mItem or mPlayer is NULL.");
            return;
        }
        if(mVideoRoot != null && mFirstPlayVideoView != null) {
            mVideoRoot.removeView(mFirstPlayVideoView);
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-11,Defect:1060991 end
        Intent intent = new Intent("com.muvee.BoomKeyVideo");
        intent.setClassName("com.muvee.boomkey.video",
                "com.muvee.boomkeyvideo.BoomKeyVideoMainActivity");
        intent.putExtra("com.muvee.dsg.boomkey.video.file.path",
                mItem.getFilePath());
        intent.putExtra("com.muvee.dsg.boomkey.time.ms", mPlayer
                .getMovieVideoView().getCurrentPosition() * 1L);
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-15,PR1694257 begin
        if (PLFUtils.getBoolean(this,"def_gallery_custom_share_enable")) {
            intent.putExtra("com.muvee.share.usecustom", true);
        } else {
            intent.putExtra("com.muvee.share.usecustom", false);
        }
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-15,PR1694257 end
        if (getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            startActivity(intent);
        } else {
            Log.e(TAG, "Can't find muvee BoomKeyVideoMainActivity App.");
        }
    }

    //[BUGFIX]-Add-Begin by TSNJ,zhe.xu,2016-01-04, alm 1040157
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.d(TAG, "onCallStateChanged:" + state);
            if(state != TelephonyManager.CALL_STATE_IDLE && mPlayer != null) {
            int mCurrentPosition = mPlayer.mVideoView.getCurrentPosition();
            mPlayer.mPauseVideo4Call();
            }
        }
    };
    //[BUGFIX]-Add-End by TSNJ,zhe.xu,2016-01-04

    /**
     * It is for boom key onBoomPressed callback
     */
    public void onBoomPressed() {
        Log.i(TAG, "onBoomPressed");
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-11,PR1400047 begin
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-1472684 begin
        /* MODIFIED-BEGIN by caihong.gu-nb, 2016-05-09,BUG-2121167*/
        if (mPlayer != null && mPlayer.is4KVideo()) {
            Toast.makeText(MovieActivity.this, R.string.not_support_video_edit,
                    Toast.LENGTH_LONG).show();
            return;
        }
        /* MODIFIED-END by caihong.gu-nb,BUG-2121167*/
        if (mPlayer.isVideoPlaying() && !isDrm) {
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-1472684 end
            //[BUGFIX]-Add by TSNJ,zhe.xu, 2016-01-26,defect 1508016 begin
            AudioManager mAudioManager =(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            if(mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
                Vibrator vb = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(110);
            }
            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-4,ALM-1717012 end
            //[BUGFIX]-Add by TSNJ,zhe.xu, 2016-01-26,defect 1508016 end
            boomKeyVideoStart();
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-11,PR1400047 end
    }

    /**
     * It is for boom key onBoomLongPress callback
     */
    public void onBoomLongPress() {
        Log.i(TAG, "onBoomLongPress");
    }

    /**
     * It is for boom key onBoomLongPress callback
     */
    public void onBoomDoublePress() {
        Log.i(TAG, "onBoomDoublePress");
    }
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-11-26,Task:982555 end

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1018928 begin
    private Uri parseUriFromPath(Uri fileUri) {
        Uri uri = fileUri;
        String path = uri.getEncodedPath();
        if (path != null) {
            path = Uri.decode(path);
            Cursor cursor = null;
            try {
                Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;
                String[] projection = new String[] { VideoColumns._ID };
                String selection = "(" + VideoColumns.DATA + "=" + "'"
                        + path + "'" + ")";
                cursor = getContentResolver().query(baseUri, projection,
                        selection, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(projection[0]);
                    uri = Uri.parse("content://media/external/video/media/"
                            + cursor.getInt(index));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        return uri;
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-04,PR1018928 end
}

package com.tct.gallery3d.app;

/*--------------------------------------------------------------------------------------------------------------------------------*/
/* 19/10/2015|    su.jiang     |  PR-735383    |[Android 5.1][Gallery_v5.2.0.1.1.0306.0]The back icon display inconsistent with GD*/
/*-----------|-----------------|---------------|----------------------------------------------------------------------------------*/
/* 26/10/2015|    su.jiang     |  PR-788215    |open a  share interface for Muvee*/
/*-----------|-----------------|---------------|---------------------------------*/
/* 09/12/2015|    su.jiang     |  PR-1101300   |Muvee share*/
/*-----------|-----------------|---------------|-----------*/
/* 21/12/2015|    su.jiang     |  PR-1190451   |[Gallery][Slo.mo]When share the slo.mo video  press the cancel  of the exporting video ,the screen freeze.*/
/*-----------|-----------------|---------------|----------------------------------------------------------------------------------------------------------*/
/* 22/12/2015|    su.jiang     |  PR-1125267   |[Android6.0][Gallery_v5.2.5.1.0320.0]The exported video progress bar will pop up after click other uninstall apk.*/
/*-----------|-----------------|---------------|-----------------------------------------------------------------------------------------------------------------*/
/* 05/01/2016| dongliang.feng  | ALM-1201291   |[Android6.0][Gallery_v5.2.5.1.0323.0]Gallery force close after exporting burst shot picture */
/*-----------|-----------------|---------------|---------------------------------*/
/* 08/01/2015|    su.jiang     |  PR-1159002   |[Gallery][Video]After the video saved to gallery ,press the "X" should back to the video preview screen.*/
/*-----------|-----------------|---------------|--------------------------------------------------------------------------------------------------------*/
/* 16/01/2015|    su.jiang     |  PR-1220379   |[Android6.0][Gallery]Exporte stop on face show interface after lock screen.*/
/*-----------|-----------------|---------------|---------------------------------------------------------------------------*/
/* 16/01/2015|    su.jiang     |  PR-1220379   |[Android6.0][Gallery]Exporte stop on face show interface after lock screen.*/
/*-----------|-----------------|---------------|---------------------------------------------------------------------------*/
/* 02/02/2016| jian.pan1            |[ALM]Defect:1539375   |[ERGO][Force Close][Gallery]Gallery FC when tap more in Share interface
/* ----------|----------------------|----------------------|----------------- */
/* 2016/02/02|  caihong.gu-nb  |  PR-1534733   |    [3rd APK][Twitter]Can not access to Twitter when share photo from gallery */
/*-----------|-----------------|---------------|---------------------------------------------------------------------------------*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tct.gallery3d.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.tct.gallery3d.ui.Log;
import com.tct.gallery3d.util.PLFUtils;

public class ShareDefaultPage extends Activity {
    @SuppressWarnings("unused")
    private static final String TAG = "ShareDefaultPage";
    // image true, video or other false
    public static final String SHARE_URI = "share-uri";
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-26,PR788215 begin
    public static final String GALLERY_SHARE = "gallery_share";
    public static final String MULTIPLE = "multiple";
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-26,PR788215 end
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 begin
    public static final String ACTION_DEFAULT_SHARE = "com.tct.gallery3d.defaultshare";
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 end

    private static final String FACKBOOK_NAME = "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias";
    private static final String TWITTER_NAME = "com.twitter.android.composer.ComposerActivity";
    private static final String INSTAGRAM_NAME = "com.instagram.android.activity.ShareHandlerActivity";
    private static final String YOUTUBE_NAME = "com.google.android.youtube.UploadIntentHandlingActivity";
    private static final String WHATSAPP_NAME = "com.whatsapp.ContactPicker";
    private static final String GALLERY_NAME = "gallery3d";//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-26,PR788215

    private static final String FACKBOOK_PACKAGE_NAME = "com.facebook.composer";//ok
    private static final String TWITTER_PACKAGE_NAME = "com.twitter.android";//ok
    private static final String INSTAGRAM_PACKAGE_NAME = "com.instagram.android";
    private static final String YOUTUBE_PACKAGE_NAME = "com.google.android.youtube";//ok
    private static final String WHATSAPP_PACKAGE_NAME = "com.whatsapp";//ok

    private int[] mImageAppIcons;
    private int[] mVideoAppIcons;

    public String[] mImageAppPKGNames = { FACKBOOK_NAME, TWITTER_NAME,
            INSTAGRAM_NAME };
    public String[] mVideoAppPKGNames = { INSTAGRAM_NAME, FACKBOOK_NAME,
            WHATSAPP_NAME, YOUTUBE_NAME };

    private int[] mExitImageAppIcons = { R.drawable.social_facebook,
            R.drawable.social_twitter, R.drawable.social_insgram };
    private int[] mExitVideoAppIcons = { R.drawable.social_insgram,
            R.drawable.social_facebook, R.drawable.social_whatsapp,
            R.drawable.social_youtube };

    private Map<String, String> packageNameMap = null;

    private String[] mImageAppName;
    private String[] mVideoAppName;

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-09,PR1101300 begin
    private int state;
    private int appPosition = -1;
    private float progress;
    private boolean isFromGallery;
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-09,PR1101300 end
    private boolean isImage;
    private boolean imageMultipe;
    private Uri imageUri;
    private ArrayList<Uri> imageUris;
    private GridView mGridView;
    private SimpleAdapter mSimpleAdapter;
    private List<Intent> mTargetIntents;
    private List<ResolveInfo> mResolveInfos;
    private List<ResolveInfo> mExitsResolveInfos;
    private ActionBar mActionBar;
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-09,PR1101300 begin
    private AlertDialog mDialog;
    private ProgressBar mProgressBar;
    private TextView mCancel;
    private TextView mSaveText; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-05, ALM-1201291
    private LinearLayout mLayout;
    private ProgressBarTask task;
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 begin
    private String mMimeType;
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 end
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-09,PR1101300 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 begin
        Intent intent = getIntent();
        mMimeType = intent.getType();
        isImage = mMimeType.startsWith("image/") ? true : false;
        imageMultipe = intent.getBooleanExtra(MULTIPLE, false);
        //isFromGallery = intent.getBooleanExtra(GALLERY_SHARE, false);
        if(!imageMultipe){
            imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        } else {
            final ArrayList<Uri> uris = intent
                    .getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            imageUris = uris;
            imageMultipe = true;
        }
        if (PLFUtils.getBoolean(this, "def_gallery_custom_share_enable")) {
            initData();
            setContentView(R.layout.share_default);
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-09,PR1101300 begin
            mLayout = (LinearLayout) this.findViewById(R.id.share_save_video_layout);
            mSaveText = (TextView) this.findViewById(R.id.share_video_text); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-05, ALM-1201291
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-26,PR788215 begin
            mSaveText.setText(getString(isImage ? R.string.share_save_image
                    : R.string.share_save_video)); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-05, ALM-1201291
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-16,PR1220379 begin
            /*if (isFromGallery){
                //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-26,PR788215 end
            } else {
                mLayout.setVisibility(View.VISIBLE);
            }*/
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-16,PR1220379 end
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-26,PR788215 end
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-09,PR1101300 end
            initActionBar();
        } else {
            Intent shareIntent = new Intent(ACTION_DEFAULT_SHARE);
            Log.i(TAG, "shareIntent mimeType = " + mMimeType + " imageUri = "
                    + imageUri);
            shareIntent.setType(mMimeType);
            shareIntent.setData(imageUri);
            this.sendBroadcast(shareIntent);
            this.finish();
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 end
    }

    private void initData() {
        String more = getResources().getString(R.string.more);
        mImageAppName = new String[] {"Facebook", "Twitter", "Instagram", more};
        mVideoAppName = new String[] {"Instagram", "Facebook", "Whatsapp", "YouTube", more};
        packageNameMap = new HashMap<String, String>();
        packageNameMap.put("Facebook", FACKBOOK_PACKAGE_NAME);
        packageNameMap.put("Twitter", TWITTER_PACKAGE_NAME);
        packageNameMap.put("Instagram", INSTAGRAM_PACKAGE_NAME);
        packageNameMap.put("Whatsapp", WHATSAPP_PACKAGE_NAME);
        packageNameMap.put("YouTube", YOUTUBE_PACKAGE_NAME);
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-09,PR1101300 begin
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            progress = intent.getFloatExtra("com.muvee.broadcast.PROGRESS", 0f);

            Log.e(TAG, "## progress == " + progress);
            // In progress,1:Success,2:Failed,3:Cancelled​
            state = intent.getIntExtra("com.muvee.broadcast.STATE", 0);
            // save path
            Log.d(TAG, "state : " + state);
            if ((progress == 100.0f) && (state == 1)) {
                String path = intent.getStringExtra("com.muvee.broadcast.PATH");
                Log.d(TAG, "path :" + path);
                imageUri = Uri.parse(path);
                mSaveText.setText(getString(isImage ? R.string.share_image_saved
                        : R.string.share_saved)); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-05, ALM-1201291
                mLayout.setVisibility(View.VISIBLE);
            }
        };
    };

    @Override
    protected void onStart() {
      //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-16,PR1220379 begin
//        if (!isFromGallery) {
//            IntentFilter intentFilter = new IntentFilter(
//                    "com.muvee.broadcast.UPDATES");
//            registerReceiver(broadcastReceiver, intentFilter);
//        }
      //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-16,PR1220379 end
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-16,PR1220379 begin
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-08,PR1159002 begin
//        if (!isFromGallery) {
//            unregisterReceiver(broadcastReceiver);
//            if (task != null && !task.isCancelled()
//                    && task.getStatus() != AsyncTask.Status.FINISHED) {
//                task.onCancelled();
//                task.cancel(true);
//            }
//        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-08,PR1159002 end
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-16,PR1220379 end
    }

    private void showShareDialog(int position){
        mLayout.setVisibility(View.INVISIBLE);
        if (progress == 100.0f && imageUri != null) {
            shareToPartyApps(position);
            return;
        }

        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-22,PR1125267 begin
        //if the tapped app is not exits, don't show the dialog
        for (int i = 0; i < mExitsResolveInfos.size(); i++) {
            ResolveInfo mResolveInfo = mExitsResolveInfos.get(i);
            if (isImage) {
                if(position + 1 == mImageAppName.length) continue;
                if (!mResolveInfo.activityInfo.name
                                .equals(mImageAppPKGNames[position])) {
                    return;
                }
            } else {
                if(position + 1 == mVideoAppName.length) continue;
                if (!mResolveInfo.activityInfo.name
                                .equals(mVideoAppPKGNames[position])) {
                    return;
                }
            }
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-22,PR1125267 end

        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-08,PR1159002 begin
        if (mExitsResolveInfos.size() == 0) {
            if (isImage) {
                if(position + 1 != mImageAppName.length) return;
            } else {
                if(position + 1 != mVideoAppName.length) return;
            }
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-08,PR1159002 end

        if(mDialog == null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            View contentView = getLayoutInflater().inflate(R.layout.share_export_dialog, null);
            dialog.setView(contentView);
            mDialog = dialog.create();
            mCancel = ((TextView)contentView.findViewById(R.id.exporting_cancel));
            mCancel.setText(String.format(getString(R.string.cancel)).toUpperCase());
//            mDialog.setCanceledOnTouchOutside(false);
            mProgressBar = (ProgressBar) contentView.findViewById(R.id.exporting_progressbar);
            mProgressBar.setMax(100);
        }
        mDialog.show();

        if(task == null) {
            task = new ProgressBarTask();
            task.execute();
            mCancel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
        }
    }

    private class ProgressBarTask extends AsyncTask<Integer,Integer, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setProgress(0);
        }

        @Override
        protected String doInBackground(Integer... params) {
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-21,PR1190451 begin
            float temp = 0;
            int max = 100;
            while(progress <= max && !isCancelled()){
                if (progress == temp && imageUri == null) continue;

                temp = progress;
                publishProgress((int)progress);
                if (progress == max) {
                    if (mDialog.isShowing() && imageUri != null){
                        mDialog.dismiss();
                        shareToPartyApps(appPosition);
                        return null;
                    }
                }
            }
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-21,PR1190451 end
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressBar.setProgress(values[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-09,PR1101300 end

    @Override
    protected void onResume() {
        super.onResume();
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 begin
        if (PLFUtils.getBoolean(this, "def_gallery_custom_share_enable")) {
            mImageAppIcons = new int[] { R.drawable.social_facebook_off,
                    R.drawable.social_twitter_off, R.drawable.social_insgram_off,
                    R.drawable.social_more };
            mVideoAppIcons = new int[] { R.drawable.social_insgram_off,
                    R.drawable.social_facebook_off, R.drawable.social_whatsapp_off,
                    R.drawable.social_youtube_off, R.drawable.social_more };
            // initialize
            checkShareApps(isImage);
            init(isImage);
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-02-02,Defect:1539375 end
    }

    private void init(final boolean isImage) {
        int[] mIcons = null;
        String[] mAppName = null;
        if (isImage) {
            mIcons = mImageAppIcons;
            mAppName = mImageAppName;
        } else {
            mIcons = mVideoAppIcons;
            mAppName = mVideoAppName;
        }
        mGridView = (GridView) findViewById(R.id.share_apps_gridview);

        List<Map<String, Object>> mDataMaps = new ArrayList<>();
        for (int i = 0; i < mAppName.length; i++) {
            Map<String, Object> mMap = new HashMap<>();
            mMap.put("icon", mIcons[i]);
            mMap.put("name", mAppName[i]);
            mDataMaps.add(mMap);
        }
        String[] from = { "icon", "name" };
        int[] to = { R.id.app_icon, R.id.app_name };
        mSimpleAdapter = new SimpleAdapter(this, mDataMaps,
                R.layout.share_app_item, from, to);
        mGridView.setAdapter(mSimpleAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                appPosition = position;
//                if (!isFromGallery) {
//                    showShareDialog(appPosition);
//                } else {
//                }
                shareToPartyApps(appPosition);
            }
        });
    }

    private void checkShareApps(boolean isImage) {
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (isImage) {
            intent.setType("image/*");
        } else {
            intent.setType("video/*");
        }
        PackageManager mPackageManager = this.getPackageManager();
        mResolveInfos = mPackageManager.queryIntentActivities(intent,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

        Log.e(TAG,"## mResolveInfos = " + mResolveInfos);
        mExitsResolveInfos = new ArrayList<>();
        for (int i = 0; i < mResolveInfos.size(); i++) {
            ResolveInfo mResolveInfo = mResolveInfos.get(i);
            if (isImage) {
                for (int j = 0; j < mImageAppPKGNames.length; j++) {
                    if (mImageAppPKGNames[j]
                            .equals(mResolveInfo.activityInfo.name)) {
                        mExitsResolveInfos.add(mResolveInfo);
                        mImageAppIcons[j] = mExitImageAppIcons[j];
                    }
                }
            } else {
                for (int j = 0; j < mVideoAppPKGNames.length; j++) {
                    if (mVideoAppPKGNames[j]
                            .equals(mResolveInfo.activityInfo.name)) {
                        mExitsResolveInfos.add(mResolveInfo);
                        mVideoAppIcons[j] = mExitVideoAppIcons[j];
                    }
                }
            }
        }
    }
    public boolean hasMarketApk(Context context) {
        String packageName = "com.android.vending";
        ApplicationInfo info = null;
        int enable = context.getPackageManager().COMPONENT_ENABLED_STATE_DISABLED;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
            enable = context.getPackageManager().getApplicationEnabledSetting(packageName);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "The Market Apk is not found.");
        }
        //[BUGFIX]-Add by TCTNJ,hao.yin, 2016-03-07,PR1719184 begin
        return info != null && (enable == context.getPackageManager().COMPONENT_ENABLED_STATE_ENABLED ||
                enable == context.getPackageManager().COMPONENT_ENABLED_STATE_DEFAULT) ? true : false;
        //[BUGFIX]-Add by TCTNJ,hao.yin, 2016-03-07,PR1719184 end
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-09,PR1101300 begin
    private void shareToPartyApps(int position) {
        if (!isFromGallery) {
            imageUri = parseUriFromPath(imageUri.toString());
        }
        Log.i(TAG, "  imageUri " + imageUri);
        Intent intent = new Intent();
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-26,PR788215 begin
        if (!imageMultipe) {
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        } else {
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                    (ArrayList<? extends Parcelable>) imageUris);
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-26,PR788215 end
        // click on the more icon
        if (isImage) {
            if (position + 1 == mImageAppName.length) {
                intent.setType("image/*");
                startActivity(Intent.createChooser(intent, getString(R.string.share)));
                return;
            }
        } else {
            if (position + 1 == mVideoAppName.length) {
                intent.setType("video/*");
                startActivity(Intent.createChooser(intent, getString(R.string.share)));
                return;
            }
        }
        for (int i = 0; i < mExitsResolveInfos.size(); i++) {
            ResolveInfo mResolveInfo = mExitsResolveInfos.get(i);
            if (isImage) {
                if (mResolveInfo.activityInfo.name
                        .equals(mImageAppPKGNames[position])) {
                    intent.setType("image/*");
                    intent.setComponent(new ComponentName(
                            mResolveInfo.activityInfo.packageName,
                            mResolveInfo.activityInfo.name));
                    startActivity(intent);
                    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/02,PR1534733
                    return;
                    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/02,PR1534733

                }
            } else {
                String name = mResolveInfo.activityInfo.name;
                Log.e(TAG,"## name = " + name);
                if (mResolveInfo.activityInfo.name
                        .equals(mVideoAppPKGNames[position])) {
                    intent.setType("video/*");
                    intent.setComponent(new ComponentName(
                            mResolveInfo.activityInfo.packageName,
                            mResolveInfo.activityInfo.name));
                    startActivity(intent);
                    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/02,PR1534733
                    return;
                    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/02/02,PR1534733

                }
            }
        }
        if(hasMarketApk(getApplicationContext())){
            String appName = null;
            if(isImage) {
                appName = mImageAppName[position];
            } else {
                appName = mVideoAppName[position];
            }
            String packageName = packageNameMap.get(appName);
            Log.e("TAG", "## appName=" + appName + ", packageName=" + packageName);
            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
            marketIntent.setData(Uri.parse("market://details?id=" + packageName));
            startActivity(marketIntent);
        }
    }

    private Uri parseUriFromPath(String path) {
        Uri uri = Uri.parse(path);;
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
                    Log.e(TAG, "uri = " + uri);
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
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-09,PR1101300 end

    private void initActionBar() {
        mActionBar = this.getActionBar();
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        mActionBar.setDisplayShowTitleEnabled(true);
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-19,PR735383 begin
        String title = getString(R.string.share);
        mActionBar.setTitle(title);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_cancel);
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-10-19,PR735383 end
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;
        default:
            break;
        }
        return true;
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-21,PR1190451 begin
    @Override
    protected void onPause() {
        super.onPause();
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-16,PR1220379 begin
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-08,PR1159002 begin
//        if (task != null) {
//            if(task.isCancelled()) {
//                task.onCancelled();
//                task.cancel(true);
//            }
//        }
//        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-08,PR1159002 end
//        if (null != mDialog && mDialog.isShowing()) mDialog.dismiss();
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-12-21,PR1190451 end
    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-16,PR1220379 end
}

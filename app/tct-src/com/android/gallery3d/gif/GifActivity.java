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
/* File     :../tct-src/com/android/gallery3d/gif/GifActivity.java            */
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
/* 04/07/2014|pingwen.tu            |636486                |[Force close][OMA */
/*           |                      |                      | Download]It will */
/*           |                      |                      | popup force clos */
/*           |                      |                      |e when sharing gi */
/*           |                      |                      |f format picture. */
/* ----------|----------------------|----------------------|----------------- */
/* 12/18/2014|   Peng.Tian          | PR868989             |Impossible to set */
/*           |                      |                      |gif.dm picture as wallpaper*/
/* ----------|----------------------|----------------------|----------------- */
/* 17/01/2015|   jian.pan1          | PR901946             |[MMS]The screen dis*/
/*           |                      |                      |play abnormal when view GIF Picture*/
/* ----------|----------------------|----------------------|----------------- */
/* 18/03/2015|   ye.chen            | PR952544             |[Android5.0][Callery v5.1.9.1.0105.0]'Set picture as' option still display in cd gif image
/* ----------|----------------------|----------------------|----------------- */
/* 07/05/2015 |    jialiang.ren     |      PR-992592       |[Bluetooth]GIF file can not set as wallpaper in Bluetooth received*/
/*------------|---------------------|----------------------|------------------------------------------------------------------*/
/* 29/05/2015 |    jialiang.ren     |      PR-1013475         |[Android5.0][Gallery_v5.1.13.1.0205.0]Set gif as */
/*                                                             wallpaper/contact will not display gif picture   */
/*------------|---------------------|-------------------------|-------------------------------------------------*/
/* 07/23/2015| jian.pan1            | PR1050312            |[GAPP][Gallery][Video Player]There's no message when select a subtitle file is not supported
/* ----------|----------------------|----------------------|----------------- */

package com.android.gallery3d.gif;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;

import com.tct.gallery3d.R;
import com.tct.gallery3d.data.MediaObject;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.ui.MenuExecutor;
import com.tct.gallery3d.util.PermissionUtil;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.Gallery;

//import com.tct.ext.drm.TctDrmManagerClient;

public class GifActivity extends Activity {
    private static final String TAG = "GifActivity";
    public static final String KEY_LOGO_BITMAP = "logo-bitmap";
    public static final String KEY_TREAT_UP_AS_BACK = "treat-up-as-back";
    public static final int REQUEST_PLAY_GIF = 7;

    private boolean mFinishOnCompletion;
    private Uri mUri;
    private boolean mTreatUpAsBack;
    private GifView mGifView;
    private String mPath;// [FEATURE]-Add by TCTNB.Peng.Cao,11/06/2012,support
                         // drm

    //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2014-11-20,PR844898 begin
    private ShareActionProvider mShareActionProvider = null;
    //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2014-11-20,PR844898 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.gif_view);
        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.gif_view_root);
        rootLayout.setGravity(Gravity.CENTER);
        mGifView = new GifView(this);
        rootLayout.addView(mGifView);

        rootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        Intent intent = getIntent();
        //[BUGFIX]-Add-BEGIN by TSNJ.chunhua.liu,01/08/2016,defect 1356256
        boolean checkResult = PermissionUtil.checkPermissions(GifActivity.this, AbstractGalleryActivity.class.getName());
        if (!checkResult) {
            Log.d("GifActivity", "error: checkPermissions failed");
            GifActivity.this.finish();
            return;
         }
        //[BUGFIX]-Add-BEGIN by TSNJ.chunhua.liu,01/08/2016,defect 1356256
        initializeActionBar(intent);
        mFinishOnCompletion = intent.getBooleanExtra(
                MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        mTreatUpAsBack = intent.getBooleanExtra(KEY_TREAT_UP_AS_BACK, false);
        mGifView.setGifFileUri(getIntent().getData());
        // [BUGFIX]-Add-BEGIN by TCTNB.Peng.Cao,06/06/2013,458910
        // mGifView.init(mUri);
        // [BUGFIX]-Add-END by TCTNB.Peng.Cao

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
        win.setAttributes(winParams);

        // We set the background in the theme to have the launching animation.
        // But for the performance (and battery), we remove the background here.
        win.setBackgroundDrawable(null);
    }

    private void initializeActionBar(Intent intent) {
        mUri = intent.getData();
        final ActionBar actionBar = getActionBar();
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-17,PR901946 begin
//        Bitmap logo = intent.getParcelableExtra(KEY_LOGO_BITMAP);
//        if (logo != null) {
//            actionBar.setLogo(new BitmapDrawable(getResources(), logo));
//        }
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
//        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-01-17,PR901946 end
        // [FEATURE]-Add-BEGIN by TCTNB.Peng.Cao,11/06/2012,support drm
        mUri = intent.getData();
        mPath = intent.getStringExtra("PATH");
        // [FEATURE]-Add-END by TCTNB.Peng.Cao
        // [BUGFIX]-Mod-BEGIN by TCTNJ.(pingwen.tu),04/07/2014, PR-636486,
        // [Force close][OMA Download]It will popup force close when sharing gif
        // format picture.
      //[BUGFIX]-Add-BEGIN by TSNJ.Peng.Tian,12/18/2014,868989
        GifActivity.this.grantUriPermission("com.tct.gallery3d", mUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //[BUGFIX]-Add-END by TSNJ.Peng.Tian
        // [BUGFIX]-Mod-END by TCTNJ.(pingwen.tu)
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-17,PR901946 begin
//        if (title != null) {
//            actionBar.setTitle(title);
//        } else {
//            // Displays the filename as title, reading the filename from the
//            // interface: {@link android.provider.OpenableColumns#DISPLAY_NAME}.
//            AsyncQueryHandler queryHandler = new AsyncQueryHandler(
//                    getContentResolver()) {
//                @Override
//                protected void onQueryComplete(int token, Object cookie,
//                        Cursor cursor) {
//                    try {
//                        if ((cursor != null) && cursor.moveToFirst()) {
//                            String displayName = cursor.getString(0);
//
//                            // Just show empty title if other apps don't
//                            // set
//                            // DISPLAY_NAME
//                            actionBar.setTitle((displayName == null) ? ""
//                                    : displayName);
//                        }
//                    } finally {
//                        Utils.closeSilently(cursor);
//                    }
//                }
//            };
//            queryHandler.startQuery(0, null, mUri,
//                    new String[] { OpenableColumns.DISPLAY_NAME }, null, null,
//                    null);
//        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-01-17,PR901946 end
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // [FEATURE]-Add-BEGIN by TCTNB.Peng.Cao,11/06/2012,support drm
        if (mUri == null)
            return true;
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-17,PR901946 begin
//        String uri = mUri.toString();

//        if (uri.contains("content://mms/")
//                || uri.contains("content://gmail-ls/")
//                || uri.contains("content://com.android.email.attachmentprovider/")) {
//            return true;
//        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-01-17,PR901946 end
        // [FEATURE]-Add-END by TCTNB.Peng.Cao

        getMenuInflater().inflate(R.menu.movie, menu);

        //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2014-11-20,PR844898 begin
        MenuItem item = menu.findItem(R.id.action_share);
        if (item != null) {
            mShareActionProvider = (ShareActionProvider)item.getActionProvider();
            mShareActionProvider.setShareHistoryFileName("gif_history.xml");
            Intent intentTemp = new Intent(Intent.ACTION_SEND)
            .setType(MenuExecutor.getMimeType(MediaObject.MEDIA_TYPE_IMAGE))
            .putExtra(Intent.EXTRA_STREAM, mUri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mShareActionProvider.setShareIntent(intentTemp);
        }
        //[BUGFIX]-Add by TCTNJ,qiang.ding1, 2014-11-20,PR844898 end

      //[BUGFIX]-Add-BEGIN by NJTS.Peng.Tian,12/28/2014,886211
      //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-18,PR952544
        if (getResources().getBoolean(R.bool.feature_Gallery2_gifWallpaperMenu_on) && !DrmManager.getInstance().isDrm(DrmManager.getInstance().convertUriToPath(mUri, getBaseContext()))) {
            menu.setGroupVisible(R.id.set_as, true);
        }
       // [BUGFIX]-Add-END by TSNJ.Peng.Tian

        // [BUGFIX]-Add-BEGIN by TCTNJ.hongda.zhu,03/26/2014,617210
        /*
         * temply disabled by chengqun.sun 2014.11.04 if
         * (TctDrmManagerClient.isDrmEnabled()) { TctDrmManagerClient client =
         * TctDrmManagerClient.getInstance(this); if (client.isDrm(mUri))
         * menu.removeGroup(R.id.subtitle_checkable); }
         *
         * if (!getResources().getBoolean(
         * R.bool.feature_Gallery2_gifWallpaperMenu_on)) {
         * menu.removeGroup(R.id.set_as); }
         *
         *
         * // [BUGFIX]-Add-END by TCTNJ.hongda.zhu,03/26/2014,617210
         * ShareActionProvider provider = GalleryActionBar
         * .initializeShareActionProvider(menu);
         *
         * // [FEATURE]-Add-BEGIN by TCTNB.Peng.Cao,11/06/2012,support drm if
         * (provider != null) { Intent intent = new Intent(Intent.ACTION_SEND);
         * intent.setType("image/*"); intent.putExtra(Intent.EXTRA_STREAM,
         * mUri); provider.setShareIntent(intent); }
         *
         * // [FEATURE]-Add-END by TCTNB.Peng.Cao
         */
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-12-05,PR862720 begin
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-23,PR1050312 begin
//        MenuItem subtitleItem = menu.findItem(R.id.subtitle);
//        subtitleItem.setVisible(false);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-23,PR1050312 end
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-12-05,PR862720 end

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mTreatUpAsBack) {
                finish();
            } else {
                startActivity(new Intent(this, Gallery.class));
                finish();
            }
            return true;
        }
     // [BUGFIX]-Add-BEGIN by TSNJ.Peng.Tian,12/18/2014,868989
        if (item.getItemId() ==R.id.setAs)
         {
            //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-07,PR992592 begin
            String path = mUri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                Cursor cursor = null;
                try {
                    Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
                    String[] projection = new String[] {Images.ImageColumns._ID};
                    String selection = "(" + Images.ImageColumns.DATA + "=" + "'" + path + "'" + ")";
                    cursor = getContentResolver().query(baseUri, projection, selection, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(projection[0]);
                        mUri = Uri.parse("content://media/external/images/media/" + cursor.getInt(index));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            }
            //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-07,PR992592 end

            Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
            intent.setDataAndType(mUri, intent.getType()).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("mimeType",intent.getType());
            startActivity(Intent.createChooser(intent,getString(R.string.set_as)));
//            finish(); //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-29,PR1013475
            return true;
          }
     // [BUGFIX]-Add-BEGIN by TSNJ.Peng.Tian
        /*
         * temply disabled by chengqun.sun 2014.11.04 // [BUGFIX]-Add-BEGIN by
         * TCTNJ.hongda.zhu,03/26/2014,617210 if (item.getItemId() ==
         * R.id.setAs) { Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
         * intent.setDataAndType(mUri, "image/*").setFlags(
         * Intent.FLAG_GRANT_READ_URI_PERMISSION); intent.putExtra("mimeType",
         * intent.getType()); startActivity(Intent.createChooser(intent,
         * getString(R.string.set_as))); finish(); return true; } //
         * [BUGFIX]-Add-END by TCTNJ.hongda.zhu,03/26/2014,617210
         */
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1392898 begin
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-25,ALM-1552405 begin
        if(mUri.toString().startsWith("content://")){
            String path=null;
            Cursor cursor = null;
            try {
                String[] projection = new String[] {Images.ImageColumns.DATA};
                cursor = getContentResolver().query(mUri,projection,null,null,null);
                if (cursor != null && cursor.moveToFirst()) {
                    path=cursor.getString(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            if(path==null)
            {
                this.finish();
            }
        }
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-2-25,ALM-1552405 end

       //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-11,PR1392898 end
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

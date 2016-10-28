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
/* ----------|----------------------|----------------------|----------------- */
/* 19/12/2014|ye.chen               |PR872588              |[DRM][Gallery2]Cannot set FL/CD/SD image as wallpaper.*/
/* ----------|----------------------|----------------------|----------------- */
/* 19/01/2015|ye.chen               |PR897846              |[OMA Download]DRM FL image can't set to wallpaper and the portrait of contact.
/* ----------|----------------------|----------------------|----------------- */
/* 05/02/2015|ye.chen               |PR913776              |[OMA Download]DRM FL image can't set to wallpaper and the portrait of contact
/* ----------|----------------------|----------------------|----------------- */
/* 13/02/2015|chengbin.du           |PR931927              |[FileManager]Can't update the crop picture timely from Category */
/* ----------|----------------------|----------------------|----------------- */
/* 15/02/2015|ye.chen               |PR932969              |[Download]SD format file can't be set to contact the pictures.
/* ----------|----------------------|----------------------|----------------- */
/* 15/02/2015|ye.chen               |PR936956              |[Clone][4.7][Downloads]Sometimes set the sd image as wallpaper will display "cannot load the image!"
/* ----------|----------------------|----------------------|----------------- */
/* 17/03/2015|ye.chen               |PR949845              |[clone][Gallery]Crop a picture show in pc incorrectly
/* ----------|----------------------|----------------------|----------------- */
/* 21/03/2015|dongliang.feng        |PR954782              |[Android5.0][Contacts_v5.1.5.2.0001.0][Hoperun][REG][Force Close]It can set SIM contact avatar in the gallery */
/* ----------|----------------------|----------------------|----------------- */
/* 03/21/2015| ye.chen              | PR956368             |    [Android5.0][Gallery v5.1.9.1.0106.0][Force Close]Gallery force close when editing the picture after deleting the picture in file manager
/* ----------|----------------------|----------------------|----------------------------------------- */
/* 03/26/2015| ye.chen              | PR958402             |[Android5.0][Gallery_v5.1.9.1.0107.0][Force Close]Gallery will happen force close when setting wifitransfer head portrait
/* ----------|----------------------|----------------------|----------------------------------------- */
/* 03/31/2015| jian.pan1            | PR962643             |    [Wi-Fi Transfer]Phone prompts "Cannot load the image!" when set advatar by using Crop picture.
/* ----------|----------------------|----------------------|----------------- */
/* 04/01/2015|ye.chen               |PR916400              |[GenericApp][Gallery]MTK DRM adaptation
/* ----------|----------------------|----------------------|----------------- */
/* 21/04/2015|qiang.ding1           |PR186236              |[DRM]Expired CD/SD video still can open in Downloads/File Manager/Notification bar
/* ----------|----------------------|----------------------|----------------- */
/* 04/23/2015| jian.pan1            | PR983876             |[Gallery][Contact][Force close]It will pop up Gallery force close when setting big picture to contact photo
/* ----------|----------------------|----------------------|----------------- */
/* 23/04/2015| qiang.ding1          | PR-186130            |[Download][DRM]Can't set DRM image as wallpaper*/
/* ----------|------------------- --|----------------------|------------------------------------------------*/
/* 05/14/2015| jian.pan1            | PR998803             |[Andriod][Gallery_v5.1.13.1.0202.0]The picture will go to right up corner when unlocking screen in croping interface
/* ----------|----------------------|----------------------|----------------- */
/* 13/07/2015 |  jialiang.ren  |   PR-1042004   |[Android 5.1][Gallery_v5.1.13.1.0212.0]The move */
/*                                               point is covered after switch landscape screen  */
/*------------|----------------|----------------|------------------------------------------------*/
/* 23/07/2015|dongliang.feng        |PR1046761             |[GMS_APK]Set picture as contact photo failed in the "Photo" APK. */
/* ----------|----------------------|----------------------|----------------- */
/* 05/08/2015|    su.jiang          |  PR-1037212          |[Android 5.1][Gallery_v5.1.13.1.0211.0]Picture details is not display completely after crop it*/
/*-----------|----------------------|----------------------|----------------------------------------------------------------------------------------------*/
/* 03/04/2016| jian.pan1            |[ALM]Defect:1660727   |[onetouch feedback]edit screentshot in moments layout display error.
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.filtershow.crop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tct.gallery3d.R;
/* MODIFIED-BEGIN by Yaoyu.Yang, 2016-10-19,BUG-3079416*/
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.SystemBarTintManager;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.exif.ExifInterface;
import com.tct.gallery3d.filtershow.cache.ImageLoader;
import com.tct.gallery3d.filtershow.tools.SaveImage;
import com.tct.gallery3d.util.ScreenUtils;

/**
 * Activity for cropping an image.
 */
public class CropActivity extends AbstractGalleryActivity {
/* MODIFIED-END by Yaoyu.Yang,BUG-3079416*/
    private static final String LOGTAG = "CropActivity";
    public static final String CROP_ACTION = "com.android.camera.action.CROP";
    private CropExtras mCropExtras = null;
    private LoadBitmapTask mLoadBitmapTask = null;

    private int mOutputX = 0;
    private int mOutputY = 0;
    private Bitmap mOriginalBitmap = null;
    private RectF mOriginalBounds = null;
    private int mOriginalRotation = 0;
    private Uri mSourceUri = null;
    private CropView mCropView = null;
    private ImageView mSaveButton = null;
    private boolean finalIOGuard = false;

    private static final int SELECT_PICTURE = 1; // request code for picker
    private static final long LIMIT_SUPPORTS_HIGHRES = 134217728; // 128Mb
    private static final int DEFAULT_COMPRESS_QUALITY = 90;
    /**
     * The maximum bitmap size we allow to be returned through the intent.
     * Intents have a maximum of 1MB in total size. However, the Bitmap seems to
     * have some overhead to hit so that we go way below the limit here to make
     * sure the intent stays below 1MB.We should consider just returning a byte
     * array instead of a Bitmap instance to avoid overhead.
     */
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-20,PR842843 begin
    public static final int MAX_BMAP_IN_INTENT = 500000;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-20,PR842843 end

    // Flags
    private static final int DO_SET_WALLPAPER = 1;
    private static final int DO_RETURN_DATA = 1 << 1;
    private static final int DO_EXTRA_OUTPUT = 1 << 2;

    private static final int FLAG_CHECK = DO_SET_WALLPAPER | DO_RETURN_DATA | DO_EXTRA_OUTPUT;

    private SystemBarTintManager mTintManager = null;//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-22,PR1009135
    private LinearLayout llMainPanel = null;//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-13,PR1042004

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setResult(RESULT_CANCELED, new Intent());
        mCropExtras = getExtrasFromIntent(intent);
        if (mCropExtras != null && mCropExtras.getShowWhenLocked()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        setContentView(R.layout.crop_activity);
        mCropView = (CropView) findViewById(R.id.cropView);
        llMainPanel = (LinearLayout)findViewById(R.id.mainPanel);//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-13,PR1042004

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            View view = LayoutInflater.from(CropActivity.this).inflate(R.layout.filtershow_actionbar, null);
            actionBar.setCustomView(view);
            mSaveButton = (ImageView) view.findViewById(R.id.crop_save);
            ImageView back = (ImageView) view.findViewById(R.id.crop_back);
            mSaveButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    startFinishOutput();
                }
            });

            back.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        Bundle extra = intent.getExtras();
        if (intent.getData() != null) {
            if(extra != null && extra.containsKey("path"))
             {
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-23, PR1046761 begin
                String uriString = extra.getString("path", null);
                if (uriString.contains("com.google.android.apps.photos")) {
                    mSourceUri = intent.getData();
                } else {
                    mSourceUri = Uri.parse(uriString);
                }
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-23, PR1046761 end
             }else{
                mSourceUri = intent.getData();
             }
            startLoadBitmap(mSourceUri);
        } else {
            pickImage();
        }

        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-22,PR1009135 begin
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-10-19,BUG-3079416*/
        //initTintManager();
        if(ScreenUtils.splitScreenIsAtBottom(this,getToolbar())){
            setStatusEnable(false);
        }else{
            setStatusEnable(true);
            setStatusColor(Color.BLACK);
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-22,PR1009135 end
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-22,PR1009135 begin
   /* private void initTintManager() {
        if(mTintManager == null) {
            mTintManager = new SystemBarTintManager(this);
        }
    }*/

//    public void setStatusEnable(boolean enable) {
//        mTintManager.setStatusBarTintEnabled(enable);
//    }
//
//    public void setStatusColor(int color) {
//        mTintManager.setStatusBarTintColor(color);
//    }
/* MODIFIED-END by Yaoyu.Yang,BUG-3079416*/
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-22,PR1009135 end

    private void enableSave(boolean enable) {
        if (mSaveButton != null) {
            mSaveButton.setEnabled(enable);
        }
    }

    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-05-14,PR998803 begin
    @Override
    protected void onRestart() {
        super.onRestart();
        if (mCropView != null) {
            mCropView.configChanged();
        }
    }
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-05-14,PR998803 end

    @Override
    protected void onDestroy() {
        if (mLoadBitmapTask != null) {
            mLoadBitmapTask.cancel(false);
        }
        super.onDestroy();
    }
  //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2014/12/17 PR-872588.
    //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/01/19 PR-897846.
    private void makeDrmToast(String name){
        String message = String.format(getString(R.string.drm_no_crop),name);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
     }

   private boolean drmCanSetWallpaper(Uri uri) {
        boolean bcansetwallpaper = true;
        DrmManager drmManager = DrmManager.getInstance();
       //[BUGFIX]-Add-BEGIN by TCTNB.Rui.Liu,01/22/2014,591709,
       //Foreward-lock picture can‘t set as wallpapler.
        if (null != uri ) {
            DrmManager.getInstance().init(this);
        }
        //[BUGFIX]-Add-END by TCTNB.Rui.Liu
        String path = ImageLoader.getFilePath(this,uri);//[BUGFIX]-Add-END by TCTNJ.ye.chen, 2015/02/05 PR-913776.
        //[BUGFIX]-Add-BEGIN by TCTNB.Peng.Cao,02/23/2014,606404
        if(null == path){
            return bcansetwallpaper;
        }
        //[BUGFIX]-Add-END by TCTNB.Peng.Cao

        String fileName = new File(path).getName();
            if(drmManager.isDrm( ImageLoader.getFilePath(this,uri))){//[BUGFIX]-Add-END by TCTNJ.ye.chen, 2015/02/05 PR-913776.
              //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/02/15 PR-932969.
              //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/03/02 PR-936956.
            //[ALM][BUGFIX]-Modify by TCTNJ,qiang.ding1, 2015-04-23,PR186130 begin
               if(drmManager.mCurrentDrm == drmManager.MTK_DRM){
                  if(drmManager.hasCountConstraint(path) ||
                       (!mCropExtras.getSetAsWallpaper())){
                       makeDrmToast(fileName);
                       bcansetwallpaper = false;
                     }
                }else{
                  if(drmManager.hasCountConstraint(path) ||
                     (!mCropExtras.getSetAsWallpaper() && (drmManager.isDrmSDFile(path) ||drmManager.isDrmCDFile(path)))){
                     makeDrmToast(fileName);
                     bcansetwallpaper = false;
                   }
                }
             //[ALM][BUGFIX]-Modify by TCTNJ,qiang.ding1, 2015-04-23,PR186130 end
              //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/03/02 PR-936956.
              //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/02/15 PR-932969.
             }else{
               }
             return bcansetwallpaper;
   }
 //[BUGFIX]-Add-END by TCTNB.ye.chen, 2015/01/19 PR-897846.
//[BUGFIX]-Add-END by TCTNB.ye.chen
    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mCropView.configChanged();
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-13,PR1042004 begin
        LayoutParams lp = (android.widget.FrameLayout.LayoutParams)llMainPanel.getLayoutParams();
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            lp.topMargin = 0;
        } else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp.topMargin = (int)getResources().getDimension(R.dimen.crop_landscape_margin_top);
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-13,PR1042004 end
    }
    /**
     * Opens a selector in Gallery to chose an image for use when none was given
     * in the CROP intent.
     */
    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                SELECT_PICTURE);
    }

    /**
     * Callback for pickImage().
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            mSourceUri = data.getData();
            startLoadBitmap(mSourceUri);
        }
    }

    /**
     * Gets screen size metric.
     */
    private int getScreenImageSize() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        return (int) Math.max(outMetrics.heightPixels, outMetrics.widthPixels);
    }

    /**
     * Method that loads a bitmap in an async task.
     */
    private void startLoadBitmap(Uri uri) {
         //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2014/12/17 PR-872588.
        if (uri != null && drmCanSetWallpaper(uri)) {
            enableSave(false);
            final View loading = findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);
            mLoadBitmapTask = new LoadBitmapTask();
            mLoadBitmapTask.execute(uri);
        } else {
          //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/02/15 PR-932969.
            if(uri == null){
                cannotLoadImage();
            }
          //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/02/15 PR-932969.
            done();
        }
    }

    /**
     * Method called on UI thread with loaded bitmap.
     */
    private void doneLoadBitmap(Bitmap bitmap, RectF bounds, int orientation) {
        final View loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        mOriginalBitmap = bitmap;
        mOriginalBounds = bounds;
        mOriginalRotation = orientation;
        if (bitmap != null && bitmap.getWidth() != 0 && bitmap.getHeight() != 0) {
            RectF imgBounds = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            mCropView.initialize(bitmap, imgBounds, imgBounds, orientation);
            if (mCropExtras != null) {
                int aspectX = mCropExtras.getAspectX();
                int aspectY = mCropExtras.getAspectY();
                mOutputX = mCropExtras.getOutputX();
                mOutputY = mCropExtras.getOutputY();
                if (mOutputX > 0 && mOutputY > 0) {
                    mCropView.applyAspect(mOutputX, mOutputY);

                }
                float spotX = mCropExtras.getSpotlightX();
                float spotY = mCropExtras.getSpotlightY();
                if (spotX > 0 && spotY > 0) {
                    mCropView.setWallpaperSpotlight(spotX, spotY);
                }
                if (aspectX > 0 && aspectY > 0) {
                    mCropView.applyAspect(aspectX, aspectY);
                }
            }
            enableSave(true);
        } else {
            Log.w(LOGTAG, "could not load image for cropping");
            cannotLoadImage();
            setResult(RESULT_CANCELED, new Intent());
            done();
        }
    }

    /**
     * Display toast for image loading failure.
     */
    private void cannotLoadImage() {
        CharSequence text = getString(R.string.cannot_load_image);
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * AsyncTask for loading a bitmap into memory.
     *
     * @see #startLoadBitmap(Uri)
     * @see #doneLoadBitmap(Bitmap)
     */
    private class LoadBitmapTask extends AsyncTask<Uri, Void, Bitmap> {
        int mBitmapSize;
        Context mContext;
        Rect mOriginalBounds;
        int mOrientation;

        public LoadBitmapTask() {
            mBitmapSize = getScreenImageSize();
            mContext = getApplicationContext();
            mOriginalBounds = new Rect();
            mOrientation = 0;
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            Uri uri = params[0];
            if (Runtime.getRuntime().maxMemory() < LIMIT_SUPPORTS_HIGHRES) {
                mBitmapSize = mBitmapSize / 2;
                }
          //[ALM][BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-04-23,PR186130 begin
            Bitmap bmap = null;
            if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                bmap = ImageLoader.loadConstrainedBitmap(mSourceUri, mContext,
                        mBitmapSize, mOriginalBounds, false);
            } else {
                bmap = getConstrainedBitmap(uri, mContext, mBitmapSize,
                        mOriginalBounds);
            }
            //[ALM][BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-04-23,PR186130 end
            mOrientation = ImageLoader.getMetadataRotation(mContext, uri);
            return bmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            doneLoadBitmap(result, new RectF(mOriginalBounds), mOrientation);
        }
    }
  //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2014/12/17 PR-872588.
    public  Bitmap getConstrainedBitmap(Uri uri, Context context, int maxSideLength,
            Rect originalBounds) {
        if (maxSideLength <= 0 || originalBounds == null || uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to getScaledBitmap");
        }
        InputStream is = null;
//[PLATFORM]-Add-BEGIN by TCTNB.(QiuRuifeng), 2013/11/20 PR-550755, reason [Gallery][DRM]DRM image cannot be set as wallpaper.
        FileDescriptor fd = null;
        ParcelFileDescriptor pfd = null;
//[PLATFORM]-Add-END by TCTNB.(QiuRuifeng)
        try {
            // Get width and height of stored bitmap
            is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
//[PLATFORM]-Mod-BEGIN by TCTNB.(QiuRuifeng), 2013/11/20 PR-550755, reason [Gallery][DRM]DRM image cannot be set as wallpaper.
            DrmManager drmManager = DrmManager.getInstance();
          //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen, 2015/02/05 PR-913776.
            if(drmManager.isDrm( ImageLoader.getFilePath(this,uri))){
                try{
                    pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                    if (pfd != null) {
                        fd = pfd.getFileDescriptor();
                       }
                    if (fd != null) {
                        BitmapFactory.decodeFileDescriptor(fd, null, options);
                       }
                } catch (Throwable t) {
                     return null;
                     }
            }else{
                BitmapFactory.decodeStream(is, null, options);
              }
//[PLATFORM]-Mod-END by TCTNB.(QiuRuifeng)
            int w = options.outWidth;
            int h = options.outHeight;
            originalBounds.set(0, 0, w, h);

            // If bitmap cannot be decoded, return null
            if (w <= 0 || h <= 0) {
                return null;
            }

            options = new BitmapFactory.Options();

            // Find best downsampling size
            int imageSide = Math.max(w, h);
            options.inSampleSize = 1;
            if (imageSide > maxSideLength) {
                int shifts = 1 + Integer.numberOfLeadingZeros(maxSideLength)
                        - Integer.numberOfLeadingZeros(imageSide);
                options.inSampleSize <<= shifts;
            }

            // Make sure sample size is reasonable
            if (options.inSampleSize <= 0 ||
                    0 >= (int) (Math.min(w, h) / options.inSampleSize)) {
                return null;
            }

            // Decode actual bitmap.
            options.inMutable = true;
            is.close();
            is = context.getContentResolver().openInputStream(uri);
//[PLATFORM]-Add-BEGIN by TCTNB.(QiuRuifeng), 2013/11/20 PR-550755, reason [Gallery][DRM]DRM image cannot be set as wallpaper.
            if(drmManager.isDrm( ImageLoader.getFilePath(this,uri))){//[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen, 2015/02/05 PR-913776.
                return BitmapFactory.decodeFileDescriptor(fd, null, options);
              }
//[PLATFORM]-Add-END by TCTNB.(QiuRuifeng)
            return BitmapFactory.decodeStream(is, null, options);
        } catch (FileNotFoundException e) {
            Log.e(LOGTAG, "FileNotFoundException: " + uri, e);
        } catch (IOException e) {
            Log.e(LOGTAG, "IOException: " + uri, e);
        } finally {
            Utils.closeSilently(is);
//[PLATFORM]-Add-BEGIN by TCTNB.(QiuRuifeng), 2013/11/20 PR-550755, reason [Gallery][DRM]DRM image cannot be set as wallpaper.
            if(pfd != null){
                Utils.closeSilently(pfd);
              }
//[PLATFORM]-Add-END by TCTNB.(QiuRuifeng)
        }
        return null;
    }
  //[BUGFIX]-Add-END by TCTNB.ye.chen, 2014/12/17 PR-872588.
    protected void startFinishOutput() {
        if (finalIOGuard) {
            return;
        } else {
            finalIOGuard = true;
        }
      //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen, 2015/03/21 PR-956368.
        DrmManager drmManager = DrmManager.getInstance();
        if(drmManager.isDrm( ImageLoader.getFilePath(this,mSourceUri))){
            if( ImageLoader.getFilePath(this,mSourceUri) == null) {
                cannotLoadImage();
                setResult(RESULT_CANCELED, new Intent());
                done();
                return;
            }
        }else{
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-31,PR962643 begin
            if(!isFileStreamExists(mSourceUri)) {
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-31,PR962643 end
                cannotLoadImage();
                setResult(RESULT_CANCELED, new Intent());
                done();
                return;
            }
        }
      //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen, 2015/03/21 PR-956368.
        enableSave(false);
        Uri destinationUri = null;
        int flags = 0;
        if (mOriginalBitmap != null && mCropExtras != null) {
            if (mCropExtras.getExtraOutput() != null) {
                destinationUri = mCropExtras.getExtraOutput();
                if (destinationUri != null) {
                    flags |= DO_EXTRA_OUTPUT;
                }
            }
            if (mCropExtras.getSetAsWallpaper()) {
                flags |= DO_SET_WALLPAPER;
            }
            if (mCropExtras.getReturnData()) {
                flags |= DO_RETURN_DATA;
            }
        }
        if (flags == 0) {
            destinationUri = SaveImage.makeAndInsertUri(this, mSourceUri);
            if (destinationUri != null) {
                flags |= DO_EXTRA_OUTPUT;
                //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-02-13,PR931927 begin
                Log.i(LOGTAG, "crop sendBroadcast UpdateFileManager");
                Intent broadiIntent = new Intent("UpdateFileManager");
                this.sendBroadcast(broadiIntent);
                //[BUGFIX]-Add by TCTNJ,chengbin.du, 2015-02-13,PR931927 end
            }
        }
        if ((flags & FLAG_CHECK) != 0 && mOriginalBitmap != null) {
            RectF photo = new RectF(0, 0, mOriginalBitmap.getWidth(), mOriginalBitmap.getHeight());
            RectF crop = getBitmapCrop(photo);
            startBitmapIO(flags, mOriginalBitmap, mSourceUri, destinationUri, crop,
                    photo, mOriginalBounds,
                    (mCropExtras == null) ? null : mCropExtras.getOutputFormat(), mOriginalRotation);
            return;
        }
        setResult(RESULT_CANCELED, new Intent());
        done();
        return;
    }

    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-31,PR962643 begin
    private boolean isFileStreamExists(Uri uri) {
        InputStream fileStream = null;
        if (uri == null) {
            Log.w(LOGTAG, "isFileStreamExit : URI is null");
            return false;
        } else {
            try {
                fileStream = getContentResolver().openInputStream(uri);
                if (fileStream == null) {
                    Log.w(LOGTAG, "fileStream is null");
                    return false;
                }
            } catch (FileNotFoundException e) {
                Log.w(LOGTAG, "can't read file:" + uri.toString(), e);
                return false;
            } finally {
                if (fileStream != null) {
                    try {
                        fileStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-31,PR962643 end

    private void startBitmapIO(int flags, Bitmap currentBitmap, Uri sourceUri, Uri destUri,
            RectF cropBounds, RectF photoBounds, RectF currentBitmapBounds, String format,
            int rotation) {
        if (cropBounds == null || photoBounds == null || currentBitmap == null
                || currentBitmap.getWidth() == 0 || currentBitmap.getHeight() == 0
                || cropBounds.width() == 0 || cropBounds.height() == 0 || photoBounds.width() == 0
                || photoBounds.height() == 0) {
            return; // fail fast
        }
        if ((flags & FLAG_CHECK) == 0) {
            return; // no output options
        }
        if ((flags & DO_SET_WALLPAPER) != 0) {
            Toast.makeText(this, R.string.setting_wallpaper, Toast.LENGTH_LONG).show();
        }

        final View loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);
        BitmapIOTask ioTask = new BitmapIOTask(sourceUri, destUri, format, flags, cropBounds,
                photoBounds, currentBitmapBounds, rotation, mOutputX, mOutputY);
        ioTask.execute(currentBitmap);
    }

    private void doneBitmapIO(boolean success, Intent intent) {
        final View loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        if (success) {
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, intent);
        }
        done();
    }

    private Bitmap cropBitmap;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-08-05,PR1037212
    private class BitmapIOTask extends AsyncTask<Bitmap, Void, Boolean> {

        private final WallpaperManager mWPManager;
        InputStream mInStream = null;
        OutputStream mOutStream = null;
        String mOutputFormat = null;
        Uri mOutUri = null;
        Uri mInUri = null;
        int mFlags = 0;
        RectF mCrop = null;
        RectF mPhoto = null;
        RectF mOrig = null;
        Intent mResultIntent = null;
        int mRotation = 0;

        // Helper to setup input stream
        private void regenerateInputStream() {
            if (mInUri == null) {
                Log.w(LOGTAG, "cannot read original file, no input URI given");
            } else {
                Utils.closeSilently(mInStream);
                try {
                    mInStream = getContentResolver().openInputStream(mInUri);
                } catch (FileNotFoundException e) {
                    Log.w(LOGTAG, "cannot read file: " + mInUri.toString(), e);
                }
            }
        }

        public BitmapIOTask(Uri sourceUri, Uri destUri, String outputFormat, int flags,
                RectF cropBounds, RectF photoBounds, RectF originalBitmapBounds, int rotation,
                int outputX, int outputY) {
            mOutputFormat = outputFormat;
            mOutStream = null;
            mOutUri = destUri;
            mInUri = sourceUri;
            mFlags = flags;
            mCrop = cropBounds;
            mPhoto = photoBounds;
            mOrig = originalBitmapBounds;
            mWPManager = WallpaperManager.getInstance(getApplicationContext());
            mResultIntent = new Intent();
            mRotation = (rotation < 0) ? -rotation : rotation;
            mRotation %= 360;
            mRotation = 90 * (int) (mRotation / 90);  // now mRotation is a multiple of 90
            mOutputX = outputX;
            mOutputY = outputY;

            if ((flags & DO_EXTRA_OUTPUT) != 0) {
                if (mOutUri == null) {
                    Log.w(LOGTAG, "cannot write file, no output URI given");
                } else {
                    try {
                        mOutStream = getContentResolver().openOutputStream(mOutUri);
                    } catch (FileNotFoundException e) {
                        Log.w(LOGTAG, "cannot write file: " + mOutUri.toString(), e);
                    }
                }
            }

            if ((flags & (DO_EXTRA_OUTPUT | DO_SET_WALLPAPER)) != 0) {
                regenerateInputStream();
            }
        }
      //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2014/12/17 PR-872588.
        // [BUGFIX]-Add-BEGIN by TCTNB.chenli.tao,03/17/2014,617461
        public Bitmap resizeLockWallpaper(Bitmap bm) {
            if (bm != null) {
                int w = bm.getWidth();
                     int h = bm.getHeight();
                     DisplayMetrics displayMetrics = new DisplayMetrics();
                     WindowManager wm2 = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                     wm2.getDefaultDisplay().getRealMetrics(displayMetrics);
                   //[BUGFIX]-Mod-BEGIN by TCTNJ.(pingwen.tu),05/23/2014, PR-636289,
                   //[Launcher][Wallpaper]It show abnormal while set the wallpaper in gallery in landscape mode
//                     int newW = displayMetrics.widthPixels;
//                     int newH = displayMetrics.heightPixels;
                     int newW = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                     int newH = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
                   //[BUGFIX]-Mod-END  by TCTNJ.(pingwen.tu)


                     float sW = 0;
                     float sH = 0;
                     if(w>h){
                         sW = ((float) newW*2) / w;
                         sH = ((float) newH) / h;
                     }else{
                         sW = ((float) newW) / w;
                         sH = ((float) newH) / h;
                     }
                     Matrix matrix = new Matrix();
                     matrix.postScale(sW, sH);
                     Bitmap newBitmap = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);
                     if (!bm.isRecycled() && bm != newBitmap) //[BUGFIX]-MOD by ping.wang,3/21/14,PR-627013
                         bm.recycle();
                     return newBitmap;
                 }
                 return null;
             }
            // [BUGFIX]-Add-end by TCTNB.chenli.tao,03/17/2014,617461
        @Override
        protected Boolean doInBackground(Bitmap... params) {
            boolean failure = false;
            Bitmap img = params[0];

            // Set extra for crop bounds
            if (mCrop != null && mPhoto != null && mOrig != null) {
                RectF trueCrop = CropMath.getScaledCropBounds(mCrop, mPhoto, mOrig);
                Matrix m = new Matrix();
                m.setRotate(mRotation);
                m.mapRect(trueCrop);
                if (trueCrop != null) {
                    Rect rounded = new Rect();
                    trueCrop.roundOut(rounded);
                    mResultIntent.putExtra(CropExtras.KEY_CROPPED_RECT, rounded);
                }
            }

            // Find the small cropped bitmap that is returned in the intent
            if ((mFlags & DO_RETURN_DATA) != 0) {
                assert (img != null);
                Bitmap ret = getCroppedImage(img, mCrop, mPhoto);
                if (ret != null) {
                    ret = getDownsampledBitmap(ret, MAX_BMAP_IN_INTENT);
                }
                if (ret == null) {
                    Log.w(LOGTAG, "could not downsample bitmap to return in data");
                    failure = true;
                } else {
                    if (mRotation > 0) {
                        Matrix m = new Matrix();
                        m.setRotate(mRotation);
                        Bitmap tmp = Bitmap.createBitmap(ret, 0, 0, ret.getWidth(),
                                ret.getHeight(), m, true);
                        if (tmp != null) {
                            ret = tmp;
                        }
                    }
                    mResultIntent.putExtra(CropExtras.KEY_DATA, ret);
                }
            }

            // Do the large cropped bitmap and/or set the wallpaper
            if ((mFlags & (DO_EXTRA_OUTPUT | DO_SET_WALLPAPER)) != 0 && mInStream != null) {
                // Find crop bounds (scaled to original image size)
                RectF trueCrop = CropMath.getScaledCropBounds(mCrop, mPhoto, mOrig);
                if (trueCrop == null) {
                    Log.w(LOGTAG, "cannot find crop for full size image");
                    failure = true;
                    return false;
                }
                Rect roundedTrueCrop = new Rect();
                trueCrop.roundOut(roundedTrueCrop);

                if (roundedTrueCrop.width() <= 0 || roundedTrueCrop.height() <= 0) {
                    Log.w(LOGTAG, "crop has bad values for full size image");
                    failure = true;
                    return false;
                }

                Bitmap crop = null;
                DrmManager drmManager = DrmManager.getInstance();

              //[ALM][BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-04-23,PR186130 begin
                if (drmManager.mCurrentDrm == drmManager.MTK_DRM) {
                    // Attempt to open a region decoder
                    BitmapRegionDecoder decoder = null;
                    try {
                        decoder = BitmapRegionDecoder.newInstance(mInStream,
                                true);
                    } catch (IOException e) {
                        Log.w(LOGTAG, "cannot open region decoder for file: "
                                + mInUri.toString(), e);
                    }
                    if (decoder != null) {
                        // Do region decoding to get crop bitmap
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inMutable = true;
                        // / M: [BUG.ADD] increase sample size to avoid OOM for
                        // extremely large image @{
                        int sampleSize = 1;
                        long availableMemory = availableMemory();
                        while ((roundedTrueCrop.width() >> (sampleSize - 1))
                                * (roundedTrueCrop.height() >> (sampleSize - 1))
                                * 4 > availableMemory) {
                            if (sampleSize > 5)
                                break;
                            sampleSize++;
                            Log.i(LOGTAG,
                                    "<BitmapIOTask.doInBackground> image too large, set sample size to "
                                            + sampleSize);
                        }
                        options.inSampleSize = sampleSize;
                        // / @}
                        crop = decoder.decodeRegion(roundedTrueCrop, options);
                        decoder.recycle();
                    }

                    // / M: [BUG.ADD] @{
                    // added for Drm image decode.
                    boolean isDrm = false;
                    String filePath = ImageLoader.getFilePath(
                            CropActivity.this, mSourceUri);
                    isDrm = DrmManager.getInstance().isDrm(filePath);
                    if (DrmManager.isDrmEnable && null != mSourceUri
                            && null != mSourceUri.getPath() && isDrm) {
                        // check if this file is drm and can get decrypted
                        // buffer
                        byte[] buffer = drmManager.forceDecryptFile(filePath,
                                false);
                        Bitmap tempBmp = BitmapFactory.decodeByteArray(buffer,
                                0, buffer.length, null);
                        crop = Bitmap.createBitmap(tempBmp,
                                roundedTrueCrop.left, roundedTrueCrop.top,
                                roundedTrueCrop.width(),
                                roundedTrueCrop.height());
                    }
                    // / @}
                    if (crop == null) {
                        // BitmapRegionDecoder has failed, try to crop in-memory
                        regenerateInputStream();
                        Bitmap fullSize = null;
                        if (mInStream != null) {
                            fullSize = BitmapFactory.decodeStream(mInStream);
                        }
                        if (fullSize != null) {
                            crop = Bitmap.createBitmap(fullSize,
                                    roundedTrueCrop.left, roundedTrueCrop.top,
                                    roundedTrueCrop.width(),
                                    roundedTrueCrop.height());
                        }
                    }
                }else{
              //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen, 2015/02/05 PR-913776.
                if(drmManager.isDrm(ImageLoader.getFilePath(CropActivity.this,mInUri))){
                    ParcelFileDescriptor pfd = null;
                    FileDescriptor fd = null;
                    Bitmap fullSize = null;
                    try{
                        pfd = getContentResolver().openFileDescriptor(mInUri, "r");
                        if (null != pfd) {
                            fd = pfd.getFileDescriptor();
                            }
                        if (null != fd) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inMutable = true;
                            fullSize = BitmapFactory.decodeFileDescriptor(fd, null, options);
                            }
                        if (fullSize != null) {
                            crop = Bitmap.createBitmap(fullSize, roundedTrueCrop.left,
                            roundedTrueCrop.top, roundedTrueCrop.width(),
                            roundedTrueCrop.height());
                            }
                        } catch (Throwable t) {
                            failure = true;
                            return false;
                        }finally {
                            if(pfd != null){
                                Utils.closeSilently(pfd);
                                }
                           }
                }else{

                    // Attempt to open a region decoder
                    BitmapRegionDecoder decoder = null;
                    try {
                        decoder = BitmapRegionDecoder.newInstance(mInStream, true);
                    } catch (IOException e) {
                        Log.w(LOGTAG, "cannot open region decoder for file: " + mInUri.toString(), e);
                    }
                    //Bitmap crop = null;
                    if (decoder != null) {
                        // Do region decoding to get crop bitmap
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inMutable = true;
                      //[BUGFIX]-Mod-BEGIN by TCTNJ.(pingwen.tu),07/21/2014, PR-740170,
                      //[Gallery]Gallery will force stopped when big picture is setted as wallpaper.
                        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-23,PR983876 begin
                        try {
                            crop = decoder.decodeRegion(roundedTrueCrop, options);
                        } catch (OutOfMemoryError e) {
                            Log.e(LOGTAG, "OutOfMemoryError e:" + e.getMessage());
                        } catch (IllegalArgumentException e) {
                            Log.e(LOGTAG, "IllegalArgumentException e:" + e.getMessage());
                        }
                        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-23,PR983876 end
                        if (crop == null) {
                           options.inSampleSize = 4;
                           crop = decoder.decodeRegion(roundedTrueCrop, options);
                           }
                      //[BUGFIX]-Mod-END  by TCTNJ.(pingwen.tu)
                        decoder.recycle();
                    }

                    if (crop == null) {
                        // BitmapRegionDecoder has failed, try to crop in-memory
                        regenerateInputStream();
                        Bitmap fullSize = null;
                        if (mInStream != null) {
                            fullSize = BitmapFactory.decodeStream(mInStream);
                        }
                        if (fullSize != null) {
                            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-23,PR983876 begin
                            int bmpW = fullSize.getWidth();
                            int bmpH = fullSize.getHeight();
                            int left = roundedTrueCrop.left;
                            int top = roundedTrueCrop.top;
                            int width = roundedTrueCrop.width();
                            int height = roundedTrueCrop.height();
                            if (width > bmpW) {
                                left = 0;
                                width = bmpW;
                            } else if (left + width > bmpW) {
                                width = bmpW - left;
                            }
                            if (height > bmpH) {
                                top = 0;
                                height = bmpH;
                            } else if (top + height > bmpH) {
                                height = bmpH - top;
                            }
                            crop = Bitmap.createBitmap(fullSize, left, top, width, height);
                            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-23,PR983876 end
                        }
                    }
                }
                }
                //[ALM][BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-04-23,PR186130 end
                if (crop == null) {
                    Log.w(LOGTAG, "cannot decode file: " + mInUri.toString());
                    failure = true;
                    return false;
                }
                if (mOutputX > 0 && mOutputY > 0) {
                    Matrix m = new Matrix();
                    RectF cropRect = new RectF(0, 0, crop.getWidth(), crop.getHeight());
                    if (mRotation > 0) {
                        m.setRotate(mRotation);
                        m.mapRect(cropRect);
                    }
                    RectF returnRect = new RectF(0, 0, mOutputX, mOutputY);
                    m.setRectToRect(cropRect, returnRect, Matrix.ScaleToFit.FILL);
                    m.preRotate(mRotation);
                    Bitmap tmp = Bitmap.createBitmap((int) returnRect.width(),
                            (int) returnRect.height(), Bitmap.Config.ARGB_8888);
                    if (tmp != null) {
                        Canvas c = new Canvas(tmp);
                        c.drawBitmap(crop, m, new Paint());
                        crop = tmp;
                    }
                } else if (mRotation > 0) {
                    Matrix m = new Matrix();
                    m.setRotate(mRotation);
                    Bitmap tmp = Bitmap.createBitmap(crop, 0, 0, crop.getWidth(),
                            crop.getHeight(), m, true);
                    if (tmp != null) {
                        crop = tmp;
                    }
                }
                // Get output compression format
                CompressFormat cf =
                        convertExtensionToCompressFormat(getFileExtension(mOutputFormat));

                cropBitmap = crop;//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-08-05,PR1037212
                // If we only need to output to a URI, compress straight to file
                if (mFlags == DO_EXTRA_OUTPUT) {
                    if (mOutStream == null
                            || !crop.compress(cf, DEFAULT_COMPRESS_QUALITY, mOutStream)) {
                        Log.w(LOGTAG, "failed to compress bitmap to file: " + mOutUri.toString());
                        failure = true;
                    } else {
                        mResultIntent.setData(mOutUri);
                    }
                } else {
                    // Compress to byte array
                    ByteArrayOutputStream tmpOut = new ByteArrayOutputStream(2048);
                    if (crop.compress(cf, DEFAULT_COMPRESS_QUALITY, tmpOut)) {

                        // If we need to output to a Uri, write compressed
                        // bitmap out
                        if ((mFlags & DO_EXTRA_OUTPUT) != 0) {
                            if (mOutStream == null) {
                                Log.w(LOGTAG,
                                        "failed to compress bitmap to file: " + mOutUri.toString());
                                failure = true;
                            } else {
                                try {
                                    mOutStream.write(tmpOut.toByteArray());
                                    mResultIntent.setData(mOutUri);
                                } catch (IOException e) {
                                    Log.w(LOGTAG,
                                            "failed to compress bitmap to file: "
                                                    + mOutUri.toString(), e);
                                    failure = true;
                                }
                            }
                        }

                        // If we need to set to the wallpaper, set it
                        if ((mFlags & DO_SET_WALLPAPER) != 0 && mWPManager != null) {
                            if (mWPManager == null) {
                                Log.w(LOGTAG, "no wallpaper manager");
                                failure = true;
                            } else {
                                try {
                                    //[BUGFIX]-Add-BEGIN by TCTNB.(ye.chen), 2014/12/18 PR-872588.
                                //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen, 2015/02/05 PR-913776.
                                    if(drmManager.isDrm(ImageLoader.getFilePath(CropActivity.this,mInUri))){
                                        String filepath = ImageLoader.getFilePath(CropActivity.this,mInUri);
                                        if(TextUtils.isEmpty(filepath)){
                                            filepath = mInUri.toString();
                                            if(filepath.startsWith("file")){
                                                int index = "file://".length();
                                                filepath = filepath.substring(index + 1);
                                            } else {
                                                filepath = drmManager.convertUriToPath(mInUri, CropActivity.this);
                                                  }
                                              }
                                      //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                                        DrmManager.getInstance().drmSetWallpaper(getApplicationContext(),filepath);
                                         }
                                    //[BUGFIX]-Add-END by TCTNB.(ye.chen)
                                    mWPManager.setStream(new ByteArrayInputStream(tmpOut
                                            .toByteArray()));
                                } catch (IOException e) {
                                    Log.w(LOGTAG, "cannot write stream to wallpaper", e);
                                    failure = true;
                                }
                            }
                        }
                    } else {
                        Log.w(LOGTAG, "cannot compress bitmap");
                        failure = true;
                    }
                }
            }
          //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-17, PR949845 begin
          //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen, 2015/03/21 PR-956368.
          //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen, 2015/03/26 PR-958402.
            if(!failure && !DrmManager.getInstance().isDrm(ImageLoader.getFilePath(CropActivity.this,mInUri)) && mCropExtras == null){
              //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen, 2015/03/21 PR-956368.
                File mDestinationFile = SaveImage.getLocalFileFromUri(getBaseContext(),mOutUri);
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-21, PR954782 begin
                if (mDestinationFile != null) {
                    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-08-05,PR1037212 begin
                    ExifInterface exif = getExifData(mSourceUri);
                    if(exif != null && cropBitmap != null ){
                        if(exif.isContainExifInfo()){
                            putExifData(mDestinationFile, mSourceUri, mInUri, cropBitmap, exif);
                        }
                    }
                    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-08-05,PR1037212 end
                    long time = System.currentTimeMillis();
                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 begin
                    ContentValues values = SaveImage.getContentValues(getBaseContext(), mOutUri, mDestinationFile, time, cropBitmap);
                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 end
                    getBaseContext().getContentResolver().update(mOutUri, values, null, null);
                }
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-21, PR954782 end
            }
          //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-17, PR949845 begin
            return !failure; // True if any of the operations failed
        }
      //[BUGFIX]-Add-END by TCTNB.ye.chen, 2014/12/17 PR-872588.
        @Override
        protected void onPostExecute(Boolean result) {
            Utils.closeSilently(mOutStream);
            Utils.closeSilently(mInStream);
            doneBitmapIO(result.booleanValue(), mResultIntent);
        }

    }

    private void done() {
        finish();
    }

    protected static Bitmap getCroppedImage(Bitmap image, RectF cropBounds, RectF photoBounds) {
        RectF imageBounds = new RectF(0, 0, image.getWidth(), image.getHeight());
        RectF crop = CropMath.getScaledCropBounds(cropBounds, photoBounds, imageBounds);
        if (crop == null) {
            return null;
        }
        Rect intCrop = new Rect();
        crop.roundOut(intCrop);
        return Bitmap.createBitmap(image, intCrop.left, intCrop.top, intCrop.width(),
                intCrop.height());
    }

    protected static Bitmap getDownsampledBitmap(Bitmap image, int max_size) {
        if (image == null || image.getWidth() == 0 || image.getHeight() == 0 || max_size < 16) {
            throw new IllegalArgumentException("Bad argument to getDownsampledBitmap()");
        }
        int shifts = 0;
        int size = CropMath.getBitmapSize(image);
        while (size > max_size) {
            shifts++;
            size /= 4;
        }
        Bitmap ret = Bitmap.createScaledBitmap(image, image.getWidth() >> shifts,
                image.getHeight() >> shifts, true);
        if (ret == null) {
            return null;
        }
        // Handle edge case for rounding.
        if (CropMath.getBitmapSize(ret) > max_size) {
            return Bitmap.createScaledBitmap(ret, ret.getWidth() >> 1, ret.getHeight() >> 1, true);
        }
        return ret;
    }

    /**
     * Gets the crop extras from the intent, or null if none exist.
     */
    protected static CropExtras getExtrasFromIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            return new CropExtras(extras.getInt(CropExtras.KEY_OUTPUT_X, 0),
                    extras.getInt(CropExtras.KEY_OUTPUT_Y, 0),
                    extras.getBoolean(CropExtras.KEY_SCALE, true) &&
                            extras.getBoolean(CropExtras.KEY_SCALE_UP_IF_NEEDED, false),
                    extras.getInt(CropExtras.KEY_ASPECT_X, 0),
                    extras.getInt(CropExtras.KEY_ASPECT_Y, 0),
                    extras.getBoolean(CropExtras.KEY_SET_AS_WALLPAPER, false),
                    extras.getBoolean(CropExtras.KEY_RETURN_DATA, false),
                    (Uri) extras.getParcelable(MediaStore.EXTRA_OUTPUT),
                    extras.getString(CropExtras.KEY_OUTPUT_FORMAT),
                    extras.getBoolean(CropExtras.KEY_SHOW_WHEN_LOCKED, false),
                    extras.getFloat(CropExtras.KEY_SPOTLIGHT_X),
                    extras.getFloat(CropExtras.KEY_SPOTLIGHT_Y));
        }
        return null;
    }

    protected static CompressFormat convertExtensionToCompressFormat(String extension) {
        return extension.equals("png") ? CompressFormat.PNG : CompressFormat.JPEG;
    }

    protected static String getFileExtension(String requestFormat) {
        String outputFormat = (requestFormat == null)
                ? "jpg"
                : requestFormat;
        outputFormat = outputFormat.toLowerCase();
        return (outputFormat.equals("png") || outputFormat.equals("gif"))
                ? "png" // We don't support gif compression.
                : "jpg";
    }

    private RectF getBitmapCrop(RectF imageBounds) {
        RectF crop = mCropView.getCrop();
        RectF photo = mCropView.getPhoto();
        if (crop == null || photo == null) {
            Log.w(LOGTAG, "could not get crop");
            return null;
        }
        RectF scaledCrop = CropMath.getScaledCropBounds(crop, photo, imageBounds);
        return scaledCrop;
    }
    //[ALM][BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-04-23,PR186130 begin
    private long availableMemory() {
        ActivityManager am = (ActivityManager) (getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE));
        android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        long availableMemory = mi.availMem;
        Log.d(LOGTAG,
                "<availableMemoryForMavPlayback>current available memory: "
                        + availableMemory);
        return availableMemory;
    }
    //[ALM][BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-04-23,PR186130 end
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-08-05,PR1037212 begin
    public ExifInterface getExifData(Uri mOriginalUri){
        ExifInterface exif = new ExifInterface();
        String mimeType = this.getContentResolver().getType(mOriginalUri);
        if (mimeType == null) {
            mimeType = ImageLoader.getMimeType(mOriginalUri);
            if (mimeType == null) {
                return exif;
            }
        }
        if (mimeType.equals(ImageLoader.JPEG_MIME_TYPE)) {
            InputStream inStream = null;
            try {
                inStream = this.getContentResolver().openInputStream(mOriginalUri);
                exif.readExif(inStream);
            } catch (FileNotFoundException e) {
                Log.w(LOGTAG, "Cannot find file: " + mOriginalUri, e);
            } catch (IOException e) {
                Log.w(LOGTAG, "Cannot read exif for: " + mOriginalUri, e);
            } finally {
                Utils.closeSilently(inStream);
            }
        }else{
             return null;
        }
        return exif;
    }

    public void putExifData(File file,Uri mOriginalUri,Uri destUri,Bitmap bitmap,ExifInterface exif){
        OutputStream s = null;
        try {
            s = exif.getExifWriterStream(file.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG,(90 > 0) ? 90 : 1, s);
            s.flush();
            s.close();
        } catch (FileNotFoundException e) {
            Log.e(LOGTAG, "File not found: " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            Log.e(LOGTAG, "Cannot read exif for: " + mOriginalUri, e);
        }finally{
                Utils.closeSilently(s);
        }
    }
   //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-08-05,PR1037212 end
}

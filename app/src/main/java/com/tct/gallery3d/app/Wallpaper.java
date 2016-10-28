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
/* ----------|----------------------|----------------------|----------------- */
/* 19/12/2014|ye.chen               |PR872588              |[DRM][Gallery2]Cannot set FL/CD/SD image as wallpaper.*/
/* ----------|----------------------|----------------------|----------------- */
/* 04/03/2015|    jialiang.ren     |      PR-928334       |[5.0][Gallery] wallpaper crop UI wrong in settings-display-gallery*/
/* ----------|---------------------|----------------------|------------------------------------------------------------------*/
/* 16/03/2015|ye.chen               |PR947874              |[REG][Download]Set picture as wallpaper,appear ONETOUCH force closed.
/* ----------|----------------------|----------------------|----------------- */
/* 04/21/2015|chengbin.du           |PR982853              |[Gallery01][drm image should be set to wallpaper] */
/* ----------|----------------------|----------------------|----------------- */
/* 22/05/2015|ye.chen               |PR1008065             |[REG][Google GMS][Photos]No response when set wallpaper in google account by gallery
/* ----------|----------------------|----------------------|----------------- */
/* 24/06/2015 |    jialiang.ren     |      PR-358912         |[Gallery]Set picture as wallpepar failed*/
/*------------|---------------------|------------------------|----------------------------------------*/
/* 16/07/2015 |  jialiang.ren  |   PR-420152    |[3rd_APK][Assistant]Unable to tap "choose a */
/*                                               wallpaper" when set a wallpaper by gallery  */
/*------------|----------------|----------------|--------------------------------------------*/
/* 22/07/2015|dongliang.feng        |PR420152              |[3rd_APK][Assistant]Unable to tap "choose a wallpaper" when set a wallpaper by gallery */
/* ----------|----------------------|----------------------|----------------- */
/* 06/08/2015 |  jialiang.ren  |   PR-1062571    |[Android5.1][Gallery_v5.2.0.1.1.0302.0][No response */
/*                                                when set picture attachment as wallpaper in Draft   */
/*------------|----------------|-----------------|--------------------------------------------------- */
/* 22/07/2015|dongliang.feng        |PR1106780             |[End User][File Manager][3rd APK]File Manager works abnormal after download picture from Baidu browser */
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.view.Display;

import com.tct.gallery3d.common.ApiHelper;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.filtershow.cache.ImageLoader;
import com.tct.gallery3d.filtershow.crop.CropActivity;
import com.tct.gallery3d.filtershow.crop.CropExtras;
import com.tct.gallery3d.util.PLFUtils;

/**
 * Wallpaper picker for the gallery application. This just redirects to the
 * standard pick action.
 */
public class Wallpaper extends Activity {
    @SuppressWarnings("unused")
    private static final String TAG = "Wallpaper";

    private static final String IMAGE_TYPE = "image/*";
    private static final String KEY_STATE = "activity-state";
    private static final String KEY_PICKED_ITEM = "picked-item";

    private static final int STATE_INIT = 0;
    private static final int STATE_PHOTO_PICKED = 1;

    private int mState = STATE_INIT;
    private Uri mPickedItem;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle != null) {
            mState = bundle.getInt(KEY_STATE);
            mPickedItem = (Uri) bundle.getParcelable(KEY_PICKED_ITEM);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle saveState) {
        saveState.putInt(KEY_STATE, mState);
        if (mPickedItem != null) {
            saveState.putParcelable(KEY_PICKED_ITEM, mPickedItem);
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private Point getDefaultDisplaySize(Point size) {
        Display d = getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= ApiHelper.VERSION_CODES.HONEYCOMB_MR2) {
            d.getSize(size);
        } else {
            size.set(d.getWidth(), d.getHeight());
        }
        return size;
    }

    @SuppressWarnings("fallthrough")
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        switch (mState) {
            case STATE_INIT: {
                mPickedItem = intent.getData();
                if (mPickedItem == null) {
                    Intent request = new Intent(Intent.ACTION_GET_CONTENT)
                            .setClass(this, DialogPicker.class)
                            .setType(IMAGE_TYPE);
                    startActivityForResult(request, STATE_PHOTO_PICKED);
                    return;
                }
                mState = STATE_PHOTO_PICKED;
                // fall-through
            }
            case STATE_PHOTO_PICKED: {
                Intent cropAndSetWallpaperIntent = null;
              //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2014/12/17 PR-872588.
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    WallpaperManager wpm = WallpaperManager.getInstance(getApplicationContext());
//                    try {
//                        cropAndSetWallpaperIntent = wpm.getCropAndSetWallpaperIntent(mPickedItem);
//                        startActivity(cropAndSetWallpaperIntent);
//                        finish();
//                        return;
//                    } catch (ActivityNotFoundException anfe) {
//                        // ignored; fallthru to existing crop activity
//                    } catch (IllegalArgumentException iae) {
//                        // ignored; fallthru to existing crop activity
//                    }
//                }

                //[BUGFIX]-Add-BEGIN by TCTNB chengbin.du, 2015/04/21 PR-982853 begin.
                if(mPickedItem == null) return;

                String path = ImageLoader.getLocalPathFromUri(this, mPickedItem);
                if(path == null && !isFileStreamExists(mPickedItem)) return;//[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/05/22 PR-1008065.

                if(DrmManager.isDrmEnable && DrmManager.getInstance().isDrm(path)) {
                    cropBySelf(cropAndSetWallpaperIntent);//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-24,PR358912
                } else {
                    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-22, PR420152 begin
                    try {
                        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-08-06,PR1062571 begin
                        Uri uri = parseUriFromPath(mPickedItem.getEncodedPath());
                        Intent cropIntent = WallpaperManager.getInstance(this).getCropAndSetWallpaperIntent(uri == null? mPickedItem : uri);
                        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-08-06,PR1062571 end
                        if (cropIntent != null) {
                            cropIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(cropIntent);
                            finish();
                        } else {
                            cropBySelf(cropAndSetWallpaperIntent);
                        }
                    } catch (IllegalArgumentException e) {
                        Log.w(TAG, e.toString());
                        cropBySelf(cropAndSetWallpaperIntent); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-11-04, PR1106780
                    }
                    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-22, PR420152 end
                }
                //[BUGFIX]-Add-BEGIN by TCTNB chengbin.du, 2015/04/21 PR-982853 end.
            }
        }
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-24,PR358912 begin
    private void cropBySelf(Intent intent) {
        int width = getWallpaperDesiredMinimumWidth();
        int height = getWallpaperDesiredMinimumHeight();
        Point size = getDefaultDisplaySize(new Point());
        float spotlightX = (float) size.x / width;
        float spotlightY = (float) size.y / height;
        intent = new Intent(CropActivity.CROP_ACTION)
            .setClass(this, CropActivity.class)
            .setDataAndType(mPickedItem, IMAGE_TYPE)
            .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            .putExtra(CropExtras.KEY_OUTPUT_X, width)
            .putExtra(CropExtras.KEY_OUTPUT_Y, height)
            .putExtra(CropExtras.KEY_ASPECT_X, width)
            .putExtra(CropExtras.KEY_ASPECT_Y, height)
            .putExtra(CropExtras.KEY_SPOTLIGHT_X, spotlightX)
            .putExtra(CropExtras.KEY_SPOTLIGHT_Y, spotlightY)
            .putExtra(CropExtras.KEY_SCALE, true)
            .putExtra(CropExtras.KEY_SCALE_UP_IF_NEEDED, true)
            .putExtra(CropExtras.KEY_SET_AS_WALLPAPER, true)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-16,PR420152
        startActivity(intent);
        finish();
    }

    private void cropByLauncher(Intent intent) {
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-16,PR420152 begin
        intent = new Intent(CropActivity.CROP_ACTION);
        String fullName = getFullName();
        String[] arr = fullName.split("#");
        String packageName = arr[0];
        String className = arr[1];
        Log.e(TAG, "packageName : " + packageName + " , className : " + className);
        intent.setClassName(packageName, className)
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-16,PR420152 end
        .setAction(WallpaperManager.ACTION_CROP_AND_SET_WALLPAPER)
        .setDataAndType(mPickedItem, IMAGE_TYPE)
        .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/03/16 PR-947874.
        startActivity(intent);
        finish();
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-24,PR358912 end

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-16,PR420152 begin
    private String getFullName() {
        boolean isOrangeLauncher = PLFUtils.getBoolean(this, "def_is_orange_launcher");
        if(isOrangeLauncher) {
            return "com.orange.launcher3#com.orange.launcher3.WallpaperCropActivity";
        } else {
            return "com.tct.launcher#com.tct.launcher.WallpaperCropActivity";
        }
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-16,PR420152 end

  //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/05/22 PR-1008065.
    private boolean isFileStreamExists(Uri uri) {
        InputStream fileStream = null;
        if (uri == null) {
            return false;
        } else {
            try {
                fileStream = getContentResolver().openInputStream(uri);
                if (fileStream == null) {
                    return false;
                }
            } catch (FileNotFoundException e) {
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
  //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/05/22 PR-1008065.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            setResult(resultCode);
            finish();
            return;
        }
        mState = requestCode;
        if (mState == STATE_PHOTO_PICKED) {
            mPickedItem = data.getData();
        }

        // onResume() would be called next
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-08-06,PR1062571 begin
    private Uri parseUriFromPath(String path) {
        Uri uri = null;
        if (path != null) {
            path = Uri.decode(path);
            Log.e(TAG, "path = " + path);
            Cursor cursor = null;
            try {
                Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
                String[] projection = new String[] { Images.ImageColumns._ID };
                String selection = "(" + Images.ImageColumns.DATA + "=" + "'"
                        + path + "'" + ")";
                cursor = getContentResolver().query(baseUri, projection,
                        selection, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(projection[0]);
                    uri = Uri.parse("content://media/external/images/media/"
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
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-08-06,PR1062571 end
}

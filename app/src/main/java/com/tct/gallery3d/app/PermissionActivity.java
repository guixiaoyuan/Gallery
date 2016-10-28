/*
 * Copyright (C) 2009 The Android Open Source Project
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
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/
/* 11/23/2015|chengbin.du-nb        |ALM-975835            |[Android6.0][Gallery_v5.2.4.1.0315.0]Add permission flow*/
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/
/* 12/23/2015|chengbin.du-nb        |ALM-1162997           |[Gallery][App permission]Not Jump to Gallery permission screen when tap setting in gallery */
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/

package com.tct.gallery3d.app;

import com.tct.gallery3d.R;
import com.tct.gallery3d.data.Log;
import com.tct.gallery3d.util.PermissionUtil;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

public class PermissionActivity extends Activity {
    private static final String TAG = "PermissionActivity";

    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 0x1;
    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 begin
    //private static final int REQUEST_ACCESS_LOCATION_PERMISSION = 0x2;

    private static final int NONE_GRANTED = 0x0;
    //private static final int ACCESS_COARSE_LOCATION_GRANTED = 0x01;
    //private static final int ACCESS_FINE_LOCATION_GRANTED = 0x02;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_GRANTED = 0x04;

    //private static final int ACCESS_LOCATION_GRANTED = ACCESS_COARSE_LOCATION_GRANTED
    //                                                    | ACCESS_FINE_LOCATION_GRANTED;
    private static final int ALL_PERMISSION_GRANTED = //ACCESS_COARSE_LOCATION_GRANTED
                                                       //| ACCESS_FINE_LOCATION_GRANTED
                                                       WRITE_EXTERNAL_STORAGE_PERMISSION_GRANTED;

    private boolean mIsNeedRequestPermissions = false;
    private boolean mIsWaitingForGrantWriteStorage = false;
    private int mPermissionCheckResult = 0;

    private RelativeLayout mPermissionMainLayout;
    private RelativeLayout mPermissionWriteStorageLayout;
    //private RelativeLayout mPermissionLocationLayout;
  //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 end

    public PermissionActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        mPermissionCheckResult = checkPermissons();
        if(mPermissionCheckResult == ALL_PERMISSION_GRANTED) {
            initializeByIntent();
        } else {
            mIsNeedRequestPermissions = true;
            setContentView(R.layout.permission);

            mPermissionMainLayout = (RelativeLayout)findViewById(R.id.permission_main);
            mPermissionWriteStorageLayout = (RelativeLayout)findViewById(R.id.permission_write_storage);
            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 begin
            //mPermissionLocationLayout = (RelativeLayout)findViewById(R.id.permission_location);

            int checkResult = mPermissionCheckResult & WRITE_EXTERNAL_STORAGE_PERMISSION_GRANTED;
            if(checkResult == WRITE_EXTERNAL_STORAGE_PERMISSION_GRANTED) {
                mPermissionMainLayout.setVisibility(View.GONE);
                mPermissionWriteStorageLayout.setVisibility(View.GONE);
                //mPermissionLocationLayout.setVisibility(View.VISIBLE);
                //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 end
            }

            Button exit = (Button)findViewById(R.id.permission_exit);
            Button settings = (Button)findViewById(R.id.permission_settings);

            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = null;
                    if(PermissionUtil.checkPermissionIntentAction(PermissionActivity.this)) {
                        intent = new Intent(PermissionUtil.TCT_ACTION_MANAGE_APP);
                        intent.putExtra(PermissionUtil.TCT_EXTRA_PACKAGE_NAME, getPackageName());
                    } else {
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                    }
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");
        if(mIsNeedRequestPermissions) {
            while(true) {
                if(mIsWaitingForGrantWriteStorage) {
                    break;
                }

                int checkResult = mPermissionCheckResult & WRITE_EXTERNAL_STORAGE_PERMISSION_GRANTED;
                if(checkResult == NONE_GRANTED) {
                    new Handler().postDelayed(new Runnable()
                    {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            checkAndRequestWriteStoragePermission();
                        }

                    }, 500);
                    break;
                } else {
                    mIsWaitingForGrantWriteStorage = false;
                }

                //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 begin
                //checkResult = mPermissionCheckResult & ACCESS_LOCATION_GRANTED;
                //if(checkResult != ACCESS_LOCATION_GRANTED) {
                //    mPermissionMainLayout.setVisibility(View.GONE);
                //    mPermissionWriteStorageLayout.setVisibility(View.GONE);
                //    mPermissionLocationLayout.setVisibility(View.VISIBLE);

                    //BUG-FIX For PR1034120 by kaiyuan.ma begin
                //    new Handler().postDelayed(new Runnable()
                //    {

                //        @Override
                //        public void run() {
                //            // TODO Auto-generated method stub
                //            checkAndRequestAccessLocationPermission();
                //      }

                //    }, 1000);
                    //BUG-FIX For PR1034120 by kaiyuan.ma end
                //    break;
                //}
                //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 end

                if(mPermissionCheckResult == ALL_PERMISSION_GRANTED) {
                    initializeByIntent();
                }
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " intent=" + data);
        setResult(resultCode, data);
        finish();
    }

    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 begin
        if (REQUEST_WRITE_STORAGE_PERMISSION == requestCode) {
            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-4-12,ALM-1938213 begin
            if(grantResults != null && grantResults.length > 0) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionCheckResult |= WRITE_EXTERNAL_STORAGE_PERMISSION_GRANTED;
                } else {
                    mIsWaitingForGrantWriteStorage = true;
                    mPermissionMainLayout.setVisibility(View.GONE);
                    mPermissionWriteStorageLayout.setVisibility(View.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_tips_in);
                    mPermissionWriteStorageLayout.setAnimation(animation);
                    //mPermissionLocationLayout.setVisibility(View.GONE);
                }
            }
            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-4-12,ALM-1938213 end
//        } else if(REQUEST_ACCESS_LOCATION_PERMISSION == requestCode) {
//            //[BUGFIX]-Modify by TCTNJ, jun.xie-nb, 2016-01-06, ALM-1307941 begin
//            boolean bPermissionGranted = true;
//            Log.d(TAG, "grantResults.length = " + grantResults.length);
//            for(int i = 0; i < grantResults.length; i++) {
//                bPermissionGranted = bPermissionGranted && (grantResults[i] == PackageManager.PERMISSION_GRANTED);
//                Log.d(TAG, "bPermissionGranted = " + bPermissionGranted);
//            }
//            if(bPermissionGranted) {
//                mPermissionCheckResult |= ACCESS_LOCATION_GRANTED;
//            }
//            //[BUGFIX]-Modify by TCTNJ, jun.xie-nb, 2016-01-06, ALM-1307941 end
//            mIsNeedRequestPermissions = false;
//            initializeByIntent();
        } else {
            Log.e(TAG, "illegal permission!!!");
        }
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 end
    }

    private int checkPermissons() {
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 begin
        int checkResult = 0;
        int[] grantResult = PermissionUtil.checkPermissions(this, new String[] {
                //Manifest.permission.ACCESS_COARSE_LOCATION,
                //Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE } );
        //if(grantResult[0] == PackageManager.PERMISSION_GRANTED) {
        //    checkResult |= ACCESS_COARSE_LOCATION_GRANTED;
        //}
        //if(grantResult[1] == PackageManager.PERMISSION_GRANTED) {
        //    checkResult |= ACCESS_FINE_LOCATION_GRANTED;
        //}
        if(grantResult[0] == PackageManager.PERMISSION_GRANTED) {
            checkResult |= WRITE_EXTERNAL_STORAGE_PERMISSION_GRANTED;
        }
        return checkResult;
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 end
    }

    private void checkAndRequestWriteStoragePermission() {
        PermissionUtil.checkAndRequestPermissions(this,
                new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                REQUEST_WRITE_STORAGE_PERMISSION );
    }

      //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 begin
//    private void checkAndRequestAccessLocationPermission() {
//        PermissionUtil.checkAndRequestPermissions(this,
//                new String[] {
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.ACCESS_FINE_LOCATION },
//                REQUEST_ACCESS_LOCATION_PERMISSION );
//    }
      //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-18,ALM-1840441 end

    protected void initializeByIntent() {
        Intent intent = getIntent();
        Log.d(TAG, "intent=" + intent);
        String action = intent.getAction();
        if(Intent.ACTION_MAIN.equals(action)) {
            intent = new Intent();
        }

        intent.setClassName(getApplicationContext(),
                PermissionUtil.com_tct_gallery3d_app_GalleryActivity);
        boolean isContains = isContainsFlag(intent.getFlags(), Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        if((Intent.ACTION_GET_CONTENT.equals(action)
                || Intent.ACTION_PICK.equals(action)) && !isContains) {
            startActivityForResult(intent, 0);
        } else {
        //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-21,PR1271652 begin
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-01-21,PR1271652 end
            startActivity(intent);
            finish();
        }
    }

    private boolean isContainsFlag(int flag, int targetFlag) {
        return (flag & targetFlag) == targetFlag;
    }
}

package com.tct.gallery3d.app;

/* MODIFIED-BEGIN by wencan.wu1, 2016-10-29,BUG-3079416*/
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.mtk.drm.frameworks.MtkDrmManager;
import com.tct.gallery3d.R;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.data.LocalImage;
import com.tct.gallery3d.data.LocalMediaItem;
import com.tct.gallery3d.data.Log;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaObject;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.filtershow.cache.ImageLoader;
import com.tct.gallery3d.util.PermissionUtil;

import java.io.File;
import java.security.PublicKey;

public class MoviePermissionActivity extends PermissionActivity {
    private static final String TAG = "MoviePermissionActivity";
    private Intent mIntent;

    protected void initializeByIntent() {
        Intent intent = getIntent();
        Log.d(TAG, "intent=" + intent);
        if (intent != null) {
            Uri uri = intent.getData();

            if (DrmManager.isDrmEnable && DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                String path = ImageLoader.getFilePath(getApplicationContext(), uri);
                if (Environment.isExternalStorageEmulated()) {
                    if (path != null && path.contains(GalleryConstant.SDCARD_PATH)) {
                        path = path.replace(GalleryConstant.SDCARD_PATH, GalleryConstant.EMULATED_PATH);
                    }
                }
                if (path != null && DrmManager.getInstance().isDrm(path)) {
                    mIntent = intent;
                    try {
                        if (!checkDrmPlayRight(this, path)) {
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        intent.setClassName(getApplicationContext(),
                PermissionUtil.com_tct_gallery3d_app_MovieActivity);
        startActivity(intent);
        finish();
    }

    public boolean checkDrmPlayRight(Context Context, final String path) {
        if (path == null) {
            return true;
        }
        final Context sContext = Context;
        final String sPath = path;

        // Check the MediaItem right status.

        // Check the MediaItem DRM scheme.
        int drmType = DrmManager.getInstance().getDrmScheme(path);

        if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
            if (drmType != DrmManager.DRM_SCHEME_OMA1_FL) {
                showMtkDrmDialog(sPath);
                return false;
            }
        }
        boolean isValid = DrmManager.getInstance().isRightsStatus(path);
        if (isValid) {
            return true;
        }
        int title;
        String message;
        DialogInterface.OnClickListener listener;
        if (drmType == DrmManager.DRM_SCHEME_OMA1_SD) {
            title = R.string.app_name;
            File file = new File(sPath);
            message = String.format(sContext.getString(R.string.drm_unlock_invalid_content), file.getName());
            listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int which) {
                    //open rights-Issuer url in web browser to purchase sd right object
                    DrmManager.getInstance().activateContent(sContext, path);
                }
            };

        } else if (drmType == DrmManager.DRM_SCHEME_OMA1_FL) {
            Toast.makeText(sContext, R.string.drm_no_valid_right, Toast.LENGTH_SHORT).show();
            finish();
            return false;
        } else {
            Toast.makeText(sContext, R.string.drm_no_valid_right, Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        new AlertDialog.Builder(sContext).setTitle(title).setMessage(message)
                .setPositiveButton(android.R.string.yes, listener)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                })
          .setNegativeButton(android.R.string.no, null).show();
        return false;
    }

    private void showMtkDrmDialog(String path) {
        int rights = DrmManager.getInstance().checkRightsStatus(path, MtkDrmManager.Action.PLAY);
        final Context context = this;
        if (MtkDrmManager.RightsStatus.RIGHTS_VALID == rights) {
            DrmManager.getInstance().showConsumeDialog(context,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (DialogInterface.BUTTON_POSITIVE == which) {
                                mIntent.setClassName(context,
                                        PermissionUtil.com_tct_gallery3d_app_MovieActivity);
                                startActivity(mIntent);
                            }
                            dialog.dismiss();
                        }
                    },
                    new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    }
            );
        } else {
            if (MtkDrmManager.RightsStatus.SECURE_TIMER_INVALID == rights) {
                DrmManager.getInstance().showSecureTimerInvalidDialog(context,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }, new DialogInterface.OnDismissListener() {
                            public void onDismiss(DialogInterface dialog) {
                                finish();
                            }
                        });
            } else {
                Dialog dialog = DrmManager.getInstance().showRefreshLicenseDialog(context, path);
                if (dialog != null) {
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogInterface dialog) {
                                                        finish();
                                                    }
                                                }
                    );
                }
            }
        }

    }
    /* MODIFIED-END by wencan.wu1,BUG-3079416*/
}

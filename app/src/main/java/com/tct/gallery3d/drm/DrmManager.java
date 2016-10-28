/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* ----------|----------------------|----------------------|----------------- */
/*    date   |        Author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 12/16/2014|     peng.tian        |    PR835313          |OMA DRM image cann't be played*/
/* ----------|----------------------|----------------------|----------------- */
/* 02/02/2015|     ye.chen          |    PR907464          |[TV LINK][HDCP]JPG, GIF DRM files in the extended model can't play
/* ----------|----------------------|----------------------|----------------- */
/* 02/06/2015|     jian.pan1        |    PR925646          |The Gallery force stop when open a gif file in the .eml file*/
/* ----------|----------------------|----------------------|----------------- */
/* ========================================================================== */
/* 15/02/2015|ye.chen               |PR932969              |[Download]SD format file can't be set to contact the pictures.
/* ----------|----------------------|----------------------|----------------- */
/* 04/01/2015|ye.chen               |PR916400              |[GenericApp][Gallery]MTK DRM adaptation
/* ----------|----------------------|----------------------|----------------- */
/* 23/04/2015|    qiang.ding1       |      PR-186130       |[Download][DRM]Can't set DRM image as wallpaper*/
/* ----------|------------------- --|----------------------|------------------------------------------------*/
/* 01/06/2015|dongliang.feng        |PR1015281             |[REG][Wi-Fi display][Wi-Fi Transfer][ANR]The video cannot play */
/*           |                      |                      |and some APK will ANR when have video in Wi-Fi Transfer */
/* ----------|----------------------|----------------------|----------------- */
/* 10/07/2015 |    jialiang.ren     |      PR-1041063         |[SW][Video Streaming]Video Streaming for long time occur no reresponding*/
/*------------|---------------------|-------------------------|------------------------------------------------------------------------*/
/* 13/07/2015 |    su.jiang     |      PR-1038196   |[Android 5.1][Gallery_v5.1.13.1.0211.0]The Current Constraint shows error in--------*/
/*------------|-----------------|-------------------|count file after sliding show-------------------------------------------------------*/

package com.tct.gallery3d.drm;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//[BUGFIX]-Add-BEGIN by NJTS.Peng.Tian,12/16/2014,PR835313
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Movie;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.data.LocalImage;
import com.tct.gallery3d.data.LocalMediaItem;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.filtershow.cache.ImageLoader;
import com.tct.gallery3d.ui.Log;
//[BUGFIX]-ADD-END by NJTS.Peng.Tian

import com.mediatek.omadrm.MtkDrmManager;
import com.tct.drm.api.TctDrmManager;

public class DrmManager {

    private static final String TAG = "DrmManager";
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
    public static final int NO_DRM = -1;
    public static final int MTK_DRM = 10;
    public static final int QCOM_DRM = 20;
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400

    public static final int ACTIONID_NOT_DRM = -1;
    public static final int ACTIONID_INVALID_DRM = -2;

    public static final int DRM_THUMBNAIL_WITH = 48;
    public static final String APP_DRM = "application/vnd.oma.drm";
    public static final String EXT_DRM_CONTENT = "dcf";

    public static final String REMAINING_REPEAT_COUNT = DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT;
    public static final String LICENSE_START_TIME = DrmStore.ConstraintsColumns.LICENSE_START_TIME;
    public static final String LICENSE_EXPIRY_TIME = DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME;
    public static final String LICENSE_AVAILABLE_TIME = DrmStore.ConstraintsColumns.LICENSE_AVAILABLE_TIME;

    public static int DRM_SCHEME_OMA1_FL;
    public static int DRM_SCHEME_OMA1_CD;
    public static int DRM_SCHEME_OMA1_SD;
    public static String RIGHTS_ISSUER;
    public static String CONSTRAINT_TYPE;
    public static String CONTENT_VENDOR;
    public static String TCT_IS_DRM = TctDrmManager.TCT_IS_DRM;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
    public static final String TCT_DRM_TYPE = TctDrmManager.TCT_DRM_TYPE;
    public static final String TCT_DRM_RIGHT_TYPE = TctDrmManager.TCT_DRM_RIGHT_TYPE;
    public static final String TCT_DRM_VALID = TctDrmManager.TCT_DRM_VALID;
    public static final String DRM_TIME_OUT_ACTION = TctDrmManager.DRM_TIME_OUT_ACTION;

  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
    public static final String TCT_DRM_METHOD = MtkDrmManager.DRM_METHOD;
    public static int mCurrentDrm = NO_DRM;
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
    public static boolean isDrmEnable = false;
    private static DrmManager sInstance = new DrmManager();

    public TctDrmManager mTctDrmManager = null;
    public MtkDrmManager mMtkDrmManager = null;
    public DrmManagerClient mDrmManagerClient = null;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public static void setScheme() {
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    DRM_SCHEME_OMA1_FL = MtkDrmManager.DRM_SCHEME_OMA1_FL;
                    DRM_SCHEME_OMA1_CD = MtkDrmManager.DRM_SCHEME_OMA1_CD;
                    DRM_SCHEME_OMA1_SD = MtkDrmManager.DRM_SCHEME_OMA1_SD;
                    RIGHTS_ISSUER = MtkDrmManager.RIGHTS_ISSUER;
                    CONSTRAINT_TYPE = TctDrmManager.CONSTRAINT_TYPE;
                    CONTENT_VENDOR = MtkDrmManager.CONTENT_VENDOR;
                    TCT_IS_DRM = MtkDrmManager.TCT_IS_DRM;
                  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    break;
                case QCOM_DRM:
                    DRM_SCHEME_OMA1_FL = TctDrmManager.DRM_SCHEME_OMA1_FL;
                    DRM_SCHEME_OMA1_CD = TctDrmManager.DRM_SCHEME_OMA1_CD;
                    DRM_SCHEME_OMA1_SD = TctDrmManager.DRM_SCHEME_OMA1_SD;
                    RIGHTS_ISSUER = TctDrmManager.RIGHTS_ISSUER;
                    CONSTRAINT_TYPE = TctDrmManager.CONSTRAINT_TYPE;
                    CONTENT_VENDOR = TctDrmManager.CONTENT_VENDOR;
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * Constructor for DrmManager.
     */
    public DrmManager() {
        mCurrentDrm = getDrmPlatform();
        setScheme();
    }

    /**
     * Initial the TctDrmManagerClient.
     *
     * @param context The context to use.
     */
    public void init(Context context) {
        if (isDrmEnable) {
            if (mDrmManagerClient == null) {
                mDrmManagerClient = new DrmManagerClient(context);
            }
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager == null) {
                        mMtkDrmManager = MtkDrmManager.getInstance(context);
                    }
                    break;
                case QCOM_DRM:
                    if (mTctDrmManager == null) {
                        mTctDrmManager = new TctDrmManager(context);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Get a DrmManager Object. init() must be called before using it.
     *
     * @return a instance of DrmManager.
     */
    public static DrmManager getInstance() {
        return sInstance;
    }

    private boolean isMTKDrm() {
        try {
            Class<?> managerClass = Class.forName("com.mediatek.drm.OmaDrmClient");
            if (managerClass.getClass() != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (LinkageError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isQcomDrm() {
        try {
            Class<?> managerClass = Class.forName("com.tct.drm.TctDrmManagerClient");
            if (managerClass.getClass() != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (LinkageError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getDrmPlatform() {
        if (isQcomDrm()) {
            isDrmEnable = TctDrmManager.isDrmEnabled();
            return QCOM_DRM;
        } else if (isMTKDrm()) {
            isDrmEnable = MtkDrmManager.isDrmEnabled();
            return MTK_DRM;
        } else {
            isDrmEnable = false;
            return NO_DRM;
        }
    }
    /**
     * This method gets Bitmap of DRM file. (Draw a little lock icon at
     * right-down part over original icon)
     *
     * @param resources the resource to use
     * @param path absolute path of the DRM file
     * @param actionId action ID of the file, which is not unique for DRM file
     * @param iconId the ID of background icon, which the new icon draws on
     * @return Bitmap of the DRM file
     */
    public Bitmap overlayDrmIconSkew(Resources resources, String path, int actionId, int iconId) {
        Bitmap bitmap = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        bitmap = mMtkDrmManager.overlayDrmIconSkew(resources, path, actionId,
                                iconId);
                    }
                    break;
                case QCOM_DRM:
                    bitmap = TctDrmManager.getDrmThumbnail(path, 48);
                    break;
                default:
                    break;
            }
        }
        return bitmap;
    }

    /**
     * Get original mimeType of a file.
     *
     * @param path The file's path.
     * @return original mimeType of the file.
     */
    public String getOriginalMimeType(String path) {
        if (isDrmEnable) {
            if (mDrmManagerClient != null) {
                return mDrmManagerClient.getOriginalMimeType(path);
            }
        }
        return "";
    }

    /**
     * This method check weather the rights-protected content has valid right to
     * transfer.
     *
     * @param path path to the rights-protected content.
     * @return true for having right to transfer, false for not having the
     *         right.
     */
    public boolean canTransfer(String path) {
        boolean flag = false;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        flag = mMtkDrmManager.checkRightsStatus(path, DrmStore.Action.TRANSFER)
                                == DrmStore.RightsStatus.RIGHTS_VALID;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    }
                    break;
                case QCOM_DRM:
                    flag = TctDrmManager.checkRightsStatus(path, DrmStore.Action.TRANSFER)
                            != DrmStore.RightsStatus.RIGHTS_VALID;
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    /**
     * check weather the rights-protected content has valid right or not
     *
     * @param path path to the rights-protected content.
     * @return true for having valid right, false for invalid right
     */
    public boolean isRightsStatus(String path) {
        boolean flag = false;
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-06,PR925646 begin
        try {
            if (isDrmEnable) {
                switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        flag = mMtkDrmManager.isRightValid(path);
                    }
                    break;
                case QCOM_DRM:
                    flag = mTctDrmManager.isRightValid(path);
                    break;
                default:
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "TctDrmManager error:" + e.getMessage());
        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-06,PR925646 end
        return flag;
    }

    /**
     * This static method check a file is DRM file, or not.
     *
     * @param fileName the file which need to be checked.
     * @return true for DRM file, false for not DRM file.
     */
    public static boolean isDrmFileExt(String fileName) {
        if (isDrmEnable) {
            String extension = null;
            final int lastDot = fileName.lastIndexOf('.');
            if ((lastDot > 0)) {
                extension = fileName.substring(lastDot + 1).toLowerCase();
            }
            if (extension != null && extension.equalsIgnoreCase(EXT_DRM_CONTENT)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDrm(String path) {
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-01, PR1015281 begin
        if (path == null) {
            return false;
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-06-01, PR1015281 end
        try {
            return executorService.submit(new newThread(path)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private class newThread implements Callable<Boolean> {
        private String path = null;

        public newThread(String path) {
            this.path = path;
        }

        public Boolean call() {
            boolean flag = new Boolean(false);
            if (isDrmEnable) {
                switch (mCurrentDrm) {
                    case MTK_DRM:
                        if (mMtkDrmManager != null) {
                            flag = mMtkDrmManager.isDrm(path);
                        }
                        break;
                    case QCOM_DRM:
                        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-10,PR1041063 begin
                        if(mTctDrmManager != null) {
                            flag = new Boolean(mTctDrmManager.isDrm(path));
                        }
                        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-10,PR1041063 end
                        break;
                    default:
                        break;
                }
            }
            return flag;
        }
    }

    public Bitmap getDrmVideoThumbnail(Bitmap bitmap, String filePath, int size) {
        Bitmap b = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                  //there is no getDrmVideoThumbnail fucntion in mtk platform
                    b = bitmap;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    break;
                case QCOM_DRM:
                    b = TctDrmManager.getDrmVideoThumbnail(bitmap, filePath, size);
                    break;
                default:
                    break;
            }
        }
        return b;
    }

    public Bitmap getDrmThumbnail(String filePath, int size) {
        Bitmap b = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        b = mMtkDrmManager.getDrmThumbnail(filePath, size);//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    }
                    break;
                case QCOM_DRM:
                    b = TctDrmManager.getDrmThumbnail(filePath, size);
                    break;
                default:
                    break;
            }
        }
        return b;
    }

    public Bitmap getDrmRealThumbnail(String filePath, BitmapFactory.Options options, int size) {
        Bitmap b = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        b = mMtkDrmManager.getDrmThumbnail(filePath, size);//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    }
                    break;
                case QCOM_DRM:
                    b = TctDrmManager.getDrmRealThumbnail(filePath, options, size);
                    break;
                default:
                    break;
            }
        }
        return b;
    }
  //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/02/15 PR-932969.
    public boolean isDrmSDFile(String path) {
        boolean flag = false;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        flag = mMtkDrmManager.isSdType(path);
                    }
                    break;
                case QCOM_DRM:
                    flag = mTctDrmManager.isSDType(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    public boolean isDrmCDFile(String path) {
        boolean flag = false;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        flag = mMtkDrmManager.isCDType(path);//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    }
                    break;
                case QCOM_DRM:
                    flag = mTctDrmManager.isCDType(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }
  //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/02/15 PR-932969.
    public int getDrmScheme(String path) {
        int flag = TctDrmManager.DRM_SCHEME_OMA1_FL;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        flag = mMtkDrmManager.getDrmScheme(path);
                    }
                    break;
                case QCOM_DRM:
                    flag = TctDrmManager.getDrmScheme(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    public ContentValues getMetadata(String path) {
        ContentValues c = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        c = mMtkDrmManager.getMetadata(path);
                    }
                    break;
                case QCOM_DRM:
                    c = TctDrmManager.getMetadata(path);
                    break;
                default:
                    break;
            }
        }
        return c;
    }

    public ContentValues getConstraints(String path, int action) {
        ContentValues c = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        c = mMtkDrmManager.getConstraints(path, action);
                    }
                    break;
                case QCOM_DRM:
                    c = TctDrmManager.getConstraints(path, action);
                    break;
                default:
                    break;
            }
        }
        return c;
    }

    public boolean isAllowForward(String path) {
        boolean flag = true;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        flag = mMtkDrmManager.isAllowForward(path);
                    }
                    break;
                case QCOM_DRM:
                    flag = TctDrmManager.isAllowForward(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

  //[BUGFIX]-Add-BEGIN by NJTS.ye.chen,02/02/2015,PR907464
    public Bitmap getDrmBitmap(String path){
        Bitmap bitmap = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        bitmap = mMtkDrmManager.getDrmThumbnail(path, 200);//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    }
                    break;
                case QCOM_DRM:
                    if (mTctDrmManager != null){
                        bitmap = mTctDrmManager.getDrmBitmap2(path);//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-07-13,PR1038196
                    }
                    break;
                default:
                    break;
              }
         }
        return bitmap;
    }
  //[BUGFIX]-Add-BEGIN by NJTS.ye.chen,02/02/2015,PR907464

    //[BUGFIX]-Add-BEGIN by NJTS.Peng.Tian,12/16/2014,PR835313
    public Movie getMovie(Uri uri,Context context){
        Movie movie = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        movie = mMtkDrmManager.getMovie(convertUriToPath(uri,context));//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    }
                    break;
                case QCOM_DRM:
                    if (mTctDrmManager != null){
                        movie = TctDrmManager.getMovie(uri, context);
                    }
                    break;
                default:
                    break;
              }
         }
         return movie;
    }

    public Movie getMovie(String path){
        Movie movie = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    if (mMtkDrmManager != null) {
                        movie = mMtkDrmManager.getMovie(path);//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                     }
                    break;
                case QCOM_DRM:
                    if (mTctDrmManager != null){
                        movie = TctDrmManager.getMovie(path);
                    }
                    break;
                default:
                    break;
             }
         }
        return movie;
    }

   public String convertUriToPath(Uri uri, Context context) {
        String path = null;
        if (null != uri) {
            String scheme = uri.getScheme();
            if (null == scheme || scheme.equals("")
                    || scheme.equals(ContentResolver.SCHEME_FILE)) {
                path = uri.getPath();

            } else if (scheme.equals("http")) {
                path = uri.toString();

            } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                String[] projection = new String[] { MediaStore.MediaColumns.DATA };
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(uri,
                            projection, null, null, null);
                    if (null == cursor || 0 == cursor.getCount()
                            || !cursor.moveToFirst()) {
                        Log.e(Uri.class.getSimpleName(),
                                "Given Uri could not be found"
                                        + " in media store");
                        return null;
                    }
                    int pathIndex = cursor
                            .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    path = cursor.getString(pathIndex);
                } catch (SQLiteException e) {
                    Log.e(Uri.class.getSimpleName(),
                            "Given Uri is not formatted in a way "
                                   + "so that it can be found in media store.");
                    return null;
                    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-06,PR925646 begin
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "e:" + e.getMessage());
                    return null;
                    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-06,PR925646 end
                } finally {
                    if (null != cursor) {
                        cursor.close();
                    }
                }
            } else {
                  Log.e(Uri.class.getSimpleName(),
                        "Given Uri scheme is not supported");
                return null;
            }
        }
        return path;
    }

    //[BUGFIX]-Add-END by NJTS.Peng.Tian
 //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400
   public void activateContent(Context context, String filepath) {
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   if (mMtkDrmManager != null) {
                       String drmtoast = context.getResources().getString(R.string.drm_toast_license_expired);
                       mMtkDrmManager.activateContent(context, filepath,drmtoast);
                   }
                   break;
               case QCOM_DRM:
                   mTctDrmManager.activateContent(context, filepath);
                   break;
               default:
               break;
           }
       }
   }

   public boolean hasCountConstraint(String filePath) {
       boolean flag = false;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   if (mMtkDrmManager != null) {
                       flag = mMtkDrmManager.hasCountConstraint(filePath);
                   }
                   break;
               case QCOM_DRM:
                   flag = mTctDrmManager.hasCountConstraint(filePath);
                   break;
               default:
                   break;
           }
       }
       return flag;
   }

   public int checkRightsStatus(String path, int action) {
       int result = -1;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   if (mMtkDrmManager != null) {
                       result = mMtkDrmManager.checkRightsStatus(path, action);//Task134580 support DRM in mtk platform by fengke at 2015.03.5
                   }
                   break;
               case QCOM_DRM:
                   result = TctDrmManager.checkRightsStatus(path, action);
                   break;
               default:
                   break;
           }
       }
       return result;
   }

   public void drmSetWallpaper(Context context, String filepath) {
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   if (mMtkDrmManager != null) {
                       if (!mMtkDrmManager.drmSetAsWallpaper(context, filepath)) {
                           String toastMsg = String.format(context.getResources().getString(R.string.drm_no_crop), filepath);
                           Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
                           return;
                       }
                       if ((filepath != null) && (!"".equals(filepath)) && (new File(filepath)).exists()) {
                           Settings.System.putString(context.getContentResolver(), MtkDrmManager.WALLPAPER_FILEPATH, filepath);
//                           mMtkDrmManager.watchingDrmWallpaperStatus(context, filepath);
                       }
                   }
                   break;
               case QCOM_DRM:
                   Settings.System.putString(context.getContentResolver(),mTctDrmManager.NEW_WALLPAPER_DRMPATH,filepath);
                   break;
               default:
                   break;
           }
       }
   }
   public Bitmap getThumbnailConsume(String path){
       Bitmap bitmap = null;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   if (mMtkDrmManager != null) {
                       bitmap = mMtkDrmManager.getThumbnailConsume(path, 640);
                   }
                   break;
               case QCOM_DRM:
//                   if (mTctDrmManager != null){
//                       bitmap = mTctDrmManager.getDrmBitmap(path);
//                   }
                   break;
               default:
                   break;
             }
        }
       return bitmap;
   }

   public boolean hasRightsToShow(Context context, String filePath) {
       boolean result = false;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   if (mMtkDrmManager != null) {
                       result = mMtkDrmManager.hasRightsToShow(context, filePath);
                   }
                   break;
               case QCOM_DRM:
                   //result = TctDrmManager.checkRightsStatus(path, action);
                   break;
               default:
                   break;
           }
       }
       return result;
   }

//   public Dialog showConsumeDialog(Context context,Uri uri,
//           DialogInterface.OnClickListener listener) {
//       Dialog result = null;
//       if (isDrmEnable) {
//           switch (mCurrentDrm) {
//               case MTK_DRM:
//                   if (mMtkDrmManager != null) {
//                       mMtkDrmManager.showConsumeDialog(context,mDrmManagerClient,uri,listener);
//                   }
//                   break;
//               case QCOM_DRM:
//                   break;
//               default:
//                   break;
//           }
//       }
//       return result;
//   }

//   public Dialog showSecureTimerInvalidDialog(Context context,
//           DialogInterface.OnClickListener clickListener,
//           DialogInterface.OnDismissListener dismissListener) {
//       Dialog result = null;
//       if (isDrmEnable) {
//           switch (mCurrentDrm) {
//               case MTK_DRM:
//                   if (mMtkDrmManager != null) {
//                       result = mMtkDrmManager.showSecureTimerInvalidDialog(context, clickListener, dismissListener);
//                   }
//                   break;
//               case QCOM_DRM:
//                   break;
//               default:
//                   break;
//           }
//       }
//       return result;
//   }



//   public Dialog showRefreshLicenseDialog(Context context, String path) {
//       Dialog result = null;
//       if (isDrmEnable) {
//           switch (mCurrentDrm) {
//               case MTK_DRM:
//                   if (mMtkDrmManager != null) {
//                       result = mMtkDrmManager.showRefreshLicenseDialog(context, path);
//                   }
//                   break;
//               case QCOM_DRM:
//                   break;
//               default:
//                   break;
//           }
//       }
//       return result;
//   }

   public int consumeRights(String path, int action) {
       int result = mMtkDrmManager.ERROR_UNKNOWN;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   if (mMtkDrmManager != null) {
                       result = mMtkDrmManager.consumeRights(path, action);
                   }
                   break;
               case QCOM_DRM:
                   break;
               default:
                   break;
           }
       }
       return result;
   }
 //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400
 //[ALM][BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-04-23,PR186130 begin
   public static byte[] forceDecryptFile(String filePath, boolean consume) {
       return MtkDrmManager.forceDecryptFile(filePath, consume);
   }
   //[ALM][BUGFIX]-Add by TCTNJ,qiang.ding1, 2015-04-23,PR186130 end

    public static boolean checkDrmPlayRight(AbstractGalleryActivity activity, final MediaItem item) {
        final Context context = activity.getAndroidContext();
        final Uri itemUri = item.getContentUri();
        if (DrmManager.isDrmEnable && item.isDrm() == GalleryConstant.ITEM_IS_DRM) {

            if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                LocalMediaItem.updateDrmRight(item);
            }
            boolean isValid = DrmManager.getInstance().isRightsStatus(ImageLoader.getLocalPathFromUri(context, itemUri));
            // Check the MediaItem right status.
            if (isValid) {
                return true;
            }

            // Check the MediaItem DRM scheme.
            int drmType = DrmManager.getInstance().getDrmScheme(item.getFilePath());

            int title;
            String message;
            DialogInterface.OnClickListener listener;
            if (drmType == DrmManager.DRM_SCHEME_OMA1_SD) {
                title = R.string.app_name;
                message = String.format(context.getString(R.string.drm_unlock_invalid_content), item.getName());
                listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dlg, int which) {
                        //open rights-Issuer url in web browser to purchase sd right object
                        DrmManager.getInstance().activateContent(context, ImageLoader.getLocalPathFromUri(context, itemUri));
                    }
                };
            } else if (drmType == DrmManager.DRM_SCHEME_OMA1_FL) {
                if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                    if (item instanceof LocalImage) {
//                        showMtkDrmDialog(context, item);
                        return false;
                    }
                }
                Toast.makeText(context, R.string.drm_no_valid_right, Toast.LENGTH_SHORT).show();
                return false;
            } else {
                title = R.string.delete;
                message = String.format(context.getString(R.string.drm_delete_invalid_content), item.getName());
                listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dlg, int which) {
                        try {
                            item.delete();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return;
                        }
                    }
                };
            }
            new AlertDialog.Builder(context).setTitle(title).setMessage(message)
                    .setPositiveButton(android.R.string.yes, listener)
                    .setNegativeButton(android.R.string.no, null).show();
            return false;
        }
        return true;
    }

//    private static void showMtkDrmDialog(Context context, MediaItem item) {
//        final LocalImage imageItem = (LocalImage) item;
//        int rights = DrmManager.getInstance().checkRightsStatus(imageItem.filePath, MtkDrmManager.Action.DISPLAY);
//        com.tct.gallery3d.app.Log.w(TAG, "DRM showMtkDrmDialog rights= " + rights);
//
//        if (MtkDrmManager.RightsStatus.RIGHTS_VALID != rights) {
//            if (MtkDrmManager.RightsStatus.SECURE_TIMER_INVALID == rights) {
//                DrmManager.getInstance().showSecureTimerInvalidDialog(context,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        }, null);
//            } else {
//                DrmManager.getInstance().showRefreshLicenseDialog(context, imageItem.filePath);
//            }
//        }
//    }
}

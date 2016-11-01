/*
 * Copyright (C) 2010 The Android Open Source Project
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
/* 03/12/2015| jian.pan1            | PR939075             |[5.0][Gallery] No option to share, edit,
 *           |                      |                      |"set as", etc. when viewing screenshot from notification
/* ----------|----------------------|----------------------|----------------- */
/* 03/16/2015|ye.chen               |PR936956              |[Clone][4.7][Downloads]Sometimes set the sd image as wallpaper will display "cannot load the image!"
/* ----------|----------------------|----------------------|----------------- */
/* 04/01/2015|ye.chen               |PR916400              |[GenericApp][Gallery]MTK DRM adaptation
/* ----------|----------------------|----------------------|----------------- */
/* 14/10/2015|chengbin.du-nb        |ALM-676093            |[Android 5.1][Gallery_v5.2.2.1.1.0305.0]The response time of switching Month view to Day view is too long.*/
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import android.content.ContentResolver;
import android.content.Context;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.mtk.drm.frameworks.MtkDrmManager;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.PanoramaMetadataSupport;
import com.tct.gallery3d.common.BitmapUtils;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.filtershow.cache.ImageLoader;
import com.tct.gallery3d.util.ThreadPool.CancelListener;
import com.tct.gallery3d.util.ThreadPool.Job;
import com.tct.gallery3d.util.ThreadPool.JobContext;

public class UriImage extends MediaItem {
    private static final String TAG = "UriImage";

    private static final int STATE_INIT = 0;
    private static final int STATE_DOWNLOADING = 1;
    private static final int STATE_DOWNLOADED = 2;
    private static final int STATE_ERROR = -1;

    private final Uri mUri;
    private final Path mPath;//[FEATURE]-Add by ye.chen,11/10/2014,support drm
    private final String mContentType;

    private DownloadCache.Entry mCacheEntry;
    private ParcelFileDescriptor mFileDescriptor;
    private int mState = STATE_INIT;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private PanoramaMetadataSupport mPanoramaMetadata = new PanoramaMetadataSupport(this);

    private GalleryApp mApplication;

    private boolean misAvlid = false;

    public UriImage(GalleryApp application, Path path, Uri uri, String contentType) {
        super(path, nextVersionNumber());
        mUri = uri;
        mPath = path;//[FEATURE]-Add by ye.chen,11/10/2014,support drm
        mApplication = Utils.checkNotNull(application);
        mContentType = contentType;
        initDrm();//[FEATURE]-Add by ye.chen,11/10/2014,support drm
    }
  //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
    private void initDrm(){
        if(isDrmEnable){
            //[BUGFIX]-Add-BEGIN by TSCD.linbo.wu,07/15/2014,PR-695258,
            //[DRM]Can't open DRM picture
            boolean isDrmtemp = DrmManager.getInstance().isDrm(ImageLoader.getLocalPathFromUri(mApplication.getAndroidContext(), mUri));
            if (isDrmtemp) {
                isDrm = 1;
            } else {
                isDrm = 0;
            }
            //[BUGFIX]-Add-END by TSCD.linbo.wu
          //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
            if (DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM) {
                isSupportForward = (isDrm == 1) && ( mTctDrmType == DrmManager.DRM_SCHEME_OMA1_SD);
                isSupportSetWallpaper = (isDrm == 1) && ( !"count".equals(mTctDrmRightType));
                isRightValid = (isDrm == 1) && (mTctDrmRightValid == 1);
            } else if (DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                String filePath = ImageLoader.getLocalPathFromUri(mApplication.getAndroidContext(), mUri);
                isSupportForward = (isDrm == 1) && (filePath != null) && (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                    DrmManager.getInstance().checkRightsStatus(filePath,MtkDrmManager.Action.TRANSFER));
                isSupportSetWallpaper = (isDrm == 1) && (filePath != null) && (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                    DrmManager.getInstance().checkRightsStatus(filePath,MtkDrmManager.Action.WALLPAPER));
                isRightValid = (isDrm == 1) && (filePath != null) && (MtkDrmManager.RightsStatus.RIGHTS_VALID ==
                    DrmManager.getInstance().checkRightsStatus(filePath,MtkDrmManager.Action.DISPLAY));
            }
          //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
            //[BUGFIX]-Add-BEGIN by TSCD.linbo.wu,07/15/2014,PR-695258,
            //[DRM]Can't open DRM picture
            misAvlid = isAvlid(mApplication.getAndroidContext(),getContentUri());
            //[BUGFIX]-Add-END by TSCD.linbo.wu
        }
    }
    //[FEATURE]-Add-END by ye.chen

  //[BUGFIX]-Add-END by ye.chen,09/04/2014,PR-775188
    public boolean isAvlid(Context context,String filePath){
        return DrmManager.getInstance().isRightsStatus(filePath);
    }

    public boolean isAvlid(Context context,Uri uri){
        return DrmManager.getInstance().isRightsStatus(ImageLoader.getLocalPathFromUri(context, uri));
    }
    //[FEATURE]-Add-END by ye.chen

    @Override
    public Job<Bitmap> requestImage(int type) {
        return new BitmapJob(type);
    }

    @Override
    public Job<BitmapRegionDecoder> requestLargeImage() {
        return new RegionDecoderJob();
    }

    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 begin
    @Override
    public Job<Bitmap> requestMomentsImage(int targetWidth, int targetHeight) {
        return null;
    }
    //[BUGFIX]-Add by TCTNJ,chengbin.du-nb, 2015-10-14,ALM-676093 end

    private void openFileOrDownloadTempFile(JobContext jc) {
        int state = openOrDownloadInner(jc);
        synchronized (this) {
            mState = state;
            if (mState != STATE_DOWNLOADED) {
                if (mFileDescriptor != null) {
                    Utils.closeSilently(mFileDescriptor);
                    mFileDescriptor = null;
                }
            }
            notifyAll();
        }
    }

    private int openOrDownloadInner(JobContext jc) {
        String scheme = mUri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)
                || ContentResolver.SCHEME_FILE.equals(scheme)) {
            try {
                if (MIME_TYPE_JPEG.equalsIgnoreCase(mContentType)) {
                    InputStream is = mApplication.getContentResolver()
                            .openInputStream(mUri);
                    mRotation = Exif.getOrientation(is);
                    Utils.closeSilently(is);
                }
                mFileDescriptor = mApplication.getContentResolver()
                        .openFileDescriptor(mUri, "r");
                if (jc.isCancelled()) return STATE_INIT;
                return STATE_DOWNLOADED;
            } catch (FileNotFoundException e) {
                Log.w(TAG, "fail to open: " + mUri, e);
                return STATE_ERROR;
            }
        } else {
            try {
                URL url = new URI(mUri.toString()).toURL();
                mCacheEntry = mApplication.getDownloadCache().download(jc, url);
                if (jc.isCancelled()) return STATE_INIT;
                if (mCacheEntry == null) {
                    Log.w(TAG, "download failed " + url);
                    return STATE_ERROR;
                }
                if (MIME_TYPE_JPEG.equalsIgnoreCase(mContentType)) {
                    InputStream is = new FileInputStream(mCacheEntry.cacheFile);
                    mRotation = Exif.getOrientation(is);
                    Utils.closeSilently(is);
                }
                mFileDescriptor = ParcelFileDescriptor.open(
                        mCacheEntry.cacheFile, ParcelFileDescriptor.MODE_READ_ONLY);
                return STATE_DOWNLOADED;
            } catch (Throwable t) {
                Log.w(TAG, "download error", t);
                return STATE_ERROR;
            }
        }
    }

    private boolean prepareInputFile(JobContext jc) {
        jc.setCancelListener(new CancelListener() {
            @Override
            public void onCancel() {
                synchronized (this) {
                    notifyAll();
                }
            }
        });

        while (true) {
            synchronized (this) {
                if (jc.isCancelled()) return false;
                if (mState == STATE_INIT) {
                    mState = STATE_DOWNLOADING;
                    // Then leave the synchronized block and continue.
                } else if (mState == STATE_ERROR) {
                    return false;
                } else if (mState == STATE_DOWNLOADED) {
                    return true;
                } else /* if (mState == STATE_DOWNLOADING) */ {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        // ignored.
                    }
                    continue;
                }
            }
            // This is only reached for STATE_INIT->STATE_DOWNLOADING
            openFileOrDownloadTempFile(jc);
        }
    }

    private class RegionDecoderJob implements Job<BitmapRegionDecoder> {
        @Override
        public BitmapRegionDecoder run(JobContext jc) {
            if (!prepareInputFile(jc)) return null;
            BitmapRegionDecoder decoder = DecodeUtils.createBitmapRegionDecoder(
                    jc, mFileDescriptor.getFileDescriptor(), false);
            mWidth = decoder.getWidth();
            mHeight = decoder.getHeight();
            return decoder;
        }
    }

    private class BitmapJob extends ImageRequest{
        private int mType;

        protected BitmapJob(int type) {
            mType = type;
        }

        @Override
        public Bitmap run(JobContext jc) {
            if (!prepareInputFile(jc)) return null;
            int targetSize = MediaItem.getTargetSize(mType);
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            MediaObject item = mPath.getObject();
            Bitmap bitmap = null;
            if(item.isDrm() != 1){
                bitmap = DecodeUtils.decodeThumbnail(jc,
                    mFileDescriptor.getFileDescriptor(), options, targetSize, mType);
            }else {
                if (mType == MediaItem.TYPE_MICROTHUMBNAIL) {
                    if(item.isDrm() == 1){
                        return getDrmThumbnails(jc,mApplication.getAndroidContext(),mUri);
                    }
                    //bitmap = BitmapUtils.resizeAndCropCenter(bitmap, targetSize, true);
                } else {
                    if(DrmManager.isDrmEnable){
                        if(item.isDrm() == 1){
                            if(item.getMediaType() == MEDIA_TYPE_GIF){
                            //[BUGFIX]-Add-BEGIN by TCTNB(Peng.Cao) 2013/06/11|PR 458910
                                return DrmManager.getInstance().getDrmRealThumbnail (ImageLoader.getLocalPathFromUri(mApplication.getAndroidContext(), mUri), options,108);
                            //[BUGFIX]-Add-END by TCTNB(Peng.Cao) 2013/06/11
                            }else{
                                bitmap = DecodeUtils.decodeThumbnail(jc,
                                    mFileDescriptor.getFileDescriptor(), options, targetSize, mType);
                                bitmap = BitmapUtils.resizeDownBySideLength(bitmap, targetSize, true);
                                if(bitmap == null)return getDrmThumbnails(jc,mApplication.getAndroidContext(),mUri);
                                return bitmap;
                            }//if (item.getMediaType() == MEDIA_TYPE_GIF)
                        }//if client.isDrm(mUri)
                    }//isDrmEnabled
                }// mType == MediaItem.TYPE_MICROTHUMBNAIL
            }// if !isDrm
            //[BUGFIX]-Add-END by TCTNB.ye.chen 2014/11/18

            if (jc.isCancelled() || bitmap == null) {
                return null;
            }

            if (mType == MediaItem.TYPE_MICROTHUMBNAIL) {
                if(item.isDrm() == 1){
                    return getDrmThumbnails(jc,mApplication.getAndroidContext(),mUri);
                }
                //bitmap = BitmapUtils.resizeAndCropCenter(bitmap, targetSize, true);
            } else {
                if(DrmManager.isDrmEnable){
                    if(item.isDrm() == 1){
                        if(item.getMediaType() == MEDIA_TYPE_GIF){
                            return getDrmThumbnails(jc,mApplication.getAndroidContext(),mUri);
                        }else{
                            bitmap = BitmapUtils.resizeDownBySideLength(bitmap, targetSize, true);
                            if(bitmap == null)return getDrmThumbnails(jc,mApplication.getAndroidContext(),mUri);
                            return bitmap;
                        }
                    }
                }
            }
            return bitmap = BitmapUtils.resizeDownBySideLength(bitmap,targetSize, true);
           //[FEATURE]-Add-END by TCTNB.ye.chen
        }
    }

    @Override
    public int getSupportedOperations() {
        //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
        if(isDrm() == 1){
            int supported = 0;
            if(isSupportSetWallpaper()&& misAvlid)supported |= SUPPORT_SETAS;
            if(isSupportForward()&&isSharable())supported |= SUPPORT_SHARE;
            return supported;
        }else{
        int supported = SUPPORT_PRINT | SUPPORT_SETAS;
        if (isSharable()) supported |= SUPPORT_SHARE;
        if (BitmapUtils.isSupportedByRegionDecoder(mContentType)) {
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-20,PR844577 begin
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-12,PR939075 begin
            if (!mContentType.endsWith("bmp")) {
                supported |= SUPPORT_FULL_IMAGE;
            }
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-12,PR939075 end
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-20,PR844577 end
        }
        return supported;
        }//[FEATURE]-Add- by ye.chen,11/10/2014,support drm
    }

    @Override
    public void getPanoramaSupport(PanoramaSupportCallback callback) {
        mPanoramaMetadata.getPanoramaSupport(mApplication, callback);
    }

    @Override
    public void clearCachedPanoramaSupport() {
        mPanoramaMetadata.clearCachedValues();
    }

    private boolean isSharable() {
        // We cannot grant read permission to the receiver since we put
        // the data URI in EXTRA_STREAM instead of the data part of an intent
        // And there are issues in MediaUploader and Bluetooth file sender to
        // share a general image data. So, we only share for local file.
        return ContentResolver.SCHEME_FILE.equals(mUri.getScheme());
    }

    @Override
    public int getMediaType() {
//        return MEDIA_TYPE_IMAGE;
          return "image/gif".equalsIgnoreCase(getMimeType())?MEDIA_TYPE_GIF:MEDIA_TYPE_IMAGE;//[FEATURE]-Mod by ye.chen,11/10/2014,support drm
    }

    @Override
    public Uri getContentUri() {
        return mUri;
    }

    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        if (mWidth != 0 && mHeight != 0) {
            details.addDetail(MediaDetails.INDEX_WIDTH, mWidth);
            details.addDetail(MediaDetails.INDEX_HEIGHT, mHeight);
        }
        if (mContentType != null) {
            details.addDetail(MediaDetails.INDEX_MIMETYPE, mContentType);
        }
        if (ContentResolver.SCHEME_FILE.equals(mUri.getScheme())) {
            String filePath = mUri.getPath();
            details.addDetail(MediaDetails.INDEX_PATH, filePath);
            MediaDetails.extractExifInfo(details, filePath);
        }
      //[FEATURE]-Add-BEGIN by TCTNB.ye.chen,11/06/2014,support drm
        if(isDrm == 1){
            setDrmPropertyDialog(mApplication.getAndroidContext(),details,mUri.getPath(),DrmStore.Action.DISPLAY);
        }
        //[FEATURE]-Add-END by TCTNB.ye.chen
        return details;
    }

    @Override
    public String getMimeType() {
        return mContentType;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mFileDescriptor != null) {
                Utils.closeSilently(mFileDescriptor);
            }
        } finally {
            super.finalize();
        }
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getRotation() {
        return mRotation;
    }

  //[FEATURE]-Add-BEGIN by ye.chen,11/10/2014,support drm
    @Override
    public String getName() {
        return mUri.getLastPathSegment();
    }

    public void recyleResources(){
        mState = STATE_INIT;
        if (mFileDescriptor != null) {
            Utils.closeSilently(mFileDescriptor);
            mFileDescriptor = null;
            mPath.setObject();
        }
    }
    //[FEATURE]-Add-END by ye.chen
}

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
/* 29/12/2014|ye.chen               |PR874635              |[DRM][SD]The *.jpg file cannot be viewed enven when the object and right files are both downloaded.
/* ----------|----------------------|----------------------|----------------- */
/* 10/03/2015|ye.chen               |PR916400              |[GenericApp][Gallery]MTK DRM adaptation
/* ----------|----------------------|----------------------|----------------- */
/* 04/22/2015| jian.pan1            | CR979742             |[5.0][Gallery] picture detail should show "Date taken" instead of current "Time"
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View.MeasureSpec;

import com.tct.gallery3d.R;
import com.tct.gallery3d.data.MediaDetails;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.ui.DetailsAddressResolver.AddressResolvingListener;

public class DetailsHelper {
    private static DetailsAddressResolver sAddressResolver;
    private DetailsViewContainer mContainer;

    public interface DetailsSource {
        public int size();
        public int getIndex();
        public MediaDetails getDetails();
    }

    public interface CloseListener {
        public void onClose();
    }

    public interface DetailsViewContainer {
        public void reloadDetails();
        public void setCloseListener(CloseListener listener);
        public void show();
        public void hide();
        public boolean isShowing();
    }

    public interface ResolutionResolvingListener {
        public void onResolutionAvailable(int width, int height);
    }

    public DetailsHelper(Activity activity, DetailsSource source) {
        mContainer = new DialogDetailsView(activity, source);
    }

    public void layout(int left, int top, int right, int bottom) {
    }

    public void reloadDetails() {
        mContainer.reloadDetails();
    }

    public void setCloseListener(CloseListener listener) {
        mContainer.setCloseListener(listener);
    }

    public static String resolveAddress(Activity activity, double[] latlng,
            AddressResolvingListener listener) {
        if (sAddressResolver == null) {
            sAddressResolver = new DetailsAddressResolver(activity);
        } else {
            sAddressResolver.cancel();
        }
        return sAddressResolver.resolveAddress(latlng, listener);
    }

    public static void resolveResolution(String path, ResolutionResolvingListener listener) {
        //[BUGFIX]-Modified by TCTNJ,ye.chen, 2014-12-29,PR874635 begain
        Bitmap bitmap = null;
        if(!DrmManager.getInstance().isDrm(path)){
            bitmap = BitmapFactory.decodeFile(path);
        }else{
            bitmap = DrmManager.getInstance().getDrmThumbnail(path,108);
        }
      //[BUGFIX]-Modified by TCTNJ,ye.chen, 2014-12-29,PR874635 end
        if (bitmap == null) return;
        listener.onResolutionAvailable(bitmap.getWidth(), bitmap.getHeight());
    }

    public static void pause() {
        if (sAddressResolver != null) sAddressResolver.cancel();
    }

    public void show() {
        mContainer.show();
    }

    public void hide() {
        mContainer.hide();
    }

    public boolean isShowing() {
        return mContainer.isShowing();
    }

    public static String getDetailsName(Context context, int key) {
        switch (key) {
            case MediaDetails.INDEX_TITLE:
                return context.getString(R.string.title);
            case MediaDetails.INDEX_DESCRIPTION:
                return context.getString(R.string.description);
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-22,CR979742 begin
            case MediaDetails.INDEX_DATETIME:
                return context.getString(R.string.date_taken);
            case MediaDetails.INDEX_MODIFIED:
                return context.getString(R.string.modified_time);
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-22,CR979742 end
            case MediaDetails.INDEX_LOCATION:
                return context.getString(R.string.location);
            case MediaDetails.INDEX_PATH:
                return context.getString(R.string.path);
            case MediaDetails.INDEX_WIDTH:
                return context.getString(R.string.width);
            case MediaDetails.INDEX_HEIGHT:
                return context.getString(R.string.height);
            case MediaDetails.INDEX_ORIENTATION:
                return context.getString(R.string.orientation);
            case MediaDetails.INDEX_DURATION:
                return context.getString(R.string.duration);
            case MediaDetails.INDEX_MIMETYPE:
                return context.getString(R.string.mimetype);
            case MediaDetails.INDEX_SIZE:
                return context.getString(R.string.file_size);
            case MediaDetails.INDEX_MAKE:
                return context.getString(R.string.maker);
            case MediaDetails.INDEX_MODEL:
                return context.getString(R.string.model);
            case MediaDetails.INDEX_FLASH:
                return context.getString(R.string.flash);
            case MediaDetails.INDEX_APERTURE:
                return context.getString(R.string.aperture);
            case MediaDetails.INDEX_FOCAL_LENGTH:
                return context.getString(R.string.focal_length);
            case MediaDetails.INDEX_WHITE_BALANCE:
                return context.getString(R.string.white_balance);
            case MediaDetails.INDEX_EXPOSURE_TIME:
                return context.getString(R.string.exposure_time);
            case MediaDetails.INDEX_ISO:
                return context.getString(R.string.iso);
              //[FEATURE]-Add-BEGIN by TCTNB.ye.chen,11/26/2014,for PR850639
              //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
            case MediaDetails.INDEX_DRM_RIGHT:
                if(DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM){
                    return context.getString(R.string.drm_protection_status);
                }else{
                    return context.getString(R.string.drm_current_right);
                }
            case MediaDetails.INDEX_DRM_DATETIME_STARTTIME:
                if(DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM){
                    return context.getString(R.string.drm_begin);
                }else{
                    return context.getString(R.string.drm_current_right);
                }
            case MediaDetails.INDEX_DRM_DATETIME_ENDTIME:
                if(DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM){
                    return context.getString(R.string.drm_end);
                }else{
                    return context.getString(R.string.drm_current_right);
                }
            case MediaDetails.INDEX_DRM_REMAINING_REPEAT_COUNT:
                if(DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM){
                    return context.getString(R.string.drm_use_left);
                }else{
                    return context.getString(R.string.drm_current_right);
                }
            case MediaDetails.INDEX_DRM_INTERVAL:
                if(DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM){
                    return context.getString(R.string.drm_current_right);
                }
            case MediaDetails.INDEX_DRM_RIGHT_ISSUER_TEXT:
                if(DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM){
                    return context.getString(R.string.right_url);
                }
            case MediaDetails.INDEX_DRM_VENDOR_URL_TEXT:
                if(DrmManager.getInstance().mCurrentDrm == DrmManager.QCOM_DRM){
                    return context.getString(R.string.vendor);
                }
              //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
             //[FEATURE]-Add-END by TCTNB.ye.chen
            default:
                return "Unknown key" + key;
        }
    }
}



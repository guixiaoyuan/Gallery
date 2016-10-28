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
/* 16/12/2015|chengbin.du-nb        |ALM-1170791           |Momments display vGallery.*/
/* ----------|----------------------|----------------------|----------------- */
/* 21/12/2015|chengbin.du-nb        |ALM-1121296           |[Gallery]Fyuse pictures can't display in gallery after capture a Fyuse picture
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.picturegrouping;

import java.util.Set;


public class Picture {
    static int DefaultWidth = 200;
    static int DefaultHeight = 200;
    static int mUIDGenerator = 0;
    public String mDBVolume;
    public long mFileId;
    public int mMediaType;
    public boolean mHasCoordinates;
    public float mLatitude, mLongitude;
    public int mWidth, mHeight;
    public long mTimestamp;
    public int mOrientation;
    Set<QualityAddress> mAddressSet;

    public Picture(String dbVolume, int mediaType, long fileId, float latitude, float longitude, long timestamp, int width, int height, int orientation){
        mDBVolume = dbVolume;
        mMediaType = mediaType;
        mFileId = fileId;
        mHasCoordinates = true;
        mLatitude = latitude;
        mLongitude = longitude;
        mTimestamp = timestamp;
        if(orientation == 0 || orientation == 180) {
            mWidth = width;
            mHeight = height;
        } else {
            mWidth = height;
            mHeight = width;
        }
        if(mWidth == 0) mWidth = DefaultWidth;
        if(mHeight == 0) mHeight = DefaultHeight;
        mOrientation = orientation;
    }

    public Picture(String dbVolume, int mediaType, long fileId, long timestamp, int width, int height, int orientation){
        mDBVolume = dbVolume;
        mMediaType = mediaType;
        mFileId = fileId;
        mHasCoordinates = false;
        mTimestamp = timestamp;
        if(orientation == 0 || orientation == 180) {
            mWidth = width;
            mHeight = height;
        } else {
            mWidth = height;
            mHeight = width;
        }
        if(mWidth == 0) mWidth = DefaultWidth;
        if(mHeight == 0) mHeight = DefaultHeight;
        mOrientation = orientation;
    }
    
    private boolean compareNullStrings(String str1, String str2){
        if ((str1 == null && str2 == null) ||
            (str1 != null && str1.equals(str2))){
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (mDBVolume != null){
            result = result * prime + mDBVolume.hashCode();
        }
        result = result * prime + (int) mFileId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        Picture other = (Picture) obj;
        if (mFileId != other.mFileId){
            return false;
        }
        if (! compareNullStrings(mDBVolume, other.mDBVolume)){
            return false;
        }
        return true;
    }
    
    @Override
    public String toString(){
        return "Picture { " + mDBVolume + "#" + mFileId + ", " + mLatitude + ", " + mLongitude + ", " + mTimestamp + ", " + 
                    ((mAddressSet == null) ? "no location list yet" : mAddressSet.size() + " locations") + " }";
    }
}

/* EOF */

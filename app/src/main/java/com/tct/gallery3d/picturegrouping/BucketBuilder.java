package com.tct.gallery3d.picturegrouping;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import android.util.Log;

public class BucketBuilder {
    interface Client {
        void onNewPictureBucket(List<Picture> pictureBucket);
    }
    
    private Client mClient;
    private List<Picture> mPictureBucket = new ArrayList<Picture>();
    private TimeZone mCurrentTimeZone = TimeZone.getDefault();
    private long mFirstPictureTimeOffset;
    
    BucketBuilder(Client client){
        mClient = client;
        Log.i(PictureGrouping.TAG, "new BucketBuilder()");
    }
    
    private void notifyClient(){
        if (mPictureBucket.size() > 0){
            try {
                mClient.onNewPictureBucket(mPictureBucket);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        mPictureBucket.clear();
    }
    
    void consumePicture(Picture picture){
        boolean newBucket = false;
        
        if (mPictureBucket.size() == 0){
            newBucket = true;
        }
        else {
            Picture firstPicture = mPictureBucket.get(0);
            long pictureTimeOffset = mCurrentTimeZone.getOffset(picture.mTimestamp * 1000) / 1000;
            if ((firstPicture.mTimestamp + mFirstPictureTimeOffset) / PictureGrouping.ONE_DAY != (picture.mTimestamp + pictureTimeOffset) / PictureGrouping.ONE_DAY){
                newBucket = true;
                notifyClient();
            }
        }
        
        if (newBucket){
            mPictureBucket.clear();
        }
        if (mPictureBucket.size() == 0){
            mFirstPictureTimeOffset = mCurrentTimeZone.getOffset(picture.mTimestamp * 1000) / 1000;
            Log.d(PictureGrouping.TAG, "mFirstPictureTimeOffset: " + (mFirstPictureTimeOffset / PictureGrouping.ONE_HOUR));
        }
        mPictureBucket.add(picture);
    } 
    
    void end(){
        notifyClient();
    }
}

/* EOF */

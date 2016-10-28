package com.tct.gallery3d.picturegrouping;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class PictureGroup {
    public UsableAddress mAddress;
    public List<Picture> mPictures = new ArrayList<Picture>();
    
    boolean mHasCoordStats = false;
    double mCoordCenter[] = new double[] { 0 , 0 };
    double mCoordStdDev[] = new double[] { 0 , 0 };
    
    void computeCoordStats(){
        int count = 0;
        // Get the center of the area
        // Note that, this doesn't work when the area is area +/- 180 degrees, but let's ignore this case
        mCoordCenter[0] = mCoordCenter[1] = 0;
        for (Picture picture : mPictures){
            if (picture.mHasCoordinates){
                mCoordCenter[0] += picture.mLatitude;
                mCoordCenter[1] += picture.mLongitude;
                count++;
            }
        }
        
        mHasCoordStats = (count > 0);
        
        if (mHasCoordStats){
            mCoordCenter[0] /= count;
            mCoordCenter[1] /= count;
            
            // Compute standard deviation too...
            mCoordStdDev[0] = mCoordStdDev[1] = 0;
            for (Picture picture : mPictures){
                if (picture.mHasCoordinates){
                    mCoordStdDev[0] += (mCoordCenter[0] - picture.mLatitude)  * (mCoordCenter[0] - picture.mLatitude);
                    mCoordStdDev[1] += (mCoordCenter[1] - picture.mLongitude) * (mCoordCenter[1] - picture.mLongitude);
                }
            }
            mCoordStdDev[0] = Math.sqrt(mCoordStdDev[0] / count);
            mCoordStdDev[1] = Math.sqrt(mCoordStdDev[1] / count);
            Log.d(PictureGrouping.TAG, "Group center: { " + mCoordCenter[0] + ", " + mCoordCenter[1] + " }, " +
                    "standard deviation: { " + (int) Utils.latitudeToMeters(mCoordStdDev[0]) + "m" + 
                    ", " + (int) Utils.longitudeToMeters(mCoordStdDev[0], mCoordStdDev[1]) + "m }");
        }
    }
    
    
    @Override
    public String toString(){
        if (mAddress == null){
            return "PictureGroup { *** null *** }";
        }
        
        return "PictureGroup { " + mAddress.mPointOfInterest + " (" + mAddress.mAdminArea + "), count: " + mPictures.size() + " }";
    }
}

/* EOF */

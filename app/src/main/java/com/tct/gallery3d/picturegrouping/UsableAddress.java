package com.tct.gallery3d.picturegrouping;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import android.util.Log;

public class UsableAddress {
    private static Deque<UsableAddress> sItems = new ArrayDeque<UsableAddress>();
    
    static UsableAddress get(){
        UsableAddress item = null;

        if (sItems.size() == 0){
            item = new UsableAddress();
            if (0 == (sUID %100)){
                Log.i(PictureGrouping.TAG, "UsableAddress.get(): already " + sUID + " items instantiated");
            }
        }
        else {
            item = sItems.pollFirst();
        }
        
        item.reset();
        
        return item;
    }

    static UsableAddress get(AdminLevel pointOfInterestType, String pointOfInterest,
            AdminLevel administrativeAreaType, String administrativeArea){
        UsableAddress item = get();
        
        item.init(pointOfInterestType, pointOfInterest,
                  administrativeAreaType, administrativeArea);
        
        return item;
    }

    static UsableAddress duplicate(UsableAddress usableAddress){
        return get(usableAddress.mPointOfInterestType, usableAddress.mPointOfInterest,
                    usableAddress.mAdminAreaType, usableAddress.mAdminArea);
    }
    
    static void release(UsableAddress item){
        sItems.addLast(item);
    }
    
    static void releaseAll(){
        sItems.clear();
    }
    
    
    //------------------------
    
    public AdminLevel mPointOfInterestType = null;
    public AdminLevel mAdminAreaType = null;
    public float mQuality;
    public String mPointOfInterest, mAdminArea;
    public List<Picture> mPictures;
    public int mRefCount;
    public float mScore;
    public boolean mScoreComputed = false;
    static int sUID = 0;
    int mUID = ++sUID;
    
    private UsableAddress(){
        
    }
    
    private void reset(){
        if (mPictures != null){
            mPictures.clear();
        }
        mRefCount = 0;
        mQuality = 0;
        mScoreComputed = false;
    }
    
    private void init(AdminLevel pointOfInterestType, String pointOfInterest,
                       AdminLevel administrativeAreaType, String administrativeArea){
        mPointOfInterestType = pointOfInterestType;
        mAdminAreaType = administrativeAreaType;
        mPointOfInterest = pointOfInterest;
        mAdminArea = administrativeArea;
    }
    
    void addReference(Picture picture, float quality){
        //Log.i(Poladroid.TAG, "addReference(" + toString() + ") with quality:" + quality + " for " + pictureLocation);
        //Utils.sleep();
        if (mPictures == null){
            mPictures = new ArrayList<Picture>(5);
        }
        mPictures.add(picture);
        mQuality *= mRefCount;
        mQuality += quality;
        mRefCount++;
        mQuality /= mRefCount;
        //if (mPictureLocations.size() != mRefCount){
        //    throw new Error("Invalid picture count: " + mPictureLocations.size() + " vs expected " + mRefCount);
        //}
        if (mPictures.size() != mRefCount){
            throw new Error("Invalid picture count: " + mPictures.size() + " vs expected " + mRefCount);
        }
    }
    
    private float getAdminLevelCoef(){
        // AdminLevel couple may have more or less interest
        // It takes into account:
        // - The 'base' admin level: ADDRESS is better than LOCATION, LOCATION better than...
        // - The 'distance' between the two levels: { ADDRESS, LOCATION } being better than { ADDRESS, COUNTRY } for example
        // However some admin couples are undesired, typically { SUBREGION, REGION }, which is worse than { SUBREGION, COUNTRY }
        
        float baseCoef = (float) (1.0 / Math.pow(PictureGrouping.ADMIN_LEVEL_BASE_PENALTY, mPointOfInterestType.ordinal()));
        
        int adminLevelDistance = mAdminAreaType.ordinal() - mPointOfInterestType.ordinal();
        
        // Manual hard-code choice here-under, based on empirical observations and personal opinion !!!
        if (mPointOfInterestType == AdminLevel.SUBREGION){
            // For SubRegion, prefer in order:
            // { SUBREGION, COUNTRY }
            // { SUBREGION, REGION }
            if (mAdminAreaType == AdminLevel.REGION){
                adminLevelDistance = AdminLevel.COUNTRY.ordinal() - mPointOfInterestType.ordinal();
            }
            else if (mAdminAreaType == AdminLevel.COUNTRY){
                adminLevelDistance = AdminLevel.REGION.ordinal() - mPointOfInterestType.ordinal();
            }
        }
        
        if (mPointOfInterestType == AdminLevel.LOCALITY){
            // For Locality, prefer in order:
            // { LOCALITY, REGION }
            // { LOCALITY, COUNTRY }
            // { LOCALITY, SUBREGION }
            
            if (mAdminAreaType == AdminLevel.SUBREGION){
                adminLevelDistance = AdminLevel.COUNTRY.ordinal() - mPointOfInterestType.ordinal();
            }
            else if (mAdminAreaType == AdminLevel.REGION){
                adminLevelDistance = AdminLevel.SUBREGION.ordinal() - mPointOfInterestType.ordinal();
            }
            else if (mAdminAreaType == AdminLevel.COUNTRY){
                adminLevelDistance = AdminLevel.REGION.ordinal() - mPointOfInterestType.ordinal();
            }
        }
        
        float distanceCoef = (float) (1.0 / Math.pow(PictureGrouping.ADMIN_LEVEL_DISTANCE_PENALTY, adminLevelDistance));
        
        return baseCoef * distanceCoef;
    }
    
    private float getGroupCompletionCoef(int groupSize){
        final double MAX_POWER = 8.0; // Empirical, not really for tuning, I guess...
        return (float) (1.0 / (float) Math.pow(PictureGrouping.GROUP_COMPLETION_PENALTY, // Empirical value
                                               MAX_POWER * (Math.max(0.0, Math.min(1.0, 1.0 * (groupSize - mRefCount) / groupSize)))));
    }
    
    float getScore(int groupSize){
        if (! mScoreComputed){
            mScoreComputed = true;
            
            mScore = 
                    mQuality *
                    getAdminLevelCoef() *
                    getGroupCompletionCoef(groupSize);
                    
        }
        return mScore;
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
        if (mPointOfInterest != null){
            result = result * prime + mPointOfInterest.hashCode();
        }
        if (mAdminArea != null){
            result = result * prime + mAdminArea.hashCode();
        }
        if (mPointOfInterestType != null){
            result = result * prime + mPointOfInterestType.ordinal();
        }
        if (mAdminAreaType != null){
            result = result * prime + mAdminAreaType.ordinal();
        }
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
        UsableAddress other = (UsableAddress) obj;
        if (! compareNullStrings(mPointOfInterest, other.mPointOfInterest)){
            return false;
        }
        else if (! compareNullStrings(mAdminArea, other.mAdminArea)){
            return false;
        }
        else if (mPointOfInterestType != other.mPointOfInterestType){
            return false;
        }
        else if (mAdminAreaType != other.mAdminAreaType){
            return false;
        }
        return true;
    }
    
    @Override
    public String toString(){
        return "UsableAddress {" + mPointOfInterestType + ", " + mPointOfInterest + " (" + mAdminAreaType + ", " + mAdminArea + "), quality: " + mQuality + ", refCount: " + mRefCount + " }";
    }
}

/* EOF */

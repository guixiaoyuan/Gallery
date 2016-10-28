package com.tct.gallery3d.picturegrouping;

import java.util.regex.Pattern;

import android.util.Log;

public class QualityAddress {
    // Begin - To be saved in DB
    String[] mAreas = new String[AdminLevel.values().length];
    float[] mAreasQuality = new float[AdminLevel.values().length];
    float mBaseQuality;
    String mCountryCode; // Only used to judge address quality
    // End - To be saved in DB
    
    private boolean[] mMultiLevelAreas = new boolean[AdminLevel.values().length];
    private boolean mMultiLevelAreasInitialized = false;
    String mIntialAddress; // Just for log
    
    
    private static final Pattern sNumberOnlyPattern = Pattern.compile("^[\\d\\s\\-]+$"); // Matches postal pattern and street range "234-432"
    private static final Pattern sPostalStylePattern = Pattern.compile(".*\\d\\d\\d\\d+.*"); // At least 3 digits...
    private static final Pattern sRoadNumberStylePattern = Pattern.compile(".*\\d+.*"); // At least 1 digits...
    
    QualityAddress(){
    }
    
    QualityAddress(int index, boolean checkRoadNumberDistance, double distance,
                   String address, float addressQuality, 
                   String locality,
                   String subRegion,
                   String region,
                   String country,
                   String countryCode){
        
        mIntialAddress = address.toString();
        
        mCountryCode = countryCode;
        
        mAreas[AdminLevel.LOCALITY.ordinal()] = locality;
        mAreas[AdminLevel.SUBREGION.ordinal()] = subRegion;
        mAreas[AdminLevel.REGION.ordinal()] = region;
        mAreas[AdminLevel.COUNTRY.ordinal()] = country;
        mAreas[AdminLevel.VOID.ordinal()]      = null;
        
        index = Math.max(0, index + 1);
        mBaseQuality = (float) (1.0 / Math.pow(1.1, 1.0 * index));
        
        if (address != null && address.isEmpty()){
            if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                Log.i(PictureGrouping.TAG, "Address line is empty");
            }
            address = null;
        }
        if (distance > PictureGrouping.MAX_ADDRESS_DISTANCE){
            if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                Log.i(PictureGrouping.TAG, "Address is too far from picture location");
            }
            address = null;
        }
        if (address != null){
            for (int i=AdminLevel.LOCALITY.ordinal(); i < mAreas.length; i++){
                if (mAreas[i] != null && mAreas[i].length() > 0){
                    // The 'equal' test is performed in next section
                    // For logging purposes, I just want the real subsets here
                    if (! mAreas[i].equals(address) && mAreas[i].contains(address)){
                        if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                            Log.i(PictureGrouping.TAG, "Address " + address + " is a subset of " + mAreas[i] + " => get rid of it");
                        }
                        address = null;
                        break;
                    }
                }
            }
        }
        mAreas[AdminLevel.ADDRESS.ordinal()] = address;
        
        for (int i=0; i < mAreas.length; i++){
            mAreasQuality[i] = 0;
            
            if (mAreas[i] != null){
                for (int j=i+1; j < mAreas.length; j++){
                    if (mAreas[i].equals(mAreas[j])){
                        mAreas[i] = null;
                        break;
                    }
                }
                if (mAreas[i] != null && mAreas[i].equals(mCountryCode)){
                    if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                        Log.i(PictureGrouping.TAG, "Area " + mAreas[i] + " is the country code");
                    }
                    mAreas[i] = null;
                }
            }
            
            if (mAreas[i] != null){
                // For foreign countries, Baidu Geocoder sometimes returns some strange and undesired strings such as
                // "Singapore(SG)_null_Singapore" or
                // "Bali(ID)_Indonesia"
                // It's quite bad, better get rid of any field containing a "Poladroid" character
                if (mAreas[i].contains("Poladroid")){
                    if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                        Log.i(PictureGrouping.TAG, "Area " + mAreas[i] + " contains an underscore");
                    }
                    mAreas[i] = null;
                }
            }
            
            if (mAreas[i] != null){
                if (sNumberOnlyPattern.matcher(mAreas[i]).matches()){
                    if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                        Log.i(PictureGrouping.TAG, "Area " + mAreas[i] + " matches number-only pattern " + sNumberOnlyPattern);
                    }
                    mAreas[i] = null;
                }
            }
            
            if (mAreas[i] != null){
                mAreasQuality[i] = 1;
                
                if (PictureGrouping.USE_FAKE_GEOCODING){
                    // With fake Geocoding, by construction all values look like ZipCode, so don't test it
                }
                else {
                    boolean matchesPostalStyle = sPostalStylePattern.matcher(mAreas[i]).matches();
                    boolean matchesRoadNumberStyle = sRoadNumberStylePattern.matcher(mAreas[i]).matches();
                    
                    if (mAreas[i].contains("(")){
                        // Maybe too detailed, e.g. "Residence name (Gate number)"
                        // (but sometimes used for translation...)
                        mAreasQuality[i] /= 1.2;
                        if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                            Log.i(PictureGrouping.TAG, "Area " + mAreas[i] + " contains brackets");
                        }
                    }
                    
                    if ((matchesRoadNumberStyle || checkRoadNumberDistance) && distance > PictureGrouping.MAX_ADDRESS_DISTANCE_FOR_ROAD_NUMBER){
                        mAreas[i] = null;
                        mAreasQuality[i] = 0;
                        if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                            Log.i(PictureGrouping.TAG, "Area " + mAreas[i] + " matches RoadNumber-style pattern and is too far from the road to be interesting and reliable => get rid of it");
                        }
                    }
                    else if (matchesPostalStyle){
                        mAreasQuality[i] /= 4;
                        if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                            Log.i(PictureGrouping.TAG, "Area " + mAreas[i] + " matches Postal-style pattern " + sPostalStylePattern);
                        }
                    }
                    else if (matchesRoadNumberStyle){
                        mAreasQuality[i] /= 2;
                        if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                            Log.i(PictureGrouping.TAG, "Area " + mAreas[i] + " matches RoadNumber-style pattern " + sRoadNumberStylePattern);
                        }
                    }
                }
            }
            
            if (mAreas[i] != null &&
                mCountryCode != null && mCountryCode.length() > 0 && mAreas[i].contains(mCountryCode)){
                if (mAreas[i].length() <= 4){
                    mAreas[i] = null;
                    mAreasQuality[i] = 0;
                    if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                        Log.i(PictureGrouping.TAG, "Area " + mAreas[i] + " contains the country code " + mCountryCode + " => get rid of it");
                    }
                }
                else {
                    mAreasQuality[i] /= 1024; // I really don't like this !!! Known issue: Address "USA" and Country code "US"
                    if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                        Log.i(PictureGrouping.TAG, "Area " + mAreas[i] + " contains the country code " + mCountryCode + "");
                    }
                }
            }
            
            
            if (mAreas[i] != null &&
                distance > PictureGrouping.MAX_ADDRESS_DISTANCE){
                String cleaned = new String(mAreas[i]);
                for (int j=i+1; j < mAreas.length; j++){
                    if (mAreas[j] != null && mAreas[j].length() > 0){
                        cleaned = cleaned.replaceAll(Pattern.quote(mAreas[j]), "");
                    }
                }
                cleaned = cleaned.replaceAll("\\s", "");
                cleaned = cleaned.replaceAll(",", "");
                cleaned = cleaned.replaceAll("\\-", "");
                if (PictureGrouping.VERBOSE_QUALITY_ADDRESSES){
                    Log.i(PictureGrouping.TAG, "Cleaned '" + mAreas[i] + "' => '" + cleaned + "'"); Utils.sleep();
                }

                if (cleaned.length() == 0){
                    if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                        Log.i(PictureGrouping.TAG, "Area " + mAreas[i] + " contains nothing else than larger admin areas => get rid of it");
                    }
                    mAreas[i] = null;
                    mAreasQuality[i] = 0;
                }
                else {
                    for (int j=i+1; j < mAreas.length; j++){
                        if (mAreas[j] != null && mAreas[j].length() > 0 && mAreas[i].contains(mAreas[j])){
                            mAreasQuality[i] /= 1.2;
                            if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                                Log.i(PictureGrouping.TAG, "Area " + mAreas[i] + " contains " + mAreas[j]);
                            }
                        }
                    }
                }
            }
        }
        mAreasQuality[AdminLevel.VOID.ordinal()] = 1;
        mAreasQuality[AdminLevel.ADDRESS.ordinal()] *= addressQuality;
        
        if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
            Log.i(PictureGrouping.TAG, "new " + toString() + " from " + address); Utils.sleep();
        }
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("QualityAddress { ").append(mBaseQuality).append(", ");
        for (AdminLevel type : AdminLevel.values()){
            sb.append(mAreas[type.ordinal()]);
            sb.append(" $").append(mAreasQuality[type.ordinal()]);
            sb.append(" / ");
        }
        sb.append(mCountryCode);
        sb.append(" }");
        
        return sb.toString();
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
        for (int i=0; i < mAreas.length; i++){
            if (mAreas[i] != null){
                result = result * prime + mAreas[i].hashCode();
            }
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
        QualityAddress other = (QualityAddress) obj;
        for (int i=0; i < mAreas.length; i++){
            if (! compareNullStrings(mAreas[i], other.mAreas[i])){
                return false;
            }
        }
        return true;
    }
    
    private void initMultiLevelAreas(){
        for (int i=0; i < mAreas.length; i++){
            mMultiLevelAreas[i] = false;
            if (mAreas[i] != null){
                for (int j=i+1; j < mAreas.length; j++){
                    if (mAreas[j] != null && mAreas[j].length() > 0){
                        if (mAreas[i].contains(mAreas[j])){
                            mMultiLevelAreas[i] = true;
                            if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                                Log.i(PictureGrouping.TAG, "MultiLevel area: " + mAreas[i] + ", because of " + mAreas[j]);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private boolean checkValidity(UsableAddress usableAddress){
        boolean valid = true;
        
        if (usableAddress.mPointOfInterestType != AdminLevel.VOID &&
            usableAddress.mPointOfInterest == null){
            valid = false;
        }
        else if (usableAddress.mAdminAreaType != AdminLevel.VOID &&
                  usableAddress.mAdminArea == null){
            valid = false;
        }
        else if (usableAddress.mAdminAreaType != AdminLevel.VOID &&
                  (mMultiLevelAreas[usableAddress.mPointOfInterestType.ordinal()] ||
                   mMultiLevelAreas[usableAddress.mAdminAreaType.ordinal()])){
            valid = false;
        }
        
        if (PictureGrouping.VERBOSE_QUALITY_ADDRESSES){
            Log.i(PictureGrouping.TAG, "checkValidity(): " + usableAddress.mPointOfInterestType + " / " + usableAddress.mPointOfInterest +
                         " (" + usableAddress.mAdminAreaType + " / " + usableAddress.mAdminArea + "), valid: " + valid); Utils.sleep();
        }
        return valid;
    }
    
    boolean getUsableAddress(AdminLevel pointOfInterestType,
                              AdminLevel administrativeAreaType,
                              UsableAddress usableAddress){
        if (! mMultiLevelAreasInitialized){
            mMultiLevelAreasInitialized = true;
            initMultiLevelAreas();
        }
        
        usableAddress.mPointOfInterestType = pointOfInterestType;
        usableAddress.mAdminAreaType = administrativeAreaType;
        
        usableAddress.mQuality = mBaseQuality;
        usableAddress.mQuality *= mAreasQuality[pointOfInterestType.ordinal()];
        usableAddress.mQuality *= mAreasQuality[administrativeAreaType.ordinal()];
        usableAddress.mPointOfInterest    = mAreas[pointOfInterestType.ordinal()];
        usableAddress.mAdminArea = mAreas[administrativeAreaType.ordinal()];
        return checkValidity(usableAddress);
    }
}

/* EOF */

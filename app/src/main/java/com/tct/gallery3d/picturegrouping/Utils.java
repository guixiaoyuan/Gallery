package com.tct.gallery3d.picturegrouping;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.util.Log;

public class Utils {
    static double distance(double latitude1, double longitude1,
                             double latitude2, double longitude2){
        double dLatitude = Math.toRadians(latitude2 - latitude1);
        double dLongitude = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(dLatitude/2) * Math.sin(dLatitude/2) +
                Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2)) *
                Math.sin(dLongitude/2) * Math.sin(dLongitude/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        final double R = 6378137; // Radius of earth in KM
        double d = R * c;
        return d;
    }

    static double offsetLatitude(double latitude, double meterOffset){
        if (true /*|| Math.abs(meterOffset) < 5000*/){
            latitude += meterOffset / 111111;
            if (latitude <= -180) latitude += 360;
            if (latitude >   180) latitude -= 360;
        }
        return latitude;
    }
    
    static double offsetLongitude(double latitude, double longitude, double meterOffset){
        if ((true /*|| Math.abs(meterOffset) < 5000*/) &&
                Math.abs(latitude) < 80){
            longitude += meterOffset / 111111 / Math.cos(Math.toRadians(latitude));
            if (longitude <= -180) longitude += 360;
            if (longitude >   180) longitude -= 360;
        }
        return longitude;
    }

    static double latitudeToMeters(double latitude){
        return latitude * 111111;
    }
    
    static double longitudeToMeters(double latitude, double longitude){
        return longitude * 111111 * Math.cos(Math.toRadians(latitude));
    }
    
    static double metersToLatitude(double meters){
        return meters / 111111;
    }
    
    static double metersToLongitude(double latitude, double meters){
        return meters / 111111 / Math.cos(Math.toRadians(latitude));
    }
    
    static String getGregorianString(long epochTime){
        SimpleDateFormat timeDateFormat = new SimpleDateFormat("EEE yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String timeDate = timeDateFormat.format(new Date(epochTime * 1000));
        return timeDate;
    }

    static String getGregorianTimeString(long epochTime){
        SimpleDateFormat timeDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timeDate = timeDateFormat.format(new Date(epochTime * 1000));
        return timeDate;
    }

    static String getGregorianDateString(long epochTime){
        SimpleDateFormat timeDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String timeDate = timeDateFormat.format(new Date(epochTime * 1000));
        return timeDate;
    }
    
    // Use this function to let enough time to Logcat to flush the logs...
    static void sleep(){
        if (PictureGrouping.DEBUG && PictureGrouping.IGNORE_PERFORMANCES){
            if (Math.random() < 0.1){
                try {
                        Thread.sleep(10);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }
    
    static long now(){
        return System.currentTimeMillis() / 1000; // epoch timestamp in seconds
    }
    
    
    

    private static final List<Integer[]> mPictureCountRequirements = new ArrayList<Integer[]>();
    private static final List<Float[]> mBucketRatioRequirements = new ArrayList<Float[]>();

    static float getBucketRatioRequirement(int groupCount, AdminLevel adminLevel){
        groupCount = Math.max(1, groupCount); // Just a safety...
        while (mBucketRatioRequirements.size() <= groupCount){
            int count = mBucketRatioRequirements.size();
            Float[] requirements = new Float[AdminLevel.values().length];
            mBucketRatioRequirements.add(requirements);
            if (count > 0){
                float a = (float) ((1.0 - 0.5 * Math.exp(- 1.0 * (count - 1) / PictureGrouping.GROUP_CREATION_RC)) / count);
                float c = (float) 0;
                float v = (float) 0;
                requirements[AdminLevel.ADDRESS.ordinal()] = a;
                for (int i=AdminLevel.ADDRESS.ordinal()+1; i < AdminLevel.COUNTRY.ordinal(); i++){
                    requirements[i] = a + (c - a) * (i - AdminLevel.ADDRESS.ordinal()) / (AdminLevel.COUNTRY.ordinal() - AdminLevel.ADDRESS.ordinal());
                }
                requirements[AdminLevel.COUNTRY.ordinal()] = c;
                requirements[AdminLevel.VOID.ordinal()]    = v;
                if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
                    for (AdminLevel al : AdminLevel.values()){                    
                        Log.i(PictureGrouping.TAG, "BucketRatioRequirements for group " + count + ", " + al + ": " + requirements[al.ordinal()]);
                    }
                }
            }
        }
        
        return mBucketRatioRequirements.get(groupCount)[adminLevel.ordinal()];
    }
    
    
    static int getPictureCountRequirement(int groupCount, AdminLevel adminLevel){
        groupCount = Math.max(1, groupCount); // Just a safety...
        while (mPictureCountRequirements.size() <= groupCount){
            int count = mPictureCountRequirements.size();
            Integer[] requirements = new Integer[AdminLevel.values().length];
            mPictureCountRequirements.add(requirements);
            if (count > 0){
                float a =  (float) (50.0 - 45.0 * Math.exp(- 1.0 * (count - 1) / PictureGrouping.GROUP_CREATION_RC));
                float c = 1;
                float v = 1;
                requirements[AdminLevel.ADDRESS.ordinal()] = (int) (a);
                for (int i=AdminLevel.ADDRESS.ordinal()+1; i < AdminLevel.COUNTRY.ordinal(); i++){
                    requirements[i] = (int) (a + (c - a) * (i - AdminLevel.ADDRESS.ordinal()) / (AdminLevel.COUNTRY.ordinal() - AdminLevel.ADDRESS.ordinal()));
                }
                requirements[AdminLevel.COUNTRY.ordinal()] = (int) (c);
                requirements[AdminLevel.VOID.ordinal()]    = (int) (v);
                if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
                    for (AdminLevel al : AdminLevel.values()){
                        Log.i(PictureGrouping.TAG, "PictureCountRequirement for group " + count + ", " + al + ": " + requirements[al.ordinal()]);
                    }
                }
            }
        }
        
        return mPictureCountRequirements.get(groupCount)[adminLevel.ordinal()];
    }
}

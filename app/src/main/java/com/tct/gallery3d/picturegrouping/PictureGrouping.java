package com.tct.gallery3d.picturegrouping;

import com.tct.gallery3d.BuildConfig;


public class PictureGrouping {
    static final String TAG = "PictureGrouping";
    
    /**
     * Debug constants
     */
    static boolean DEBUG = BuildConfig.DEBUG && false;
    static final boolean DEBUG_LOADER = DEBUG && true;
    static final boolean DEBUG_QUALITY_ADDRESSES = DEBUG && true;
    static final boolean VERBOSE_QUALITY_ADDRESSES = DEBUG && false;
    static final boolean DEBUG_ADDRESS_CACHE = DEBUG && true;
    static final boolean VERBOSE_ADDRESS_CACHE = DEBUG && false;
    static final boolean DEBUG_GROUPING_PIPELINE = DEBUG && true;
    static final boolean IGNORE_PERFORMANCES = DEBUG && false; // Add some 'sleep' once in a while, so that the Logcat has time to evacuate info
    static final boolean DEBUG_STRING_ESCAPE = DEBUG && false;
    static final float GEOCODER_FAILURE_RATE = 0.0f; // Between 0 and 1. Just for test, MUST be 0.0 for regular use
    
    // For quick test: convert latitude in a set of Address, Locality, SubRegion, Region and Country,
    // instead of querying the Geocoder API (much faster and controllable outcome)
    // Address:   180.123 =>  A180123 
    // Locality:  180.12  =>  L18012
    // SubRegion: 180.1   => SR1801
    // Region:    180     =>  R180
    // Country:   18      =>  C18
    static final boolean USE_FAKE_GEOCODING = false;
    
    
    /**
     * More constants (not for tuning)
     */
    static final double TOO_FAR_AWAY           = 100 * 1000 * 1000 * 1000; // // 100 millions km...
    
    static final long ONE_SECOND =  1;
    static final long ONE_MINUTE = 60 * ONE_SECOND;
    static final long ONE_HOUR   = 60 * ONE_MINUTE;
    static final long ONE_DAY    = 24 * ONE_HOUR;
    static final long ONE_WEEK   =  7 * ONE_DAY;
    
    static final long ONE_METER =    1;
    static final long ONE_KM    = 1000 * ONE_METER;
    
    
    /**
     * Tunable parameters
     * Can be tuned, but hope it won't be needed
     */
    
    // Defines how many pictures are needed to make a group
    // and how many percents of the daily bucket this group shall represent
    // See functions in the Utils class. Functions use exponential function, hence the RC (i.e. Time Constant)
    static final float GROUP_CREATION_RC = 10f;
    
    // Some parameters that tells when an address starts to become invalid
    // (i.e. when it is more a locality than an actual address or point of interest)
    static final double MAX_ADDRESS_DISTANCE   = 2000; // 2km
    // Same to judge the validity of a street name coming with a street number
    // If we are too far, better ignore it, seek for the street name alone or a neighborhood name
    static final double MAX_ADDRESS_DISTANCE_FOR_ROAD_NUMBER = 100; // 100m
    
    // Parameter to geographically attach some pictures to an existing group
    // typically when they are close to the group (< 2.0 * group-std-deviation relative to group-center)
    // but do not belong to the same admin area
    // I.e. because the group is covering several admin areas, or because GPS info is inaccurate
    static final double GROUP_RATTACHMENT_MAX_STD_DEV_RATIO = 2.0; // around 4% of photos will be out, if assuming Normal distribution
    
    // Several parameters helping to sort UsableAddresses so that the best one may form an interesting group 
    static final float ADMIN_LEVEL_BASE_PENALTY     = 2.00f; // May have importance, to balance different classes of ReadableLocations
    static final float ADMIN_LEVEL_DISTANCE_PENALTY = 1.01f; // Not important to tune, just for ordering within a certain class
    static final float GROUP_COMPLETION_PENALTY     = 2.00f; // May have importance, to balance different classes of ReadableLocations, keep somewhat similar to ADMIN_LEVEL_BASE_PENALTY
    
    
    // Quality relative weight for different parts of the Address structures
    // The values are not very important, as long as the order remains the same
    static final float THOROUGHFARE_QUALITY = 0.8f; // At best it's a street name, so it's just average, never great
    static final float ADDRESS_LINE_QUALITY = 0.9f; // Average quality, sometimes good, sometimes poor, needs a lot of cleaning
    static final float FEATURE_QUALITY      = 1.0f; // From Address.getFeature(), can be sometimes very good
    static final float PLACE_QUALITY        = 2.0f; // Say from Google Places, well filtered, shall be nearly perfect
}

/* EOF */


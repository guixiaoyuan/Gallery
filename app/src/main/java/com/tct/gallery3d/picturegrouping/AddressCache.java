/* ----------|----------------------|----------------------|----------------- */
/* 01/18/2016| jun.xie-nb           |[ALM]Defect:958124    |Memory Leak
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.picturegrouping;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.tct.gallery3d.picturegrouping.AddressDBContract.AddressTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * The AddressCache class is a single instance class
 * Currently it is NOT thread safe ! And this rule is enforced
 * 
 * API:
 * >> static getInstance()
 * >> addListener(), removeListener()
 * >> getAddressSet(latitude, longitude)
 * >> prefetchAddress(latitude, longitude)
 * That's it ~
 */
public class AddressCache {
    private static AddressCache sAddressCache;
    
    static AddressCache getInstance(Context context){
        if (sAddressCache == null){
            sAddressCache = new AddressCache(context);
        }
        else {
            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-1,ALM-1584697 begin
            try{
                sAddressCache.checkThread();
            }catch(Throwable th){
            }
            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-1,ALM-1584697 end
        }
        return sAddressCache;
    }
    

    
    //=========================================================
    
    private AddressDBHelper mDBHelper;

    private Context mContext;
    private Handler mHandler = new Handler();
    
    interface Listener {
        void onAddressCacheUpdated(boolean successful);
    }
    
    private Set<WeakReference<Listener>> mListeners = new HashSet<WeakReference<Listener>>();
    private int mSuccessCount, mConsecutiveFailures;
    private static final int MAX_CONSECUTIVE_FAILURES = 10;
    
    private long mThreadId = Thread.currentThread().getId();
    private ArrayDeque<CachedAddress> mGeodecodingJobs = new ArrayDeque<CachedAddress>();
    private boolean mGeodecodingJobInProgress;
    private CachedAddress mTempCacheKey = new CachedAddress(0, 0);
    private Map<CachedAddress, CachedAddress> mCachedAddresses = new HashMap<CachedAddress, CachedAddress>();
    
    private static class CachedAddress {
        private static final double LATITUDE_HASH_COEF = 1000;
        private static final double LATITUDE_UNHASH_COEF = 0.001;
        private static final double LONGITUDE_HASH_COEF = 1000;
        private static final double LONGITUDE_UNHASH_COEF = 0.001;
        
        boolean mGeodecodingInProgress;
        int mLatitudeHash, mLongitudeHash;
        Set<QualityAddress> mAddressSet;
        int mFailCount;
        long mFailTimestamp; // epoch timestamp in seconds
        
        CachedAddress(int latitudeHash, int longitudeHash){
            init(latitudeHash, longitudeHash);
        }
        
        void init(int latitudeHash, int longitudeHash){
            //mLatitude = latitude;
            //mLongitude = longitude;
            mLatitudeHash = latitudeHash;
            mLongitudeHash = longitudeHash;
        }
        
        boolean isTimeToGeodecode(){
            if (mFailCount <= 0){
                if (PictureGrouping.DEBUG_ADDRESS_CACHE){
                    Log.i(PictureGrouping.TAG, "CachedAddress.isTimeToGeodecode(" + this + "): found null fail count !? => try again"); Utils.sleep();
                }
                return true; // Should not meet this case... anyway, just return true...
            }
            
            // Implement the retry policy here...
            long now = Utils.now();
            
            // Lets say, wait 5 minutes after 1 failure, then multiply by 2 for each failure, up to 1 week
            final long MIN_RETRY_DELAY = 5 * PictureGrouping.ONE_MINUTE;
            final long MAX_RETRY_DELAY = 7 * PictureGrouping.ONE_DAY;
            final long RETRY_PENALTY = 2;
            long retryDelay = Math.min(MAX_RETRY_DELAY, MIN_RETRY_DELAY * (long) Math.pow(RETRY_PENALTY, mFailCount - 1));
            
            final long BACKWARD_CLOCK_THRESHOLD = 2 * PictureGrouping.ONE_DAY;
            if (now < mFailTimestamp - BACKWARD_CLOCK_THRESHOLD){
                // End-user has set a time in the past
                // to avoid being blocked forever, let's reset everything
                if (PictureGrouping.DEBUG_ADDRESS_CACHE){
                    Log.i(PictureGrouping.TAG, "CachedAddress.isTimeToGeodecode(" + this + "): found failTimestamp too far in the future => try again");
                }
                return true;
            }
            else {
                if (now > mFailTimestamp + retryDelay){
                    if (PictureGrouping.DEBUG_ADDRESS_CACHE){
                        Log.i(PictureGrouping.TAG, "CachedAddress.isTimeToGeodecode(" + this + "): retry delay of " + (retryDelay / PictureGrouping.ONE_MINUTE) + "min is over => try again");
                    }
                    return true;
                }
                else {
                    if (PictureGrouping.DEBUG_ADDRESS_CACHE){
                        Log.i(PictureGrouping.TAG, "CachedAddress.isTimeToGeodecode(" + this + "): retry delay of " + (retryDelay / PictureGrouping.ONE_MINUTE) + "min is NOT over yet => don't retry");
                    }
                    return false;
                }
            }
        }
        
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = result * prime + mLatitudeHash;
            result = result * prime + mLongitudeHash;
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
            CachedAddress other = (CachedAddress) obj;
            if (mLatitudeHash != other.mLatitudeHash){
                return false;
            }
            if (mLongitudeHash != other.mLongitudeHash){
                return false;
            }
            return true;
        }
        
        static int hashLatitude(float latitude){
            // 0.001 degrees of latitude ~ 100 m
            return (int) (LATITUDE_HASH_COEF * latitude);
        }
        
        static int hashLongitude(float latitude, float longitude){
            // For the moment ignore the fact that longitude-to-meter ration changes with latitude 
            // 0.001 degrees of longitude ~ 100 m (around equator)
            return (int) (LONGITUDE_HASH_COEF * longitude);
        }
        
        static float unhashLatitude(float latitudeHash){
            // 0.001 degrees of latitude ~ 100 m
            return (float) (LATITUDE_UNHASH_COEF * latitudeHash);
        }
        
        static float unhashLongitude(float latitudeHash, float longitudeHash){
            // For the moment ignore the fact that longitude-to-meter ration changes with latitude 
            // 0.001 degrees of longitude ~ 100 m (around equator)
            return (float) (LONGITUDE_UNHASH_COEF * longitudeHash);
        }
        
        
        
        private String getFallbackInfo(List<Address> addressList, String infoType){
            final HashMap<String, Integer> stats = new HashMap<String, Integer>();
            int validAreas = 0;

            for (Address address : addressList){
                String area = null;
                if (false) ; // Just for alignment of following lines...
                else if ("locality"    .equals(infoType)) area = address.getLocality();
                else if ("subAdminArea".equals(infoType)) area = address.getSubAdminArea();
                else if ("adminArea"   .equals(infoType)) area = address.getAdminArea();
                else if ("countryName" .equals(infoType)) area = address.getCountryName();
                else if ("countryCode" .equals(infoType)) area = address.getCountryCode();
                else throw new Error("A typo somewhere ? " + infoType);

                if (area != null && ! area.isEmpty()){
                    validAreas++;
                    Integer hitCount = stats.get(area);
                    if (hitCount == null){
                        hitCount = 0;
                    }
                    hitCount++;
                    stats.put(area, hitCount);
                }
            }

            List<String> areas = new ArrayList<String>(stats.keySet());
            Collections.sort(areas, 
                             new Comparator<String>(){
                @Override
                public int compare(String lhs, String rhs) {
                    return stats.get(rhs) - stats.get(lhs);
                }
            });
            for(String area : areas){
                if (PictureGrouping.DEBUG_ADDRESS_CACHE){
                    Log.i(PictureGrouping.TAG, "- " + area + ": " + stats.get(area) + " / " + addressList.size());
                }
            }

            String area = null;
            float score = 0;
            if (! areas.isEmpty()){
                String bestArea = areas.get(0);
                int bestHitCount = stats.get(bestArea);
                score = 100f * bestHitCount / validAreas;
                if (score > 80){ // 80%
                    area = bestArea;
                }
            }
            if (PictureGrouping.DEBUG_ADDRESS_CACHE){
                Log.i(PictureGrouping.TAG, "getFallbackInfo(" + infoType + ") => " + area + ", score: " + score + "%");
            }
            return area;
        }

        private double distance(Address address){
            double distance = PictureGrouping.TOO_FAR_AWAY;
            if (address.hasLatitude() && address.hasLongitude()){
                distance = Utils.distance(LATITUDE_UNHASH_COEF * mLatitudeHash, LONGITUDE_UNHASH_COEF * mLongitudeHash,
                                          address.getLatitude(), address.getLongitude());
                //Log.i(Poladroid.TAG, "Distance: " + distance + "m");
            }
            return distance;
        }
        
        // Addresses received from the Geodecoder
        void parseAddressList(List<Address> addressList){
            mGeodecodingInProgress = false;
            
            if (addressList == null){
                mFailCount++;
                mFailTimestamp = Utils.now();
                
            }
            else {
                mFailCount = 0;
                mFailTimestamp = -1;
                
                mAddressSet = new HashSet<QualityAddress>();
                
                // Some addresses may not have the locality / region / country
                // so by default they can't filter out these names
                // Here we try to make sure they all have enough information to filter 
                // locality / region / country from the address
                String locality    = getFallbackInfo(addressList, "locality");
                String subRegion   = getFallbackInfo(addressList, "subAdminArea");
                String region      = getFallbackInfo(addressList, "adminArea");
                String country     = getFallbackInfo(addressList, "countryName");
                String countryCode = getFallbackInfo(addressList, "countryCode");
                
                for (int i=0; i < addressList.size(); i++){
                    Address address = addressList.get(i);
                    
                    double distance = distance(address);
                    
                    if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                        Log.i(PictureGrouping.TAG, "Address #" + (i+1) + " of " + addressList.size() + ": distance: " + distance + ", " + address); Utils.sleep();
                    }
                    
                    String myLocality    = address.getLocality()     != null ? address.getLocality()     : locality;
                    String mySubRegion   = address.getSubAdminArea() != null ? address.getSubAdminArea() : subRegion;
                    String myRegion      = address.getAdminArea()    != null ? address.getAdminArea()    : region;
                    String myCountry     = address.getCountryName()  != null ? address.getCountryName()  : country;
                    String myCountryCode = address.getCountryCode()  != null ? address.getCountryCode()  : countryCode;
                    
                    // Some Geocoders, depending on country, may sort address lines from <street> to <country> or from <country> to <street>
                    // So we have to consider both the first and the last lines 
                    int addressLineCount = address.getMaxAddressLineIndex() + 1;
                    if (addressLineCount > 0){
                        QualityAddress qualityAddress = 
                                new QualityAddress(i, true, distance,
                                                   address.getAddressLine(0), 
                                                   PictureGrouping.ADDRESS_LINE_QUALITY,
                                                   myLocality, mySubRegion, myRegion, myCountry, myCountryCode);
                        mAddressSet.add(qualityAddress);
                    }
                    
                    if (addressLineCount > 1){
                        QualityAddress qualityAddress = 
                                new QualityAddress(i, true, distance,
                                                   address.getAddressLine(addressLineCount-1),
                                                   PictureGrouping.ADDRESS_LINE_QUALITY,
                                                   myLocality, mySubRegion, myRegion, myCountry, myCountryCode);
                        mAddressSet.add(qualityAddress);
                    }
                    
                    // The thoroughfare (street name) will never be awesome, give it a lower quality
                    if (address.getThoroughfare() != null){
                        QualityAddress qualityAddress = 
                                new QualityAddress(i, true, distance,
                                                   address.getThoroughfare(),
                                                   PictureGrouping.THOROUGHFARE_QUALITY,
                                                   myLocality, mySubRegion, myRegion, myCountry, myCountryCode);
                        mAddressSet.add(qualityAddress);
                    }
                    
                    // The feature is intended to be like a "point-of-interest", so give it a good quality
                    if (address.getFeatureName() != null){
                        QualityAddress qualityAddress = 
                                new QualityAddress(i, false, distance,
                                                   address.getFeatureName(),
                                                   PictureGrouping.FEATURE_QUALITY,
                                                   myLocality, mySubRegion, myRegion, myCountry, myCountryCode);
                        mAddressSet.add(qualityAddress);
                    }
                }
            }
            
            if (PictureGrouping.DEBUG_QUALITY_ADDRESSES){
                Log.i(PictureGrouping.TAG, "CachedAddress.parseAddressList(" + ((addressList == null) ? null : addressList.size()) + " => " + this);
            }
        }

        
        @Override
        public String toString(){
            return "CachedAddress { " + (LATITUDE_UNHASH_COEF * mLatitudeHash) + ", " + (LONGITUDE_UNHASH_COEF * mLongitudeHash) + 
                        ", addressSet: " + ((mAddressSet == null) ? null : mAddressSet.size()) +
                        ", failCount: " + mFailCount + " (" + mFailTimestamp + ") }";
        }
    }
    
    private AddressCache(Context context){
        Log.i(PictureGrouping.TAG, "AddressCache.onCreate(){");
        
        mContext = context;
        mDBHelper = new AddressDBHelper(mContext);
        mCachedAddresses.clear();
        
        //readAllFromDatabase();
        
        Log.i(PictureGrouping.TAG, "} AddressCache.onCreate()");
    }
 
    private void checkThread(){
        long threadId = Thread.currentThread().getId();
        if (threadId != mThreadId){
            throw new Error("The address cache has been called from two different threads: instantiated on " + mThreadId + " and currently used on " + threadId);
        }
    } 
    
    void addListener(Listener listener){
        checkThread();
        
        // A little bit tricky with these WeakReferences...
        Iterator<WeakReference<Listener>> iterator = mListeners.iterator();
        while (iterator.hasNext()){
            WeakReference<Listener> listenerRef = iterator.next();
            if (listenerRef.get() == null ||
                listenerRef.get() == listener){
                // Already in the list...
                return;
            }
        }
        
        mListeners.add(new WeakReference<Listener>(listener));
    }
    
    void removeListener(Listener listener){
        checkThread();
        
        // A little bit tricky with these WeakReferences...
        Iterator<WeakReference<Listener>> iterator = mListeners.iterator();
        while (iterator.hasNext()){
            WeakReference<Listener> listenerRef = iterator.next();
            if (listenerRef.get() == null ||
                listenerRef.get() == listener){
                iterator.remove();
            }
        }
    }
    
    
    private CachedAddress getFromCacheOrDatabase(float latitude, float longitude){
        int latitudeHash = CachedAddress.hashLatitude(latitude);
        int longitudeHash = CachedAddress.hashLongitude(latitude, longitude);
        mTempCacheKey.init(latitudeHash, longitudeHash);
        
        CachedAddress cachedAddress = mCachedAddresses.get(mTempCacheKey);
        if (cachedAddress == null){
            cachedAddress = queryFromDatabase(latitudeHash, longitudeHash);
            if (cachedAddress != null){
                mCachedAddresses.put(cachedAddress, cachedAddress);
            }
        }
        
        return cachedAddress;
    }
    
    // Return true if a Geodecoding job has been enqueued
    // Return false otherwise (address already known, or not time to retry geodecoding)
    boolean prefetchAddress(float latitude, float longitude){
        checkThread();
        
        CachedAddress cachedAddress = getFromCacheOrDatabase(latitude, longitude);
        if (cachedAddress == null){
            return enqueueGeodecodingJob(latitude, longitude);
        }
        else if (cachedAddress.mAddressSet == null){
            return enqueueGeodecodingJob(cachedAddress);
        }
        return false;
    }
    
    
    Set<QualityAddress> getAddressSet(float latitude, float longitude){
        checkThread();
        Set<QualityAddress> addressSet = null;
        
        CachedAddress cachedAddress = getFromCacheOrDatabase(latitude, longitude);
        if (cachedAddress == null){
            enqueueGeodecodingJob(latitude, longitude);
        }
        else {
            if (cachedAddress.mAddressSet == null){
                enqueueGeodecodingJob(cachedAddress);
            }
            addressSet = cachedAddress.mAddressSet;
        }
        
        return addressSet;
    }
    
    
    // Enqueue Geodecoding job for coordinates which are NOT YET in the cache
    // Return true if a Geodecoding job has been enqueued
    // Return false otherwise (address already known, or not time to retry geodecoding)
    private boolean enqueueGeodecodingJob(float latitude, float longitude){
        CachedAddress cachedAddress = 
                new CachedAddress(CachedAddress.hashLatitude(latitude),
                                  CachedAddress.hashLongitude(latitude, longitude));
        cachedAddress.mLatitudeHash = CachedAddress.hashLatitude(latitude);
        cachedAddress.mLongitudeHash = CachedAddress.hashLongitude(latitude, longitude);
        mCachedAddresses.put(cachedAddress, cachedAddress);
        
        reallyEnqueueGeodecodingJob(cachedAddress); // Put in the queue
        return true;
    }
    
    
    // Enqueue Geodecoding job for coordinates which are ALREADY in the cache
    // Return true if a Geodecoding job has been enqueued
    // Return false otherwise (address already known, or not time to retry geodecoding)
    private boolean enqueueGeodecodingJob(CachedAddress cachedAddress){
        if (cachedAddress.mGeodecodingInProgress){
            return true; // Already in the queue
        }
        if (cachedAddress.isTimeToGeodecode()){
            reallyEnqueueGeodecodingJob(cachedAddress); // Put in the queue
            return true;
        }
        return false; // Not in the queue
    }
    
    
    private void reallyEnqueueGeodecodingJob(CachedAddress cachedAddress){
        if (PictureGrouping.DEBUG_ADDRESS_CACHE){
            Log.i(PictureGrouping.TAG, "AddressCache.reallyEnqueueGeodecodingJob(" + cachedAddress + ")");
        }
        cachedAddress.mGeodecodingInProgress = true;
        mGeodecodingJobs.add(cachedAddress);
        triggerGeodecoding();
    }
    
    private Runnable mGeodecodingRunnable = 
            new Runnable(){
        @Override
        public void run(){
            try {
                CachedAddress cachedAddress = mGeodecodingJobs.poll();
                if (cachedAddress != null){
                    mGeodecodingJobInProgress = true;
                    if (PictureGrouping.USE_FAKE_GEOCODING){
                        List<Address> addressList = new ArrayList<Address>();
                        for (int i=0; i < 8; i++){
                            Address address = new Address(Locale.US);
                            address.setLatitude(CachedAddress.unhashLatitude(cachedAddress.mLatitudeHash));
                            address.setLongitude(CachedAddress.unhashLongitude(cachedAddress.mLatitudeHash, cachedAddress.mLongitudeHash));
                            address.setAddressLine(0, "A"  + Long.toString((long) (address.getLatitude() * 1000)));
                            address.setLocality(      "L"  + Long.toString((long) (address.getLatitude() * 100)));
                            address.setSubAdminArea(  "SR" + Long.toString((long) (address.getLatitude() * 10)));
                            address.setAdminArea(     "R"  + Long.toString((long) (address.getLatitude())));
                            address.setCountryName(   "C"  + Long.toString((long) (address.getLatitude() / 10)));
                            
                            addressList.add(address);
                        }
                        cachedAddress.parseAddressList(addressList);
                    }
                    else {
                        GeocoderTask.getAddressFromLocation(CachedAddress.unhashLatitude(cachedAddress.mLatitudeHash),
                                                            CachedAddress.unhashLongitude(cachedAddress.mLatitudeHash, cachedAddress.mLongitudeHash),
                                                            mContext,
                                                            new GeocoderHandler(AddressCache.this, cachedAddress));
                    }
                }
                else {
                    if (mGeodecodingJobs.size() == 0){
                        triggerUpdateComplete();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    
    private void triggerGeodecoding(){
        if (! mGeodecodingJobInProgress){
            mHandler.removeCallbacks(mGeodecodingRunnable);
            mHandler.removeCallbacks(mUpdateCompleteRunnable);
            mHandler.post(mGeodecodingRunnable);
        }
    }
    
    private void triggerUpdateComplete(){
        // Optional: Wait a little before actually invoking the callback
        // in case some other queries are on the way...
        mHandler.postDelayed(mUpdateCompleteRunnable, 1000);
    }
    
    
    private Runnable mUpdateCompleteRunnable = 
            new Runnable(){
        @Override
        public void run(){
            Iterator<WeakReference<Listener>> iterator = mListeners.iterator();
            while (iterator.hasNext()){
                WeakReference<Listener> listenerRef = iterator.next();
                Listener listener = listenerRef.get();
                if (listenerRef.get() == null){
                    iterator.remove();
                }
                else {
                    try {
                        listener.onAddressCacheUpdated(mSuccessCount > 0);
                    }
                    catch (Exception e){
                        Log.w(PictureGrouping.TAG, "*** Problem with listener: " + listener);
                        e.printStackTrace();
                    }
                }
            }
            mSuccessCount = 0;
        }
    };
    
    
    private void parseAddressList(CachedAddress cachedAddress, List<Address> addressList){
        if (PictureGrouping.DEBUG_ADDRESS_CACHE){
            Log.i(PictureGrouping.TAG, "AddressCache.parseAddressList(): success: " + (addressList != null) +
                      ", size: " + ((addressList != null) ? addressList.size() : "N/A"));
        }
        mGeodecodingJobInProgress = false;
        cachedAddress.parseAddressList(addressList);
        List<CachedAddress> cachedAddresses = new ArrayList<CachedAddress>();
        cachedAddresses.add(cachedAddress);
        saveToDatabase(cachedAddresses);
        
        if (addressList != null){
            mSuccessCount++;
            mConsecutiveFailures = 0;
        }
        else {
            mConsecutiveFailures++;
            if (mConsecutiveFailures >= MAX_CONSECUTIVE_FAILURES){
                Log.d(PictureGrouping.TAG, "AddressCache.parseAddressList(): reached max number of consecutive failures " +
                        "=> set all other geocoding requests as failed without trying... "+
                        "items concerned: " + mGeodecodingJobs.size());
                cachedAddresses = new ArrayList<CachedAddress>(mGeodecodingJobs);
                Log.d(PictureGrouping.TAG, "cachedAddresses.size(): " + cachedAddresses.size());
                mGeodecodingJobs.clear();
                Log.d(PictureGrouping.TAG, "cachedAddresses.size(): " + cachedAddresses.size());
                for (CachedAddress address : cachedAddresses){
                    address.parseAddressList(null);
                }
                saveToDatabase(cachedAddresses);
            }
        }
        
        if (mGeodecodingJobs.size() == 0){
            triggerUpdateComplete();
        }
        else {
            triggerGeodecoding();
        }
    }
    
    private static class GeocoderHandler extends Handler {
        private WeakReference<AddressCache> mAddressCache;
        private WeakReference<CachedAddress> mCachedAddress;
        
        GeocoderHandler(AddressCache addressCache, CachedAddress cachedAddress){
            mAddressCache = new WeakReference<AddressCache>(addressCache);
            mCachedAddress = new WeakReference<CachedAddress>(cachedAddress);
        }
        
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Log.d(PictureGrouping.TAG, "AddressCache.GeocoderHandler.handleMessage()");
                    Bundle bundle = message.getData();
                    ArrayList<Address> addressList = bundle.getParcelableArrayList("address-list");
                    //double latitude = bundle.getDouble("latitude");
                    //double longitude = bundle.getDouble("longitude");
                    AddressCache addressCache = mAddressCache.get();
                    CachedAddress cachedAddress = mCachedAddress.get();
                    if (addressCache != null && cachedAddress != null){
                        addressCache.parseAddressList(cachedAddress, addressList);
                    }
                    break;
                    
                default:
            }
            //mActivity.mTextView.setText(locationAddress);
        }
    }
    
    
    private void saveToDatabase(List<CachedAddress> cachedAddresses){
        try {
            if (PictureGrouping.DEBUG_ADDRESS_CACHE){
                Log.i(PictureGrouping.TAG, "AddressCache.saveToDatabase(itemCount: " + cachedAddresses.size() + "){");
            }
            
            String tableName = AddressTable.TABLE_NAME;
            
            // Clear everything and query missing data
            
            long rowId = -1;
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            
            try {
                db.beginTransaction();
                
                for (CachedAddress cachedAddress : cachedAddresses){
                    ContentValues values = new ContentValues();
                    values.put(AddressTable._LATITUDE_HASH, cachedAddress.mLatitudeHash);
                    values.put(AddressTable._LONGITUDE_HASH, cachedAddress.mLongitudeHash);
                    if (cachedAddress.mAddressSet != null){
                        if (PictureGrouping.VERBOSE_ADDRESS_CACHE){
                            Log.i(PictureGrouping.TAG, "cachedAddress.mAddressSet: " + cachedAddress.mAddressSet.size());
                        }
                        values.put(AddressTable._ADDRESS_SET, AddressDBContract.packAddressSet(cachedAddress.mAddressSet));
                    }
                    else {
                        values.put(AddressTable._ADDRESS_SET, (String) null);
                    }
                    values.put(AddressTable._FAIL_COUNT, cachedAddress.mFailCount);
                    values.put(AddressTable._FAIL_TIMESTAMP, cachedAddress.mFailTimestamp);
    
                    rowId = db.insert(tableName, null, values);
                    if (rowId < 0){
                        throw new Exception("*** Insertion failed for " + cachedAddress);
                    }
                    if (rowId >= 0){
                        Log.d(PictureGrouping.TAG, "Inserted one item into the location table: " + cachedAddress);
                    }
                }
                
                db.setTransactionSuccessful();
            } 
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        if (PictureGrouping.DEBUG_ADDRESS_CACHE){
            Log.i(PictureGrouping.TAG, "} AddressCache.saveToDatabase()");
        }
    }
    
    
    // This function could return a simple Set, or even a List
    // but we will want to query the result to match with Pictures from the MediaStore DB
    // and a Map is the best way to do it...
    /*
    private void readAllFromDatabase(){
        Log.i(Poladroid.TAG, "AddressCache.readAllFromDatabase(){");
        
        mCachedAddresses.clear();
        
        try {
            String tableName = AddressTable.TABLE_NAME;
            String[] projection = null; // Let's get everything (just for test)
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;
            
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            
            try {
                db.acquireReference();
                
                Cursor cursor = db.query(tableName,  // The table to query
                                         projection,                               // The columns to return
                                         selection,                                // The columns for the WHERE clause
                                         selectionArgs,                            // The values for the WHERE clause
                                         null,                                     // don't group the rows
                                         null,                                     // don't filter by row groups
                                         sortOrder);                               // The sort order
                
                int latitudeHashIndex = cursor.getColumnIndex(AddressTable._LATITUDE_HASH);
                int longitudeHashIndex = cursor.getColumnIndex(AddressTable._LONGITUDE_HASH);
                int addressSetIndex = cursor.getColumnIndex(AddressTable._ADDRESS_SET);
                int failCountIndex = cursor.getColumnIndex(AddressTable._FAIL_COUNT);
                int failTimestampIndex = cursor.getColumnIndex(AddressTable._FAIL_TIMESTAMP);

                while (cursor.moveToNext()){
                    try {
                        int latitudeHash = cursor.getInt(latitudeHashIndex);
                        int longitudeHash = cursor.getInt(longitudeHashIndex);
                        int failCount = cursor.getInt(failCountIndex);
                        long failTimestamp = cursor.getLong(failTimestampIndex);
                        Set<QualityAddress> addressSet = null;
                        if (! cursor.isNull(addressSetIndex)){
                            String addressSetString = cursor.getString(addressSetIndex);
                            if (Poladroid.VERBOSE_ADDRESS_CACHE){
                                Log.i(Poladroid.TAG, "addressSetString: " + addressSetString);
                            }
                            addressSet = AddressDBContract.unpackAddressSet(addressSetString);
                        }
                        
                        CachedAddress cachedAddress = new CachedAddress(latitudeHash, longitudeHash);
                        cachedAddress.mAddressSet = addressSet;
                        cachedAddress.mFailCount = failCount;
                        cachedAddress.mFailTimestamp = failTimestamp;
                        
                        mCachedAddresses.put(cachedAddress, cachedAddress);
                        //Log.d(Poladroid.TAG, "New cached address from DB: " + cachedAddress); Utils.sleep();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            finally {
                db.releaseReference();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    
        Log.i(Poladroid.TAG, "} AddressCache.readAllFromDatabase() => address count: " + mCachedAddresses.size());
    }
    */
    
    
    // This function could return a simple Set, or even a List
    // but we will want to query the result to match with Pictures from the MediaStore DB
    // and a Map is the best way to do it...
    private CachedAddress queryFromDatabase(int latitudeHash, int longitudeHash){
        if (PictureGrouping.DEBUG_ADDRESS_CACHE){
            Log.i(PictureGrouping.TAG, "AddressCache.queryFromDatabase(" + latitudeHash + ", " + longitudeHash + "){");
        }
        
        CachedAddress cachedAddress = null;
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 begin
        Cursor cursor = null;
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 end
        
        try {
            String tableName = AddressTable.TABLE_NAME;
            String[] projection = null; // Let's get everything (just for test)
            String selection = AddressTable._LATITUDE_HASH + " = + " + latitudeHash + " AND " + AddressTable._LONGITUDE_HASH + " = " + longitudeHash;
            String[] selectionArgs = null;
            String sortOrder = null;
            
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            
            try {
                db.acquireReference();
                //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 begin
                cursor = db.query(tableName,  // The table to query
                                         projection,                               // The columns to return
                                         selection,                                // The columns for the WHERE clause
                                         selectionArgs,                            // The values for the WHERE clause
                                         null,                                     // don't group the rows
                                         null,                                     // don't filter by row groups
                                         sortOrder);                               // The sort order
                //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 end
                int addressSetIndex = cursor.getColumnIndex(AddressTable._ADDRESS_SET);
                int failCountIndex = cursor.getColumnIndex(AddressTable._FAIL_COUNT);
                int failTimestampIndex = cursor.getColumnIndex(AddressTable._FAIL_TIMESTAMP);
                
                while (cursor.moveToNext()){
                    try {
                        int failCount = cursor.getInt(failCountIndex);
                        long failTimestamp = cursor.getLong(failTimestampIndex);
                        Set<QualityAddress> addressSet = null;
                        if (! cursor.isNull(addressSetIndex)){
                            String addressSetString = cursor.getString(addressSetIndex);
                            if (PictureGrouping.VERBOSE_ADDRESS_CACHE){
                                Log.i(PictureGrouping.TAG, "addressSetString: " + addressSetString);
                            }
                            addressSet = AddressDBContract.unpackAddressSet(addressSetString);
                        }
                        
                        cachedAddress = new CachedAddress(latitudeHash, longitudeHash);
                        cachedAddress.mAddressSet = addressSet;
                        cachedAddress.mFailCount = failCount;
                        cachedAddress.mFailTimestamp = failTimestamp;
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            finally {
                //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 begin
                if(cursor != null) {
                    cursor.close();
                }
                //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-1-18,ALM-958124 en
                db.releaseReference();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        
        if (PictureGrouping.DEBUG_ADDRESS_CACHE){
            Log.i(PictureGrouping.TAG, "} AddressCache.queryFromDatabase() => cachedAddress: " + cachedAddress);
        }
        
        return cachedAddress;
    }
}

/* EOF */

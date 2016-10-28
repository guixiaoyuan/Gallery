package com.tct.gallery3d.picturegrouping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tct.gallery3d.BuildConfig;

import android.content.Context;
import android.util.Log;


public class GroupBuilder implements BucketBuilder.Client {
    interface Client {
        void onNewGroup(PictureGroup pictureGroup);
    }
    
    private Client mClient;
    private BucketBuilder mBucketBuilder = new BucketBuilder(this);
    private AddressCache mAddressCache;
    
    GroupBuilder(Context context, Client client){
        mClient = client;
        mAddressCache = AddressCache.getInstance(context);
        Log.i(PictureGrouping.TAG, "new GroupBuilder()");
        
        //Utils.getPictureCountRequirement(10, AdminLevel.ADDRESS);
        //Utils.getBucketRatioRequirement(10, AdminLevel.ADDRESS);
    }
    
    void consumePicture(Picture picture){
        mBucketBuilder.consumePicture(picture);
    }
    
    void end(){
        mBucketBuilder.end();
    }
    
    @Override
    public void onNewPictureBucket(List<Picture> pictureBucket){
        Log.i(PictureGrouping.TAG, "GroupBuilder.onNewPictureBucket(" + ((pictureBucket != null) ? pictureBucket.get(0) : null) + ")");
        buildGroups(pictureBucket);
    }

    private Comparator<Picture> mPictureTimestampComparator = 
            new Comparator<Picture>(){
        @Override
        public int compare(Picture lhs, Picture rhs){
            // Sort by decreasing timestamp
            if (rhs.mTimestamp > lhs.mTimestamp) return  1;
            if (rhs.mTimestamp < lhs.mTimestamp) return -1;
            return 0;
        }
    };
        
    private void notifyClient(PictureGroup group){
        if (group != null){
            try {
                Collections.sort(group.mPictures, mPictureTimestampComparator);
                mClient.onNewGroup(group);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    

    
    
    private boolean isValidGroup(int pictureCount, int bucketSize, AdminLevel adminLevel){
        if (bucketSize <= 0) return false;
        
        float bucketRatio = 1f * pictureCount / bucketSize;
        
        boolean isValidGroup = false;
        
        int groupCount = 0;
        int pictureCountRequirement;
        do {
            groupCount++;
            pictureCountRequirement = Utils.getPictureCountRequirement(groupCount, adminLevel);
            if (pictureCount >= pictureCountRequirement){
                float bucketRatioRequirement = Utils.getBucketRatioRequirement(groupCount, adminLevel);
                if (bucketRatio >= bucketRatioRequirement){
                    Log.i(PictureGrouping.TAG, "ok for groupCount " + groupCount +
                          ", pictureCount: " + pictureCount + 
                          ", pictureCountRequirement: " + pictureCountRequirement + 
                          ", bucketRatio: " + bucketRatio + 
                          ", bucketRatioRequirement: " + bucketRatioRequirement);
                    isValidGroup = true;
                }
            }
        }
        while(pictureCount >= pictureCountRequirement && ! isValidGroup);
        
        Log.i(PictureGrouping.TAG, "GroupBuilder.isValidGroup(pictCount: " + pictureCount + ", bucketSize: " + bucketSize + ", adminLevel: " + adminLevel + ") => " + isValidGroup);
        
        return isValidGroup;
    }
    
    
    
    private void classifyPicture(Picture up, PictureGroup pg){
        if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
            Log.i(PictureGrouping.TAG, "classifyPicture(" + up + " => " + pg + ")");
        }
        pg.mPictures.add(up);
    }
    
    private void classifyPictures(List<Picture> unclassifiedPictures, 
                                       Picture pRef1, PictureGroup pg1, Picture pRef2, PictureGroup pg2){
        for (Picture up : unclassifiedPictures){
            if (pRef1 != null && pg1 != null && pRef2 != null && pg2 != null){
                if (Math.abs(up.mTimestamp - pRef1.mTimestamp) < Math.abs(up.mTimestamp - pRef2.mTimestamp)){
                    classifyPicture(up, pg1);
                }
                else {
                    classifyPicture(up, pg2);
                }
            }
            else if (pg1 != null){
                classifyPicture(up, pg1);
            }
            else if (pg2 != null){
                classifyPicture(up, pg2);
            }
        }
    }
    
    
    private void classifyPicturesWithoutLocation(List<Picture> pictureBucket, List<PictureGroup> allGroups){
        // Attach location-less pictures to other groups
        Map<Picture, PictureGroup> picture2Group = new HashMap<Picture, PictureGroup>(pictureBucket.size());
        for(PictureGroup group : allGroups){
            for(Picture picture : group.mPictures){
                picture2Group.put(picture, group);
            }
        }
        
        Picture pRef = null;
        PictureGroup pgRef = null;
        List<Picture> unclassifiedPictures = new ArrayList<Picture>(pictureBucket.size());
        for (Picture p : pictureBucket){
            PictureGroup pg = picture2Group.get(p);
            if (pg != null){
                // Found a pictureGroup, can classify some photos
                classifyPictures(unclassifiedPictures, pRef, pgRef, p, pg);
                unclassifiedPictures.clear();
                pRef = p;
                pgRef = pg;
            }
            else {
                // This is a picture without group, add it to the unclassified list
                unclassifiedPictures.add(p);
            }
        }
        // For the last photos
        if (unclassifiedPictures.size() > 0){
            if (pgRef == null){
                // We have NO group in progress, probably because we have no photo in any group yet
                if (allGroups.size() == 0){
                    PictureGroup fallbackGroup = new PictureGroup();
                    fallbackGroup.mAddress = UsableAddress.get();
                    allGroups.add(fallbackGroup);
                }
                classifyPictures(unclassifiedPictures, null, allGroups.get(0), null, null);
            }
            else {
                // We have a group in progress
                classifyPictures(unclassifiedPictures, null, pgRef, null, null);
            }
        }
        unclassifiedPictures.clear();
    } 
    
    
    private void tieToGroups(List<PictureGroup> groups, List<Picture> remainingPictures){
        if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
            Log.i(PictureGrouping.TAG, "GroupBuilder.tieToGroups(remainingPictures: " + remainingPictures.size() + "){");
        }
        
        for (PictureGroup group : groups){
            group.computeCoordStats();
        }
        
        Iterator<Picture> iterator = remainingPictures.iterator();
        while (iterator.hasNext()){
            Picture picture = iterator.next();
            
            if (picture.mHasCoordinates){
                PictureGroup bestGroup = null;
                double bestDistance = 0;
                for (PictureGroup group : groups){
                    if (group.mHasCoordStats){
                        double latitudeDistance = Math.abs(group.mCoordCenter[0] - picture.mLatitude);
                        double longitudeDistance = Math.abs(group.mCoordCenter[1] - picture.mLongitude);
                        if (latitudeDistance < PictureGrouping.GROUP_RATTACHMENT_MAX_STD_DEV_RATIO * group.mCoordStdDev[0] &&
                            longitudeDistance < PictureGrouping.GROUP_RATTACHMENT_MAX_STD_DEV_RATIO * group.mCoordStdDev[1]){
                            double distance = Math.sqrt(latitudeDistance*latitudeDistance + longitudeDistance*longitudeDistance);
                            if (picture.mAddressSet.iterator().next() != null){
                                Log.i(PictureGrouping.TAG, "==> Could rattach picture: " + picture.mAddressSet.iterator().next() +
                                      ", to group: " + group +
                                      ", distance to center: " + (int) Utils.latitudeToMeters(distance) + "m");
                                if (bestGroup == null || distance < bestDistance){
                                    if (bestGroup != null){
                                        Log.i(PictureGrouping.TAG, "==> Better than previous proposals !!");
                                    }
                                    bestGroup = group;
                                    bestDistance = distance;
                                }
                            }
                        }
                    }
                }
                
                if (bestGroup != null){
                    Log.i(PictureGrouping.TAG, "==> Rattaching picture: " + picture.mAddressSet.iterator().next() +
                                 ", to group: " + bestGroup);
                    bestGroup.mPictures.add(picture);
                    iterator.remove();
                }
            }
        }
        
        if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
            Log.i(PictureGrouping.TAG, "} GroupBuilder.tieToGroups() => remainingPictures: " + remainingPictures.size());
        }
    }
    
    
    private void cleanHeatMap(Map<UsableAddress, UsableAddress> heatMap){
        for (UsableAddress usableAddress : heatMap.keySet()){
            UsableAddress.release(usableAddress);
        }
        heatMap.clear();
    }
    
    
    private Map<UsableAddress, UsableAddress> buildHeatMap(List<Picture> remainingPictures, AdminLevel pointOfInterestType){
        if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
            Log.i(PictureGrouping.TAG, "GroupBuilder.buildHeatMap(){");
        }
        
        Map<UsableAddress, UsableAddress> heatMap = new HashMap<UsableAddress, UsableAddress>();
        
        Set<UsableAddress> set = new HashSet<UsableAddress>();
        UsableAddress tmpReadableLocation = UsableAddress.get();
        
        for (Picture picture : remainingPictures){
            //Log.i(Poladroid.TAG, "@@@>Processing: " + picture);
            if (picture.mAddressSet != null){
                for (QualityAddress address : picture.mAddressSet){
                    //Log.i(Poladroid.TAG, "#> Processing: " + pictureLocation.mIntialAddress);
                    for (AdminLevel administrativeAreaType : AdminLevel.values()){
                        if (pointOfInterestType.ordinal() < administrativeAreaType.ordinal() || 
                            pointOfInterestType == AdminLevel.VOID && administrativeAreaType == AdminLevel.VOID){
                            if (address.getUsableAddress(pointOfInterestType, administrativeAreaType, tmpReadableLocation)){
                                if (! set.contains(tmpReadableLocation)){
                                    UsableAddress ref = 
                                            UsableAddress.get(tmpReadableLocation.mPointOfInterestType,
                                                              tmpReadableLocation.mPointOfInterest, 
                                                              tmpReadableLocation.mAdminAreaType,
                                                              tmpReadableLocation.mAdminArea);
                                    set.add(ref);
                                    
                                    ref = heatMap.get(tmpReadableLocation);
                                    if (ref == null){
                                        ref = UsableAddress.get(tmpReadableLocation.mPointOfInterestType,
                                                                tmpReadableLocation.mPointOfInterest, 
                                                                tmpReadableLocation.mAdminAreaType,
                                                                tmpReadableLocation.mAdminArea);
                                        heatMap.put(ref, ref);
                                        //Log.i(Poladroid.TAG, tmpReadableLocation.toString() + " not yet mapped");
                                    }
                                    else {
                                        //Log.i(Poladroid.TAG, tmpReadableLocation.toString() + " already mapped as " + ref);
                                    }
                                    ref.addReference(picture, tmpReadableLocation.mQuality);
                                }
                            }
                        }
                    }
                }
            }
            
            for (UsableAddress usableAddress : set){
                UsableAddress.release(usableAddress);
            }
            if (set.size() == 0){
                if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
                    Log.i(PictureGrouping.TAG, "GroupBuilder.buildHeatMaps(" + pointOfInterestType + "): didn't find any valid UsageAddress for " + picture);
                    if (picture.mAddressSet == null){
                        Log.i(PictureGrouping.TAG, "GroupBuilder.buildHeatMap(): picture.mAddressSet is null !?");
                    }
                    else if (picture.mAddressSet.size() == 0){
                        Log.i(PictureGrouping.TAG, "GroupBuilder.buildHeatMap(): picture.mAddressSet is empty !?");
                    }
                    else {
                        for (QualityAddress qualityAddress : picture.mAddressSet){
                            Log.i(PictureGrouping.TAG, "Including: " + qualityAddress);
                        }
                    }
                }
            }
            set.clear();
        }
        
        if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
            Log.i(PictureGrouping.TAG, "} GroupBuilder.buildHeatMap()");
        }
        
        return heatMap;
    }
    
    private List<UsableAddress> sortHeatMap(Map<UsableAddress, UsableAddress> heatMap,
                                            AdminLevel pointOfInterestType,
                                            final int initialPictureCount){
        if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
            Log.i(PictureGrouping.TAG, "GroupBuilder.sortHeatMap(){");
            
            Log.i(PictureGrouping.TAG, "-----------------------------------");
            Log.i(PictureGrouping.TAG, pointOfInterestType.toString() + " Heat-Map");
        }
        
        List<UsableAddress> sortedHeatMap = new ArrayList<UsableAddress>(heatMap.keySet());
        Collections.sort(sortedHeatMap,
                         new Comparator<UsableAddress>(){
                @Override
                public int compare(UsableAddress lhs, UsableAddress rhs){
                    return (int) Math.signum(rhs.getScore(initialPictureCount) - lhs.getScore(initialPictureCount));
                }
            });
        
        if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
            for (UsableAddress usableAddress : sortedHeatMap){
                Log.i(PictureGrouping.TAG, "Score: " + usableAddress.getScore(initialPictureCount) + " for " + usableAddress); Utils.sleep();
            }
            
            Log.i(PictureGrouping.TAG, "} GroupBuilder.sortHeatMap() => item count: " + heatMap.size());
        }
        
        return sortedHeatMap;
    }
    
    
    private PictureGroup findInterestingGroup(List<Picture> remainingPictures, AdminLevel pointOfInterestType, int initialPictureCount){
        if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
            Log.i(PictureGrouping.TAG, "GroupBuilder.findInterestingGroup(remainingPictures: " + remainingPictures.size() + ", pointOfInterestType: " + pointOfInterestType + "){");
        }
        
        Map<UsableAddress, UsableAddress> heatMap = buildHeatMap(remainingPictures, pointOfInterestType);
        List<UsableAddress> sortedHeatMap = sortHeatMap(heatMap, pointOfInterestType, initialPictureCount);
        
        PictureGroup group = null;
        if (sortedHeatMap != null && sortedHeatMap.size() > 0){
            UsableAddress usableAddress = sortedHeatMap.get(0);
            if (usableAddress != null &&
                (usableAddress.mRefCount == remainingPictures.size() ||
                 isValidGroup(usableAddress.mRefCount, initialPictureCount, usableAddress.mPointOfInterestType))){
                group = new PictureGroup();
                group.mAddress = UsableAddress.duplicate(usableAddress);
                group.mPictures.addAll(usableAddress.mPictures);
            }
        }
        
        cleanHeatMap(heatMap);
        
        if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
            Log.i(PictureGrouping.TAG, "} GroupBuilder.findInterestingGroup() => group: " + group);
        }
        
        return group;
    }
    
    
    private void buildGroups(List<Picture> pictureBucket){
        Log.i(PictureGrouping.TAG, "GroupBuilder.buildGroups(){");
        
        List<Picture> picturesWithoutAddress = new ArrayList<Picture>(pictureBucket.size());
        List<Picture> picturesWithAddress = new ArrayList<Picture>(pictureBucket.size());
        int pictureWithCoordinates = 0;
        for (Picture picture : pictureBucket){
            boolean hasAddress = false;
            
            if (picture.mHasCoordinates){
                pictureWithCoordinates++;
                picture.mAddressSet = mAddressCache.getAddressSet(picture.mLatitude, picture.mLongitude);
                if (picture.mAddressSet != null && picture.mAddressSet.size() > 0){
                    if (PictureGrouping.VERBOSE_ADDRESS_CACHE){
                        Log.i(PictureGrouping.TAG, "QualityAddresses for " + picture + ": " + picture.mAddressSet.size());
                    }
                    hasAddress = true;
                }
            }
            
            if (hasAddress){
                picturesWithAddress.add(picture);
            }
            else {
                picturesWithoutAddress.add(picture);
            }
        }
        
        if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
            Log.i(PictureGrouping.TAG, "=================================");
            Log.i(PictureGrouping.TAG, "Daily bucket " + Utils.getGregorianDateString(pictureBucket.get(0).mTimestamp));
            Log.i(PictureGrouping.TAG, "---------------------------------");
            Log.i(PictureGrouping.TAG, "Bucket picture count: " + pictureBucket.size());
            Log.i(PictureGrouping.TAG, "Bucket pictures with coordinates: " + pictureWithCoordinates);
            Log.i(PictureGrouping.TAG, "Bucket pictures with cached address: " + picturesWithAddress.size());
            Log.i(PictureGrouping.TAG, "Bucket pictures without coordinates or cached address: " + picturesWithoutAddress.size());
        }
        
        int initialPictureCount = picturesWithAddress.size();
        
        List<PictureGroup> groups = new ArrayList<PictureGroup>();
        for (AdminLevel pointOfInterestType : AdminLevel.values()){;
            List<PictureGroup> levelGroups = new ArrayList<PictureGroup>();
            PictureGroup group = null;
            do {
                group = findInterestingGroup(picturesWithAddress, pointOfInterestType, initialPictureCount);
                if (group != null){
                    if (group.mPictures.size() == picturesWithAddress.size()){
                        // This is the last group, go back up in AdminLevel to find a better title...
                        for (int adminLevelOrdinal = pointOfInterestType.ordinal() - 1; adminLevelOrdinal >= 0; adminLevelOrdinal--){
                            AdminLevel tmpPointOfInterestType = AdminLevel.values()[adminLevelOrdinal];
                            PictureGroup tmpGroup = findInterestingGroup(picturesWithAddress, tmpPointOfInterestType, initialPictureCount);
                            if (tmpGroup != null && tmpGroup.mPictures.size() == picturesWithAddress.size()){
                                if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
                                    Log.i(PictureGrouping.TAG, "Found better proposal for the last group: " + tmpGroup);
                                }                                
                                group = tmpGroup;
                            }
                            else {
                                break;
                            }
                        }
                    }
                    
                    levelGroups.add(group);
                    picturesWithAddress.removeAll(group.mPictures);
                    if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
                        Log.i(PictureGrouping.TAG, "Remaining picture count: " + picturesWithAddress.size());
                    }
                }
            }
            while (group != null);
            
            if (levelGroups.size() > 0){
                tieToGroups(levelGroups, picturesWithAddress);
                groups.addAll(levelGroups);
            }
            
            if (picturesWithAddress.size() == 0){
                break;
            }
        }
        
        
        // Check we did a good work
        if (picturesWithAddress.size() > 0){
            Log.i(PictureGrouping.TAG, "Remaining location-aware pictures: " + picturesWithAddress.size());
            if (BuildConfig.DEBUG){
                throw new Error("*** The algorithm hasn't consumed every picture with address !");
            }
            classifyPicturesWithoutLocation(picturesWithAddress, groups);
        }
        
        classifyPicturesWithoutLocation(picturesWithoutAddress, groups);
        
        
        // Publish the groups ~
        Log.i(PictureGrouping.TAG, "========> Grouping summary:");
        int pictureCount = 0;
        for(PictureGroup group : groups){
            pictureCount += group.mPictures.size();
            Log.i(PictureGrouping.TAG, "========> " + group);
            if (group.mPictures != null && group.mPictures.size() > 0){ // Just to be safe...
                notifyClient(group);
            }
        }
        
        // Check we did a good work
        if (pictureCount != pictureBucket.size()){
            Log.i(PictureGrouping.TAG, "Remaining pictures: " + (pictureBucket.size() - pictureCount));
            if (BuildConfig.DEBUG){
                throw new Error("The algorithm hasn't consumed every picture !");
            }
        }
        
        UsableAddress.releaseAll();
        
        Log.i(PictureGrouping.TAG, "} GroupBuilder.buildGroups()");
    }
}

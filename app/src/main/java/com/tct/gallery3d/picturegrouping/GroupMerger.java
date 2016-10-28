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
/* 26/12/2015|chengbin.du-nb        |ALM-1003219           |[Gallery]The moments display wrong date of pictures
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.picturegrouping;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;


public class GroupMerger implements GroupBuilder.Client {
    interface Client {
        void onNewGroup(PictureGroup pictureGroup);
    }
    
    private Client mClient;
    private GroupBuilder mGroupBuilder;
    
    //private PictureGroup mMergedGroup;
    private List<PictureGroup> mGroupsToMerge = new ArrayList<PictureGroup>(); 
    private final int MIN_PICTURE_COUNT = Utils.getPictureCountRequirement(1, AdminLevel.ADDRESS);

    
    GroupMerger(Context context, Client client){
        mClient = client;
        mGroupBuilder = new GroupBuilder(context, this);
        Log.i(PictureGrouping.TAG, "new GroupMerger(MIN_PICTURE_COUNT: " + MIN_PICTURE_COUNT + ")");
    }
    
    void consumePicture(Picture picture){
        mGroupBuilder.consumePicture(picture);
    }
    
    void end(){
        mGroupBuilder.end();
        mergeGroups(null);
    }
    
    @Override
    public void onNewGroup(PictureGroup pictureGroup){
        notifyClient(pictureGroup);
    }
    
    private void notifyClient(PictureGroup group){
        if (group != null){
            try {
                //Collections.sort(group.mPictures, mPictureTimestampComparator);
                mClient.onNewGroup(group);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    
    private void mergeGroups(){
        List<PictureGroup> groups = new ArrayList<PictureGroup>();
        
        // Merge
        PictureGroup group = null;
        for (int i=mGroupsToMerge.size()-1; i >= 0; i--){
            PictureGroup currentGroup = mGroupsToMerge.get(i);
            if (group == null){
                group = currentGroup;
            }
            else if (Math.abs(group.mPictures.get(0).mTimestamp - currentGroup.mPictures.get(0).mTimestamp) <= PictureGrouping.ONE_WEEK){
                // Merging
                group.mPictures.addAll(currentGroup.mPictures);
            }
            else {
                // Starting a new group
                groups.add(group);
                group = currentGroup;
            }
        }
        if (group != null){
            groups.add(group);
        }
        mGroupsToMerge.clear();
        
        // Publish results
        for (int i=groups.size()-1; i >= 0; i--){
            PictureGroup currentGroup = groups.get(i);
            notifyClient(currentGroup);
        }
    }
    
    private void mergeGroups(PictureGroup newGroup){
        if (newGroup == null){
            if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
                Log.i(PictureGrouping.TAG, "GroupMerger.mergeGroups(): This is the end...");
            }
            mergeGroups();
        }
        else {
            // Can we merge ?
            PictureGroup oldestGroup = null;
            if (mGroupsToMerge.size() > 0){
                oldestGroup = mGroupsToMerge.get(mGroupsToMerge.size() - 1);
            }
            // Same address, small picture count, timestamp proximity
            if (oldestGroup != null &&
                newGroup.mAddress != null && newGroup.mAddress.equals(oldestGroup.mAddress) &&
                newGroup.mPictures != null && newGroup.mPictures.size() > 0 && newGroup.mPictures.size() < MIN_PICTURE_COUNT &&
                Math.abs(oldestGroup.mPictures.get(0).mTimestamp - newGroup.mPictures.get(0).mTimestamp) <= PictureGrouping.ONE_WEEK){
                // Yes ! Can merge !
                if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
                    Log.i(PictureGrouping.TAG, "GroupMerger.mergeGroups(): To be merged: " + newGroup + " with existing list of " + mGroupsToMerge.size() + " groups");
                }
                mGroupsToMerge.add(newGroup);
            }
            else {
                // No, cannot merge !
                // Merge existing groups, if any
                if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
                    Log.i(PictureGrouping.TAG, "GroupMerger.mergeGroups(): Merging previous " + mGroupsToMerge.size() + " groups");
                }
                mergeGroups(); // Will clear mGroupsToMerge
                // and check if we have a mergeable group
                if (newGroup.mAddress != null &&
                    newGroup.mPictures != null && newGroup.mPictures.size() > 0 && newGroup.mPictures.size() < MIN_PICTURE_COUNT){
                    // Create a new list of groups to merge
                    if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
                        Log.i(PictureGrouping.TAG, "GroupMerger.mergeGroups(): Potential merge: " + newGroup);
                    }
                    mGroupsToMerge.add(newGroup);
                }
                else {
                    // Just emit the group directly
                    if (PictureGrouping.DEBUG_GROUPING_PIPELINE){
                        Log.i(PictureGrouping.TAG, "GroupMerger.mergeGroups(): Not a potential merge: " + newGroup);
                    }
                    notifyClient(newGroup);
                }
            }
        }
    }
}

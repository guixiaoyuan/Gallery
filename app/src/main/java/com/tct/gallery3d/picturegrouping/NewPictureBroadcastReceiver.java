package com.tct.gallery3d.picturegrouping;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NewPictureBroadcastReceiver extends BroadcastReceiver {
    private static boolean sHasNewPictures = false;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // assumes WordService is a registered service
        //Intent intent = new Intent(context, WordService.class);
        //context.startService(intent);
        Log.i(PictureGrouping.TAG, "NewPictureBroadcastReceiver.onReceive(" + intent + ")");
        
        sHasNewPictures = true;
        
        Intent prefetchService = new Intent(context, PrefetchService.class);
        prefetchService.setData(intent.getData());
        context.startService(prefetchService);
    }
    
    
    // Function intended for the MainActivity, and for the MainActivity ONLY !
    // It is very simplistic, make a new one if you have more complex needs
    static boolean hasNewPictures(){
        boolean hasNewPictures = sHasNewPictures;
        sHasNewPictures = false;
        Log.i(PictureGrouping.TAG, "NewPictureBroadcastReceiver.hasNewPictures() => " + hasNewPictures);
        return hasNewPictures;
    }
} 


/* EOF */
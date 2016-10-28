package com.tct.gallery3d.picturegrouping;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GeocoderTask {
    static {
        Log.i(PictureGrouping.TAG, "GeocoderTask.<init>() => Geocoder.isPresent(): " + Geocoder.isPresent());
    }
    
    public static void getAddressFromLocation(final double latitude, final double longitude,
                                                final Context context, final Handler handler) {

        Log.i(PictureGrouping.TAG, "GeocoderTask.getAddressFromLocation(" + latitude + ", " + longitude + ")");
        
        
        Thread thread = new Thread() {
            @Override
            public void run() {
                ArrayList<Address> addressList = null;
                try {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> list = geocoder.getFromLocation(latitude, longitude, 10);
                    if (list == null){
                        Log.d(PictureGrouping.TAG, "No address list for " + latitude + ", " + longitude);
                    }
                    else {
                        if (Math.random() < PictureGrouping.GEOCODER_FAILURE_RATE){
                            Log.i(PictureGrouping.TAG, "GeocoderTask.getAddressFromLocation(): simulate Geocoder failure");
                        }
                        else {
                            addressList = new ArrayList<Address>(list);
                        }
                    }
                }
                catch (Exception e){
                    Log.e(PictureGrouping.TAG, "Unable connect to Geocoder", e);
                }
                finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    if (addressList != null){
                        bundle.putParcelableArrayList("address-list", addressList);
                    }
                    message.setData(bundle);
                    Log.d(PictureGrouping.TAG, "Sending result to handler: " + addressList);
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }
}

/* EOF */
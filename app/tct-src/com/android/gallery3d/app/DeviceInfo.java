/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.tcl.com
 * PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

public class DeviceInfo {
    private static final String TAG = "Gallery-DeviceInfo";
    private static final String dlnaservice_DeviceInfo = "jrdcom.dlnaservice.DeviceInfo";
    private Class<?> JrdDeviceInfo = null;
    public Object obj=null;//fengke
    //private Context mContext;
    
    
    public DeviceInfo(Object this_obj) {
        //this.mContext = context;
        try {
            JrdDeviceInfo = Class.forName(dlnaservice_DeviceInfo);
            Log.d(TAG, "DeviceInfo:"+JrdDeviceInfo);
            obj = this_obj;
        } catch (Exception e) {
            Log.d(TAG, "DeviceInfo: e = "+e);
        }
    }
    
    public String getName() {
        String getName = null;
        if (JrdDeviceInfo != null && obj != null) {
            try {
                Method method=JrdDeviceInfo.getMethod("getName");//PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18
                getName = (String) method.invoke(obj);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: getName e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager getName:"+getName);
        return getName;
    }

    public String getUid() {
        String getUid = null;
        if (JrdDeviceInfo != null && obj != null) {
            try {
                Method method=JrdDeviceInfo.getMethod("getUid");
                getUid = (String) method.invoke(obj);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: getUid e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager getUid:"+getUid);
        return getUid;
    }

}

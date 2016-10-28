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

import com.android.gallery3d.app.DeviceInfo;

public class DLNAManager {
    private static final String TAG = "Gallery-DLNAManager";
    private static final String dlnaservice_DLNAManager = "jrdcom.dlnaservice.DLNAManager";
    private Class<?> JrdDLNAManager = null;
    private Object obj=null;
    private Context mContext;
    private static DLNAStatusController mDlnaStatusCrl = null;
    
    private static DLNAManager mDLNAManager = null;

    //final java.lang.reflect.Field field = d.getField(fieldName);
    //final int id = field.getInt(null);
    public final static String DEVICE_ADD="android.intent.device_add";
    public final static String DEVICE_REMOVE="android.intent.device_remove";
    public final static String SERVICE_STOP = "android.intent.action.DLNA_STOP";
    public final static String SERVICE_START = "android.intent.action.DLNA_START";
    public final static String SHARED_INNER_STOP = "android.intent.action.shared_inner_stop";
    public final static String SHARED_OUTER_STOP = "android.intent.action.shared_outer_stop";



    public DLNAManager(Context context) {
        this.mContext = context;
        try {
            JrdDLNAManager = Class.forName(dlnaservice_DLNAManager);
            Log.d(TAG, "DLNAManager:"+JrdDLNAManager);
            obj = context.getSystemService("dlna");
        } catch (Exception e) {
            Log.d(TAG, "DLNAManager: e = "+e);
        }
    }


    public static DLNAManager getInstance(Context context) {
        if (mDLNAManager == null) {
            mDLNAManager = new DLNAManager(context);
        }
        return mDLNAManager;
    }



    public String getPreviousFile(String identification){
        String ret_str = null;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("getPreviousFile", String.class);
                ret_str = (String) method.invoke(obj, identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: getPreviousFile e = "+ e);
            }
        }
        return ret_str;
    }

    public boolean mediaControlStreamPlay(String url, String name,String identification){
        boolean bRet = false;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlStreamPlay", String.class, String.class, String.class);
                bRet = (Boolean) method.invoke(obj, url, name, identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlStreamPlay e = "+ e);
            }
        }
        return bRet;
    }

    public void setTVListener(String identification,PlayStateListener listener, ProgressListener progressListener){
        if (JrdDLNAManager != null && obj != null) {
            /*
            try {
                Method method=JrdDLNAManager.getMethod("setTVListener", String.class, PlayStateListener.class, ProgressListener.class);
                method.invoke(obj, identification, listener, progressListener);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: setTVListener e = "+ e);
            }
            */
            mDlnaStatusCrl = DLNAStatusController.getInstance(mContext);
            mDlnaStatusCrl.SetDlnaListener(identification, listener, progressListener);
        }
    }

    public boolean hasConnected(){
        Boolean hasConnected = false;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("hasConnected");//PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18
                hasConnected = (Boolean) method.invoke(obj);
                //Log.d(TAG, "DLNAManager: hasConnected = "+hasConnected);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: hasConnected e = "+ e);
            }
        }
//disabled the dlna function because the project has not support dlna setting yet.
        //return hasConnected;
        return false;
    }

    public boolean mediaControlSetVolume(int time,String identification) {
        Boolean setSuccess = false;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlSetVolume", int.class, String.class);//PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18
                setSuccess = (Boolean) method.invoke(obj,time,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlSetVolume e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mediaControlSetVolume:"+setSuccess);
        return setSuccess;
    }

    public int mediaControlGetVolume(String identification) {
        int getVolume = -1;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlGetVolume", String.class);//PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18
                getVolume = (Integer) method.invoke(obj,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlGetVolume e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mediaControlGetVolume:"+getVolume);
        return getVolume;
    }

    public Object[] getDevicelist() {
        Object[] mDeviceList = null;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("getDevicelist");//PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18
                mDeviceList = (Object[]) method.invoke(obj);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: getDevicelist e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mDeviceList :"+mDeviceList);
        return mDeviceList;
    }


    public boolean mediaControlPlayCurr(final String path, final String fileType,String identification) {
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlPlayCurr", String.class, String.class, String.class);//PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18
                method.invoke(obj,path,fileType,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlPlayCurr e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mediaControlPlayCurr");
        return true;
    }


    public boolean mediaControlPlayNext(String path,String type,String identification) {
        Boolean returnValue = false;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlPlayNext", String.class, String.class, String.class);//PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18
                returnValue = (Boolean)method.invoke(obj,path,type,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlPlayNext e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mediaControlPlayNext returnValue = " + returnValue);
        return returnValue;
    }

    public boolean mediaControlPlay(String identification) {
        Boolean returnValue = false;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlPlay", String.class);
                returnValue = (Boolean)method.invoke(obj,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlPlay e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mediaControlPlay returnValue = " + returnValue);
        return returnValue;
    }


    public boolean mediaControlStop(String identification) {
        Boolean returnValue = false;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlStop", String.class);
                returnValue = (Boolean)method.invoke(obj,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlStop e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mediaControlStop returnValue = " + returnValue);
        return returnValue;
    }



    public boolean mediaControlPause(String identification) {
        Boolean returnValue = false;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlPause", String.class);
                returnValue = (Boolean)method.invoke(obj,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlPause e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mediaControlPause returnValue = " + returnValue);
        return returnValue;
    }

    public boolean mediaControlSeek(long position,String identification) {
        Boolean returnValue = false;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlSeek", long.class, String.class);//PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18
                returnValue = (Boolean)method.invoke(obj,position,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlSeek e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mediaControlSeek returnValue = " + returnValue);
        return returnValue;
    }

    public void setCurrentDevice(DeviceInfo device,String identification) {
        if (JrdDLNAManager != null && obj != null) {
            try {
                //PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18 start
                Class<?> deviceInfoClass = Class.forName("jrdcom.dlnaservice.DeviceInfo");
                Method method=JrdDLNAManager.getMethod("setCurrentDevice", deviceInfoClass, String.class);
                //PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18 end
                Object deviceInfoObj=null;
                if (device != null) {
                    deviceInfoObj = device.obj;
                }
                method.invoke(obj,deviceInfoObj,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: setCurrentDevice e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager setCurrentDevice");
    }


    public int mediaControlGetPlayState(String identification) {
        int returnValue = -1;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlGetPlayState",String.class);
                returnValue = (Integer)method.invoke(obj,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlGetPlayState e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mediaControlGetPlayState returnValue = " + returnValue);
        return returnValue;
    }


    public long mediaControlGetMediaDuration(String identification) {
        long returnValue = -1;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlGetMediaDuration",String.class);
                returnValue = (Long)method.invoke(obj,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlGetMediaDuration e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mediaControlGetMediaDuration returnValue = " + returnValue);
        return returnValue;
    }

    public long mediaControlGetCurPlayPosition(String identification) {
        long returnValue = -1;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("mediaControlGetCurPlayPosition",String.class);
                returnValue = (Long)method.invoke(obj,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: mediaControlGetCurPlayPosition e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager mediaControlGetCurPlayPosition returnValue = " + returnValue);
        return returnValue;
    }

    public String[] getCurrentDeviceSupportMediaType(String identification) {
        String[] returnValue = null;
        if (JrdDLNAManager != null && obj != null) {
            try {
                Method method=JrdDLNAManager.getMethod("getCurrentDeviceSupportMediaType",String.class);
                returnValue = (String[])method.invoke(obj,identification);
            } catch (Exception e) {
                Log.v(TAG, "DLNAManager: getCurrentDeviceSupportMediaType e = "+ e);
            }
        }
        Log.d(TAG, "DLNAManager getCurrentDeviceSupportMediaType returnValue = " + returnValue);
        return returnValue;
    }

}

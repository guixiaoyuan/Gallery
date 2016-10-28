package com.android.gallery3d.popupvideo;

import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.IWindowManager;

// [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014,FR828601 Pop-up Video play
/**
 * This class is created to monitor the rotation changing of window.
 * Unfortunately, I only find the API to add watcher, but no API can remove it.
 * So I design this class as signal instance mode. If you get a way to remove
 * the watcher, please let me known. Thanks!
 */
public class TctPopupWindowRotationWatcher {
    public static final String TAG = "TctPopupWindowRotationWatcher";
    private static TctPopupWindowRotationWatcher mInstance = null;
    private IWindowManager mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager
            .getService("window"));
    private Handler mHandler = new Handler();
    private Runnable mOnRotationChangedListener = null;

    /**
     * Constructor method
     */
    private TctPopupWindowRotationWatcher() {
        try {
            mIWindowManager.watchRotation(new IRotationWatcher.Stub() {
                @Override
                public void onRotationChanged(int rotation) throws RemoteException {
                    if (mOnRotationChangedListener != null) {
                        mHandler.post(mOnRotationChangedListener);
                    }
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Watch rotation error.");
        }
    }

    /**
     * Set a listener to receive the rotation changed event
     * @param listener listener
     */
    public void setOnRotationChangedListener(Runnable listener) {
        mOnRotationChangedListener = listener;
    }

    /**
     * Get an instance of this class
     * @return instance
     */
    public static TctPopupWindowRotationWatcher getInstance() {
        if (mInstance == null) {
            mInstance = new TctPopupWindowRotationWatcher();
        }

        return mInstance;
    }
}
// [FEATURE]-Add-END by jian.pan1

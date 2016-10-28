package com.tct.gallery3d.polaroid.handler;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tct.gallery3d.polaroid.Poladroid;
import com.tct.gallery3d.polaroid.PolaroidActivity;
import com.tct.gallery3d.polaroid.config.FilterConfig;

public class ForegroundHandler extends Handler {
    private final WeakReference<PolaroidActivity> mActivityRef;

    public ForegroundHandler(PolaroidActivity activity) {
        mActivityRef = new WeakReference<PolaroidActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        PolaroidActivity activity = mActivityRef.get();
        if (activity == null) {
            Log.i(Poladroid.TAG,
                    "ForegroundHandler.handleMessage(): activity has already been destroyed, ignoring incoming message");
            return;
        }

        switch (msg.what) {
        case Poladroid.BG2UI_FILTER_CMD_COMPLETE: {
            FilterConfig filterConfig = (FilterConfig) msg.obj;
            Log.d(Poladroid.TAG, "ForegroundHandler.handleMessage(BG2UI_FILTER_CMD_COMPLETE, "
                    + filterConfig + "){");

            if (filterConfig.mFilterCompletionHandler == null) {
                Log.d(Poladroid.TAG, "*** filterConfig.mFilterCompletionHandler is null...");
            } else {
                try {
                    filterConfig.mFilterCompletionHandler.onFilterComplete(filterConfig);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Log.d(Poladroid.TAG, "} ForegroundHandler.handleMessage(BG2UI_FILTER_CMD_COMPLETE)");
        }
            break;

        default: {
            Log.e(Poladroid.TAG, "*** ForegroundHandler.handleMessage(#" + msg.what
                    + "): unexpected message id");
            break;
        }
        }
    }
}

/* ----------|----------------------|----------------------|------------------------------------------------------------------*/
/* 12/10/2015|dongliang.feng        |ALM-1096821           |[Android 6.0][Gallery_v5.2.5.1.0319.0][Monitor]It pop up FC information when enter the guest mode */
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/

package com.tct.gallery3d.picturegrouping;

import com.tct.gallery3d.ui.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedBroadcastReceiver";

    public BootCompletedBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BootCompletedBroadcastReceiver.onReceive");
        Intent prefetchService = new Intent(context, PrefetchService.class);
        context.startService(prefetchService);
    }
}

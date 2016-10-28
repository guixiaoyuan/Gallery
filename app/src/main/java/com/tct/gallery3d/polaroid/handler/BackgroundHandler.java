/* 07/14/2015| jian.pan1            | PR1043560            |[Android 5.1][Gallery_v5.1.13.1.0212.0]It exit gallery when tap back key in edit interface
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.polaroid.handler;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.RenderScript.Priority;
import android.support.v8.renderscript.Type;
import android.util.Log;

import com.tct.gallery3d.polaroid.Poladroid;
import com.tct.gallery3d.polaroid.config.FilterConfig;
import com.tct.gallery3d.polaroid.manager.Filter;

public class BackgroundHandler extends Handler {
    private Context mContext;
    private Handler mForegroundHandler;
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 begin
    private boolean isHandlerQuit = false;

    public void setHandlerQuit(boolean isHandlerQuit) {
        this.isHandlerQuit = isHandlerQuit;
    }
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 end

    private ArrayList<FilterConfig> mFilterConfigs = new ArrayList<FilterConfig>();

    public BackgroundHandler(Looper looper, Handler foregroundHandler, Context context) {
        super(looper);

        mContext = context;
        mForegroundHandler = foregroundHandler;
    }

    private void processFilterConfig(FilterConfig filterConfig) {
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 begin
        if (isHandlerQuit)
            return;
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 end
        Allocation inAllocation = null; // TODO
        Allocation outAllocation = null; // TODO
        RenderScript rs = null;
        long begin = System.currentTimeMillis();

        try {
            Point inResolution = new Point();
            if (filterConfig.mInCrop != null) {
                inResolution.x = filterConfig.mInCrop.width();
                inResolution.y = filterConfig.mInCrop.height();
            } else {
                inResolution.x = filterConfig.mInDrawable.getIntrinsicWidth();
                inResolution.y = filterConfig.mInDrawable.getIntrinsicHeight();
            }

            Point outResolution;
            if (filterConfig.mOutResolution != null) {
                outResolution = filterConfig.mOutResolution;
            } else {
                outResolution = inResolution;
            }

            Filter filter = filterConfig.mFilter;
            Log.d(Poladroid.TAG,
                    "BackgroundHandler.processFilterConfig(" + filter.getName() + ", in: "
                            + filterConfig.mInDrawable.getIntrinsicWidth() + ","
                            + filterConfig.mInDrawable.getIntrinsicHeight() + "){");
            rs = RenderScript.create(mContext);
            rs.setPriority(Priority.NORMAL);

            // RenderScript rs = mAllocationCache.getRenderScript();
            Type.Builder tb = new Type.Builder(rs, Element.RGBA_8888(rs));
            tb.setX(outResolution.x);
            tb.setY(outResolution.y);
            Type type = tb.create();
            filter.prepare(rs, mContext, filterConfig.mQuality, outResolution.x, outResolution.y,
                    type);
            inAllocation = Allocation.createTyped(rs, type); // mAllocationCache.getAllocation(type);
            Log.d(Poladroid.TAG, "Allocation: " + inAllocation.getType().getX() + "x"
                    + inAllocation.getType().getY());
            Bitmap inBitmap = filterConfig.mInDrawable.getBitmap();
            Log.d(Poladroid.TAG, "Bitmap: " + inBitmap.getWidth() + "x" + inBitmap.getHeight());
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 begin
            if (isHandlerQuit)
                return;
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 end
            inAllocation.copyFrom(filterConfig.mInDrawable.getBitmap());
            // bitmap.recycle();
            // Poladroid.saveAllocation(mInAllocation, "inAllocation");
            outAllocation = Allocation.createTyped(rs, type);

            filter.render(rs, inAllocation, outAllocation);
            // rs.finish();

            Bitmap bitmap = Bitmap.createBitmap(outResolution.x, outResolution.y,
                    Bitmap.Config.ARGB_8888);
            Log.d(Poladroid.TAG, "Copying to bitmap (RS running)");
            outAllocation.copyTo(bitmap);
            Log.d(Poladroid.TAG, "Copied to bitmap (RS done)");
            // inAllocation.destroy();
            // outAllocation.destroy();
            filterConfig.mOutDrawable = new BitmapDrawable(mContext.getResources(), bitmap);

            // It's hard to tell which filter stage took all this time...
            // Let's share between all...
            filter.destroy(rs, mContext);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inAllocation != null) {
                    inAllocation.destroy();
                    inAllocation = null;
                }
                if (outAllocation != null) {
                    outAllocation.destroy();
                    outAllocation = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (rs != null) {
                rs.destroy();
            }

            long end = System.currentTimeMillis();
            filterConfig.mRenderingDuration = end - begin;
        }
        Log.d(Poladroid.TAG, "} BackgroundHandler.processFilterConfig()");
    }

    private void processNextFilterConfig() {
        Log.d(Poladroid.TAG, "BackgroundHandler.processNextFilterConfig(){");
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 begin
        if (isHandlerQuit)
            return;
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 end
        if (mFilterConfigs.size() > 0) {
            try {
                FilterConfig filterConfig = mFilterConfigs.remove(0);
                processFilterConfig(filterConfig);

                Message msg = Message.obtain();
                msg.what = Poladroid.BG2UI_FILTER_CMD_COMPLETE;
                msg.obj = filterConfig;
                mForegroundHandler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mFilterConfigs.size() > 0) {
            triggerProcessing();
        }
        Log.d(Poladroid.TAG, "} BackgroundHandler.processNextFilterConfig()");
    }

    private Runnable mProcessNextFilterConfig = new Runnable() {
        @Override
        public void run() {
            try {
                processNextFilterConfig();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void triggerProcessing() {
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 begin
        if (isHandlerQuit)
            return;
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 end
        Log.d(Poladroid.TAG, "BackgroundHandler.triggerProcessing()");
        this.post(mProcessNextFilterConfig);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case Poladroid.UI2BG_ADD_FILTER_CMD:
            try {
                FilterConfig filterConfig = (FilterConfig) msg.obj;
                mFilterConfigs.add(filterConfig);
                Collections.sort(mFilterConfigs, FilterConfig.mComparator);
                Log.d(Poladroid.TAG, "BackgroundHandler.handleMessage(UI2BG_ADD_FILTER_CMD, "
                        + filterConfig + ") => " + mFilterConfigs.size() + " items");
                triggerProcessing();
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        case Poladroid.UI2BG_REMOVE_FILTER_CMD:
            try {
                FilterConfig filterConfig = (FilterConfig) msg.obj;
                for (int i = mFilterConfigs.size() - 1; i >= 0; i--) {
                    FilterConfig other = mFilterConfigs.get(i);
                    if ((filterConfig.mImagePrio == other.mImagePrio || filterConfig.mImagePrio == FilterConfig.ALL_PRIORITIES)
                            && (filterConfig.mSizePrio == other.mSizePrio || filterConfig.mSizePrio == FilterConfig.ALL_PRIORITIES)
                            && (filterConfig.mSequencePrio == other.mSequencePrio || filterConfig.mSequencePrio == FilterConfig.ALL_PRIORITIES)) {
                        mFilterConfigs.remove(i);
                    }
                }
                // Not useful according to current algo... but safe...
                Collections.sort(mFilterConfigs, FilterConfig.mComparator);
                Log.d(Poladroid.TAG, "BackgroundHandler.handleMessage(UI2BG_REMOVE_FILTER_CMD, "
                        + filterConfig + ") => " + mFilterConfigs.size() + " items");
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        default:
            Log.e(Poladroid.TAG, "*** BackgroundHandler.handleMessage(#" + msg.what
                    + "): unexpected message id");
            break;
        }
    }
}

/* EOF */

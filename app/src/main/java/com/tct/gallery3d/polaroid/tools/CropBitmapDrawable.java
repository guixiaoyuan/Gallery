package com.tct.gallery3d.polaroid.tools;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tct.gallery3d.polaroid.PolaroidActivity;
import com.tct.gallery3d.polaroid.Poladroid;
import com.tct.gallery3d.polaroid.config.FilterConfig;
import com.tct.gallery3d.polaroid.config.FilterConfig.FilterCompletionHandler;
import com.tct.gallery3d.polaroid.config.PolaroidConfig;
import com.tct.gallery3d.polaroid.manager.FilterManager;
import com.tct.gallery3d.polaroid.manager.FilterOperation.Quality;

public class CropBitmapDrawable extends AsyncTask<BitmapDrawable, Void, BitmapDrawable> {
    private Context mContext;
    private Rect mInCrop;
    private Point mOutResolution;
    private Handler mBackgroundHandler;
    private PolaroidConfig mPolaroidConfig;
    private int mImageSeq;
    private FilterCompletionHandler mFilterCompletionHandler;
    private Handler mHandler;

    public CropBitmapDrawable(Context context, Rect inCrop, Point outResolution,
            Handler backgroundHandler, PolaroidConfig polaroidConfig, int imageSeq,
            Handler handler, FilterCompletionHandler filterCompletionHandler) {
        this.mContext = context;
        this.mInCrop = inCrop;
        this.mOutResolution = outResolution;
        this.mBackgroundHandler = backgroundHandler;
        this.mPolaroidConfig = polaroidConfig;
        this.mImageSeq = imageSeq;
        this.mFilterCompletionHandler = filterCompletionHandler;
        this.mHandler = handler;

    }

    protected BitmapDrawable doInBackground(BitmapDrawable... inDrawable) {
        Log.d(Poladroid.TAG, "CropBitmapDrawable.doInBackground(){");
        try {
            return Utils.getScaledCroppedDrawable(mContext, inDrawable[0], mInCrop, mOutResolution);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.d(Poladroid.TAG, "} CropBitmapDrawable.doInBackground()");
        }
        return null;
    }

    protected void onPostExecute(BitmapDrawable inSaveDrawable) {
        Log.d(Poladroid.TAG, "CropBitmapDrawable.onPostExecute(){");
        try {
            Message msg;
            FilterConfig filterConfig;

            // Remove any ongoing request for preview params
            msg = Message.obtain();
            msg.what = Poladroid.UI2BG_REMOVE_FILTER_CMD;
            filterConfig = new FilterConfig(FilterConfig.ALL_PRIORITIES, Poladroid.FULL_SIZE_PRIORITY,
                    FilterConfig.ALL_PRIORITIES);
            msg.obj = filterConfig;
            mBackgroundHandler.sendMessage(msg);

            // Add request for new params
            msg = Message.obtain();
            msg.what = Poladroid.UI2BG_ADD_FILTER_CMD;
            filterConfig = new FilterConfig(inSaveDrawable, mInCrop, mOutResolution,
                    Quality.HIGHEST, FilterManager.getFilter(mPolaroidConfig.mFilterName),
                    mImageSeq, Poladroid.FULL_SIZE_PRIORITY, 0, mFilterCompletionHandler);
            msg.obj = filterConfig;
            mBackgroundHandler.sendMessage(msg);
        } catch (Exception e) {
            mHandler.sendMessage(mHandler.obtainMessage(PolaroidActivity.MSG_CROP_IMAGE_ERROR));
            e.printStackTrace();
        }
        Log.d(Poladroid.TAG, "} CropBitmapDrawable.onPostExecute()");
    }
}

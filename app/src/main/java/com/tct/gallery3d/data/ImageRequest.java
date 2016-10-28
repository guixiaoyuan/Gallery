package com.tct.gallery3d.data;

import android.graphics.Bitmap;

import com.tct.gallery3d.util.ThreadPool.CancelListener;
import com.tct.gallery3d.util.ThreadPool.Job;
import com.tct.gallery3d.util.ThreadPool.JobContext;

public abstract class ImageRequest implements Job<Bitmap>, JobContext {

    protected volatile boolean mIsCancelled = false;

    @Override
    public boolean isCancelled() {
        return mIsCancelled;
    }

    @Override
    public void setCancelListener(CancelListener listener) {
    }

    @Override
    public boolean setMode(int mode) {
        return false;
    }

    @Override
    public abstract Bitmap run(JobContext jc);

    public Bitmap requestBitmap() {
        return run(this);
    }

    public void cancelRequest() {
        mIsCancelled = true;
    }
}

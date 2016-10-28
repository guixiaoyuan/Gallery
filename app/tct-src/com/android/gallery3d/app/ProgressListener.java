package com.android.gallery3d.app;

public interface ProgressListener {

    public void onProgressChanged(long position, long duration);
    public void onSeekStart();
    public void onSeekStop();
}

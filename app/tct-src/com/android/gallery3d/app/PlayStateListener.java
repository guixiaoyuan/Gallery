package com.android.gallery3d.app;

public interface PlayStateListener {

    public void onPlayStateChanged(int state);
    public void onStateStart();
    public void onStateStop();
}

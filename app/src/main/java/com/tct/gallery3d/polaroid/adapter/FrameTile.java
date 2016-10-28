package com.tct.gallery3d.polaroid.adapter;

import android.util.Log;

import com.tct.gallery3d.polaroid.Poladroid;
import com.tct.gallery3d.polaroid.manager.Frame;

public class FrameTile {
    public Frame mFrame;

    public FrameTile(Frame frame) {
        mFrame = frame;

        Log.d(Poladroid.TAG, "new " + toString());
    }

    @Override
    public String toString() {
        if (mFrame == null) {
            return "FrameTile { <null-frame> }";
        }

        return "FrameTile { " + mFrame.mName + " }";
    }
}

package com.tct.gallery3d.polaroid.manager;

import android.graphics.Typeface;
import android.util.Log;

import com.tct.gallery3d.polaroid.Poladroid;

public class Font {
    private String mName;
    private Typeface mTypeface;

    public Font(String name, Typeface typeface) {
        mName = name;
        mTypeface = typeface;

        Log.d(Poladroid.TAG, "new " + this);
    }

    public String getName() {
        return mName;
    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    @Override
    public String toString() {
        return "Font { " + mName + ", " + mTypeface + "}";
    }
}

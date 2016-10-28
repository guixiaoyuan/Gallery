package com.tct.gallery3d.app.model;

import android.graphics.Bitmap;

import com.tct.gallery3d.app.FaceShowActivity.FaceShowImageLoader;
import com.tct.gallery3d.data.LocalMediaItem;
import com.tct.gallery3d.data.Path;

public class FaceShowItem {

    public int position;
    public LocalMediaItem item;
    public Bitmap bitmap;
    public FaceShowImageLoader loader;

    public FaceShowItem(int position, LocalMediaItem item) {
        this.position = position;
        this.item = item;
        this.bitmap = null;
        this.loader = null;
    }
}

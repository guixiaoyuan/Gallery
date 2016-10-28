package com.tct.gallery3d.app.model;

import android.graphics.Bitmap;

import com.tct.gallery3d.app.BurstShotActivity.BurstShotImageLoader;
import com.tct.gallery3d.data.LocalMediaItem;

public class BurstShotItem {

    public boolean isSelected;
    public int position;
    public LocalMediaItem item;
    public Bitmap bitmap;
    public BurstShotImageLoader loader;

    public BurstShotItem(boolean isSelected, int position, LocalMediaItem item) {
        this.isSelected = isSelected;
        this.position = position;
        this.item = item;
        this.bitmap = null;
        this.loader = null;
    }

}

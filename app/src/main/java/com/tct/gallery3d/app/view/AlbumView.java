package com.tct.gallery3d.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.tct.gallery3d.R;

public class AlbumView extends RecyclingImageView {

    public AlbumView(Context context) {
        this(context, null);
    }

    public AlbumView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path clipPath = new Path();
        int w = this.getWidth();
        int h = this.getHeight();
        float radius = getContext().getResources().getDimension(R.dimen.album_radius);
        clipPath.addRoundRect(new RectF(0, 0, w, h), radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}

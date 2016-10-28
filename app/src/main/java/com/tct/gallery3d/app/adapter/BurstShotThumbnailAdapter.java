package com.tct.gallery3d.app.adapter;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.BurstShotActivity;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.model.BurstShotItem;
import com.tct.gallery3d.ui.Log;// MODIFIED by hao.yin, 2016-03-21, BUG-1841612

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

public class BurstShotThumbnailAdapter extends BurstShotBaseAdapter {
    private static final String TAG = "BurstShotThumbnailAdapter";

    private int mInitPositon = 0;
    private Context mContext;

    public BurstShotThumbnailAdapter(Context context, BurstShotItem[] items, int initPosition) {
        super(context, items);
        mContext = context;
        setItemSize();
        mInitPositon = initPosition;
        Log.d(TAG, "=====construction=====mInitPosition="+initPosition);// MODIFIED by hao.yin, 2016-03-21, BUG-1841612
    }


    public void setItemSize() {
        WindowManager wm = ((BurstShotActivity)mContext).getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        Configuration configuration = mContext.getResources().getConfiguration();
        if (configuration.orientation == configuration.ORIENTATION_LANDSCAPE) {
            mItemWidth = (int) (width * 0.075f);
            mItemHeight = (int) (height * 0.15f);
        } else {
            mItemWidth = (int) (width * 0.14f);
            mItemHeight = (int) (height * 0.1f);
        }
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder = null;
        if (v == null) {
            holder = new ViewHolder();
            v = mInflater.inflate(R.layout.burstshot_thumbnail_item, null);
            holder.imagePicture = (ImageView) v.findViewById(R.id.thumbnail_item);
            holder.imageIcon = (ImageView) v
                    .findViewById(R.id.thumbnail_selected);
            holder.imagePicture.getLayoutParams().width = mItemWidth;
            holder.imagePicture.getLayoutParams().height = mItemHeight;

            if (mInitPositon == position) {
                holder.imageIcon.setVisibility(View.VISIBLE);
            }
            v.setTag(holder);

        } else {
            holder = (ViewHolder) v.getTag();
        }

       Log.d(TAG, "=====position="+position+";mItems[position].bitmap="+(mItems[position].bitmap==null)+";nowTime="+System.currentTimeMillis());// MODIFIED by hao.yin, 2016-03-21, BUG-1841612
        if (mItems[position].bitmap == null) {
            holder.imagePicture.setBackgroundColor(Color.GRAY);
        } else {
            holder.imagePicture.setImageBitmap(mItems[position].bitmap);
        }

        return v;
    }

    public void setPosition(int position) {
        this.mInitPositon = position;
    }

    public void destory() {
        //TODO
    }
}

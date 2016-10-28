package com.tct.gallery3d.app.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.BurstShotActivity;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.model.BurstShotItem;
import com.tct.gallery3d.ui.Log;// MODIFIED by hao.yin, 2016-03-21, BUG-1841612

public class BurstShotPhotoAdapter extends BurstShotBaseAdapter {

    private static final String TAG = "BurstShotPhotoAdapter";

    private Context mContext;

    public BurstShotPhotoAdapter(Context context, BurstShotItem[] items) {
        super(context, items);
        mContext = context;
        setItemSize();
    }

    public void setItemSize() {
        WindowManager wm = ((BurstShotActivity)mContext).getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        Configuration configuration = mContext.getResources().getConfiguration();
        if (configuration.orientation == configuration.ORIENTATION_LANDSCAPE) {
            mItemWidth = (int) (width * 0.20f);
            mItemHeight = (int) (height * 0.50f);
        } else {
            mItemWidth = (int) (width * 0.68f);
            mItemHeight = (int) (height * 0.68f);
        }
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder = null;
        if (v == null) {
            holder = new ViewHolder();
            v = mInflater.inflate(R.layout.burstshot_picture_item, null);
            holder.imagePicture = (ImageView) v.findViewById(R.id.picture_item);
            holder.imageIcon = (ImageView) v.findViewById(R.id.icon_item);
            LayoutParams layoutParams = new LayoutParams(mItemWidth, mItemHeight);
            holder.imagePicture.setLayoutParams(layoutParams);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Log.d(TAG, "=====position="+position+";mItems[position].bitmap="+(mItems[position].bitmap==null)+";isSelected="+mItems[position].isSelected+";nowTime="+System.currentTimeMillis());// MODIFIED by hao.yin, 2016-03-21, BUG-1841612
        if (mItems[position].bitmap == null) {
            holder.imagePicture.setBackgroundColor(Color.GRAY);
        } else {
            holder.imagePicture.setImageBitmap(mItems[position].bitmap);
        }

        if (mItems[position].isSelected) {
            holder.imageIcon.setImageResource(R.drawable.ic_select_selected);
        } else {
            holder.imageIcon.setImageResource(R.drawable.ic_select_unselect);
        }
        return v;
    }

    public void destory() {
        //TODO
    }
}

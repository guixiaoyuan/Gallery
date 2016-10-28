/*------------------------------------------------------------------------------------------------------------*/
/* 14/01/2015|    su.jiang     |  PR-1238478   |[Android 6.0][Gallery]The faceshow picture will change to big.*/
/*-----------|-----------------|---------------|--------------------------------------------------------------*/
package com.tct.gallery3d.app.adapter;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.model.FaceShowItem;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;


public class FaceShowAdapter extends BaseAdapter {

    private Context context = null;
    private FaceShowItem[] mItems = null;
    private LayoutInflater inflater = null;

    private int mItemWidth = 0;

    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-14,PR1238478 begin
    public FaceShowAdapter(Context context, FaceShowItem[] items,int itemWidth) {
        this.context = context;
        this.mItems = items;
        inflater = LayoutInflater.from(this.context);
        mItemWidth = itemWidth;
    }
    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-14,PR1238478 end

    @Override
    public int getCount() {
        return mItems.length;
    }

    @Override
    public Object getItem(int position) {
        return mItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.faceshow_item, null);
            holder.imagePicture = (ImageView) convertView
                    .findViewById(R.id.imageView);
            LayoutParams layoutParams = new LayoutParams(mItemWidth, mItemWidth);
            holder.imagePicture.setLayoutParams(layoutParams);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mItems[position].bitmap == null) {
            holder.imagePicture.setBackgroundColor(Color.GRAY);
        } else {
            holder.imagePicture.setImageBitmap(mItems[position].bitmap);
        }
        return convertView;
    }

    class ViewHolder {
        ImageView imagePicture;
    }
}
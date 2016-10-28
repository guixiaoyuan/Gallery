package com.tct.gallery3d.app.adapter;

import com.tct.gallery3d.app.model.BurstShotItem;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class BurstShotBaseAdapter extends BaseAdapter {
    private static final String TAG = "BurstShotBaseAdapter";

    protected LayoutInflater mInflater = null;
    protected BurstShotItem[] mItems = null;
    protected DisplayMetrics mActualMetrics = null;

    protected int mItemWidth = 0;
    protected int mItemHeight = 0;

    public BurstShotBaseAdapter(Context context, BurstShotItem[] items) {
        mInflater = LayoutInflater.from(context);
        this.mItems = items;
        mActualMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(mActualMetrics);
    }

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
        return null;
    }

    protected class ViewHolder {
        ImageView imagePicture = null;
        ImageView imageIcon = null;
    }

}

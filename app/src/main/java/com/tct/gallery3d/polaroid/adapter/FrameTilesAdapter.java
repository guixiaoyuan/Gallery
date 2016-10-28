package com.tct.gallery3d.polaroid.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;

import com.tct.gallery3d.R;
import com.tct.gallery3d.polaroid.Poladroid;
import com.tct.gallery3d.polaroid.view.PolaroidView;

public class FrameTilesAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<FrameTile> mTiles;
    private final LayoutInflater mInflater;
    private BitmapDrawable mPictureDrawable;

    public FrameTilesAdapter(Context context, ArrayList<FrameTile> tiles) {
        mTiles = tiles;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (mTiles == null) {
            return 0;
        }
        return mTiles.size();
    }

    @Override
    public FrameTile getItem(int position) {
        // Log.d(Poladroid.TAG, "FilterTilesAdapter.getItem(" + position + ")");
        return mTiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        Log.d(Poladroid.TAG, "FrameTilesAdapter.getView(" + position + "){");

        if (convertView == null) {
            view = mInflater.inflate(R.layout.polaroid_frame_tile, parent, false);
        } else {
            view = convertView;
        }

        FrameTile frameTile = getItem(position);

        PolaroidView polaroidView = (PolaroidView) view.findViewById(R.id.polaroid);
        polaroidView.setFrame(frameTile.mFrame);
        polaroidView.setTouchEnable(false);

        EditText sloganView = (EditText) view.findViewById(R.id.slogan);
        sloganView.setInputType(InputType.TYPE_NULL);

        if (mPictureDrawable == null) {
            Log.d(Poladroid.TAG, "FrameTilesDrawable.getView(" + position + "): mPictureDrawable is null");
        } else {
            polaroidView.setPicture(mPictureDrawable);
        }

        Log.d(Poladroid.TAG, "} FrameTilesAdapter.getView(" + position + ")");

        return view;
    }

    public void setPicture(BitmapDrawable pictureDrawable) {
        Log.d(Poladroid.TAG, "FrameTilesDrawable.setPicture(" + pictureDrawable + ")");
        mPictureDrawable = pictureDrawable;
    }
}

/* EOF */

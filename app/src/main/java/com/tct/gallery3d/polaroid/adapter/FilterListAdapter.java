package com.tct.gallery3d.polaroid.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.tct.gallery3d.R;

public class FilterListAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<FilterTile> mFilterTiles;
    private final LayoutInflater mInflater;

    FilterListAdapter(Context context, ArrayList<FilterTile> effectTiles) {
        mFilterTiles = effectTiles;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (mFilterTiles == null) {
            return 0;
        }
        return mFilterTiles.size();
    }

    @Override
    public FilterTile getItem(int position) {
        // Log.d(Poladroid.TAG, "FilterListAdapter.getItem(" + position + ")");
        return mFilterTiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        // Log.d(Poladroid.TAG, "FilterListAdapter.getView(" + position + "){");

        if (convertView == null) {
            view = mInflater.inflate(R.layout.polaroid_filter_tile, parent, false);
        } else {
            view = convertView;
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.image);

        FilterTile filterTile = getItem(position);
        if (filterTile != null) {
            filterTile.getThumb(imageView);
        }

        // Log.d(Poladroid.TAG, "} FilterListAdapter.getView(" + position + ")");

        return view;
    }
}

/* EOF */

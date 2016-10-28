/* ----------|----------------------|----------------------|----------------- */
/* 05/08/2015| jian.pan1            | PR997424             |App drawer active item background
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.model.TabItem;

public class TabAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;

    private TabItem[] tabs = null;

    private Context mContext;

    public TabAdapter(Context context, TabItem[] tabs) {
        this.tabs = tabs;
        this.mContext = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return tabs.length;
    }

    @Override
    public Object getItem(int position) {
        return tabs[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.drawer_list_item, null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        TabItem item = tabs[position];

        ImageView icon = (ImageView) convertView.findViewById(R.id.ic);
        TextView title = (TextView) convertView.findViewById(R.id.tx);

        icon.setImageResource(item.isSelected() ? item.getSelectedIconId()
                : item.getNormalIconId());
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-05-08,PR997424 begin
        convertView.setBackgroundResource(item.isSelected() ? R.drawable.shape_selector_selected
                : R.drawable.selector_list_item);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-05-08,PR997424 end
        title.setText(item.getTitleStringId());
        int titleColorId = item.isSelected() ? item.getSelectedTitleColorId()
                : item.getNormalTitleColorId();
        title.setTextColor(mContext.getResources().getColor(titleColorId));

        return convertView;
    }
}

package com.tct.gallery3d.app.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.data.AlbumSetManager;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.MediaSetUtils;

import java.util.List;

public class CollapseManageSetDataAdapter extends BaseAdapter {

    private List<MediaSet> mList;
    private Context mContext;

    public CollapseManageSetDataAdapter(Context context) {
        mContext = context;
        mList = AlbumSetManager.getInstance().getAllALbum();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.item_collapse_manage_albums, null);
        final MediaSet album = mList.get(i);
        CollapseAlbumItemViewHolder holder = null;
        if (null == holder) {
            holder = new CollapseAlbumItemViewHolder();
            holder.check = (Switch) view.findViewById(R.id.collapse_switch);
            holder.collapseName = (TextView) view.findViewById(R.id.collapse_album_name);
            view.setTag(holder);
        } else {
            holder = (CollapseAlbumItemViewHolder) view.getTag();
        }
        holder.collapseName.setText(album.getName());
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(GalleryConstant.COLLAPSE_DATA_NAME, mContext.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        int bucketId = GalleryUtils.getBucketId(album.getAlbumFilePath());
        if (bucketId == MediaSetUtils.CAMERA_BUCKET_ID || bucketId == MediaSetUtils.DOWNLOAD_BUCKET_ID
                || bucketId == MediaSetUtils.IMPORTED_BUCKET_ID || bucketId == MediaSetUtils.SNAPSHOT_BUCKET_ID
                || bucketId == MediaSetUtils.EDITED_ONLINE_PHOTOS_BUCKET_ID || bucketId == MediaSetUtils.SDCARD_CAMERA_BUCKET_ID
                || bucketId == MediaSetUtils.PICTURES_BUCKET_ID || bucketId == GalleryUtils.getBucketId(Environment.getExternalStorageDirectory().toString())) {
            holder.check.setChecked(false);
            holder.check.setEnabled(false);
        }
        holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putString(album.getAlbumFilePath(), album.getName());
                } else {
                    editor.putString(album.getAlbumFilePath(), null);
                }
                editor.commit();
            }
        });

        if (sharedPreferences.getString(album.getAlbumFilePath(), null) != null) {
            holder.check.setChecked(true);
        } else {
            holder.check.setChecked(false);
        }

        return view;
    }

    public class CollapseAlbumItemViewHolder {
        Switch check;
        TextView collapseName;
    }
}

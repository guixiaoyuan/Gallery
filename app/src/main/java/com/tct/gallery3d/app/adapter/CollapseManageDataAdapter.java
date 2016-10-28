package com.tct.gallery3d.app.adapter;

import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.LoadingListener;
import com.tct.gallery3d.app.data.AlbumSetDataLoader;
import com.tct.gallery3d.app.fragment.CollapseManageFragment;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.data.MediaSource;
import com.tct.gallery3d.ui.Log;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.MediaSetUtils;

import java.util.ArrayList;
import java.util.List;

import com.tct.gallery3d.app.constant.GalleryConstant;


public class CollapseManageDataAdapter extends RecyclerView.Adapter<CollapseManageDataAdapter.CollapseViewHolder>
        implements AlbumSetDataLoader.DataListener, LoadingListener {


    private static final String TAG = "CollapseManageDataAdapter";

    private AbstractGalleryActivity mContext;
    private CollapseManageFragment mFragment;

    private AlbumSetDataLoader mLoader;
    private DataManager mDataManager;

    private static final int CACHE_SIZE = 64;
    private static final int SPANCOUNT = 8;

    private boolean mGetContent;
    private boolean mGetMultiContent;
    private int mTotal;
    final List<Boolean> mList = new ArrayList<>();
    final Boolean[] mBoolean = new Boolean[11];

    private LinearLayoutManager mLayoutManager;


    public CollapseManageDataAdapter(CollapseManageFragment fragment, LinearLayoutManager layoutManager) {
        Log.d(TAG, "CollapseDataAdapter");
        mFragment = fragment;
        mLayoutManager = layoutManager;
        mContext = (AbstractGalleryActivity) fragment.getGalleryContext();

        initManager();
        setHasStableIds(true);
    }

    private void initManager() {
        mDataManager = mContext.getDataManager();
        MediaSet mediaSet = mDataManager.getMediaSet(MediaSource.LOCAL_SELECT_PATH);
        mLoader = new AlbumSetDataLoader(mContext, mediaSet, CACHE_SIZE);
        mLoader.setModelListener(this);
        mLoader.setLoadingListener(this);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        for (int i = 0; i < mTotal; i++) {
            mList.add(i,false);
        }
        return mTotal;
    }

    @Override
    public CollapseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_collapse_manage_albums, parent, false);
        CollapseManageDataAdapter.CollapseViewHolder holder = new CollapseManageDataAdapter.CollapseViewHolder(view);
        holder.mSwitch.setChecked(true);
        holder.mSwitch.setVisibility(View.VISIBLE);
        for (int i = 0; i < mTotal; i++) {
            holder.mSwitch.setChecked(mBoolean[i]);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(CollapseViewHolder holder, int position) {
        int size = mLoader.size();
        holder.setIsRecyclable(false);
        if (size > 0) {
            setActiveWindow(size, position);
            setLayoutInfo(holder, position);
        }
    }

    class CollapseViewHolder extends RecyclerView.ViewHolder {
        private TextView mCollpaseAlbumName;
        private Switch mSwitch;

        public CollapseViewHolder(View view) {
            super(view);
            mCollpaseAlbumName = (TextView) view.findViewById(R.id.collapse_album_name);
            mSwitch = (Switch) view.findViewById(R.id.collapse_switch);
        }
    }

    private void setActiveWindow(int size, int position) {
        int first = mLayoutManager.findFirstVisibleItemPosition();
        int last = mLayoutManager.findLastVisibleItemPosition();
        //int columns = mLayoutManager.getSpanCount();
        int columns = SPANCOUNT;

        int min = position - columns * 2;
        int max = position + columns * 2;
        if (mLayoutManager.findFirstVisibleItemPosition() >= 0) {
            min = Math.min(min, first - columns * 2);
            max = Math.max(max, last + columns * 4);
        }
        int start = Math.max(0, min);
        int end = Math.min(max, size);
        mLoader.setActiveWindow(start, end);
    }

    private void setLayoutInfo(CollapseViewHolder collapseViewHolder, int slotIndex) {
        synchronized (mLoader) {

            final CollapseViewHolder holder = collapseViewHolder;
            final int position = slotIndex;

            final MediaSet album = mLoader.getMediaSet(position);
            if (album != null) {
                holder.mCollpaseAlbumName.setText(album.getName());
                holder.mSwitch.setVisibility(View.VISIBLE);
                SharedPreferences sharedPreferences = mContext.getSharedPreferences(GalleryConstant.COLLAPSE_DATA_NAME, mContext.MODE_PRIVATE);
                holder.mSwitch.setTag(new Integer(position));
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                int bucketId = GalleryUtils.getBucketId(album.getAlbumFilePath());
                if (bucketId == MediaSetUtils.CAMERA_BUCKET_ID || bucketId == MediaSetUtils.DOWNLOAD_BUCKET_ID
                        || bucketId == MediaSetUtils.IMPORTED_BUCKET_ID || bucketId == MediaSetUtils.SNAPSHOT_BUCKET_ID
                        || bucketId == MediaSetUtils.EDITED_ONLINE_PHOTOS_BUCKET_ID || bucketId == MediaSetUtils.SDCARD_CAMERA_BUCKET_ID
                        || bucketId == MediaSetUtils.PICTURES_BUCKET_ID || bucketId == GalleryUtils.getBucketId(Environment.getExternalStorageDirectory().toString())) {
                    holder.mSwitch.setChecked(false);
                    holder.mSwitch.setEnabled(false);
                }

                holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                    //holder.mSwitch.setChecked(true);
                    mBoolean[position] = true;
                } else {
                    //holder.mSwitch.setChecked(false);
                    mBoolean[position] = false;
                }

            }

        }
    }


    public void resume() {
        mLoader.resume();

    }

    public void pause() {
        Log.d(TAG, "pause");
        mLoader.pause();
    }

    public void destroy() {
        mLoader.setLoadingListener(null);
        mLoader.setModelListener(null);
        mLoader = null;
    }

    @Override
    public void onContentChanged(int index) {
        notifyItemChanged(index);
    }

    @Override
    public void onSizeChanged(int index, boolean showCollapse) {
        mTotal = index;
        notifyDataSetChanged();
    }

    @Override
    public void onLoadingStarted() {
    }

    @Override
    public void onLoadingFinished(boolean loadingFailed) {
    }

    public int getFirstPosition() {
        return mLayoutManager.findFirstCompletelyVisibleItemPosition();
    }

    public int getLastPosition() {
        return mLayoutManager.findLastCompletelyVisibleItemPosition();
    }

}

package com.tct.gallery3d.app.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AbstractGalleryFragment;
import com.tct.gallery3d.app.AlbumActivity;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.LoadingListener;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.data.AlbumSetDataLoader;
import com.tct.gallery3d.app.fragment.GalleryFragment;
import com.tct.gallery3d.app.view.AlbumItem;
import com.tct.gallery3d.data.MediaSource;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.ui.Log;

public class CollapseAlbumSetDataAdapter extends RecyclerView.Adapter<CollapseAlbumSetDataAdapter.AlbumViewHolder>
        implements OnClickListener,
        AlbumSetDataLoader.DataListener, LoadingListener {

    private static final String TAG = "CollapseAlbumSetDataAdapter";

    private AbstractGalleryActivity mContext;
    private GalleryFragment mFragment;

    private AlbumSetDataLoader mLoader;
    private DataManager mDataManager;

    private static final int CACHE_SIZE = 64;

    private boolean mGetContent;
    private boolean mGetMultiContent;
    private int mTotal;

    private GridLayoutManager mLayoutManager;

    public static final String COLLAPSEALBUMCOUNT = "collapseAlbumCount";

    public CollapseAlbumSetDataAdapter(GalleryFragment fragment, GridLayoutManager layoutManager) {
        mFragment = fragment;
        mLayoutManager = layoutManager;
        mContext = (AbstractGalleryActivity) fragment.getGalleryContext();
        Bundle data = fragment.getArguments();
        mGetContent = data.getBoolean(GalleryActivity.KEY_GET_CONTENT, false);
        mGetMultiContent = data.getBoolean(Intent.EXTRA_ALLOW_MULTIPLE, false);
        initManager();
        setHasStableIds(true);
    }

    private void initManager() {
        mDataManager = mContext.getDataManager();
        MediaSet mediaSet = mDataManager.getMediaSet(MediaSource.LOCAL_COLLAPSED_PATH);
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
        return mTotal;
    }

    @Override
    public CollapseAlbumSetDataAdapter.AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        AlbumViewHolder holder = new AlbumViewHolder(view);
        holder.mContent.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(CollapseAlbumSetDataAdapter.AlbumViewHolder holder, int position) {
        int size = mLoader.size();
        if (size > 0) {
            setActiveWindow(size, position);
            AlbumItem contentItem = holder.mContent;
            setLayoutInfo(contentItem, position);
        }
    }


    class AlbumViewHolder extends RecyclerView.ViewHolder {
        private AlbumItem mContent;

        public AlbumViewHolder(View view) {
            super(view);
            mContent = (AlbumItem) view.findViewById(R.id.item);
        }
    }

    private void setActiveWindow(int size, int position) {
        int first = mLayoutManager.findFirstVisibleItemPosition();
        int last = mLayoutManager.findLastVisibleItemPosition();
        int columns = mLayoutManager.getSpanCount();

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


    private void setLayoutInfo(AlbumItem albumItem, int slotIndex) {
        synchronized (mLoader) {
            MediaSet album = mLoader.getMediaSet(slotIndex);
            if (album != null) {
                albumItem.initAlbumSet(album);
                int size = mLoader.getTotalCount(slotIndex);
                albumItem.setAlbumItemCount(size);
                MediaItem item = mLoader.getCoverItem(slotIndex);
                if (item != null) {
                    albumItem.setSlotIndex(slotIndex);
                    mFragment.loadThumbnail(item, albumItem.getCover());
                }
            }
        }
    }

    public void resume() {
        Log.d(TAG, "resume");
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
    public void onClick(View v) {
        if (!AbstractGalleryFragment.checkClickable(mContext)) {
            return;
        }
        if (v instanceof AlbumItem) {
            AlbumItem albumItem = (AlbumItem) v;
            int slotIndex = albumItem.getSlotIndex();
            enterAlbumList(slotIndex);
        }
    }

    private void enterAlbumList(int index) {
        MediaSet mediaSet = mLoader.getMediaSet(index);
        if (mediaSet == null) {
            return;
        }
        String path = mediaSet.getPath().toString();
        String title = mediaSet.getName();
        int count = mLoader.getTotalCount(index);

        Bundle data = new Bundle();
        data.putString(GalleryConstant.KEY_MEDIA_PATH, path);
        data.putString(GalleryConstant.KEY_MEDIA_NAME, title);
        data.putInt(GalleryConstant.KEY_MEDIA_ITEM_COUNT, count);
        Intent intent = new Intent(mContext, AlbumActivity.class);
        if (mGetContent) {
            data.putBoolean(GalleryActivity.KEY_GET_CONTENT, mGetContent);
            data.putBoolean(Intent.EXTRA_ALLOW_MULTIPLE, mGetMultiContent);
        }
        intent.putExtras(data);
        mContext.startActivityForResult(intent, 0);
        mContext.overridePendingTransition(R.anim.slide_in_right_short, R.anim.slide_out_right_short);
    }

    @Override
    public void onContentChanged(int index) {
        notifyItemChanged(index);
    }

    @Override
    public void onSizeChanged(int index, boolean showCollapse) {
        mTotal = index;
        notifyDataSetChanged();
        if (mTotal == 0) {
            Intent intent = new Intent();
            intent.putExtra(COLLAPSEALBUMCOUNT, mTotal);
            mContext.setResult(GalleryConstant.REQUEST_COLLAPSE_CODE, intent);
            mContext.finish();
        }
    }

    @Override
    public void onLoadingStarted() {
        mFragment.showEmptyView(false);
    }

    @Override
    public void onLoadingFinished(boolean loadingFailed) {
        mFragment.showEmptyView(mTotal == 0);
    }

    public int getFirstPosition() {
        return mLayoutManager.findFirstCompletelyVisibleItemPosition();
    }

    public int getLastPosition() {
        return mLayoutManager.findLastCompletelyVisibleItemPosition();
    }
}

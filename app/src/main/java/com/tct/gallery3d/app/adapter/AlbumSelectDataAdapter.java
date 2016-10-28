package com.tct.gallery3d.app.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AbstractGalleryFragment;
import com.tct.gallery3d.app.NewAlbumDialogActivity;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.LoadingListener;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.data.AlbumSetDataLoader;
import com.tct.gallery3d.app.fragment.GalleryFragment;
import com.tct.gallery3d.app.view.AlbumItem;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.ui.Log;

public class AlbumSelectDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener,
        AlbumSetDataLoader.DataListener, LoadingListener {

    private static final String TAG = "AlbumSelectDataAdapter";

    private AbstractGalleryActivity mContext;
    private GalleryFragment mFragment;

    private AlbumSetDataLoader mLoader;
    private DataManager mDataManager;

    private static final int CACHE_SIZE = 64;

    private int mTotal;

    private static final int ITEM_VIEW_TYPE_SECTION = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private GridLayoutManager mLayoutManager;

    public AlbumSelectDataAdapter(GalleryFragment fragment, GridLayoutManager layoutManager) {
        mFragment = fragment;
        mLayoutManager = layoutManager;
        mContext = (AbstractGalleryActivity) fragment.getGalleryContext();

        Bundle data = fragment.getArguments();
        String path = data.getString(GalleryConstant.KEY_MEDIA_PATH);

        initManager(path);
        setHasStableIds(true);
    }

    private void initManager(String path) {
        mDataManager = mContext.getDataManager();
        MediaSet mediaSet = mDataManager.getMediaSet(path);
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

    /*@Override
    public AlbumSelectDataAdapter.AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        AlbumViewHolder holder = new AlbumViewHolder(view);
        holder.mContent.setOnClickListener(this);
        return holder;
    }*/


    public int getItemViewType(int position) {
        return position == 0 ? ITEM_VIEW_TYPE_SECTION : ITEM_VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerView.ViewHolder holder;

        //        View view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        //        holder = new AlbumViewHolder(view);
        //        holder.mContent.setOnClickListener(this);

        switch (viewType) {
            case ITEM_VIEW_TYPE_SECTION:
                View sectionview = LayoutInflater.from(mContext).inflate(R.layout.new_album_header, parent, false);
                holder = new SectionViewHolder(sectionview);
                sectionview.setOnClickListener(this);
                break;
            case ITEM_VIEW_TYPE_ITEM:
                View view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
                holder = new AlbumViewHolder(view);
                ((AlbumViewHolder) holder).mContent.setOnClickListener(this);
                break;
            default:
                holder = null;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int size = mLoader.size();

//        if (size > 0) {
//            setActiveWindow(size, position);
//            AlbumItem contentItem = holder.mContent;
//            setLayoutInfo(contentItem, position);
//        }

        int type = getItemViewType(position);
        switch (type) {
            case ITEM_VIEW_TYPE_SECTION:
                SectionViewHolder headerHolder = (SectionViewHolder) holder;
                headerHolder.mImageView.setImageResource(R.drawable.ic_add);
                break;
            case ITEM_VIEW_TYPE_ITEM:
                if (size > 0) {
                    setActiveWindow(size, position - 1);
                    AlbumItem contentItem = ((AlbumViewHolder) holder).mContent;
                    setLayoutInfo(contentItem, position - 1);
                }
                break;
        }
    }

    class SectionViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;
        TextView mTextView;

        public SectionViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.new_album_header_img);
            mTextView = (TextView) view.findViewById(R.id.new_album_header_title);
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
        int first = mLayoutManager.findFirstVisibleItemPosition() - 1;
        int last = mLayoutManager.findLastVisibleItemPosition() - 1;
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
        if (v.getId() == R.id.new_album_header) {

            Intent intent = new Intent(mContext, NewAlbumDialogActivity.class);
            intent.putExtra(NewAlbumDialogActivity.KEY_CREATE_NEW_ALBUM,true);
            intent.putExtra(NewAlbumDialogActivity.KEY_NEED_SELECT_FRAGMENT,false);
            mContext.startActivityForResult(intent,GalleryConstant.REQUEST_NEW_ALBUM);
        }
        if (v instanceof AlbumItem) {
            AlbumItem albumItem = (AlbumItem) v;
            int slotIndex = albumItem.getSlotIndex();
            selectAlbum(slotIndex);
        }
    }

    private void selectAlbum(int index) {
        if (!AbstractGalleryFragment.checkClickable(mContext)) {
            return;
        }
        MediaSet mediaSet = mLoader.getMediaSet(index);
        if (mediaSet != null) {
            String albumFilePath = mediaSet.getAlbumFilePath();
            if (albumFilePath == null) {
                return;
            }
            Intent intent = new Intent();
            intent.putExtra(GalleryConstant.KEY_PATH_RETURN, mediaSet.getAlbumFilePath() + "/");
            mFragment.getActivity().setResult(GalleryActivity.RESULT_OK, intent);
            mFragment.getActivity().finish();
        }
    }

    @Override
    public void onContentChanged(int index) {
        notifyItemChanged(index + 1);
    }

    @Override
    public void onSizeChanged(int index, boolean showCollapse) {
        mTotal = index + 1;
        notifyDataSetChanged();
    }

    @Override
    public void onLoadingStarted() {
        mFragment.showEmptyView(false);
    }

    @Override
    public void onLoadingFinished(boolean loadingFailed) {
        mFragment.showEmptyView(mTotal == 0);
    }
}

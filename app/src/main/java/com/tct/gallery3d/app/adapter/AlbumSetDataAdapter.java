package com.tct.gallery3d.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.tct.gallery3d.app.CollapseAlbumsActivity;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.LoadingListener;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.data.AlbumSetDataLoader;
import com.tct.gallery3d.app.fragment.GalleryFragment;
import com.tct.gallery3d.app.view.AlbumItem;
import com.tct.gallery3d.app.view.CollapseAlbumItem;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.LocalAlbum;
import com.tct.gallery3d.data.LocalMergeAlbum;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaObject;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.data.MediaSource;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.ui.Log;
import com.tct.gallery3d.util.GalleryUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import com.tct.gallery3d.app.constant.GalleryConstant;


public class AlbumSetDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements OnClickListener,
        AlbumSetDataLoader.DataListener, LoadingListener {

    private static final String TAG = "AlbumSetDataAdapter";

    private AbstractGalleryActivity mContext;
    private GalleryFragment mFragment;

    private AlbumSetDataLoader mLoader;
    private DataManager mDataManager;


    private static final int CACHE_SIZE = 64;
    public static final int VIEW_TYPE_COLLAPSE_ALBUM = 0;
    public static final int VIEW_TYPE_ITEM = 1;

    public static final int MAX_COLLAPSE_COVER_AMOUNT = 4;

    private boolean mGetContent;
    private boolean mGetMultiContent;
    private int mTotal;
    private boolean mShowCollapse = false;

    private GridLayoutManager mLayoutManager;
    private CollapseAlbumItem mCollapseAlbumItem;

    public AlbumSetDataAdapter(GalleryFragment fragment, GridLayoutManager layoutManager) {
        Log.d(TAG, "AlbumSetDataAdapter");
        mFragment = fragment;
        mLayoutManager = layoutManager;
        mContext = (AbstractGalleryActivity) fragment.getGalleryContext();

        Bundle data = fragment.getArguments();
        String path = data.getString(GalleryConstant.KEY_MEDIA_PATH);
        mGetContent = data.getBoolean(GalleryActivity.KEY_GET_CONTENT, false);
        mGetMultiContent = data.getBoolean(Intent.EXTRA_ALLOW_MULTIPLE, false);

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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        RecyclerView.ViewHolder holder;

        switch (viewType) {
            case VIEW_TYPE_ITEM:
                view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
                holder = new AlbumViewHolder(view);
                view.setOnClickListener(this);
                break;
            case VIEW_TYPE_COLLAPSE_ALBUM:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_collapse_album, parent, false);
                holder = new CollapseViewHolder(view);
                view.setOnClickListener(this);
                break;
            default:
                holder = null;
        }
        return holder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);
        int size = 0;
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                size = mLoader.size();
                AlbumViewHolder albumViewHolder = (AlbumViewHolder) holder;
                if (size > 0) {
                    setActiveWindow(size, position);
                    AlbumItem contentItem = albumViewHolder.mContent;
                    setLayoutInfo(contentItem, position);
                }
                break;
            case VIEW_TYPE_COLLAPSE_ALBUM:
                CollapseViewHolder collapseViewHolder = (CollapseViewHolder) holder;
                mCollapseAlbumItem = collapseViewHolder.mContent;
                SharedPreferences sharedPreferences = mContext.getAndroidContext().getSharedPreferences(GalleryConstant.COLLAPSE_DATA_NAME, Context.MODE_PRIVATE);
                Map<String, ?> map = sharedPreferences.getAll();
                Iterator iter = map.entrySet().iterator();
                ArrayList<String> collapsePathList = new ArrayList();
                String key;
                String albumName;
                ArrayList<MediaSet> mediaSets = new ArrayList<>();
                MediaSet mediaset;
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    key = (String) entry.getKey();
                    albumName = (String) entry.getValue();
                    if (key != null) {
                        mediaset = getLocalAlbum(mContext.getDataManager(),
                                MediaObject.MEDIA_TYPE_ALL, Path.fromString(MediaSource.LOCAL_SET_PATH), GalleryUtils.getBucketId(key), albumName);
                        if (mediaset.getMediaItemCount() != 0) {
                            mediaSets.add(mediaset);
                            collapsePathList.add(key);
                        }
                    }
                }
                mCollapseAlbumItem.setAlbumItemCount(collapsePathList.size());
                mCollapseAlbumItem.initAlbumSet();
                int coverCount;
                if (collapsePathList.size() >= MAX_COLLAPSE_COVER_AMOUNT) {
                    coverCount = MAX_COLLAPSE_COVER_AMOUNT;
                } else {
                    coverCount = collapsePathList.size();
                }

                for (int coverPosition = 0; coverPosition < coverCount; coverPosition++) {
                    mediaset = mediaSets.get(coverPosition);
                    if (mediaset != null) {
                        mFragment.loadThumbnail(mediaset.getCoverMediaItem().get(0), mCollapseAlbumItem.getCover(coverPosition));
                    }
                }
                if (coverCount < MAX_COLLAPSE_COVER_AMOUNT) {
                    for (int resetCoverPosition = 0; resetCoverPosition < MAX_COLLAPSE_COVER_AMOUNT - coverCount; resetCoverPosition++) {
                        mCollapseAlbumItem.resetCover(MAX_COLLAPSE_COVER_AMOUNT - resetCoverPosition - 1);
                    }
                }
                break;
        }
    }

    public void setTotal(boolean isTotal, int collapseAlbumCount) {
        if (isTotal && collapseAlbumCount == 0) {
            mTotal = mTotal - 1;
            mShowCollapse = false;
        }
        notifyDataSetChanged();
    }

    private MediaSet getLocalAlbum(DataManager manager, int type, Path parent, int id, String name) {
        synchronized (DataManager.LOCK) {
            Path path = parent.getChild(id);
            MediaObject object = manager.peekMediaObject(path);
            if (object != null) {
                MediaSet mediaSet = (MediaSet) object;
                mediaSet.reload();
                return mediaSet;
            }
            switch (type) {
                case MediaObject.MEDIA_TYPE_IMAGE:
                    return new LocalAlbum(path, ((GalleryApp) mContext.getApplication()), id, true, name);
                case MediaObject.MEDIA_TYPE_VIDEO:
                    return new LocalAlbum(path, ((GalleryApp) mContext.getApplication()), id, false, name);
                case MediaObject.MEDIA_TYPE_ALL:
                    Comparator<MediaItem> comp = DataManager.sDateTakenComparator;
                    return new LocalMergeAlbum(path, comp, new MediaSet[]{
                            getLocalAlbum(manager, MediaObject.MEDIA_TYPE_IMAGE, Path.fromString(MediaSource.LOCAL_IMAGE_SET_PATH), id, name),
                            getLocalAlbum(manager, MediaObject.MEDIA_TYPE_VIDEO, Path.fromString(MediaSource.LOCAL_VIDEO_SET_PATH), id, name)}, id);
            }
            throw new IllegalArgumentException(String.valueOf(type));
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (mShowCollapse) {
            if (position == mTotal - 1) {
                return VIEW_TYPE_COLLAPSE_ALBUM;
            }
            return VIEW_TYPE_ITEM;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        public AlbumItem mContent;

        public AlbumViewHolder(View view) {
            super(view);
            mContent = (AlbumItem) view.findViewById(R.id.item);
        }
    }

    class CollapseViewHolder extends RecyclerView.ViewHolder {
        public CollapseAlbumItem mContent;

        public CollapseViewHolder(View view) {
            super(view);
            mContent = (CollapseAlbumItem) view.findViewById(R.id.item);
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
        if (v instanceof CollapseAlbumItem) {
            Bundle data = new Bundle();
            data.putBoolean(GalleryActivity.KEY_GET_CONTENT, mGetContent);
            data.putBoolean(Intent.EXTRA_ALLOW_MULTIPLE, mGetMultiContent);
            Intent intent = new Intent(mContext, CollapseAlbumsActivity.class);
            intent.putExtras(data);
            mContext.startActivityForResult(intent, GalleryConstant.REQUEST_COLLAPSE_CODE);
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
        mShowCollapse = showCollapse;
        if (mShowCollapse) {
            mTotal = index + 1;
        } else {
            mTotal = index;
        }
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

    public int getFirstPosition() {
        return mLayoutManager.findFirstCompletelyVisibleItemPosition();
    }

    public int getLastPosition() {
        return mLayoutManager.findLastCompletelyVisibleItemPosition();
    }
}

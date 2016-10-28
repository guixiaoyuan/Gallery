package com.tct.gallery3d.app.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.ImageView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AbstractGalleryFragment;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.LoadingListener;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.data.AlbumDataLoader;
import com.tct.gallery3d.app.data.AlbumDataLoader.DataListener;
import com.tct.gallery3d.app.fragment.AlbumFragment;
import com.tct.gallery3d.app.fragment.PhotoFragment;
import com.tct.gallery3d.app.view.PhotoItem;
import com.tct.gallery3d.app.view.RecyclingImageView;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.data.MediaSource;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.image.ImageWorker;
import com.tct.gallery3d.ui.ActionModeHandler;
import com.tct.gallery3d.ui.ActionModeHandler.ActionModeListener;
import com.tct.gallery3d.ui.SelectionManager;
import com.tct.gallery3d.util.GalleryUtils;

public class AlbumDataAdapter extends RecyclerView.Adapter<AlbumDataAdapter.AlbumListViewHolder> implements
        OnClickListener, OnLongClickListener, DataListener, SelectionManager.SelectionListener, ActionModeListener,
        LoadingListener {

    private static final String TAG = "AlbumDataAdapter";

    private AbstractGalleryActivity mContext;
    private AlbumFragment mFragment;
    private LayoutInflater mInflater;
    private AlbumDataLoader mLoader;
    private GridLayoutManager mLayoutManager;
    private SelectionManager mSelectionManager;
    private ActionModeHandler mActionModeHandler;
    private MediaSet mMediaSet;
    private RecyclerView.LayoutParams mImageViewLayoutParams;
    private PhotoItem mItem;
    private long mPreVersion = -1;

    private int mItemHeight;
    private int mNumColumns;
    private int mTotal;

    private boolean mGetContent;
    private boolean mGetMultiContent;
    private String mPath;
    private boolean mPrePrivateState;

    public AlbumDataAdapter(AlbumFragment fragment) {
        mFragment = fragment;
        mContext = (AbstractGalleryActivity) fragment.getGalleryContext();
        Bundle data = fragment.getArguments();
        mPath = data.getString(GalleryConstant.KEY_MEDIA_PATH);

        initManager(mPath);
        mGetContent = data.getBoolean(GalleryActivity.KEY_GET_CONTENT, false);
        mGetMultiContent = data.getBoolean(Intent.EXTRA_ALLOW_MULTIPLE, false);
        mImageViewLayoutParams = new RecyclerView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mInflater = LayoutInflater.from(mContext);
        setHasStableIds(true);
        mPrePrivateState = GalleryAppImpl.getTctPrivacyModeHelperInstance(mContext).isPrivacyModeEnable();
    }

    public void setLayoutManager(GridLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    private void initManager(String path) {
        mMediaSet = mContext.getDataManager().getMediaSet(path);
        mLoader = new AlbumDataLoader(mContext, mMediaSet);
        mLoader.setDataListener(this);
        mLoader.setLoadingListener(this);

        mSelectionManager = new SelectionManager(mContext, false);
        mSelectionManager.setSelectionListener(this);
        mSelectionManager.setSourceMediaSet(mMediaSet);
        mActionModeHandler = new ActionModeHandler(mContext, mFragment, mSelectionManager);
        mActionModeHandler.setActionModeListener(this);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void resume() {
        Log.d(TAG, "resume");
        mLoader.resume();
        mActionModeHandler.resume();
        boolean currentPrivateMode = GalleryAppImpl.getTctPrivacyModeHelperInstance(mContext).isPrivacyModeEnable();
        if (mPrePrivateState ^ currentPrivateMode) {
            mPrePrivateState = currentPrivateMode;
            if (MediaSource.LOCAL_PRIVATE_SET_PATH.equals(mPath)) {
                mContext.onBackPressed();
            }
        }
    }

    public void pause() {
        Log.d(TAG, "pause");
        mLoader.pause();
        mActionModeHandler.pause();
    }

    public void destroy() {
        mActionModeHandler.destroy();
        mLoader.setDataListener(null);
        mLoader.setLoadingListener(null);
        mLoader = null;
        mSelectionManager.setSelectionListener(null);
        mSelectionManager = null;
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        return mFragment.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
        case SelectionManager.ENTER_SELECTION_MODE: {
            if (mGetMultiContent) {
                mActionModeHandler.setGetMultiContent(true);
            }
            mActionModeHandler.setFromAlbumSetPage(true);
            mActionModeHandler.startActionMode();
            mActionModeHandler.isNeedShare(true);
            notifyDataSetChanged();
            break;
        }
        case SelectionManager.LEAVE_SELECTION_MODE: {
            mActionModeHandler.finishActionMode();
            notifyDataSetChanged();
            break;
        }
        case SelectionManager.SELECT_ALL_MODE: {
            mActionModeHandler.updateSupportedOperation();
            break;
        }
        }
    }

    @Override
    public void onSelectionChange(Path path, boolean selected) {
        int count = mSelectionManager.getSelectedCount();
        mActionModeHandler.setTitle(String.valueOf(count));
        mActionModeHandler.updateSupportedOperation(path, selected);
        if (count > 0 && !mSelectionManager.getAutoLeaveSelectionMode()) {
            mSelectionManager.setAutoLeaveSelectionMode(true);
        }
    }

    public void enterSelectionMode(boolean autoLeave) {
        mSelectionManager.setAutoLeaveSelectionMode(autoLeave);
        mSelectionManager.enterSelectionMode();
    }

    @Override
    public void onContentChanged(int index) {
        notifyItemChanged(index);
    }

    @Override
    public void onSizeChanged(int size) {
        if (mPreVersion != -1 && mPreVersion != mLoader.getmVersion() && mSelectionManager.inSelectionMode()) {
            //mSelectionManager.leaveSelectionMode();
            mActionModeHandler.refreshSelectedCount();
        }
        mPreVersion = mLoader.getmVersion();
        mTotal = size;
        notifyDataSetChanged();
    }

    @Override
    public boolean onLongClick(View v) {
        // Forbidden to long press: 1.In selection mode
        // 2.From another entrance and not allowed to enter selecton mode
        // 3.Content instance of PhotoFragment
        if (mSelectionManager.inSelectionMode() || (mGetContent && !mGetMultiContent)
                || !AbstractGalleryFragment.checkClickable(mContext)) {
            return false;
        }
        if (v instanceof PhotoItem) {
            PhotoItem contentItem = (PhotoItem) v;
            MediaItem item = contentItem.getMediaItem();
            if (item != null) {
                Path path = item.getPath();
                mSelectionManager.toggle(path);
                enterSelectionMode(true);
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (!AbstractGalleryFragment.checkClickable(mContext)) {
            return;
        }
        mItem = (PhotoItem) v;
        if (v instanceof PhotoItem) {
            PhotoItem content = (PhotoItem) v;
            MediaItem item = content.getMediaItem();
            if (item != null) {
                Path path = item.getPath();
                if (mSelectionManager.inSelectionMode()) {
                    mSelectionManager.toggle(path);
                    content.setSelected(mSelectionManager.isItemSelected(item.getPath()));
                } else if (mGetContent) {
                    Intent intent = new Intent(null, item.getContentUri())
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mContext.setResult(Activity.RESULT_OK, intent);
                    mContext.finish();
                } else {
                    // Check the drm play right.
                    if (!DrmManager.checkDrmPlayRight(mContext, item)) {
                        return;
                    }
                    int slotIndex = content.getSlotIndex();
                    int innerIndex = content.getInnerIndex();
                    String albumSetPath = mMediaSet.getPath().toString();

                    Bundle data = new Bundle();
                    data.putInt(GalleryConstant.KEY_FROM_PAGE, GalleryConstant.FROM_ALBUM_PAGE);
                    data.putInt(GalleryConstant.KEY_INDEX_SLOT, slotIndex);
                    data.putInt(GalleryConstant.KEY_INDEX_HINT, innerIndex);
                    data.putString(GalleryConstant.KEY_MEDIA_SET_PATH, albumSetPath);
                    data.putString(GalleryConstant.KEY_MEDIA_ITEM_PATH, item.getPath().toString());
                    data.putInt(GalleryConstant.KEY_MEDIA_ITEM_COUNT, mTotal);

                    int[] location = new int[2];
                    getLocation(v,location);
                    //v.getLocationOnScreen(location);
                    data.putInt(PhotoFragment.LOCATION_X, location[0]);
                    data.putInt(PhotoFragment.LOCATION_Y, location[1]);
                    data.putInt(PhotoFragment.WIDTH, v.getWidth());
                    data.putInt(PhotoFragment.HEIGHT, v.getHeight());

                    ImageView imageView = content.getContent();
                    Drawable drawable = imageView.getDrawable();
                    mContext.startPhotoPage(data, drawable);
                    mItem = content;
                }
            }
        }
    }
    private  void getLocation(View target, int[] position) {

        position[0] += target.getLeft();
        position[1] += target.getTop();

        ViewParent viewParent = target.getParent();
        while (viewParent instanceof View) {
            final View view = (View) viewParent;

            if (view.getId() == android.R.id.content) return;

            position[0] -= view.getScrollX();
            position[1] -= view.getScrollY();

            position[0] += view.getLeft();
            position[1] += view.getTop();

            viewParent = view.getParent();
        }

        position[0] = (int) (position[0] + 0.5f);
        position[1] = (int) (position[1] + 0.5f);
    }

    public boolean initLocation(int slotIndex, int innerIndex) {
        return initLocation(innerIndex);
    }

    private boolean initLocation(int index) {
        View content = mLayoutManager.findViewByPosition(index);
        if (content == null) {
            int start = mLayoutManager.findFirstCompletelyVisibleItemPosition();
            int end = mLayoutManager.findLastCompletelyVisibleItemPosition();
            int position = -1;
            if (index < start) {
                position = start;
            }
            if (index > end) {
                position = end;
            }
            int offset = mLayoutManager.findViewByPosition(position).getTop() - mLayoutManager.getPaddingTop();
            mLayoutManager.scrollToPositionWithOffset(index, offset);
            return false;
        }
        setOriginalInfo(content);
        return true;
    }

    public void setOriginalInfo(View content){
        if (content == null) {
            return;
        }
        mItem = (PhotoItem) content.findViewById(R.id.item);
        int[] location = new int[2];
        mItem.getLocationOnScreen(location);
        mItem.setVisibility(View.INVISIBLE);
        PhotoFragment fragment = ((PhotoFragment) mFragment.getFragment(PhotoFragment.TAG));
        if (fragment != null) {
            fragment.setOriginalInfo(mItem.getWidth(), mItem.getHeight(), location[0], location[1]);
        }
    }

    public void setContentVisible(boolean visible) {
        if (mItem != null) {
            mItem.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    /**
     * Sets the item height. Useful for when we know the column width so the
     * height can be set to match.
     *
     * @param height
     */
    public void setItemHeight(int height) {
        if (height == mItemHeight) {
            return;
        }
        mItemHeight = height;
        mImageViewLayoutParams = new RecyclerView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
        notifyDataSetChanged();
    }

    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
        mLayoutManager.setSpanCount(mNumColumns);
    }

    public int getNumColumns() {
        return mNumColumns;
    }

    @Override
    public int getItemCount() {
        return mTotal;
    }

    @Override
    public AlbumListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.photo_item, parent, false);
        AlbumListViewHolder holder = new AlbumListViewHolder(view, viewType);
        holder.mContent.setOnClickListener(this);
        holder.mContent.setOnLongClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(AlbumListViewHolder holder, int position) {
        int size = mLoader.size();
        if (size > 0) {
            setActiveWindow(size, position);
            PhotoItem contentItem = holder.mContent;
            if (contentItem.getLayoutParams().height != mItemHeight) {
                contentItem.setLayoutParams(mImageViewLayoutParams);
            }
            setLayoutInfo(contentItem, position);
        }
    }

    @Override
    public void onViewRecycled(AlbumListViewHolder holder) {
        if (holder instanceof AlbumListViewHolder) {
            PhotoItem photoItem = (PhotoItem) holder.itemView;
            ImageView imageView = photoItem.getContent();
            ImageWorker.cancelWork(imageView);
        }
        super.onViewRecycled(holder);
    }

    @Override
    public void onLoadingStarted() {
        mFragment.showEmptyView(false);
    }

    @Override
    public void onLoadingFinished(boolean loadingFailed) {
        mFragment.showEmptyView(mTotal == 0);
    }

    class AlbumListViewHolder extends ViewHolder {
        private PhotoItem mContent;

        public AlbumListViewHolder(View view, int viewType) {
            super(view);
            mContent = (PhotoItem) view.findViewById(R.id.item);
        }
    }

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-01,BUG-2208330*/
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

    private void setLayoutInfo(PhotoItem contentItem, int index) {
        if (!mLoader.isActive(index)) {
            contentItem.reset();
            return;
        }
        MediaItem item = mLoader.get(index);
        if (item == null) {
            contentItem.reset();
            return;
        }
        RecyclingImageView imageView = contentItem.getContent();
        // Load the bitmap.
        mFragment.loadThumbnail(item, imageView);
        contentItem.setMediaItem(item);
        contentItem.setSlotIndex(index);
        contentItem.setInnerIndex(index);

        boolean showSelect = mSelectionManager.inSelectionMode();
        contentItem.showSelected(showSelect);
        contentItem.setSelected(mSelectionManager.isItemSelected(item.getPath()));
    }

    public ActionModeHandler getActionModeHandler() {
        return mActionModeHandler;
    }

    public boolean isInSelectionMode() {
        return mSelectionManager.inSelectionMode();
    }

    public int getFirstPosition() {
        return mLayoutManager.findFirstCompletelyVisibleItemPosition();
    }

    public  boolean isInActionMode(){
        if(null != mSelectionManager){
            return mSelectionManager.inSelectionMode();
        }
        return false;
    }
}

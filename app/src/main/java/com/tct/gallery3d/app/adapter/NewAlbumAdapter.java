package com.tct.gallery3d.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AbstractGalleryFragment;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.app.NewAlbumSelectActivity;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.fragment.GalleryFragment;
import com.tct.gallery3d.app.fragment.MomentsFragment;
import com.tct.gallery3d.app.fragment.NewAlbumFragment;
import com.tct.gallery3d.app.fragment.PhotoFragment;
import com.tct.gallery3d.app.section.SectionCursor;
import com.tct.gallery3d.app.section.SectionInfo;
import com.tct.gallery3d.app.view.DragSelectRecyclerView;
import com.tct.gallery3d.app.view.ImageInformation;
import com.tct.gallery3d.app.view.PhotoItem;
import com.tct.gallery3d.app.view.RecyclingImageView;
import com.tct.gallery3d.data.LocalImage;
import com.tct.gallery3d.data.LocalMediaItem;
import com.tct.gallery3d.data.LocalVideo;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.data.MomentsNewAlbum;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.image.ImageWorker;
import com.tct.gallery3d.ui.ActionModeHandler;
import com.tct.gallery3d.ui.SelectionManager;
import com.tct.gallery3d.util.GalleryUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewAlbumAdapter extends DragSelectRecyclerViewAdapter<RecyclerView.ViewHolder> implements
        SelectionManager.SelectionListener, ActionModeHandler.ActionModeListener, View.OnClickListener, View
        .OnLongClickListener, MomentsNewAlbum.ContentChangeListener {

    public static final String TAG = NewAlbumAdapter.class.getSimpleName();

    private GalleryApp mApplication;
    private AbstractGalleryActivity mContext;
    private NewAlbumFragment mFragment;
    private GridLayoutManager mLayoutManager;
    private SelectionManager mSelectionManager;
    private ActionModeHandler mActionModeHandler;

    private DragSelectRecyclerView mRecyclerView;
    private PhotoItem mItem;
    private MomentsNewAlbum mMediaSet;

    private GalleryFragment.State mState = GalleryFragment.State.DAY;
    private List<List<ImageInformation>> mValuesList = new ArrayList<>();
    private HashMap<Integer, Integer> mSpanSizeMap = new HashMap<>();
    private HashMap<Integer, Integer> mItemHeightMap = new HashMap<>();

    private int mItemHeight = 0;
    private int mHeaderHeight = 0;
    private int mNumColumns = 1;
    private float mR = 0;
    private static final int mPadding = 1;

    public static final int ITEM_VIEW_ONE = 1;
    public static final int ITEM_VIEW_TWO = 2;
    public static final int ITEM_VIEW_THREE = 3;

    public static final int VIEW_TYPE_SECTION = 0;
    public static final int VIEW_TYPE_ITEM = 1;

    private static final int DEFAULT_SPAN_SIZE = 1;
    private static final int KEY_UPDATE_ADDRESS = 0;
    private String tPath;

    public NewAlbumAdapter(NewAlbumFragment context, RecyclerView recyclerView) {
        super(null);
        mApplication = (GalleryApp) context.getActivity().getApplication();
        mFragment = context;
        mRecyclerView = (DragSelectRecyclerView) recyclerView;
        mContext = (AbstractGalleryActivity) context.getGalleryContext();
        mR = getR(mContext);
        Bundle bundle = context.getArguments();
        String mediaPath = bundle.getString(GalleryConstant.KEY_MEDIA_PATH);
        initManager(mediaPath);
        setHasStableIds(true);
        if (!mSelectionManager.inSelectionMode()) {
            mSelectionManager.setAutoLeaveSelectionMode(false);
            mSelectionManager.enterSelectionMode();
        }
    }


    private void initManager(String mediaPath) {
        mMediaSet = (MomentsNewAlbum) mContext.getDataManager().getMediaSet(mediaPath);
        mMediaSet.addContentChangeListener(this);
        mSelectionManager = new SelectionManager(mContext, false);
        mSelectionManager.setSelectionListener(this);
        mSelectionManager.setSourceMediaSet(mMediaSet);
        mActionModeHandler = new ActionModeHandler(mContext, mFragment, mSelectionManager);
        mActionModeHandler.setActionModeListener(this);
        mHeaderHeight = mContext.getResources().getDimensionPixelSize(R.dimen.moments_header_height);
    }

    /**
     * 1. 1/3<= A/C <= 2
     * 2. 2.5*r <= 2A+C <= 4*r
     *
     * @param position
     * @return
     */
    public int getCount(int position, int index, int firstPosition) {
        List<ImageInformation> mValues = mValuesList.get(index);
        if (position < firstPosition + mValues.size() - 1) {
            float A = (mValues.get(position - firstPosition).getWidth() / (float) mValues.get(position - firstPosition).getHeight());
            if (position < firstPosition + mValues.size() - 3) {
                float B = (float) mValues.get(position - firstPosition + 1).getWidth() / (float) mValues.get(position - firstPosition + 1).getHeight();
                float C = (float) mValues.get(position - firstPosition + 2).getWidth() / (float) mValues.get(position - firstPosition + 2).getHeight();
                if (A == B) {
                    if (0.33333 <= A / C && A / C <= 2 && 2 * mR <= 2 * A + C && 2 * A + C <= 4 * mR) {
                        return ITEM_VIEW_THREE;
                    }
                }
                if (A == C) {
                    if (0.33333 <= A / B && A / B <= 2 && 2 * mR <= 2 * A + B && 2 * A + B <= 4 * mR) {
                        return ITEM_VIEW_THREE;
                    }
                }
                if (B == C) {
                    if (0.33333 <= B / A && B / A <= 2 && 2 * mR <= 2 * B + A && 2 * B + A <= 4 * mR) {
                        return ITEM_VIEW_THREE;
                    }
                }

            }
            if (position < firstPosition + mValues.size() - 2) {
                float B = (float) mValues.get(position - firstPosition + 1).getWidth() / (float) mValues.get(position - firstPosition + 1).getHeight();
                if (0.25 <= A / B && A / B <= 4 && 2 * mR <= A + B && A + B <= 4 * mR) {
                    return ITEM_VIEW_TWO;
                }
            }
            return ITEM_VIEW_ONE;
        }
        return ITEM_VIEW_ONE;
    }

    @Override
    public int getItemViewType(int position) {
        SectionCursor cursor = getCursor();
        boolean isSection = cursor.isSection(position);
        if (isSection) {
            return VIEW_TYPE_SECTION;
        }
        return VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        int type = getItemViewType(position);
        switch (type) {
            case VIEW_TYPE_SECTION:
                return getSectionForPosition(position).getTime().hashCode();
        }
        return super.getItemId(position);
    }

    public void resume() {
        Log.d(TAG, "resume");
        mActionModeHandler.resume();
    }

    public void pause() {
        Log.d(TAG, "pause");
        mActionModeHandler.pause();
    }

    public void destroy() {
        mActionModeHandler.setActionModeListener(null);
        mActionModeHandler.destroy();
        mActionModeHandler = null;
        mSelectionManager.setSelectionListener(null);
        mSelectionManager = null;
        mMediaSet.removeContentChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        if (!AbstractGalleryFragment.checkClickable(mContext)) {
            return;
        }
        PhotoItem content;
        if (view instanceof PhotoItem) {
            content = (PhotoItem) view;
            MediaItem item = content.getMediaItem();
            if (item == null) {
                return;
            }
            Path path = item.getPath();
            if (mSelectionManager.inSelectionMode()) {
                mSelectionManager.toggle(path);
                content.setSelected(mSelectionManager.isItemSelected(item.getPath()));
                if(tPath == null){
                    mContext.setResult(99,(new Intent()).putExtra("path",path.toString()));
                    ((NewAlbumSelectActivity)mContext).finish();
                }
            } else {
                mSelectionManager.toggle(path);
                mSelectionManager.setAutoLeaveSelectionMode(false);
                mSelectionManager.enterSelectionMode();
            }
        }
    }

    public void setPath(String path){
        tPath = path;
    }
    @Override
    public boolean onLongClick(View view) {
        PhotoItem photoItem = null;
        if (view instanceof PhotoItem) {
            photoItem = (PhotoItem) view;
        }
        if (photoItem == null) {
            return false;
        }
        MediaItem item = photoItem.getMediaItem();
        if (item == null) {
            return false;
        }
        Path path = item.getPath();
        if (mSelectionManager.inSelectionMode()) {
            if (!mSelectionManager.isItemSelected(path)) {
                mSelectionManager.toggle(path);
                (view).setSelected(true);
            }
        } else {
            mSelectionManager.toggle(path);
            mSelectionManager.setAutoLeaveSelectionMode(false);
            mSelectionManager.enterSelectionMode();
        }
        mRecyclerView.setDragSelectActive(true, photoItem.getPosition());
        return true;
    }

    @Override
    public void onContentChange() {
        mFragment.restartLoader(false);
        if(mSelectionManager.inSelectionMode()){
            mActionModeHandler.refreshSelectedCount();
        }
    }

    class SectionHolder extends RecyclerView.ViewHolder {
        TextView mTime;
        TextView mAddress;
        LinearLayout mLocation;

        public SectionHolder(View view) {
            super(view);
            mTime = (TextView) view.findViewById(R.id.time);
            mLocation = (LinearLayout) view.findViewById(R.id.location);
            mAddress = (TextView) view.findViewById(R.id.location_text);
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        public PhotoItem mContent;

        public ItemHolder(View view) {
            super(view);
            mContent = (PhotoItem) view.findViewById(R.id.item);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerView.ViewHolder holder;
        switch (viewType) {
            case VIEW_TYPE_SECTION:
                View header = LayoutInflater.from(parent.getContext()).inflate(R.layout.moments_header, parent, false);
                holder = new SectionHolder(header);
                break;
            case VIEW_TYPE_ITEM:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
                view.setPadding(mPadding, mPadding, mPadding, mPadding);
                holder = new ItemHolder(view);
                view.setOnClickListener(this);
                view.setOnLongClickListener(this);
                break;
            default:
                holder = null;
        }
        return holder;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case KEY_UPDATE_ADDRESS:
                    notifyItemChanged((int) msg.obj);
                    break;
            }
        }
    };

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        super.onBindViewHolderCursor(holder, cursor);
        final int position = cursor.getPosition();
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_SECTION:
                SectionHolder headerHolder = (SectionHolder) holder;
                SectionInfo info = ((SectionCursor<SectionInfo>) cursor).getSection(position);
                info.setOnAddressChangedListener(new SectionInfo.AddressChangedListener() {
                    @Override
                    public void onAddressChange() {
                        mHandler.sendMessage(mHandler.obtainMessage(KEY_UPDATE_ADDRESS, position));
                    }
                });
                headerHolder.mTime.setText(info.getTime());
                updateAddress(headerHolder, info);
                break;
            case VIEW_TYPE_ITEM:
                ItemHolder itemHolder = (ItemHolder) holder;
                PhotoItem contentItem = itemHolder.mContent;
                MediaItem item = createItem(cursor, mApplication);

                contentItem.setPosition(position);
                contentItem.setMediaItem(item);

                boolean isStaggered = false;
                contentItem.setSquare(!isStaggered);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) contentItem.getLayoutParams();
                int headerNum = countHeaderSize(position);
                if (isStaggered) {
                    params.height = mItemHeightMap.get(position - headerNum);
                    contentItem.setLayoutParams(params);
                }

                setLayoutInfo(contentItem, position - headerNum, item);
                break;
        }
    }

    private void updateAddress(SectionHolder holder, SectionInfo info) {
        String address = info.getAddress();
        if (!TextUtils.isEmpty(address)) {
            holder.mLocation.setVisibility(View.VISIBLE);
        } else {
            holder.mLocation.setVisibility(View.INVISIBLE);
        }
        holder.mAddress.setText(address);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof ItemHolder) {
            PhotoItem photoItem = (PhotoItem) holder.itemView;
            ImageView imageView = photoItem.getContent();
            ImageWorker.cancelWork(imageView);
        }
        super.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        return super.onFailedToRecycleView(holder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SectionCursor<SectionInfo> getCursor() {
        return (SectionCursor<SectionInfo>) super.getCursor();
    }

    public SectionInfo getSectionForPosition(int position) {
        if (position < 0) {
            position = 0;
        }
        if (position >= getItemCount()) {
            position = getItemCount() - 1;
        }
        return getDateOrPathSection(position);
    }

    public SectionInfo getDateOrPathSection(int position) {
        SectionCursor<SectionInfo> cursor = getCursor();
        return cursor.getSection(position);
    }

    public void setLayoutManager(GridLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
        CustomSpanSize customSpanSize = new CustomSpanSize();
        customSpanSize.setSpanIndexCacheEnabled(true);
        mLayoutManager.setSpanSizeLookup(customSpanSize);
    }

    private class CustomSpanSize extends GridLayoutManager.SpanSizeLookup {
        @Override
        public int getSpanSize(int position) {
            setSpanIndexCacheEnabled(true);

            int type = getItemViewType(position);
            switch (type) {
                case VIEW_TYPE_SECTION:
                    return mLayoutManager.getSpanCount();
            }
            return DEFAULT_SPAN_SIZE;
        }
    }

    private float getR(Context context) {
        return (float) GalleryUtils.getScreenWidth(context) / (float) GalleryUtils.getScreenHeight(context);
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        return mFragment.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case SelectionManager.ENTER_SELECTION_MODE: {
                mActionModeHandler.setFromAlbumSetPage(true);
                mActionModeHandler.setNewAlbumSelectTag(true);
                mActionModeHandler.startActionMode();
                mFragment.setSelectionModeStatus(true);
                mActionModeHandler.isNeedShare(true);
                notifyDataSetChanged();
                break;
            }
            case SelectionManager.LEAVE_SELECTION_MODE: {
                mActionModeHandler.finishActionMode();
                mFragment.setSelectionModeStatus(false);
                notifyDataSetChanged();
                break;
            }
            case SelectionManager.SELECT_ALL_MODE: {
                mActionModeHandler.updateSupportedOperation();
                break;
            }
        }
    }

    public void enterSelectionMode(boolean autoLeave) {
        mSelectionManager.setAutoLeaveSelectionMode(autoLeave);
        mSelectionManager.enterSelectionMode();
    }

    @Override
    public void onSelectionChange(Path path, boolean selected) {
        int count = mSelectionManager.getSelectedCount();
        mActionModeHandler.setTitle(String.valueOf(count));
        mActionModeHandler.updateSupportedOperation(path, selected);
    }

    @Override
    public void selectRange(int from, int to, int min, int max) {
        PhotoItem item = null;
        Path path;
        RecyclerView.ViewHolder holder;
        View view;
        if (from == to) {
            // Finger is back on the initial item, unselect everything else
            for (int i = min; i <= max; i++) {
                if (i == from) continue;
                holder = mRecyclerView.findViewHolderForLayoutPosition(i);
                if (holder != null) {
                    view = holder.itemView;
                    if (view instanceof PhotoItem) {
                        item = (PhotoItem) view;
                    }
                }
                if (item != null) {
                    path = item.getMediaItem().getPath();
                    if (!mSelectionManager.isItemSelected(path)) {
                        continue;
                    }
                    mSelectionManager.toggle(path);
                    item.setSelected(mSelectionManager.isItemSelected(path));
                }
            }
            return;
        }
        if (to < from) {
            // When selecting from one to previous items
            for (int i = to; i <= from; i++) {
                if (i == from) continue;
                holder = mRecyclerView.findViewHolderForLayoutPosition(i);
                if (holder != null) {
                    view = holder.itemView;
                    if (view instanceof PhotoItem) {
                        item = (PhotoItem) view;
                    }
                }
                if (item != null) {
                    path = item.getMediaItem().getPath();
                    if (mSelectionManager.isItemSelected(path)) {
                        continue;
                    }
                    mSelectionManager.toggle(path);
                    item.setSelected(mSelectionManager.isItemSelected(path));
                }
            }
            if (min > -1 && min < to) {
                // Unselect items that were selected during this drag but no longer are
                for (int i = min; i < to; i++) {
                    if (i == from) continue;
                    holder = mRecyclerView.findViewHolderForLayoutPosition(i);
                    if (holder != null) {
                        view = holder.itemView;
                        if (view instanceof PhotoItem) {
                            item = (PhotoItem) view;
                        }
                    }
                    if (item != null) {
                        path = item.getMediaItem().getPath();
                        if (!mSelectionManager.isItemSelected(path)) {
                            continue;
                        }
                        mSelectionManager.toggle(path);
                        item.setSelected(mSelectionManager.isItemSelected(path));
                    }
                }
            }
            if (max > -1) {
                for (int i = from + 1; i <= max; i++) {
                    holder = mRecyclerView.findViewHolderForLayoutPosition(i);
                    if (holder != null) {
                        view = holder.itemView;
                        if (view instanceof PhotoItem) {
                            item = (PhotoItem) view;
                        }
                    }
                    if (item != null) {
                        path = item.getMediaItem().getPath();
                        if (!mSelectionManager.isItemSelected(path)) {
                            continue;
                        }
                        mSelectionManager.toggle(path);
                        item.setSelected(mSelectionManager.isItemSelected(path));
                    }
                }
            }
        } else {
            // When selecting from one to next items
            for (int i = from; i <= to; i++) {
                if (i == from) continue;
                holder = mRecyclerView.findViewHolderForLayoutPosition(i);
                if (holder != null) {
                    view = holder.itemView;
                    if (view instanceof PhotoItem) {
                        item = (PhotoItem) view;
                    }
                }
                if (item != null) {
                    path = item.getMediaItem().getPath();
                    if (mSelectionManager.isItemSelected(path)) {
                        continue;
                    }
                    mSelectionManager.toggle(path);
                    item.setSelected(mSelectionManager.isItemSelected(path));
                }
            }
            if (max > -1 && max > to) {
                // Unselect items that were selected during this drag
                for (int i = to + 1; i <= max; i++) {
                    if (i == from) continue;
                    holder = mRecyclerView.findViewHolderForLayoutPosition(i);
                    if (holder != null) {
                        view = holder.itemView;
                        if (view instanceof PhotoItem) {
                            item = (PhotoItem) view;
                        }
                    }
                    if (item != null) {
                        path = item.getMediaItem().getPath();
                        if (!mSelectionManager.isItemSelected(path)) {
                            continue;
                        }
                        mSelectionManager.toggle(path);
                        item.setSelected(mSelectionManager.isItemSelected(path));
                    }
                }
            }
            if (min > -1) {
                for (int i = min; i < from; i++) {
                    holder = mRecyclerView.findViewHolderForLayoutPosition(i);
                    if (holder != null) {
                        view = holder.itemView;
                        if (view instanceof PhotoItem) {
                            item = (PhotoItem) view;
                        }
                    }
                    if (item != null) {
                        path = item.getMediaItem().getPath();
                        if (!mSelectionManager.isItemSelected(path)) {
                            continue;
                        }
                        mSelectionManager.toggle(path);
                        item.setSelected(mSelectionManager.isItemSelected(path));
                    }
                }
            }
        }
    }

    public boolean initLocation(int slotIndex, int innerIndex) {
        return initLocation(innerIndex);
    }

    private boolean initLocation(int index) {
        index = getCursor().getRelPosition(index);
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
            View v = mLayoutManager.findViewByPosition(position);
            if (v != null) {
                int offset = v.getTop() - mLayoutManager.getPaddingTop();
                mLayoutManager.scrollToPositionWithOffset(index, offset);
            } else {
                mLayoutManager.scrollToPosition(index);
            }
            return false;
        }
        setOriginalInfo(content);
        return true;
    }

    public void setOriginalInfo(View content) {
        if (content == null) {
            return;
        }
        mItem = (PhotoItem) content.findViewById(R.id.item);
        int[] location = new int[2];
        if (mItem != null) {
            mItem.getLocationOnScreen(location);
            mItem.setVisibility(View.INVISIBLE);
            PhotoFragment fragment = ((PhotoFragment) mFragment.getFragment(PhotoFragment.TAG));
            if (fragment != null) {
                fragment.setOriginalInfo(mItem.getWidth(), mItem.getHeight(), location[0], location[1]);
            }
        }
    }

    public void setContentVisible(boolean visible) {
        if (mItem != null) {
            mItem.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public int getFirstPosition() {
        return mLayoutManager.findFirstCompletelyVisibleItemPosition();
    }

    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
        mLayoutManager.setSpanCount(mNumColumns);
    }

    public String getAlbumPath() {
        return mMediaSet.getPath().toString();
    }

    public ActionModeHandler getActionModeHandler() {
        return mActionModeHandler;
    }

    public boolean canSlideShow() {
        int setType = mMediaSet.getMediaSetType();
        return setType == MediaSet.MEDIASET_TYPE_IMAGE;
    }

    public synchronized void setSpanList() {
        if (mValuesList.size() != 0) {
            mValuesList.clear();
            mSpanSizeMap.clear();
            mItemHeightMap.clear();
        }
        int Width = GalleryUtils.getScreenWidth(mContext);
        int Height = GalleryUtils.getScreenHeight(mContext);
        SectionCursor<SectionInfo> cursor = getCursor();
        cursor.moveToFirst();
        List<ImageInformation> infos = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.isSection(i)) {
                if (infos.size() > 0) {
                    mValuesList.add(infos);
                }
                infos = new ArrayList<>();
            } else {
                cursor.moveToPosition(i);
                int width = cursor.getInt(MomentsNewAlbum.INDEX_WIDTH);
                int height = cursor.getInt(MomentsNewAlbum.INDEX_HEIGHT);
                if (width == 0 || height == 0) {
                    width = Height / 2;
                    height = Height / 2;
                }
                infos.add(new ImageInformation(width, height));
            }
        }
        if (infos.size() > 0) {
            mValuesList.add(infos);
        }
        int firstPosition = 0;
        int lastPosition;
        List<ImageInformation> values;
        for (int i = 0; i < mValuesList.size(); i++) {
            int a = firstPosition;
            values = mValuesList.get(i);
            lastPosition = firstPosition + values.size();
            while (a < lastPosition) {
                int w = values.get(a - firstPosition).getWidth();
                int h = values.get(a - firstPosition).getHeight();
                float A = w / h;
                int count = getCount(a, i, firstPosition);
                switch (count) {
                    case ITEM_VIEW_ONE:
                        if (A >= 1) {
                            mItemHeightMap.put(a, Width * h / w);
                        } else {
                            mItemHeightMap.put(a, 3 * Height / 5);
                        }
                        mSpanSizeMap.put(a, 6);
                        a = a + 1;
                        break;
                    case ITEM_VIEW_TWO:
                        float B = (values.get(a - firstPosition + 1).getWidth() / (float) values.get(a - firstPosition + 1).getHeight());
                        if (A >= 1 && B < 1) {
                            mItemHeightMap.put(a, 2 * Width * h / (3 * w));
                            mItemHeightMap.put(a + 1, 2 * Width * h / (3 * w));
                            mSpanSizeMap.put(a, 4);
                            mSpanSizeMap.put(a + 1, 2);
                        } else if (A < 1 && B >= 1) {
                            mItemHeightMap.put(a, Width * h / (3 * w));
                            mItemHeightMap.put(a + 1, Width * h / (3 * w));
                            mSpanSizeMap.put(a, 2);
                            mSpanSizeMap.put(a + 1, 4);
                        } else {
                            mItemHeightMap.put(a, Width * h / (2 * w));
                            mItemHeightMap.put(a + 1, Width * h / (2 * w));
                            mSpanSizeMap.put(a, 3);
                            mSpanSizeMap.put(a + 1, 3);
                        }
                        a = a + 2;
                        break;
                    case ITEM_VIEW_THREE:
                        if (h / w > 3) {
                            mItemHeightMap.put(a, Height / 3);
                            mItemHeightMap.put(a + 1, Height / 3);
                            mItemHeightMap.put(a + 2, Height / 3);
                        } else {
                            mItemHeightMap.put(a, Width * h / (3 * w));
                            mItemHeightMap.put(a + 1, Width * h / (3 * w));
                            mItemHeightMap.put(a + 2, Width * h / (3 * w));
                        }
                        mSpanSizeMap.put(a, 2);
                        mSpanSizeMap.put(a + 1, 2);
                        mSpanSizeMap.put(a + 2, 2);
                        a = a + 3;
                        break;
                }
            }
            firstPosition = lastPosition;
        }
    }

    private void setLayoutInfo(PhotoItem contentItem, int index, final MediaItem item) {
        final RecyclingImageView imageView = contentItem.getContent();
        // Load the bitmap.
        mFragment.loadThumbnail(item, imageView, new ImageWorker.OnImageLoadedListener() {
            @Override
            public void onImageLoaded(boolean success) {
                if (success) {
                }
            }
        });
        contentItem.setMediaItem(item);
        contentItem.setSlotIndex(index);
        contentItem.setInnerIndex(index);

        boolean showSelect = mSelectionManager.inSelectionMode();
        contentItem.showSelected(showSelect);
        contentItem.setSelected(mSelectionManager.isItemSelected(item.getPath()));
        ViewGroup.LayoutParams shadowParams = contentItem.mShadowLayout.getLayoutParams();
        shadowParams.height = (int) mContext.getResources().getDimension(R.dimen.moments_shadow_large_height);
    }


    private MediaItem createItem(Cursor cursor, GalleryApp app) {
        LocalMediaItem item;
        int mediaType = cursor.getInt(MomentsNewAlbum.INDEX_MEDIA_TYPE);
        boolean isImage = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE == mediaType;
        int idIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
        long id = cursor.getLong(idIndex);
        Path itemPath = isImage ? LocalImage.ITEM_PATH : LocalVideo.ITEM_PATH;
        Path childPath = itemPath.getChild(id);
        if (isImage) {
            item = new LocalImage(childPath, app, cursor, true);
        } else {
            item = new LocalVideo(childPath, app, cursor, true);
        }
        return item;
    }

    private int countHeaderSize(int position) {
        return getCursor().getSectionCount(position);
    }

    public void setState(MomentsFragment.State state) {
        mState = state;
    }
}

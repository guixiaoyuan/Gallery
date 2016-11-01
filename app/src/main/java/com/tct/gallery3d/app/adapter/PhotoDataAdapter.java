package com.tct.gallery3d.app.adapter;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.mtk.drm.frameworks.MtkDrmManager;
import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.data.PhotoDataLoader;
import com.tct.gallery3d.app.data.PhotoDataLoader.DataListener;
import com.tct.gallery3d.app.fragment.PhotoFragment;
import com.tct.gallery3d.app.view.PhotoDetailView;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.FilterDeleteSet;
import com.tct.gallery3d.data.LocalImage;
import com.tct.gallery3d.data.LocalMediaItem;
import com.tct.gallery3d.data.LocalVideo;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaObject;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.ui.MenuExecutor;
import com.tct.gallery3d.ui.SelectionManager;
import com.tct.gallery3d.util.DragUtil;
import com.tct.gallery3d.util.ScreenUtils;

import java.util.HashMap;
import java.util.Map;

public class PhotoDataAdapter extends PagerAdapter implements DataListener ,View.OnLongClickListener{

    private AbstractGalleryActivity mContext;
    private PhotoFragment mFragment;
    private ViewPager mViewPager;

    private FilterDeleteSet mMediaSet;
    private SelectionManager mSelectionManager;
    private MenuExecutor mMenuExecutor;
    private Path mDeletePath;

    private PhotoDataLoader mLoader;
    private LayoutInflater mInflater;
    private boolean mIsSingleItem;

    private MediaItem mSingleMediaItem = null;

    private int mTotal;
    private int mFirst;
    private Map<Integer, View> mContainsViews;

    private MediaItem mMediaItem;
    private PhotoDetailView mPhotoDetailView;
    private boolean isShowConsume = false;

    public PhotoDataAdapter(PhotoFragment fragment, ViewPager viewPager) {
        mContainsViews = new HashMap<>();
        mFragment = fragment;
        mContext = (AbstractGalleryActivity) fragment.getGalleryContext();
        DataManager manager = mContext.getDataManager();

        Bundle data = fragment.getArguments();
        mViewPager = viewPager;
        String itemPath = data.getString(GalleryConstant.KEY_MEDIA_ITEM_PATH);
        Path path = Path.fromString(itemPath);
        mIsSingleItem = data.getBoolean(GalleryActivity.KEY_SINGLE_ITEM_ONLY);
        mSingleMediaItem = (MediaItem) manager.getMediaObject(path);
        if (mIsSingleItem) {
            return;
        }
        String setPath = data.getString(GalleryConstant.KEY_MEDIA_SET_PATH);
        setPath = "/filter/delete/{" + setPath + "}";
        mMediaSet = (FilterDeleteSet) manager.getMediaSet(setPath);
        if(mFragment.mInnerIndex == GalleryConstant.INVALID_INDEX){
            mFragment.mInnerIndex = mMediaSet.getIndexOfItem(path, GalleryConstant.INVALID_INDEX);
        }
        mFirst = mFragment.mInnerIndex;
        int count = data.getInt(GalleryConstant.KEY_MEDIA_ITEM_COUNT, GalleryConstant.INVALID_ITEM_COUNT);
        if (count != GalleryConstant.INVALID_ITEM_COUNT) {
            mTotal = count;
        } else {
            mTotal = mMediaSet.getMediaItemCount();
        }
        notifyDataSetChanged();
        mLoader = new PhotoDataLoader(mContext, mMediaSet);
        mLoader.setDataListener(this);
        mSelectionManager = new SelectionManager(mContext, false);
        mSelectionManager.setSourceMediaSet(mMediaSet);
        mMenuExecutor = new MenuExecutor(mContext, mSelectionManager);
    }

    public void resume() {
        if (mIsSingleItem)
            return;
        mLoader.resume();
    }

    public PhotoDetailView getPhotoDetailView(int position) {
        if (mContainsViews != null) {
            View view = mContainsViews.get(position);
            if(view != null){
                PhotoDetailView photoView = (PhotoDetailView) view.findViewById(R.id.imageView);
                return  photoView;
            }
        }
        return null;
    }

    public void pause() {
        if (mIsSingleItem)
            return;
        mLoader.pause();
    }

    public void destroy() {
        if (mIsSingleItem)
            return;
        mLoader.setDataListener(null);
        mLoader = null;
        mSelectionManager.setSelectionListener(null);
        mSelectionManager = null;
        mContainsViews.clear();
        mContainsViews = null;
    }

    @Override
    public int getCount() {
        if (mIsSingleItem)
            return GalleryConstant.DEFAULT_ITEM_COUNT;
        return mTotal;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int getItemPosition(Object object) {
        PhotoDetailView imageView = (PhotoDetailView) ((View) object).findViewById(R.id.imageView);
        return imageView.getPosition();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(mContext);
        }

        View view = mInflater.inflate(R.layout.photo_detail_adapter_item, container, false);
        container.addView(view);
        PhotoDetailView photoView = (PhotoDetailView) view.findViewById(R.id.imageView);
        photoView.setGestureEventListener(mFragment);
        photoView.setOnClickListener(mFragment);
        photoView.setOnLongClickListener(this);
        photoView.setPosition(position);
        ImageButton photoViewButton = (ImageButton) view.findViewById(R.id.id_photoview_button);
        if (ScreenUtils.getScreenInfo(mContext) == ScreenUtils.tempScreenInPortFull) {
            photoViewButton.setPadding(0, 0, 0, 150);
        } else {
            photoViewButton.setPadding(0, 0, 0, 0);
        }
        photoViewButton.setOnClickListener(mFragment);

        setLayoutInfo(photoView, photoViewButton, position);
        mContainsViews.put(position, view);
        return view;
    }

    private void setLayoutInfo(int position) {
        if (mContainsViews != null) {
            View view = mContainsViews.get(position);
            if (view != null) {
                PhotoDetailView photoView = (PhotoDetailView) view.findViewById(R.id.imageView);
                ImageButton photoViewButton = (ImageButton) view.findViewById(R.id.id_photoview_button);
                setLayoutInfo(photoView, photoViewButton, position);
            }
        }
    }

    private void setLayoutInfo(PhotoDetailView photoView, ImageButton photoViewButton, int position) {
        if (photoView != null) {
            photoView.reset();
        } else {
            return;
        }
        MediaItem item = null;
        if (position == mFirst || mIsSingleItem) {
            mFirst = GalleryConstant.INVALID_INDEX;
            item = mSingleMediaItem;
        } else {
            if (mLoader.size() > 0) {
                setActiveWindow(position);
                item = mLoader.get(position);
            }
        }

        if (item != null) {
            photoView.setRotation(item.getRotation());
            String burstId = getBurstShotId(position);
            if (burstId != null && ExifInfoFilter.getInstance(mContext)
                    .queryType(item.getPath().getSuffix()) == ExifInfoFilter.BURSTSHOTS) {
                photoViewButton.setVisibility(View.VISIBLE);
            } else {
                photoViewButton.setVisibility(View.INVISIBLE);
            }
            if (position == getCurrentIndex()) {
                mFragment.updateUI(item);
            }
            int type = item.getMediaType();

            switch (type) {
                case MediaItem.MEDIA_TYPE_VIDEO:
                    photoView.setIsVideo(true);
                    break;
                case MediaItem.MEDIA_TYPE_GIF:
                    photoView.setIsGif(true);
                    break;
                default:
                    break;
            }
            if (item.isDrm() == DrmManager.IS_DRM) {
                photoView.setIsDrm(true);
            }
            if (type == MediaItem.MEDIA_TYPE_IMAGE && item.isDrm() == DrmManager.IS_DRM && DrmManager.getInstance().mCurrentDrm == DrmManager.MTK_DRM) {
                int drmType = DrmManager.getInstance().getDrmScheme(item.getFilePath());
                if (drmType != DrmManager.DRM_SCHEME_OMA1_FL) {
                    if (position == getCurrentIndex()) {
                        mMediaItem = item;
                        mPhotoDetailView = photoView;
                        if (!isShowConsume) {
                            showMtkDrmDialog(mContext.getAndroidContext(), item);
                            isShowConsume = true;
                        }
                    } else {
                        mFragment.loadThumbnail(item, photoView);
                    }
                } else {
                    mFragment.loadLarge(item, photoView);
                }
            } else {
                mFragment.loadLarge(item, photoView);
            }
        }
    }

    public void showMtkDrmDialog(Context context, MediaItem item) {
        final MediaItem mediaItem = (MediaItem) item;
        int rights = DrmManager.getInstance().checkRightsStatus(mediaItem.getFilePath(), MtkDrmManager.Action.DISPLAY);

        if (MtkDrmManager.RightsStatus.RIGHTS_VALID == rights) {
            DrmManager.getInstance().showConsumeDialog(context,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (DialogInterface.BUTTON_POSITIVE == which) {
                                mFragment.loadLarge(mMediaItem, mPhotoDetailView);
                                DrmManager.getInstance().consumeRights(mediaItem.getFilePath(), MtkDrmManager.Action.DISPLAY);
                            } else {
                                mFragment.loadThumbnail(mMediaItem, mPhotoDetailView);
                            }
                            dialog.dismiss();
                        }
                    },
                    new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            isShowConsume = false;
                        }
                    }
            );
        } else {
            if (MtkDrmManager.RightsStatus.SECURE_TIMER_INVALID == rights) {
                DrmManager.getInstance().showSecureTimerInvalidDialog(context,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        },
                        new DialogInterface.OnDismissListener() {
                            public void onDismiss(DialogInterface dialog) {
                                isShowConsume = false;
                            }
                        }
                );
            } else {
                DrmManager.getInstance().showRefreshLicenseDialog(context, mediaItem.getFilePath());
                isShowConsume = false;
            }
        }
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        if (mContainsViews.containsKey(position)) {
            mContainsViews.remove(position);
        }
    }

    public int getCurrentIndex() {
        if (mIsSingleItem) {
            return GalleryConstant.DEFAULT_INDEX;
        }
        return mViewPager.getCurrentItem();
    }

    public MediaItem getCurrentMediaItem() {
        if (getCount() == 0) return null;
        if (mIsSingleItem) {
            return mSingleMediaItem;
        }
        int index = mViewPager.getCurrentItem();
        return getMediaItem(index);
    }

    public MediaItem getMediaItem(int index) {
        if (mIsSingleItem) {
            return mSingleMediaItem;
        }
        return mLoader.get(index);
    }

    @Override
    public void onContentChanged(int index) {
        setLayoutInfo(index);
    }

    @Override
    public void onSizeChanged(int size) {
        mTotal = size;
        notifyDataSetChanged();
        if (size <= mFragment.mInnerIndex) {
            mFragment.mInnerIndex = size - 1;
        }
        mViewPager.setCurrentItem(mFragment.mInnerIndex);
        setActiveWindow(getCurrentIndex());
        if (size == 0) {
            mFragment.onBackPressed();
//        } else {
//            mFragment.updateUi();
        }
    }

    public String getBurstShotId(int index) {
        if (!mIsSingleItem) {
            MediaItem item = getMediaItem(index);
            if (item != null) {
                return ExifInfoFilter.getInstance(mContext).queryBurstShotId(((LocalMediaItem) item).id + "");
            }
        }
        return null;
    }

    public boolean isSlowMotion(int index) {
        if (mIsSingleItem)
            return false;
        return getExifType(index) == ExifInfoFilter.SLOWMOTION;
    }

    private int getExifType(int index) {
        if (!mIsSingleItem) {
            MediaItem item = getMediaItem(index);
            if (item != null) {
                return ExifInfoFilter.getInstance(mContext).queryType(((LocalMediaItem) item).id + "");
            }
        }
        return ExifInfoFilter.NONE;
    }

    public String getMediaSetPath() {
        if (mIsSingleItem)
            return "";
        return mMediaSet.getPath().toString();
    }

    public void delete(Path path) {
        String confirmMsg = mContext.getResources().getQuantityString(R.plurals.delete_selection, 1);
        mDeletePath = path;
        mSelectionManager.deSelectAll();
        mSelectionManager.toggle(path);
        mMenuExecutor.onMenuClicked(R.id.action_delete, confirmMsg, mConfirmDialogListener, false);
    }

    private MenuExecutor.ProgressListener mConfirmDialogListener = new MenuExecutor.ProgressListener() {
        @Override
        public void onProgressUpdate(int index) {
        }

        @Override
        public void onProgressComplete(int result) {
            if (mDeletePath == null)
                return;
            mMediaSet.removeDeletion(mDeletePath);
            mDeletePath = null;
            mFragment.dismissProgressDialog();
        }

        @Override
        public void onConfirmDialogShown() {
        }

        @Override
        public void onConfirmDialogDismissed(boolean confirmed) {
        }

        @Override
        public void onProgressStart() {
            mFragment.showProgressDialog();
        }
    };

    private void setActiveWindow(int position) {
        if (mLoader.size() > 0) {
            int start = Math.max(0, position - 2);
            int end = Math.min(getCount(), position + 2);
            mLoader.setActiveWindow(start, end);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (null != getCurrentMediaItem()) {
            if (ScreenUtils.isInSplitScreen(mContext)) {
                DragUtil.startDrag(getCurrentMediaItem(), view);
                return true;
            }
        }
        return false;
    }

}
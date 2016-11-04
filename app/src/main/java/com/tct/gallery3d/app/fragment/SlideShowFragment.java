/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.gallery3d.app.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AbstractGalleryFragment;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.SystemBarTintManager;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.data.PhotoDataLoader;
import com.tct.gallery3d.data.FilterDeleteSet;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.image.ImageWorker.OnImageLoadedListener;

import java.util.ArrayList;

public class SlideShowFragment extends GalleryFragment implements OnTouchListener { // MODIFIED by Yaoyu.Yang, 2016-07-14,BUG-2208330

    public static final String TAG = SlideShowFragment.class.getSimpleName();
    private static final int MSG_LOAD_NEXT_BITMAP = 1;
    private static final int MSG_SHOW_PENDING_BITMAP = 2;
    private static final int SLIDESHOW_TIME = 4100;
    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-22,BUG-2208330*/
    private static final int PREPARE_TIME = 1000;
    private static final int LAST_IMAGE = 1;
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
    private static final int MULTIPLE_LARGER_IMAGE = 2;
    private static SlideShowFragment sInstance;
    private AbstractGalleryActivity mContext;
    private LayoutInflater mInflater;
    private ViewGroup mSlideShowLayout;
    private FilterDeleteSet mMediaSet;
    private ViewFlipper mFlipper;
    private Handler mHandler;
    private PhotoDataLoader mLoader; // MODIFIED by Yaoyu.Yang, 2016-07-14,BUG-2208330
    private int mFromPageType = GalleryConstant.FROM_NONE_PAGE;
    private int mMediaSize;
    private int mCurrentPosition;
    private int mVideoSerialCount = 0; // MODIFIED by Yaoyu.Yang, 2016-07-22,BUG-2208330
    private View mDecorView = null;

    public static SlideShowFragment getInstance() {
        if (sInstance == null) {
            sInstance = new SlideShowFragment();
        }
        return sInstance;
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        mContext = (AbstractGalleryActivity) getGalleryContext();
        updateSystemUi(); // MODIFIED by Yaoyu.Yang, 2016-07-19,BUG-2208330
        initData();
        mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                case MSG_SHOW_PENDING_BITMAP:
                    showPendingBitmap();
                    break;
                case MSG_LOAD_NEXT_BITMAP:
                    mVideoSerialCount = 0; // MODIFIED by Yaoyu.Yang, 2016-07-22,BUG-2208330
                    loadNextBitmap(mCurrentPosition);
                    break;
                default:
                    throw new AssertionError();
                }
            }
        };
    }

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-19,BUG-2208330*/
    private void updateSystemUi() {
        mContext.initSystemBar(true);
        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-25,BUG-2208330*/
        mDecorView = mContext.getWindow().getDecorView();
        mContext.setStatusColor(SystemBarTintManager.COLOR_TRANSPARENT);
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    private void initData() {
        Bundle data = getArguments();
        if (null != data) {
            String setPath = data.getString(GalleryConstant.KEY_MEDIA_SET_PATH);
            setPath = "/filter/delete/{" + setPath + "}";
            mMediaSet = (FilterDeleteSet) mContext.getDataManager().getMediaSet(setPath);
            /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-14,BUG-2208330*/
            mLoader = new PhotoDataLoader(mContext, mMediaSet);
            mFromPageType = data.getInt(GalleryConstant.KEY_FROM_PAGE, GalleryConstant.FROM_NONE_PAGE);
            mCurrentPosition = data.getInt(GalleryConstant.KEY_INDEX_SLOT);
        }
        mMediaSize = mMediaSet.getMediaItemCount();
        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mInflater == null) {
            mInflater = inflater;
        }
        if (mSlideShowLayout == null) {
            mSlideShowLayout = (ViewGroup) inflater.inflate(R.layout.photo_slideshow, null);
        }
        initViews(mSlideShowLayout);
        return mSlideShowLayout;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateSystemUi();
        reLoadView();
    }

    private void reLoadView() {
        if (mFlipper != null) {
            if(mFlipper.getChildCount() > 0){
                mFlipper.removeViewAt(0);
            }
            if (mCurrentPosition > mVideoSerialCount)
                mCurrentPosition = mCurrentPosition - LAST_IMAGE - mVideoSerialCount;
            mVideoSerialCount = 0;
            mHandler.removeMessages(MSG_LOAD_NEXT_BITMAP);
            mHandler.sendEmptyMessageDelayed(MSG_LOAD_NEXT_BITMAP, PREPARE_TIME);
        }
    }
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
    @Override
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
    public void onResume() {
        super.onResume();
        mLoader.resume(); // MODIFIED by Yaoyu.Yang, 2016-07-18,BUG-2208330
        if (mFlipper.getChildCount() != 0 && mCurrentPosition < mMediaSize) {
            showPendingBitmap();
        } else {
            removeSlideShowFragment();
        }
    }

    private void loadNextBitmap(int currentIndex) {
        int childCount = mFlipper.getChildCount();
        if (childCount == 2) {
            mFlipper.removeViewAt(1);
        }
        loadView(currentIndex);
        if (0 != childCount) {
            mFlipper.setDisplayedChild(0);
        }
        mHandler.sendEmptyMessage(MSG_SHOW_PENDING_BITMAP);
    }

    private void showPendingBitmap() {
        if (mCurrentPosition + 1 < mMediaSize) {
            mCurrentPosition++;
        } else {
            mCurrentPosition = 0;
        }
        while (isValidPosition(mCurrentPosition)) {
            /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-02,BUG-2208330*/
            if (mCurrentPosition + 1 < mMediaSize) {
                mCurrentPosition++;
                mVideoSerialCount++;
            } else {
                mCurrentPosition = 0;
                mVideoSerialCount = 0;
            }
            /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
        }
        if (mFlipper.getChildCount() == 1) {
            mHandler.sendEmptyMessage(MSG_LOAD_NEXT_BITMAP);
        } else {
            mHandler.sendEmptyMessageDelayed(MSG_LOAD_NEXT_BITMAP, SLIDESHOW_TIME);
        }
    }

    @Override
    public void onPause() {
        mLoader.pause(); // MODIFIED by Yaoyu.Yang, 2016-07-18,BUG-2208330
        mFlipper.removeAllViews();
        mContext.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler.removeMessages(MSG_LOAD_NEXT_BITMAP);
        mHandler.removeMessages(MSG_SHOW_PENDING_BITMAP);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void reflashScreenView() {
        switch (mFromPageType) {
        case GalleryConstant.FROM_ALBUMSET_PAGE:
        case GalleryConstant.FROM_ALBUM_PAGE:
        case GalleryConstant.FROM_MOMENTS_PAGE:
        case GalleryConstant.FROM_PHOTODETAIL_PAGE:
            mContext.setNavigationEnable(true);
            break;
        default:
            break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.initSystemBar(false);
        Window window = mContext.getWindow();
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        reflashScreenView();
        setCurrentContent();
        mFromPageType = GalleryConstant.FROM_NONE_PAGE;
    }

    private void setCurrentContent() {
        AbstractGalleryFragment fragment = null;
        switch (mFromPageType) {
        case GalleryConstant.FROM_ALBUMSET_PAGE:
            fragment = (AlbumSetFragment) ((GalleryActivity) mContext)
                    .getCurrentContent(GalleryActivity.PAGE_ALBUMS);
            break;
        case GalleryConstant.FROM_ALBUM_PAGE:
            fragment = (AlbumFragment) getFragment(AlbumFragment.TAG);
            break;
        case GalleryConstant.FROM_MOMENTS_PAGE:
            fragment = (MomentsFragment) ((GalleryActivity) mContext).getCurrentContent(GalleryActivity.PAGE_MOMENTS);
            break;
        case GalleryConstant.FROM_PHOTODETAIL_PAGE:
            fragment = (PhotoFragment) getFragment(PhotoFragment.TAG);
            break;
        default:
            break;
        }
        mContext.setContent(fragment);
    }

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-18,BUG-2208330*/
    private MediaItem getCurrentMediaItem(int current) {
        MediaItem item = mLoader.get(current);
        if (item == null) {
            ArrayList<MediaItem> mediaItems = mMediaSet.getMediaItem(current, 1);
            if (mediaItems.size() == 0) {
                removeSlideShowFragment();
                return null;
            }
            item = mediaItems.get(0);
        }
        return item;
    }
    /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    private void initViews(View root) {
        if (mFlipper == null) {
            mFlipper = (ViewFlipper) root.findViewById(R.id.mViewFlipper);
        }
        mFlipper.setOnTouchListener(this);
        mFlipper.removeAllViews();
        mFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_null));
        mFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slideshow_anim));
        while (isValidPosition(mCurrentPosition)) {
            mCurrentPosition++;
        }
        loadView(mCurrentPosition);
    }

    private void loadView(int current) {
        if (mLoader.size() > 0) {
            int start = Math.max(0, current - 2);
            int end = Math.min(mMediaSize, current + 2);
            mLoader.setActiveWindow(start, end);
        }
        View view = mInflater.inflate(R.layout.slideshow_item_adapter, mFlipper, false);
        final ImageView slide = (ImageView) view.findViewById(R.id.id_slideShowAdapter);
        final MediaItem item = getCurrentMediaItem(current);
        if (item == null) {
            return;
        }
        loadThumbnail(item, slide, new OnImageLoadedListener() {
            @Override
            public void onImageLoaded(boolean success) {
                loadGlide(item, slide);
            }
        });
        mFlipper.addView(view, 0);

    }

    /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-22,BUG-2208330*/
    private boolean isValidPosition(int current) {
        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-14,BUG-2208330*/
        MediaItem item = getCurrentMediaItem(current);
        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/
        if (item == null) {
            return false;
        }
        int type = item.getMediaType();
        switch (type) {
        case MediaItem.MEDIA_TYPE_VIDEO:
            return true;
        default:
            break;
        }
        return false;
        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

    }

    private void removeSlideShowFragment() {
        FragmentManager fm = mContext.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(FragmentTransaction.TRANSIT_NONE, FragmentTransaction.TRANSIT_NONE);
        ft.remove(this);
        ft.commit();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        removeSlideShowFragment();
        return true;
    }

   @Override
    public boolean onBackPressed() {
        removeSlideShowFragment();
        return true;
    }

}

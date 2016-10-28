/* ----------|----------------------|----------------------|----------------- */
/* 11/25/2015| jian.pan1            | [ALM]Defect:979970   |[Android6.0][Gallery_v5.2.4.1.0315.0][Force Close]Gallery force close after tap category
/* ----------|----------------------|----------------------|----------------- */
/* 12/26/2015| jian.pan1            |[ALM]Defect:1048523   |[Gallery]There is no show list as Fyuse in moment of gallery
/* ----------|----------------------|----------------------|----------------- */
/* 01/04/2016| dongliang.feng       |ALM-1278603           |Custom Fyuse function of Gallery */
/* ----------|----------------------|----------------------|----------------- */
/* 01/26/2016| jian.pan1            |[ALM]Defect:1496305   |Gallery fyuse function control
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.app;

import com.tct.gallery3d.R;
import com.tct.gallery3d.data.MomentsAlbum;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.PLFUtils;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class MomentsPopupMenu implements OnClickListener {

    private AbstractGalleryActivity mActivity = null;
    private View mParentView = null;
    private OnPopupMenuSelectedListener mListener = null;

    private PopupWindow mPopupWindow = null;
    private LinearLayout mItemAll = null;
    private LinearLayout mItemPhoto = null;
    private LinearLayout mItemVideo = null;
    private ImageView mItemAllSelect = null;
    private ImageView mItemPhotoSelect = null;
    private ImageView mItemVideoSelect = null;

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-04, ALM-1278603 begin
    private boolean mFyuseEnable = true;
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-26,Defect:1048523
    private LinearLayout mItemFyuse = null;
    private ImageView mItemFyuseSelect = null;
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-04, ALM-1278603 end

    private int mStatusBarHeight = 0;
    private int mOffset = 0;
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-11-25,Defect:979970 begin
    public int mFilterMode = MomentsAlbum.FILTER_ALL;

    public boolean isListenerEquals(OnPopupMenuSelectedListener listener) {
        return listener.equals(mListener);
    }

    public void setListener(OnPopupMenuSelectedListener listener) {
        this.mListener = listener;
    }
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-11-25,Defect:979970 end

    private OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                mPopupWindow.dismiss();
            }
            return false;
        }
    };

    public interface OnPopupMenuSelectedListener {
        public void onPopupMenuSelected(int filterType);
    }

    public MomentsPopupMenu(AbstractGalleryActivity activity, OnPopupMenuSelectedListener listener) {
        mActivity = activity;
        mListener = listener;

        View menuView = mActivity.getLayoutInflater().inflate(R.layout.moments_popup_menu, null, true);
        menuView.setFocusableInTouchMode(true);
        mParentView = menuView;
        mPopupWindow = new PopupWindow(menuView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
        mPopupWindow.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.popup_bg));
        mPopupWindow.setFocusable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setTouchInterceptor(mTouchListener);
        mPopupWindow.setElevation(30);

        mItemAll = (LinearLayout)menuView.findViewById(R.id.actionbar_menu_all);
        mItemPhoto = (LinearLayout)menuView.findViewById(R.id.actionbar_menu_photo);
        mItemVideo = (LinearLayout)menuView.findViewById(R.id.actionbar_menu_video);
        mItemAll.setOnClickListener(this);
        mItemPhoto.setOnClickListener(this);
        mItemVideo.setOnClickListener(this);

        mItemAllSelect = (ImageView)menuView.findViewById(R.id.menu_all_select);
        mItemPhotoSelect = (ImageView)menuView.findViewById(R.id.menu_photo_select);
        mItemVideoSelect = (ImageView)menuView.findViewById(R.id.menu_video_select);
        mItemAllSelect.setVisibility(View.VISIBLE);
        mItemPhotoSelect.setVisibility(View.INVISIBLE);
        mItemVideoSelect.setVisibility(View.INVISIBLE);

        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-04, ALM-1278603 begin
        mFyuseEnable = PLFUtils.getBoolean(mActivity, "def_fyuse_enable");
        mItemFyuse = (LinearLayout)menuView.findViewById(R.id.actionbar_menu_fyuse);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-26,Defect:1496305 begin
        if (mFyuseEnable && GalleryUtils.hasFyusionApk(activity)) {
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-01-26,Defect:1496305 end
            mItemFyuse.setVisibility(View.VISIBLE);
            mItemFyuse.setOnClickListener(this);
            mItemFyuseSelect = (ImageView)menuView.findViewById(R.id.menu_fyuse_select);
            mItemFyuseSelect.setVisibility(View.INVISIBLE);
        } else {
            mItemFyuse.setVisibility(View.GONE);
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-04, ALM-1278603 end

        mStatusBarHeight = (int)mActivity.getResources().getDimension(R.dimen.status_bar_height);
        mOffset = (int)mActivity.getResources().getDimension(R.dimen.menu_offset);
    }

    public void show() {
        if (mPopupWindow != null) {
            mPopupWindow.showAtLocation(mParentView, Gravity.TOP | Gravity.RIGHT, mOffset, mStatusBarHeight+mOffset);
        }
    }

    public void closePopupMenu() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-11-25,Defect:979970 begin
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.actionbar_menu_all:
            mListener.onPopupMenuSelected(MomentsAlbum.FILTER_ALL);
            initSelectItem(mItemAllSelect);
            mFilterMode = MomentsAlbum.FILTER_ALL;
            break;
        case R.id.actionbar_menu_photo:
            mListener.onPopupMenuSelected(MomentsAlbum.FILTER_PHOTO);
            initSelectItem(mItemPhotoSelect);
            mFilterMode = MomentsAlbum.FILTER_PHOTO;
            break;
        case R.id.actionbar_menu_video:
            mListener.onPopupMenuSelected(MomentsAlbum.FILTER_VIDEO);
            initSelectItem(mItemVideoSelect);
            mFilterMode = MomentsAlbum.FILTER_VIDEO;
            break;
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-26,Defect:1048523 begin
        case R.id.actionbar_menu_fyuse:
            mListener.onPopupMenuSelected(MomentsAlbum.FILTER_FYUSE);
            initSelectItem(mItemFyuseSelect);
            mFilterMode = MomentsAlbum.FILTER_FYUSE;
            break;
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-26,Defect:1048523 end

        default:
            break;
        }
        closePopupMenu();
    }

    private void initSelectItem(ImageView imageView) {
        mItemAllSelect.setVisibility(View.INVISIBLE);
        mItemPhotoSelect.setVisibility(View.INVISIBLE);
        mItemVideoSelect.setVisibility(View.INVISIBLE);
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-04, ALM-1278603 begin
        if (mFyuseEnable && GalleryUtils.hasFyusionApk(mActivity)) {
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-26,Defect:1048523
            mItemFyuseSelect.setVisibility(View.INVISIBLE);
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-04, ALM-1278603 end
        imageView.setVisibility(View.VISIBLE);
    }

    public int getCurrentFilterMode() {
        return mFilterMode;
    }
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-11-25,Defect:979970 end

    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-3,ALM-1539291 begin
    public void resetFilter() {
        mListener.onPopupMenuSelected(MomentsAlbum.FILTER_ALL);
        initSelectItem(mItemAllSelect);
        mFilterMode = MomentsAlbum.FILTER_ALL;
    }
    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-3,ALM-1539291 end
}

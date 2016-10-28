package com.tct.gallery3d.app.section;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.adapter.CursorRecyclerAdapter;
import com.tct.gallery3d.app.data.MomentsLoader;
import com.tct.gallery3d.app.fragment.GalleryFragment;
import com.tct.gallery3d.data.MomentsNewAlbum;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.util.GalleryUtils;

public class MomentsCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = MomentsCallbacks.class.getSimpleName();
    public static final int LOADER_PHOTO = 1;

    private GalleryFragment mContext;
    private CursorRecyclerAdapter mAdapter;
    private MomentsLoader mMomentsLoader;

    public MomentsCallbacks(GalleryFragment context, CursorRecyclerAdapter adapter) {
        mContext = context;
        mAdapter = adapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        Uri uri = MediaStore.Files.getContentUri(MomentsNewAlbum.VOLUME_NAME);
        // Video Projection and ItemPath
        String[] projection = GalleryUtils.getProjection();

        String selectionDrm = "";
        if (DrmManager.isDrmEnable && GalleryActivity.TV_LINK_DRM_HIDE_FLAG) {
            selectionDrm = " AND (" + DrmManager.TCT_IS_DRM + "=0 OR " + DrmManager.TCT_IS_DRM + " IS NULL)";
        }
        String selection = MomentsNewAlbum.SELECTION + selectionDrm;
        String[] selectionArgs = null;
        String sort = MomentsNewAlbum.SORT_ORDER_DATE;
        mMomentsLoader = new MomentsLoader(mContext, uri, projection, selection, selectionArgs, sort);
        return mMomentsLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
//        mContext.setViewItemVisibility();
//        mContext.setTempScreenSpanCount();
//        mContext.resumeWork();
        mAdapter.swapCursor(data);
        if (data == null || data.getCount() == 0) {
            mContext.showEmptyView(true);
        } else {
            mContext.showEmptyView(false);
        }
        mContext.onLoadFinished();

//        mAdapter.setSpanList();
//        mAdapter.setState(mContext.mState);
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
        mAdapter.swapCursor(null);
        if (mMomentsLoader != null) {
            mMomentsLoader.release();
        }
    }
}

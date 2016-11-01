package com.tct.gallery3d.app.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.fragment.GalleryFragment;
import com.tct.gallery3d.app.fragment.MomentsFragment;
import com.tct.gallery3d.app.section.DaySection;
import com.tct.gallery3d.app.section.MonthSection;
import com.tct.gallery3d.data.GappTypeInfo;
import com.tct.gallery3d.data.MomentsNewAlbum;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.util.GalleryUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.tct.gallery3d.app.constant.GalleryConstant;

public class MomentsLoader extends CursorLoader {

    public static final String TAG = MomentsLoader.class.getSimpleName();
    private GalleryFragment mFragment;
    private List<Long> mHidden = new ArrayList<>();
    private String mSelection = "";
    private boolean mIsOperation = false;

    @Override
    public void onContentChanged() {
        if (mIsOperation) {
        } else {
            super.onContentChanged();
        }
    }

    /**
     * add to know the operation progress
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action------------" + action);
            if (GalleryUtils.INTENT_OPERATION_START.equals(action)) {
                mIsOperation = true;
            } else if (GalleryUtils.INTENT_OPERATION_FINISH.equals(action)) {
                mIsOperation = false;
                onContentChanged();
            }
        }
    };

    /**
     * add to unregister the receiver
     */
    public void release() {
        mFragment.getActivity().unregisterReceiver(mReceiver);
    }

    public MomentsLoader(GalleryFragment context, Uri uri, String[] projection, String selection, String[] selectionArgs,
                         String sortOrder) {
        this(context.getContext(), uri, projection, selection, selectionArgs, sortOrder);
        IntentFilter filter = new IntentFilter();
        filter.addAction(GalleryUtils.INTENT_OPERATION_START);
        filter.addAction(GalleryUtils.INTENT_OPERATION_FINISH);
        context.getActivity().registerReceiver(mReceiver, filter);
        mFragment = context;
    }

    public MomentsLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
        mSelection = selection;
    }

    @Override
    public Cursor loadInBackground() {
        Log.d(TAG, "MomentsLoader -- loadInBackground");
        setSelection(mSelection);
        Cursor cursor;
        try {
            cursor = super.loadInBackground();
        } catch (Exception e) {
            if (GalleryAppImpl.sHasPrivateColumn) {
                if (GalleryAppImpl.sHasNewColumn) {
                    setProjection(MomentsNewAlbum.PRIVATE_NEW_PROJECTION);
                } else {
                    setProjection(MomentsNewAlbum.PRIVATE_PROJECTION);
                }
            } else {
                if (GalleryAppImpl.sHasNewColumn) {
                    setProjection(MomentsNewAlbum.NEW_PROJECTION);
                } else {
                    setProjection(MomentsNewAlbum.PROJECTION);
                }
            }
            cursor = super.loadInBackground();
        }

        // Hidden the burst shot
        if (cursor != null) {
            mHidden.clear();
            String collapsePath = getCollapsePath();
            try {
                while (cursor.moveToNext()) {
                    buildHidden(cursor, collapsePath);
                }
            } catch (Exception e) {
                return null;
            }
            ExifInfoFilter.getInstance(getContext()).notifySaveExifCache();
            if (mHidden.size() > 0) {
                StringBuilder build = new StringBuilder(mSelection);
                build.append(" and " + MediaStore.Files.FileColumns._ID + " not in (");
                for (Long id : mHidden) {
                    build.append(id + ",");
                }
                build.setCharAt(build.length() - 1, ')');
                setSelection(build.toString());
                cursor.close();
                cursor = super.loadInBackground();
            }
        }

        if (cursor != null) {
            switch (mFragment.mState) {
                case MONTH:
                    cursor = new MonthSection(getContext(), cursor);
                    break;
                case DAY:
                case STAGGERED:
                    cursor = new DaySection(getContext(), cursor);
                    break;
            }
        }
        return cursor;
    }

    private void buildHidden(Cursor cursor, String collapsePath) {
        long fileId = cursor.getLong(MomentsNewAlbum.INDEX_ID);
        int mediaType = cursor.getInt(MomentsNewAlbum.INDEX_MEDIA_TYPE);
        String path = cursor.getString(MomentsNewAlbum.INDEX_DATA);
        String bucketId = cursor.getString(MomentsNewAlbum.INDEX_BUCKET_ID);
        long timestamp = cursor.getLong(MomentsNewAlbum.INDEX_DATE_TAKEN);
        int type = ExifInfoFilter.getInstance(getContext()).queryType(String.valueOf(fileId));
        if (type == ExifInfoFilter.NONE) {
            if (GalleryAppImpl.sHasNewColumn) {
                int gappMediaTypeIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_MEDIA_TYPE);
                int burstIdIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_ID);
                int burstIndexIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_INDEX);
                GappTypeInfo gappTypeInfo = new GappTypeInfo();
                gappTypeInfo.setType(cursor.getInt(gappMediaTypeIndex));
                gappTypeInfo.setBurstshotId(cursor.getInt(burstIdIndex));
                gappTypeInfo.setBurstshotIndex(cursor.getInt(burstIndexIndex));
                type = ExifInfoFilter.getInstance(getContext()).filter(String.valueOf(fileId), path, mediaType, timestamp, true, false, gappTypeInfo);
            } else {
                type = ExifInfoFilter.getInstance(getContext()).filter(String.valueOf(fileId), path, mediaType, timestamp, true, false, null);
            }
        }
        if (type == ExifInfoFilter.BURSTSHOTSHIDDEN || (collapsePath != null && collapsePath.contains(bucketId))) {
            mHidden.add(fileId);
        }
    }

    private String getCollapsePath() {
        SharedPreferences sharedPreferences = GalleryAppImpl.getInstance().getSharedPreferences(GalleryConstant.COLLAPSE_DATA_NAME, Context.MODE_PRIVATE);
        Iterator iter = sharedPreferences.getAll().entrySet().iterator();
        String val = null;
        StringBuilder build = new StringBuilder();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            val = (String) entry.getKey();
            if (val != null) {
                build.append("," + GalleryUtils.getBucketId(val));
            }
        }
        return build.toString();
    }
}
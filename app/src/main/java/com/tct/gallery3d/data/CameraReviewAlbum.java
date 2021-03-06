package com.tct.gallery3d.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorWindowAllocationException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;

import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.MediaSetUtils;

public class CameraReviewAlbum extends MediaSet {
    private static final String TAG = "CameraReviewAlbum";
    private static final String EXTERNAL_MEDIA = "external";

    private static final int INVALID_COUNT = -1;

    private final String mWhereClause;
    private final String mOrderClause;
    private final Uri mBaseUri;
    private String[] mImageProjection;
    private String[] mVideoProjection;

    private final GalleryApp mApplication;
    private final ContentResolver mResolver;

    private final ChangeNotifier mNotifier;
    private final Path mImageItemPath;
    private final Path mVideoItemPath;
    private int mCachedCount = INVALID_COUNT;
    // key:uri value:_id
    private HashMap<Uri, String> mUriInfoMap = null;
    private ArrayList<String> mImageList = new ArrayList<String>();
    private ArrayList<String> mVideoList = new ArrayList<String>();

    public CameraReviewAlbum(Path path, GalleryApp application) {
        super(path, nextVersionNumber());
        // mUriInfoMap = uriInfoMap;
        mApplication = application;
        mResolver = application.getContentResolver();

        mWhereClause = FileColumns._ID + " in ";
        mOrderClause = FileColumns.DATE_MODIFIED + " DESC, " + FileColumns._ID + " DESC";
        mBaseUri = Files.getContentUri(EXTERNAL_MEDIA);

        // Image Projection and ItemPath
        mImageProjection = LocalImage.getImageProjection();

        mImageItemPath = LocalImage.ITEM_PATH;

        mVideoProjection = LocalVideo.getVideoProjection();

        mVideoItemPath = LocalVideo.ITEM_PATH;

        mNotifier = new ChangeNotifier(this, mBaseUri, application);
    }

    @Override
    public boolean isCameraRoll() {
        return super.isCameraRoll();
    }

    @Override
    public Uri getContentUri() {
        return Files.getContentUri(EXTERNAL_MEDIA);
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        DataManager dataManager = mApplication.getDataManager();
        ArrayList<MediaItem> list = new ArrayList<MediaItem>();
        GalleryUtils.assertNotInRenderThread();

        mUriInfoMap = dataManager.getUriInfoMap();
        initTypeList(mUriInfoMap);
        if (mImageList != null && !mImageList.isEmpty()) {
            list.addAll(getMediaItemList(dataManager, mImageList, true));
        }
        if (mVideoList != null && !mVideoList.isEmpty()) {
            list.addAll(getMediaItemList(dataManager, mVideoList, false));
        }
        Collections.sort(list, comparator);
        if (start >= list.size()) {
            return new ArrayList<MediaItem>();
        } else if (start + count > list.size()) {
            return MediaSetUtils.subList(list, start, list.size());
        } else {
            return MediaSetUtils.subList(list, start, start + count);
        }
    }

    Comparator<MediaItem> comparator = new Comparator<MediaItem>() {

        @Override
        public int compare(MediaItem lhs, MediaItem rhs) {
            return -Utils.compare(lhs.getDateModifiedInMs(), rhs.getDateModifiedInMs());
        }
    };

    private ArrayList<MediaItem> getMediaItemList(DataManager dataManager,
                                                  ArrayList<String> idList, boolean isImage) {
        ArrayList<MediaItem> list = new ArrayList<MediaItem>();
        StringBuffer IdArray = GalleryUtils.buildStringByList(idList);
        Cursor cursor = queryCameraReview(mBaseUri, IdArray, isImage);
        if (cursor == null) {
            Log.w(TAG, "query fail: " + mBaseUri);
            return list;
        }
        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                Path childPath = null;
                if (isImage) {
                    childPath = mImageItemPath.getChild(id);
                } else {
                    childPath = mVideoItemPath.getChild(id);
                }
                MediaItem item = loadOrUpdateItem(childPath, cursor, dataManager, mApplication,
                        isImage);
                list.add(item);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private void initTypeList(HashMap<Uri, String> map) {
        if (map != null && map.size() > 0) {
            mImageList.clear();
            mVideoList.clear();
            Set<Uri> uriKeys = map.keySet();
            for (Uri uri : uriKeys) {
                String type = mResolver.getType(uri);
                if (type == null)
                    type = "image/*";
                if (type.startsWith("image/")) {
                    mImageList.add(map.get(uri));
                } else if (type.startsWith("video/")) {
                    mVideoList.add(map.get(uri));
                }
            }
        }
    }

    private Cursor queryCameraReview(Uri uri, StringBuffer idBuffer, boolean isImage) {
        Cursor cursor = null;
        String[] projection = null;
        if (isImage) {
            projection = mImageProjection;
        } else {
            projection = mVideoProjection;
        }
        try {
            cursor = mResolver.query(uri, projection, mWhereClause + idBuffer.toString(), null,
                    mOrderClause);
        } catch (SQLiteException e) {
            e.printStackTrace();
            if (GalleryAppImpl.sHasPrivateColumn) {
                projection = LocalImage.PRIVATE_PROJECTION;
            } else {
                projection = LocalImage.PROJECTION;
            }

            cursor = mResolver.query(uri, projection, mWhereClause + idBuffer.toString(), null,
                    mOrderClause);
        } catch (CursorWindowAllocationException e) {
            e.printStackTrace();
        }
        return cursor;
    }

    private int queryCameraReviewCount() {
        mUriInfoMap = mApplication.getDataManager().getUriInfoMap();
        initTypeList(mUriInfoMap);
        int count = 0;
        if (mImageList != null && !mImageList.isEmpty()) {
            count += getReviewCount(mImageList, true);
        }
        if (mVideoList != null && !mVideoList.isEmpty()) {
            count += getReviewCount(mVideoList, false);
        }
        Log.i(TAG, "###queryCameraReviewCount count :" + count);
        return count;
    }

    private int getReviewCount(ArrayList<String> idList, boolean isImage) {
        int count = 0;
        StringBuffer IdArray = GalleryUtils.buildStringByList(idList);
        Cursor cursor = queryCameraReview(mBaseUri, IdArray, isImage);
        if (cursor != null && cursor.getCount() > 0) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }
        return count;
    }

    @Override
    public int getMediaSetType() {
        int mMediaSetType = MediaSet.MEDIASET_TYPE_UNKNOWN;
        List<MediaItem> items = this.getMediaItem(0, this.getMediaItemCount());
        for (MediaObject mediaObject : items) {
            mMediaSetType |= mediaObject.getMediaType();
        }
        return mMediaSetType;
    }

    private static MediaItem loadOrUpdateItem(Path path, Cursor cursor, DataManager dataManager,
                                              GalleryApp app, boolean isImage) {
        synchronized (DataManager.LOCK) {
            LocalMediaItem item = (LocalMediaItem) dataManager.peekMediaObject(path);
            if (item == null) {
                if (isImage) {
                    item = new LocalImage(path, app, cursor);
                } else {
                    item = new LocalVideo(path, app, cursor);
                }
            } else {
                item.updateContent(cursor);
            }
            return item;
        }
    }

    @Override
    public int getMediaItemCount() {
        if (mApplication.getDataManager().getCameraReviewInit()) {
            mCachedCount = INVALID_COUNT;
            mApplication.getDataManager().setCameraReviewInit(false);
        }
        if (mCachedCount == INVALID_COUNT) {
            mCachedCount = queryCameraReviewCount();
        }
        return mCachedCount;
    }

    @Override
    public String getName() {
        return "Camera Review";
    }

    @Override
    public long reload() {
        if (mNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            mCachedCount = INVALID_COUNT;
        }
        return mDataVersion;
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_DELETE;
    }

    @Override
    public void delete() {
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

}

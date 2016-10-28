package com.tct.gallery3d.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorWindowAllocationException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.util.GalleryUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.tct.gallery3d.app.constant.GalleryConstant;

public class MomentsNewAlbum extends MediaSet {

    public static final String TAG = MomentsNewAlbum.class.getSimpleName();

    private final GalleryApp mApplication;
    private final ContentResolver mResolver;

    private final ChangeNotifier mImageNotifier;
    private final ChangeNotifier mVideoNotifier;
    private final ParallaxObserver mParallaxObserver;

    public static final String VOLUME_NAME = "external";

    public static final int INDEX_ID = 0;
    public static final int INDEX_CAPTION = 1;
    public static final int INDEX_MIME_TYPE = 2;
    public static final int INDEX_MEDIA_TYPE = 3;
    public static final int INDEX_DATA = 4;
    public static final int INDEX_DATE_ADDED = 5;
    public static final int INDEX_DATE_MODIFIED = 6;
    public static final int INDEX_LATITUDE = 7;
    public static final int INDEX_LONGITUDE = 8;
    public static final int INDEX_DATE_TAKEN = 9;
    public static final int INDEX_WIDTH = 10;
    public static final int INDEX_HEIGHT = 11;
    public static final int INDEX_ORIENTATION = 12;
    public static final int INDEX_BUCKET_ID = 13;
    public static final int INDEX_SIZE = 14;
    public static final int INDEX_RESOLUTION = 15;
    public static final int INDEX_DURATION = 16;
    public static final int INDEX_IS_PRIVATE = 17;
    public static final int INDEX_IS_DRM = 18;
    public static final int INDEX_DRM_TYPE = 19;
    public static final int INDEX_DRM_RIGHT_TYPE = 20;
    public static final int INDEX_DRM_VALID = 21;
    public static final int INDEX_DRM_METHOD = 22;

    public static final String[] NEW_PROJECTION = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.NO_COLUMN,                      // 17
//            DrmManager.TCT_IS_DRM,                        // 18
//            DrmManager.TCT_DRM_TYPE,                      // 19
//            DrmManager.TCT_DRM_RIGHT_TYPE,                // 20
//            DrmManager.TCT_DRM_VALID,                     // 21
//            DrmManager.TCT_DRM_METHOD,                    // 22
            GappTypeInfo.GAPP_MEDIA_TYPE,
            GappTypeInfo.GAPP_BURST_ID,
            GappTypeInfo.GAPP_BURST_INDEX
    };

    public static final String[] NEW_PROJECTION_DRM = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.NO_COLUMN,                      // 17
            DrmManager.TCT_IS_DRM,                          // 18
            DrmManager.TCT_DRM_TYPE,                        // 19
            DrmManager.TCT_DRM_RIGHT_TYPE,                  // 20
            DrmManager.TCT_DRM_VALID,                       // 21
//            DrmManager.TCT_DRM_METHOD,                    // 22
            GappTypeInfo.GAPP_MEDIA_TYPE,
            GappTypeInfo.GAPP_BURST_ID,
            GappTypeInfo.GAPP_BURST_INDEX
    };

    public static final String[] NEW_PROJECTION_MTK = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.NO_COLUMN,                      // 17
            DrmManager.TCT_IS_DRM,                          // 18
            DrmManager.TCT_DRM_TYPE,                        // 19
            DrmManager.TCT_DRM_RIGHT_TYPE,                  // 20
            DrmManager.TCT_DRM_VALID,                       // 21
            DrmManager.TCT_DRM_METHOD,                      // 22
            GappTypeInfo.GAPP_MEDIA_TYPE,
            GappTypeInfo.GAPP_BURST_ID,
            GappTypeInfo.GAPP_BURST_INDEX
    };

    public static final String[] PROJECTION = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.NO_COLUMN,                      // 17
//            DrmManager.TCT_IS_DRM,                        // 18
//            DrmManager.TCT_DRM_TYPE,                      // 19
//            DrmManager.TCT_DRM_RIGHT_TYPE,                // 20
//            DrmManager.TCT_DRM_VALID,                     // 21
//            DrmManager.TCT_DRM_METHOD,                    // 22
    };

    public static final String[] PROJECTION_DRM = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.NO_COLUMN,                      // 17
            DrmManager.TCT_IS_DRM,                          // 18
            DrmManager.TCT_DRM_TYPE,                        // 19
            DrmManager.TCT_DRM_RIGHT_TYPE,                  // 20
            DrmManager.TCT_DRM_VALID,                       // 21
//            DrmManager.TCT_DRM_METHOD,                    // 22

    };

    public static final String[] PROJECTION_MTK = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.NO_COLUMN,                      // 17
            DrmManager.TCT_IS_DRM,                          // 18
            DrmManager.TCT_DRM_TYPE,                        // 19
            DrmManager.TCT_DRM_RIGHT_TYPE,                  // 20
            DrmManager.TCT_DRM_VALID,                       // 21
            DrmManager.TCT_DRM_METHOD,                      // 22
    };

    public static final String[] PRIVATE_NEW_PROJECTION = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.IS_PRIVATE,                     // 17
//            DrmManager.TCT_IS_DRM,                        // 18
//            DrmManager.TCT_DRM_TYPE,                      // 19
//            DrmManager.TCT_DRM_RIGHT_TYPE,                // 20
//            DrmManager.TCT_DRM_VALID,                     // 21
//            DrmManager.TCT_DRM_METHOD,                    // 22
            GappTypeInfo.GAPP_MEDIA_TYPE,
            GappTypeInfo.GAPP_BURST_ID,
            GappTypeInfo.GAPP_BURST_INDEX
    };

    public static final String[] PRIVATE_NEW_PROJECTION_DRM = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.IS_PRIVATE,                     // 17
            DrmManager.TCT_IS_DRM,                          // 18
            DrmManager.TCT_DRM_TYPE,                        // 19
            DrmManager.TCT_DRM_RIGHT_TYPE,                  // 20
            DrmManager.TCT_DRM_VALID,                       // 21
//            DrmManager.TCT_DRM_METHOD,                    // 22
            GappTypeInfo.GAPP_MEDIA_TYPE,
            GappTypeInfo.GAPP_BURST_ID,
            GappTypeInfo.GAPP_BURST_INDEX
    };

    public static final String[] PRIVATE_NEW_PROJECTION_MTK = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.IS_PRIVATE,                     // 17
            DrmManager.TCT_IS_DRM,                          // 18
            DrmManager.TCT_DRM_TYPE,                        // 19
            DrmManager.TCT_DRM_RIGHT_TYPE,                  // 20
            DrmManager.TCT_DRM_VALID,                       // 21
            DrmManager.TCT_DRM_METHOD,                      // 22
            GappTypeInfo.GAPP_MEDIA_TYPE,
            GappTypeInfo.GAPP_BURST_ID,
            GappTypeInfo.GAPP_BURST_INDEX
    };

    public static final String[] PRIVATE_PROJECTION = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.IS_PRIVATE,                     // 17
//            DrmManager.TCT_IS_DRM,                        // 18
//            DrmManager.TCT_DRM_TYPE,                      // 19
//            DrmManager.TCT_DRM_RIGHT_TYPE,                // 20
//            DrmManager.TCT_DRM_VALID,                     // 21
//            DrmManager.TCT_DRM_METHOD,                    // 22
    };

    public static final String[] PRIVATE_PROJECTION_DRM = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.IS_PRIVATE,                     // 17
            DrmManager.TCT_IS_DRM,                          // 18
            DrmManager.TCT_DRM_TYPE,                        // 19
            DrmManager.TCT_DRM_RIGHT_TYPE,                  // 20
            DrmManager.TCT_DRM_VALID,                       // 21
//            DrmManager.TCT_DRM_METHOD,                    // 22

    };

    public static final String[] PRIVATE_PROJECTION_MTK = {
            BaseColumns._ID,                                // 0
            MediaStore.Files.FileColumns.TITLE,             // 1
            MediaStore.Files.FileColumns.MIME_TYPE,         // 2
            MediaStore.Files.FileColumns.MEDIA_TYPE,        // 3
            MediaStore.Files.FileColumns.DATA,              // 4
            MediaStore.Files.FileColumns.DATE_ADDED,        // 5
            MediaStore.Files.FileColumns.DATE_MODIFIED,     // 6
            MediaStore.Images.ImageColumns.LATITUDE,        // 7
            MediaStore.Images.ImageColumns.LONGITUDE,       // 8
            MediaStore.Images.ImageColumns.DATE_TAKEN,      // 9
            MediaStore.Images.ImageColumns.WIDTH,           // 10
            MediaStore.Images.ImageColumns.HEIGHT,          // 11
            MediaStore.Images.ImageColumns.ORIENTATION,     // 12
            MediaStore.Images.ImageColumns.BUCKET_ID,       // 13
            MediaStore.Images.ImageColumns.SIZE,            // 14
            MediaStore.Video.VideoColumns.RESOLUTION,       // 15
            MediaStore.Video.VideoColumns.DURATION,         // 16
            GalleryConstant.IS_PRIVATE,                     // 17
            DrmManager.TCT_IS_DRM,                          // 18
            DrmManager.TCT_DRM_TYPE,                        // 19
            DrmManager.TCT_DRM_RIGHT_TYPE,                  // 20
            DrmManager.TCT_DRM_VALID,                       // 21
            DrmManager.TCT_DRM_METHOD,                      // 22
    };

    // exclude media files, they would be here also.
    public static final String SELECTION =
            "( " +
                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR " +
                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + " )";

    public static final String SORT_ORDER_DATE = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC, "
            + MediaStore.Images.ImageColumns._ID + " DESC";

    private static final int INVALID_COUNT = -1;

    private final Uri mBaseUri;
    private final String[] mProjection;
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final String mSortOrder;

    private final Path mItemPath;
    private final List<ContentChangeListener> mListeners;
    private int mCachedCount = INVALID_COUNT;
    private int mMediaDisplayCount = 0;

    public MomentsNewAlbum(Path path, GalleryApp application) {
        super(path, nextVersionNumber());
        mItemPath = path;
        mApplication = application;
        mResolver = application.getContentResolver();
        mListeners = new ArrayList<>();

        mImageNotifier = new ChangeNotifier(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, application) {
            @Override
            protected void onChange(boolean selfChange) {
                super.onChange(selfChange);
                notifyContentListener();
            }
        };
        mVideoNotifier = new ChangeNotifier(this, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, application) {
            @Override
            protected void onChange(boolean selfChange) {
                super.onChange(selfChange);
                notifyContentListener();
            }
        };
        mParallaxObserver = new ParallaxObserver();
        mApplication.getDataManager().registerParallaxListener(mParallaxObserver);


        mBaseUri = MediaStore.Files.getContentUri(VOLUME_NAME);

        mProjection = GalleryUtils.getProjection();

        mSelection = SELECTION;
        mSelectionArgs = null;
        mSortOrder = SORT_ORDER_DATE;
    }


    @Override
    public String getName() {
        return "MomentsAlbum";
    }

    @Override
    public long reload() {
        if (mImageNotifier.isDirty() || mVideoNotifier.isDirty() || mParallaxObserver.isDirty) {
            mDataVersion = nextVersionNumber();
            mCachedCount = INVALID_COUNT;
        }
        return mDataVersion;
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        DataManager dataManager = mApplication.getDataManager();
        ArrayList<MediaItem> list = new ArrayList<>();
        GalleryUtils.assertNotInRenderThread();
        Uri uri = mBaseUri;
        String[] projection = mProjection;
        String selection = mSelection;
        String[] selectionArgs = mSelectionArgs;
        String sortOrder = mSortOrder;

        Cursor cursor = queryMediaStore(uri, projection, selection, selectionArgs, sortOrder);
        if (cursor == null) {
            Log.w(TAG, "query fail: " + uri);
            return list;
        }

        try {
            int offset = 0;
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int type = ExifInfoFilter.getInstance(mApplication.getAndroidContext()).queryType(String.valueOf(id));
                if (isNeedHideBurstShot(type)) {
                    continue;
                } else {
                    if (offset >= start) {
                        Path childPath = mItemPath.getChild(id);
                        MediaItem item = loadOrUpdateItem(childPath, cursor, dataManager, mApplication);
                        list.add(item);
                        if (list.size() >= count) {
                            break;
                        }
                    } else {
                        offset++;
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    @Override
    public int getMediaSetType() {
        if (getMediaItemCount() > 0) {
            Cursor cursor = null;
            try {
                Uri uri = mBaseUri;
                String[] projection = mProjection;
                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                String[] selectionArgs = mSelectionArgs;
                String sortOrder = mSortOrder;
                cursor = queryMediaStore(uri, projection, selection, selectionArgs, sortOrder);

                if (cursor != null && cursor.getCount() > 0) {
                    mMediaSetType = MEDIASET_TYPE_IMAGE;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != cursor) {
                    cursor.close();
                }
            }
        } else {
            mMediaSetType = MEDIASET_TYPE_UNKNOWN;
        }
        return mMediaSetType;
    }

    @Override
    public int getMediaItemCount() {
        ExifInfoFilter filter = ExifInfoFilter.getInstance(mApplication.getAndroidContext());
        int hiddenCount = 0;
        if (mCachedCount == INVALID_COUNT) {
            Cursor cursor = null;
            Uri uri = mBaseUri;
            String[] projection = mProjection;
            String selection = mSelection;
            String[] selectionArgs = mSelectionArgs;
            String sortOrder = mSortOrder;
            try {
                cursor = queryMediaStore(uri, projection, selection, selectionArgs, sortOrder);
                if (cursor == null) {
                    Log.w(TAG, "getMediaItemCount query fail");
                    return 0;
                }
                while (cursor.moveToNext()) {
                    int idIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
                    long id = cursor.getLong(idIndex);
                    int type = filter.queryType(String.valueOf(id));
                    if (type == ExifInfoFilter.NONE) {
                        String path = cursor.getString(INDEX_DATA);
//                        long timestamp = cursor.getLong(INDEX_DATE_TAKEN) * 1000;
                        long timestamp = cursor.getLong(INDEX_DATE_TAKEN);
                        int mediaType = cursor.getInt(INDEX_MEDIA_TYPE);
                        if (GalleryAppImpl.sHasNewColumn) {
                            int gappMediaTypeIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_MEDIA_TYPE);
                            int burstIdIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_ID);
                            int burstIndexIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_INDEX);
                            GappTypeInfo gappTypeInfo = new GappTypeInfo();
                            gappTypeInfo.setType(cursor.getInt(gappMediaTypeIndex));
                            gappTypeInfo.setBurstshotId(cursor.getInt(burstIdIndex));
                            gappTypeInfo.setBurstshotIndex(cursor.getInt(burstIndexIndex));
                            type = filter.filter(String.valueOf(id), path, mediaType, timestamp, true, false, gappTypeInfo);
                        } else {
                            type = filter.filter(String.valueOf(id), path, mediaType, timestamp, true, false, null);
                        }

                    }
                    if (isNeedHideBurstShot(type)) {
                        hiddenCount++;
                    }
                }
            } finally {
                filter.notifySaveExifCache();
                mCachedCount = cursor.getCount();
                if (cursor != null) {
                    cursor.close();
                }
                mMediaDisplayCount = mCachedCount - hiddenCount;
            }
        }
        return mMediaDisplayCount;
    }

    @Override
    public int getTotalMediaItemCount() {
        return getMediaItemCount();
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_INFO;
    }

    public class ParallaxObserver implements DataManager.ParallaxSourceListener {
        public boolean isDirty = false;

        @Override
        public void onParallaxChanged() {
            isDirty = true;
            notifyContentListener();
        }
    }

    private boolean isNeedHideBurstShot(int type) {
        return type == ExifInfoFilter.BURSTSHOTSHIDDEN;
    }

    private static MediaItem loadOrUpdateItem(Path path, Cursor cursor, DataManager dataManager, GalleryApp app) {
        int mediaType = cursor.getInt(INDEX_MEDIA_TYPE);
        boolean isImage = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE == mediaType;
        synchronized (DataManager.LOCK) {
            LocalMediaItem item = (LocalMediaItem) dataManager.peekMediaObject(path);
            if (item == null) {
                if (isImage) {
                    item = new LocalImage(path, app, cursor, true);
                } else {
                    item = new LocalVideo(path, app, cursor, true);
                }
            } else {
                item.updateContent(cursor, true);
            }
            return item;
        }
    }

    private Cursor queryMediaStore(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SharedPreferences sharedPreferences = mApplication.getAndroidContext().getSharedPreferences(GalleryConstant.COLLAPSE_DATA_NAME, Context.MODE_PRIVATE);
        Map<String, ?> map = sharedPreferences.getAll();
        if (map.size() > 0) {
            Iterator iter = map.entrySet().iterator();
            StringBuffer selectionBuffer = new StringBuffer();
            String albumPath;
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                albumPath = (String) entry.getKey();
                if (albumPath != null) {
                    if (iter.hasNext()) {
                        selectionBuffer.append(GalleryUtils.getBucketId(albumPath) + ",");
                    } else {
                        selectionBuffer.append(GalleryUtils.getBucketId(albumPath));
                    }
                }
            }
            selection = selection + " and " + MediaStore.Images.ImageColumns.BUCKET_ID + " NOT IN ( " + selectionBuffer.toString() + " )";
        }
        try {
            cursor = mResolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (SQLiteException e) {
            e.printStackTrace();
            if (GalleryAppImpl.sHasPrivateColumn) {
                projection = PRIVATE_PROJECTION;
            } else {
                projection = PROJECTION;
            }
            cursor = mResolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (CursorWindowAllocationException e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public interface ContentChangeListener {
        void onContentChange();
    }

    public void addContentChangeListener(ContentChangeListener listener) {
        mListeners.add(listener);
    }

    public void removeContentChangeListener(ContentChangeListener listener) {
        mListeners.remove(listener);
    }

    public void clearContentChangeListener() {
        mListeners.clear();
    }

    private void notifyContentListener() {
        for (ContentChangeListener listener : mListeners) {
            listener.onContentChange();
        }
    }
}

package com.tct.gallery3d.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindowAllocationException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.util.GalleryUtils;

import java.util.ArrayList;
import java.util.List;

public class PrivateAlbum extends MediaSet {

    public static final String TAG = PrivateAlbum.class.getSimpleName();

    private final GalleryApp mApplication;
    private final ContentResolver mResolver;

    private final ChangeNotifier mImageNotifier;
    private final ChangeNotifier mVideoNotifier;

    public static final String VOLUME_NAME = "external";


    // exclude media files, they would be here also.
    public static final String SELECTION =
            "( " + "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR " +
                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + " )" + " AND " +
                    GalleryConstant.IS_PRIVATE + "=" + GalleryConstant.PRIVATE_ITEM + " )";

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

    public PrivateAlbum(Path path, GalleryApp application) {
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

        mBaseUri = MediaStore.Files.getContentUri(VOLUME_NAME);

        mProjection = GalleryUtils.getProjection();

        mSelection = SELECTION;
        mSelectionArgs = null;
        mSortOrder = SORT_ORDER_DATE;
    }


    @Override
    public String getName() {
        return mApplication.getResources().getString(R.string.private_album_name);
    }

    @Override
    public long reload() {
        if (mImageNotifier.isDirty() || mVideoNotifier.isDirty()) {
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
        Context context = mApplication.getAndroidContext();
        ExifInfoFilter filter = ExifInfoFilter.getInstance(context);
        int privateItemCount = 0;
        int hiddenCount = 0;
        boolean isInPrivacyMode = GalleryAppImpl.getTctPrivacyModeHelperInstance(context).isInPrivacyMode();
        boolean isPrivacyModeEnable = GalleryAppImpl.getTctPrivacyModeHelperInstance(context).isPrivacyModeEnable();
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
                    if (isInPrivacyMode && isPrivacyModeEnable) {
                        privateItemCount++;
                    }
                    int id = cursor.getInt(0);
                    int type = ExifInfoFilter.getInstance(mApplication.getAndroidContext()).queryType(String.valueOf(id));
                    if (isNeedHideBurstShot(type)) {
                        hiddenCount++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                filter.notifySaveExifCache();
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        Log.d(TAG, "PrivateALbum--------privateItemCount = " + privateItemCount + " hiddenCount = " + hiddenCount);
        return privateItemCount - hiddenCount;
    }

    @Override
    public int getTotalMediaItemCount() {
        return getMediaItemCount();
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_INFO | SUPPORT_PRIVATE;
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
        int mediaType = cursor.getInt(MomentsNewAlbum.INDEX_MEDIA_TYPE);
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
        try {
            cursor = mResolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (SQLiteException e) {
            e.printStackTrace();
            if (GalleryAppImpl.sHasPrivateColumn) {
                projection = MomentsNewAlbum.PRIVATE_PROJECTION;
            } else {
                projection = MomentsNewAlbum.PROJECTION;
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

    private void notifyContentListener() {
        for (ContentChangeListener listener : mListeners) {
            listener.onContentChange();
        }
    }
}

/* ----------|----------------------|----------------------|----------------- */
/* 2016/02/22|  caihong.gu-nb       |  PR-1623780          |[Translation][Gallery][Video]The translation problem of  gallery*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/
package com.tct.gallery3d.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorWindowAllocationException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.db.DataBaseManager;
import com.tct.gallery3d.db.DataBaseManager.FavoriteDBListener;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.MediaSetUtils;

public class FavoriteAlbum extends MediaSet {
    private static final String TAG = "FavoriteAlbum";
    private static final String[] COUNT_PROJECTION = { "count(*)" };
    private static final String EXTERNAL_MEDIA = "external";

    public static final Path FAVORITE_IMAGE_PATH = Path.fromString("/local/favorite/image");
    public static final Path FAVORITE_VIDEO_PATH = Path.fromString("/local/favorite/video");
    public static final Path FAVORITE_ALL_PATH = Path.fromString("/local/favorite");

    private static final int INVALID_COUNT = -1;

    private final String mWhereClause;
    private final String mOrderClause;
    private final Uri mBaseUri;
    private String[] mProjection;
    private StringBuffer favoriteIdArray = new StringBuffer();

    private final GalleryApp mApplication;
    private final ContentResolver mResolver;
    //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 begin
    private String mName;
    //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 end

    private final boolean mIsImage;
    private final int mMediaType;

    private final ChangeNotifier mNotifier;
    private Path mItemPath;
    private int mCachedCount = INVALID_COUNT;
    private DataBaseManager mDataBaseManager = null;

    private FavoriteDBObserver mFavoriteObserver = new FavoriteDBObserver();

    public class FavoriteDBObserver implements FavoriteDBListener {
        public boolean isDirty = false;
        @Override
        public void onDBChanged() {
            isDirty = true;
            reload();
            notifyContentChanged();
        }
    }

    public FavoriteAlbum(Path path, GalleryApp application, boolean isImage) {
        super(path, nextVersionNumber());
        mApplication = application;
        mResolver = application.getContentResolver();
        mName = application.getResources().getString(R.string.favorite);
        mIsImage = isImage;
        mMediaType = mIsImage ? FileColumns.MEDIA_TYPE_IMAGE : FileColumns.MEDIA_TYPE_VIDEO;

        if (isImage) {
            mWhereClause = ImageColumns._ID + " in ";
            mOrderClause = ImageColumns.DATE_MODIFIED + " DESC, "
                    + ImageColumns._ID + " DESC";
            mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;

            mProjection = LocalImage.getImageProjection();

            mItemPath = LocalImage.ITEM_PATH;
        } else {
            mWhereClause = VideoColumns._ID + " in ";
            mOrderClause = VideoColumns.DATE_MODIFIED + " DESC, "
                    + VideoColumns._ID + " DESC";
            mBaseUri = Video.Media.EXTERNAL_CONTENT_URI;

            mProjection = LocalVideo.getVideoProjection();

            mItemPath = LocalVideo.ITEM_PATH;
        }

        mNotifier = new ChangeNotifier(this, mBaseUri, application);
        mDataBaseManager = mApplication.getDataBaseManager();
        mDataBaseManager.registerFavoriteDBListener(mFavoriteObserver);
    }

    @Override
    public boolean isCameraRoll() {
         return super.isCameraRoll();
    }

    @Override
    public Uri getContentUri() {
        if (mIsImage) {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        DataManager dataManager = mApplication.getDataManager();
        /*Uri uri = mBaseUri.buildUpon()
                .appendQueryParameter("limit", start + "," + count).build();*/
        Uri uri = mBaseUri;
        ArrayList<MediaItem> list = new ArrayList<MediaItem>();
        GalleryUtils.assertNotInRenderThread();

        Cursor cursor = null;
        try{
            List<String> favoriteList = mDataBaseManager.queryAllFavouriteIds(mMediaType);
            favoriteIdArray = GalleryUtils.buildStringByList(favoriteList);

            cursor = mResolver.query(
                    uri, mProjection, mWhereClause + favoriteIdArray.toString(),
                    null,
                    mOrderClause);
        } catch(SQLiteException e) {
            e.printStackTrace();
            cursor = mResolver.query(
                    uri, mProjection, mWhereClause + favoriteIdArray.toString(),
                    null,
                    mOrderClause);
        } catch (CursorWindowAllocationException e) {
            e.printStackTrace();
        }

        if (cursor == null) {
            Log.w(TAG, "query fail: " + uri);
            return list;
        }

        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                Path childPath = mItemPath.getChild(id);
                MediaItem item = loadOrUpdateItem(childPath, cursor,
                        dataManager, mApplication, mIsImage);
                list.add(item);
            }

        } finally {
            cursor.close();
        }
        if(start >= list.size()) {
            return new ArrayList<MediaItem>();
        } else if(start + count > list.size()) {
            return MediaSetUtils.subList(list, start, list.size());
        } else {
            return MediaSetUtils.subList(list, start, start + count);
        }
    }

    @Override
    public int getMediaSetType() {
        if(getMediaItemCount() > 0) {
            if(mIsImage) {
                mMediaSetType = MediaSet.MEDIASET_TYPE_IMAGE;
            } else {
                mMediaSetType = MediaSet.MEDIASET_TYPE_VIDEO;
            }
        } else {
            mMediaSetType = MediaSet.MEDIASET_TYPE_UNKNOWN;
        }
        return mMediaSetType;
    }

    private static MediaItem loadOrUpdateItem(Path path, Cursor cursor,
            DataManager dataManager, GalleryApp app, boolean isImage) {
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

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri,
            String[] projection, int id) {
        return resolver.query(uri, projection, "_id=?",
                new String[]{String.valueOf(id)}, null);
    }

    @Override
    public int getMediaItemCount() {
        mCachedCount = mDataBaseManager.queryFavoriteCount(mMediaType);
        return mCachedCount;
    }

    @Override
    public String getName() {
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 begin
        mName = mApplication.getResources().getString(R.string.favorite);
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 end
        return mName;
    }

    @Override
    public long reload() {
        if (mNotifier.isDirty() || mFavoriteObserver.isDirty) {
            mDataVersion = nextVersionNumber();
            mCachedCount = INVALID_COUNT;
            mFavoriteObserver.isDirty = false;
        }
        return mDataVersion;
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_INFO;
    }

    @Override
    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        List<String> favoriteList = mDataBaseManager.queryAllFavouriteIds(mMediaType);
        favoriteIdArray = GalleryUtils.buildStringByList(favoriteList);

        mResolver.delete(mBaseUri, mWhereClause + favoriteIdArray.toString(),
                null);
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

}

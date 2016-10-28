/* ----------|----------------------|----------------------|----------------- */
/* 2016/02/22|  caihong.gu-nb       |  PR-1623780          |[Translation][Gallery][Video]The translation problem of  gallery*/
/*-----------|----------------------|----------------------|---------------------------------------------------------------------------------*/

package com.tct.gallery3d.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorWindowAllocationException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter.FilterSourceListener;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.MediaSetUtils;

import java.util.ArrayList;
import java.util.List;

public class FaceShowAlbum extends MediaSet {
    private static final String TAG = "FaceShowAlbum";
    private static final String[] COUNT_PROJECTION = {"count(*)"};

    private static final int INVALID_COUNT = -1;
    private final String mWhereClause;
    private final String mOrderClause;
    private final Uri mBaseUri;
    private String[] mProjection;

    private final GalleryApp mApplication;
    private final ContentResolver mResolver;
    private final int mBucketId;
    //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 begin
    private String mName;
    //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 end

    private final ChangeNotifier mNotifier;
    private final Path mItemPath;
    private int mCachedCount = INVALID_COUNT;

    private StringBuffer faceShowIdArray = new StringBuffer();

    private final FaceShowListenter mFaceShowListener = new FaceShowListenter();

    public class FaceShowListenter implements FilterSourceListener {
        public boolean isDirty = false;

        @Override
        public void onSourceChanged() {
            isDirty = true;
            reload();
        }
    }

    public FaceShowAlbum(Path path, GalleryApp application) {
        super(path, nextVersionNumber());
        mApplication = application;
        mResolver = application.getContentResolver();
        mBucketId = MediaSetUtils.CAMERA_BUCKET_ID;
        mName = application.getResources().getString(R.string.selfies);

        mWhereClause = FileColumns._ID + " in ";//[DEFECT]-modified by yuanxi.jiang,01/08/2016,defect 1357509
        mOrderClause = ImageColumns.DATE_MODIFIED + " DESC, "
                + ImageColumns._ID + " DESC";
        mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;

        mProjection = LocalImage.getImageProjection();

        mItemPath = LocalImage.ITEM_PATH;

        mNotifier = new ChangeNotifier(this, mBaseUri, application);
        ExifInfoFilter.getInstance(mApplication.getAndroidContext()).registerFilterSourceListener(mFaceShowListener);
    }

    @Override
    public boolean isCameraRoll() {
        return super.isCameraRoll();
    }

    @Override
    public Uri getContentUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                .appendQueryParameter(LocalSource.KEY_BUCKET_ID,
                        String.valueOf(mBucketId)).build();
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        DataManager dataManager = mApplication.getDataManager();
        Uri uri = mBaseUri.buildUpon()
                .appendQueryParameter("limit", start + "," + count).build();
//        Uri uri = mBaseUri;
        ArrayList<MediaItem> list = new ArrayList<MediaItem>();
        GalleryUtils.assertNotInRenderThread();

        Cursor cursor = null;
        try {
            List<String> faceShowList = ExifInfoFilter.getInstance(mApplication.getAndroidContext())
                    .queryFaceshow(mApplication.getAndroidContext(), 0); //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909
            faceShowIdArray = GalleryUtils.buildStringByList(faceShowList);

            cursor = mResolver.query(
                    uri, mProjection, mWhereClause + faceShowIdArray.toString(),//[DEFECT]-modified by yuanxi.jiang,01/08/2016,defect 1357509
                    null,//[DEFECT]-modified by yuanxi.jiang,01/08/2016,defect 1357509
                    mOrderClause);
        } catch (SQLiteException e) {
            e.printStackTrace();
            if (GalleryAppImpl.sHasPrivateColumn) {
                mProjection = LocalImage.PRIVATE_PROJECTION;
            } else {
                mProjection = LocalImage.PROJECTION;
            }
            cursor = mResolver.query(
                    uri, mProjection, mWhereClause + faceShowIdArray.toString(),//[DEFECT]-modified by yuanxi.jiang,01/08/2016,defect 1357509
                    null,//[DEFECT]-modified by yuanxi.jiang,01/08/2016,defect 1357509
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
                        dataManager, mApplication);
                list.add(item);
            }

            mMediaSetType = MEDIASET_TYPE_UNKNOWN;
            if (list.size() > 0) {
                mMediaSetType = MEDIASET_TYPE_IMAGE;
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    @Override
    public int getMediaSetType() {
        return MediaSet.MEDIASET_TYPE_IMAGE;
    }

    private static MediaItem loadOrUpdateItem(Path path, Cursor cursor,
                                              DataManager dataManager, GalleryApp app) {
        synchronized (DataManager.LOCK) {
            LocalMediaItem item = (LocalMediaItem) dataManager.peekMediaObject(path);
            if (item == null) {
                item = new LocalImage(path, app, cursor);
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
        String whereClause = FileColumns.MEDIA_TYPE + " = ?";
        int mediaType = FileColumns.MEDIA_TYPE_IMAGE;
        int count = 0;
        Cursor cursor = null;
        boolean isOld = false;
        try {
            try {
                cursor = mApplication.getContentResolver().query(EXTERNAL_URI, NEWPROJECTION, whereClause,
                        new String[]{String.valueOf(mediaType)}, null);
            } catch (SQLiteException e) {
                cursor = mApplication.getContentResolver().query(EXTERNAL_URI, PROJECTION, whereClause,
                        new String[]{String.valueOf(mediaType)}, null);
                isOld = true;
            }

            if (cursor != null) {
                ExifInfoFilter filter = ExifInfoFilter.getInstance(mApplication.getAndroidContext());
                while (cursor.moveToNext()) {
                    int idIndex = cursor.getColumnIndex(FileColumns._ID);
                    long id = cursor.getLong(idIndex);
                    int type = filter.queryType(String.valueOf(id));
                    if (type == ExifInfoFilter.FACESHOW) {
                        count++;
                    }
                    if (type == ExifInfoFilter.NONE) {
                        int dataIndex = cursor.getColumnIndex(FileColumns.DATA);
                        int dateModifiedIndex = cursor.getColumnIndex(FileColumns.DATE_MODIFIED);
                        int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);
                        String path = cursor.getString(dataIndex);
                        long timestamp = cursor.getLong(dateModifiedIndex) * 1000;
                        if (!cursor.isNull(dateTakenIndex)) {
                            timestamp = cursor.getLong(dateTakenIndex);
                        }
                        int newType = 0;
                        if (!isOld) {
                            int gappMediaTypeIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_MEDIA_TYPE);
                            int burstIdIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_ID);
                            int burstIndexIndex = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_INDEX);
                            GappTypeInfo gappTypeInfo = new GappTypeInfo();
                            gappTypeInfo.setType(cursor.getInt(gappMediaTypeIndex));
                            gappTypeInfo.setBurstshotId(cursor.getInt(burstIdIndex));
                            gappTypeInfo.setBurstshotIndex(cursor.getInt(burstIndexIndex));
                            newType = filter.filter(String.valueOf(id), path, mediaType, timestamp, true, false, gappTypeInfo);
                        } else {
                            newType = filter.filter(String.valueOf(id), path, mediaType, timestamp, true, false, null);
                        }

                        if (newType == ExifInfoFilter.FACESHOW) {
                            count++;
                        }
                    }
                }
                filter.notifySaveExifCache();
                mCachedCount = count;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return mCachedCount;
    }

    @Override
    public String getName() {
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 begin
        mName = mApplication.getResources().getString(R.string.selfies);
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/02/22,PR1623780 end
        return mName;
    }

    @Override
    public long reload() {
        if (mFaceShowListener.isDirty || mNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            mCachedCount = INVALID_COUNT;
            mFaceShowListener.isDirty = false;
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
        mResolver.delete(mBaseUri, mWhereClause,
                new String[]{String.valueOf(mBucketId)});
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

}

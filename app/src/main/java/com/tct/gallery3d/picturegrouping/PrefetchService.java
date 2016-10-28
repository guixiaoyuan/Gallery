/* ----------|----------------------|----------------------|----------------- */
/* 01/27/2016| dongliange.feng      |ALM-1490867           |[GAPP][Android6.0][Gallery][Force Close][REG]Gallery force close after taking photos */
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.picturegrouping;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;

import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.GappTypeInfo;
import com.tct.gallery3d.data.LocalImage;
import com.tct.gallery3d.data.LocalVideo;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.image.ImageWorker;
import com.tct.gallery3d.util.MediaSetUtils;
import com.tct.gallery3d.util.PermissionUtil;
import com.tct.gallery3d.util.TctLog;

import java.util.ArrayList;

public class PrefetchService extends Service {

    private static final String TAG = "PrefetchService";
    private static final String VOLUME_NAME = "external";
    private static final String[] PROJECTION = {FileColumns._ID,
            FileColumns.DATA,
            FileColumns.MIME_TYPE,
            FileColumns.DATE_MODIFIED,
            ImageColumns.DATE_TAKEN,
            ImageColumns.LATITUDE,
            ImageColumns.LONGITUDE
    };

    private static final String[] NEWPROJECTION = {FileColumns._ID,
            FileColumns.DATA,
            FileColumns.MIME_TYPE,
            FileColumns.DATE_MODIFIED,
            ImageColumns.DATE_TAKEN,
            ImageColumns.LATITUDE,
            ImageColumns.LONGITUDE,
            GappTypeInfo.GAPP_MEDIA_TYPE,
            GappTypeInfo.GAPP_BURST_ID,
            GappTypeInfo.GAPP_BURST_INDEX
    };

    private Context mContext;
    private ImageWorker mWorker;

    private HandlerThread mHandlerThread4Exif;
    private Handler mHandler4Exif;

    private Handler mHandler4Address;

    private int mTotalCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mWorker = ((GalleryAppImpl)getApplication()).getImageWorker();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean checkResult = PermissionUtil.checkPermissions(this, getClass().getName());
        if (checkResult) {
            if (mHandlerThread4Exif == null) {
                mHandlerThread4Exif = new HandlerThread("PrefetchExif");
                mHandlerThread4Exif.start();
            }
            if (mHandler4Exif == null) {
                mHandler4Exif = new Handler(mHandlerThread4Exif.getLooper());
            }

            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-27, ALM-1490867 begin
            if (mHandler4Address == null) {
                HandlerThread bgThread = BackgroundThread.getInstance();
                mHandler4Address = new Handler(bgThread.getLooper());
            }
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2016-01-27, ALM-1490867 end

            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-11,ALM-1783185 begin
            if(intent != null) {
                prefetch(intent.getData());
            }
            //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-11,ALM-1783185 end

        } else {
            Log.i(TAG, "PrefetchService.checkPermissions failed");
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void prefetch(Uri sendUri) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = null;
        String selection = null;
        String sortOrder = null;
        if (sendUri == null) { //start from BootCompletedBroadcastReceiver
            uri = MediaStore.Files.getContentUri(VOLUME_NAME);
            selection =
                    "( " +
                    FileColumns.MEDIA_TYPE + "=" +
                    FileColumns.MEDIA_TYPE_IMAGE + " OR " +
                    FileColumns.MEDIA_TYPE + "=" +
                    FileColumns.MEDIA_TYPE_VIDEO + " )" +
                    " AND " +
                    "( ";
            ArrayList<Integer> bucketsId = MediaSetUtils.getMomentsBucketsId(contentResolver, uri);
            for(int i = 0; i < bucketsId.size(); i++) {
                selection += ImageColumns.BUCKET_ID + "=" + bucketsId.get(i);
                if(i < (bucketsId.size() - 1))
                    selection += " OR ";
            }
            selection += " )";
            String selectionDrm = "";
            if (DrmManager.isDrmEnable && GalleryActivity.TV_LINK_DRM_HIDE_FLAG) {
                selectionDrm = " AND (" + DrmManager.TCT_IS_DRM + "=0 OR " + DrmManager.TCT_IS_DRM + " IS NULL)";
            }
            selection += selectionDrm;
            sortOrder = FileColumns.DATE_MODIFIED + " DESC";
        } else { //start from NewPictureBroadcastReceiver
            uri = sendUri;
            selection = null;
            sortOrder = null;
        }

        Cursor cursor = null;
        boolean isOld = false;
        try {
            try{
                cursor = contentResolver.query(uri, NEWPROJECTION, selection, null, sortOrder);
                GalleryAppImpl.sHasNewColumn = true;
            }catch (SQLiteException e){
                cursor = contentResolver.query(uri, PROJECTION, selection, null, sortOrder);
                GalleryAppImpl.sHasNewColumn = false;
                isOld = true;
            }

            if (cursor != null) {
                mTotalCount = cursor.getCount();
                Log.i(TAG, "MediaStore.Files cursor total count : " + mTotalCount);
                int index = 0;
                while (cursor.moveToNext()) {
                    int idIndex = cursor.getColumnIndex(FileColumns._ID);
                    long id = cursor.getLong(idIndex);

                    prefetchAddress(cursor, id, index);
                    if (sendUri != null) {
                        prefetchExifInfo(cursor, id, index, isOld);
                    }
                    index++;
                }
            }
        } catch(Exception e) {
            Log.e(TAG, "prefetch query error : " + e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public void prefetchExifInfo(Cursor cursor, long id, int index, boolean isOld) {
        int dataIndex = cursor.getColumnIndex(FileColumns.DATA);
        int mimeTypeIndex = cursor.getColumnIndex(FileColumns.MIME_TYPE);
        int dateModifiedIndex = cursor.getColumnIndex(FileColumns.DATE_MODIFIED);
        int dateTakenIndex = cursor.getColumnIndex(ImageColumns.DATE_TAKEN);

        String path = cursor.getString(dataIndex);
        String mimeType = cursor.getString(mimeTypeIndex);
        long timestamp = cursor.getLong(dateModifiedIndex) * 1000;
        if (!cursor.isNull(dateTakenIndex)) {
            timestamp = cursor.getLong(dateTakenIndex);
        }
        if(!isOld){
            int gappMediaTypeIndex  = cursor.getColumnIndex(GappTypeInfo.GAPP_MEDIA_TYPE);
            int burstIdIndex  = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_ID);
            int burstIndexIndex  = cursor.getColumnIndex(GappTypeInfo.GAPP_BURST_INDEX);
            GappTypeInfo gappTypeInfo = new GappTypeInfo();
            gappTypeInfo.setType(cursor.getInt(gappMediaTypeIndex));
            gappTypeInfo.setBurstshotId(cursor.getInt(burstIdIndex));
            gappTypeInfo.setBurstshotIndex(cursor.getInt(burstIndexIndex));
            mHandler4Exif.post(new ExifInfoPrefetchRunnable(null, index, id, path, timestamp, mimeType, cursor.getCount(), gappTypeInfo));
        }else{
            mHandler4Exif.post(new ExifInfoPrefetchRunnable(null, index, id, path, timestamp, mimeType, cursor.getCount(), null));
        }

    }

    private class ExifInfoPrefetchRunnable implements Runnable {
        private Uri mMediaURI;
        private int mIndex;
        private long mId;
        private String mPath;
        private long mTimestamp;
        private String mMimeType;
        private int mCount;
        private GappTypeInfo mGappTypeInfo;

        public ExifInfoPrefetchRunnable(Uri uri, int index, long id, String path, long timestamp, String mimeType, int count, GappTypeInfo gappTypeInfo) {
            mMediaURI = uri;
            mIndex = index;
            mId = id;
            mPath = path;
            mTimestamp = timestamp;
            mMimeType = mimeType;
            mCount = count;
            mGappTypeInfo = gappTypeInfo;
        }

        @Override
        public void run() {
            int mediaType = FileColumns.MEDIA_TYPE_NONE;
            if (mMimeType != null) {
                String mimeType = mMimeType.toLowerCase();
                if (mimeType.startsWith("image/")) {
                    mediaType = FileColumns.MEDIA_TYPE_IMAGE;
                } else if (mimeType.startsWith("video/")) {
                    mediaType = FileColumns.MEDIA_TYPE_VIDEO;
                }
            }
            int type = ExifInfoFilter.getInstance(mContext).filter(String.valueOf(mId), mPath, mediaType, mTimestamp, true, true, mGappTypeInfo);
            TctLog.i(TAG, "path = " + mPath + ", type = " + type + ", MimeType = " + mMimeType);

            if (mIndex + 1 == mCount) {
                ExifInfoFilter.getInstance(mContext).notifySaveExifCache();
            }

            if (type != ExifInfoFilter.BURSTSHOTSHIDDEN) {
                final Path path;
                switch (mediaType) {
                    case FileColumns.MEDIA_TYPE_IMAGE:
                        path = LocalImage.ITEM_PATH.getChild(mId);
                        break;
                    case FileColumns.MEDIA_TYPE_VIDEO:
                        path = LocalVideo.ITEM_PATH.getChild(mId);
                        break;
                    default:
                        path = null;
                        break;
                }

                if (path != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            DataManager dm = DataManager.from(mContext);
                            MediaItem item = (MediaItem) dm.getMediaObject(path);
                            mWorker.loadLarge(item, null);
                        }
                    });
                }
            }
        }
    }

    private Handler mHandler = new Handler();

    public void prefetchAddress(Cursor cursor, long id, int index) {
        int latitudeIndex = cursor.getColumnIndex(ImageColumns.LATITUDE);
        int longitudeIndex = cursor.getColumnIndex(ImageColumns.LONGITUDE);
        boolean hasCoordinates = !cursor.isNull(latitudeIndex) && !cursor.isNull(longitudeIndex);
        if (hasCoordinates) {
            float latitude = (float) cursor.getDouble(latitudeIndex);
            float longitude = (float) cursor.getDouble(longitudeIndex);

            Uri mediaURI = Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            mHandler4Address.post(new AddressPrefetchRunnable(mediaURI, index, id, latitude, longitude));
        }
    }

    private class AddressPrefetchRunnable implements Runnable, AddressCache.Listener {
        private Uri mMediaURI;
        private int mIndex;
        private long mId;
        private float mLatitude;
        private float mLongitude;

        public AddressPrefetchRunnable(Uri uri, int index, long id, float latitude, float longitude) {
            mMediaURI = uri;
            mIndex = index;
            mId = id;
            mLatitude = latitude;
            mLongitude = longitude;
        }

        @Override
        public void run() {
            AddressCache addressCache = AddressCache.getInstance(getApplicationContext());
            addressCache.addListener(this);
            boolean waitingForCompletion = addressCache.prefetchAddress(mLatitude, mLongitude);
            if (waitingForCompletion) {
                
            }
        }

        @Override
        public void onAddressCacheUpdated(boolean successful) {
            
        }
    }
}

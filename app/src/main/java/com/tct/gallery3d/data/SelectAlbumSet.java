package com.tct.gallery3d.data;

import android.database.Cursor;
import android.hardware.display.DisplayManagerGlobal;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.util.Future;
import com.tct.gallery3d.util.FutureListener;
import com.tct.gallery3d.util.MediaSetUtils;
import com.tct.gallery3d.util.ThreadPool;

import java.util.ArrayList;
import java.util.Comparator;

public class SelectAlbumSet extends MediaSet
        implements FutureListener<ArrayList<MediaSet>> {
    @SuppressWarnings("unused")
    private static final String TAG = "SelectAlbumSet";

    public static final Path PATH_IMAGE = Path.fromString("/local/image");
    public static final Path PATH_VIDEO = Path.fromString("/local/video");


    private static final Uri[] mWatchUris =
            {MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.EXTERNAL_CONTENT_URI};

    private final GalleryApp mApplication;
    private final int mType;
    private ArrayList<MediaSet> mAlbums = new ArrayList<MediaSet>();
    private final ChangeNotifier mNotifier;
    private final String mName;
    private final Handler mHandler;
    private boolean mIsLoading;

    private Future<ArrayList<MediaSet>> mLoadTask;
    private ArrayList<MediaSet> mLoadBuffer;

    private int bucketID = 0;   //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909

    public SelectAlbumSet(Path path, GalleryApp application) {
        super(path, nextVersionNumber());
        mApplication = application;
        mHandler = new Handler(application.getMainLooper());
        mType = getTypeFromPath(path);
        mNotifier = new ChangeNotifier(this, mWatchUris, application);
        mName = application.getResources().getString(
                R.string.set_label_local_albums);
    }

    private static int getTypeFromPath(Path path) {
        String name[] = path.split();
        if (name.length < 2) {
            throw new IllegalArgumentException(path.toString());
        }
        return getTypeFromString(name[1]);
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        return mAlbums.get(index);
    }

    @Override
    public int getSubMediaSetCount() {
        return mAlbums.size();
    }

    @Override
    public String getName() {
        return mName;
    }

    private static int findBucket(BucketHelper.BucketEntry entries[], int bucketId) {
        for (int i = 0, n = entries.length; i < n; ++i) {
            if (entries[i].bucketId == bucketId) return i;
        }
        return -1;
    }

    private int queryCameraCount() {
        int count = 0;
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] column = new String[]{"count(*)"};
        //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909 start
        String where = null;
        if (bucketID == MediaSetUtils.CAMERA_BUCKET_ID)
            where = "bucket_id=" + MediaSetUtils.CAMERA_BUCKET_ID + " and "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " or " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        if (bucketID == MediaSetUtils.SDCARD_CAMERA_BUCKET_ID)
            where = "bucket_id=" + MediaSetUtils.SDCARD_CAMERA_BUCKET_ID + " and "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " or " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909 end
        Cursor cursor = mApplication.getContentResolver().query(uri, column, where, null, null);
        if(cursor != null && cursor.getCount() > 0) {
            Utils.assertTrue(cursor.moveToNext());
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    private class AlbumsLoader implements ThreadPool.Job<ArrayList<MediaSet>> {

        @Override
        @SuppressWarnings("unchecked")
        public ArrayList<MediaSet> run(ThreadPool.JobContext jc) {
            BucketHelper.BucketEntry[] entries = BucketHelper.loadBucketEntries(
                    jc, mApplication.getContentResolver(), mType);

            if (jc.isCancelled()) return null;

            int offset = 0;
            int index = findBucket(entries, MediaSetUtils.CAMERA_BUCKET_ID);
            if (index != -1) {
                circularShiftRight(entries, offset++, index);
            }
            int otherIndex = findBucket(entries, MediaSetUtils.SDCARD_CAMERA_BUCKET_ID);
            if (otherIndex != -1) {
                circularShiftRight(entries, offset++, otherIndex);
            }
            ArrayList<MediaSet> albums = new ArrayList<MediaSet>();
            DataManager dataManager = mApplication.getDataManager();
            for (BucketHelper.BucketEntry entry : entries) {
                Log.i(TAG, "entry bucketName:"+entry.bucketName+"  "+mPath);
                MediaSet album = getLocalAlbum(dataManager,
                        mType, mPath, entry.bucketId, entry.bucketName);
                int count = album.getMediaItemCount();
                if (count == 0) continue;
                album.mAlbumFilePath = entry.getAlbumPath();
                if(entry.bucketId == MediaSetUtils.CAMERA_BUCKET_ID || entry.bucketId == MediaSetUtils.SDCARD_CAMERA_BUCKET_ID) {
                    bucketID = entry.bucketId;
                    int allCameraCount = queryCameraCount();
                    if (entry.bucketId == MediaSetUtils.CAMERA_BUCKET_ID) {
                        Log.i(TAG, "cameraCountFromPhone = " + allCameraCount);
                    } else {
                        Log.i(TAG, "cameraCountFromSDcard = " + allCameraCount);
                    }
                    if(allCameraCount > 0) {
                     /*MODIFIED-END by caihong.gu-nb,BUG-1871616*/
                        album.mAlbumType = DataSourceType.ALBUM_CAMERA;
                        albums.add(album);
                    } else {
                        offset--;
                    }
                } else {
                    album.mAlbumType = DataSourceType.ALBUM_NORMAL;
                    albums.add(album);
                }
            }
            return albums;
        }
    }

    private MediaSet getLocalAlbum(
            DataManager manager, int type, Path parent, int id, String name) {
        synchronized (DataManager.LOCK) {
            Path path = parent.getChild(id);
            MediaObject object = manager.peekMediaObject(path);
            if (object != null) {
                //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/22,PR1431112 begin
                MediaSet mediaSet = (MediaSet) object;
                mediaSet.reload();
                return mediaSet;
                //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/01/22,PR1431112 end
            }
            switch (type) {
                case MEDIA_TYPE_IMAGE:
                    return new LocalAlbum(path, mApplication, id, true, name);
                case MEDIA_TYPE_VIDEO:
                    return new LocalAlbum(path, mApplication, id, false, name);
                case MEDIA_TYPE_ALL: // MODIFIED by Yaoyu.Yang, 2016-08-04,BUG-2208330
                    Comparator<MediaItem> comp = DataManager.sDateTakenComparator;
                    return new LocalMergeAlbum(path, comp, new MediaSet[] {
                            getLocalAlbum(manager, MEDIA_TYPE_IMAGE, PATH_IMAGE, id, name),
                            getLocalAlbum(manager, MEDIA_TYPE_VIDEO, PATH_VIDEO, id, name)}, id);
            }
            throw new IllegalArgumentException(String.valueOf(type));
        }
    }

    @Override
    public synchronized boolean isLoading() {
        return mIsLoading;
    }

    @Override
    // synchronized on this function for
    //   1. Prevent calling reload() concurrently.
    //   2. Prevent calling onFutureDone() and reload() concurrently
    public synchronized long reload() {
        int wifiDisplayStatus = DisplayManagerGlobal.getInstance().getWifiDisplayStatus().getActiveDisplayState();
        if (mNotifier.isDirty() || GalleryActivity.LAST_WIFI_DISPLAY_STATE != wifiDisplayStatus) {
            GalleryActivity.LAST_WIFI_DISPLAY_STATE = wifiDisplayStatus;
            if (mLoadTask != null) mLoadTask.cancel();
            mIsLoading = true;
            mLoadTask = mApplication.getThreadPool().submit(new AlbumsLoader(), this);
        }
        if (mLoadBuffer != null) {
            mAlbums = mLoadBuffer;
            mLoadBuffer = null;
            for (MediaSet album : mAlbums) {
                album.reload();
            }
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    @Override
    public synchronized void onFutureDone(Future<ArrayList<MediaSet>> future) {
        if (mLoadTask != future) return; // ignore, wait for the latest task
        mLoadBuffer = future.get();
        mIsLoading = false;
        if (mLoadBuffer == null) mLoadBuffer = new ArrayList<MediaSet>();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyContentChanged();
            }
        });
    }

    private static <T> void circularShiftRight(T[] array, int i, int j) {
        T temp = array[j];
        for (int k = j; k > i; k--) {
            array[k] = array[k - 1];
        }
        array[i] = temp;
    }
}

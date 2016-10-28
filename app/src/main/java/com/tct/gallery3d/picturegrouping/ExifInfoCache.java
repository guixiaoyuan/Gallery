package com.tct.gallery3d.picturegrouping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.tct.gallery3d.util.TctLog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

public class ExifInfoCache {
    private static String TAG = "ExifInfoCache";

    private ExifInfoDBHelper mDBHelper = null;
    private Context mContext = null;
    private ConcurrentHashMap<String, ExifItem> mCachedExifInfos = new ConcurrentHashMap<String, ExifItem>();
    private ConcurrentHashMap<String, ExifItem> mCachedReady = new ConcurrentHashMap<String, ExifItem>();
    private ArrayList<ExifItem> mBurstShots = new ArrayList<ExifItem>();

    private Handler mHandler;

    public ExifInfoCache(Context context) {
        mContext = context;
        mDBHelper = new ExifInfoDBHelper(mContext);
        mHandler = new Handler(mContext.getMainLooper());
        mCachedExifInfos.clear();
        readAllFromDatabase();
    }

    public static class ExifItem {
        public String id;
        public Integer type;
        public String burstId;
        public String burstIndex;
        public String path;
        public String time;

        public ExifItem(String id, Integer type, String burstId, String burstIndex, String path, String time) {
            this.id = id;
            this.type = type;
            this.burstId = burstId == null ? "" : burstId;
            this.burstIndex = burstIndex == null ? "" : burstIndex;
            this.path = path == null ? "" : path;
            this.time = time == null ? "" : time;
        }

        public boolean equal(ExifItem item) {
            return (id.equals(item.id) && burstId.equals(item.burstId) && burstIndex.equals(item.burstIndex));
        }
    }

    private ExifItem getFromCache(String contentId) {
        return mCachedExifInfos.get(contentId);
    }

    public void prefetchExifInfo(String contentId, int type, boolean needSave) {
        synchronized (mCachedExifInfos) {
            if (getFromCache(contentId) == null) {
                ExifItem item = new ExifItem(contentId, type, null, null, null, null);
                mCachedExifInfos.put(contentId, item);
                if (needSave) {
                    mCachedReady.put(contentId, item);
                }
            }
        }
    }

    //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909 start
    public void prefetchExifInfo(String contentId, int type, boolean needSave, String path){
        synchronized (mCachedExifInfos) {
            if (getFromCache(contentId) == null) {
                ExifItem item = new ExifItem(contentId, type, null, null, path, null);
                mCachedExifInfos.put(contentId, item);
                if (needSave) {
                    mCachedReady.put(contentId, item);
                }
            }
        }
    }
    //[DEFECT]-modified by dekuan.liu,01/31/2016,Defect 1392909 end

    public Hashtable<String, ArrayList<ExifItem>> prepareBurstCache() {
        Hashtable<String, ArrayList<ExifItem>> burstShotCache = new Hashtable<String, ArrayList<ExifItem>>();

        for (int i = 0; i < mBurstShots.size(); i++) {
            ExifItem exifItem = mBurstShots.get(i);
            ArrayList<ExifItem> burstShotArray = (ArrayList<ExifItem>) (burstShotCache.get(exifItem.burstId));
            if (burstShotArray == null) {
                burstShotArray = new ArrayList<ExifItem>();
                burstShotCache.put(exifItem.burstId, burstShotArray);
            }
            if (exifItem.type == ExifInfoFilter.BURSTSHOTS) {
                burstShotArray.add(0, exifItem);
            } else {
                burstShotArray.add(exifItem);
            }
        }
        mBurstShots.clear();

        return burstShotCache;
    }

    public void prefetchBurstExif(ExifItem item) {
        if (item == null || item.id == null) return;

        synchronized (mCachedExifInfos) {
            String contentId = item.id;
            if (getFromCache(contentId) == null) {
                mCachedExifInfos.put(contentId, item);
                mCachedReady.put(contentId, item);
            }
        }
    }

    public void updateBurstExifType(final ExifItem exifItem, boolean immediate) {
        if (exifItem == null || exifItem.id == null) return;
        ExifItem item = mCachedReady.get(exifItem.id);
        if (item != null) {
            item.type = exifItem.type;
        } else {
            mCachedReady.put(exifItem.id, exifItem);
        }
        if (immediate) {
            saveToDatabase();
        }
    }

    public ArrayList<ExifItem> getBurstShot(String burstId) {
        ArrayList<ExifItem> burstShots = new ArrayList<ExifItem>();

        try {
            String tableName = ExifInfoDBHelper.EXIF_INFO_TABLE_NAME;
            String[] projection = null;
            String selection = ExifInfoDBHelper.BURST_ID + " = + " + burstId;
            String[] selectionArgs = null;
            String sortOrder = ExifInfoDBHelper.TYPE + " ASC, " + ExifInfoDBHelper.BURST_INDEX + " DESC";
            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            try {
                db.acquireReference();
                Cursor cursor = null;
                try {
                    cursor = db.query(tableName, // The table to query
                                      projection, // The columns to return
                                      selection, // The columns for the WHERE clause
                                      selectionArgs, // The values for the WHERE clause
                                      null, // don't group the rows
                                      null, // don't filter by row groups
                                      sortOrder); // The sort order

                    if (cursor != null) {
                        int contentIdIndex = cursor.getColumnIndex(ExifInfoDBHelper.CONTENT_ID);
                        int typeIndex = cursor.getColumnIndex(ExifInfoDBHelper.TYPE);
                        int burstIndexIndex = cursor.getColumnIndex(ExifInfoDBHelper.BURST_INDEX);
                        int pathIndex = cursor.getColumnIndex(ExifInfoDBHelper.PATH);
                        int timeIndex = cursor.getColumnIndex(ExifInfoDBHelper.TIME);
                        while (cursor.moveToNext()) {
                            try {
                                String contentId = cursor.getString(contentIdIndex);
                                Integer type = Integer.valueOf(cursor.getInt(typeIndex));
                                String burstIndex = cursor.getString(burstIndexIndex);
                                String path = cursor.getString(pathIndex);
                                String time = cursor.getString(timeIndex);
                                burstShots.add(new ExifItem(contentId, type, burstId, burstIndex, path, time));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    TctLog.e(TAG, "ExifInfoCache.getBurstShot db.query error : " + e);
                } finally {
                    if (cursor != null) cursor.close();
                }
            } finally {
                db.releaseReference();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return burstShots;
    }

    public Integer getType(String contentId) {
        synchronized (mCachedExifInfos) {
            ExifItem item = getFromCache(contentId);
            if (item != null) {
                return item.type;
            }
            return ExifInfoFilter.NONE;
        }
    }

    public ExifItem getExifItem(String contentId) {
        synchronized (mCachedExifInfos) {
            return getFromCache(contentId);
        }
    }

    public void removeCachedExifInfo(String contentId) {
        if (mCachedExifInfos.get(contentId) != null) {
            mCachedExifInfos.remove(contentId);
            removeFromDatabase(contentId);
        }
    }

    public Set<Map.Entry<String, ExifItem>> getEntrySet() {
        return mCachedExifInfos.entrySet();
    }

    public void saveToDatabase() {
        if (mCachedReady.size() == 0) {
            return;
        }

        final HashMap<String, ExifItem> cachedReady = new HashMap<>(mCachedReady);
        mCachedReady.clear();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String tableName = ExifInfoDBHelper.EXIF_INFO_TABLE_NAME;
                    SQLiteDatabase db = mDBHelper.getWritableDatabase();

                    try {
                        db.beginTransaction();
                        Set<String> keys = cachedReady.keySet();
                        Iterator<String> iter = keys.iterator();
                        while (iter.hasNext()) {
                            String contentId = (String)iter.next();
                            ContentValues values = new ContentValues();
                            values.put(ExifInfoDBHelper.CONTENT_ID, contentId);
                            ExifItem item = cachedReady.get(contentId);
                            values.put(ExifInfoDBHelper.TYPE, item.type);
                            values.put(ExifInfoDBHelper.BURST_ID, item.burstId == null ? "" : item.burstId);
                            values.put(ExifInfoDBHelper.BURST_INDEX, item.burstIndex == null ? "" : item.burstIndex);
                            values.put(ExifInfoDBHelper.PATH, item.path);
                            values.put(ExifInfoDBHelper.TIME, item.time);
                            long rowId = db.insert(tableName, null, values);
                            if (rowId < 0) {
                                throw new Exception("*** Insertion failed");
                            }
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void removeFromDatabase(final String contentId) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String tableName = ExifInfoDBHelper.EXIF_INFO_TABLE_NAME;
                    SQLiteDatabase db = mDBHelper.getWritableDatabase();

                    try {
                        db.acquireReference();

                        String whereClause = ExifInfoDBHelper.CONTENT_ID + " = + " + contentId;
                        int ret = db.delete(tableName, whereClause, null);
                        if (ret == 0) {
                            TctLog.d(TAG, "delete one item into the exif info table error: " + contentId);
                        }
                    } finally {
                        db.releaseReference();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void readAllFromDatabase() {
        mCachedExifInfos.clear();

        try {
            String tableName = ExifInfoDBHelper.EXIF_INFO_TABLE_NAME;
            String[] projection = null;
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = ExifInfoDBHelper.CONTENT_ID + " DESC";
            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            try {
                db.acquireReference();
                Cursor cursor = null;
                try {
                    cursor = db.query(tableName, // The table to query
                                      projection, // The columns to return
                                      selection, // The columns for the WHERE clause
                                      selectionArgs, // The values for the WHERE clause
                                      null, // don't group the rows
                                      null, // don't filter by row groups
                                      sortOrder); // The sort order

                    if (cursor != null) {
                        int contentIdIndex = cursor.getColumnIndex(ExifInfoDBHelper.CONTENT_ID);
                        int typeIndex = cursor.getColumnIndex(ExifInfoDBHelper.TYPE);
                        int burstIdIndex = cursor.getColumnIndex(ExifInfoDBHelper.BURST_ID);
                        int burstIndexIndex = cursor.getColumnIndex(ExifInfoDBHelper.BURST_INDEX);
                        int pathIndex = cursor.getColumnIndex(ExifInfoDBHelper.PATH);
                        int timeIndex = cursor.getColumnIndex(ExifInfoDBHelper.TIME);
                        while (cursor.moveToNext()) {
                            try {
                                String contentId = cursor.getString(contentIdIndex);
                                Integer type = Integer.valueOf(cursor.getInt(typeIndex));
                                String burstId = cursor.getString(burstIdIndex);
                                String burstIndex = cursor.getString(burstIndexIndex);
                                String path = cursor.getString(pathIndex);
                                String time = cursor.getString(timeIndex);
                                ExifItem exifItem = new ExifItem(contentId, type, burstId, burstIndex, path, time);
                                mCachedExifInfos.put(contentId, exifItem);
                                if (type == ExifInfoFilter.BURSTSHOTS || type == ExifInfoFilter.BURSTSHOTSHIDDEN) {
                                    mBurstShots.add(exifItem);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    TctLog.e(TAG, "ExifInfoCache.readAllFromDatabase db.query error : " + e);
                } finally {
                    if (cursor != null) cursor.close();
                }
            } finally {
                db.releaseReference();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private Integer queryFromDatabase(String contentId) {
        TctLog.i(TAG, "ExifInfoCache.queryFromDatabase(" + contentId + "){");

        Integer type = null;
        try {
            String tableName = ExifInfoDBHelper.EXIF_INFO_TABLE_NAME;
            String[] projection = null; // Let's get everything (just for test)
            String selection = ExifInfoDBHelper.CONTENT_ID + " = + " + contentId;
            String[] selectionArgs = null;
            String sortOrder = null;
            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            try {
                db.acquireReference();
                Cursor cursor = null;
                try {
                    cursor = db.query(tableName, // The table to query
                                      projection, // The columns to return
                                      selection, // The columns for the WHERE clause
                                      selectionArgs, // The values for the WHERE clause
                                      null, // don't group the rows
                                      null, // don't filter by row groups
                                      sortOrder); // The sort order

                    if (cursor != null) {
                        int typeIndex = cursor.getColumnIndex(ExifInfoDBHelper.TYPE);

                        while (cursor.moveToNext()) {
                            type = Integer.valueOf(cursor.getInt(typeIndex));
                            TctLog.i(TAG, "typeString: " + type);
                        }
                    }
                } catch (Exception e) {
                    TctLog.e(TAG, "ExifInfoCache.queryFromDatabase db.query error : " + e);
                } finally {
                    if (cursor != null) cursor.close();
                }
            } finally {
                db.releaseReference();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TctLog.i(TAG, "} ExifInfoCache.queryFromDatabase()");

        return type;
    }*/
}
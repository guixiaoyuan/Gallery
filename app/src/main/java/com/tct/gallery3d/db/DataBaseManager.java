package com.tct.gallery3d.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;

import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.common.Utils;
import com.tct.gallery3d.util.GalleryUtils;

public class DataBaseManager {

    private static final String TAG = "DataBaseManager";
    public static final String DB_NAME = "gallery.db";

    private static final String[] COLUMN_COUNT = new String[]{"count(*)"};
    public static final Uri FILE_URI = MediaStore.Files.getContentUri("external");

    public static int DB_VERSION = 1;

    private SQLiteDatabase db = null;
    private SQLiteHelper dbHelper = null;
    private Context mContext = null;

    private ArrayList<FavoriteDBListener> mDBListeners = new ArrayList<FavoriteDBListener>();

    private Handler notifyHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            List<String> allFaIds = queryAllFavouriteIds(FileColumns.MEDIA_TYPE_NONE);
            if(allFaIds == null || allFaIds.size() == 0) return;
            StringBuffer sb = GalleryUtils.buildStringByList(allFaIds);
            //BUG-FIX For PR1192698 by kaiyuan.ma begin
            Cursor cursor;
            try {
                cursor = mContext.getContentResolver().query(FILE_URI, new String[]{"_id"}, "_id in " + sb.toString(), null, null);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return;
            }
            //Cursor cursor = mContext.getContentResolver().query(FILE_URI, new String[]{"_id"}, "_id in " + sb.toString(), null, null);
            ////BUG-FIX For PR1192698 by kaiyuan.ma end
            List<String> queryList = null;
            List<String> dirtyList = null;
            if(cursor != null) {
                queryList = new ArrayList<String>();
                dirtyList = new ArrayList<String>();
                while (cursor.moveToNext()) {
                    queryList.add(String.valueOf(cursor.getInt(0)));
                }
                cursor.close();
                for (int i = 0; i < allFaIds.size(); i++) {
                    String id = allFaIds.get(i);
                    if(!queryList.contains(id)) {
                        dirtyList.add(id);
                    }
                }
            } else {
                dirtyList = allFaIds;
            }
            int result = deleteFavoriteByList(dirtyList);
            Log.e(TAG, "## delete dirty data, count=" + result);
        }
    };

    public void registerFavoriteDBListener(FavoriteDBListener dbListener) {
        synchronized(mDBListeners) {
            if(!mDBListeners.contains(dbListener)) {
                mDBListeners.add(dbListener);
            }
        }
    }

    public void unregisterFavoriteDBListener(FavoriteDBListener dbListener) {
        synchronized(mDBListeners) {
            if(mDBListeners.contains(dbListener)) {
                mDBListeners.remove(dbListener);
            }
        }
    }

    public interface FavoriteDBListener {
        void onDBChanged();
    }

    public DataBaseManager(Context context) {
        mContext = context;
        dbHelper = new SQLiteHelper(context, DB_NAME, null, DB_VERSION);
        db = dbHelper.getWritableDatabase();
        context.getContentResolver().registerContentObserver(FILE_URI, true, new Notifier(notifyHandler));
        notifyHandler.sendEmptyMessage(0);
    }

    public void close() {
        db.close();
        dbHelper.close();
    }

    public synchronized ArrayList<String> queryAllFavouriteIds(int mediaType){
        ArrayList<String> allFavouriteList = null;
        Cursor cursor = null;
        try {
            String where = mediaType == FileColumns.MEDIA_TYPE_NONE ? null : SQLiteHelper.COLUMN_MEDIATYPE + "=" + mediaType;
            cursor = db.query(SQLiteHelper.TABLE_NAME,
                    new String[] { SQLiteHelper.COLUMN_ID },
                    where, null,
                    null, null, SQLiteHelper.COLUMN_FID + " DESC");
            if(cursor == null || cursor.getCount() <= 0) {
                return null;
            }
            allFavouriteList = new ArrayList<String>();
            while(cursor.moveToNext()){
                String id = cursor.getString(0);
                allFavouriteList.add(id);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null)
                cursor.close();
        }

        return allFavouriteList;
    }

    public synchronized int queryFavoriteCount(int mediaType) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = db.query(SQLiteHelper.TABLE_NAME, COLUMN_COUNT, SQLiteHelper.COLUMN_MEDIATYPE + "=" + mediaType, null, null, null, null);
            if(cursor == null) {
                return 0;
            }
            Utils.assertTrue(cursor.moveToNext());
            count = cursor.getInt(0);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null)
                cursor.close();
        }
        return count;
    }

    public synchronized long insertDataOfFavourite(String id, int mediaType) {
        if (id == null || id.length() == 0)
            return 0;

        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_ID, id);
        values.put(SQLiteHelper.COLUMN_MEDIATYPE, mediaType);

        long insertCount = db.insert(SQLiteHelper.TABLE_NAME, SQLiteHelper.COLUMN_ID,
                values);
        Log.d(TAG, "DataBaseManager.insertDataOfFavourite " + insertCount);

        for (int i = 0; i < mDBListeners.size(); i++) {
            mDBListeners.get(i).onDBChanged();
        }

        return insertCount;
    }

    public synchronized int deleteDataOfFavourite(String id) {
        if (id == null || id.length() == 0)
            return 0;

        int result = db.delete(SQLiteHelper.TABLE_NAME, SQLiteHelper.COLUMN_ID
                + " = ? ", new String[] { id });
        Log.d(TAG, "DataBaseManager.deleteDataOfFavourite " + result);

        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-14,ALM-1783194 begin
        new Thread(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                for (int i = 0; i < mDBListeners.size(); i++) {
                    mDBListeners.get(i).onDBChanged();
                }
            }
        }).start();
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-14,ALM-1783194 end

        return result;
    }

    public synchronized int deleteFavoriteByList(List<String> idList) {
        StringBuffer dirtyString = GalleryUtils.buildStringByList(idList);
        int result = db.delete(SQLiteHelper.TABLE_NAME, SQLiteHelper.COLUMN_ID
                + " in " + dirtyString.toString(), null);
        for (int i = 0; i < mDBListeners.size(); i++) {
            mDBListeners.get(i).onDBChanged();
        }
        return result;
    }

    public synchronized boolean isFavorite(int id) {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = db.query(SQLiteHelper.TABLE_NAME,
                    COLUMN_COUNT, SQLiteHelper.COLUMN_ID + "=" + id,
                    null, null, null, null);
            if(cursor != null) {
                Utils.assertTrue(cursor.moveToNext());
                int count = cursor.getInt(0);
                result = count > 0;
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null)
                cursor.close();
        }
        return result;
    }

    class Notifier extends ContentObserver {
        Handler mHandler = null;

        public Notifier(Handler handler) {
            super(handler);
            mHandler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHandler.sendEmptyMessage(0);
        }
    }
}

/* 09/01/2015|    su.jiang     |  PR-1247121   |[Gallery]The slo.mo video can't into favorite albums..*/
/*-----------|-----------------|---------------|------------------------------------------------------*/
package com.tct.gallery3d.provider;

import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.db.DataBaseManager;
import com.tct.gallery3d.db.SQLiteHelper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;

public class GalleryFavouriteProvider extends ContentProvider{

    private static final String TAG = "GalleryFavouriteProvider";

    private static final String AUTHORITY = "com.tct.gallery.favourite.provider";
    private static final UriMatcher sUriMatcher;

    private static final int QUERY = 1;
    private static final int INSERT = 2;
    private static final int DELETE = 3;

    private SQLiteHelper dbHelper = null;
    private DataManager mDataManager;
    private DataBaseManager mDataBaseManager;//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-09,PR1247121

    static{
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "/query/#", QUERY);
        sUriMatcher.addURI(AUTHORITY, "/insert/#",INSERT);
        sUriMatcher.addURI(AUTHORITY, "/delete/#",DELETE);
    }

    @Override
    public boolean onCreate() {
        GalleryApp app = (GalleryApp) getContext().getApplicationContext();
        mDataManager = app.getDataManager();
        mDataBaseManager = app.getDataBaseManager();//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-09,PR1247121
        dbHelper = new SQLiteHelper(getContext(),DataBaseManager.DB_NAME , null, DataBaseManager.DB_VERSION);
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (sUriMatcher.match(uri) != DELETE) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        int cnt = 0;
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-09,PR1247121 begin
        long id = ContentUris.parseId(uri);
        cnt = mDataBaseManager.deleteDataOfFavourite(String.valueOf(id));
        Log.e(TAG, "rowId == " + cnt);
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-09,PR1247121 begin
        return cnt;
    }

    @Override
    public String getType(Uri uri) {
        long token = Binder.clearCallingIdentity();
        try {
            Path path = Path.fromString(uri.getPath());
            MediaItem item = (MediaItem) mDataManager.getMediaObject(path);
            return item != null ? item.getMimeType() : null;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != INSERT) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sUriMatcher.match(uri) == INSERT) {
            long id = ContentUris.parseId(uri);
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-09,PR1247121 begin
            long rowId = mDataBaseManager.insertDataOfFavourite(String.valueOf(id), 3);
            //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-09,PR1247121 end
            Log.e(TAG, "rowId == " + rowId);
            return null;
        }
        throw new SQLException("Failed to insert row into" + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        if (sUriMatcher.match(uri) != QUERY) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Cursor cursor = null;
        if (sUriMatcher.match(uri) == QUERY) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            cursor = db.query(SQLiteHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        }
        Log.e(TAG, "cursor == " + cursor);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return 0;
    }
}

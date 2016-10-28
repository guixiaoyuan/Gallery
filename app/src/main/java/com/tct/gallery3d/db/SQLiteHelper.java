package com.tct.gallery3d.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore.Files.FileColumns;

import com.tct.gallery3d.app.Log;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLiteHelper";

    public static final String TABLE_NAME = "file";

    public static final String COLUMN_FID = "fid";
    public static final String COLUMN_ID = FileColumns._ID;
    public static final String COLUMN_MEDIATYPE = FileColumns.MEDIA_TYPE;

    public SQLiteHelper(Context context, String name, CursorFactory factory,
            int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + "(" +
                COLUMN_FID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_ID + " TEXT," + COLUMN_MEDIATYPE + " INTEGER)");
        Log.d(TAG, "SQLiteHelper.onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        Log.d(TAG, "SQLiteHelper.onUpgrade");
        onCreate(db);
    }

}

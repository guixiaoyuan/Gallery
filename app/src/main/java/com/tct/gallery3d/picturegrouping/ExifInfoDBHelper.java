package com.tct.gallery3d.picturegrouping;

import com.tct.gallery3d.util.TctLog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ExifInfoDBHelper extends SQLiteOpenHelper {
    private static String TAG = "ExifInfoDBHelper";
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "ExifInfo.db";
    public static final String EXIF_INFO_TABLE_NAME = "ExifInfo";
    public static final String CONTENT_ID = "content_id";
    public static final String TYPE = "type";
    public static final String BURST_ID = "burst_id";
    public static final String BURST_INDEX = "burst_index";
    public static final String PATH = "path";
    public static final String TIME = "time";
    private static final String SQL_CREATE_EXIF_INFO_TABLE =
                    "CREATE TABLE " + EXIF_INFO_TABLE_NAME + " (" +
                    CONTENT_ID + " TEXT, " +
                    TYPE + " INTEGER DEFAULT 0, " +
                    BURST_ID + " TEXT, " +
                    BURST_INDEX + " TEXT, " +
                    PATH + " TEXT, " +
                    TIME + " TEXT, " +
                    "UNIQUE (" + CONTENT_ID + ") ON CONFLICT REPLACE" +
                    " )";
    private static final String SQL_DELETE_EXIF_INFO_TABLE =
            "DROP TABLE IF EXISTS " + EXIF_INFO_TABLE_NAME;

    public ExifInfoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TctLog.d(TAG, "ExifInfoDBHelper create");
        db.execSQL(SQL_CREATE_EXIF_INFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_EXIF_INFO_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}

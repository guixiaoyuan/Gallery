package com.tct.gallery3d.picturegrouping;

import com.tct.gallery3d.picturegrouping.AddressDBContract.AddressTable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



public class AddressDBHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_LOCATION_TABLE =
            "CREATE TABLE " + AddressTable.TABLE_NAME + " (" +
                    AddressTable._LATITUDE_HASH + " INTEGER, " +
                    AddressTable._LONGITUDE_HASH + " INTEGER, " +
                    AddressTable._ADDRESS_SET + " TEXT, " +
                    AddressTable._FAIL_COUNT + " INTEGER, " +
                    AddressTable._FAIL_TIMESTAMP + " INTEGER, " +
                    "UNIQUE (" + AddressTable._LATITUDE_HASH + ", " + AddressTable._LONGITUDE_HASH + ") ON CONFLICT REPLACE" +
            " )";

    
    private static final String SQL_DELETE_LOCATION_TABLE =
            "DROP TABLE IF EXISTS " + AddressTable.TABLE_NAME;
    
    
    // If you change the database schema, you must increment the database version.
    // Version 1: includes overall address quality, and field / quality for Address, Locality, SubRegion, Region, Country and Void
    // Version 2: includes the country-code, without quality
    public static final int DATABASE_VERSION = 3;
    static final String DATABASE_NAME = "AddressCache.db";
    
    public AddressDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(PictureGrouping.TAG, "new AddressDBHelper()");
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(PictureGrouping.TAG, "AddressDBHelper.onCreate(){");
        
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
        
        Log.d(PictureGrouping.TAG, "} AddressDBHelper.onCreate()");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(PictureGrouping.TAG, "AddressDBHelper.onUpgrade()");
        
        // TODO...
        db.execSQL(SQL_DELETE_LOCATION_TABLE);

        onCreate(db);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(PictureGrouping.TAG, "AddressDBHelper.onDowngrade()");
        
        onUpgrade(db, oldVersion, newVersion);
    }
}

/* EOF */


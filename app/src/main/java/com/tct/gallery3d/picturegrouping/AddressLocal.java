package com.tct.gallery3d.picturegrouping;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.util.Log;

import java.text.DecimalFormat;

public class AddressLocal {
    public static final String TAG = AddressLocal.class.getSimpleName();
    private AddressDBHelper mDBHelper;

    public AddressLocal(Context mContext) {
        mDBHelper = new AddressDBHelper(mContext);
    }

    public void saveToDatabase(Address address, double latitude, double longitude) {
        try {
            String tableName = AddressDBContract.AddressTable.TABLE_NAME;

            // Clear everything and query missing data
            if (address == null) return;
            long rowId;
            SQLiteDatabase db = mDBHelper.getWritableDatabase();

            try {
                db.beginTransaction();
                ContentValues values = new ContentValues();
                values.put(AddressDBContract.AddressTable._LATITUDE_HASH, dataFormat(latitude));
                values.put(AddressDBContract.AddressTable._LONGITUDE_HASH, dataFormat(longitude));
                values.put(AddressDBContract.AddressTable._ADDRESS_SET, AddressDBContract.packAddress(address));
                values.put(AddressDBContract.AddressTable._FAIL_COUNT, 0);
                values.put(AddressDBContract.AddressTable._FAIL_TIMESTAMP, -1);

                rowId = db.insert(tableName, null, values);
                if (rowId < 0) {
                    throw new Exception("*** Insertion failed for " + address);
                }
                if (rowId >= 0) {
                    Log.d(TAG, "Inserted one item into the location table: " + address);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (PictureGrouping.DEBUG_ADDRESS_CACHE) {
            Log.i(TAG, "} AddressCache.saveToDatabase()");
        }
    }


    public Address queryFromDatabase(double latitudeHash, double longitudeHash) {
        String localAddressString = null;

        Cursor cursor = null;

        latitudeHash = Double.valueOf(dataFormat(latitudeHash));
        longitudeHash = Double.valueOf(dataFormat(longitudeHash));

        try {
            String tableName = AddressDBContract.AddressTable.TABLE_NAME;
            String[] projection = null; // Let's get everything (just for test)
            String selection = AddressDBContract.AddressTable._LATITUDE_HASH + " = + " + latitudeHash + " AND " + AddressDBContract.AddressTable._LONGITUDE_HASH + " = " + longitudeHash;
            String[] selectionArgs = null;
            String sortOrder = null;

            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            try {
                db.acquireReference();
                cursor = db.query(tableName,  // The table to query
                        projection,           // The columns to return
                        selection,            // The columns for the WHERE clause
                        selectionArgs,        // The values for the WHERE clause
                        null,                 // don't group the rows
                        null,                 // don't filter by row groups
                        sortOrder);           // The sort order

                int addressSetIndex = cursor.getColumnIndex(AddressDBContract.AddressTable._ADDRESS_SET);

                while (cursor.moveToNext()) {
                    try {
                        localAddressString = cursor.getString(addressSetIndex);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.releaseReference();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AddressDBContract.unPackAddress(localAddressString);
    }

    private String dataFormat(double location) {
        DecimalFormat df = new DecimalFormat("#.000");
        return df.format(location);
    }
}

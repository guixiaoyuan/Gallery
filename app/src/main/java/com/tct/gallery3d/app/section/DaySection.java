package com.tct.gallery3d.app.section;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.text.format.DateUtils;

import com.tct.gallery3d.R;
import com.tct.gallery3d.data.MomentsNewAlbum;
import com.tct.gallery3d.image.AsyncTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DaySection extends SectionCursor<SectionInfo> {

    private static final int DEFAULT_INDEX = 5;

    public DaySection(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public SectionInfo createSection(Cursor cursor) {
//        getWrappedCursor().moveToPosition(position);
//        long dateAdded = getWrappedCursor().getLong(MomentsNewAlbum.INDEX_DATE_TAKEN);

        long dateAdded = cursor.getLong(MomentsNewAlbum.INDEX_DATE_TAKEN);
        double latitude = SectionInfo.INVALID_LOCATION;
        double longitude = SectionInfo.INVALID_LOCATION;
        if (!cursor.isNull(MomentsNewAlbum.INDEX_LATITUDE)){
            latitude = cursor.getDouble(MomentsNewAlbum.INDEX_LATITUDE);
        }
        if (!cursor.isNull(MomentsNewAlbum.INDEX_LONGITUDE)) {
            longitude = cursor.getDouble(MomentsNewAlbum.INDEX_LONGITUDE);
        }
        return createSectionInfo(getContext(), dateAdded, latitude, longitude);
    }

    @Override
    public void createLocation(List<SectionInfo> sections) {
        if (sections == null) {
            return;
        }
        AsyncTask<List<SectionInfo>, Void, Void> task = new AsyncTask<List<SectionInfo>, Void, Void>() {
            @Override
            protected Void doInBackground(List<SectionInfo>... params) {
                List<SectionInfo> sections = params[0];
                for (SectionInfo section : sections) {
                    if (!section.hasCoordinates()) continue;
                    double latitude = section.getLatitude();
                    double longitude = section.getLongitude();
                    Address localAddress = null;
                    if (mAddressLocal != null) {
                        localAddress = mAddressLocal.queryFromDatabase(latitude, longitude);
                    }
                    if (localAddress == null && latitude != SectionInfo.INVALID_LOCATION && longitude != SectionInfo.INVALID_LOCATION) {
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        try {
                            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, DEFAULT_INDEX);
                            for (Address address : addressList) {
                                if (address.getAddressLine(2) != null) {
                                    mAddressLocal.saveToDatabase(address, latitude, longitude);
                                    localAddress = address;
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    section.setAddress(localAddress);
                }
                return null;
            }
        };
        task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, sections);
    }


    public SectionInfo createSectionInfo(Context context, long timeMillis, double latitude, double longitude) {
        SectionInfo info = new SectionInfo();
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(System.currentTimeMillis());

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeMillis);

        String time = null;
        if (current.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
            if (current.get(Calendar.MONTH) == date.get(Calendar.MONTH)) {
                if (current.get(Calendar.DATE) == date.get(Calendar.DATE)) {
                    time = context.getString(R.string.moment_album_today);
                } else if (current.get(Calendar.DATE) == date.get(Calendar.DATE) + 1) {
                    time = context.getString(R.string.moment_album_yesterday);
                }
            }
            if (time == null) {
                time = DateUtils.formatDateTime(context, timeMillis, DateUtils.FORMAT_SHOW_DATE);
            }
        } else {
            time = DateUtils.formatDateTime(context, timeMillis, DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE);
        }
        info.setLatitude(latitude);
        info.setLongitude(longitude);
        info.setTime(time);
        return info;
    }

}

package com.tct.gallery3d.app.section;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.format.DateUtils;

import com.tct.gallery3d.data.MomentsNewAlbum;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class MonthSection extends SectionCursor<SectionInfo> {

    public MonthSection(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public SectionInfo createSection(Cursor cursor) {
//        getWrappedCursor().moveToPosition(position);
//        long dateAdded = getWrappedCursor().getLong(MomentsNewAlbum.INDEX_DATE_TAKEN);
        long dateAdded = cursor.getLong(MomentsNewAlbum.INDEX_DATE_TAKEN);
        return formatDateByMonth(getContext(), dateAdded);
    }

    @Override
    public void createLocation(List<SectionInfo> sections) {

    }

    public SectionInfo formatDateByMonth(Context context, long timeMillis) {
        SectionInfo info = new SectionInfo();
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(System.currentTimeMillis());

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeMillis);
        String time;
        if (current.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
            time = DateUtils.formatDateTime(context, timeMillis, DateUtils.FORMAT_NO_MONTH_DAY);
        } else {
            time = DateUtils.formatDateTime(context, timeMillis, DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY);
        }
        info.setTime(time);
        return info;
    }

}

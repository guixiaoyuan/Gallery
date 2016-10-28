package com.tct.gallery3d.app.section;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;

import com.tct.gallery3d.picturegrouping.AddressLocal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class SectionCursor<T> extends CursorWrapper {

    protected static final String TAG = SectionCursor.class.getSimpleName();
    private Context mContext;
    private int mPosition;
    protected AddressLocal mAddressLocal = null;

    private TreeMap<Integer, T> mSections = new TreeMap<>();
    private List<T> mLocations = new ArrayList<>();

    public SectionCursor(Context context, Cursor cursor) {
        super(cursor);
        mContext = context;
        mAddressLocal = new AddressLocal(context);
        buildSection();
        createLocation(mLocations);
    }

    public Context getContext() {
        return mContext;
    }

    public synchronized void buildSection() {
        int position = 0;
        int count = 0;
        T section = null;
        HashMap<Integer, T> sections = new HashMap<>();
        List<T> locations = new ArrayList<>();
        Cursor cursor = getWrappedCursor();
        if(cursor != null){
            cursor.moveToFirst();

            int total = cursor.getCount();
            if(total > 0){
                do {
                    T curSection = createSection(cursor);
                    if (curSection == null) {
                        throw new IllegalStateException("Section should not be null");
                    }
                    if (!curSection.equals(section)) {
                        sections.put(position + count, curSection);
                        count++;
                        // Add the section which need get the location to the list.
                        locations.add(curSection);
                    }
                    section = curSection;
                    position++;
                } while (cursor.moveToNext());
            }
        }

        mSections.clear();
        if (sections.size() > 0) {
            mSections.putAll(sections);
        }
        // Clear the old sections, fill the sections that need get the location.
        mLocations.clear();
        if (locations.size() > 0) {
            mLocations.addAll(locations);
        }
    }

    public boolean isSection(int position) {
        return mSections.get(position) != null;
    }

    public int getSectionCount(int position) {
        int count = 0;
        TreeMap<Integer, T> sections = (TreeMap<Integer, T>) mSections.clone();
        for (Integer sectionPos : sections.keySet()) {
            if (sectionPos < position) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    public int getRelPosition(int position) {
        TreeMap<Integer, T> sections = (TreeMap<Integer, T>) mSections.clone();
        for (Integer sectionPos : sections.keySet()) {
            if (sectionPos < position) {
                position++;
            } else {
                break;
            }
        }
        return position;
    }

    public abstract T createSection(Cursor cursor);

    /**
     * 1.Query location from data base.
     * 2.If the data base didn't restore the data, search the location from the network.
     * 3.Update the @SectionInfo.
     * 4.Notify the main thread to update the UI.
     * @param locations contains the all header sections
     */
    public abstract void createLocation(List<T> locations);

    public T getSection(int position) {
        TreeMap<Integer, T> sections = (TreeMap<Integer, T>) mSections.clone();

        if (sections.get(position) != null) {
            return sections.get(position);
        }
        int prevSectionPos = position;
        for (Integer sectionPos : sections.keySet()) {
            if (sectionPos < position) {
                prevSectionPos = sectionPos;
            } else {
                break;
            }
        }
        return sections.get(prevSectionPos);
    }

    private int getCursorPosition(int position) {
        int sectionCount = 0;
        TreeMap<Integer, T> sections = (TreeMap<Integer, T>) mSections.clone();
        for (Integer sectionPos : sections.keySet()) {
            if (sectionPos < position) {
                sectionCount++;
            } else {
                break;
            }
        }
        position -= sectionCount;
        return position;
    }

    @Override
    public int getCount() {
        return super.getCount() + mSections.size();
    }

    @Override
    public boolean moveToPosition(int position) {
        mPosition = position;
        if (!isSection(position)) {
            position = getCursorPosition(position);
            return super.moveToPosition(position);
        }
        return true;
    }

    @Override
    public int getPosition() {
        return mPosition;
    }

    @Override
    public boolean moveToNext() {
        return moveToPosition(mPosition + 1);
    }

    @Override
    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    @Override
    public boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    @Override
    public boolean move(int offset) {
        return moveToPosition(mPosition + offset);
    }

    @Override
    public boolean moveToPrevious() {
        return moveToPosition(mPosition - 1);
    }

    @Override
    public String getString(int columnIndex) {
        if (isSection(mPosition)) {
            throw new IllegalStateException("Can't access section row.");
        }
        return super.getString(columnIndex);
    }

    @Override
    public int getInt(int columnIndex) {
        if (isSection(mPosition)) {
            throw new IllegalStateException("Can't access section row.");
        }
        return super.getInt(columnIndex);
    }

    @Override
    public float getFloat(int columnIndex) {
        if (isSection(mPosition)) {
            throw new IllegalStateException("Can't access section row.");
        }
        return super.getFloat(columnIndex);
    }

    @Override
    public long getLong(int columnIndex) {
        if (isSection(mPosition)) {
            throw new IllegalStateException("Can't access section row.");
        }
        return super.getLong(columnIndex);
    }

    @Override
    public short getShort(int columnIndex) {
        if (isSection(mPosition)) {
            throw new IllegalStateException("Can't access section row.");
        }
        return super.getShort(columnIndex);
    }

    @Override
    public byte[] getBlob(int columnIndex) {
        if (isSection(mPosition)) {
            throw new IllegalStateException("Can't access section row.");
        }
        return super.getBlob(columnIndex);
    }

    @Override
    public int getColumnIndex(String columnName) {
        return super.getColumnIndex(columnName);
    }
}

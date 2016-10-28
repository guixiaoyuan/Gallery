package com.tct.gallery3d.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.tct.gallery3d.data.LocalVideoAlbum;
import com.tct.gallery3d.data.MediaSet;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AlbumSortUtils {
    private static final String TAG = "AlbumSortUtils";
    public static final String ALBUM_SORT_KEY = "album_sort_key";
    private static final String SORT_ERROR_MESSAGE = "album sort by date error";
    public static final int ALBUM_SORT_BY_DEFAULT = 0;
    public static final int ALBUM_SORT_BY_COUNT = 1;
    public static final int ALBUM_SORT_BY_DATE = 2;
    public static final int ALBUM_SORT_BY_NAME = 3;

    private static final int SORT_BORFOE_OTHERS = -1;
    private static final int SORT_AFTER_OTHERS = 1;
    private static final int SORT_NOT_CHANGE = 0;


    private static AlbumSortUtils mAlbumSort = null;
    private Comparator<MediaSet> mComparator;
    private ArrayList<SortListener> mListeners = new ArrayList<SortListener>();

    public static int getSelectItemByType(Context context) {
        final SharedPreferences settings = context.getSharedPreferences(
                AlbumSortUtils.ALBUM_SORT_KEY, 0);
        final int sortType = settings.getInt(AlbumSortUtils.ALBUM_SORT_KEY, AlbumSortUtils.ALBUM_SORT_BY_DEFAULT);
        return  sortType;
    }

    public static AlbumSortUtils getAlbumSort() {
        if (mAlbumSort == null) {
            mAlbumSort = new AlbumSortUtils();
        }
        return mAlbumSort;
    }

    public void setListener(SortListener listener) {
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void setSortType(int sortBy) {
        switch (sortBy) {
            case ALBUM_SORT_BY_COUNT:
                mComparator = new Comparator<MediaSet>() {
                    @Override
                    public int compare(MediaSet o1, MediaSet o2) {
                        int i1 = o1.getMediaItemCount();
                        int i2 = o2.getMediaItemCount();
                        if (i1 - i2 > 0) return SORT_BORFOE_OTHERS;
                        if (i1 - i2 < 0) return SORT_AFTER_OTHERS;
                        return SORT_NOT_CHANGE;
                    }
                };
                break;
            case ALBUM_SORT_BY_DATE:
                mComparator = new Comparator<MediaSet>() {
                    @Override
                    public int compare(MediaSet o1, MediaSet o2) {
                        try {
                            String path1 = o1.getAlbumFilePath();
                            String path2 = o2.getAlbumFilePath();

                            long o1ModifiedInMs = 0;
                            long o2ModifiedInMs = 0;
                            if (o1.getCoverMediaItem() == null) {
                                if (o1 instanceof LocalVideoAlbum) {
                                    o1ModifiedInMs = ((LocalVideoAlbum) o1).getLastModified();
                                } else {
                                    o1ModifiedInMs = (new File(path1).lastModified());
                                }
                            } else {
                                o1ModifiedInMs = (o1.getCoverMediaItem().get(0).getDateModifiedInMs());
                            }
                            if (o2.getCoverMediaItem() == null) {
                                if (o2 instanceof LocalVideoAlbum) {
                                    o2ModifiedInMs = ((LocalVideoAlbum) o2).getLastModified();
                                } else {
                                    o2ModifiedInMs = (new File(path2).lastModified());
                                }
                            } else {
                                o2ModifiedInMs = o2.getCoverMediaItem().get(0).getDateModifiedInMs();
                            }
                            long num = o1ModifiedInMs - o2ModifiedInMs;

                            if (num > 0) return SORT_BORFOE_OTHERS;
                            if (num < 0) return SORT_AFTER_OTHERS;
                            return SORT_NOT_CHANGE;
                        } catch (Exception e) {
                            Log.e(TAG, SORT_ERROR_MESSAGE + e);
                            return SORT_NOT_CHANGE;
                        }
                    }
                };
                break;
            case ALBUM_SORT_BY_NAME:
                mComparator = new Comparator<MediaSet>() {
                    @Override
                    public int compare(MediaSet o1, MediaSet o2) {
                        Collator myCollator = Collator.getInstance(java.util.Locale.CHINA);
                        int ret = myCollator.compare(o1.getName().toLowerCase(), o2.getName().toLowerCase());
                        return ret;
                    }
                };
                break;
            default:
                mComparator = new Comparator<MediaSet>() {
                    @Override
                    public int compare(MediaSet o1, MediaSet o2) {
                        return SORT_NOT_CHANGE;
                    }
                };
                break;
        }

        setTypeChange();
    }

    private void setTypeChange() {
        if (mListeners != null) {
            for (int i = 0; i < mListeners.size(); i++) {
                mListeners.get(i).sortTypeChanged();
            }
        }
    }

    public void sort(ArrayList<MediaSet> list,Context context) {
        if (getSelectItemByType(context) == ALBUM_SORT_BY_DEFAULT) {
            return;
        }
        if (mComparator == null) {
            initType(context);
        }
        Collections.sort(list, mComparator);
    }

    private void initType(Context context) {
        setSortType(getSelectItemByType(context));
    }

    public static interface SortListener {
        public void sortTypeChanged();
    }
}

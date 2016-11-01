package com.tct.gallery3d.data;

import java.util.ArrayList;
import java.util.List;

public class AlbumSetManager {
    private List<MediaSet> all = new ArrayList<>();
    private static AlbumSetManager sInstance;

    public AlbumSetManager() {

    }

    public static AlbumSetManager getInstance() {
        if (sInstance == null) {
            sInstance = new AlbumSetManager();
        }
        return sInstance;
    }

    public List<MediaSet> getAllALbum() {
        return all;
    }

    public void setAll(List<MediaSet> all) {
        this.all = all;
    }
}

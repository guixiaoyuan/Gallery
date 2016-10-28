package com.tct.gallery3d.data;

/**
 * Created by ts on 16-9-18.
 */

public class GappTypeInfo {

    public static final String GAPP_MEDIA_TYPE = "gapp_media_type";
    public static final String GAPP_BURST_ID = "gapp_burst_id";
    public static final String GAPP_BURST_INDEX = "gapp_burst_index";
    private int type;
    private int burstshotId;
    private int burstshotIndex;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBurstshotId() {
        return burstshotId;
    }

    public void setBurstshotId(int burstshotId) {
        this.burstshotId = burstshotId;
    }

    public int getBurstshotIndex() {
        return burstshotIndex;
    }

    public void setBurstshotIndex(int burstshotIndex) {
        this.burstshotIndex = burstshotIndex;
    }

    @Override
    public String toString() {
        return "GappTypeInfo{" +
                "type=" + type +
                ", burstshotId=" + burstshotId +
                ", burstshotIndex=" + burstshotIndex +
                '}';
    }
}

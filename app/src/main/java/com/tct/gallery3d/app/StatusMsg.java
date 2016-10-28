package com.tct.gallery3d.app;

public class StatusMsg {

    public static final String REMOTE_PATH = "00019700101000000043";
    public static final String PHOTO_ID = "PHOTO_ID";
    public static final String PHOTO_LIST = "PHOTO_LIST";
    public static final String STATUS = "STATUS";
    public static final String CANCEL_CODE = "CANCEL_CODE";

    public static final int TYPE_BACKUP = 1;
    public static final int TYPE_RESTORE = 2;

    public static final int GET_THUMB_SUCCESS = 1008;
    public static final int GET_THUMB_ERROR = 1009;

    public static final int LOAD_DATA_BEGIN = 1016;
    public static final int LOAD_DATA_END = 1017;
    public static final int TRANSFER_CANCEL = 1018;

    public static final int CANT_BACKUP_VIDEO = 1019;
    public static final int GET_IMAGE_PATH_BEGIN = 1020;
    public static final int GET_IMAGE_PATH_END = 1021;

    public static final int BACKUP_PHOTOS_BEGIN = 1022;
    public static final int RESTORE_PHOTOS_BEGIN = 1023;

    public static final int BACKUP_ERROR = 1024;
    public static final int ABANDON_BACKUP = 1025;

    public static final int TRANSFER_EXCEPTION = 1026;
    public static final int NETWORK_ERROR = 1027;

    public static final int NEW_PHOTOS_SEARCHED = 1028;
    public static final int CLEAR_DIRTY_DATA = 1029;

    public static final int DELETE_PHOTOS_FINISHED = 1030;
    public static final int DELETE_PHOTOS_BEGIN = 1031;

    public static final int FORCE_CLOSE_DIALOG = 1032;

    public static final int PHOTO_IS_BROKEN = 1033;
    
    public static final int PHOTO_DOWNLOAD = 1034;

    public static final int NEW_PHOTO_ADD = 1035;
}

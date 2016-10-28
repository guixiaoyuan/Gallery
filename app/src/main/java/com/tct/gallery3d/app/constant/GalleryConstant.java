package com.tct.gallery3d.app.constant;

public abstract class GalleryConstant {

    public static final boolean DEBUG = false;

    public static final String KEY_FROM_PAGE = "from_page";
    public static final int FROM_NONE_PAGE = -1;
    public static final int FROM_MOMENTS_PAGE = 0;
    public static final int FROM_ALBUMSET_PAGE = 1;
    public static final int FROM_ALBUM_PAGE = 2;
    public static final int FROM_PHOTODETAIL_PAGE = 3;
    public static final int FROM_FACESHOW_PAGE = 4;

    public static final String KEY_MEDIA_PATH = "media-path";
    public static final String KEY_MEDIA_NAME = "media-name";
    public static final String KEY_SET_TITLE = "media-title";
    public static final String KEY_MEDIA_SET_PATH = "media-set-path";
    public static final String KEY_MEDIA_ITEM_PATH = "media-item-path";
    public static final String KEY_MEDIA_ITEM_COUNT = "media-item-count";
    public static final String KEY_PATH_RETURN = "path-return";

    public static final String KEY_INDEX_SLOT = "index-slot";
    public static final String KEY_INDEX_HINT = "index-hint";

    public static final String MUTE_TRIM_URI = "mute-or-trim";
    public static final int REQUEST_TRIM_MUTE = 6;
    public static final int REQUEST_BURSTSHOT = 7;
    public static final int REQUEST_COPY = 100;
    public static final int REQUEST_MOVE = 101;
    public static final int REQUEST_NEW_ALBUM = 102;
    public static final int REQUEST_RENAME_ALBUM = 103;

    public static final int START_SLIDE_SHOW_MSG = 1018;

    public static final int REQUEST_COLLAPSE_CODE = 1019;

    public static final String ACTION_EDIT = "com.muvee.iclipeditor.EDIT";
    public static final String ACTION_TRIM = "com.muvee.iclipeditor.TRIM";
    public static final String EXTRA_USE_CUSTOM = "com.muvee.share.usecustom";
    public static final String ACTION_SLOW_MO = "com.muvee.slowmoedit.SLOW_MO";
    public static final String ACTION_SLOW_MO_PLAY = "com.muvee.slowmoedit.SLOW_MO_PLAY";
    public static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";
    public static final String ACTION_DRM_ATTACH_DATA = "android.drm.action.ATTACH_DATA";

    //faceshow
    public static final String DEFAULT_PATH = "/local/image/item";
    public static final String MUVEE_FACESHOW_FACE_SHOW = "com.muvee.faceshow.FACE_SHOW";
    public static final String MUVEE_FACESHOW_THEME = "com.muvee.faceshow.theme";
    public static final String MUVEE_FACESHOW_SELFIEFOLDER = "com.muvee.faceshow.selfiefolder";
    public static final String MUVEE_FACESHOW_IMAGELIST = "com.muvee.faceshow.imagelist";
    public static final String MUVEE_FACESHOW_OUTPUT_RESOLUTION = "com.muvee.faceshow.output.resolution";
    public static final String MUVEE_FACESHOW_OUTPUT_BITRATE = "com.muvee.faceshow.output.bitrate";
    public static final String MUVEE_SHARE_USECUSTOM = "com.muvee.share.usecustom";

    public static final String FACESHOW_SELFIEFOLDER = "/storage/emulated/0/DCIM/Selfie";
    public static final int MUVEE_FACESHOW_THEME_ID = 3;
    public static final String DEFAULT_FACESHOW_OUTPUT_RESOLUTION = "720x720";
    public static final int DEFAULT_FACESHOW_OUTPUT_BITRATE = 8000000;
    public static final String DEF_NAME = "def_gallery_custom_share_enable";
    public static final  int minMuveeCount = 5;

    public static final String COLLAPSE_DATA_NAME = "CollapseAlbumData";

    public static final int DEFAULT_INDEX = 1;
    public static final int INVALID_INDEX = -1;
    public static final int DEFAULT_ITEM_COUNT = 1;
    public static final int INVALID_ITEM_COUNT = -1;

    public static final float MAX_SCALE = 4f;//The max scale value of Photo preview

    public static final int ITEM_IS_DRM = 1;

    public static final int PUBLIC_ITEM = 0 ;
    public static final int PRIVATE_ITEM = 1;

    public static final String IS_PRIVATE = "tct_is_private";

    public static final int NO_COLUMN_RETURN_VALUE = -1;
    public static final String NO_COLUMN = "0";

    public static final int MENU_CLOSE_ANIMATION_DURATION = 350;
}

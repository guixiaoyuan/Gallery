package com.tct.gallery3d.polaroid;

public class Poladroid {
    public static final String TAG = "Poladroid";

    // Priorities for image processing (on background thread)
    public static final int THUMB_SIZE_PRIORITY = 1;
    public static final int PREVIEW_SIZE_PRIORITY = 2;
    public static final int FULL_SIZE_PRIORITY = 3;

    // Some message ids for communication between foreground and background
    // threads
    public static final int UI2BG_ADD_FILTER_CMD = 1;
    public static final int UI2BG_REMOVE_FILTER_CMD = 2;
    public static final int UI2BG_PAUSE = 3; // TODO
    public static final int UI2BG_RESUME = 4; // TODO
    public static final int UI2BG_STOP = 5; // TODO
    public static final int BG2UI_FILTER_CMD_COMPLETE = 6;

}

/* EOF */


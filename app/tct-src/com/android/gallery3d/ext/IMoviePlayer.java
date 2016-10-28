/* ========================================================================== */
/* Modifications on Features list / Changes Request / Problems Report         */
/* -------------------------------------------------------------------------- */
/* date      | author         | key              | comment (what, where, why) */
/* ----------|----------------|------------------|--------------------------- */
/* 08/30/2013| jiawei.li      | FR-487461        | [Ergo] Video player        */
/*-----------|----------------|------------------|--------------------------- */
/* ========================================================================== */

package com.android.gallery3d.ext;

/**
 * MoviePlayer extension functions interface
 */
public interface IMoviePlayer {
    /**
     * add new bookmark Uri.
     */
    //void addBookmark();
    /**
     * start current item and stop playing video.
     * @param item
     */
    void startNextVideo(IMovieItem item);
    /**
     * Loop current video.
     * @param loop
     */
    void setLoop(boolean loop);
    /**
     * Loop current video or not
     * @return
     */
    boolean getLoop();
    /**
     * Show video details.
     */
    //void showDetail();
    /**
     * Can stop current video or not.
     * @return
     */
    boolean canStop();
    /**
     * Stop current video.
     */
    void stopVideo();
}

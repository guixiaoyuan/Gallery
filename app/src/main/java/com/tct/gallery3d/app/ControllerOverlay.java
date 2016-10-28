/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* ----------|----------------------|----------------------|----------------- */
/* 21/01/2015|jian.pan1             |FR904501              |Gallery Ergo 5.1.4 -
/*           |                      |                      |Fast forward and reverse
/* ----------|----------------------|----------------------|----------------- */
/* 04/03/2015|dongliang.feng        |CR940102              |[Gallery_Ergo_5.1.9.pdf]Video Lock Function */
/* ----------|----------------------|----------------------|----------------- */
/* 04/29/2015|dongliang.feng        |CR989796              |[5.0][Gallery]video play backward/forward */
/* ----------|----------------------|----------------------|----------------- */
/* 28/10/2015|    su.jiang     |  PR-791930    |[Android5.1][Gallery_v5.2.3.1.1.0307.0]It can play or paused video when locking antion bar*/
/*-----------|-----------------|---------------|------------------------------------------------------------------------------------------*/

package com.tct.gallery3d.app;

import android.view.View;

public interface ControllerOverlay {

  interface Listener {
    void onPlayPause();
    void onSeekStart();
    void onSeekMove(int time);
    void onSeekEnd(int time, int trimStartTime, int trimEndTime);
    void onShown();
    void onHidden();
    void onReplay();
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 begin
    void onFastForward();
    void onReverse();
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-21,PR904501 end

    //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    //get current video is from RTSP
    boolean onIsRTSP();
    //[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
    // [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014, For FR828601 Pop-up Video play
    void onShowPopupVideo();
    // [FEATURE]-Add-END by jian.pan1
    void onControlSystemUI(boolean isHideSytemUI);
    void udpateTimeBar();//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-10-28,PR791930
  }

  interface PlayerControlPanelListener {
    void onLoopMode(int mode);
    void onFullScreenMode(int mode);
    void onVideoLockMode(int mode); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-04, CR940102
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 begin
    void onVideoFastForwardOrBackward(boolean isForward);
    void updatePlayerControlShowState(boolean showing);
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-04-29, CR989796 end
  }

  void setListener(Listener listener);

  void setCanReplay(boolean canReplay);

  /**
   * @return The overlay view that should be added to the player.
   */
  View getView();

  void show();

  void showPlaying();

  void showPaused();

  void showEnded();

  void showLoading();

  void showErrorMessage(String message);

  void setTimes(int currentTime, int totalTime,
          int trimStartTime, int trimEndTime);

  //[FEATURE]-Add-BEGIN by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
  //set view enabled (play/pause asynchronous processing)
  void setViewEnabled(boolean isEnabled);
  //view from disable to resume (play/pause asynchronous processing)
  void setPlayPauseReplayResume();
//[FEATURE]-Add-END by TCTNB(Haoli Zhang), 2014/11/5, FR-824326 No WiFi Display Extension Mode
}

package com.android.gallery3d.popupvideo;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.MovieActivity;
import com.tct.gallery3d.util.PLFUtils;

import android.content.Context;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

// [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014,FR828601 Pop-up Video play
public class TctPopupVideoComponent {
    public static final String TAG = "TctPopupVideoComponent";

    private static TctPopupVideoComponent mInstance = null;

    private Context mContext;

    private int COLOR_BORDER_SHOW;
    private int COLOR_BORDER_HIDE;

    private float mDefaultLongEdgeLength = 0.0f;

    private TctPopupWindow mPopupWindow = null;
    private View mRootView = null;
    private ImageButton mCloseButton = null;
    private TctPopupVideoView mVideoView = null;
    private ImageView mAudioCover = null;
    private UiState mUiState = new UiState();

    public static final String ACTION_SUPERMODE = "android.intent.jrdcom.action.ACTION_SUPERMODE";
    public static final String EXTRA_SUPERMODE_STATUS = "supermode_status";
    private int mResumePosition = -1;
    private boolean mIsFeatureOn = false;

    /**
     * Here we manage the state of UI.
     */
    private class UiState {
        private boolean mMoving = false;
        private boolean mPlaying = false;

        /**
         * Set UI state as moving.<br>
         * With moving state, only video view is displayed.
         * @param moving true: moving state, false: non-moving state
         * @see #setPlaying(boolean)
         */
        public void setMoving(boolean moving) {
            if (mMoving != moving) {
                mMoving = moving;

                updateUi();
            }
        }

        /**
         * Set UI state as playing.<br>
         * With playing state, only border & video view is displayed. With
         * non-playing state, everything is displayed
         * @param playing
         * @see #setMoving(boolean)
         */
        public void setPlaying(boolean playing) {
            if (mPlaying != playing) {
                mPlaying = playing;

                updateUi();
            }
        }

        /**
         * update UI according to the status.
         * @see #setMoving(boolean)
         * @see #setPlaying(boolean)
         */
        private void updateUi() {
            if (mMoving) {
                // in moving status, DO NOT show anything except the video
                mRootView.setBackgroundColor(COLOR_BORDER_HIDE);
                mCloseButton.setVisibility(View.INVISIBLE);
            } else {
                mRootView.setBackgroundColor(COLOR_BORDER_SHOW);

                if (mPlaying) {
                    // video is playing
                    mCloseButton.setVisibility(View.INVISIBLE);
                } else {
                    // video is paused
                    mCloseButton.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private TctPopupWindow.OnStatusChangedListener mOnWindowStatusChangedListener = new TctPopupWindow.OnStatusChangedListener() {
        @Override
        public void onStatusChanged(int oldStatus, int newStatus) {
            switch (oldStatus) {
                case TctPopupWindow.STATUS_FLYING:    // leave flying mode
                case TctPopupWindow.STATUS_SCROLLING: // leave scrolling mode
                case TctPopupWindow.STATUS_SCALING:   // leave scaling mode
                    mUiState.setMoving(false);
                    break;
            }

            switch (newStatus) {
                case TctPopupWindow.STATUS_FLYING:    // enter flying mode
                case TctPopupWindow.STATUS_SCROLLING: // enter scrolling mode
                case TctPopupWindow.STATUS_SCALING:   // enter scaling mode
                    mUiState.setMoving(true);
                    break;
            }
        }
    };

    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        boolean pausedByCall = false;

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    if (pausedByCall) {
                        mPopupWindow.flyAway(false);

                        pausedByCall = false;
                    }
                    break;

                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    // do nothing
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    pauseVideo();
                    TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                    int callState = tm.getCallState();
                    if (callState != TelephonyManager.CALL_STATE_IDLE) {
                        // paused by a call
                        mPopupWindow.flyAway(true);
                        pausedByCall = true;
                    }
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // do nothing
                    break;
            }

        }
    };

    /**
     * Manage the Screen On / Off event
     */
    private BroadcastReceiver mSrceenOnOffReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                // Do not resume video after screen on
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                pauseVideo();
            }
        }
    };

    private BroadcastReceiver mSuperModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SUPERMODE.equals(intent.getAction())) {
                boolean isOn = intent.getBooleanExtra(EXTRA_SUPERMODE_STATUS, false);
                if (isOn) {
                    Log.i(TAG, "Exit popup video by super mode");
                    dismissPopupVideo();
                }
            }
        }
    };

    /**
     * Constructor method. Please get an instance via
     * {@link #getInstance(Context)}
     * @param context
     * @see #getInstance(Context)
     */
    private TctPopupVideoComponent(Context context) {
        mContext = context.getApplicationContext();

        COLOR_BORDER_SHOW = mContext.getResources().getColor(android.R.color.black);
        COLOR_BORDER_HIDE = mContext.getResources().getColor(android.R.color.transparent);

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mPopupWindow = new TctPopupWindow(mContext);
        mPopupWindow.setOnStatusChangedListener(mOnWindowStatusChangedListener);

        mIsFeatureOn = PLFUtils.getBoolean(mContext, "feature_gallery2_popupVideo_on");
    }

    /**
     * Obtain an instance of this class
     * @param context context
     * @return instance
     */
    public static TctPopupVideoComponent getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TctPopupVideoComponent(context);
        }
         if(TctPopupWindow.isCteatedPop()){
                  Log.i(TAG,"call dismissPopupVideo");
                  mInstance.dismissPopupVideo();
         }
        return mInstance;
    }

    /**
     * The default length of longer edge. The length is generated by 2/3 of
     * shorter edge of screen
     * @return length
     */
    protected float getDefaultLongEdgeLength() {
        if (mDefaultLongEdgeLength == 0.0f) {
            PointF windowSize = mPopupWindow.getScreenSize();
            mDefaultLongEdgeLength = Math.min(windowSize.x, windowSize.y) * 2 / 3;
        }

        return mDefaultLongEdgeLength;
    }

    /**
     * Whether pop up video is showing
     * @return true: showing, false: not shown
     */
    public boolean isPopupVideoShowing() {
        return mPopupWindow.isCreated();
    }

    /**
     * Show pop up video on the screen
     * @param uri URI of the video resource
     * @param position The position of the video
     * @see #dismissPopupVideo()
     */
    public void showPopupVideo(final Uri uri, int position) {
        if (mPopupWindow.isShowing()) {
            return;
        }

        Log.i(TAG, "Popup Video show");
        // MovieActivity.wakeLock.acquire();
        // Log.i(TAG, "MovieActivity wakeLock acquire");
        mRootView = mPopupWindow.setContentView(R.layout.popup_video_component_view);
        mCloseButton = (ImageButton) mRootView.findViewById(R.id.close_button);
        mVideoView = (TctPopupVideoView) mRootView.findViewById(R.id.video_view);
        mAudioCover = (ImageView) mRootView.findViewById(R.id.audio_cover);

        // Press on the window will play / pause the video
        mPopupWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoView.isPlaying()) {
                    pauseVideo();
                } else {
                    playVideo();
                }
            }
        });

        // Long press to back to Gallery
        mPopupWindow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mResumePosition = mVideoView.getCurrentPosition();
                dismissPopupVideo();

                Intent intent = new Intent(mContext, MovieActivity.class);
                intent.setData(uri);
                intent.setFlags((intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                        | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                mContext.startActivity(intent);
                return true;
            }
        });

        // Press on the resume button will close pop up window
        mCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // dismiss pop up video view
                dismissPopupVideo();
            }
        });

        // setup video
        setupVideo(uri, position, new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                dismissPopupVideo();
            }
        });

        // play video
        playVideo();

        // generate default window size.
        float videoWidth = mVideoView.getVideoWidth();
        float videoHeight = mVideoView.getVideoHeight();
        float longEdgeLength = getDefaultLongEdgeLength();
        if ((videoWidth == 0) || (videoHeight == 0)) {
            videoWidth = longEdgeLength;
            videoHeight = videoWidth * 9.0f / 16.0f;
            mVideoView.setVisibility(View.INVISIBLE);
            mAudioCover.setVisibility(View.VISIBLE);
        } else {
            if (videoWidth > videoHeight) {
                videoHeight = videoHeight * longEdgeLength / videoWidth;
                videoWidth = longEdgeLength;
            } else {
                videoWidth = videoWidth * longEdgeLength / videoHeight;
                videoHeight = longEdgeLength;
            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mSrceenOnOffReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(ACTION_SUPERMODE);
        mContext.registerReceiver(mSuperModeReceiver, filter);

        // show pop up window
        mPopupWindow.setWindowRect(0, 0, videoWidth, videoHeight);
        mPopupWindow.create();
        mPopupWindow.show();
    }

    /**
     * dismiss the pop up video from screen
     * @see #showPopupVideo(Uri, int)
     */
    public void dismissPopupVideo() {
        if (!mPopupWindow.isShowing() && !TctPopupWindow.isCteatedPop()) {
            return;
        }

        Log.i(TAG, "Popup Video dismiss");
        mContext.unregisterReceiver(mSrceenOnOffReceiver);

        mContext.unregisterReceiver(mSuperModeReceiver);

        // MovieActivity.wakeLock.release();
        // Log.i(TAG, "------MovieActivity.wakeLock.release()------");
        mPopupWindow.hide();
        mPopupWindow.close();
        mPopupWindow.setContentView(null);

        stopVideo();
    }

    /**
     * Pause popup video with resource released.
     */
    public void pausePopupVideoReleased() {
        if (mPopupWindow.isShowing()) {
            mUiState.setPlaying(false);
            int pos = mVideoView.getCurrentPosition();
            stopVideo();
            mVideoView.seekTo(pos);
        }
    }

    /**
     * Play video with audio focus requested
     */
    protected void playVideo() {
        // deal with audio focus
        mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        mUiState.setPlaying(mVideoView.play());
    }

    /**
     * Pause video
     */
    protected void pauseVideo() {
        mUiState.setPlaying(false);
        mVideoView.pause();

        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
    }

    /**
     * Stop video with audio focus abandoned
     */
    protected void stopVideo() {
        mUiState.setPlaying(false);
        mVideoView.stop();

        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
    }

    /**
     * Configure the video to be played
     * @param uri UIR of the video
     * @param position position of the video
     * @param listener receive complete event
     */
    protected void setupVideo(Uri uri, int position, OnCompletionListener listener) {
        mVideoView.setUri(uri);
        mVideoView.seekTo(position);
        mVideoView.setOnCompletionListener(listener);
    }

    /**
     * Get the position that movie player should be resumed. The value is
     * set when long press on the pop up window.<br>
     * NOTE: the value only can be get once. It will be reset to -1 as soon
     * as the method is called.
     */
    public int getResumePositionAndReset() {
        int pos = mResumePosition;
        mResumePosition = -1;
        return pos;
    }

    /**
     * Return whether pop up video feature is on.
     */
    public boolean isFeatureOn() {
        return mIsFeatureOn;
    }
}
// [FEATURE]-Add-END by jian.pan1

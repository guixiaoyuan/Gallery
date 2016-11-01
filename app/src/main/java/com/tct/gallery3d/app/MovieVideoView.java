/* ----------|----------------------|----------------------|------------------*/
/* 10/1/2015  |ye.chen               |PR898610              |[Video Streaming]Cannot recover in 5 minutes after pause for more than 1 minutes.
/* ----------|----------------------|----------------------|----------------- */
/* 19/01/2015|dongliang.feng        |PR906557              |[Gallery]Video gallery */
/*           |                      |                      |display abnormal pause icon */
/* ----------|----------------------|----------------------|----------------- */
/* ----------|----------------------|------------------- --|-------------------*/
/* 3/02/2015 |qiang.ding1           |PR901897              |[Clone][4.7][MMS][FM Radio] FM Radio
 *           |                      |                      |would stop working after
 *           |                      |                      |preview a mms with audio/video*/
/* ----------|----------------------|------------------ ---|-------------------*/
/* 04/02/2015|ye.chen               |PR918354              |[Streaming]The screen is asleep when buffering
/* ----------|----------------------|----------------------|----------------- */
/* 05/02/2015|ye.chen               |FR908268              |[Video streaming]It shouldn't pause live TV and prompt whether resume video
/* ----------|----------------------|----------------------|----------------- */
/* 10/02/2015|dongliang.feng        |PR928216              |[Video Streaming]The video */
/*           |                      |                      |continue to play directly after */
/*           |                      |                      |pause->lock->unlock screen */
/* ----------|----------------------|----------------------|----------------- */
/* 28/02/2015|dongliang.feng        |PR936095              |[Video Streaming][FC]It will force */
/*           |                      |                      |close when loop playing video streaming */
/* ----------|----------------------|----------------------|----------------- */
/* 03/03/2015|ye.chen               |CR938507              |video full screen as default
/* ----------|----------------------|----------------------|----------------- */
/* 03/12/2015|    ye.chen           |      CR-938507       |video full screen as default  */
/* ----------|----------------------|----------------------|-----------------------------------------*/
/* 14/03/2015|dongliang.feng        |PR948961              |[Android5.0][Gallery_v5.1.9.1.0103.0] */
/*           |                      |                      |[Video streaming]The device will stop */
/*           |                      |                      |loading online video when unlock the screen */
/* ----------|----------------------|----------------------|----------------- */
/* 03/23/2015|    ye.chen           |      PR-956416       |[SMC]com.tct.gallery3d happend wtf due android.util.Log$TerribleFailure
/* ----------|----------------------|----------------------|-----------------------------------------*/
/* 24/03/2015|dongliang.feng        |PR957026              |[clone][Gallery]When play video it will displays half screen */
/* ----------|----------------------|----------------------|----------------- */
/* 27/04/2015 |    jialiang.ren     |      PR-986309       |[HOMO][Orange][22][HLS] 1.05 - Audio Only - No default icon displayed*/
/*------------|---------------------|----------------------|---------------------------------------------------------------------*/
/* 07/07/2015 |    jialiang.ren     |      PR-1035544         |[Streaming]Background display black when play 3GP stream*/
/*------------|---------------------|-------------------------|--------------------------------------------------------*/
/* 09/07/2015 |    jialiang.ren     |      PR-1040213         |[Video Streaming]The progress bar has no response and "Can't play*/
/*                                                             this video."popup when pull progress bar with a video playing    */
/*------------|---------------------|-------------------------|-----------------------------------------------------------------*/

package com.tct.gallery3d.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.mozilla.universalchardet.UniversalDetector;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.MediaPlayer.TrackInfo;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

import com.tct.gallery3d.R;
import com.tct.gallery3d.util.PLFUtils;

// [FEATURE]-Add-BEGIN by jian.pan1,11/05/2014, For FR824779 Video subtitle
public class MovieVideoView extends SurfaceView implements MediaPlayerControl {

    private final static String TAG = "MovieVideoView";
    private Uri         mUri;
    private Map<String, String> mHeaders;
    private int         mDuration;

    private static final int STATE_ERROR              = -1;
    private static final int STATE_IDLE               = 0;
    private static final int STATE_PREPARING          = 1;
    private static final int STATE_PREPARED           = 2;
    private static final int STATE_PLAYING            = 3;
    private static final int STATE_PAUSED             = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private int mScreenMode = MovieControllerOverlay.FULLSCREEN_MODE_OFF;

    private int mCurrentState = STATE_IDLE;
    private int mTargetState  = STATE_IDLE;

    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-28,PR848284 begin
    private static final int VIDEO_FULL_SCREEN_OFFSET = 5;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-28,PR848284 end

    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private int         mAudioSession;
    private int         mVideoWidth;
    private int         mVideoHeight;
    private int         mSurfaceWidth;
    private int         mSurfaceHeight;
    private MediaController mMediaController;
    private OnCompletionListener mOnCompletionListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-24, PR957026
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private int         mCurrentBufferPercentage;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener  mOnInfoListener;
    private int         mSeekWhenPrepared;
    private boolean     mCanPause;
    private boolean     mCanSeekBack;
    private boolean     mCanSeekForward;

    private Context mContext;
    private Uri subtitleUri;
    private OnTimedTextListener mOnTimedTextListener;

    private PowerManager.WakeLock wakeLock = null;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-02-04,PR918354 begin

    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 begin
    private static final float mMinimumTransiantVolumeValue = 0.1f;
    private static final float mNormalVolumeValue = 1f;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 end

    public void setOnTimedTextListener(MediaPlayer.OnTimedTextListener mOnTimedTextListener) {
        this.mOnTimedTextListener = mOnTimedTextListener;
    }

    public void setSubtitle(Uri subtitleUri) {
        this.subtitleUri = subtitleUri;
    }

    public MovieVideoView(Context context) {
        super(context);
        mContext = context;
        initVideoView();
    }

    public MovieVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        initVideoView();
    }

    public MovieVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initVideoView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mScreenMode == MovieControllerOverlay.FULLSCREEN_MODE_OFF) {
            int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
            int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
            if (mVideoWidth > 0 && mVideoHeight > 0) {

                int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
                int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
                int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

                if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                    // the size is fixed
                    width = widthSpecSize;
                    height = heightSpecSize;

                    // for compatibility, we adjust size based on aspect ratio
                    if ( mVideoWidth * height  < width * mVideoHeight ) {
                        //Log.i("@@@", "image too wide, correcting");
                        width = height * mVideoWidth / mVideoHeight;
                    } else if ( mVideoWidth * height  > width * mVideoHeight ) {
                        //Log.i("@@@", "image too tall, correcting");
                        height = width * mVideoHeight / mVideoWidth;
                    }
                } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                    // only the width is fixed, adjust the height to match aspect ratio if possible
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                    if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                        // couldn't match aspect ratio within the constraints
                        height = heightSpecSize;
                    }
                } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                    // only the height is fixed, adjust the width to match aspect ratio if possible
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                    if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                        // couldn't match aspect ratio within the constraints
                        width = widthSpecSize;
                    }
                } else {
                    // neither the width nor the height are fixed, try to use actual video size
                    width = mVideoWidth;
                    height = mVideoHeight;
                    if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                        // too tall, decrease both width and height
                        height = heightSpecSize;
                        width = height * mVideoWidth / mVideoHeight;
                    }
                    if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                        // too wide, decrease both width and height
                        width = widthSpecSize;
                        height = width * mVideoHeight / mVideoWidth;
                    }
                }
            } else {
                // no size yet, just adopt the given spec sizes
            }
            setMeasuredDimension(width, height);
        } else if(mScreenMode == MovieControllerOverlay.FULLSCREEN_MODE_ON) {
            int width = getDefaultSize(0, widthMeasureSpec);
            int height = getDefaultSize(0, heightMeasureSpec);
            //[BUGFIX]-Add by TCTNJ,ye.chen, 2014-11-14,FR834074 begin
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-28,PR848284 begin
          //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-03,CR938507 begin
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            } else if (mVideoWidth > 0 && mVideoHeight > 0) {
                if (mVideoWidth * height >= width * mVideoHeight) {
                    width = width+VIDEO_FULL_SCREEN_OFFSET;
                } else if (mVideoWidth * height < width * mVideoHeight) {
                    width = width+VIDEO_FULL_SCREEN_OFFSET;
                }
            }
          //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-03,CR938507 begin
            Log.d(TAG, "onMeasure() set size: " + width + 'x' + height);
            Log.d(TAG, "onMeasure() video size: " + mVideoWidth + 'x' + mVideoHeight);
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-28,PR848284 end
            //[BUGFIX]-Add by TCTNJ,ye.chen, 2014-11-14,FR834074 end
            setMeasuredDimension(width, height);
        }
    }

    public void requestScreenMode(int screenMode) {
        Log.d(TAG, "requestScreenMode:" + screenMode);
        mScreenMode = screenMode;
        requestLayout();
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(MovieVideoView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(MovieVideoView.class.getName());
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = desiredSize;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(desiredSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState  = STATE_IDLE;
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState  = STATE_IDLE;
            releaseWakeLock();//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-23,PR956416 begin
        }
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            return;
        }

        release(false);
        try {
            mMediaPlayer = new MediaPlayer();
            acquireWakeLock();//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-02-04,PR918354 begin
            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }

            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mDuration = -1;
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(mContext, mUri, mHeaders);

            if (mContext.getResources().getBoolean(R.bool.feature_gallery2_subtitle_on)) {
                Log.d(TAG, "openVideo() Handle auto subtitle");
                String s = mUri.toString();
                s = handleContentUri(s);
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-12-03,PR824779 begin
                // decode special string eg.space etc.
                s = Uri.decode(s);
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-12-03,PR824779 end
                if(subtitleUri == null && s.startsWith("file://")){
                    s = s.substring(7, s.length());
                    File file = new File(s);
                    String fileName = file.getName();
                    int index = fileName.lastIndexOf(".");
                    if(index >= 0) {
                        String namePrefix = fileName.substring(0, index);
                        File parent = file.getParentFile();
                        File expectedSrtFile = new File(parent, namePrefix+".srt");
                        Log.d(TAG, "Expected Srt File: " + expectedSrtFile.toString());
                        if(expectedSrtFile.exists() && expectedSrtFile.isFile()){
                            subtitleUri = Uri.fromFile(expectedSrtFile);
                        }
                    }
                }
            }

            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);

            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 begin
            if (mShouldPause) {
                mOnShouldPause.onShouldPauseVideo();
            } else {
                mMediaPlayer.prepareAsync();
                mCurrentState = STATE_PREPARING;
            }
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 end

            attachMediaController();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-02-04,PR918354 begin
    private void acquireWakeLock() {
        if (wakeLock == null) {
            Log.d("debug", "Acquiring wake lock");
            PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |PowerManager.ON_AFTER_RELEASE, this.getClass().getCanonicalName());
            wakeLock.acquire();
            mSurfaceHolder.setKeepScreenOn(true);
        }
    }
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            Log.d("debug", "release wake lock");
            wakeLock.release();
            wakeLock = null;
            mSurfaceHolder.setKeepScreenOn(false);
        }
    }
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-02-04,PR918354 begin
    public String handleContentUri(String inputUri) {
        if(!inputUri.startsWith("content://"))
            return inputUri;
        Uri uri = Uri.parse(inputUri);
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Video.Media.DATA};
            ContentResolver resolver = mContext.getContentResolver();
            cursor = resolver.query(uri, proj, null, null, null);
            int index = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(index);
            return Uri.fromFile(new File(path)).toString();
        } catch(Exception e) {
            return inputUri;
        } finally {
            if(cursor != null)
                cursor.close();
        }
    }

    public void setMediaController(MediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ?
                    (View)this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
        new MediaPlayer.OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();

                //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-27,PR986309 begin
                if (mOnVideoSizeChangedListener != null) {//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-07,PR1035544
                    mOnVideoSizeChangedListener.onVideoSizeChanged(mp, mVideoWidth,
                            mVideoHeight);
                }
                //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-27,PR986309 end

                if (mVideoWidth != 0 && mVideoHeight != 0) {
                    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-24, PR957026 begin
                    getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                    requestLayout();
                    if (mOnVideoSizeChangedListener != null) {
                        mOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height);//[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-12, CR938507 begin
                    }
                    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-24, PR957026 end
                }
            }
    };

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-27,PR986309 begin
    public boolean isHlsResource() {
        return (mUri != null && (mUri.getScheme().equalsIgnoreCase("http") ||
                mUri.getScheme().equalsIgnoreCase("https")) && mUri.getPath().endsWith(".m3u8"));
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-27,PR986309 end

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-14,PR839341 begin
          //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
            // Get the capabilities of the player for this stream
            if (mp.getDuration() <= 0) {//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-07-09,PR1040213
                 mCanPause = mCanSeekBack = mCanSeekForward = false;
            } else {
                 mCanPause = mCanSeekBack = mCanSeekForward = true;
            }
          //[BUGFIX]-begin by TCTNJ.ye.chen,02/05/2015,908268
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-14,PR839341 end

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared;
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-19,PR824779 begin
            // fix bug subtitle not update after select subtitle from file manager
            if(subtitleUri != null) {
                String s = subtitleUri.toString();
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-12-03,PR824779 begin
                // decode special string eg.space etc.
                s = Uri.decode(s);
                //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-12-03,PR824779 end
                String srtFilePath = subtitleUri.getPath();
                if (s.endsWith(".srt")) {
                    Log.d(TAG, "AddTimedTextSource s:"+s+" PATH:"+srtFilePath);
                    if (s.startsWith("file://")) {
                        srtFilePath = s.substring(7, s.length());
                    }
                    if (TextUtils.isEmpty(srtFilePath))
                        return;
                    try{
                        srtFilePath = handleSubtitleCharset(srtFilePath);
                        mMediaPlayer.addTimedTextSource(srtFilePath, MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
                    } catch(Exception e) {
                        Log.i(TAG, "addTimedTextSource error e:"+e.getMessage());
                    }
                    Log.d(TAG, "Try to Select TimedText Track");
                    TrackInfo[] trackInfos = mMediaPlayer.getTrackInfo();
                    if(trackInfos!=null && trackInfos.length>0) {
                        for(int i=trackInfos.length-1; i >= 0; i--) {
                            TrackInfo info = trackInfos[i];
                            if(info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT){
                                Log.d(TAG, "Select TimedText Track");
                                mMediaPlayer.selectTrack(i);
                                mMediaPlayer.setOnTimedTextListener(mOnTimedTextListener);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "is not .srt file");
                }
            } else {
                Log.e(TAG, "subtitleUri is null");
            }
            releaseWakeLock();//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-02-04,PR918354 begin
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    if (mTargetState == STATE_PLAYING) {
                        start();
                        if (mMediaController != null) {
                            mMediaController.show();
                        }
                    } else if (!isPlaying() &&
                               (seekToPosition != 0 || getCurrentPosition() > 0)) {
                       if (mMediaController != null) {
                           mMediaController.show(0);
                       }
                   }
                }
            } else {
                if (mTargetState == STATE_PLAYING) {
                    Log.d(TAG, "onPrepared() , start to play");
                    start();
                }
            }
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-19,PR824779 end
        }
    };

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 begin
    public interface OnShouldPause {
        public void onShouldPauseVideo();
    }
    OnShouldPause mOnShouldPause;
    private boolean mShouldPause = false;

    public void setOnShouldPause(OnShouldPause shouldPause) {
        mOnShouldPause = shouldPause;
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 end

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-14, PR948961 begin
    public void setShouldPause() {
        mShouldPause = !isPlaying() && (mCurrentState != STATE_PREPARING)
                && (mCurrentState != STATE_PREPARED) && (mCurrentState != STATE_IDLE)
                && (mCurrentState != STATE_ERROR);
        Log.d(TAG, "when MoviePlayer onPause() , is video paused = " + mShouldPause);
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-14, PR948961 end

    private String handleSubtitleCharset(String inputFile){
        try{
            FileInputStream fis = new FileInputStream(inputFile);
            UniversalDetector detector = new UniversalDetector(null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int number = 0;
            while((number = fis.read(buffer)) >= 0) {
                baos.write(buffer, 0, number);
                detector.handleData(buffer, 0, number);
            }
            detector.dataEnd();
            fis.close();
            String charset = detector.getDetectedCharset();
            if(charset == null)
                charset = "UTF-8";

            String content = new String(baos.toByteArray(), charset);
            byte[] bytes = content.getBytes();
            File cacheDir = mContext.getCacheDir();
            File tempSrtFile = new File(cacheDir, "tempSrtFile.srt");
            FileOutputStream fos = new FileOutputStream(tempSrtFile);
            fos.write(bytes);
            fos.flush();
            fos.close();
            return tempSrtFile.getAbsolutePath();
        } catch(Exception e) {
            return inputFile;
        }
    }

    private MediaPlayer.OnCompletionListener mCompletionListener =
        new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-26,PR841080 begin
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.hide();
            }
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2014-11-26,PR841080 end
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener =
        new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.d(TAG, "Error: " + framework_err + "," + impl_err);
            releaseWakeLock();//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-02-04,PR918354 begin
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mMediaController != null) {
                mMediaController.hide();
            }

            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }

            if (getWindowToken() != null) {
                int messageId;

                if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                    messageId = R.string.VideoView_error_text_invalid_progressive_playback;
                } else {
                    messageId = R.string.VideoView_error_text_unknown;
                }
              //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-02-25,PR1657947 begin
                mp.reset();
              //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-02-25,PR1657947 end
                new AlertDialog.Builder(mContext)
                        .setMessage(messageId)
                        .setPositiveButton(R.string.VideoView_error_button,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        if (mOnCompletionListener != null) {
                                            mOnCompletionListener.onCompletion(mMediaPlayer);
                                        }
                                    }
                                })
                        .setCancelable(false)
                        .show();
            }
            return true;
        }
    };

    ///[BUGFIX]-ADD-BEGIN BY TSNJ.LIUDEKUAN ON 2016/01/12 FOR DEFECT1126560
    private MoviePlayer mMoviePlayer;
    public void setMoviePlayer (MoviePlayer player) {
        mMoviePlayer = player;
    }
    ///[BUGFIX]-ADD-END BY TSNJ.LIUDEKUAN

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
        new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            ///[BUGFIX]-ADD-BEGIN BY TSNJ.LIUDEKUAN ON 2016/01/12 FOR DEFECT1126560
            if (mMoviePlayer != null) {
                mMoviePlayer.checkPlayingIfNeeded();
            }
            ///[BUGFIX]-ADD-END BY TSNJ.LIUDEKUAN
        }
    };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l)
    {
        mOnPreparedListener = l;
    }
  //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-12, CR938507 begin
    public void setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener l)
    {
        mOnVideoSizeChangedListener = l; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-24, PR957026
    }
  //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-12, CR938507 END
    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener l)
    {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(OnErrorListener l)
    {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(OnInfoListener l) {
        mOnInfoListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState =  (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 begin
                if (!mShouldPause) {
                    start();
                }
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 end
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            openVideo();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceHolder = null;
            if (mMediaController != null) mMediaController.hide();
            release(true);
        }
    };

    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState  = STATE_IDLE;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                                     keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                                     keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                                     keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                                     keyCode != KeyEvent.KEYCODE_MENU &&
                                     keyCode != KeyEvent.KEYCODE_CALL &&
                                     keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 begin
    public void setAudioFocusChangeListener(
            AudioManager.OnAudioFocusChangeListener audioFocusChangeListener) {
        this.mAudioFocusChangeListener = audioFocusChangeListener;
    }
    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 end

    public void start() {
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 begin
        if (mShouldPause
                && null != mMediaPlayer) { //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-28, PR936095
            Log.d(TAG, "start() , set media player prepareAsync()");
            mShouldPause = false;
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            return;
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-02-10, PR928216 end

        if (isInPlaybackState()) {
            Log.i("MovieTAG", "--->MovieVideoView start() start");
            mMediaPlayer.start();
            Log.i("MovieTAG", "--->MovieVideoView start() end");
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 begin
        ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(
                mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 end
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                Log.i("MovieTAG", "==>MovieVideoView pause() start");
                mMediaPlayer.pause();
                Log.i("MovieTAG", "==>MovieVideoView pause() end");
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-19, PR906557 begin
    public void resume(boolean isLocalFile) {
       //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen,1/10/2015,898610,[Video Streaming]Cannot recover in 5 minutes after pause for more than 1 minutes.
        if (!isLocalFile) {
            mTargetState = STATE_PLAYING;
        }
      //[BUGFIX]-Add-BEGIN by TCTNJ.ye.chen,1/10/2015,898610,[Video Streaming]Cannot recover in 5 minutes after pause for more than 1 minutes.
        openVideo();
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-19, PR906557 end

    public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
        }
        mSeekWhenPrepared = msec;
    }

    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    public boolean canPause() {
        return mCanPause;
    }

    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 begin
    public void setAudioVolumCanDuck() {
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.setVolume(mMinimumTransiantVolumeValue, mMinimumTransiantVolumeValue);
        }
    }

    public void setAudioVolumFocus() {
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.setVolume(mNormalVolumeValue, mNormalVolumeValue);
        }
    }
    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-3-15,ALM-1786141 end
}
// [FEATURE]-Add-END by jian.pan1

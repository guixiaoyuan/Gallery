package com.android.gallery3d.popupvideo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

// [FEATURE]-Add-BEGIN by jian.pan1,11/06/2014,FR828601 Pop-up Video play
public class TctPopupVideoView extends SurfaceView {
    public static final String TAG = "TctPopupVideoView";
    protected MediaPlayer mMediaPlayer;
    private Uri mUri = null;
    private OnCompletionListener mOnCompletionListener = null;
    private int mStartPosition = 0;

    /**
     * Constructor Method
     * @param context context
     * @see #TctPopupVideoView(Context, AttributeSet)
     * @see #TctPopupVideoView(Context, AttributeSet, int)
     */
    public TctPopupVideoView(Context context) {
        this(context, null);
    }

    /**
     * Constructor Method
     * @param context context
     * @param attrs attributes
     * @see #TctPopupVideoView(Context)
     * @see #TctPopupVideoView(Context, AttributeSet, int)
     */
    public TctPopupVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor Method
     * @param context context
     * @param attrs attributes
     * @param defStyle style
     * @see #TctPopupVideoView(Context, AttributeSet)
     * @see #TctPopupVideoView(Context, AttributeSet, int)
     */
    public TctPopupVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        getHolder().addCallback(new Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setDisplay(holder);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    /**
     * Set a listener to receive the event of video finished
     * @param listener listener
     * @see OnCompletionListener
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    /**
     * Set media source URI
     * @param uri URI
     */
    public void setUri(Uri uri) {
        mUri = uri;
    }

    /**
     * Play video
     * @return true: play successfully, false: failed
     * @see #pause()
     * @see #stop()
     */
    public boolean play() {
        if (mUri == null) {
            return false;
        }

        boolean result = true;
        try {
            // create media player
            if (mMediaPlayer == null) {
                mMediaPlayer = MediaPlayer.create(getContext(), mUri);
                mMediaPlayer.seekTo(mStartPosition);
                mMediaPlayer.setLooping(false);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
                if (getHolder().getSurface().isValid()) {
                    mMediaPlayer.setDisplay(getHolder());
                }
            }

            // play
            mMediaPlayer.start();
            result = true;
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "create mediaplayer failed. e: " + e.toString());
            result = false;
        } catch (SecurityException e) {
            Log.i(TAG, "create mediaplayer failed. e: " + e.toString());
            result = false;
        } catch (IllegalStateException e) {
            Log.i(TAG, "create mediaplayer failed. e: " + e.toString());
            result = false;
        } catch (NullPointerException e) {
            Log.i(TAG, "create mediaplayer failed. e: " + e.toString());
            result = false;
        } finally {
            if (!result && (mMediaPlayer != null)) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }

        return result;
    }

    /**
     * Stop video
     * @see #play()
     * @see #pause()
     */
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * Pause video
     * @see #play()
     * @see #stop()
     */
    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    /**
     * Get playing status
     * @return true: playing, false: not playing
     * @see #play()
     * @see #pause()
     * @see #stop()
     */
    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    /**
     * Set the current position of the video
     * @param position position
     * @see #getCurrentPosition()
     */
    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        } else {
            mStartPosition = position;
        }
    }

    /**
     * Get the current position of the video
     * @return position
     * @see #seekTo(int)
     */
    public int getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return mStartPosition;
        }
    }

    /**
     * Get the width of the video. Only can be call after {@link #play()} is
     * called
     * @return width
     * @see #play()
     */
    public int getVideoWidth() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getVideoWidth();
        }
        return 0;
    }

    /**
     * Get the height of the video. Only can be call after {@link #play()} is
     * called
     * @return height
     * @see #play()
     */
    public int getVideoHeight() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getVideoHeight();
        }
        return 0;
    }
}
// [FEATURE]-Add-END by jian.pan1

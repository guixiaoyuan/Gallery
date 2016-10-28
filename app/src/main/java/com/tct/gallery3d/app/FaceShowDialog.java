/* ----------|-----------------|---------------|----------------- */
/* 31/12/2015|dongliang.feng   |ALM-1173782    |[Android 6.0][Gallery_v5.2.5.1.0321.0][Monitor][Force close]It appears FC when click selfies after open a faceshow */
/* ----------|-----------------|---------------|----------------- */

package com.tct.gallery3d.app;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.util.CustomVideoView;

public class FaceShowDialog implements OnClickListener {

    private static final String TAG = "FaceShowDialog";
    public static final int MSG_GET_START = 0x2;

    private Context mContext;
    private AlertDialog mDialog;
    private LayoutInflater mInflater;
    private View mContentView;
    private CustomVideoView mFirstShow;
    private Button mGetStart;
    private TextView mRepresent;
    private Uri mUri;
    android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();

    public FaceShowDialog(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        initViews();
    }

    public AlertDialog getDialog() {
        return mDialog;
    }

    private void initViews() {
        mContentView = mInflater.inflate(R.layout.faceshow_dialog, null);
        mGetStart = (Button) mContentView.findViewById(R.id.faceshow_getstart);
        mFirstShow = (CustomVideoView) mContentView.findViewById(R.id.faceshow_firsttime);
        mFirstShow.setZOrderOnTop(true);
        mUri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.faceshow);
        try {
            if (mUri != null) {
                mmr.setDataSource(mContext, mUri);
            }
            String width = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            mFirstShow.setVideoHeight(Integer.parseInt(height));
            mFirstShow.setVideoWidth(Integer.parseInt(width));
        } catch (Exception ex) {
            Log.e(TAG, "MediaMetadataRetriever exception " + ex);
        } finally {
            mmr.release();
        }
        startPlay();
        mRepresent = (TextView) mContentView.findViewById(R.id.faceshow_represent);
        mGetStart.setOnClickListener(this);
        AlertDialog.Builder dialog;
        dialog = new AlertDialog.Builder(mContext);
        dialog.setView(mContentView);
        mDialog = dialog.create();
    }

    public void show() {
        if (mDialog != null && !mDialog.isShowing()) {
            Log.e(TAG, "show");
            mDialog.show();
        } else {
            return;
        }
    }

    public void dissmiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.faceshow_getstart:
            dissmiss();
            break;
        default:
            break;
        }
    }

    public void setTitle() {
        String represent = String.format(mContext.getString(R.string.faceshow_takemore));
        mRepresent.setText(represent);
        mGetStart.setText(mContext.getResources().getString(R.string.gotit));
        mContentView.invalidate();
        show();
    }

    public void pauseVideo() {
        mFirstShow.pause();
    }

    private void startPlay() {
        mFirstShow.setVideoURI(mUri);
        mFirstShow.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                mFirstShow.setBackground(null);
                mp.start();
                mp.setLooping(true);
            }
        });
    }
}

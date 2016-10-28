package com.tct.gallery3d.app;

import com.tct.gallery3d.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BurstShotDialog implements OnClickListener {

    public static final int MSG_OVERWRITE = 0x1000;
    public static final int MSG_INDIVIDUAL_IMAGE = 0x1001;

    private Context mContext;
    private AlertDialog mDialog;
    private Button mBtnOverWrite;
    private Button mBtnIndividualImage;
    private LayoutInflater mInflater;
    private View mContentView;
    private Handler mHandler;

    public BurstShotDialog(Context context, Handler handler) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mHandler = handler;
        initView();
    }

    private void initView() {
        mContentView = mInflater.inflate(R.layout.burstshot_save_dialog, null);
        mBtnOverWrite = (Button) mContentView
                .findViewById(R.id.button_burst_overwrite);
        mBtnIndividualImage = (Button) mContentView
                .findViewById(R.id.button_burst_individual);
        mBtnOverWrite.setOnClickListener(this);
        mBtnIndividualImage.setOnClickListener(this);
        AlertDialog.Builder dialog;
        dialog = new AlertDialog.Builder(mContext);
        dialog.setView(mContentView);
        mDialog = dialog.create();
    }

    public void show() {
        if (mDialog != null && !mDialog.isShowing()) {
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
        case R.id.button_burst_overwrite:
            dissmiss();
            mHandler.sendMessage(mHandler.obtainMessage(MSG_OVERWRITE));
            break;
        case R.id.button_burst_individual:
            dissmiss();
            mHandler.sendMessage(mHandler.obtainMessage(MSG_INDIVIDUAL_IMAGE));
            break;
        default:
            break;
        }
    }

}

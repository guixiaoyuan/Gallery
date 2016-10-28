/* ----------|----------------------|----------------------|----------------------------------- */
/* 24/01/2015|jian.pan1             |FR889883              |[WiFi Display][EM]Gallery Should
/*           |                      |                      |support slide show with three fingers
/* ----------|----------------------|----------------------|----------------------------------- */
package com.tct.gallery3d.multipoint;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.tct.gallery3d.R;

public class ThreeFingerShare {
    private String TAG = "ThreeFingerShare";
    private Context mContext;
    private WindowManager.LayoutParams mWindowParams = null;
    private WindowManager mWindowManager;
    private View mContentView = null;
    private ImageView title_bar_red, hand;
    private Button done;
    private CheckBox never;
    private TextView tips_content;
    private int handStartY;
    private final int handImgHight = 110;
    private AnimatorSet handAnimator;
    private int mDuration = 1000;
    private boolean isAdd = true;

    public void onPause() {
        Log.d(TAG, "onPause   remove");
        isAdd = false;
        mWindowManager.removeView(mContentView);
    }

    public void onResume() {
        Log.d(TAG, "onResume   addView");
        isAdd = true;
        mWindowManager.addView(mContentView, getWindowParams());
    }

    public ThreeFingerShare(Context mContext) {
        Log.d(TAG, "ThreeFingerShare");
        this.mContext = mContext;
        mWindowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        mContentView = View.inflate(mContext, R.layout.three_finger_share_tips,
                null);
        mWindowManager.addView(mContentView, getWindowParams());
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        handStartY = dm.heightPixels / 2;
        handAnimator = new AnimatorSet();
        initView();
        setViewProperty();
        setAnimation();
    }

    private void initView() {
        Log.d(TAG, "initView");
        title_bar_red = (ImageView) mContentView
                .findViewById(R.id.title_bar_red);
        hand = (ImageView) mContentView.findViewById(R.id.hand);
        never = (CheckBox) mContentView.findViewById(R.id.never);
        done = (Button) mContentView.findViewById(R.id.done);
        tips_content = (TextView) mContentView.findViewById(R.id.tips_content);
        done.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mWindowManager.removeView(mContentView);
                isAdd = false;
                setSP();
            }
        });
    }

    private void setViewProperty() {
        hand.setY(handStartY);
    }

    private void setAnimation() {
        Log.d(TAG, "setAnimation");
        ObjectAnimator handMove1;
        handMove1 = ObjectAnimator.ofFloat(hand, "translationY", handStartY,
                handStartY - handImgHight);
        handMove1.setDuration(mDuration);
        handMove1.addListener(new EmptyAnimator() {
            @Override
            public void onAnimationStart(Animator animation) {
                tips_content.setText(R.string.three_finger_tips0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hand.setFocusableInTouchMode(true);
                hand.requestFocusFromTouch();
            }
        });
        ObjectAnimator handMove2;
        handMove2 = ObjectAnimator.ofFloat(hand, "translationY", handStartY
                - handImgHight, handImgHight / 2);
        handMove2.setDuration(mDuration * 2);
        handMove2.addListener(new EmptyAnimator() {
            @Override
            public void onAnimationStart(Animator animation) {
                title_bar_red.setVisibility(View.VISIBLE);
                tips_content.setText(R.string.three_finger_tips1);
            }
        });
        ObjectAnimator handMove3;
        handMove3 = ObjectAnimator.ofFloat(hand, "translationY",
                handImgHight / 2, 0);
        handMove3.setDuration(mDuration);
        handMove3.addListener(new EmptyAnimator() {
            @Override
            public void onAnimationStart(Animator animation) {
                title_bar_red.setSelected(true);
                tips_content.setText(R.string.three_finger_tips2);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });
        ObjectAnimator handMover4;
        handMover4 = ObjectAnimator.ofFloat(hand, "stay", 0, 0);
        handMover4.addListener(new EmptyAnimator() {

            @Override
            public void onAnimationEnd(Animator animation) {
                title_bar_red.setSelected(false);
                hand.setFocusable(false);
            }
        });
        handAnimator.playSequentially(handMove1, handMove2, handMove3,
                handMover4);
        handAnimator.addListener(new EmptyAnimator() {
            @Override
            public void onAnimationStart(Animator animation) {
                setViewProperty();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                handAnimator.start();
            }
        });

        handAnimator.start();
    }

    public static boolean isAlwaysShow(Context context) {
        SharedPreferences sp = context.getSharedPreferences("tips",
                Context.MODE_PRIVATE);
        // [BUGFIX]-MOD-BEGIN by TCTNB Ke.Meng,01/20/2014,588564
        return sp.getBoolean("always_show", true);
        // [BUGFIX]-MOD-END by TCTNB Ke.Meng
    }

    public boolean isAdd() {
        return isAdd;
    }

    private void setSP() {
        SharedPreferences sp = mContext.getSharedPreferences("tips",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("always_show", never.isChecked());
        editor.commit();
    }

    protected WindowManager.LayoutParams getWindowParams() {
        if (mWindowParams == null) {
            mWindowParams = new WindowManager.LayoutParams();
            mWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mWindowParams.format = PixelFormat.RGBA_8888;
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN;
            mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
        }
        return mWindowParams;
    }

    class EmptyAnimator implements AnimatorListener {

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }
    }
}

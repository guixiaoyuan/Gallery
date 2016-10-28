/*---------------------------------------------------------------------------------------------------------------------------------*/
/* 03/10/2016|    su.jiang     |  PR-1534334   |[Pre-cts][ALRU][boom key]many strings show in english when use boom key in gallery.*/
/*-----------|-----------------|---------------|-----------------------------------------------------------------------------------*/
package com.tct.gallery3d.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.ui.CustomViewPager;
import com.tct.gallery3d.util.PLFUtils;

public class BoomKeyTipsActivity extends Activity implements OnPageChangeListener, OnClickListener{

    private CustomViewPager mViewPager = null;

    private ImageView[] mDots = new ImageView[3];//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-10,PR1534334

    private TextView mTxSkip = null;

    private TextView mTxDone = null;

    private TextView mTxMsg = null;

    private ImageView mArrowLeft = null;

    private ImageView mArrowRight = null;
    
    private View mBtnLeft = null;
    
    private View mBtnRight = null;

    private List<View> mViewList = new ArrayList<View>(3);//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-10,PR1534334
    
    private String[] mTipMsg = new String[3];//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-10,PR1534334

    private LayoutInflater mInflater = null;

    private ViewPagerAdapter mAdapter = null;

    private int mCurrPosition = 0;

    private String mBucketId = null;

    private String[] mBucketIds = null;
    
    private Uri mBoomKeyUri = null;
    
    private VideoView mVideoView = null;
    
    private MediaPlayer.OnPreparedListener mOnPrepareListener = null;
    
    private boolean mFirstShow = true;
    
    private Uri mUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(1, 143, 163));
        setContentView(R.layout.layout_boomkey_tips);
        mInflater = LayoutInflater.from(this);
        Intent intent = getIntent();
        mBucketId = intent.getStringExtra("bucketId");
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/11,PR1694004 begin
        mBucketIds = intent.getStringArrayExtra("bucketIds");
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/11,PR1694004 end
        mBoomKeyUri = Uri.parse(intent.getStringExtra("uri"));

        initView();
        initData();
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        initView();
        initData();
    }*/

    private void initView() {
        mViewPager = (CustomViewPager) findViewById(R.id.view_pager);
        ImageView dot1 = (ImageView) findViewById(R.id.dot1);
        ImageView dot2 = (ImageView) findViewById(R.id.dot2);
        ImageView dot3 = (ImageView) findViewById(R.id.dot3);
        mDots[0] = dot1;
        mDots[1] = dot2;
        mDots[2] = dot3;
        mTxMsg = (TextView) findViewById(R.id.tx_msg);
        mTxSkip = (TextView) findViewById(R.id.tx_skip);
        mTxSkip.setText(getResources().getString(R.string.tx_skip).toUpperCase());
        mTxDone = (TextView) findViewById(R.id.tx_done);
        mTxDone.setText(getResources().getString(R.string.tx_done).toUpperCase());
        mArrowLeft = (ImageView) findViewById(R.id.iv_arrow_left);
        mArrowRight = (ImageView) findViewById(R.id.iv_arrow_right);
        mBtnLeft = findViewById(R.id.btn_arrow_left);
        mBtnRight = findViewById(R.id.btn_arrow_right);
        mBtnLeft.setOnClickListener(this);
        mBtnRight.setOnClickListener(this);
    }

    private void initData() {
        for (int i = 0; i < 3; i++) {
            mViewList.add(null);
        }
        mUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_tip_boomkey);
        mTipMsg[0] = getResources().getString(R.string.boom_key_tip_1);
        mTipMsg[1] = getResources().getString(R.string.boom_key_tip_2);
        mTipMsg[2] = getResources().getString(R.string.boom_key_tip_4);//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-10,PR1534334
        mFirstShow = true;
        mAdapter = new ViewPagerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(3); //MODIFIED by jian.pan1, 2016-03-29,BUG-1871694
        mViewPager.setOnPageChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.anim_null, R.anim.anim_tips_out);
    }

    class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View v, Object o) {
            return v == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
             /*MODIFIED-BEGIN by jian.pan1, 2016-03-29,BUG-1871694*/
            View view = mViewList.get(position);
            if (view == null) {
                view = loadView(position, container);
                mViewList.set(position, view);
            }
             /*MODIFIED-END by jian.pan1,BUG-1871694*/
            container.addView(view);
            return view;
        }
    }

    private void startPlay() {
        mVideoView.setVideoURI(mUri);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {  
            @Override  
            public void onPrepared(final MediaPlayer mp) {
                mVideoView.setBackground(null);
                mp.start();
                mp.setLooping(true);  
            }  
        });
    }

    private void stopPlay() {
        mVideoView.stopPlayback();
    }

    private View loadView(int position, ViewGroup container) {
        View view = null;
        switch (position) {
        case 0:
            view = mInflater.inflate(R.layout.layout_1, container, false);
            mVideoView = (VideoView) view.findViewById(R.id.video_view);
            if(mFirstShow) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startPlay();
                        mFirstShow = false;
                    }
                }, 100);
            }
            break;
        case 1:
            view = mInflater.inflate(R.layout.layout_2, container, false);
             /*MODIFIED-BEGIN by jian.pan1, 2016-03-29,BUG-1871694*/
            ((TextView) view.findViewById(R.id.tips_tab_moments_tv))
                    .setText(getResources()
                            .getString(R.string.main_tab_moments).toUpperCase());
            ((TextView) view.findViewById(R.id.tips_tab_albums_tv))
                    .setText(getResources().getString(R.string.albums)
                            .toUpperCase());
            break;
        case 2:
            view = mInflater.inflate(R.layout.layout_3, container, false);
            ((TextView) view.findViewById(R.id.tv_press))
                    .setText(getResources().getString(R.string.press)
                            .toUpperCase());
            ((TextView) view.findViewById(R.id.tv_boomkey))
                    .setText(getResources().getString(R.string.boom_key)
                            .toUpperCase());
                             /*MODIFIED-END by jian.pan1,BUG-1871694*/
            break;
        }
        return view;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
//[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-01-26,PR1490949 begin
        //if(state == ViewPager.SCROLL_STATE_DRAGGING && mCurrPosition == 0) {
         //   stopPlay();
       // }
//[BUGFIX]-Modify by TCTNJ,xinrong.wang, 2016-01-26,PR1490949 begin
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        if(position == 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startPlay();
                }
            }, 100);
        }
//[BUGFIX]-add by TCTNJ,xinrong.wang, 2016-01-26,PR1490949 begin
       if(mCurrPosition==0)
    	{
            stopPlay();	
    	}
//[BUGFIX]-add by TCTNJ,xinrong.wang, 2016-01-26,PR1490949 end
        mTxMsg.setText(mTipMsg[position]);
        mDots[position].setImageResource(R.drawable.dot_selected);
        mDots[mCurrPosition].setImageResource(R.drawable.dot_normal);

        if(position == 2 && mCurrPosition == 1) {//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-10,PR1534334
            mArrowLeft.setVisibility(View.VISIBLE);
            mArrowRight.setVisibility(View.GONE);
            mTxSkip.setVisibility(View.GONE);
            mTxDone.setVisibility(View.VISIBLE);
        }
        if(position == 1 && mCurrPosition == 2) {//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-10,PR1534334
            mArrowLeft.setVisibility(View.GONE);
            mArrowRight.setVisibility(View.VISIBLE);
            mTxSkip.setVisibility(View.VISIBLE);
            mTxDone.setVisibility(View.GONE);
        }
        mCurrPosition = position;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.btn_arrow_left:
            if(mCurrPosition == 2) {//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-10,PR1534334
//                mViewPager.();
            } else {
                openBoomKeyPicture();
            }
            break;

        case R.id.btn_arrow_right:
            if(mCurrPosition == 2) {//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-10,PR1534334
                openBoomKeyPicture();
            } else {
//                mViewPager.moveToRight();
            }
            break;
        }
    }

    private void openBoomKeyPicture() {
        Intent intent = new Intent("com.muvee.BoomKeyPicture");
        intent.setClassName("com.muvee.boomkey.picture", "com.muvee.boomkey.picture.BoomImageMainActivity");
        if(mBucketId != null) {
            intent.putExtra("com.muvee.dsg.boomkey.picture.bucket.id", mBucketId);
        }
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/11,PR1694004 begin
        if(mBucketIds != null){
            intent.putExtra("com.muvee.dsg.boomkey.picture.bucket.ids", mBucketIds);
        }
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/11,PR1694004 end
        intent.putExtra("com.muvee.dsg.boomkey.picture.start.image.uri", mBoomKeyUri);
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-15,PR1694257 begin
        if (PLFUtils.getBoolean(this,"def_gallery_custom_share_enable")) {
            intent.putExtra("com.muvee.share.usecustom", true);
        } else {
            intent.putExtra("com.muvee.share.usecustom", false);
        }
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-15,PR1694257 end
        startActivity(intent);
        overridePendingTransition(R.anim.anim_tips_in, R.anim.anim_tips_out);
        this.finish();
    }
}

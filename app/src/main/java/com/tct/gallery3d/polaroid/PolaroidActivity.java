/* ----------|----------------------|----------------------|----------------- */
/* 06/18/2015| jian.pan1            | PR1025898            |[Android 5.1][Camera_v5.1.11.0210.0][Polaroid]The save icon show invalid after inputting characters
/* ----------|----------------------|----------------------|----------------- */
/* 06/19/2015| chengbin.du          | PR1026015            |[Android 5.1][Gallery_Polaroid_v5.1.13.1.0209.0][Force Close]Pop up Force Close when editting pic
/* ----------|----------------------|----------------------|----------------- */
/* 07/07/2015| chengbin.du          | PR1036843            |[Android 5.1][Gallery_v5.1.13.1.0211.0_Polaroid]The rotated pic edit in Polaroid mode is the original one
/* ----------|----------------------|----------------------|----------------- */
/* 07/14/2015| jian.pan1            | PR1043560            |[Android 5.1][Gallery_v5.1.13.1.0212.0]It exit gallery when tap back key in edit interface
/* ----------|----------------------|----------------------|----------------- */
/* 07/15/2015| jian.pan1            | PR1040393            |[Android 5.1][Gallery_v5.1.13.1.0212.0]The division line is not display smooth when use polariud edit
/* ----------|----------------------|----------------------|----------------- */
/* 08/14/2015| dongliang.feng       | PR1067891            |[Android5.1][Gallery_v5.2.0.1.1.0303.0][[Force Close]Gallery has stopped when damage picture via Polaroid editor
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.polaroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.SystemBarTintManager;
import com.tct.gallery3d.exif.ExifInterface;
import com.tct.gallery3d.polaroid.adapter.FilterTile;
import com.tct.gallery3d.polaroid.adapter.FilterTilesAdapter;
import com.tct.gallery3d.polaroid.adapter.FrameTile;
import com.tct.gallery3d.polaroid.adapter.FrameTilesAdapter;
import com.tct.gallery3d.polaroid.config.FilterConfig;
import com.tct.gallery3d.polaroid.config.FilterConfig.FilterCompletionHandler;
import com.tct.gallery3d.polaroid.config.PolaroidConfig;
import com.tct.gallery3d.polaroid.handler.BackgroundHandler;
import com.tct.gallery3d.polaroid.handler.ForegroundHandler;
import com.tct.gallery3d.polaroid.imageshow.PreviewParams;
import com.tct.gallery3d.polaroid.manager.Filter;
import com.tct.gallery3d.polaroid.manager.FilterManager;
import com.tct.gallery3d.polaroid.manager.FilterOperation.Quality;
import com.tct.gallery3d.polaroid.manager.FontManager;
import com.tct.gallery3d.polaroid.manager.Frame;
import com.tct.gallery3d.polaroid.manager.FrameManager;
import com.tct.gallery3d.polaroid.manager.FrameResource;
import com.tct.gallery3d.polaroid.tools.CropBitmapDrawable;
import com.tct.gallery3d.polaroid.tools.SaveBitmapDrawable;
import com.tct.gallery3d.polaroid.tools.Utils;
import com.tct.gallery3d.polaroid.view.CropImageView;
import com.tct.gallery3d.polaroid.view.PolaroidView;

// [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-18,PR1025898 begin
public class PolaroidActivity extends Activity implements FilterCompletionHandler, OnClickListener, PolaroidView.ISaveStateListener {
// [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-18,PR1025898 end

    private static final String TAG = "PolaroidActivity";

    private PolaroidView mPolaroidView;
    private CropImageView mCropImage;
    private ImageButton mFiltersButton, mFramesButton, mFontButton;
    private ImageButton mLocationButton, mDateButton, mSloganButton;
    private View mFilterOptionsView, mFrameOptionsView;
    private ForegroundHandler mForegroundHandler;
    private BitmapDrawable mInDrawable;
    private BitmapDrawable mInFilterTileDrawable;
    private BitmapDrawable mInFrameTileDrawable;
    private BitmapDrawable mInPreviewDrawable;
    // null after saving
    private BitmapDrawable mPreviewDrawable;
    private PreviewParams mPreviewParams;
    private LinearLayout mFilterTilesView;
    private LinearLayout mFrameTilesView;
    private LinearLayout mTagTilesView;
    private HandlerThread mHandlerThread;
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 begin
    private BackgroundHandler mBackgroundHandler;
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 end
    private FilterTilesAdapter mFilterTilesAdapter;
    private FrameTilesAdapter mFrameTilesAdapter;
    private View mSelectedFilterTile;
    private View mSelectedFrameTile;
    private PolaroidConfig mPolaroidConfig;
    // null when not saving, not null when saving
    private ProgressBar mSaveProgress;
    private int mImageSeq = 0; // To be incremented when loading a new image
    private boolean mLayoutComplete = false;
    private boolean mIsSquare = false;
    private int mTagStatus = 0;
    private String mFilePath = null;
    private Uri mShareUri = null;
    private boolean mHasSaved = false;
    private SystemBarTintManager mTintManager;

    private ImageView mLeftButton;
    private ImageView mRightButton;
    private ImageView mShareButton;

    private SaveBitmapDrawable saveBitmapDrawable;
    private long mClickTime = 0;// [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-08-04,PR1059410

    // saving picture toast show time
    private static final int SAVING_TOAST_DURATION = 2 * 1000;
    // saving picture size is always 1024*1024px
    private static final int SAVING_PHOTO_SIZE = 1024;

    private static final int POLAROID_EDITOR_LIMIT_SIZE = 320;

    private static int CURRENT_ACTION_CROP = 0;
    private static int CURRENT_ACTION_EDITOR = 1;
    private int mCurrentState = CURRENT_ACTION_CROP;

    public static final int NO_TAG = 0;
    public static final int TAG_LOCATION = 0x01;
    public static final int TAG_DATE = 0x02;
    public static final int TAG_SLOGAN = 0x04;

    public static final String EXIF_TAG_SOFTWARE_VALUE = "Alcatel Polaroid Editor";

    private enum ToolbarState {
        FILTERS, FRAMES, FONT, NONE
    };

    private ToolbarState mCurrToolBar = ToolbarState.FILTERS;

    public static final int MSG_SAVE_IMAGE_COMPLETED = 0x10;
    public static final int MSG_CROP_IMAGE_ERROR = 0x20;
    public static final int MSG_SHARE_IMAGE_SUCCESS = 0x30;
    public static final int MSG_SHARE_IMAGE_ERROR = 0x30;

    public boolean isShareImage = false;
    private int mFrameSize;

    private int currFilterIndex = 0;
    private int currFrameIndex = 0;
    private ViewFlipper viewFlipper;
    private TextView mEffectName1;
    private TextView mEffectName2;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MSG_SAVE_IMAGE_COMPLETED:
                String outFileName = (String) msg.obj;
                if (!TextUtils.isEmpty(outFileName)) {
                    Toast.makeText(PolaroidActivity.this,
                            getResources().getString(R.string.save_iamge_compelete),
                            SAVING_TOAST_DURATION).show();
                    Log.i(Poladroid.TAG, "save image completed, and isShare:" + isShareImage);

                    updateFile(outFileName);
                    mHasSaved = true;
                } else {
                    Toast.makeText(PolaroidActivity.this,
                            getResources().getString(R.string.save_image_error),
                            SAVING_TOAST_DURATION).show();
                }
                hideProgress();
                updateViewEnable(true);
                if (mRightButton != null && mCurrentState == CURRENT_ACTION_EDITOR) {
                    mRightButton.setEnabled(false);
                }
                break;
            case MSG_CROP_IMAGE_ERROR:
                hideProgress();
                updateViewEnable(true);
                break;
            case PolaroidView.MSG_SWIP_LEFT:
                if (mCurrToolBar == ToolbarState.FILTERS) {
                    if (mFilterTilesView != null && currFilterIndex - 1 >= 0) {
                        updateSwipView(PolaroidView.MSG_SWIP_LEFT, mCurrToolBar,
                                currFilterIndex - 1);
                    }
                } else if (mCurrToolBar == ToolbarState.FRAMES) {
                    if (mFrameTilesView != null && currFrameIndex - 1 >= 0) {
                        updateSwipView(PolaroidView.MSG_SWIP_LEFT, mCurrToolBar, currFrameIndex - 1);
                    }
                }
                break;
            case PolaroidView.MSG_SWIP_RIGHT:
                if (mCurrToolBar == ToolbarState.FILTERS) {
                    if (mFilterTilesView != null
                            && currFilterIndex + 1 < mFilterTilesAdapter.getCount()) {
                        updateSwipView(PolaroidView.MSG_SWIP_RIGHT, mCurrToolBar,
                                currFilterIndex + 1);
                    }
                } else if (mCurrToolBar == ToolbarState.FRAMES) {
                    if (mFrameTilesView != null
                            && currFrameIndex + 1 < mFrameTilesAdapter.getCount()) {
                        updateSwipView(PolaroidView.MSG_SWIP_RIGHT, mCurrToolBar,
                                currFrameIndex + 1);
                    }
                }
                break;
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Poladroid.TAG, "PolaroidActivity.onCreate(){");

        getWindow().setFormat(PixelFormat.RGBA_8888);
        setContentView(R.layout.polaroid_main_activity);

        initActionBar();

        // Do not change the order: frame depends on font, filter depends on
        // frame
        FontManager.init(this);
        FrameManager.init(this);
        FilterManager.init(this);

        mPolaroidConfig = new PolaroidConfig(this);
        mForegroundHandler = new ForegroundHandler(this);
        mHandlerThread = new HandlerThread("Background Thread");
        mHandlerThread.start();
        mBackgroundHandler = new BackgroundHandler(mHandlerThread.getLooper(), mForegroundHandler,
                this);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 begin
        mBackgroundHandler.setHandlerQuit(false);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 end
        initToolbar();

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        mEffectName1 = (TextView) findViewById(R.id.effect_name_1);
        mEffectName2 = (TextView) findViewById(R.id.effect_name_2);
        // Populate the frame list -- MUST be BEFORE the filter list
        // (because the default filter may have preferred frame to select...)
        mFrameTilesView = (LinearLayout) findViewById(R.id.frame_tiles);
        populateFrameTilesAdapter();
        // Populate the filter list
        mFilterTilesView = (LinearLayout) findViewById(R.id.filter_tiles);
        // Setup Polaroid view
        mPolaroidView = (PolaroidView) findViewById(R.id.polaroid);
        mPolaroidView.setVisibility(View.INVISIBLE);
        mPolaroidView.setMainHandler(mHandler);
        mPolaroidView.setTouchEnable(true);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-18,PR1025898 begin
        mPolaroidView.setSaveStateListener(this);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-18,PR1025898 end

        mCropImage = (CropImageView) findViewById(R.id.cropview);
        mCropImage.setVisibility(View.INVISIBLE);

        mSaveProgress = (ProgressBar) findViewById(R.id.saveProgress);
        showProgress();

        processIntent();

        mPolaroidView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.d(Poladroid.TAG,
                                "PolaroidActivity.mPolaroidPreviewView.OnGlobalLayoutListener(){");
                        try {
                            mLayoutComplete = true;
                            mPolaroidView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            selectFilterTile(mFilterTilesView.getChildAt(0), false, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d(Poladroid.TAG,
                                "} PolaroidActivity.mPolaroidPreviewView.OnGlobalLayoutListener()");
                    }
                });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        initTintManager();
        setStatusEnable(true);
        setStatusColor(SystemBarTintManager.POLAROID_STATUS_BAR_COLOR);

        Log.d(Poladroid.TAG, "} PolaroidActivity.onCreate()");
    }

    private void processIntent() {
        Intent intent = getIntent();
        Uri mSelectedImageUri = intent.getData();
        if (mSelectedImageUri != null) {
            mIsSquare = startLoadBitmap(mSelectedImageUri);
            if (mInDrawable != null) {
                if (!mIsSquare) {
                    updateCropView();
                } else {
                    Log.i(Poladroid.TAG, "processIntent current picture is square.");
                    updatePolaroidView(false);
                }
            } else {
                Log.e(Poladroid.TAG, "processIntent mInDrawable is NULL");
            }
        } else {
            pickImage();
        }
    }

    // TODO: use thread to load bitmap
    private boolean startLoadBitmap(Uri uri) {
        boolean isSquare = false;

        if (uri.toString().startsWith("file://")) {
            mFilePath = uri.getPath();
        } else if (uri.toString().startsWith("content://")) {
            mFilePath = getPath(uri, PolaroidActivity.this);
        } else {
            mFilePath = uri.toString();
        }

        Log.i(Poladroid.TAG, "startLoadBitmap mFilePath:" + mFilePath);
        boolean cantLoadImage = false; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-14, PR1067891
        if (!TextUtils.isEmpty(mFilePath)) {
            // [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-07-07,PR1036843 begin
            // read exif from file
            ExifInterface exif = new ExifInterface();
            try {
                exif.readExif(mFilePath);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                Log.e(TAG, "getExifInfo FileNotFoundException:", e1);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "getExifInfo IOException:", e);
            }
            int degrees = 0;
            Integer orientation = exif.getTagIntValue(ExifInterface.TAG_ORIENTATION);
            if(orientation != null) {
                degrees = ExifInterface.getRotationForOrientationValue(orientation.shortValue());
            }

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(mFilePath, bitmapOptions);
            if(degrees != 0) {
                Log.d(TAG, "image degrees=" + degrees);
                Matrix m = new Matrix();
                m.postRotate(degrees);
                Bitmap tmp = bitmap;
                bitmap = Bitmap.createBitmap(tmp, 0, 0,
                            tmp.getWidth(), tmp.getHeight(), m, true);
                tmp.recycle();
            }
            // [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-07-07,PR1036843 end

            if (bitmap != null) {
                int picW = bitmap.getWidth();
                int picH = bitmap.getHeight();
                Log.i(Poladroid.TAG, "canUsePolaroidEditor picW:" + picW + " picH:" + picH);
                if (picW < POLAROID_EDITOR_LIMIT_SIZE || picH < POLAROID_EDITOR_LIMIT_SIZE) {
                    showMinImgageDialog();
                } else {
                    mPolaroidView.assyncGetExifInfo(mFilePath);
                    Utils.setCurrentStoragePath(mFilePath);
                    if (picW == picH) {
                        isSquare = true;
                        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1040393 begin
                        bitmap = Bitmap.createScaledBitmap(bitmap, SAVING_PHOTO_SIZE,
                                SAVING_PHOTO_SIZE, true);
                        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-15,PR1040393 end
                    } else {
                        Log.i(Poladroid.TAG, "picture is not squre, need crop.");
                    }
                    mInDrawable = new BitmapDrawable(getResources(), bitmap);
                }
            } else {
                cantLoadImage = true; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-14, PR1067891
                Log.e(Poladroid.TAG, "startLoadBitmap bitmap is NULL.");
            }
        } else {
            cantLoadImage = true; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-14, PR1067891
            Log.e(Poladroid.TAG, "startLoadBitmap mFilePath is NULL.");
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-14, PR1067891 begin
        if (cantLoadImage) {
            Toast.makeText(PolaroidActivity.this, R.string.cannot_load_image, Toast.LENGTH_SHORT)
                    .show();
            done();
        }
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-14, PR1067891 end
        return isSquare;
    }

    private void showMinImgageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_min_image_save_message).setTitle(
                R.string.dialog_min_image_save_title);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                done();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void updateCropView() {
        mCropImage.setDrawable(mInDrawable, SAVING_PHOTO_SIZE, SAVING_PHOTO_SIZE);
        mCropImage.setVisibility(View.VISIBLE);
        mPolaroidView.setVisibility(View.INVISIBLE);
        hideProgress();
    }

    private void hideProgress() {
        if (mSaveProgress.getVisibility() == View.VISIBLE) {
            mSaveProgress.setVisibility(View.GONE);
        }
    }

    private void showProgress() {
        if (mSaveProgress.getVisibility() != View.VISIBLE) {
            mSaveProgress.setVisibility(View.VISIBLE);
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void initActionBar() {
        ActionBar mActionBar = getActionBar();
        View customActionBarView = getLayoutInflater()
                .inflate(R.layout.polaroid_title_layout, null);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        mActionBar.setCustomView(customActionBarView, layoutParams);
        mLeftButton = (ImageView) findViewById(R.id.editor_left);
        mRightButton = (ImageView) findViewById(R.id.editor_right);
        mShareButton = (ImageView) findViewById(R.id.editor_share);
        mLeftButton.setOnClickListener(this);
        mRightButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);
    }

    private void updateActionBar(int currState) {
        if (currState == CURRENT_ACTION_CROP) {
            mLeftButton.setImageResource(R.drawable.polaroid_selector_crop_cancel);
            mRightButton.setImageResource(R.drawable.polaroid_selector_crop_ok);
            mShareButton.setVisibility(View.GONE);
        } else if (currState == CURRENT_ACTION_EDITOR) {
            mLeftButton.setImageResource(R.drawable.polaroid_selector_back);
            mRightButton.setImageResource(R.drawable.polaroid_selector_save);
            mShareButton.setVisibility(View.VISIBLE);
        }
    }

    private void setActionBarEnabled(boolean enabled) {
        if (mLeftButton != null) {
            mLeftButton.setEnabled(enabled);
        }
        if (mRightButton != null) {
            mRightButton.setEnabled(enabled);
        }
        if (mShareButton != null) {
            mShareButton.setEnabled(enabled);
        }
    }

    private void updateSwipView(int direction, ToolbarState state, int index) {
        if (state == ToolbarState.FILTERS) {
            View filterTile = mFilterTilesView.getChildAt(index);
            selectFilterTile(filterTile, true, true);
        } else if (state == ToolbarState.FRAMES) {
            View frameTile = mFrameTilesView.getChildAt(index);
            selectFrameTile(frameTile, true, true);
        }
        if (direction == PolaroidView.MSG_SWIP_LEFT) {
            viewFlipper.setInAnimation(PolaroidActivity.this, R.anim.view_transition_in_left);
            viewFlipper.setOutAnimation(PolaroidActivity.this, R.anim.view_transition_out_left);
            viewFlipper.showNext();
        } else if (direction == PolaroidView.MSG_SWIP_RIGHT) {
            viewFlipper.setInAnimation(PolaroidActivity.this, R.anim.view_transition_in_right);
            viewFlipper.setOutAnimation(PolaroidActivity.this, R.anim.view_transition_out_right);
            viewFlipper.showPrevious();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Poladroid.TAG, "PolaroidActivity.onDestroy() {");
        if (mHandlerThread != null) {
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 begin
            mBackgroundHandler.setHandlerQuit(true);
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-14,PR1043560 end
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }
        destoryBitmapResource();
        isShareImage = false;
        Log.d(Poladroid.TAG, "} PolaroidActivity.onDestroy()");
    }

    private void initTintManager() {
        if (mTintManager == null) {
            mTintManager = new SystemBarTintManager(this);
        }
    }

    private void setStatusEnable(boolean enable) {
        mTintManager.setStatusBarTintEnabled(enable);
    }

    private void setStatusColor(int color) {
        mTintManager.setStatusBarTintColor(color);
    }

    /***
     * Toolbar state
     */
    private void initToolbar() {
        mFiltersButton = (ImageButton) findViewById(R.id.filters_button);
        mFiltersButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setToolbarState(ToolbarState.FILTERS);
            }
        });
        mFramesButton = (ImageButton) findViewById(R.id.frames_button);
        mFramesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setToolbarState(ToolbarState.FRAMES);
            }
        });
        mFontButton = (ImageButton) findViewById(R.id.font_button);
        mFontButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int frameIndex = FrameManager.getIndexOfFrame(mPolaroidConfig.mFrameName);
                if (frameIndex == mFrameSize - 1)
                    return;

                setToolbarState(ToolbarState.FONT);
            }
        });

        mLocationButton = (ImageButton) findViewById(R.id.location_tag_button);
        mLocationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setTagState(TAG_LOCATION);
            }
        });
        mDateButton = (ImageButton) findViewById(R.id.date_tag_button);
        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setTagState(TAG_DATE);
            }
        });
        mSloganButton = (ImageButton) findViewById(R.id.slogan_tag_button);
        mSloganButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setTagState(TAG_SLOGAN);
            }
        });

        mFilterOptionsView = (View) findViewById(R.id.filter_options);
        mFrameOptionsView = (View) findViewById(R.id.frame_options);
        mTagTilesView = (LinearLayout) findViewById(R.id.tag_tiles);

        setToolbarState(ToolbarState.FILTERS);
        setToolBarEnable(false);
    }

    private void updateViewEnable(boolean enable) {
        setToolBarEnable(enable);
        mPolaroidView.setSloganEnable(enable);
        setActionBarEnabled(enable);
    }

    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-08-04,PR1059410 begin
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown keyCode:" + keyCode + " isShareImage:" + isShareImage);
        if (keyCode == KeyEvent.KEYCODE_BACK && isShareImage) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-08-04,PR1059410 end

    private void setToolBarEnable(boolean enable) {
        if (mFiltersButton != null) {
            mFiltersButton.setEnabled(enable);
        }
        if (mFramesButton != null) {
            mFramesButton.setEnabled(enable);
        }
        if (mFontButton != null) {
            mFontButton.setEnabled(enable);
        }
        if (enable) {
            setToolbarState(mCurrToolBar);
        } else {
            setToolbarState(ToolbarState.NONE);
        }
    }

    private void setToolbarState(ToolbarState toolbarState) {
        // mToolbarState = toolbarState;
        switch (toolbarState) {
        case FILTERS:
            mFilterOptionsView.setVisibility(View.VISIBLE);
            mFrameOptionsView.setVisibility(View.INVISIBLE);
            mTagTilesView.setVisibility(View.INVISIBLE);
            mFiltersButton.setSelected(true);
            mFramesButton.setSelected(false);
            mFontButton.setSelected(false);
            mCurrToolBar = ToolbarState.FILTERS;
            break;
        case FRAMES:
            mFilterOptionsView.setVisibility(View.INVISIBLE);
            mFrameOptionsView.setVisibility(View.VISIBLE);
            mTagTilesView.setVisibility(View.INVISIBLE);
            mFiltersButton.setSelected(false);
            mFramesButton.setSelected(true);
            mFontButton.setSelected(false);
            mCurrToolBar = ToolbarState.FRAMES;
            break;
        case FONT:
            mFilterOptionsView.setVisibility(View.INVISIBLE);
            mFrameOptionsView.setVisibility(View.INVISIBLE);
            mTagTilesView.setVisibility(View.VISIBLE);
            mFiltersButton.setSelected(false);
            mFramesButton.setSelected(false);
            mFontButton.setSelected(true);
            mCurrToolBar = ToolbarState.FONT;
            break;
        case NONE:
            mFilterOptionsView.setVisibility(View.INVISIBLE);
            mFrameOptionsView.setVisibility(View.INVISIBLE);
            mTagTilesView.setVisibility(View.INVISIBLE);
            mFiltersButton.setSelected(false);
            mFramesButton.setSelected(false);
            mFontButton.setSelected(false);
            break;
        }
    }

    private void setTagState(int tag) {
        switch (tag) {
        case TAG_LOCATION: {
            if ((mTagStatus & TAG_LOCATION) == TAG_LOCATION) {
                mLocationButton.setImageResource(R.drawable.polaroid_assets_location_tag_off);
            } else {
                mLocationButton.setImageResource(R.drawable.polaroid_assets_location_tag_on);
            }
            break;
        }
        case TAG_DATE: {
            if ((mTagStatus & TAG_DATE) == TAG_DATE) {
                mDateButton.setImageResource(R.drawable.polaroid_assets_date_tag_off);
            } else {
                mDateButton.setImageResource(R.drawable.polaroid_assets_date_tag_on);
            }
            break;
        }
        case TAG_SLOGAN: {
            if ((mTagStatus & TAG_SLOGAN) == TAG_SLOGAN) {
                mSloganButton.setImageResource(R.drawable.polaroid_assets_slogan_tag_off);
            } else {
                mSloganButton.setImageResource(R.drawable.polaroid_assets_slogan_tag_on);
            }
            break;
        }
        }
        mTagStatus = mTagStatus ^ tag;
        mPolaroidView.displayTagView(mTagStatus);

        if (mRightButton != null && mCurrentState == CURRENT_ACTION_EDITOR) {
            mRightButton.setEnabled(true);
        }
        mHasSaved = false;
    }

    private void resetTag() {
        mLocationButton.setImageResource(R.drawable.polaroid_assets_location_tag_off);
        mDateButton.setImageResource(R.drawable.polaroid_assets_date_tag_off);
        mSloganButton.setImageResource(R.drawable.polaroid_assets_slogan_tag_off);
        mTagStatus = NO_TAG;
        mPolaroidView.displayTagView(mTagStatus);
    }

    /**
     * Filter tile adapter
     */
    private ArrayList<FilterTile> listFilterTiles() {
        Log.d(Poladroid.TAG, "PolaroidActivity.listFilterTiles(){");
        final ArrayList<FilterTile> filterTiles = new ArrayList<FilterTile>(10);

        Point outResolution = new Point();
        outResolution.x = getResources().getDimensionPixelSize(R.dimen.polaroid_tilewidth);
        outResolution.y = getResources().getDimensionPixelSize(R.dimen.polaroid_tileheight);

        Rect inCrop = Utils.getCenteredScaledCrop(mInDrawable, outResolution);

        Log.d(Poladroid.TAG, "Creating original thumbnail {");
        mInFilterTileDrawable = Utils.getScaledCroppedDrawable(this, mInDrawable, inCrop,
                outResolution);
        Log.d(Poladroid.TAG, "} Creating original thumbnail");

        int sequencePrio = 0;
        for (Iterator<Filter> iterator = FilterManager.iterator(); iterator.hasNext();) {
            Filter filter = iterator.next();
            FilterTile FilterTile = new FilterTile(mInFilterTileDrawable, inCrop, outResolution,
                    filter, mImageSeq, ++sequencePrio, mBackgroundHandler);
            filterTiles.add(FilterTile);
        }
        Log.d(Poladroid.TAG, "} PolaroidActivity.listFilterTiles() => " + filterTiles.size() + " filters");
        return filterTiles;
    }

    private void populateFilterTilesAdapter() {
        ArrayList<FilterTile> filterTiles = listFilterTiles();
        mFilterTilesAdapter = new FilterTilesAdapter(this, filterTiles);
    }

    private void populateFilterTilesView(ViewGroup parent, BaseAdapter adapter) {
        Log.d(Poladroid.TAG, "PolaroidActivity.populateFilterTilesView(" + adapter.getCount()
                + " filters){");

        HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.filter_options);
        scrollView.scrollTo(0, 0);

        parent.removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++) {
            FrameLayout tileView = (FrameLayout) adapter.getView(i, null, parent);
            parent.addView(tileView, i);
            FilterTile tile = (FilterTile) adapter.getItem(i);
            tileView.setTag(tile);
            tile.setView(tileView);
            tileView.setOnClickListener(mFilterTileOnClickListener);
            if (i == 0 && mLayoutComplete) {
                selectFilterTile(tileView, false, true);
            }
        }
        Log.d(Poladroid.TAG, "} PolaroidActivity.populateFilterTilesView()");
    }

    private OnClickListener mFilterTileOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            selectFilterTile(view, true, true);
        }
    };

    private void setFilterTileSelected(View view) {
        if (mSelectedFilterTile != null) {
            mSelectedFilterTile.setSelected(false);
            mSelectedFilterTile = null;
        }
        mSelectedFilterTile = view;
        if (view != null) {
            view.setSelected(true);
        } else {
            Log.e(Poladroid.TAG, "setFilterTileSelected view is NULL");
        }
    }

    private void selectFilterTile(View view, boolean selectedManually, boolean updatePreview) {
        Log.d(Poladroid.TAG, "PolaroidActivity.selectFilterTile(manually: " + selectedManually
                + ", filterSelected: " + mPolaroidConfig.mFilterSelected + ", frameSelected: "
                + mPolaroidConfig.mFrameSelected + ", filterFromFrame: "
                + mPolaroidConfig.mFilterFromFrame + "){");

        try {
            setFilterTileSelected(view);

            mPolaroidConfig.mFilterSelected |= selectedManually;

            int filterIndex = mFilterTilesView.indexOfChild(view);
            currFilterIndex = filterIndex;
            Filter filter = FilterManager.getFilter(filterIndex);
            mPolaroidConfig.mFilterName = filter.getName();
            if (selectedManually) {
                mPolaroidConfig.mFilterFromFrame = false;
            }

            if (mUpdateFrameTiles != view && !mPolaroidConfig.mFilterFromFrame) {
                // A short while later update frame tiles (not at same time, to
                // avoid increasing filter duration)
                Log.d(Poladroid.TAG, "PolaroidActivity.selectFilterTile(): posting mUpdateFrameTiles");
                mForegroundHandler.removeCallbacks(mUpdateFrameTiles);
                mForegroundHandler.postDelayed(mUpdateFrameTiles, 300);
            }

            if (!mPolaroidConfig.mFrameSelected && !mPolaroidConfig.mFilterFromFrame) {
                mPolaroidConfig.mFrameFromFilter = true;

                try {
                    // If there is any preferred frame, try to set it too
                    int frameIndex = 0;
                    Frame preferredFrame = filter.getPreferredFrame();
                    if (preferredFrame == null) {
                        Log.d(Poladroid.TAG, "Filter " + filter.getName() + " has no preferred frame");
                    } else {
                        Log.d(Poladroid.TAG, "Filter " + filter.getName() + " has a preferred frame: "
                                + preferredFrame.getName());
                        int i = 0;
                        for (Iterator<Frame> iterator = FrameManager.iterator(); iterator.hasNext(); i++) {
                            Frame frame = iterator.next();
                            if (preferredFrame.getName().equals(frame.getName())) {
                                Log.d(Poladroid.TAG, "Found the frame at index " + i);
                                frameIndex = i;
                                break;
                            }
                        }
                    }
                    if (mFilterTilesView != null) {
                        View frameTile = mFrameTilesView.getChildAt(frameIndex);
                        selectFrameTile(frameTile, false, false);
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }

            if (updatePreview) {
                updatePreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(Poladroid.TAG, "} PolaroidActivity.selectFilterTile()");
    }

    private Runnable mUpdateFrameTiles = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(Poladroid.TAG, "PolaroidActivity.mUpdateFrameTiles.run(){");
                ImageView imageView = (ImageView) mSelectedFilterTile.findViewById(R.id.image);
                mInFrameTileDrawable = (BitmapDrawable) imageView.getDrawable();
                populateFrameTilesView(mFrameTilesView, mFrameTilesAdapter);
                Log.d(Poladroid.TAG, "} PolaroidActivity.mUpdateFrameTiles.run()");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Frame tile adapter
     */
    private void populateFrameTilesAdapter() {
        ArrayList<FrameTile> frameTiles = listFrameTiles();
        mFrameSize = frameTiles.size();
        mFrameTilesAdapter = new FrameTilesAdapter(this, frameTiles);
    }

    private ArrayList<FrameTile> listFrameTiles() {
        Log.d(Poladroid.TAG, "PolaroidActivity.listFrameTiles(){");
        final ArrayList<FrameTile> frameTiles = new ArrayList<FrameTile>(10);

        for (Iterator<Frame> iterator = FrameManager.iterator(); iterator.hasNext();) {
            Frame frame = iterator.next();
            FrameTile frameTile = new FrameTile(frame);
            frameTiles.add(frameTile);
        }

        Log.d(Poladroid.TAG, "} PolaroidActivity.listFrameTiles() => " + frameTiles.size() + " frames");
        return frameTiles;
    }

    private void populateFrameTilesView(ViewGroup parent, FrameTilesAdapter adapter) {
        Log.d(Poladroid.TAG, "PolaroidActivity.populateFrameTilesView(" + adapter.getCount() + " frames){");

        HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.frame_options);
        scrollView.scrollTo(0, 0);

        if (mInFrameTileDrawable == null) {
            mInFrameTileDrawable = mInFilterTileDrawable;
            if (mInFrameTileDrawable == null) {
                mInFrameTileDrawable = mInDrawable;
            }
        }
        adapter.setPicture(mInFrameTileDrawable);

        String selectedFrameName = null;
        Filter selectedFilter = FilterManager.getFilter(mPolaroidConfig.mFilterName);
        if (selectedFilter != null) {
            Frame preferredFrame = selectedFilter.getPreferredFrame();
            if (preferredFrame != null) {
                selectedFrameName = preferredFrame.getName();
            }
        }

        parent.removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++) {
            FrameLayout tileView = (FrameLayout) adapter.getView(i, null, parent);
            parent.addView(tileView, i);
            FrameTile tile = (FrameTile) adapter.getItem(i);
            tileView.setTag(tile);
            tileView.setOnClickListener(mFrameTileOnClickListener);

            if (!mPolaroidConfig.mFrameSelected
                    && ((selectedFrameName == null && i == 0) || (selectedFrameName != null && selectedFrameName
                            .equals(tile.mFrame.getName()))) && mLayoutComplete) {
                selectFrameTile(tileView, false, true);
                // setFrameTileSelected(tileView);
            }
        }
        Log.d(Poladroid.TAG, "} PolaroidActivity.populateFrameTilesView()");
    }

    private OnClickListener mFrameTileOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            selectFrameTile(view, true, true);
        }
    };

    private void setFrameTileSelected(View view) {
        if (mSelectedFrameTile != null) {
            mSelectedFrameTile.setSelected(false);
            mSelectedFrameTile = null;
        }
        mSelectedFrameTile = view;
        view.setSelected(true);
    }

    private void selectFrameTile(View view, boolean selectedManually, boolean updatePreview) {
        Log.d(Poladroid.TAG, "PolaroidActivity.selectFrameTile(manually: " + selectedManually
                + ", filterSelected: " + mPolaroidConfig.mFilterSelected + ", frameSelected: "
                + mPolaroidConfig.mFrameSelected + ", frameFromFilter: "
                + mPolaroidConfig.mFrameFromFilter + "){");

        try {
            setFrameTileSelected(view);

            mPolaroidConfig.mFrameSelected |= selectedManually;

            int frameIndex = mFrameTilesView.indexOfChild(view);
            currFrameIndex = frameIndex;
            Frame frame = FrameManager.getFrame(frameIndex);
            mPolaroidConfig.mFrameName = frame.getName();

            Log.e(Poladroid.TAG, "frameIndex=" + frameIndex + " mFrameSize:" + mFrameSize);
            if (frameIndex == mFrameSize - 1) {
                resetTag();
            }

            if (selectedManually) {
                mPolaroidConfig.mFrameFromFilter = false;
            }

            if (!mPolaroidConfig.mFilterSelected && !mPolaroidConfig.mFrameFromFilter) {
                mPolaroidConfig.mFilterFromFrame = true;
                try {
                    // If there is any preferred filter, try to set it too
                    int filterIndex = 0;
                    Filter preferredFilter = frame.getPreferredFilter();
                    if (preferredFilter == null) {
                        Log.d(Poladroid.TAG, "Frame " + frame.getName() + " has no preferred filter");
                    } else {
                        Log.d(Poladroid.TAG, "Frame " + frame.getName() + " has a preferred filter: "
                                + preferredFilter.getName());
                        int i = 0;
                        for (Iterator<Filter> iterator = FilterManager.iterator(); iterator
                                .hasNext(); i++) {
                            Filter filter = iterator.next();
                            if (preferredFilter.getName().equals(filter.getName())) {
                                Log.d(Poladroid.TAG, "Found the filter at index " + i);
                                filterIndex = i;
                                break;
                            }
                        }
                    }
                    if (mFilterTilesView != null) {
                        View filterTile = mFilterTilesView.getChildAt(filterIndex);
                        selectFilterTile(filterTile, false, false);
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }

            if (updatePreview) {
                updatePreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(Poladroid.TAG, "} PolaroidActivity.selectFrameTile(){");
    }

    private void updatePreview() {
        Log.d(Poladroid.TAG, "PolaroidActivity.updatePreview(){");

        if (mPolaroidView == null) {
            Log.d(Poladroid.TAG, "mPolaroidView is still null, init in progress...");
        } else {
            Frame frame = FrameManager.getFrame(mPolaroidConfig.mFrameName);
            Point outResolution = PolaroidView.computePictureResolution(mPolaroidView.getLayout(),
                    frame);

            Rect inCrop = Utils.getCenteredScaledCrop(mInDrawable, outResolution);

            PreviewParams newPreviewParams = new PreviewParams(mImageSeq, mInDrawable,
                    mPolaroidConfig.mFilterName, inCrop, outResolution);

            if (mPreviewParams == null || !mPreviewParams.equals(newPreviewParams)) {
                Log.d(Poladroid.TAG, "PolaroidActivity.updatePreview(): need to update preview image");
                if (mPreviewParams != null) {
                    Log.d(Poladroid.TAG, "mPreviewParams.mImageSeq : " + mPreviewParams.mImageSeq);
                }
                Log.d(Poladroid.TAG, "newPreviewParams.mImageSeq: " + newPreviewParams.mImageSeq);

                if (mPreviewParams == null
                        || mPreviewParams.mImageSeq != newPreviewParams.mImageSeq
                        || !mPreviewParams.mInCrop.equals(newPreviewParams.mInCrop)
                        || !mPreviewParams.mOutResolution.equals(newPreviewParams.mOutResolution)) {
                    Log.d(Poladroid.TAG, "Creating original preview drawable {");
                    mInPreviewDrawable = Utils.getScaledCroppedDrawable(this, mInDrawable, inCrop,
                            outResolution);
                    Log.d(Poladroid.TAG, "} Creating preview drawable");
                }

                mPreviewParams = newPreviewParams;

                Message msg;
                FilterConfig filterConfig;

                // Remove any ongoing request for preview params
                msg = Message.obtain();
                msg.what = Poladroid.UI2BG_REMOVE_FILTER_CMD;
                filterConfig = new FilterConfig(FilterConfig.ALL_PRIORITIES,
                        Poladroid.PREVIEW_SIZE_PRIORITY, FilterConfig.ALL_PRIORITIES);
                msg.obj = filterConfig;
                mBackgroundHandler.sendMessage(msg);

                // Add request for new params
                msg = Message.obtain();
                msg.what = Poladroid.UI2BG_ADD_FILTER_CMD;
                filterConfig = new FilterConfig(mInPreviewDrawable, inCrop, outResolution,
                        Quality.HIGHEST, FilterManager.getFilter(mPolaroidConfig.mFilterName),
                        mImageSeq, Poladroid.PREVIEW_SIZE_PRIORITY, 0, this);
                msg.obj = filterConfig;
                mBackgroundHandler.sendMessage(msg);
            } else {
                mPolaroidConfig.mSlogan = mPolaroidView.getSlogan();
                mPolaroidView.setFrame(frame);
                mPolaroidView.setSlogan(mPolaroidConfig.mSlogan);

                if (mPreviewDrawable != null) {
                    Log.d(Poladroid.TAG, "PolaroidActivity.updatePreview(): found valid preview image");
                    mPolaroidView.setPicture(mPreviewDrawable);
                }
                mPolaroidView.setVisibility(View.VISIBLE);
                hideProgress();
            }
            if (mRightButton != null && mCurrentState == CURRENT_ACTION_EDITOR) {
                mRightButton.setEnabled(true);
            }
            mHasSaved = false;
        }

        Log.d(Poladroid.TAG, "} PolaroidActivity.updatePreview()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(Poladroid.TAG, "PolaroidActivity.onPause(){");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-08-04,PR1059410 begin
        isShareImage = false;
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-08-04,PR1059410 end
        Log.d(Poladroid.TAG, "PolaroidActivity.onResume(){");
    }

    private void savePolaroid(boolean isShare) {
        Log.d(Poladroid.TAG, "PolaroidActivity.savePolaroid(){");
        this.isShareImage = isShare;
        try {
            if (mSaveProgress.getVisibility() != View.GONE) {
                Log.w(Poladroid.TAG, "*** PolaroidActivity.savePolaroid(): Already saving something !???");
            } else {
                showProgress();
                updateViewEnable(false);
                mHasSaved = false;
                // For selected frame, pick the best resource
                // suitable for our full resolution input image
                int pictureWidth = mInDrawable.getIntrinsicWidth();
                int pictureHeight = mInDrawable.getIntrinsicHeight();
                Log.d(Poladroid.TAG, "Original picture size: " + pictureWidth + "x" + pictureHeight);

                Frame frame = FrameManager.getFrame(mPolaroidConfig.mFrameName);
                FrameResource frameResource = frame.pickBestResourceForPicture(pictureWidth,
                        pictureHeight);
                // NOT supposed to be
                Log.d(Poladroid.TAG, "Best frame resource: " + frameResource);
                int framePictureWidth = frameResource.mPictureLocation.width();
                int framePictureHeight = frameResource.mPictureLocation.height();
                Log.d(Poladroid.TAG, "Frame picture (unresized): " + framePictureWidth + "x"
                        + framePictureHeight); // NOT supposed to be null
                // Find the limiting factor: width or height
                float ratio = 1.0f;
                if (pictureWidth * framePictureHeight <= pictureHeight * framePictureWidth) {
                    // Scale according to width, Crop vertically
                    ratio = 1.0f * pictureWidth / framePictureWidth;
                } else {
                    // Scale according to height, Crop horizontally
                    ratio = 1.0f * pictureHeight / framePictureHeight;
                }

                // Set the size of our polaroid view so that
                // the internal picture view will be more or less equal
                // to our full resolution input image
                int polaroidWidth = (int) Math.floor(ratio * frameResource.mTargetResolution.x);
                int polaroidHeight = (int) Math.floor(ratio * frameResource.mTargetResolution.y);
                Log.d(Poladroid.TAG, "Polaroid size: " + polaroidWidth + "x" + polaroidHeight);

                Rect layout = new Rect(0, 0, polaroidWidth, polaroidHeight);
                Point outResolution = PolaroidView.computePictureResolution(layout, frame);
                // To avoid some VERY VERY bad performances, make sure to be
                // multiple of 4 (or 8, even better for 64 bits)
                outResolution.x = ((outResolution.x + 3) / 4) * 4;
                outResolution.y = ((outResolution.y + 3) / 4) * 4;
                Rect inCrop = Utils.getCenteredScaledCrop(mInDrawable, outResolution);

                CropBitmapDrawable cropBitmapDrawable = new CropBitmapDrawable(this, inCrop,
                        outResolution, mBackgroundHandler, mPolaroidConfig, mImageSeq, mHandler,
                        this);
                cropBitmapDrawable.execute(mInDrawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(Poladroid.TAG, "} PolaroidActivity.savePolaroid()");
    }

    private static final int SELECT_FILE = 0;
    private static final int REQUEST_CAMERA = 1;

    private void openImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };

        Log.d(Poladroid.TAG, "PolaroidActivity.openImage()");
        AlertDialog.Builder builder = new AlertDialog.Builder(PolaroidActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(),
                            "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(Poladroid.TAG, "PolaroidActivity.onActivityResult(req: " + requestCode + ", res: "
                + resultCode + ", data: " + data + ")");

        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_CAMERA) {
                    File f = new File(Environment.getExternalStorageDirectory().toString());
                    for (File temp : f.listFiles()) {
                        if (temp.getName().equals("temp.jpg")) {
                            f = temp;
                            break;
                        }
                    }

                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), bitmapOptions);
                    mInDrawable = new BitmapDrawable(getResources(), bitmap);
                } else if (requestCode == SELECT_FILE) {
                    Uri selectedImageUri = data.getData();
                    startLoadBitmap(selectedImageUri);
                }

                mImageSeq++;

                mPolaroidView.setVisibility(View.INVISIBLE);
                setToolbarState(ToolbarState.FILTERS);
                mPolaroidConfig.reset(this);
                updatePreview();
                mInFilterTileDrawable = null;
                populateFilterTilesAdapter();
                mInFrameTileDrawable = mInFilterTileDrawable;
                populateFrameTilesView(mFrameTilesView, mFrameTilesAdapter);
                populateFilterTilesView(mFilterTilesView, mFilterTilesAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getPath(Uri uri, Activity activity) {
        String path = "";
        String[] projection = { MediaColumns.DATA };
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, projection, null, null, null);
            // cursor = activity.managedQuery(uri, projection, null, null,
            // null);
            int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
        } catch (Exception e) {
            Log.e(Poladroid.TAG, "getPath error, e:" + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    @Override
    public void onFilterComplete(FilterConfig filterConfig) {
        Log.d(Poladroid.TAG, "PolaroidActivity.onFilterComplete(" + filterConfig + "){");

        if (filterConfig.mSizePrio == Poladroid.PREVIEW_SIZE_PRIORITY && filterConfig.mOutDrawable != null
                && mPreviewParams != null) {
            Log.d(Poladroid.TAG, "Got preview drawable fromMSG_SAVE_IMAGE_COMPLETED " + filterConfig);
            recycleBitmap(mPreviewDrawable, "onFilterComplete recycle PreviewDrawable");
            mPreviewDrawable = filterConfig.mOutDrawable;
            updatePreview();

            String info = String.format(Locale.US, "%s: %d ms (%d operation%s)",
                    filterConfig.mFilter.getName().toUpperCase(Locale.US),
                    filterConfig.mRenderingDuration, filterConfig.mFilter.getSize(),
                    (filterConfig.mFilter.getSize() == 1) ? "" : "s");
            Log.i(Poladroid.TAG, "--TIME USED:" + info);
        } else if (filterConfig.mSizePrio == Poladroid.FULL_SIZE_PRIORITY
                && filterConfig.mOutDrawable != null) {
            mPolaroidConfig.mSlogan = mPolaroidView.getSlogan();
            mPolaroidConfig.mDateTag = mPolaroidView.getDateTag();
            mPolaroidConfig.mLocationTag = mPolaroidView.getLocationTag();
            mPolaroidConfig.mTagStatus = mTagStatus;
            saveBitmapDrawable = new SaveBitmapDrawable(this, filterConfig, mPolaroidConfig,
                    mInDrawable, mPolaroidConfig.mSlogan, mHandler);
            saveBitmapDrawable.execute();
        }

        Log.d(Poladroid.TAG, "} PolaroidActivity.onFilterComplete()");
    }

    private void launchPolaroidShare(Uri uri) {
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-08-04,PR1059410 begin
        isShareImage = true;
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-08-04,PR1059410 end
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        Log.d(Poladroid.TAG, "launchPhotoShare Share Uri: " + uri);
        try {
            startActivity(Intent.createChooser(intent,
                    getResources().getString(R.string.photo_share)));
        } catch (android.content.ActivityNotFoundException e) {
            Log.e(Poladroid.TAG, "launchPhotoShare Cannot find any activity", e);
        }
    }

    private void updateFile(String fileName) {
        MediaScannerConnection.scanFile(PolaroidActivity.this, new String[] { fileName }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i(Poladroid.TAG,
                                "Utils.saveBitmap.MediaScannerConnection.onScanCompleted(scanned: "
                                        + path + ") => URI: " + uri);
                        if (uri != null) {
                            mShareUri = uri;
                            if (isShareImage) {
                                launchPolaroidShare(uri);
                            }
                        } else {
                            Log.e(Poladroid.TAG, "shareImage error, uri is NULL");
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-08-04,PR1059410 begin
        long currentTime = System.currentTimeMillis();
        if (currentTime - mClickTime < 500) {
            return;
        }
        mClickTime = currentTime;
        switch (v.getId()) {
        case R.id.editor_left:
            if (isShareImage)
                return;
            // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-08-04,PR1059410 end
            done();
            break;
        case R.id.editor_right:
            if (mCurrentState == CURRENT_ACTION_CROP) {
                updatePolaroidView(true);
            } else if (mCurrentState == CURRENT_ACTION_EDITOR) {
                // not share and not exit
                savePolaroid(false);
            }
            break;
        case R.id.editor_share:
            if (mShareUri != null && mHasSaved) {
                launchPolaroidShare(mShareUri);
            } else {
                // share but not exit
                savePolaroid(true);
            }
            break;
        default:
            break;
        }
    }

    public void done() {
        hideProgress();
        finish();
    }

    private void updatePolaroidView(boolean needCrop) {
        if (needCrop) {
            BitmapDrawable tmpDrawable = new BitmapDrawable(getResources(), mCropImage.getCropImage());
            recycleBitmap(mInDrawable, "updatePolaroidView recycle InDrawable");
            mInDrawable = tmpDrawable;
        } else {
            Log.i(Poladroid.TAG, "updatePolaroidView current picture is square.");
        }
        mCurrentState = CURRENT_ACTION_EDITOR;
        updateActionBar(mCurrentState);

        mImageSeq++;

        mPolaroidView.setVisibility(View.INVISIBLE);
        mCropImage.setVisibility(View.INVISIBLE);
        setToolbarState(ToolbarState.FILTERS);
        mPolaroidConfig.reset(this);
        // updatePreview();
        mInFilterTileDrawable = null;
        populateFilterTilesAdapter();
        mInFrameTileDrawable = mInFilterTileDrawable;
        populateFrameTilesView(mFrameTilesView, mFrameTilesAdapter);
        populateFilterTilesView(mFilterTilesView, mFilterTilesAdapter);
        // mPolaroidView.setFrame(FrameManager.getFrame(0));
        setToolBarEnable(true);
        setActionBarEnabled(true);
    }

    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-18,PR1025898 begin
    @Override
    public void updateSaveState() {
        mHasSaved = false;
        if (mRightButton != null && mCurrentState == CURRENT_ACTION_EDITOR) {
            mRightButton.setEnabled(true);
        }
    }
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-18,PR1025898 end

    // [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-06-19,PR1026015 begin
    private void destoryBitmapResource() {
        destoryFilterBitmapResource();
        recycleBitmap(mInDrawable, "InDrawable");
        recycleBitmap(mInFilterTileDrawable, "InFilterTileDrawable");
        recycleBitmap(mInFrameTileDrawable, "InFrameTileDrawable");
        recycleBitmap(mInPreviewDrawable, "InPreviewDrawable");
        recycleBitmap(mPreviewDrawable, "PreviewDrawable");
    }

    private void destoryFilterBitmapResource() {
        if(mFilterTilesView != null) {
            int count = mFilterTilesView.getChildCount();
            for(int i = 0; i < count; i++) {
                View view = mFilterTilesView.getChildAt(i);
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                Drawable drawable = imageView.getDrawable();
                if(drawable instanceof BitmapDrawable)
                    recycleBitmap((BitmapDrawable)drawable, "FilterBitmapResource index=" + i);
            }
        }
    }

    private static int mTotalRecycleMemorySize = 0;
    private void recycleBitmap(BitmapDrawable target, String msg) {
        if(target != null && !target.getBitmap().isRecycled()) {
            int memoryAllocation = target.getBitmap().getAllocationByteCount();
            Log.i(TAG, "recycleBitmap " + msg + " memory size is " + (memoryAllocation / 1024) + "kb");
            target.getBitmap().recycle();
            mTotalRecycleMemorySize += memoryAllocation;
            Log.i(TAG, "total recycle memory size is " + (mTotalRecycleMemorySize / 1024) + "kb");
        }
    }
    // [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-06-19,PR1026015 end
}
/* EOF */
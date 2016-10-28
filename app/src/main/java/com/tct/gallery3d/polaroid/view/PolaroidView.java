/* ----------|----------------------|----------------------|----------------- */
/* 06/18/2015| jian.pan1            | PR1025898            |[Android 5.1][Camera_v5.1.11.0210.0][Polaroid]The save icon show invalid after inputting characters
/* ----------|----------------------|----------------------|----------------- */
/* 06/19/2015| chengbin.du          | PR1026702            |[Android 5.1][Gallery_Polaroid_v5.1.13.1.0209.0]The cursor change to front after tapping on tag
/* ----------|----------------------|----------------------|----------------- */
/* 06/30/2015| chengbin.du          | PR1026689            |[Android 5.1][Gallery_Polaroid_v5.1.13.1.0209.0]Can't show the expression completely
/* ----------|----------------------|----------------------|----------------- */
/* 07/03/2015| chengbin.du          | PR1036203            |[Android 5.1][Gallery_v5.1.13.1.0211.0_Polaroid]The cursor is out of the input frame
/* ----------|----------------------|----------------------|----------------- */
/* 07/09/2015| chengbin.du          | PR1040101            |[Android 5.1][Gallery_Polaroid_v5.1.13.1.0212.0]The cursor jump to the text end automatically
/* ----------|----------------------|----------------------|----------------- */
/* 07/10/2015| chengbin.du          | PR1041948            |[Android 5.1][Gallery_v5.1.13.1.0212.0][Force Close][Monitor]Gallery will force close when entering gallery from camera
/* ----------|----------------------|----------------------|----------------- */
/* 07/14/2015| chengbin.du          | PR1026715            |[Android5.1][Gallery_Polaroid_v5.1.13.1.0209.0]The slogan is not in the middle of the screen.
/* ----------|----------------------|----------------------|----------------- */
/* 07/16/2015| chengbin.du          | PR1026690            |[Android5.1][Gallery_v5.1.13.1.0209.0_polaroid]Slogan change to other place when switch frame
/* ----------|----------------------|----------------------|----------------- */
/* 07/29/2015| jian.pan1            | PR1051698            |[Android 5.1][Gallery_v5.1.13.1.0214.0]The slogan is display too small
/* ----------|----------------------|----------------------|----------------- */
/* 08/07/2015| dongliang.feng       | PR495513             |[Android5.1][Gallery_v5.2.0.1.1.0302.0][Force Close]Gallery force close when add location information on polaroid edit
/* ----------|----------------------|----------------------|----------------- */
/* 08/17/2015| dongliang.feng       | PR511142             |[Android5.1][Gallery_v5.2.0.1.1.0303.0]It not display location information in polaroid edit interface
/* ----------|----------------------|----------------------|----------------- */
/* 09/11/2015| dongliang.feng       | PR497147             |[Android 5.1][Gallery_v5.2.0.1.1.0302.0]The operation bar should disappear after tapping ‘done’
/* ----------|----------------------|----------------------|----------------- */
/* 08/07/2015| dongliang.feng       | PR1063050            |[Language][Korean][Spanish][Simplified Chinese][Traditional Chinese]Polaroid
/*           |                      |                      |edit screen the title "Polaroid" , "No LOCATION" ,"Give mu a hug" not translate
/* ----------|----------------------|----------------------|----------------- */
/* 10/09/2015| dongliang.feng       | PR541179             |[Android5.1][Gallery_v5.1.13.1.0221.0]The slogan is display too samll
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.polaroid.view;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.tct.gallery3d.R;
import com.tct.gallery3d.exif.ExifInterface;
import com.tct.gallery3d.exif.ExifTag;
import com.tct.gallery3d.polaroid.Poladroid;
import com.tct.gallery3d.polaroid.PolaroidActivity;
import com.tct.gallery3d.polaroid.manager.Font;
import com.tct.gallery3d.polaroid.manager.Frame;
import com.tct.gallery3d.polaroid.manager.FrameResource;
import com.tct.gallery3d.polaroid.tools.Utils;
import com.tct.gallery3d.util.ReverseGeocoder;

public class PolaroidView extends FrameLayout implements TextWatcher{
    private static final String TAG = "PolaroidView";

    private ImageView mBgFrameView, mFgFrameView;
    private ImageView mPictureView;
    private SloganText mSloganView;// [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-07-16,PR1026690
    private FrameLayout mBoxView;
    private TagView mDateView;
    private TagView mLocationView;

    private int mTagStatus = PolaroidActivity.NO_TAG;
    private String mDateTagText = null;
    private String mLocationTagText = ""; //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-09, PR495513
    private float mResourceRatio = 1.0f;
    private double[] mLatLong = null;
    private float mOldX = 0f;

    private Rect mLayout = new Rect();
    private Rect mBoxLayout = new Rect();
    private Rect mPictureLayout = new Rect();
    private Rect mSloganLayout = new Rect();

    private Frame mFrame;
    private FrameResource mFrameResource;

    private TextPaint mSloganPaint = new TextPaint();

    private Handler mHandler = null;
    private Handler mMainHandler = null;

    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-29,PR1051698 begin
    private InputMethodManager mKeyboardManager;
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-29,PR1051698 end

    private boolean touchEnable = false;
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-18,PR1025898 begin
    private ISaveStateListener mSaveStateListener;

    public void setSaveStateListener(ISaveStateListener listener) {
        mSaveStateListener = listener;
    }

    public interface ISaveStateListener {
        public void updateSaveState();
    }
    // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-18,PR1025898 end

    public boolean isTouchEnable() {
        return touchEnable;
    }

    public void setTouchEnable(boolean touchEnable) {
        this.touchEnable = touchEnable;
    }

    private GoogleApiClient mGoogleApiClient = null;
    private ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle connectionHint) {
            Log.e(TAG, "ConnectionCallbacks onConnected");
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    getAddressFromGoogleService();
                }
            });
            t.start();
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.e(TAG, "ConnectionSuspended");
        }
    };
    private OnConnectionFailedListener mConnectionFailedListener = new OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e(TAG, "onConnectionFailed result " + result.getErrorCode());
        }
    };

    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-11, PR497147 begin
    private ActionMode mActionMode = null;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-11, PR497147 end

    public static final int MSG_SWIP_LEFT = 0;
    public static final int MSG_SWIP_RIGHT = 1;

    protected enum State {
        NONE, STARTSWIPE, SWIPING, STOPSWIPE
    }

    private State mState = State.NONE;

    public Handler getMainHandler() {
        return mMainHandler;
    }

    public void setMainHandler(Handler mainHandler) {
        this.mMainHandler = mainHandler;
    }

    public PolaroidView(Context context) {
        this(context, null);
        return; // Nothing more here, intentionally
    }

    public PolaroidView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        return; // Nothing more here, intentionally
    }

    public PolaroidView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // The real constructor is here...
        Log.d(Poladroid.TAG, "new PolaroidView()");
        mHandler = new Handler(Looper.getMainLooper());

        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-29,PR1051698 begin
        mKeyboardManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-29,PR1051698 end

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.polaroid_view, this, true);

        mBoxView = (FrameLayout) view.findViewById(R.id.box);
        mFgFrameView = (ImageView) view.findViewById(R.id.fg_frame);
        mBgFrameView = (ImageView) view.findViewById(R.id.bg_frame);
        mPictureView = (ImageView) view.findViewById(R.id.picture);
        mSloganView = (SloganText) view.findViewById(R.id.slogan);// [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-07-16,PR1026690
        mDateView = (TagView) view.findViewById(R.id.date_text);
        mLocationView = (TagView) view.findViewById(R.id.location_text);

        mSloganView.addTextChangedListener(this);
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-11, PR497147 begin
        mSloganView.setCustomSelectionActionModeCallback(mActionModeCallback);
        mSloganView.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (null != mActionMode && actionId == EditorInfo.IME_ACTION_DONE) {
                    mActionMode.finish();
                }
                return false;
            }
        });
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-11, PR497147 end
        displayTagView(PolaroidActivity.NO_TAG);
    }

    public void displayTagView(int tag) {
        mTagStatus = tag;
        int index = mSloganView.getSelectionEnd();

        if ((tag & PolaroidActivity.TAG_SLOGAN) != 0) {
            mSloganView.setVisibility(VISIBLE);
            layoutSlogan(mBoxLayout, mPictureLayout);
        } else {
            mSloganView.setVisibility(GONE);
        }

        if ((tag & PolaroidActivity.TAG_DATE) != 0) {
            mDateView.setVisibility(VISIBLE);
            layoutDateTag(mBoxLayout, mPictureLayout, mDateTagText);
        } else {
            mDateView.setVisibility(GONE);
        }

        if ((tag & PolaroidActivity.TAG_LOCATION) != 0) {
            mLocationView.setVisibility(VISIBLE);
            layoutLocationTag(mBoxLayout, mPictureLayout, mLocationTagText);
        } else {
            mLocationView.setVisibility(GONE);
        }

        // [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-07-09,PR1040101 begin
        mSloganView.setSelection(index);
        // [BUGFIX]-Add by TCTNJ,chengbin.du, 2015-07-09,PR1040101 end
    }

    public void assyncGetExifInfo(String file) {
        final String value = file;
        new Thread(new Runnable() {
            @Override
            public void run() {
                getExifInfo(value);
            }
        }).start();
    }

    public void getExifInfo(String file) {
        Log.d(TAG, "getExifInfo+");
        if (file == null)
            return;

        // read exif from file
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(file);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            Log.e(TAG, "getExifInfo FileNotFoundException:", e1);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getExifInfo IOException:", e);
        }

        // get exif
        String dateValue = "";
        ExifTag tag = exif.getTag(ExifInterface.TAG_DATE_TIME);
        if (tag != null) {
            dateValue = tag.getValueAsString();
        }

        mLatLong = exif.getLatLongAsDoubles();
        if (mLatLong != null) {
            Log.d(TAG, "latitute=" + mLatLong[0] + " longitude=" + mLatLong[1]);
        }

        // parse date
        if (dateValue.length() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss");
            try {
                Date date = sdf.parse(dateValue);
                SimpleDateFormat formater = new SimpleDateFormat("MM.dd.yyyy");
                mDateTagText = formater.format(date);
                Log.e(TAG, "image exif datetime : " + mDateTagText);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }  else {
            SimpleDateFormat formater = new SimpleDateFormat("MM.dd.yyyy");
            mDateTagText = formater.format(new Date());
            Log.d(TAG, "update date tag:" + mDateTagText);
        }

        // reverse address
        getAddressInfo(mLatLong);
        Log.d(TAG, "getExifInfo-");
    }

    private void getAddressInfo(double[] latlong) {
        if (latlong != null) {
            Log.d(TAG, "lacation: " + latlong[0] + " " + latlong[1]);
            ReverseGeocoder geocoder = new ReverseGeocoder(getContext());
            Address address = geocoder.lookupAddress(latlong[0], latlong[1], false);
            if (address != null) {
                //address.getThoroughfare()
                //address.getFeatureName()
                final String tag = address.getLocality() + " " + address.getAdminArea();
                Log.e(TAG, "getAddressInfo result " + tag);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLocationTagText = tag.toUpperCase();
                        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-17, PR511142 begin
                        if (mLocationView.getVisibility() == VISIBLE) {
                            layoutLocationTag(mBoxLayout, mPictureLayout, mLocationTagText);
                        }
                        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-17, PR511142 end
                    }
                });
            } else {
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-17, PR511142 begin
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLocationTagText = getResources().getString(R.string.polaroid_no_location); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-07, PR1063050;
                        if (mLocationView.getVisibility() == VISIBLE) {
                            layoutLocationTag(mBoxLayout, mPictureLayout, mLocationTagText);
                        }
                    }
                });
                //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-17, PR511142 end
            }
        } else {
            startGoogleService();
        }
    }

    private void startGoogleService() {
        Log.d(TAG, "startGoogleService");
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        if(resultCode == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Services Available");
            if(mGoogleApiClient == null) {
                Log.d(TAG, "Create GoogleApiClient");
                mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mConnectionFailedListener)
                    .addApi(LocationServices.API).build();
                mGoogleApiClient.connect();
            } else if(mGoogleApiClient.isConnected()) {
                Log.d(TAG, "GoogleApiClient isConnected");
                getAddressFromGoogleService();
            } else if(!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                Log.d(TAG, "GoogleApiClient isn't Connected");
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Google Play Services Unavailable");
            mLocationTagText = getResources().getString(R.string.polaroid_no_location); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-07, PR1063050;
        }
    }

    private void getAddressFromGoogleService() {
        Log.d(TAG, "getAddressFromGoogleService");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location != null) {
            Log.d(TAG, "getLastLocation SUCCESS");
            double[] latlong = new double[2];
            latlong[0] = location.getLatitude();
            latlong[1] = location.getLongitude();
            getAddressInfo(latlong);
        } else {
            Log.e(TAG, "getAddressFromGoogleService is null");
            mLocationTagText = getResources().getString(R.string.polaroid_no_location); //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-08-07, PR1063050;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            Log.d(Poladroid.TAG, "PolaroidView.onLayout(left: " + left + ", top: " + top + ", right: "
                    + right + ", bottom: " + bottom + "){");

            mLayout.left = left + getPaddingLeft();
            mLayout.top = top + getPaddingTop();
            mLayout.right = right - getPaddingRight();
            mLayout.bottom = bottom - getPaddingBottom();

            updateLayout(false);

            Log.d(Poladroid.TAG, "} PolaroidView.onLayout()");
        }
    }

    private static float getResourceRatio(Rect polaroidLayout, FrameResource frameResource) {
        float ratio = 1.0f;

        if (frameResource != null) {
            int targetWidth = Math.max(0, polaroidLayout.right - polaroidLayout.left);
            int targetHeight = Math.max(0, polaroidLayout.bottom - polaroidLayout.top);

            // If the frame is exactly matching one of our dimensions or just
            // slightly smaller
            // then use it without any scaling (i.e. keep best quality)
            // Otherwise, make it fit into our layout (while keeping frame w/h
            // ratio)
            final float NO_STRETCH_LIMIT = 1.00f; // 0.80f
            if ((frameResource.mTargetResolution.x <= targetWidth
                    && 1.0f * frameResource.mTargetResolution.x >= NO_STRETCH_LIMIT * targetWidth && frameResource.mTargetResolution.y <= targetHeight)
                    || (frameResource.mTargetResolution.y <= targetHeight
                            && 1.0f * frameResource.mTargetResolution.y >= NO_STRETCH_LIMIT
                                    * targetHeight && frameResource.mTargetResolution.x <= targetWidth)) {
                Log.d(Poladroid.TAG, "Frame resource fits quite well without stretching / shrinking");
                ratio = 1.0f;
            } else {
                Log.d(Poladroid.TAG, "Frame resource needs stretching / shrinking");
                ratio = Math.min(1.0f * targetWidth / frameResource.mTargetResolution.x, 1.0f
                        * targetHeight / frameResource.mTargetResolution.y);
            }
            Log.d(Poladroid.TAG, "Frame resource resize ratio: " + ratio);
        }
        return ratio;
    }

    private static Rect getBoxLayout(Rect polaroidLayout, FrameResource frameResource,
            float resourceRatio) {
        Rect layout = new Rect();

        int targetWidth = Math.max(0, polaroidLayout.right - polaroidLayout.left);
        int targetHeight = Math.max(0, polaroidLayout.bottom - polaroidLayout.top);

        int boxWidth = (int) Math.floor(resourceRatio * frameResource.mTargetResolution.x + 0.001f);
        int boxHeight = (int) Math
                .floor(resourceRatio * frameResource.mTargetResolution.y + 0.001f);
        layout.left = (targetWidth - boxWidth) / 2;
        layout.top = (targetHeight - boxHeight) / 2;
        layout.right = layout.left + boxWidth;
        layout.bottom = layout.top + boxHeight;

        return layout;
    }

    private static Rect getViewLayout(Rect boxLayout, Rect resourceLocation,
            FrameResource frameResource) {
        Rect layout = new Rect();
        // Compute the coords of the picture inside this frame, extended to
        // floor / ceil pixels
        layout.left = (int) Math.floor(1.0f * boxLayout.width() * resourceLocation.left
                / frameResource.mTargetResolution.x);
        layout.right = (int) Math.ceil(1.0f * boxLayout.width() * resourceLocation.right
                / frameResource.mTargetResolution.x);
        layout.top = (int) Math.floor(1.0f * boxLayout.height() * resourceLocation.top
                / frameResource.mTargetResolution.y);
        layout.bottom = (int) Math.ceil(1.0f * boxLayout.height() * resourceLocation.bottom
                / frameResource.mTargetResolution.y);
        return layout;
    }

    private static Rect getPictureLayout(Rect boxLayout, FrameResource frameResource) {
        return getViewLayout(boxLayout, frameResource.mPictureLocation, frameResource);
    }

    private Rect getSloganLayout(Rect boxLayout, Rect pictureLayout, int tagStatus) {
        Rect sloganLocation = new Rect();
        if ((tagStatus & (PolaroidActivity.TAG_DATE | PolaroidActivity.TAG_LOCATION)) == 0) {
            sloganLocation.left = pictureLayout.left + (int)Math.ceil(mFrameResource.mSloganMargin.left * mResourceRatio);
            sloganLocation.right = pictureLayout.right - (int)Math.ceil(mFrameResource.mSloganMargin.right * mResourceRatio);
            sloganLocation.top = pictureLayout.bottom + (int)Math.ceil(mFrameResource.mSloganMargin.top * mResourceRatio);
            sloganLocation.bottom = boxLayout.bottom - (int)Math.ceil(mFrameResource.mSloganMargin.bottom * mResourceRatio);
        } else {
            sloganLocation.left = pictureLayout.left + (int)Math.ceil(mFrameResource.mSloganMargin.left * mResourceRatio);
            sloganLocation.right = pictureLayout.right - (int)Math.ceil(pictureLayout.width() * 0.28);
            sloganLocation.top = pictureLayout.bottom + (int)Math.ceil(mFrameResource.mSloganMargin.top * mResourceRatio);
            sloganLocation.bottom = boxLayout.bottom - (int)Math.ceil(mFrameResource.mSloganMargin.bottom * mResourceRatio);
        }
        return sloganLocation;
    }

    private void updateLayout(boolean fromFrame) {
        // This function is called when the layout is changed (from parent view)
        // Or when the frame is changed
        Log.d(Poladroid.TAG, "PolaroidView.updateLayout(){");
        try {
            if (mFrame == null) {
                Log.d(Poladroid.TAG, "No frame set yet, just use empty layout for all child views");

                mFrameResource = null;
                mBoxView.layout(0, 0, 0, 0);
                mBgFrameView.layout(0, 0, 0, 0);
                mFgFrameView.layout(0, 0, 0, 0);
                mPictureView.layout(0, 0, 0, 0);
                mSloganView.layout(0, 0, 0, 0);
                mDateView.layout(0, 0, 0, 0);
                mLocationView.layout(0, 0, 0, 0);
            } else {
                int targetWidth = Math.max(0, mLayout.right - mLayout.left);
                int targetHeight = Math.max(0, mLayout.bottom - mLayout.top);
                // Ignore padding for time being (TODO)
                mFrameResource = mFrame.pickBestResourceForTarget(targetWidth, targetHeight);
                // NOT supposed to be null
                Log.d(Poladroid.TAG, "Best frame resource: " + mFrameResource);

                mResourceRatio = getResourceRatio(mLayout, mFrameResource);

                Rect boxLayout = getBoxLayout(mLayout, mFrameResource, mResourceRatio);
                mBoxLayout = boxLayout;

                Log.d(Poladroid.TAG,
                        "Box/Frame layout" + ": left: " + boxLayout.left + ", top: "
                                + boxLayout.top + ", resolution: " + boxLayout.width() + "x"
                                + boxLayout.height());
                mBoxView.layout(boxLayout.left, boxLayout.top, boxLayout.right, boxLayout.bottom);
                mFgFrameView.layout(0, 0, boxLayout.width(), boxLayout.height());
                mBgFrameView.layout(0, 0, boxLayout.width(), boxLayout.height());

                // Compute the coords of the picture inside this frame, extended
                // to floor / ceil pixels
                Rect pictureLayout = getPictureLayout(boxLayout, mFrameResource);
                mPictureLayout = pictureLayout;
                Log.d(Poladroid.TAG, "Picture layout" + ": left: " + pictureLayout.left + ", top: "
                        + pictureLayout.top + ", resolution: " + pictureLayout.width() + "x"
                        + pictureLayout.height());
                mPictureView.layout(pictureLayout.left, pictureLayout.top, pictureLayout.right,
                        pictureLayout.bottom);

                layoutSlogan(mBoxLayout, mPictureLayout);

                if (!fromFrame) {
                    applyFrame(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(Poladroid.TAG, "} PolaroidView.updateLayout()");
    }

    private void updateTagLayout() {
        displayTagView(mTagStatus);
    }

    private static final int alpha = 10;
    private void layoutSlogan(Rect boxLayout, Rect pictureLayout) {
        mSloganLayout = getSloganLayout(boxLayout, pictureLayout, mTagStatus);
        Log.d(Poladroid.TAG, "Slogan layout" + ": left: " + mSloganLayout.left + ", top: "
                + mSloganLayout.top + ", resolution: " + mSloganLayout.width() + "x"
                + mSloganLayout.height());
        mSloganView.layout(mSloganLayout.left, mSloganLayout.top, mSloganLayout.right,
                mSloganLayout.bottom);
        mSloganView.setMaxWidth(mSloganLayout.width());
        mSloganView.setMaxHeight(mSloganLayout.height());
        if ((mTagStatus & (PolaroidActivity.TAG_DATE | PolaroidActivity.TAG_LOCATION)) == 0) {
            mSloganView.setGravity(Gravity.CENTER);
        } else {
            mSloganView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        }

        mSloganView.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mSloganView.setSingleLine(false);
        mSloganView.setFilters(new InputFilter[] { new InputFilter.LengthFilter(80) });
        mSloganView.setHorizontallyScrolling(false);
        mSloganView.setTextColor(mFrame.mSloganFontColor);
        mSloganView.setShadowLayer(
                (int) Math.ceil(mResourceRatio * mFrameResource.mSloganBorderSize), 0, 0,
                mFrame.mSloganBorderColor);
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                mSloganView.requestFocus();
//            }
//        });
    }
    private void layoutDateTag(Rect boxLayout, Rect pictureLayout, String dateText) {
        Paint paint = new Paint();
        paint.setColor(mFrame.mSloganFontColor);
        paint.setTextSize(mResourceRatio * mFrameResource.mTagFontSize);
        paint.setStrokeWidth(2.0f);
        if(mFrame.mFont != null)
            paint.setTypeface(mFrame.mFont.getTypeface());
        else
            paint.setTypeface(Typeface.DEFAULT);

        FontMetrics fm = paint.getFontMetrics();
        Log.e(TAG, "date layout ascent=" + fm.ascent + " descent=" + fm.descent + " top=" + fm.top + " bottom=" + fm.bottom + " leading=" + fm.leading);
        float textWidth = paint.measureText(dateText);
        float textHeight = fm.descent - fm.ascent;
        float bottomHeight = boxLayout.bottom - pictureLayout.bottom;
        Rect layout = new Rect();
        layout.left = pictureLayout.right - (int)Math.ceil(textWidth) - 2 * alpha;
        layout.top = pictureLayout.bottom + ((int) Math.ceil(bottomHeight) / 2) - (int)Math.ceil(textHeight);
        layout.right = pictureLayout.right - alpha;
        layout.bottom = pictureLayout.bottom + ((int) Math.ceil(bottomHeight) / 2);

        mDateView.layout(layout.left, layout.top, layout.right, layout.bottom);
        mDateView.setPadding(0, 0, 0, 0);
        Log.e(TAG, "date layout left=" + layout.left + " top=" + layout.top + " right=" + layout.right + " bottom=" + layout.bottom);
        mDateView.setTextString(dateText);
        mDateView.setTextPaint(paint);
    }

    private void layoutLocationTag(Rect boxLayout, Rect pictureLayout, String dateText) {
        Paint paint = new Paint();
        paint.setColor(mFrame.mSloganFontColor);
        paint.setTextSize(mResourceRatio * mFrameResource.mTagFontSize);
        paint.setStrokeWidth(2.0f);
        if(mFrame.mFont != null)
            paint.setTypeface(mFrame.mFont.getTypeface());
        else
            paint.setTypeface(Typeface.DEFAULT);

        FontMetrics fm = paint.getFontMetrics();
        Log.e(TAG, "location layout ascent=" + fm.ascent + " descent=" + fm.descent + " top=" + fm.top + " bottom=" + fm.bottom + " leading=" + fm.leading);
        float textWidth = paint.measureText(dateText);
        float textHeight = fm.descent - fm.ascent;
        float bottomHeight = boxLayout.bottom - pictureLayout.bottom;

        Rect layout = new Rect();
        layout.left = pictureLayout.right - (int)Math.ceil(textWidth) - 2 * alpha;
        layout.right = pictureLayout.right - alpha;
        if((mTagStatus & PolaroidActivity.TAG_DATE) > 0) {
            layout.top = pictureLayout.bottom + ((int) Math.ceil(bottomHeight) / 2);
            layout.bottom = pictureLayout.bottom + ((int) Math.ceil(bottomHeight) / 2) + (int)Math.ceil(textHeight);
        } else {
            layout.top = pictureLayout.bottom + ((int) Math.ceil(bottomHeight) / 2) - (int)Math.ceil(textHeight);
            layout.bottom = pictureLayout.bottom + ((int) Math.ceil(bottomHeight) / 2);
        }

        mLocationView.layout(layout.left, layout.top, layout.right, layout.bottom);
        mLocationView.setPadding(0, 0, 0, 0);
        Log.e(TAG, "location layout left=" + layout.left + " top=" + layout.top + " right=" + layout.right + " bottom=" + layout.bottom);
        mLocationView.setTextString(dateText);
        mLocationView.setTextPaint(paint);
    }

    private void calculateSloganFont(Editable s) {
        if(s.length() == 0) return;

        float textSize = Utils.autofitTextSize(s, mSloganPaint,
                mSloganLayout.width(), mSloganLayout.height(),
                Layout.Alignment.ALIGN_NORMAL,
                mResourceRatio * mFrameResource.mSloganFontSize);
        mSloganView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    private void applyFont(Font font) {
        Log.d(Poladroid.TAG, "PolaroidView.applyFont(" + font + ")");
        Log.d(Poladroid.TAG, "Baseline before: " + mSloganView.getBaseline());
        if (font != null) {
            mSloganView.setTypeface(font.getTypeface());
            mSloganPaint.setTypeface(font.getTypeface());
        } else {
            mSloganView.setTypeface(Typeface.DEFAULT);
            mSloganPaint.setTypeface(Typeface.DEFAULT);
        }
        mSloganView.scrollTo(0, 0);
        Log.d(Poladroid.TAG, "Baseline after: " + mSloganView.getBaseline());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(Poladroid.TAG, "PolaroidView.onSizeChanged(new: " + w + "x" + h + ", old:" + oldw + "x"
                + oldh + ")");
    }

    private static void recycleImageView(String name, ImageView imageView) {
        try {
            if (imageView != null) {
                Drawable drawable = imageView.getDrawable();
                if (BitmapDrawable.class.isInstance(drawable)) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    if (bitmap != null) {
                        Log.d(Poladroid.TAG, "PolaroidView.recycleImageView(" + name + ")");
                        bitmap.recycle();
                    }
                }

                imageView.setImageResource(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Only call this when DELETING the view
    // and when you are sure that NONE of the BitmapDrawables is shared with
    // anybody else
    public void recycle() {
        Log.d(Poladroid.TAG, "PolaroidView.recycle(){");
        recycleImageView("mFgFrameView", mFgFrameView);
        recycleImageView("mBgFrameView", mBgFrameView);
        recycleImageView("mPictureView", mPictureView);
        Log.d(Poladroid.TAG, "} PolaroidView.recycle()");
    }

    void applyFrame(boolean fromLayout) {
        Log.d(Poladroid.TAG, "PolaroidView.applyFrame(" + mFrame + "){");
        if (mFrame != null) {
            applyFont(mFrame.mFont);
        } else {
            applyFont(null);
        }
        if (!fromLayout) {
            updateLayout(true); // This will update mFrameResource
            updateTagLayout();
        }
        if (mFrame != null && mFrameResource != null) {

            mFgFrameView.setImageResource(mFrameResource.mFgResId);
            mBgFrameView.setImageResource(mFrameResource.mBgResId);
        } else {
            Log.w(Poladroid.TAG, "*** Could not find resources for specified frame");
            mFrame = null;
            mFgFrameView.setImageResource(0);
            mBgFrameView.setImageResource(0);
        }
        Log.d(Poladroid.TAG, "} PolaroidView.applyFrame()");
    }

    public void setFrame(Frame frame) {
        if (null == frame) {
            Log.i(Poladroid.TAG, "*** PolaroidView.setFrame(" + frame + ")");
        }

        if (frame != mFrame) {
            mFrame = frame;

            applyFrame(false);
        }
    }

    // Compute target picture resolution for specified frame,
    // assuming the view layout will remain the same
    // This is for the activity to start background decoding
    // with the right outResolution while the frame hasn't been set in the
    // PolaroidView yet
    public static Point computePictureResolution(Rect layout, Frame frame) {
        Log.d(Poladroid.TAG, "PolaroidView.computePictureResolution(" + frame + "){");

        int targetWidth = Math.max(0, layout.right - layout.left);
        int targetHeight = Math.max(0, layout.bottom - layout.top);
        // Ignore padding for time being (TODO)
        FrameResource frameResource = frame.pickBestResourceForTarget(targetWidth, targetHeight);
        // NOT supposed to be null
        Log.d(Poladroid.TAG, "Best frame resource: " + frameResource);

        float resourceRatio = getResourceRatio(layout, frameResource);
        Rect boxLayout = getBoxLayout(layout, frameResource, resourceRatio);

        Rect pictureLayout = getPictureLayout(boxLayout, frameResource);
        Point pictureResolution = new Point(pictureLayout.width(), pictureLayout.height());

        Log.d(Poladroid.TAG, "} PolaroidView.computePictureResolution(" + frame + ") => "
                + pictureResolution);

        return pictureResolution;
    }

    public Rect getLayout() {
        return new Rect(mLayout);
    }

    Point getPictureResolution() {
        return new Point(mPictureView.getWidth(), mPictureView.getHeight());
    }

    public void setSlogan(String string) {
        mSloganView.setText(string);
        mSloganView.setSelection(string.length());
        Log.d(TAG, "setSlogan" + " LENGTH:" + string.length());
    }

    public String getSlogan() {
        return mSloganView.getText().toString();
    }

    public void setSloganEnable(boolean enable) {
        if (mSloganView != null) {
            if (enable) {
                mSloganView.setEnabled(true);
            } else {
                mSloganView.setEnabled(false);
            }
        }
    }

    public void setDateTag(String string) {
        mDateTagText = string;
    }

    public String getDateTag() {
        return mDateTagText;
    }

    public void setLocationTag(String string) {
        mLocationTagText = string;
    }

    public String getLocationTag() {
        return mLocationTagText;
    }

    public void setPicture(BitmapDrawable pictureDrawable) {
        Log.d(Poladroid.TAG, "PolaroidView.setPicture()");
        if (pictureDrawable != null) {
            pictureDrawable.setDither(true);
        }
        mPictureView.setImageDrawable(pictureDrawable);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
        case (MotionEvent.ACTION_DOWN):
            mOldX = event.getX();
            mState = State.STARTSWIPE;
            break;

        case (MotionEvent.ACTION_UP):
        case (MotionEvent.ACTION_CANCEL):
            mState = State.STOPSWIPE;
            break;

        case (MotionEvent.ACTION_MOVE):
            if (mState == State.STARTSWIPE) {
                float deltaX = event.getX() - mOldX;
                Log.i(TAG, "ACTION_MOVE deltaX:" + deltaX);
                if (deltaX >= 100) {
                    // right swipe
                    Log.i(TAG, "ACTION_MOVE MSG_SWIP_LEFT deltaX:" + deltaX);
                    if (mMainHandler != null) {
                        // mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_SWIP_LEFT));
                    }
                    mState = State.SWIPING;
                } else if (deltaX <= -100) {
                    // left swipe
                    Log.i(TAG, "ACTION_MOVE MSG_SWIP_RIGHT deltaX:" + deltaX);
                    if (mMainHandler != null) {
                        // mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_SWIP_RIGHT));
                    }
                    mState = State.SWIPING;
                }
            }
            break;
        default:
            break;
        }
        return touchEnable;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.d(TAG, "beforeTextChanged" + " s=" + " start=" + start + " count=" + count + " after=" + after);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d(TAG, "onTextChanged" + " s=" + " start=" + start + " before=" + before + " count=" + count);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-29,PR1051698 begin
        int index = (s.length() == start) ? start - 1 : start;
        if (s.length() > 0 && s.charAt(index) == '\n' && mSloganView != null) {
            Log.i(TAG, "onTextChanged has enter input.");
            String str = s.toString().replace(String.valueOf('\n'), "");
            mSloganView.setText(str);
            mSloganView.setSelection(index);
            if (mKeyboardManager != null) {
                mKeyboardManager.hideSoftInputFromWindow(mSloganView.getWindowToken(), 0);
            }
        }
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-29,PR1051698 end
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-18,PR1025898 begin
        if (mSaveStateListener != null) {
            mSaveStateListener.updateSaveState();
        }
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-06-18,PR1025898 end
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.e(TAG, "afterTextChanged");
        if(mSloganView.getVisibility() == VISIBLE
                && s == mSloganView.getText()) //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-10, PR541179
            calculateSloganFont(s);
    }
}
/* EOF */


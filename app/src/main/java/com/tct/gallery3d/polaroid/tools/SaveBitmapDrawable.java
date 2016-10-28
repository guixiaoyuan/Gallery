package com.tct.gallery3d.polaroid.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.tct.gallery3d.R;
import com.tct.gallery3d.exif.ExifInterface;
import com.tct.gallery3d.exif.ExifTag;
import com.tct.gallery3d.polaroid.Poladroid;
import com.tct.gallery3d.polaroid.PolaroidActivity;
import com.tct.gallery3d.polaroid.config.FilterConfig;
import com.tct.gallery3d.polaroid.config.PolaroidConfig;
import com.tct.gallery3d.polaroid.manager.Frame;
import com.tct.gallery3d.polaroid.manager.FrameManager;
import com.tct.gallery3d.polaroid.manager.FrameResource;
import com.tct.gallery3d.polaroid.view.PolaroidView;

public class SaveBitmapDrawable extends AsyncTask<Void, Void, String> {

    private static final String TAG = "SaveBitmapDrawable";
    private Context mContext;
    private FilterConfig mFilterConfig;
    private PolaroidConfig mPolaroidConfig;
    private BitmapDrawable mInDrawable;
    private String mSlogan;
    private Handler mHandler;
    PolaroidView polaroidSaveView = null;

    public SaveBitmapDrawable(Context context, FilterConfig filterConfig,
            PolaroidConfig polaroidConfig, BitmapDrawable inDrawable, String slogan, Handler handler) {
        this.mContext = context;
        this.mFilterConfig = filterConfig;
        this.mPolaroidConfig = polaroidConfig;
        this.mInDrawable = inDrawable;
        this.mSlogan = slogan;
        this.mHandler = handler;
    }

    protected String doInBackground(Void... params) {
        String outFilename = null;

        Log.d(Poladroid.TAG, "SaveBitmapDrawable.doInBackground(){");
        Bitmap outBitmap = null;
        polaroidSaveView = null;
        try {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.polaroid_save, null);
            Frame frame = FrameManager.getFrame(mPolaroidConfig.mFrameName);
            polaroidSaveView = (PolaroidView) view.findViewById(R.id.polaroid);
            polaroidSaveView.setTouchEnable(false);

            Point savingResolution = getSavingResolution(mInDrawable, frame);
            polaroidSaveView.setFrame(frame);
            polaroidSaveView.layout(0, 0, savingResolution.x, savingResolution.y);

            polaroidSaveView.setPicture(mFilterConfig.mOutDrawable);

            polaroidSaveView.setSlogan(mPolaroidConfig.mSlogan);
            polaroidSaveView.setDateTag(mPolaroidConfig.mDateTag);
            polaroidSaveView.setLocationTag(mPolaroidConfig.mLocationTag);

            polaroidSaveView.displayTagView(mPolaroidConfig.mTagStatus);

            View boxView = polaroidSaveView.findViewById(R.id.box);
            int outWidth = boxView.getWidth();
            int outHeight = boxView.getHeight();
            outBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);

            Canvas outCanvas = new Canvas(outBitmap);
            boxView.draw(outCanvas);

            ExifInterface exif = getPolaroidExifInfo(outFilename);
            outFilename = Utils.saveBitmap(
                    mContext,
                    outBitmap,
                    "polaroid_" + Utils.getGregorianString() + "Poladroid"
                            + mFilterConfig.mFilter.getName(), Bitmap.CompressFormat.JPEG,
                    Utils.DEFAULT_JPEG_QUALITY, exif);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outBitmap != null && !outBitmap.isRecycled()) {
                outBitmap.recycle();
                outBitmap = null;
            }
            Log.d(Poladroid.TAG, "} SaveBitmapDrawable.doInBackground()");
        }

        return outFilename;
    }

    private ExifInterface getPolaroidExifInfo(String file) {
        ExifInterface exif = new ExifInterface();
        ExifTag tag = exif.buildTag(ExifInterface.TAG_SOFTWARE,
                PolaroidActivity.EXIF_TAG_SOFTWARE_VALUE);
        exif.setTag(tag);
        return exif;
    }

    protected void onPostExecute(String outFilename) {
        Log.d(Poladroid.TAG, "SaveBitmapDrawable.onPostExecute(){");
        mHandler.sendMessage(mHandler.obtainMessage(PolaroidActivity.MSG_SAVE_IMAGE_COMPLETED,
                outFilename));
        Log.d(Poladroid.TAG, "} SaveBitmapDrawable.onPostExecute()");
    }

    private static Point getSavingResolution(BitmapDrawable drawable, Frame frame) {
        Point resolution = new Point();

        // For selected frame, pick the best resource
        // suitable for our full resolution input image
        int pictureWidth = drawable.getIntrinsicWidth();
        int pictureHeight = drawable.getIntrinsicHeight();
        FrameResource frameResource = frame.pickBestResourceForPicture(pictureWidth, pictureHeight);
        // NOT supposed to be null
        Log.d(Poladroid.TAG, "Best frame resource: " + frameResource);

        // Set the size of our polaroid view so that
        // the internal picture view will be more or less equal
        // to our full resolution input image
        resolution.x = frameResource.mTargetResolution.x * pictureWidth
                / frameResource.mPictureLocation.width();
        resolution.y = frameResource.mTargetResolution.y * pictureHeight
                / frameResource.mPictureLocation.height();

        return resolution;
    }

    public void recycle() {
        if (polaroidSaveView != null) {
            polaroidSaveView.recycle();
            polaroidSaveView = null;
        }
    }
}
